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

package  mir.storage;

import java.util.*;


/**
 * Interfacedefinition für Datenbank-Adpatoren. Die Adaptoren legen
 * jeweils das Verhalten und die Befehlsmächtigkeit der Datenbank
 * fest.
 *
 * @author <RK>
 *
 * @version $Id: DatabaseAdaptor.java,v 1.1.1.1.6.1 2002/09/01 21:31:41 mh Exp $
 */

public interface  DatabaseAdaptor{

	 /* Liefert den Namen der Adaptorklasse
	 * @return Adaptorklasse als String
	 */
	public abstract String getDriver ();


	/**
	 * Liefert die URL für JDBC zurück, in den die Parameter user, pass und host
	 * eingefügt werden. Die URL wird aus der Konfiguration geholt.
	 *
	 * @param user user als String
	 * @param pass passwort als String
	 * @param host host als String
	 * @return url als String
	 */
	public abstract String getURL (String user, String pass, String host);


	/**
	 * Gibt zurück, ob das SQL der Datenbank den <code>limit</code>-Befehl beherrscht.
	 * @return true wenn ja, sonst false
	 */
	public abstract boolean hasLimit ();


	/**
	 * Liefert zurück, ob der <code>limit</code>-Befehl erst start und dann offset
	 * hat (true), oder umgekehrt. Nur Relevant, wenn hasLimit true zurückliefert.
	 *
	 * @return true wenn erstes, sonst false
	 */
	public abstract boolean reverseLimit ();


	/**
	 * Liefert ein Properties-Objekt zurueck mit user und password.
	 * @param user
	 * @param password
	 * @return Properties
	 */
	public abstract Properties getProperties (String user, String password);


	/**
	 * Gibt SQL-Stringfragment zurück, mit dem nach einem insert-Befehl ermittelt
	 * werden kann, wie man den primary-Key des eingefügten Datensatzes bekommt.
	 *
	 * @param theDB Database-Objekt, aus dem ggf. noetige Informationen geholt
	 * werden können, wie z.B. der Tabellenname
	 * @return SQL-Statement als String
	 */
	public abstract String getLastInsertSQL (Database theDB);
}


