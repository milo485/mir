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

package mir.util;

import gnu.regexp.RE;
import gnu.regexp.REException;

import java.util.List;
import java.util.Vector;

public class StringRoutines {

  private StringRoutines() {
  }

  static int indexOfCharacters(String aString, char[] aCharacters, int aFrom) {
    int i;
    int result=-1;
    int position;

    for (i=0; i<aCharacters.length ; i++) {
      position = aString.indexOf(aCharacters[i], aFrom);

      if (position != -1 && ( result == -1 || position < result )) {
        result = position;
      }
    }

    return result;
  }

  static String replaceStringCharacters(String aText, char[] aCharactersToReplace, String[] aStringsToSubstitute) {
    if (aText==null)
      return null;

    int position, nextPosition;
    int i;
    StringBuffer result = new StringBuffer();

    position=0;
    do {
      nextPosition = StringRoutines.indexOfCharacters(aText, aCharactersToReplace, position);

      if (nextPosition<0)
        nextPosition = aText.length();

      result.append(aText.substring(position, nextPosition));

      if (nextPosition<aText.length())
        for (i=0; i<aCharactersToReplace.length; i++) {
          if (aCharactersToReplace[i] == aText.charAt(nextPosition)) {
            result.append(aStringsToSubstitute[i]);
            break;
          }
        }
      position=nextPosition+1;
    }
    while (nextPosition<aText.length()) ;

    return result.toString();
  }


  public static String interpretAsString(Object aValue) throws Exception {
    if (aValue instanceof String)
      return (String) aValue;

    if (aValue instanceof Integer)
      return ((Integer) aValue).toString();

    if (aValue == null)
      return "";

    throw new Exception("String expected, "+aValue+" found");
  }

  public static int interpretAsInteger(Object aValue) throws Exception {
    if (aValue instanceof Integer)
      return ((Integer) aValue).intValue();

    if (aValue instanceof String)
      try {
        return Integer.parseInt((String) aValue);
      }
      catch (Throwable t) {
        throw new Exception("Integer expected, "+aValue+" found");
      }

    throw new Exception("Integer expected, "+aValue+" found");
  }

  /**
   *
   * @param aSource
   * @param aSearchExpression
   * @param aReplacement
   * @return
   * @throws Exception
   */
  public static String performRegularExpressionReplacement(String aSource,
      String aSearchExpression, String aReplacement) throws Exception {

    RE regularExpression;

    regularExpression = new RE(aSearchExpression);

    return regularExpression.substituteAll(aSource, aReplacement);
  }

  /**
   *
   * @param aSource
   * @param aSearchExpression
   * @return
   * @throws REException
   */
  public static boolean performRegularExpressionSearch(String aSource,
      String aSearchExpression) throws REException {
    RE regularExpression;

    regularExpression = new RE(aSearchExpression);

    return regularExpression.isMatch(aSource);
  }

  /**
   * Separates a string based on a separator:
   *     <code>seperateString("a:b:c", ":");</code> will lead to
   *     a List with 3 Strings: <code>"a"</code>, <code>"b"</code> and <code>"c"</code>
   *
   * @param aString     The string to split
   * @param aSeparator
   * @return
   */

  public static List splitString(String aString, String aSeparator) {
    List result= new Vector();
    int previousPosition = 0;
    int position;
    int endOfNamePosition;

    if (aString!=null) {
      while ( (position = aString.indexOf(aSeparator, previousPosition)) >= 0) {
        result.add(aString.substring(previousPosition, position));
        previousPosition = position + aSeparator.length();
      }
      result.add(aString.substring(previousPosition, aString.length()));
    }

    return result;
  }

  /**
   * Separates a String into at most 2 parts based on a separator:
   * <ul>
   *   <li>
   *     <code>seperateString("a:b:c", ":");</code> will lead to
   *     a List with 2 Strings: <code>"a"</code> and <code>"b:c"</code>
   *   <li>
   *     <code>seperateString("abc", ":");</code> will lead to
   *     a List with a single String: <code>"abc"</code>
   * </ul>
   *
   *
   * @param aString
   * @param aSeparator
   * @return
   */
  public static List separateString(String aString, String aSeparator) {
    List result= new Vector();
    int previousPosition = 0;
    int position;
    int endOfNamePosition;

    if((position = aString.indexOf(aSeparator, previousPosition))>=0) {
      result.add(aString.substring(previousPosition, position));
      previousPosition = position + aSeparator.length();
    }

    result.add(aString.substring(previousPosition, aString.length()));

    return result;
  }
}