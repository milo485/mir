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

package mir.storage.store.test;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

import java.util.HashSet;
import java.util.Set;

import mir.storage.store.StorableObject;
import mir.storage.store.StoreContainerType;
import mir.storage.store.StoreIdentifier;

public class EntityC1 implements StorableObject {

	String id;

	public EntityC1(String id) {
		this.id=id;
	}

	public StoreIdentifier getStoreIdentifier() {
		return new StoreIdentifier(this, StoreContainerType.STOC_TYPE_ENTITY,id);
	}

	public Set getNotifyOnReleaseSet() {
    HashSet notifiees = new HashSet();
    // simulating a relation from EntityC1 to EntityC2/Entitylist
    notifiees.add(StoreContainerType.valueOf( EntityC2.class,
                                              StoreContainerType.STOC_TYPE_ENTITYLIST));
    // simulates a relation to EntityC2 with uniqueIdentifier "1"
    notifiees.add(new StoreIdentifier(EntityC2.class,"1"));
    notifiees.add(new StoreIdentifier(EntityC2.class,"18"));
		return (Set)notifiees;
	}
}