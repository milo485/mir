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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import mir.config.MirPropertiesConfiguration;
import mir.log.LoggerWrapper;
import mir.session.Request;
import mir.session.Response;
import mir.session.Session;
import mir.session.SessionExc;
import mir.session.SessionFailure;
import mir.session.SessionHandler;
import mir.session.UploadedFile;
import mir.storage.StorageObject;
import mir.util.ExceptionFunctions;
import mircoders.global.MirGlobal;
import mircoders.module.ModuleMediaType;

/**
 *
 * <p>Title: Experimental session handler for comment postings </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public abstract class MirBasicPostingSessionHandler implements SessionHandler {
  protected LoggerWrapper logger;
  protected MirPropertiesConfiguration configuration;

  private String normalResponseGenerator;
  private String dupeResponseGenerator;
  private String unsupportedMediaTypeResponseGenerator;
  private String finalResponseGenerator;


  public MirBasicPostingSessionHandler() {
    logger = new LoggerWrapper("Localizer.OpenPosting");
    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (Throwable t) {
      logger.fatal("Cannot load configuration: " + t.toString());

      throw new RuntimeException("Cannot load configuration: " + t.toString());
    }
  }

  protected void setNormalResponseGenerator(String aGenerator) {
    normalResponseGenerator = aGenerator;
  }

  protected void setResponseGenerators(String aNormalResponseGenerator, String aDupeResponseGenerator,
        String anUnsupportedMediaTypeResponseGenerator, String aFinalResponseGenerator) {
    setNormalResponseGenerator(aNormalResponseGenerator);
    dupeResponseGenerator = aDupeResponseGenerator;
    unsupportedMediaTypeResponseGenerator = anUnsupportedMediaTypeResponseGenerator;
    finalResponseGenerator = aFinalResponseGenerator;
  }

  public void processRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    if (MirGlobal.abuse().getOpenPostingDisabled()) {
      makeOpenPostingDisabledResponse(aRequest, aSession, aResponse);
      aSession.terminate();
    }
    else {
      if (aSession.getAttribute("initialRequest") == null) {
        initialRequest(aRequest, aSession, aResponse);
        aSession.setAttribute("initialRequest", "no");
      }
      else {
        subsequentRequest(aRequest, aSession, aResponse);
      }
    }
  };

  protected void initialRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    initializeSession(aRequest, aSession);
    initializeResponseData(aRequest, aSession, aResponse);
    makeInitialResponse(aRequest, aSession, aResponse);
  }

  public void subsequentRequest(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    try {

      try {
        List validationErrors = new Vector();

        if (!shouldProcessRequest(aRequest, aSession, validationErrors)) {
          initializeResponseData(aRequest, aSession, aResponse);
          makeResponse(aRequest, aSession, aResponse, validationErrors);
        }
        else {
          preProcessRequest(aRequest, aSession);
          Iterator i = aRequest.getUploadedFiles().iterator();
          while (i.hasNext()) {
            processUploadedFile(aRequest, aSession, (UploadedFile) i.next());
          }
          postProcessRequest(aRequest, aSession);
          initializeResponseData(aRequest, aSession, aResponse);
          makeFinalResponse(aRequest, aSession, aResponse);
          aSession.terminate();
        }
      }
      catch (Throwable t) {
        initializeResponseData(aRequest, aSession, aResponse);
        makeErrorResponse(aRequest, aSession, aResponse, t);
        aSession.terminate();
      }
    }
    catch (Throwable t) {
      aSession.terminate();

      throw new SessionFailure(t);
    }
  }

  protected void initializeSession(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    if (MirGlobal.abuse().getOpenPostingPassword()) {
      String password = (String) aSession.getAttribute("password");
      if (password==null) {
        password = generateOnetimePassword();
        aSession.setAttribute("password", password);
      }
    }
    else {
      aSession.deleteAttribute("password");
    }

    logger.debug("referrer = " + aRequest.getHeader("Referer"));

    aSession.setAttribute("referer", aRequest.getHeader("Referer"));
    aSession.setAttribute("nrmediaitems",
        new Integer(configuration.getInt("ServletModule.OpenIndy.DefaultMediaUploadItems")));
  }

  protected void initializeResponseData(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    int nrMediaItems = ((Integer) aSession.getAttribute("nrmediaitems")).intValue();
    List mediaItems = new Vector();
    int i=0;

    while (i<nrMediaItems) {
      i++;
      mediaItems.add(new Integer(i));
    }

    aResponse.setResponseValue("nrmediaitems", new Integer(nrMediaItems));
    aResponse.setResponseValue("mediaitems", mediaItems);
    aResponse.setResponseValue("password", aSession.getAttribute("password"));
    aResponse.setResponseValue("referer", aSession.getAttribute("referer"));
    aResponse.setResponseValue("errors", null);
  }

  protected void makeInitialResponse(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    aResponse.setResponseGenerator(normalResponseGenerator);
  };

  protected void makeResponse(Request aRequest, Session aSession, Response aResponse, List anErrors) throws SessionExc, SessionFailure {
    aResponse.setResponseValue("errors", anErrors);
    aResponse.setResponseGenerator(normalResponseGenerator);
  };

  protected void makeFinalResponse(Request aRequest, Session aSession, Response aResponse) throws SessionExc, SessionFailure {
    aResponse.setResponseGenerator(finalResponseGenerator);
  };

  protected void makeErrorResponse(Request aRequest, Session aSession, Response aResponse, Throwable anError) throws SessionExc, SessionFailure {
    anError.printStackTrace();
    Throwable rootCause = ExceptionFunctions.traceCauseException(anError);

    if (rootCause instanceof DuplicatePostingExc)
      aResponse.setResponseGenerator(dupeResponseGenerator);
    if (rootCause instanceof ModuleMediaType.UnsupportedMimeTypeExc) {
      aResponse.setResponseValue("mimetype", ((ModuleMediaType.UnsupportedMimeTypeExc) rootCause).getMimeType());
      aResponse.setResponseGenerator(unsupportedMediaTypeResponseGenerator);
    }
    else {
      aResponse.setResponseValue("errorstring", anError.getMessage());
      aResponse.setResponseGenerator(configuration.getString("Localizer.OpenSession.ErrorTemplate"));
    }
  };

  protected void makeOpenPostingDisabledResponse(Request aRequest, Session aSession, Response aResponse) {
    aResponse.setResponseGenerator(configuration.getString("ServletModule.OpenIndy.PostingDisabledTemplate"));
  }

  protected void preProcessRequest(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
  };
  public void processUploadedFile(Request aRequest, Session aSession, UploadedFile aFile) throws SessionExc, SessionFailure {
  };
  protected void postProcessRequest(Request aRequest, Session aSession) throws SessionExc, SessionFailure {
  };

  protected boolean shouldProcessRequest(Request aRequest, Session aSession, List aValidationErrors) throws SessionExc, SessionFailure {
    int nrMediaItems = ((Integer) aSession.getAttribute("nrmediaitems")).intValue();
    try {
      nrMediaItems = Math.min(configuration.getInt("ServletModule.OpenIndy.MaxMediaUploadItems"), Integer.parseInt(aRequest.getParameter("nrmediaitems")));
    }
    catch (Throwable t) {
    }

    aSession.setAttribute("nrmediaitems", new Integer(nrMediaItems));

    if (aRequest.getParameter("post")==null)
      return false;
    else {
      validate(aValidationErrors, aRequest, aSession);
      return (aValidationErrors == null || aValidationErrors.size() == 0);
    }
  }

  protected void validate(List aResults, Request aRequest, Session aSession) throws SessionExc, SessionFailure {
    String password = (String) aSession.getAttribute("password");

    if (password!=null) {
      String submittedPassword= aRequest.getParameter("password").trim();

      if (!password.equals(submittedPassword)) {
        aResults.add(new ValidationError("password", "passwordmismatch"));
      }
    }
  }

  /**
   * Class that represents a validation error
   *
   * <p>Title: </p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2003</p>
   * <p>Company: </p>
   * @author not attributable
   * @version 1.0
   */

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

  /**
   * Convenience validation method to test wether a field has been filled in
   *
   * @param aRequest
   * @param aFieldName
   * @param anErrorMessageResource
   * @param aValidationResults
   * @return
   */

  protected boolean testFieldEntered(Request aRequest, String aFieldName, String anErrorMessageResource, List aValidationResults) {
    Object value = aRequest.getParameter(aFieldName);
    if (value==null || !(value instanceof String) || ((String) value).trim().length()==0) {
      aValidationResults.add(new ValidationError(aFieldName, anErrorMessageResource));
      return false;
    }
    else
      return true;
  }

  /**
   * Convenience validation method to test wether a field is numeric

   * @param aRequest
   * @param aFieldName
   * @param anErrorMessageResource
   * @param aValidationResults
   * @return
   */

  protected boolean testFieldIsNumeric(Request aRequest, String aFieldName, String anErrorMessageResource, List aValidationResults) {
    Object value = aRequest.getParameter(aFieldName);
    if (value!=null) {
      try {
        Integer.parseInt((String) value);
        return true;
      }
      catch (Throwable t) {
        aValidationResults.add(new ValidationError(aFieldName, anErrorMessageResource));
        return false;
      }
    }
    return true;
  }

  /**
   * Convenience validation method to test wether a field is numeric

   * @param aRequest
   * @param aFieldName
   * @param anErrorMessageResource
   * @param aValidationResults
   * @return
   */

  protected boolean testFieldLength(Request aRequest, String aFieldName, int aMaxLength, String anErrorMessageResource, List aValidationResults) {
    String value = aRequest.getParameter(aFieldName);

    if (value!=null) {
      if (value.length()>aMaxLength) {
        aValidationResults.add(new ValidationError(aFieldName, anErrorMessageResource));
        return false;
      }
      else return true;
    }
    return true;
  }

  /**
   * Method to generate a one-time password
   *
   * @return a password, to be used once
   */

  protected String generateOnetimePassword() {
    Random r = new Random();
    int random = r.nextInt();

    long l = System.currentTimeMillis();

    l = (l*l*l*l)/random;
    if (l<0)
      l = l * -1;

    String returnString = ""+l;

    return returnString.substring(5);
  }


  /**
   *
   * @param aRequest
   * @param aStorage
   * @return
   * @throws SessionExc
   * @throws SessionFailure
   */

  protected static final Map getIntersectingValues(Request aRequest, StorageObject aStorage) throws SessionExc, SessionFailure {
    Map result = new HashMap();

    Iterator i = aStorage.getFields().iterator();

    while (i.hasNext()) {
      String fieldName = (String) i.next();
      Object value = aRequest.getParameter(fieldName);
      if (value != null)
        result.put(fieldName, value);
    }

    return result;
  }

  protected static class DuplicatePostingExc extends SessionExc {
    public DuplicatePostingExc(String aMessage) {
      super(aMessage);
    }
  }

}
