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
        result.add(new CommonsUploadedFileAdapter(request, (FileItem) files.get(i)));
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
      session.setAttribute(aName, aNewValue);
    }

    public void terminate() {
      session.invalidate();
    }
  }
}