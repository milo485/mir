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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mir.entity.Entity;
import mir.session.Request;
import mir.session.Response;
import mir.session.Session;
import mir.session.SessionExc;
import mir.session.SessionFailure;
import mir.session.UploadedFile;
import mir.util.ExceptionFunctions;
import mircoders.entity.EntityComment;
import mircoders.global.MirGlobal;
import mircoders.media.MediaUploadProcessor;
import mircoders.module.ModuleComment;
import mircoders.module.ModuleMediaType;
import mircoders.storage.DatabaseComment;
import mircoders.storage.DatabaseCommentToMedia;
import mircoders.storage.DatabaseContent;

/**
 *
 * <p>Title: Experimental session handler for comment postings </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Zapata
 * @version 1.0
 */

public class MirBasicCommentPostingHandler extends MirBasicPostingSessionHandler {
  protected ModuleComment commentModule = new ModuleComment(DatabaseComment.getInstance());
  protected DatabaseCommentToMedia commentToMedia = DatabaseCommentToMedia.getInstance();

  protected void initializeResponseData(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    super.initializeResponseData(aRequest, aSession, aResponse);

    Iterator i = DatabaseComment.getInstance().getFields().iterator();
    while (i.hasNext()) {
      String field = (String) i.next();
      aResponse.setResponseValue(field, aRequest.getParameter(field));
    }
  }

  public void validate(List aResults, Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    testFieldEntered(aRequest, "title", "validationerror.missing", aResults);
    testFieldEntered(aRequest, "description", "validationerror.missing", aResults);
    testFieldEntered(aRequest, "creator", "validationerror.missing", aResults);
  }

  protected void initializeSession(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    super.initializeSession(aRequest, aSession);

    String articleId = aRequest.getParameter("to_media");
    if (articleId==null)
      throw new SessionExc("initializeCommentPosting: article id not set!");

    aSession.setAttribute("to_media", articleId);
  };

  public void finalizeComment(Request aRequest, Session aSession, EntityComment aComment) throws SessionExc, SessionFailure {
    aComment.setValueForProperty("is_published", "1");
    aComment.setValueForProperty("to_comment_status", "1");
    aComment.setValueForProperty("is_html","0");
  }

  public void preProcessRequest(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    try {
      String id;
      Map values = getIntersectingValues(aRequest, DatabaseComment.getInstance());

      EntityComment comment = (EntityComment) commentModule.createNew();
      comment.setValues(values);
      comment.setValueForProperty("to_media", (String) aSession.getAttribute("to_media"));
      finalizeComment(aRequest, aSession, comment);
      id = comment.insert();
      if (id == null) {
        logger.info("Duplicate comment rejected");
        throw new DuplicateCommentExc("Duplicate comment rejected");
      }
      aSession.setAttribute("comment", comment);
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
      Entity mediaItem = MediaUploadProcessor.processMediaUpload(aFile, values);
      mediaItem.update();
      commentToMedia.addMedia(((EntityComment) aSession.getAttribute("comment")).getId(), mediaItem.getId());
    }
    catch (Throwable t) {
      throw new SessionFailure(t);
    }
  }

  public void postProcessRequest(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    EntityComment comment = (EntityComment) aSession.getAttribute("comment");

    MirGlobal.abuse().checkComment(comment, aRequest, null);
    try {
      MirGlobal.localizer().openPostings().afterCommentPosting(comment);
    }
    catch (Throwable t) {
      throw new SessionFailure(t);
    }
    DatabaseContent.getInstance().setUnproduced("id=" + comment.getValue("to_media"));
    logger.info("Comment posted");
  };

  protected void makeInitialResponse(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.EditTemplate"));
  };

  protected void makeResponse(Request aRequest, Session aSession, Response aResponse, List anErrors) throws SessionExc, SessionFailure {
    aResponse.setResponseValue("errors", anErrors);
    aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.EditTemplate"));
  };

  protected void makeFinalResponse(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.DoneTemplate"));
  };

  protected void makeErrorResponse(Request aRequest, Session aSession, Response aResponse, Throwable anError) throws SessionExc, SessionFailure {
    anError.printStackTrace();
    Throwable rootCause = ExceptionFunctions.traceCauseException(anError);

    if (rootCause instanceof DuplicateCommentExc)
      aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.DupeTemplate"));
    if (rootCause instanceof ModuleMediaType.UnsupportedMimeTypeExc) {
      aResponse.setResponseValue("mimetype", ((ModuleMediaType.UnsupportedMimeTypeExc) rootCause).getMimeType());
      aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.UnsupportedMediaTemplate"));
    }
    else
      super.makeErrorResponse(aRequest, aSession, aResponse, anError);
  };

  protected static class DuplicateCommentExc extends SessionExc {
    public DuplicateCommentExc(String aMessage) {
      super(aMessage);
    }
  }
}