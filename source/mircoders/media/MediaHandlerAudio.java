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

import mir.media.MirMedia;

/**
 * Handles audio media, like mp3 and maybe it could also handle some other.
 * It is MediaHandlerGeneric with different icons.
 *
 * @see mir.media.MediaHandlerGeneric
 * @see mir.media.MirMedia
 * @author mh <mh@nadir.org>
 * @version $Id: MediaHandlerAudio.java,v 1.7 2003/01/25 17:50:35 idfx Exp $
 */

public class MediaHandlerAudio extends MediaHandlerGeneric implements MirMedia
{

  private static String tinyIcon;
  private static String bigIcon;
  	
	static {
    tinyIcon = configuration.getString("Producer.Icon.TinyAudio");
    bigIcon = configuration.getString("Producer.Icon.BigAudio");
	}

  public String getTinyIcon()
  {
    return tinyIcon;
  }

  public String getBigIconName()
  {
    return bigIcon;
  }

  public String getIconAlt()
  {
    return "Audio";
  }

  public boolean isAudio()
  {
    return true;
  }

}
