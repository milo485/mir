/*
 * Copyright (C) 2001, 2002  The Mir-coders group
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

package mir.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codestudio.util.JDBCPool;
import com.codestudio.util.JDBCPoolMetaData;
import com.codestudio.util.SQLManager;
import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.log.LoggerWrapper;
import mir.storage.DatabaseAdaptor;

/**
 * Title:        Mir
 * Description:  Abstract servlet-class
 * Copyright:    Copyright (c) 2001, 2002
 * Company:      Mir-coders group
 * @author       idfx, the Mir-coders group
 * @version      $Id: AbstractServlet.java,v 1.28 2003/04/20 14:42:09 idfx Exp $
 */

public abstract class AbstractServlet extends HttpServlet {
  protected static String lang;
  protected LoggerWrapper logger;
  protected MirPropertiesConfiguration configuration;

  /**
   * Constructor for AbstractServlet.
   */
  public AbstractServlet() {
    super();
    logger = new LoggerWrapper("Servlet");
  }

  protected void setNoCaching(HttpServletResponse aResponse) {
    //nothing in Mir can or should be cached as it's all dynamic...
    //
    //this needs to be done here and not per page (via meta tags) as some
    //browsers have problems w/ it per-page -mh
    aResponse.setHeader("Pragma", "no-cache");
    aResponse.setDateHeader("Expires", 0);
    aResponse.setHeader("Cache-Control", "no-cache");
  }

  /**
   * Bind the language to the session
   */
  protected void setLanguage(HttpSession session, String language) {
    logger.debug("setting language to " + language);

    session.setAttribute("language", language);
    session.setAttribute("locale", new Locale(language, ""));
  }

  /**
   * Get the session-bound language
   */
  protected String getLanguage(HttpServletRequest aRequest, HttpSession session) {
    String lang = (String) session.getAttribute("language");

    if (lang == null || lang.length()==0) {
      lang = getAcceptLanguage(aRequest);
    }

    return lang;
  }

  /**
   * get the locale either from the session or the accept-language header ot the request
   * this supersedes getLanguage for the new i18n
   */
  public Locale getLocale(HttpServletRequest aRequest) {
    Locale loc = null;
    HttpSession session = aRequest.getSession(false);
    if (session != null) {
      // session can be null in case of logout
      loc = (Locale) session.getAttribute("locale");
    }
    // if there is nothing in the session get it fron the accept-language
    if (loc == null) {
      loc = aRequest.getLocale();
    }

    logger.debug("getting locale: " + loc.getLanguage());

    return loc;
  }

  /**
   * Checks the Accept-Language of the client browser.
   * If this language is available it returns its country-code,
   * else it returns the standard-language
   */
  protected String getAcceptLanguage(HttpServletRequest aRequest) {
    Locale loc = aRequest.getLocale();
    lang = loc.getLanguage();
    return lang;
  }

  /**
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    MirPropertiesConfiguration.setContext(config.getServletContext());
    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (PropertiesConfigExc e) {
      throw new ServletException(e);
    }

    String dbUser = configuration.getString("Database.Username");
    String dbPassword = configuration.getString("Database.Password");
    String dbHost = configuration.getString("Database.Host");
    String dbAdapName = configuration.getString("Database.Adaptor");
    String dbName = configuration.getString("Database.Name");

    DatabaseAdaptor adaptor;
    try {
      adaptor = (DatabaseAdaptor) Class.forName(dbAdapName).newInstance();
    }
    catch (Exception e) {
      throw new ServletException("Could not load DB adapator: " +
                                 e.toString());
    }

    String dbDriver;
    String dbUrl;
    try {
      dbDriver = adaptor.getDriver();
      dbUrl = adaptor.getURL(dbUser, dbPassword, dbHost);
    }
    catch (Exception e) {
      throw new ServletException(e);
    }

    JDBCPoolMetaData meta = new JDBCPoolMetaData();
    meta.setDbname(dbName);
    meta.setDriver(dbDriver);
    meta.setURL(dbUrl);
    meta.setUserName(dbUser);
    meta.setPassword(dbPassword);
    meta.setJNDIName("mir");
    meta.setMaximumSize(10);
    meta.setMinimumSize(1);
    meta.setPoolPreparedStatements(false);
    meta.setCacheEnabled(false);
    meta.setCacheSize(15);
    meta.setDebugging(false);
//    meta.setLogFile(dblogfile+".pool");

    SQLManager manager = SQLManager.getInstance();
    JDBCPool pool = null;
    if (manager != null) {
      pool = manager.createPool(meta);
    }
  }
  
  private void setEncoding(HttpServletRequest request){
  	try {
			Class reqClass = request.getClass();
			Method method = reqClass.getMethod("setCharacterEncoding", new Class[]{String.class});
			String encoding = configuration.getString("Mir.DefaultHTMLCharset");
			method.invoke(request, new Object[]{encoding});			
		} catch (NoSuchMethodException e) {
			// TODO set the encoding in a zapata-way
			e.printStackTrace();
			logger.error("not yes implemented: " + e.getMessage());
		} catch (SecurityException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
  }

  protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if ( (configuration.getString("RootUri") == null) ||
        configuration.getString("RootUri").equals("")) {
      configuration.setProperty("RootUri", request.getContextPath());
    }
    setEncoding(request);
    process(request, response);
  }

  abstract public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
