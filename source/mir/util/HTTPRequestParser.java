package mir.util;

import javax.servlet.*;
import javax.servlet.http.*;

public class HTTPRequestParser {
  private HttpServletRequest request;

  public HTTPRequestParser(HttpServletRequest aRequest) {
    request = aRequest;
  }

  public boolean hasParameter(String aName) {
    return request.getParameter(aName)!=null;
  }

  public String getParameterWithDefault(String aName, String aDefault) {
    if (hasParameter(aName))
      return request.getParameter(aName);
    else
      return aDefault;
  }

  public String getParameter(String aName) {
    return getParameterWithDefault(aName, "");
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