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

import java.io.*;
import java.lang.*;
import java.sql.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import javax.servlet.*;
import javax.servlet.http.*;

import freemarker.template.*;
import com.oreilly.servlet.multipart.*;
import com.oreilly.servlet.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;
import org.apache.fop.apps.XSLTInputHandler;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.queryParser.*;

import org.apache.log.*;

import mir.servlet.*;
import mir.module.*;
import mir.misc.*;
import mir.entity.*;
import mir.storage.*;
import mir.media.*;
import mir.log.*;

import mircoders.entity.*;
import mircoders.storage.*;
import mircoders.module.*;
import mircoders.producer.*;
import mircoders.media.MediaRequest;
import mircoders.global.*;
import mircoders.localizer.*;
import mircoders.search.*;

/*
 *  ServletModuleOpenIndy -
 *   is the open-access-servlet, which is responsible for
 *    adding comments to articles &
 *    open-postings to the newswire
 *
 * @author mir-coders group
 * @version $Id: ServletModuleOpenIndy.java,v 1.49 2002/12/01 15:05:51 zapata Exp $
 *
 */

public class ServletModuleOpenIndy extends ServletModule
{

  private String        commentFormTemplate, commentFormDoneTemplate,
    commentFormDupeTemplate;
  private String        postingFormTemplate, postingFormDoneTemplate,
    postingFormDupeTemplate;
  private String        searchResultsTemplate;
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
    try {
      logger = new LoggerWrapper("ServletModule.OpenIndy");

      commentFormTemplate = MirConfig.getProp("ServletModule.OpenIndy.CommentTemplate");
      commentFormDoneTemplate = MirConfig.getProp("ServletModule.OpenIndy.CommentDoneTemplate");
      commentFormDupeTemplate = MirConfig.getProp("ServletModule.OpenIndy.CommentDupeTemplate");
      postingFormTemplate = MirConfig.getProp("ServletModule.OpenIndy.PostingTemplate");
      postingFormDoneTemplate = MirConfig.getProp("ServletModule.OpenIndy.PostingDoneTemplate");
      postingFormDupeTemplate = MirConfig.getProp("ServletModule.OpenIndy.PostingDupeTemplate");
      searchResultsTemplate = MirConfig.getProp("ServletModule.OpenIndy.SearchResultsTemplate");
      directOp = MirConfig.getProp("DirectOpenposting").toLowerCase();
      passwdProtection = MirConfig.getProp("PasswdProtection").toLowerCase();
      mainModule = new ModuleComment(DatabaseComment.getInstance());
      contentModule = new ModuleContent(DatabaseContent.getInstance());
      themenModule = new ModuleTopics(DatabaseTopics.getInstance());
      imageModule = new ModuleImages(DatabaseImages.getInstance());
      defaultAction="addposting";

    }
    catch (StorageObjectException e) {
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

    if (aid!=null && !aid.equals(""))
      {
        SimpleHash mergeData = new SimpleHash();

        // onetimepasswd
        if(passwdProtection.equals("yes")){
          String passwd = this.createOneTimePasswd();
          System.out.println(passwd);
          HttpSession session = req.getSession(false);
          session.setAttribute("passwd",passwd);
          mergeData.put("passwd", passwd);
        }

        if (language!=null) {
          HttpSession session = req.getSession(false);
          session.setAttribute("Locale", new Locale(language, ""));
          session.setAttribute("passwd",language);
        }

        mergeData.put("aid", aid);
        deliver(req, res, mergeData, commentFormTemplate);
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
        catch (StorageObjectException e) { throw new ServletModuleException(e.toString());}
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

    String maxMedia = MirConfig.getProp("ServletModule.OpenIndy.MaxMediaUploadItems");
    String numOfMedia = req.getParameter("medianum");
    if(numOfMedia==null||numOfMedia.equals("")){
      numOfMedia="1";
    } else if(Integer.parseInt(numOfMedia) > Integer.parseInt(maxMedia)) {
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
      SimpleList popUpData = DatabaseLanguage.getInstance().getPopupData();
      extraInfo.put("languagePopUpData", popUpData );
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
          //withValues.put(k,StringUtil.removeHTMLTags(v));
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
    catch (FileHandlerException e) { throw new ServletModuleException("MediaException: "+ e.getMessage());}
    catch (IOException e) { throw new ServletModuleException("IOException: "+ e.getMessage());}
    catch (StorageObjectException e) { throw new ServletModuleException("StorageObjectException" + e.getMessage());}
    catch (ModuleException e) { throw new ServletModuleException("ModuleException"+e.getMessage());}

    deliver(req, res, mergeData, postingFormDoneTemplate);
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

      String searchSubmitValue = req.getParameter("search_submit");

      if (searchSubmitValue != null && searchSubmitValue.equals("Back")){
        int totalHits = ((Integer) session.getAttribute("numberOfHits")).intValue();
        int newPosition=((Integer)session.getAttribute("positionInResults")).intValue()-increment;
        if (newPosition < 0 || newPosition >= totalHits){
          throw new ServletModuleUserException("newPosition: index out bounds, value was:"+(new Integer(newPosition)).toString());
        }
        session.setAttribute("positionInResults",new Integer(newPosition));

      }
      else {
        if (searchSubmitValue != null && searchSubmitValue.equals("Forward")){
          int totalHits = ((Integer) session.getAttribute("numberOfHits")).intValue();
          int newPosition=((Integer)session.getAttribute("positionInResults")).intValue()+increment;
          if (newPosition < 0 || newPosition >= totalHits){
            throw new ServletModuleUserException("newPosition: index out bounds, value was:"+(new Integer(newPosition)).toString());
          }
          session.setAttribute("positionInResults",new Integer(newPosition));

        }
        else {
          String indexPath=MirConfig.getProp("IndexPath");


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
          if (pIR-increment>=0){
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

    String generateFO=MirConfig.getProp("GenerateFO");
    String generatePDF=MirConfig.getProp("GeneratePDF");

    //don't do anything if we are not making FO files, or if we are
    //pregenerating PDF's
    if (generateFO.equals("yes") && generatePDF.equals("no")){
      //fop complains unless you do the logging this way
      org.apache.log.Logger log = null;
      Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
      log = hierarchy.getLoggerFor("fop");
      log.setPriority(Priority.WARN);

      String producerStorageRoot=MirConfig.getProp("Producer.StorageRoot");
      String producerDocRoot=MirConfig.getProp("Producer.DocRoot");
      String templateDir=MirConfig.getPropWithHome("HTMLTemplateProcessor.Dir");
      String xslSheet=templateDir + "/"
        + MirConfig.getProp("Producer.PrintableContent.html2foStyleSheetName");
      try {
        String idParam = req.getParameter(ID_REQUEST_PARAM);
        if (idParam != null) {
          EntityContent contentEnt =
            (EntityContent)contentModule.getById(idParam);
          String publishPath = contentEnt.getValue("publish_path");
          String foFile = producerStorageRoot + producerDocRoot + "/"
            + publishPath + "/" + idParam + ".fo";
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



