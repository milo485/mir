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

package mircoders.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import mir.log.LoggerWrapper;
import mir.entity.EntityList;
import mir.storage.Database;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;
import mircoders.entity.EntityContent;
import mircoders.entity.EntityTopics;

/**
 * <b>This class implements the 1-n-relation between
 * content and topic
 *
 */

public class DatabaseContentToTopics extends Database implements StorageObject{

  private static DatabaseContentToTopics instance;

  // the following *has* to be sychronized cause this static method
  // could get preemted and we could end up with 2 instances of DatabaseFoo.
  // see the "Singletons with needles and thread" article at JavaWorld -mh
  public synchronized static DatabaseContentToTopics getInstance()
    throws StorageObjectFailure {
    if (instance == null) {
      instance = new DatabaseContentToTopics();
      instance.myselfDatabase = instance;
    }
    return instance;
  }

  private DatabaseContentToTopics() throws StorageObjectFailure {
    super();

    logger = new LoggerWrapper("Database.ContentToTopics");

    hasTimestamp = false;
    theTable="content_x_topic";
    try { this.theEntityClass = Class.forName("mir.entity.GenericEntity"); }
    catch (Exception e) { throw new StorageObjectFailure(e); }

  }

  /**
   * This class return an EntityList of Topics
   * @param EntityContent content
   * @returns EntityList
   */
  public EntityList getTopics(EntityContent content) {
    EntityList returnList=null;
    if (content != null) {
      // get all to_topic from content_x_topic
      String id = content.getId();
      String subselect = "id in (select topic_id from " + theTable + " where content_id=" + id+")";

      try {
        returnList = DatabaseTopics.getInstance().selectByWhereClause(subselect,-1);
      }
      catch (Exception e) {
        logger.error("-- get topics failed " + e.toString());
      }
    }
    return returnList;
  }

  /**
   * Returns a ArrayList of Integer-Objects from a content-id.
   * @returns ArrayList
   */
  public ArrayList getTopicsOfContent(String contentId)
    throws StorageObjectFailure {
    ArrayList returnList = new ArrayList();

    if (contentId != null) {
      String sql = "select topic_id from " + theTable + " where content_id=" + contentId;
      Connection con=null;Statement stmt=null;
      try {
        con = getPooledCon();
        // should be a preparedStatement because is faster
        stmt = con.createStatement();
        ResultSet rs = executeSql(stmt,sql);
        if(rs!=null){
          while(rs.next()){
            returnList.add(new Integer(rs.getInt("topic_id")));
          }
        }
      }
      catch (Exception e) {
        logger.error("DatabaseContentToTopics.getTopicsOfContent: " + e.getMessage());
      }
      finally {
        freeConnection(con,stmt);
      }
    }
    return returnList;
  }

  /**
   * Set new topics
   */
  public void setTopics(String contentId, String[] topicId)
    throws StorageObjectFailure {
    if (contentId == null){
      return;
    }
    if (topicId==null || topicId[0]==null) {
      return;
    }
    //first check which topics this article has
    Collection hasTopics = getTopicsOfContent(contentId);
    Collection toSet = new ArrayList();
    Collection toDelete = new ArrayList();

    if(hasTopics!=null && hasTopics.size()>0){
      //now we check if there are new topics and copy them to an array.
      for(int i = 0; i< topicId.length;i++){
        boolean set=false;
        int whichTopic = 0;
        for(Iterator it=hasTopics.iterator();it.hasNext();){
          Integer topic = (Integer)it.next();
          if(topicId[i].equals(topic.toString())){
            set=true;
          } else {
            whichTopic = i;
          }
        }
        if(set==false){
          toSet.add(topicId[i]);
          logger.debug("to set: "+ topicId[i]);
        }
      }
      //now we check if we have to delete topics
      for(Iterator it=hasTopics.iterator();it.hasNext();){
        boolean delete=true;
        int whichTopic = 0;
        Integer topic = (Integer)it.next();
        for(int i = 0; i< topicId.length;i++){
          if(topicId[i].equals(topic.toString())){
            delete=false;
          } else {
            whichTopic = i;
          }
        }
        if(delete==true){
          toDelete.add(topic.toString());
          logger.debug("to delete: "+ topic.toString());
        }
      }
    } else {
      //all the topics has to be set, so we copy all to the array
                        for (int i = 0; i < topicId.length; i++){
                                toSet.add(topicId[i]);
                        }
    }

    //first delete all row with content_id=contentId
    String sql = "delete from "+ theTable +" where content_id=" + contentId
                + " and topic_id in (";
    boolean first=false;
    for(Iterator it = toDelete.iterator(); it.hasNext();){
      if(first==false){
        first=true;
      } else {
        sql+=",";
      }
      sql+= (String)it.next();
    }
    sql+=")";
    Connection con=null;Statement stmt=null;
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      int rs = executeUpdate(stmt,sql);
    } catch (Exception e) {
      logger.error("-- deleting topics failed");
    } finally {
      freeConnection(con,stmt);
    }

    //now insert
    //first delete all row with content_id=contentId
    for (Iterator it = toSet.iterator(); it.hasNext();) {
      sql = "insert into "+ theTable +" (content_id,topic_id) values ("
            + contentId + "," + (String)it.next() + ")";
      try {
        con = getPooledCon();
        // should be a preparedStatement because is faster
        stmt = con.createStatement();
        int rs = executeUpdate(stmt,sql);
      }
      catch (Exception e) {
        logger.error("-- set topics failed -- insert laenge topicId" + topicId.length);
      } finally {
        freeConnection(con,stmt);
      }
    }
  }

  public void deleteByContentId(String contentId)
    throws StorageObjectFailure {
    if (contentId == null) {
      //theLog.printDebugInfo("-- delete topics failed -- no content id");
      return;
    }
    //delete all row with content_id=contentId
    String sql = "delete from "+ theTable +" where content_id=" + contentId;

    Connection con=null;Statement stmt=null;
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      ResultSet rs = executeSql(stmt,sql);
    } catch (Exception e) {
      //theLog.printDebugInfo("-- delete topics failed  ");
    } finally {
      freeConnection(con,stmt);
    }
  }

  public void deleteByTopicId(String topicId)
    throws StorageObjectFailure {
    if (topicId == null) {
      //theLog.printDebugInfo("-- delete topics failed -- no topic id");
      return;
    }
    //delete all row with content_id=contentId
    String sql = "delete from "+ theTable +" where topic_id=" + topicId;

    Connection con=null;Statement stmt=null;
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      ResultSet rs = executeSql(stmt,sql);
    }
    catch (Exception e) {
      logger.error("-- delete topics failed ");
    }
    finally {
      freeConnection(con,stmt);
    }
  }


  public EntityList getContent(EntityTopics topic)
    throws StorageObjectFailure {
    EntityList returnList=null;
    if (topic != null) {
      String id = topic.getId();
      String select = "select content_id from " + theTable + " where topic_id=" + id;

      // execute select statement
      Connection con=null;Statement stmt=null;
      try {
        con = getPooledCon();
        // should be a preparedStatement because is faster
        stmt = con.createStatement();
        ResultSet rs = executeSql(stmt,select);
        if (rs!=null) {
          String topicSelect= "id IN (";
          boolean first=true;
          while (rs.next()) {
            if (first==false) topicSelect+=",";
            topicSelect += rs.getString(1);
            first=false;
          }
          topicSelect+=")";
          if (first==false)
            returnList = DatabaseContent.getInstance().selectByWhereClause(topicSelect,-1);
        }
      }
      catch (Exception e) {
        logger.error("-- get contetn failed");
      }
      finally { freeConnection(con,stmt);}
    }
    return returnList;
  }
}
