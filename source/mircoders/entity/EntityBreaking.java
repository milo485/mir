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

package mircoders.entity;

/**
 * Title: EntityBreaking
 * Description: Maps table "breaking" to Java Object
 * Copyright:    Copyright (c) 2001
 * Company:      Indymedia.de
 * @author /rk
 * @version 1.0
 */


import java.util.Set;

import mir.entity.Entity;
import mir.storage.StorageObject;
import mir.storage.store.StorableObject;
import mir.storage.store.StoreIdentifier;


public class EntityBreaking extends Entity implements StorableObject
{
	public EntityBreaking() {	super();	}
	public EntityBreaking(StorageObject theStorage) {	this();	setStorage(theStorage);	}


    /**
   *  Method:       getStoreIdentifier
   *  Description:  returns unique StoreIdentifer under which the Entity
   *                is Stored. Based upon primary key and tablename.
   *
   *  @return       StoreIdentifier
   */
  public StoreIdentifier getStoreIdentifier() {
    String id = getId();
    if ( id!=null && theStorageObject!= null )
     return new StoreIdentifier(this, id+"@"+theStorageObject.getTableName());
    return null;
  }

  /**
   *  Method:       getNotifyOnReleaseSet()
   *  Description:  returns empty Set, GenericContainer does not implement
   *                dependencies.
   *
   *  @return       null
   */
  public Set getNotifyOnReleaseSet() { return null; }

}