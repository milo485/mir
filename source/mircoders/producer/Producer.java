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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import mir.config.MirPropertiesConfiguration;
import mir.misc.HTMLParseException;
import mir.misc.HTMLTemplateProcessor;
import mir.misc.LineFilterWriter;
import mir.misc.Logfile;
import mir.module.ModuleException;
import mir.storage.StorageObjectFailure;
import mircoders.entity.EntityUsers;
import mircoders.module.ModuleContent;
import mircoders.module.ModuleFeature;
import mircoders.module.ModuleImages;
import mircoders.module.ModuleLinksImcs;
import mircoders.module.ModuleSchwerpunkt;
import mircoders.module.ModuleTopics;
import mircoders.module.ModuleUploadedMedia;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseFeature;
import mircoders.storage.DatabaseImages;
import mircoders.storage.DatabaseLinksImcs;
import mircoders.storage.DatabaseTopics;
import freemarker.template.TemplateModelRoot;

abstract public class Producer {

  protected static MirPropertiesConfiguration configuration;
  protected static String   producerDocRoot; 
  protected static String   producerStorageRoot;
  protected static String   producerProductionHost;
  protected static String   producerOpenAction;
  protected static String   actionRoot; 
  protected static Logfile  theLog;
  protected static ModuleTopics         topicsModule;
  protected static ModuleLinksImcs      linksImcsModule;
  protected static ModuleSchwerpunkt    schwerpunktModule;
  protected static ModuleFeature        featureModule;
  protected static ModuleContent        contentModule;
  protected static ModuleImages         imageModule;
  protected static ModuleUploadedMedia  uploadedMediaModule;

  static {
		// init
    try {
			configuration = MirPropertiesConfiguration.instance();
      producerDocRoot = configuration.getString("Producer.DocRoot");
      producerStorageRoot = configuration.getString("Producer.StorageRoot");
      producerProductionHost = configuration.getString("Producer.ProductionHost");
      producerOpenAction = configuration.getString("Producer.OpenAction");
      actionRoot = configuration.getString("RootUri") + "/Mir";
      theLog = Logfile.getInstance(configuration.getStringWithHome("Producer.Logfile"));
      contentModule = new ModuleContent(DatabaseContent.getInstance());
      topicsModule = new ModuleTopics(DatabaseTopics.getInstance());
      linksImcsModule = new ModuleLinksImcs(DatabaseLinksImcs.getInstance());
      schwerpunktModule = new ModuleSchwerpunkt(DatabaseFeature.getInstance());
      featureModule = new ModuleFeature(DatabaseFeature.getInstance());
      imageModule = new ModuleImages(DatabaseImages.getInstance());
      uploadedMediaModule = new ModuleUploadedMedia(DatabaseImages.getInstance());

    }
    catch(Exception e)
    {
      System.err.println("*** failed to initialize Producer " + e.toString());
    }
  }

	public void handle(PrintWriter htmlout, EntityUsers user)
		throws StorageObjectFailure, ModuleException {
		handle(htmlout,user,false,false);
	}

	abstract public void handle(PrintWriter htmlout, EntityUsers user, boolean forced, boolean sync)
		throws StorageObjectFailure, ModuleException;

//
// Methods for producing files

	public boolean produce(String template, String filename, TemplateModelRoot model, PrintWriter htmlout) {
		return _produce(template, filename, model, htmlout, false,
                    configuration.getString("Mir.DefaultEncoding"));
	}

	public boolean produce(String template, String filename, TemplateModelRoot model, PrintWriter htmlout, String encoding) {
		return _produce(template, filename, model, htmlout, false, encoding);
	}

	public boolean produce_compressed(String template, String filename, TemplateModelRoot model, PrintWriter htmlout) {
		return _produce(template, filename, model, htmlout, true,
                    configuration.getString("Mir.DefaultEncoding"));
	}

	private boolean _produce(String template, String filename, TemplateModelRoot model, PrintWriter htmlout, boolean compressed, String encoding) {
		try {
			File f = new File(producerStorageRoot + filename);
			File dir = new File(f.getParent());
			dir.mkdirs();
			// it's important that we set the desired encoding. It should be UTF8
      // not the platform default.
      OutputStreamWriter outputFileStream =
        new OutputStreamWriter(new FileOutputStream(f), encoding);
			PrintWriter outStream;
			if (compressed==true) {
				outStream = new LineFilterWriter(outputFileStream);
			} else {
				outStream = new PrintWriter(outputFileStream);
			}

			HTMLTemplateProcessor.process(null,template, model, outStream,null);
			outputFileStream.close();
			outStream.close();

			printHTML(htmlout, "Produced <a href=\"" + producerProductionHost+producerDocRoot +
                        filename + "\">" + filename + "</a>");
			//theLog.printInfo("Produced: " + producerStorageRoot + filename);
    	//theLog.printDebugInfo("free mem:" + java.lang.Runtime.getRuntime().freeMemory());
      //theLog.printDebugInfo("total mem:" + java.lang.Runtime.getRuntime().totalMemory());
			return true;

		} catch(IOException exception){
			logHTML(htmlout, "Producer: File could not be written " + filename);
      System.out.println(exception.toString());
			return false;
		} catch(HTMLParseException exception){
			logHTML(htmlout,"Producer: Error in HTML-parsing: " + filename);
			return false;
		}
	}

	//
	// filename methods

	public String indexFileNameForPageCount(int pc) {
		return fileNameForPageCount("/index", pc);
	}

	public String fileNameForPageCount(String stub, int pc) {
		String fileName = producerDocRoot + stub;
		if (pc>1) {
			fileName+=pc;
		}
		fileName += ".html";
		return fileName;
	}

	/**
	 * logging
	 */

  public void logHTMLFinish(PrintWriter htmlout,String moduleName, int pageCount, long startTime, long endTime) {
    // timing and message to browser
    long overall = endTime - startTime;
    int pagesPerMinute=0; float perMinute = (float)overall/(float)60000;
    if (perMinute >0) pagesPerMinute = (int) ((float)pageCount / perMinute);

    logHTML(htmlout, "Producer."+moduleName+" finished producing: " +
            overall + " ms for "+ pageCount+" Pages = " +pagesPerMinute + " pages/min");
    printHTML(htmlout, "Back to <a href=\""+actionRoot+"\">Admin-Startage</a>");
  }

	public void logHTML(PrintWriter out, String s) {
		_print(out, s, true);
	}

	public void printHTML(PrintWriter out, String s) {
		_print(out, s, false);
	}

	private void _print(PrintWriter out, String s, boolean log) {
		if (out != null) { out.println(s+"<br />");out.flush(); }
		if (log == true) {
			theLog.printInfo(s);
		}
	}

}
