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
package mircoders.media;

import java.io.File;
import java.io.InputStream;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.entity.Entity;
import mir.log.LoggerWrapper;
import mir.media.MediaExc;
import mir.media.MediaFailure;
import mir.media.MirMedia;
import mir.misc.FileUtil;
import mir.misc.StringUtil;
import mircoders.entity.EntityImages;
import freemarker.template.SimpleList;

/**
 * This class handles saving, fetching creating representations
 * for all images. The image content is stored in the DB. The content is
 * written out to a file at the ProducerImages level.
 * Remember that Handlers for specific image types, Gif, Jpeg, etc..
 * should override it.
 * It implements the MirMedia interface.
 * <p>
 * slowly starting to look better, a next step would be to have the
 * representation stuff (WebdbImage) happen here.
 * -mh 01.03.2002
 *
 * @see mir.media.MirMedia
 * @author mh
 * @version $Id: MediaHandlerImages.java,v 1.22 2003/04/21 12:42:48 idfx Exp $
 */


public abstract class MediaHandlerImages implements MirMedia
{
  protected static MirPropertiesConfiguration configuration;
  protected static final String PNG = "PNG";
  protected static final String JPEG = "JPEG";

  protected LoggerWrapper logger;

  static {
    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (PropertiesConfigExc e) {
      throw new RuntimeException("Can't get configuration: " + e.getMessage());
    }
  }

  abstract String getType();

  public MediaHandlerImages() {
    logger = new LoggerWrapper("Media.Images");
  }

  public InputStream getMedia(Entity ent, Entity mediaTypeEnt) throws MediaExc, MediaFailure {
    InputStream inputStream;

    try {
      inputStream = ((EntityImages)ent).getImage();
    }
    catch (Throwable e) {
      logger.error("MediaHandlerImages.getImage: " + e.toString());

      throw new MediaFailure(e);
    }

    return inputStream;
  }

  public void set(InputStream in, Entity ent, Entity mediaTypeEnt) throws MediaExc, MediaFailure {

    try {
      ((EntityImages)ent).setImage(in, getType());
    }
    catch (Throwable e) {
      logger.error("MediaHandlerImages.set: "+e.getMessage());
      e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));

      throw new MediaFailure(e);
    }
  }

  public void produce(Entity ent, Entity mediaTypeEnt) throws MediaExc, MediaFailure {
    String date = ent.getValue("date");
    String datePath = StringUtil.webdbDate2path(date);
    String ext = "."+mediaTypeEnt.getValue("name");
    String filepath = datePath+ent.getId()+ext;
    String iconFilePath = configuration.getString("Producer.StorageRoot")
                          +getIconStoragePath() + filepath;
    String productionFilePath = getStoragePath() + File.separator + filepath;


    if (ent.getValue("icon_data")!= null &&
        ent.getValue("image_data")!= null) {
      // make icon
      try {
        InputStream in = ((EntityImages)ent).getIcon();
        FileUtil.write(iconFilePath, in);
        in = ((EntityImages)ent).getImage();
        FileUtil.write(productionFilePath, in);
        ent.setValueForProperty("icon_path",getIconStoragePath()+filepath);
        ent.setValueForProperty("publish_path",filepath);
        ent.update();
      }
      catch (Throwable e) {
        logger.error("MediaHandlerImages.produce: " + e.toString());
        throw new MediaFailure("MediaHandlerImages.produce: " + e.toString(), e);
      }
    }
    else {
      logger.error("MediaHandlerImages.produce: missing image or icon OID for: " + ent.getId());

      throw new MediaExc("MediaHandlerImages.produce: missing image or icon OID for: " + ent.getId());
    }
  }


  public InputStream getIcon(Entity ent) throws MediaExc, MediaFailure {
    InputStream in;
    try {
      in = ((EntityImages)ent).getIcon();
    }
    catch (Throwable e) {
      logger.error("MediaHandlerImages.getIcon: " + e.toString());
      throw new MediaFailure(e);
    }

    return in;
  }

  public SimpleList getURL(Entity ent, Entity mediaTypeEnt) {
    SimpleList theList = new SimpleList();
    theList.add(ent);
    return theList;
  }

  public String getStoragePath() {
    return configuration.getString("Producer.Image.Path");
  }

  public String getIconStoragePath() {
    return configuration.getString("Producer.Image.IconPath");
  }

  public String getPublishHost() {
    return StringUtil.removeSlash(configuration.getString("Producer.Image.Host"));
  }

  public String getTinyIconName() {
    return configuration.getString("Producer.Icon.TinyImage");
  }

  public String getBigIconName() {
    return configuration.getString("Producer.Icon.BigImage");
  }

  public String getIconAltName() {
    return "Image";
  }

  public boolean isVideo() {
    return false;
  }

  public boolean isAudio() {
    return false;
  }

  public boolean isImage () {
    return true;
  }

  public String getDescr(Entity mediaType) {
    return "image/jpeg";
  }

}
