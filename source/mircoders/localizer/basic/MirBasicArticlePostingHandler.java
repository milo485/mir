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

package mircoders.localizer.basic;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mir.entity.Entity;
import mir.misc.StringUtil;
import mir.session.Request;
import mir.session.Response;
import mir.session.Session;
import mir.session.SessionExc;
import mir.session.SessionFailure;
import mir.session.UploadedFile;
import mir.util.ExceptionFunctions;
import mircoders.entity.EntityContent;
import mircoders.global.MirGlobal;
import mircoders.media.MediaUploadProcessor;
import mircoders.module.ModuleContent;
import mircoders.module.ModuleMediaType;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseContentToMedia;
import mircoders.storage.DatabaseContentToTopics;

/**
 *
 * <p>Title: Experimental session handler for article postings </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Zapata
 * @version 1.0
 */

public class MirBasicArticlePostingHandler extends MirBasicPostingSessionHandler {
  protected ModuleContent contentModule = new ModuleContent(DatabaseContent.getInstance());
  protected DatabaseContentToMedia contentToMedia = DatabaseContentToMedia.getInstance();
  protected DatabaseContent contentDatabase = DatabaseContent.getInstance();

  public MirBasicArticlePostingHandler() {
    super();

    setResponseGenerators(
      configuration.getString("Localizer.OpenSession.article.EditTemplate"),
      configuration.getString("Localizer.OpenSession.article.DupeTemplate"),
      configuration.getString("Localizer.OpenSession.article.UnsupportedMediaTemplate"),
      configuration.getString("Localizer.OpenSession.article.DoneTemplate"));
  }

  protected void initializeResponseData(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    super.initializeResponseData(aRequest, aSession, aResponse);

    Iterator i = DatabaseContent.getInstance().getFields().iterator();
    while (i.hasNext()) {
      String field = (String) i.next();
      aResponse.setResponseValue(field, aRequest.getParameter(field));
    }
    aResponse.setResponseValue("to_topic", aRequest.getParameters("to_topic"));
  }

  public void validate(List aResults, Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    super.validate(aResults, aRequest, aSession);

    testFieldEntered(aRequest, "title", "validationerror.missing", aResults);
    testFieldEntered(aRequest, "description", "validationerror.missing", aResults);
    testFieldEntered(aRequest, "creator", "validationerror.missing", aResults);
    testFieldEntered(aRequest, "content_data", "validationerror.missing", aResults);
  }

  public void finalizeArticle(Request aRequest, Session aSession, EntityContent anArticle) throws SessionExc, SessionFailure {
    anArticle.setValueForProperty("is_published", "1");
    anArticle.setValueForProperty("is_produced", "0");
    anArticle.setValueForProperty("date", StringUtil.date2webdbDate(new GregorianCalendar()));
    anArticle.setValueForProperty("is_html","0");
    anArticle.setValueForProperty("publish_path", StringUtil.webdbDate2path(anArticle.getValue("date")));
    anArticle.setValueForProperty("to_article_type", "1");
    anArticle.setValueForProperty("to_publisher", "1");
  }

  public void setArticleTopics(Request aRequest, Session aSession, EntityContent aContent) throws SessionExc, SessionFailure {
    // topics:
    List topics = aRequest.getParameters("to_topic");
    if (topics.size() > 0) {
      try {
        DatabaseContentToTopics.getInstance().setTopics(aContent.getId(), topics);
      }
      catch (Throwable e) {
        logger.error("setting content_x_topic failed");
        throw new SessionFailure("MirBasicArticlePostingHandler: can't set topics: " + e.toString(), e);
      }
    }
  }

  public void preProcessRequest(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    try {
      String id;
      Map values = getIntersectingValues(aRequest, DatabaseContent.getInstance());

      EntityContent article = (EntityContent) contentModule.createNew();
      article.setValues(values);

      finalizeArticle(aRequest, aSession, article);
      id = article.insert();
      if (id == null) {
        logger.info("Duplicate article rejected");
        throw new DuplicatePostingExc("Duplicate article rejected");
      }
      aSession.setAttribute("content", article);


      setArticleTopics(aRequest, aSession, article);

    }
    catch (Throwable t) {
      throw new SessionFailure(t);
    }
  }

  public void processUploadedFile(Request aRequest, Session aSession, UploadedFile aFile) throws SessionExc, SessionFailure {
    try {
      Map values = new HashMap();
      values.put("title", aRequest.getParameter(aFile.getFieldName()+"_title"));
      values.put("creator", aRequest.getParameter("creator"));
      values.put("to_publisher", "0");
      values.put("is_published", "1");
      values.put("is_produced", "1");
      Entity mediaItem = MediaUploadProcessor.processMediaUpload(aFile, values);
      mediaItem.update();
      contentToMedia.addMedia(((EntityContent) aSession.getAttribute("content")).getId(), mediaItem.getId());
    }
    catch (Throwable t) {
      throw new SessionFailure(t);
    }
  }

  public void postProcessRequest(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    EntityContent article = (EntityContent) aSession.getAttribute("content");

    MirGlobal.abuse().checkArticle(article, aRequest, null);
    try {
      MirGlobal.localizer().openPostings().afterContentPosting(article);
    }
    catch (Throwable t) {
      throw new SessionFailure(t);
    }
    logger.info("article posted");
  };

}