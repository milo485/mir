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

import org.apache.log.*;

import mir.servlet.*;
import mir.module.*;
import mir.misc.*;
import mir.entity.*;
import mir.storage.*;
import mir.media.*;

import mircoders.entity.*;
import mircoders.storage.*;
import mircoders.module.*;
import mircoders.producer.*;
import mircoders.media.MediaRequest;

/*
 *  ServletModuleOpenIndy -
 *   is the open-access-servlet, which is responsible for
 *    adding comments to articles &
 *    open-postings to the newswire
 *
 * @author $Author: mh $
 * @version $Revision: 1.38.2.6 $ $Date: 2002/12/13 05:50:52 $
 *
 */

public class ServletModuleOpenIndy extends ServletModule
{

  private String        commentFormTemplate, commentFormDoneTemplate,
                        commentFormDupeTemplate;
  private String        postingFormTemplate, postingFormDoneTemplate,
                        postingFormDupeTemplate;
  private ModuleContent contentModule;
  private ModuleImages  imageModule;
  private ModuleTopics  themenModule;
  private String        directOp ="yes";
  private String        passwdProtection ="yes";
  // Singelton / Kontruktor
  private static ServletModuleOpenIndy instance = new ServletModuleOpenIndy();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleOpenIndy() {
    try {
      theLog = Logfile.getInstance(MirConfig.getProp("Home") + MirConfig.getProp("ServletModule.OpenIndy.Logfile"));
      commentFormTemplate = MirConfig.getProp("ServletModule.OpenIndy.CommentTemplate");
      commentFormDoneTemplate = MirConfig.getProp("ServletModule.OpenIndy.CommentDoneTemplate");
      commentFormDupeTemplate = MirConfig.getProp("ServletModule.OpenIndy.CommentDupeTemplate");
      postingFormTemplate = MirConfig.getProp("ServletModule.OpenIndy.PostingTemplate");
      postingFormDoneTemplate = MirConfig.getProp("ServletModule.OpenIndy.PostingDoneTemplate");
      postingFormDupeTemplate = MirConfig.getProp("ServletModule.OpenIndy.PostingDupeTemplate");
      directOp = MirConfig.getProp("DirectOpenposting").toLowerCase();
			passwdProtection = MirConfig.getProp("PasswdProtection").toLowerCase();
      mainModule = new ModuleComment(DatabaseComment.getInstance());
      contentModule = new ModuleContent(DatabaseContent.getInstance());
      themenModule = new ModuleTopics(DatabaseTopics.getInstance());
      imageModule = new ModuleImages(DatabaseImages.getInstance());
      defaultAction="addposting";
			
    }
    catch (StorageObjectException e) {
        theLog.printError("servletmoduleopenindy could not be initialized");
    }
  }


  /**
   *  Method for making a comment
   */

  public void addcomment(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    String aid = req.getParameter("aid"); // the article id the comment will belong to
    if (aid!=null && !aid.equals(""))
    {
			SimpleHash mergeData = new SimpleHash();

			// onetimepasswd
			if(passwdProtection.equals("yes")){
				String passwd = this.createOneTimePasswd();
				HttpSession session = req.getSession(false);
				session.setAttribute("passwd",passwd);
				mergeData.put("passwd", passwd);
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
        theLog.printDebugInfo("id: "+id);
        //insert was not successfull
        if(id==null){
          deliver(req, res, new SimpleHash(), commentFormDupeTemplate);
        }
        
        // producing new page
        new ProducerContent().handle(null, null, true, false, aid);

        // sync the server
        int exitValue = Helper.rsync();
        theLog.printDebugInfo("rsync:"+exitValue);

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
    } catch (Exception e) {
      theLog.printError("languagePopUpData or getTopicslist failed "
                        +e.toString());
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
      } catch (FileHandlerUserException e) {
        throw new ServletModuleUserException(e.getMsg());
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
          //this doesn't quite work yet, so for now, just delete the really
          //bad ones.
          //withValues.put(k,StringUtil.approveHTMLTags(v));
          withValues.put(k,StringUtil.deleteForbiddenTags(v));
        } else if (k.equals("description")) {
          String tmp = StringUtil.deleteForbiddenTags(v);
          withValues.put(k,StringUtil.deleteHTMLTableTags(tmp));
        } else {
          //we don't want people fucking with the author/title, etc..
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
      
      // owner is openposting user
      withValues.put("to_publisher","1");
      if (withValues.get("creator").toString().equals(""))
        withValues.put("creator","Anonym");

      // inserting  content into database
      String cid = contentModule.add(withValues);
      theLog.printDebugInfo("id: "+cid);
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
        } catch (Exception e) {
          theLog.printError("setting content_x_topic failed");
          contentModule.deleteById(cid);
          throw new ServletModuleException("smod - openindy :: insposting: setting content_x_topic failed: "+e.toString());
        } //end try
      } //end if
       
      //if we're here all is ok... associate the media to the article
      for(int i=0;i<mediaList.size();i++) {
        Entity mediaEnt = (Entity)mediaList.elementAt(i);
        DatabaseContentToMedia.getInstance().addMedia(cid,mediaEnt.getId());
      }

      // producing openpostinglist
      new ProducerOpenPosting().handle(null,null,false,false);
      // producing new page
      new ProducerContent().handle(null, null, false, false,cid);
      //if direct op producing startpage
      if (directOp.equals("yes")) new ProducerStartPage().handle(null,null);
      
			//produce the topicPages if set
			//should be more intelligent
			//if(setTopic==true) new ProducerTopics().handle(null,null);
			
      // sync the server
      //should be configureable
      int exitValue = Helper.rsync();
      theLog.printDebugInfo("rsync: "+exitValue);

    }
    catch (FileHandlerException e) { throw new ServletModuleException("MediaException: "+ e.toString());}
    catch (IOException e) { throw new ServletModuleException("IOException: "+ e.toString());}
    catch (StorageObjectException e) { throw new ServletModuleException("StorageObjectException" + e.toString());}
    catch (ModuleException e) { throw new ServletModuleException("ModuleException"+e.toString());}

    deliver(req, res, mergeData, postingFormDoneTemplate);
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
      Logger log = null;
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

    theLog.printDebugInfo("Wrong file type uploaded!: " + fileName+" "
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
      
}



