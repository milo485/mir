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
import java.util.HashMap;
import java.util.Map;

import mir.entity.Entity;
import mir.entity.EntityList;
import mir.log.LoggerWrapper;
import mir.storage.StorageObject;
import mir.storage.StorageObjectExc;
import mir.storage.StorageObjectFailure;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseContentToMedia;
import mircoders.storage.DatabaseContentToTopics;

/**
 * this class implements mapping of one line of the database table content
 * to a java object
 *
 * @version $Id: EntityContent.java,v 1.20 2003/09/03 18:29:04 zapata Exp $
 * @author mir-coders group
 *
 */


public class EntityContent extends Entity
{

  String mirconf_extLinkName  = configuration.getString("Producer.ExtLinkName");
  String mirconf_intLinkName  = configuration.getString("Producer.IntLinkName");
  String mirconf_mailLinkName = configuration.getString("Producer.MailLinkName");
  String mirconf_imageRoot    = configuration.getString("Producer.ImageRoot");

  //this should always be transient i.e it can never be stored in the db
  //or ObjectStore. (so the ObjectStore should only be caching what comes
  //directly out of the DB. @todo confirm this with rk. -mh
  Map _entCache = new HashMap();
  Boolean _hasMedia = null;

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

  private boolean hasMedia() throws StorageObjectFailure
  {
    if (_hasMedia == null) {
      try {
        _hasMedia =
            new Boolean(DatabaseContentToMedia.getInstance().hasMedia(this));
      } catch (StorageObjectExc e) {
        throw new StorageObjectFailure(e);
      }
    }
    return _hasMedia.booleanValue();
  }
}
