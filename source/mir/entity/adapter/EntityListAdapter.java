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

package mir.entity.adapter;

import java.util.AbstractList;
import java.util.List;
import java.util.Vector;

import mir.entity.EntityBrowser;

public class EntityListAdapter extends AbstractList {
  private int skip;
  private int maximumLength;
  private EntityBrowser browser;
  private boolean exhausted = false;
  private boolean skipped = false;

  private List cache;

  protected EntityListAdapter(EntityBrowser aBrowser, int aSkip, int aMaximumLength) {
    browser = aBrowser;
    skip = aSkip;
    maximumLength = aMaximumLength;
    cache = new Vector();
  }

  protected EntityListAdapter(EntityBrowser aBrowser, int aMaximumLength) {
    this(aBrowser, 0, aMaximumLength);
  }

  protected EntityListAdapter(EntityBrowser aBrowser) {
    this(aBrowser, 0, -1);
  }

  private void skip() {
    int i;

    try {
      if (!skipped) {
        for(i=0; i<skip; i++)
          if (browser.hasNext())
            browser.next();
      }
      skipped=true;
    }
    catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  private void fetchNext() {
    try {
      if (!exhausted) {
        if (browser.hasNext())
          cache.add(browser.next());

        exhausted = !browser.hasNext() || (maximumLength>0 && cache.size()>=maximumLength) ;
      }
    }
    catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }

  }

  private void exhaust() {
    skip();

    while (!exhausted)
      fetchNext();
  }

  private void fetchUntil(int anIndex) {
    skip();

    while (!exhausted && anIndex>=cache.size())
      fetchNext();
  }

  public int size() {
    exhaust();

    return cache.size();
  }

  public Object get(int anIndex) {
    fetchUntil(anIndex);
    return cache.get(anIndex);
  }
}