package mircoders.localizer.basic;

import java.util.*;
import mir.log.LoggerWrapper;
import mir.session.*;
import mir.config.*;
import mir.util.*;
import mir.entity.*;
import mircoders.storage.*;
import mircoders.global.*;
import mircoders.localizer.*;
import mircoders.entity.*;
import mircoders.module.*;
import mircoders.media.*;

/**
 *
 * <p>Title: Experimental session handler for comment postings </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MirBasicCommentPostingSessionHandler implements SessionHandler {
  protected LoggerWrapper logger;
  protected MirPropertiesConfiguration configuration;
  protected boolean initialRequest;
  protected ModuleComment commentModule;
  protected DatabaseCommentToMedia commentToMedia = DatabaseCommentToMedia.getInstance();

  public MirBasicCommentPostingSessionHandler() {
    logger = new LoggerWrapper("Localizer.OpenPosting.Comment");
    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (Throwable t) {
      logger.fatal("Cannont load configuration: " + t.toString());

      throw new RuntimeException("Cannont load configuration: " + t.toString());
    }
    initialRequest= true;
    commentModule= new ModuleComment(DatabaseComment.getInstance());
  }

  public void processRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    if (aSession.getAttribute("initialRequest")==null) {
      initialRequest(aRequest, aSession, aResponse);
      aSession.setAttribute("initialRequest", "no");
    }
    else {
      subsequentRequest(aRequest, aSession, aResponse);
    }
  };

  public String generateOnetimePassword() {
    Random r = new Random();
    int random = r.nextInt();

    long l = System.currentTimeMillis();

    l = (l*l*l*l)/random;
    if (l<0)
      l = l * -1;

    String returnString = ""+l;

    return returnString.substring(5);
  }

  public void initializeResponseData(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    if (MirGlobal.abuse().getOpenPostingPassword()) {
      String password = (String) aSession.getAttribute("password");
      if (password==null) {
        password = generateOnetimePassword();
        aSession.setAttribute("password", password);
      }
      aResponse.setResponseValue("password", password);
    }
    else {
      aResponse.setResponseValue("password", null);
      aSession.deleteAttribute("password");
    }

    aResponse.setResponseValue("errors", null);
  };

  public void initialRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure{
    Iterator i = DatabaseContent.getInstance().getFields().iterator();
    while (i.hasNext()) {
      aResponse.setResponseValue( (String) i.next(), null);
    }

    String articleId = aRequest.getParameter("to_media");

    if (articleId == null)
      throw new SessionExc("MirBasicCommentPostingSessionHandler.initialRequest: article id not set!");

    aSession.setAttribute("to_media", articleId);

    initializeResponseData(aRequest, aSession, aResponse);

    try {
      aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.EditTemplate"));
    }
    catch (Throwable e) {
      throw new SessionFailure("Can't get configuration: " + e.getMessage(), e);
    }

  }

  public boolean testFieldExists(Request aRequest, String aFieldName, String anErrorMessageResource, List aValidationResults) {
    Object value = aRequest.getParameter(aFieldName);
    if (value==null || !(value instanceof String) || ((String) value).trim().length()==0) {
      logger.debug("  missing field " + aFieldName + " value = " + value);
      aValidationResults.add(new ValidationError(aFieldName, anErrorMessageResource));
      return false;
    }
    else
      return true;
  }

  public boolean testFieldIsNumeric(Request aRequest, String aFieldName, String anErrorMessageResource, List aValidationResults) {
    Object value = aRequest.getParameter(aFieldName);
    if (value!=null) {
      try {
        Integer.parseInt((String) value);
        return true;
      }
      catch (Throwable t) {
        logger.debug("  field not numeric: " + aFieldName + " value = " + value);
        aValidationResults.add(new ValidationError(aFieldName, anErrorMessageResource));
        return false;
      }
    }
    return true;
  }


  public List validate(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    List result = new Vector();

    testFieldExists(aRequest, "title", "validationerror.missing", result);
    testFieldExists(aRequest, "description", "validationerror.missing", result);
    testFieldExists(aRequest, "creator", "validationerror.missing", result);

    return result;
  }

  public void subsequentRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    try {
      Map commentFields = new HashMap();

      Iterator i = DatabaseContent.getInstance().getFields().iterator();
      while (i.hasNext()) {
        String field = (String) i.next();
        aResponse.setResponseValue(field, aRequest.getParameter(field));
        if (aRequest.getParameter(field)!=null) {
          commentFields.put(field, aRequest.getParameter(field));
        }
      }

      initializeResponseData(aRequest, aSession, aResponse);

      List validationErrors = validate(aRequest, aSession);

      if (validationErrors != null && validationErrors.size()>0) {
        returnValidationErrors(aRequest, aSession, aResponse, validationErrors);
      }
      else {
        EntityComment comment = (EntityComment) commentModule.createNew ();
        comment.setValues(commentFields);

        finishComment(aRequest, aSession, comment);

        String id = comment.insert();
        if(id==null){
          afterDuplicateCommentPosting(aRequest, aSession, aResponse, comment);
          logger.info("Dupe comment rejected");
          aSession.terminate();
        }
        else {
          i = aRequest.getUploadedFiles().iterator();
          while (i.hasNext()) {
            UploadedFile file = (UploadedFile) i.next();
            processMediaFile(aRequest, aSession, comment, file);
          }

          afterCommentPosting(aRequest, aSession, aResponse, comment);
          MirGlobal.abuse().checkComment(comment, aRequest, null);
          MirGlobal.localizer().openPostings().afterCommentPosting(comment);
          logger.info("Comment posted");
          aSession.terminate();
        }
      }
    }
    catch (Throwable t) {
      ExceptionFunctions.traceCauseException(t).printStackTrace();

      throw new SessionFailure("MirBasicCommentPostingSessionHandler.subsequentRequest: " + t.getMessage(), t);
    }
  }

  public void initializeCommentPosting(Request aRequest, Session aSession, Response aResponse) throws SessionFailure, SessionExc {
    String articleId = aRequest.getParameter("to_media");
    if (articleId==null)
      articleId = aRequest.getParameter("aid");

    if (articleId==null)
      throw new SessionExc("initializeCommentPosting: article id not set!");

    aSession.setAttribute("to_media", articleId);
    processCommentPosting(aRequest, aSession, aResponse);
  };

  public void returnValidationErrors(Request aRequest, Session aSession, Response aResponse, List aValidationErrors) throws SessionFailure, SessionExc {
    aResponse.setResponseValue("errors", aValidationErrors);
    aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.EditTemplate"));
  };

  public void processCommentPosting(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    if (MirGlobal.abuse().getOpenPostingPassword()) {
      String password = generateOnetimePassword();
      aSession.setAttribute("password", password);
      aResponse.setResponseValue("password", password);
      aResponse.setResponseValue("passwd", password);
    }
    else {
      aResponse.setResponseValue("password", null);
    }

    aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.EditTemplate"));
  };

  public void processMediaFile(Request aRequest, Session aSession, EntityComment aComment, UploadedFile aFile) throws SessionExc, SessionFailure {
    try {
      Entity mediaItem = MediaUploadProcessor.processMediaUpload(aFile);
      finishMedia(aRequest, aSession, aFile, mediaItem);
      mediaItem.update();
      commentToMedia.addMedia(aComment.getId(), mediaItem.getId());
    }
    catch (Throwable t) {
      throw new SessionFailure(t);
    }
  }

  public void finishMedia(Request aRequest, Session aSession, UploadedFile aFile, Entity aMedia) throws SessionExc, SessionFailure {
  }

  public void finishComment(Request aRequest, Session aSession, EntityComment aComment) throws SessionExc, SessionFailure {
    if (aSession.getAttribute("to_media") == null)
      throw new SessionExc("missing to_media");

    aComment.setValueForProperty("is_published", "1");
    aComment.setValueForProperty("to_comment_status", "1");
    aComment.setValueForProperty("is_html","0");
    aComment.setValueForProperty("to_media", (String) aSession.getAttribute("to_media"));
  };

  public void addMedia(Request aRequest, Session aSession, EntityComment aComment)  throws SessionExc, SessionFailure {
  }

  public void afterCommentPosting(Request aRequest, Session aSession, Response aResponse, EntityComment aComment) {
    DatabaseContent.getInstance().setUnproduced("id=" + aComment.getValue("to_media"));
    aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.DoneTemplate"));
  };

  public void afterDuplicateCommentPosting(Request aRequest, Session aSession, Response aResponse, EntityComment aComment) {
    aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.comment.DupeTemplate"));
  };

  public class ValidationError {
    private String field;
    private String message;
    private List parameters;

    public ValidationError(String aField, String aMessage) {
      this (aField, aMessage, new String[] {});
    }

    public ValidationError(String aField, String aMessage, Object aParameter) {
      this (aField, aMessage, new Object[] {aParameter});
    }

    public ValidationError(String aField, String aMessage, Object[] aParameters) {
      field = aField;
      message = aMessage;
      parameters = Arrays.asList(aParameters);
    }

    public String getMessage() {
      return message;
    }

    public String getField() {
      return field;
    }

    public List getParameters() {
      return parameters;
    }
  }



}