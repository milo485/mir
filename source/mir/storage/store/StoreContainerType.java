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
 * Title:         StoreContainerType
 *
 * Description:   StoreContainerTypes are uniqe Objects and are generated
 *                via @see valueOf(Class stocClass, int stocType).
 *                For every combination of stocClass and stocType there is
 *                only one Object instantiated.
 *
 * Copyright:     Copyright (c) 2002
 * Company:       indy
 *
 * @author        rk
 * @version 1.0
 */

import java.util.HashMap;
import mir.misc.*;

public class StoreContainerType {

	public final static int     STOC_TYPE_UNKNOWN=-1;
	public final static int     STOC_TYPE_ENTITY=0;
	public final static int     STOC_TYPE_ENTITYLIST=1;
  public final static int     STOC_TYPE_MAX=STOC_TYPE_ENTITYLIST;

	private static HashMap[]    uniqueTypes=new HashMap[STOC_TYPE_MAX+1];
  private static ObjectStore  o_store=ObjectStore.getInstance();
	private static Logfile      storeLog;
	private Class               stocClass=null;
	private int                 stocType=STOC_TYPE_UNKNOWN;

  static {
    uniqueTypes[STOC_TYPE_ENTITY]= new HashMap();
    uniqueTypes[STOC_TYPE_ENTITYLIST]=new HashMap();
  }

	private StoreContainerType() {}

	private StoreContainerType(Class stocClass, int stocType) {
		this.stocClass=stocClass;
		this.stocType=stocType;
	}

	public static StoreContainerType valueOf(Class stoc_class, int stoc_type) {
		StoreContainerType returnStocType=null;
    if (stoc_type>=0 && stoc_type <= STOC_TYPE_MAX) {
      HashMap current = uniqueTypes[stoc_type];
      if ( current.containsKey(stoc_class) )
			  returnStocType=(StoreContainerType)current.get(stoc_class);
		  else {
			  returnStocType=new StoreContainerType(stoc_class,stoc_type);
			  current.put(stoc_class,returnStocType);
		  }
    }
		return returnStocType;
	}

  public int getStocType() { return stocType; }
  public Class getStocClass() { return stocClass; }
  public String getConfPrefix() {
    return StoreUtil.getPropNameFor(stocClass)+"."+stringForStoreType(stocType);
  }
  public int getDefaultSize() {
    String confProperty= "StoreContainer."+stringForStoreType(stocType)+".DefaultSize";
    return
      StringUtil.parseInt( o_store.getConfProperty(confProperty),10 );
  }

	public String toString() {
		StringBuffer sb = new StringBuffer(this.stocClass.toString());
		sb.append("@").append(stringForStoreType(stocType));
		return sb.toString();
	}

	private static String stringForStoreType(int stocType) {
		switch(stocType) {
			case STOC_TYPE_ENTITY: return "Entity";
			case STOC_TYPE_ENTITYLIST: return "EntityList";
			default: return "UNKNOWN";
		}
	}
}