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

import java.util.Map;

import mir.entity.Entity;
import mir.entity.adapter.EntityAdapter;
import mir.log.LoggerWrapper;
import mir.media.MediaHelper;
import mir.media.MirMedia;
import mir.producer.ProducerExc;
import mir.producer.ProducerNode;
import mir.util.ParameterExpander;
import mircoders.entity.EntityUploadedMedia;
import mircoders.storage.DatabaseUploadedMedia;

public class MediaGeneratingProducerNode implements ProducerNode {
  private String mediaEntityKey;

  public MediaGeneratingProducerNode(String aMediaEntityKey) {
    mediaEntityKey = aMediaEntityKey;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) {
    Object data;
    Entity entity;
    EntityUploadedMedia uploadedMediaEntity = null;
    Entity mediaType = null;
    MirMedia currentMediaHandler;

    try {

      data = ParameterExpander.findValueForKey( aValueMap, mediaEntityKey );

      if (!(data instanceof EntityAdapter)) {
        throw new ProducerExc("MediaGeneratingProducerNode: value of '"+mediaEntityKey+"' is not an EntityAdapter, but an " + data.getClass().getName());
      }

      entity = ((EntityAdapter) data).getEntity();
      if (! (entity instanceof EntityUploadedMedia)) {
        throw new ProducerExc("MediaGeneratingProducerNode: value of '"+mediaEntityKey+"' is not an uploaded media EntityAdapter, but a " + entity.getClass().getName() + " adapter");
      }

      uploadedMediaEntity = (EntityUploadedMedia) entity;

      mediaType = DatabaseUploadedMedia.getInstance().getMediaType(entity);

      currentMediaHandler = MediaHelper.getHandler( mediaType );
      currentMediaHandler.produce(entity, mediaType);
      entity.setValueForProperty("publish_server", currentMediaHandler.getPublishHost());
      entity.setValueForProperty("icon_is_produced", "1");
      entity.setValueForProperty("is_produced", "1");
      entity.update();

      aLogger.info("media with id "+uploadedMediaEntity.getValue("id") + ", mediaType " + mediaType.getValue("name") + " successfully produced");
    }
    catch (Throwable t) {
      String message = "Error while generating media";
      try {
        if (uploadedMediaEntity!=null)
          message = message +  " with id "+uploadedMediaEntity.getValue("id");
        if (mediaType!=null) {
          message = message + ", mediaType " + mediaType.getValue("name");
        }
      }
      catch (Throwable s) {
      }

      message = message + ": " + t.getMessage();
      aLogger.error(message);
    }
  }
}

