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

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import mir.servlet.*;
import mir.entity.adapter.*;
import mir.log.*;

import mircoders.global.*;
import mircoders.localizer.*;
import mircoders.storage.*;
import mircoders.entity.*;
import mircoders.module.*;

public class ServletModuleLocalizer extends ServletModule {
  private static ServletModuleLocalizer instance = new ServletModuleLocalizer();
  public static ServletModule getInstance() { return instance; }

  private ModuleContent contentModule;
  private ModuleComment commentModule;

  private ServletModuleLocalizer() {
    try {
      contentModule = new ModuleContent(DatabaseContent.getInstance());
      commentModule = new ModuleComment(DatabaseComment.getInstance());

      logger = new LoggerWrapper("ServletModule.Localizer");
    }
    catch (Exception e) {
      logger.error("ServletModuleLocalizer could not be initialized: " + e.getMessage());
    }
  }

  public void commentoperation(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleException {
    String idString = aRequest.getParameter("id");
    String operationString = aRequest.getParameter("operation");
    String returnUrlString = aRequest.getParameter("returnurl");

    MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation;
    EntityAdapter comment;
    EntityComment entity;

    try {
      entity = (EntityComment) commentModule.getById(idString);

      if (entity!=null) {
        comment = MirGlobal.localizer().dataModel().adapterModel().makeEntityAdapter("comment", entity);
        operation = MirGlobal.localizer().adminInterface().simpleCommentOperationForName(operationString);
        operation.perform(comment);
      }

      redirect(aResponse, returnUrlString);
    }
    catch (Throwable e) {
      e.printStackTrace(System.out);
      throw new ServletModuleException(e.getMessage());
    }
  }

  public void articleoperation(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleException {
    String articleIdString = aRequest.getParameter("articleid");
    String operationString = aRequest.getParameter("operation");
    String returnUrlString = aRequest.getParameter("returnurl");

    MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation;
    EntityAdapter article;
    EntityContent entity;

    try {
      entity = (EntityContent) contentModule.getById(articleIdString);

      if (entity!=null) {
        article = MirGlobal.localizer().dataModel().adapterModel().makeEntityAdapter("content", entity);
        operation = MirGlobal.localizer().adminInterface().simpleArticleOperationForName(operationString);
        operation.perform(article);
      }

      redirect(aResponse, returnUrlString);
    }
    catch (Throwable e) {
      e.printStackTrace(System.out);
      throw new ServletModuleException(e.getMessage());
    }
  }


}