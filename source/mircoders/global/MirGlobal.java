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

package mircoders.global;

import mir.misc.*;
import mircoders.localizer.*;

public class MirGlobal {
  static private MirConfig configuration;
  static private MirLocalizer localizer;
  static private ProducerEngine producerEngine;

  public static MirLocalizer localizer() {
    String localizerClassName;
    Class localizerClass;

    if (localizer == null ) {
      synchronized(MirGlobal.class) {
        if (localizer == null ) {
          localizerClassName = getConfigPropertyWithDefault("Mir.Localizer", "mirlocal.loaclizer.basic.MirBasicLocalizer");

          try {
            localizerClass = Class.forName(localizerClassName);
          }
          catch (Throwable t) {
            throw new ConfigException("localizer class '" + localizerClassName + "' not found: " + t.toString());
          }

          if (!(MirLocalizer.class.isAssignableFrom(localizerClass)))
            throw new ConfigException("localizer class '" + localizerClassName + "' is not assignable from MirLocalizer");

          try {
            localizer = new MirCachingLocalizerDecorator((MirLocalizer) localizerClass.newInstance());
          }
          catch (Throwable t) {
            throw new ConfigException("localizer class '" + localizerClassName + "' cannot be instantiated: " + t.toString());
          }
        }
      }
    }

    return localizer;
  }

  public static MirConfig config() {
    if (configuration == null) {
      configuration = new MirConfig();
    }

    return configuration;
  }

  public static ProducerEngine producerEngine() {
    if (producerEngine == null) {
      producerEngine = new ProducerEngine();
    }

    return producerEngine;
  }

  public static String getConfigPropertyWithDefault(String aPropertyName, String aDefault) {
    String result;

    result = config().getProp(aPropertyName);

    if (result==null)
      result = aDefault;

    return result;
  }

  public static String getConfigProperty(String aPropertyName) {
    String result;

    result = config().getProp(aPropertyName);

    if (result==null)
      throw new ConfigException("Property '" + aPropertyName + "' not present");

    return result;
  }

  public static int getConfigIntegerProperty(String aPropertyName) {
    String result;

    result = config().getProp(aPropertyName);

    return Integer.parseInt(result);
  }

  public static boolean getConfigBooleanProperty(String aPropertyName) {
    String result;

    result = config().getProp(aPropertyName);

    if (result==null)
      throw new ConfigException("Boolean property '" + aPropertyName + "' not present");

    return (result.equals("yes") || result.equals("1"));
  }
}
