package mir.session;

import java.util.Map;

public interface Response {
  public void setResponseValue(String aName, Object aValue);
  public void setResponseGenerator(String aGenerator);
}
