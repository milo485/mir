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
package  mircoders.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.servlet.ServletContext;

import freemarker.template.SimpleList;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration$PropertiesConfigExc;
import mir.entity.Entity;
import mir.log.LoggerWrapper;
import mir.media.MediaExc;
import mir.media.MediaFailure;
import mir.media.MirMedia;
import mir.misc.FileUtil;
import mir.misc.StringUtil;


/**
 * This is the Generic MediaHandler. It stores the media data on
 * the filesystem and keeps basic metadata  (size, type...) in the
 * DB. Usually only representation needs to be overridden.
 * See the MediaHandlerAudio class to see an example of how one
 * could override it.
 * <p>
 * Most media handlers should override this class.
 * <p>
 * In theory, it could be used to handle miscellaneous media that
 * we don't have entered in the media_type table, (like RTF documents,
 * PS, PDF, etc..)
 * <p>
 * Of course it implements the MirMedia interface.
 *
 * @see mir.media.MirMedia
 * @author mh <mh@nadir.org>
 * @version $Id: MediaHandlerGeneric.java,v 1.20 2003/04/29 02:36:50 zapata Exp $
 */

public class MediaHandlerGeneric implements MirMedia
{
    protected static MirPropertiesConfiguration configuration;
    protected static String imageHost;
    protected static String imageRoot;

    protected LoggerWrapper logger;

    static {
      try {
        configuration = MirPropertiesConfiguration.instance();
      }
      catch (PropertiesConfigExc e) {
      }
      imageHost = configuration.getString("Producer.Image.Host");
      imageRoot = configuration.getString("Producer.ImageRoot");
    }

    public MediaHandlerGeneric() {
      logger = new LoggerWrapper("Media.Generic");
    }

    public void set (InputStream in, Entity ent, Entity mediaTypeEnt ) throws MediaExc, MediaFailure {
      String ext = mediaTypeEnt.getValue("name");
      String mediaFname = ent.getId() + "." + ext;
      String date = ent.getValue("date");
      String datePath = StringUtil.webdbDate2path(date);
      try {
        long size = FileUtil.write(getStoragePath() + File.separator + datePath +
                                   File.separator + mediaFname, in);
        ent.setValueForProperty("publish_path", datePath + mediaFname);
        ent.setValueForProperty("size", new Long(size).toString());
        ent.update();
      }
      catch (Throwable e) {
        logger.error("MediaHandlerGeneric.set: " + e.toString());
        throw new MediaFailure(e);
      }
    }

    public void produce (Entity ent, Entity mediaTypeEnt ) throws MediaExc, MediaFailure {
      //check first if the media file exist since produced
      //location is also the storage location

      String date = ent.getValue("date");
      String datePath = StringUtil.webdbDate2path(date);
      String relPath = datePath+ent.getId()+"."+mediaTypeEnt.getValue("name");
      String fname = getStoragePath()+relPath;
      if(! new File(fname).exists())
        throw new MediaExc("error in MirMedia.produce(): " + relPath + " does not exist!");
    }

    public InputStream getMedia (Entity ent, Entity mediaTypeEnt) throws MediaExc, MediaFailure {
      String publishPath = ent.getValue("publish_path");
      String fname = getStoragePath()+publishPath;
      File f = new File(fname);
      if(! f.exists())
        throw new MediaExc("error in MirMedia.getMedia(): " + fname + " does not exist!");

      FileInputStream inputStream;
      try {
        inputStream = new FileInputStream(f);
      }
      catch (Throwable e) {
        throw new MediaFailure("MediaHandlerGeneric.getMedia(): " + e.toString(), e);
      }

      return inputStream;
    }

    public InputStream getIcon (Entity ent) throws MediaExc, MediaFailure {
      return null;
    }

    public String getIconMimeType (Entity aMediaEntity, Entity aMediaType) throws MediaExc, MediaFailure {
      ServletContext servletContext = MirPropertiesConfiguration.getContext();
      String fileName = aMediaEntity.getId()+"."+aMediaType.getValue("name");

      return servletContext.getMimeType(fileName);
    };

    public String getStoragePath()
    {
        return configuration.getString("Producer.Media.Path");
    }

    public String getIconStoragePath()
    {
        return configuration.getString("Producer.Image.IconPath");
    }

    public String getPublishHost()
    {
        return StringUtil.removeSlash(configuration.getString("Producer.Media.Host"));
    }

    public String getTinyIconName()
    {
        return configuration.getString("Producer.Icon.TinyText");
    }

    public String getBigIconName()
    {
        return configuration.getString("Producer.Icon.BigText");
    }

    public String getIconAltName()
    {
        return "Generic media";
    }

    public SimpleList getURL(Entity ent, Entity mediaTypeEnt)
    {
      SimpleList theList = new SimpleList();
      theList.add(ent);
      return theList;
    }

    public boolean isVideo()
    {
      return false;
    }

    public boolean isAudio()
    {
      return false;
    }

    public boolean isImage()
    {
      return false;
    }

    public String getDescr( Entity mediaType)
    {
      return mediaType.getValue("mime_type");
    }

}



