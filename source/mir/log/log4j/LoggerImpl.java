/*
 * Copyright (C) 2001, 2002  The Mir-coders group
 *
 * This file is part of Mir.
 *
 * Mir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Mir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, The Mir-coders gives permission to link
 * the code of this program with the com.oreilly.servlet library, any library
 * licensed under the Apache Software License, The Sun (tm) Java Advanced
 * Imaging library (JAI), The Sun JIMI library (or with modified versions of
 * the above that use the same license as the above), and distribute linked
 * combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than the above
 * mentioned libraries.  If you modify this file, you may extend this exception
 * to your version of the file, but you are not obligated to do so.  If you do
 * not wish to do so, delete this exception statement from your version.
 */

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