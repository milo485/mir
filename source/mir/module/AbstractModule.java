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

package  mir.module;

import java.sql.SQLException;
import java.util.HashMap;

import freemarker.template.SimpleHash;

import mir.entity.Entity;
import mir.entity.EntityList;
import mir.storage.StorageObject;
import mir.storage.StorageObjectExc;
import mir.storage.StorageObjectFailure;


/**
 * This class provides the base functionality for the derived Module-Classes.
 * These classes should provide methods to make more or less complex actions
 * on Database and Entity classes. The modules are used by ServletModules.
 * Future possibility could be access via Applications.
 *
 * Abstrakte Klasse, von denen die Modules die Basisfunktionalit?t erben.
 * Die Moduleschicht dient dazu, Funktionalitaeten zur Verf?gung zu stellen,
 * die von mehreren ServletModulen verwendet werden.
 *
 */

public class AbstractModule {
  protected StorageObject theStorage;

  public void setStorage(StorageObject storage) {
    this.theStorage = storage;
  }

  /**
   * Liefert das Standard-StorageObject zur?ck, mit dem das Module assoziiert ist.
   * @return Standard-StorageObject
   */
  public StorageObject getStorageObject () {
    return theStorage;
  }

  /**
   *   Holt eine Entity anhand der Id via StorageObject
   *   @param String der Entity
   *   @return Entity
   */
  public Entity getById (String id) throws ModuleException {
    try {
      if (theStorage == null)
        throw  new ModuleException("No StorageObject set!");
      Entity entity = (Entity)theStorage.selectById(id);
      if (entity == null)
        throw new ModuleException("No object for id = " + id);
      else return entity;
    }
    catch (StorageObjectExc e){
      throw new ModuleException(e.toString());
    }
  }

  /**
   *   Holt eine EntityListe anhand des WhereClause via StorageObject
   *   @param String whereclause
   *   @param offset - ab welchem Datensatz die gematchten Entities zurueckgeliefert werden
   *   @return EntityList Liste der gematchten Datens?tze
   */
  public EntityList getByWhereClause (String whereClause, int offset) throws ModuleException {
    try {
      if (theStorage == null)
        throw  new ModuleException("Kein StorageObject gesetzt");
      return theStorage.selectByWhereClause(whereClause, offset);
    }
    catch (StorageObjectFailure e){
      throw new ModuleException(e.toString());
    }
  }

  /**
   *   Holt eine EntityListe anhand des WhereClause aus dem StorageObject
   *   @param String where WhereClause
   *   @param String order Sortierreihenfolge
   *   @param offset - ab welchem Datensatz die gematchten Entities zurueckgeliefert werden
   *   @return EntityList Liste der gematchten Datens?tze
   */
  public EntityList getByWhereClause (String where, String order, int offset) throws ModuleException {
    try {
      if (theStorage==null) throw new ModuleException("Kein StorageObject gesetzt");
      return theStorage.selectByWhereClause(where, order, offset);
    }
    catch (StorageObjectFailure e){
      throw new ModuleException(e.toString());
    }
  }
  /**
   *   Executes a where clause on the StorageObject with order criteria
   *   fetching from offset the number of limit objects
   *
   *   @param String where
   *   @param String order
   *   @param int offset
   *   @param int limit
   *   @return EntityList
   */

  public EntityList getByWhereClause(String where, String order, int offset, int limit) throws ModuleException
  {
    try {
      if (theStorage==null) throw new ModuleException("StorageObject not set!");
      return theStorage.selectByWhereClause(where, order, offset, limit);
    }
    catch (StorageObjectFailure e){
      throw new ModuleException(e.toString());
    }
  }

  /**
   *   Holt eine EntityListe anhand des Wertes aValue von Feld aField aus dem StorageObject
   *   @param String aField - Feldname im StorageObject
   *   @param String aValue - Wert in Feld im StorageObject
   *   @param offset - ab welchem Datensatz die gematchten Entities zurueckgeliefert werden
   *   @return EntityList Liste der gematchten Datens?tze
   */
  public EntityList getByFieldValue (String aField, String aValue, int offset) throws ModuleException {
    String whereClause;
    whereClause = aField + " like '%" + aValue + "%'";
    return getByWhereClause(whereClause, offset);
  }

  /**
   * Standardfunktion, um einen Datensatz via StorageObject einzuf?gen
   * @param theValues Hash mit Spalte/Wert-Paaren
   * @return Id des eingef?gten Objekts
   * @exception ModuleException
   */
  public String add (HashMap theValues) throws ModuleException {
    try {
      Entity theEntity = (Entity)theStorage.getEntityClass().newInstance();
      theEntity.setStorage(theStorage);
      theEntity.setValues(theValues);
      return theEntity.insert();
    } catch (Exception e) {
      throw new ModuleException(e.toString());
    }
  }

  /**
   * Standardfunktion, um einen Datensatz via StorageObject zu aktualisieren
   * @param theValues Hash mit Spalte/Wert-Paaren
   * @return Id des eingef?gten Objekts
   * @exception ModuleException
   */
  public String set (HashMap theValues) throws ModuleException {
    try {
      Entity theEntity = theStorage.selectById((String)theValues.get("id"));
      if (theEntity == null)
        throw new ModuleException("Kein Objekt mit id in Datenbank id: " + theValues.get("id"));
      theEntity.setValues(theValues);
      theEntity.update();
      return theEntity.getId();
    }
    catch (StorageObjectExc e){
      throw new ModuleException(e.toString());
    }
  }

  /**
   * Deletes a record using an id
   * @param idParam
   * @exception ModuleException
   */
  public void deleteById (String idParam) throws ModuleException {
    try {
      theStorage.delete(idParam);
    } catch (StorageObjectFailure e){
      throw new ModuleException(e.toString());
    }
  }

  /**
   * Liefert den Lookuptable aller Objekte des StorageObjects
   * @return freemarker.template.SimpleHash
   */
  public SimpleHash getHashData() {
    return theStorage.getHashData();
  }

  /**
   * returns the number of rows
   */
  public int getSize(String where)
      throws SQLException,StorageObjectFailure {
    return theStorage.getSize(where);
  }

}
