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
 *  ServletModuleHidden - output of so called "censored" articles
 *  @author mh
 *  @version $Id
 *
 */

public class ServletModuleHidden extends ServletModule
{

	// Singelton / Kontruktor
	private static ServletModuleHidden instance = new ServletModuleHidden();
	public static ServletModule getInstance() { return instance; }

	private ServletModuleHidden() {
		theLog = Logfile.getInstance(MirConfig.getProp("Home") + MirConfig.getProp("ServletModule.Hidden.Logfile"));
		templateListString = MirConfig.getProp("ServletModule.Hidden.ListTemplate");
		try {
			mainModule = new ModuleContent(DatabaseContent.getInstance());
		}
		catch (StorageObjectException e) {
			theLog.printError("servletmoduleHidden could not be initialized");
		}
	}


	public void list(HttpServletRequest req, HttpServletResponse res)
		throws ServletModuleException
	{
			// Parameter auswerten
			SimpleHash mergeData = new SimpleHash();
      String query_year = req.getParameter("year"); 
      String query_month = req.getParameter("month"); 
      String order = "webdb_create";

			// sql basteln
      String whereClause = "is_published=false AND webdb_create LIKE '"+
                            query_year+"-"+query_month+"%'";

			theLog.printDebugInfo("sql-whereclause: " + whereClause);

			// fetch und ausliefern
			try {

				if ((query_year!=null && !query_year.equals("")) 
            && (query_month!=null && !query_month.equals(""))) {
          EntityList theList = mainModule.getByWhereClause(whereClause, order, -1);
					if (theList!=null && theList.size()>0) {

						//make articleHash
						StringBuffer buf= new StringBuffer("id in (");boolean first=true;
						for(int i=0;i<theList.size();i++) {
							if (first==false) buf.append(",");
							first=false;
							buf.append(theList.elementAt(i).getValue("to_media"));
						}
						buf.append(")");
						SimpleHash articleHash =
                HTMLTemplateProcessor.makeSimpleHash(
                 mainModule.getByWhereClause(buf.toString(),-1));
						mergeData.put("articleHash", articleHash);

            // send the year and month for use in the list template
            mergeData.put("year", query_year);
            mergeData.put("month", query_month);
						// get comment
						mergeData.put("contentlist",theList);
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
