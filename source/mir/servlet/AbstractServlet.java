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

package mir.servlet;

import mir.misc.Logfile;
import mir.misc.MirConfig;
import mir.misc.StringUtil;
import mir.storage.StorageObjectException;

import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Random;

/**
 * Title:        Mir
 * Description:  Abstract servlet-class
 * Copyright:    Copyright (c) 2001, 2002
 * Company:      Mir-coders group
 * @author       idfx, the Mir-coders group
 * @version      $Id: AbstractServlet.java,v 1.15.4.5 2002/12/10 09:02:22 mh Exp $
 */

public abstract class AbstractServlet extends HttpServlet {
    protected static String lang;
    protected static Logfile theLog;

    protected void setNoCaching(HttpServletResponse res) {
      //nothing in Mir can or should be cached as it's all dynamic...
      //
      //this needs to be done here and not per page (via meta tags) as some
      //browsers have problems w/ it per-page -mh
      res.setHeader("Pragma", "no-cache");
      res.setDateHeader("Expires", 0);
      res.setHeader("Cache-Control", "no-cache");
    }

    /**
     * the configration
     */
    protected boolean getConfig(HttpServletRequest req)
            throws UnavailableException {

        String name = super.getServletName();

        // init config
        MirConfig.initConfig(super.getServletContext(), req.getContextPath(),
                              name, getInitParameter("Config"));

        theLog = Logfile.getInstance(MirConfig.getPropWithHome(name + ".Logfile"));
        theLog.printInfo(name + " started.");
        theLog.printInfo("Path is: " + MirConfig.getProp("Home"));
        theLog.printInfo("Root URI is: " + MirConfig.getProp("RootUri"));
        theLog.printInfo("StandardLanguage is: " + MirConfig.getProp("StandardLanguage"));
        try {
            MirConfig.initDbPool();
        }
        catch (StorageObjectException e) {
            throw new UnavailableException(
                    "Could not initialize database pool. -- "
                    + e.toString(), 0);
        }
        super.getServletContext().setAttribute("mir.confed", new Boolean(true));
        return true;
    }

    /**
     * Bind the language to the session
     */
    protected void setLanguage(HttpSession session, String language) {
        session.setAttribute("Language", language);
    }

    protected void setLocale(HttpSession session, Locale loc) {
        session.setAttribute("Locale", loc);
    }

    /**
     * Get the session-bound language
     */
    protected String getLanguage(HttpServletRequest req, HttpSession session) {
        String lang = (String) session.getAttribute("Language");
        if (lang == null || lang.equals("")) {
            return getAcceptLanguage(req);
        }
        else {
            return lang;
        }
    }

    /**
     * get the locale either from the session or the accept-language header ot the request
     * this supersedes getLanguage for the new i18n
     */
    public Locale getLocale(HttpServletRequest req) {
        Locale loc=null;
        HttpSession session = req.getSession(false);
        if (session!=null) {
            // session can be null in case of logout
            loc = (Locale) session.getAttribute("Locale");
        }
        // if there is nothing in the session get it fron the accept-language
        if (loc == null) {
            loc = req.getLocale();
        }
        return loc;
    }

    /**
     * Checks the Accept-Language of the client browser.
     * If this language is available it returns its country-code,
     * else it returns the standard-language
     */
    protected String getAcceptLanguage(HttpServletRequest req) {
        Locale loc = req.getLocale();
        lang = loc.getLanguage();
        return lang;
    }
}
