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

package  mir.misc;

import  java.net.*;
import  java.io.*;
import  java.util.*;
import  java.lang.*;

import  javax.servlet.ServletContext;
import  javax.servlet.http.*;

import  mir.storage.StorageObjectException;
import  mir.storage.DatabaseAdaptor;
import com.codestudio.util.*;

/**
 * Title:        Mir
 * Description:  Class that allows access to all Mir
 *               config values
 * Copyright:    Copyright (c) 2001
 * Company:      Indymedia
 * @author       mh <heckmann@hbe.ca>
 * @version 0.1
 */


/**
 * This class is a layer above the Configuration
 * It manages access to config variables that are
 * both generated on the fly and found in the config file.
 */

public class MirConfig extends Configuration {

  private static HashMap configHash = null;
  private static HashMap brokerHash = new HashMap();
  private static int      instances=0;

  /**
   * Initializes Configuration hash that contains all values.
   * loads the properties-file and any other values
   * @param uri, the root Uri of the install
   * @param home, The absolute path if the install root.
   * @param name, The name of the servlet (usually "Mir")
   * @param confName, the name of the config file to load.
   */
  public static synchronized void initConfig(ServletContext ctx, String ctxPath,
                                            String name, String confName) {

    initConfResource(ctx.getRealPath("/WEB-INF/")+"/"+confName);

    configHash = new HashMap();

    configHash.put("Home", ctx.getRealPath("/WEB-INF/")+"/");
    configHash.put("ServletContext", ctx);
    configHash.put("RootUri", ctxPath);

    Enumeration resKeys = getResourceKeys();
    while(resKeys.hasMoreElements()) {
      String keyNm = (String)resKeys.nextElement();
      configHash.put(keyNm, getProperty(keyNm));
    }
  }
  /**
   * Returns the property asked for by pulling it out a HashMap
   * @param a String containing the property name (key)
   * @return a String containing the prop. value
   */
  public static void setServletName(String servletName) {
    configHash.put("ServletName",servletName);
  }

  /**
   * Returns the property asked for by pulling it out a HashMap
   * @param a String containing the property name (key)
   * @return a String containing the prop. value
   */
  public static String getProp(String propName) {
    String result = (String)configHash.get(propName);

    if (result==null)
      throw new ConfigException("config property '"+propName+"' not available!");

    return result;
  }

  /**
   * Returns the property asked for by pulling it out a HashMap and
   * appending it to configproperty "Home"
   * @param a String containing the property name (key)
   * @return a String containing the prop.value
   */
  public static String getPropWithHome(String propName) {
    return getProp("Home") + getProp(propName);
  }

  /**
   * Returns the property asked for iin raw Object form by
   * pulling it out a HashMap
   * @param a String containing the property name (key)
   * @return an Object containing the prop.value
   */
  public static Object getPropAsObject(String propName) {
    return configHash.get(propName);
  }

  public static void initDbPool () throws StorageObjectException {
    if (configHash == null) {
        throw new StorageObjectException("MirConfig -- Trying initialize "+
                                        "DB pool when system not yet "+
                                        "configured");
    }
    String dbUser=getProp("Database.Username");
    String dbPassword=getProp("Database.Password");
    String dbHost=getProp("Database.Host");
    String dbAdapName=getProp("Database.Adaptor");
    DatabaseAdaptor adaptor;
    try {
      adaptor = (DatabaseAdaptor)Class.forName(dbAdapName).newInstance();
    } catch (Exception e) {
      throw new StorageObjectException("Could not load DB adapator: "+
                                        e.toString());
    }
    String dbDriver=adaptor.getDriver();
    String dbUrl=adaptor.getURL(dbUser,dbPassword, dbHost);
    System.out.println("adding Broker with: " +dbDriver+":"+dbUrl );
    addBroker( dbDriver, dbUrl);
  }

  public static void addBroker(String driver, String URL)
    throws StorageObjectException {

    if (configHash == null) {
        throw new StorageObjectException("MirConfig -- Trying initialize "+
                                        "DB pool when system not yet "+
                                        "configured");
    }
    String username,passwd,min,max,log,reset,dbname,dblogfile;

    if(!brokerHash.containsKey("Pool.broker")){
      username=getProp("Database.Username");
      passwd=getProp("Database.Password");
      min=getProp("Database.poolMin");
      max=getProp("Database.poolMax");
      dbname=getProp("Database.Name");
      log=getProp("Home")+ configHash.get("Database.PoolLog");
      reset=getProp("Database.poolResetTime");
      dblogfile=getPropWithHome("Database.Logfile");

      System.err.println("-- making Broker for -"
                          +driver+" - " +URL
                          + " log " + log + " user "
                          + username + " pass: " + passwd);

      JDBCPoolMetaData meta = new JDBCPoolMetaData();
      meta.setDbname(dbname);
      meta.setDriver(driver);
      meta.setURL(URL);
      meta.setUserName(username);
      meta.setPassword(passwd);
      meta.setJNDIName("mir");
      meta.setMaximumSize(Integer.parseInt(max));
      meta.setMinimumSize(Integer.parseInt(min));
      meta.setPoolPreparedStatements(false);
      meta.setCacheEnabled(false);
      meta.setCacheSize(15);
      meta.setDebugging(false);
      meta.setLogFile(dblogfile+".pool");

      JDBCPool pool = SQLManager.getInstance().createPool(meta);

      if (pool!=null){
        instances++;
        brokerHash.put("Pool.broker",pool);
      }

    } // end if
  }

  /**
   * Finalize method
   */
  public void finalize(){
    instances --;
    try {
      super.finalize();
    } catch (Throwable t) {}
  }

  public static Map allSettings() {
    return configHash;
  }

}
