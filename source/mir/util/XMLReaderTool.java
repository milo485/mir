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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XMLReaderTool {

  public static void checkValidIdentifier(String anIdentifier) throws XMLReader.XMLReaderExc {
  }

  public static String getStringAttributeWithDefault(Map anAttributes, String aKey, String aDefault) {
    if (anAttributes.containsKey(aKey))
      return (String) anAttributes.get(aKey);
    else
      return aDefault;
  }

  public static void checkIntegerAttribute(Map anAttributes, String aKey) throws XMLReader.XMLReaderExc {
    try {
      Integer.parseInt((String) anAttributes.get(aKey));
    }
    catch (Throwable t) {
      throw new XMLReader.XMLReaderExc("attribute '"+aKey+"' is not an integer" );
    }
  }

  public static int getIntegerAttributeWithDefault(Map anAttributes, String aKey, int aDefault) throws XMLReader.XMLReaderExc  {
    String value;

    if (anAttributes.containsKey(aKey)) {
      checkIntegerAttribute(anAttributes, aKey);
      return Integer.parseInt((String) anAttributes.get(aKey));
    }
    else
      return aDefault;
  }

  public static void checkAttributes(Map anAttributes, String[] aRequiredAttributes, String[] anOptionalAttributes)  throws XMLReader.XMLReaderExc {
    checkAttributeSet(anAttributes.keySet(),
       new HashSet(Arrays.asList(aRequiredAttributes)),
       new HashSet(Arrays.asList(anOptionalAttributes)));
  }

  public static void checkAttributeSet(Set aSet, Set aRequiredElements, Set anOptionalElements) throws XMLReader.XMLReaderExc{
    Iterator i;

    i = aSet.iterator();
    while (i.hasNext()) {
      Object item = i.next();

      if (!(aRequiredElements.contains(item) || anOptionalElements.contains(item)))
        throw new XMLReader.XMLReaderExc("unknown attribute '" + item + "'" );
    }

    i = aRequiredElements.iterator();
    while (i.hasNext()) {
      Object item = i.next();

      if (!(aSet.contains(item)))
        throw new XMLReader.XMLReaderExc("missing required attribute '" + item + "'" );
    }

  }

  /**
   * Returns the namespace part of a qualified XML Tag name <br>
   * Example:<br>
   * <code>getNameSpaceFromQualifiedName("dc:creator");</code> <br>
   * will return <code>"dc"</code>
   *
   * @param aQualifiedName
   * @return
   */

  public static String getNameSpaceFromQualifiedName(String aQualifiedName) {
    List parts = StringRoutines.splitString(aQualifiedName, ":");

    if (parts.size()<=1)
      return null;
    else
      return (String) parts.get(0);
  }

  /**
   * Returns the localname part of a qualified XML Tag name <br>
   * Example:<br>
   * <code>getLocalNameFromQualifiedName("dc:creator");</code> <br>
   * will return <code>"creator"</code>
   *
   * @param aQualifiedName
   * @return
   */

  public static String getLocalNameFromQualifiedName(String aQualifiedName) {
    List parts = StringRoutines.splitString(aQualifiedName, ":");

    if (parts.size()<1)
      return null;
    if (parts.size()==1)
      return (String) parts.get(0);
    else
      return (String) parts.get(1);
  }

}
