package mir.log;

import java.io.*;

public class LoggerWrapper {
  private Object object;
  public final static int DEBUG_MESSAGE = 1;
  public final static int INFO_MESSAGE = 2;
  public final static int WARN_MESSAGE = 3;
  public final static int ERROR_MESSAGE = 4;
  public final static int FATAL_MESSAGE = 5;

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

  public void message( int aType, String aMessage) {
    switch(aType) {
      case DEBUG_MESSAGE:
        debug(aMessage);
        break;
      case INFO_MESSAGE:
        info(aMessage);
        break;
      case WARN_MESSAGE:
        warn(aMessage);
        break;
      case ERROR_MESSAGE:
        error(aMessage);
        break;
      case FATAL_MESSAGE:
        fatal(aMessage);
        break;
      default:
        warn("LoggerWrapper.message: Unknown message type ("+aType+") for message '" + aMessage + "'");
    }
  }

  public PrintWriter asPrintWriter(int aMessageType) {
    return new PrintWriter(new LoggerToWriterAdapter(this, aMessageType));
  }
}

