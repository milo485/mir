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

import java.util.*;
import java.io.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

import com.oreilly.servlet.multipart.FilePart;

import mircoders.storage.DatabaseMediaType;
import mircoders.producer.ProducerMedia;
import mir.storage.StorageObjectException;
import mir.storage.Database;
import mir.module.ModuleException;
import mir.entity.*;
import mir.misc.*;
import mir.media.*;

/*
 *  MediaRequest.java -
 *    Takes an HTTPServletRequest from a mutltipart form and finds the files
 *    uploaded via the com.oreilly.servlet.multipart package. Finally the
 *    appropriate media objects are set.
 *
 * @author mh
 * @version $Id: MediaRequest.java,v 1.9 2002/12/01 15:05:51 zapata Exp $
 *
 */

public class MediaRequest implements FileHandler
{

  String _user;
  EntityList _returnList = new EntityList();
  boolean _produce, _publish;

  public MediaRequest(String user, boolean produce, boolean publish) {
    _user = user;
    _produce = produce;
    _publish = publish;
  }

  public EntityList getEntityList() {
    return _returnList;
  }

  /*
   * parses the files in the uploaded media and creates media Entity's out of
   * them.  Produces them if the "produce" argument is true. The "publish"
   * parameter determines if it should publish per default in the case where no
   * is_published parameter (from the upload form) is supplied. (for backwards
   * compatibility.)
   */
  public void setFile(FilePart filePart, int fileNum, HashMap mediaValues)
    throws FileHandlerException, FileHandlerUserException {

    String mediaId=null;
    MirMedia mediaHandler;
    Database mediaStorage = null;
    ProducerMedia mediaProducer = null;

    try {
      String fileName = filePart.getFileName();

      //get the content-type from what the client browser
      //sends us. (the "Oreilly method")
      String contentType = filePart.getContentType();

      //theLog.printInfo("FROM BROWSER: "+contentType);

      //if the client browser sent us unknown (text/plain is default)
      //or if we got application/octet-stream, it's possible that
      //the browser is in error, better check against the file extension
      if (contentType.equals("text/plain") ||
          contentType.equals("application/octet-stream")) {
        /**
         * Fallback to finding the mime-type through the standard ServletApi
         * ServletContext getMimeType() method.
         *
         * This is a way to get the content-type via the .extension,
         * we could maybe use a magic method as an additional method of
         * figuring out the content-type, by looking at the header (first
         * few bytes) of the file. (like the file(1) command). We could
         * also call the "file" command through Runtime. This is an
         * option that I almost prefer as it is already implemented and
         * exists with an up-to-date map on most modern Unix like systems.
         * I haven't found a really nice implementation of the magic method
         * in pure java yet.
         *
         * The first method we try thought is the "Oreilly method". It
         * relies on the content-type that the client browser sends and
         * that sometimes is application-octet stream with
         * broken/mis-configured browsers.
         *
         * The map file we use for the extensions is the standard web-app
         * deployment descriptor file (web.xml). See Mir's web.xml or see
         * your Servlet containers (most likely Tomcat) documentation.
         * So if you support a new media type you have to make sure that
         * it is in this file -mh
         */
        ServletContext ctx =
          (ServletContext)MirConfig.getPropAsObject("ServletContext");
        contentType = ctx.getMimeType(fileName);
        if (contentType==null)
          contentType = "text/plain"; // rfc1867 says this is the default
      }
      //theLog.printInfo("CONTENT TYPE IS: "+contentType);

      if (contentType.equals("text/plain") ||
          contentType.equals("application/octet-stream")) {
        _throwBadContentType(fileName, contentType);
      }

      String mediaTitle = (String)mediaValues.get("media_title"+fileNum);
      if ( (mediaTitle == null) || (mediaTitle.length() == 0)) {
        //  uncomment the next line and comment out the exception throw
        //  if you'd rather just assign missing media titles automatically
        //  mediaTitle="media item "+fileNum;
        throw new FileHandlerUserException("Missing field: media title "+mediaTitle+fileNum);
      }

      // TODO: need to add all the extra fields that can be present in the
      // admin upload form. -mh
      mediaValues.put("title", mediaTitle);
      mediaValues.put("date", StringUtil.date2webdbDate(
                                                    new GregorianCalendar()));
      mediaValues.put("to_publisher", _user);
      //mediaValues.put("to_media_folder", "7"); // op media_folder
      mediaValues.put("is_produced", "0");

      // icky backwards compatibility code -mh
      if (_publish == true) {
        mediaValues.put("is_published", "1");
      } else {
        if (!mediaValues.containsKey("is_published"))
          mediaValues.put("is_published", "0");
      }

      // @todo this should probably be moved to DatabaseMediaType -mh
      String[] cTypeSplit = StringUtil.split(contentType, "/");
      String wc = " mime_type LIKE '"+cTypeSplit[0]+"%'";

      DatabaseMediaType mediaTypeStor = DatabaseMediaType.getInstance();
      EntityList mediaTypesList = mediaTypeStor.selectByWhereClause(wc);

      String mediaTypeId = null;

      //if we didn't find an entry matching the
      //content-type int the table.
      if (mediaTypesList.size() == 0) {
       _throwBadContentType(fileName, contentType);
      }

      Entity mediaType = null;
      Entity mediaType2 = null;

      // find out if we an exact content-type match if so take it.
      // otherwise try to match majortype/*
      // @todo this should probably be moved to DatabaseMediaType -mh
      for(int j=0;j<mediaTypesList.size();j++) {
        if(contentType.equals(
              mediaTypesList.elementAt(j).getValue("mime_type")))
          mediaType = mediaTypesList.elementAt(j);
        else if ((mediaTypesList.elementAt(j).getValue("mime_type")).equals(
                  cTypeSplit[0]+"/*") )
          mediaType2= mediaTypesList.elementAt(j);
      }

      if ( (mediaType == null) && (mediaType2 == null) ) {
        _throwBadContentType(fileName, contentType);
      } else if( (mediaType == null) && (mediaType2 != null) ) {
        mediaType = mediaType2;
      }

      //get the class names from the media_type table.
      mediaTypeId = mediaType.getId();
      // ############### @todo: merge these and the getURL call into one
      // getURL helper call that just takes the Entity as a parameter
      // along with media_type
      try {
        mediaHandler = MediaHelper.getHandler(mediaType);
        mediaStorage = MediaHelper.getStorage(mediaType,
                                            "mircoders.storage.Database");
      }
      catch (MirMediaException e) {
        throw new FileHandlerException (e.getMessage());
      }
      mediaValues.put("to_media_type",mediaTypeId);

      //load the classes via reflection
      String MediaId;
      Entity mediaEnt = null;
      try {
        mediaEnt = (Entity)mediaStorage.getEntityClass().newInstance();
        if (_produce == true) {
          Class prodCls = Class.forName("mircoders.producer.Producer"+
                                        mediaType.getValue("tablename"));
          mediaProducer = (ProducerMedia)prodCls.newInstance();
        }
      } catch (Exception e) {
        throw new FileHandlerException("Error in MediaRequest: "+e.toString());
      }

      mediaEnt.setStorage(mediaStorage);
      mediaEnt.setValues(mediaValues);
      mediaId = mediaEnt.insert();

      //save and store the media data/metadata
      try {
        mediaHandler.set(filePart.getInputStream(), mediaEnt, mediaType);
      }
      catch (MirMediaException e) {
        throw new FileHandlerException(e.getMessage());
      }
      try {
        if (_produce == true )
          mediaProducer.handle(null, null, false, false, mediaId);
      } catch (ModuleException e) {
        // first try to delete it.. don't catch exception as we've already..
        try { mediaStorage.delete(mediaId); } catch (Exception e2) {}
        throw new FileHandlerException("error in MediaRequest: "+e.toString());
      }

      _returnList.add(mediaEnt);
    }
    catch (StorageObjectException e) {
      // first try to delete it.. don't catch exception as we've already..
      try { mediaStorage.delete(mediaId); } catch (Exception e2) {}
      throw new FileHandlerException("error in MediaRequest: "+e.toString());
    } //end try/catch block

  } // method setFile()

  private void _throwBadContentType (String fileName, String contentType)
    throws FileHandlerUserException {

    //theLog.printDebugInfo("Wrong file type uploaded!: " + fileName+" "
      //                    +contentType);
    throw new FileHandlerUserException("The file you uploaded is of the "
        +"following mime-type: "+contentType
        +", we do not support this mime-type. "
        +"Error One or more files of unrecognized type. Sorry");
  }

}

