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
import java.sql.Statement;

import mir.entity.EntityList;
import mir.entity.EntityRelation;
import mir.storage.Database;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;
import mircoders.entity.EntityContent;

/**
 * <b>this class implements the access to the content-table</b>
 *
 *
 */

public class DatabaseContent extends Database implements StorageObject {

  private static DatabaseContent      instance;
  private static EntityRelation       relationComments;
  private static EntityRelation       relationFeature;

  // Contructors / Singleton

  // the following *has* to be sychronized cause this static method
  // could get preemted and we could end up with 2 instances of DatabaseFoo.
  // see the "Singletons with needles and thread" article at JavaWorld -mh
  public synchronized static DatabaseContent getInstance()
    throws StorageObjectFailure {

    if (instance == null ) {
      instance = new DatabaseContent();
      instance.myselfDatabase = instance;
    }
    return instance;
  }

  private DatabaseContent()
    throws StorageObjectFailure {

    super();
    this.theTable="content";
    this.theCoreTable="media";

    relationComments = new EntityRelation("id", "to_media", DatabaseComment.getInstance(), EntityRelation.TO_MANY);
    relationFeature = new EntityRelation("id", "to_feature", DatabaseFeature.getInstance(), EntityRelation.TO_ONE);
    try { this.theEntityClass = Class.forName("mircoders.entity.EntityContent"); }
    catch (Exception e) { throw new StorageObjectFailure(e); }
  }

  // methods

  /**
   * sets the database flag is_produced to unproduced
   */

  public void setUnproduced(String where) throws StorageObjectFailure
  {
    Connection con=null;Statement stmt=null;
    String sql = "update content set is_produced='0' where " + where;
    theLog.printDebugInfo("set unproduced: "+where);
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      executeUpdate(stmt,sql);
      theLog.printDebugInfo("set unproduced: "+where);
    }
    catch (Exception e) {_throwStorageObjectException(e, "-- set unproduced failed");}
    finally { freeConnection(con,stmt);}
  }

  /**
   * returns the comments that belong to the article (via entityrelation)
   * where db-flag is_published is true
   */
  public EntityList getComments(EntityContent entC) throws StorageObjectFailure {
    return relationComments.getMany(entC,"webdb_create","is_published='1'");
  }

  /**
   * returns the features that belong to the article (via entityrelation)
   */
  public EntityList getFeature(EntityContent entC) throws StorageObjectFailure {
    return relationFeature.getMany(entC);
  }

  public boolean delete(String id) throws StorageObjectFailure
  {
    DatabaseComment.getInstance().deleteByContentId(id);
    super.delete(id);
    return true;
  }

}
