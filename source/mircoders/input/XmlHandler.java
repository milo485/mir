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
/**
 * Title:        Indy
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      indymedia.de
 * @author idfx
 * @version 1.0
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */



package mircoders.input;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


/**
 * put your documentation comment here
 */
public class XmlHandler extends DefaultHandler {
  static HashMap valueHash = new HashMap();

  /**
   * parses every starting XML-Element
   * @param uri
   * @param name
   * @param qname
   * @param atts
   */
  public void startElement (String uri, String name, String qname, Attributes atts) {
    HashMap values = new HashMap();
    if (name.equals("content")) {
      //table content
      valueHash.put("table", "Content");
      valueHash.put("values", values);
      // content-articles should be published immediatly
      ((HashMap)valueHash.get("values")).put("is_published", "1");
    }
    else if (name.equals("breaking")) {
      //table content
      valueHash.put("table", "Breaking");
      valueHash.put("values", values);
    }
    else {
      //System.out.println(name + ": " + atts.getValue("value"));
      ((HashMap)valueHash.get("values")).put(name, atts.getValue("value"));
    }
  }

  /**
   * Returns the HashMap filled with the Values of the parsed XML-File
   * @return
   */
  public static HashMap returnHash () {
    return  valueHash;
  }
}



