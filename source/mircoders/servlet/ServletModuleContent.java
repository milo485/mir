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
import mir.media.*;
import mir.module.*;
import mir.misc.*;
import mir.storage.*;
import mir.entity.*;

import mircoders.storage.*;
import mircoders.module.*;
import mircoders.entity.*;


/*
 *  ServletModuleContent -
 *  deliver html for the article admin form.
 *
 * @version $Id: ServletModuleContent.java,v 1.22 2002/11/04 04:35:22 mh Exp $
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
    try {
  		theLog = Logfile.getInstance(MirConfig.getProp("Home") + MirConfig.getProp("ServletModule.Content.Logfile"));
      templateListString = MirConfig.getProp("ServletModule.Content.ListTemplate");
      //templateOpString = MirConfig.getProp("ServletModule.Content.OpTemplate");
      templateObjektString = MirConfig.getProp("ServletModule.Content.ObjektTemplate");
      templateConfirmString = MirConfig.getProp("ServletModule.Content.ConfirmTemplate");
      mainModule = new ModuleContent(DatabaseContent.getInstance());
      themenModule = new ModuleTopics(DatabaseTopics.getInstance());
      schwerpunktModule = new ModuleSchwerpunkt(DatabaseFeature.getInstance());
      imageModule = new ModuleImages(DatabaseImages.getInstance());
    } catch (StorageObjectException e) {
      theLog.printDebugInfo("servletmodulecontent konnte nicht initialisiert werden");
    }
  }

  // Methoden

  public void list(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    try {
      EntityUsers user = _getUser(req);
      EntityList   theList;
      String       offsetParam = req.getParameter("offset");
      int          offset =0;

      // hier offsetcode bearbeiteb
      if (offsetParam != null && !offsetParam.equals(""))
          offset = Integer.parseInt(offsetParam);

      if (req.getParameter("next") != null)
          offset=Integer.parseInt(req.getParameter("nextoffset"));
      else
          if (req.getParameter("prev") != null)
            offset = Integer.parseInt(req.getParameter("prevoffset"));

      String        whereParam = req.getParameter("where");
      String        orderParam = req.getParameter("order");

      theList = ((ModuleContent)mainModule).getContent(whereParam, orderParam, offset, user);
      _list(theList, req, res);
    } catch (ModuleException e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void listop(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    try {
      EntityUsers user = _getUser(req);
      EntityList   theList;
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

      theList = ((ModuleContent)mainModule).getContent(whereParam, orderParam, offset, user);
      _list(theList, req, res);
    } catch (ModuleException e) {
      throw new ServletModuleException(e.toString());
    }
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
      _list(theList, req, res);
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
      //theLog.printDebugInfo(":: content :: got intersecting values");
      String now = StringUtil.date2webdbDate(new GregorianCalendar());
      withValues.put("date", now);
      withValues.put("publish_path", StringUtil.webdbDate2path(now));
      withValues.put("to_publisher", user.getId());
      withValues.put("is_produced", "0");
      if (!withValues.containsKey("is_published"))
        withValues.put("is_published","0");
      if (!withValues.containsKey("is_html"))
        withValues.put("is_html","0");

//      ML: this is not multi-language friendly and this can be done in a template
//      if (withValues.get("creator").toString().equals(""))
//        withValues.put("creator","Anonym");


      String id = mainModule.add(withValues);
      DatabaseContentToTopics.getInstance().setTopics(id,req.getParameterValues("to_topic"));
      //theLog.printDebugInfo(":: content :: inserted");
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
    // hier pruefen ob dem akt. user loeschen erlaubt ist...
    String idParam = req.getParameter("id");
    if (idParam == null) throw new ServletModuleException("Falscher Aufruf: (id) nicht angegeben");

    String confirmParam = req.getParameter("confirm");
    String cancelParam = req.getParameter("cancel");

    if (confirmParam == null && cancelParam == null) {
      // HTML Ausgabe zum Confirmen!
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

          /** @todo the following two should be imlied in
           *  DatabaseContent */

          //delete rows in the content_x_topic-table
          DatabaseContentToTopics.getInstance().deleteByContentId(idParam);
          //delete rows in the comment-table
          DatabaseComment.getInstance().deleteByContentId(idParam);
        } catch (ModuleException e) {
          throw new ServletModuleException(e.toString());
        } catch (StorageObjectException e) {
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
    if (idParam == null) throw new ServletModuleException("Falscher Aufruf: (id) nicht angegeben");
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
      theLog.printError("smod content :: attach :: could not get entityContent");
    }
    catch(StorageObjectException e) {
      theLog.printError("smod content :: attach :: could not get entityContent");
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
      theLog.printError("smod content :: dettach :: could not get entityContent");
    }
    catch(StorageObjectException e) {
      theLog.printError("smod content :: dettach :: could not get entityContent");
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
      theLog.printError("smod content :: newswire :: could not get entityContent");
    }
    catch(StorageObjectException e) {
      theLog.printError("smod content :: dettach :: could not get entityContent");
    }

    list(req, res);
  }


  public void update(HttpServletRequest req, HttpServletResponse res)
    throws ServletModuleException
  {
    try {

      EntityUsers   user = _getUser(req);
      if (user==null) theLog.printDebugInfo("user null!");
      String idParam = req.getParameter("id");
      if (idParam == null) throw new ServletModuleException("Wrong call: (id) is missing");

      HashMap withValues = getIntersectingValues(req, DatabaseContent.getInstance());
      String[] topic_id = req.getParameterValues("to_topic");
      String content_id = req.getParameter("id");
      // withValues.put("publish_path", StringUtil.webdbDate2path((String)withValues.get("date")));
      if(user != null) withValues.put("user_id", user.getId());
      withValues.put("is_produced", "0");
      if (!withValues.containsKey("is_published"))
        withValues.put("is_published","0");
      if (!withValues.containsKey("is_html"))
        withValues.put("is_html","0");

//      ML: this is not multi-language friendly and this can be done in a template
//      if (withValues.get("creator").toString().equals(""))
//        withValues.put("creator","Anonym");

      //theLog.printDebugInfo("updating. ");
      String id = mainModule.set(withValues);
      DatabaseContentToTopics.getInstance().setTopics(req.getParameter("id"),topic_id);
      //theLog.printDebugInfo("update done. ");
      String whereParam = req.getParameter("where");
      String orderParam = req.getParameter("order");
      if ((whereParam!=null && !whereParam.equals("")) || (orderParam!=null && !orderParam.equals(""))){
        //theLog.printDebugInfo("update to list");
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
      } else {
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
      try {
        extraInfo.put("articletypePopupData",
                        DatabaseArticleType.getInstance().getPopupData());
      } catch (Exception e) {
        theLog.printError("articletype could not be fetched.");
      }
      try {
        extraInfo.put("languagePopupData", DatabaseLanguage.getInstance().getPopupData());
      } catch (Exception e) {
        theLog.printError("language-popup could not be fetched.");
      }

      extraInfo.put("schwerpunktPopupData", schwerpunktModule.getSchwerpunktAsSimpleList());
      // hier code um zur liste zurueckzukommen
      String offsetParam, whereParam, orderParam;
      if ((offsetParam = req.getParameter("offset"))!=null) extraInfo.put("offset", offsetParam);
      if ((whereParam = req.getParameter("where"))!=null) extraInfo.put("where", whereParam);
      if ((orderParam = req.getParameter("order"))!=null) extraInfo.put("order", orderParam);
      extraInfo.put("login_user", _getUser(req));
      deliver(req, res, entContent, extraInfo, templateObjektString);
    } catch (Exception e) {
      throw new ServletModuleException(e.toString());
    }
  }


  public void _list(EntityList theList, HttpServletRequest req, HttpServletResponse res)
    throws ServletModuleException {

    try {
      // hier dann htmlcode rausschreiben
      if (theList == null || theList.getCount() == 0 || theList.getCount()>1) {
        SimpleHash modelRoot = HTMLTemplateProcessor.makeSimpleHashWithEntitylistInfos(theList);
        modelRoot.put("themenHashData", themenModule.getHashData());
        modelRoot.put("schwerpunktHashData", schwerpunktModule.getHashData());
        modelRoot.put("articletypeHash", DatabaseArticleType.getInstance().getHashData());
        deliver(req, res, modelRoot, templateListString);
      } else  { // count = 1
        _showObject(theList.elementAt(0).getId(),req,res);
      }
    } catch (StorageObjectException e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void _listop(EntityList theList, HttpServletRequest req, HttpServletResponse res)
    throws ServletModuleException {

    try {
      // delivering html
      if (theList == null || theList.getCount() == 0 || theList.getCount()>1) {
        SimpleHash modelRoot = HTMLTemplateProcessor.makeSimpleHashWithEntitylistInfos(theList);
        modelRoot.put("articletypeHash", DatabaseArticleType.getInstance().getHashData());

    EntityContent       currentContent;
    EntityList          upMediaEntityList;
    EntityList          imageEntityList;
    EntityList          currentMediaList;
    Entity              mediaType;
    EntityMedia         uploadedMedia;
    SimpleList          opList;
      String imageRoot = MirConfig.getProp("Producer.ImageRoot");

    SimpleHash          contentHash;
    Class               mediaHandlerClass=null;
    MirMedia            mediaHandler=null;
    String              mediaHandlerName=null;
    Database            mediaStorage=null;
    String              tinyIcon;
    String              iconAlt;

      for (int i=0; i < theList.size();i++) {
        currentContent = (EntityContent)theList.elementAt(i);
        //fetching/setting the images
        upMediaEntityList = DatabaseContentToMedia.getInstance().getUploadedMedia(currentContent);
        if (upMediaEntityList!=null && upMediaEntityList.getCount()>=1) {
          tinyIcon = null;
          iconAlt = null;
          mediaHandler = null;
          mediaHandlerName = null;
          for (int n=0; n < upMediaEntityList.size();n++) {
            uploadedMedia = (EntityMedia)upMediaEntityList.elementAt(n);
            mediaType = uploadedMedia.getMediaType();

            //must of had a non-existant to_media_type entry..
            //let's save our ass.
            if (mediaType != null) {
                try {
                  mediaHandlerName = mediaType.getValue("classname");
                  mediaHandlerClass = Class.forName("mir.media.MediaHandler"+mediaHandlerName);
                  mediaHandler = (MirMedia)mediaHandlerClass.newInstance();
                } catch (Exception e) {
                  theLog.printError("ProducerStartpage:problem in reflection: "+mediaHandlerName);
                }

                //the "best" media type to show
                if (mediaHandler.isVideo()) {
                  tinyIcon = MirConfig.getProp("Producer.Icon.TinyVideo");
                  iconAlt = "Video";
                  break;
                } else if (mediaHandler.isAudio()) {
                  tinyIcon = MirConfig.getProp("Producer.Icon.TinyAudio");
                  iconAlt = "Audio";
                } else if (tinyIcon == null && !mediaHandler.isImage()) {
                  tinyIcon = mediaHandler.getTinyIconName();
                  iconAlt = mediaHandler.getIconAltName();
                }
            }
          }
          //it only has image(s)
          if (tinyIcon == null) {
            tinyIcon = MirConfig.getProp("Producer.Icon.TinyImage");
            iconAlt = "Image";
          }

        // uploadedMedia Entity list is empty.
        // we only have text
        } else {
          tinyIcon = MirConfig.getProp("Producer.Icon.TinyText");
          iconAlt = "Text";
        }

        try{
          //mediaList = HTMLTemplateProcessor.makeSimpleList(upMediaEntityList);
          contentHash = (SimpleHash)theList.get(i);
          contentHash.put("tiny_icon", imageRoot+"/"+tinyIcon);
          contentHash.put("icon_alt", iconAlt);
        } catch (Exception e){}
      }


        deliver(req, res, modelRoot, templateListString);
      } else  { // count = 1
        _showObject(theList.elementAt(0).getId(), req, res);
      }
    } catch (StorageObjectException e) {
      throw new ServletModuleException(e.toString());
    }
  }

  private EntityUsers _getUser(HttpServletRequest req)
  {
    HttpSession session=req.getSession(false);
    return (EntityUsers)session.getAttribute("login.uid");
  }
}

