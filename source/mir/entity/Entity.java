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
import java.util.List;
import java.util.Map;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.log.LoggerWrapper;
import mir.misc.StringUtil;
import mir.storage.StorageObject;
import mir.storage.StorageObjectExc;
import mir.storage.StorageObjectFailure;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelRoot;

/**
 * Base class the entities are derived from. Provides base functionality of
 * an entity. Entities are used to represent rows of a database table.<p>
 * Interfacing TemplateHashModel and TemplateModelRoot to be freemarker compliant
 *
 * @version $Id: Entity.java,v 1.20 2003/04/21 12:42:46 idfx Exp $
 * @author rk
 *
 */

public class Entity implements TemplateHashModel, TemplateModelRoot
{
  protected static MirPropertiesConfiguration configuration;

  private boolean changed;
  protected Map theValuesHash; // tablekey / value
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

    this.changed = false;
  }

  /**
   * Constructor
   * @param StorageObject The StorageObject of the Entity.
   */
  public Entity(StorageObject StorageObject) {
    this();
    setStorage(StorageObject);
  }

  /*
   * Sets the StorageObject of the Entity.
   */
  public void setStorage(StorageObject storage) {
    this.theStorageObject = storage;
  }

  /**
   * Sets the values of the Entity.
   * @param theStringValues Map containing the new values of the Entity
   */

  public void setValues(Map theStringValues) {
    /** @todo should be synchronized */
    if (theStringValues != null) {
      theValuesHash = new HashMap();
      theValuesHash.putAll(theStringValues);
    }
    else
      logger.warn("Entity.setValues called with null Map");
  }

  /**
   * Returns whether the content of the Entity has changed.
   * @return true wenn ja, sonst false
   */
  public boolean changed() {
    return changed;
  }

  /**
   * Returns the primary key of the Entity.
   * @return String Id
   */
  public String getId() {
    return (String) getValue(theStorageObject.getIdName());
  }

  /**
   * Defines the primary key of the Entity
   * @param id
   */
  public void setId(String id) {
    theValuesHash.put(theStorageObject.getIdName(), id);
  }

  /**
   * Returns the value of a field by field name.
   * @param field The name of the field
   * @return value of the field
   */
  public String getValue(String field) {
    String returnValue = null;
    if (field != null) {
      if (field.equals("webdb_create_formatted")) {
        if (hasValueForField("webdb_create"))
          returnValue = StringUtil.dateToReadableDate(getValue("webdb_create"));
      }
      else if (field.equals("webdb_lastchange_formatted")) {
        if (hasValueForField("webdb_lastchange"))
          returnValue = StringUtil.dateToReadableDate(getValue(
              "webdb_lastchange"));
      }
      else
        returnValue = (String) theValuesHash.get(field);
    }
    return returnValue;
  }

  public boolean hasValueForField(String field) {
    if (theValuesHash != null)
      return theValuesHash.containsKey(field);
    return false;
  }

  /**
   * Insers Entity into the database via StorageObject
   * @return Primary Key of the Entity
   * @exception StorageObjectException
   */
  public String insert() throws StorageObjectExc {
    logger.debug("Entity: trying to insert ...");
    if (theStorageObject != null) {
      return theStorageObject.insert( (Entity)this);
    }
    else
      throw new StorageObjectExc("theStorageObject == null!");
  }

  /**
   * Saves changes of this Entity to the database
   * @exception StorageObjectException
   */
  public void update() throws StorageObjectFailure {
    theStorageObject.update( (Entity)this);
  }

  /**
   * Sets the value for a field. Issues a log message if the field name
   * supplied was not found in the Entity.
   * @param theProp The field name whose value has to be set
   * @param theValue The new value of the field
   * @exception StorageObjectException
   */
  public void setValueForProperty(String theProp, String theValue) throws
      StorageObjectFailure {
    this.changed = true;
    if (isField(theProp))
      theValuesHash.put(theProp, theValue);
    else {
      logger.warn("Entity.setValueForProperty: Property not found: " + theProp + " (" + theValue + ")");
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
   * Returns a Map with all values of the Entity.
   * @return Map with field name as key and the corresponding values
   *
       * @deprecated This method is deprecated and will be deleted in the next release.
   *  Entity interfaces freemarker.template.TemplateHashModel now and can
   *  be used in the same way as SimpleHash.
   */
  public Map getValues() {
    logger.warn("using deprecated Entity.getValues() - a waste of resources");
    return theValuesHash;
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

  protected void throwStorageObjectFailure(Throwable e, String wo) throws
      StorageObjectFailure {
    logger.error(e.toString() + " function: " + wo);
    e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

    throw new StorageObjectFailure("Storage Object Exception in entity", e);
  }

  // Now implements freemarkers TemplateHashModel
  // two methods have to be overridden:
  // 1. public boolean isEmpty() throws TemplateModelException
  // 2. public TemplateModel get(java.lang.String key) throws TemplateModelException

  public boolean isEmpty() throws TemplateModelException {
    return (theValuesHash == null || theValuesHash.isEmpty()) ? true : false;
  }

  public TemplateModel get(java.lang.String key) throws TemplateModelException {
    return new SimpleScalar(getValue(key));
  }

  public void put(java.lang.String key, TemplateModel model) {
    // putting should only take place via setValue and is limited to the
    // database fields associated with the entity. no additional freemarker
    // stuff will be available via Entity.
    logger.warn("put is called on entity! - the values will be lost!");
  }

  public void remove(java.lang.String key) {
    // do we need this?
  }

  //////////////////////////////////////////////////////////////////////////////////
}

