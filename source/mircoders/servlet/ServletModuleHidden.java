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
package mircoders.servlet;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.entity.adapter.EntityIteratorAdapter;
import mir.log.LoggerWrapper;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.storage.StorageObjectFailure;
import mir.util.CachingRewindableIterator;
import mir.util.HTTPRequestParser;
import mir.util.JDBCStringRoutines;
import mircoders.global.MirGlobal;
import mircoders.module.ModuleContent;
import mircoders.storage.DatabaseContent;

/*
 *  ServletModuleHidden - output of so called "censored" articles
 *  @author mh
 *  @version $Id
 *
 */

public class ServletModuleHidden extends ServletModule
{
  private static ServletModuleHidden instance = new ServletModuleHidden();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleHidden() {
    super();

    logger = new LoggerWrapper("ServletModule.Hidden");

    try {
      mainModule = new ModuleContent(DatabaseContent.getInstance());
    }
    catch (StorageObjectFailure e) {
      logger.error("initialization of servletmoduleHidden failed: " + e.getMessage());
    }
  }


  public void list(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc
  {
// determine parameter
    HTTPRequestParser requestParser = new HTTPRequestParser(req);
    Map responseData = ServletHelper.makeGenerationData(req, res, new Locale[] { getLocale(req), getFallbackLocale(req)});

    String query_year = requestParser.getParameter("year");
    String query_month = requestParser.getParameter("month");

    try {
      if ((query_year!=null && !query_year.equals("")) && (query_month!=null && !query_month.equals(""))) {
        String whereClause = "is_published=false AND webdb_create LIKE "+
            "'"+JDBCStringRoutines.escapeStringLiteral(query_year)+"-"+JDBCStringRoutines.escapeStringLiteral(query_month)+"%'";

        Iterator articleList =
            new CachingRewindableIterator(
              new EntityIteratorAdapter( whereClause, "webdb_create", 100,
                 MirGlobal.localizer().dataModel().adapterModel(), "content", -1, 0)
        );

        responseData.put("year", query_year);
        responseData.put("month", query_month);
        responseData.put("articles", articleList);
      }

      ServletHelper.generateResponse(res.getWriter(), responseData, listGenerator);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }
}
