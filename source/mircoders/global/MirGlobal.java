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

package mircoders.global;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.entity.adapter.EntityAdapter;
import mir.log.LoggerWrapper;
import mir.misc.ConfigException;
import mircoders.accesscontrol.AccessControl;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.entity.EntityUsers;
import mircoders.localizer.MirAdminInterfaceLocalizer;
import mircoders.localizer.MirCachingLocalizerDecorator;
import mircoders.localizer.MirLocalizer;

public class MirGlobal {
  static private MirPropertiesConfiguration configuration;
  static private MirLocalizer localizer;
  static private ProducerEngine producerEngine;
  static private Abuse abuse;
  static private MRUCache mruCache;
  static private AccessControl accessControl;
  static private Map articleOperations;
  static private Map commentOperations;
  static private Map loggedInUsers = new HashMap();
  static private LoggerWrapper logger = new LoggerWrapper("Global");
  static private LoggerWrapper adminUsageLogger = new LoggerWrapper("AdminUsage");

  public synchronized static MirLocalizer localizer() {
    String localizerClassName;
    Class localizerClass;

    if (localizer == null ) {
      localizerClassName = config().getString("Mir.Localizer", "mirlocal.localizer.basic.MirBasicLocalizer");

      try {
        localizerClass = Class.forName(localizerClassName);
      }
      catch (Throwable t) {
        throw new ConfigException("localizer class '" + localizerClassName + "' not found: " + t.toString());
      }

      if (!(MirLocalizer.class.isAssignableFrom(localizerClass)))
        throw new ConfigException("localizer class '" + localizerClassName + "' is not assignable from MirLocalizer");

      try {
        localizer = new MirCachingLocalizerDecorator((MirLocalizer) localizerClass.newInstance());
      }
      catch (Throwable t) {
        throw new ConfigException("localizer class '" + localizerClassName + "' cannot be instantiated: " + t.toString());
      }
    }

    return localizer;
  }

  public static Abuse abuse() {
    if (abuse==null) {
      synchronized(MirGlobal.class) {
        if (abuse==null)
          abuse = new Abuse();
      }
    }

    return abuse;
  }

  public static MirPropertiesConfiguration config() {
    try {
      return MirPropertiesConfiguration.instance();
    }
    catch (PropertiesConfigExc e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public static ProducerEngine producerEngine() {
    if (producerEngine == null) {
      producerEngine = new ProducerEngine();
    }

    return producerEngine;
  }

  public static MRUCache mruCache() {
    synchronized(MirGlobal.class) {
      if (mruCache == null) {
        mruCache = new MRUCache();
      }
      return mruCache;
    }
  }

  public static synchronized AccessControl accessControl() {
    if (accessControl == null) {
      accessControl=new AccessControl();
    }

    return accessControl;
  }

  public static void performArticleOperation(EntityUsers aUser, EntityContent  anArticle, String anOperation) {
    MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation = getArticleOperationForName(anOperation);

    try {
      EntityAdapter user = null;
      if (aUser!=null)
          user = localizer().dataModel().adapterModel().makeEntityAdapter("user", aUser);

      if (operation!=null)
        operation.perform(
            user,
            localizer().dataModel().adapterModel().makeEntityAdapter("content", anArticle));
    }
    catch (Throwable t) {
      t.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

      throw new RuntimeException(t.toString());
    }
  }

  public static void performCommentOperation(EntityUsers aUser, EntityComment  aComment, String anOperation) {
    MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation = getCommentOperationForName(anOperation);

    try {
      EntityAdapter user = null;
      if (aUser!=null)
          user = localizer().dataModel().adapterModel().makeEntityAdapter("user", aUser);

      if (operation!=null)
        operation.perform(
            user,
            localizer().dataModel().adapterModel().makeEntityAdapter("comment", aComment));
    }
    catch (Throwable t) {
      throw new RuntimeException(t.toString());
    }
  }

  private synchronized static MirAdminInterfaceLocalizer.MirSimpleEntityOperation getArticleOperationForName(String aName) {
    try {
      if (articleOperations == null) {
        articleOperations = new HashMap();
        Iterator i = localizer().adminInterface().simpleArticleOperations().iterator();
        while (i.hasNext()) {
          MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation = (MirAdminInterfaceLocalizer.MirSimpleEntityOperation) i.next();
          articleOperations.put(operation.getName(), operation);
        }
      }

      return (MirAdminInterfaceLocalizer.MirSimpleEntityOperation) articleOperations.get(aName);
    }
    catch (Throwable t) {
      throw new RuntimeException(t.toString());
    }
  }

  private synchronized static MirAdminInterfaceLocalizer.MirSimpleEntityOperation getCommentOperationForName(String aName) {
    try {
      if (commentOperations == null) {
        commentOperations = new HashMap();
        Iterator i = localizer().adminInterface().simpleCommentOperations().iterator();
        while (i.hasNext()) {
          MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation = (MirAdminInterfaceLocalizer.MirSimpleEntityOperation) i.next();
          commentOperations.put(operation.getName(), operation);
        }
      }

      return (MirAdminInterfaceLocalizer.MirSimpleEntityOperation) commentOperations.get(aName);
    }
    catch (Throwable t) {
      throw new RuntimeException(t.toString());
    }
  }


  public static List getLoggedInUsers() {
    List result = new Vector();

    synchronized (loggedInUsers) {
      Iterator i = loggedInUsers.entrySet().iterator();

      while (i.hasNext()) {
        Map.Entry entry = (Map.Entry) i.next();

        Map item = new HashMap();
        item.put("name", entry.getKey());
        item.put("count", entry.getValue());
        result.add(item);
      }
    }

    return result;
  }

  public static void registerLogin(String aName) {
    modifyLoggedInCount(aName, 1);
  }

  public static void registerLogout(String aName) {
    modifyLoggedInCount(aName, -1);
  }

  private static void modifyLoggedInCount(String aName, int aModifier) {
    synchronized (loggedInUsers) {
      Integer count = (Integer) loggedInUsers.get(aName);
      if (count==null)
        count = new Integer(0);

      if (count.intValue()+aModifier<=0) {
        loggedInUsers.remove(aName);
      }
      else {
        loggedInUsers.put(aName, new Integer(count.intValue() + aModifier));
      }
    }
  }

  public static void logAdminUsage(EntityUsers aUser, String anObject, String aDescription) {
    try {
      if (config().getString("Mir.Admin.LogAdminActivity", "0").equals("1")) {
        String user = "unknown (" + aUser.toString() + ")";
        if (aUser != null)
          user = aUser.getValue("login");
        adminUsageLogger.info(user + " | " + anObject + " | " + aDescription);
      }
    }
    catch (Throwable t) {
      logger.error("Error while logging admin usage ("+aUser.toString()+", "+aDescription+"): " +t.toString());
    }
  }
}


