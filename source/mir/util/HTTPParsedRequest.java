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
package mir.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import mir.log.LoggerWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;

public class HTTPParsedRequest {
  static final String MULTIPART_FORMDATA_CONTENTTYPE = "multipart/form-data";

  private HttpServletRequest request;
  private String encoding;
  private int maxUploadSize;
  private String tempDir;

  private Map stringValues;
  private Map listValues;
  private List files;

  private LoggerWrapper logger;

  public HTTPParsedRequest(HttpServletRequest aRequest, String anEncoding, int aMaxUploadSize, String aTempDir) throws UtilExc, UtilFailure {
    logger = new LoggerWrapper("Utility,HTTPParsedRequest");

    request = aRequest;
    encoding = anEncoding;
    maxUploadSize = aMaxUploadSize;
    tempDir = aTempDir;

    stringValues = new HashMap();
    listValues = new HashMap();
    files = new Vector();

    parseRequest(aRequest);
  }

  public HTTPParsedRequest(HttpServletRequest aRequest, int aMaxUploadSize, String aTempDir) throws UtilExc, UtilFailure {
    this(aRequest, aRequest.getCharacterEncoding(), aMaxUploadSize, aTempDir);
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public String getParameter(String aName) {
    return (String) stringValues.get(aName);
  }

  public String getHeader(String aName) {
    return request.getHeader(aName);
  }

  public List getFiles() {
    return files;
  }

  public List getParameterList(String aName) {
    if (listValues.containsKey(aName))
      return (List) listValues.get(aName);
    else
      return new Vector();
  }

  protected void parseRequest(HttpServletRequest aRequest) throws UtilExc, UtilFailure {

    try {
      String contentType = aRequest.getContentType();
      List parts = StringRoutines.splitString(contentType, ";");

      Enumeration e = aRequest.getParameterNames();

      while (e.hasMoreElements()) {
        String name = (String) e.nextElement();

        stringValues.put(name, aRequest.getParameter(name));
        List listValue = new Vector(Arrays.asList(aRequest.getParameterValues(name)));
        listValues.put(name, listValue);
      }

      if (parts.size()>0 && ((String) parts.get(0)).trim().toLowerCase().equals(MULTIPART_FORMDATA_CONTENTTYPE)) {
        parseMultipartRequest();
      }
    }
    catch (Throwable t) {
      t.printStackTrace();

      throw new UtilFailure(t);
    }
  }

  protected void parseMultipartRequest() throws UtilExc, UtilFailure {
    try {
      FileUpload upload = new FileUpload();

      upload.setSizeMax(maxUploadSize);
      upload.setSizeThreshold(4096);
      upload.setRepositoryPath(tempDir);

      List items = upload.parseRequest(request);

      Iterator i = items.iterator();
      while (i.hasNext()) {
        FileItem item = (FileItem) i.next();

        if (item.isFormField()) {
          if (!stringValues.containsKey(item.getName())) {
            stringValues.put(item.getFieldName(), item.getString(encoding));
          }

          List listValue = (List) listValues.get(item.getFieldName());
          if (listValue == null) {
            listValue = new Vector();
            listValues.put(item.getFieldName(), listValue);
          }
          listValue.add(item.getString(encoding));
        }
        else {
          if (item.getSize()>0)
            files.add(item);
        }
      }
    }
    catch (Throwable t) {
      throw new UtilFailure(t);
    }
  }
}
