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

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mir.servlet.AbstractServlet;
import mir.servlet.ServletModuleDispatch;
import mir.servlet.ServletModuleUserExc;
import mir.util.ExceptionFunctions;
import mircoders.global.MirGlobal;
import mircoders.servlet.ServletModuleOpenIndy;

/**
 *  OpenMir.java - main servlet for open posting and comment feature to articles
 *
 *  @author RK 1999-2001, the mir-coders group
 *  @version $Id: OpenMir.java,v 1.31 2003/04/09 02:06:06 zapata Exp $
 *
 */


public class OpenMir extends AbstractServlet {
  public void process(HttpServletRequest aRequest, HttpServletResponse aResponse)
        throws ServletException, IOException {
                if ((configuration.getString("RootUri") == null) ||
                        configuration.getString("RootUri").equals("")) {
                  configuration.setProperty("RootUri", aRequest.getContextPath());
                }

    long startTime = System.currentTimeMillis();
    long sessionConnectTime=0;

    HttpSession session = aRequest.getSession();

    checkLanguage(session, aRequest);

    //make sure client browsers don't cache anything
    setNoCaching(aResponse);

    aResponse.setContentType("text/html");
    //aResponse.setContentType("text/html; charset="+MirPropertiesConfiguration.instance().getString("Mir.DefaultHTMLCharset"));

    try {
      ServletModuleDispatch.dispatch(ServletModuleOpenIndy.getInstance(), aRequest, aResponse);
    }
    catch (Throwable e) {
      Throwable cause = ExceptionFunctions.traceCauseException(e);

      if (cause instanceof ServletModuleUserExc)
        handleUserError(aRequest, aResponse, aResponse.getWriter(), (ServletModuleUserExc) cause);
      else
        handleError(aRequest, aResponse, aResponse.getWriter(), cause);
    }

    sessionConnectTime = System.currentTimeMillis() - startTime;
    logger.debug("EXECTIME (ServletModuleOpenIndy): " + sessionConnectTime + " ms");
  }

  private void handleUserError(HttpServletRequest aRequest, HttpServletResponse aResponse,
                               PrintWriter out, ServletModuleUserExc anException) {
    ((ServletModuleOpenIndy) ServletModuleOpenIndy.getInstance()).handleUserError(aRequest, aResponse, out, anException);
  }

  private void handleError(HttpServletRequest aRequest, HttpServletResponse aResponse,PrintWriter out, Throwable anException) {
    ((ServletModuleOpenIndy) ServletModuleOpenIndy.getInstance()).handleError(aRequest, aResponse, out, anException);
  }

  public String getServletInfo(){
    return "OpenMir "+configuration.getString("Mir.Version");
  }


  /**
   * Selects the language for the response.
   *
   * @param session
   * @param aRequest
   */
  private void checkLanguage(HttpSession aSession, HttpServletRequest aRequest) {
    String requestLanguage = aRequest.getParameter("language");
    String sessionLanguage = (String) aSession.getAttribute("language");
    String acceptLanguage = aRequest.getLocale().getLanguage();
    String defaultLanguage = MirGlobal.config().getString("Mir.Login.DefaultLanguage", "en");

    logger.debug(" requestlanguage = " + requestLanguage + ", sessionLanugage = " + sessionLanguage +
                 ", acceptLanguage = " + acceptLanguage + ", defaultLanguage = " + defaultLanguage);

    String language = requestLanguage;

    if (language==null)
      language = sessionLanguage;

    if (language==null)
      language = acceptLanguage;

    if (language==null)
      language = defaultLanguage;

    setLanguage(aSession, language);
  }
}

