/*
 * TemplateServlet.java created on 01.09.2003
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
package mir.core.ui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mir.config.MirPropertiesConfiguration;
import mir.servlet.ServletModuleExc;
import mir.util.GeneratorDateTimeFunctions;
import mir.util.GeneratorExpressionFunctions;
import mir.util.GeneratorHTMLFunctions;
import mir.util.GeneratorIntegerFunctions;
import mir.util.GeneratorListFunctions;
import mir.util.GeneratorRegularExpressionFunctions;
import mir.util.GeneratorStringFunctions;
import mir.util.ResourceBundleGeneratorFunction;
import mir.util.StringRoutines;
import mircoders.servlet.ServletHelper;

import org.apache.struts.util.MessageResources;

/**
 * TemplateServlet
 * @author idefix
 * @version $Id: TemplateServlet.java,v 1.2 2003/09/07 16:55:00 idfx Exp $
 */
public class TemplateServlet extends HttpServlet {

	/**
	 * 
	 */
	public TemplateServlet() {
		super();
	}
	
	
	
	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected final void doGet(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		process(request, response);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected final void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		process(request, response);
	}
	
	/**
	 * Processes the request and invokes the presentation
	 * @param request
	 * @param response
	 */
	protected void process(HttpServletRequest request, HttpServletResponse response) 
		throws IOException, ServletException {
		try {
			Map requestData = new HashMap();
			Enumeration keys = request.getAttributeNames();
			while(keys.hasMoreElements()){
				final String key = (String)keys.nextElement();
				requestData.put(key, request.getAttribute(key));
			}
			requestData.putAll(request.getParameterMap());
			
			Map sessionData = new HashMap();
			HttpSession httpSession = request.getSession();
			keys = httpSession.getAttributeNames();
			while(keys.hasMoreElements()){
				final String key = (String)keys.nextElement();
				sessionData.put(key, httpSession.getAttribute(key));
			}
			
			Map applicationData = new HashMap();
			ServletContext servletContext = getServletContext();
			keys = servletContext.getAttributeNames();
			while(keys.hasMoreElements()){
				final String key = (String)keys.nextElement();
				applicationData.put(key, servletContext.getAttribute(key));
			}		
			
			Map utilityMap = new HashMap();
			utilityMap.put("compressWhitespace", 
				new freemarker.template.utility.CompressWhitespace());
			utilityMap.put("encodeHTML", 
				new GeneratorHTMLFunctions.encodeHTMLGeneratorFunction());
			utilityMap.put("encodeXML", 
				new GeneratorHTMLFunctions.encodeXMLGeneratorFunction());
			utilityMap.put("encodeURI", 
				new GeneratorHTMLFunctions.encodeURIGeneratorFunction());
			utilityMap.put("subString", 
				new GeneratorStringFunctions.subStringFunction());
			utilityMap.put("subList", 
				new GeneratorListFunctions.subListFunction());
			utilityMap.put("isOdd", 
				new GeneratorIntegerFunctions.isOddFunction());
			utilityMap.put("increment", 
				new GeneratorIntegerFunctions.incrementFunction());
			utilityMap.put("evaluate", 
				new GeneratorExpressionFunctions.evaluateExpressionFunction());
			utilityMap.put("constructString", 
				new GeneratorStringFunctions.constructStructuredStringFunction());
			utilityMap.put("escapeJDBCString", 
				new GeneratorStringFunctions.jdbcStringEscapeFunction());
			utilityMap.put("regexpreplace", 
				new GeneratorRegularExpressionFunctions.regularExpressionReplaceFunction());
			utilityMap.put("datetime", 
				new GeneratorDateTimeFunctions.DateTimeFunctions(
					MirPropertiesConfiguration.instance().getString("Mir.DefaultTimezone")));
			utilityMap.put("encodeLink", 
				new GeneratorHTMLFunctions.encodeLinksGeneratorFunction(response));

			Map configData = MirPropertiesConfiguration.instance().allSettings();
			configData.put("docRoot", request.getContextPath());
			configData.put("now", new Date());
			
			//administeroperations
			List administerOperations = new Vector();
			String settings[] = MirPropertiesConfiguration.instance().getStringArray("Mir.Localizer.Admin.AdministerOperations");
			if (settings!=null) {
				for (int i = 0; i < settings.length; i++) {
					String setting = settings[i].trim();

					if (setting.length() > 0) {
						List parts = StringRoutines.splitString(setting, ":");
						if (parts.size() != 2) {
//							logger.error("config error: " + settings[i] + ", 2 parts expected");
						}
						else {
							Map entry = new HashMap();
							entry.put("name", (String) parts.get(0));
							entry.put("url", (String) parts.get(1));
							administerOperations.add(entry);
						}
					}
				}
			}
						
			Map templateData = new HashMap();
			templateData.put("request", requestData);
			templateData.put("session", sessionData);
			templateData.put("application", applicationData);
			templateData.put("utility", utilityMap);
			templateData.put("config", configData);
			templateData.put("administeroperations", administerOperations);
			
			String templateName = generateTemplateString(request);
			
			Locale[] locales = new Locale[2];
			locales[0] = request.getLocale();
			locales[1] = request.getLocale();
		
			//write the servlet
			PrintWriter printWriter = response.getWriter();
			generateResponse(printWriter, templateData, templateName, locales);
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}
	
	private String generateTemplateString(HttpServletRequest request){
		String returnString = request.getServletPath();
		returnString = returnString.substring(0, returnString.indexOf("."));
		return returnString + ".tmpl";
	}
	
	private void generateResponse(PrintWriter printWriter, Map map, 
		String templateString, Locale[] locales) 
		throws ServletModuleExc{
		if(templateString.startsWith("/admin")){
			map.put( "lang",
					new ResourceBundleGeneratorFunction( locales,
						 new MessageResources[] { 
						 		MessageResources.getMessageResources("bundles.admin"),
								MessageResources.getMessageResources("bundles.adminlocal")
								}));
								
			templateString = templateString.substring(templateString.lastIndexOf('/'));
			ServletHelper.generateResponse(printWriter, map, templateString);	
		}
		if(templateString.startsWith("/open")){
			map.put( "lang",
				new ResourceBundleGeneratorFunction( locales,
					new MessageResources[] { 
						MessageResources.getMessageResources("bundles.open"),
						MessageResources.getMessageResources("bundles.open")
					}));
								
			templateString = templateString.substring(templateString.lastIndexOf('/'));
			ServletHelper.generateOpenPostingResponse(printWriter, map, templateString);	
		}		
	}

	/**
	 * @see javax.servlet.Servlet#destroy()
	 */
	public void destroy() {
		super.destroy();
	}

	/**
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		MirPropertiesConfiguration.setContext(servletConfig.getServletContext());
		try {
			MirPropertiesConfiguration.instance();
		}
		catch (Throwable t) {
			throw new ServletException("can't read configuration: " + t.toString());
		}
	}
}
