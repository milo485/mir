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

package mircoders.producer;

import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import java.util.*;
import java.sql.*;

import freemarker.template.*;

import mir.misc.*;
import mir.media.*;
import mir.storage.*;
import mir.module.*;
import mir.entity.*;

import mircoders.module.*;
import mircoders.entity.*;
import mircoders.storage.*;



abstract public class ProducerList extends Producer {

  public String listTemplate;
  public String whereClause;
  public String orderBy;
  public String fileDesc;
  protected HashMap additional = new HashMap();



  public void handle(PrintWriter htmlout, EntityUsers user, boolean sync, boolean force)
    throws StorageObjectException, ModuleException {
    handleIt(htmlout,user,force);
  }

  /** @todo this should return the number of pages produced! */
  public void handleIt(PrintWriter htmlout, EntityUsers user, boolean force)
    throws StorageObjectException, ModuleException {

    logHTML(htmlout, "Producer.List: started");
    int newsPerPage = Integer.parseInt(MirConfig.getProp("Producer.StartPage.Newswire"));
    long                sessionConnectTime = 0;
    long                startTime = (new java.util.Date()).getTime();
    String              htmlFileName = "";
    String              currentMediaId;
    EntityContent       currentContent;
    EntityList          list;
    EntityUsers         userEntity=null;
    SimpleHash          imageHash = new SimpleHash();
    int size = 0;
    int listSize = 0;

    int maxItemsOnPage = Integer.parseInt(MirConfig.getProp("Lists.Max.Items"));

    try {
      listSize = contentModule.getSize(whereClause);
    } catch (Exception e) {
      logHTML(htmlout,e.toString());
    }

    int modRest = listSize % maxItemsOnPage;
    int numberOfPages = (listSize - modRest) / maxItemsOnPage;
    boolean first=true;
    for (int i = 0;i < numberOfPages+1;i ++) {
      //break the loop, if only athe actuell pages should be produced
      if (force == false && i==3) {
        break;
      }
      //break, if only the first page has to be produced
      if (force == false && modRest != 0 && first==false){
        break;
      }


      if (first==true) {
        //get the data for the first page
        size=maxItemsOnPage + modRest;
        list = contentModule.getContent(whereClause, orderBy, 0, size, userEntity);
        first=false;
      } else {
        //get the data for the other pages
        list = contentModule.getContent(whereClause, orderBy, size, maxItemsOnPage, userEntity);
        size = size + maxItemsOnPage;
      }

      //now produce the pages
      if (list!=null || force==true) {
        SimpleHash mergeData = HTMLTemplateProcessor.makeSimpleHashWithEntitylistInfos(list);
        //process hashmap additional and add to mergedata
        if (additional != null) {
          Set set = additional.keySet();
          for (Iterator it = set.iterator();it.hasNext();) {
            String key = (String)it.next();
            mergeData.put(key,(TemplateModel)additional.get(key));
          }
        }

        if (i==0){
          htmlFileName = "/" + fileDesc + ".shtml";
          mergeData.put("filename",fileDesc + ".shtml");
          mergeData.put("previousPage","");
          if(numberOfPages<=1){
            mergeData.put("nextPage","");
          } else {
            mergeData.put("nextPage",fileDesc + (numberOfPages-1) + ".shtml");
          }
        } else {
          if (i==1 && numberOfPages > 2){
            mergeData.put("previousPage",fileDesc + ".shtml");
            mergeData.put("nextPage",fileDesc + (numberOfPages-2) + ".shtml");
          } else {
            if (i==(numberOfPages-1)){
              mergeData.put("previousPage",fileDesc + (numberOfPages-i+1) + ".shtml");
              mergeData.put("nextPage","");
            } else {
              mergeData.put("previousPage",fileDesc + (numberOfPages-(i-1)) + ".shtml");
              mergeData.put("nextPage",fileDesc + (numberOfPages-(i+1)) + ".shtml");
            }
          }
          htmlFileName = "/" + fileDesc + (numberOfPages-i) + ".shtml";
          mergeData.put("filename",fileDesc + (numberOfPages-i) + ".shtml");
        }

        //producing the html-files
        boolean retVal = produce(listTemplate, htmlFileName, mergeData, htmlout);
      } //end if
    }//end for

    sessionConnectTime = new java.util.Date().getTime() - startTime;
    logHTML(htmlout, "Producer.List finished: " + sessionConnectTime + " ms.");
  } //end handle

  public void setAdditional(String key, TemplateModel value) {
    additional.put(key,value);
  }

}
