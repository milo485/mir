/*
 * Copyright (C) 2001, 2002 The Mir-coders group
 *
 * This file is part of Mir.
 *
 * Mir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Mir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, The Mir-coders gives permission to link
 * the code of this program with the com.oreilly.servlet library, any library
 * licensed under the Apache Software License, The Sun (tm) Java Advanced
 * Imaging library (JAI), The Sun JIMI library (or with modified versions of
 * the above that use the same license as the above), and distribute linked
 * combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than the above
 * mentioned libraries.  If you modify this file, you may extend this exception
 * to your version of the file, but you are not obligated to do so.  If you do
 * not wish to do so, delete this exception statement from your version.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.util.MessageResources;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleList;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import mir.config.MirPropertiesConfiguration;
import mir.generator.FreemarkerGenerator;
import mir.log.LoggerWrapper;
import mir.misc.HTMLTemplateProcessor;
import mir.misc.StringUtil;
import mir.servlet.AbstractServlet;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleDispatch;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleUserExc;
import mir.util.ExceptionFunctions;
import mir.util.StringRoutines;
import mircoders.entity.EntityUsers;
import mircoders.global.MirGlobal;
import mircoders.module.ModuleMessage;
import mircoders.module.ModuleUsers;
import mircoders.storage.DatabaseArticleType;
import mircoders.storage.DatabaseMessages;
import mircoders.storage.DatabaseUsers;




/**
 * Mir.java - main servlet, that dispatches to servletmodules
 *
 * @author $Author: zapata $
 * @version $Id: Mir.java,v 1.41 2003/03/17 20:47:03 zapata Exp $
 *
 */
public class Mir extends AbstractServlet {
  private static ModuleUsers usersModule = null;
  private static ModuleMessage messageModule = null;
  private final static Map servletModuleInstanceHash = new HashMap();

  //I don't know about making this static cause it removes the
  //possibility to change the config on the fly.. -mh
  private static List loginLanguages = null;
  public HttpSession session;

  protected TemplateModel getLoginLanguages() throws ServletException {
    synchronized (Mir.class) {
      try {
        if (loginLanguages == null) {
          MessageResources messageResources2 =
            MessageResources.getMessageResources("bundles.admin");
          MessageResources messageResources =
            MessageResources.getMessageResources("bundles.adminlocal");
          List languages =
            StringRoutines.splitString(MirGlobal.config().getString("Mir.Login.Languages", "en"), ";");

          loginLanguages = new Vector();

          Iterator i = languages.iterator();

          while (i.hasNext()) {
            String code = (String) i.next();
            Locale locale = new Locale(code, "");
            String name = messageResources.getMessage(locale, "languagename");

            if (name == null) {
              name = messageResources2.getMessage(locale, "languagename");
            }

            if (name == null) {
              name = code;
            }

            Map record = new HashMap();
            record.put("name", name);
            record.put("code", code);
            loginLanguages.add(record);
          }
        }

        return FreemarkerGenerator.makeAdapter(loginLanguages);
      }
      catch (Throwable t) {
        throw new ServletException(t.getMessage());
      }
    }
  }

  // FIXME: this should probalby go into AbstractServlet so it can be used in
  // OpenMir as well -mh
  protected String getDefaultLanguage(HttpServletRequest aRequest) {
    String defaultlanguage =
      MirGlobal.config().getString("Mir.Login.DefaultLanguage", "");

    if (defaultlanguage.length() == 0) {
      Locale locale = aRequest.getLocale();
      defaultlanguage = locale.getLanguage();
    }

    return defaultlanguage;
  }

  public void process(HttpServletRequest aRequest, HttpServletResponse aResponse)
    throws ServletException, IOException, UnavailableException {
    long startTime = System.currentTimeMillis();
    long sessionConnectTime = 0;
    EntityUsers userEntity;
    String http = "";

    configuration.addProperty("ServletName", getServletName());

    session = aRequest.getSession(true);
    userEntity = (EntityUsers) session.getAttribute("login.uid");

    if (aRequest.getServerPort() == 443) {
      http = "https";
    } else {
      http = "http";
    }

    //make sure client browsers don't cache anything
    setNoCaching(aResponse);

    //FIXME: this seems kind of hackish and only here because we can have
    // default other than the one that the browser is set to.
    Locale locale = new Locale(getDefaultLanguage(aRequest), "");
    MessageResources messageResources =
      MessageResources.getMessageResources("bundles.admin");
    String htmlcharset = messageResources.getMessage(locale, "htmlcharset");

    aResponse.setContentType("text/html; charset=" + htmlcharset);

    String moduleName = aRequest.getParameter("module");
    checkLanguage(session, aRequest);

    /** @todo for cleanup and readability this should be moved to
     *  method loginIfNecessary() */
    if ((moduleName != null) && moduleName.equals("direct")) {
      //...
    }

    // Authentication
    if (((moduleName != null) && moduleName.equals("login")) ||
        (userEntity == null)) {
      String user = aRequest.getParameter("login");
      String passwd = aRequest.getParameter("password");
      logger.debug("--login: evaluating for user: " + user);
      userEntity = allowedUser(user, passwd);

      if (userEntity == null) {
        // login failed: redirecting to login
        logger.warn("--login: failed!");
        _sendLoginPage(aResponse, aRequest, aResponse.getWriter());

        return;
      } else if ((moduleName != null) && moduleName.equals("login")) {
        // login successful
        logger.info("--login: successful! setting uid: " + userEntity.getId());
        session.setAttribute("login.uid", userEntity);
        logger.debug("--login: trying to retrieve login.target");

        String target = (String) session.getAttribute("login.target");

        if (target != null) {
          logger.debug("Redirect: " + target);

          int serverPort = aRequest.getServerPort();
          String redirect = "";
          String redirectString = "";

          if (serverPort == 80) {
            redirect =
              aResponse.encodeURL(http + "://" + aRequest.getServerName() + target);
            redirectString =
              "<html><head><meta http-equiv=refresh content=\"1;URL=" +
              redirect + "\"></head><body>going <a href=\"" + redirect +
              "\">Mir</a></body></html>";
          } else {
            redirect =
              aResponse.encodeURL(http + "://" + aRequest.getServerName() + ":" +
                aRequest.getServerPort() + target);
            redirectString =
              "<html><head><meta http-equiv=refresh content=\"1;URL=" +
              redirect + "\"></head><body>going <a href=\"" + redirect +
              "\">Mir</a></body></html>";
          }

          aResponse.getWriter().println(redirectString);

          //aResponse.sendRedirect(redirect);
        } else {
          // redirecting to default target
          logger.debug("--login: no target - redirecting to default");
          _sendStartPage(aResponse, aRequest, aResponse.getWriter(), userEntity);
        }

        return;
      }
       // if login succesful
    }
     // if login

    if ((moduleName != null) && moduleName.equals("logout")) {
      logger.info("--logout");
      session.invalidate();

      //session = aRequest.getSession(true);
      //checkLanguage(session, aRequest);
      _sendLoginPage(aResponse, aRequest, aResponse.getWriter());

      return;
    }

    // Check if authed!
    if (userEntity == null) {
      // redirect to loginpage
      String redirectString = aRequest.getRequestURI();
      String queryString = aRequest.getQueryString();

      if ((queryString != null) && !queryString.equals("")) {
        redirectString += ("?" + aRequest.getQueryString());
        logger.debug("STORING: " + redirectString);
        session.setAttribute("login.target", redirectString);
      }

      _sendLoginPage(aResponse, aRequest, aResponse.getWriter());

      return;
    }

    // If no module is specified goto standard startpage
    if ((moduleName == null) || moduleName.equals("")) {
      logger.debug("no module: redirect to standardpage");
      _sendStartPage(aResponse, aRequest, aResponse.getWriter(), userEntity);

      return;
    }

    // end of auth
    // From now on regular dispatching...
    try {
      // get servletmodule by parameter and continue with dispacher
      ServletModule smod = getServletModuleForName(moduleName);
      ServletModuleDispatch.dispatch(smod, aRequest, aResponse);
    }
    catch (Throwable e) {
      Throwable cause = ExceptionFunctions.traceCauseException(e);

      if (cause instanceof ServletModuleUserExc)
        handleUserError(aRequest, aResponse, aResponse.getWriter(), (ServletModuleUserExc) cause);
      else
        handleError(aRequest, aResponse, aResponse.getWriter(), cause);

    }

    // timing...
    sessionConnectTime = System.currentTimeMillis() - startTime;
    logger.info("EXECTIME (" + moduleName + "): " + sessionConnectTime + " ms");
  }

  /**
   *  Private method getServletModuleForName returns ServletModule
   *  from Cache
   *
   * @param moduleName
   * @return ServletModule
   *
   */
  private static ServletModule getServletModuleForName(String moduleName) throws ServletModuleExc {
    // Instance in Map ?
    if (!servletModuleInstanceHash.containsKey(moduleName)) {
      // was not found in hash...
      try {
        Class theServletModuleClass = null;

        try {
          // first we try to get ServletModule from stern.che3.servlet
          theServletModuleClass =
            Class.forName("mircoders.servlet.ServletModule" + moduleName);
        } catch (ClassNotFoundException e) {
          // on failure, we try to get it from lib-layer
          theServletModuleClass =
            Class.forName("mir.servlet.ServletModule" + moduleName);
        }

        Method m = theServletModuleClass.getMethod("getInstance", null);
        ServletModule smod = (ServletModule) m.invoke(null, null);

        // we put it into map for further reference
        servletModuleInstanceHash.put(moduleName, smod);

        return smod;
      }
      catch (Exception e) {
        throw new ServletModuleExc("*** error resolving classname for " + moduleName + " -- " + e.getMessage());
      }
    }
    else {
      return (ServletModule) servletModuleInstanceHash.get(moduleName);
    }
  }

  private void handleUserError(HttpServletRequest aRequest, HttpServletResponse aResponse,
                               PrintWriter out, ServletModuleUserExc anException) {
    try {
      logger.info("user error: " + anException.getMessage());
      SimpleHash modelRoot = new SimpleHash();
      MessageResources messages = MessageResources.getMessageResources("bundles.admin");
      modelRoot.put("errorstring",
          new SimpleScalar(
              messages.getMessage(getLocale(aRequest), anException.getMessage(), anException.getParameters())
          ));
      modelRoot.put("date", new SimpleScalar(StringUtil.date2readableDateTime(new GregorianCalendar())));
      HTMLTemplateProcessor.process(
          aResponse,MirPropertiesConfiguration.instance().getString("Mir.UserErrorTemplate"),
          modelRoot, out, getLocale(aRequest));
      out.close();
    }
    catch (Exception e) {
      logger.error("Error in UserErrorTemplate");
    }

  }

  private void handleError(HttpServletRequest aRequest, HttpServletResponse aResponse,PrintWriter out, Throwable anException) {

    try {
      logger.error("error: " + anException);
      SimpleHash modelRoot = new SimpleHash();
      modelRoot.put("errorstring", new SimpleScalar(anException.getMessage()));
      modelRoot.put("date", new SimpleScalar(StringUtil.date2readableDateTime(
                                               new GregorianCalendar())));
      HTMLTemplateProcessor.process(aResponse,MirPropertiesConfiguration.instance().getString("Mir.ErrorTemplate"),
                                    modelRoot,out, getLocale(aRequest));
      out.close();
    }
    catch (Exception e) {
      logger.error("Error in ErrorTemplate");
    }
  }

  /**
   *  evaluate login for user / password
   */
  protected EntityUsers allowedUser(String user, String password) {
    try {
      if (usersModule == null) {
        usersModule = new ModuleUsers(DatabaseUsers.getInstance());
      }

      return usersModule.getUserForLogin(user, password);
    }
    catch (Exception e) {
      logger.debug(e.getMessage());
      e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

      return null;
    }
  }

  // Redirect-methods
  private void _sendLoginPage(HttpServletResponse aResponse, HttpServletRequest aRequest,
    PrintWriter out) {
    String loginTemplate = configuration.getString("Mir.LoginTemplate");
    String sessionUrl = aResponse.encodeURL("");

    try {
      SimpleHash mergeData = new SimpleHash();
      SimpleList languages = new SimpleList();

      mergeData.put("session", sessionUrl);

      mergeData.put("defaultlanguage", getDefaultLanguage(aRequest));
      mergeData.put("languages", getLoginLanguages());

      HTMLTemplateProcessor.process(aResponse, loginTemplate, mergeData, out,
        getLocale(aRequest));
    }
    catch (Throwable e) {
      handleError(aRequest, aResponse, out, e);
    }
  }

  private void _sendStartPage(HttpServletResponse aResponse, HttpServletRequest aRequest,
    PrintWriter out, EntityUsers userEntity) {
    String startTemplate = "start_admin.template";
    String sessionUrl = aResponse.encodeURL("");

    try {
      // merge with logged in user and messages
      SimpleHash mergeData = new SimpleHash();
      mergeData.put("session", sessionUrl);
      mergeData.put("login_user", userEntity);

      if (messageModule == null) {
        messageModule = new ModuleMessage(DatabaseMessages.getInstance());
      }

      mergeData.put("messages",
        messageModule.getByWhereClause(null, "webdb_create desc", 0, 10));

      mergeData.put("articletypes",
        DatabaseArticleType.getInstance().selectByWhereClause("", "id", 0, 20));

      HTMLTemplateProcessor.process(aResponse, startTemplate, mergeData, out,
        getLocale(aRequest));
    }
    catch (Exception e) {
      e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
      handleError(aRequest, aResponse, out, e);
    }
  }

  public String getServletInfo() {
    return "Mir " + configuration.getString("Mir.Version");
  }

  private void checkLanguage(HttpSession session, HttpServletRequest aRequest) {
    // a lang parameter always sets the language
    String lang = aRequest.getParameter("language");

    if (lang != null) {
      logger.info("selected language " + lang + " overrides accept-language");
      setLanguage(session, lang);
    }
    // otherwise store language from accept header in session
    else if (session.getAttribute("language") == null) {
      logger.info("accept-language is " + aRequest.getLocale().getLanguage());
      setLanguage(session, aRequest.getLocale().getLanguage());
    }
  }
}
