/*
 * AuthenticationFilter.java created on 04.09.2003
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
package mir.core.ui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import mir.core.model.MirUser;
import mir.core.ui.servlet.*;

/**
 * AuthenticationFilter
 * @author idefix
 * @version $Id: AuthenticationFilter.java,v 1.2 2003/09/07 16:55:00 idfx Exp $
 */
public class AuthenticationFilter implements Filter {
	private FilterConfig _filterConfig;
	
	/**
	 * 
	 */
	public AuthenticationFilter() {
		super();
	}

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(final FilterConfig filterConfig) 
		throws ServletException {
		_filterConfig = filterConfig;
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest, 
		ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest)servletRequest;	
		String requestUri = request.getRequestURI();
		
		if(requestUri != null 
			&& requestUri.startsWith(request.getContextPath() + "/admin")
			&& requestUri.indexOf("logon") == -1){
			//check if authenticated, only if in admin-module
			HttpSession httpSession = request.getSession();
			MirUser mirUser = 
				(MirUser)httpSession.getAttribute(ServletConstants.USER);
			
			if(mirUser == null){
				//user is not authorized to access
				//set redirect attributes that the user comes to place he wants to be
				httpSession.setAttribute(ServletConstants.REDIRECT_ACTION, requestUri);
				httpSession.setAttribute(ServletConstants.REDIRECT_QUERY_STRING, 
					request.getQueryString());
				
				//send user to logon-page
				servletRequest.getRequestDispatcher("/admin/logon.do")
					.forward(servletRequest, servletResponse);
			} 
		} 
		 
		filterChain.doFilter(servletRequest, servletResponse);
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		_filterConfig = null;
	}

}
