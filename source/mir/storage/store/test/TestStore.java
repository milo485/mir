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
package mir.storage.store.test;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

import mir.storage.store.ObjectStore;
import mir.storage.store.StorableObject;
import mir.storage.store.StoreIdentifier;

public class TestStore {

        private static ObjectStore o_store = ObjectStore.getInstance();

        public TestStore() {

        }

        public static void main(String[] args) {
                long startTime = System.currentTimeMillis();
                System.out.println("Starting testrun on ObjectStore...");
                TestStore testStore1 = new TestStore();
                testStore1.startTest();
                System.out.println("Finished testrun on ObjectStore. ("
                                + (System.currentTimeMillis() - startTime) + " ms)");
        }

        public void startTest() {

                EntityC1 c1 = new EntityC1("1");
                o_store.add(c1.getStoreIdentifier());
                EntityC1 c12 = new EntityC1("2");
                o_store.add(c12.getStoreIdentifier());
    o_store.add(c12.getStoreIdentifier()); // should not be added as it's there already

    EntityC2 c2;
    for (int i=0; i<20; i++) {
      c2 = new EntityC2(""+i);
      o_store.add(c2.getStoreIdentifier());
    } // should contain only 10

    // test cycle: search in store

    StorableObject reference; StoreIdentifier search_sid;

    // search for EntityC1
    search_sid=new StoreIdentifier(EntityC1.class,"1");
    reference=o_store.use(search_sid);
    if (reference==null)
      System.out.println("--- should have found" + search_sid.toString());

    search_sid=new StoreIdentifier(EntityC1.class,"A");
    reference=o_store.use(search_sid);
    if (reference!=null)
      System.out.println("--- should not have found" + search_sid.toString());

    search_sid=new StoreIdentifier(EntityC3.class,"1");
    reference=o_store.use(search_sid);
    if (reference!=null)
      System.out.println("--- should not have found" + search_sid.toString());

    // test cycle: invalidation */
    search_sid=new StoreIdentifier(EntityC1.class,"1");
    o_store.invalidate(search_sid);

                System.out.println(o_store.toString());
    /** @todo compare values of store and state failed if values are not
     *  right*/



        }
}