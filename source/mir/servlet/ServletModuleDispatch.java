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

package  mir.servlet;

import  java.lang.reflect.*;
import  javax.servlet.http.*;
import  mir.servlet.ServletModuleException;
import  mir.misc.*;
import  mir.log.*;


/**
 * Dispatcher, calls the method passed to ServletModule Class, through the "do"
 * Parameter (via POST or GET)
 *
 * @version $Id: ServletModuleDispatch.java,v 1.8 2002/10/25 03:25:15 zapata Exp $
 *
 * @Author rk
 *
 */
public final class ServletModuleDispatch {

  private static LoggerWrapper logger;
  private static final Class[] SIGNATURE = { HttpServletRequest.class, HttpServletResponse.class };

  static {
    logger = new LoggerWrapper("servlet.dispatch");
  }

 /**
  * private constructor to prevent unwanted instantiation;
  */

  private ServletModuleDispatch () {
  }

        /**
         *  Die Dispatch-Routine ruft das von dem Hauptservlet kommende ServletModule
         *  mit dem per HttpServletRequest angegebenen Paramter <code>do</code> auf.
         *  Ist kein Parameter angegeben, so wird versucht, in die <code>defaultAction</code>
         *  des ServletModules zu springen.
         *
         * @param req Http-Request, das vom Dispatcher an die Methode des
         *    ServletModules durchgereicht wird
         * @param res Http-Response, die vom Dispatcher an die Methode des
         *    ServletModules durchgereicht wird
         * @param sMod ServletModule, an das dispatched wird.
         * @param mod Name des Modules als String (für Logfile)
         */

  public static void dispatch(ServletModule sMod, HttpServletRequest req,
                              HttpServletResponse res) throws ServletModuleException, ServletModuleUserException
  {
    //sMod.predeliver(req,res);

    String doParam = req.getParameter("do");
    logger.info("ServletModuleDispatch: " + sMod.toString() + " with method " + doParam);
    if (doParam == null) {
      if (sMod.defaultAction() != null)
        doParam = sMod.defaultAction();
      else
        throw new ServletModuleException("no parameter do supplied!");
    }

    try {
      Method method = sMod.getClass().getMethod(doParam,SIGNATURE);
      if (method != null) {
        method.invoke(sMod,new Object[] {req,res} );
        return;
      }
      else logger.debug("method lookup unsuccesful");
    }
    catch ( NoSuchMethodException e) {
      throw new ServletModuleException("no such method '"+doParam+"' (" + e.getMessage() + ")");
    }
    catch ( SecurityException e) {
      throw new ServletModuleException("method not allowed!" + e.getMessage());
    }
    catch ( InvocationTargetException e) {
      System.out.println(e.getMessage());
      if (e.getTargetException() instanceof ServletModuleUserException) {
        throw new ServletModuleUserException(e.getTargetException().getMessage());
      }
      else {
        e.printStackTrace();
        throw new ServletModuleException(e.getTargetException().getMessage());
      }
    }
    catch ( IllegalAccessException e) {
      throw new ServletModuleException("illegal method not allowed!" + e.getMessage());
    }

//hopefully we don't get here ...
    throw new ServletModuleException("delivery failed! -- ");
  }
}
