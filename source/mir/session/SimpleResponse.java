package mir.session;

import java.util.*;

public class SimpleResponse implements Response {
  private String generator;
  private Map values;

  public SimpleResponse() {
    values = new HashMap();
  }

  public SimpleResponse(Map aMap) {
    values = aMap;
  }

  public void setResponseValue(String aName, Object aValue) {
    values.put(aName, aValue);
  }

  public Map getResponseValues() {
    return values;
  }

  public void setResponseGenerator(String aGenerator) {
    generator = aGenerator;
  }

  public String getResponseGenerator() {
    return generator;
  }
}