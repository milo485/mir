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

import freemarker.template.SimpleHash;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.entity.EntityList;
import mir.misc.HTMLParseException;
import mir.misc.HTMLTemplateProcessor;
import mir.misc.Logfile;
import mir.misc.MirConfig;
import mir.module.ModuleException;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleException;
import mir.storage.StorageObjectException;
import mir.log.*;

import mircoders.module.ModuleLanguage;
import mircoders.module.ModuleLinksImcs;
import mircoders.storage.DatabaseLanguage;
import mircoders.storage.DatabaseLinksImcs;

/*
 *  ServletModuleLinksImcs -
 *  liefert HTML fuer LinksImcs
 *
 *
 * @author RK
 */

public class ServletModuleLinksImcs extends ServletModule {
  private ModuleLanguage languageModule;

  // Singelton / Kontruktor
  private static ServletModuleLinksImcs instance = new ServletModuleLinksImcs();

  public static ServletModule getInstance() {
    return instance;
  }


  private ServletModuleLinksImcs() {
    logger = new LoggerWrapper("ServletModule.LinksImcs");
    templateListString = MirConfig.getProp("ServletModule.LinksImcs.ListTemplate");
    templateObjektString = MirConfig.getProp("ServletModule.LinksImcs.ObjektTemplate");
    templateConfirmString = MirConfig.getProp("ServletModule.LinksImcs.ConfirmTemplate");

    try {
      mainModule = new ModuleLinksImcs(DatabaseLinksImcs.getInstance());
      languageModule = new ModuleLanguage(DatabaseLanguage.getInstance());
    }
    catch (StorageObjectException e) {
      logger.error("Initialization of ServletModuleLinksImcs failed!: " + e.getMessage());
    }
  }

  public void add(HttpServletRequest req, HttpServletResponse res)
          throws ServletModuleException {
    try {
      SimpleHash modelRoot = new SimpleHash();
      EntityList theParentList;
      EntityList theLanguageList;
      int offset = 0;

      theParentList = mainModule.getByWhereClause("to_parent_id=NULL", "title", offset, 1000);
      theLanguageList = languageModule.getByWhereClause(null, "name", 0);

      modelRoot.put("new", "1");
      modelRoot.put("parentlist", theParentList);
      modelRoot.put("languagelist", theLanguageList);

      if (theParentList == null || theParentList.getCount() == 0 || theParentList.getCount() > 1) {
        HTMLTemplateProcessor.process(res, templateObjektString, modelRoot, res.getWriter(), getLocale(req));
      }
      else {
        deliver(req, res, modelRoot, templateObjektString);
      }

    }
    catch (ModuleException e) {
      throw new ServletModuleException(e.toString());
    }
    catch (HTMLParseException e) {
      throw new ServletModuleException(e.toString());
    }
    catch (IOException e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void list(HttpServletRequest req, HttpServletResponse res)
          throws ServletModuleException {
    try {

      SimpleHash modelRoot = new SimpleHash();
      EntityList theParentList;
      EntityList theImcsList;
      EntityList theLanguageList;
      String offsetParam = req.getParameter("offset");
      String where = "";
      String offset = "";
      PrintWriter out = res.getWriter();

      // Parameter auswerten
      String query_text = req.getParameter("query_text");
      modelRoot.put("query_text", query_text);
      if (query_text != null) modelRoot.put("query_text_encoded", URLEncoder.encode(query_text));
      String query_field = req.getParameter("query_field");
      modelRoot.put("query_field", query_field);
      String parent = req.getParameter("to_parent_id");
      modelRoot.put("to_parent_id", parent);
      String language = req.getParameter("to_language");
      modelRoot.put("to_language", language);
      modelRoot.put("language", getLanguage(req));

      String whereClause = "";
      boolean isFirst = true;
      if (query_text != null && !query_text.equalsIgnoreCase("")) {
        whereClause += "lower(" + query_field + ") like lower('%" + query_text + "%')";
        isFirst = false;
      }
      if (parent != null && !parent.equals("")) {
        if (isFirst == false) whereClause += " and ";
        whereClause += "to_parent_id='" + parent + "'";
        isFirst = false;
      }
      if (language != null && !language.equals("")) {
        if (isFirst == false) whereClause += " and ";
        whereClause += "to_language='" + language + "'";
        isFirst = false;
      }

      // hier offsetcode bearbeiten
      if (offsetParam != null && !offsetParam.equals("")) {
        offset = offsetParam;
      }
      if (req.getParameter("next") != null) {
        offset = req.getParameter("nextoffset");
      }
      else {
        if (req.getParameter("prev") != null) {
          offset = req.getParameter("prevoffset");
        }
      }

      if (offset == null || offset.equals("")) offset = "0";
      modelRoot.put("offset", (new Integer(offset)).toString());

      theParentList = mainModule.getByWhereClause("to_parent_id=NULL", "title", 0, 1000);
      theImcsList = mainModule.getByWhereClause(whereClause, "title", (new Integer(offset)).intValue());
      theLanguageList = languageModule.getByWhereClause(null, "name", 0);

      modelRoot.put("parentlist", theParentList);
      modelRoot.put("imcslist", theImcsList);
      modelRoot.put("languagelist", theLanguageList);
      modelRoot.put("count", (new Integer(theImcsList.getCount())).toString());
      modelRoot.put("from", (new Integer(theImcsList.getFrom())).toString());
      modelRoot.put("to", (new Integer(theImcsList.getTo())).toString());
      if (theImcsList.hasNextBatch())
        modelRoot.put("next", (new Integer(theImcsList.getNextBatch())).toString());
      if (theImcsList.hasPrevBatch())
        modelRoot.put("prev", (new Integer(theImcsList.getPrevBatch())).toString());

      HTMLTemplateProcessor.process(res, templateListString, modelRoot, res.getWriter(), getLocale(req));

    }
    catch (Exception e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void edit(HttpServletRequest req, HttpServletResponse res)
          throws ServletModuleException {
    try {

      SimpleHash modelRoot = new SimpleHash();
      EntityList parentList;
      EntityList theLanguageList;
      int offset = 0;
      String idParam = req.getParameter("id");
      String where = "";

      parentList = mainModule.getByWhereClause("to_parent_id=NULL", "title", offset, 1000);
      theLanguageList = languageModule.getByWhereClause(null, "name", 0);

      modelRoot.put("parentlist", parentList);
      modelRoot.put("languagelist", theLanguageList);
      modelRoot.put("entity", mainModule.getById(idParam));
      modelRoot.put("new", "0");
      deliver(req, res, modelRoot, templateObjektString);

    }
    catch (ModuleException e) {
      throw new ServletModuleException(e.toString());
    }
  }

}

