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
import java.lang.reflect.*;
import java.util.*;
import java.sql.*;

import freemarker.template.*;

import mir.misc.*;
import mir.media.*;
import mir.storage.*;
import mir.module.*;
import mir.entity.*;

import mircoders.entity.*;
import mircoders.storage.*;



public class ProducerTopics extends ProducerList {

  public String where;
  String              currentMediaId;
  EntityList          upMediaEntityList;
  EntityList          imageEntityList;
  EntityList          currentMediaList;
  Entity              mediaType;
  EntityMedia         uploadedMedia;
  Class               mediaHandlerClass=null;
  MirMedia            mediaHandler=null;
  String              mediaHandlerName=null;
  Database            mediaStorage=null;
  String              tinyIcon;
  String              iconAlt;

  public void handle(PrintWriter htmlout, EntityUsers user, boolean force, boolean sync, String id)
    throws StorageObjectException, ModuleException {
    where=id;
    handle(htmlout,user,force,sync);
  }

  public void handle(PrintWriter htmlout, EntityUsers user, boolean force, boolean sync)
    throws StorageObjectException, ModuleException
  {
    long startTime = System.currentTimeMillis();
    int pageCount =0;
    logHTML(htmlout, "Producer.Topics: started");

    /** @todo should be done in static */
    listTemplate = MirConfig.getProp("Producer.TopicList.Template");

    orderBy="webdb_create desc";
    EntityList topicsEntityList;
    if(where==null){
      topicsEntityList = topicsModule.getByWhereClause("","title", -1);
    } else {
      topicsEntityList = topicsModule.getByWhereClause(where,"title", -1);
    }

    for(int i=0; i < topicsEntityList.size(); i++){

      EntityTopics currentTopic = (EntityTopics)topicsEntityList.elementAt(i);

      try {
      EntityList contentEntityList = DatabaseContentToTopics.getInstance().getContent(currentTopic);
      String whereClauseSpecial=null;

      if (contentEntityList!=null || force==true) {
        if (contentEntityList!=null){
          boolean first=true;
          whereClause="is_published='1' AND to_article_type >= 0 AND to_article_type <=2 AND id IN (";
          whereClauseSpecial="is_published='1' AND to_article_type=3 AND id IN (";
          for(int j=0; j < contentEntityList.size(); j++){
            if(first==false) {
              whereClause += ",";
              whereClauseSpecial += ",";
            }
            EntityContent currentContent = (EntityContent)contentEntityList.elementAt(j);
            whereClause += currentContent.getId();
            whereClauseSpecial += currentContent.getId();

            setAdditional("topic",currentTopic);

            first = false;
          }
          whereClause += ")";
          whereClauseSpecial += ")";
        }

        if(contentEntityList==null && force==true){
          //hihi, das ist eigentlich boese
          whereClause="is_published='1' AND to_article_type>=0 AND id IN (0)";
        }

        fileDesc = currentTopic.getValue("filename");

        // get the startarticle
        EntityList entityList = contentModule.getContent(whereClauseSpecial,"webdb_create desc",0,1);
        String currentMediaId = null;
        SimpleHash imageHash = new SimpleHash();
        EntityContent currentContent;
        if(entityList != null && entityList.size()==1){
          currentContent = (EntityContent)entityList.elementAt(0);
          try {
              setAdditional("special",currentContent);
          } catch (Exception e) {
            theLog.printError("ProducerTopics: problem with start special media: "+currentContent.getId()+" "+e.toString()+" <font color=\"red\">skipping</font>");
            logHTML(htmlout,"ProducerTopics: problem with start special media: "+currentContent.getId()+" "+e.toString());
          }
        }

        //set the list of topics
        setAdditional("topicslist",topicsEntityList);

        handleIt(htmlout,user,force);
        pageCount++;
      }
      } catch (Exception e) {
        theLog.printError("ProducerTopics: problem with start special media: "
        +e.toString()+" <font color=\"red\">skipping</font>");
        logHTML(htmlout,"ProducerTopics: problem with topic id: "
        +currentTopic.getId()+ "<font color=\"red\">skipping</font>");
      }
    }
    logHTMLFinish(htmlout, "Topics", pageCount, startTime, System.currentTimeMillis());
  }

  public static void main(String argv[]){
    try {
      new ProducerOpenPosting().handle(new PrintWriter(System.out), null,false, false);
    } catch(Exception e) {
      System.err.println(e.toString());
    }
  }
}
