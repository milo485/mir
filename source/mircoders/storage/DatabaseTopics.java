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

package mircoders.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.entity.Entity;
import mir.entity.EntityBrowser;
import mir.log.LoggerWrapper;
import mir.storage.Database;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DatabaseTopics extends Database implements StorageObject{
  private static DatabaseTopics instance;

  public synchronized static DatabaseTopics getInstance() {
    if (instance == null) {
      instance = new DatabaseTopics();
    }
    return instance;
  }

  private DatabaseTopics() throws StorageObjectFailure {
    super();

    logger = new LoggerWrapper("Database.Topics");

    hasTimestamp = false;
    theTable = "topic";
    theEntityClass = mircoders.entity.EntityTopics.class;
  }

  public List getPopupData() throws StorageObjectFailure {
    List result = new Vector();
    Iterator i = new EntityBrowser(this, "", "title", 100, -1, 0);

    while (i.hasNext()) {
      Entity e = (Entity) i.next();
      Map entry = new HashMap();
      entry.put("key", e.getId());
      entry.put("value", e.getValue("title"));

      result.add(entry);
    }

    return result;
  }
}
