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

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.entity.Entity;
import mir.entity.EntityList;
import mir.entity.StorableObjectEntity;
import mir.log.LoggerWrapper;
import mir.misc.HTMLTemplateProcessor;
import mir.misc.StringUtil;
import mir.storage.store.ObjectStore;
import mir.storage.store.StorableObject;
import mir.storage.store.StoreContainerType;
import mir.storage.store.StoreIdentifier;
import mir.storage.store.StoreUtil;
import mir.util.JDBCStringRoutines;

import com.codestudio.util.SQLManager;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleList;


/**
 * Diese Klasse implementiert die Zugriffsschicht auf die Datenbank.
 * Alle Projektspezifischen Datenbankklassen erben von dieser Klasse.
 * In den Unterklassen wird im Minimalfall nur die Tabelle angegeben.
 * Im Konfigurationsfile findet sich eine Verweis auf den verwendeten
 * Treiber, Host, User und Passwort, ueber den der Zugriff auf die
 * Datenbank erfolgt.
 *
 * @version $Id: Database.java,v 1.39 2003/04/21 12:42:47 idfx Exp $
 * @author rk
 *
 */
public class Database implements StorageObject {
  private static Class GENERIC_ENTITY_CLASS = mir.entity.StorableObjectEntity.class;
  private static Class STORABLE_OBJECT_ENTITY_CLASS = mir.entity.StorableObjectEntity.class;


  private static SimpleHash POPUP_EMPTYLINE = new SimpleHash();
  protected static final ObjectStore o_store = ObjectStore.getInstance();
  private static final int _millisPerHour = 60 * 60 * 1000;
  private static final int _millisPerMinute = 60 * 1000;

  static {
    // always same object saves a little space
    POPUP_EMPTYLINE.put("key", "");
    POPUP_EMPTYLINE.put("value", "--");
  }

  protected LoggerWrapper logger;
  protected MirPropertiesConfiguration configuration;
  protected String theTable;
  protected String theCoreTable = null;
  protected String thePKeyName = "id";
  protected int thePKeyType;
  protected int thePKeyIndex;
  protected boolean evaluatedMetaData = false;
  protected ArrayList metadataFields;
  protected ArrayList metadataLabels;
  protected ArrayList metadataNotNullFields;
  protected int[] metadataTypes;
  protected Class theEntityClass;
  protected StorageObject myselfDatabase;
  protected SimpleList popupCache = null;
  protected boolean hasPopupCache = false;
  protected SimpleHash hashCache = null;
  protected boolean hasTimestamp = true;
  private String database_driver;
  private String database_url;
  private int defaultLimit;
  protected DatabaseAdaptor theAdaptor;
  private SimpleDateFormat _dateFormatterOut =
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private SimpleDateFormat _dateFormatterIn =
    new SimpleDateFormat("yyyy-MM-dd HH:mm");
  private Calendar _cal = new GregorianCalendar();

  /**
   * Kontruktor bekommt den Filenamen des Konfigurationsfiles ?bergeben.
   * Aus diesem file werden <code>Database.Logfile</code>,
   * <code>Database.Username</code>,<code>Database.Password</code>,
   * <code>Database.Host</code> und <code>Database.Adaptor</code>
   * ausgelesen und ein Broker f?r die Verbindugen zur Datenbank
   * erzeugt.
   *
   * @param   String confFilename Dateiname der Konfigurationsdatei
   */
  public Database() throws StorageObjectFailure {
    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (PropertiesConfigExc e) {
      throw new StorageObjectFailure(e);
    }
    logger = new LoggerWrapper("Database");

    String theAdaptorName = configuration.getString("Database.Adaptor");
    defaultLimit = Integer.parseInt(configuration.getString("Database.Limit"));

    try {
      theEntityClass = GENERIC_ENTITY_CLASS;
      theAdaptor = (DatabaseAdaptor) Class.forName(theAdaptorName).newInstance();
    }
    catch (Throwable e) {
      logger.error("Error in Database() constructor with " + theAdaptorName + " -- " + e.getMessage());
      throw new StorageObjectFailure("Error in Database() constructor.", e);
    }
  }

  /**
   * Liefert die Entity-Klasse zur?ck, in der eine Datenbankzeile gewrappt
   * wird. Wird die Entity-Klasse durch die erbende Klasse nicht ?berschrieben,
   * wird eine mir.entity.GenericEntity erzeugt.
   *
   * @return Class-Objekt der Entity
   */
  public java.lang.Class getEntityClass() {
    return theEntityClass;
  }

  /**
   * Liefert die Standardbeschr?nkung von select-Statements zur?ck, also
   * wieviel Datens?tze per Default selektiert werden.
   *
   * @return Standard-Anzahl der Datens?tze
   */
  public int getLimit() {
    return defaultLimit;
  }

  /**
   * Liefert den Namen des Primary-Keys zur?ck. Wird die Variable nicht von
   * der erbenden Klasse ?berschrieben, so ist der Wert <code>PKEY</code>
   * @return Name des Primary-Keys
   */
  public String getIdName() {
    return thePKeyName;
  }

  /**
   * Liefert den Namen der Tabelle, auf das sich das Datenbankobjekt bezieht.
   *
   * @return Name der Tabelle
   */
  public String getTableName() {
    return theTable;
  }

  /*
  *   Dient dazu vererbte Tabellen bei objectrelationalen DBMS
  *   zu speichern, wenn die id einer Tabelle in der parenttabelle verwaltet
  *   wird.
  *   @return liefert theCoreTabel als String zurueck, wenn gesetzt, sonst
  *    the Table
   */
  public String getCoreTable() {
    if (theCoreTable != null) {
      return theCoreTable;
    } else {
      return theTable;
    }
  }

  /**
   * Liefert Feldtypen der Felder der Tabelle zurueck (s.a. java.sql.Types)
   * @return int-Array mit den Typen der Felder
   * @exception StorageObjectException
   */
  public int[] getTypes() throws StorageObjectFailure {
    if (metadataTypes == null) {
      get_meta_data();
    }

    return metadataTypes;
  }

  /**
   * Liefert eine Liste der Labels der Tabellenfelder
   * @return ArrayListe mit Labeln
   * @exception StorageObjectException
   */
  public List getLabels() throws StorageObjectFailure {
    if (metadataLabels == null) {
      get_meta_data();
    }

    return metadataLabels;
  }

  /**
   * Liefert eine Liste der Felder der Tabelle
   * @return ArrayList mit Feldern
   * @exception StorageObjectException
   */
  public List getFields() throws StorageObjectFailure {
    if (metadataFields == null) {
      get_meta_data();
    }

    return metadataFields;
  }

  /*
  *   Gets value out of ResultSet according to type and converts to String
  *   @param inValue  Wert aus ResultSet.
  *   @param aType  Datenbanktyp.
  *   @return liefert den Wert als String zurueck. Wenn keine Umwandlung moeglich
  *           dann /unsupported value/
   */
  private String getValueAsString(ResultSet rs, int valueIndex, int aType)
    throws StorageObjectFailure {
    String outValue = null;

    if (rs != null) {
      try {
        switch (aType) {
        case java.sql.Types.BIT:
          outValue = (rs.getBoolean(valueIndex) == true) ? "1" : "0";

          break;

        case java.sql.Types.INTEGER:
        case java.sql.Types.SMALLINT:
        case java.sql.Types.TINYINT:
        case java.sql.Types.BIGINT:

          int out = rs.getInt(valueIndex);

          if (!rs.wasNull()) {
            outValue = new Integer(out).toString();
          }

          break;

        case java.sql.Types.NUMERIC:

          /** @todo Numeric can be float or double depending upon
           *  metadata.getScale() / especially with oracle */
          long outl = rs.getLong(valueIndex);

          if (!rs.wasNull()) {
            outValue = new Long(outl).toString();
          }

          break;

        case java.sql.Types.REAL:

          float tempf = rs.getFloat(valueIndex);

          if (!rs.wasNull()) {
            tempf *= 10;
            tempf += 0.5;

            int tempf_int = (int) tempf;
            tempf = (float) tempf_int;
            tempf /= 10;
            outValue = "" + tempf;
            outValue = outValue.replace('.', ',');
          }

          break;

        case java.sql.Types.DOUBLE:

          double tempd = rs.getDouble(valueIndex);

          if (!rs.wasNull()) {
            tempd *= 10;
            tempd += 0.5;

            int tempd_int = (int) tempd;
            tempd = (double) tempd_int;
            tempd /= 10;
            outValue = "" + tempd;
            outValue = outValue.replace('.', ',');
          }

          break;

        case java.sql.Types.CHAR:
        case java.sql.Types.VARCHAR:
        case java.sql.Types.LONGVARCHAR:
          outValue = rs.getString(valueIndex);

          break;

        case java.sql.Types.LONGVARBINARY:
          outValue = rs.getString(valueIndex);

          break;

        case java.sql.Types.TIMESTAMP:

          // it's important to use Timestamp here as getting it
          // as a string is undefined and is only there for debugging
          // according to the API. we can make it a string through formatting.
          // -mh
          Timestamp timestamp = (rs.getTimestamp(valueIndex));

          if (!rs.wasNull()) {
            java.util.Date date = new java.util.Date(timestamp.getTime());
            outValue = _dateFormatterOut.format(date);
            _cal.setTime(date);

            int offset =
              _cal.get(Calendar.ZONE_OFFSET) + _cal.get(Calendar.DST_OFFSET);
            String tzOffset =
              StringUtil.zeroPaddingNumber(offset / _millisPerHour, 2, 2);
            outValue = outValue + "+" + tzOffset;
          }

          break;

        default:
          outValue = "<unsupported value>";
          logger.warn( "Unsupported Datatype: at " + valueIndex + " (" + aType + ")");
        }
      } catch (SQLException e) {
        throw new StorageObjectFailure("Could not get Value out of Resultset -- ",
          e);
      }
    }

    return outValue;
  }

  /*
  *   select-Operator um einen Datensatz zu bekommen.
  *   @param id Primaerschluessel des Datensatzes.
  *   @return liefert EntityObject des gefundenen Datensatzes oder null.
   */
  public Entity selectById(String id) throws StorageObjectExc {
    if ((id == null) || id.equals("")) {
      throw new StorageObjectExc("Database.selectById: Missing id");
    }

    // ask object store for object
    if (StoreUtil.implementsStorableObject(theEntityClass)) {
      String uniqueId = id;

      if (theEntityClass.equals(StorableObjectEntity.class)) {
        uniqueId += ("@" + theTable);
      }

      StoreIdentifier search_sid = new StoreIdentifier(theEntityClass, uniqueId);
      logger.debug("CACHE: (dbg) looking for sid " + search_sid.toString());

      Entity hit = (Entity) o_store.use(search_sid);

      if (hit != null) {
        return hit;
      }
    }

    Statement stmt = null;
    Connection con = getPooledCon();
    Entity returnEntity = null;

    try {
      ResultSet rs;

      /** @todo better prepared statement */
      String selectSql =
        "select * from " + theTable + " where " + thePKeyName + "=" + id;
      stmt = con.createStatement();
      rs = executeSql(stmt, selectSql);

      if (rs != null) {
        if (evaluatedMetaData == false) {
          evalMetaData(rs.getMetaData());
        }

        if (rs.next()) {
          returnEntity = makeEntityFromResultSet(rs);
        }
        else {
          logger.debug("No data for id: " + id + " in table " + theTable);
        }

        rs.close();
      }
      else {
        logger.debug("No Data for Id " + id + " in Table " + theTable);
      }
    }
    catch (SQLException sqe) {
      throwSQLException(sqe, "selectById");
      return null;
    }
    catch (NumberFormatException e) {
      logger.error("ID is no number: " + id);
    }
    finally {
      freeConnection(con, stmt);
    }

    return returnEntity;
  }

  /**
   *   select-Operator um Datensaetze zu bekommen, die key = value erfuellen.
   *   @param key  Datenbankfeld der Bedingung.
   *   @param value  Wert die der key anehmen muss.
   *   @return EntityList mit den gematchten Entities
   */
  public EntityList selectByFieldValue(String aField, String aValue) throws StorageObjectFailure {
    return selectByFieldValue(aField, aValue, 0);
  }

  /**
   *   select-Operator um Datensaetze zu bekommen, die key = value erfuellen.
   *   @param key  Datenbankfeld der Bedingung.
   *   @param value  Wert die der key anehmen muss.
   *   @param offset  Gibt an ab welchem Datensatz angezeigt werden soll.
   *   @return EntityList mit den gematchten Entities
   */
  public EntityList selectByFieldValue(String aField, String aValue, int offset) throws StorageObjectFailure {
    return selectByWhereClause(aField + "=" + aValue, offset);
  }

  /**
   * select-Operator liefert eine EntityListe mit den gematchten Datens?tzen zur?ck.
   * Also offset wird der erste Datensatz genommen.
   *
   * @param wc where-Clause
   * @return EntityList mit den gematchten Entities
   * @exception StorageObjectException
   */
  public EntityList selectByWhereClause(String where)
    throws StorageObjectFailure {
    return selectByWhereClause(where, 0);
  }

  /**
   * select-Operator liefert eine EntityListe mit den gematchten Datens?tzen zur?ck.
   * Als maximale Anzahl wird das Limit auf der Konfiguration genommen.
   *
   * @param wc where-Clause
   * @param offset ab welchem Datensatz.
   * @return EntityList mit den gematchten Entities
   * @exception StorageObjectException
   */
  public EntityList selectByWhereClause(String whereClause, int offset)
    throws StorageObjectFailure {
    return selectByWhereClause(whereClause, null, offset);
  }

  /**
   * select-Operator liefert eine EntityListe mit den gematchten Datens?tzen zur?ck.
   * Also offset wird der erste Datensatz genommen.
   * Als maximale Anzahl wird das Limit auf der Konfiguration genommen.
   *
   * @param wc where-Clause
   * @param ob orderBy-Clause
   * @return EntityList mit den gematchten Entities
   * @exception StorageObjectException
   */
  public EntityList selectByWhereClause(String where, String order)
    throws StorageObjectFailure {
    return selectByWhereClause(where, order, 0);
  }

  /**
   * select-Operator liefert eine EntityListe mit den gematchten Datens?tzen zur?ck.
   * Als maximale Anzahl wird das Limit auf der Konfiguration genommen.
   *
   * @param wc where-Clause
   * @param ob orderBy-Clause
   * @param offset ab welchem Datensatz
   * @return EntityList mit den gematchten Entities
   * @exception StorageObjectException
   */
  public EntityList selectByWhereClause(String whereClause, String orderBy,
    int offset) throws StorageObjectFailure {
    return selectByWhereClause(whereClause, orderBy, offset, defaultLimit);
  }

  /**
   * select-Operator liefert eine EntityListe mit den gematchten Datens?tzen zur?ck.
   * @param wc where-Clause
   * @param ob orderBy-Clause
   * @param offset ab welchem Datensatz
   * @param limit wieviele Datens?tze
   * @return EntityList mit den gematchten Entities
   * @exception StorageObjectException
   */
  public EntityList selectByWhereClause(String wc, String ob, int offset,
    int limit) throws StorageObjectFailure {
    // check o_store for entitylist
    if (StoreUtil.implementsStorableObject(theEntityClass)) {
      StoreIdentifier search_sid =
        new StoreIdentifier(theEntityClass,
          StoreContainerType.STOC_TYPE_ENTITYLIST,
          StoreUtil.getEntityListUniqueIdentifierFor(theTable, wc, ob, offset,
            limit));
      EntityList hit = (EntityList) o_store.use(search_sid);

      if (hit != null) {
        logger.debug("CACHE (hit): " + search_sid.toString());

        return hit;
      }
    }

    // local
    EntityList theReturnList = null;
    Connection con = null;
    Statement stmt = null;
    ResultSet rs;
    int offsetCount = 0;
    int count = 0;

    // build sql-statement

    /** @todo count sql string should only be assembled if we really count
     *  see below at the end of method //rk */
    if ((wc != null) && (wc.trim().length() == 0)) {
      wc = null;
    }

    StringBuffer countSql =
      new StringBuffer("select count(*) from ").append(theTable);
    StringBuffer selectSql =
      new StringBuffer("select * from ").append(theTable);

    if (wc != null) {
      selectSql.append(" where ").append(wc);
      countSql.append(" where ").append(wc);
    }

    if ((ob != null) && !(ob.trim().length() == 0)) {
      selectSql.append(" order by ").append(ob);
    }

    if (theAdaptor.hasLimit()) {
      if ((limit > -1) && (offset > -1)) {
        selectSql.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
      }
    }

    // execute sql
    try {
      con = getPooledCon();
      stmt = con.createStatement();

      // selecting...
      rs = executeSql(stmt, selectSql.toString());

      if (rs != null) {
        if (!evaluatedMetaData) {
          evalMetaData(rs.getMetaData());
        }

        theReturnList = new EntityList();

        Entity theResultEntity;

        while (rs.next()) {
          theResultEntity = makeEntityFromResultSet(rs);
          theReturnList.add(theResultEntity);
          offsetCount++;
        }

        rs.close();
      }

      // making entitylist infos
      if (!(theAdaptor.hasLimit())) {
        count = offsetCount;
      }

      if (theReturnList != null) {
        // now we decide if we have to know an overall count...
        count = offsetCount;

        if ((limit > -1) && (offset > -1)) {
          if (offsetCount == limit) {
            /** @todo counting should be deffered to entitylist
             *  getSize() should be used */
            rs = executeSql(stmt, countSql.toString());

            if (rs != null) {
              if (rs.next()) {
                count = rs.getInt(1);
              }

              rs.close();
            }
            else {
              logger.error("Could not count: " + countSql);
            }
          }
        }

        theReturnList.setCount(count);
        theReturnList.setOffset(offset);
        theReturnList.setWhere(wc);
        theReturnList.setOrder(ob);
        theReturnList.setStorage(this);
        theReturnList.setLimit(limit);

        if (offset >= limit) {
          theReturnList.setPrevBatch(offset - limit);
        }

        if ((offset + offsetCount) < count) {
          theReturnList.setNextBatch(offset + limit);
        }

        if (StoreUtil.implementsStorableObject(theEntityClass)) {
          StoreIdentifier sid = theReturnList.getStoreIdentifier();
          logger.debug("CACHE (add): " + sid.toString());
          o_store.add(sid);
        }
      }
    } catch (SQLException sqe) {
      throwSQLException(sqe, "selectByWhereClause");
    } finally {
      try {
        if (con != null) {
          freeConnection(con, stmt);
        }
      } catch (Throwable t) {
      }
    }

    return theReturnList;
  }

  /**
   *  Bastelt aus einer Zeile der Datenbank ein EntityObjekt.
   *
   *  @param rs Das ResultSetObjekt.
   *  @return Entity Die Entity.
   */
  private Entity makeEntityFromResultSet(ResultSet rs)
    throws StorageObjectFailure {
    /** @todo OS: get Pkey from ResultSet and consult ObjectStore */
    Map theResultHash = new HashMap();
    String theResult = null;
    int theType;
    Entity returnEntity = null;

    try {
      int size = metadataFields.size();

      for (int i = 0; i < size; i++) {
        // alle durchlaufen bis nix mehr da
        theType = metadataTypes[i];

        if (theType == java.sql.Types.LONGVARBINARY) {
          InputStreamReader is =
            (InputStreamReader) rs.getCharacterStream(i + 1);

          if (is != null) {
            char[] data = new char[32768];
            StringBuffer theResultString = new StringBuffer();
            int len;

            while ((len = is.read(data)) > 0) {
              theResultString.append(data, 0, len);
            }

            is.close();
            theResult = theResultString.toString();
          } else {
            theResult = null;
          }
        } else {
          theResult = getValueAsString(rs, (i + 1), theType);
        }

        if (theResult != null) {
          theResultHash.put(metadataFields.get(i), theResult);
        }
      }

      if (theEntityClass != null) {
        returnEntity = (Entity) theEntityClass.newInstance();
        returnEntity.setValues(theResultHash);
        returnEntity.setStorage(myselfDatabase);

        if (returnEntity instanceof StorableObject) {
          logger.debug("CACHE: ( in) " + returnEntity.getId() + " :" + theTable);
          o_store.add(((StorableObject) returnEntity).getStoreIdentifier());
        }
      } else {
        throwStorageObjectException("Internal Error: theEntityClass not set!");
      }
    } catch (IllegalAccessException e) {
      throwStorageObjectException("No access! -- " + e.getMessage());
    } catch (IOException e) {
      throwStorageObjectException("IOException! -- " + e.getMessage());
    } catch (InstantiationException e) {
      throwStorageObjectException("No Instatiation! -- " + e.getMessage());
    } catch (SQLException sqe) {
      throwSQLException(sqe, "makeEntityFromResultSet");

      return null;
    }

    return returnEntity;
  }

  /**
   * insert-Operator: f?gt eine Entity in die Tabelle ein. Eine Spalte WEBDB_CREATE
   * wird automatisch mit dem aktuellen Datum gefuellt.
   *
   * @param theEntity
   * @return der Wert des Primary-keys der eingef?gten Entity
   */
  public String insert(Entity theEntity) throws StorageObjectFailure {
    //cache
    invalidatePopupCache();

    // invalidating all EntityLists corresponding with theEntityClass
    if (StoreUtil.implementsStorableObject(theEntityClass)) {
      StoreContainerType stoc_type =
        StoreContainerType.valueOf(theEntityClass,
          StoreContainerType.STOC_TYPE_ENTITYLIST);
      o_store.invalidate(stoc_type);
    }

    String returnId = null;
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      List streamedInput = theEntity.streamedInput();
      StringBuffer f = new StringBuffer();
      StringBuffer v = new StringBuffer();
      String aField;
      String aValue;
      boolean firstField = true;

      // make sql-string
      for (int i = 0; i < getFields().size(); i++) {
        aField = (String) getFields().get(i);

        if (!aField.equals(thePKeyName)) {
          aValue = null;

          // exceptions
          if (aField.equals("webdb_create") ||
              aField.equals("webdb_lastchange")) {
            aValue = "NOW()";
          } else {
            if ((streamedInput != null) && streamedInput.contains(aField)) {
              aValue = "?";
            } else {
              if (theEntity.hasValueForField(aField)) {
                aValue =
                  "'" +
                  JDBCStringRoutines.escapeStringLiteral((String) theEntity.getValue(
                      aField)) + "'";
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
      }
       // end for

      // insert into db
      StringBuffer sqlBuf =
        new StringBuffer("insert into ").append(theTable).append("(").append(f)
                                        .append(") values (").append(v).append(")");
      String sql = sqlBuf.toString();

      logger.debug("INSERT: " + sql);
      con = getPooledCon();
      con.setAutoCommit(false);
      pstmt = con.prepareStatement(sql);

      if (streamedInput != null) {
        for (int i = 0; i < streamedInput.size(); i++) {
          String inputString =
            (String) theEntity.getValue((String) streamedInput.get(i));
          pstmt.setBytes(i + 1, inputString.getBytes());
        }
      }

      int ret = pstmt.executeUpdate();

      if (ret == 0) {
        //insert failed
        return null;
      }

      pstmt =
        con.prepareStatement(theAdaptor.getLastInsertSQL(
            (Database) myselfDatabase));

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
      }

      freeConnection(con, pstmt);
    }

    /** @todo store entity in o_store */
    return returnId;
  }

  /**
   * update-Operator: aktualisiert eine Entity. Eine Spalte WEBDB_LASTCHANGE
   * wird automatisch mit dem aktuellen Datum gefuellt.
   *
   * @param theEntity
   */
  public void update(Entity theEntity) throws StorageObjectFailure {
    Connection con = null;
    PreparedStatement pstmt = null;

    /** @todo this is stupid: why do we prepare statement, when we
     *  throw it away afterwards. should be regular statement
     *  update/insert could better be one routine called save()
     *  that chooses to either insert or update depending if we
     *  have a primary key in the entity. i don't know if we
     *  still need the streamed input fields. // rk  */
    /** @todo extension: check if Entity did change, otherwise we don't need
     *  the roundtrip to the database */
    /** invalidating corresponding entitylists in o_store*/
    if (StoreUtil.implementsStorableObject(theEntityClass)) {
      StoreContainerType stoc_type =
        StoreContainerType.valueOf(theEntityClass,
          StoreContainerType.STOC_TYPE_ENTITYLIST);
      o_store.invalidate(stoc_type);
    }

    List streamedInput = theEntity.streamedInput();
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
      if (  !(aField.equals(thePKeyName) ||
            aField.equals("webdb_create") ||
            aField.equals("webdb_lastchange") ||
            ((streamedInput != null) && streamedInput.contains(aField)))) {
        if (theEntity.hasValueForField(aField)) {
          if (firstField == false) {
            fv.append(", ");
          }
          else {
            firstField = false;
          }

          fv.append(aField).append("='").append(JDBCStringRoutines.escapeStringLiteral((String) theEntity.getValue(aField))).append("'");

          //              fv.append(aField).append("='").append(StringUtil.quote((String)theEntity.getValue(aField))).append("'");
        }
      }
    }

    StringBuffer sql =
      new StringBuffer("update ").append(theTable).append(" set ").append(fv);

    // exceptions
    if (metadataFields.contains("webdb_lastchange")) {
      sql.append(",webdb_lastchange=NOW()");
    }

    // special case: the webdb_create requires the field in yyyy-mm-dd HH:mm
    // format so anything extra will be ignored. -mh
    if (metadataFields.contains("webdb_create") &&
        theEntity.hasValueForField("webdb_create")) {
      // minimum of 10 (yyyy-mm-dd)...
      if (theEntity.getValue("webdb_create").length() >= 10) {
        String dateString = theEntity.getValue("webdb_create");

        // if only 10, then add 00:00 so it doesn't throw a ParseException
        if (dateString.length() == 10) {
          dateString = dateString + " 00:00";
        }

        // TimeStamp stuff
        try {
          java.util.Date d = _dateFormatterIn.parse(dateString);
          Timestamp tStamp = new Timestamp(d.getTime());
          sql.append(",webdb_create='" + tStamp.toString() + "'");
        } catch (ParseException e) {
          throw new StorageObjectFailure(e);
        }
      }
    }

    if (streamedInput != null) {
      for (int i = 0; i < streamedInput.size(); i++) {
        sql.append(",").append(streamedInput.get(i)).append("=?");
      }
    }

    sql.append(" where id=").append(id);

    //theLog.printInfo("UPDATE: " + sql);
    // execute sql
    try {
      con = getPooledCon();
      con.setAutoCommit(false);
      pstmt = con.prepareStatement(sql.toString());

      if (streamedInput != null) {
        for (int i = 0; i < streamedInput.size(); i++) {
          String inputString =
            theEntity.getValue((String) streamedInput.get(i));
          pstmt.setBytes(i + 1, inputString.getBytes());
        }
      }

      pstmt.executeUpdate();
    } catch (SQLException sqe) {
      throwSQLException(sqe, "update");
    } finally {
      try {
        con.setAutoCommit(true);
      } catch (Exception e) {
        ;
      }

      freeConnection(con, pstmt);
    }
  }

  /*
  *   delete-Operator
  *   @param id des zu loeschenden Datensatzes
  *   @return boolean liefert true zurueck, wenn loeschen erfolgreich war.
   */
  public boolean delete(String id) throws StorageObjectFailure {
    invalidatePopupCache();

    // ostore send notification
    if (StoreUtil.implementsStorableObject(theEntityClass)) {
      String uniqueId = id;

      if (theEntityClass.equals(StorableObjectEntity.class)) {
        uniqueId += ("@" + theTable);
      }

      logger.debug("CACHE: (del) " + id);

      StoreIdentifier search_sid =
        new StoreIdentifier(theEntityClass,
          StoreContainerType.STOC_TYPE_ENTITY, uniqueId);
      o_store.invalidate(search_sid);
    }

    /** @todo could be prepared Statement */
    Statement stmt = null;
    Connection con = null;
    int res = 0;
    String sql =
      "delete from " + theTable + " where " + thePKeyName + "='" + id + "'";

    //theLog.printInfo("DELETE " + sql);
    try {
      con = getPooledCon();
      stmt = con.createStatement();
      res = stmt.executeUpdate(sql);
    } catch (SQLException sqe) {
      throwSQLException(sqe, "delete");
    } finally {
      freeConnection(con, stmt);
    }

    return (res > 0) ? true : false;
  }

  /* noch nicht implementiert.
  * @return immer false
   */
  public boolean delete(EntityList theEntityList) {
    invalidatePopupCache();

    return false;
  }

  /**
   * Diese Methode sollte ueberschrieben werden, wenn fuer die abgeleitete Database-Klasse
   * eine SimpleList mit Standard-Popupdaten erzeugt werden koennen soll.
   * @return null
   */
  public SimpleList getPopupData() throws StorageObjectFailure {
    return null;
  }

  /**
   *  Holt Daten fuer Popups.
   *  @param name  Name des Feldes.
   *  @param hasNullValue  Wenn true wird eine leerer  Eintrag fuer die Popups erzeugt.
   *  @return SimpleList Gibt freemarker.template.SimpleList zurueck.
   */
  public SimpleList getPopupData(String name, boolean hasNullValue)
    throws StorageObjectFailure {
    return getPopupData(name, hasNullValue, null);
  }

  /**
   *  Holt Daten fuer Popups.
   *  @param name  Name des Feldes.
   *  @param hasNullValue  Wenn true wird eine leerer  Eintrag fuer die Popups erzeugt.
   *  @param where  Schraenkt die Selektion der Datensaetze ein.
   *  @return SimpleList Gibt freemarker.template.SimpleList zurueck.
   */
  public SimpleList getPopupData(String name, boolean hasNullValue, String where)
    throws StorageObjectFailure {
    return getPopupData(name, hasNullValue, where, null);
  }

  /**
   *  Holt Daten fuer Popups.
   *  @param name  Name des Feldes.
   *  @param hasNullValue  Wenn true wird eine leerer  Eintrag fuer die Popups erzeugt.
   *  @param where  Schraenkt die Selektion der Datensaetze ein.
   *  @param order  Gibt ein Feld als Sortierkriterium an.
   *  @return SimpleList Gibt freemarker.template.SimpleList zurueck.
   */
  public SimpleList getPopupData(String name, boolean hasNullValue,
    String where, String order) throws StorageObjectFailure {
    // caching
    if (hasPopupCache && (popupCache != null)) {
      return popupCache;
    }

    SimpleList simpleList = null;
    Connection con = null;
    Statement stmt = null;

    // build sql
    StringBuffer sql =
      new StringBuffer("select ").append(thePKeyName).append(",").append(name)
                                 .append(" from ").append(theTable);

    if ((where != null) && !(where.length() == 0)) {
      sql.append(" where ").append(where);
    }

    sql.append(" order by ");

    if ((order != null) && !(order.length() == 0)) {
      sql.append(order);
    } else {
      sql.append(name);
    }

    // execute sql
    try {
      con = getPooledCon();
    } catch (Exception e) {
      throw new StorageObjectFailure(e);
    }

    try {
      stmt = con.createStatement();

      ResultSet rs = executeSql(stmt, sql.toString());

      if (rs != null) {
        if (!evaluatedMetaData) {
          get_meta_data();
        }

        simpleList = new SimpleList();

        // if popup has null-selector
        if (hasNullValue) {
          simpleList.add(POPUP_EMPTYLINE);
        }

        SimpleHash popupDict;

        while (rs.next()) {
          popupDict = new SimpleHash();
          popupDict.put("key", getValueAsString(rs, 1, thePKeyType));
          popupDict.put("value", rs.getString(2));
          simpleList.add(popupDict);
        }

        rs.close();
      }
    }
    catch (Exception e) {
      logger.error("getPopupData: " + e.getMessage());
      throw new StorageObjectFailure(e);
    } finally {
      freeConnection(con, stmt);
    }

    if (hasPopupCache) {
      popupCache = simpleList;
    }

    return simpleList;
  }

  /**
   * Liefert alle Daten der Tabelle als SimpleHash zurueck. Dies wird verwandt,
   * wenn in den Templates ein Lookup-Table benoetigt wird. Sollte nur bei kleinen
   * Tabellen Verwendung finden.
   * @return SimpleHash mit den Tabellezeilen.
   */
  public SimpleHash getHashData() {
    /** @todo dangerous! this should have a flag to be enabled, otherwise
     *  very big Hashes could be returned */
    if (hashCache == null) {
      try {
        hashCache =
          HTMLTemplateProcessor.makeSimpleHash(selectByWhereClause("", -1));
      }
      catch (StorageObjectFailure e) {
        logger.debug(e.getMessage());
      }
    }

    return hashCache;
  }

  /* invalidates the popupCache
   */
  protected void invalidatePopupCache() {
    /** @todo  invalidates toooo much */
    popupCache = null;
    hashCache = null;
  }

  /**
   * Diese Methode fuehrt den Sqlstring <i>sql</i> aus und timed im Logfile.
   * @param stmt Statemnt
   * @param sql Sql-String
   * @return ResultSet
   * @exception StorageObjectException
   */
  public ResultSet executeSql(Statement stmt, String sql)
                            throws StorageObjectFailure, SQLException {
    ResultSet rs;
    long startTime = System.currentTimeMillis();

    try {
      rs = stmt.executeQuery(sql);

      logger.debug((System.currentTimeMillis() - startTime) + "ms. for: " + sql);
    }
    catch (SQLException e) {
      logger.error(e.getMessage() +"\n" + (System.currentTimeMillis() - startTime) + "ms. for: " + sql);
      throw e;
    }

    return rs;
  }

  /**
   * returns the number of rows in the table
   */
  public int getSize(String where) throws SQLException, StorageObjectFailure {
    long startTime = System.currentTimeMillis();
    String sql = "SELECT Count(*) FROM " + theTable;

    if ((where != null) && !(where.length() == 0)) {
      sql = sql + " where " + where;
    }

    Connection con = null;
    Statement stmt = null;
    int result = 0;

    try {
      con = getPooledCon();
      stmt = con.createStatement();

      ResultSet rs = executeSql(stmt, sql);

      while (rs.next()) {
        result = rs.getInt(1);
      }
    }
    catch (SQLException e) {
      logger.error("Database.getSize: " + e.getMessage());
    }
    finally {
      freeConnection(con, stmt);
    }

    //theLog.printInfo(theTable + " has "+ result +" rows where " + where);
    logger.debug((System.currentTimeMillis() - startTime) + "ms. for: " + sql);

    return result;
  }

  public int executeUpdate(Statement stmt, String sql)
    throws StorageObjectFailure, SQLException {
    int rs;
    long startTime = System.currentTimeMillis();

    try {
      rs = stmt.executeUpdate(sql);

      logger.debug((System.currentTimeMillis() - startTime) + "ms. for: " + sql);
    }
    catch (SQLException e) {
      logger.debug("Failed: " + (System.currentTimeMillis() - startTime) + "ms. for: " + sql);
      throw e;
    }

    return rs;
  }

  public int executeUpdate(String sql)
    throws StorageObjectFailure, SQLException {
    int result = -1;
    long startTime = System.currentTimeMillis();
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = getPooledCon();
      pstmt = con.prepareStatement(sql);
      result = pstmt.executeUpdate();
    }
    catch (Throwable e) {
      logger.error("Database.executeUpdate(" + sql + "): " + e.getMessage());
      throw new StorageObjectFailure("Database.executeUpdate(" + sql + "): " + e.getMessage(), e);
    }
    finally {
      freeConnection(con, pstmt);
    }

    logger.debug((System.currentTimeMillis() - startTime) + "ms. for: " + sql);
    return result;
  }

  /**
   * Wertet ResultSetMetaData aus und setzt interne Daten entsprechend
   * @param md ResultSetMetaData
   * @exception StorageObjectException
   */
  private void evalMetaData(ResultSetMetaData md) throws StorageObjectFailure {
    this.evaluatedMetaData = true;
    this.metadataFields = new ArrayList();
    this.metadataLabels = new ArrayList();
    this.metadataNotNullFields = new ArrayList();

    try {
      int numFields = md.getColumnCount();
      this.metadataTypes = new int[numFields];

      String aField;
      int aType;

      for (int i = 1; i <= numFields; i++) {
        aField = md.getColumnName(i);
        metadataFields.add(aField);
        metadataLabels.add(md.getColumnLabel(i));
        aType = md.getColumnType(i);
        metadataTypes[i - 1] = aType;

        if (aField.equals(thePKeyName)) {
          thePKeyType = aType;
          thePKeyIndex = i;
        }

        if (md.isNullable(i) == ResultSetMetaData.columnNullable) {
          metadataNotNullFields.add(aField);
        }
      }
    }
    catch (SQLException e) {
      throwSQLException(e, "evalMetaData");
    }
  }

  /**
   *  Wertet die Metadaten eines Resultsets fuer eine Tabelle aus,
   *  um die alle Columns und Typen einer Tabelle zu ermitteln.
   */
  private void get_meta_data() throws StorageObjectFailure {
    Connection con = null;
    PreparedStatement pstmt = null;
    String sql = "select * from " + theTable + " where 0=1";

    try {
      con = getPooledCon();
      pstmt = con.prepareStatement(sql);

      logger.debug("METADATA: " + sql);
      ResultSet rs = pstmt.executeQuery();
      evalMetaData(rs.getMetaData());
      rs.close();
    }
    catch (SQLException e) {
      throwSQLException(e, "get_meta_data");
    }
    finally {
      freeConnection(con, pstmt);
    }
  }

  public Connection getPooledCon() throws StorageObjectFailure {
    Connection con = null;

    try {
      con = SQLManager.getInstance().requestConnection();
    }
    catch (SQLException e) {
      logger.error("could not connect to the database " + e.getMessage());

      throw new StorageObjectFailure("Could not connect to the database", e);
    }

    return con;
  }

  public void freeConnection(Connection con, Statement stmt)
    throws StorageObjectFailure {
    SQLManager.closeStatement(stmt);
    SQLManager.getInstance().returnConnection(con);
  }

  /**
   * Wertet SQLException aus und wirft dannach eine StorageObjectException
   * @param sqe SQLException
   * @param wo Funktonsname, in der die SQLException geworfen wurde
   * @exception StorageObjectException
   */
  protected void throwSQLException(SQLException sqe, String aFunction)
    throws StorageObjectFailure {
    String state = "";
    String message = "";
    int vendor = 0;

    if (sqe != null) {
      state = sqe.getSQLState();
      message = sqe.getMessage();
      vendor = sqe.getErrorCode();
    }

    String information =
        "SQL Error: " +
        "state= " + state +
        ", vendor= " + vendor +
        ", message=" + message +
        ", function= " + aFunction;

    logger.error(information);

    throw new StorageObjectFailure(information, sqe);
  }

  protected void _throwStorageObjectException(Exception e, String aFunction)
    throws StorageObjectFailure {

    if (e != null) {
      logger.error(e.getMessage() + aFunction);
      throw new StorageObjectFailure(aFunction, e);
    }
  }

  /**
   * Loggt Fehlermeldung mit dem Parameter Message und wirft dannach
   * eine StorageObjectException
   * @param message Nachricht mit dem Fehler
   * @exception StorageObjectException
   */
  void throwStorageObjectException(String aMessage) throws StorageObjectFailure {
    logger.error(aMessage);
    throw new StorageObjectFailure(aMessage, null);
  }
}
