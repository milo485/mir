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
package mircoders.producer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Map;

import mir.entity.Entity;
import mir.entity.adapter.EntityAdapter;
import mir.log.LoggerWrapper;
import mir.misc.StringUtil;
import mir.producer.ProducerFailure;
import mir.producer.ProducerNode;
import mir.util.ParameterExpander;
import mircoders.entity.EntityContent;
import mircoders.search.AudioSearchTerm;
import mircoders.search.ContentSearchTerm;
import mircoders.search.ImagesSearchTerm;
import mircoders.search.IndexUtil;
import mircoders.search.KeywordSearchTerm;
import mircoders.search.TextSearchTerm;
import mircoders.search.TopicSearchTerm;
import mircoders.search.UnIndexedSearchTerm;
import mircoders.search.VideoSearchTerm;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;


public class IndexingProducerNode implements ProducerNode {
  private String contentKey;
  private String indexPath;

  public IndexingProducerNode(String aContentKey, String pathToIndex) {
    contentKey = aContentKey;
    indexPath = pathToIndex;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger)
    throws ProducerFailure {
    IndexWriter indexWriter = null;
    Object data;
    Entity entity;
    String index = null;
    long startTime;
    long endTime;

    startTime = System.currentTimeMillis();

    try {
      index = ParameterExpander.expandExpression(aValueMap, indexPath);
      data = ParameterExpander.findValueForKey(aValueMap, contentKey);

      if (!(data instanceof EntityAdapter)) {
        throw new ProducerFailure("IndexingProducerNode: value of '" +
          contentKey + "' is not an EntityAdapter, but an " +
          data.getClass().getName(), null);
      }

      entity = ((EntityAdapter) data).getEntity();

      if (!(entity instanceof EntityContent)) {
        throw new ProducerFailure("IndexingProducerNode: value of '" +
          contentKey + "' is not a content EntityAdapter, but a " +
          entity.getClass().getName() + " adapter", null);
      }

      aLogger.info("Indexing " + (String) entity.getValue("id") + " into " +
        index);

      // create an index here if one did not already exist
      if (!(IndexReader.indexExists(index))) {
        aLogger.error("Didn't find existing index, so I'm making one in " +
          index);

        IndexWriter indexCreator =
          new IndexWriter(index, new StandardAnalyzer(), true);
        indexCreator.close();
      }

      IndexUtil.unindexEntity((EntityContent) entity, index);

      indexWriter = new IndexWriter(index, new StandardAnalyzer(), false);

      Document theDoc = new Document();

      // Keyword is stored and indexed, but not tokenized
      // Text is tokenized,stored, indexed
      // Unindexed is not tokenized or indexed, only stored
      // Unstored is tokenized and indexed, but not stored
      //this initialization should go somewhere global like an xml file....
      (new KeywordSearchTerm("id", "", "id", "", "id")).index(theDoc, entity);

      String textValue = entity.getValue("webdb_create");
      Calendar calendar = GregorianCalendar.getInstance();
      int year;
      int month;
      int day;
      int hours;
      int minutes;
      Date date;
      String formattedDate="";

      if (textValue!=null) {
        try {
          year = Integer.parseInt(textValue.substring(0,4));
          month = Integer.parseInt(textValue.substring(5,7));
          day = Integer.parseInt(textValue.substring(8,10));
	  hours = Integer.parseInt(textValue.substring(11,13));
          minutes = Integer.parseInt(textValue.substring(14,16));

          calendar.set(year, month-1, day, hours, minutes);
          date = calendar.getTime();
	  SimpleDateFormat formatter = new SimpleDateFormat ("yyyy.MM.dd hh:mm");
	  formattedDate=formatter.format(date);
        }
	catch (Throwable t){
	  aLogger.error("Error while generating content date to index: " + t.getMessage());
	  t.printStackTrace(aLogger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
	}
      }
      (new KeywordSearchTerm("webdb_create_formatted", "search_date",
        "webdb_create_formatted", "webdb_create_formatted",
        "webdb_create_formatted")).indexValue(theDoc,formattedDate);
     

      (new UnIndexedSearchTerm("", "", "", "where", "where")).indexValue(theDoc,
        StringUtil.webdbDate2path(entity.getValue("date")) +
        entity.getValue("id") + ".shtml");

      (new TextSearchTerm("creator", "search_creator", "creator", "creator",
        "creator")).index(theDoc, entity);
      (new TextSearchTerm("title", "search_title", "title", "title", "title")).index(theDoc,
        entity);
      (new UnIndexedSearchTerm("description", "search_content", "description",
        "description", "description")).index(theDoc, entity);
      (new UnIndexedSearchTerm("webdb_create", "search_irrelevant",
        "creationDate", "creationDate", "creationDate")).index(theDoc, entity);

      (new ContentSearchTerm("content_data", "search_content", "content", "", "")).indexValue(theDoc,
        entity.getValue("content_data") + " " + entity.getValue("description") +
        " " + entity.getValue("title"));

      (new TopicSearchTerm()).index(theDoc, entity);

      (new ImagesSearchTerm()).index(theDoc, entity);

      (new AudioSearchTerm()).index(theDoc, entity);

      (new VideoSearchTerm()).index(theDoc, entity);

      //comments-just aggregate all relevant fields
      //removed until i get a chance to do this right
      //String commentsAggregate = "";
      //TemplateModel comments=entity.get("to_comments");
      //if (comments != null){
      // while (((TemplateListModel)comments).hasNext()){
      //    TemplateModel aComment = ((TemplateListModel)comments).next();
      //    commentsAggregate = commentsAggregate + " " + ((TemplateHashModel)aComment).get("title").toString()
      //     + " " + ((TemplateHashModel)aComment).get("creator").toString()
      //      + " " + ((TemplateHashModel)aComment).get("text").toString();
      //  }
      //}
      //theDoc.add(Field.UnStored("comments",commentsAggregate));
      indexWriter.addDocument(theDoc);
    }
    catch (Throwable t) {
      aLogger.error("Error while indexing content: " + t.getMessage());
      t.printStackTrace(aLogger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
    }
    finally {
      if (indexWriter != null) {
        try {
          indexWriter.close();
        } catch (Throwable t) {
          aLogger.warn("Error while closing indexWriter: " + t.getMessage());
        }
      }

      try {
        FSDirectory theIndexDir = FSDirectory.getDirectory(index, false);

        if (IndexReader.isLocked(theIndexDir)) {
          IndexReader.unlock(theIndexDir);
        }
      } catch (Throwable t) {
        aLogger.warn("Error while unlocking index: " + t.getMessage());
      }
    }

    endTime = System.currentTimeMillis();

    aLogger.info("  IndexTime: " + (endTime - startTime) + " ms<br>");
  }
}
