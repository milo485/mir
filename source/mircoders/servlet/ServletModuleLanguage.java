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
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import mir.servlet.*;
import mir.module.*;
import mir.misc.*;
import mir.entity.*;
import mir.storage.*;

import mircoders.entity.*;
import mircoders.storage.*;
import mircoders.module.*;

/*
 *  ServletModuleLanguage -
 *  Servlet-Interface to Language
 *
 *
 * @author idefix
 */

public class ServletModuleLanguage extends ServletModule
{

  // Singelton / Kontruktor
  private static ServletModuleLanguage instance = new ServletModuleLanguage();
  public static ServletModule getInstance() { return instance; }

  private ServletModuleLanguage() {
	  theLog = Logfile.getInstance(MirConfig.getProp("Home") + MirConfig.getProp("ServletModule.Language.Logfile"));
    templateListString = MirConfig.getProp("ServletModule.Language.ListTemplate");
	  templateObjektString = MirConfig.getProp("ServletModule.Language.ObjektTemplate");
	  templateConfirmString = MirConfig.getProp("ServletModule.Language.ConfirmTemplate");
    try {
      mainModule = new ModuleLanguage(DatabaseLanguage.getInstance());
    } catch (StorageObjectException e) {
      theLog.printDebugInfo("servletmodulelanguage konnte nicht initialisiert werden");
    }
  }
}
