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

package mir.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.entity.EntityList;
import mir.log.LoggerWrapper;
import mir.misc.HTMLParseException;
import mir.misc.HTMLTemplateProcessor;
import mir.misc.LineFilterWriter;
import mir.module.AbstractModule;
import mir.module.ModuleException;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModelRoot;


/**
 * Abstract class ServletModule provides the base functionality for servlets.
 * Deriving a class from ServletModule enables class to insert/edit/update/delete
 * and list Entity from a Database via mainModule.
 *
 *
 *  Abstrakte Klasse ServletModule stellt die Basisfunktionalitaet der
 *  abgeleiteten ServletModule zur Verf?gung.
 *
 * @version 28.6.1999
 * @author RK
 */

public abstract class ServletModule {

  public String defaultAction;
  protected LoggerWrapper logger;
	protected MirPropertiesConfiguration configuration;
  protected AbstractModule mainModule;
  protected String templateListString;
  protected String templateObjektString;
  protected String templateConfirmString;
  
  
  public ServletModule(){
    try {
      configuration = MirPropertiesConfiguration.instance();
    } catch (PropertiesConfigExc e) {
      e.printStackTrace(System.err);
    }
  }
  

  /**
   * Singelton - Methode muss in den abgeleiteten Klassen ueberschrieben werden.
   * @return ServletModule
   */
  public static ServletModule getInstance() {
    return null;
  }

  /**
   * get the module name to be used for generic operations like delete.
   */
  protected String getOperationModuleName() {
    return getClass().getName().substring((new String("mircoders.servlet.ServletModule")).length());
  }

  /**
   * get the session binded language
   */
  public String getLanguage(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    String language = (String) session.getAttribute("Language");
    if (language == null) {
      language = configuration.getString("StandardLanguage");
    }
    return language;
  }

  /**
   * get the locale either from the session or the accept-language header ot the request
   * this supersedes getLanguage for the new i18n
   */
  public Locale getLocale(HttpServletRequest req) {
    Locale loc = null;
    HttpSession session = req.getSession(false);
    if (session != null) {
      // session can be null in case of logout
      loc = (Locale) session.getAttribute("Locale");
    }
    // if there is nothing in the session get it fron the accept-language
    if (loc == null) {
      loc = req.getLocale();
    }
    return loc;
  }

  public void redirect(HttpServletResponse aResponse, String aQuery) throws ServletModuleException {
    try {
      aResponse.sendRedirect(MirPropertiesConfiguration.instance().getString("RootUri") + "/Mir?"+aQuery);
    }
    catch (Throwable t) {
      throw new ServletModuleException(t.getMessage());
    }
  }

  /**
   *  list(req,res) - generische Listmethode. Wennn die Funktionalitaet
   *  nicht reicht, muss sie in der abgeleiteten ServletModule-Klasse
   *  ueberschreiben werden.
   *
   * @param req Http-Request, das vom Dispatcher durchgereicht wird
   * @param res Http-Response, die vom Dispatcher durchgereicht wird
   */
  public void list(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException {
    try {
      EntityList theList;
      String offsetParam = req.getParameter("offset");
      int offset = 0;
      PrintWriter out = res.getWriter();

      // hier offsetcode bearbeiten
      if (offsetParam != null && !offsetParam.equals("")) {
        offset = Integer.parseInt(offsetParam);
      }
      if (req.getParameter("next") != null) {
        offset = Integer.parseInt(req.getParameter("nextoffset"));
      }
      else {
        if (req.getParameter("prev") != null) {
          offset = Integer.parseInt(req.getParameter("prevoffset"));
        }
      }
      theList = mainModule.getByWhereClause(null, offset);

      HTMLTemplateProcessor.process(res, templateListString, theList, out, getLocale(req));
    }
    catch (Exception e) {
      throw new ServletModuleException(e.getMessage());
    }
  }

  /**
   *  add(req,res) - generische Addmethode. Wennn die Funktionalitaet
   *  nicht reicht, muss sie in der abgeleiteten ServletModule-Klasse
   *  ueberschreiben werden.
   * @param req Http-Request, das vom Dispatcher durchgereicht wird
   * @param res Http-Response, die vom Dispatcher durchgereicht wird
   */
  public void add(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException {

    try {
      SimpleHash mergeData = new SimpleHash();
      mergeData.put("new", "1");
      deliver(req, res, mergeData, templateObjektString);
    }
    catch (Exception e) {
      throw new ServletModuleException(e.getMessage());
    }
  }

  /**
   *  insert(req,res) - generische Insertmethode, folgt auf add.
   *  Wennn die Funktionalitaet
   *  nicht reicht, muss sie in der abgeleiteten ServletModule-Klasse
   *  ueberschreiben werden.
   *
   * @param req Http-Request, das vom Dispatcher durchgereicht wird
   * @param res Http-Response, die vom Dispatcher durchgereicht wird
   */
  public void insert(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException, ServletModuleUserException {
    try {
      HashMap withValues = getIntersectingValues(req, mainModule.getStorageObject());
      logger.debug("--trying to add...");
      String id = mainModule.add(withValues);
      logger.debug("--trying to deliver..." + id);
      list(req, res);
    }
    catch (Exception e) {
      throw new ServletModuleException(e.getMessage());
    }
  }

  /**
   *  delete(req,res) - generic delete method. Can be overridden in subclasses.
   *
   */

  public void delete(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException {
    try {
      String idParam = req.getParameter("id");

      if (idParam == null)
        throw new ServletModuleException("Invalid call to delete: no id supplied");

      String confirmParam = req.getParameter("confirm");
      String cancelParam = req.getParameter("cancel");
      if (confirmParam == null && cancelParam == null) {
        SimpleHash mergeData = new SimpleHash();

        mergeData.put("module", getOperationModuleName());
        mergeData.put("infoString", getOperationModuleName() + ": " + idParam);
        mergeData.put("id", idParam);
        mergeData.put("where", req.getParameter("where"));
        mergeData.put("order", req.getParameter("order"));
        mergeData.put("offset", req.getParameter("offset"));
        // this stuff is to be compatible with the other more advanced
        // search method used for media and comments
        mergeData.put("query_media_folder", req.getParameter("query_media_folder"));
        mergeData.put("query_is_published", req.getParameter("query_is_published"));
        mergeData.put("query_text", req.getParameter("query_text"));
        mergeData.put("query_field", req.getParameter("query_field"));

        deliver(req, res, mergeData, templateConfirmString);
      }
      else {
        if (confirmParam != null && !confirmParam.equals("")) {
          //theLog.printInfo("delete confirmed!");
          mainModule.deleteById(idParam);
          list(req, res); // back to list
        }
        else {
          if (req.getParameter("where") != null)
            list(req, res);
          else
            edit(req, res);
        }
      }
    }
    catch (Exception e) {
      throw new ServletModuleException(e.getMessage());
    }
  }

  /**
   *  edit(req,res) - generische Editmethode. Wennn die Funktionalitaet
   *  nicht reicht, muss sie in der abgeleiteten ServletModule-Klasse
   *  ueberschreiben werden.
   *
   * @param req Http-Request, das vom Dispatcher durchgereicht wird
   * @param res Http-Response, die vom Dispatcher durchgereicht wird
   */
  public void edit(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException {
    try {
      String idParam = req.getParameter("id");
      deliver(req, res, mainModule.getById(idParam), templateObjektString);
    }
    catch (ModuleException e) {
      throw new ServletModuleException(e.getMessage());
    }
  }

  /**
   *  update(req,res) - generische Updatemethode. Wennn die Funktionalitaet
   *  nicht reicht, muss sie in der abgeleiteten ServletModule-Klasse
   *  ueberschreiben werden.
   *
   * @param req Http-Request, das vom Dispatcher durchgereicht wird
   * @param res Http-Response, die vom Dispatcher durchgereicht wird
   */

  public void update(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException {
    try {
      String idParam = req.getParameter("id");
      HashMap withValues = getIntersectingValues(req, mainModule.getStorageObject());

      String id = mainModule.set(withValues);
      String whereParam = req.getParameter("where");
      String orderParam = req.getParameter("order");

      if ((whereParam != null && !whereParam.equals("")) || (orderParam != null && !orderParam.equals(""))) {
        list(req, res);
      }
      else {
        edit(req, res);
      }
    }
    catch (Exception e) {
      throw new ServletModuleException(e.getMessage());
    }
  }

  /**
   * deliver liefert das Template mit dem Filenamen templateFilename
   * an den HttpServletResponse res aus, nachdem es mit den Daten aus
   * TemplateModelRoot rtm gemischt wurde
   *
   * @param res Http-Response, die vom Dispatcher durchgereicht wird
   * @param rtm beinahalten das freemarker.template.TempalteModelRoot mit den
   *   Daten, die ins Template gemerged werden sollen.
   * @param tmpl Name des Templates
   * @exception ServletModuleException
   */
  public void deliver(HttpServletRequest req, HttpServletResponse res,
                      TemplateModelRoot rtm, TemplateModelRoot popups,
                      String templateFilename)
      throws ServletModuleException {
    if (rtm == null) rtm = new SimpleHash();
    try {
      PrintWriter out = res.getWriter();
      HTMLTemplateProcessor.process(res, templateFilename, rtm, popups, out, getLocale(req));

      // we default to admin bundles here, which is not exactly beautiful...
      // but this whole producer stuff is going to be rewritten soon.
      // ServletModuleOpenIndy overwrites deliver() to use open bundles
      // (br1)
      out.close();
    }
    catch (HTMLParseException e) {
      throw new ServletModuleException(e.getMessage());
    } catch (IOException e) {
      throw new ServletModuleException(e.getMessage());
    }
  }


  /**
   * deliver liefert das Template mit dem Filenamen templateFilename
   * an den HttpServletResponse res aus, nachdem es mit den Daten aus
   * TemplateModelRoot rtm gemischt wurde
   *
   * @param res Http-Response, die vom Dispatcher durchgereicht wird
   * @param rtm beinahalten das freemarker.template.TempalteModelRoot mit den
   *   Daten, die ins Template gemerged werden sollen.
   * @param tmpl Name des Templates
   * @exception ServletModuleException
   */
  public void deliver(HttpServletRequest req, HttpServletResponse res,
                      TemplateModelRoot rtm, String templateFilename)
      throws ServletModuleException {
    deliver(req, res, rtm, null, templateFilename);
  }

  /**
   * deliver liefert das Template mit dem Filenamen templateFilename
   * an den HttpServletResponse res aus, nachdem es mit den Daten aus
   * TemplateModelRoot rtm gemischt wurde
   *
   * @param res Http-Response, die vom Dispatcher durchgereicht wird
   * @param rtm beinahalten das freemarker.template.TempalteModelRoot mit den
   *   Daten, die ins Template gemerged werden sollen.
   * @param tmpl Name des Templates
   * @exception ServletModuleException
   */
  public void deliver_compressed(HttpServletRequest req, HttpServletResponse res,
                                 TemplateModelRoot rtm, String templateFilename)
      throws ServletModuleException {
    if (rtm == null) rtm = new SimpleHash();
    try {
      PrintWriter out = new LineFilterWriter(res.getWriter());
      //PrintWriter out =  res.getWriter();
      HTMLTemplateProcessor.process(res, templateFilename, rtm, out, getLocale(req));
      out.close();
    }
    catch (HTMLParseException e) {
      throw new ServletModuleException(e.getMessage());
    }
    catch (IOException e) {
      throw new ServletModuleException(e.getMessage());
    }
  }

  /**
   * deliver liefert das Template mit dem Filenamen templateFilename
   * an den HttpServletResponse res aus, nachdem es mit den Daten aus
   * TemplateModelRoot rtm gemischt wurde
   *
   * @param out ist der OutputStream, in den die gergten Daten geschickt werden sollen.
   * @param rtm beinahalten das freemarker.template.TempalteModelRoot mit den
   *   Daten, die ins Template gemerged werden sollen.
   * @param tmpl Name des Templates
   * @exception ServletModuleException
   */
  private void deliver(HttpServletResponse res, HttpServletRequest req, PrintWriter out,
                       TemplateModelRoot rtm, String templateFilename)
      throws HTMLParseException {
    HTMLTemplateProcessor.process(res, templateFilename, rtm, out, getLocale(req));
  }

  /**
   *  Wenn die abgeleitete Klasse diese Methode ueberschreibt und einen String mit einem
   *  Methodennamen zurueckliefert, dann wird diese Methode bei fehlender Angabe des
   *  doParameters ausgefuehrt.
   *
   * @return Name der Default-Action
   */
  public String defaultAction() {
    return defaultAction;
  }

  /**
   *  Hier kann vor der Datenaufbereitung schon mal ein response geschickt
   *  werden (um das subjektive Antwortverhalten bei langsamen Verbindungen
   *  zu verbessern).
   */
  public void predeliver(HttpServletRequest req, HttpServletResponse res) {
    ;
  }

  /**
   * Holt die Felder aus der Metadatenfelderliste des StorageObjects, die
   * im HttpRequest vorkommen und liefert sie als HashMap zurueck
   *
   * @return HashMap mit den Werten
   */
  public HashMap getIntersectingValues(HttpServletRequest req, StorageObject theStorage)
      throws ServletModuleException {
    ArrayList theFieldList;
    try {
      theFieldList = theStorage.getFields();
    }
    catch (StorageObjectFailure e) {
      throw new ServletModuleException("ServletModule.getIntersectingValues: " + e.getMessage());
    }

    HashMap withValues = new HashMap();
    String aField, aValue;

    for (int i = 0; i < theFieldList.size(); i++) {
      aField = (String) theFieldList.get(i);
      aValue = req.getParameter(aField);
      if (aValue != null) withValues.put(aField, aValue);
    }
    return withValues;
  }

}
