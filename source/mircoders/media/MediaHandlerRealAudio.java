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

package  mircoders.media;

import java.util.*;
import java.io.StringReader;

import freemarker.template.SimpleList;
import freemarker.template.SimpleHash;

import mir.media.*;
import mir.entity.*;
import mir.misc.*;
import mir.storage.*;



/**
 * Handles realAudio .it manages the ram file.
 *
 * 03.2002 - reworked Realmedia handling. -mh
 *
 * @see mir.media.MediaHandlerGeneric
 * @see mir.media.MirMedia
 * @author john <john@manifestor.org>, mh <heckmann@hbe.ca>
 * @version 11.10.2001
 */


public class MediaHandlerRealAudio extends MediaHandlerAudio implements
  MirMedia
{
  public void produce (Entity ent, Entity mediaTypeEnt )
    throws MirMediaException {

    // first see if the file exists
    super.produce(ent, mediaTypeEnt);

    String baseName = ent.getId();
    String date = ent.getValue("date");
    String datePath = StringUtil.webdbDate2path(date);
    String rtspDir = MirConfig.getProp("Producer.RealMedia.Path");
    String rtspMediaHost = MirConfig.getProp("Producer.RealMedia.Host");

    String RealMediaPointer = rtspMediaHost+ent.getValue("publish_path");
    String RealMediaFile = datePath+ent.getId()+".ram";
    try {
      //write an rm (ram?. -mh) file
      FileUtil.write(super.getStoragePath()+"/"+RealMediaFile,
                      new StringReader(RealMediaPointer), "US-ASCII");
    } catch (Exception e) {
      theLog.printError(e.toString());
      throw new MirMediaException(e.toString());
    }
  }

  public SimpleList getURL(Entity ent, Entity mediaTypeEnt)
  {
    SimpleList theList = new SimpleList();

    //String stringSize = ent.getValue("size");
    //int size = Integer.parseInt(stringSize, 10)/1024;
    theList.add(ent);
   
    String basePath=StringUtil.regexpReplace(ent.getValue("publish_path"),
                                            ".ra$","");

    // @todo the texts ("title") below urgently need to be sanely localizaeble
    // somehow
    SimpleHash ramHash = new SimpleHash();
    ramHash.put("publish_path", basePath+".ram");
    ramHash.put("publish_server", MirConfig.getProp("Producer.Media.Host"));
    ramHash.put("title", "stream URL");
    theList.add(ramHash);

    return theList;

  }

  public String getStoragePath()
  {
    return MirConfig.getProp("Producer.RealMedia.Path");
  }

  public String getDescr(Entity mediaType)
  {
    return "RealMedia Audio";
  }

  public String getPublishHost()
  {
    return MirConfig.getProp("Producer.RealMedia.Host");
  }

}
        
        

