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

package mircoders.entity;

import freemarker.template.SimpleList;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import mir.entity.Entity;
import mir.media.MediaHelper;
import mir.media.MirMedia;
import mir.storage.StorageObject;
import mir.storage.StorageObjectException;
import mircoders.storage.DatabaseUploadedMedia;

import java.util.HashMap;

/**
 * Diese Klasse enthält die Daten eines MetaObjekts
 *
 * @author RK
 * @version 29.6.1999
 */


public class EntityUploadedMedia extends Entity {


  public EntityUploadedMedia() {
    super();
  }

  public EntityUploadedMedia(StorageObject theStorage) {
    this();
    setStorage(theStorage);
  }

  public void setValues(HashMap theStringValues) {
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
  public Entity getMediaType() throws StorageObjectException {
    Entity ent = null;
    try {
      ent = DatabaseUploadedMedia.getInstance().getMediaType(this);
    }
    catch (StorageObjectException e) {
      throwStorageObjectException(e, "get MediaType failed -- ");
    }
    return ent;
  }

  public String getValue(String key) {
    String returnValue = null;

    if (key != null) {
      if (key.equals("big_icon"))
        returnValue = getBigIconName();
      else if (key.equals("descr"))
        returnValue = getDescr();
      else if (key.equals("mediatype"))
        returnValue = getMediaTypeString();
      else if (key.equals("mimetype"))
        returnValue = getMimeType();
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
      theLog.printWarning("-- getMediaTypeString: could not fetch data "
                          + this.getClass().toString() + " " + ex.toString());
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
      theLog.printWarning("-- getBigIconName: could not fetch data "
                          + this.getClass().toString() + " " + ex.toString());
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
    catch (Exception ex) {
      theLog.printWarning("-- getUrl: could not fetch data "
                          + this.getClass().toString() + " " + ex.toString());
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
      theLog.printWarning("-- getDescr: could not fetch data "
                          + this.getClass().toString() + " " + ex.toString());
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
      theLog.printWarning("-- getBigIconName: could not fetch data "
                          + this.getClass().toString() + " " + ex.toString());
    }
    return null;
  }

}
