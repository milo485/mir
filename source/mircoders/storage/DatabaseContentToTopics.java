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

package mircoders.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import mir.entity.EntityList;
import mir.log.LoggerWrapper;
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

  public static DatabaseContentToTopics getInstance() {
    if (instance == null) {
      synchronized (DatabaseContentToTopics.class) {
        if (instance == null) {
          instance = new DatabaseContentToTopics();
          instance.myselfDatabase = instance;
        }
      }
    }
    return instance;
  }

  private DatabaseContentToTopics() {
    super();

    logger = new LoggerWrapper("Database.ContentToTopics");

    hasTimestamp = false;
    theTable="content_x_topic";
    theEntityClass = mir.entity.GenericEntity.class;
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
  public List getTopicsOfContent(String contentId)
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

  private String getIdListExpression(List aList) {
    String result = "";

    Iterator i = aList.iterator();

    while (i.hasNext()) {
      result = result + i.next().toString();
      if (i.hasNext())
        result = result + ", ";
    }
    return result;
  }

  public void setTopics(String anArticleId, String [] aTopics) throws StorageObjectFailure {
    if (aTopics==null)
      setTopics(anArticleId, (List) null);
    else
      setTopics(anArticleId, Arrays.asList(aTopics));
  }

  public void setTopics(String anArticleId, List aTopics) throws StorageObjectFailure {
    List newTopics = new Vector();
    if (aTopics!=null) {
      Iterator i = aTopics.iterator();

      while (i.hasNext()) {
        newTopics.add(new Integer(Integer.parseInt((String) i.next())));
      }
    }

    List currentTopics = getTopicsOfContent(anArticleId);
    logger.debug("New topics = " + newTopics.toString());
    logger.debug("Current topics = " + currentTopics.toString());
    List topicsToDelete = new Vector(currentTopics);
    topicsToDelete.removeAll(newTopics);
    List topicsToAdd = new Vector(newTopics);
    topicsToAdd.removeAll(currentTopics);
    logger.debug("to delete = " + topicsToDelete.toString());
    logger.debug("to add = " + topicsToAdd.toString());


    if (!topicsToDelete.isEmpty()) {
      String sql =
          "delete from " + theTable + " " +
          "where content_id=" + anArticleId +
          "        and topic_id in (" + getIdListExpression(topicsToDelete) + ")";

      Connection connection=null;
      Statement statement=null;
      try {
        connection = getPooledCon();
        statement = connection.createStatement();
        int rs = executeUpdate(statement, sql);
      }
      catch (Exception e) {
        logger.error("-- deleting topics failed");
      }
      finally {
        try {
          freeConnection(connection, statement);
        }
        catch (Throwable t) {
        }
      }
    }

    Iterator i = topicsToAdd.iterator();
    while (i.hasNext()) {
      Integer topicId = (Integer) i.next();
      String sql =
          "insert into " + theTable + " (content_id, topic_id) "+
          "values (" + anArticleId + "," + topicId + ")";
      Connection connection=null;
      Statement statement=null;
      try {
        connection = getPooledCon();
        // should be a preparedStatement because is faster
        statement = connection.createStatement();
        int rs = executeUpdate(statement, sql);
      }
      catch (Exception e) {
        logger.error("-- adding topics failed");
      }
      finally {
        try {
          freeConnection(connection, statement);
        }
        catch (Throwable t) {
        }
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
