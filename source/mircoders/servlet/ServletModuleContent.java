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
import org.apache.lucene.index.*;

import freemarker.template.*;

import mir.servlet.*;
import mir.media.*;
import mir.module.*;
import mir.misc.*;
import mir.storage.*;
import mir.entity.*;
import mir.util.*;
import mir.entity.adapter.*;
import mir.log.*;

import mircoders.global.*;
import mircoders.storage.*;
import mircoders.module.*;
import mircoders.entity.*;
import mircoders.localizer.*;
import mircoders.search.IndexUtil;

/*
 *  ServletModuleContent -
 *  deliver html for the article admin form.
 *
 * @version $Id: ServletModuleContent.java,v 1.31 2003/01/18 15:55:14 john Exp $
 * @author rk, mir-coders
 *
 */

public class ServletModuleContent extends ServletModule
{
  static ModuleTopics         themenModule;
  static ModuleSchwerpunkt    schwerpunktModule;
  static ModuleImages         imageModule;

  static String templateOpString;

// Singelton / Kontruktor

  private static ServletModuleContent instance = new ServletModuleContent();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleContent() {
    logger = new LoggerWrapper("ServletModule.Content");
    try {
      templateListString = MirConfig.getProp("ServletModule.Content.ListTemplate");
      templateObjektString = MirConfig.getProp("ServletModule.Content.ObjektTemplate");
      templateConfirmString = MirConfig.getProp("ServletModule.Content.ConfirmTemplate");

      mainModule = new ModuleContent(DatabaseContent.getInstance());
      themenModule = new ModuleTopics(DatabaseTopics.getInstance());
      schwerpunktModule = new ModuleSchwerpunkt(DatabaseFeature.getInstance());
      imageModule = new ModuleImages(DatabaseImages.getInstance());
    }
    catch (StorageObjectException e) {
      logger.error("servletmodulecontent konnte nicht initialisiert werden");
    }
  }

// Methoden

  public void list(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    EntityUsers user = _getUser(req);
    String offsetParam = req.getParameter("offset");
    String whereParam = req.getParameter("where");
    String orderParam = req.getParameter("order");

    int offset =0;

    if (offsetParam != null && !offsetParam.equals(""))
      offset = Integer.parseInt(offsetParam);

    if (req.getParameter("next") != null)
      offset=Integer.parseInt(req.getParameter("nextoffset"));
    else
      if (req.getParameter("prev") != null)
        offset = Integer.parseInt(req.getParameter("prevoffset"));

    returnArticleList(req, res, whereParam, orderParam, offset);
  }

  public void listop(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    EntityUsers user = _getUser(req);
    String       offsetParam = req.getParameter("offset");
    int          offset =0;

    String whereParam = req.getParameter("where");

    if (whereParam==null) whereParam = "to_article_type='0'";

// hier offsetcode bearbeiteb
    if (offsetParam != null && !offsetParam.equals(""))
      offset = Integer.parseInt(offsetParam);

    if (req.getParameter("next") != null)
      offset=Integer.parseInt(req.getParameter("nextoffset"));
    else
      if (req.getParameter("prev") != null)
        offset = Integer.parseInt(req.getParameter("prevoffset"));

    String orderParam = req.getParameter("order");

    returnArticleList(req, res, whereParam, orderParam, offset);
  }


  public void search(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException {
    try {
      EntityUsers   user = _getUser(req);
      EntityList    theList;
      String        fieldParam = req.getParameter("field");
      String        fieldValueParam = req.getParameter("fieldvalue");
      String        orderParam = req.getParameter("order");

      theList = ((ModuleContent)mainModule).getContentByField(fieldParam, fieldValueParam, orderParam, 0, user);
      returnArticleList(req, res, "lower("+ fieldParam + ") like lower('%" + JDBCStringRoutines.escapeStringLiteral(fieldValueParam) + "%')", orderParam, 0);
    } catch (ModuleException e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void add(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException {
    _showObject(null, req, res);
  }


  public void insert(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
//theLog.printDebugInfo(":: content :: trying to insert");
    try {
      EntityUsers   user = _getUser(req);
      HashMap withValues = getIntersectingValues(req, DatabaseContent.getInstance());

      String now = StringUtil.date2webdbDate(new GregorianCalendar());
      withValues.put("date", now);
      withValues.put("publish_path", StringUtil.webdbDate2path(now));
      withValues.put("to_publisher", user.getId());
      withValues.put("is_produced", "0");
      if (!withValues.containsKey("is_published"))
        withValues.put("is_published","0");
      if (!withValues.containsKey("is_html"))
        withValues.put("is_html","0");

      String id = mainModule.add(withValues);
      DatabaseContentToTopics.getInstance().setTopics(id,req.getParameterValues("to_topic"));

      _showObject(id, req, res);
    }
    catch (StorageObjectException e) {
      throw new ServletModuleException(e.toString());
    }
    catch (ModuleException e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void delete(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    EntityUsers   user = _getUser(req);

    String idParam = req.getParameter("id");
    if (idParam == null) throw new ServletModuleException("Invalid call: id missing");

    String confirmParam = req.getParameter("confirm");
    String cancelParam = req.getParameter("cancel");

    logger.info("where = " + req.getParameter("where"));

    if (confirmParam == null && cancelParam == null) {

      SimpleHash mergeData = new SimpleHash();
      mergeData.put("module", "Content");
      mergeData.put("infoString", "Content: " + idParam);
      mergeData.put("id", idParam);
      mergeData.put("where", req.getParameter("where"));
      mergeData.put("order", req.getParameter("order"));
      mergeData.put("offset", req.getParameter("offset"));
      deliver(req, res, mergeData, templateConfirmString);
    }
    else {
      if (confirmParam!= null && !confirmParam.equals("")) {
        try {
          mainModule.deleteById(idParam);

          /** @todo the following two should be implied in
           *  DatabaseContent */

          //delete rows in the content_x_topic-table
          DatabaseContentToTopics.getInstance().deleteByContentId(idParam);
          //delete rows in the comment-table
          DatabaseComment.getInstance().deleteByContentId(idParam);
	  //delete from lucene index, if any
	  String index=MirConfig.getProp("IndexPath");
	  if (IndexReader.indexExists(index)){
	    IndexUtil.unindexID(idParam,index);
	  }
	  
        } catch (ModuleException e) {
          throw new ServletModuleException(e.toString());
        } catch (StorageObjectException e) {
          throw new ServletModuleException(e.toString());
        } catch (IOException e) {
	  throw new ServletModuleException(e.toString());
	}
        list(req,res);
      }
      else {
        // Datensatz anzeigen
        _showObject(idParam, req, res);
      }
    }
  }

  public void edit(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    String        idParam = req.getParameter("id");
    if (idParam == null) throw new ServletModuleException("Invalid call: id not supplied ");
    _showObject(idParam, req, res);
  }

// methods for attaching media file
  public void attach(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    String  mediaIdParam = req.getParameter("mid");
    String  idParam = req.getParameter("cid");
    if (idParam == null||mediaIdParam==null) throw new ServletModuleException("smod content :: attach :: cid/mid missing");

    try {
      EntityContent entContent = (EntityContent)mainModule.getById(idParam);
      entContent.attach(mediaIdParam);
    }
    catch(ModuleException e) {
      logger.error("smod content :: attach :: could not get entityContent");
    }
    catch(StorageObjectException e) {
      logger.error("smod content :: attach :: could not get entityContent");
    }

    _showObject(idParam, req, res);
  }

  public void dettach(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    String  cidParam = req.getParameter("cid");
    String  midParam = req.getParameter("mid");
    if (cidParam == null) throw new ServletModuleException("smod content :: dettach :: cid missing");
    if (midParam == null) throw new ServletModuleException("smod content :: dettach :: mid missing");

    try {
      EntityContent entContent = (EntityContent)mainModule.getById(cidParam);
      entContent.dettach(cidParam,midParam);
    }
    catch(ModuleException e) {
      logger.error("smod content :: dettach :: could not get entityContent");
    }
    catch(StorageObjectException e) {
      logger.error("smod content :: dettach :: could not get entityContent");
    }

    _showObject(cidParam, req, res);
  }

  public void newswire(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    String  idParam = req.getParameter("id");
    if (idParam == null) throw new ServletModuleException("smod content :: newswire :: id missing");
    try {
      EntityContent entContent = (EntityContent)mainModule.getById(idParam);
      entContent.newswire();
    }
    catch(ModuleException e) {
      logger.error("smod content :: newswire :: could not get entityContent");
    }
    catch(StorageObjectException e) {
      logger.error("smod content :: dettach :: could not get entityContent");
    }

    list(req, res);
  }


  public void update(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException
  {
    try {

      EntityUsers   user = _getUser(req);
      if (user==null) logger.debug("user null!");
      String idParam = req.getParameter("id");
      if (idParam == null) throw new ServletModuleException("Wrong call: (id) is missing");

      HashMap withValues = getIntersectingValues(req, DatabaseContent.getInstance());
      String[] topic_id = req.getParameterValues("to_topic");
      String content_id = req.getParameter("id");

      if(user != null) withValues.put("user_id", user.getId());
      withValues.put("is_produced", "0");
      if (!withValues.containsKey("is_published"))
        withValues.put("is_published","0");
      if (!withValues.containsKey("is_html"))
        withValues.put("is_html","0");

      String id = mainModule.set(withValues);
      DatabaseContentToTopics.getInstance().setTopics(req.getParameter("id"),topic_id);

      String whereParam = req.getParameter("where");
      String orderParam = req.getParameter("order");

      if ((whereParam!=null && !whereParam.equals("")) || (orderParam!=null && !orderParam.equals(""))){
        list(req,res);
      }
      else
        _showObject(idParam, req, res);
    }
    catch (StorageObjectException e) {
      throw new ServletModuleException(e.toString());
    }
    catch (ModuleException e) {
      throw new ServletModuleException(e.toString());
    }
  }

/*
  * HelperMethod shows the basic article editing form.
  *
  * if the "id" parameter is null, it means show an empty form to add a new
  * article.
*/
  private void _showObject(String id, HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException {

    SimpleHash extraInfo = new SimpleHash();
    try {
      TemplateModelRoot entContent;

      if (id != null) {
        entContent = (TemplateModelRoot)mainModule.getById(id);
      }
      else {
        SimpleHash withValues = new SimpleHash();
        withValues.put("new", "1");
        withValues.put("is_published", "0");
        String now = StringUtil.date2webdbDate(new GregorianCalendar());
        withValues.put("date", new SimpleScalar(now));
        EntityUsers   user = _getUser(req);
        withValues.put("login_user", user);
        entContent = withValues;
      }

      extraInfo.put("themenPopupData", themenModule.getTopicsAsSimpleList());
      extraInfo.put("articletypePopupData", DatabaseArticleType.getInstance().getPopupData());
      extraInfo.put("languagePopupData", DatabaseLanguage.getInstance().getPopupData());

      // code to be able to return to the list:
      String offsetParam, whereParam, orderParam;
      if ((offsetParam = req.getParameter("offset"))!=null) extraInfo.put("offset", offsetParam);
      if ((whereParam = req.getParameter("where"))!=null) extraInfo.put("where", whereParam);
      if ((orderParam = req.getParameter("order"))!=null) extraInfo.put("order", orderParam);

      extraInfo.put("login_user", _getUser(req));
      deliver(req, res, entContent, extraInfo, templateObjektString);
    }
    catch (Exception e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void returnArticleList(HttpServletRequest aRequest, HttpServletResponse aResponse,
                                String aWhereClause, String anOrderByClause, int anOffset) throws ServletModuleException {
    // ML: experiment in using the producer's generation system instead of the
    //     old one...

    EntityAdapterModel model;
    int nrArticlesPerPage = 20;
    int count;

    try {
      Map responseData = ServletHelper.makeGenerationData(getLocale(aRequest));
      model = MirGlobal.localizer().dataModel().adapterModel();

      Object contentList =
          new CachingRewindableIterator(
          new EntityIteratorAdapter( aWhereClause, anOrderByClause, nrArticlesPerPage,
          MirGlobal.localizer().dataModel().adapterModel(), "content", nrArticlesPerPage, anOffset)
      );

      responseData.put("nexturl", null);
      responseData.put("prevurl", null);

      count=mainModule.getSize(aWhereClause);

      if (count>=anOffset+nrArticlesPerPage) {
        responseData.put("nexturl" ,
                         "module=Content&do=list&where=" + HTMLRoutines.encodeURL(aWhereClause) +
                         "&order=" + HTMLRoutines.encodeURL(anOrderByClause) +
                         "&offset=" + (anOffset + nrArticlesPerPage));
      }
      if (anOffset>0) {
        responseData.put("prevurl" ,
                         "module=Content&do=list&where=" + HTMLRoutines.encodeURL(aWhereClause) +
                         "&order=" + HTMLRoutines.encodeURL(anOrderByClause) +
                         "&offset=" + Math.max(anOffset - nrArticlesPerPage, 0));
      }

      responseData.put("articles", contentList);

      responseData.put("thisurl" ,
                       "module=Content&do=list&where=" + HTMLRoutines.encodeURL(aWhereClause) +
                       "&order=" + HTMLRoutines.encodeURL(anOrderByClause) +
                       "&offset=" + anOffset);

      responseData.put("from" , Integer.toString(anOffset+1));
      responseData.put("count", Integer.toString(count));
      responseData.put("to", Integer.toString(Math.min(anOffset+nrArticlesPerPage, count)));
      responseData.put("offset" , Integer.toString(anOffset));
      responseData.put("order", anOrderByClause);
      responseData.put("where" , aWhereClause);

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, "contentlist.template");
    }
    catch (Throwable e) {
      throw new ServletModuleException(e.toString());
    }
  }

  private EntityUsers _getUser(HttpServletRequest req)
  {
    HttpSession session=req.getSession(false);
    return (EntityUsers)session.getAttribute("login.uid");
  }
}
