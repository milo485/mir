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

package mir.storage.store;

/**
 * Title:         StoreContainer
 *
 * Description:   This is the bucket object for one type of StorableObjects,
 *                mainy a linked list of StoreIdenfiers. On use or creation
 *                an object stored in StoreIdentifier is put to head of the
 *                list. if maximum size of the list is reached, the
 *                StoreIdentifier at the end of the list is released.
 *
 * Copyright:     Copyright (c) 2002
 * Company:       indy
 * @author        //rk
 * @version 1.0
 */

import java.util.LinkedList;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import mir.log.LoggerWrapper;
import mir.misc.StringUtil;

public class StoreContainer {
  private final static int DEFAULT_SIZE = 10;
  private static int uniqueCounter = 10000;

  private LinkedList container;
  private StoreContainerType stocType;
  private int maxSize = DEFAULT_SIZE, uniqueId;
  private int addCount = 0, removeCount = 0, storeOutCount;
  private int hitCount = 0, missCount = 0;
  private static ObjectStore o_store = ObjectStore.getInstance();

  protected LoggerWrapper logger = new LoggerWrapper("Database.ObjectStore");

  // avoid construction without parameters
  private StoreContainer() {};


  public StoreContainer(StoreContainerType stoc_type) {
    this.uniqueId = ++uniqueCounter;
    this.stocType = stoc_type;
    this.container = new LinkedList();
    int defaultSize = stoc_type.getDefaultSize();
    String confProperty = stoc_type.getConfPrefix() + ".DefaultSize";
    String confedSize = o_store.getConfProperty(confProperty);
    if (confedSize != null) {
      this.maxSize = StringUtil.parseInt(confedSize, defaultSize);
    }
  }

  public StoreContainer(StoreContainerType stoc_type, int maxSize) {
    this();
    this.maxSize = maxSize;
  }

  public synchronized StorableObject use(StoreIdentifier sid) {
    int hit = container.indexOf(sid);
    if (hit >= 0) {
      StoreIdentifier hitSid = (StoreIdentifier) container.get(hit);
      if (hitSid != null) {
        hitCount++;
        return hitSid.use();
      }
    }
    missCount++;
    return null;
  }

  public boolean has(StoreIdentifier sid) {
    return container.contains(sid);
  }

  public void add(StoreIdentifier sid) {
    if (sid != null && sid.hasReference()) {
      if (has(sid)) {
        moveToHead(sid);
        logger.debug("OBJECTStore: tried to add sid " + sid.toString() + " that was already in store.");
      }
      else {
        container.addFirst(sid);
        shrinkIfNecessary();
        addCount++;
      }
    }
  }

  /**
   *  Method:       invalidate(StorableObject sto)
   *  Description:  finds @see StorableObject, propagates invalidation to
   *                @see StoreIdentifier and removes StoreIdentifier from
   *                list.
   */
  public synchronized void invalidate(StoreIdentifier search_sid) {
    if (search_sid != null) {
      int hit = container.indexOf(search_sid);
      if (hit >= 0) {
        StoreIdentifier sid = (StoreIdentifier) container.get(hit);
        container.remove(sid);
        sid.invalidate();
        removeCount++;
      }
    }
  }

  public synchronized void invalidate() {
    StoreIdentifier sid;
    while (container.size() > 0) {
      sid = (StoreIdentifier) container.getLast();
      container.removeLast();
      sid.invalidate();
    }
  }

  /**
   *  Method:       setSize
   *  Description:  readjusts StoreContainer size to value.
   *
   */
  public void setSize(int size) {
    if (size < 0)
      return;
    shrinkToSize(size);
    this.maxSize = size;
  }

  private void shrinkIfNecessary() {
    shrinkToSize(maxSize);
  }

  private void shrinkToSize(int size) {
    if (size < container.size()) {
      // shrink
      while (size < container.size()) {
        StoreIdentifier sid = (StoreIdentifier) container.getLast();
        container.remove(sid);
        sid.release();
        storeOutCount++;
      }
    }
  }

  private synchronized void moveToHead(StoreIdentifier sid) {
    if (sid != null) {
      container.remove(sid);
      container.addFirst(sid);
    }
  }

  /**
   *  Method:       toString()
   *  Description:  gives out statistical Information, viewable via
   *                @see ServletStoreInfo.
   *
   *  @return       String
   */
  public String toString() {
    return toHtml(null);
  }

  public String toHtml(HttpServletRequest req) {
    boolean showingContent = false;
    float hitRatio = 0;
    long divisor = hitCount + missCount;
    if (divisor > 0)
      hitRatio = (float) hitCount / (float) divisor;
    hitRatio *= 100;

    StringBuffer sb = new StringBuffer("StoreContainer id: ");
    sb.append(uniqueId).append(" for ");
    sb.append(stocType.toString());
    if (req != null) {
      String show = req.getParameter("stoc_show");
      if (show != null && show.equals("" + uniqueId)) {
        // show all entries in container
        sb.append(" [<b>showing</b>]");
        showingContent = true;
      }
      else
        sb.append(" [<a href=\"?stoc_show=" + uniqueId + "\">show</a>]");
    }
    sb.append("\n  [current/maximum size: ");
    sb.append(container.size()).append("/").append(maxSize);
    sb.append("]\n  [added/stored out/removed: ").append(addCount).append("/");
    sb.append(storeOutCount).append("/").append(removeCount).append(
        "]\n  [hit/miss/ratio: ");
    sb.append(hitCount).append("/").append(missCount).append("/");
    sb.append(hitRatio).append("%]\n");

    if (showingContent) {
      sb.append("  <b>Container contains following references:</b>\n  ");
      ListIterator it = container.listIterator();
      while (it.hasNext()) {
        StoreIdentifier sid = (StoreIdentifier) it.next();
        sb.append(sid.toString()).append("\n  ");
      }
      sb.append("<b>End of List</b>\n\n");

    }

    return sb.toString();
  }

}