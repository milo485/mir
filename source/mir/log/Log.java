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
