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
package mircoders.module;

import java.util.Map;

import mir.entity.Entity;
import mir.entity.EntityList;
import mir.log.LoggerWrapper;
import mir.module.AbstractModule;
import mir.module.ModuleExc;
import mir.module.ModuleFailure;
import mir.storage.StorageObject;

public class ModuleTopics extends AbstractModule {

  static LoggerWrapper logger = new LoggerWrapper("Module.Topics");

  public ModuleTopics(StorageObject theStorage) {
    this.theStorage = theStorage;
  }

  /**
   *  Method getTopicList
   *
   *  @return SimpleList of all Topics sorted by title
   *
   */
  public EntityList getTopicsList() throws ModuleExc, ModuleFailure {
    try {
      return getByWhereClause("", "title", -1);
    }
    catch (Throwable e) {
      logger.error("ModuleTopics.getTopicsList: topics could not be fetched: " + e.getMessage());

      throw new ModuleFailure("ModuleTopics.getTopicsList: topics could not be fetched: " + e.getMessage(), e);
    }
  }

  /**
   * Overrides the AbstractModule.set(),
   * All dependent ContentEntities are set unproduced.
   * @param theValues Hash mit Spalte/Wert-Paaren
   * @return Id des eingef?gten Objekts
   * @exception ModuleException
   */
  public String set(Map theValues) throws ModuleExc, ModuleFailure {
    try {
      Entity theEntity = theStorage.selectById((String) theValues.get("id"));
      if (theEntity == null) {
        throw new ModuleExc("No topic with id  " + theValues.get("id") + " found");
      }
      theEntity.setValues(theValues);
      theEntity.update();

      return theEntity.getId();
    }
    catch (Throwable e) {
      throw new ModuleFailure(e);
    }
  }

}
