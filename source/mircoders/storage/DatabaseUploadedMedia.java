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

import java.util.Iterator;

import mir.entity.Entity;
import mir.entity.EntityBrowser;
import mir.log.LoggerWrapper;
import mir.storage.Database;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;

public class DatabaseUploadedMedia extends Database implements StorageObject {
  private static DatabaseUploadedMedia  instance;

  public synchronized static DatabaseUploadedMedia getInstance() {
    if (instance == null ) {
      instance = new DatabaseUploadedMedia();
    }

    return instance;
  }

  private DatabaseUploadedMedia() {
    super();

    logger = new LoggerWrapper("Database.UploadedMedia");

    theTable="uploaded_media";
    theCoreTable="media";
    theEntityClass = mircoders.entity.EntityUploadedMedia.class;
  }


  /**
   * returns the media_type that belongs to the media item (via entityrelation)
   * where db-flag is_published is true
   */
  public Entity getMediaType(Entity ent) throws StorageObjectFailure {
    Entity type=null;
    try {
      Iterator i = new EntityBrowser(DatabaseMediaType.getInstance(), ent.getValue("to_media_type") + " = id" , "id", 1);
      if (i.hasNext())
        type = (Entity) i.next();
    }
    catch (Throwable t) {
      logger.error("DatabaseUploadedMedia :: failed to get media_type: " + t.getMessage());

      throw new StorageObjectFailure("DatabaseUploadedMedia :: failed to get media_type", t);
    }
    return type;
  }

}
