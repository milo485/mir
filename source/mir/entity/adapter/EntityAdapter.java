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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mir.entity.Entity;
import mir.util.CachingRewindableIterator;

public class EntityAdapter implements Map {
  private Entity entity;
  private EntityAdapterDefinition definition;
  private Map calculatedFieldsCache;
  private EntityAdapterModel model;

  public EntityAdapter(Entity anEntity, EntityAdapterDefinition aDefinition, EntityAdapterModel aModel) {
    entity = anEntity;
    definition = aDefinition;
    calculatedFieldsCache = new HashMap();
    model = aModel;
  }

  public boolean containsKey(Object aKey) {
    try {
      if (aKey instanceof String)
        return     entity.hasValueForField((String) aKey)
                || definition.hasCalculatedField((String) aKey)
                || entity.getFields().contains(aKey);
    }
    catch (Throwable t) {
    }

    return false;
  }

  public boolean equals(Object anObject) {
    return        anObject instanceof EntityAdapter
           && ((EntityAdapter) anObject).entity.equals(entity);
  }

  public int hashCode() {
    return entity.hashCode();
  }

  public Entity getEntity() {
    return entity;
  }

  public EntityAdapterModel getModel() {
    return model;
  }

  public Object get(Object aKey) {
    Object result;

    if (calculatedFieldsCache.containsKey(aKey)) {
      return calculatedFieldsCache.get(aKey);
    }
    else if (aKey instanceof String && definition.hasCalculatedField((String) aKey)) {
      result = definition.getCalculatedField((String) aKey).getValue(this);
      calculatedFieldsCache.put(aKey, result);

      return result;
    }
    else if (aKey instanceof String) {
      return entity.getValue((String) aKey);
    }
    else {
      return null;
    }
  }

  public boolean isEmpty() {
    return false;
  }

  public Set keySet() {
    throw new UnsupportedOperationException("EntityAdapter.keySet()");
  }

  public Object put(Object aKey, Object value) {
    throw new UnsupportedOperationException("EntityAdapter.put()");
  }

  public void putAll(Map t) {
    throw new UnsupportedOperationException("EntityAdapter.putAll()");
  }

  public Object remove(Object aKey) {
    throw new UnsupportedOperationException("EntityAdapter.remove()");
  }

  public int size() {
    throw new UnsupportedOperationException("EntityAdapter.size()");
  }

  public Collection values() {
    throw new UnsupportedOperationException("EntityAdapter.values()");
  }

  public void clear() {
    throw new UnsupportedOperationException("EntityAdapter.clear()");
  }

  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException("EntityAdapter.containsValue()");
  }

  public Set entrySet() {
    throw new UnsupportedOperationException("EntityAdapter.entrySet()");
  }

  public Object getRelation(String aWhereClause, String anOrderByClause, String aDefinition) {
    try {
      return
          new CachingRewindableIterator(
            new EntityIteratorAdapter(
                aWhereClause, anOrderByClause, -1, getModel(), aDefinition));
    }
    catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  public Object getToOneRelation(String aWhereClause, String anOrderByClause, String aDefinition) {
    try {
      Iterator i = new EntityIteratorAdapter(aWhereClause, anOrderByClause, -1, getModel(), aDefinition);

      if (i.hasNext())
        return i.next();
      else
        return null;
    }
    catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }
}