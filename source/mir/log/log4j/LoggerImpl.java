package mir.log.log4j;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import mir.misc.MirConfig;


public class LoggerImpl implements mir.log.Logger {

    private static Map loggers = new HashMap();

    public LoggerImpl() {
      System.setProperty("log.home", MirConfig.getPropWithHome("Log.Home"));
        PropertyConfigurator.configure(MirConfig.getPropWithHome("Log.log4j.ConfigurationFile").trim());
    }


    public void debug( Object o, String s ) {
        this.getLogger(o).debug(s);
    }

    public void info( Object o, String s ) {
        this.getLogger(o).info(s);
    }

    public void warn( Object o, String s ) {
        this.getLogger(o).warn(s);
    }

    public void error( Object o, String s ) {
        this.getLogger(o).error(s);
    }

    public void fatal( Object o, String s ) {
        this.getLogger(o).fatal(s);
    }


    private Logger getLogger( Object o ) {
        String name;

        if (o instanceof String) {
            name = (String) o;
        } else if (o instanceof Class) {
            name = ((Class)o).getName();
        } else if (o!=null) {
            name = o.getClass().getName();
        } else {
            name = "generic";
        }

        Logger l = (Logger)loggers.get(name);
        if (l==null) {
            l = Logger.getLogger(name);
            loggers.put(name, l);
        }
        return l;
    }
}