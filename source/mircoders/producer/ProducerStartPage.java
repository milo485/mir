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
import java.util.*;

import freemarker.template.*;

import mir.entity.*;
import mir.misc.*;
import mir.module.*;
import mir.storage.*;
import mircoders.module.*;
import mircoders.storage.*;
import mircoders.entity.*;


public class ProducerStartPage extends Producer {

  private static String startPageTemplate = MirConfig.getProp("Producer.StartPage.Template");
  private static String featuresRSSTemplate = MirConfig.getProp("Producer.FeaturesRSS.Template");
  private static int itemsPerPage = Integer.parseInt(MirConfig.getProp("Producer.StartPage.Items"));
  private static int newsPerPage = Integer.parseInt(MirConfig.getProp("Producer.StartPage.Newswire"));

  public static void main(String argv[]){
    try {
      // Why are we reloading the configuration here?
      // is there something I'm missing?
      // mh. <heckmann@hbe.ca>
      // Configuration.initConfig(argv[0]);
      new ProducerStartPage().handle(new PrintWriter(System.out), null);
    } catch(Exception e) {
      System.err.println(e.toString());
    }
  }

  public void handle(PrintWriter htmlout, EntityUsers user, boolean force,boolean sync)
    throws StorageObjectException, ModuleException
  {
    long    startTime = System.currentTimeMillis();
    printHTML(htmlout, "Producer.StartPage: started");
    SimpleHash startPageModel = new SimpleHash();

    // breaking news
    ModuleBreaking breakingModule = new ModuleBreaking(DatabaseBreaking.getInstance());
    startPageModel.put("breakingnews", breakingModule.getBreakingNews());
    startPageModel.put("topics", topicsModule.getTopicsList());
    startPageModel.put("newswire", contentModule.getNewsWire(0,newsPerPage));
    startPageModel.put("startspecial", contentModule.getStartArticle());
    startPageModel.put("features", contentModule.getFeatures(0,itemsPerPage));
    startPageModel.put("dc_now", new SimpleScalar(StringUtil.date2w3DateTime(new GregorianCalendar())));


    /* @todo switch to compressed */
    produce(startPageTemplate, "/index.shtml", startPageModel, htmlout);
    
    /* should be mandatory in light of new www.indy newswire.
     *  but remember Mir is not indy specific. -mh. 
     *  Also should it really always be produced in UTF8 chars -mh? 
     */
    produce(featuresRSSTemplate, "/features.1-0.rdf", startPageModel, htmlout, "UTF8");

    // finished
    logHTMLFinish(htmlout, "Startpage", 1, startTime, System.currentTimeMillis());

    if(sync==true){
      logHTML(htmlout, "Producer.Startpage: rsyncing...");
      Helper.rsync();
      printHTML(htmlout, "Producer.Startpage: rsync done");
    }
  }
}

