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

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.entity.adapter.EntityAdapterModel;
import mir.entity.adapter.EntityIteratorAdapter;
import mir.log.LoggerWrapper;
import mir.misc.StringUtil;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.util.CachingRewindableIterator;
import mir.util.HTTPRequestParser;
import mir.util.JDBCStringRoutines;
import mir.util.SQLQueryBuilder;
import mir.util.StringRoutines;
import mir.util.URLBuilder;
import mircoders.entity.EntityContent;
import mircoders.global.MirGlobal;
import mircoders.module.ModuleContent;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseContentToTopics;

/*
 *  ServletModuleContent -
 *  deliver html for the article admin form.
 *
 * @version $Id: ServletModuleContent.java,v 1.52.2.12 2003/10/23 14:55:25 rk Exp $
 * @author rk, mir-coders
 *
 */

public class ServletModuleContent extends ServletModule
{
  private static ServletModuleContent instance = new ServletModuleContent();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleContent() {
    super();

    logger = new LoggerWrapper("ServletModule.Content");

    try {
      mainModule = new ModuleContent(DatabaseContent.getInstance());
    }
    catch (Throwable e) {
      logger.fatal("ServletModuleContent could not be initialized: " + e.toString());
    }
  }

  public void list(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    String where = requestParser.getParameter("where");
    String order = requestParser.getParameterWithDefault("order", "webdb_create desc");
    int offset = requestParser.getIntegerWithDefault("offset", 0);
    String selectArticleUrl = requestParser.getParameter("selectarticleurl");

    returnList(aRequest, aResponse, where, order, offset, selectArticleUrl);
  }

  public void search(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc, ServletModuleFailure {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      SQLQueryBuilder queryBuilder = new SQLQueryBuilder();
      String searchField = requestParser.getParameterWithDefault("searchfield", "");
      String searchValue = requestParser.getParameterWithDefault("searchvalue", "").trim();
      String searchOrder = requestParser.getParameterWithDefault("searchorder", "");
      String searchispublished = requestParser.getParameterWithDefault("searchispublished", "");
      String searchArticleType = requestParser.getParameterWithDefault("searcharticletype", "");
      String selectArticleUrl = requestParser.getParameter("selectarticleurl");

      if (searchValue.length()>0) {
        if (searchField.equals("id"))
          queryBuilder.appendAndCondition(
            "id='"+JDBCStringRoutines.escapeStringLiteral(searchValue)+"'");
        else if (searchField.equals("contents"))
          queryBuilder.appendAndCondition(
            "(lower(content_data) like " + "'%" + JDBCStringRoutines.escapeStringLiteral(searchValue.toLowerCase()) + "%')"+
            " or (lower(description) like " + "'%" + JDBCStringRoutines.escapeStringLiteral(searchValue.toLowerCase()) + "%')");
        else
          queryBuilder.appendAndCondition(
            "lower("+ searchField + ") like " +
            "'%" + JDBCStringRoutines.escapeStringLiteral(searchValue.toLowerCase()) + "%'");
      }

      if (searchispublished.length()>0) {
        if (searchispublished.equals("0"))
          queryBuilder.appendAndCondition("is_published='f'");
        else
          queryBuilder.appendAndCondition("is_published='t'");
      }

      if (searchArticleType.length()>0) {
        queryBuilder.appendAndCondition("to_article_type="+Integer.parseInt(searchArticleType));
      }

      if (searchOrder.length()>0) {
        if (searchOrder.equals("datedesc"))
          queryBuilder.appendDescendingOrder("webdb_create");
        else if (searchOrder.equals("dateasc"))
          queryBuilder.appendAscendingOrder("webdb_create");
        else if (searchOrder.equals("title"))
          queryBuilder.appendAscendingOrder("title");
        else if (searchOrder.equals("creator"))
          queryBuilder.appendAscendingOrder("creator");
      }

      returnList(aRequest, aResponse, queryBuilder.getWhereClause(), queryBuilder.getOrderByClause(), 0, selectArticleUrl);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void add(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    editObject(aRequest, aResponse, null);
  }


  public void insert(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
//theLog.printDebugInfo(":: content :: trying to insert");
    try {
      Map withValues = getIntersectingValues(aRequest, DatabaseContent.getInstance());

      String now = StringUtil.date2webdbDate(new GregorianCalendar());
      withValues.put("date", now);
      withValues.put("publish_path", StringUtil.webdbDate2path(now));
      withValues.put("to_publisher", ServletHelper.getUser(aRequest).getId());
      withValues.put("is_produced", "0");
      if (!withValues.containsKey("is_published"))
        withValues.put("is_published","0");
      if (!withValues.containsKey("is_html"))
        withValues.put("is_html","0");

      String webdbCreate = (String) withValues.get("webdb_create");
      if (webdbCreate==null || webdbCreate.trim().length()==0)
        withValues.remove("webdb_create");

      String id = mainModule.add(withValues);
      logAdminUsage(aRequest, id, "object added");

      List topics;

      DatabaseContentToTopics.getInstance().setTopics(id, aRequest.getParameterValues("to_topic"));

      editObject(aRequest, aResponse, id);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void edit(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    String idParam = aRequest.getParameter("id");
    if (idParam == null)
      throw new ServletModuleExc("Invalid call: id not supplied ");
    editObject(aRequest, aResponse, idParam);
  }

// methods for attaching media file
  public void attach(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    String  mediaIdParam = aRequest.getParameter("mid");
    String  articleId = aRequest.getParameter("articleid");

    if (articleId == null || mediaIdParam==null)
      throw new ServletModuleExc("smod content :: attach :: articleid/mid missing");

    try {
      EntityContent entContent = (EntityContent) mainModule.getById(articleId);
      entContent.attach(mediaIdParam);
    }
    catch(Throwable e) {
      throw new ServletModuleFailure(e);
    }

    logAdminUsage(aRequest, articleId, "media " + mediaIdParam + " attached");

    editObject(aRequest, aResponse, articleId);
  }

  public void dettach(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    String  articleId = aRequest.getParameter("articleid");
    String  midParam = aRequest.getParameter("mid");
    if (articleId == null)
      throw new ServletModuleExc("smod content :: dettach :: articleid missing");
    if (midParam == null)
      throw new ServletModuleExc("smod content :: dettach :: mid missing");

    try {
      EntityContent entContent = (EntityContent)mainModule.getById(articleId);
      entContent.dettach(articleId, midParam);
    }
    catch(Throwable e) {
      throw new ServletModuleFailure(e);
    }

    logAdminUsage(aRequest, articleId, "media " + midParam + " deattached");

    editObject(aRequest, aResponse, articleId);
  }

  public void update(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

      String returnUrl = requestParser.getParameter("returnurl");

      String idParam = aRequest.getParameter("id");
      if (idParam == null)
        throw new ServletModuleExc("Wrong call: (id) is missing");

      Map withValues = getIntersectingValues(aRequest, DatabaseContent.getInstance());

      String content_id = aRequest.getParameter("id");

      withValues.put("is_produced", "0");
      if (!withValues.containsKey("is_published"))
        withValues.put("is_published","0");
      if (!withValues.containsKey("is_html"))
        withValues.put("is_html","0");

      String webdbCreate = (String) withValues.get("webdb_create");
      if (webdbCreate==null || webdbCreate.trim().length()==0)
        withValues.remove("webdb_create");

      String id = mainModule.set(withValues);

      logAdminUsage(aRequest, id, "object modified");

      DatabaseContentToTopics.getInstance().setTopics(aRequest.getParameter("id"), aRequest.getParameterValues("to_topic"));

      if (returnUrl!=null && !returnUrl.equals("")){
        redirect(aResponse, returnUrl);
      }
      else
        editObject(aRequest, aResponse, idParam);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }


  /**
   * HelperMethod shows the basic article editing form.
   *
   * if the "id" parameter is null, it means show an empty form to add a new
   * article.
   *
   * @param id
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   */
  public void editObject(HttpServletRequest aRequest, HttpServletResponse aResponse, String id)
      throws ServletModuleExc {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
      EntityAdapterModel model = MirGlobal.localizer().dataModel().adapterModel();
      Map article;
      URLBuilder urlBuilder = new URLBuilder();

      urlBuilder.setValue("module", "Content");
      urlBuilder.setValue("do", "edit");
      urlBuilder.setValue("id", id);
      urlBuilder.setValue("returnurl", requestParser.getParameter("returnurl"));

      if (id!=null) {
        responseData.put("new", Boolean.FALSE);
        article = model.makeEntityAdapter("content", mainModule.getById(id));
      }
      else {
        List fields = DatabaseContent.getInstance().getFields();
        responseData.put("new", Boolean.TRUE);
        article = new HashMap();
        Iterator i = fields.iterator();
        while (i.hasNext()) {
          article.put(i.next(), null);
        }

        article.put("to_topics", null);

        MirGlobal.localizer().adminInterface().initializeArticle(article);
      }
      responseData.put("article", article);

      List topicsList = new Vector();

      String[] topicCategories = configuration.getStringArray("Mir.Localizer.Admin.TopicLists");

      if (topicCategories.length==0 ) {
        Map categoryMap = new HashMap();
        categoryMap.put("key", "topic");
        categoryMap.put("listtype", "0");
        categoryMap.put("listparameter", "3");
        categoryMap.put("items",
                        new EntityIteratorAdapter("", "title",
            20, MirGlobal.localizer().dataModel().adapterModel(), "topic"));
        topicsList.add(categoryMap);
      }
      else
      {

        for (int i = 0; i < topicCategories.length; i++) {
          try {
            Map categoryMap = new HashMap();
            List parts = StringRoutines.splitString(topicCategories[i], ":");
            String key = null;
            String listtype = "0";
            String listparameter = "5";
            String where = "";
            String order = "";

            if (parts.size() > 0)
              key = (String) parts.get(0);
            if (parts.size() > 1)
              listtype = (String) parts.get(1);
            if (parts.size() > 2)
              listparameter = (String) parts.get(2);
            if (parts.size() > 3)
              where = (String) parts.get(3);
            if (parts.size() > 4)
              order = (String) parts.get(4);

            if (key != null) {
              categoryMap.put("key", key);
              categoryMap.put("listtype", listtype);
              categoryMap.put("listparameter", listparameter);
              categoryMap.put("items",
                              new EntityIteratorAdapter(where, order,
                  20, MirGlobal.localizer().dataModel().adapterModel(), "topic"));
              topicsList.add(categoryMap);
            }
          }
          catch (Throwable t) {
            logger.error("error while preparing topics: " + t.toString());
          }
        }
      }

      responseData.put("topics", topicsList);

      responseData.put("returnurl", requestParser.getParameter("returnurl"));
      responseData.put("thisurl", urlBuilder.getQuery());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, editGenerator);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void returnList(HttpServletRequest aRequest, HttpServletResponse aResponse,
     String aWhereClause, String anOrderByClause, int anOffset) throws ServletModuleExc {
    this.returnList(aRequest, aResponse, aWhereClause, anOrderByClause, anOffset, null);
  }

  public void returnList(
       HttpServletRequest aRequest,
       HttpServletResponse aResponse,
       String aWhereClause,
       String anOrderByClause,
       int anOffset,
       String aSelectArticleUrl) throws ServletModuleExc {

    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    URLBuilder urlBuilder = new URLBuilder();
    EntityAdapterModel model;
    int count;

    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
      model = MirGlobal.localizer().dataModel().adapterModel();

      Object articleList =
          new CachingRewindableIterator(
            new EntityIteratorAdapter( aWhereClause, anOrderByClause, nrEntitiesPerListPage,
               MirGlobal.localizer().dataModel().adapterModel(), "content", nrEntitiesPerListPage, anOffset)
      );

      responseData.put("nexturl", null);
      responseData.put("prevurl", null);

      count=mainModule.getSize(aWhereClause);

      urlBuilder.setValue("module", "Content");
      urlBuilder.setValue("do", "list");
      urlBuilder.setValue("where", aWhereClause);
      urlBuilder.setValue("order", anOrderByClause);


      urlBuilder.setValue("searchfield", requestParser.getParameter("searchfield"));
      urlBuilder.setValue("searchvalue", requestParser.getParameter("searchvalue"));
      urlBuilder.setValue("searchispublished", requestParser.getParameter("searchispublished"));
      urlBuilder.setValue("searchorder", requestParser.getParameter("searchorder"));
      urlBuilder.setValue("searcharticletype", requestParser.getParameter("searcharticletype"));
      urlBuilder.setValue("selectarticleurl", aSelectArticleUrl);

      responseData.put("searchfield", requestParser.getParameter("searchfield"));
      responseData.put("searchvalue", requestParser.getParameter("searchvalue"));
      responseData.put("searchispublished", requestParser.getParameter("searchispublished"));
      responseData.put("searchorder", requestParser.getParameter("searchorder"));
      responseData.put("searcharticletype", requestParser.getParameter("searcharticletype"));
      responseData.put("selectarticleurl", aSelectArticleUrl);

      urlBuilder.setValue("offset", anOffset);
      responseData.put("offset" , new Integer(anOffset).toString());
      responseData.put("thisurl" , urlBuilder.getQuery());

      if (count>=anOffset+nrEntitiesPerListPage) {
        urlBuilder.setValue("offset", (anOffset + nrEntitiesPerListPage));
        responseData.put("nexturl" , urlBuilder.getQuery());
      }

      if (anOffset>0) {
        urlBuilder.setValue("offset", Math.max(anOffset - nrEntitiesPerListPage, 0));
        responseData.put("prevurl" , urlBuilder.getQuery());
      }

      responseData.put("articles", articleList);

      responseData.put("from" , Integer.toString(anOffset+1));
      responseData.put("count", Integer.toString(count));
      responseData.put("to", Integer.toString(Math.min(anOffset+nrEntitiesPerListPage, count)));
      responseData.put("offset" , Integer.toString(anOffset));
      responseData.put("order", anOrderByClause);
      responseData.put("where" , aWhereClause);

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, listGenerator);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void selectparent(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      URLBuilder urlBuilder = new URLBuilder();

      urlBuilder.setValue("module", "Content");
      urlBuilder.setValue("do", "setparent");
      urlBuilder.setValue("childid", requestParser.getParameter("id"));
      urlBuilder.setValue("returnurl", requestParser.getParameter("returnurl"));

      returnList(aRequest, aResponse, "", "", 0, urlBuilder.getQuery());
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void listchildren(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      String articleId = requestParser.getParameter("article_id");

      if (articleId == null)
        throw new ServletModuleExc("ServletModuleContent.listchildren: article_id not set!");

      returnList(aRequest, aResponse, "to_content = " + articleId, "", 0, null);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void setparent(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    String articleId = aRequest.getParameter("childid");
    String parentId  = aRequest.getParameter("id");
    String returnUrl = aRequest.getParameter("returnurl");

    try {
      EntityContent article = (EntityContent) mainModule.getById(articleId);
      article.setValueForProperty("to_content", parentId);
      article.setProduced(false);
      article.update();
      logAdminUsage(aRequest, articleId, "parent set to " + parentId);
    }
    catch(Throwable e) {
      logger.error("ServletModuleContent.setparent: " + e.getMessage());
      throw new ServletModuleFailure(e);
    }

    redirect(aResponse, returnUrl);
  }

  public void clearparent(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    String articleId = requestParser.getParameter("id");
    String returnUrl = requestParser.getParameter("returnurl");

    try {
      EntityContent article = (EntityContent) mainModule.getById(articleId);
      article.setValueForProperty("to_content", "");
      article.setProduced(false);
      article.update();
      logAdminUsage(aRequest, articleId, "parent cleared");
    }
    catch(Throwable e) {
      e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
      logger.error("ServletModuleContent.clearparent: " + e.getMessage());

      throw new ServletModuleFailure("ServletModuleContent.clearparent: " + e.getMessage(), e);
    }

    redirect(aResponse, returnUrl);
  }
}
