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

package mircoders.module;

import java.util.HashMap;

import freemarker.template.SimpleList;

import mir.entity.Entity;
import mir.entity.EntityList;
import mir.module.AbstractModule;
import mir.module.ModuleException;
import mir.storage.StorageObject;
import mir.storage.StorageObjectExc;
import mir.storage.StorageObjectFailure;
import mir.log.*;

import mircoders.entity.EntityContent;
import mircoders.entity.EntityTopics;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseContentToTopics;
import mircoders.storage.DatabaseTopics;

/*
 *  ThemenModule -
 *
 *
 * @author RK
 */

public class ModuleTopics extends AbstractModule {

  static LoggerWrapper logger = new LoggerWrapper("Module.Topics");

  public ModuleTopics(StorageObject theStorage) {
    this.theStorage = theStorage;
  }

  public SimpleList getTopicsAsSimpleList() throws ModuleException {
    try {
      return ((DatabaseTopics) theStorage).getPopupData();
    }
    catch (StorageObjectFailure e) {
      throw new ModuleException(e.toString());
    }
  }

  /**
   *  Method getTopicList
   *
   *  @return SimpleList of all Topics sorted by title
   *
   */
  public EntityList getTopicsList() {
    EntityList returnList = null;
    try {
      returnList = getByWhereClause("", "title", -1);
    }
    catch (Exception e) {
      logger.warn("--getTopicsList: topics could not be fetched: " + e.getMessage());
    }
    return returnList;
  }

  /**
   * Overrides the AbstractModule.set(),
   * All dependent ContentEntities are set unproduced.
   * @param theValues Hash mit Spalte/Wert-Paaren
   * @return Id des eingef?gten Objekts
   * @exception ModuleException
   */
  public String set(HashMap theValues) throws ModuleException {
    try {
      Entity theEntity = theStorage.selectById((String) theValues.get("id"));
      if (theEntity == null) {
        throw new ModuleException("Kein Objekt mit id in Datenbank id: " + theValues.get("id"));
      }
      theEntity.setValues(theValues);
      DatabaseContentToTopics db = DatabaseContentToTopics.getInstance();
      DatabaseContent dbc = DatabaseContent.getInstance();
      EntityList contentList = db.getContent((EntityTopics) theEntity);
      if (contentList!=null) {
        for (int i = 0; i < contentList.size(); i++) {
          dbc.setUnproduced("id=" + ((EntityContent) contentList.elementAt(i)).getId());
        }
      }
      theEntity.update();
      return theEntity.getId();
    }
    catch (StorageObjectFailure e) {
      e.printStackTrace(System.err);
      throw new ModuleException(e.toString());
    } catch (StorageObjectExc e) {
      e.printStackTrace(System.err);
      throw new ModuleException(e.toString());
    }
  }

}
