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

package  mircoders.storage;

import  java.lang.*;
import  java.sql.*;
import  java.io.*;
import  java.util.*;
import  freemarker.template.*;
import  mir.storage.*;
import  mir.entity.*;
import  mir.misc.*;
import  mir.util.*;


/**
 * <b>Diese Klasse implementiert die Datenbankverbindung zur MetaObjekt-Tabelle
 *
 *
 */
public class DatabaseLinksImcs extends Database
    implements StorageObject {
  private static DatabaseLinksImcs instance;

  /**
   * put your documentation comment here
   * @return
   * @exception StorageObjectException
   */
  // the following *has* to be sychronized cause this static method
  // could get preemted and we could end up with 2 instances of DatabaseFoo..
  // see the "Singletons with needles and thread" article at JavaWorld -mh
  public synchronized static DatabaseLinksImcs getInstance() throws
      StorageObjectException {
    if (instance == null) {
      instance = new DatabaseLinksImcs();
      instance.myselfDatabase = instance;
    }
    return instance;
  }

  /**
   * put your documentation comment here
   */
  private DatabaseLinksImcs() throws StorageObjectException {
    super();
    ////this.cache = new HashMap();
    this.hasTimestamp = false;
    this.theTable = "links_imcs";
    try {
      this.theEntityClass = Class.forName("mircoders.entity.EntityLinksImcs");
    }
    catch (Exception e) {
      throw new StorageObjectException(e.toString());
    }
  }

  /** @todo toooo much copy/paste in this class //rk  */

  public String insert(Entity theEntity) throws StorageObjectException {
    String returnId = "0";
    Connection con = null;
    PreparedStatement pstmt = null;
    //cache
    invalidatePopupCache();
    try {
      HashMap theEntityValues = theEntity.getValues();
      ArrayList streamedInput = theEntity.streamedInput();
      StringBuffer f = new StringBuffer();
      StringBuffer v = new StringBuffer();
      String aField, aValue;
      boolean firstField = true;
      // make sql-string
      for (int i = 0; i < getFields().size(); i++) {
        aField = (String) getFields().get(i);
        if (!aField.equals(thePKeyName)) {
          aValue = null;
          // sonderfaelle
          if (aField.equals("webdb_create")) {
            aValue = "NOW()";
          }
          else {
            if (streamedInput != null && streamedInput.contains(aField)) {
              aValue = "?";
            }
            else {
              if (theEntityValues.containsKey(aField)) {
                if (aField.equals("to_parent_id")) {
                  aValue = JDBCStringRoutines.escapeStringLiteral((String) theEntityValues.get(aField));
                }
                else {
                  aValue = "'" + JDBCStringRoutines.escapeStringLiteral((String) theEntityValues.get(aField)) +  "'";
                }
              }
            }
          }
          // wenn Wert gegeben, dann einbauen
          if (aValue != null) {
            if (firstField == false) {
              f.append(",");
              v.append(",");
            }
            else {
              firstField = false;
            }
            f.append(aField);
            v.append(aValue);
          }
        }
      } // end for
      // insert into db
      StringBuffer sqlBuf = new StringBuffer("insert into ").append(theTable).
          append("(").append(f).append(") values (").append(v).append(")");
      String sql = sqlBuf.toString();
      theLog.printInfo("INSERT: " + sql);
      con = getPooledCon();
      con.setAutoCommit(false);
      pstmt = con.prepareStatement(sql);
      if (streamedInput != null) {
        for (int i = 0; i < streamedInput.size(); i++) {
          String inputString = (String) theEntityValues.get(streamedInput.get(i));
          pstmt.setBytes(i + 1, inputString.getBytes());
        }
      }
      pstmt.execute();
      pstmt = con.prepareStatement(theAdaptor.getLastInsertSQL( (Database)
          myselfDatabase));
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      returnId = rs.getString(1);
      theEntity.setId(returnId);
    }
    catch (SQLException sqe) {
      throwSQLException(sqe, "insert");
    }
    finally {
      try {
        con.setAutoCommit(true);
      }
      catch (Exception e) {
        ;
      }
      freeConnection(con, pstmt);
    }
    return returnId;
  }

  public void update(Entity theEntity) throws StorageObjectException {
    Connection con = null;
    PreparedStatement pstmt = null;
    ArrayList streamedInput = theEntity.streamedInput();
    HashMap theEntityValues = theEntity.getValues();
    String id = theEntity.getId();
    String aField;
    StringBuffer fv = new StringBuffer();
    boolean firstField = true;
    //cache
    invalidatePopupCache();
    // build sql statement
    for (int i = 0; i < getFields().size(); i++) {
      aField = (String) metadataFields.get(i);
      // only normal cases
      if (! (aField.equals(thePKeyName) || aField.equals("webdb_create") ||
             aField.equals("webdb_lastchange") ||
             (streamedInput != null && streamedInput.contains(aField)))) {
        if (theEntityValues.containsKey(aField)) {
          if (firstField == false) {
            fv.append(", ");
          }
          else {
            firstField = false;
          }
          if (aField.equals("to_parent_id")) {
            fv.append(aField).append("=").append(JDBCStringRoutines.escapeStringLiteral((String)theEntityValues.get(aField)));
          }
          else {
            fv.append(aField).append("='").append(JDBCStringRoutines.escapeStringLiteral((String)theEntityValues.get(aField))).append("'");
          }
        }
      }
    }
    StringBuffer sql = new StringBuffer("update ").append(theTable).append(
        " set ").append(fv);
    // exceptions
    if (metadataFields.contains("webdb_lastchange")) {
      sql.append(",webdb_lastchange=NOW()");
    }
    if (streamedInput != null) {
      for (int i = 0; i < streamedInput.size(); i++) {
        sql.append(",").append(streamedInput.get(i)).append("=?");
      }
    }
    sql.append(" where id=").append(id);
    theLog.printInfo("UPDATE: " + sql);
    // execute sql
    try {
      con = getPooledCon();
      con.setAutoCommit(false);
      pstmt = con.prepareStatement(sql.toString());
      if (streamedInput != null) {
        for (int i = 0; i < streamedInput.size(); i++) {
          String inputString = (String) theEntityValues.get(streamedInput.get(i));
          pstmt.setBytes(i + 1, inputString.getBytes());
        }
      }
      pstmt.executeUpdate();
    }
    catch (SQLException sqe) {
      throwSQLException(sqe, "update");
    }
    finally {
      try {
        con.setAutoCommit(true);
      }
      catch (Exception e) {
        ;
      }
      freeConnection(con, pstmt);
    }
  }

}