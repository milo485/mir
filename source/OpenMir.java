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
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;

import mir.config.MirPropertiesConfiguration;
import mir.misc.HTMLTemplateProcessor;
import mir.misc.StringUtil;
import mir.servlet.AbstractServlet;
import mir.servlet.ServletModuleDispatch;
import mir.servlet.ServletModuleException;
import mir.servlet.ServletModuleUserException;
import mircoders.servlet.ServletModuleOpenIndy;

/**
 *  OpenMir.java - main servlet for open posting and comment feature to articles
 *
 *  @author RK 1999-2001, the mir-coders group
 *  @version $Id: OpenMir.java,v 1.20 2003/02/23 05:00:10 zapata Exp $
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

    long startTime = System.currentTimeMillis();
    long sessionConnectTime=0;

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

    res.setContentType("text/html");
    //res.setContentType("text/html; charset="+MirPropertiesConfiguration.instance().getString("Mir.DefaultHTMLCharset"));
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
    sessionConnectTime = System.currentTimeMillis() - startTime;
    logger.debug("EXECTIME (ServletModuleOpenIndy): " + sessionConnectTime + " ms");
  }

  private void handleUserError(HttpServletRequest req, HttpServletResponse res,
                               PrintWriter out, String errorString) {
    try {
      logger.error(errorString);
      SimpleHash modelRoot = new SimpleHash();
      modelRoot.put("errorstring", new SimpleScalar(errorString));
      modelRoot.put("date", new SimpleScalar(StringUtil.date2readableDateTime(new GregorianCalendar())));
      HTMLTemplateProcessor.process(res,MirPropertiesConfiguration.instance().getString("Mir.UserErrorTemplate"),
                                    modelRoot, out, req.getLocale() );
      out.close();
    }
    catch (Exception e) {
      logger.error("Error in UserErrorTemplate");
    }

  }

  private void handleError(HttpServletRequest req, HttpServletResponse res,PrintWriter out, String errorString) {

    try {
      logger.error(errorString);
      SimpleHash modelRoot = new SimpleHash();
      modelRoot.put("errorstring", new SimpleScalar(errorString));
      modelRoot.put("date", new SimpleScalar(StringUtil.date2readableDateTime(
                                               new GregorianCalendar())));
      HTMLTemplateProcessor.process(res,MirPropertiesConfiguration.instance().getString("Mir.ErrorTemplate"),
                                    modelRoot,out, req.getLocale());
      out.close();
    }
    catch (Exception e) {
      logger.error("Error in ErrorTemplate");
    }

  }

  public String getServletInfo(){
    return "OpenMir "+configuration.getString("Mir.Version");
  }

}

