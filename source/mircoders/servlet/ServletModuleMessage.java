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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.log.LoggerWrapper;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleFailure;
import mircoders.global.MirGlobal;
import mircoders.module.ModuleMessage;
import mircoders.storage.DatabaseMessages;

/**
 * Title:       ServletModuleMessage
 * Description:
 * Copyright:   Copyright (c) 2001-2002
 * Company:     mir-coders
 * @author
 * @version 1.0
 */


public class ServletModuleMessage extends ServletModule
{
  private static ServletModuleMessage instance = new ServletModuleMessage();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleMessage() {
    super();
    logger = new LoggerWrapper("ServletModule.Messages");

    try {
      model = MirGlobal.localizer().dataModel().adapterModel();
      definition = "internalMessage";
      mainModule = new ModuleMessage(DatabaseMessages.getInstance());
    }
    catch (Throwable e) {
      logger.error("initialization of ServletModuleMessage failed!: " + e.getMessage());

      throw new ServletModuleFailure(e);
    }
  }

  protected void initializeNewObject(Map aNewObject, HttpServletRequest aRequest, HttpServletResponse aResponse) {
    aNewObject.put("creator", ServletHelper.getUserName(aRequest));
  }
}
