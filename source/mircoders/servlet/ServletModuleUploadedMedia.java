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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mir.config.MirPropertiesConfiguration;
import mir.entity.Entity;
import mir.entity.EntityList;
import mir.log.LoggerWrapper;
import mir.media.MediaHelper;
import mir.media.MirMedia;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.servlet.ServletModuleUserExc;
import mir.session.UploadedFile;
import mir.util.ExceptionFunctions;
import mir.util.HTTPParsedRequest;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.entity.EntityUploadedMedia;
import mircoders.entity.EntityUsers;
import mircoders.media.MediaUploadProcessor;
import mircoders.module.*;
import mircoders.storage.DatabaseComment;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseMediafolder;

import org.apache.commons.fileupload.FileItem;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleList;

/*
 *  ServletModuleBilder -
 *  liefert HTML fuer Bilder
 *
 * @version $Id: ServletModuleUploadedMedia.java,v 1.27 2003/04/26 00:42:22 zapata Exp $
 * @author RK, the mir-coders group
 */

public abstract class ServletModuleUploadedMedia
        extends mir.servlet.ServletModule {

  //private static DatabaseRights dbRights;

  public static ServletModule getInstance() {
    return null;
  }

  public ServletModuleUploadedMedia() {
    super();
    logger = new LoggerWrapper("ServletModule.UploadedMedia");
  }

  public void insert(HttpServletRequest req, HttpServletResponse res)
          throws ServletModuleExc, ServletModuleUserExc {
    try {
      HTTPParsedRequest parsedRequest = new HTTPParsedRequest(req,
          configuration.getString("Mir.DefaultEncoding"),
          configuration.getInt("MaxMediaUploadSize")*1024,
          configuration.getString("TempDir"));

      EntityUsers user = _getUser(req);
      Map mediaValues = new HashMap();

      mediaValues.put("to_publisher", _getUser(req).getId());

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
          entContent.attach(((EntityUploadedMedia) i.next()).getId());
        }

        ((ServletModuleContent) ServletModuleContent.getInstance())._showObject(articleid, req, res);

        return;
      }

      if (commentid!=null) {
        EntityComment comment = (EntityComment) DatabaseComment.getInstance().selectById(commentid);

        i=mediaList.iterator();

        while (i.hasNext()) {
          comment.attach( ( (EntityUploadedMedia) i.next()).getId());
        }

        ((ServletModuleComment) ServletModuleComment.getInstance()).showComment(commentid, req, res);

        return;
      }

      SimpleHash mergeData = new SimpleHash();
      SimpleHash popups = new SimpleHash();
      mergeData.put("contentlist", mir.generator.FreemarkerGenerator.makeAdapter(mediaList));

      mergeData.put("count", Integer.toString(mediaList.size()));
      mergeData.put("from", "1");
      mergeData.put("to", Integer.toString(mediaList.size()));

      //fetch the popups
      popups.put("mediafolderPopupData", DatabaseMediafolder.getInstance().getPopupData());
      // raus damit
      deliver(req, res, mergeData, popups, templateListString);
    }
    catch (Throwable t) {
      Throwable cause = ExceptionFunctions.traceCauseException(t);

      if (cause instanceof ModuleMediaType.UnsupportedMimeTypeExc) {
        throw new ServletModuleUserExc("media.error.unsupportedformat", new String[] {});
      }
      throw new ServletModuleFailure("ServletModuleUploadedMedia.insert: " + t.toString(), t);
    }
  }

  public void update(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc {

    try {
      HTTPParsedRequest parsedRequest = new HTTPParsedRequest(req,
          configuration.getString("Mir.DefaultEncoding"),
          configuration.getInt("MaxMediaUploadSize")*1024,
          configuration.getString("TempDir"));
      EntityUsers user = _getUser(req);
      Map mediaValues = new HashMap();

      Iterator i = mainModule.getStorageObject().getFields().iterator();
      while (i.hasNext()) {
        String field = (String) i.next();
        String value = parsedRequest.getParameter(field);
        if (value!=null)
          mediaValues.put(field, value);
      }

      mediaValues.put("to_publisher", user.getId());
      mediaValues.put("is_produced", "0");
      if (!mediaValues.containsKey("is_published"))
        mediaValues.put("is_published", "0");

      String id = mainModule.set(mediaValues);
      logger.debug("update: media ID = " + id);
      _edit(id, req, res);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure("upload -- exception " + e.toString(), e);
    }

  }


  public void list(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc {
    // Parameter auswerten
    SimpleHash mergeData = new SimpleHash();
    SimpleHash popups = new SimpleHash();

    String query_text = req.getParameter("query_text");
    mergeData.put("query_text", query_text);
    if (query_text != null) mergeData.put("query_text_encoded", URLEncoder.encode(query_text));
    String query_field = req.getParameter("query_field");
    mergeData.put("query_field", query_field);
    String query_is_published = req.getParameter("query_is_published");
    mergeData.put("query_is_published", query_is_published);
    String query_media_folder = req.getParameter("query_media_folder");
    mergeData.put("query_media_folder", query_media_folder);
    String offset = req.getParameter("offset");
    if (offset == null || offset.equals("")) offset = "0";
    mergeData.put("offset", offset);

    String order = req.getParameter("order");
    if (order == null || order.equals("")) order = "webdb_lastchange desc";

    // if in connection mode to content
    mergeData.put("articleid", req.getParameter("articleid"));
    mergeData.put("commentid", req.getParameter("commentid"));


    // sql basteln
    String whereClause = "";
    boolean isFirst = true;
    if (query_text != null && !query_text.equalsIgnoreCase("")) {
      whereClause += "lower(" + query_field + ") like lower('%" + query_text + "%')";
      isFirst = false;
    }
    if (query_is_published != null && !query_is_published.equals("")) {
      if (isFirst == false) whereClause += " and ";
      whereClause += "is_published='" + query_is_published + "'";
      isFirst = false;
    }
    if (query_media_folder != null && !query_media_folder.equals("")) {
      if (isFirst == false) whereClause += " and ";
      whereClause += "to_media_folder='" + query_media_folder + "'";
    }
    //theLog.printDebugInfo("sql-whereclause: " + whereClause + " order: " + order + " offset: " + offset);

    // fetch and deliver
    try {
      if (query_text != null || query_is_published != null || query_media_folder != null) {
        EntityList theList = mainModule.getByWhereClause(whereClause, order, (new Integer(offset)).intValue(), 10);
        if (theList != null) {
          mergeData.put("contentlist", theList);
          if (theList.getOrder() != null) {
            mergeData.put("order", theList.getOrder());
            mergeData.put("order_encoded", URLEncoder.encode(theList.getOrder()));
          }
          mergeData.put("count", (new Integer(theList.getCount())).toString());
          mergeData.put("from", (new Integer(theList.getFrom())).toString());
          mergeData.put("to", (new Integer(theList.getTo())).toString());
          if (theList.hasNextBatch())
            mergeData.put("next", (new Integer(theList.getNextBatch())).toString());
          if (theList.hasPrevBatch())
            mergeData.put("prev", (new Integer(theList.getPrevBatch())).toString());
        }
      }
      popups.put("mediafolderPopupData", DatabaseMediafolder.getInstance().getPopupData());

      deliver(req, res, mergeData, popups, templateListString);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }


  public void add(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc {
    try {
      SimpleHash mergeData = new SimpleHash();
      SimpleHash popups = new SimpleHash();
      String maxMedia = MirPropertiesConfiguration.instance().getString("ServletModule.OpenIndy.MaxMediaUploadItems");
      String numOfMedia = req.getParameter("medianum");

      mergeData.put("new", "1");
      mergeData.put("articleid", req.getParameter("articleid"));
      mergeData.put("commentid", req.getParameter("commentid"));

      popups.put("mediafolderPopupData", DatabaseMediafolder.getInstance().getPopupData());

      if (numOfMedia==null || numOfMedia.equals("")) {
        numOfMedia="1";
      }
      else if(Integer.parseInt(numOfMedia) > Integer.parseInt(maxMedia)) {
        numOfMedia = maxMedia;
      }

      int mediaNum = Integer.parseInt(numOfMedia);
      SimpleList mediaFields = new SimpleList();
      for(int i =0; i<mediaNum;i++){
        Integer mNum = new Integer(i+1);
        mediaFields.add(mNum.toString());
      }
      mergeData.put("medianum",numOfMedia);
      mergeData.put("mediafields",mediaFields);
      deliver(req, res, mergeData, popups, templateObjektString);
    }
    catch (Exception e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void edit(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc {
    String idParam = req.getParameter("id");
    _edit(idParam, req, res);
  }

  private void _edit(String idParam, HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc {
    if (idParam != null && !idParam.equals("")) {
      try {
        SimpleHash popups = new SimpleHash();
        popups.put("mediafolderPopupData", DatabaseMediafolder.getInstance().getPopupData());
        deliver(req, res, mainModule.getById(idParam), popups,
                templateObjektString);
      }
      catch (Throwable e) {
        throw new ServletModuleFailure(e);
      }
    }
    else {
      throw new ServletModuleExc("ServletmoduleUploadedMedia :: _edit without id");
    }
  }


  /** @todo should be in ServletModule.java */
  private EntityUsers _getUser(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    return (EntityUsers) session.getAttribute("login.uid");
  }

  public void getMedia(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc {
    String idParam = req.getParameter("id");
    if (idParam!=null && !idParam.equals("")) {
      try {
        EntityUploadedMedia ent = (EntityUploadedMedia)mainModule.getById(idParam);
        Entity mediaType = ent.getMediaType();
        MirMedia mediaHandler;

        ServletContext ctx = MirPropertiesConfiguration.getContext();
        String fName = ent.getId()+"."+mediaType.getValue("name");

        mediaHandler = MediaHelper.getHandler(mediaType);
        InputStream in = mediaHandler.getMedia(ent, mediaType);

        res.setContentType(ctx.getMimeType(fName));
        //important that before calling this res.getWriter was not called first
        ServletOutputStream out = res.getOutputStream();

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

  public void getIcon(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc
  {
    String idParam = req.getParameter("id");
    if (idParam!=null && !idParam.equals("")) {
      try {
        EntityUploadedMedia ent = (EntityUploadedMedia)mainModule.getById(idParam);
        Entity mediaType = ent.getMediaType();
        MirMedia mediaHandler;

        ServletContext ctx = MirPropertiesConfiguration.getContext();
        String fName = ent.getId()+"."+mediaType.getValue("name");

        mediaHandler = MediaHelper.getHandler(mediaType);
        InputStream in = mediaHandler.getIcon(ent);

        res.setContentType(ctx.getMimeType(fName));
        //important that before calling this res.getWriter was not called first
        ServletOutputStream out = res.getOutputStream();

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
    else logger.error("getIcon: id not specified.");
    // no exception allowed
  }

}


