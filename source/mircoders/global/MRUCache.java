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

package mircoders.global;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.log.LoggerWrapper;

import mircoders.global.CacheKey;

public class MRUCache {
  private Map cache;
  private LinkedList mruList;
  private int cacheMaxItems;

  private MirPropertiesConfiguration configuration;
  private LoggerWrapper logger;
  


  public MRUCache() {
    logger = new LoggerWrapper("Global.MRUCache");
    try {
      configuration = MirPropertiesConfiguration.instance();
	}
    catch (PropertiesConfigExc e) {
      throw new RuntimeException("Can't get configuration: " + e.getMessage());
    }
    cacheMaxItems=Integer.parseInt(configuration.getString("Global.Cache.Items"));
    cache = new HashMap();
    mruList= new LinkedList();
    
  }

  /**
   * Checks if the cache has an object with the specified key  
   */

  public boolean hasObject(CacheKey aCacheKey) {
    synchronized (cache) {
      logger.info("MRUCache was this big : "+ mruList.size());
      return cache.containsKey(aCacheKey);
    }
  }
  
  /**
   * Stores an object in the cache by placing it at the top of the
   * list If the object is in the cache, it promotes it to the top of
   * the list.  If the object is not in the cache, it adds it to the
   * top of the list, and then checks the max size of the cache versus
   * the new size to see if it needs to remove the last element from
   * the cache.
   */

  public void storeObject(CacheKey aCacheKey,Object data) {
    synchronized (cache) {
      if (! hasObject(aCacheKey)){
	// add to the cache
	cache.put(aCacheKey,data);
	if (mruList.size() >= cacheMaxItems){
	  removeObject((CacheKey) mruList.getLast());
	}
      }
      mruList.remove(aCacheKey);
      mruList.addFirst(aCacheKey);
    }
  }  
  
  public void removeObject(CacheKey aCacheKey){
    synchronized (cache) {
      mruList.remove(aCacheKey);
      cache.remove(aCacheKey);
    }
  }
  
  /**
   * Moves requested item to front of cache
   */

  public Object getObject(CacheKey aCacheKey){
    synchronized (cache) {
      mruList.remove(aCacheKey);
      mruList.addFirst(aCacheKey);
      return cache.get(aCacheKey);
    }
  }
}



