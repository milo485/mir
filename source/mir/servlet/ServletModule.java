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
package mir.servlet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.entity.adapter.EntityAdapterDefinition;
import mir.entity.adapter.EntityAdapterEngine;
import mir.entity.adapter.EntityAdapterModel;
import mir.log.LoggerWrapper;
import mir.module.AbstractModule;
import mir.storage.StorageObject;
import mir.util.HTTPRequestParser;
import mir.util.URLBuilder;
import mircoders.servlet.ServletHelper;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public abstract class ServletModule {
  public String defaultAction;
  protected LoggerWrapper logger;
  protected MirPropertiesConfiguration configuration;
  protected Locale fallbackLocale;

  protected AbstractModule mainModule;
  protected String definition;
  protected EntityAdapterModel model;

  protected String listGenerator;
  protected String editGenerator;
  protected String deleteConfirmationGenerator;
  protected int nrEntitiesPerListPage;


  public ServletModule(){
    definition = null;
    model = null;

    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (PropertiesConfigExc e) {
      throw new RuntimeException("Can't get configuration: " + e.getMessage());
    }

    listGenerator = configuration.getString("ServletModule."+getOperationModuleName()+".ListTemplate");
    editGenerator = configuration.getString("ServletModule."+getOperationModuleName()+".EditTemplate");
    deleteConfirmationGenerator = configuration.getString("ServletModule."+getOperationModuleName()+".DeleteConfirmationTemplate");
    nrEntitiesPerListPage =
        configuration.getInt("ServletModule."+getOperationModuleName()+".ListSize",
        configuration.getInt("ServletModule.Default.ListSize", 20));

    fallbackLocale = new Locale(configuration.getString("Mir.Admin.FallbackLanguage", "en"), "");
  }


  /**
   * Singleton instance retrievel method. MUST be overridden in subclasses.
   *
   * @return ServletModule the single instance of the servletmodule class
   */
  public static ServletModule getInstance() {
    return null;
  }

  /**
   * Get the module name
   *
   * @return
   */
  protected String getOperationModuleName() {
    return getClass().getName().substring((new String("mircoders.servlet.ServletModule")).length());
  }

  /**
   * get the locale either from the session or the accept-language header ot the request
   * this supersedes getLanguage for the new i18n
   */
  public Locale getLocale(HttpServletRequest aRequest) {
    Locale loc = null;
    HttpSession session = aRequest.getSession(false);
    if (session != null) {
      // session can be null in case of logout
      loc = (Locale) session.getAttribute("locale");
    }
    // if there is nothing in the session get it fron the accept-language
    if (loc == null) {
      loc = aRequest.getLocale();
    }
    return loc;
  }

  /**
   * get the locale either from the session or the accept-language header ot the request
   * this supersedes getLanguage for the new i18n
   */
  public Locale getFallbackLocale(HttpServletRequest aRequest) {
    return fallbackLocale;
  }

  /**
   * Function to specify the default ordering for lists. May be overridden.
   *
   *
   * @return
   */
  public String getDefaultListOrdering() {

    if (mainModule!=null && mainModule.getStorageObject()!=null){
      if (mainModule.getStorageObject().getFields().contains("webdb_create"))
        return "webdb_create desc";
    }

    return "id asc";
  }

  /**
   *
   * @param aResponse
   * @param aQuery
   * @throws ServletModuleExc
   * @throws ServletModuleFailure
   */
  public void redirect(HttpServletResponse aResponse, String aQuery) throws ServletModuleExc, ServletModuleFailure {
    try {
      aResponse.sendRedirect(aResponse.encodeRedirectURL(MirPropertiesConfiguration.instance().getString("RootUri") + "/Mir?"+aQuery));
    }
    catch (Throwable t) {
      throw new ServletModuleFailure("ServletModule.redirect: " +t.getMessage(), t);
    }
  }

  /**
   * Generic list method
   *
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */

  public void list(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    String where = requestParser.getParameter("where");
    String order = requestParser.getParameterWithDefault("order", getDefaultListOrdering());
    int offset = requestParser.getIntegerWithDefault("offset", 0);

    returnList(aRequest, aResponse, where, order, offset);
  }


  public void returnList(HttpServletRequest aRequest, HttpServletResponse aResponse,
     String aWhereClause, String anOrderByClause, int anOffset) throws ServletModuleExc {

    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    URLBuilder urlBuilder = new URLBuilder();
    int count;

    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});

      List list =
         EntityAdapterEngine.retrieveAdapterList(model, definition, aWhereClause, anOrderByClause, nrEntitiesPerListPage, anOffset);

      responseData.put("nexturl", null);
      responseData.put("prevurl", null);
      responseData.put("module", getOperationModuleName());

      count=mainModule.getSize(aWhereClause);

      urlBuilder.setValue("module", getOperationModuleName());
      urlBuilder.setValue("do", "list");
      urlBuilder.setValue("where", aWhereClause);
      urlBuilder.setValue("order", anOrderByClause);

      urlBuilder.setValue("searchfield", requestParser.getParameter("searchfield"));
      urlBuilder.setValue("searchtext", requestParser.getParameter("searchtext"));
      urlBuilder.setValue("searchispublished", requestParser.getParameter("searchispublished"));
      urlBuilder.setValue("searchstatus", requestParser.getParameter("searchstatus"));
      urlBuilder.setValue("searchorder", requestParser.getParameter("searchorder"));

      responseData.put("searchfield", requestParser.getParameter("searchfield"));
      responseData.put("searchtext", requestParser.getParameter("searchtext"));
      responseData.put("searchispublished", requestParser.getParameter("searchispublished"));
      responseData.put("searchstatus", requestParser.getParameter("searchstatus"));
      responseData.put("searchorder", requestParser.getParameter("searchorder"));

      urlBuilder.setValue("offset", anOffset);
      responseData.put("offset" , new Integer(anOffset).toString());
      responseData.put("thisurl" , urlBuilder.getQuery());

      if (count>anOffset+nrEntitiesPerListPage) {
        urlBuilder.setValue("offset", anOffset + nrEntitiesPerListPage);
        responseData.put("nexturl" , urlBuilder.getQuery());
      }

      if (anOffset>0) {
        urlBuilder.setValue("offset", Math.max(anOffset - nrEntitiesPerListPage, 0));
        responseData.put("prevurl" , urlBuilder.getQuery());
      }

      responseData.put("entities", list);
      responseData.put("from" , Integer.toString(anOffset+1));
      responseData.put("count", Integer.toString(count));
      responseData.put("to", Integer.toString(Math.min(anOffset+nrEntitiesPerListPage, count)));

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, listGenerator);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void editObject(HttpServletRequest aRequest, HttpServletResponse aResponse, Object anObject, boolean anIsNew, String anId) throws ServletModuleExc {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    URLBuilder urlBuilder = new URLBuilder();
    EntityAdapterModel model;

    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});

      responseData.put("module", getOperationModuleName());
      responseData.put("entity", anObject);
      responseData.put("new", new Boolean(anIsNew));


      urlBuilder.setValue("module", getOperationModuleName());
      urlBuilder.setValue("returnurl", requestParser.getParameter("returnurl"));
      if (anIsNew)
        urlBuilder.setValue("do", "add");
      else {
        urlBuilder.setValue("id", anId);
        urlBuilder.setValue("do", "edit");
      }
      responseData.put("returnurl", requestParser.getParameter("returnurl"));
      responseData.put("thisurl", urlBuilder.getQuery());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, editGenerator);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }


  /**
   * Generic add method
   *
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */
  public void add(HttpServletRequest aRequest, HttpServletResponse aResponse)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure  {

    Map object = new HashMap();

    Iterator i = mainModule.getStorageObject().getFields().iterator();

    while (i.hasNext())
      object.put(i.next(), "");

    initializeNewObject(object, aRequest, aResponse);

    editObject(aRequest, aResponse, object, true, null);
  }

  protected void initializeNewObject(Map aNewObject, HttpServletRequest aRequest, HttpServletResponse aResponse) {
  }

  /**
   * Method called when the user edits an object.
   *
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */
  public void edit(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure  {
    edit(aRequest, aResponse, aRequest.getParameter("id"));
  }

  /**
   * Generic edit method
   *
   * @param aRequest
   * @param aResponse
   * @param anIdentifier
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */
  public void edit(HttpServletRequest aRequest, HttpServletResponse aResponse, String anIdentifier)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure  {
    try {
      editObject(aRequest, aResponse, model.makeEntityAdapter(definition, mainModule.getById(anIdentifier)), false, anIdentifier);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  /**
   * Generic update method
   *
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */
  public void update(HttpServletRequest aRequest, HttpServletResponse aResponse)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

      String id = aRequest.getParameter("id");
      Map withValues = getIntersectingValues(aRequest, mainModule.getStorageObject());
      mainModule.set(withValues);

      String returnUrl = requestParser.getParameter("returnurl");

      if (returnUrl!=null) {
        redirect(aResponse, returnUrl);
      }
      else {
        edit(aRequest, aResponse, id);
      }
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  /**
   * Generic insert method
   *
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */
  public void insert(HttpServletRequest aRequest, HttpServletResponse aResponse)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

      Map object = getIntersectingValues(aRequest, mainModule.getStorageObject());

      String id = processInstertedObject(object, aRequest, aResponse);

      String returnUrl = requestParser.getParameter("returnurl");

      if (returnUrl!=null) {
        redirect(aResponse, returnUrl);
      }
      else {
        edit(aRequest, aResponse, id);
      }
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public String processInstertedObject(Map anObject, HttpServletRequest aRequest, HttpServletResponse aResponse) {
    try {
      return mainModule.add(anObject);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  };

  /**
   *
   * @param aRequest
   * @param aResponse
   */
  public void confirmdelete(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    try {
      String idParam = aRequest.getParameter("id");
      String confirmParam = aRequest.getParameter("confirm");
      String cancelParam = aRequest.getParameter("cancel");

      if (confirmParam != null && !confirmParam.equals("")) {
        mainModule.deleteById(idParam);
        redirect(aResponse, aRequest.getParameter("okurl"));
      }
      else
        redirect(aResponse, aRequest.getParameter("cancelurl"));
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  /**
   *
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */
  public void delete(HttpServletRequest aRequest, HttpServletResponse aResponse)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure  {
    try {
      String idParam = aRequest.getParameter("id");

      if (idParam == null)
        throw new ServletModuleExc("Invalid call to delete: no id supplied");

      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});

      responseData.put("module", getOperationModuleName());
      responseData.put("id", idParam);
      responseData.put("cancelurl", aRequest.getParameter("cancelurl"));
      responseData.put("okurl", aRequest.getParameter("okurl"));

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, deleteConfirmationGenerator);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
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
   * Gets the fields from a httprequest and matches them with the metadata from
   * the storage object. Returns the keys that match, with their values.
   *
   * @return Map with the values
   */
  public Map getIntersectingValues(HttpServletRequest aRequest, StorageObject theStorage)
      throws ServletModuleExc, ServletModuleFailure {

    try {
      HTTPRequestParser parser;
      List theFieldList;

      parser = new HTTPRequestParser(aRequest);

      theFieldList = theStorage.getFields();

      Map withValues = new HashMap();
      String aField, aValue;

      for (int i = 0; i < theFieldList.size(); i++) {
        aField = (String) theFieldList.get(i);

        aValue = parser.getParameter(aField);
        if (aValue != null)
          withValues.put(aField, aValue);
      }
      return withValues;
    }
    catch (Throwable e) {
      e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

      throw new ServletModuleFailure( "ServletModule.getIntersectingValues: " + e.getMessage(), e);
    }
  }
}