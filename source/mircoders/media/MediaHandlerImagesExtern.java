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

package mircoders.media;


import java.lang.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import freemarker.template.SimpleList;

import mir.media.*;
import mir.misc.*;
import mir.entity.*;
import mir.storage.*;

import mircoders.entity.*;
import mircoders.storage.*;


/**
 * Image handler that stores images outside of the database. Will be replaced by the new
 *   media handling system.
 * @author Zapata
 * @version 1.0
 */

public class MediaHandlerImagesExtern extends MediaHandlerGeneric
{
  public void produce(Entity anImageEntity, Entity mediaTypeEnt) throws MirMediaException
  {
    try {
      String date = anImageEntity.getValue("date");
      String datePath = StringUtil.webdbDate2path(date);
      String ext = "." + mediaTypeEnt.getValue("name");
      String filePath = datePath + anImageEntity.getId() + ext;
      String iconFilePath = MirConfig.getProp("Producer.StorageRoot") + getIconStoragePath() + filePath;
      String imageFilePath = getStoragePath() + File.separator + filePath;

      File imageFile = new File(imageFilePath);
      File iconFile = new File(iconFilePath);

      if (!imageFile.exists()) {
        throw new MirMediaException("error in MediaHandlerImagesExtern.produce(): " + filePath + " does not exist!");
      }
      else {
        ImageProcessor processor = new ImageProcessor(imageFile, "JPEG");

        processor.descaleImage(50, 0.8F);
        File dir = new File(iconFile.getParent());
          if (dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        processor.writeScaledData(iconFile);

        anImageEntity.setValueForProperty("img_height", new Integer(processor.getHeight()).toString());
        anImageEntity.setValueForProperty("img_width", new Integer(processor.getWidth()).toString());

        anImageEntity.setValueForProperty("icon_height", new Integer(processor.getScaledHeight()).toString());
        anImageEntity.setValueForProperty("icon_width", new Integer(processor.getScaledWidth()).toString());

        anImageEntity.setValueForProperty("icon_path", getIconStoragePath()+filePath);
        anImageEntity.setValueForProperty("publish_path", filePath);

        anImageEntity.update();
      }
    }
    catch(Throwable t) {
      t.printStackTrace(System.out);
      throw new MirMediaException(t.getMessage());
    }
  }


  public InputStream getIcon(Entity anImageEntity) throws MirMediaException
  {
    try {
      Entity mediaType = DatabaseUploadedMedia.getInstance().getMediaType(
          anImageEntity);

      String date = anImageEntity.getValue("date");
      String datePath = StringUtil.webdbDate2path(date);
      String ext = "." + mediaType.getValue("name");
      String filePath = MirConfig.getProp("Producer.StorageRoot") +
          getIconStoragePath() + datePath + anImageEntity.getId() + ext;

      return new FileInputStream(new File(filePath));
    }
    catch (Throwable t) {
      throw new MirMediaException(t.getMessage());
    }
  }

  public String getStoragePath()
  {
    return MirConfig.getProp("Producer.Image.Path");
  }

  public String getIconStoragePath()
  {
    return MirConfig.getProp("Producer.Image.IconPath");
  }

  public String getPublishHost()
  {
    return StringUtil.removeSlash(MirConfig.getProp("Producer.Image.Host"));
  }

  public String getTinyIconName()
  {
    return MirConfig.getProp("Producer.Icon.TinyImage");
  }

  public String getBigIconName()
  {
    return MirConfig.getProp("Producer.Icon.BigImage");
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
