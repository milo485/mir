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
package mir.session;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;

import mir.util.HTTPParsedRequest;

public class HTTPAdapters {
  public static class HTTPRequestAdapter implements Request {
    private HttpServletRequest request;

    public HTTPRequestAdapter(HttpServletRequest aRequest) {
      request = aRequest;
    }

    public String getHeader(String aHeaderName) {
      return request.getHeader(aHeaderName);
    };

    public String getParameter(String aName) {
      return request.getParameter(aName);
    };

    public List getUploadedFiles() {
      return new Vector();
    };

    public List getParameters(String aName) {
      return Arrays.asList(request.getParameterValues(aName));
    };

    public HttpServletRequest getRequest() {
      return request;
    }
  }

  public static class HTTPParsedRequestAdapter implements Request {
    private HTTPParsedRequest request;

    public HTTPParsedRequestAdapter(HTTPParsedRequest aRequest) {
      request = aRequest;
    }

    public String getHeader(String aHeaderName) {
      return request.getHeader(aHeaderName);
    };

    public String getParameter(String aName) {
      return request.getParameter(aName);
    };

    public List getParameters(String aName) {
      return request.getParameterList(aName);
    };

    public List getUploadedFiles() {
      List result = new Vector();
      List files = request.getFiles();

      for (int i=0; i<files.size(); i++) {
        result.add(new CommonsUploadedFileAdapter((FileItem) files.get(i)));
      }

      return result;
    };

    public HttpServletRequest getRequest() {
      return request.getRequest();
    }
  }

  public static class HTTPSessionAdapter implements Session {
    private HttpSession session;

    public HTTPSessionAdapter(HttpSession aSession) {
      session = aSession;
    }
    public Object getAttribute(String aName) {
      return session.getAttribute(aName);
    }

    public void deleteAttribute(String aName) {
      session.removeAttribute(aName);
    }

    public void setAttribute(String aName, Object aNewValue) {
      if (aNewValue==null)
        deleteAttribute(aName);
      else
        session.setAttribute(aName, aNewValue);
    }

    public void terminate() {
      session.invalidate();
    }
  }
}