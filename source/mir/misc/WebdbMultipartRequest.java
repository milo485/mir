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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Indymedia
 * @author
 * @version 1.0
 */

public class WebdbMultipartRequest
{
  HttpServletRequest    req=null;
  Hashtable             parameters = new Hashtable();
  MultipartParser       mp=null;
  FileHandler           _fHandler;

  public WebdbMultipartRequest(HttpServletRequest theReq, FileHandler handler)
    throws FileHandlerException, FileHandlerUserException, IOException, PropertiesConfigExc
  {
    req=theReq;
    int maxSize;
    try {
      maxSize =
        MirPropertiesConfiguration.instance().getInt("MaxMediaUploadSize");
    } catch (PropertiesConfigExc e) {
      maxSize = 1024;
      throw e;
    }
    mp = new MultipartParser(req, 1024*maxSize);
    _fHandler = handler;
    _evaluateRequest();
  }


  /**
   * The following comment and some code was adapted from the Oreilley cos.jar 
   * package. -mh 2001.09.20
   *
   * Returns all the parameters as a HashMap of Strings, any parameter 
   * that sent without a value will be null.  A value 
   * is guaranteed to be in its normal, decoded form.  If A parameter 
   * has multiple values, only the last one is returned (for backward 
   * compatibility).  For parameters with multiple values, it's possible
   * the last "value" may be null.
   *
   * @return A HashMap of String representations of the  parameter values.
   */
  public HashMap getParameters(){
    HashMap pHash = new HashMap();
    String value = new String();

    Enumeration Keys = parameters.keys();
    while(Keys.hasMoreElements()) {
      String KeyNm = (String)Keys.nextElement();
      Vector values = (Vector)parameters.get(KeyNm);
      if (values == null || values.size() == 0) {
        value = null;
      } else {
        value = (String)values.elementAt(values.size() - 1);
      } //endif
      pHash.put(KeyNm, value);
    } // end while
    return pHash;
  }

  /**
   * The following code and comment stolen from oreilley cos.jar.
   * -mh. 2001.09.20
   *
   * Returns the values of the named parameter as a String array, or null if 
   * the parameter was not sent.  The array has one entry for each parameter 
   * field sent.  If any field was sent without a value that entry is stored 
   * in the array as a null.  The values are guaranteed to be in their 
   * normal, decoded form.  A single value is returned as a one-element array.
   *
   * @param name the parameter name.
   * @return the parameter values.
   */
  public String[] getParameterValues(String name) {
    try {
      Vector values = (Vector)parameters.get(name);
      if (values == null || values.size() == 0) {
        return null;
      }
      String[] valuesArray = new String[values.size()];
      values.copyInto(valuesArray);
      return valuesArray;
    }
    catch (Exception e) {
      return null;
    }
  }

  private void _evaluateRequest() throws FileHandlerException,
    FileHandlerUserException, IOException {

    Part part;
    int i = 1;
    while ((part = mp.readNextPart()) != null) {
      String name = part.getName();
      if (part.isParam()) {
        // It's a parameter part, add it to the vector of values
        ParamPart paramPart = (ParamPart) part;
        String value = paramPart.getStringValue();
        Vector existingValues = (Vector)parameters.get(name);
        if (existingValues == null) {
          existingValues = new Vector();
          parameters.put(name, existingValues);
        }
        existingValues.addElement(value);
      }
      else if (part.isFile()) {
        // nur das erste uploadfile beruecksichtigen
        FilePart filePart = (FilePart) part;
        String fn = filePart.getFileName();
        if (filePart.getFileName() != null) {
          if (_fHandler != null)
            _fHandler.setFile(filePart, i, getParameters());
          i++;
        }
      }
    } // while */
  }

}
