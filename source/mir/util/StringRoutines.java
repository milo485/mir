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

import gnu.regexp.RE;

import java.util.List;
import java.util.Vector;

public class StringRoutines {

  private StringRoutines() {
  }

  public static int indexOfCharacters(String aString, char[] aCharacters, int aFrom) {
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

  public static String replaceStringCharacters(String aText, char[] aCharactersToReplace, String[] aStringsToSubstitute) {
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
  /**
   *
   * @param aText
   * @param anEscapeCharacater
   * @param aCharactersToReplace
   * @param aStringsToSubstitute
   * @return
   */

  public static String replaceEscapedStringCharacters(String aText, char anEscapeCharacter, char[] aCharactersToReplace, String[] aStringsToSubstitute) {
    if (aText==null)
      return null;

    int position, nextPosition;
    int i;
    StringBuffer result = new StringBuffer();

    position=0;
    do {
      nextPosition = aText.indexOf(anEscapeCharacter, position);

      if (nextPosition<0)
        nextPosition = aText.length();

      result.append(aText.substring(position, nextPosition));

      if (nextPosition+1<aText.length()) {
        nextPosition = nextPosition+1;

        boolean found = false;
        for (i = 0; i < aCharactersToReplace.length; i++) {
          if (aCharactersToReplace[i] == aText.charAt(nextPosition)) {
            result.append(aStringsToSubstitute[i]);
            found=true;
            break;
          }
        }

        if (!found) {
          result.append(aText.charAt(nextPosition));
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
      String aSearchExpression, String aReplacement) throws UtilExc {
    try {
      RE regularExpression;

      regularExpression = new RE(aSearchExpression);

      return regularExpression.substituteAll(aSource, aReplacement);
    }
    catch (Throwable t) {
      throw new UtilFailure("StringRoutines.performRegularExpressionReplacement: " + t.toString(), t);
    }
  }

  public static String performCaseInsensitiveRegularExpressionReplacement(String aSource,
      String aSearchExpression, String aReplacement) throws UtilExc {
    try {
      RE regularExpression;

      regularExpression = new RE(aSearchExpression, RE.REG_ICASE);

      return regularExpression.substituteAll(aSource, aReplacement);
    }
    catch (Throwable t) {
      throw new UtilFailure("StringRoutines.performRegularExpressionReplacement: " + t.toString(), t);
    }
  }

  /**
   *
   * @param aSource
   * @param aSearchExpression
   * @return
   * @throws REException
   */
  public static boolean performRegularExpressionSearch(String aSource,
      String aSearchExpression) throws UtilExc {
    try {
      RE regularExpression;

      regularExpression = new RE(aSearchExpression);

      return regularExpression.isMatch(aSource);
    }
    catch (Throwable t) {
      throw new UtilFailure("StringRoutines.performRegularExpressionSearch: " + t.toString(), t);
    }
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

    if((position = aString.indexOf(aSeparator, previousPosition))>=0) {
      result.add(aString.substring(previousPosition, position));
      previousPosition = position + aSeparator.length();
    }

    result.add(aString.substring(previousPosition, aString.length()));

    return result;
  }

  public static List splitStringWithEscape(String aString, char aSeparator, char anEscape) {
    List result= new Vector();
    int previousPosition = 0;
    int position;
    int endOfNamePosition;
    StringBuffer currentItem = new StringBuffer();

    if (aString!=null) {
      while ((position = indexOfCharacters(aString, new char[] {aSeparator, anEscape}, previousPosition))>=0) {
        currentItem.append(aString.substring(previousPosition, position));

        if (aString.charAt(position)==aSeparator) {
          result.add(currentItem.toString());
          currentItem.delete(0, currentItem.length());
        }
        else {
          currentItem.append(aString.charAt(position));
          if (aString.length()>position+1) {
            position=position+1;
            currentItem.append(aString.charAt(position));
          }
        }
        previousPosition = position + 1;
      }
      currentItem.append(aString.substring(previousPosition, aString.length()));
      result.add(currentItem.toString());
    }

    return result;
  }

  public static String replicateString(String aString, int aCount) {
    StringBuffer result = new StringBuffer();

    for (int i=0; i<aCount; i++)
      result.append(aString);

    return result.toString();
  }

  public static String replicateChar(char aCharacter, int aCount) {
    char result[] = new char[aCount];

    for (int i=0; i<aCount; i++)
      result[i]= aCharacter;

    return new String(result);
  }

  public static String padStringLeft(String aString, int aLength, char aPadCharacter) {
    if (aString.length()<aLength)
      return replicateChar(aPadCharacter, aLength-aString.length()) + aString;
    else
      return aString;
  }

  private static final char HEX_CHARACTERS[] = {
      '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  public static String convertToHex(long aData, int aNumberOfDigits) {
    StringBuffer result = new StringBuffer();

    for (int digit = aNumberOfDigits-1; digit>=0; digit--) {
      int value = (int) (aData >> (digit*4)) & 0xf;
      result.append(HEX_CHARACTERS[value]);
    }

    return result.toString();
  }

  public static String convertToHex(byte[] aData) {
    StringBuffer result = new StringBuffer();

    for (int i = 0; i<aData.length; i++) {
      result.append(convertToHex(aData[i], 2));

    }

    return result.toString();
  }
}
