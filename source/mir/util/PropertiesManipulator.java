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
package mir.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import multex.Exc;
import multex.Failure;

public class PropertiesManipulator {
  private List entries;
  private Map values;

  public PropertiesManipulator() {
    entries = new Vector();
    values = new HashMap();
  }

  public void addEmptyLine() {
    entries.add(new EmptyLine());
  }

  public void addComment(String aComment) {
    entries.add(new Comment(aComment));
  }

  public void addEntry(String aKey, String aValue) {
    entries.add(new Entry(aKey, aValue));
    values.put(aKey, aValue);
  }

  public Iterator getEntries() {
    return entries.iterator();
  }

  public String get(String aKey) {
    return (String) values.get(aKey);
  }

  public boolean containsKey(String aKey) {
    return values.containsKey(aKey);
  }

  public static class Comment {
    private String comment;

    public Comment(String aComment) {
      comment = aComment;
    }

    public String getComment() {
      return comment;
    }
  }

  public static class EmptyLine {
    public EmptyLine() {
    }
  }

  public static class Entry {
    private String key;
    private String value;

    public Entry(String aKey, String aValue) {
      key = aKey;
      value = aValue;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }

  private final static String PLAIN= "[^\\\\]*";
  private final static String ESCAPE= "\\\\[ tn]";
  private final static String UNICODE= "\\\\u[a-fA-F0-9][a-fA-F0-9][a-fA-F0-9][a-fA-F0-9]";


  private static String decode(String aValue) {
    try {
      SimpleParser parser = new SimpleParser(aValue);
      StringBuffer result = new StringBuffer();

      while (!parser.isAtEnd()) {
        result.append(parser.parse(PLAIN));

        if (!parser.isAtEnd()) {
          if (parser.parses(UNICODE)) {
            String unicode = parser.parse(UNICODE);

            result.append((char) Integer.parseInt(unicode.substring(2,6), 16));
          }
          else if (parser.parses(ESCAPE)) {
            String escape = parser.parse(ESCAPE);
            result.append(escape.substring(1));
          }
          else
            throw new PropertiesManipulatorExc("Invalid escape code: " + parser.remainingData());
        }
      }

      return result.toString();
    }
    catch (Throwable t) {
      throw new PropertiesManipulatorFailure(t);
    }
  }

  private static String encode(String aValue, boolean aUseUnicodeEscapes) {
    try {
      StringBuffer result = new StringBuffer();
      boolean leadingspace=true;

      for (int i = 0; i<aValue.length(); i++) {
        char c = aValue.charAt(i);

        if (aUseUnicodeEscapes && (c<0x20 || c>0x7e)) {
          String code=Integer.toHexString(c);
          result.append("\\u");
          for (int j=0; j<4-code.length(); j++)
            result.append("0");
          result.append(code);
        }
        else if (c=='\\')
        {
          result.append("\\\\");
        }
        else if (c=='\n')
        {
          result.append("\\n");
        }
        else if (c=='\r')
        {
          result.append("\\r");
        }
        else if (c=='\t')
        {
          result.append("\\t");
        }
        else if (c==' ' && leadingspace) {
          result.append("\\ ");
        }
        else {
          result.append(c);
        }

        leadingspace = leadingspace && c ==' ';
      }

      return result.toString();
    }
    catch (Throwable t) {
      throw new PropertiesManipulatorFailure(t);
    }
  }

  // ML: to be fixed
  private final static String SPACE = "[\t\n\r ]*";
  private final static String KEY = "(([\\\\].)|([^\\\\=: \t\n\r]))*";
  private final static String SEPARATOR = "[\t\n\r ]*[:=]?[\t\n\r ]*";
  private final static String VALUE = "(([\\\\].)|([^\\\\]))*";


  public static PropertiesManipulator readProperties(InputStream anInputStream) throws PropertiesManipulatorExc, PropertiesManipulatorFailure {
    return readProperties(anInputStream, "ISO-8859-1");
  }

  public static PropertiesManipulator readProperties(InputStream anInputStream, String anEncoding) throws PropertiesManipulatorExc, PropertiesManipulatorFailure {
    try {
      PropertiesManipulator result = new PropertiesManipulator();
      LineNumberReader reader = new LineNumberReader(new InputStreamReader(anInputStream, anEncoding));

      String line = reader.readLine();

      while (line != null) {
        String trimmedLine = line.trim();

        if (trimmedLine.length() == 0) {
          result.addEmptyLine();
        }
        else if (trimmedLine.startsWith("!") || trimmedLine.startsWith("#")) {
          result.addComment(line);
        }
        else {
          SimpleParser parser = new SimpleParser(line);
          parser.skip(SPACE);
          String key = parser.parse(KEY);
          parser.skip(SEPARATOR);
          String value = parser.parse(VALUE);
          while (parser.remainingData().length()>0) {
            if (!parser.remainingData().equals("\\"))
              throw new PropertiesManipulatorExc("internal error: remainingData = " + parser.remainingData());

            line = reader.readLine();
            if (line==null) {
              throw new PropertiesManipulatorExc("Unexpected end of file");
            }
            parser = new SimpleParser(line);
            parser.skip(SPACE);
            value = value + parser.parse(VALUE);
          }

          result.addEntry(decode(key), decode(value));
        }
        line = reader.readLine();
      }

      reader.close();

      return result;
    }
    catch (PropertiesManipulatorExc t) {
      throw t;
    }
    catch (Throwable t) {
      throw new PropertiesManipulatorFailure(t);
    }
  }

  public static void writeProperties(PropertiesManipulator aProperties, OutputStream anOutputStream) throws PropertiesManipulatorExc, PropertiesManipulatorFailure {
    writeProperties(aProperties, anOutputStream, "ISO-8859-1", true);
  }

  public static void writeProperties(PropertiesManipulator aProperties, OutputStream anOutputStream, String anEncoding, boolean aUseUnicodeEscapes) throws PropertiesManipulatorExc, PropertiesManipulatorFailure {
    try {
      PrintWriter p = new PrintWriter(new OutputStreamWriter(anOutputStream, anEncoding));

      try {
        Iterator i = aProperties.getEntries();

        while (i.hasNext()) {
          Object entry = i.next();

          if (entry instanceof EmptyLine) {
            p.println();
          }
          else if (entry instanceof Comment) {
            p.println(((Comment) entry).getComment());
          }
          else if (entry instanceof Entry) {
            String key = encode( ( (Entry) entry).getKey(), aUseUnicodeEscapes);
            String value = "";
            if ( ( (Entry) entry).getValue() != null)
              value = encode( ( (Entry) entry).getValue(), aUseUnicodeEscapes);

            String line = key + " = " + value;

            p.println(line);

          }
          else throw new PropertiesManipulatorExc("Unknown entry class: " +entry.getClass().getName());
        }
      }
      finally {
        p.close();
      }
    }
    catch (Throwable t) {
      throw new PropertiesManipulatorFailure(t);
    }
  }

  public static class PropertiesManipulatorFailure extends Failure {
    public PropertiesManipulatorFailure(Throwable aThrowable) {
      super(aThrowable.getMessage(), aThrowable);
    }

    public PropertiesManipulatorFailure(String aMessage, Throwable aThrowable) {
      super(aMessage, aThrowable);
    }
  }

  public static class PropertiesManipulatorExc extends Exc {
    public PropertiesManipulatorExc(String aMessage) {
      super(aMessage);
    }
  }
}