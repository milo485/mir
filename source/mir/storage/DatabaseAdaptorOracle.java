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

import java.util.Properties;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;


/**
 * Diese Klasse implementiert Interface DatabaseAdaptor fuer Oracle
 *
 * @author <RK>
 * @version 15.05.2000
 */
public final class DatabaseAdaptorOracle implements DatabaseAdaptor {
  /**
   * Liefert den Namen der Adaptorklasse <code>Adaptor.Oracle.Driver</code>
   * f?r Oracle zur?ck.
   * @return Adaptorklasse als String
   */
  public String getDriver() throws PropertiesConfigExc {
    return MirPropertiesConfiguration.instance().getString("Adaptor.Oracle.Driver");
  }

  /**
   * Liefert die URL f?r JDBC zur?ck, in den die Parameter user, pass und host
   * eingef?gt werden. Die URL wird aus der Konfiguration geholt.
   *
   * @param user user als String
   * @param pass passwort als String
   * @param host host als String
   * @return url als String
   */
  public String getURL(String user, String pass, String host)
    throws PropertiesConfigExc {
    return MirPropertiesConfiguration.instance().getString("Adaptor.Oracle.URL");

    /** @todo   hier muesste bessererweise $HOST durch HOST ersetzt, etc. werden */
  }

  /**
   * Gibt zur?ck, ob das SQL der Datenbank den <code>limit</code>-Befehl beherrscht.
   * @return false
   */
  public boolean hasLimit() {
    return false;
  }

  /**
   * Liefert zur?ck, ob der <code>limit</code>-Befehl erst start und dann offset
   * hat (true), oder umgekehrt. Nur Relevant, wenn hasLimit true zur?ckliefert.
   *
   * @return false
   */
  public boolean reverseLimit() {
    return false;
  }

  /**
   * Liefert ein Properties-Objekt zurueck mit user und password.
   * @param user
   * @param password
   * @return Properties
   */
  public Properties getProperties(String user, String password) {
    return null;
  }

  public String getLastInsertSQL(Database theDB) {
    return "select currval('" + theDB.getCoreTable() + "_id_seq')";
  }
}
