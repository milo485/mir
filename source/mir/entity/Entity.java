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
package  mir.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.log.LoggerWrapper;
import mir.storage.StorageObject;
import mir.storage.StorageObjectExc;
import mir.storage.StorageObjectFailure;

/**
 * Base class the entities are derived from. Provides base functionality of
 * an entity. Entities are used to represent rows of a database table.<p>
 *
 * @version $Id: Entity.java,v 1.21.2.5 2003/10/23 14:55:28 rk Exp $
 * @author rk
 *
 */

public class Entity
{
  protected static MirPropertiesConfiguration configuration;

//  protected Map theValuesHash; // tablekey / value
  protected Map values;
  protected StorageObject theStorageObject;
  protected List streamedInput = null;
  protected LoggerWrapper logger;

  static {
    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (PropertiesConfigExc e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public Entity() {
    logger = new LoggerWrapper("Entity");

    values = new HashMap();
  }

  /**
   * Constructor
   * @param StorageObject The StorageObject of the Entity.
   */

  public Entity(StorageObject StorageObject) {
    this();

    setStorage(StorageObject);
  }

  /**
   *
   * @param storage
   */

  public void setStorage(StorageObject storage) {
    this.theStorageObject = storage;
  }

  /**
   * Sets the values of the Entity. (Only to be called by the Storage Object)
   *
   * @param aMap Map containing the new values of the Entity
   */

  public void setValues(Map aMap) {
    if (aMap!=null) {
      Iterator i = aMap.entrySet().iterator();
      synchronized(this) {
        while (i.hasNext()) {
          Map.Entry entry = (Map.Entry) i.next();

          setValueForProperty( (String) entry.getKey(), (String) entry.getValue());
        }
      }
    }
  }

  /**
   * Returns the primary key of the Entity.
   * @return String Id
   */
  public String getId() {
    return (String) getValue(theStorageObject.getIdName());
  }

  /**
   * Defines the primary key of the Entity (only to be set by the StorageObject)
   * @param id
   */
  public void setId(String id) {
    setValueForProperty(theStorageObject.getIdName(), id);
  }

  /**
   * Returns the value of a field by field name.
   * @param field The name of the field
   * @return value of the field
   */
  public String getValue(String field) {
    String returnValue = null;

    if (field != null) {
      returnValue = (String) values.get(field);
    }
    return returnValue;
  }

  public boolean hasValueForField(String field) {
    return values.containsKey(field);
  }

  /**
   * Insers Entity into the database via StorageObject
   * @return Primary Key of the Entity
   * @exception StorageObjectException
   */
  public String insert() throws StorageObjectExc {
    logger.debug("Entity: trying to insert ...");

    if (theStorageObject != null) {
      return theStorageObject.insert(this);
    }
    else
      throw new StorageObjectExc("theStorageObject == null!");
  }

  /**
   * Saves changes of this Entity to the database
   * @exception StorageObjectException
   */
  public void update() throws StorageObjectFailure {
    theStorageObject.update(this);
  }

  /**
   * Sets the value for a field. Issues a log message if the field name
   * supplied was not found in the Entity.
   * @param theProp The field name whose value has to be set
   * @param theValue The new value of the field
   * @exception StorageObjectException
   */
  public void setValueForProperty(String theProp, String theValue) throws StorageObjectFailure {
    try {
      if (isField(theProp))
        values.put(theProp, theValue);
      else {
        logger.warn("Entity.setValueForProperty: Property not found: " + theProp + " (" + theValue + ")");
      }
    }
    catch (Throwable t) {
      logger.error("Entity.setValueForProperty: " + t.toString());
      t.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

      throw new StorageObjectFailure(t);
    }
  }

  /**
   * Returns the field names of the Entity as ArrayListe.
   * @return ArrayList with field names
       * @exception StorageObjectException is throuwn if database access was impossible
   */
  public List getFields() throws StorageObjectFailure {
    return theStorageObject.getFields();
  }

  /**
   * Returns an int[] with the types of the fields
   * @return int[] that contains the types of the fields
   * @exception StorageObjectException
   */
  public int[] getTypes() throws StorageObjectFailure {
    return theStorageObject.getTypes();
  }

  /**
   * Returns an ArrayList with field names
   * @return List with field names
   * @exception StorageObjectException
   */
  public List getLabels() throws StorageObjectFailure {
    return theStorageObject.getLabels();
  }


  /**
   * Returns an ArrayList with all database fields that can
   * be evaluated as streamedInput.
   * Could be automated by the types (blob, etc.)
   * Until now to be created manually in the inheriting class
   *
   *  Liefert einen ArrayList mit allen Datenbankfeldern, die
   *  als streamedInput ausgelesen werden muessen.
   *  Waere automatisierbar ueber die types (blob, etc.)
   *  Bisher manuell anzulegen in der erbenden Klasse
   */

  public List streamedInput() {
    return streamedInput;
  }

  /** Returns whether fieldName is a valid field name of this Entity.
   * @param fieldName
   * @return true in case fieldName is a field name, else false.
   * @exception StorageObjectException
   */
  public boolean isField(String fieldName) throws StorageObjectFailure {
    return theStorageObject.getFields().contains(fieldName);
  }

  protected void throwStorageObjectFailure(Throwable e, String wo) throws StorageObjectFailure {
    logger.error(e.toString() + " function: " + wo);
    e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

    throw new StorageObjectFailure("Storage Object Exception in entity", e);
  }
}

