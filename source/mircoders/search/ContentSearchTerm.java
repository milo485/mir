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

package mircoders.search;

import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import mir.entity.Entity;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


public class ContentSearchTerm extends SearchTerm{

  public String templateVariable;
  public String dataField;
  public String matchField;
  public String paramName;
  public String partOfEntity;

  public ContentSearchTerm(String anEntityPart,String aParamName,String aMatchField,String aDataField, String aTemplateVariable){
    partOfEntity = anEntityPart;
    paramName = aParamName;
    matchField = aMatchField;
    dataField = aDataField;
    templateVariable = aTemplateVariable;

  }

  public void index(Document doc, Entity entity){
    doc.add(Field.UnStored(matchField,entity.getValue(partOfEntity)));
  }

  public void indexValue(Document doc, String value){
    doc.add(Field.UnStored(matchField, value));
  }


  public String makeTerm(HttpServletRequest req){
      String wanted = req.getParameter(paramName);
      if (wanted != null && !(wanted.equals(""))){
        String searchBoolean = req.getParameter("search_boolean");
        if (searchBoolean != null && searchBoolean.equals("phrase")){
          return matchField + ":" + "\"" + wanted + "\"";
        }
        else {
          if (searchBoolean != null && searchBoolean.equals("and")){
            StringTokenizer st = new StringTokenizer(wanted);
            String composite = "(";
            while (st.hasMoreTokens()) {
              composite = composite + " +" + matchField + ":" + st.nextToken();
            }
            composite = composite + ")";
            return composite;
          }
          else {
            //default to or
            StringTokenizer st = new StringTokenizer(wanted);
            String composite = "(";
            while (st.hasMoreTokens()) {
              composite = composite + " " + matchField + ":" + st.nextToken();
            }
            composite = composite + ")";
            return composite;
          }
        }
      }
      else {
        return null;
      }
  }

  public void returnMeta(Map result,Document doc){
    return;
  }


}


