package mir.session;

import java.util.*;

public interface Request {
  public String getParameter(String aName);
  public List getUploadedFiles();
  public List getParameters(String aName);
}