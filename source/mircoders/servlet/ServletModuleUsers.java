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

package mircoders.servlet;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.log.LoggerWrapper;
import mir.module.ModuleException;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleException;
import mir.storage.StorageObjectFailure;
import mircoders.module.ModuleUsers;
import mircoders.storage.DatabaseUsers;
import freemarker.template.SimpleHash;

/*
 *  ServletModuleUsers -
 *  liefert HTML fuer Users
 *
 *
 * @author RK
 */

public class ServletModuleUsers extends mir.servlet.ServletModule
{
  private static ServletModuleUsers instance = new ServletModuleUsers();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleUsers() {
    super();
    logger = new LoggerWrapper("ServletModule.Users");

    templateListString = configuration.getString("ServletModule.Users.ListTemplate");
    templateObjektString = configuration.getString("ServletModule.Users.ObjektTemplate");
    templateConfirmString = configuration.getString("ServletModule.Users.ConfirmTemplate");

    try {
      mainModule = new ModuleUsers(DatabaseUsers.getInstance());
    }
    catch (StorageObjectFailure e) {
      logger.debug("initialization of ServletModuleUsers failed!: " + e.getMessage());
    }
  }

  public void edit(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException
  {
    String idParam = req.getParameter("id");

    if (idParam == null)
      throw new ServletModuleException("ServletModuleUser.edit: invalid call: (id) not specified");

    try {
      deliver(req, res, mainModule.getById(idParam), templateObjektString);
    }
    catch (ModuleException e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void add(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException
  {
    try {
      SimpleHash mergeData = new SimpleHash();
      mergeData.put("new", "1");
      deliver(req, res, mergeData, templateObjektString);
    }
    catch (Exception e) { throw new ServletModuleException(e.toString());}
  }

  public void insert(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException
  {
    try {
      HashMap withValues = getIntersectingValues(req, mainModule.getStorageObject());
      String id = mainModule.add(withValues);
      deliver(req, res, mainModule.getById(id), templateObjektString);
    }
    catch (Exception e) { throw new ServletModuleException(e.toString());}
  }
}
