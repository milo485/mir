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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.config.MirPropertiesConfiguration;
import mir.entity.Entity;
import mir.entity.adapter.EntityAdapter;
import mir.entity.adapter.EntityAdapterModel;
import mir.entity.adapter.EntityIteratorAdapter;
import mir.log.LoggerWrapper;
import mir.media.MediaHelper;
import mir.media.MirMedia;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.servlet.ServletModuleUserExc;
import mir.session.UploadedFile;
import mir.util.CachingRewindableIterator;
import mir.util.ExceptionFunctions;
import mir.util.HTTPParsedRequest;
import mir.util.HTTPRequestParser;
import mir.util.JDBCStringRoutines;
import mir.util.SQLQueryBuilder;
import mir.util.URLBuilder;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.entity.EntityUploadedMedia;
import mircoders.global.MirGlobal;
import mircoders.media.MediaUploadProcessor;
import mircoders.module.ModuleMediaType;
import mircoders.storage.DatabaseComment;
import mircoders.storage.DatabaseContent;

import org.apache.commons.fileupload.FileItem;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author the mir coders
 * @version 1.0
 */

public abstract class ServletModuleUploadedMedia extends ServletModule {
  protected String moduleName = "UploadedMedia";

  public static ServletModule getInstance() {
    return null;
  }

  public ServletModuleUploadedMedia() {
    super();

    definition = "uploadedMedia";
    logger = new LoggerWrapper("ServletModule.UploadedMedia");
    try {
      model = MirGlobal.localizer().dataModel().adapterModel();
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public void insert(HttpServletRequest aRequest, HttpServletResponse aResponse)
          throws ServletModuleExc, ServletModuleUserExc {
    try {
      HTTPParsedRequest parsedRequest = new HTTPParsedRequest(aRequest,
          configuration.getString("Mir.DefaultEncoding"),
          configuration.getInt("MaxMediaUploadSize")*1024,
          configuration.getString("TempDir"));

      Map mediaValues = new HashMap();

      mediaValues.put("to_publisher", ServletHelper.getUser(aRequest).getId());

      Iterator i = mainModule.getStorageObject().getFields().iterator();
      while (i.hasNext()) {
        String field = (String) i.next();
        String value = parsedRequest.getParameter(field);
        if (value!=null)
          mediaValues.put(field, value);
      }

      List mediaList = new Vector();

      i = parsedRequest.getFiles().iterator();
      while (i.hasNext()) {
        UploadedFile file = new mir.session.CommonsUploadedFileAdapter((FileItem) i.next());

        String suffix = file.getFieldName().substring(5);
        mediaValues.put("title", parsedRequest.getParameter("media_title" + suffix));

        mediaList.add(MediaUploadProcessor.processMediaUpload(file, mediaValues));
      }

      String articleid = parsedRequest.getParameter("articleid");
      String commentid = parsedRequest.getParameter("commentid");

      if (articleid!=null) {
        EntityContent entContent = (EntityContent) DatabaseContent.getInstance().selectById(articleid);

        i=mediaList.iterator();

        while (i.hasNext()) {
          String id = ((EntityUploadedMedia) i.next()).getId();

          entContent.attach(id);
          logAdminUsage(aRequest, id, "object attached to article " + articleid);
        }

        ((ServletModuleContent) ServletModuleContent.getInstance()).editObject(aRequest, aResponse, articleid);


        return;
      }

      if (commentid!=null) {
        EntityComment comment = (EntityComment) DatabaseComment.getInstance().selectById(commentid);

        i=mediaList.iterator();

        while (i.hasNext()) {
          String id = ((EntityUploadedMedia) i.next()).getId();

          comment.attach(id);

          logAdminUsage(aRequest, id, "object attached to comment " + commentid);
        }

        ((ServletModuleComment) ServletModuleComment.getInstance()).editObject(aRequest, aResponse, commentid);

        return;
      }

      logAdminUsage(aRequest, "", mediaList.size() + " objects added");

      returnUploadedMediaList(aRequest, aResponse, mediaList, 1, mediaList.size(), mediaList.size(), "", null, null);
    }
    catch (Throwable t) {
      Throwable cause = ExceptionFunctions.traceCauseException(t);

      if (cause instanceof ModuleMediaType.UnsupportedMimeTypeExc) {
        throw new ServletModuleUserExc("media.error.unsupportedformat", new String[] {});
      }
      throw new ServletModuleFailure("ServletModuleUploadedMedia.insert: " + t.toString(), t);
    }
  }

  public void update(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {

    try {
      HTTPParsedRequest parsedRequest = new HTTPParsedRequest(aRequest,
          configuration.getString("Mir.DefaultEncoding"),
          configuration.getInt("MaxMediaUploadSize")*1024,
          configuration.getString("TempDir"));
      Map mediaValues = new HashMap();

      Iterator i = mainModule.getStorageObject().getFields().iterator();
      while (i.hasNext()) {
        String field = (String) i.next();
        String value = parsedRequest.getParameter(field);
        if (value!=null)
          mediaValues.put(field, value);
      }

      mediaValues.put("to_publisher", ServletHelper.getUser(aRequest).getId());
      mediaValues.put("is_produced", "0");
      if (!mediaValues.containsKey("is_published"))
        mediaValues.put("is_published", "0");

      String id = mainModule.set(mediaValues);
      logger.debug("update: media ID = " + id);
      logAdminUsage(aRequest, id, "object modified");

      editUploadedMediaObject(id, aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure("upload -- exception " + e.toString(), e);
    }

  }

  public void returnUploadedMediaList(HttpServletRequest aRequest, HttpServletResponse aResponse,
                                      Object aList, int aFrom, int aTo, int aCount,
                                      String aThisUrl, String aNextUrl, String aPreviousUrl) throws ServletModuleExc {

    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] {getLocale(aRequest), getFallbackLocale(aRequest)});

      responseData.put("searchfield", requestParser.getParameterWithDefault("searchfield", ""));
      responseData.put("searchtext", requestParser.getParameterWithDefault("searchtext", ""));
      responseData.put("searchispublished", requestParser.getParameterWithDefault("searchispublished", ""));
      responseData.put("searchmediafolder", requestParser.getParameterWithDefault("searchmediafolder", ""));
      responseData.put("articleid", requestParser.getParameter("articleid"));
      responseData.put("commentid", requestParser.getParameter("commentid"));

      responseData.put("thisurl", aThisUrl);
      responseData.put("nexturl", aNextUrl);
      responseData.put("prevurl", aPreviousUrl);

      responseData.put("from", Integer.toString(aFrom));
      responseData.put("count", Integer.toString(aCount));
      responseData.put("to", Integer.toString(aTo));

      responseData.put("medialist", aList);

      addExtraData(responseData);
      ServletHelper.generateResponse(aResponse.getWriter(), responseData, listGenerator);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public void returnUploadedMediaList(HttpServletRequest aRequest, HttpServletResponse aResponse,
                                      String aWhereClause, String anOrderByClause, int anOffset) throws ServletModuleExc {

    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    URLBuilder urlBuilder = new URLBuilder();
    EntityAdapterModel model;
    String nextPageUrl = null;
    String previousPageUrl = null;
    String thisUrl = null;

    int count;

    try {
      model = MirGlobal.localizer().dataModel().adapterModel();

      Object mediaList =
          new CachingRewindableIterator(
          new EntityIteratorAdapter(aWhereClause, anOrderByClause, nrEntitiesPerListPage,
                   model, definition, nrEntitiesPerListPage, anOffset)
          );

      count = mainModule.getSize(aWhereClause);

      urlBuilder.setValue("module", moduleName);
      urlBuilder.setValue("do", "list");
      urlBuilder.setValue("where", aWhereClause);
      urlBuilder.setValue("order", anOrderByClause);

      urlBuilder.setValue("articleid", requestParser.getParameter("articleid"));
      urlBuilder.setValue("commentid", requestParser.getParameter("commentid"));
      urlBuilder.setValue("searchfield", requestParser.getParameter("searchfield"));
      urlBuilder.setValue("searchtext", requestParser.getParameter("searchtext"));
      urlBuilder.setValue("searchispublished", requestParser.getParameter("searchispublished"));
      urlBuilder.setValue("searchmediafolder", requestParser.getParameter("searchmediafolder"));
      urlBuilder.setValue("where", aWhereClause);
      urlBuilder.setValue("order", anOrderByClause);

      urlBuilder.setValue("offset", anOffset);
      thisUrl = urlBuilder.getQuery();

      if (count >= anOffset + nrEntitiesPerListPage) {
        urlBuilder.setValue("offset", anOffset + nrEntitiesPerListPage);
        nextPageUrl = urlBuilder.getQuery();
      }

      if (anOffset > 0) {
        urlBuilder.setValue("offset", Math.max(anOffset - nrEntitiesPerListPage, 0));
        previousPageUrl = urlBuilder.getQuery();
      }

      returnUploadedMediaList(aRequest, aResponse, mediaList,
              anOffset+1, anOffset+nrEntitiesPerListPage, count, thisUrl,
              nextPageUrl, previousPageUrl);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void search(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    SQLQueryBuilder queryBuilder = new SQLQueryBuilder();

    String queryField = "";
    String searchField = requestParser.getParameter("searchfield");
    String searchText = requestParser.getParameter("searchtext");
    String searchIsPublished = requestParser.getParameter("searchispublished");
    String searchMediaFolder = requestParser.getParameter("searchmediafolder");

    queryBuilder.appendDescendingOrder("webdb_create");

    if (searchIsPublished!=null)
      if (searchIsPublished.equals("0")) {
        queryBuilder.appendAndCondition("is_published='f'");
      }
      else if (searchIsPublished.equals("1")) {
        queryBuilder.appendAndCondition("is_published='t'");
      }

    if (searchField!=null && searchText!=null && searchText.length()>0) {
        queryBuilder.appendAndCondition(
          "lower(" + searchField + ") like " +
          "'%" + JDBCStringRoutines.escapeStringLiteral(searchText.toLowerCase()) + "%'");
    }

    if (searchMediaFolder!=null && searchMediaFolder.length()>0) {
      queryBuilder.appendAndCondition("to_media_folder="+Integer.parseInt(searchMediaFolder));
    }

    returnUploadedMediaList(aRequest, aResponse,
            queryBuilder.getWhereClause(), queryBuilder.getOrderByClause(), requestParser.getIntegerWithDefault("offset", 0));
  }

  public void list(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    returnUploadedMediaList(aRequest, aResponse,
       requestParser.getParameterWithDefault("where", ""),
       requestParser.getParameterWithDefault("order", "webdb_create desc"),
       requestParser.getIntegerWithDefault("offset", 0));
  }


  public void add(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] {getLocale(aRequest), getFallbackLocale(aRequest)});

      int nrMedia = requestParser.getIntegerWithDefault("nrmedia", 1);
      int maxNrMedia=configuration.getInt("ServletModule.OpenIndy.MaxMediaUploadItems", 20);

      List fields = mainModule.getStorageObject().getFields();
      Map media = new HashMap();
      Iterator i = fields.iterator();
      while (i.hasNext()) {
        media.put(i.next(), null);
      }
      media.put("to_media_folder", new Integer(7));
      responseData.put("uploadedmedia", media);

      responseData.put("new", Boolean.TRUE);
      responseData.put("articleid", requestParser.getParameter("articleid"));
      responseData.put("commentid", requestParser.getParameter("commentid"));
      responseData.put("returnurl", null);

      if (nrMedia<=0)
        nrMedia=1;
      if (nrMedia>maxNrMedia)
        nrMedia=maxNrMedia;

      List mediaFields = new Vector();
      for (int j=0; j<nrMedia; j++)
        mediaFields.add(new Integer(j));

      responseData.put("nrmedia", new Integer(nrMedia));
      responseData.put("mediafields", mediaFields);

      responseData.put("edittemplate", editGenerator);
      responseData.put("module", moduleName);

      addExtraData(responseData);

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, "uploadedmedia.template");
    }
    catch (Exception e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void edit(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    editUploadedMediaObject(aRequest.getParameter("id"), aRequest, aResponse);
  }

  private void editUploadedMediaObject(String idParam, HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    if (idParam != null && !idParam.equals("")) {
      try {
        Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] {getLocale(aRequest), getFallbackLocale(aRequest)});
        EntityAdapter object =
            model.makeEntityAdapter(definition, mainModule.getById(idParam));
        responseData.put("uploadedmedia", object);
        responseData.put("new", Boolean.FALSE);
        responseData.put("articleid", null);
        responseData.put("commentid", null);
        responseData.put("returnurl", null);
        responseData.put("thisurl", null);

        responseData.put("edittemplate", editGenerator);
        responseData.put("module", moduleName);

        addExtraData(responseData);

        ServletHelper.generateResponse(aResponse.getWriter(), responseData, "uploadedmedia.template");
      }
      catch (Throwable e) {
        throw new ServletModuleFailure(e);
      }
    }
    else {
      throw new ServletModuleExc("ServletmoduleUploadedMedia :: editUploadedMediaObject without id");
    }
  }

  public void getMedia(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    String idParam = aRequest.getParameter("id");
    if (idParam!=null && !idParam.equals("")) {
      try {
        EntityUploadedMedia ent = (EntityUploadedMedia)mainModule.getById(idParam);
        Entity mediaType = ent.getMediaType();
        MirMedia mediaHandler;

        ServletContext ctx = MirPropertiesConfiguration.getContext();
        String fName = ent.getId()+"."+mediaType.getValue("name");

        mediaHandler = MediaHelper.getHandler(mediaType);
        InputStream in = mediaHandler.getMedia(ent, mediaType);

        aResponse.setContentType(ctx.getMimeType(fName));
        //important that before calling this aResponse.getWriter was not called first
        ServletOutputStream out = aResponse.getOutputStream();

        int read ;
        byte[] buf = new byte[8 * 1024];
        while((read = in.read(buf)) != -1) {
          out.write(buf, 0, read);
        }
        in.close();
        out.close();
      }
      catch (Throwable e) {
        throw new ServletModuleFailure(e);
      }
    }
    else logger.error("id not specified.");
    // no exception allowed
  }

  public void getIcon(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    String idParam = aRequest.getParameter("id");
    if (idParam!=null && !idParam.equals("")) {
      try {
        EntityUploadedMedia ent = (EntityUploadedMedia) mainModule.getById(idParam);
        Entity mediaType = ent.getMediaType();
        MirMedia mediaHandler;

        mediaHandler = MediaHelper.getHandler(mediaType);
        InputStream in = mediaHandler.getIcon(ent);

        if (in==null)
          throw new ServletModuleExc("no icon available");

        aResponse.setContentType(mediaHandler.getIconMimeType(ent, mediaType));
        //important that before calling this aResponse.getWriter was not called first
        ServletOutputStream out = aResponse.getOutputStream();

        int read ;
        byte[] buf = new byte[8 * 1024];
        while((read = in.read(buf)) != -1) {
          out.write(buf, 0, read);
        }
        in.close();
        out.close();
      }

      catch (Throwable e) {
        logger.error("getIcon: " + e.toString());
      }
    }
    else logger.error("getIcon: id not specified.");
    // no exception allowed
  }

  protected void addExtraData(Map aTarget) throws ServletModuleExc, ServletModuleFailure {
    try {
      aTarget.put("mediafolders",
                  new EntityIteratorAdapter("", "", 20, MirGlobal.localizer().dataModel().adapterModel(), "mediaFolder"));
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public void showarticles(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    String idParam = aRequest.getParameter("id");
    if (idParam!=null && !idParam.equals("")) {
      try {
        EntityUploadedMedia entity = (EntityUploadedMedia) mainModule.getById(idParam);

        ServletModuleContent.getInstance().returnList(
            aRequest,
            aResponse,
            "exists (select * from content_x_media where content_id=content.id and media_id=" + JDBCStringRoutines.escapeStringLiteral( idParam ) + ")", "", 0);
      }
      catch (Throwable t) {
        throw new ServletModuleFailure(t);
      }
    }
    else logger.error("showarticles: id not specified.");
  }

  public void showcomments(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    String idParam = aRequest.getParameter("id");
    if (idParam!=null && !idParam.equals("")) {
      try {
        EntityUploadedMedia entity = (EntityUploadedMedia) mainModule.getById(idParam);

        ServletModuleComment.getInstance().returnList(
            aRequest,
            aResponse,
            "exists (select * from comment_x_media where comment_id=comment.id and media_id=" + JDBCStringRoutines.escapeStringLiteral( idParam ) + ")", "", 0);
      }
      catch (Throwable t) {
        throw new ServletModuleFailure(t);
      }
    }
    else logger.error("editObjects: id not specified.");
  }

}