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
import java.util.Map;

import mir.entity.EntityList;
import mir.log.LoggerWrapper;
import mir.module.AbstractModule;
import mir.module.ModuleExc;
import mir.module.ModuleFailure;
import mir.storage.StorageObject;
import mir.util.JDBCStringRoutines;
import mircoders.entity.EntityUsers;
import mircoders.global.MirGlobal;


/*
 *  Users Module -
 *
 *
 * @author RK
 */

public class ModuleUsers extends AbstractModule
{
  static LoggerWrapper logger = new LoggerWrapper("Module.Users");

  public ModuleUsers(StorageObject theStorage)
  {
    if (theStorage == null)
      logger.warn("ModuleUsers(): StorageObject was null!");

    this.theStorage = theStorage;
  }

  /**
   * Authenticate and lookup a user
   *
   * @param user              The user to lookup
   * @param password          The password
   * @return                  The authenticated user, or <code>null</code> if the user
   *                          doesn't exist, or the supplied password is invalid.
   * @throws ModuleException
   */

  public EntityUsers getUserForLogin(String user, String password) throws ModuleExc, ModuleFailure {
    try {
      String whereString =
          "login='" + JDBCStringRoutines.escapeStringLiteral(user) + "' " +
          "and password='" + JDBCStringRoutines.escapeStringLiteral(
          MirGlobal.localizer().adminInterface().makePasswordDigest(password)) +
          "' " +
          "and is_admin='1'";

      EntityList userList = getByWhereClause(whereString, -1);

      if (userList != null && userList.getCount() == 1)
        return (EntityUsers) userList.elementAt(0);
      else
        return null;
    }
    catch (Throwable t) {
      throw new ModuleFailure(t);
    }
  }

  private Map digestPassword(Map aValues) throws ModuleExc, ModuleFailure {
    Map result = aValues;

    try {
      if (aValues.containsKey("password")) {
        result = new HashMap();
        result.putAll(aValues);
        result.put("password",
            MirGlobal.localizer().adminInterface().
            makePasswordDigest( (String) aValues.get("password")));
      }
    }
    catch (Throwable t) {
      throw new ModuleFailure("ModuleUsers.add: " + t.getMessage(), t);
    }

    return result;
  }

  public String add (Map theValues) throws ModuleExc, ModuleFailure {
    try {
      return super.add(digestPassword(theValues));
    }
    catch (Throwable t) {
      throw new ModuleFailure(t);
    }
  }

  /**
   * Standardfunktion, um einen Datensatz via StorageObject zu aktualisieren
   * @param theValues Hash mit Spalte/Wert-Paaren
   * @return Id des eingef?gten Objekts
   * @exception ModuleException
   */
  public String set (Map theValues) throws ModuleExc, ModuleFailure {
    try {
      return super.set(digestPassword(theValues));
    }
    catch (Throwable t) {
      throw new ModuleFailure(t);
    }
  }
}