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
import java.io.FileInputStream;
import java.io.InputStream;

import mir.config.MirPropertiesConfiguration;
import mir.entity.Entity;
import mir.log.LoggerWrapper;
import mir.media.MediaExc;
import mir.media.MediaFailure;
import mir.misc.StringUtil;
import mircoders.storage.DatabaseUploadedMedia;
import mircoders.module.*;


/**
 * Image handler that stores images outside of the database. Will be replaced by the new
 *   media handling system.
 * @author Zapata
 * @version 1.0
 */

public class MediaHandlerImagesExtern extends MediaHandlerGeneric
{
  private int maxIconSize;
  private float minDescaleRatio;
  private int minDescaleReduction;

  public MediaHandlerImagesExtern() {

    logger = new LoggerWrapper("Media.Images.Extern");
    try {
      MirPropertiesConfiguration configuration = MirPropertiesConfiguration.instance();
    }
    catch (Throwable t) {
      logger.fatal("MediaHandlerImagesExtern: can't get configuration");

      throw new RuntimeException(t.toString());
    }

    maxIconSize = configuration.getInt("Producer.Image.MaxIconSize");
    minDescaleRatio = configuration.getFloat("Producer.Image.MinDescalePercentage")/100;
    minDescaleReduction = configuration.getInt("Producer.Image.MinDescaleReduction");
  }

  public void produce(Entity anImageEntity, Entity mediaTypeEnt) throws MediaExc, MediaFailure {
    try {
      String date = anImageEntity.getValue("date");
      String datePath = StringUtil.webdbDate2path(date);
      String ext = "." + mediaTypeEnt.getValue("name");
      String fileBasePath = datePath + anImageEntity.getId();
      String filePath = fileBasePath + ext;
      String iconPath = getIconStoragePath() + fileBasePath + ".jpg";
      String iconStoragePath = configuration.getString("Producer.StorageRoot") + iconPath;
      String imageFilePath = getStoragePath() + File.separator + filePath;

      File imageFile = new File(imageFilePath);
      File iconFile = new File(iconStoragePath);

      if (!imageFile.exists()) {
        throw new MediaExc("error in MediaHandlerImagesExtern.produce(): " + filePath + " does not exist!");
      }
      else {
        ImageProcessor processor = new ImageProcessor(imageFile);

        processor.descaleImage(maxIconSize, minDescaleRatio, minDescaleReduction);
        File dir = new File(iconFile.getParent());
          if (dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        processor.writeScaledData(iconFile, "JPEG");

        anImageEntity.setValueForProperty("img_height", new Integer(processor.getHeight()).toString());
        anImageEntity.setValueForProperty("img_width", new Integer(processor.getWidth()).toString());

        anImageEntity.setValueForProperty("icon_height", new Integer(processor.getScaledHeight()).toString());
        anImageEntity.setValueForProperty("icon_width", new Integer(processor.getScaledWidth()).toString());

        anImageEntity.setValueForProperty("icon_path", iconPath);
        anImageEntity.setValueForProperty("publish_path", filePath);

        anImageEntity.update();


      }
    }
    catch(Throwable t) {
      logger.error("MediaHandlerImagesExtern.produce: " + t.getMessage());
      t.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
      throw new MediaFailure(t.getMessage(), t);
    }
  }


  public InputStream getIcon(Entity anImageEntity) throws MediaExc, MediaFailure {
    try {
      String filePath =
          configuration.getString("Producer.StorageRoot") + anImageEntity.getValue("icon_path");

      logger.info(filePath);

      return new FileInputStream(new File(filePath));
    }
    catch (Throwable t) {
      return null;
    }
  }

  public String getIconMimeType(Entity anImageEntity, Entity aMediaType) {
    return "image/jpeg";
  }

  public String getStoragePath()
  {
    return configuration.getString("Producer.Image.Path");
  }

  public String getIconStoragePath()
  {
    return configuration.getString("Producer.Image.IconPath");
  }

  public String getPublishHost()
  {
    return StringUtil.removeSlash(configuration.getString("Producer.Image.Host"));
  }

  public String getTinyIconName()
  {
    return configuration.getString("Producer.Icon.TinyImage");
  }

  public String getBigIconName()
  {
    return configuration.getString("Producer.Icon.BigImage");
  }

  public String getIconAltName()
  {
    return "Image";
  }

  public boolean isVideo()
  {
    return false;
  }

  public boolean isAudio()
  {
    return false;
  }

  public boolean isImage ()
  {
    return true;
  }

  public String getDescr(Entity mediaType)
  {
     return "image/jpeg";
  }
}
