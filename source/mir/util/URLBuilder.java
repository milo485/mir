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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class URLBuilder {
  private Map keyValues;
  private String base;

  public URLBuilder(String aBase) {
    keyValues = new HashMap();
    base = aBase;
  }

  public URLBuilder() {
    this("");
  }

  public void setValue(String aKey, String aValue) {
    if (aValue!=null)
      keyValues.put(aKey, aValue);
    else
      deleteKey(aKey);
  }

  public void setValue(String aKey, int aValue) {
    keyValues.put(aKey, Integer.toString(aValue));
  }

  public void deleteKey(String aKey) {
    keyValues.remove(aKey);
  }

  public String getQuery() {
    StringBuffer query = new StringBuffer();
    Iterator i;

    i = keyValues.entrySet().iterator();

    while(i.hasNext()) {
      Map.Entry entry = (Map.Entry) i.next();

      query.append(HTMLRoutines.encodeURL((String) entry.getKey()));
      query.append("=");
      query.append(HTMLRoutines.encodeURL((String) entry.getValue()));

      if (i.hasNext())
        query.append("&");
    }

    return query.toString();
  }

  public String getUrl() {
    return base + "?" + getQuery();
  }
}