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

package mircoders.entity;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import mir.log.LoggerWrapper;
import mir.entity.Entity;
import mir.entity.EntityList;
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
 * @version $Id: EntityContent.java,v 1.15 2003/02/23 05:00:13 zapata Exp $
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
  HashMap _entCache = new HashMap();
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
   * make openposting to newswire
   */

  public void newswire() throws StorageObjectFailure
  {
    String sql = "update content set to_article_type='1', is_produced='0' where id='" + getId()+"'";
    try {
      theStorageObject.executeUpdate(sql);
    } catch (StorageObjectFailure e) {
      throwStorageObjectFailure(e, "\n -- newswire failed");
    } catch (SQLException e) {
      throwStorageObjectFailure(e, "\n -- newswire failed");
    }
  }


  /**
   * dettach from media
   */
  public void dettach(String cid,String mid) throws StorageObjectFailure
  {
    if (mid!=null){
      try{
        DatabaseContentToMedia.getInstance().delete(cid,mid);
      }
      catch (Exception e){
        throwStorageObjectFailure(e, "\n -- failed to get instance");
      }

      //set Content to unproduced
      setProduced(false);
    }
  }

  /**
   * attach to media
   */

  public void attach(String mid) throws StorageObjectFailure
  {
    if (mid!=null) {
      //write media-id mid and content-id in table content_x_media
      try{
        DatabaseContentToMedia.getInstance().addMedia(getId(),mid);
      } catch(StorageObjectFailure e){
        throwStorageObjectFailure(e, "attach: could not get the instance");
      }
      //set Content to unproduced
      setProduced(false);
    }
    else {
      logger.error("EntityContent: attach without mid");
    }
  }

  /**
   * overridden method getValue to include formatted date into every
   * entityContent
   */

  public TemplateModel get(java.lang.String key) throws TemplateModelException
  {
    if (key!=null) {
      if (_entCache.containsKey(key)) {
        return (TemplateModel)_entCache.get(key);
      }
      if (key.equals("to_comments")) {
        try {
          _entCache.put(key, getComments());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          logger.warn("EntityContent.getComments: could not fetch data " + ex.toString());

          throw new TemplateModelException(ex.toString());
        }
      }
      if (key.equals("to_media_images")) {
        try {
          _entCache.put(key, getImagesForContent());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          logger.warn("EntityContent.getImagesForContent: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      if (key.equals("to_media_audio")) {
        try {
          _entCache.put(key, getAudioForContent());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          logger.warn("EntityContent.getAudioForContent: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      if (key.equals("to_media_video")) {
        try {
          _entCache.put(key, getVideoForContent());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          logger.warn("EntityContent.getVideoForContent: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      if (key.equals("to_media_other")) {
        try {
          _entCache.put(key, getOtherMediaForContent());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          logger.warn("EntityContent.getOtherMediaForContent: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      else if (key.equals("to_topics")) {
        try {
          _entCache.put(key,
                        DatabaseContentToTopics.getInstance().getTopics(this));
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          logger.warn("EntityContent.getTopics: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      else {
        return new SimpleScalar(getValue(key));
      }

    }
    return null;
  }

  /**
   * overridden method setValues to patch creator_main_url
   */
  public void setValues(HashMap theStringValues) {
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

  /**
   * fetches all the comments belonging to an article
   *
   * @return freemarker.template.SimpleList
   */
  private EntityList getComments() throws StorageObjectFailure {
    return ((DatabaseContent)theStorageObject).getComments(this);
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

  //######## @todo all of the following getBlahForContent should have
  // and optimized version where LIMIT=1 sql for list view.
  private EntityList getImagesForContent()
      throws StorageObjectFailure, TemplateModelException
  {
    if (hasMedia())
      return DatabaseContentToMedia.getInstance().getImages(this);
    else
      return null;
  }

  private EntityList getAudioForContent()
      throws StorageObjectFailure, TemplateModelException
  {
    if (hasMedia())
      return DatabaseContentToMedia.getInstance().getAudio(this) ;
    else
      return null;
  }

  private EntityList getVideoForContent()
      throws StorageObjectFailure, TemplateModelException
  {
    if (hasMedia())
      return DatabaseContentToMedia.getInstance().getVideo(this) ;
    else
      return null;
  }

  private EntityList getOtherMediaForContent()
      throws StorageObjectFailure, TemplateModelException
  {
    if (hasMedia())
      return DatabaseContentToMedia.getInstance().getOther(this);
    else
      return null;
  }

}
