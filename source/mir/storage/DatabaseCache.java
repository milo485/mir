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

package mir.storage;

import java.util.ArrayList;

public class DatabaseCache {
  private final ArrayList _cache;
  private int _counter;
  private final int _max;
  
  public DatabaseCache(int i_max){
    _counter = 0;
    _max = i_max;
    _cache = new ArrayList(_max);
  }
  
  public DatabaseCache(){
    _counter = -1;
    _max = 100;
    _cache = new ArrayList(_max);
  }
  
  public synchronized void put(String key, Object value){
    if(_counter >=_max){
      _cache.remove(0);
      _cache.trimToSize();
      _counter --;
      System.out.println("put: remove " + _counter);
    }
    _cache.add(new Entry(key,value));
    _counter ++;
    System.out.println("put: put " + _counter);
  }
  
  public synchronized void clear(){
    _cache.clear();
  }
      
  public int containsKey(String key){
    for(int i = 0; i<_cache.size(); i++){
      if( _cache.get(i)!=null && ((Entry)_cache.get(i)).getKey().equals(key) )
        return i;
    }
    return -1;
  }
  
  public int containsValue(Object o){
    for(int i = 0; i<_cache.size(); i++){
      if( _cache.get(i)!=null && ((Entry)_cache.get(i)).getValue().equals(o) )
        return i;
    }
    return -1;
  }
  
  public Object get(String key){
    for(int i = 0; i<_cache.size(); i++){
      if( _cache.get(i) != null &&
        ((Entry)_cache.get(i)).getKey(key) != null &&
        ((Entry)_cache.get(i)).getKey(key).equals(key) ) {
        System.out.println("test2: "+((Entry)_cache.get(i)).getKey(key));
        return ((Entry)_cache.get(i)).getValue();
      }
    }
    return null;
  }
  
  public synchronized boolean remove(String key){
    int i = containsKey(key);
    if(i==-1){
      return false;
    }
    _cache.remove(i);
    _cache.trimToSize();
    _counter --;
    return true;
  }
  
  public int size(){
    return _counter;
  }
  
  private class Entry {
    private String _key;
    private Object _value;
    
    public Entry(String i_key, Object i_value){
      _key = i_key;
      _value = i_value;
    }
        
    public void put(String i_key, Object i_value){
      _key = i_key;
      _value = i_value;
    }
    
    public Object getValue(String i_key){
      if(i_key.equals(_key)){
        return _value;
      } else {
        return null;
      }
    }
    
    public Object getValue(){
      return _value;
    }
    
    public String getKey(Object i_o){
      if(i_o.equals(_value)){
        return _key;
      } else {
        return null;
      }
    }

    public String getKey(){
        return _key;
    }
  }//Entry

}


