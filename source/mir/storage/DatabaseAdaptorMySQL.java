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
package mir.storage;

import java.util.Properties;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;


/**
 * <b>Diese Klasse implementiert die abstrakte Klasse DatabaseAdaptor
 *
 * @author <RK>
 * @version 27.6.1999
 */
public final class DatabaseAdaptorMySQL implements DatabaseAdaptor {
  public String getDriver() throws PropertiesConfigExc {
    return MirPropertiesConfiguration.instance().getString("Adaptor.MySQL.Driver");
  }

  public String getURL(String user, String pass, String host)
    throws PropertiesConfigExc {
    return MirPropertiesConfiguration.instance().getString("Adaptor.MySQL.URL");
  }

  public boolean hasLimit() {
    return true;
  }

  public boolean reverseLimit() {
    return false;
  }

  public Properties getProperties(String user, String password) {
    return null;
  }

  public String getLastInsertSQL(Database theDB) {
    return "select last_insert_id()";
  }
}
