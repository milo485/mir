package mir.log;

public class LoggerWrapper {
  private Object object;

  public LoggerWrapper( Object anObject ) {
    object = anObject;
  }

  public void debug( String aMessage ) {
    Log.debug(object, aMessage);
  };

  public void info( String aMessage ) {
    Log.info(object, aMessage);
  };

  public void warn( String aMessage ) {
    Log.warn(object, aMessage);
  };


  public void error( String aMessage ) {
    Log.error(object, aMessage);
  };

  public void fatal( String aMessage ) {
    Log.fatal(object, aMessage);
  };
}