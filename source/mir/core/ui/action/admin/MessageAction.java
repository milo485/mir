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

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.core.service.storage.MessageService;
import mir.core.ui.servlet.ServletConstants;

import multex.Failure;

import net.sf.hibernate.SessionFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * AuthenticationAction
 * @author idefix
 * @version $Id: MessageAction.java,v 1.1 2003/09/07 16:55:00 idfx Exp $
 */
public class MessageAction extends Action {
	private MirPropertiesConfiguration _configuration;

	public MessageAction(){
		try {
			_configuration = MirPropertiesConfiguration.instance();
		} catch (PropertiesConfigExc e) {
			throw new Failure("could not load config", e);
		}		
	}
	
	/**
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, 
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {

		String param = actionMapping.getParameter();
		if(param.equals("list")){
			return list(actionMapping, actionForm, request, response);	
		}
		return null;
	}
	
	private ActionForward list(ActionMapping actionMapping, ActionForm actionForm, 
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		//retrieve parameters
		String offsetString = request.getParameter(ServletConstants.OFFSET);
		int offset = 0;
		if(offsetString != null){
			offset = new Integer(offsetString).intValue();
		}
		
		//access to persistence
		ServletContext context = getServlet().getServletContext();
		MessageService messageService = 
			new MessageService((SessionFactory)context
				.getAttribute(ServletConstants.SESSION_FACTORY));
		
		//retrieve entities
		List messages = messageService.list(offset);
		
		//configure the data to send to view
		int listSize = _configuration.getInt("ServletModule.Default.ListSize");
		Integer lastOffset;
		if(offset-listSize < 0){
			lastOffset = new Integer(0);
		} else {
			lastOffset = new Integer(offset-listSize);
		}
		request.setAttribute(ServletConstants.LAST_OFFSET, lastOffset);
		request.setAttribute(ServletConstants.NEXT_OFFSET, 
			new Integer(offset + listSize));
		request.setAttribute(ServletConstants.OFFSET, 
			new Integer(offset));
		request.setAttribute("messages", messages);
		
		//show the view
		return actionMapping.findForward("success");	
	}
}
