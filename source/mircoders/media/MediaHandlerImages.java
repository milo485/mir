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
import mir.storage.StorageObjectException;
import mircoders.entity.EntityImages;

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
 * @version $Date: 2002/11/04 04:35:22 $ $Revision: 1.11 $
 */


public abstract class MediaHandlerImages implements MirMedia
{
  static Logfile theLog = Logfile.getInstance(MirConfig.getProp("Home")+
                                                "log/media.log");
  static final String PNG = "PNG"; 
  static final String JPEG = "JPEG"; 

  abstract String getType();

	public InputStream getMedia(Entity ent, Entity mediaTypeEnt)
    throws MirMediaException
	{
    InputStream in;
    try {
      in = ((EntityImages)ent).getImage();
    } catch ( StorageObjectException e) {
      theLog.printDebugInfo("MediaHandlerImages.getImage: "+e.toString()); 
      throw new MirMediaException(e.toString());
    }

    return in;
  }

	public void set(InputStream in, Entity ent, Entity mediaTypeEnt)
    throws MirMediaException {

    try {
      ((EntityImages)ent).setImage(in, getType());
    } catch ( StorageObjectException e) {
      theLog.printError("MediaHandlerImages.set: "+e.toString()); 
      throw new MirMediaException(e.toString());
    }

	}

  public void produce(Entity ent, Entity mediaTypeEnt) throws MirMediaException
  {
    String date = ent.getValue("date");
    String datePath = StringUtil.webdbDate2path(date);
    String ext = "."+mediaTypeEnt.getValue("name");
    String filepath = datePath+ent.getId()+ext;
    String iconFilePath = MirConfig.getProp("Producer.StorageRoot")
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
      } catch ( Exception e) {
        String msg = "MediaHandlerImages.produce - Error: " + e.toString();
        theLog.printError(msg);
        throw new MirMediaException(msg);
      }
    } else {
      String msg="MediaHandlerImages.produce - missing image or icon OID for: "+
                  ent.getId();
      theLog.printError(msg);
      throw new MirMediaException(msg);
    }
  }
                        

	public InputStream getIcon(Entity ent) throws MirMediaException
	{
    InputStream in;
    try {
      in = ((EntityImages)ent).getIcon();
    } catch ( StorageObjectException e) {
      theLog.printDebugInfo("MediaHandlerImages.getIcon: "+e.toString()); 
      throw new MirMediaException(e.toString());
    }

    return in;
  }

  public SimpleList getURL(Entity ent, Entity mediaTypeEnt)
  {
    SimpleList theList = new SimpleList();
    theList.add(ent);
    return theList;
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
    return MirConfig.getProp("Producer.Image.Host");
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
    return "";
  }

}
