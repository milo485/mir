package mir.util;

import java.util.*;

public class URLBuilder {
  private Map keyValues;
  private String base;

  public URLBuilder(String aBase) {
    keyValues = new HashMap();
    base = aBase;
  }

  public URLBuilder() {
    this("");
  }

  public void setValue(String aKey, String aValue) {
    keyValues.put(aKey, aValue);
  }

  public void setValue(String aKey, int aValue) {
    keyValues.put(aKey, Integer.toString(aValue));
  }

  public void deleteKey(String aKey) {
    keyValues.remove(aKey);
  }

  public String getQuery() {
    StringBuffer query = new StringBuffer();
    Iterator i;

    i = keyValues.entrySet().iterator();

    while(i.hasNext()) {
      Map.Entry entry = (Map.Entry) i.next();

      query.append(HTMLRoutines.encodeURL((String) entry.getKey()));
      query.append("=");
      query.append(HTMLRoutines.encodeURL((String) entry.getValue()));

      if (i.hasNext())
        query.append("&");
    }

    return query.toString();
  }

  public String getUrl() {
    return base + "?" + getQuery();
  }
}