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

import java.util.Map;

import mir.entity.Entity;
import mir.log.LoggerWrapper;
import mir.module.AbstractModule;
import mir.module.ModuleExc;
import mir.module.ModuleFailure;
import mir.storage.StorageObject;
import mircoders.storage.DatabaseComment;
import mircoders.storage.DatabaseContent;
import freemarker.template.SimpleList;


/*
 *  ModuleComment - methods and access for comments
 *
 * @author RK
 */

public class ModuleComment extends AbstractModule
{
  static LoggerWrapper logger = new LoggerWrapper("Module.Comment");

  public ModuleComment(StorageObject theStorage)
  {
    if (theStorage == null) logger.warn("StorageObject was null!");
    this.theStorage = theStorage;
  }

  public SimpleList getCommentAsSimpleList() throws ModuleExc, ModuleFailure {
    try {
      return ((DatabaseComment)theStorage).getPopupData();
    }
    catch (Throwable e) {
      throw new ModuleFailure(e);
    }
  }

  public void deleteById (String anId) throws ModuleExc, ModuleFailure {
    try {
      Entity theEntity = theStorage.selectById((String)anId);
      if (theEntity != null)
        DatabaseContent.getInstance().setUnproduced("id=" + theEntity.getValue("to_media"));

      super.deleteById(anId);
    }
    catch (Throwable e) {
      throw new ModuleFailure(e);
    }
  }

  /**
   *
   * @param theValues
   * @return
   * @throws ModuleExc
   * @throws ModuleFailure
   */

  public String set(Map theValues) throws ModuleExc, ModuleFailure {
    try {
      Entity theEntity = theStorage.selectById((String)theValues.get("id"));
      if (theEntity == null)
         throw new ModuleExc("No Object in the database with id " + theValues.get("id"));
      DatabaseContent.getInstance().setUnproduced("id=" + theEntity.getValue("to_media"));
      theEntity.setValues(theValues);
      theEntity.update();
      return theEntity.getId();
    }
    catch (Throwable e) {
      throw new ModuleFailure(e);
    }
  }
}
