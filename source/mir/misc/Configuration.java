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


/**
 * Diese Klasse realisert den Zugriff auf die Konfiguration.
 *
 */
public class Configuration {
  
  private static HashMap  confs = new HashMap(); // key: conffilename, confHash
  private String          confFilename;
  
  private static String   defaultconfFilename;
  private static Properties conf;

  protected static void initConfResource(String confName) {

    conf = new Properties();
    try {
        conf.load( new BufferedInputStream(new FileInputStream(confName)));
    }
    catch ( java.io.FileNotFoundException fnfe ) {
        System.err.println("could not read config file. not found: "+confName);
    }
    catch ( java.io.IOException ioex ) {
        System.err.println("could not read config file: "+confName);
    }

    confs.put("confname",confName);
  }

  protected static Enumeration getResourceKeys() {
    return conf.propertyNames();
  }


  /**
   * Fragt ab, ob das Betriebssystem Windows ist.
   * @return true wenn ja, sonst false.
   */
  protected static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
  }

   /**
   * Liefert Wert einer Property zurueck
   * @param propName
   * @return Wert der Property
   */
  protected static String getProperty(String propName) {  // default
    return conf.getProperty(propName);
  }
  
   /**
   * Checks if open posting should be direct or indirect
   * @return true if open posting should be direct
   */
  protected static boolean directOp() {
    String op = conf.getProperty("DirectOpenposting");
    if(op.equals("yes") || op.equals("Yes") || op.equals("y") || op.equals("Y")){
      return true;
    }
    return false;
  }

  /**
   * Liest eine Property eines Modules aus der Konfiguration
   * @param filename
   * @param theModule
   * @param propName
   * @return Wert der Property
   */
  protected String getProperty(String filename ,String theModule, String propName) {
    return getProperty(filename, theModule + "." + propName);
  }

    /**
   * Liest eine Property aus der Konfiguration
   * @param filename
   * @param propName
   * @return Wert der Property
   */
  protected static String getProperty(String filename, String propName) {
    if (filename != null) {
      String prop = null;
      HashMap conf = ((HashMap)confs.get("confname"));

      if (conf == null) {
        System.err.println("Keine Konfiguration fuer " + filename);
      } else {
        prop = (String)conf.get(propName);
      }

      if (prop == null) {
        System.err.println("Keine Konfiguration fuer " + filename + " " + propName);
      }

      return prop;

    } else {
        System.err.println("--- filename null!");
    }

    return null;
  }

  /**
   * Liefert Hashtable mit den Konfigurationen
   * @return
   */
  public static HashMap getConfs(){
    return confs;
  }

} //end of class
