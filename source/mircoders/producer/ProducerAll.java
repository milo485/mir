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

import mircoders.module.*;
import mircoders.entity.*;
import mircoders.storage.*;


public class ProducerAll extends Producer{

	private boolean rsync;

	public static void main(String argv[])
	{
		try {	new ProducerAll().handle(new PrintWriter(System.out), null, false,false);	}
		catch(Exception e) { System.err.println(e.toString()); }
	}

	// handle all
	public void handle(PrintWriter htmlout, EntityUsers user, boolean force,boolean sync)
    {
		printHTML(htmlout, "Producer.All: started");

		long                sessionConnectTime = 0;
		long                startTime = (new java.util.Date()).getTime();
    
        try {
            new ProducerImages().handle(htmlout, user, force,sync);
        } catch (Exception e) {
            logHTML(htmlout, "Producer.All <font color=\"red\">ERROR:</font>"
                +" in ProducerImages continuing "+ e.toString());
        }
        try {
            new ProducerAudio().handle(htmlout, user, force,sync);
        } catch (Exception e) {
            logHTML(htmlout, "Producer.All <font color=\"red\">ERROR:</font>"
                +" in ProducerAudio continuing "+ e.toString());
        }
        try {
            new ProducerVideo().handle(htmlout, user, force,sync);
        } catch (Exception e) {
            logHTML(htmlout, "Producer.All <font color=\"red\">ERROR:</font>"
                +" in ProducerVideo continuing "+ e.toString());
        }
        try {
            new ProducerOther().handle(htmlout, user, force,sync);
        } catch (Exception e) {
            logHTML(htmlout, "Producer.All <font color=\"red\">ERROR:</font>"
                +" in ProducerOther continuing "+ e.toString());
        }
        try {
            new ProducerStartPage().handle(htmlout, user, force,sync);
        } catch (Exception e) {
            logHTML(htmlout, "Producer.All <font color=\"red\">ERROR:</font>"
                +" in Producer.StartPage continuing "+ e.toString());
        }
        try {
            new ProducerContent().handle(htmlout, user, force,sync);
        } catch (Exception e) {
            logHTML(htmlout, "Producer.All <font color=\"red\">ERROR:</font>"
                +" in Producer.Content continuing "+ e.toString());
        }
        try {
            new ProducerOpenPosting().handle(htmlout, user, force,sync);
        } catch (Exception e) {
            logHTML(htmlout, "Producer.All <font color=\"red\">ERROR:</font>"
                +" in Producer.OpenPosting continuing "+ e.toString());
        }
        try {
            new ProducerTopics().handle(htmlout, user, force,sync);
        } catch (Exception e) {
            logHTML(htmlout, "Producer.All <font color=\"red\">ERROR:</font>"
                +" in Producer.Topics continuing "+ e.toString());
        }
        try {
            new ProducerNavigation().handle(htmlout, user, force,sync);
        } catch (Exception e) {
            logHTML(htmlout, "Producer.All <font color=\"red\">ERROR:</font>"
                +" in Producer.Navigation continuing "+ e.toString());
        }

		// Finish
		sessionConnectTime = new java.util.Date().getTime() - startTime;
		logHTML(htmlout, "Producer.All finished: " + sessionConnectTime + " ms.");

		// do we have to rsync the site
		if (sync==true){
			sessionConnectTime = 0;
			if (Helper.rsync()!=0){
				sessionConnectTime = new java.util.Date().getTime() - startTime;
				logHTML(htmlout, "Rsync failed: " + sessionConnectTime + " ms.");
			} else {
				sessionConnectTime = new java.util.Date().getTime() - startTime;
				logHTML(htmlout, "Rsync succeded: " + sessionConnectTime + " ms.");
			}
		}
	}

	// handle all
	public void handle2(PrintWriter htmlout, EntityUsers user, boolean force,boolean sync)
		throws StorageObjectException, ModuleException {
		printHTML(htmlout, "Producer.All: started");

		long                sessionConnectTime = 0;
		long                startTime = (new java.util.Date()).getTime();
		EntityContent   currentContent;

		//get all new unproduced content-entities
		String whereClause="is_produced='0' && to_article_type>0";
		String orderBy="webdb_create desc";
		EntityList entityList = contentModule.getContent(whereClause,orderBy,0,-1,null);

		//get their values
		while (entityList != null) {
			for(int i=0;i<entityList.size();i++) {
				currentContent = (EntityContent)entityList.elementAt(i);
				EntityList topicEntityList = DatabaseContentToTopics.getInstance().getTopics(currentContent);
				SimpleHash topicHash = HTMLTemplateProcessor.makeSimpleHash(topicEntityList);

				try {
					//check if this content item is related to a topic
					if(currentContent.getId().equals(topicHash.get("content_id"))){
						// produce the ToicsList
						new ProducerTopics().handle(htmlout, user, force,sync,topicHash.get("topic_id").toString());
					}
				} catch (TemplateModelException e) {
					logHTML(htmlout, e.toString());
				}
			}
		}

		new ProducerContent().handle(htmlout, user, force,sync);
		new ProducerOpenPosting().handle(htmlout, user, force,sync);
		new ProducerStartPage().handle(htmlout, user, force,sync);
		new ProducerTopics().handle(htmlout, user, force,sync);

		// Finish
		sessionConnectTime = new java.util.Date().getTime() - startTime;
		logHTML(htmlout, "Producer.All finished: " + sessionConnectTime + " ms.");
	}
}

