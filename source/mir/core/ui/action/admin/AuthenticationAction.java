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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.core.model.MirUser;
import mir.core.service.storage.UserService;
import mir.core.ui.action.DispatchAction;
import mir.core.ui.servlet.ServletConstants;
import mir.util.StringRoutines;
import mircoders.global.MirGlobal;
import multex.Failure;
import net.sf.hibernate.SessionFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.action.RedirectingActionForward;
import org.apache.struts.util.MessageResources;

/**
 * AuthenticationAction
 * @author idefix
 * @version $Id: AuthenticationAction.java,v 1.2 2003/09/10 20:58:27 idfx Exp $
 */
public class AuthenticationAction extends DispatchAction {
	private MirPropertiesConfiguration _configuration;
	
	public AuthenticationAction(){
		try {
			_configuration = MirPropertiesConfiguration.instance();
		} catch (PropertiesConfigExc e) {
			throw new Failure("could not load config", e);
		}
	}
	
	private ActionForward logon(ActionMapping actionMapping, ActionForm actionForm, 
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		
		//retrieve the form input data				
		DynaActionForm form = (DynaActionForm)actionForm;
		String login = (String) form.get("login");
		String password = (String) form.get("password");
	
		if((login == null || login.trim().length() == 0) 
			&& (password == null || password.trim().length() == 0)){
			//no input --> error
			return actionMapping.findForward("failed");
		}
		
		//access to persitence layer
		ServletContext context = this.getServlet().getServletContext();
		UserService userService = 
			new UserService((SessionFactory)context
				.getAttribute(ServletConstants.SESSION_FACTORY));
		setLoginLanguages(context);
		
		//try to retrieve user for the given values	
		MirUser mirUser = userService.loadUser(login, password);
		if(mirUser == null){
			//not existent user --> error
			return actionMapping.findForward("failed");
		}
		
		//add user to the session context
		HttpSession session = request.getSession();
		session.setAttribute(ServletConstants.USER, mirUser);
		
		//check if we have to redirect the user
		String requestUri = 
			(String) session.getAttribute(ServletConstants.REDIRECT_ACTION);
		
		if(requestUri != null && requestUri.indexOf("index") == -1 
			&& requestUri.indexOf("logon") == -1){
			
			// we have to redirect the user
			requestUri = requestUri.substring(requestUri.lastIndexOf("/"));

			//retrieve the correct action name
			String queryString = 
				(String) session.getAttribute(ServletConstants.REDIRECT_QUERY_STRING);
			if(queryString != null){
				requestUri = requestUri + "?" +queryString;
			}
			
			//construct a redirect
			RedirectingActionForward actionForward = 
				new RedirectingActionForward(requestUri);
			
			//return the redirect
			return actionForward;
		}
		
		//normal redirect to the startpage
		return actionMapping.findForward("success");	
	}
	
	private ActionForward logoff(ActionMapping actionMapping, ActionForm actionForm, 
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {
		
		//kill session
		HttpSession session = request.getSession();
		session.setAttribute(ServletConstants.USER, null);
		session.invalidate();
		
		//return to the logon form
		return actionMapping.findForward("index");
	}
	
	private void setLoginLanguages(ServletContext context) throws ServletException {
		List loginLanguages = 
			(List) context.getAttribute(ServletConstants.LOGIN_LANGUAGES);	
		try {
			if (loginLanguages == null) {
				MessageResources messageResources =
					MessageResources.getMessageResources("bundles.adminlocal");
				MessageResources messageResources2 =
					MessageResources.getMessageResources("bundles.admin");

				List languages =
					StringRoutines.splitString(
						MirGlobal.config().getString("Mir.Login.Languages", "en"), ";");

				loginLanguages = new ArrayList();
				Iterator i = languages.iterator();
				while (i.hasNext()) {
					String code = (String) i.next();
					Locale locale = new Locale(code, "");
					String name = messageResources.getMessage(locale, "languagename");

					if (name == null) {
						name = messageResources2.getMessage(locale, "languagename");
					}

					if (name == null) {
						name = code;
					}

					Map record = new HashMap();
					record.put("name", name);
					record.put("code", code);
					loginLanguages.add(record);
				}
				context.setAttribute(ServletConstants.LOGIN_LANGUAGES, 
					loginLanguages);
				context.setAttribute(ServletConstants.DEFAULT_LANGUAGE, 
					_configuration.getString("Mir.Login.DefaultLanguage"));
			}
		} catch (Throwable t) {
			throw new ServletException(t.getMessage());
		}
	}
}
