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

import mir.entity.*;
import mir.misc.*;
import mir.storage.*;

import mir.storage.*;

/**
 * This class maps one line of the comment-table to a java-object.
 *
 * @author $Author: mh $
 * @version $Revision: 1.9.2.1 $ $Date: 2002/09/01 21:31:43 $
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
  public void setValues(HashMap theStringValues)
  {
    if (theStringValues != null) {
      if (!theStringValues.containsKey("is_published")) {
       theStringValues.put("is_published","0");
			}

			if (theStringValues.containsKey("main_url")){
				if (((String)theStringValues.get("main_url")).equalsIgnoreCase("http://")) {
					theStringValues.remove("main_url");
				} else if ((!((String)theStringValues.get("main_url")).startsWith("http://"))
									&& ((String)theStringValues.get("main_url")).length()>0){
					theStringValues.put("main_url","http://"+((String)theStringValues.get("main_url")));
				}
			}

    }
    super.setValues(theStringValues);
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
      if (field.equals("date_formatted") || field.equals("webdb_create_short"))
      {
  		  if (hasValueForField("webdb_create"))
      	  returnField = StringUtil.dateToReadableDate(getValue("webdb_create"));
  		}
      else if (field.equals("description_parsed")) {
        /** @todo the config stuff should be moved to StringUtil */
        String extLinkName = MirConfig.getProp("Producer.ExtLinkName");
        String intLinkName = MirConfig.getProp("Producer.IntLinkName");
        String mailLinkName = MirConfig.getProp("Producer.MailLinkName");
        String imageRoot = MirConfig.getProp("Producer.ImageRoot");
        returnField = StringUtil.createHTML(getValue("description"),imageRoot,mailLinkName,extLinkName,intLinkName);
      }
      else
        return super.getValue(field);
    }
    return returnField;
	}


}
