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

import java.io.*;
import java.net.*;
import javax.servlet.http.*;
import javax.servlet.*;
import freemarker.template.*;

import mir.servlet.*;
import mir.misc.*;
import mir.entity.*;
import mir.storage.*;
import mir.module.*;
import mir.log.*;

import mircoders.module.*;
import mircoders.storage.*;

/*
 *  ServletModuleBreaking -
 *  Authentified Navigation for Breaking News
 *
 *
 */

public class ServletModuleBreaking extends ServletModule
{

// Singelton / Kontruktor

  private static ServletModuleBreaking instance = new ServletModuleBreaking();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleBreaking() {
    logger = new LoggerWrapper("ServletModule.Breaking");
    templateListString = MirConfig.getProp("ServletModule.Breaking.ListTemplate");
    templateObjektString = MirConfig.getProp("ServletModule.Breaking.ObjektTemplate");
    templateConfirmString = MirConfig.getProp("ServletModule.Breaking.ConfirmTemplate");
    try {
      DatabaseBreaking dbb = DatabaseBreaking.getInstance();
      mainModule = new ModuleBreaking(dbb);
    }
    catch (StorageObjectException e) {
      logger.error("Initializatoin of ServletModuleBreaking failed!: " + e.getMessage());
    }
  }

  public void list(HttpServletRequest req, HttpServletResponse res)
      throws ServletModuleException
  {
    logger.debug("-- breaking: list");
// fetch and deliver
    try {
      SimpleHash mergeData = new SimpleHash();
      String offset = req.getParameter("offset");
      if (offset==null || offset.equals("")) offset="0";
      mergeData.put("offset",offset);
      EntityList theList = mainModule.getByWhereClause(null, "webdb_create desc", (new Integer(offset)).intValue());
      mergeData.put("contentlist",theList);
      if(theList.getOrder()!=null) {
        mergeData.put("order", theList.getOrder());
        mergeData.put("order_encoded", URLEncoder.encode(theList.getOrder()));
      }
      mergeData.put("count", (new Integer(theList.getCount())).toString());
      mergeData.put("from", (new Integer(theList.getFrom())).toString());
      mergeData.put("to", (new Integer(theList.getTo())).toString());
      if (theList.hasNextBatch())
        mergeData.put("next", (new Integer(theList.getNextBatch())).toString());
      if (theList.hasPrevBatch())
        mergeData.put("prev", (new Integer(theList.getPrevBatch())).toString());

// raus damit
      HTMLTemplateProcessor.process(res, templateListString, mergeData, res.getWriter(), getLocale(req));
    }
    catch (ModuleException e) {throw new ServletModuleException(e.toString());}
    catch (IOException e) {throw new ServletModuleException(e.toString());}
    catch (Exception e) {throw new ServletModuleException(e.toString());}
  }
}
