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

package  mir.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.TemplateModelRoot;


/**
 *  Standard ServletModule, dass eine Template anzeigt, und nicht
 *  mit Daten mischt. Damit ist es moeglich, einfache HTML-Seiten
 *  als templates zu benutzen. Damit kann auf einfache Weise die
 *  Funktionalitaet der Freemarker-Bibliothek (beispielsweise Navigationen
 *  per <code>include</code> einzubauen) benutzt werden. Die Templates
 *  werden aus dem per Konfiguration angegebenem template-Verzeichnis
 *  im Ordner "/html" genommen.
 *
 * @author RK
 */
public class ServletModuleShow extends ServletModule {
	private static ServletModuleShow instance = new ServletModuleShow();

	/**
	 * Ein ServletModuleShow-Objekt wird ?ber getInstance geliefert. Es gibt zur
	 * Laufzeit nur ein Objekt (Singleton)
	 * @return ServletModuleShow
	 */
	public static ServletModule getInstance () {
		return  instance;
	}

	/**
	 * Initialisierung leer.
	 */
	private ServletModuleShow () {
	}

	/**
	 * defaultAction (s.a ServletModule)
	 * @return "show"
	 */
	public String defaultAction () {
		return  "show";
	}

	/**
	 * Standardmethode, die die Funktionalitaet des ServletModules implementiert.
	 *
	 * @param req Http-Request, das vom Dispatcher durchgereicht wird
	 * @param res Http-Response, die vom Dispatcher durchgereicht wird
	 * @return String fuer Logfile
	 * @exception ServletModuleException
	 */
  public void show(HttpServletRequest req, HttpServletResponse res) throws ServletModuleException {
    try {
      String idParam = req.getParameter("tmpl");
      if (!(idParam==null || idParam.equals(""))) {
  	    deliver(req, res, (TemplateModelRoot)null, "html/"+idParam+".template");
      }
      else {
        throw new ServletModuleException("Falsches template: " + idParam);
      }
    }
    catch (Exception e) {
      throw new ServletModuleException(e.toString());
    }
  }

}




