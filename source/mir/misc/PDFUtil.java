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

package mir.misc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletResponse;

import mircoders.global.MirGlobal;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.apps.XSLTInputHandler;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.xml.sax.XMLReader;

public class PDFUtil {
    
  public static void makePDF(String foFilePath,Object pdfDestination,String stylesheetPath) throws Exception
  {
    try{
      Driver driver = new Driver();
      
      //stupid logging that fop wants to use, needs to be changed
      Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
      Logger fopLog=null;
      fopLog = hierarchy.getLoggerFor("fop");
      fopLog.setPriority(Priority.WARN);
      driver.setLogger(fopLog);
      
      driver.setRenderer(Driver.RENDER_PDF);
  
      File foFile=new File(foFilePath);
      
      String html2foStyleSheetPath;
      if (stylesheetPath == "FROMCONFIG"){
	html2foStyleSheetPath=MirGlobal.getConfigProperty("Home") 
	  + MirGlobal.getConfigProperty("HTMLTemplateProcessor.Dir")
          + "/" 
          + MirGlobal.getConfigProperty("Producer.PrintableContent.html2foStyleSheetName"); 
      }
      else {
	  html2foStyleSheetPath=stylesheetPath;
      }
      File html2foStyleSheet=new File(html2foStyleSheetPath);
      InputHandler inputHandler =
	new XSLTInputHandler(foFile, html2foStyleSheet);
      XMLReader parser = inputHandler.getParser();
      
      if (pdfDestination instanceof String) {
	String filePath = (String) pdfDestination;
	driver.setOutputStream(new FileOutputStream(filePath));
	driver.render(parser, inputHandler.getInputSource());
      }
      else if (pdfDestination instanceof HttpServletResponse){
	HttpServletResponse res = (HttpServletResponse) pdfDestination; 
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	driver.setOutputStream(out);
	res.setContentType("application/pdf");
	
	driver.render(parser, inputHandler.getInputSource());
	
	byte[] content = out.toByteArray();
	res.setContentLength(content.length);
	res.getOutputStream().write(content);
	res.getOutputStream().flush();
      }
      else {
	throw new Exception("I'm sorry but I don't know how to output a pdf to an object of type" + pdfDestination.getClass().getName());
      }
    }
  
    catch (Exception ex){
	throw(ex);
    }
  }
}



