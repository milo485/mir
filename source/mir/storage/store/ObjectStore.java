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

package mir.storage.store;

/**
 * Title:         ObjectStore for StorableObjects
 * Description:   ObjectStore holds a Map of @see StoreContainer for all possible
 *                @see StoreIdentifier.
 *
 *                @see StorageIdentifier - identitfies one object in the ObjectStore
 *                      i.e. in its apropriate bucket. It holds a unique identifier
 *                      of a StorableObject and a reference on the StorableObject.
 *
 *                @see StoreContainer - "Buckets" to store different types of Objects
 *                      in one Container. These buckets are cofigurable via
 *                      config.properties.
 *
 *                @see StoreContainerType - is a signature for all StoreContainer
 *                      and StoreIdentifier.
 *
 *                @see StorableObjects - Interface Object have to implement to
 *                      be handled by the ObjectStore
 *
 *                @see ServletStoreInfo - Maintenance Servlet for ObjectStore.
 *                      Displays information about current content of the
 *                      ObjectStore.
 *
 *
 * Copyright:     Copyright (c) 2002
 * Company:       indy
 * @author        rk
 * @version 1.0
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import mir.config.MirPropertiesConfiguration;
import mir.log.LoggerWrapper;

public class ObjectStore {

  private final static ObjectStore INSTANCE = new ObjectStore();
  private final static HashMap containerMap = new HashMap(); // StoreContainerType/StoreContainer
  private static long storeHit = 0, storeMiss = 0;
  private Properties ostoreConf;
  private LoggerWrapper logger;

  private ObjectStore() {
    String confName = null;

    logger = new LoggerWrapper("Database.ObjectStore");
    Properties conf = new Properties();

    try {
      confName =
          MirPropertiesConfiguration.instance().getString("Home") +
          "etc/objectstore.properties";
      conf.load(new BufferedInputStream(new FileInputStream(confName)));
    }
    catch (java.io.FileNotFoundException fnfe) {
      logger.error("could not read config file. not found: " + confName);
    }
    catch (Throwable t) {
      logger.error("could not get config: " + t.getMessage());
    }
    ostoreConf = conf;
  }

  public static ObjectStore getInstance() {
    return INSTANCE;
  }

  /**
   *  Method:       use
   *  Description:  The ObjectStore tries to find the @see StoreIdentifier sid
   *                and returns the stored Object.
   *
   *  @return       StorableObject is null when no StorableObject for the
   *                StoreIdentifier sid is found.
   */
  public StorableObject use(StoreIdentifier sid) {
    if (sid != null) {
      StorableObject storeObject = null;
      StoreContainer stoc = getStoreContainerForSid(sid);
      if (stoc != null)
        storeObject = stoc.use(sid);
      else
        logger.warn("container not found for: " + sid.toString());
      if (storeObject != null) {
        storeHit++;
        return storeObject;
      }
    }
    storeMiss++;
    return null;

  }

  /**
   *  Method:       add
   *  Description:  A StoreIdentifier is added to the ObjectStore, if it
   *                contains a reference to a @see StorableObject.
   */
  public void add(StoreIdentifier sid) {
    if (sid != null && sid.hasReference()) {
      // find the right StoreContainer for sid
      StoreContainer stoc = getStoreContainerForSid(sid);
      if (stoc == null) {
        // we have to make new StoreContainer
        StoreContainerType stocType = sid.getStoreContainerType();
        stoc = new StoreContainer(stocType);
        containerMap.put(stocType, stoc);
      }
      stoc.add(sid);
    }
  }

  /**
   *  Method:       invalidate(StorableObject sto)
   *  Description:  ObjectStore is notified of change of a @see StorableObject
   *                sto and invalidates all relevant cache entries.
   */

  public void invalidate(StoreIdentifier sid) {
    // propagate invalidation to StoreContainer
    if (sid != null) {
      StoreContainer stoc = getStoreContainerForSid(sid);
      stoc.invalidate(sid);
    }
  }

  /**
   *  Method:       invalidate(StoreContainerType)
   *  Description:  serves to invalidate a whole StoreContainer
   *
   *  @return
   */
  public void invalidate(StoreContainerType stoc_type) {
    if (stoc_type != null) {
      /** @todo invalidates too much:
       *  improvement: if instanceof StoreContainerEntity && EntityList
       *  then invalidate only StoreIdentifier matching the right table
       */
      StoreContainer stoc = getStoreContainerForStocType(stoc_type);
      if (stoc != null)
        stoc.invalidate();
    }

  }

  // internal methods for StoreContainer managment

  /**
   *  Method:       getStoreContainerForSid
   *  Description:  private method to find the right @see StoreContainer for
   *                the @see StoreIdentifier sid.
   *
   *  @return       StoreContainer is null when no Container is found.
   */
  private StoreContainer getStoreContainerForSid(StoreIdentifier sid) {
    // find apropriate container for a specific sid
    if (sid != null) {
      StoreContainerType stoc_type = sid.getStoreContainerType();
      return getStoreContainerForStocType(stoc_type);
    }
    return null;
  }

  private StoreContainer getStoreContainerForStocType(StoreContainerType
      stoc_type) {
    if (stoc_type != null && containerMap.containsKey(stoc_type))
      return (StoreContainer) containerMap.get(stoc_type);
    return null;
  }

  private boolean has(StoreIdentifier sid) {
    StoreContainer stoc = getStoreContainerForSid(sid);
    return (stoc != null && stoc.has(sid)) ? true : false;
  }

  public String getConfProperty(String name) {
    if (name != null) {
      String returnValue = "";
      try {
        return ostoreConf.getProperty(name);
      }
      catch (MissingResourceException e) {
        logger.error("getConfProperty: " + e.toString());
      }
    }
    return null;
  }

  /**
   *  Method:       toString()
   *  Description:  Displays statistical information about the ObjectStore.
       *                Further information is gathered from all @see StoreContainer
   *
   *  @return       String
   */
  public String toString() {
    return toHtml(null);
  }

  public String toHtml(HttpServletRequest req) {
    float hitRatio = 0;
    long divisor = storeHit + storeMiss;
    if (divisor > 0)
      hitRatio = (float) storeHit / (float) divisor;
    hitRatio *= 100;

    StringBuffer sb = new StringBuffer("Mir-ObjectStore ");
    sb.append( ( (req != null) ? html_version() : version())).append("\n");
    sb.append("ObjectStore overall hits/misses/ratio: ").append(storeHit);
    sb.append("/").append(storeMiss).append("/").append(hitRatio);
    sb.append("%\nCurrently ").append(containerMap.size());
    sb.append(" StoreContainer in use - listing information:\n");

    // ask container for information
    StoreContainer currentStoc;
    for (Iterator it = containerMap.keySet().iterator(); it.hasNext(); ) {
      currentStoc = (StoreContainer) containerMap.get(it.next());
      sb.append(currentStoc.toHtml(req));
    }

    return sb.toString();
  }

  /**
   *  Method:       html_version()
       *  Description:  returns ObjectStore version as String for HTML representation
   *
   *  @return       String
   */
  private String html_version() {
    return "<i>" + version() + "</i>";
  }

  /**
   *  Method:       version()
   *  Description:  returns ObjectStore version as String
   *
   *  @return       String
   */
  private String version() {
    return "v_sstart3__1.0";
  }

}