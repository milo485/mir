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

import mir.log.LoggerWrapper;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.util.HTTPRequestParser;
import mir.util.URLBuilder;
import mircoders.global.Abuse;
import mircoders.global.MirGlobal;

public class ServletModuleAbuse extends ServletModule {
  private static ServletModuleAbuse instance = new ServletModuleAbuse();
  private String editFilterTemplate;
  private String listFiltersTemplate;
  private String mainTemplate;
  private String viewLogTemplate;

  public static ServletModule getInstance() { return instance; }

  private ServletModuleAbuse() {
    logger = new LoggerWrapper("ServletModule.Abuse");
    defaultAction = "showsettings";

    editFilterTemplate = configuration.getString("ServletModule.Abuse.EditFilter.Template");
    listFiltersTemplate = configuration.getString("ServletModule.Abuse.ListFilters.Template");
    viewLogTemplate = configuration.getString("ServletModule.Abuse.ViewLog.Template");
    mainTemplate = configuration.getString("ServletModule.Abuse.Main.Template");
  }

  public void edit(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    String id=requestParser.getParameterWithDefault("id", "");

    if (id.equals("")) {
      editfilter(aRequest, aResponse, "", "", "", "", "", "", "");
    }
    else {
      Abuse.FilterRule filter = MirGlobal.abuse().getFilter(id);
      if (filter==null)
        throw new ServletModuleExc("Filter not found");

      editfilter(aRequest, aResponse, filter.getId(), filter.getType(), filter.getExpression(), filter.getComments(), filter.getCommentAction(), filter.getArticleAction(), "");
    }
  }

  public void editfilter(HttpServletRequest aRequest, HttpServletResponse aResponse,
        String anId, String aType, String anExpression, String aComments,
        String aCommentAction, String anArticleAction, String anErrorMessage) throws ServletModuleExc {
    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});

      responseData.put("id", anId);
      responseData.put("type", aType);
      responseData.put("expression", anExpression);
      responseData.put("comments", aComments);
      responseData.put("articleaction", anArticleAction);
      responseData.put("commentaction", aCommentAction);
      responseData.put("errormessage", anErrorMessage);

      responseData.put("articleactions", MirGlobal.abuse().getArticleActions());
      responseData.put("commentactions", MirGlobal.abuse().getCommentActions());
      responseData.put("filtertypes", MirGlobal.abuse().getFilterTypes());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, editFilterTemplate);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void updatefilter(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    String type=requestParser.getParameterWithDefault("type", "");
    String id=requestParser.getParameterWithDefault("id", "");
    String expression=requestParser.getParameterWithDefault("expression", "");
    String commentaction=requestParser.getParameterWithDefault("commentaction", "");
    String articleaction=requestParser.getParameterWithDefault("articleaction", "");
    String comments=requestParser.getParameterWithDefault("comments", "");

    String errorMessage;

    String userName = ServletHelper.getUserName(aRequest);

    if (id.equals("")) {
      errorMessage = MirGlobal.abuse().addFilter(type, expression,comments, commentaction, articleaction);
    }
    else {
      errorMessage = MirGlobal.abuse().setFilter(id, type, expression, comments, commentaction, articleaction);
    }

    if (errorMessage!=null) {
      editfilter(aRequest, aResponse, id, type, expression, comments, commentaction, articleaction, errorMessage);
    }
    else {
      MirGlobal.abuse().save();
      showfilters(aRequest, aResponse);
    }
  }

  public void delete(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

    String id=requestParser.getParameterWithDefault("id", "");
    MirGlobal.abuse().deleteFilter(id);

    MirGlobal.abuse().save();

    showfilters(aRequest, aResponse);
  }

  public void add(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc {
    editfilter(aRequest, aResponse, "", "", "", "", "", "", "");
  }
  public void showfilters(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    URLBuilder urlBuilder = new URLBuilder();

    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});

      urlBuilder.setValue("module", "Abuse");
      urlBuilder.setValue("do", "showfilters");
      responseData.put("thisurl", urlBuilder.getQuery());

      responseData.put("filters", MirGlobal.abuse().getFilters());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, listFiltersTemplate);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void showsettings(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    URLBuilder urlBuilder = new URLBuilder();

    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});

      urlBuilder.setValue("module", "Abuse");
      urlBuilder.setValue("do", "showsettings");

      responseData.put("thisurl", urlBuilder.getQuery());

      responseData.put("articleactions", MirGlobal.abuse().getArticleActions());
      responseData.put("commentactions", MirGlobal.abuse().getCommentActions());

      responseData.put("disableop", new Boolean(MirGlobal.abuse().getOpenPostingDisabled()));
      responseData.put("passwordop", new Boolean(MirGlobal.abuse().getOpenPostingPassword()));
      responseData.put("logenabled", new Boolean(MirGlobal.abuse().getLogEnabled()));
      responseData.put("logsize", Integer.toString(MirGlobal.abuse().getLogSize()));
      responseData.put("usecookies", new Boolean(MirGlobal.abuse().getCookieOnBlock()));
      responseData.put("articleaction", MirGlobal.abuse().getArticleBlockAction());
      responseData.put("commentaction", MirGlobal.abuse().getCommentBlockAction());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, mainTemplate);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void savesettings(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    try {
      HTTPRequestParser parser = new HTTPRequestParser(aRequest);

      MirGlobal.abuse().setOpenPostingDisabled(parser.getParameterWithDefault("disableop", "").equals("1"));
      MirGlobal.abuse().setOpenPostingPassword(parser.getParameterWithDefault("passwordop", "").equals("1"));
      MirGlobal.abuse().setLogEnabled(parser.getParameterWithDefault("logenabled", "").equals("1"));

      try {
        MirGlobal.abuse().setLogSize(parser.getIntegerWithDefault("logsize", MirGlobal.abuse().getLogSize()));
      }
      catch (Throwable t) {
      }

      MirGlobal.abuse().setCookieOnBlock(parser.getParameterWithDefault("usecookies", "").equals("1"));

      MirGlobal.abuse().setArticleBlockAction(parser.getParameter("articleaction"));
      MirGlobal.abuse().setCommentBlockAction(parser.getParameter("commentaction"));

      MirGlobal.abuse().save();

      showsettings(aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void showlog(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    URLBuilder urlBuilder = new URLBuilder();
    int count;

    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
      urlBuilder.setValue("module", "Abuse");
      urlBuilder.setValue("do", "showlog");
      responseData.put("thisurl", urlBuilder.getQuery());

      responseData.put("log", MirGlobal.abuse().getLog());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, viewLogTemplate);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }
}