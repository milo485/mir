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

import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.entity.adapter.EntityIteratorAdapter;
import mir.log.LoggerWrapper;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleFailure;
import mir.util.CachingRewindableIterator;
import mir.util.URLBuilder;
import mircoders.global.MirGlobal;

public class ServletModuleAdmin extends ServletModule
{
  private static ServletModuleAdmin instance = new ServletModuleAdmin();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleAdmin() {
    logger = new LoggerWrapper("ServletModule.Admin");
    defaultAction = "start";
  }

  public void superusermenu(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    URLBuilder urlBuilder = new URLBuilder();
    int count;

    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
      urlBuilder.setValue("module", "Admin");
      urlBuilder.setValue("do", "superusermenu");

      responseData.put("thisurl" , urlBuilder.getQuery());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, "superusermenu.template");
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void start(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    String startTemplate = configuration.getString("Mir.StartTemplate");
    String sessionUrl = aResponse.encodeURL("");

    try {
      Map mergeData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] {getLocale(aRequest), getFallbackLocale(aRequest)}
          , "bundles.admin", "bundles.adminlocal");
      mergeData.put("messages",
                    new CachingRewindableIterator(
          new EntityIteratorAdapter("", "webdb_create desc", 10,
                                    MirGlobal.localizer().dataModel().adapterModel(), "internalMessage", 10, 0)));

      mergeData.put("fileeditentries", ( (ServletModuleFileEdit) ServletModuleFileEdit.getInstance()).getEntries());
      mergeData.put("administeroperations", ( (ServletModuleLocalizer) ServletModuleLocalizer.getInstance()).getAdministerOperations());

      mergeData.put("searchvalue", null);
      mergeData.put("searchfield", null);
      mergeData.put("searchispublished", null);
      mergeData.put("searcharticletype", null);
      mergeData.put("searchorder", null);
      mergeData.put("selectarticleurl", null);

      ServletHelper.generateResponse(aResponse.getWriter(), mergeData, startTemplate);
    }
    catch (Exception e) {
      throw new ServletModuleFailure(e);
    }
  }

}
