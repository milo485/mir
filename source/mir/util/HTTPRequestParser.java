package mir.util;

import javax.servlet.http.HttpServletRequest;

public class HTTPRequestParser {
  private HttpServletRequest request;
  private String encoding;

  public HTTPRequestParser(HttpServletRequest aRequest) {
    this(aRequest, aRequest.getCharacterEncoding());
  }

  public HTTPRequestParser(HttpServletRequest aRequest, String anEncoding) {
    request = aRequest;
    encoding = anEncoding;
  }

  public boolean hasParameter(String aName) {
    return request.getParameter(aName)!=null;
  }

  public String getParameterWithDefault(String aName, String aDefault) {
    if (hasParameter(aName))
      return getParameter(aName);
    else
      return aDefault;
  }

  public String getParameter(String aName) {
    try {
      String result = request.getParameter(aName);
      String requestEncoding = request.getCharacterEncoding();
      if (requestEncoding==null)
        requestEncoding = "ISO-8859-1";

      if (result != null && encoding!=null && !encoding.equals(requestEncoding)) {
        result = new String(result.getBytes(requestEncoding), encoding);
      }

      return result;
    }
    catch (Throwable t) {
      throw new RuntimeException("HTTPRequestParser.getParameter: " + t.getMessage());
    }
  }

  public int getIntegerWithDefault(String aName, int aDefault) {
    int result = aDefault;
    String value = getParameter(aName);

    try {
      result = Integer.parseInt(value);
    }
    catch (Throwable t) {
    }
    return result;
  }
}