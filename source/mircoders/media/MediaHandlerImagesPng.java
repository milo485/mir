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

import mir.entity.Entity;
import mir.media.MirMedia;

/**
 * This class handles saving, fetching creating representations
 * for all png images. The image content is stored in the DB. The content is
 * written out to a file at the ProducerImages level.
 * It implements the MirMedia interface.
 * <p>
 *
 * @see mir.media.MirMedia
 * @see mircoders.media.MediaHandlerImages
 * @author mh ,mir-coders
 * @version $Id: MediaHandlerImagesPng.java,v 1.5 2003/01/25 17:50:35 idfx Exp $
 */


public class MediaHandlerImagesPng extends MediaHandlerImages implements MirMedia
{
  public String getType() {
    return PNG;
  }

  public String getDescr(Entity mediaType)
  {
      return "image/png";
  }

}
