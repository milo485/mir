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

import java.net.URLEncoder;

public class HTMLRoutines {

  public static String encodeURL(String aString) {
    return URLEncoder.encode(aString);
  }

  /**
   *
   *
   * @param aString
   * @param anEncoding the encoding to use (Note: JDK 1.3 does not seem to support custom
   *             encodings, so this parameter is ignored for now)
   * @return
   */

  public static String encodeURL(String aString, String anEncoding) {
    try {
      return URLEncoder.encode(aString);
    }
    catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  public static String encodeHTML(String aText) {
    final char[] CHARACTERS_TO_ESCAPE = { '&', '<', '>', '"' };
    final String[] ESCAPE_CODES = { "&amp;", "&lt;", "&gt;", "&quot;" };

    return StringRoutines.replaceStringCharacters(aText, CHARACTERS_TO_ESCAPE, ESCAPE_CODES);
  }

  public static String encodeXML(String aText) {
//#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
    final char[] CHARACTERS_TO_ESCAPE = { '&', '<', '>', '"', '\'',
        '\u0000', '\u0001', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007', '\u0008', '\u0000', '\u000B',
        '\u000C', '\u000E', '\u000F', '\u0010', '\u0011', '\u0012', '\u0013', '\u0014', '\u0015', '\u0016',
        '\u0017', '\u0018', '\u0019', '\u001A', '\u001B', '\u001C', '\u001D', '\u001E'  };
    final String[] ESCAPE_CODES = { "&amp;", "&lt;", "&gt;", "&quot;", "&apos;",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", ""};


    return StringRoutines.replaceStringCharacters(aText, CHARACTERS_TO_ESCAPE, ESCAPE_CODES);
  }
}