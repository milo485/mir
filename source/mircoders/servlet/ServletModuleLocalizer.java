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

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mir.entity.adapter.EntityAdapter;
import mir.log.LoggerWrapper;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.util.*;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.entity.EntityUsers;
import mircoders.global.MirGlobal;
import mircoders.localizer.MirAdminInterfaceLocalizer;
import mircoders.module.ModuleComment;
import mircoders.module.ModuleContent;
import mircoders.storage.DatabaseComment;
import mircoders.storage.DatabaseContent;

public class ServletModuleLocalizer extends ServletModule {
  private static ServletModuleLocalizer instance = new ServletModuleLocalizer();
  public static ServletModule getInstance() { return instance; }

  private ModuleContent contentModule;
  private ModuleComment commentModule;
  private List administerOperations;

  private ServletModuleLocalizer() {
    try {
      logger = new LoggerWrapper("ServletModule.Localizer");

      contentModule = new ModuleContent(DatabaseContent.getInstance());
      commentModule = new ModuleComment(DatabaseComment.getInstance());

      administerOperations = new Vector();

      String settings[] = configuration.getStringArray("Mir.Localizer.Admin.AdministerOperations");

      if (settings!=null) {
        for (int i = 0; i < settings.length; i++) {
          String setting = settings[i].trim();

          if (setting.length() > 0) {
            List parts = StringRoutines.splitString(setting, ":");
            if (parts.size() != 2) {
              logger.error("config error: " + settings[i] + ", 2 parts expected");
            }
            else {
              Map entry = new HashMap();
              entry.put("name", (String) parts.get(0));
              entry.put("url", (String) parts.get(1));
              administerOperations.add(entry);
            }
          }
        }
      }
    }
    catch (Exception e) {
      logger.error("ServletModuleLocalizer could not be initialized: " + e.getMessage());
    }


  }

  private EntityAdapter getActiveUser(HttpServletRequest aRequest) throws ServletModuleExc {
    try {
      HttpSession session = aRequest.getSession(false);
      return MirGlobal.localizer().dataModel().adapterModel().makeEntityAdapter
          ("user", (EntityUsers) session.getAttribute("login.uid"));
    }
    catch (Throwable e) {
      throw new ServletModuleFailure("ServletModuleLocalizer.getActiveUser: " + e.getMessage(), e);
    }
  }

  public void performCommentOperation(EntityAdapter aUser, String anId, String anOperation) {
    MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation;
    EntityAdapter comment;
    EntityComment entity;

    try {
      entity = (EntityComment) commentModule.getById(anId);

      if (entity != null) {
        comment = MirGlobal.localizer().dataModel().adapterModel().makeEntityAdapter("comment", entity);
        operation = MirGlobal.localizer().adminInterface().simpleCommentOperationForName(anOperation);
        operation.perform(aUser, comment);
        logger.info("Operation " + anOperation + " successfully performed on comment " + anId);
      }
      else {
        logger.error("Error while performing " + anOperation + " on comment " + anId + ": comment is null");
      }
    }
    catch (Throwable e) {
      logger.error("Error while performing " + anOperation + " on comment " + anId + ": " + e.getMessage());
    }
  }

  public void commentoperation(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    String commentIdString = aRequest.getParameter("id");
    String operationString = aRequest.getParameter("operation");
    String returnUrlString = aRequest.getParameter("returnurl");

    performCommentOperation(getActiveUser(aRequest), commentIdString, operationString);

    redirect(aResponse, returnUrlString);
  }

  public void commentoperationbatch(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    String returnUrlString = aRequest.getParameter("returnurl");

    String[] operations = aRequest.getParameterValues("operation");

    for (int i=0; i<operations.length; i++) {
      if (operations[i].length()>0) {
        List parts = StringRoutines.splitString(operations[i], ";");

        if (parts.size() != 2) {
          logger.error("commentoperationbatch: operation string invalid: " +
                       operations[i]);
        }
        else {
          String commentIdString = (String) parts.get(0);
          String operationString = (String) parts.get(1);

          performCommentOperation(getActiveUser(aRequest), commentIdString, operationString);
        }
      }
    }

    redirect(aResponse, returnUrlString);
  }

  public void performArticleOperation(EntityAdapter aUser, String anId, String anOperation) {
    MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation;
    EntityAdapter article;
    EntityContent entity;

    try {
      entity = (EntityContent) contentModule.getById(anId);

      if (entity != null) {
        article = MirGlobal.localizer().dataModel().adapterModel().
            makeEntityAdapter("content", entity);
        operation = MirGlobal.localizer().adminInterface().
            simpleArticleOperationForName(anOperation);
        operation.perform(aUser, article);
        logger.info("Operation " + anOperation + " successfully performed on article " + anId);
      }
      else {
        logger.error("Error while performing " + anOperation + " on article " + anId + ": article is null");
      }
    }
    catch (Throwable e) {
      logger.error("Error while performing " + anOperation + " on article " + anId + ": " + e.getMessage());
    }
  }

  public void articleoperation(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    String articleIdString = aRequest.getParameter("articleid");
    String operationString = aRequest.getParameter("operation");
    String returnUrlString = aRequest.getParameter("returnurl");

    performArticleOperation(getActiveUser(aRequest), articleIdString, operationString);
    redirect(aResponse, returnUrlString);
  }

  public void articleoperationbatch(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    String returnUrlString = aRequest.getParameter("returnurl");

    String[] operations = aRequest.getParameterValues("operation");

    for (int i=0; i<operations.length; i++) {
      if (operations[i].length()>0) {
        List parts = StringRoutines.splitString(operations[i], ";");

        if (parts.size() != 2) {
          logger.error("articleoperationbatch: operation string invalid: " + operations[i]);
        }
        else {
          String articleIdString = (String) parts.get(0);
          String operationString = (String) parts.get(1);

          performArticleOperation(getActiveUser(aRequest), articleIdString, operationString);
        }
      }
    }

    redirect(aResponse, returnUrlString);
  }

  public List getAdministerOperations() throws ServletModuleExc {
    return administerOperations;
  }
}