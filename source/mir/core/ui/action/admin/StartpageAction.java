/*
 * AuthenticationAction.java created on 05.09.2003
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
package mir.core.ui.action.admin;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.core.service.storage.MessageService;
import mir.core.ui.servlet.ServletConstants;
import net.sf.hibernate.SessionFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * AuthenticationAction
 * @author idefix
 * @version $Id: StartpageAction.java,v 1.1 2003/09/07 16:55:00 idfx Exp $
 */
public class StartpageAction extends Action {
	
	/**
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, 
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {

		String param = actionMapping.getParameter();
		if(param.equals("index")){
			return index(actionMapping, actionForm, request, response);	
		}
		return null;
	}
	
	private ActionForward index(ActionMapping actionMapping, ActionForm actionForm, 
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		
		ServletContext context = this.getServlet().getServletContext();
		MessageService messageService = 
			new MessageService((SessionFactory)context
				.getAttribute(ServletConstants.SESSION_FACTORY));
		List messages = messageService.list(0);
		
		request.setAttribute("messages", messages);
		return actionMapping.findForward("success");	
	}
}
