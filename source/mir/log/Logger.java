package mir.log;


public interface Logger {
  public void debug( Object o, String s);
  public void info( Object o, String s);
  public void warn( Object o, String s);
  public void error( Object o, String s);
  public void fatal( Object o, String s);
}
