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

import java.util.ArrayList;
import java.util.Set;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.log.LoggerWrapper;
import mir.storage.StorageObject;
import mir.storage.store.StorableObject;
import mir.storage.store.StoreContainerType;
import mir.storage.store.StoreIdentifier;
import mir.storage.store.StoreUtil;
import freemarker.template.TemplateListModel;
import freemarker.template.TemplateModel;

/**
 *
 * Container class for lists of Entities.
 * Now implements freemarker.template.TemplateListModel
 * and @see mir.storage.store.StorableObject.
 *
 * @author <RK>
 * first version	27.6.1999
 *
 *  @version 1.0 (freemarker compliant & and storable in ObjectStore)
 */
public class EntityList implements TemplateListModel, StorableObject {
  protected static MirPropertiesConfiguration configuration;
  protected LoggerWrapper logger;
  private ArrayList           theEntityArrayList = new ArrayList();
  private String              whereClause, orderClause;
  private StorageObject       theStorage;
  private int                 count, offset, limit;
  private int                 offsetnext = -1, offsetprev = -1;
  private int                 freemarkerListPointer=-1;


  static {
    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (PropertiesConfigExc e) {
      throw new RuntimeException("Unable to get configuration: " + e.getMessage());
    }
  }

  /**
   * Constructor.
   */
  public EntityList(){
    logger = new LoggerWrapper("Entity.List");
  }

/* get/set EntityClass of Objects stored in EntityList */
  public void setStorage(StorageObject storage) { this.theStorage=storage; }
  public StorageObject getStorage() { return theStorage; }

  public void setLimit(int limit) { this.limit = limit; }

  /**
   * Sets the WHERE clause that fetched the Entities of this EntityList from the database.
   * @param wc The string that contains the WHERE clause
   */
  public void setWhere(String wc) {
    this.whereClause = wc;
  }

  /**
   * Returns the WHERE clause that returned this EntityList from the database
   * @return whereClause The WHERE clause
   */
  public String getWhere() {
    return whereClause;
  }


  /**
   * Sets the sorting criterium of this EntityList
   * @param oc
   */
  public void setOrder(String oc) {
    this.orderClause = oc;
  }

  /**
   * Returns the sorting criterium.
   * @return orderClause The sort order
   */
  public String getOrder() {
    return orderClause;
  }

  /**
   * Sets the number of rows that match the WHERE clause
   * @param i The number of rows that match the WHERE clause
   */
  public void setCount(int i) {
    this.count = i;
  }

  /**
   * Returns the number of rows that match the WHERE clause
   * @return The number of rows ...
   */
  public int getCount() {
    return count;
  }

  /**
   * Sets the offset
   * @param i The offset
   */
  public void setOffset(int i) {
    offset = i;
  }

  /**
   * Returns the offset
   * @return offset
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Sets the offset of the next batch of Entities.
   * @param i The next offset
   */
  public void setNextBatch(int i) {
    offsetnext = i;
  }

  /**
   * Returns the offset of the next batch of Entities.
   * @return offset of the next batch
   */
  public int getNextBatch() {
    return offsetnext;
  }

  /**
   * Returns whether there is a next batch within the WHERE clause
   * @return true if yes, false if no.
   */
  public boolean hasNextBatch() {
    return (offsetnext >= 0);
  }

  /**
   * Sets the offset of the previous batch.
   * @param i the previous offset
   */
  public void setPrevBatch(int i) {
    offsetprev = i;
  }

  /**
   * Returns the offset of the previous batch.
   * @return offset of the previous batch
   */
  public int getPrevBatch() {
    return offsetprev;
  }

  /**
   * Returns whether there is a previous batch.
   * @return true if yes, false if no
   */
  public boolean hasPrevBatch() {
    return (offsetprev >= 0);
  }

  /**
   * Returns the start index of the batch.
   * @return
   */
  public int getFrom() {
    return offset+1;
  }

  /**
   * Returns the end index of the batch.
   * @return
   */
  public int getTo() {
    if (hasNextBatch())
      return offsetnext;
    else
      return count;
  }

  /**
   * Inserts an Entity into the EntityList.
   * @param anEntity The entity to be inserted.
   */

  public void add (Entity anEntity) {
    if (anEntity!=null)
      theEntityArrayList.add(anEntity);
    else
      logger.warn("EntityList: add called with empty Entity");
  }


  /**
   * @return The number of Entities in the EntityList.
   */

  public int size() {
    return theEntityArrayList.size();
  }


  /**
   * Returns the element at position i in the EntityList as Entity
   * @param i the position of the element in question
   * @return The element at position i.
   */

  public Entity elementAt(int i) {
    /** @todo check if i is in list.size() */
    return (Entity)theEntityArrayList.get(i);
  }


// The following methods have to be implemented
// for this class to be an implementation of the
// TemplateListModel of the Freemarker packages

  public TemplateModel get(int i) { return elementAt(i); }
  public boolean isRewound() { return (freemarkerListPointer==-1) ? true : false; }
  public void rewind() { freemarkerListPointer=-1; }

  public TemplateModel next() {
    if (hasNext()) {
    freemarkerListPointer++;return get(freemarkerListPointer); }
    else return null;
  }


  /**
   * Returns whether there is a next element
   * @return true if there is a next element, else false
   */

  public boolean hasNext() {
    return theEntityArrayList.size()>0 && freemarkerListPointer+2<=theEntityArrayList.size();
  }


  /**
   * Returns whether EntityList is empty or not
   * @return true in case of empty list, false otherwise
   */

  public boolean isEmpty() {
    if (theEntityArrayList!=null)
      return theEntityArrayList.size()<1 ;
    else return false;
  }


// Methods to implement StorableObject

  public Set getNotifyOnReleaseSet() { return null; }

  public StoreIdentifier getStoreIdentifier() {
    if ( theStorage!=null ) {
      return
      new StoreIdentifier( this, StoreContainerType.STOC_TYPE_ENTITYLIST,
      StoreUtil.getEntityListUniqueIdentifierFor( theStorage.getTableName(),
      whereClause, orderClause, offset, limit ));
    }
    logger.warn("EntityList could not return StoreIdentifier");
    return null;
  }

}
