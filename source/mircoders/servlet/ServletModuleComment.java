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

import org.apache.struts.util.MessageResources;

import freemarker.template.*;

import mir.servlet.*;
import mir.module.*;
import mir.misc.*;
import mir.entity.*;
import mir.storage.*;
import mir.generator.*;
import mir.entity.*;
import mir.entity.adapter.*;
import mir.util.*;
import mir.log.*;

import mircoders.storage.*;
import mircoders.global.*;
import mircoders.localizer.*;
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
    logger = new LoggerWrapper("ServletModule.Comment");

    templateListString = MirConfig.getProp("ServletModule.Comment.ListTemplate");
    templateObjektString = MirConfig.getProp("ServletModule.Comment.ObjektTemplate");
    templateConfirmString = MirConfig.getProp("ServletModule.Comment.ConfirmTemplate");

    try {
      mainModule = new ModuleComment(DatabaseComment.getInstance());
      moduleContent = new ModuleContent(DatabaseContent.getInstance());
    }
    catch (StorageObjectException e) {
      logger.error("servletmodule comment could not be initialized:" + e.getMessage());
    }
  }

  public void edit(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    String idParam = req.getParameter("id");

    if (idParam == null)
      throw new ServletModuleException("Invalid call: id not supplied ");

    showComment(idParam, req, res);
  }

  public void showComment(String anId, HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleException {
    try {
      SimpleHash extraInfo = new SimpleHash();
      TemplateModelRoot data;

      data = (TemplateModelRoot) mainModule.getById(anId);

      extraInfo.put("languages", DatabaseLanguage.getInstance().getPopupData());
      extraInfo.put("comment_status_values", DatabaseCommentStatus.getInstance().getPopupData());

      deliver(aRequest, aResponse, data, extraInfo, templateObjektString);
    }
    catch (Exception e) {
      throw new ServletModuleException(e.toString());
    }
  }


  public void list(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleException
  {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    String where = requestParser.getParameter("where");
    String order = requestParser.getParameterWithDefault("order", "webdb_create desc");
    int offset = requestParser.getIntegerWithDefault("offset", 0);

    returnCommentList(aRequest, aResponse, where, order, offset);
  }

  public void search(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleException
  {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    String queryField = "";
    String searchField = requestParser.getParameter("searchfield");
    String searchText = requestParser.getParameter("searchtext");
    String searchIsPublished = requestParser.getParameter("searchispublished");
    String searchOrder = requestParser.getParameter("searchorder");

    String whereClause = " (1=1) ";
    String orderClause = "webdb_create desc";

    if (searchIsPublished.equals("0")) {
      whereClause=whereClause + "is_published='f'";
    }
    else if (searchIsPublished.equals("1")) {
      whereClause=whereClause + "is_published='t'";
    }

    if (!searchField.equals("") && !searchText.equals("")) {
      queryField="";

      if (searchField.equals("title"))
        queryField = "title";
      else if (searchField.equals("creator"))
        queryField = "creator";
      else if (searchField.equals("description"))
        queryField = "description";
      else
        queryField = "";

      if (!queryField.equals("")) {
        if (!whereClause.equals(""))
          whereClause = whereClause + " and ";

        whereClause = whereClause + "lower(" + queryField + ") like '%" + JDBCStringRoutines.escapeStringLiteral(searchText.toLowerCase()) + "%'";
      }
    }

    System.out.println("search order = " + searchOrder);

    if (searchOrder.equals("datedesc")) {
      orderClause = "webdb_create desc";
    }
    else if (searchOrder.equals("dateasc")) {
      orderClause = "webdb_create asc";
    }
    else if (searchOrder.equals("articletitle")) {
      orderClause = "(select content.title from content where content.id = comment.to_media) asc";
    }

    returnCommentList(aRequest, aResponse, whereClause, orderClause, 0);
  }

  public void articlecomments(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    String articleIdString = req.getParameter("articleid");
    int articleId;

    try {
      articleId  = Integer.parseInt(articleIdString);

      returnCommentList( req, res, "to_media="+articleId, "webdb_create desc", 0);
    }
    catch (ServletModuleException e) {
      throw e;
    }
    catch (Throwable e) {
      throw new ServletModuleException(e.getMessage());
    }
  }

  public void returnCommentList(HttpServletRequest aRequest, HttpServletResponse aResponse,
     String aWhereClause, String anOrderByClause, int anOffset) throws ServletModuleException {
    // ML: experiment in using the producer's generation system instead of the
    //     old one...

    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    URLBuilder urlBuilder = new URLBuilder();
    EntityAdapterModel model;
    int nrCommentsPerPage = 20;
    int count;

    try {
      Map responseData = ServletHelper.makeGenerationData(getLocale(aRequest));
      model = MirGlobal.localizer().dataModel().adapterModel();

      Object commentList =
          new CachingRewindableIterator(
            new EntityIteratorAdapter( aWhereClause, anOrderByClause, nrCommentsPerPage,
              MirGlobal.localizer().dataModel().adapterModel(), "comment", nrCommentsPerPage, anOffset)
      );

      responseData.put("nexturl", null);
      responseData.put("prevurl", null);

      count=mainModule.getSize(aWhereClause);

      urlBuilder.setValue("module", "Comment");
      urlBuilder.setValue("do", "list");
      urlBuilder.setValue("where", aWhereClause);
      urlBuilder.setValue("order", anOrderByClause);

      urlBuilder.setValue("searchfield", requestParser.getParameter("searchfield"));
      urlBuilder.setValue("searchtext", requestParser.getParameter("searchtext"));
      urlBuilder.setValue("searchispublished", requestParser.getParameter("searchispublished"));
      urlBuilder.setValue("searchorder", requestParser.getParameter("searchorder"));

      responseData.put("searchfield", requestParser.getParameter("searchfield"));
      responseData.put("searchtext", requestParser.getParameter("searchtext"));
      responseData.put("searchispublished", requestParser.getParameter("searchispublished"));
      responseData.put("searchorder", requestParser.getParameter("searchorder"));

      urlBuilder.setValue("offset", anOffset);
      responseData.put("thisurl" , urlBuilder.getQuery());

      if (count>=anOffset+nrCommentsPerPage) {
        urlBuilder.setValue("offset", anOffset + nrCommentsPerPage);
        responseData.put("nexturl" , urlBuilder.getQuery());
      }

      if (anOffset>0) {
        urlBuilder.setValue("offset", Math.max(anOffset - nrCommentsPerPage, 0));
        responseData.put("prevurl" , urlBuilder.getQuery());
      }

      responseData.put("comments", commentList);
      responseData.put("from" , Integer.toString(anOffset+1));
      responseData.put("count", Integer.toString(count));
      responseData.put("to", Integer.toString(Math.min(anOffset+nrCommentsPerPage, count)));

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, "commentlist.template");
    }
    catch (Throwable e) {
      e.printStackTrace(new PrintWriter(new LoggerToWriterAdapter(logger, logger.ERROR_MESSAGE)));

      throw new ServletModuleException(e.getMessage());
    }
  }
}

