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

/**
 * Base class the entities are derived from. Provides base functionality of
 * an entity. Entities are used to represent rows of a database table.<p>
 */

package  mir.entity;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.sql.*;

import freemarker.template.*;

import mir.storage.*;
import mir.misc.*;

/**
 * Base Class of Entities
 * Interfacing TemplateHashModel and TemplateModelRoot to be freemarker compliant
 *
 * @version $Id: Entity.java,v 1.10 2002/11/04 04:35:21 mh Exp $
 * @author rk
 *
 */

public class Entity implements TemplateHashModel, TemplateModelRoot
{
  private boolean             changed;
  protected HashMap           theValuesHash;   // tablekey / value
  protected StorageObject     theStorageObject;
  protected static Logfile    theLog;
  protected ArrayList         streamedInput=null;
  private static int instances = 0;
    static {
      theLog = Logfile.getInstance(MirConfig.getProp("Home") + MirConfig.getProp("Entity.Logfile"));
    }

    public Entity() {

      this.changed = false;
      instances++;
      Integer i = new Integer(instances);
      //System.err.println("New abstract entity instance: "+i.toString());
    }

  /**
   * Constructor
   * @param StorageObject The StorageObject of the Entity.
   */
  public Entity (StorageObject StorageObject) {
    this();
    setStorage(StorageObject);
  }

  /*
   * Sets the StorageObject of the Entity.
   */
  public void setStorage (StorageObject storage) {
    this.theStorageObject = storage;
  }

  /**
   * Sets the values of the Entity.
   * @param theStringValues HashMap containing the new values of the Entity
   */

  public void setValues(HashMap theStringValues)
  {
    /** @todo should be synchronized */
    if (theStringValues!=null) {
      theValuesHash = new HashMap();
      String aKey;
      Set set = theStringValues.keySet();
      Iterator it = set.iterator();
      int size = set.size();
      for (int i = 0; i < size; i++) {
        aKey = (String)it.next();
        theValuesHash.put(aKey, (String)theStringValues.get(aKey));
      }
    }
    else theLog.printWarning("Entity.setValues called with null HashMap");
 }

  /**
   * Returns whether the content of the Entity has changed.
   * @return true wenn ja, sonst false
   */
  public boolean changed () {
    return  changed;
  }

  /**
   * Returns the primary key of the Entity.
   * @return String Id
   */
  public String getId () {
    return  (String)getValue(theStorageObject.getIdName());
  }

  /**
   * Defines the primary key of the Entity
   * @param id
   */
  public void setId (String id) {
    theValuesHash.put(theStorageObject.getIdName(), id);
      }

  /**
   * Returns the value of a field by field name.
   * @param field The name of the field
   * @return value of the field
   */
  public String getValue (String field) {
    String returnValue = null;
    if (field != null)
    {
      if (field.equals("webdb_create_formatted"))
      {
        if (hasValueForField("webdb_create"))
          returnValue=StringUtil.dateToReadableDate(getValue("webdb_create"));
      }
      else if (field.equals("webdb_lastchange_formatted"))
      {
        if (hasValueForField("webdb_lastchange"))
          returnValue=StringUtil.dateToReadableDate(getValue("webdb_lastchange"));
      }
      else if (field.equals("webdb_create_dc"))
      {
        if (hasValueForField("webdb_create"))
          returnValue=StringUtil.webdbdateToDCDate(getValue("webdb_create"));
      }
      else
        returnValue = (String)theValuesHash.get(field);
    }
    return returnValue;
  }

  public boolean hasValueForField(String field)
  {
    if (theValuesHash!=null)
      return theValuesHash.containsKey(field);
    return false;
  }

  /**
   * Insers Entity into the database via StorageObject
   * @return Primary Key of the Entity
   * @exception StorageObjectException
   */
  public String insert () throws StorageObjectException {
    theLog.printDebugInfo("Entity: trying to insert ...");
    if (theStorageObject != null) {
      return theStorageObject.insert((Entity)this);
    }
    else
      throw  new StorageObjectException("Kein StorageObject gesetzt!");
  }

  /**
   * Saves changes of this Entity to the database
   * @exception StorageObjectException
   */
  public void update () throws StorageObjectException {
    theStorageObject.update((Entity)this);
  }

  /**
   * Sets the value for a field. Issues a log message if the field name
   * supplied was not found in the Entity.
   * @param theProp The field name whose value has to be set
   * @param theValue The new value of the field
   * @exception StorageObjectException
   */
  public void setValueForProperty (String theProp, String theValue)
    throws StorageObjectException {
    this.changed = true;
    if (isField(theProp))
      theValuesHash.put(theProp, theValue);
    else {
      theLog.printWarning("Property not found: " + theProp+theValue);
    }

  }

  /**
   * Returns the field names of the Entity as ArrayListe.
   * @return ArrayList with field names
   * @exception StorageObjectException is throuwn if database access was impossible
   */
  public ArrayList getFields () throws StorageObjectException {
    return  theStorageObject.getFields();
    }

  /**
   * Returns an int[] with the types of the fields
   * @return int[] that contains the types of the fields
   * @exception StorageObjectException
   */
  public int[] getTypes () throws StorageObjectException {
    return  theStorageObject.getTypes();
    }

  /**
   * Returns an ArrayList with field names
   * @return List with field names
   * @exception StorageObjectException
   */
  public ArrayList getLabels () throws StorageObjectException {
    return  theStorageObject.getLabels();
    }

  /**
   * Returns a Hashmap with all values of the Entity.
   * @return HashMap with field name as key and the corresponding values
   *
   * @deprecated This method is deprecated and will be deleted in the next release.
   *  Entity interfaces freemarker.template.TemplateHashModel now and can
   *  be used in the same way as SimpleHash.

   */
    public HashMap getValues() {
      theLog.printWarning("## using deprecated Entity.getValues() - a waste of resources");
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

  public ArrayList streamedInput() {
    return streamedInput;
  }

   /** Returns whether fieldName is a valid field name of this Entity.
   * @param fieldName
   * @return true in case fieldName is a field name, else false.
   * @exception StorageObjectException
   */
  public boolean isField (String fieldName) throws StorageObjectException {
    return  theStorageObject.getFields().contains(fieldName);
  }

   /** Returns the number of instances of this Entity
   * @return int The number of instances
   */
  public int getInstances() {
     return instances;
  }

  protected void throwStorageObjectException (Exception e, String wo) throws StorageObjectException {
    theLog.printError( e.toString() + " Funktion: "+ wo);
    throw  new StorageObjectException("Storage Object Exception in entity" +e.toString());
  }

  /**
   * Frees an instance
   */
  /*public void finalize () {
    instances--;
    Integer i = new Integer(instances);
    System.err.println("Removing abstract entity instance: "+i.toString());
    try {
      super.finalize();
    } catch (Throwable t) {
      System.err.println(t.toString());
    }
  }*/


  // Now implements freemarkers TemplateHashModel
  // two methods have to be overridden:
  // 1. public boolean isEmpty() throws TemplateModelException
  // 2. public TemplateModel get(java.lang.String key) throws TemplateModelException

  public boolean isEmpty() throws TemplateModelException
  {
    return (theValuesHash==null || theValuesHash.isEmpty()) ? true : false;
  }

  public TemplateModel get(java.lang.String key) throws TemplateModelException
  {
		return new SimpleScalar(getValue(key));
  }
	
	public void put(java.lang.String key, TemplateModel model)
  {
    // putting should only take place via setValue and is limited to the
    // database fields associated with the entity. no additional freemarker
    // stuff will be available via Entity.
    theLog.printWarning("### put is called on entity! - the values will be lost!");
  }

  public void remove(java.lang.String key)
  {
    // do we need this?
  }


  //////////////////////////////////////////////////////////////////////////////////


}

