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
package mir.entity;

import mir.storage.StorageObject;
import mir.storage.StorageObjectExc;
import mir.storage.StorageObjectFailure;

public class EntityRelation {

  public String         fromId;
  public String         toId;
  public StorageObject  storage;
  public int            type;

  public final static int TO_ONE    =  1;
  public final static int TO_MANY   =  2;


  /**
   *  Kontruktor fuer EntityRelation
   *  @param fromId ist der Feldname in der ausgehenden Tabelle fuer die Relation
   *  @param toId ist der Feldname in der Zieltablle
   *  @param storage ist das StorageObject, ueber das der Zugriff auf die abhaengige
   *         Tabelle erfolgt.
   *  @param type ist der Typ der EntityRelation (TO_ONE oder TO_MANY)
   */

  public EntityRelation(String fromId, String toId, StorageObject storage, int type) {
      this.fromId = fromId;
      this.toId = toId;
      this.storage = storage;
      this.type = type;
  }

  /**
   *   @return Liefert eine abhaengige Entity mit den verknuepften
   *           Entities, wenn es sich um eine TO_ONE Beziehung handelt, ansonsten
   *           null.
   */

  public Entity getOne(Entity entity) throws StorageObjectExc {
    if (type==TO_ONE) {
      return storage.selectById(entity.getValue(fromId));
    }
    else return null;
  }

  /**
   *   @return Liefert eine freemarker.template.SimpleList mit den verknuepften
   *           Entities, wenn es sich um eine TO_MANY Liste handelt, ansonsten
   *           null.
   */

  public EntityList getMany(Entity entity) throws StorageObjectFailure{
    if (type==TO_MANY) {
      return storage.selectByFieldValue(toId, entity.getValue(fromId));
    }
    else return null;
  }

  /**
   *   @return Liefert eine freemarker.template.SimpleList mit den verknuepften
   *           Entities, wenn es sich um eine TO_MANY Liste handelt, ansonsten
   *           null.
   */

  public EntityList getMany(Entity entity, String order) throws StorageObjectFailure{
    if (type==TO_MANY) {
      return storage.selectByWhereClause(toId+"="+entity.getValue(fromId), order,-1);
    }
    else return null;
  }

  /**
   *   @return Liefert eine freemarker.template.SimpleList mit den verknuepften
   *           Entities, wenn es sich um eine TO_MANY Liste handelt, ansonsten
   *           null.
   */

  public EntityList getMany(Entity entity, String order, String whereClause) throws StorageObjectFailure{
    if (type==TO_MANY) {
      return storage.selectByWhereClause(toId + "=" + entity.getValue(fromId) + " and " + whereClause, order,-1);
    }
    else return null;
  }

  /**
   *   @return The reference name of the related table.
   */

  public String getName() {
    return "to" + storage.getTableName();
  }


}
