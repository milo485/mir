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

package mir.misc;

import java.io.IOException;

import com.icl.saxon.PreparedStyleSheet;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.icl.saxon.trax.Transformer;

import java.util.Hashtable;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */


public class XslStyleCache {

  private static Hashtable cache;
  private static XslStyleCache single = new XslStyleCache();


  /**
   * XSLStyleCache constructor comment.
  */
  private XslStyleCache() {
    cache = new Hashtable();
  }

  /**
   * singleton constructor
   */
  private static synchronized XslStyleCache getInstance() {
    return single;
  }


  /**
   * Clear Cache
   */
  public static void clear() {
    cache.clear();
  }

  /**
   * This method was created in VisualAge.
   * @return
   * @param key java.lang.String
   */
  public static PreparedStyleSheet getPreparedStyleSheet( String key )
    throws SAXException {

    PreparedStyleSheet styleSheet = (PreparedStyleSheet)single.cache.get( key );
    try {
      if ( styleSheet == null ) {
        styleSheet = new PreparedStyleSheet();
        styleSheet.prepare( InputSourceResolver.resolve( key ) );
        single.cache.put( key, styleSheet );
      }
    } catch ( IOException ex ) {
      throw new SAXException( "tunneld IOExcpetion:" + ex.getMessage() );
    }

    return styleSheet;
  }

  /**
   * This method was created in VisualAge.
   * @return
   * @param key java.lang.String
   */
  public static Transformer getTransformer( String key ){

    PreparedStyleSheet styleSheet = (PreparedStyleSheet)single.cache.get( key );
    try {
      if ( styleSheet == null ) {
        styleSheet = new PreparedStyleSheet();
        styleSheet.prepare( InputSourceResolver.resolve( key ) );
        single.cache.put( key, styleSheet );
      }
    } catch ( IOException ex ) {
      //throw new SAXException( "tunneld IOExcpetion:" + ex.getMessage() );
    } catch (SAXException ex) {}

    return styleSheet.newTransformer();
  }

}