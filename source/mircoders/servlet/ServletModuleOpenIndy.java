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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUtils;

import gnu.regexp.RE;
import gnu.regexp.REMatch;
import gnu.regexp.REMatchEnumeration;
import gnu.regexp.REException;

import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.struts.util.MessageResources;

import mir.entity.Entity;
import mir.entity.EntityList;
import mir.generator.Generator;
import mir.log.LoggerWrapper;
import mir.misc.FileHandler;
import mir.misc.StringUtil;
import mir.misc.WebdbMultipartRequest;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.servlet.ServletModuleUserExc;
import mir.storage.StorageObjectFailure;
import mir.util.*;
import mircoders.pdf.PDFGenerator;
import mir.session.*;
import mir.util.HTTPRequestParser;
import mir.util.StringRoutines;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.global.CacheKey;
import mircoders.global.MirGlobal;
import mircoders.media.*;
import mircoders.media.UnsupportedMediaFormatExc;
import mircoders.module.ModuleComment;
import mircoders.module.ModuleContent;
import mircoders.module.ModuleImages;
import mircoders.module.ModuleTopics;
import mircoders.search.AudioSearchTerm;
import mircoders.search.ContentSearchTerm;
import mircoders.search.ImagesSearchTerm;
import mircoders.search.KeywordSearchTerm;
import mircoders.search.MediaSearchTerm;
import mircoders.search.TextSearchTerm;
import mircoders.search.TopicSearchTerm;
import mircoders.search.UnIndexedSearchTerm;
import mircoders.search.VideoSearchTerm;
import mircoders.storage.DatabaseComment;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseContentToMedia;
import mircoders.storage.DatabaseCommentToMedia;
import mircoders.storage.DatabaseContentToTopics;
import mircoders.storage.DatabaseImages;
import mircoders.storage.DatabaseLanguage;
import mircoders.storage.DatabaseTopics;

/*
 *  ServletModuleOpenIndy -
 *   is the open-access-servlet, which is responsible for
 *    adding comments to articles &
 *    open-postings to the newswire
 *
 * @author mir-coders group
 * @version $Id: ServletModuleOpenIndy.java,v 1.77 2003/04/18 15:37:29 john Exp $
 *
 */

public class ServletModuleOpenIndy extends ServletModule
{

  private String        commentFormTemplate, commentFormDoneTemplate, commentFormDupeTemplate;
  private String        postingFormTemplate, postingFormDoneTemplate, postingFormDupeTemplate;
  private String        searchResultsTemplate;
  private String        prepareMailTemplate,sentMailTemplate;
  private ModuleContent contentModule;
  private ModuleComment commentModule;
  private ModuleImages  imageModule;
  private ModuleTopics  topicsModule;
  private String        directOp ="yes";
  // Singelton / Kontruktor
  private static ServletModuleOpenIndy instance = new ServletModuleOpenIndy();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleOpenIndy() {
    super();
    try {
      logger = new LoggerWrapper("ServletModule.OpenIndy");

      commentFormTemplate = configuration.getString("ServletModule.OpenIndy.CommentTemplate");
      commentFormDoneTemplate = configuration.getString("ServletModule.OpenIndy.CommentDoneTemplate");
      commentFormDupeTemplate = configuration.getString("ServletModule.OpenIndy.CommentDupeTemplate");

      postingFormTemplate = configuration.getString("ServletModule.OpenIndy.PostingTemplate");
      postingFormDoneTemplate = configuration.getString("ServletModule.OpenIndy.PostingDoneTemplate");
      postingFormDupeTemplate = configuration.getString("ServletModule.OpenIndy.PostingDupeTemplate");

      searchResultsTemplate = configuration.getString("ServletModule.OpenIndy.SearchResultsTemplate");
      prepareMailTemplate = configuration.getString("ServletModule.OpenIndy.PrepareMailTemplate");
      sentMailTemplate = configuration.getString("ServletModule.OpenIndy.SentMailTemplate");
      directOp = configuration.getString("DirectOpenposting").toLowerCase();
      commentModule = new ModuleComment(DatabaseComment.getInstance());
      mainModule = commentModule;
      contentModule = new ModuleContent(DatabaseContent.getInstance());
      topicsModule = new ModuleTopics(DatabaseTopics.getInstance());
      imageModule = new ModuleImages(DatabaseImages.getInstance());
      defaultAction="addposting";
    }
    catch (StorageObjectFailure e) {
      logger.error("servletmoduleopenindy could not be initialized: " + e.getMessage());
    }
  }

  /**
   * Method to return an "apology" when open postings are disabled
   *
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */
  public void openPostingDisabled(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {
    deliver(aRequest, aResponse, (Map) null, null,
       configuration.getString("ServletModule.OpenIndy.PostingDisabledTemplate"));
  }

  /**
   *  Method for making a comment
   */

  public void addcomment(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {
    if (MirGlobal.abuse().getOpenPostingDisabled()) {
      openPostingDisabled(req, res);

      return;
    }

    String aid = req.getParameter("aid"); // the article id the comment will belong to

    if (aid != null && !aid.equals("")) {
      try {
        Map mergeData = new HashMap();

        // onetimepasswd
        if (MirGlobal.abuse().getOpenPostingPassword()) {
          String passwd = this.createOneTimePasswd();
          HttpSession session = req.getSession(false);
          session.setAttribute("passwd", passwd);
          mergeData.put("passwd", passwd);
        }
        else {
          mergeData.put("passwd", (String)null);
        }
        mergeData.put("aid", aid);

        Map extraInfo = new HashMap();
        extraInfo.put("languagePopUpData", DatabaseLanguage.getInstance().getPopupData());

        deliver(req, res, mergeData, extraInfo, commentFormTemplate);
      }
      catch (Throwable t) {
        throw new ServletModuleFailure("ServletModuleOpenIndy.addcomment: " + t.getMessage(), t);
      }
    }
    else
      throw new ServletModuleExc("aid not set!");
  }

  /**
   *  Method for inserting a comment into the Database and delivering
   *  the commentDone Page
   */

  public void inscomment(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {
    if (MirGlobal.abuse().getOpenPostingDisabled()) {
      openPostingDisabled(req, res);

      return;
    }

    String aid = req.getParameter("to_media"); // the article id the comment will belong to
    if (aid != null && !aid.equals("")) {
      // ok, collecting data from form
      try {
        Map withValues = getIntersectingValues(req, DatabaseComment.getInstance());

        //no html in comments(for now)
        for (Iterator i = withValues.keySet().iterator(); i.hasNext(); ) {
          String k = (String) i.next();
          String v = (String) withValues.get(k);

          withValues.put(k, StringUtil.removeHTMLTags(v));
        }
        withValues.put("is_published", "1");
        withValues.put("to_comment_status", "1");
        withValues.put("is_html", "0");

        //checking the onetimepasswd
        HttpSession session = req.getSession(false);
        String sessionPasswd = (String) session.getAttribute("passwd");
        if (sessionPasswd != null) {
          String passwd = req.getParameter("passwd");
          if (passwd == null || passwd.length() == 0) {
            throw new ServletModuleUserExc("comment.error.missingpassword", new String[] {});
          }
          if (!sessionPasswd.equals(passwd)) {
            throw new ServletModuleUserExc("comment.error.invalidpassword", new String[] {});
          }
          session.invalidate();
        }

        String id = mainModule.add(withValues);

        SimpleResponse response = new SimpleResponse();
        response.setResponseGenerator(commentFormDoneTemplate);

        if (id == null) {
          deliver(req, res, (Map)null, null, commentFormDupeTemplate);
        }
        else {
          DatabaseContent.getInstance().setUnproduced("id=" + aid);

          try {
            EntityComment comment = (EntityComment) DatabaseComment.getInstance().selectById(id);
            MirGlobal.localizer().openPostings().afterCommentPosting(comment);
            MirGlobal.abuse().checkComment(
                comment, new HTTPAdapters.HTTPRequestAdapter(req), res);
          }
          catch (Throwable t) {
            throw new ServletModuleExc(t.getMessage());
          }
        }

        // redirecting to url
        // should implement back to article
        deliver(req, res, response.getResponseValues(), null, response.getResponseGenerator());
      }
      catch (Throwable e) {
        throw new ServletModuleFailure(e);
      }
    }
    else
      throw new ServletModuleExc("aid not set!");

  }

  /**
   *  Method for delivering the form-Page for open posting
   */

  public void addposting(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure
  {
    try {
      if (MirGlobal.abuse().getOpenPostingDisabled()) {
        openPostingDisabled(req, res);

        return;
      }

      Map mergeData = new HashMap();

      // onetimepasswd
      if (MirGlobal.abuse().getOpenPostingPassword()) {
        String passwd = generateOnetimePassword();
        HttpSession session = req.getSession(false);
        session.setAttribute("passwd", passwd);
        mergeData.put("passwd", passwd);
      }
      else {
        mergeData.put("passwd", (String)null);
      }

      String maxMedia = configuration.getString("ServletModule.OpenIndy.MaxMediaUploadItems");
      String defaultMedia = configuration.getString("ServletModule.OpenIndy.DefaultMediaUploadItems");
      String numOfMedia = req.getParameter("medianum");

      if (numOfMedia == null || numOfMedia.equals("")) {
        numOfMedia = defaultMedia;
      }
      else if (Integer.parseInt(numOfMedia) > Integer.parseInt(maxMedia)) {
        numOfMedia = maxMedia;
      }

      int mediaNum = Integer.parseInt(numOfMedia);
      List mediaFields = new Vector();
      for (int i = 0; i < mediaNum; i++) {
        Integer mNum = new Integer(i + 1);
        mediaFields.add(mNum.toString());
      }
      mergeData.put("medianum", numOfMedia);
      mergeData.put("mediafields", mediaFields);
      mergeData.put("to_topic", null);

      Map extraInfo = new HashMap();
      extraInfo.put("languagePopUpData", DatabaseLanguage.getInstance().getPopupData());
      extraInfo.put("themenPopupData", topicsModule.getTopicsAsSimpleList());

      extraInfo.put("topics", topicsModule.getTopicsList());
      deliver(req, res, mergeData, extraInfo, postingFormTemplate);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  /**
   *  Method for inserting an open posting into the Database and delivering
   *  the postingDone Page
   */

  public void insposting(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure
  {
    if (MirGlobal.abuse().getOpenPostingDisabled()) {
      openPostingDisabled(req, res);

      return;
    }

    Map mergeData = new HashMap();
    boolean setMedia=false;
    boolean setTopic = false;

    try {

      WebdbMultipartRequest mp = null;
      EntityList mediaList = null;
      try {
        // new MediaRequest, "1" is the id for the openPosting user
        MediaRequest mediaReq = new MediaRequest("1", true);
        mp = new WebdbMultipartRequest(req, (FileHandler)mediaReq);
        mediaList = mediaReq.getEntityList();
      }
      catch (Throwable e) {
        throw new ServletModuleFailure(e);
      }

      Map withValues = mp.getParameters();

      //checking the onetimepasswd
      HttpSession session = req.getSession(false);
      String sessionPasswd = (String) session.getAttribute("passwd");
      if (sessionPasswd != null){
        String passwd = (String) withValues.get("passwd");

        if ( passwd == null || passwd.length()==0) {
          throw new ServletModuleUserExc("posting.error.missingpassword", new String[] {});
        }
        if (!sessionPasswd.equals(passwd)) {
          throw new ServletModuleUserExc("posting.error.invalidpassword", new String[] {});
        }
        session.invalidate();
      }

      if ((((String)withValues.get("title")).length() == 0) ||
          (((String)withValues.get("description")).length() == 0) ||
          (((String)withValues.get("content_data")).length() == 0))
        throw new ServletModuleUserExc("posting.error.missingfield", new String[] {});

      // call the routines that escape html

      for (Iterator i=withValues.keySet().iterator(); i.hasNext(); ){
        String k=(String)i.next();
        String v=(String)withValues.get(k);

        if (k.equals("content_data")){
          //this doesn't quite work yet, so for now, all html goes
          //withValues.put(k,StringUtil.approveHTMLTags(v));
          withValues.put(k,StringUtil.deleteForbiddenTags(v));
        }
        else if (k.equals("description")) {
          String tmp = StringUtil.deleteForbiddenTags(v);
          withValues.put(k,StringUtil.deleteHTMLTableTags(tmp));
        }
        else {
          withValues.put(k,StringUtil.removeHTMLTags(v));
        }

      }

      withValues.put("date", StringUtil.date2webdbDate(new GregorianCalendar()));
      withValues.put("publish_path", StringUtil.webdbDate2path((String)withValues.get("date")));
      withValues.put("is_produced", "0");
      withValues.put("is_published","1");
      if (directOp.equals("yes"))
        withValues.put("to_article_type","1");

      withValues.put("to_publisher","1");

      // inserting  content into database
      String cid = contentModule.add(withValues);
      logger.debug("id: "+cid);
      //insert was not successfull
      if(cid==null){

        //How do we know that it was not succesful cause of a
        //dupe, what if it failed cause of "No space left on device"?
        //Or is there something I am missing? Wouldn't it be better
        //to have an explicit dupe check and then insert? I have no
        //idea what I am talking about. this comment is in case
        //I forget to explicitely ask. -mh
        deliver(req, res, mergeData, null, postingFormDupeTemplate);
        return;
      }

      String[] to_topicsArr = mp.getParameterValues("to_topic");

      if (to_topicsArr != null && to_topicsArr.length > 0) {
        try{
          DatabaseContentToTopics.getInstance().setTopics(cid,to_topicsArr);
          setTopic = true;
        }
        catch (Throwable e) {
          logger.error("setting content_x_topic failed");
          contentModule.deleteById(cid);
          throw new ServletModuleFailure("smod - openindy :: insposting: setting content_x_topic failed: "+e.toString(), e);
        } //end try
      } //end if

      //if we're here all is ok... associate the media to the article
      for(int i=0;i<mediaList.size();i++) {
        Entity mediaEnt = (Entity)mediaList.elementAt(i);
        DatabaseContentToMedia.getInstance().addMedia(cid,mediaEnt.getId());
      }

      EntityContent article = (EntityContent) contentModule.getById(cid);

      try {
        MirGlobal.abuse().checkArticle(article, req, res);
        MirGlobal.localizer().openPostings().afterContentPosting(article);
      }
      catch (Throwable t) {
        logger.error("Error while post-processing article: " + t.getMessage());
      }
    }
    catch (Throwable e) {
      Throwable cause = ExceptionFunctions.traceCauseException(e);

      if (cause instanceof UnsupportedMediaFormatExc) {
        throw new ServletModuleUserExc("media.unsupportedformat", new String[] {} );
      }
      throw new ServletModuleFailure(e);
    }

    deliver(req, res, mergeData, null, postingFormDoneTemplate);
  }

  /**
   * Routine for innitiation tha d
   *
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */
/*
  public void comment(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {
    try {
      if (MirGlobal.abuse().getOpenPostingDisabled()) {
        openPostingDisabled(aRequest, aResponse);

        return;
      }

      Request request = new HTTPAdapters.HTTPParsedRequestAdapter(new HTTPParsedRequest(aRequest, 1000000, "/tmp"));
      Session session = new HTTPAdapters.HTTPSessionAdapter(aRequest.getSession());
      SimpleResponse response = new SimpleResponse(
          ServletHelper.makeGenerationData(new Locale[] {getLocale(aRequest), getFallbackLocale(aRequest)}, "bundles.open"));

      Iterator i = DatabaseContent.getInstance().getFields().iterator();
      while (i.hasNext()) {
        response.setResponseValue((String) i.next(), null);
      }

      MirGlobal.localizer().openPostings().initializeCommentPosting(request, session, response);

      ServletHelper.generateOpenPostingResponse(aResponse.getWriter(), response.getResponseValues(), response.getResponseGenerator());
    }
    catch (Throwable t) {
      t.printStackTrace(System.out);
      System.out.println("comment: " + t.toString());
      throw new ServletModuleFailure("ServletModuleOpenIndy.addcomment: " + t.getMessage(), t);
    }
  }

  public void processcomment(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {
    try {
      if (MirGlobal.abuse().getOpenPostingDisabled()) {
        openPostingDisabled(aRequest, aResponse);

        return;
      }

      Request request = new HTTPAdapters.HTTPParsedRequestAdapter(new HTTPParsedRequest(aRequest, 1000000, "/tmp"));
      Session session = new HTTPAdapters.HTTPSessionAdapter(aRequest.getSession());
      SimpleResponse response = new SimpleResponse(
          ServletHelper.makeGenerationData(new Locale[] {getLocale(aRequest), getFallbackLocale(aRequest)}, "bundles.open"));
      Map commentFields = new HashMap();

      Iterator i = DatabaseContent.getInstance().getFields().iterator();
      while (i.hasNext()) {
        String field = (String) i.next();
        response.setResponseValue(field, request.getParameter(field));
        if (request.getParameter(field)!=null) {
          commentFields.put(field, request.getParameter(field));
        }
      }

      List validationErrors = MirGlobal.localizer().openPostings().validateCommentPosting(request, session);

      if (validationErrors != null && validationErrors.size()>0) {
        MirGlobal.localizer().openPostings().processCommentPosting(request, session, response);

        ServletHelper.generateOpenPostingResponse(aResponse.getWriter(), response.getResponseValues(), response.getResponseGenerator());
      }
      else {
        EntityComment comment = (EntityComment) commentModule.createNew ();
        comment.setValues(commentFields);
        MirGlobal.abuse().checkComment(comment, aRequest, aResponse);
        MirGlobal.localizer().openPostings().finishCommentPosting(request, session, comment);

        String id = comment.insert();
        if(id==null){
          MirGlobal.localizer().openPostings().afterDuplicateCommentPosting(request, session, response, comment);

          logger.info("Dupe comment rejected");

          ServletHelper.generateOpenPostingResponse(aResponse.getWriter(), response.getResponseValues(), response.getResponseGenerator());
        }
        else {
          // media
          List mediaItems = new Vector();
          i = request.getUploadedFiles().iterator();
          while (i.hasNext()) {
            UploadedFile file = (UploadedFile) i.next();
            Entity mediaItem = MediaUploadProcessor.processMediaUpload(file);
            DatabaseCommentToMedia.getInstance().addMedia(comment.getId(), mediaItem.getId());
          }

          MirGlobal.localizer().openPostings().afterCommentPosting(request, session, response, comment);

          MirGlobal.abuse().logComment(aRequest.getRemoteAddr(), id, new Date(), (String) aRequest.getHeader("User-Agent"));
          DatabaseContent.getInstance().setUnproduced("id=" + comment.getValue("to_media"));
          logger.info("Comment posted");
          ServletHelper.generateOpenPostingResponse(aResponse.getWriter(), response.getResponseValues(), response.getResponseGenerator());
        }
      }
    }
    catch (Throwable t) {
      ExceptionFunctions.traceCauseException(t).printStackTrace();

      throw new ServletModuleFailure("ServletModuleOpenIndy.addcomment: " + t.getMessage(), t);
    }
  }
*/

  private static final String SESSION_REQUEST_KEY="sessionid";

  public void opensession(HttpServletRequest aRequest, HttpServletResponse aResponse)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {

    try {
      Request request = new HTTPAdapters.HTTPParsedRequestAdapter(new HTTPParsedRequest(aRequest, 1000000, "/tmp"));

      if (aRequest.isRequestedSessionIdValid() && !aRequest.isRequestedSessionIdFromURL() &&
          !aRequest.getRequestedSessionId().equals(aRequest.getParameter(SESSION_REQUEST_KEY)))
        aRequest.getSession().invalidate();

      Session session = new HTTPAdapters.HTTPSessionAdapter(aRequest.getSession());

      SimpleResponse response = new SimpleResponse(
          ServletHelper.makeGenerationData(new Locale[] {getLocale(aRequest), getFallbackLocale(aRequest)},
             "bundles.open"));

      response.setResponseValue("actionURL", aResponse.encodeURL(HttpUtils.getRequestURL(aRequest).toString())+"?"+SESSION_REQUEST_KEY+"="+aRequest.getSession().getId());

      SessionHandler handler = MirGlobal.localizer().openPostings().getOpenSessionHandler(request, session);

      handler.processRequest(request, session, response);
      ServletHelper.generateOpenPostingResponse(aResponse.getWriter(), response.getResponseValues(), response.getResponseGenerator());
    }
    catch (Throwable t) {
      logger.error(t.toString());
      t.printStackTrace(logger.asPrintWriter(logger.DEBUG_MESSAGE));

      throw new ServletModuleFailure(t);
    }
  }

  /**
   * Method for preparing and sending a content as an email message
   */

  public void mail(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure
  {
    String aid = req.getParameter("mail_aid");
    if (aid == null){
      throw new ServletModuleExc("An article id must be specified in requests to email an article.  Something therefore went badly wrong....");
    }

    String to = req.getParameter("mail_to");
    String from = req.getParameter("mail_from");
    String from_name = req.getParameter("mail_from_name");
    String comment = req.getParameter("mail_comment");
    String mail_language = req.getParameter("mail_language");

    Map mergeData = new HashMap();

    if (to == null || from == null || from_name == null|| to.equals("") || from.equals("") || from_name.equals("") || mail_language == null || mail_language.equals("")){

      for (Enumeration theParams = req.getParameterNames(); theParams.hasMoreElements() ;) {
        String pName=(String)theParams.nextElement();
        if (pName.startsWith("mail_")){
          mergeData.put( pName,req.getParameter(pName) );
        }
      }

      deliver(req, res, mergeData, null, prepareMailTemplate);
    }
    else {
      //run checks on to and from and mail_language to make sure no monkey business occurring
      if (mail_language.indexOf('.') != -1 || mail_language.indexOf('/') != -1 ) {
        throw new ServletModuleExc("Invalid language");
      }
      if (to.indexOf('\n') != -1
          || to.indexOf('\r') != -1
          || to.indexOf(',') != -1) {
        throw new ServletModuleUserExc("email.error.invalidtoaddress", new String[] {to});
      }
      if (from.indexOf('\n') != -1 || from.indexOf('\r') != -1 || from.indexOf(',') != -1 ) {
        throw new ServletModuleUserExc("email.error.invalidfromaddress", new String[] {from});
      }


      EntityContent contentEnt;
      try{
        contentEnt = (EntityContent)contentModule.getById(aid);
      }
      catch (Throwable e){
        throw new ServletModuleFailure("Couldn't get content for article "+aid + ": " + e.getMessage(), e);
      }
      String producerStorageRoot=configuration.getString("Producer.StorageRoot");
      String producerDocRoot=configuration.getString("Producer.DocRoot");
      String publishPath = contentEnt.getValue("publish_path");
      String txtFilePath = producerStorageRoot + producerDocRoot + "/" + mail_language +
                                                                                                         publishPath + "/" + aid + ".txt";


      File inputFile = new File(txtFilePath);
      String content;

      try{
        FileReader in = new FileReader(inputFile);
        StringWriter out = new StringWriter();
        int c;
        while ((c = in.read()) != -1)
          out.write(c);
        in.close();
        content= out.toString();
      }
      catch (FileNotFoundException e){
        throw new ServletModuleFailure("No text file found in " + txtFilePath, e);
      }
      catch (IOException e){
        throw new ServletModuleFailure("Problem reading file in " + txtFilePath, e);
      }
      // add some headers
      content = "To: " + to + "\nReply-To: "+ from + "\n" + content;
      // put in the comment where it should go
      if (comment != null) {
        String commentTextToInsert = "\n\nAttached comment from " + from_name + ":\n" + comment;
        try {
          content=StringRoutines.performRegularExpressionReplacement(content,"!COMMENT!",commentTextToInsert);
        }
        catch (Throwable e){
          throw new ServletModuleFailure("Problem doing regular expression replacement " + e.toString(), e);
        }
      }
      else{
        try {
          content=StringRoutines.performRegularExpressionReplacement(content,"!COMMENT!","");
        }
        catch (Throwable e){
          throw new ServletModuleFailure("Problem doing regular expression replacement " + e.toString(), e);
        }
      }

      SMTPClient client=new SMTPClient();
      try {
        int reply;
        client.connect(configuration.getString("ServletModule.OpenIndy.SMTPServer"));

        reply = client.getReplyCode();

        if (!SMTPReply.isPositiveCompletion(reply)) {
          client.disconnect();
          throw new ServletModuleExc("SMTP server refused connection.");
        }

        client.sendSimpleMessage(configuration.getString("ServletModule.OpenIndy.EmailIsFrom"), to, content);

        client.disconnect();
        //mission accomplished
        deliver(req, res, mergeData, null, sentMailTemplate);
      }
      catch(IOException e) {
        if(client.isConnected()) {
          try {
            client.disconnect();
          } catch(IOException f) {
            // do nothing
          }
        }
        throw new ServletModuleFailure(e);
      }
    }
  }



  /**
   * Method for querying a lucene index
   *
   * @param req
   * @param res
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */

  public void search(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {
    try {
      final String[] search_variables = { "search_content", "search_boolean", "search_creator",
          "search_topic", "search_hasImages", "search_hasAudio", "search_hasVideo", "search_sort",
          "search_submit", "search_back", "search_forward" };
      HTTPRequestParser requestParser = new HTTPRequestParser(req);

      int increment=10;

      HttpSession session = req.getSession(false);

      String queryString="";

      Map mergeData = new HashMap();

      KeywordSearchTerm dateTerm = new KeywordSearchTerm("date_formatted","search_date","webdb_create_formatted","webdb_create_formatted","webdb_create_formatted");
      UnIndexedSearchTerm whereTerm = new UnIndexedSearchTerm("","","","where","where");
      TextSearchTerm creatorTerm = new TextSearchTerm("creator","search_creator","creator","creator","creator");
      TextSearchTerm titleTerm = new TextSearchTerm("title","search_content","title","title","title");
      TextSearchTerm descriptionTerm =  new TextSearchTerm("description","search_content","description","description","description");
      ContentSearchTerm contentTerm = new ContentSearchTerm("content_data","search_content","content","","");
      TopicSearchTerm topicTerm = new TopicSearchTerm();
      ImagesSearchTerm imagesTerm = new ImagesSearchTerm();
      AudioSearchTerm audioTerm = new AudioSearchTerm();
      VideoSearchTerm videoTerm = new VideoSearchTerm();
      MediaSearchTerm mediaTerm = new MediaSearchTerm();
      
      //make the query available to subsequent iterations

      Iterator j = Arrays.asList(search_variables).iterator();
      while (j.hasNext()) {
        String variable = (String) j.next();

        mergeData.put(variable, requestParser.getParameter(variable));
      }

      try{
        mergeData.put("topics", topicsModule.getTopicsAsSimpleList());
      }
      catch(Throwable e) {
        logger.debug("Can't get topics: " + e.toString());
      }

      String searchBackValue = req.getParameter("search_back");
      String searchForwardValue = req.getParameter("search_forward");

      if (searchBackValue != null){
        int totalHits = ((Integer) session.getAttribute("numberOfHits")).intValue();
        int newPosition=((Integer)session.getAttribute("positionInResults")).intValue()-increment;
        if (newPosition<0)
          newPosition=0;
        if (newPosition >= totalHits)
          newPosition=totalHits-1;
        session.setAttribute("positionInResults",new Integer(newPosition));
      }
      else {
        if (searchForwardValue != null){
          int totalHits = ((Integer) session.getAttribute("numberOfHits")).intValue();
          int newPosition=((Integer)session.getAttribute("positionInResults")).intValue()+increment;
          if (newPosition<0)
            newPosition=0;
          if (newPosition >= totalHits)
            newPosition=totalHits-1;

          session.setAttribute("positionInResults",new Integer(newPosition));
        }
        else {
          String indexPath=configuration.getString("IndexPath");


          String creatorFragment = creatorTerm.makeTerm(req);
          if (creatorFragment != null){
            queryString = queryString + " +" + creatorFragment;
          }

          // search title, description, and content for something
          // the contentTerm uses param "search_boolean" to combine its terms
          String contentFragment = contentTerm.makeTerm(req);
          if (contentFragment != null){
            logger.debug("contentFragment: " + contentFragment);
            queryString = queryString + " +" + contentFragment;
          }

          String topicFragment = topicTerm.makeTerm(req);
          if (topicFragment != null){
            queryString = queryString + " +" + topicFragment;
          }

          String imagesFragment = imagesTerm.makeTerm(req);
          if (imagesFragment != null){
            queryString = queryString + " +" + imagesFragment;
          }

          String audioFragment = audioTerm.makeTerm(req);
          if (audioFragment != null){
            queryString = queryString + " +" + audioFragment;
          }

          String videoFragment = videoTerm.makeTerm(req);
          if (videoFragment != null){
            queryString = queryString + " +" + videoFragment;
          }

          String mediaFragment = mediaTerm.makeTerm(req);
          if (mediaFragment != null){
            queryString = queryString + " +" + mediaFragment;
          }

          if (queryString == null || queryString == ""){
            queryString = "";
          }
          else{
            try{
              Searcher searcher = null;
              try {
                searcher = new IndexSearcher(indexPath);
              }
              catch(IOException e) {
                logger.debug("Can't open indexPath: " + indexPath);
                throw new ServletModuleExc("Problem with Search Index! : "+ e.toString());
              }

              Query query = null;
              try {
                query = QueryParser.parse(queryString, "content", new StandardAnalyzer());
              }
              catch(Exception e) {
                searcher.close();
                logger.debug("Query don't parse: " + queryString);
                throw new ServletModuleExc("Problem with Query String! (was '"+queryString+"')");
              }

              Hits hits = null;
              try {
                hits = searcher.search(query);
              }
              catch(IOException e) {
                searcher.close();
                logger.debug("Can't get hits: " + e.toString());
                throw new ServletModuleExc("Problem getting hits!");
              }

              int start = 0;
              int end = hits.length();

              String sortBy=req.getParameter("search_sort");
              if (sortBy == null || sortBy.equals("")){
                throw new ServletModuleExc("Please let me sort by something!(missing search_sort)");
              }

              // here is where the documents will go for storage across sessions
              ArrayList theDocumentsSorted = new ArrayList();

              if (sortBy.equals("score")){
                for(int i = start; i < end; i++) {
                  theDocumentsSorted.add(hits.doc(i));
                }
              }
              else{
                // then we'll sort by date!
                Map dateToPosition = new HashMap(end,1.0F); //we know how big it will be
                for(int i = start; i < end; i++) {
                  String creationDate=(hits.doc(i)).get("creationDate");
                  // do a little dance in case two contents created at the same second!
                  if (dateToPosition.containsKey(creationDate)){
                    ((ArrayList) (dateToPosition.get(creationDate))).add(new Integer(i));
                  }
                  else{
                    ArrayList thePositions = new ArrayList();
                    thePositions.add(new Integer(i));
                    dateToPosition.put(creationDate,thePositions);
                  }
                }
                Set keys = dateToPosition.keySet();
                ArrayList keyList= new ArrayList(keys);
                Collections.sort(keyList);
                if (sortBy.equals("date_desc")){
                  Collections.reverse(keyList);
                }
                else{
                  if (!sortBy.equals("date_asc")){
                    throw new ServletModuleExc("don't know how to sort by: "+ sortBy);
                  }
                }
                ListIterator keyTraverser = keyList.listIterator();
                while (keyTraverser.hasNext()){
                  ArrayList positions = (ArrayList)dateToPosition.get((keyTraverser.next()));
                  ListIterator positionsTraverser=positions.listIterator();
                  while (positionsTraverser.hasNext()){
                    theDocumentsSorted.add(hits.doc(((Integer)(positionsTraverser.next())).intValue()));
                  }
                }
              }

              try{
                searcher.close();
              }
              catch (IOException e){
                logger.debug("Can't close searcher: " + e.toString());
                throw new ServletModuleFailure("Problem closing searcher(normal):" + e.getMessage(), e);
              }


              session.removeAttribute("numberOfHits");
              session.removeAttribute("theDocumentsSorted");
              session.removeAttribute("positionInResults");

              session.setAttribute("numberOfHits",new Integer(end));
              session.setAttribute("theDocumentsSorted",theDocumentsSorted);
              session.setAttribute("positionInResults",new Integer(0));

            }
            catch (IOException e){
              logger.debug("Can't close searcher: " + e.toString());
              throw new ServletModuleFailure("Problem closing searcher: " + e.getMessage(), e);
            }
          }
        }
      }

      try {
        ArrayList theDocs = (ArrayList)session.getAttribute("theDocumentsSorted");
        if (theDocs != null){

          mergeData.put("numberOfHits", ((Integer)session.getAttribute("numberOfHits")).toString());
          List theHits = new Vector();
          int pIR=((Integer)session.getAttribute("positionInResults")).intValue();
          int terminus;
          int numHits=((Integer)session.getAttribute("numberOfHits")).intValue();

          if (!(pIR+increment>=numHits)){
            mergeData.put("hasNext","y");
          }
          else {
            mergeData.put("hasNext", null);
          }
          if (pIR>0){
            mergeData.put("hasPrevious","y");
          }
          else {
            mergeData.put("hasPrevious", null);
          }

          if ((pIR+increment)>numHits){
            terminus=numHits;
          }
          else {
            terminus=pIR+increment;
          }
          for(int i = pIR; i < terminus; i++) {
            Map h = new HashMap();
            Document theHit = (Document)theDocs.get(i);
            whereTerm.returnMeta(h,theHit);
            creatorTerm.returnMeta(h,theHit);
            titleTerm.returnMeta(h,theHit);
            descriptionTerm.returnMeta(h,theHit);
            dateTerm.returnMeta(h,theHit);
            imagesTerm.returnMeta(h,theHit);
            audioTerm.returnMeta(h,theHit);
            videoTerm.returnMeta(h,theHit);
            theHits.add(h);
          }
          mergeData.put("hits",theHits);
        }
      }
      catch (Throwable e) {
        logger.error("Can't iterate over hits: " + e.toString());

        throw new ServletModuleFailure("Problem getting hits: " + e.getMessage(), e);
      }

      mergeData.put("queryString",queryString);

      deliver(req, res, mergeData, null, searchResultsTemplate);
    }
    catch (NullPointerException n){
      throw new ServletModuleFailure("Null Pointer: "+n.toString(), n);
    }
  }

  /*
   * Method for dynamically generating a pdf using iText
   */


  public void getpdf(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {
    long starttime=System.currentTimeMillis();
    String ID_REQUEST_PARAM = "id";
    int maxArticlesInNewsleter = 15; // it is nice not to be dos'ed
    try {
        String idParam = req.getParameter(ID_REQUEST_PARAM);
        if (idParam != null) {
          

          RE re = new RE("[0-9]+");
	  
	  
          REMatch[] idMatches=re.getAllMatches(idParam);
	  
	  String cacheSelector="";
	  
	  for (int i = 0; i < idMatches.length; i++){
	    cacheSelector=   cacheSelector + "," + idMatches[i].toString();
	  }
	  
	  String cacheType="pdf";
	  
	  CacheKey theCacheKey = new CacheKey(cacheType,cacheSelector);
	  
	  byte[] thePDF;
	  
	  if (MirGlobal.mruCache().hasObject(theCacheKey)){
	    logger.info("fetching pdf from cache");
	    thePDF = (byte[]) MirGlobal.mruCache().getObject(theCacheKey);
	  }
	  else {
	    logger.info("generating pdf and caching it");
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    PDFGenerator pdfMaker = new PDFGenerator(out);
	    
	    if (idMatches.length > 1){
	      pdfMaker.addLine();
	      for (int i = 0; i < idMatches.length  && i < maxArticlesInNewsleter; i++){
		REMatch aMatch = idMatches[i];
		String id=aMatch.toString();
		EntityContent contentEnt = (EntityContent)contentModule.getById(id);
		pdfMaker.addIndexItem(contentEnt);
	      }
	    }
	    
	    for (int i = 0; i < idMatches.length; i++){
	      REMatch aMatch = idMatches[i];
	      String id=aMatch.toString();
	      EntityContent contentEnt = (EntityContent)contentModule.getById(id);
	      
	      pdfMaker.add(contentEnt);
	    }
	    
	    pdfMaker.stop();
	    thePDF  = out.toByteArray();
	    
	    //and save all our hard work!
	    MirGlobal.mruCache().storeObject(theCacheKey,thePDF);
	  }
	  
	  res.setContentType("application/pdf");
	  res.setContentLength(thePDF.length);
	  res.getOutputStream().write(thePDF);
	  res.getOutputStream().flush();
	  String elapsedtime=(new Long(System.currentTimeMillis()-starttime)).toString();
	  logger.info("pdf retireval took "+elapsedtime + " milliseconds"  );

        }
        else {
          throw new ServletModuleExc("Missing id.");
        }
    }
    catch (Throwable t) {
      logger.error(t.toString());
      throw new ServletModuleFailure(t);
    }

  }


  public String generateOnetimePassword() {
    Random r = new Random();
    int random = r.nextInt();

    long l = System.currentTimeMillis();

    l = (l * l * l * l) / random;
    if (l < 0)
      l = l * -1;

    String returnString = "" + l;

    return returnString.substring(5);
  }

  public void deliver(HttpServletRequest aRequest, HttpServletResponse aResponse, Map aData, Map anExtra, String aGenerator)
      throws ServletModuleFailure {
    try {
      deliver(aResponse.getWriter(), aRequest, aResponse, aData, anExtra, aGenerator);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public void deliver(PrintWriter anOutputWriter, HttpServletRequest aRequest, HttpServletResponse aResponse, Map aData, Map anExtra, String aGenerator)
      throws ServletModuleFailure {
    try {
      Map responseData = ServletHelper.makeGenerationData(new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)}, "bundles.open");
      responseData.put("data", aData);
      responseData.put("extra", anExtra);


      Generator generator = MirGlobal.localizer().generators().makeOpenPostingGeneratorLibrary().makeGenerator(aGenerator);
      generator.generate(anOutputWriter, responseData, logger.asPrintWriter(logger.INFO_MESSAGE));

      anOutputWriter.close();
    }
    catch (Throwable e) {
      logger.error("Error while generating " + aGenerator + ": " + e.getMessage());

      throw new ServletModuleFailure(e);
    }
  }

  public void handleError(HttpServletRequest aRequest, HttpServletResponse aResponse,PrintWriter out, Throwable anException) {
    try {
      logger.error("error: " + anException);
      Map data = new HashMap();

      data.put("errorstring", anException.getMessage());
      data.put("date", StringUtil.date2readableDateTime(new GregorianCalendar()));

      deliver(out, aRequest, aResponse, data, null, configuration.getString("ServletModule.OpenIndy.ErrorTemplate"));
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void handleUserError(HttpServletRequest aRequest, HttpServletResponse aResponse,
                               PrintWriter out, ServletModuleUserExc anException) {
    try {
      logger.warn("user error: " + anException.getMessage());
      Map data = new HashMap();

      MessageResources messages = MessageResources.getMessageResources("bundles.open");
      data.put("errorstring",
              messages.getMessage(getLocale(aRequest), anException.getMessage(), anException.getParameters())
          );
      data.put("date", StringUtil.date2readableDateTime(new GregorianCalendar()));

      deliver(out, aRequest, aResponse, data, null, configuration.getString("ServletModule.OpenIndy.UserErrorTemplate"));
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  private String createOneTimePasswd() {
    return "";
  }
}
