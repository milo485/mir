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

package mir.util;

import javax.servlet.http.HttpServletRequest;

public class HTTPRequestParser {
  private HttpServletRequest request;
  private String encoding;

  public HTTPRequestParser(HttpServletRequest aRequest) {
    this(aRequest, aRequest.getCharacterEncoding());
  }

  public HTTPRequestParser(HttpServletRequest aRequest, String anEncoding) {
    request = aRequest;
    encoding = anEncoding;
  }

  public boolean hasParameter(String aName) {
    return request.getParameter(aName)!=null;
  }

  public String getParameterWithDefault(String aName, String aDefault) {
    if (hasParameter(aName))
      return getParameter(aName);
    else
      return aDefault;
  }

  public String getParameter(String aName) {
    try {
      String result = request.getParameter(aName);
//      String requestEncoding = request.getCharacterEncoding();
//      if (requestEncoding==null)
//        requestEncoding = "ISO-8859-1";

//      if (result != null && encoding!=null && !encoding.equals(requestEncoding)) {
//        result = new String(result.getBytes(requestEncoding), encoding);
//      }

      return result;
    }
    catch (Throwable t) {
      throw new RuntimeException("HTTPRequestParser.getParameter: " + t.getMessage());
    }
  }

  public int getIntegerWithDefault(String aName, int aDefault) {
    int result = aDefault;
    String value = getParameter(aName);

    try {
      result = Integer.parseInt(value);
    }
    catch (Throwable t) {
    }
    return result;
  }
}
