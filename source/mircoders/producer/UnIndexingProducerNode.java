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

import java.io.PrintWriter;
import java.util.Map;

import mir.entity.Entity;
import mir.entity.adapter.EntityAdapter;
import mir.log.LoggerToWriterAdapter;
import mir.log.LoggerWrapper;
import mir.producer.ProducerFailure;
import mir.producer.ProducerNode;
import mir.util.ParameterExpander;
import mircoders.entity.EntityContent;
import mircoders.search.IndexUtil;


public class UnIndexingProducerNode implements ProducerNode {
  private String contentKey;
  private String indexPath;


  public UnIndexingProducerNode(String aContentKey, String pathToIndex) {
    contentKey = aContentKey;
    indexPath=pathToIndex;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure {
    Object data;
    Entity entity;
    String index=null;

    long startTime;
    long endTime;

    startTime = System.currentTimeMillis();

    try {
      index = ParameterExpander.expandExpression(aValueMap, indexPath);
      data = ParameterExpander.findValueForKey( aValueMap, contentKey );

      if (! (data instanceof EntityAdapter)) {
        throw new ProducerFailure("UnIndexingProducerNode: value of '"+contentKey+"' is not an EntityAdapter, but an " + data.getClass().getName(), null);
      }

      entity = ((EntityAdapter) data).getEntity();
      if (! (entity instanceof EntityContent)) {
        throw new ProducerFailure("UnIndexingProducerNode: value of '"+contentKey+"' is not a content EntityAdapter, but a " + entity.getClass().getName() + " adapter", null);
      }
      aLogger.info("UnIndexing " + (String) entity.getValue("id") + " out of " + index);

      IndexUtil.unindexEntity((EntityContent) entity,index);
      
    }
    catch (Throwable t) {
      aLogger.error("Error while unindexing content: " + t.getMessage());
      t.printStackTrace(new PrintWriter(new LoggerToWriterAdapter(aLogger, LoggerWrapper.DEBUG_MESSAGE)));
    }
    
    endTime = System.currentTimeMillis();

    aLogger.info("  UnIndexTime: " + (endTime-startTime) + " ms<br>");
  }
}










