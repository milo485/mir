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
package  mir.media;

import java.io.InputStream;
import java.util.List;

import mir.entity.Entity;

/**
 * Interface for Media handling in Mir. All media handlers
 * must implement this interface. Each specific media type,
 * be it Gif, Jpeg, Mp3 audio, Real Audio or quicktime video
 * has special needs when it comes to representation on the various
 * pages (article, list, summary), must be stored differently and has a
 * different URL, etc... This interface allows Mir to support
 * an infinite (I hope) number of media types. Once this is done,
 * no code at any other level in Mir needs to be changed other than
 * adding the content-type <-> media handler name mapping in the
 * media_type table. The following is an example of the media_type
 * table:
 * <p>
 * id |  name   |        mime_type         | classname |   tablename   | dcname<br>
 *---+---------+--------------------------+-----------+---------------+-------<br>
 *  2 | unknown | application/octet-stream | --        | UploadedMedia | <br>
 *  3 | jpg     | image/gif                | ImagesGif | Images        | <br>
 *  4 | mp3     | audio/mp3                | Audio     | UploadedMedia | <br>
 * <p>
 * The "id" field is used as a mapping in the table that contains the media type
 * to the media_type table. For example, the images table has a to_media_type
 * field that contains the id in the media_type table.
 * <p>
 * The "name" field is used for various display/filenaming purposes. it should
 * match a valid file extension name for a media_type (we could have used the
 * content-type map for this....).
 * <p>
 * The "mime_type" field is the most important as it does maps the type to Java
 * classes (the storage and media_handler name). We call those classes using
 * reflection. This way, once a Handler for a specific media type is implemented
 * and entered into the media_type table, no other Mir code needs to be modified.
 * <p>
 * The "classname" field is the name of the media handler (e.g MediaHandlerAudio)
 * we use it to call the MediaHandler methods via reflection.
 * <p>
 * The "tablename" is the name of the database storage classes (e.g DatabaseImages
 * and EntityImages). We use this to fetch/storage the media (meta)data in the db.
 * <p?
 * The "dcname" field is as of yet unused. Do search for "Dublin Core" on google
 * to learn more.
 * <p>
 * Most media handlers should just extend MediaHandlerGeneric (i.e inherit from
 * ) and just override the things that need to be specific. see MediaHandlerAudio
 *
 * @author <mh@nadir.org>, the Mir-coders group
 * @version $Id: MirMedia.java,v 1.18.2.1 2003/09/03 17:49:38 zapata Exp $
 */

public interface  MirMedia{

  /**
   * Takes the uploaded media data itself, along with the media Entity
   * which contains the Media metadata plus the MediaType entity containing
   * all the info for the specific media type itself. It's job is store the
   * Media data (content) itself, this could be on the local filesystem, in the
   * DB or even on a remote host. It then inserts the MetaData in the DB.
   * @param InputStream, a stream of the uploaded data.
   * @param ent, an Entity holding the media MetaData
   * @param mediaType, an Entity holding the media_table entry
   * @return boolean, success/fail
   * @see mir.entity.Entity
   */
  public abstract void set (InputStream in, Entity ent, Entity mediaTypeEnt ) throws MediaExc, MediaFailure;

  public abstract void produce (Entity ent, Entity mediaTypeEnt ) throws MediaExc, MediaFailure;

  /**
   * Get's the media data from storage and returns it as an InputStream
   * Not very useful for most media types as they are stored in a file,
   * but very usefull for ones stored in the DB as it is necessary to get
   * it first before making a file out of it (in Producer*).
   * @param ent, an Entity holding the media MetaData
   * @param mediaType, an Entity holding the media_table entry
   * @return java.io.InputStream
   * @see mir.entity.Entity
   */
  public abstract InputStream getMedia (Entity ent, Entity mediaTypeEnt) throws MediaExc, MediaFailure;

  /**
   * Pretty much like get() above. But get's the specific Icon
   * representation. useful for media stored in the DB.
   * @param ent, an Entity holding the media MetaData
   * @return java.io.InputStream
   * @see mir.entity.Entity
   */
  public abstract InputStream getIcon (Entity ent) throws MediaExc, MediaFailure;


  /**
   *
   * @param ent
   * @return
   * @throws MediaExc
   * @throws MediaFailure
   */
  public abstract String getIconMimeType (Entity aMediaEntity, Entity aMediaType) throws MediaExc, MediaFailure;

  /**
   * gets the final content representation for the media
   * in the form of a URL (String) that allows someone to
   * download, look at or listen to the media. (HREF, img src
   * streaming link, etc..)
   * It should use the helper functions in the StringUtil class to
   * build URL's safely, eliminating any *illegal* user input.
   * @param ent, an Entity holding the media MetaData
   * @param mediaTypeEnt, an Entity holding the media_table entry
   * @return String, the url.
   * @see mir.entity.Entity
   * @see mir.misc.StringUtil
   */
  public abstract List getURL (Entity ent, Entity mediaTypeEnt) throws MediaExc, MediaFailure;

        /**
   * Returns the absolute filesystem path to where the media
   * content should be stored. This path is usually defined
   * in the configuration wich is accessible through the MirConfig
   * class.
   * @return String, the path.
   * @see mir.misc.MirConfig
   */
  public abstract String getStoragePath () throws MediaExc, MediaFailure;

        /**
   * Returns the *relative* filesystem path to where the media
   * icon content should be stored. It is relative to the path
   * returned by getStoragePath()
   * This path is usually defined
   * in the configuration wich is accessible through the MirConfig
   * class.
   * @return String, the path.
   * @see mir.misc.MirConfig
   */
  public abstract String getIconStoragePath () throws MediaExc, MediaFailure;

        /**
   * Returns the base URL to that the media is accessible from
   * to the end user. This could be a URL to another host.
   * This is used in the Metadata stored in the DB and later on
   * ,the templates use it.
   * It is usually defined
   * in the configuration witch is accessible through the MirConfig
   * class.
   * @return String, the base URL to the host.
   * @see mir.misc.MirConfig
   */
  public abstract String getPublishHost () throws MediaExc, MediaFailure;

        /**
   * Returns the file name of the Icon representing the media type.
   * It is used in the summary view.
   * It is usually defined
   * in the configuration wich is accessible through the MirConfig
   * class.
   * @return String, the icon filename.
   * @see mir.misc.MirConfig
   */
  public abstract String getBigIconName ();

        /**
   * Returns the file name of the small Icon representing
   * the media type.
   * It is used in the right hand newswire list of the startpage.
   * It is usually defined
   * in the configuration wich is accessible through the MirConfig
   * class.
   * @return String, the icon filename.
   * @see mir.misc.MirConfig
   */
  public abstract String getTinyIconName ();

        /**
   * Returns the IMG SRC "ALT" text to be used
   * for the Icon representations
   * @return String, the ALT text.
   */
  public abstract String getIconAltName ();

        /**
   * your can all figure it out.
   * @return boolean.
   */
  public abstract boolean isVideo ();

        /**
   * you can all figure it out.
   * @return boolean.
   */
  public abstract boolean isAudio ();

        /**
   * you can all figure it out.
   * @return boolean.
   */
  public abstract boolean isImage ();

  /**
   * returns a brief text dscription of what this
   * media type is.
   * @return String
   */
  public abstract String getDescr (Entity mediaTypeEnt);

}


