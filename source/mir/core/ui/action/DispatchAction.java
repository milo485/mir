/*
 * DispatchAction.java
 * 
 * Copyright (C) 2001, 2002, 2003 The Mir-coders group
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
package mir.core.ui.action;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/**
 * DispatchAction<br>
 * This Action dispatches a call of execute() to a method with a name defined
 * as parameter in the action mapping. To use this Action you have to define a 
 * action mapping in the following way:<br>
 * 
 * <code><pre>
 * <action-mappings>
 *   <action
 *      parameter="logon"
 *      path="/logon"
 *      type="my.action.MyAction"
 *   ... 
 *   />
 *   </action> 
 * <action-mappings>
 * </pre></code><br>
 * If <code>logon.do</code> is called by the user the method <code>logon()</code>
 * of the class MyAction is called.
 * 
 * 
 * @version $Id: DispatchAction.java,v 1.1 2003/09/10 20:58:27 idfx Exp $
 * @author idefix
 */
public class DispatchAction extends Action {

	/**
	 * Dispatches to a method with a name defined as parameter of the action mapping.
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, 
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		String parameter = actionMapping.getParameter();
		Method method = getClass().getDeclaredMethod(parameter, 
			new Class[]{ActionMapping.class, ActionForm.class,
			HttpServletRequest.class, HttpServletResponse.class});
		method.setAccessible(true);
		return (ActionForward) method.invoke(
				this, new Object[]{actionMapping, actionForm, request, response});
	}
}
