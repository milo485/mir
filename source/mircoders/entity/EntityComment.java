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

import java.util.Map;

import mir.entity.Entity;
import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;
import mircoders.storage.DatabaseCommentToMedia;
import mircoders.storage.DatabaseContent;

/**
 * This class maps one line of the comment-table to a java-object.
 *
 * @author $Author: idfx $
 * @version $Revision: 1.16 $ $Date: 2003/04/21 12:42:53 $
 */


public class EntityComment extends Entity
{

  public EntityComment()
  {
    super();
  }

  public EntityComment(StorageObject theStorage) {
    this();
    setStorage(theStorage);
  }


  /**
   * overridden method setValues to patch creator_main_url
   */
  public void setValues(Map theStringValues)
  {
    if (theStringValues != null) {
      if (!theStringValues.containsKey("is_published")) {
        theStringValues.put("is_published","0");
      }

      if (theStringValues.containsKey("main_url")){
        if (((String)theStringValues.get("main_url")).equalsIgnoreCase("http://")) {
          theStringValues.remove("main_url");
        }
        else if ((!((String)theStringValues.get("main_url")).startsWith("http://"))
                     && ((String)theStringValues.get("main_url")).length()>0) {
            theStringValues.put("main_url","http://"+((String)theStringValues.get("main_url")));
        }
      }

    }
    super.setValues(theStringValues);
  }

  /**
   * Deattaches media from a comment
   *
   * @param aCommentId
   * @param aMediaId
   * @throws StorageObjectFailure
   */
  public void dettach(String aCommentId,String aMediaId) throws StorageObjectFailure
  {
    if (aMediaId!=null){
      try{
        DatabaseCommentToMedia.getInstance().delete(aCommentId, aMediaId);
      }
      catch (Exception e){
        throwStorageObjectFailure(e, "dettach: failed to get instance");
      }

      DatabaseContent.getInstance().setUnproduced("id="+getValue("to_media"));
    }
  }

  /**
   *
   * @param aMediaId
   * @throws StorageObjectFailure
   */

  public void attach(String aMediaId) throws StorageObjectFailure
  {
    if (aMediaId!=null) {
      try{
        DatabaseCommentToMedia.getInstance().addMedia(getId(), aMediaId);
      }
      catch(StorageObjectFailure e){
        throwStorageObjectFailure(e, "attach: could not get the instance");
      }

      DatabaseContent.getInstance().setUnproduced("id="+getValue("to_media"));
    }
    else {
      logger.error("EntityContent: attach without mid");
    }
  }
}
