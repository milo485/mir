package mir.session;

public interface Session {
  public Object getAttribute(String aName);
  public void setAttribute(String aName, Object aNewValue);
  public void deleteAttribute(String aName);
  public void terminate();
}
