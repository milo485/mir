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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.struts.util.MessageResources;
import gnu.regexp.RE;
import gnu.regexp.REMatch;

import mir.entity.Entity;
import mir.generator.Generator;
import mir.log.LoggerWrapper;
import mir.misc.StringUtil;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.servlet.ServletModuleUserExc;
import mir.session.HTTPAdapters;
import mir.session.Request;
import mir.session.Session;
import mir.session.SessionHandler;
import mir.session.SimpleResponse;
import mir.session.UploadedFile;
import mir.storage.StorageObjectFailure;
import mir.util.ExceptionFunctions;
import mir.util.HTTPParsedRequest;
import mir.util.HTTPRequestParser;
import mir.util.StringRoutines;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.global.CacheKey;
import mircoders.global.MirGlobal;
import mircoders.media.MediaUploadProcessor;
import mircoders.module.ModuleComment;
import mircoders.module.ModuleContent;
import mircoders.module.ModuleImages;
import mircoders.module.ModuleMediaType;
import mircoders.module.ModuleTopics;
import mircoders.pdf.PDFGenerator;
import mircoders.search.AudioSearchTerm;
import mircoders.search.ContentSearchTerm;
import mircoders.search.ImagesSearchTerm;
import mircoders.search.KeywordSearchTerm;
import mircoders.search.TextSearchTerm;
import mircoders.search.TopicSearchTerm;
import mircoders.search.TopicMatrixSearchTerm;
import mircoders.search.UnIndexedSearchTerm;
import mircoders.search.VideoSearchTerm;
import mircoders.storage.DatabaseComment;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseContentToMedia;
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
 * @version $Id: ServletModuleOpenIndy.java,v 1.90 2003/09/03 18:29:05 zapata Exp $
 *
 */

public class ServletModuleOpenIndy extends ServletModule
{

  private String        commentFormTemplate, commentFormDoneTemplate, commentFormDupeTemplate;
  private String        postingFormTemplate, postingFormDoneTemplate, postingFormDupeTemplate;
  private String        searchResultsTemplate;
  private String        prepareMailTemplate,sentMailTemplate,emailAnArticleTemplate;
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
      emailAnArticleTemplate = configuration.getString("ServletModule.OpenIndy.MailableArticleTemplate");
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
      extraInfo.put("themenPopupData", DatabaseTopics.getInstance().getPopupData());

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

  public void insposting(HttpServletRequest aRequest, HttpServletResponse aResponse) throws
      ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {
    if (MirGlobal.abuse().getOpenPostingDisabled()) {
      openPostingDisabled(aRequest, aResponse);

      return;
    }

    try {
      HTTPParsedRequest parsedRequest = new HTTPParsedRequest(
          aRequest,
          configuration.getString("Mir.DefaultEncoding"),
          configuration.getInt("MaxMediaUploadSize")*1024,
          configuration.getString("TempDir"));

      Map mergeData = new HashMap();

      HttpSession session = aRequest.getSession(false);
      String sessionPasswd = (String) session.getAttribute("passwd");
      if (sessionPasswd != null) {
        String passwd = (String) parsedRequest.getParameter("passwd");

        if (passwd == null || passwd.length() == 0) {
          throw new ServletModuleUserExc("posting.error.missingpassword", new String[] {});
        }
        if (!sessionPasswd.equals(passwd)) {
          throw new ServletModuleUserExc("posting.error.invalidpassword", new String[] {});
        }
        session.invalidate();
      }

      if ((((String) parsedRequest.getParameter("title")).length() == 0) ||
          (((String) parsedRequest.getParameter("description")).length() == 0) ||
          (((String) parsedRequest.getParameter("content_data")).length() == 0))
        throw new ServletModuleUserExc("posting.error.missingfield", new String[] {});

      List mediaList = new Vector();
      Iterator i = parsedRequest.getFiles().iterator();

      while (i.hasNext()) {
        UploadedFile file = new mir.session.CommonsUploadedFileAdapter((FileItem) i.next());
        Map mediaValues = new HashMap();

        String suffix = file.getFieldName().substring(5); // media${m}
        logger.debug("media_title" + suffix);
        String title = parsedRequest.getParameter("media_title" + suffix);

        mediaValues.put("title", StringUtil.removeHTMLTags(title));
        mediaValues.put("creator", StringUtil.removeHTMLTags(parsedRequest.getParameter("creator")));
        mediaValues.put("to_publisher", "0");
        mediaValues.put("is_published", "1");
        mediaValues.put("to_media_folder", "7");

        mediaList.add(MediaUploadProcessor.processMediaUpload(file, mediaValues));
      }

      Map withValues = new HashMap();
      i = DatabaseContent.getInstance().getFields().iterator();
      while (i.hasNext()) {
        String field = (String) i.next();
        String value = parsedRequest.getParameter(field);
        if (value!=null)
          withValues.put(field, value);
      }


      for (i = withValues.keySet().iterator(); i.hasNext(); ) {
        String k = (String) i.next();
        String v = (String) withValues.get(k);

        if (k.equals("content_data")) {
          //this doesn't quite work yet, so for now, all html goes
          //withValues.put(k,StringUtil.approveHTMLTags(v));
          withValues.put(k, StringUtil.deleteForbiddenTags(v));
        }
        else if (k.equals("description")) {
          String tmp = StringUtil.deleteForbiddenTags(v);
          withValues.put(k, StringUtil.deleteHTMLTableTags(tmp));
        }
        else {
          withValues.put(k, StringUtil.removeHTMLTags(v));
        }
      }

      withValues.put("date", StringUtil.date2webdbDate(new GregorianCalendar()));
      withValues.put("publish_path",
                     StringUtil.webdbDate2path( (String) withValues.get("date")));
      withValues.put("is_produced", "0");
      withValues.put("is_published", "1");
      if (directOp.equals("yes"))
        withValues.put("to_article_type", "1");

      withValues.put("to_publisher", "1");

      // inserting  content into database
      String cid = contentModule.add(withValues);
      logger.debug("id: " + cid);
      //insert was not successfull
      if (cid == null) {
        deliver(aRequest, aResponse, mergeData, null, postingFormDupeTemplate);
        return;
      }

      List topics = parsedRequest.getParameterList("to_topic");
      if (topics.size() > 0) {
        try {
          DatabaseContentToTopics.getInstance().setTopics(cid, topics);
        }
        catch (Throwable e) {
          logger.error("setting content_x_topic failed");
          contentModule.deleteById(cid);
          throw new ServletModuleFailure(
              "smod - openindy :: insposting: setting content_x_topic failed: " +
              e.toString(), e);
        }
      }

      i = mediaList.iterator();
      while (i.hasNext()) {
        Entity mediaEnt = (Entity) i.next();
        DatabaseContentToMedia.getInstance().addMedia(cid, mediaEnt.getId());
      }

      EntityContent article = (EntityContent) contentModule.getById(cid);
      try {
        MirGlobal.abuse().checkArticle(
            article, new HTTPAdapters.HTTPRequestAdapter(aRequest), aResponse);
        MirGlobal.localizer().openPostings().afterContentPosting(article);
      }
      catch (Throwable t) {
        logger.error("Error while post-processing article: " + t.getMessage());
      }
      deliver(aRequest, aResponse, mergeData, null, postingFormDoneTemplate);
    }
    catch (Throwable e) {
      e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
      Throwable cause = ExceptionFunctions.traceCauseException(e);

      if (cause instanceof ModuleMediaType.UnsupportedMimeTypeExc) {
        throw new ServletModuleUserExc("media.unsupportedformat", new String[] {});
      }
      throw new ServletModuleFailure(e);
    }
  }

  /**
   * Due to a serious shortcoming of Tomcat 3.3, an extra sessionid parameter is
   *   generated into open session urls. Tomcat 3.3 makes it impossible to
   *   distinguish between sessions that are identified using a url and those
   *   that are identified using cookies: if both a sessionid cookie and a sessionid
   *   url are available, tomcat 3.3 pretends the url wasn't there...
   */
  private static final String SESSION_REQUEST_KEY="sessionid";

  /**
   * Selects the language for the response.
   *
   * @param session
   * @param aRequest
   */

  protected Locale getResponseLocale(HttpSession aSession, HttpServletRequest aRequest) {
    String requestLanguage = aRequest.getParameter("language");
    String sessionLanguage = (String) aSession.getAttribute("language");
    String acceptLanguage = aRequest.getLocale().getLanguage();
    String defaultLanguage = configuration.getString("Mir.Login.DefaultLanguage", "en");

    String language = requestLanguage;

    if (language==null)
      language = sessionLanguage;

    if (language==null)
      language = acceptLanguage;

    if (language==null)
      language = defaultLanguage;

    aSession.setAttribute("language", language);

    return new Locale(language, "");
  }

  /**
   * Dispatch method for open sessions: a flexible extensible and customizable way
   *   for open access. Can be used for postings, but also for lots of other stuff.
   *
   * @param aRequest
   * @param aResponse
   * @throws ServletModuleExc
   * @throws ServletModuleUserExc
   * @throws ServletModuleFailure
   */

  public void opensession(HttpServletRequest aRequest, HttpServletResponse aResponse)
      throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {

    try {
      Request request =
          new HTTPAdapters.HTTPParsedRequestAdapter(new HTTPParsedRequest(aRequest,
              configuration.getString("Mir.DefaultEncoding"),
              configuration.getInt("MaxMediaUploadSize")*1024,
              configuration.getString("TempDir")));

      if (aRequest.isRequestedSessionIdValid() && !aRequest.isRequestedSessionIdFromURL() &&
          !aRequest.getRequestedSessionId().equals(aRequest.getParameter(SESSION_REQUEST_KEY)))
        aRequest.getSession().invalidate();

      Session session = new HTTPAdapters.HTTPSessionAdapter(aRequest.getSession());

      SimpleResponse response = new SimpleResponse(
          ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getResponseLocale(aRequest.getSession(), aRequest), getFallbackLocale(aRequest)},
             "bundles.open"));

      response.setResponseValue("actionURL", aResponse.encodeURL(MirGlobal.config().getString("RootUri") + "/servlet/OpenMir")+"?"+SESSION_REQUEST_KEY+"="+aRequest.getSession().getId());

      SessionHandler handler = MirGlobal.localizer().openPostings().getOpenSessionHandler(request, session);

      handler.processRequest(request, session, response);
      ServletHelper.generateOpenPostingResponse(aResponse.getWriter(), response.getResponseValues(), response.getResponseGenerator());
    }
    catch (Throwable t) {
      logger.error(t.toString());
      t.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

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
    String from_ip = req.getRemoteAddr();
    String comment = req.getParameter("mail_comment");
    String mail_language = req.getParameter("mail_language");

    Map mergeData = new HashMap();
    mergeData.put("mail_to",to);
    mergeData.put("mail_from",from);
    mergeData.put("mail_from_name",from_name);
    mergeData.put("mail_comment",comment);
    mergeData.put("mail_aid",aid);
    mergeData.put("mail_language",mail_language);


    if (to == null || from == null || from_name == null|| to.equals("") || from.equals("") || from_name.equals("") || mail_language == null || mail_language.equals("")){
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

      CacheKey theCacheKey=new CacheKey("email",aid+mail_language);
      String theEmailText;

      if (MirGlobal.mruCache().hasObject(theCacheKey)){
        logger.info("fetching email text for article "+aid+" from cache");
        theEmailText = (String) MirGlobal.mruCache().getObject(theCacheKey);
      }
      else {
        EntityContent contentEnt;
        try {
          contentEnt = (EntityContent) contentModule.getById(aid);
          StringWriter theEMailTextWriter = new StringWriter();
          PrintWriter dest = new PrintWriter(theEMailTextWriter);
          Map articleData = new HashMap();
          articleData.put("article", MirGlobal.localizer().dataModel().adapterModel().makeEntityAdapter("content", contentEnt));
          articleData.put("languagecode", mail_language);
          deliver(dest, req, res, articleData, null, emailAnArticleTemplate, mail_language);
          theEmailText = theEMailTextWriter.toString();
          MirGlobal.mruCache().storeObject(theCacheKey, theEmailText);
        }
        catch (Throwable e) {
          throw new ServletModuleFailure("Couldn't get content for article " + aid + mail_language + ": " + e.getMessage(), e);
        }
      }

      String content = theEmailText;


      // add some headers
      content = "To: " + to + "\nReply-To: "+ from + "\nX-Originating-IP: "+ from_ip + "\n" + content;
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

  public void search(HttpServletRequest req, HttpServletResponse res) throws ServletModuleExc, ServletModuleUserExc, ServletModuleFailure {
    try {
      final String[] search_variables = {
          "search_content", "search_boolean", "search_creator",
          "search_topic", "search_hasImages", "search_hasAudio", "search_hasVideo", "search_sort",
          "search_submit", "search_back", "search_forward"};
      HTTPRequestParser requestParser = new HTTPRequestParser(req);

      int increment = 10;

      HttpSession session = req.getSession(false);

      String queryString = "";

      Map mergeData = new HashMap();

      KeywordSearchTerm dateTerm = new KeywordSearchTerm("date_formatted", "search_date", "webdb_create_formatted", "webdb_create_formatted", "webdb_create_formatted");
      UnIndexedSearchTerm whereTerm = new UnIndexedSearchTerm("", "", "", "where", "where");
      TextSearchTerm creatorTerm = new TextSearchTerm("creator", "search_creator", "creator", "creator", "creator");
      TextSearchTerm titleTerm = new TextSearchTerm("title", "search_content", "title", "title", "title");
      TextSearchTerm descriptionTerm = new TextSearchTerm("description", "search_content", "description", "description", "description");
      ContentSearchTerm contentTerm = new ContentSearchTerm("content_data", "search_content", "content", "", "");
      TopicSearchTerm topicTerm = new TopicSearchTerm();
      TopicMatrixSearchTerm topicMatrixTerm = new TopicMatrixSearchTerm();
      ImagesSearchTerm imagesTerm = new ImagesSearchTerm();
      AudioSearchTerm audioTerm = new AudioSearchTerm();
      VideoSearchTerm videoTerm = new VideoSearchTerm();

      //make the query available to subsequent iterations

      Iterator j = Arrays.asList(search_variables).iterator();
      while (j.hasNext()) {
        String variable = (String) j.next();

        mergeData.put(variable, requestParser.getParameter(variable));
      }

      try {
        mergeData.put("topics", DatabaseTopics.getInstance().getPopupData());
      }
      catch (Throwable e) {
        logger.debug("Can't get topics: " + e.toString());
      }

      String searchBackValue = req.getParameter("search_back");
      String searchForwardValue = req.getParameter("search_forward");

      if (searchBackValue != null) {
        int totalHits = ( (Integer) session.getAttribute("numberOfHits")).intValue();
        int newPosition = ( (Integer) session.getAttribute("positionInResults")).intValue() - increment;
        if (newPosition < 0)
          newPosition = 0;
        if (newPosition >= totalHits)
          newPosition = totalHits - 1;
        session.setAttribute("positionInResults", new Integer(newPosition));
      }
      else {
        if (searchForwardValue != null) {
          int totalHits = ( (Integer) session.getAttribute("numberOfHits")).intValue();
          int newPosition = ( (Integer) session.getAttribute("positionInResults")).intValue() + increment;
          if (newPosition < 0)
            newPosition = 0;
          if (newPosition >= totalHits)
            newPosition = totalHits - 1;

          session.setAttribute("positionInResults", new Integer(newPosition));
        }
        else {
          String indexPath = configuration.getString("IndexPath");

          String creatorFragment = creatorTerm.makeTerm(req);
          if (creatorFragment != null) {
            queryString = queryString + " +" + creatorFragment;
          }

          // search title, description, and content for something
          // the contentTerm uses param "search_boolean" to combine its terms
          String contentFragment = contentTerm.makeTerm(req);
          if (contentFragment != null) {
            logger.debug("contentFragment: " + contentFragment);
            queryString = queryString + " +" + contentFragment;
          }

          String topicFragment = topicTerm.makeTerm(req);
          if (topicFragment != null) {
            queryString = queryString + " +" + topicFragment;
          }

          String topicMatrixFragment = topicMatrixTerm.makeTerm(req);
          if (topicMatrixFragment != null) {
            queryString = queryString + " +" + topicMatrixFragment;
          }

          String imagesFragment = imagesTerm.makeTerm(req);
          if (imagesFragment != null) {
            queryString = queryString + " +" + imagesFragment;
          }

          String audioFragment = audioTerm.makeTerm(req);
          if (audioFragment != null) {
            queryString = queryString + " +" + audioFragment;
          }

          String videoFragment = videoTerm.makeTerm(req);
          if (videoFragment != null) {
            queryString = queryString + " +" + videoFragment;
          }

          if (queryString == null || queryString == "") {
            queryString = "";
          }
          else {
            try {
              Searcher searcher = null;
              try {
                searcher = new IndexSearcher(indexPath);
              }
              catch (IOException e) {
                logger.debug("Can't open indexPath: " + indexPath);
                throw new ServletModuleExc("Problem with Search Index! : " + e.toString());
              }

              Query query = null;
              try {
                query = QueryParser.parse(queryString, "content", new StandardAnalyzer());
              }
              catch (Exception e) {
                searcher.close();
                logger.debug("Query don't parse: " + queryString);
                throw new ServletModuleExc("Problem with Query String! (was '" + queryString + "')");
              }

              Hits hits = null;
              try {
                hits = searcher.search(query);
              }
              catch (IOException e) {
                searcher.close();
                logger.debug("Can't get hits: " + e.toString());
                throw new ServletModuleExc("Problem getting hits!");
              }

              int start = 0;
              int end = hits.length();

              String sortBy = req.getParameter("search_sort");
              if (sortBy == null || sortBy.equals("")) {
                throw new ServletModuleExc("Please let me sort by something!(missing search_sort)");
              }

              // here is where the documents will go for storage across sessions
              ArrayList theDocumentsSorted = new ArrayList();

              if (sortBy.equals("score")) {
                for (int i = start; i < end; i++) {
                  theDocumentsSorted.add(hits.doc(i));
                }
              }
              else {
                // then we'll sort by date!
                Map dateToPosition = new HashMap(end, 1.0F); //we know how big it will be
                for (int i = start; i < end; i++) {
                  String creationDate = (hits.doc(i)).get("creationDate");
                  // do a little dance in case two contents created at the same second!
                  if (dateToPosition.containsKey(creationDate)) {
                    ( (ArrayList) (dateToPosition.get(creationDate))).add(new Integer(i));
                  }
                  else {
                    ArrayList thePositions = new ArrayList();
                    thePositions.add(new Integer(i));
                    dateToPosition.put(creationDate, thePositions);
                  }
                }
                Set keys = dateToPosition.keySet();
                ArrayList keyList = new ArrayList(keys);
                Collections.sort(keyList);
                if (sortBy.equals("date_desc")) {
                  Collections.reverse(keyList);
                }
                else {
                  if (!sortBy.equals("date_asc")) {
                    throw new ServletModuleExc("don't know how to sort by: " + sortBy);
                  }
                }
                ListIterator keyTraverser = keyList.listIterator();
                while (keyTraverser.hasNext()) {
                  ArrayList positions = (ArrayList) dateToPosition.get( (keyTraverser.next()));
                  ListIterator positionsTraverser = positions.listIterator();
                  while (positionsTraverser.hasNext()) {
                    theDocumentsSorted.add(hits.doc( ( (Integer) (positionsTraverser.next())).intValue()));
                  }
                }
              }

              try {
                searcher.close();
              }
              catch (IOException e) {
                logger.debug("Can't close searcher: " + e.toString());
                throw new ServletModuleFailure("Problem closing searcher(normal):" + e.getMessage(), e);
              }

              session.removeAttribute("numberOfHits");
              session.removeAttribute("theDocumentsSorted");
              session.removeAttribute("positionInResults");

              session.setAttribute("numberOfHits", new Integer(end));
              session.setAttribute("theDocumentsSorted", theDocumentsSorted);
              session.setAttribute("positionInResults", new Integer(0));

            }
            catch (IOException e) {
              logger.debug("Can't close searcher: " + e.toString());
              throw new ServletModuleFailure("Problem closing searcher: " + e.getMessage(), e);
            }
          }
        }
      }

      try {
        ArrayList theDocs = (ArrayList) session.getAttribute("theDocumentsSorted");
        if (theDocs != null) {

          mergeData.put("numberOfHits", ( (Integer) session.getAttribute("numberOfHits")).toString());
          List theHits = new Vector();
          int pIR = ( (Integer) session.getAttribute("positionInResults")).intValue();
          int terminus;
          int numHits = ( (Integer) session.getAttribute("numberOfHits")).intValue();

          if (! (pIR + increment >= numHits)) {
            mergeData.put("hasNext", "y");
          }
          else {
            mergeData.put("hasNext", null);
          }
          if (pIR > 0) {
            mergeData.put("hasPrevious", "y");
          }
          else {
            mergeData.put("hasPrevious", null);
          }

          if ( (pIR + increment) > numHits) {
            terminus = numHits;
          }
          else {
            terminus = pIR + increment;
          }
          for (int i = pIR; i < terminus; i++) {
            Map h = new HashMap();
            Document theHit = (Document) theDocs.get(i);
            whereTerm.returnMeta(h, theHit);
            creatorTerm.returnMeta(h, theHit);
            titleTerm.returnMeta(h, theHit);
            descriptionTerm.returnMeta(h, theHit);
            dateTerm.returnMeta(h, theHit);
            imagesTerm.returnMeta(h, theHit);
            audioTerm.returnMeta(h, theHit);
            videoTerm.returnMeta(h, theHit);
            theHits.add(h);
          }
          mergeData.put("hits", theHits);
        }
      }
      catch (Throwable e) {
        logger.error("Can't iterate over hits: " + e.toString());

        throw new ServletModuleFailure("Problem getting hits: " + e.getMessage(), e);
      }

      mergeData.put("queryString", queryString);

      deliver(req, res, mergeData, null, searchResultsTemplate);
    }
    catch (NullPointerException n) {
      throw new ServletModuleFailure("Null Pointer: " + n.toString(), n);
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

  public void deliver(HttpServletRequest aRequest, HttpServletResponse aResponse, Map aData, Map anExtra, String aGenerator) throws ServletModuleFailure {
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
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)}, "bundles.open");
      responseData.put("data", aData);
      responseData.put("extra", anExtra);


      Generator generator = MirGlobal.localizer().generators().makeOpenPostingGeneratorLibrary().makeGenerator(aGenerator);
      generator.generate(anOutputWriter, responseData, logger);

      anOutputWriter.close();
    }
    catch (Throwable e) {
      logger.error("Error while generating " + aGenerator + ": " + e.getMessage());

      throw new ServletModuleFailure(e);
    }
  }

  public void deliver(PrintWriter anOutputWriter, HttpServletRequest aRequest, HttpServletResponse aResponse, Map aData, Map anExtra, String aGenerator,String aLocaleString)
      throws ServletModuleFailure {
    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { new Locale(aLocaleString,""), getFallbackLocale(aRequest)}, "bundles.open");
      responseData.put("data", aData);
      responseData.put("extra", anExtra);


      Generator generator = MirGlobal.localizer().generators().makeOpenPostingGeneratorLibrary().makeGenerator(aGenerator);
      generator.generate(anOutputWriter, responseData, logger);

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
      data.put("errorstring", messages.getMessage(getLocale(aRequest), anException.getMessage(), anException.getParameters()));
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
