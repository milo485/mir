/*
 * Copyright (C) 2001, 2002 The Mir-coders group
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
 * the code of this program with  any library licensed under the Apache Software License, 
 * The Sun (tm) Java Advanced Imaging library (JAI), The Sun JIMI library 
 * (or with modified versions of the above that use the same license as the above), 
 * and distribute linked combinations including the two.  You must obey the 
 * GNU General Public License in all respects for all of the code used other than 
 * the above mentioned libraries.  If you modify this file, you may extend this 
 * exception to your version of the file, but you are not obligated to do so.  
 * If you do not wish to do so, delete this exception statement from your version.
 */


package mir.log;

import java.io.Writer;

public class LoggerToWriterAdapter extends Writer {
  private LoggerWrapper logger;
  private int messageType;
  private StringBuffer lineBuffer;
  private String lineSeparator;

  public LoggerToWriterAdapter(LoggerWrapper aLogger, int aMessageType) {
    lineBuffer = new StringBuffer();
    logger = aLogger;
    messageType = aMessageType;
    lineSeparator = System.getProperty("line.separator");
  }

  public LoggerToWriterAdapter(Logger aLogger, int aMessageType) {
    this(new LoggerWrapper(aLogger), aMessageType);
  }

  public void close() {
    flush();
  }

  public void flush() {
    if (lineBuffer.length()>0) {
      logger.message(messageType, lineBuffer.toString());
      lineBuffer.delete(0, lineBuffer.length());
    }
  }

  protected void checkBuffer() {
    int from = 0;
    int until = lineBuffer.toString().indexOf(lineSeparator, from);

    while (until>-1) {
      String line = lineBuffer.substring(from, until);
      logger.message(messageType, line);
      from = until + lineSeparator.length();
      until = lineBuffer.toString().indexOf(lineSeparator, from);
    }

    lineBuffer.delete(0, from);
  };

  public void write(char[] aBuffer, int anOffset, int aLength)  {
    lineBuffer.append(aBuffer, anOffset, aLength);
    checkBuffer();
  }
}