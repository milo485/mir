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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.log.LoggerWrapper;
import mir.misc.FileExtFilter;
import mir.misc.HTMLTemplateProcessor;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleException;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleList;

/*
 *  ServletModuleFileEdit -
 *  Allows one to do a basic edit of a file in a directory specified
 *  in the config file.
 *
 * @author $Author: idfx $
 * @version $Revision: 1.4 $ $Date: 2003/01/25 17:50:36 $
 *
 */

public class ServletModuleFileEdit extends ServletModule
{

// Singelton / Kontruktor

  private static ServletModuleFileEdit instance =
      new ServletModuleFileEdit();
  public static ServletModule getInstance() { return instance; }

  private String _dirName;
  private String _extName;

  private ServletModuleFileEdit() {
    super();

    logger = new LoggerWrapper("ServletModule.FileEdit");

    _dirName = configuration.getString("ServletModule.FileEdit.FileDirectory");
    _extName = configuration.getString("ServletModule.FileEdit.ExtFilter");

    templateListString =configuration.getString("ServletModule.FileEdit.ListTemplate");
    templateObjektString =configuration.getString("ServletModule.FileEdit.ObjektTemplate");
    templateConfirmString =configuration.getString("ServletModule.FileEdit.ConfirmTemplate");
  }

  public void list(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException
  {
// fetch and deliver
    try {
      SimpleHash mergeData = new SimpleHash();
      String offset = req.getParameter("offset");
      if (offset==null || offset.equals("")) offset="0";
      mergeData.put("offset",offset);
      File dir = new File(_dirName);
      System.out.println("DIRNAME: "+_dirName);
      FileExtFilter extFilter = new FileExtFilter(_extName);
      String[] dirEntries = dir.list(extFilter);
      SimpleList theList = new SimpleList();
      for ( int i = 0; i < dirEntries.length; ++i ) {
        System.out.println(" FILE: "+dirEntries[i]);
        theList.add(dirEntries[i]);
      }
      mergeData.put("filelist",theList);

      HTMLTemplateProcessor.process(res, templateListString, mergeData, res.getWriter(), getLocale(req));
    }
    catch (IOException e) {throw new ServletModuleException(e.toString());}
    catch (Exception e) {throw new ServletModuleException(e.toString());}
  }

  public void edit(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException
  {
    String filename = req.getParameter("filename");
    if (filename == null) throw new ServletModuleException("No filename specified");
    try {

      File f = new File(_dirName, filename);
      FileReader in = new FileReader(f);
      StringWriter out = new StringWriter();

      int c;
      while ((c = in.read()) != -1)
        out.write(c);
      in.close();
      out.close();
      SimpleHash withValues = new SimpleHash();
      withValues.put("text", out.toString());
      withValues.put("filename", filename);


      deliver(req, res, withValues, null, templateObjektString);
    } catch (Exception e) {
      throw new ServletModuleException(e.toString());
    }
  }

  public void update(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException
  {
    String filename = req.getParameter("filename");
    if (filename == null) throw new ServletModuleException("No filename specified");
    try {

      File f = new File(_dirName, filename);
      StringReader in = new StringReader(req.getParameter("text"));
      FileWriter out = new FileWriter(f);

      int c;
      while ((c = in.read()) != -1)
        out.write(c);
      in.close();
      out.close();

      edit(req, res);
    } catch (Exception e) {
      throw new ServletModuleException(e.toString());
    }
  }
}
