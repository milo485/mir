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
import freemarker.template.SimpleList;
import mir.entity.Entity;
import mir.entity.EntityList;
import mir.media.MediaHelper;
import mir.media.MirMedia;
import mir.media.MirMediaException;
import mir.media.MirMediaUserException;
import mir.misc.MirConfig;
import mir.misc.MpRequest;
import mir.misc.StringUtil;
import mir.misc.WebdbMultipartRequest;
import mir.module.ModuleException;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleException;
import mir.servlet.ServletModuleUserException;
import mir.storage.Database;
import mir.storage.StorageObjectException;
import mircoders.entity.EntityUsers;
import mircoders.storage.DatabaseMediaType;
import mircoders.storage.DatabaseMediafolder;
import mircoders.media.MediaRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.GregorianCalendar;
import java.util.HashMap;

/*
 *  ServletModuleBilder -
 *  liefert HTML fuer Bilder
 *
 *
 * @author RK
 */

public abstract class ServletModuleUploadedMedia
        extends mir.servlet.ServletModule {

  //private static DatabaseRights dbRights;

  public static ServletModule getInstance() {
    return null;
  }

  public void insert(HttpServletRequest req, HttpServletResponse res)
          throws ServletModuleException, ServletModuleUserException {
    try {
      WebdbMultipartRequest mp = new WebdbMultipartRequest(req);
      EntityUsers user = _getUser(req);
      EntityList mediaList =
        new MediaRequest(mp, user.getId()).getMedia(false, false);
      list(req, res);
    }
    catch (MirMediaUserException e) {
      throw new ServletModuleUserException(e.getMsg());
    }
    catch (MirMediaException e) {
      throw new ServletModuleException(
              "upload -- media handling exception " + e.toString());
    }
    catch (IOException e) {
      throw new ServletModuleException("upload -- ioexception " + e.toString());
    }
  }

  public void update(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException {

    try {
      WebdbMultipartRequest mp = new WebdbMultipartRequest(req);
      HashMap parameters = mp.getParameters();

      EntityUsers user = _getUser(req);
      parameters.put("to_publisher", user.getId());
      parameters.put("is_produced", "0");
      if (!parameters.containsKey("is_published"))
        parameters.put("is_published", "0");

      String id = mainModule.set(parameters);
      theLog.printError("media ID" + id);
      _edit(id, req, res);
    }
    catch (IOException e) {
      throw new ServletModuleException("upload -- ioexception " + e.toString());
    }
    catch (ModuleException e) {
      throw new ServletModuleException("upload -- moduleexception " + e.toString());
    }

  }


  public void list(HttpServletRequest req, HttpServletResponse res)
          throws ServletModuleException {
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
    if (order == null) order = "webdb_lastchange desc";

    // if in connection mode to content
    String cid = req.getParameter("cid");
    mergeData.put("cid", cid);


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

    // fetch und ausliefern
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
      //fetch the popups
      popups.put("mediafolderPopupData", DatabaseMediafolder.getInstance().getPopupData());
      // raus damit
      deliver(req, res, mergeData, popups, templateListString);
    }
    catch (ModuleException e) {
      throw new ServletModuleException(e.toString());
    }
    catch (Exception e) {
      throw new ServletModuleException(e.toString());
    }
  }


  public void add(HttpServletRequest req, HttpServletResponse res)
          throws ServletModuleException {
    try {
      SimpleHash mergeData = new SimpleHash();
      mergeData.put("new", "1");
      SimpleHash popups = new SimpleHash();
      popups.put("mediafolderPopupData", DatabaseMediafolder.getInstance().getPopupData());
      String maxMedia = MirConfig.getProp("ServletModule.OpenIndy.MaxMediaUploadItems");
      String numOfMedia = req.getParameter("medianum");
      if(numOfMedia==null||numOfMedia.equals("")){
        numOfMedia="1";
      } else if(Integer.parseInt(numOfMedia) > Integer.parseInt(maxMedia)) {
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
    } catch (Exception e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void edit(HttpServletRequest req, HttpServletResponse res)
          throws ServletModuleException {
    String idParam = req.getParameter("id");
    _edit(idParam, req, res);
  }

  private void _edit(String idParam, HttpServletRequest req, HttpServletResponse res)
          throws ServletModuleException {
    if (idParam != null && !idParam.equals("")) {
      try {
        SimpleHash popups = new SimpleHash();
        popups.put("mediafolderPopupData", DatabaseMediafolder.getInstance().getPopupData());
        deliver(req, res, mainModule.getById(idParam), popups,
                templateObjektString);
      }
      catch (ModuleException e) {
        throw new ServletModuleException(e.toString());
      }
      catch (StorageObjectException e) {
        throw new ServletModuleException(e.toString());
      }
    }
    else {
      throw new ServletModuleException("ServletmoduleUploadedMedia :: _edit without id");
    }
  }


  /** @todo should be in ServletModule.java */
  private EntityUsers _getUser(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    return (EntityUsers) session.getAttribute("login.uid");
  }

}


