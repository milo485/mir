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


/**
 * Dispatcher, calls the method passed to ServletModule Class, through the "do"
 * Parameter (via POST or GET)
 *
 * @version $Id: ServletModuleDispatch.java,v 1.6.4.1 2002/09/01 21:31:41 mh Exp $
 *
 * @Author rk
 *
 */
public final class ServletModuleDispatch {

	private static Logfile theLog;
	private static final    Class[] SIGNATURE =
														{ HttpServletRequest.class, HttpServletResponse.class };


	static {
		theLog = Logfile.getInstance("/tmp/smod.dispatch");
	}

	/**
	 * privater Konstruktor, um versehentliche Instantiierung zu verhindern
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
			theLog.printInfo("SerletModuleDispatch: " + sMod.toString() + " with method " + doParam);
			if (doParam == null) {
				if (sMod.defaultAction() != null) doParam = sMod.defaultAction();
				else throw new ServletModuleException("no parameter do supplied!");
			}

			try {
				Method method = sMod.getClass().getMethod(doParam,SIGNATURE);
				if (method != null) {
					method.invoke(sMod,new Object[] {req,res} );
					return;
				}
				else theLog.printDebugInfo("method lookup unsuccesful");
			}
			catch ( NoSuchMethodException e) { throw new ServletModuleException("no such method!" + e.toString());}
			catch ( SecurityException e) { throw new ServletModuleException("method not allowed!" + e.toString());}
			catch ( InvocationTargetException e) {
				if (e.getTargetException().getClass().getName().equals("mir.servlet.ServletModuleUserException")) {
						throw new ServletModuleUserException(((ServletModuleUserException)e.getTargetException()).getMsg());
				} else {
						e.printStackTrace();
						throw new ServletModuleException(e.getTargetException().toString());
				}
			}
			catch ( IllegalAccessException e) { throw new ServletModuleException("illegal method not allowed!" + e.toString());}

			//hopefully we don't get here ...
			throw new ServletModuleException("delivery failed! -- ");
	}
}
