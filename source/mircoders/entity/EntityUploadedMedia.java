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
package mircoders.entity;

import java.sql.SQLException;
import java.util.Map;

import mir.entity.Entity;
import mir.log.LoggerWrapper;
import mir.media.MediaHelper;
import mir.media.MirMedia;
import mir.misc.NumberUtils;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;
import mircoders.storage.DatabaseUploadedMedia;
import freemarker.template.SimpleList;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 *
 * @author mh, mir-coders group
 * @version $Id: EntityUploadedMedia.java,v 1.26 2003/04/21 12:42:53 idfx Exp $
 */


public class EntityUploadedMedia extends Entity {


  public EntityUploadedMedia() {
    super();

    logger = new LoggerWrapper("Entity.UploadedMedia");
  }

  public EntityUploadedMedia(StorageObject theStorage) {
    this();
    setStorage(theStorage);
  }

  public void update() throws StorageObjectFailure {
    super.update();
    try {
      theStorageObject.executeUpdate("update content set is_produced='0' where exists(select * from content_x_media where to_content=content.id and to_media=" + getId()+")");
    }
    catch (SQLException e) {
      throwStorageObjectFailure(e, "EntityAudio :: update :: failed!! ");
    }
  }

  public void setValues(Map theStringValues) {
    if (theStringValues != null) {
      if (!theStringValues.containsKey("is_published"))
        theStringValues.put("is_published", "0");
    }
    super.setValues(theStringValues);
  }


  /**
   * fetches the MediaType entry assiciated w/ this media
   *
   * @return mir.entity.Entity
   */
  public Entity getMediaType() throws StorageObjectFailure {
    Entity ent = null;
    try {
      ent = DatabaseUploadedMedia.getInstance().getMediaType(this);
    }
    catch (StorageObjectFailure e) {
      throwStorageObjectFailure(e, "get MediaType failed -- ");
    }
    return ent;
  }

  public String getValue(String key) {
    String returnValue = null;

    if (key != null) {
      if (key.equals("big_icon"))
        returnValue = getBigIconName();
      else if (key.equals("descr") || key.equals("media_descr"))
        returnValue = getDescr();
      else if (key.equals("mediatype"))
        returnValue = getMediaTypeString();
      else if (key.equals("mimetype"))
        returnValue = getMimeType();
      else if (key.equals("human_readable_size")) {
        String size = super.getValue("size");
        if (size != null)
          returnValue = NumberUtils.humanReadableSize(Double.parseDouble(size));
      }
      else
        returnValue = super.getValue(key);
    }
    return returnValue;
  }

  public TemplateModel get(java.lang.String key) throws TemplateModelException {
    if (key.equals("url"))
      return getUrl();
    return new SimpleScalar(getValue(key));
  }

  // @todo  all these methods should be merged into 1
  // and the MediaHandler should be cached somehow.
  private String getMediaTypeString() {
    MirMedia mediaHandler = null;
    Entity mediaType = null;

    try {
      mediaType = getMediaType();
      mediaHandler = MediaHelper.getHandler(mediaType);
      String t;
      if (mediaHandler.isAudio())
        return "audio";
      else if (mediaHandler.isImage())
        return "image";
      else if (mediaHandler.isVideo())
        return "video";
      else
        return "other";
    }
    catch (Exception ex) {
      logger.warn("EntityUploadedMedia.getMediaTypeString: could not fetch data: " + ex.toString());
    }
    return null;
  }

  private String getBigIconName() {
    MirMedia mediaHandler = null;
    Entity mediaType = null;

    try {
      mediaType = getMediaType();
      mediaHandler = MediaHelper.getHandler(mediaType);
      return mediaHandler.getBigIconName();
    }
    catch (Exception ex) {
      logger.warn("EntityUploadedMedia.getBigIconName: could not fetch data: " + ex.toString());
    }
    return null;
  }

  private SimpleList getUrl() {
    MirMedia mediaHandler = null;
    Entity mediaType = null;

    try {
      mediaType = getMediaType();
      mediaHandler = MediaHelper.getHandler(mediaType);
      return mediaHandler.getURL(this, mediaType);
    }
    catch (Throwable t) {
      logger.warn("EntityUploadedMedia.getUrl: could not fetch data: " + t.toString());
    }
    return null;
  }

  private String getDescr() {
    MirMedia mediaHandler = null;
    Entity mediaType = null;

    try {
      mediaType = getMediaType();
      mediaHandler = MediaHelper.getHandler(mediaType);
      return mediaHandler.getDescr(mediaType);
    }
    catch (Exception ex) {
      logger.warn("EntityUploadedMedia.getDescr: could not fetch data: " + ex.toString());
    }
    return null;
  }
  private String getMimeType() {
    Entity mediaType = null;

    try {
      mediaType = getMediaType();
      return mediaType.getValue("mime_type");
    }
    catch (Exception ex) {
      logger.warn("EntityUploadedMedia.getBigIconName: could not fetch data: " + ex.toString());
    }
    return null;
  }

}
