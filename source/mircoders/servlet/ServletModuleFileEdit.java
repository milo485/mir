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

package mircoders.servlet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import mir.util.StringRoutines;
import mir.util.URLBuilder;

/*
 *  ServletModuleFileEdit -
 *  Allows one to do a basic edit of a file in a directory specified
 *  in the config file.
 *
 * @author $Author: zapata $
 * @version $Revision: 1.14 $ $Date: 2003/09/03 18:29:05 $
 *
 */

public class ServletModuleFileEdit extends ServletModule
{
  private static ServletModuleFileEdit instance = new ServletModuleFileEdit();
  public static ServletModule getInstance() { return instance; }

  private Map directories;
  private List directoryNames;

  private FilenameFilter dirFilter;

  private class FileEditDirectory {
    private String name;
    private FileFunctions.RegExpFileFilter filter;
    private File rootDirectory;
    private boolean recursive;

    public FileEditDirectory(String aName, String aRootDirectory, String aFilter, boolean aRecursive) {
      name = aName;
      rootDirectory = new File(aRootDirectory);
      filter = new FileFunctions.RegExpFileFilter(aFilter);
      recursive = aRecursive;
    }

    public String getName() {
      return name;
    }

    public FileFunctions.RegExpFileFilter getFilter() {
      return filter;
    }

    public File getRootDirectory() {
      return rootDirectory;
    }

    public boolean getRecursive() {
      return recursive;
    }
  }

  private ServletModuleFileEdit() {
    super();

    logger = new LoggerWrapper("ServletModule.FileEdit");

    directories = new HashMap();
    directoryNames = new Vector();

    String settings[] = configuration.getStringArray("ServletModule.FileEdit.Configuration");

    if (settings!=null) {
      for (int i = 0; i < settings.length; i++) {
        String setting = settings[i].trim();

        if (setting.length() > 0) {
          List parts = StringRoutines.splitString(setting, ":");
          if (parts.size() != 4) {
            logger.error("config error: " + settings[i] + ", 4 parts expected");
          }
          else {
            String name = (String) parts.get(0);
            String directory = (String) parts.get(1);
            String filter = (String) parts.get(2);
            String recursive = (String) parts.get(3);

            directories.put(name, new FileEditDirectory(name, directory, filter,
                recursive.equals("1") || recursive.toLowerCase().equals("y")));
            directoryNames.add(name);
          }
        }
      }
    }

    dirFilter = new FileFunctions.DirectoryFilter();
  }

  public List getEntries() {
    return directoryNames;
  }

  public FileEditDirectory getDirectory(HttpServletRequest aRequest) throws ServletModuleExc {
    FileEditDirectory result = (FileEditDirectory) directories.get(aRequest.getParameter("entry"));
    if (result == null)
      throw new ServletModuleExc("Unknown entry: " + aRequest.getParameter("entry"));

    return result;
  }

  public void list(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    listSubDirectory(getDirectory(aRequest), "/", aRequest, aResponse);
  }

  public void edit(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);

      String filename = requestParser.getParameter("filename");
      String subDirectory = requestParser.getParameterWithDefault("subdirectory", "");

      if (filename == null)
        throw new ServletModuleExc("No filename  specified");

      editFile(getDirectory(aRequest), filename, subDirectory, aRequest, aResponse);
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

      listSubDirectory(getDirectory(aRequest), subDirectoryName+File.separator+directoryName, aRequest, aResponse);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void update(HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    HTTPRequestParser requestParser = new HTTPRequestParser(aRequest);
    String filename = requestParser.getParameter("filename");
    String subDirectory = requestParser.getParameter("subdirectory");
    String text = requestParser.getParameter("text");
    FileEditDirectory directory = getDirectory(aRequest);

    try {
      File f = new File(new File(directory.getRootDirectory(), subDirectory), filename);

      if (validateDirectory(directory, f)) {
        StringReader in = new StringReader(text);
        FileWriter out = new FileWriter(f);

        int c;
        while ( (c = in.read()) != -1)
          out.write(c);
        in.close();
        out.close();

        editFile(directory, filename, subDirectory, aRequest, aResponse);
      }
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void listSubDirectory(FileEditDirectory aDirectory, String aSubDirectory, HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
      File dir = new File(aDirectory.getRootDirectory(), aSubDirectory);

      if (!validateDirectory(aDirectory, dir) || !dir.isDirectory()) {
        dir = aDirectory.getRootDirectory();
        aSubDirectory = "";
      }

      responseData.put("filelist", FileFunctions.getDirectoryContentsAsList(dir, aDirectory.getFilter()));

      if (aDirectory.getRecursive()) {
        List dirs = new Vector();
        if (!dir.getCanonicalPath().equals(aDirectory.getRootDirectory().getCanonicalPath()))
          responseData.put("updir", new File(aSubDirectory).getParent());

        dirs.addAll(FileFunctions.getDirectoryContentsAsList(dir, dirFilter));

        responseData.put("dirlist", dirs);
      }
      else {
        responseData.put("dirlist", null);
        responseData.put("updir", null);
      }

      responseData.put("subdirectory", aSubDirectory);
      responseData.put("entry", aDirectory.getName());

      ServletHelper.generateResponse(aResponse.getWriter(), responseData, listGenerator);
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  public void editFile(FileEditDirectory aDirectory, String aFileName, String aSubDirectory, HttpServletRequest aRequest, HttpServletResponse aResponse) throws ServletModuleExc
  {
    try {
      File f = new File(new File(aDirectory.getRootDirectory(), aSubDirectory), aFileName);

      if (!validateDirectory(aDirectory, f) || f.isDirectory() || !validateFile(aDirectory, f)) {
        listSubDirectory(aDirectory, "", aRequest, aResponse);
      }
      else {
        Map responseData = ServletHelper.makeGenerationData(aRequest, aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
        URLBuilder urlBuilder = new URLBuilder();

        urlBuilder.setValue("module", "FileEdit");
        urlBuilder.setValue("do", "enter");
        urlBuilder.setValue("entry", aDirectory.getName());
        urlBuilder.setValue("directory", "");
        urlBuilder.setValue("subdirectory", aSubDirectory);

        FileReader in = new FileReader(f);
        StringWriter out = new StringWriter();

        int c;
        while ( (c = in.read()) != -1)
          out.write(c);
        in.close();
        out.close();

        responseData.put("entry", aDirectory.getName());
        responseData.put("text", out.toString());
        responseData.put("filename", aFileName);
        responseData.put("subdirectory", aSubDirectory);
        responseData.put("returnurl", urlBuilder.getQuery());

        ServletHelper.generateResponse(aResponse.getWriter(), responseData, editGenerator);
      }
    }
    catch (Throwable e) {
      throw new ServletModuleFailure(e);
    }
  }

  protected boolean validateDirectory(FileEditDirectory aDirectory, File aFile) {
    try {
      return (aFile.getCanonicalPath().startsWith(aDirectory.getRootDirectory().getCanonicalPath()));
    }
    catch (Throwable t) {
      return false;
    }
  }

  protected boolean validateFile(FileEditDirectory aDirectory, File aFile) {
    try {
      return aDirectory.getFilter().accept(aFile.getParentFile(), aFile.getName());
    }
    catch (Throwable t) {
      return false;
    }
  }
}
