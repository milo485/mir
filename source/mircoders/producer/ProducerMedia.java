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

package mircoders.producer;

import java.io.*;
import java.lang.*;
import java.util.*;

//import freemarker.template.*;

import mir.misc.*;
import mir.storage.*;
import mir.module.*;
import mir.entity.*;
import mir.media.*;

import mircoders.media.*;
import mircoders.entity.*;
import mircoders.storage.*;

abstract public class ProducerMedia extends Producer {

  abstract Database getStorage() throws StorageObjectException;

  public void handle(PrintWriter htmlout, EntityUsers user, boolean force,
    boolean sync) throws StorageObjectException, ModuleException {
    handle(htmlout,user,force,sync,null);
  }

  public void handle(PrintWriter htmlout,EntityUsers user,boolean force,
    boolean sync, String id) throws StorageObjectException, ModuleException
  {
    long                sessionConnectTime = 0;
    long                startTime = (new java.util.Date()).getTime();
    String              whereClause;
    String              orderBy;
    Entity              currentMedia;
    MirMedia            currentMediaHandler;
    EntityList          batchEntityList;

    int contentBatchsize =
            Integer.parseInt(MirConfig.getProp("Producer.Content.Batchsize"));
    orderBy = "webdb_lastchange desc";

    // get batch of non-produced medias, that are to be published
    whereClause="is_published='1'";
    if (id!= null) {
      whereClause += " and id="+id;
      // optimization to avoid select count(*)..
      contentBatchsize = -1;
    }
    if (force==false) whereClause += " and is_produced='0'";

    batchEntityList = getStorage().selectByWhereClause(whereClause,
                                                orderBy, 0, contentBatchsize);

    while (batchEntityList != null) {
      for(int i=0;i<batchEntityList.size();i++) {
        currentMedia = (Entity)batchEntityList.elementAt(i);
        try {
          Entity currentMediaType =
                DatabaseUploadedMedia.getInstance().getMediaType(currentMedia);
          currentMediaHandler = MediaHelper.getHandler( currentMediaType );

          // now produce
          currentMediaHandler.produce(currentMedia,currentMediaType);
          currentMedia.setValueForProperty("publish_server",
                                        currentMediaHandler.getPublishHost());
          currentMedia.setValueForProperty("icon_is_produced", "1");
          currentMedia.setValueForProperty("is_produced", "1");
          currentMedia.update();
          logHTML(htmlout,"produced media id "+currentMedia.getId()
                  +": "+currentMediaType.getValue("mime_type")+" success");
        } catch (Exception e) {
          // don't throw and exception here, just log.
          // we don't want to make the admin interface unuseable
          theLog.printError("media exception: "+currentMedia.getId()+
                            e.toString());
          logHTML(htmlout, "problem with media id: "+currentMedia.getId()+
                  " <font color=\"Red\"> failed!</font>: "+e.toString());
          e.printStackTrace(htmlout);
        }
      }

      // if next batch get it...
      if (batchEntityList.hasNextBatch()){
        batchEntityList = uploadedMediaModule.getByWhereClause(whereClause,
          orderBy, batchEntityList.getNextBatch(),contentBatchsize);
      } else {
        batchEntityList=null;
      }
    }
    // Finish
    sessionConnectTime = new java.util.Date().getTime() - startTime;
    logHTML(htmlout, "Producer.Media finished: " + sessionConnectTime + " ms.");
  }

}
