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
 * Please note: this media handler produces
 * 3 media files, the raw .mp3, a .m3u which is
 * contains the URL for the mp3 and a .pls which
 * contains the URL to the mp3 in shoutcast playlist
 * format. What's important is that the web server (of
 * the media host) must recognize the .m3u and .pls file
 * extensions and send the proper "audio/x-mpegurl"
 * and "audio/x-scpls" mime-types respectively.
 * If the web server is apache, it's easy, just
 * add:
 *
 * audio/x-mpegurl                 m3u
 * audio/x-scpl                    pls
 *
 * to the file pointed to by the "TypesConfig"
 * command in your apache config file. Or add
 * and equivalent AddType command to your httpd.conf.
 * Of course this assumes that the mod_mime is loaded.
 *
 * If the web server is not apache, then your on your own.
 *
 * @see mir.media.MirMedia
 * @author mh <mh@nadir.org>
 * @version $Id: MediaHandlerMp3.java,v 1.15.2.1 2003/09/03 17:49:39 zapata Exp $
 */

public class MediaHandlerMp3 extends MediaHandlerAudio implements MirMedia
{
  protected LoggerWrapper logger;

  public MediaHandlerMp3() {
    logger = new LoggerWrapper("Media.Audio.Mp3");
  }

  public void produce(Entity ent, Entity mediaTypeEnt) throws MediaExc, MediaFailure {

    // first check if the file exists
    super.produce(ent, mediaTypeEnt);

    String baseName = ent.getId();
    String date = ent.getValue("date");
    String datePath = StringUtil.webdbDate2path(date);
    String mp3Pointer = getPublishHost() + ent.getValue("publish_path");
    String mpegURLFile = baseName + ".m3u";
    String playlistFile = baseName + ".pls";

    try {
      //write the "meta" files
      //first the .m3u since it only contains one line
      FileUtil.write(getStoragePath() + "/" + datePath + "/" + mpegURLFile,
                     new StringReader(mp3Pointer), "US-ASCII");
      //now the .pls file
      FileUtil.write(getStoragePath() + "/" + datePath + "/" + playlistFile,
                     new StringReader(mp3Pointer), "US-ASCII");
    }
    catch (Throwable e) {
      logger.error("MediaHandlerMp3.produce: " + e.toString());

      throw new MediaFailure(e);
    }
  }

  public List getURL(Entity ent, Entity mediaTypeEnt) {
    List theList = new Vector();

    //String stringSize = ent.getValue("size");
    //int size = Integer.parseInt(stringSize, 10)/1024;
    theList.add(ent);

    String basePath = StringUtil.regexpReplace(ent.getValue("publish_path"),
                                               ".mp3$", "");

    // @todo the texts ("title") below urgently need to be sanely localizaeble
    // somehow
    Map m3uHash = new HashMap();
    m3uHash.put("publish_path", basePath + ".m3u");
    m3uHash.put("publish_server", ent.getValue("publish_server"));
    m3uHash.put("title", "stream URL");
    theList.add(m3uHash);

    Map plsHash = new HashMap();
    plsHash.put("publish_path", basePath + ".pls");
    plsHash.put("publish_server", ent.getValue("publish_server"));
    plsHash.put("title", "playlist URL");
    theList.add(plsHash);

    return theList;

  }

  public String getDescr(Entity mediaType) {
    return "mp3";
  }
}



