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

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

import freemarker.template.*;

import mir.misc.*;
import mir.servlet.*;

import mircoders.servlet.*;
import mircoders.module.*;
import mircoders.entity.*;
import mircoders.storage.*;

/**
 *  OpenMir.java - main servlet for open posting and comment feature to articles
 *
 *  @author RK 1999-2001, the mir-coders group
 *  @version $Id: OpenMir.java,v 1.17 2002/12/23 03:12:46 mh Exp $
 *
 */


public class OpenMir extends AbstractServlet {

  //private static boolean                confed=false;
  private static String lang;
  public HttpSession session;

  public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    doPost(req,res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {

    long            startTime = (new java.util.Date()).getTime();
    long            sessionConnectTime=0;

    // get the configration - this could conflict if 2 mirs are in the
    // VM maybe? to be checked. -mh
    // -- they would have different servlet contexts, so the following is
    // no problem (br1)
    if(getServletContext().getAttribute("mir.confed") == null) {
      getConfig(req);
    }
      
    session = req.getSession();

    if(session.getAttribute("Language")==null){
      if (req.getParameter("language")!=null) {
        setLanguage(session, req.getParameter("language"));
      }
      else {
        setLanguage(session, getAcceptLanguage(req));
      }
    }

    if (req.getParameter("language")!=null)
      setLocale(session, new Locale(req.getParameter("language"), "") );

    //make sure client browsers don't cache anything
    setNoCaching(res);

    res.setContentType("text/html; charset="
                      +MirConfig.getProp("Mir.DefaultHTMLCharset"));
    try {
      ServletModuleDispatch.dispatch(ServletModuleOpenIndy.getInstance(),req,res);
    }
    catch (ServletModuleUserException e) {
      handleUserError(req,res,res.getWriter(), e.getMessage());
    }
    catch (ServletModuleException e){
      e.printStackTrace();
      handleError(req,res,res.getWriter(), "OpenIndy :: ServletException in Module ServletModule -- " + e.getMessage());
    }
    // timing...
    sessionConnectTime = new java.util.Date().getTime() - startTime;
    theLog.printInfo("EXECTIME (ServletModuleOpenIndy): " + sessionConnectTime + " ms");
  }

  private void handleUserError(HttpServletRequest req, HttpServletResponse res,
                               PrintWriter out, String errorString) {
    try {
      theLog.printError(errorString);
      SimpleHash modelRoot = new SimpleHash();
      modelRoot.put("errorstring", new SimpleScalar(errorString));
      modelRoot.put("date", new SimpleScalar(StringUtil.date2readableDateTime(new GregorianCalendar())));
      HTMLTemplateProcessor.process(res,MirConfig.getProp("Mir.UserErrorTemplate"),
                                    modelRoot, out, req.getLocale() );
      out.close();
    }
    catch (Exception e) {
      System.err.println("Error in UserErrorTemplate");
    }

  }

  private void handleError(HttpServletRequest req, HttpServletResponse res,PrintWriter out, String errorString) {

    try {
      theLog.printError(errorString);
      SimpleHash modelRoot = new SimpleHash();
      modelRoot.put("errorstring", new SimpleScalar(errorString));
      modelRoot.put("date", new SimpleScalar(StringUtil.date2readableDateTime(
                                               new GregorianCalendar())));
      HTMLTemplateProcessor.process(res,MirConfig.getProp("Mir.ErrorTemplate"),
                                    modelRoot,out, req.getLocale());
      out.close();
    }
    catch (Exception e) {
      System.err.println("Error in ErrorTemplate");
    }

  }

  public String getServletInfo(){
    return "OpenMir "+MirConfig.getProp("Mir.Version");
  }

}

