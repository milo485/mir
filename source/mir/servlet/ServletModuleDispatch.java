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
 * @version $Id: ServletModuleDispatch.java,v 1.15.2.1 2003/09/03 17:49:38 zapata Exp $
 *
 * @Author rk
 *
 */
public final class ServletModuleDispatch {

  private static LoggerWrapper logger = new LoggerWrapper("ServletModule.Dispatch");
  private static final Class[] SIGNATURE = { HttpServletRequest.class, HttpServletResponse.class };

  /**
   * private parameter-less constructor to prevent unwanted instantiation
   */

  private ServletModuleDispatch () {
  }

  /**
   * Method to dispatch servletmodule requests.
   *
   * @param aServletModule
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */

  public static void dispatch(ServletModule aServletModule, HttpServletRequest aRequest,
       HttpServletResponse aResponse) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure
  {
    String doParam = aRequest.getParameter("do");
    logger.info("ServletModuleDispatch: " + aServletModule.toString() + " with method " + doParam);
    if (doParam == null) {
      if (aServletModule.defaultAction() != null)
        doParam = aServletModule.defaultAction();
      else
        throw new ServletModuleExc("no parameter do supplied!");
    }

    try {
      Method method = aServletModule.getClass().getMethod(doParam,SIGNATURE);
      if (method != null) {
        method.invoke(aServletModule,new Object[] {aRequest,aResponse} );
        return;
      }
      else logger.debug("method lookup unsuccesful");
    }
    catch ( InvocationTargetException e) {
      logger.error( "invocation target exception: " + e.toString());
      e.getTargetException().printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

      throw new ServletModuleFailure(e.getTargetException().getMessage(), e.getTargetException());
    }
    catch (Throwable t) {
      logger.error( "ServletModuleDispatch: " + t.toString());
      throw new ServletModuleFailure(t);
    }
  }
}
