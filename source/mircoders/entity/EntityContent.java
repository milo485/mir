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

import java.lang.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.lang.reflect.*;

import freemarker.template.*;

import mir.entity.*;
import mir.misc.*;
import mir.media.*;
import mir.storage.*;

import mircoders.storage.*;

/**
 * this class implements mapping of one line of the database table content
 * to a java object
 *
 * @version $Id: EntityContent.java,v 1.11 2002/11/04 04:35:21 mh Exp $
 * @author mir-coders group
 *
 */


public class EntityContent extends Entity
{

  String mirconf_extLinkName  = MirConfig.getProp("Producer.ExtLinkName");
  String mirconf_intLinkName  = MirConfig.getProp("Producer.IntLinkName");
  String mirconf_mailLinkName = MirConfig.getProp("Producer.MailLinkName");
  String mirconf_imageRoot    = MirConfig.getProp("Producer.ImageRoot");

  //this should always be transient i.e it can never be stored in the db
  //or ObjectStore. (so the ObjectStore should only be caching what comes
  //directly out of the DB. @todo confirm this with rk. -mh
  HashMap _entCache = new HashMap();
  Boolean _hasMedia = null;

	// constructors

	public EntityContent()
	{
		super();
    //content_data is now filed-type "text"
		//streamedInput = new ArrayList();
		//streamedInput.add("content_data");
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

	public void setProduced(boolean yesno) throws StorageObjectException
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
		} catch (StorageObjectException e) {
            throwStorageObjectException(e, "\n -- set produced failed");
		} catch (SQLException e) {
            throwStorageObjectException(e, "\n -- set produced failed");
		} finally {
			theStorageObject.freeConnection(con,stmt);
		}
	}


 /**
	* make openposting to newswire
	*/

	public void newswire() throws StorageObjectException
	{
		String sql = "update content set to_article_type='1', is_produced='0' where id='" + getId()+"'";
		try {
				theStorageObject.executeUpdate(sql);
		} catch (StorageObjectException e) {
            throwStorageObjectException(e, "\n -- newswire failed");
		} catch (SQLException e) {
            throwStorageObjectException(e, "\n -- newswire failed");
		}
	}


 /**
	* dettach from media
	*/
	public void dettach(String cid,String mid) throws StorageObjectException
	{
		if (mid!=null){
			try{
				DatabaseContentToMedia.getInstance().delete(cid,mid);
			} catch (Exception e){
                throwStorageObjectException(e, "\n -- failed to get instance");
			}
			//set Content to unproduced
			setProduced(false);
		}
	}

 /**
	* attach to media
	*/

	public void attach(String mid) throws StorageObjectException
	{
		if (mid!=null) {
			//write media-id mid and content-id in table content_x_media
			try{
				DatabaseContentToMedia.getInstance().addMedia(getId(),mid);
			} catch(StorageObjectException e){
				throwStorageObjectException(e, "attach: could not get the instance");
			}
			//set Content to unproduced
			setProduced(false);
		}	else {
			theLog.printError("EntityContent: attach without mid");
		}
	}

	/**
	 * overridden method getValue to include formatted date into every
	 * entityContent
	 */

	public String getValue(String field)
  {
    String returnField = null;
    if (field!=null)
    {
      if (field.equals("date_formatted") || field.equals("webdb_create_short") )
      {
  		  if (hasValueForField("date"))
      	returnField = StringUtil.webdbDate2readableDate(getValue("webdb_create"));
  		}
      else if (field.equals("description_parsed"))
        returnField = getDescriptionParsed();
      else if (field.equals("description_sentence"))
        returnField = getDescriptionSentence();
      else if (field.equals("content_data_parsed"))
        returnField = getContentDataParsed();
      else
        return super.getValue(field);
    }
    return returnField;
	}

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
        } catch (Exception ex) {
          theLog.printWarning("-- getComments: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      if (key.equals("to_media_images")) {
        try {
          _entCache.put(key, getImagesForContent());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          theLog.printWarning("-- getImagesForContent: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      if (key.equals("to_media_audio")) {
        try {
          _entCache.put(key, getAudioForContent());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          theLog.printWarning("-- getAudioForContent: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      if (key.equals("to_media_video")) {
        try {
          _entCache.put(key, getVideoForContent());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          theLog.printWarning("-- getVideoForContent: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      if (key.equals("to_media_other")) {
        try {
          _entCache.put(key, getOtherMediaForContent());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          theLog.printWarning("-- getOtherMediaForContent: could not fetch data " + ex.toString());
          throw new TemplateModelException(ex.toString());
        }
      }
      else if (key.equals("to_media_icon")) {
        try {
          _entCache.put(key, getUploadedMediaForNewswire());
          return (TemplateModel)_entCache.get(key);
        }
        catch (Exception ex) {
          theLog.printWarning("-- getUploadedMediaForNewswire: could not fetch data " + ex.toString());
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
          theLog.printWarning("-- getTopics: could not fetch data " + ex.toString());
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
        } else if (!((String)theStringValues.get("creator_main_url")).startsWith("http://")){
          theStringValues.put("creator_main_url","http://"+((String)theStringValues.get("creator_main_url")));
        }
      }
		}
		super.setValues(theStringValues);
	}


  private String getContentDataParsed() {
    String returnField = getValue("content_data");
    if ((returnField!=null) && (returnField.length()>0) ) {
      returnField=StringUtil.deleteForbiddenTags(returnField);
      //create http-links and email-links
      if (getValue("is_html").equals("0")) {
        returnField = StringUtil.createHTML(returnField,mirconf_imageRoot,
                                            mirconf_mailLinkName,mirconf_extLinkName,
                                            mirconf_intLinkName);
      }
      // commented this out as I don't think it necessary as we don't
      // "encodeHTML" in the first place anymore.. -mh 2002.07.19
      //returnField = StringUtil.decodeHTMLinTags(returnField);
    }
    return returnField;
  }

  private String getDescriptionSentence() {
    String returnField = getValue("description");
    if (returnField != null && returnField.length()>0) {
       returnField = StringUtil.removeHTMLTags(returnField);
       int endOfFirstSentence=StringUtil.findEndOfSentence(returnField,0);
       if (endOfFirstSentence > 0){
	 returnField = returnField.substring(0,endOfFirstSentence);
       }
    }
    return returnField;
  }

  private String getDescriptionParsed() {
    String returnField = getValue("description");
    if (returnField != null && returnField.length()>0) {
      returnField = StringUtil.deleteForbiddenTags(returnField);
      if (getValue("is_html").equals("0")) {
        returnField = StringUtil.createHTML(returnField,mirconf_imageRoot,
                                            mirconf_mailLinkName,mirconf_extLinkName,
                                            mirconf_intLinkName);
      }
      returnField = StringUtil.decodeHTMLinTags(returnField);
    }
    return returnField;
  }

	/**
	 * fetches all the comments belonging to an article
	 *
	 * @return freemarker.template.SimpleList
	 */
	private EntityList getComments() throws StorageObjectException {
		return ((DatabaseContent)theStorageObject).getComments(this);
	}

  // @todo this needs to optimized. expensive SQL
  private SimpleHash getUploadedMediaForNewswire()
    throws StorageObjectException, TemplateModelException
  {
    // fetching/setting the images
    // return to_media_icons
    String        tinyIcon = null, iconAlt = null;
    MirMedia      mediaHandler = null;
    EntityUploadedMedia uploadedMedia;
    Entity        mediaType;
    SimpleHash    returnHash = new SimpleHash();

    EntityList upMediaEntityList =
                    DatabaseContentToMedia.getInstance().getUploadedMedia(this);
    if (upMediaEntityList!=null && upMediaEntityList.getCount()>=1) {

      for (int n=0; n < upMediaEntityList.size();n++) {
        uploadedMedia = (EntityUploadedMedia)upMediaEntityList.elementAt(n);
        mediaType = uploadedMedia.getMediaType();
        try {
          mediaHandler = MediaHelper.getHandler( mediaType );
        } catch (MirMediaException ex) {
          throw new TemplateModelException(ex.toString());
        }
        //the "best" media type to show
        if (mediaHandler.isVideo()) {
          tinyIcon = MirConfig.getProp("Producer.Icon.TinyVideo");
          iconAlt = "Video";
          break;
        } else if (mediaHandler.isAudio()) {
          tinyIcon = MirConfig.getProp("Producer.Icon.TinyAudio");
          iconAlt = "Audio";
        } else if (tinyIcon == null && !mediaHandler.isImage()) {
          tinyIcon = mediaHandler.getTinyIconName();
          iconAlt = mediaHandler.getIconAltName();
        }

      }
      //it only has image(s)
      if (tinyIcon == null) {
        tinyIcon = MirConfig.getProp("Producer.Icon.TinyImage");
        iconAlt = "Image";
      }
    // uploadedMedia Entity list is empty.
    // we only have text
    } else {
      tinyIcon = MirConfig.getProp("Producer.Icon.TinyText");
      iconAlt = "Text";
    }
    returnHash.put("tiny_icon", mirconf_imageRoot+"/"+tinyIcon);
    returnHash.put("icon_alt", iconAlt);
    return returnHash;
  }

  private boolean hasMedia() throws StorageObjectException
  {
    if (_hasMedia == null) {
      _hasMedia =
        new Boolean(DatabaseContentToMedia.getInstance().hasMedia(this));
    }
    return _hasMedia.booleanValue();
  }

  //######## @todo all of the following getBlahForContent should have
  // and optimized version where LIMIT=1 sql for list view.
  private EntityList getImagesForContent()
    throws StorageObjectException, TemplateModelException
  {
    if (hasMedia())
      return DatabaseContentToMedia.getInstance().getImages(this);
    else
      return null;
  }

  private EntityList getAudioForContent()
    throws StorageObjectException, TemplateModelException
  {
    if (hasMedia())
      return DatabaseContentToMedia.getInstance().getAudio(this) ;
    else
      return null;
  }

  private EntityList getVideoForContent()
    throws StorageObjectException, TemplateModelException
  {
    if (hasMedia())
      return DatabaseContentToMedia.getInstance().getVideo(this) ;
    else
      return null;
  }

  private EntityList getOtherMediaForContent()
    throws StorageObjectException, TemplateModelException
  {
    if (hasMedia())
      return DatabaseContentToMedia.getInstance().getOther(this);
    else
      return null;
  }

}
