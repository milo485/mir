/*
 * ServletConstants.java created on 05.09.2003
 * 
 * Copyright (C) 2001, 2002, 2003 The Mir-coders group
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
package mir.core.ui.servlet;

/**
 * ServletConstants<br>
 * Some constant string values needed as keys to store values in the servlet context.
 * @author idefix
 * @version $Id: ServletConstants.java,v 1.2 2003/09/07 16:55:00 idfx Exp $
 */
public interface ServletConstants {
	public static final String REDIRECT_ACTION = "redirect action";

	public static final String REDIRECT_QUERY_STRING = "redirect query string";

	public static final String NEXT_OFFSET = "nextoffset";

	public static final String LAST_OFFSET = "lastoffset";

	public static final String OFFSET = "offset";

	public static final String DEFAULT_LANGUAGE = "defaultlanguage";

	public static final String LOGIN_LANGUAGES = "languages";

	public static final String SESSION_FACTORY = "session factory";

	public static final String USER = "user";

}
