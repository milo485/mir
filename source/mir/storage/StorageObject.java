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
package mir.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleList;

import mir.entity.Entity;
import mir.entity.EntityList;


/**
 * Implementiert Interface f?r die Speicherschicht.
 * Bislang gibt es in der Bibliothek nur die M?glichkeit
 * in einer Datenbank zu speichern.
 * @author RK
 * @version        29.6.1999
 */
public interface StorageObject {
  /**
   * Dokumentation siehe Database.java
   * @param id
   * @return Entity
   * @exception StorageObjectException
   */
  abstract public Entity selectById(String id) throws StorageObjectExc;

  /**
   * Dokumentation siehe Database.java
   * @param aField
   * @param aValue
   * @return EntityList
   * @exception StorageObjectException
   */
  abstract public EntityList selectByFieldValue(String aField, String aValue)
    throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @param whereClause
   * @return EntityList
   * @exception StorageObjectException
   */
  abstract public EntityList selectByWhereClause(String whereClause)
    throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @param whereClause
   * @param offset
   * @return EntityList
   * @exception StorageObjectException
   */
  abstract public EntityList selectByWhereClause(String whereClause, int offset)
    throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @param whereClause
   * @param orderBy
   * @param offset
   * @return EntityList
   * @exception StorageObjectException
   */
  abstract public EntityList selectByWhereClause(String whereClause,
    String orderBy, int offset) throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @param whereClause
   * @param orderBy
   * @param offset
   * @param limit
   * @return EntityList
   * @exception StorageObjectException
   */
  abstract public EntityList selectByWhereClause(String whereClause,
    String orderBy, int offset, int limit) throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @param id
   * @return boolen
   * @exception StorageObjectException
   */
  abstract public boolean delete(String id) throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @return ArrayList
   * @exception StorageObjectException
   */
  abstract public List getFields() throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @return int[]
   * @exception StorageObjectException
   */
  abstract public int[] getTypes() throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @return ArrayList
   * @exception StorageObjectException
   */
  abstract public List getLabels() throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @param a
   * @exception StorageObjectException
   */
  abstract public void update(Entity a) throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @param a
   * @return String id
   * @exception StorageObjectException
   */
  abstract public String insert(Entity a) throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @return Class Klasse der Entity
   */
  abstract public Class getEntityClass();

  /**
   * put your documentation comment here
   * @return
   */
  abstract public String getIdName();

  /**
   * Dokumentation siehe Database.java
   * @return String
   */
  abstract public String getTableName();

  /**
   * Dokumentation siehe Database.java
   * @return SimpleHash
   */
  abstract public SimpleHash getHashData();

  /**
   * Dokumentation siehe Database.java
   * @return Connection
   * @exception StorageObjectException
   */
  abstract public Connection getPooledCon() throws StorageObjectFailure;

  /**
   *
   * @param a
   * @param sql
   * @return
   * @throws StorageObjectFailure
   * @throws SQLException
   */
  abstract public ResultSet executeSql(Statement a, String sql) throws StorageObjectFailure, SQLException;

  /**
   *
   * @param sql
   * @return
   * @throws StorageObjectFailure
   * @throws SQLException
   */
  abstract public ResultSet executeSql(String sql) throws StorageObjectFailure, SQLException;

  /**
   * Executes 1 sql statement and returns the results as a <code>List</code> of <code>Map</code>s
   *
   * @param sql
   * @return
   * @throws StorageObjectFailure
   * @throws StorageObjectExc
   */
  abstract public List executeFreeSql(String sql, int aLimit) throws StorageObjectFailure, StorageObjectExc;

  /**
   * Executes 1 sql statement and returns the first result row as a <<code>Map</code>s
   * (<code>null</code> if there wasn't any row)
   *
   * @param sql
   * @return
   * @throws StorageObjectFailure
   * @throws StorageObjectExc
   */
  abstract public Map executeFreeSingleRowSql(String sql) throws StorageObjectFailure, StorageObjectExc ;

  /**
   * Executes 1 sql statement and returns the first column of the first result row as a <<code>String</code>s
   * (<code>null</code> if there wasn't any row)
   *
   * @param sql
   * @return
   * @throws StorageObjectFailure
   * @throws StorageObjectExc
   */
  abstract public String executeFreeSingleValueSql(String sql) throws StorageObjectFailure, StorageObjectExc ;

  /**
   * Dokumentation siehe Database.java
   * @param con
   * @param stmt
   */
  abstract public void freeConnection(Connection con, Statement stmt)
    throws StorageObjectFailure;

  /**
   * Dokumentation siehe Database.java
   * @return
   */
  abstract public SimpleList getPopupData() throws StorageObjectFailure;

  abstract public int executeUpdate(Statement a, String sql)
    throws StorageObjectFailure, SQLException;

  abstract public int executeUpdate(String sql)
    throws StorageObjectFailure, SQLException;

  abstract public int getSize(String where)
    throws SQLException, StorageObjectFailure;
}
