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
package  mir.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.log.LoggerWrapper;


/**
 * Dispatcher, calls the method passed to ServletModule Class, through the "do"
 * Parameter (via POST or GET)
 *
 * @version $Id: ServletModuleDispatch.java,v 1.15 2003/04/21 12:42:50 idfx Exp $
 *
 * @Author rk
 *
 */
public final class ServletModuleDispatch {

  private static LoggerWrapper logger = new LoggerWrapper("ServletModule.Dispatch");
  private static final Class[] SIGNATURE = { HttpServletRequest.class, HttpServletResponse.class };

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
   * @param mod Name des Modules als String (f?r Logfile)
   */

  public static void dispatch(ServletModule sMod, HttpServletRequest req,
          HttpServletResponse res) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure
  {
    String doParam = req.getParameter("do");
    logger.info("ServletModuleDispatch: " + sMod.toString() + " with method " + doParam);
    if (doParam == null) {
      if (sMod.defaultAction() != null)
        doParam = sMod.defaultAction();
      else
        throw new ServletModuleExc("no parameter do supplied!");
    }

    try {
      Method method = sMod.getClass().getMethod(doParam,SIGNATURE);
      if (method != null) {
        method.invoke(sMod,new Object[] {req,res} );
        return;
      }
      else logger.debug("method lookup unsuccesful");
    }
    catch ( InvocationTargetException e) {
      logger.error( "invocation target exception: " + e.getMessage());
      e.getTargetException().printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

      throw new ServletModuleFailure(e.getTargetException().getMessage(), e.getTargetException());
    }
    catch (Throwable t) {
      logger.error( "ServletModuleDispatch: " + t.getMessage());
      t.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
      throw new ServletModuleFailure(t);
    }
  }
}
