package mir.log;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;

public class Log {

  private static Logger myLogger;

  static {
    try {
      String loggerClass = MirPropertiesConfiguration.instance().getString("Log.LogClass");
      myLogger = (Logger) Class.forName(loggerClass).newInstance();
    }
    catch (java.lang.ClassNotFoundException cnfe) {
      System.err.println("Log was not able to initialize: class not found");
      cnfe.printStackTrace(System.err);
    }
    catch (java.lang.InstantiationException ie) {
      System.err.println(
          "Log was not able to initialize: could not initialize class");
      ie.printStackTrace(System.err);
    }
    catch (java.lang.IllegalAccessException iae) {
      System.err.println("Log was not able to initialize: illegal access");
      iae.printStackTrace(System.err);
    }
    catch (PropertiesConfigExc e) {
      e.printStackTrace(System.err);
    }
  }

  public static void debug(Object o, String s) {
    myLogger.debug(o, s);
  }

  public static void info(Object o, String s) {
    myLogger.info(o, s);
  }

  public static void warn(Object o, String s) {
    myLogger.warn(o, s);
  }

  public static void error(Object o, String s) {
    myLogger.error(o, s);
  }

  public static void fatal(Object o, String s) {
    myLogger.fatal(o, s);
  }
}
