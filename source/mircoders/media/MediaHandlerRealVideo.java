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
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.entity.Entity;
import mir.log.LoggerWrapper;
import mir.media.MediaExc;
import mir.media.MediaFailure;
import mir.media.MirMedia;
import mir.misc.FileUtil;
import mir.misc.StringUtil;



/**
 * Handles realVideo .it manages the ram file.
 *
 * 03.2002 - reworked Realmedia handling. -mh
 *
 * @see mir.media.MediaHandlerGeneric
 * @see mir.media.MirMedia
 * @author john <john@manifestor.org>, mh <mh@nadir.org>
 * @version $Id: MediaHandlerRealVideo.java,v 1.19.2.1 2003/09/03 17:49:40 zapata Exp $
 */


public class MediaHandlerRealVideo extends MediaHandlerVideo implements MirMedia
{
  protected LoggerWrapper logger;

  public MediaHandlerRealVideo() {
    logger = new LoggerWrapper("Media.Video.Real");
  }

  public void produce (Entity ent, Entity mediaTypeEnt) throws MediaExc, MediaFailure {
    // first see if the file exists
    super.produce(ent, mediaTypeEnt);

    String baseName = ent.getId();
    String date = ent.getValue("date");
    String datePath = StringUtil.webdbDate2path(date);
    String rtspDir = configuration.getString("Producer.RealMedia.Path");
    String rtspMediaHost = configuration.getString("Producer.RealMedia.Host");

    String RealMediaPointer = rtspMediaHost+ent.getValue("publish_path");
    String RealMediaFile = datePath+ent.getId()+".ram";
    try {
      //write an rm (ram?. -mh) file
      FileUtil.write(super.getStoragePath()+File.separator+RealMediaFile,
                      new StringReader(RealMediaPointer), "US-ASCII");
    }
    catch (Throwable e) {
      logger.error("MediaHandlerRealVideo.produce: " + e.toString());

      throw new MediaFailure(e);
    }
  }

  public List getURL(Entity ent, Entity mediaTypeEnt) {
    List theList = new Vector();

    //String stringSize = ent.getValue("size");
    //int size = Integer.parseInt(stringSize, 10)/1024;
    theList.add(ent);

    String basePath=StringUtil.regexpReplace(ent.getValue("publish_path"),
                                            ".rm$","");

    // @todo the texts ("title") below urgently need to be sanely localizaeble
    // somehow
    Map ramHash = new HashMap();
    ramHash.put("publish_path", basePath+".ram");
    ramHash.put("publish_server", configuration.getString("Producer.Media.Host"));
    ramHash.put("title", "stream URL");
    theList.add(ramHash);

    return theList;

  }

  public String getStoragePath() {
    return configuration.getString("Producer.RealMedia.Path");
  }

  public String getDescr(Entity mediaType) {
    return "RealMedia";
  }

  public String getPublishHost() {
    return StringUtil.removeSlash(configuration.getString("Producer.RealMedia.Host"));
  }

}



