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

import mir.entity.EntityList;
import mir.log.LoggerWrapper;
import mir.storage.Database;
import mir.storage.StorageObject;
import mir.storage.StorageObjectExc;
import mir.storage.StorageObjectFailure;
import mircoders.entity.EntityContent;
import mircoders.entity.EntityUploadedMedia;

/**
 * <b>implements abstract DB connection to the content_x_media SQL table
 *
 * @author RK, mir-coders group
 * @version $Id: DatabaseContentToMedia.java,v 1.18 2003/04/21 12:42:48 idfx Exp $
 *
 */

public class DatabaseContentToMedia extends Database implements StorageObject{

  private static DatabaseContentToMedia instance;

  public static DatabaseContentToMedia getInstance() {
    if (instance == null) {
      synchronized (DatabaseContentToMedia.class) {
        if (instance == null) {
          instance = new DatabaseContentToMedia();
          instance.myselfDatabase = instance;
        }
      }
    }
    return instance;
  }

  private DatabaseContentToMedia() {
    super();

    logger = new LoggerWrapper("Database.ContentToMedia");

    hasTimestamp = false;
    theTable = "content_x_media";
    theEntityClass = mir.entity.GenericEntity.class;
  }

  /**
   * get all the media-files belonging to a content entity
   *
   */
  public EntityList getMedia(EntityContent content) throws StorageObjectFailure {
    EntityList returnList = null;
    if (content != null) {
      // get all to_topic from media_x_topic
      String id = content.getId();
      String subselect = "id in (select media_id from " + theTable +
          " where content_id=" + id + ")";

      try {
        // media should stay in uploaded order. this is especially important
        // for photo stories which require a specific chronologic order.
        // this is why we have the the second parameter "id"
        returnList = DatabaseMedia.getInstance().selectByWhereClause(subselect,
            "id", -1);
      }
      catch (Throwable e) {
        logger.debug("-- get media failed " + e.toString());
        throw new StorageObjectFailure("-- get media failed ", e);
      }
    }
    return returnList;
  }

  public boolean hasMedia(EntityContent content) throws StorageObjectFailure,
      StorageObjectExc {
    if (content != null) {
      try {
        if (selectByWhereClause("content_id=" + content.getId(), -1).size() ==
            0)
          return false;
        else
          return true;
      }
      catch (Exception e) {
        logger.error("DatabaseContentToMedia.hasMedia: " + e.toString());
        throw new StorageObjectFailure("DatabaseContentToMedia.hasMedia: " +
                                       e.toString(), e);
      }
    }
    else {
      logger.error("DatabaseContentToMedia.hasMedia: content == null");
      throw new StorageObjectExc(
          "DatabaseContentToMedia.hasMedia: content == null");
    }
  }

  /**
   * get all the audio belonging to a content entity
   *
   */
  public EntityList getAudio(EntityContent content) throws StorageObjectFailure {
    EntityList returnList = null;
    if (content != null) {
      // get all to_topic from media_x_topic
      String id = content.getId();
      //this is not supported by mysql
      String subselect = "id in (select media_id from " + theTable +
          " where content_id=" + id + ")";

      try {
        // media should stay in uploaded order. this is especially important
        // for photo stories which require a specific chronologic order.
        // this is why we have the the second parameter "id"
        returnList = DatabaseAudio.getInstance().selectByWhereClause(subselect,
            "id", -1);
      }
      catch (Exception e) {
        logger.error("DatabaseContentToMedia.getAudio: " + e.toString());
        throw new StorageObjectFailure("DatabaseContentToMedia.getAudio: " +
                                       e.toString(), e);
      }
    }
    return returnList;
  }

  /**
   * get all the video belonging to a content entity
   *
   */
  public EntityList getVideo(EntityContent content) throws StorageObjectFailure {
    EntityList returnList = null;
    if (content != null) {
      // get all to_topic from media_x_topic
      String id = content.getId();
      //this is not supported by mysql
      String subselect = "id in (select media_id from " + theTable +
          " where content_id=" + id + ")";

      try {
        // media should stay in uploaded order. this is especially important
        // for photo stories which require a specific chronologic order.
        // this is why we have the the second parameter "id"
        returnList = DatabaseVideo.getInstance().selectByWhereClause(subselect,
            "id", -1);
      }
      catch (Exception e) {
        logger.error("DatabaseContentToMedia.getVideo: " + e.toString());
        throw new StorageObjectFailure("DatabaseContentToMedia.getVideo: " +
                                       e.toString(), e);
      }
    }
    return returnList;
  }

  /**
   * get all the images belonging to a content entity
   *
   */
  public EntityList getImages(EntityContent content) throws
      StorageObjectFailure {
    EntityList returnList = null;
    if (content != null) {
      // get all to_topic from media_x_topic
      String id = content.getId();
      //this is not supported by mysql
      String subselect = "id in (select media_id from " + theTable +
          " where content_id=" + id + ")";

      try {
        // media should stay in uploaded order. this is especially important
        // for photo stories which require a specific chronologic order.
        // this is why we have the the second parameter "id"
        returnList = DatabaseImages.getInstance().selectByWhereClause(subselect,
            "id", -1);
      }
      catch (Exception e) {
        logger.error("DatabaseContentToMedia.getImages: " + e.toString());
        throw new StorageObjectFailure("DatabaseContentToMedia.getImages: " +
                                       e.toString(), e);
      }
    }
    return returnList;
  }

  /**
   * get all the uploaded/other Media belonging to a content entity
   *
   */
  public EntityList getOther(EntityContent content) throws StorageObjectFailure {
    /** @todo this should only fetch published media / rk */

    EntityList returnList = null;
    if (content != null) {
      // get all to_topic from media_x_topic
      String id = content.getId();
      //this is not supported by mysql
      String subselect = "id in (select media_id from " + theTable +
          " where content_id=" + id + ")";

      try {
        // media should stay in uploaded order. this is especially important
        // for photo stories which require a specific chronologic order.
        // this is why we have the the second parameter "id"
        returnList = DatabaseOther.getInstance().selectByWhereClause(subselect,
            "id");
      }
      catch (Exception e) {
        logger.error("DatabaseContentToMedia.getOther: " + e.toString());
        throw new StorageObjectFailure("DatabaseContentToMedia.getOther: " + e.toString(), e);
      }
    }
    return returnList;
  }

  /**
   * get all the uploaded/other Media belonging to a content entity
   *
   */
  public EntityList getUploadedMedia(EntityContent content) throws
      StorageObjectFailure {
    /** @todo this should only fetch published media / rk */

    EntityList returnList = null;
    if (content != null) {
      // get all to_topic from media_x_topic
      String id = content.getId();
      //this is not supported by mysql
      String subselect = "id in (select media_id from " + theTable +
          " where content_id=" + id + ")";

      try {
        returnList = DatabaseUploadedMedia.getInstance().selectByWhereClause(
            subselect,
            "id");
      }
      catch (Exception e) {
        logger.error("DatabaseContentToMedia.getUploadedMedia: " + e.toString());
        throw new StorageObjectFailure(
            "DatabaseContentToMedia.getUploadedMedia: " + e.toString(), e);
      }
    }
    return returnList;
  }

  public void setMedia(String contentId, String[] mediaId) throws
      StorageObjectFailure {
    if (contentId == null) {
      return;
    }
    if (mediaId == null || mediaId[0] == null) {
      return;
    }
    //first delete all row with content_id=contentId
    String sql = "delete from " + theTable + " where content_id=" + contentId;

    Connection con = null;
    Statement stmt = null;
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      ResultSet rs = executeSql(stmt, sql);
    }
    catch (Exception e) {
      logger.error("-- set media failed -- delete");
      throw new StorageObjectFailure("-- set media failed -- delete", e);
    }
    finally {
      freeConnection(con, stmt);
    }

    //now insert
    //first delete all row with content_id=contentId
    for (int i = 0; i < mediaId.length; i++) {
      sql = "insert into " + theTable + " (content_id,media_id) values ("
          + contentId + "," + mediaId[i] + ")";
      try {
        con = getPooledCon();
        // should be a preparedStatement because is faster
        stmt = con.createStatement();
        int rs = executeUpdate(stmt, sql);
      }
      catch (Exception e) {
        logger.error("-- set topics failed -- insert");
        throw new StorageObjectFailure("-- set topics failed -- insert ", e);
      }
      finally {
        freeConnection(con, stmt);
      }
    }
  }

  public void addMedia(String contentId, String mediaId) throws
      StorageObjectFailure {
    if (contentId == null && mediaId == null) {
      return;
    }

    Connection con = null;
    Statement stmt = null;
    //now insert

    String sql = "insert into " + theTable + " (content_id,media_id) values ("
        + contentId + "," + mediaId + ")";
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      int rs = executeUpdate(stmt, sql);
    }
    catch (Exception e) {
      logger.error("-- add media failed -- insert");
      throw new StorageObjectFailure("-- add media failed -- insert ", e);
    }
    finally {
      freeConnection(con, stmt);
    }
  }

  public void setMedia(String contentId, String mediaId) throws
      StorageObjectFailure {
    if (contentId == null && mediaId == null) {
      return;
    }
    //first delete all row with content_id=contentId
    String sql = "delete from " + theTable + " where content_id=" + contentId;

    Connection con = null;
    Statement stmt = null;
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      int rs = executeUpdate(stmt, sql);
    }
    catch (Exception e) {
      logger.error("-- set media failed -- delete");
      throw new StorageObjectFailure("-- set media failed -- delete ", e);
    }
    finally {
      freeConnection(con, stmt);
    }

    //now insert
    //first delete all row with content_id=contentId

    sql = "insert into " + theTable + " (content_id,media_id) values ("
        + contentId + "," + mediaId + ")";
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      int rs = executeUpdate(stmt, sql);
    }
    catch (Exception e) {
      logger.error("-- set media failed -- insert");
      throw new StorageObjectFailure("-- set media failed -- insert ", e);
    }
    finally {
      freeConnection(con, stmt);
    }
  }

  public void deleteByContentId(String contentId) throws StorageObjectFailure {
    if (contentId == null) {
      //theLog.printDebugInfo("-- delete topics failed -- no content id");
      return;
    }
    //delete all row with content_id=contentId
    String sql = "delete from " + theTable + " where content_id=" + contentId;

    Connection con = null;
    Statement stmt = null;
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      int rs = executeUpdate(stmt, sql);
    }
    catch (Exception e) {
      logger.error("-- delete by contentId failed  ");
      throw new StorageObjectFailure(
          "-- delete by content id failed -- delete ", e);
    }
    finally {
      freeConnection(con, stmt);
    }
  }

  public void deleteByMediaId(String mediaId) throws StorageObjectFailure {
    if (mediaId == null) {
      //theLog.printDebugInfo("-- delete topics failed -- no topic id");
      return;
    }
    //delete all row with content_id=contentId
    String sql = "delete from " + theTable + " where media_id=" + mediaId;

    Connection con = null;
    Statement stmt = null;
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      int rs = executeUpdate(stmt, sql);
      logger.debug("-- delete media success ");
    }
    catch (Exception e) {
      logger.error("-- delete media failed ");
      throw new StorageObjectFailure("-- delete by media id failed -- ", e);
    }
    finally {
      freeConnection(con, stmt);
    }
  }

  public void delete(String contentId, String mediaId) throws
      StorageObjectFailure {
    if (mediaId == null || contentId == null) {
      logger.debug("-- delete media failed -- missing parameter");
      return;
    }
    //delete all row with content_id=contentId and media_id=mediaId
    String sql = "delete from " + theTable + " where media_id=" + mediaId +
        " and content_id= " + contentId;

    Connection con = null;
    Statement stmt = null;
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      int rs = executeUpdate(stmt, sql);
      logger.debug("-- delete content_x_media success ");
    }
    catch (Exception e) {
      logger.error("-- delete content_x_media failed ");
      throw new StorageObjectFailure("-- delete content_x_media failed -- ", e);
    }
    finally {
      freeConnection(con, stmt);
    }
  }

  public EntityList getContent(EntityUploadedMedia media) throws
      StorageObjectFailure {
    EntityList returnList = null;
    if (media != null) {
      String id = media.getId();
      String select = "select content_id from " + theTable + " where media_id=" +
          id;

      // execute select statement
      Connection con = null;
      Statement stmt = null;
      try {
        con = getPooledCon();
        // should be a preparedStatement because is faster
        stmt = con.createStatement();
        ResultSet rs = executeSql(stmt, select);
        if (rs != null) {
          String mediaSelect = "id IN (";
          boolean first = true;
          while (rs.next()) {
            if (first == false)
              mediaSelect += ",";
            mediaSelect += rs.getString(1);
            first = false;
          }
          mediaSelect += ")";
          if (first == false)
            returnList = DatabaseContent.getInstance().selectByWhereClause(
                mediaSelect, -1);
        }
      }
      catch (Exception e) {
        logger.error("-- get content failed");
        throw new StorageObjectFailure("-- get content failed -- ", e);
      }
      finally {
        freeConnection(con, stmt);
      }
    }
    return returnList;
  }

  /**
   * Returns a EntityList with all content-objects having a relation to a media
   */

  public EntityList getContent() throws StorageObjectFailure {
    EntityList returnList = null;

    String select = "select distinct content_id from " + theTable;
    // execute select statement
    Connection con = null;
    Statement stmt = null;
    try {
      con = getPooledCon();
      // should be a preparedStatement because is faster
      stmt = con.createStatement();
      ResultSet rs = executeSql(stmt, select);
      if (rs != null) {
        String mediaSelect = "id IN (";
        boolean first = true;
        while (rs.next()) {
          if (first == false)
            mediaSelect += ",";
          mediaSelect += rs.getString(1);
          first = false;
        }
        mediaSelect += ")";
        if (first == false)
          returnList = DatabaseContent.getInstance().selectByWhereClause(
              mediaSelect, "webdb_lastchange desc");
      }
    }
    catch (Exception e) {
      logger.error("-- get content failed");
      throw new StorageObjectFailure("-- get content failed -- ", e);
    }
    finally {
      freeConnection(con, stmt);
    }

    return returnList;
  }

}
