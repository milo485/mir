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

import java.io.PrintWriter;

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

