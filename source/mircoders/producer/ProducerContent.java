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
import mircoders.storage.*;

//for pdf production
import org.apache.fop.apps.* ;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.apache.log.*;

public class ProducerContent extends Producer {

	private String contentTemplate=MirConfig.getProp("Producer.Content.Template");
	private String contentPrintableTemplate=MirConfig.getProp("Producer.PrintableContent.Template");
	private String html2foStyleSheetName=MirConfig.getProp("Producer.PrintableContent.html2foStyleSheetName");
	private String generateFO=MirConfig.getProp("GenerateFO");
	private String generatePDF=MirConfig.getProp("GeneratePDF");
	private String producerStorageRoot=MirConfig.getProp("Producer.StorageRoot");
	private Logger fopLog=null;
	private String templateDir = MirConfig.getPropWithHome("HTMLTemplateProcessor.Dir");
	
	public static void main(String argv[]){
		//Configuration.initConfig("config");
		System.out.println(MirConfig.getProp("Producer.DocRoot"));

		try {
			new ProducerContent().handle(new PrintWriter(System.out), null,
																		false,false);
		} catch(Exception e) {
			System.err.println(e.toString());
		}
	}

	public void handle(PrintWriter htmlout, EntityUsers user, boolean force,
										boolean sync)
		throws StorageObjectException, ModuleException {

		handle(htmlout,user,force,sync,null);
	}

	public void handle(PrintWriter htmlout, EntityUsers user, boolean force,
										 boolean sync, String id) throws StorageObjectException,
										 ModuleException
	{

		long                startTime = System.currentTimeMillis();
		int                 pageCount=0;

		String              whereClause = " ";
		String              orderBy = " ";
		String              htmlFileName = null;
		String              foFileName = null;
		String              pdfFileName = null;
		EntityContent       currentContent;
		EntityList          batchEntityList;
		EntityUsers         userEntity=null;

		int                 contentBatchsize =
						Integer.parseInt(MirConfig.getProp("Producer.Content.Batchsize"));
		// production of the content-pages

		/** @todo this should be moved to ModuleContent */
		orderBy="webdb_lastchange desc";
		if(force==true){
			whereClause="is_published='1'";
			// if true: produces a single content item
			if(id !=null){
				whereClause += " AND id="+id;
				// I think this avoids a select count(*)...
				contentBatchsize=-1;
			}
			batchEntityList = contentModule.getContent(whereClause, orderBy, 0,
																								contentBatchsize, userEntity);
		} else {
			whereClause="is_produced='0' AND is_published='1'";
			//if true produces a single contentitem
			if(id !=null){
				whereClause += " AND id="+id;
				// this avoids a select count(*)...
				contentBatchsize=-1;
			}
			batchEntityList = contentModule.getContent(whereClause, orderBy, 0,
																								contentBatchsize, userEntity);
		}

		while (batchEntityList!=null) {
			for(int i=0;i<batchEntityList.size();i++) {
				currentContent = (EntityContent)batchEntityList.elementAt(i);

				try {

					SimpleHash mergeData=new SimpleHash();
					mergeData.put("content", currentContent);

					/** @todo this should be assembled in entity */
					String date = currentContent.getValue("date");
					String year = date.substring(0,4);
					String month = date.substring(4,6);
					htmlFileName =  "/" + year + "/" + month + "/" +
													currentContent.getValue("id") + ".shtml";
					
					//produce html
					boolean retVal = produce(contentTemplate, htmlFileName, mergeData, htmlout);
					if ( retVal ) currentContent.setProduced(true);
					
					//produce xsl:fo and pdf version(if desired)
					if (generateFO.toLowerCase().equals("yes")){
            foFileName =  "/" + year + "/" + month + "/"
                          + currentContent.getValue("id") + ".fo";
            boolean foRetVal = produce(contentPrintableTemplate, foFileName,
                                        mergeData, htmlout, "UTF8");
					
            if (generatePDF.toLowerCase().equals("yes")){
              pdfFileName =  producerStorageRoot + "/" + year
                              + "/" + month + "/"
                              + currentContent.getValue("id") + ".pdf";
              Driver driver = new Driver();
						
              Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
              fopLog = hierarchy.getLoggerFor("fop");
              fopLog.setPriority(Priority.WARN);
						
              driver.setLogger(fopLog);
              driver.setRenderer(Driver.RENDER_PDF);
              File foFile=new File(producerStorageRoot + foFileName);
              File html2foStyleSheet=new File(templateDir+"/"
                                              +html2foStyleSheetName);
              InputHandler inputHandler =
                new XSLTInputHandler(foFile, html2foStyleSheet);
              XMLReader parser = inputHandler.getParser();
              driver.setOutputStream(new FileOutputStream(pdfFileName));
              driver.render(parser, inputHandler.getInputSource());
            }
					}
				} catch(Exception e) {
					String errorText = "Producer.Content <font color=red>ERROR</font> while producing content ID:"
										+ currentContent.getId()+", skipping it :: "+e.toString();
					logHTML(htmlout, errorText);
					theLog.printError(errorText);
					e.printStackTrace();
				}
				pageCount++;
			}//for
			// if next batch get it...
			if (batchEntityList.hasNextBatch()){
				batchEntityList = contentModule.getContent(whereClause, orderBy,
																				batchEntityList.getNextBatch(),
																				contentBatchsize, userEntity);
			} else {
				batchEntityList=null;
			}
		}
		logHTMLFinish(htmlout, "Content", pageCount, startTime, System.currentTimeMillis());
		/** @todo why no syncing here? */
	}
}

