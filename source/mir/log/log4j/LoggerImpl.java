package mir.log.log4j;

import java.util.HashMap;
import java.util.Map;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class LoggerImpl implements mir.log.Logger {
  private static Map loggers = new HashMap();

  public LoggerImpl() throws PropertiesConfigExc {
    System.setProperty("log.home",
        MirPropertiesConfiguration.instance().getStringWithHome("Log.Home"));
    PropertyConfigurator.configure(
        MirPropertiesConfiguration.instance().getStringWithHome("Log.log4j.ConfigurationFile").trim());
  }

  public void debug(Object o, String s) {
    this.getLogger(o).debug(s);
  }

  public void info(Object o, String s) {
    this.getLogger(o).info(s);
  }

  public void warn(Object o, String s) {
    this.getLogger(o).warn(s);
  }

  public void error(Object o, String s) {
    this.getLogger(o).error(s);
  }

  public void fatal(Object o, String s) {
    this.getLogger(o).fatal(s);
  }

  private Logger getLogger(Object o) {
    String name;
    Logger l;

    if (o instanceof String) {
      name = (String) o;
    }
    else if (o instanceof Class) {
      name = ( (Class) o).getName();
    }
    else if (o != null) {
      name = o.getClass().getName();
    }
    else {
      name = "generic";
    }

    synchronized (loggers) {
      l = (Logger) loggers.get(name);
      if (l == null) {
        if (!loggers.containsKey(name)) {
          l = Logger.getLogger(name);
          loggers.put(name, l);
        }
        l = (Logger) loggers.get(name);
      }
    }

    return l;
  }
}