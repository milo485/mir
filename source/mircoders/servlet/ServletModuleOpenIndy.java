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
import java.util.Collections;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.entity.Entity;
import mir.entity.EntityList;
import mir.log.LoggerWrapper;
import mir.misc.FileHandler;
import mir.misc.FileHandlerException;
import mir.misc.FileHandlerUserException;
import mir.misc.HTMLParseException;
import mir.misc.HTMLTemplateProcessor;
import mir.misc.StringUtil;
import mir.misc.WebdbMultipartRequest;
import mir.module.ModuleException;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleException;
import mir.servlet.ServletModuleUserException;
import mir.storage.StorageObjectFailure;
import mir.util.StringRoutines;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.global.MirGlobal;
import mircoders.media.MediaRequest;
import mircoders.module.ModuleComment;
import mircoders.module.ModuleContent;
import mircoders.module.ModuleImages;
import mircoders.module.ModuleTopics;
import mircoders.search.AudioSearchTerm;
import mircoders.search.ContentSearchTerm;
import mircoders.search.ImagesSearchTerm;
import mircoders.search.KeywordSearchTerm;
import mircoders.search.TextSearchTerm;
import mircoders.search.TopicSearchTerm;
import mircoders.search.UnIndexedSearchTerm;
import mircoders.search.VideoSearchTerm;
import mircoders.storage.DatabaseComment;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseContentToMedia;
import mircoders.storage.DatabaseContentToTopics;
import mircoders.storage.DatabaseImages;
import mircoders.storage.DatabaseLanguage;
import mircoders.storage.DatabaseTopics;

import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.XSLTInputHandler;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleList;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelRoot;

/*
 *  ServletModuleOpenIndy -
 *   is the open-access-servlet, which is responsible for
 *    adding comments to articles &
 *    open-postings to the newswire
 *
 * @author mir-coders group
 * @version $Id: ServletModuleOpenIndy.java,v 1.58 2003/01/25 17:50:36 idfx Exp $
 *
 */

public class ServletModuleOpenIndy extends ServletModule
{

  private String        commentFormTemplate, commentFormDoneTemplate,
    commentFormDupeTemplate;
  private String        postingFormTemplate, postingFormDoneTemplate,
    postingFormDupeTemplate;
  private String        searchResultsTemplate;
  private String        prepareMailTemplate,sentMailTemplate;
  private ModuleContent contentModule;
  private ModuleComment commentModule;
  private ModuleImages  imageModule;
  private ModuleTopics  themenModule;
  private String        directOp ="yes";
  private String        passwdProtection ="yes";
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
      passwdProtection = configuration.getString("PasswdProtection").toLowerCase();
      mainModule = new ModuleComment(DatabaseComment.getInstance());
      contentModule = new ModuleContent(DatabaseContent.getInstance());
      themenModule = new ModuleTopics(DatabaseTopics.getInstance());
      imageModule = new ModuleImages(DatabaseImages.getInstance());
      defaultAction="addposting";

    }
    catch (StorageObjectFailure e) {
      logger.error("servletmoduleopenindy could not be initialized: " + e.getMessage());
    }
  }


  /**
   *  Method for making a comment
   */

  public void addcomment(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    String aid = req.getParameter("aid"); // the article id the comment will belong to
    String language = req.getParameter("language");

    if (aid!=null && !aid.equals("")) {
      try {
        SimpleHash mergeData = new SimpleHash();

        // onetimepasswd
        if (passwdProtection.equals("yes")) {
          String passwd = this.createOneTimePasswd();
          System.out.println(passwd);
          HttpSession session = req.getSession(false);
          session.setAttribute("passwd", passwd);
          mergeData.put("passwd", passwd);
        }

        if (language != null) {
          HttpSession session = req.getSession(false);
          session.setAttribute("Locale", new Locale(language, ""));
          session.setAttribute("passwd", language);
        }

        mergeData.put("aid", aid);

        SimpleHash extraInfo = new SimpleHash();
        extraInfo.put("languagePopUpData", DatabaseLanguage.getInstance().getPopupData());

        deliver(req, res, mergeData, extraInfo, commentFormTemplate);
      }
      catch (Throwable t) {
        throw new ServletModuleException("ServletModuleOpenIndy.addcomment: " + t.getMessage());
      }
    }
    else throw new ServletModuleException("aid not set!");
  }

  /**
   *  Method for inserting a comment into the Database and delivering
   *  the commentDone Page
   */

  public void inscomment(HttpServletRequest req, HttpServletResponse res)
    throws ServletModuleException,ServletModuleUserException
  {
    String aid = req.getParameter("to_media"); // the article id the comment will belong to
    if (aid!=null && !aid.equals(""))
      {
        // ok, collecting data from form
        try {
          HashMap withValues = getIntersectingValues(req, DatabaseComment.getInstance());

          //no html in comments(for now)
          for (Iterator i=withValues.keySet().iterator(); i.hasNext(); ){
            String k=(String)i.next();
            String v=(String)withValues.get(k);

            withValues.put(k,StringUtil.removeHTMLTags(v));
          }
          withValues.put("is_published","1");
          withValues.put("to_comment_status","1");

          //checking the onetimepasswd
          if(passwdProtection.equals("yes")){
            HttpSession session = req.getSession(false);
            String sessionPasswd = (String)session.getAttribute("passwd");
            if ( sessionPasswd == null){
              throw new ServletModuleUserException("Lost password");
            }
            String passwd = req.getParameter("passwd");
            if ( passwd == null || (!sessionPasswd.equals(passwd))) {
              throw new ServletModuleUserException("Missing password");
            }
            session.invalidate();
          }

          // inserting into database
          String id = mainModule.add(withValues);
          logger.debug("id: "+id);
          //insert was not successfull
          if(id==null){
            deliver(req, res, new SimpleHash(), commentFormDupeTemplate);
          } else {
            DatabaseContent.getInstance().setUnproduced("id="+aid);

            try {
              EntityComment comment = (EntityComment) DatabaseComment.getInstance().selectById(id);
              MirGlobal.localizer().openPostings().afterCommentPosting(comment);
            }
            catch (Throwable t) {
              throw new ServletModuleException(t.getMessage());
            }
          }

          // redirecting to url
          // should implement back to article
          SimpleHash mergeData = new SimpleHash();
          deliver(req, res, mergeData, commentFormDoneTemplate);
        }
        catch (StorageObjectFailure e) { throw new ServletModuleException(e.toString());}
        catch (ModuleException e) { throw new ServletModuleException(e.toString());}

      }
    else throw new ServletModuleException("aid not set!");

  }

  /**
   *  Method for delivering the form-Page for open posting
   */

  public void addposting(HttpServletRequest req, HttpServletResponse res)
    throws ServletModuleException {
    SimpleHash mergeData = new SimpleHash();

    // onetimepasswd
    if(passwdProtection.equals("yes")){
      String passwd = this.createOneTimePasswd();
      System.out.println(passwd);
      HttpSession session = req.getSession(false);
      session.setAttribute("passwd",passwd);
      mergeData.put("passwd", passwd);
    }

    String maxMedia = configuration.getString("ServletModule.OpenIndy.MaxMediaUploadItems");
    String defaultMedia = configuration.getString("ServletModule.OpenIndy.DefaultMediaUploadItems");
    String numOfMedia = req.getParameter("medianum");

    if(numOfMedia==null||numOfMedia.equals("")){
      numOfMedia=defaultMedia;
    }
    else if(Integer.parseInt(numOfMedia) > Integer.parseInt(maxMedia)) {
      numOfMedia = maxMedia;
    }

    int mediaNum = Integer.parseInt(numOfMedia);
    SimpleList mediaFields = new SimpleList();
    for(int i =0; i<mediaNum;i++){
      Integer mNum = new Integer(i+1);
      mediaFields.add(mNum.toString());
    }
    mergeData.put("medianum",numOfMedia);
    mergeData.put("mediafields",mediaFields);


    SimpleHash extraInfo = new SimpleHash();
    try{
      extraInfo.put("languagePopUpData", DatabaseLanguage.getInstance().getPopupData() );
      extraInfo.put("themenPopupData", themenModule.getTopicsAsSimpleList());

      extraInfo.put("topics", themenModule.getTopicsList());

    }
    catch (Exception e) {
      logger.error("languagePopUpData or getTopicslist failed "+e.toString());
      throw new ServletModuleException("OpenIndy -- failed getting language or topics: "+e.toString());
    }



    deliver(req, res, mergeData, extraInfo, postingFormTemplate);
  }

  /**
   *  Method for inserting an open posting into the Database and delivering
   *  the postingDone Page
   */

  public void insposting(HttpServletRequest req, HttpServletResponse res)
    throws ServletModuleException, ServletModuleUserException
  {
    SimpleHash mergeData = new SimpleHash();
    boolean setMedia=false;
    boolean setTopic = false;

    try {

      WebdbMultipartRequest mp = null;
      EntityList mediaList = null;
      try {
        // new MediaRequest, "1" is the id for the openPosting user
        MediaRequest mediaReq = new MediaRequest("1", true, true);
        mp = new WebdbMultipartRequest(req, (FileHandler)mediaReq);
        mediaList = mediaReq.getEntityList();
      }
      catch (FileHandlerUserException e) {
        throw new ServletModuleUserException(e.getMessage());
      } catch (PropertiesConfigExc e) {
        throw new ServletModuleUserException(e.getMessage());
      }

      HashMap withValues = mp.getParameters();

      //checking the onetimepasswd
      if(passwdProtection.equals("yes")){
        HttpSession session = req.getSession(false);
        String sessionPasswd = (String)session.getAttribute("passwd");
        if ( sessionPasswd == null){
          throw new ServletModuleUserException("Lost password");
        }
        String passwd = (String)withValues.get("passwd");
        if ( passwd == null || (!sessionPasswd.equals(passwd))) {
          throw new ServletModuleUserException("Missing password");
        }
        session.invalidate();
      }

      if ((((String)withValues.get("title")).length() == 0) ||
          (((String)withValues.get("description")).length() == 0) ||
          (((String)withValues.get("content_data")).length() == 0))
        throw new ServletModuleUserException("Missing field");

      // call the routines that escape html

      for (Iterator i=withValues.keySet().iterator(); i.hasNext(); ){
        String k=(String)i.next();
        String v=(String)withValues.get(k);

        if (k.equals("content_data")){
          //this doesn't quite work yet, so for now, all html goes
          //withValues.put(k,StringUtil.approveHTMLTags(v));
          withValues.put(k,StringUtil.deleteForbiddenTags(v));
        } else if (k.equals("description")) {
          String tmp = StringUtil.deleteForbiddenTags(v);
          withValues.put(k,StringUtil.deleteHTMLTableTags(tmp));
        } else {
          withValues.put(k,StringUtil.removeHTMLTags(v));
        }

      }

      withValues.put("date", StringUtil.date2webdbDate(new GregorianCalendar()));
      withValues.put("publish_path", StringUtil.webdbDate2path((String)withValues.get("date")));
      withValues.put("is_produced", "0");
      // by default stuff is published, they can be un-published through the
      // admin interface.
      withValues.put("is_published","1");
      // if op direct article-type == newswire
      if (directOp.equals("yes")) withValues.put("to_article_type","1");

      withValues.put("to_publisher","1");

      // owner is openposting user
      //      ML: this is not multi-language friendly and this can be done in a template
      //      if (withValues.get("creator").toString().equals(""))
      //        withValues.put("creator","Anonym");

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
        deliver(req, res, mergeData, postingFormDupeTemplate);
      }

      String[] to_topicsArr = mp.getParameterValues("to_topic");

      if (to_topicsArr != null && to_topicsArr.length > 0) {
        try{
          DatabaseContentToTopics.getInstance().setTopics(cid,to_topicsArr);
          setTopic = true;
        }
        catch (Exception e) {
          logger.error("setting content_x_topic failed");
          contentModule.deleteById(cid);
          throw new ServletModuleException("smod - openindy :: insposting: setting content_x_topic failed: "+e.toString());
        } //end try
      } //end if

      //if we're here all is ok... associate the media to the article
      for(int i=0;i<mediaList.size();i++) {
        Entity mediaEnt = (Entity)mediaList.elementAt(i);
        DatabaseContentToMedia.getInstance().addMedia(cid,mediaEnt.getId());
      }

      try {
        MirGlobal.localizer().openPostings().afterContentPosting(
                                                                 (EntityContent)contentModule.getById(cid));
      }
      catch (Throwable t) {
        throw new ServletModuleException(t.getMessage());
      }
    }
    catch (FileHandlerException e) {
      e.printStackTrace(System.out);
      throw new ServletModuleException("MediaException: "+ e.getMessage());
    }
    catch (IOException e) { throw new ServletModuleException("IOException: "+ e.getMessage());}
    catch (StorageObjectFailure e) { throw new ServletModuleException("StorageObjectException" + e.getMessage());}
    catch (ModuleException e) { throw new ServletModuleException("ModuleException"+e.getMessage());}

    deliver(req, res, mergeData, postingFormDoneTemplate);
  }

    /*
   * Method for preparing and sending a content as an email message
   */

  public void mail(HttpServletRequest req, HttpServletResponse res)
    throws ServletModuleException, ServletModuleUserException {
    String aid = req.getParameter("mail_aid");
    if (aid == null){
      throw new ServletModuleUserException("An article id must be specified in requests to email an article.  Something therefore went badly wrong....");
    }

    String to = req.getParameter("mail_to");
    String from = req.getParameter("mail_from");
    String from_name = req.getParameter("mail_from_name");
    String comment = req.getParameter("mail_comment");
    String mail_language = req.getParameter("mail_language");

    SimpleHash mergeData = new SimpleHash();

    if (to == null || from == null || from_name == null|| to.equals("") || from.equals("") || from_name.equals("") || mail_language == null || mail_language.equals("")){

      for (Enumeration theParams = req.getParameterNames(); theParams.hasMoreElements() ;) {
        String pName=(String)theParams.nextElement();
        if (pName.startsWith("mail_")){
          mergeData.put(pName,new SimpleScalar(req.getParameter(pName)));
        }
      }
      deliver(req,res,mergeData,prepareMailTemplate);
    }
    else {
      //run checks on to and from and mail_language to make sure no monkey business occurring
      if (mail_language.indexOf('.') != -1 || mail_language.indexOf('/') != -1 ){
        throw new ServletModuleUserException("Sorry, you've entered an illegal character into the language field.  Go back and try again, asshole.");
      }
      if (to.indexOf('\n') != -1
          || to.indexOf('\r') != -1
          || to.indexOf(',') != -1
          || from.indexOf('\n') != -1
          || from.indexOf('\r') != -1
          || from.indexOf(',') != -1 ){
        throw new ServletModuleUserException("Sorry, you've entered an illegal character into the from or to field.  Go back and try again.");
      }
      EntityContent contentEnt;
      try{
        contentEnt = (EntityContent)contentModule.getById(aid);
      }
      catch (ModuleException e){
        throw new ServletModuleUserException("Couldn't get content for article "+aid);
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
        throw new ServletModuleUserException("No text file found in " + txtFilePath);
      }
      catch (IOException e){
        throw new ServletModuleUserException("Problem reading file in " + txtFilePath);
      }
      // add some headers
      content = "To: " + to + "\nReply-To: "+ from + "\n" + content;
      // put in the comment where it should go
      if (comment != null) {
        String commentTextToInsert = "\n\nAttached comment from " + from_name + ":\n" + comment;
        try {
          content=StringRoutines.performRegularExpressionReplacement(content,"!COMMENT!",commentTextToInsert);
        }
        catch (Exception e){
          throw new ServletModuleUserException("Problem doing regular expression replacement " + e.toString());
        }
      }
      else{
        try {
          content=StringRoutines.performRegularExpressionReplacement(content,"!COMMENT!","");
        }
        catch (Exception e){
          throw new ServletModuleUserException("Problem doing regular expression replacement " + e.toString());
        }
      }

      SMTPClient client=new SMTPClient();
      try {
				int reply;
				client.connect(configuration.getString("ServletModule.OpenIndy.SMTPServer"));
				System.out.print(client.getReplyString());
				
				reply = client.getReplyCode();
				
				if(!SMTPReply.isPositiveCompletion(reply)) {
				  client.disconnect();
				  throw new ServletModuleUserException("SMTP server refused connection.");
				}
				
				client.sendSimpleMessage(configuration.getString("ServletModule.OpenIndy.EmailIsFrom"),to,content);
				
				client.disconnect();
				//mission accomplished
				deliver(req,res,mergeData,sentMailTemplate); 
      } catch(IOException e) {
        if(client.isConnected()) {
          try {
            client.disconnect();
          } catch(IOException f) {
            // do nothing
          }
        }
        throw new ServletModuleUserException(e.toString());
      }
    }
  }


  /*
   * Method for querying a lucene index
   */
  public void search(HttpServletRequest req, HttpServletResponse res)
    throws ServletModuleException, ServletModuleUserException {
    try {
      int increment=10;

      HttpSession session = req.getSession(false);

      String queryString="";

      SimpleHash mergeData = new SimpleHash();

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

      //make the query available to subsequent iterations

      for (Enumeration theParams = req.getParameterNames(); theParams.hasMoreElements() ;) {
        String pName=(String)theParams.nextElement();
        if (pName.startsWith("search_")){
          mergeData.put(pName,new SimpleScalar(req.getParameter(pName)));
        }
      }

      try{
        mergeData.put("topics", themenModule.getTopicsAsSimpleList());
      }
      catch(ModuleException e) {
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
                throw new ServletModuleUserException("Problem with Search Index! : "+ e.toString());
              }

              Query query = null;
              try {
                query = QueryParser.parse(queryString, "content", new StandardAnalyzer());
              }
              catch(Exception e) {
                searcher.close();
                logger.debug("Query don't parse: " + queryString);
                throw new ServletModuleUserException("Problem with Query String! (was '"+queryString+"')");
              }

              Hits hits = null;
              try {
                hits = searcher.search(query);
              }
              catch(IOException e) {
                searcher.close();
                logger.debug("Can't get hits: " + e.toString());
                throw new ServletModuleUserException("Problem getting hits!");
              }

              int start = 0;
              int end = hits.length();

              String sortBy=req.getParameter("search_sort");
              if (sortBy == null || sortBy.equals("")){
                throw new ServletModuleUserException("Please let me sort by something!(missing search_sort)");
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
                HashMap dateToPosition = new HashMap(end,1.0F); //we know how big it will be
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
                    throw new ServletModuleUserException("don't know how to sort by: "+ sortBy);
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
                throw new ServletModuleUserException("Problem closing searcher(normal)!");
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
              throw new ServletModuleUserException("Problem closing searcher!");
            }
          }
        }
      }

      try {
        ArrayList theDocs = (ArrayList)session.getAttribute("theDocumentsSorted");
        if (theDocs != null){

          mergeData.put("numberOfHits", ((Integer)session.getAttribute("numberOfHits")).toString());
          SimpleList theHits = new SimpleList();
          int pIR=((Integer)session.getAttribute("positionInResults")).intValue();
          int terminus;
          int numHits=((Integer)session.getAttribute("numberOfHits")).intValue();

          if (!(pIR+increment>=numHits)){
            mergeData.put("hasNext","y");
          }
          if (pIR>0){
            mergeData.put("hasPrevious","y");
          }

          if ((pIR+increment)>numHits){
            terminus=numHits;
          }
          else {
            terminus=pIR+increment;
          }
          for(int i = pIR; i < terminus; i++) {
            SimpleHash h = new SimpleHash();
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
      catch (Exception e) {
        logger.debug("Can't iterate over hits: " + e.toString());
        throw new ServletModuleUserException("Problem getting hits!");
      }

      mergeData.put("queryString",queryString);
      deliver(req,res,mergeData,searchResultsTemplate);
    }
    catch (NullPointerException n){
      n.printStackTrace();
      throw new ServletModuleUserException("Null Pointer"+n.toString());
    }
  }

  /*
   * Method for dynamically generating a pdf from a fo file
   */
  public void getpdf(HttpServletRequest req, HttpServletResponse res)
    throws ServletModuleException, ServletModuleUserException {
    String ID_REQUEST_PARAM = "id";
    String language = req.getParameter("language");
    String generateFO=configuration.getString("GenerateFO");
    String generatePDF=configuration.getString("GeneratePDF");


    //don't do anything if we are not making FO files, or if we are
    //pregenerating PDF's
    if (generateFO.equals("yes") && generatePDF.equals("no")){
      //fop complains unless you do the logging this way
      org.apache.log.Logger log = null;
      Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
      log = hierarchy.getLoggerFor("fop");
      log.setPriority(Priority.WARN);

      String producerStorageRoot=configuration.getString("Producer.StorageRoot");
      String producerDocRoot=configuration.getString("Producer.DocRoot");
      //      String templateDir=MirConfig.getPropWithHome("HTMLTemplateProcessor.Dir");
      String xslSheet=configuration.getString("Producer.HTML2FOStyleSheet");
      try {
        String idParam = req.getParameter(ID_REQUEST_PARAM);
        if (idParam != null) {
          EntityContent contentEnt =
            (EntityContent)contentModule.getById(idParam);
          String publishPath = StringUtil.webdbDate2path(contentEnt.getValue("date"));
          String foFile;

          if (language == null){
            foFile = producerStorageRoot + producerDocRoot + "/"
              + publishPath  + idParam + ".fo";
          }
          else{
            foFile = producerStorageRoot + producerDocRoot + "/"
              + language + publishPath  + idParam + ".fo";
          }
          logger.debug("USING FILES" + foFile + " and " + xslSheet);
          XSLTInputHandler input = new XSLTInputHandler(new File(foFile),
                                                        new File(xslSheet));

          ByteArrayOutputStream out = new ByteArrayOutputStream();
          res.setContentType("application/pdf");

          Driver driver = new Driver();
          driver.setLogger(log);
          driver.setRenderer(Driver.RENDER_PDF);
          driver.setOutputStream(out);
          driver.render(input.getParser(), input.getInputSource());

          byte[] content = out.toByteArray();
          res.setContentLength(content.length);
          res.getOutputStream().write(content);
          res.getOutputStream().flush();
        } else {
          throw new ServletModuleUserException("Missing id parameter.");
        }
      } catch (Exception ex) {
        logger.error(ex.toString());
        throw new ServletModuleException(ex.toString());
      }
    } else {
      throw new ServletModuleUserException("Can't generate a PDF because the config tells me not to.");
    }
  }

  private void _throwBadContentType (String fileName, String contentType)
    throws ServletModuleUserException {

    logger.error("Wrong file type uploaded!: " + fileName+" "
                          +contentType);
    throw new ServletModuleUserException("The file you uploaded is of the "
                                         +"following mime-type: "+contentType
                                         +", we do not support this mime-type. "
                                         +"Error One or more files of unrecognized type. Sorry");
  }

  protected String createOneTimePasswd(){
    Random r = new Random();
    int random = r.nextInt();
    long l = System.currentTimeMillis();
    l = (l*l*l*l)/random;
    if(l<0) l = l * -1;
    String returnString = ""+l;
    return returnString.substring(5);
  }


  /* this is an overwritten method of ServletModule in order
     to use different bundles for open and admin */
  public void deliver(HttpServletRequest req, HttpServletResponse res,
                      TemplateModelRoot rtm, TemplateModelRoot popups,
                      String templateFilename)
    throws ServletModuleException {
    if (rtm == null) rtm = new SimpleHash();
    try {
      PrintWriter out = res.getWriter();
      HTMLTemplateProcessor.process(res, templateFilename, rtm, popups, out,
                                    getLocale(req), "bundles.open");
      out.close();
    }	catch (HTMLParseException e) {
      throw new ServletModuleException(e.toString());
    } catch (IOException e) {
      throw new ServletModuleException(e.toString());
    }
  }
}



