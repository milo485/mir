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

import java.util.*;
import java.io.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import freemarker.template.*;


import mir.util.*;
import mir.producer.*;
//import mir.generator.*;
import mircoders.global.*;
import mircoders.localizer.*;
import mir.entity.*;
import mir.entity.adapter.*;
import mircoders.entity.*;
import mircoders.storage.*;


public class IndexingProducerNode implements ProducerNode {
  private String contentKey;
  private String indexPath;


  public IndexingProducerNode(String aContentKey, String pathToIndex) {
    contentKey = aContentKey;
    indexPath=pathToIndex;
  }

  public void produce(Map aValueMap, String aVerb, PrintWriter aLogger) throws ProducerFailure {
    IndexWriter indexWriter;
    Object data;
    Entity entity;

    long startTime;
    long endTime;

    startTime = System.currentTimeMillis();

    try {
      data = ParameterExpander.findValueForKey( aValueMap, contentKey );

      if (! (data instanceof EntityAdapter)) {
        throw new ProducerFailure("IndexingProducerNode: value of '"+contentKey+"' is not an EntityAdapter, but an " + data.getClass().getName(), null);
      }

      entity = ((EntityAdapter) data).getEntity();
      if (! (entity instanceof EntityContent)) {
        throw new ProducerFailure("IndexingProducerNode: value of '"+contentKey+"' is not a content EntityAdapter, but a " + entity.getClass().getName() + " adapter", null);
      }
      aLogger.println("Indexing " + (String) entity.getValue("id") + " into " + indexPath);
      aLogger.flush();

      IndexReader indexReader = IndexReader.open(indexPath);
      indexReader.delete(new Term("id",entity.getValue("id")));
      indexReader.close();

      indexWriter = new IndexWriter(indexPath, new StandardAnalyzer(), false);
      Document theDoc =  new Document();

      // Keyword is stored and indexed, but not tokenized
      // Text is tokenized,stored, indexed
      // Unindexed is not tokenized or indexed, only stored
      // Unstored is tokenized and indexed, but not stored

      theDoc.add(Field.Keyword("id",entity.getValue("id")));
      theDoc.add(Field.Keyword("where",entity.getValue("publish_path")+entity.getValue("id")+".shtml"));
      theDoc.add(Field.Text("creator",entity.getValue("creator")));
      theDoc.add(Field.Text("title",entity.getValue("title")));
      theDoc.add(Field.Keyword("webdb_create",entity.getValue("webdb_create_formatted")));
      theDoc.add(Field.UnStored("content_and_description",entity.getValue("description")+entity.getValue("content_data")));

      //topics
      TemplateModel topics=entity.get("to_topics");
      aLogger.println("THE CLASS NAME WAS: "+entity.get("to_topics").getClass().getName());
      while (((TemplateListModel)topics).hasNext()){
          theDoc.add(Field.UnStored("topic",((TemplateHashModel)((TemplateListModel)topics).next()).get("title").toString()));
      }


      //media

      //images
      TemplateModel images=entity.get("to_media_images");
      if (images != null){
          theDoc.add(Field.UnStored("media","images"));
      }
      //audio
      TemplateModel audio=entity.get("to_media_audio");
      if (audio != null){
          theDoc.add(Field.UnStored("media","audio"));
      }
      //video
      TemplateModel video=entity.get("to_media_video");
      if (video != null){
          theDoc.add(Field.UnStored("media","video"));
      }

      //comments-just aggregate all relevant fields
      String commentsAggregate = "";
      TemplateModel comments=entity.get("to_comments");
      if (comments != null){
        while (((TemplateListModel)comments).hasNext()){
          TemplateModel aComment = ((TemplateListModel)comments).next();
          commentsAggregate = commentsAggregate + " " + ((TemplateHashModel)aComment).get("title").toString()
            + " " + ((TemplateHashModel)aComment).get("creator").toString()
            + " " + ((TemplateHashModel)aComment).get("text").toString();
        }
      }
      theDoc.add(Field.UnStored("comments",commentsAggregate));

      indexWriter.addDocument(theDoc);
      indexWriter.close();

    }
    catch (Throwable t) {
      aLogger.println("Error while indexing content: " + t.getMessage());
      t.printStackTrace(aLogger);
      //should remove index lock here.....jd
      throw new ProducerFailure(t.getMessage(), t);
    }




    endTime = System.currentTimeMillis();

    aLogger.println("  IndexTime: " + (endTime-startTime) + " ms<br>");
    aLogger.flush();
  }
}



