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
/**
 * Title:        Indy
 * Description:  Parses Xml-Files into the Database
 * Copyright:    Copyright (c) 2001
 * Company:      indymedia.de
 * @author idfx
 * @version 1.0
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */



package mircoders.input;

import  java.io.*;
import  java.util.*;
import  java.lang.reflect.*;
import  mir.misc.*;
import  mir.storage.*;
import  mir.module.*;
import  mir.storage.*;
import  mir.module.*;
import  org.xml.sax.*;
import  org.xml.sax.helpers.*;
import  com.icl.saxon.aelfred.*;


/**
 * put your documentation comment here
 */
public class XmlInputParser {
  static Logfile logger;

  /**
   * the main-method
   * a DirectoryName should be given
   */
  public static void main (String[] args) {
    //logging
    File logDir = new File(args[0] + "LOG");
    if (!logDir.exists()) {
      logDir.mkdir();
    }
    logger = Logfile.getInstance(args[0] + "LOG/xml.log");
    XmlInputParser xmlInputParser = new XmlInputParser();
    //get the config-file
    /* Commented out for now since it seems unused and need more
     * info regarding it. Marc Heckmann <heckmann@hbe.ca>
    MirConfig.initConfig("config"); */
    //parse the xml-files in the given directory
    xmlInputParser.parse(args[0]);
    // stop freemarker templateCache (cracy)
    HTMLTemplateProcessor.stopAutoUpdate();
    //stop it
    System.exit(0);
  }

  /**
   * this method parses the xml-file an
   * returns 0 if succesful
   * returns -1 if failed
   */
  private boolean loadXml (String fileName) {
    try {
      XMLReader reader = new SAXDriver();
      InputSource is = new InputSource(new FileInputStream(fileName));
      reader.setContentHandler(new XmlHandler());
      reader.parse(is);
    } catch (IOException ex) {
      logger.printError(ex.toString());
      return  false;
    } catch (SAXException ex) {
      logger.printError(ex.toString());
      return  false;
    }
    return  true;
  }

  /**
   * Reads all XML-Files in the given Directory
   * and returns a String[] with the filenames
   * @param dir
   * @return
   */
  public String[] readDir (String dir) {
    File file = new File(dir);
    String[] fileNames = file.list(new XmlFilenameFilter());
    return  fileNames;
  }

  /**
   * parses the XML-Files in the given Directory
   * @param dir
   */
  public void parse (String dir) {
    File goodDir = new File(dir + "/GOOD");
    File badDir = new File(dir + "/BAD");
    boolean result = false;
    //read the directory
    String[] fileNames = readDir(dir);
    for (int i = 0; i < fileNames.length; i++) {
      //parse every file
      result = loadXml(dir + "/" + fileNames[i]);
      if (result == true) {                     //if succesfully parsed
        HashMap hash = XmlHandler.returnHash();
        HashMap val = (HashMap)hash.get("values");
        //set the default user
        val.put("to_publisher", "5");
        String table = (String)hash.get("table");
        AbstractModule moduleInstance = null;
        try {
          Class databaseClass = Class.forName("mir.storage.Database" +
              table);
          Method m = databaseClass.getMethod("getInstance", null);
          Database databaseInstance = (Database)m.invoke(null, null);
          moduleInstance = (AbstractModule)Class.forName("mir.module.Module"
              + table).newInstance();
          //AbstractModule moduleInstance = new ModuleContent(databaseInstance);
          moduleInstance.setStorage(databaseInstance);
        } catch (Exception e) {
          //logger.printError(e.toString());
          result = false;
        }
        result = insert(val, moduleInstance);
      }
      if (result == false) {                    //if error
        File file = new File(dir + "/" + fileNames[i]);
        if (!badDir.exists()) {                 //exits Bad-Dir?
          badDir.mkdir();
        }
        if (!file.renameTo(new File(dir + "/BAD/" + fileNames[i]))) {
          logger.printError("Failed move to BAD: " + fileNames[i]);
        }
      }
      else {                    //end if(result == false)
        File file = new File(dir + "/" + fileNames[i]);
        if (!goodDir.exists()) {                //exists Good-Dir?
          goodDir.mkdir();
        }
        if (!file.renameTo(new File(dir + "/GOOD/" + fileNames[i]))) {
          logger.printError("Failed move to GOOD: " + fileNames[i]);
        }
        logger.printInfo("Successfully parsed: " + fileNames[i]);
      }         //end else (result == true)
    }           //end for
  }             //end parse

  /**
   *   Holt die Felder aus der Metadatenfelderliste des StorageObjects, die
   *   im HttpRequest vorkommen und liefert sie als HashMap zurueck
   *   @return HashMap
   */
  public HashMap getIntersectingValues (HashMap values, StorageObject theStorage) {
    ArrayList theFieldList;
    try {
      theFieldList = theStorage.getFields();
    } catch (StorageObjectException e) {
      logger.printError("Failed: " + e.toString());
      return  null;
    }
    HashMap withValues = new HashMap();
    String aField, aValue;
    for (int i = 0; i < theFieldList.size(); i++) {
      aField = (String)theFieldList.get(i);
      aValue = (String)values.get(aField);
      if (aValue != null)
        withValues.put(aField, aValue);
    }
    return  withValues;
  }

  /**
   * Inserts a hash with values in a table
   * @param values
   * @param module
   * @return
   */
  public boolean insert (HashMap values, AbstractModule module) {
    try {
      HashMap withValues = getIntersectingValues(values, module.getStorageObject());
      module.add(withValues);
    } catch (Exception e) {
      logger.printError("Failed to insert: " + e.toString());
      return  false;
    }
    return  true;
  }
}



