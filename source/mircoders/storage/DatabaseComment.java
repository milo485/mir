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

package mircoders.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import freemarker.template.SimpleList;
import mir.log.LoggerWrapper;
import mir.storage.Database;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;

/**
 * <b>This class implements the access to the comment-table for the
 *    media table.
 *
 *
 */

public class DatabaseComment extends Database implements StorageObject{

  private static DatabaseComment instance;

  public static DatabaseComment getInstance() {
    if (instance == null) {
      synchronized (DatabaseComment.class) {
        if (instance == null) {
          instance = new DatabaseComment();
          instance.myselfDatabase = instance;
        }
      }
    }
    return instance;
  }

  private DatabaseComment() {
    super();
    hasTimestamp = false;
    theTable = "comment";
    logger = new LoggerWrapper("Database.Comment");

    this.theEntityClass = mircoders.entity.EntityComment.class;
  }

  public SimpleList getPopupData() throws StorageObjectFailure {
    return getPopupData("title", true);
  }

  public boolean deleteByContentId(String id) throws StorageObjectFailure {
    Statement stmt = null;
    Connection con = null;
    String sql;
    int res = 0;

    /** @todo comments and topics should be deleted */
    sql = "delete from " + theTable + " where to_media=" + id;
    logger.info("DELETE "+ sql);

    try {
      con = getPooledCon();
      stmt = con.createStatement();
      res = stmt.executeUpdate(sql);
    }
    catch (SQLException sqe) {
      new StorageObjectFailure(sqe);
      return false;
    }
    finally {
      freeConnection(con, stmt);
    }
    return true;
  }
}
