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

package mircoders.module;

import mir.entity.EntityList;
import mir.log.LoggerWrapper;
import mir.module.AbstractModule;
import mir.module.ModuleExc;
import mir.module.ModuleFailure;
import mir.storage.StorageObject;

/*
 *  ContentObjekt -
 *
 * @version $Id: ModuleContent.java,v 1.18 2003/03/09 19:14:21 idfx Exp $
 *
 * @author RK, mir-coders
 *
 */

public class ModuleContent extends AbstractModule
{
  static LoggerWrapper logger = new LoggerWrapper("Module.Content");

  public ModuleContent() {
    super();
  }

  public ModuleContent(StorageObject theStorage) {
    this.theStorage = theStorage;
  }

//
// various methods to retrieve content entities

//  public EntityList getFeatures(int offset, int limit) throws ModuleExc, ModuleFailure {
//    return getContent("is_published=true AND to_article_type=2", "webdb_create desc",
//                      offset, limit);
//  }

//  public EntityList getNewsWire(int offset, int limit) throws ModuleExc, ModuleFailure {
//    return getContent("is_published=true AND to_article_type = 1",
//                      "webdb_create desc",offset,limit);
//  }

//  public EntityList getStartArticle() throws ModuleExc, ModuleFailure {
//    EntityList returnList = getContent("is_published=true AND to_article_type=4",
//                                       "webdb_create desc",0,1);
//if no startspecial exists
//    if (returnList==null || returnList.size()==0)
//      returnList = getContent("is_published=true AND to_article_type=3",
//                              "webdb_create desc",0,1);

//    return returnList;
//  }

/*
      public EntityList getContent(Map searchValues, boolean concat, int offset, EntityUsers user) throws ModuleException {

    try {

      String whereClause ="", aField, aValue;
      boolean first = true;

      Set set = searchValues.keySet();
      Iterator it = set.iterator();
      for (int i=0;i<set.size();i++) {
        aField = (String)it.next();
        aValue = (String)searchValues.get(aField);

        if (first == false)
          whereClause +=  (concat) ? " and " : " or ";
        else
          first = false;

        whereClause += "(";

// default: hier splitten der eintraege und verknupfung mit AND OR NOT
        StringTokenizer st = new StringTokenizer(aValue);
        boolean firstToken = true;
        while(st.hasMoreTokens()) {
          String notString = "";
          String tokenConcat = " OR ";
          String nextToken = st.nextToken();

          if (nextToken.startsWith("+")) {
            nextToken = nextToken.substring(1);
            tokenConcat = " AND ";
          }
          if (nextToken.startsWith("-")) {
            nextToken = nextToken.substring(1);
            tokenConcat = " AND ";
            notString = " NOT ";
          }
          if (firstToken == true) {
            tokenConcat = "";
            firstToken = false;
          }


          whereClause += tokenConcat + aField + notString + " like '";
          whereClause += nextToken + "%'";
        }
        whereClause += ") ";
      }
      return theStorage.selectByWhereClause(whereClause, offset);
    }
    catch (StorageObjectFailure e){
      throw new ModuleException(e.toString());
    }

  }
*/
/*
  public EntityList getContentByField(String aField, String aValue, String orderBy, int offset,
                                      EntityUsers user) throws ModuleException
  {
    String whereClause = "lower("+aField + ") like lower('%" + JDBCStringRoutines.escapeStringLiteral(aValue) + "%')";
    return getContent(whereClause, orderBy, offset, user);
  }


  public EntityList getContent(String whereClause, String orderBy, int offset,
                               int limit, EntityUsers user) throws ModuleException {

    try {
      if (user!=null){
        if (!user.isAdmin())
          whereClause += " and to_publisher='" + user.getId()+"'";
      }
      return theStorage.selectByWhereClause(whereClause, orderBy, offset, limit);
    }
    catch (StorageObjectFailure e){	throw new ModuleException(e.toString()); }
  }
*/

  public EntityList getContent(String whereClause, String orderBy,int offset, int limit) throws ModuleExc, ModuleFailure {
    try {
      return theStorage.selectByWhereClause(whereClause, orderBy, offset, limit);
    }
    catch (Throwable e){
      throw new ModuleFailure(e);
    }
  }
/*
  public EntityList getContent(String whereClause, String orderBy, int offset, EntityUsers user)
      throws ModuleException
  {
    try {
      if (whereClause !=null) {

// for the different article_types
        if(whereClause.equals("newswire")) {
          whereClause="is_published='1' and to_article_type='1'";
          orderBy = "webdb_create desc";
        }
        if(whereClause.equals("feature")) {
          whereClause="is_published='1' and to_article_type='2'";
          orderBy = "webdb_create desc";
        }
        if(whereClause.equals("themenspecial")) {
          whereClause="is_published='1' and to_article_type='3'";
          orderBy = "webdb_create desc";
        }
        if(whereClause.equals("special")) {
          whereClause="is_published='1' and to_article_type='4'";
          orderBy = "webdb_create desc";
        }

        if(whereClause.equals("comments")) {
          whereClause="not (comment is null or comment like '')";
          orderBy = "webdb_lastchange desc";
        }

        if(whereClause.equals("nfrei")) {
          whereClause="is_published='0'"; orderBy="webdb_create desc";
        }

        if(whereClause.equals("lastchange")) {
          whereClause=""; orderBy="webdb_lastchange desc";
        }

        if(whereClause.equals("media")) {
          return DatabaseContentToMedia.getInstance().getContent();
        }
      }
      return theStorage.selectByWhereClause(whereClause, orderBy, offset);
    }
    catch (StorageObjectFailure e) {
      throw new ModuleException(e.toString());
    }
  }
*/
}


