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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 *
 * <p>Title: </p>
 * <p>Description:
 *   Class to parse structured content:
 *   <tt>
 *      {
 *        name = 'Countries'
 *        countries = [ spain 'united kingdom' germany ]
 *        cities = {
 *          spain = [ 'madrid' barcelona bilbao ]
 *          'united kingdom' = [ 'london' 'lancaster' ]
 *          germany = [ 'berlin' 'bremen' ]
 *        }
 *      }
 *   </tt>
 *
 *   And put it into a <code>String</code>/<code>Map</code>/<code>List</code> structure.
 *  </p>
 *  <p>
 *    Parsing is be very optimistic: no exception is ever to be thrown.
 *  </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class StructuredContentParser {
  public StructuredContentParser() {
  }

  private static class Scanner {
    private String data;
    private int position;

    public Scanner(String aData) {
      data = aData;
      position = 0;
    }

    public String scanIdentifier() {
      StringBuffer result = new StringBuffer();

      while (!isAtEnd() && Character.isJavaIdentifierPart(peek())) {
        result.append(scan());
      }

      return result.toString();
    }

    public String scanString() {
      StringBuffer result = new StringBuffer();
      char delimiter='\'';

      if (!isAtEnd()) {
        delimiter=scan();
      }

      boolean finished = false;

      while (!finished && !isAtEnd()) {
        char data = scan();

        if (data == delimiter) {
          if (!isAtEnd() && peek() == delimiter) {
            scan();
            result.append(data);
          }
          else
            finished=true;
        }
        else
          result.append(data);
      }

      return result.toString();
    }

    public void skipSpace() {
      while (!isAtEnd() && Character.isWhitespace(peek()))
        scan();
    }

    public boolean isAtEnd() {
      return position >= data.length();
    }

    public char peek() {
      if (!isAtEnd()) {
        return data.charAt(position);
      }
      else
        return '\0';
    }

    public char scan() {
      if (!isAtEnd()) {
        char result =data.charAt(position);
        position++;
        return result;
      }
      else
        return '\0';
    }
  }

  public static List parseList(Scanner aScanner) {
    List result = new Vector();
    aScanner.skipSpace();
    if (aScanner.peek() == '[')
      aScanner.scan();
    aScanner.skipSpace();
    Object object = parseObject(aScanner);

    while (object != null) {
      result.add(object);
      aScanner.skipSpace();
      object = parseObject(aScanner);
    }

    aScanner.skipSpace();
    if (aScanner.peek() == ']')
      aScanner.scan();

    return result;
  }

  public static Map parseMap(Scanner aScanner) {
    Map result = new HashMap();
    aScanner.skipSpace();
    if (aScanner.peek() == '{')
      aScanner.scan();
    aScanner.skipSpace();
    Object key = parseObject(aScanner);
    aScanner.skipSpace();
    if (aScanner.peek() == '=')
      aScanner.scan();
    aScanner.skipSpace();
    Object value = parseObject(aScanner);


    while (key != null && value!=null) {
      result.put(key, value);
      aScanner.skipSpace();
      key = parseObject(aScanner);
      aScanner.skipSpace();
      if (aScanner.peek() == '=')
        aScanner.scan();
      aScanner.skipSpace();
      value = parseObject(aScanner);
    }

    aScanner.skipSpace();
    if (aScanner.peek() == '}')
      aScanner.scan();

    return result;
  }

  public static Object parseObject(Scanner aScanner) {
    Object result = null;

    aScanner.skipSpace();

    if (!aScanner.isAtEnd()) {
      char data = aScanner.peek();

      if (data == '[')
        return parseList(aScanner);
      if (data == '{')
        return parseMap(aScanner);
      if (data == '\'' || data == '"')
        return aScanner.scanString();
      if (Character.isJavaIdentifierPart(data))
        return aScanner.scanIdentifier();
    }

    return null;
  }

  public static Object parse(String aData) {
    Scanner scanner = new Scanner(aData);

    return parseObject(scanner);
  }

  public static String constructStringLiteral(String aString) {
    final char[] CHARACTERS_TO_ESCAPE = { '\'' };
    final String[] ESCAPE_CODES = { "\'\'" };

    return "'" +  StringRoutines.replaceStringCharacters(aString, CHARACTERS_TO_ESCAPE, ESCAPE_CODES) + "'";
  }
}