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

package mir.rss;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class RSSAggregator {
  private String orderBy;
  private boolean orderReversed;
  private int capacity;
  private List items;
  private String selectionField;
  private Object selectionValue;

  public RSSAggregator(int aCapacity, String anOrderBy, boolean anOrderReversed, String aSelectionField, Object aSelectionValue) {
    orderBy = anOrderBy;
    orderReversed = anOrderReversed;
    capacity = aCapacity;
    items = new Vector();
    selectionValue = aSelectionValue;
    selectionField = aSelectionField;
  }

  public void appendItems(List anItems) {
    Iterator i = anItems.iterator();

    while (i.hasNext()) {
      Object item = i.next();

      if (item instanceof RDFResource)
        appendItem((RDFResource) item);
    }
  }

  public int compareItems(RDFResource anItem1, RDFResource anItem2) {
    int result = 0;

    Object value1 = anItem1.get(orderBy);
    Object value2 = anItem2.get(orderBy);

    if (value1 instanceof Comparable && value2 instanceof Comparable) {
      result = ((Comparable) value1).compareTo(value2);

      if (orderReversed)
        result = -result;
    }

    return result;
  }

  public void appendItem(RDFResource anItem) {
    if (selectionField!=null && selectionValue!=null) {
      if (!selectionValue.equals(anItem.get(selectionField)))
        return;
    }

    int i = items.size();

    if (orderBy!=null) {
      if (anItem.get(orderBy) == null)
        return;

      while (i > 0 && compareItems(anItem, (RDFResource) items.get(i-1)) < 0) {
        i--;
      }
    }

    items.add(i, anItem);
    while (items.size()>0 && items.size()>capacity)
      items.remove(items.size()-1);
  }

  public List getItems() {
    return items;
  }
}