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
import java.util.*;

import freemarker.template.*;

import mir.misc.*;
import mir.storage.*;
import mir.module.*;
import mir.entity.*;

import mircoders.entity.*;

/**
 * Title:        mir - another content management system
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      indymedia
 * @author idefix
 * @version 1.0
 */

public class ProducerNavigation extends Producer {

  private static String naviPageTemplate = MirConfig.getProp("Producer.Navigation.Template");

  public void handle(PrintWriter htmlout, EntityUsers user, boolean forced, boolean sync)
    throws mir.module.ModuleException, mir.storage.StorageObjectException {

        printHTML(htmlout, "Producer.Navigation: started");

		long                sessionConnectTime = 0;
		long                startTime = (new java.util.Date()).getTime();
		String              nowWebdbDate = StringUtil.date2webdbDate(new GregorianCalendar());
		String              whereClause;
		String              orderBy;
		FileWriter          outputFile;
		String              htmlFileName;
		EntityContent       currentContent;
		EntityList          entityList;
		SimpleHash          naviPageModel;

        // get the imclinks
        entityList = linksImcsModule.getByWhereClause("", "sortpriority, title", -1);
        EntityList theParentList = linksImcsModule.getByWhereClause("to_parent_id=NULL", "sortpriority, title", -1);

		// put the informations into the navipagemodel
		naviPageModel = new SimpleHash();
		naviPageModel.put("topics", topicsModule.getTopicsList());
        naviPageModel.put("imclist", entityList);
        naviPageModel.put("parentlist", theParentList);

		htmlFileName = "/navigation.inc";

		produce(naviPageTemplate, htmlFileName, naviPageModel, new LineFilterWriter(htmlout));

		// Finish
		sessionConnectTime = new java.util.Date().getTime() - startTime;
		logHTML(htmlout, "Producer.Navigation finished: " + sessionConnectTime + " ms.");

		if(sync==true){
			Helper.rsync();
			logHTML(htmlout, "Producer.Startseite: rsync done");
		}
	}

}
