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

package mir.storage.store;

/**
 * Title:         Class StoreIdentifier
 * Description:   StoreIdentifier has two functions.
 *                A) StoreIdenfier holds a reference to a @see StorableObject
 *                or B) StoreIdentifier is used too search for a @see StorableObject
 *                in the @see StoreContainer that matches its
 *
 * Copyright:     Copyright (c) 2002
 * Company:       indy
 * @author        rk
 * @version 1.0
 */
import java.util.Iterator;
import java.util.Set;

import mir.entity.EntityList;
import mir.misc.Logfile;

public class StoreIdentifier {

	/** @todo check if invalidating already to avoid deadlocks
	 *  what about concurrency? */

	private static Logfile      storeLog;
  private static ObjectStore  o_store = ObjectStore.getInstance();

	private StoreContainerType  stocType=null;
	private StorableObject      reference=null;
	private String              uniqueIdentifier=null; // id for Entity & sql for EntityList
	private long                timesUsed;
	private boolean             invalidating=false;

	/** @todo initialize logfile  */

	private StoreIdentifier() {}

	public StoreIdentifier(StorableObject reference, int storeType, String uniqueIdentifier) {
    Class theClass;
    if (reference instanceof EntityList)
      theClass=((EntityList)reference).getStorage().getEntityClass();
    else
      theClass=reference.getClass();
    this.uniqueIdentifier=uniqueIdentifier;
		this.stocType = StoreContainerType.valueOf(theClass, storeType);
		this.reference=reference;
	}

  public StoreIdentifier(StorableObject reference, String uniqueIdentifier) {
    this(reference, StoreContainerType.STOC_TYPE_ENTITY, uniqueIdentifier);
  }

  public StoreIdentifier(Class theClass, String uniqueIdentifier) {
    this(theClass, StoreContainerType.STOC_TYPE_ENTITY,uniqueIdentifier);
  }

  public StoreIdentifier(Class theClass, int storeType, String uniqueIdentifier) {
    this.uniqueIdentifier=uniqueIdentifier;
		this.stocType = StoreContainerType.valueOf(theClass, storeType);
  }
	/**
	 *  Method:       ivalidate
	 *  Description:
	 *
	 *  @return
	 */
	public void invalidate() {
    System.out.println("Invalidating: " + toString());
		// avoid deadlock due to propagation.
		if (!invalidating) {
			invalidating=true;
      if ( stocType!=null &&
           stocType.getStocType()==StoreContainerType.STOC_TYPE_ENTITY )
      {
        System.out.println("Propagating invalidation to EntityList for " + toString());
        // we should invalidate related ENTITY_LIST
        StoreContainerType entityListStocType =
            StoreContainerType.valueOf( stocType.getStocClass(),
                                        StoreContainerType.STOC_TYPE_ENTITYLIST );
        o_store.invalidate(entityListStocType);
      }

      // propagate invalidation to Set
			Set set = reference.getNotifyOnReleaseSet();
      if (set!=null) {
        for (Iterator it = set.iterator(); it.hasNext(); ) {
          Object o = it.next();
          if ( o instanceof StoreIdentifier ) {
            System.out.println("Propagating invalidation to StoreIdentifier: " + o.toString());
            // propagate invalidation to a specific StoreIdentifier in cache
            o_store.invalidate((StoreIdentifier)o);
          } else if ( o instanceof StoreContainerType ) {
            System.out.println("Propagating invalidation to StoreContainer: " + o.toString());
            // propagate invalidation to a whole StoreContainer
            o_store.invalidate((StoreContainerType)o);
          }

        }
      }
			release();
		}
	}

	public void release() {
		this.reference=null;
		this.uniqueIdentifier=null;
		this.stocType=null;
	}

	public StorableObject use() {
		timesUsed++;
		return reference;
	}

	/**
	 *  Method equals for comparison between two identifier
	 *
	 *  @return true if yes otherwise false
	 *
	 */
	public boolean equals(Object sid) {
    if ( !(sid instanceof StoreIdentifier) ) return false;
    if ( ((StoreIdentifier)sid).getStoreContainerType()==stocType &&
         ((StoreIdentifier)sid).getUniqueIdentifier().equals(uniqueIdentifier) ) {
      return true;
    }
		return false;
	}

	public StoreContainerType getStoreContainerType() { return stocType; }
	public String getUniqueIdentifier() { return uniqueIdentifier; }
	public boolean hasReference() { return (reference==null) ? false:true; }

	public String toString() {
    StringBuffer id = new StringBuffer(uniqueIdentifier);
    id.append("@storetype: ").append(stocType.toString());
    if (reference != null) id.append(" ("+timesUsed).append(") times used.");
		return id.toString();
	}


}