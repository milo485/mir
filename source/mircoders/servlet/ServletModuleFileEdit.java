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

package mircoders.servlet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.log.LoggerWrapper;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.util.FileFunctions;
import mir.util.HTTPRequestParser;
import mir.util.URLBuilder;

/*
 *  ServletModuleFileEdit -
 *  Allows one to do a basic edit of a file in a directory specified
 *  in the config file.
 *
 * @author $Author: zapata $
 * @version $Revision: 1.8 $ $Date: 2003/03/17 20:47:04 $
 *
 */

public class ServletModuleFileEdit extends ServletModule
{
  private static ServletModuleFileEdit instance = new ServletModuleFileEdit();
  public static ServletModule getInstance() { return instance; }

  private File rootDirectory;
  private FilenameFilter filter;
  private FilenameFilter dirFilter;
  private boolean recurse;

  private ServletModuleFileEdit() {
    super();

    logger = new LoggerWrapper("ServletModule.FileEdit");

    rootDirectory = new File(configuration.getString("ServletModule.FileEdit.FileDirectory"));
    recurse = configuration.getString("ServletModule.FileEdit.Recursive", "").equals("1");

    filter = new FileFunctions.RegExpFileFilter(configuration.getString("ServletModule.FileEdit.ExtFilter"));
    dirFilter = new FileFunctions.DirectoryFilter();

    templateListString =configuration.getString("ServletModule.FileEdit.ListTemplate");
    templateObjektString =configuration.getString("ServletModule.FileEdit.ObjektTemplate");
    templateConfirmString =configuration.getString("ServletModule.FileEdit.ConfirmTemplate");
  }

  public void list(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    listSubDirectory("/", aRequest, aResponse);
  }

  public void edit(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      String filename = requestParser.getParameter("filename");
      String subDirectory = requestParser.getParameterWithDefault("subdirectory", "");

      if (filename == null)
        throw new ServletModuleExc("No filename  specified");

      editFile(filename, subDirectory, aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void enter(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
      String directoryName = requestParser.getParameter("directory");
      String subDirectoryName = requestParser.getParameter("subdirectory");

      if (directoryName==null | subDirectoryName==null)
        throw new ServletModuleExc("No directory/subDirectory specified");

      listSubDirectory(subDirectoryName+File.separator+directoryName, aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }
  public void update(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    String filename = aRequest.getParameter("filename");
    String subDirectory = aRequest.getParameter("subdirectory");
    String text = aRequest.getParameter("text");

    try {
      File f = new File(new File(rootDirectory, subDirectory), filename);

      if (validateDirectory(f)) {
        StringReader in = new StringReader(text);
        FileWriter out = new FileWriter(f);

        int c;
        while ( (c = in.read()) != -1)
          out.write(c);
        in.close();
        out.close();

        editFile(filename, subDirectory, aRequest, aResponse);
      }
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void listSubDirectory(String aSubDirectory, HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      Map responseData = ServletHelper.makeGenerationData(getLocale(aRequest));
      File dir = new File(rootDirectory, aSubDirectory);

      if (!validateDirectory(dir) || !dir.isDirectory()) {
        dir = rootDirectory;
        aSubDirectory = "";
      }

      responseData.put("filelist", Arrays.asList(dir.list(filter)));

      if (recurse) {
        List dirs = new Vector();
        if (!dir.getCanonicalPath().equals(rootDirectory.getCanonicalPath()))
          responseData.put("updir", new File(aSubDirectory).getParent());

        dirs.addAll(Arrays.asList(dir.list(dirFilter)));

        responseData.put("dirlist", dirs);
      }
      else {
        responseData.put("dirlist", null);
        responseData.put("updir", null);
      }

      responseData.put("subdirectory", aSubDirectory);

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, templateListString);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void editFile(String aFileName, String aSubDirectory, HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      File f = new File(new File(rootDirectory, aSubDirectory), aFileName);

      if (!validateDirectory(f) || f.isDirectory() || !validateFile(f)) {
        listSubDirectory("", aRequest, aResponse);
      }
      else {
        Map responseData = ServletHelper.makeGenerationData(getLocale(aRequest));
        URLBuilder urlBuilder = new URLBuilder();

        urlBuilder.setValue("module", "FileEdit");
        urlBuilder.setValue("do", "enter");
        urlBuilder.setValue("directory", "");
        urlBuilder.setValue("subdirectory", aSubDirectory);

        FileReader in = new FileReader(f);
        StringWriter out = new StringWriter();

        int c;
        while ( (c = in.read()) != -1)
          out.write(c);
        in.close();
        out.close();

        responseData.put("text", out.toString());
        responseData.put("filename", aFileName);
        responseData.put("subdirectory", aSubDirectory);
        responseData.put("returnurl", urlBuilder.getQuery());

        ServletHelper.generateResponse(aResponse.getWriter(), responseData, templateObjektString);
      }
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  protected boolean validateDirectory(File aFile) {
    try {
      return (aFile.getCanonicalPath().startsWith(rootDirectory.getCanonicalPath()));
    }
    catch (Throwable t) {
      return false;
    }
  }

  protected boolean validateFile(File aFile) {
    try {
      return filter.accept(aFile.getParentFile(), aFile.getName());
    }
    catch (Throwable t) {
      return false;
    }
  }
}
