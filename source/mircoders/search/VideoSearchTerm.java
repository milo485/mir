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

package mircoders.search;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import mir.entity.Entity;
import mir.entity.EntityList;
import mir.storage.StorageObjectFailure;
import mircoders.entity.EntityContent;
import mircoders.storage.DatabaseContentToMedia;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


public class VideoSearchTerm extends SearchTerm{
  public static String matchField       = "hasVideo";
  public static String paramName        = "search_hasVideo";
  public static String templateVariable = "hasVideo";

  public void index(Document doc, Entity entity) throws StorageObjectFailure{
    EntityList video = DatabaseContentToMedia.getInstance().getVideo((EntityContent) entity);
    if (video != null && video.size()>0){
      doc.add(Field.Keyword(matchField,"y"));
    }
  }

  public String makeTerm(HttpServletRequest req){
    String wanted = req.getParameter(paramName);
    if (wanted != null && wanted.equals("y")){
      return matchField + ":" + "\"" + wanted + "\"";
    }
    else {
      return null;
    }
  }

  public void returnMeta(Map result,Document doc){
    result.put(templateVariable, doc.get(matchField));
  }


}


