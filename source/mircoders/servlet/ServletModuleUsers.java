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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.entity.adapter.EntityAdapterModel;
import mir.entity.adapter.EntityIteratorAdapter;
import mir.log.LoggerWrapper;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.servlet.ServletModuleUserExc;
import mir.util.CachingRewindableIterator;
import mir.util.HTTPRequestParser;
import mir.util.URLBuilder;
import mircoders.entity.EntityUsers;
import mircoders.global.MirGlobal;
import mircoders.module.ModuleUsers;
import mircoders.storage.DatabaseUsers;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class ServletModuleUsers extends ServletModule
{
  private static ServletModuleUsers instance = new ServletModuleUsers();
  public static ServletModule getInstance() { return instance; }
  protected ModuleUsers usersModule;

  private ServletModuleUsers() {
    super();
    logger = new LoggerWrapper("ServletModule.Users");

    try {
      model = MirGlobal.localizer().dataModel().adapterModel();
      definition = "user";
      usersModule = new ModuleUsers(DatabaseUsers.getInstance());
      mainModule = usersModule;
    }
    catch (Throwable e) {
      logger.debug("initialization of ServletModuleUsers failed!: " + e.getMessage());
    }
  }

  public void edit(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    String idParam = aRequest.getParameter("id");

    if (idParam == null)
      throw new ServletModuleExc("ServletModuleUser.edit: invalid call: (id) not specified");

    try {
      EntityUsers user = (EntityUsers) mainModule.getById(idParam);
      MirGlobal.accessControl().user().assertMayEditUser(ServletHelper.getUser(aRequest), user);

      showUser(idParam, false, aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void add(HttpServletRequest aRequest, HttpServletResponse aResponse)
      throws ServletModuleExc
  {
    try {
      MirGlobal.accessControl().user().assertMayAddUsers(ServletHelper.getUser(aRequest));

      showUser(null, false, aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public String validatePassword(EntityUsers aUser, HTTPRequestParser aRequestParser) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure
  {
    if ( (aRequestParser.getParameter("newpassword") != null &&
          aRequestParser.getParameter("newpassword").length() > 0) ||
        (aRequestParser.getParameter("newpassword2") != null &&
         aRequestParser.getParameter("newpassword2").length() > 0)
        ) {
      String newPassword = aRequestParser.getParameterWithDefault("newpassword", "");
      String newPassword2 = aRequestParser.getParameterWithDefault("newpassword2", "");
      String oldPassword = aRequestParser.getParameterWithDefault("oldpassword", "");

      try {
        if (!usersModule.checkUserPassword(aUser, oldPassword)) {
          throw new ServletModuleUserExc("user.error.incorrectpassword", new String[] {});
        }
      }
      catch (Throwable t) {
        throw new ServletModuleFailure(t);
      }


      if (newPassword.length() == 0 || newPassword2.length() == 0) {
        throw new ServletModuleUserExc("user.error.missingpasswords", new String[] {});
      }

      if (!newPassword.equals(newPassword2)) {
        throw new ServletModuleUserExc("user.error.passwordmismatch", new String[] {});
      }

      return newPassword;
    }
    else
      return null;
  }

  public void insert(HttpServletRequest aRequest, HttpServletResponse aResponse)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure
  {
    try {
      MirGlobal.accessControl().user().assertMayAddUsers(ServletHelper.getUser(aRequest));

      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      Map withValues = getIntersectingValues(aRequest, mainModule.getStorageObject());

      String newPassword=validatePassword(ServletHelper.getUser(aRequest), requestParser);
      if (newPassword!=null)
        withValues.put("password", newPassword);
      else
        throw new ServletModuleUserExc("user.error.missingpassword", new String[] {});

      String id = mainModule.add(withValues);

      logAdminUsage(aRequest, id, "object added");

      if (requestParser.hasParameter("returnurl"))
        redirect(aResponse, requestParser.getParameter("returnurl"));
      else
        list(aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void update(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure
  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      String id = requestParser.getParameter("id");
      EntityUsers user = (EntityUsers) mainModule.getById(id);
      MirGlobal.accessControl().user().assertMayEditUser(ServletHelper.getUser(aRequest), user);

      Map withValues = getIntersectingValues(aRequest, mainModule.getStorageObject());
      if (!withValues.containsKey("is_admin"))
        withValues.put("is_admin","0");

      String newPassword=validatePassword(ServletHelper.getUser(aRequest), requestParser);
      if (newPassword!=null)
        withValues.put("password", MirGlobal.localizer().adminInterface().makePasswordDigest(newPassword));

      mainModule.set(withValues);

      logAdminUsage(aRequest, id, "object modified");

      if (requestParser.hasParameter("returnurl"))
        redirect(aResponse, requestParser.getParameter("returnurl"));
      else
        list(aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void updatepassword(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure
  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      String id = requestParser.getParameter("id");
      EntityUsers user = (EntityUsers) mainModule.getById(id);
      MirGlobal.accessControl().user().assertMayChangeUserPassword(ServletHelper.getUser(aRequest), user);

      String newPassword=validatePassword(ServletHelper.getUser(aRequest), requestParser);
      if (newPassword!=null) {
        user.setValueForProperty("password", MirGlobal.localizer().adminInterface().makePasswordDigest(newPassword));
        user.update();

        logAdminUsage(aRequest, id, "password changed");

        // hackish: to make sure the cached logged in user is up-to-date:
        ServletHelper.setUser(aRequest, (EntityUsers) mainModule.getById(ServletHelper.getUser(aRequest).getId()));
      }

      if (requestParser.hasParameter("returnurl"))
        redirect(aResponse, requestParser.getParameter("returnurl"));
      else
        redirect(aResponse, "");
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void list(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    int offset = requestParser.getIntegerWithDefault("offset", 0);

    returnUserList(aRequest, aResponse, offset);
  }

  public void returnUserList(
       HttpServletRequest aRequest,
       HttpServletResponse aResponse,
       int anOffset) throws ServletModuleExc {

// ML: to be deleted, support for 3 extra vars to be added

    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    URLBuilder urlBuilder = new URLBuilder();
    EntityAdapterModel model;
    int count;

    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
      model = MirGlobal.localizer().dataModel().adapterModel();

      Object userList =
          new CachingRewindableIterator(
            new EntityIteratorAdapter( "", "login", nrEntitiesPerListPage,
               MirGlobal.localizer().dataModel().adapterModel(), "user", nrEntitiesPerListPage, anOffset)
      );

      responseData.put("nexturl", null);
      responseData.put("prevurl", null);

      count=mainModule.getSize("");

      urlBuilder.setValue("module", "Users");
      urlBuilder.setValue("do", "list");

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

      responseData.put("users", userList);
      responseData.put("mayDeleteUsers", new Boolean(MirGlobal.accessControl().user().mayDeleteUsers(ServletHelper.getUser(aRequest))));
      responseData.put("mayAddUsers", new Boolean(MirGlobal.accessControl().user().mayAddUsers(ServletHelper.getUser(aRequest))));
      responseData.put("mayEditUsers", new Boolean(MirGlobal.accessControl().user().mayEditUsers(ServletHelper.getUser(aRequest))));

      responseData.put("from" , Integer.toString(anOffset+1));
      responseData.put("count", Integer.toString(count));
      responseData.put("to", Integer.toString(Math.min(anOffset+nrEntitiesPerListPage, count)));
      responseData.put("offset" , Integer.toString(anOffset));

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, listGenerator);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void showUser(String anId, boolean anOnlyPassword, HttpServletRequest aRequest, HttpServletResponse aResponse)
      throws ServletModuleExc {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
      EntityAdapterModel model = MirGlobal.localizer().dataModel().adapterModel();
      Map user;
      URLBuilder urlBuilder = new URLBuilder();

      urlBuilder.setValue("module", "Users");
      if (anOnlyPassword)
        urlBuilder.setValue("do", "changepassword");
      else
        urlBuilder.setValue("do", "edit");
      urlBuilder.setValue("id", anId);
      urlBuilder.setValue("returnurl", requestParser.getParameter("returnurl"));

      if (anId!=null) {
        responseData.put("new", Boolean.FALSE);
        user = model.makeEntityAdapter("user", mainModule.getById(anId));
      }
      else {
        List fields = DatabaseUsers.getInstance().getFields();
        responseData.put("new", Boolean.TRUE);
        user = new HashMap();
        Iterator i = fields.iterator();
        while (i.hasNext()) {
          user.put(i.next(), null);
        }

        MirGlobal.localizer().adminInterface().initializeArticle(user);
      }
      responseData.put("user", user);
      responseData.put("passwordonly", new Boolean(anOnlyPassword));

      responseData.put("returnurl", requestParser.getParameter("returnurl"));
      responseData.put("thisurl", urlBuilder.getQuery());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, editGenerator);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void delete(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleUserExc, ServletModuleExc, ServletModuleFailure {
    try {
      EntityUsers user = (EntityUsers) mainModule.getById(aRequest.getParameter("id"));

      MirGlobal.accessControl().user().assertMayDeleteUser(ServletHelper.getUser(aRequest), user);

      super.delete(aRequest, aResponse);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public void changepassword(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    String idParam = aRequest.getParameter("id");

    if (idParam == null)
      throw new ServletModuleExc("ServletModuleUser.edit: invalid call: (id) not specified");

    try {
      EntityUsers user = (EntityUsers) mainModule.getById(idParam);
      MirGlobal.accessControl().user().assertMayChangeUserPassword(ServletHelper.getUser(aRequest), user);

      showUser(idParam, true, aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }
}

