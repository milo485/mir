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
 * the code of this program with  any library licensed under the Apache Software License,
 * The Sun (tm) Java Advanced Imaging library (JAI), The Sun JIMI library
 * (or with modified versions of the above that use the same license as the above),
 * and distribute linked combinations including the two.  You must obey the
 * GNU General Public License in all respects for all of the code used other than
 * the above mentioned libraries.  If you modify this file, you may extend this
 * exception to your version of the file, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.util.MessageResources;
import mir.config.MirPropertiesConfiguration;
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
import mircoders.servlet.ServletHelper;
import mircoders.storage.DatabaseUsers;




/**
 * Mir.java - main servlet, that dispatches to servletmodules
 *
 * @author $Author: zapata $
 * @version $Id: Mir.java,v 1.50 2003/09/03 18:29:01 zapata Exp $
 *
 */
public class Mir extends AbstractServlet {
  private static ModuleUsers usersModule = null;
  private static ModuleMessage messageModule = null;
  private final static Map servletModuleInstanceHash = new HashMap();
  private static Locale fallbackLocale = null;

  private static List loginLanguages = null;

  protected List getLoginLanguages() throws ServletException {
    synchronized (Mir.class) {
      try {
        if (loginLanguages == null) {
          MessageResources messageResources =
            MessageResources.getMessageResources("bundles.adminlocal");
          MessageResources messageResources2 =
            MessageResources.getMessageResources("bundles.admin");

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

        return loginLanguages;
      }
      catch (Throwable t) {
        throw new ServletException(t.getMessage());
      }
    }
  }

  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    usersModule = new ModuleUsers(DatabaseUsers.getInstance());
  }

  protected String getDefaultLanguage(HttpServletRequest aRequest) {
    String defaultlanguage =
      MirGlobal.config().getString("Mir.Login.DefaultLanguage", "");

    if (defaultlanguage.length() == 0) {
      Locale locale = aRequest.getLocale();
      defaultlanguage = locale.getLanguage();
    }

    return defaultlanguage;
  }

  protected synchronized Locale getFallbackLocale() throws ServletException {
    try {
      if (fallbackLocale == null) {
        fallbackLocale = new Locale(MirPropertiesConfiguration.instance().getString("Mir.Admin.FallbackLanguage", "en"), "");
      }
    }
    catch (Throwable t) {
      throw new ServletException(t.getMessage());
    }

    return fallbackLocale;
  }

  public EntityUsers checkCredentials(HttpServletRequest aRequest) throws ServletException {
    try {
      EntityUsers user = ServletHelper.getUser(aRequest);

      String username = aRequest.getParameter("login");
      String password = aRequest.getParameter("password");

      if (username != null && password != null) {
        user = usersModule.getUserForLogin(username, password);


        ServletHelper.setUser(aRequest, user);
      }

      return user;
    }
    catch (Throwable t) {
      t.printStackTrace();

      throw new ServletException(t.toString());
    }
  }

  public void process(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletException, IOException, UnavailableException {
    try {
      long startTime = System.currentTimeMillis();
      long sessionConnectTime = 0;

      HttpSession session = aRequest.getSession(true);
      setNoCaching(aResponse);
      Locale locale = new Locale(getDefaultLanguage(aRequest), "");
      aResponse.setContentType("text/html; charset=" +
                               configuration.
                               getString("Mir.DefaultHTMLCharset", "UTF-8"));

      EntityUsers userEntity = checkCredentials(aRequest);

      if (userEntity == null) {
        String queryString = aRequest.getQueryString();

        if ( (queryString != null) && (queryString.length() != 0) && session.getAttribute("login.target") == null &&
             (aRequest.getParameter("module")==null ||
              (!aRequest.getParameter("module").equals("login") && !aRequest.getParameter("module").equals("logout")))) {
          session.setAttribute("login.target", queryString);
        }

        _sendLoginPage(aResponse, aRequest);
      }
      else {
        String moduleName = aRequest.getParameter("module");
        checkLanguage(session, aRequest);

        if ( ( (moduleName == null) || moduleName.equals(""))) {
          moduleName="Admin";
        }


        if (moduleName.equals("login")) {
          String target = (String) session.getAttribute("login.target");

          if (target != null) {
            ServletHelper.redirect(aResponse, target);
          }
          else {
            ServletHelper.redirect(aResponse, "");
          }
        }
        else if (moduleName.equals("logout")) {
          logger.info(userEntity.getValue("login") + " has logged out");
          session.invalidate();
          _sendLoginPage(aResponse, aRequest);
          return;
        }
        else {
          try {
            ServletModule servletModule = getServletModuleForName(moduleName);
            ServletModuleDispatch.dispatch(servletModule, aRequest, aResponse);

            sessionConnectTime = System.currentTimeMillis() - startTime;
            logger.info("EXECTIME (" + moduleName + "): " + sessionConnectTime + " ms");
          }
          catch (Throwable e) {
            Throwable cause = ExceptionFunctions.traceCauseException(e);

            if (cause instanceof ServletModuleUserExc)
              handleUserError(aRequest, aResponse, (ServletModuleUserExc) cause);
            else
              handleError(aRequest, aResponse, cause);
          }

          if (aRequest.getParameter("killsession")!=null)
            aRequest.getSession().invalidate();
        }
      }
    }
    catch (Throwable t) {
      t.printStackTrace();

      throw new ServletException(t.toString());
    }
  }

  /**
   * caching routine to get a module for a module name
   *
   * @param moduleName the module name
   * @return the requested module
   * @throws ServletModuleExc
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
        }
        catch (ClassNotFoundException e) {
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

  private void handleUserError(HttpServletRequest aRequest, HttpServletResponse aResponse, ServletModuleUserExc anException) {
    try {
      logger.info("user error: " + anException.getMessage());

      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] {getLocale(aRequest), getFallbackLocale()});

      MessageResources messages = MessageResources.getMessageResources("bundles.admin");
      responseData.put("errorstring", messages.getMessage(getLocale(aRequest), anException.getMessage(), anException.getParameters()));
      responseData.put("date", new GregorianCalendar().getTime());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, MirPropertiesConfiguration.instance().getString("Mir.UserErrorTemplate"));
    }
    catch (Throwable e) {
      logger.error("Error handling user error" + e.toString());
    }
  }

  private void handleError(HttpServletRequest aRequest, HttpServletResponse aResponse, Throwable anException) {
    try {
      logger.error("error: " + anException);

      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] {getLocale(aRequest), getFallbackLocale()});

      responseData.put("errorstring", anException.toString());
      responseData.put("date", new GregorianCalendar().getTime());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, MirPropertiesConfiguration.instance().getString("Mir.ErrorTemplate"));
    }
    catch (Throwable e) {
      logger.error("Error handling error: " + e.toString());
    }
  }

  // Redirect-methods
  private void _sendLoginPage(HttpServletResponse aResponse, HttpServletRequest aRequest) {
    String loginTemplate = configuration.getString("Mir.LoginTemplate");

    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] {getLocale(aRequest), getFallbackLocale()});

      responseData.put("defaultlanguage", getDefaultLanguage(aRequest));
      responseData.put("languages", getLoginLanguages());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, loginTemplate);
    }
    catch (Throwable e) {
      handleError(aRequest, aResponse, e);
    }
  }

  public String getServletInfo() {
    return "Mir " + configuration.getString("Mir.Version");
  }
}
