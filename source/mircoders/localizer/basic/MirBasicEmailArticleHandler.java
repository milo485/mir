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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import mir.config.MirPropertiesConfiguration;
import mir.generator.Generator;
import mir.generator.GeneratorHelper;
import mir.log.LoggerWrapper;
import mir.session.Request;
import mir.session.Response;
import mir.session.Session;
import mir.session.SessionExc;
import mir.session.SessionFailure;
import mir.session.SessionHandler;
import mir.session.ValidationHelper;
import mir.session.HTTPAdapters.HTTPRequestAdapter;
import mir.util.StringRoutines;
import mircoders.entity.EntityContent;
import mircoders.global.CacheKey;
import mircoders.global.MirGlobal;
import mircoders.module.ModuleContent;
import mircoders.storage.DatabaseContent;

import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.struts.util.MessageResources;


/**
 *
 * <p>Title: Tenative session handler for emailing postings </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author john
 * @version 1.0
 */


public class MirBasicEmailArticleHandler implements SessionHandler {
  protected LoggerWrapper logger;
  protected MirPropertiesConfiguration configuration;

  public MirBasicEmailArticleHandler() {
    logger = new LoggerWrapper("Localizer.EmailArticle");
    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (Throwable t) {
      logger.fatal("Cannot load configuration: " + t.toString());
      
      throw new RuntimeException("Cannot load configuration: " + t.toString());
    }
  }
  
  public void processRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    if (aSession.getAttribute("initialRequest") == null) {
      aSession.setAttribute("initialRequest", "no");
      initialRequest(aRequest, aSession, aResponse);
    }
    else {
      subsequentRequest(aRequest, aSession, aResponse);
    }
  }
  
  protected void initialRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    initializeSession(aRequest, aSession);
    makeInitialResponse(aRequest, aSession, aResponse);
  }
  
  protected void initializeSession(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    /* do nothing for now */
    
  }
  
  protected void makeInitialResponse(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    /* if you do not supply an aid to this handler, it should return an error page */
    /* if you supply a non-functioning/non-published  aid to this handler, it should return an error page, but at a
       later stage, because we don't check the db until we are potentially populating the cache*/
    /* otherwise you get to address an article and add some comments */
    String articleID = aRequest.getParameter("mail_aid");
    if (articleID == null){
      throw new SessionExc("makeInitialResponse: article id not set!");
    }
    else {
      aSession.setAttribute("email.aid",articleID);
      aResponse.setResponseValue("errors", null);
      
      String mail_language = configuration.getString("Mir.Login.DefaultLanguage", "en");	
      aResponse.setResponseValue("mail_language",mail_language);
      aResponse.setResponseValue("mail_to","");
      aResponse.setResponseValue("mail_from","");
      aResponse.setResponseValue("mail_from_name","");
      aResponse.setResponseValue("mail_comment","");
      
      aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.email.PrepareTemplate"));

      
    }
  }
  
  protected boolean shouldSendMail(Request aRequest, Session aSession, Response aResponse,List aValidationErrors) throws SessionExc, SessionFailure{
    if (validate(aRequest,aSession,aResponse,aValidationErrors)){
      String to=aRequest.getParameter("mail_to");
      if (to.indexOf('@') == -1
	  || to.indexOf('\n') != -1
          || to.indexOf('\r') != -1
          || to.indexOf(',') != -1) {
	throw new SessionExc("Invalid to address"); // we might want to see this in a log, so it is not a validation error 
      }
      else return true; // go for it
    }
    else{
      return false; //validation failed, but not in a potentially abusive way
    }
    
  }
  
    



  protected boolean validate(Request aRequest, Session aSession, Response aResponse,List aValidationErrors) throws SessionExc, SessionFailure{
    
    if (ValidationHelper.testFieldEntered(aRequest, "mail_to", "validationerror.missing", aValidationErrors))
      aResponse.setResponseValue("mail_to",aRequest.getParameter("mail_to"));
    if (ValidationHelper.testFieldEntered(aRequest, "mail_from", "validationerror.missing", aValidationErrors))
      aResponse.setResponseValue("mail_from",aRequest.getParameter("mail_from"));
    if (ValidationHelper.testFieldEntered(aRequest, "mail_from_name", "validationerror.missing", aValidationErrors))
      aResponse.setResponseValue("mail_from_name",aRequest.getParameter("mail_from_name"));
    if (ValidationHelper.testFieldEntered(aRequest, "mail_language", "validationerror.missing", aValidationErrors))
      aResponse.setResponseValue("mail_language",aRequest.getParameter("mail_language"));

    return (aValidationErrors==null || aValidationErrors.size() == 0);
  }

  protected String getEmailText(String aid,String language) throws SessionExc{
    String theText;
    CacheKey theCacheKey=new CacheKey("email",aid+language);

    if (MirGlobal.mruCache().hasObject(theCacheKey)){
      logger.info("fetching email text for article "+aid+" from cache");
      theText = (String) MirGlobal.mruCache().getObject(theCacheKey);
    }
    else {
      try {
	ModuleContent contentModule = new ModuleContent(DatabaseContent.getInstance());
	EntityContent contentEnt = (EntityContent) contentModule.getById(aid);
	
	Map articleData = new HashMap();
	articleData.put("article", MirGlobal.localizer().dataModel().adapterModel().makeEntityAdapter("content", contentEnt));
	articleData.put("languagecode", language);
	Map responseData = GeneratorHelper.makeBasicGenerationData(new Locale[] {new Locale(language,""),new Locale(configuration.getString("Mir.Admin.FallbackLanguage", "en"), "")},"bundles.open","bundles.open");
	responseData.put("data",articleData);
	
	String emailAnArticleTemplate = configuration.getString("Localizer.OpenSession.email.MailTemplate");
	
	Generator generator = MirGlobal.localizer().generators().makeOpenPostingGeneratorLibrary().makeGenerator(emailAnArticleTemplate);
	
	StringWriter theEmailStringWriter = new StringWriter();
	PrintWriter theEmailPrintWriter = new PrintWriter(theEmailStringWriter);
	generator.generate(theEmailPrintWriter, responseData, logger);
	
	theEmailStringWriter.close();

	theText = theEmailStringWriter.toString();
	MirGlobal.mruCache().storeObject(theCacheKey, theText);
      }
      catch (Throwable e) {
	throw new SessionExc("Couldn't get content for article " + aid + language + ": " + e.getMessage());
      }
    }

    return theText;
  }

  protected String getExtraEmailHeaders(Request aRequest,String to,String from) throws SessionExc {
    
    String headers = "To: " + to + "\nReply-To: "+ from+"\n";;
    if (configuration.getString("Localizer.OpenSession.email.includeSenderIP","no").equals("yes"))
      headers= headers+"X-Originating-IP: "+ ((HTTPRequestAdapter)aRequest).getRequest().getRemoteAddr() + "\n";
    
    return headers;
  }

  protected String interpolateComment(String emailText,String comment,String from_name,String language) throws SessionExc{
      if (comment != null) {
	MessageResources messages = MessageResources.getMessageResources("bundles.open");
	String commentTextToInsert=messages.getMessage(new Locale(language,""),"email.comment.intro", from_name)+"\n";
	
	try {
	  emailText=StringRoutines.performRegularExpressionReplacement(emailText,"!COMMENT!",commentTextToInsert);
	}
	catch (Throwable e){
	  throw new SessionExc("Problem doing regular expression replacement :" + e.toString());
	}
      }
      else{
	try {
	emailText=StringRoutines.performRegularExpressionReplacement(emailText,"!COMMENT!","");
	}
	catch (Throwable e){
	  throw new SessionExc("Problem doing regular expression replacement " + e.toString());
	}
      }
      return emailText;
  }
  
  protected boolean doTheSMTP(String aMessage,String aTo,String aFrom) throws SessionExc{
   SMTPClient client=new SMTPClient();
   try {
     int reply;
     client.connect(configuration.getString("ServletModule.OpenIndy.SMTPServer"));
     reply = client.getReplyCode();
     
     if (!SMTPReply.isPositiveCompletion(reply)) {
       client.disconnect();
	throw new SessionExc("SMTP server refused connection.");
     }
     boolean trueIfItWorked = client.sendSimpleMessage(configuration.getString("ServletModule.OpenIndy.EmailIsFrom"), aTo, aMessage);
     client.disconnect();
     // mission accomplished??
     if (! trueIfItWorked)
       throw new SessionExc(client.getReplyString());
     else
       return trueIfItWorked;
   }
   catch(IOException e) {
      if(client.isConnected()) {
	try {
	  client.disconnect();
	} catch(IOException f) {
	  // do nothing
	}
      }
      throw new SessionExc(e.getMessage());
   } 
  }
  
  protected boolean sendMail(Request aRequest,Session aSession,Response aResponse) throws SessionExc,SessionFailure {
    String aid=aRequest.getParameter("mail_aid");
    String to=aRequest.getParameter("mail_to");
    String from=aRequest.getParameter("mail_from");
    String from_name=aRequest.getParameter("mail_from_name");
    String language=aRequest.getParameter("mail_language");
    String comment=aRequest.getParameter("mail_comment");
    
    String theEmailText=getEmailText((String) aSession.getAttribute("email.aid"),language);
    String headers=getExtraEmailHeaders(aRequest,to,from);
    theEmailText=interpolateComment(theEmailText,comment,from_name,language);
    String message=headers+"\n"+theEmailText;

    return doTheSMTP(message,to,from);
       
  }


  protected void subsequentRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    List validationErrors = new Vector();
    if (shouldSendMail(aRequest,aSession,aResponse,validationErrors)){
      
      sendMail(aRequest,aSession,aResponse);
      aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.email.DoneTemplate"));
    }
    else {
      aResponse.setResponseValue("mail_comment",aRequest.getParameter("mail_comment"));  //everything else is required 
      aResponse.setResponseValue("errors",validationErrors);
      aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.email.PrepareTemplate"));
    }
    
  }
  
      /*
      String mail_language = aRequest.getParameter("mail_language");
      if (mail_language == null)
	mail_language = configuration.getString("Mir.Login.DefaultLanguage", "en");	
       
      aResponse.setResponseValue("mail_language",mail_language);
      */




}
