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
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Random;

/**
 * Title:        Mir
 * Description:  Abstract servlet-class
 * Copyright:    Copyright (c) 2001
 * Company:      Indymedia
 * @author       idfx
 * @version 1.0
 */

public abstract class AbstractServlet extends HttpServlet {
    protected static String lang;
    protected static Logfile theLog;

    /**
     * the configration
     */
    protected boolean getConfig(HttpServletRequest req)
            throws UnavailableException {

        //String RealPath = super.getServletContext().getRealPath("/");
        String Uri = req.getRequestURI();
        String Name = super.getServletName();
        String RootUri = StringUtil.replace(Uri, "/servlet/" + Name, "");

        // init config
        //MirConfig.initConfig(RealPath, RootUri, Name, getInitParameter("Config"));
        MirConfig.initConfig(super.getServletContext(), RootUri, Name,
                              getInitParameter("Config"));

        theLog = Logfile.getInstance(MirConfig.getPropWithHome(Name + ".Logfile"));
        theLog.printInfo(Name + " started.");
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
        /* not needed anymore due to new i18n
          File f = new File(HTMLTemplateProcessor.templateDir+"/"+lang);
        //is there an existing template-path?
        if(!f.isDirectory()){
          //no there isn't. we use standard-language
          lang = MirConfig.getProp("StandardLanguage");
          theLog.printDebugInfo("language not existing");
        }
        theLog.printDebugInfo("Language: " + lang);
        */
        return lang;
    }
}
