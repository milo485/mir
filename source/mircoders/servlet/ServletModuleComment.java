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

package mircoders.servlet;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

import freemarker.template.*;

import mir.servlet.*;
import mir.module.*;
import mir.misc.*;
import mir.entity.*;
import mir.storage.*;

import mir.entity.*;
import mircoders.storage.*;
import mircoders.module.*;

/*
 *  ServletModuleComment - controls navigation for Comments
 *
 *
 * @author RK
 */

public class ServletModuleComment extends ServletModule
{

	private ModuleContent     moduleContent;

	// Singelton / Kontruktor
	private static ServletModuleComment instance = new ServletModuleComment();
	public static ServletModule getInstance() { return instance; }

	private ServletModuleComment() {
		theLog = Logfile.getInstance(MirConfig.getProp("Home") + MirConfig.getProp("ServletModule.Comment.Logfile"));
		templateListString = MirConfig.getProp("ServletModule.Comment.ListTemplate");
		templateObjektString = MirConfig.getProp("ServletModule.Comment.ObjektTemplate");
		templateConfirmString = MirConfig.getProp("ServletModule.Comment.ConfirmTemplate");
		try {
			mainModule = new ModuleComment(DatabaseComment.getInstance());
			moduleContent = new ModuleContent(DatabaseContent.getInstance());
		}
		catch (StorageObjectException e) {
			theLog.printError("servletmodule: comment could not be initialized");
		}
	}


	public void list(HttpServletRequest req, HttpServletResponse res)
		throws ServletModuleException
	{
			// Parameter auswerten
			SimpleHash mergeData = new SimpleHash();
			String query_text = req.getParameter("query_text");
			mergeData.put("query_text",query_text);
			if (query_text!=null) mergeData.put("query_text_encoded",URLEncoder.encode(query_text));
			String query_field = req.getParameter("query_field");
			mergeData.put("query_field",query_field);
			String query_is_published = req.getParameter("query_is_published");
			mergeData.put("query_is_published",query_is_published);

			String offset = req.getParameter("offset");
			if (offset==null || offset.equals("")) offset="0";
			mergeData.put("offset",offset);

			// patching order
			String order = req.getParameter("order");
			if(order!=null) {
				mergeData.put("order", order);
				mergeData.put("order_encoded", URLEncoder.encode(order));
				if (order.equals("webdb_create")) order="webdb_create desc";
			}

			// sql basteln
			String whereClause=""; boolean isFirst=true;
			if (query_text!=null && !query_text.equalsIgnoreCase("")) {
				whereClause += "lower("+query_field+") like lower('%"+query_text+"%')"; isFirst=false;}
			if (query_is_published != null && !query_is_published.equals("")) {
				if (isFirst==false) whereClause+=" and ";
				whereClause += "is_published='"+query_is_published+"'";
				isFirst=false;
			}

			//System.out.println("sql-whereclause: " + whereClause + " order: " + order + " offset: " + offset);

			// fetch und ausliefern
			try {

				if (query_text!=null || query_is_published!=null ) {
					EntityList theList = mainModule.getByWhereClause(whereClause, order, (new Integer(offset)).intValue());
					if (theList!=null && theList.size()>0) {

						//make articleHash for comment
						StringBuffer buf= new StringBuffer("id in (");boolean first=true;
						for(int i=0;i<theList.size();i++) {
							if (first==false) buf.append(",");
							first=false;
							buf.append(theList.elementAt(i).getValue("to_media"));
						}
						buf.append(")");
						SimpleHash articleHash = HTMLTemplateProcessor.makeSimpleHash(moduleContent.getByWhereClause(buf.toString(),-1));
						mergeData.put("articleHash", articleHash);

						// get comment
						mergeData.put("contentlist",theList);
						mergeData.put("count", (new Integer(theList.getCount())).toString());
						mergeData.put("from", (new Integer(theList.getFrom())).toString());
						mergeData.put("to", (new Integer(theList.getTo())).toString());
						if (theList.hasNextBatch())
							mergeData.put("next", (new Integer(theList.getNextBatch())).toString());
						if (theList.hasPrevBatch())
							mergeData.put("prev", (new Integer(theList.getPrevBatch())).toString());
					}
				}
				// raus damit
				HTMLTemplateProcessor.process(res, templateListString, mergeData, res.getWriter(), getLocale(req));
			}
			catch (ModuleException e) {throw new ServletModuleException(e.toString());}
			catch (IOException e) {throw new ServletModuleException(e.toString());}
			catch (Exception e) {throw new ServletModuleException(e.toString());}
	}
}
