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

package mircoders.entity;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import mir.entity.Entity;
import mir.log.LoggerWrapper;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;
import mir.util.StringRoutines;
import mircoders.storage.DatabaseContentToMedia;

/**
 * this class implements mapping of one line of the database table content
 * to a java object
 *
 * @version $Id: EntityContent.java,v 1.19.2.3 2003/09/19 23:34:20 zapata Exp $
 * @author mir-coders group
 *
 */


public class EntityContent extends Entity
{
  // constructors

  public EntityContent()
  {
    super();

    logger = new LoggerWrapper("Entity.Content");
  }

  public EntityContent(StorageObject theStorage) {
    this();

    setStorage(theStorage);
  }

  //
  // methods

  /**
   * set is_produced flag for the article
   */

  public void setProduced(boolean yesno) throws StorageObjectFailure
  {
    String value = (yesno) ? "1":"0";
    if (value.equals( getValue("is_produced") )) return;

    Connection con=null;Statement stmt=null;
    String sql = "update content set is_produced='" + value + "' where id='" + getId()+"'";
    try {
      con = theStorageObject.getPooledCon();
      /** @todo should be preparedStatement: faster!! */
      stmt = con.createStatement();
      theStorageObject.executeUpdate(stmt,sql);
    }
    catch (StorageObjectFailure e) {
      throwStorageObjectFailure(e, "\n -- set produced failed");
    }
    catch (SQLException e) {
      throwStorageObjectFailure(e, "\n -- set produced failed");
    }
    finally {
      theStorageObject.freeConnection(con,stmt);
    }
  }

  /**
   * Deattaches media from an article
   *
   * @param anArticleId
   * @param aMediaId
   * @throws StorageObjectFailure
   */
  public void dettach(String anArticleId, String aMediaId) throws StorageObjectFailure
  {
    if (aMediaId!=null){
      try{
        DatabaseContentToMedia.getInstance().delete(anArticleId, aMediaId);
      }
      catch (Exception e){
        throwStorageObjectFailure(e, "\n -- failed to get instance");
      }

      setProduced(false);
    }
  }

  /**
   * Attaches media to an article
   *
   * @param mid
   * @throws StorageObjectFailure
   */

  public void attach(String aMediaId) throws StorageObjectFailure
  {
    if (aMediaId!=null) {
      try{
        DatabaseContentToMedia.getInstance().addMedia(getId(),aMediaId);
      }
      catch(StorageObjectFailure e){
        throwStorageObjectFailure(e, "attach: could not get the instance");
      }
      setProduced(false);
    }
    else {
      logger.error("EntityContent: attach without mid");
    }
  }

  /**
   * overridden method setValues to patch creator_main_url
   */
  public void setValues(Map theStringValues) {
    if (theStringValues != null) {
      if (theStringValues.containsKey("creator_main_url")){
        if (((String)theStringValues.get("creator_main_url")).equalsIgnoreCase("http://")){
          theStringValues.remove("creator_main_url");
        }
        else if (!((String)theStringValues.get("creator_main_url")).startsWith("http://")){
          theStringValues.put("creator_main_url","http://"+((String)theStringValues.get("creator_main_url")));
        }
      }
    }
    super.setValues(theStringValues);
  }

  public void appendToComments(String aLine) {
    StringBuffer comment = new StringBuffer();
    try {
      comment.append(StringRoutines.interpretAsString(getValue("comment")));
    }
    catch (Throwable t) {
    }
    if (comment.length() > 0 && comment.charAt(comment.length() - 1) != '\n') {
      comment.append('\n');
    }

    comment.append(aLine);
    setValueForProperty("comment", comment.toString());
  }



}
