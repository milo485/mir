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

package  mir.misc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;


/**
 * Hilfs-Klasse, die in Logfiles schreibt.
 *
 */
public final class Logfile {
		public static final int LOG_INFO = 0;
		public static final int LOG_WARNING = 1;
		public static final int LOG_ERROR = 2;
		public static final int LOG_DEBINFO = 3;

		private static String lineSeparator;

		private static HashMap /* filename / instance */ instanceRepository;
		private RandomAccessFile raf;
		private String fileName;

	/**
	 * lineSeparator ermitteln und Repository anlegen
	 */
		static {
			// System.runFinalizersOnExit(true);
			lineSeparator = System.getProperty("line.separator");
			instanceRepository = new HashMap();
		}

	/**
	 * Singleton zurueckliefern, anhand des Filenamens,
	 * also pro <code>fileName</code> wird eine Instanz der Logfileklassen
	 * angelegt.
	 *
	 * @param fileName
	 * @return Logfile
	 */
		public static Logfile getInstance(String fileName) {
			Logfile returnLogfile = null;
			System.err.println(fileName);
			if (fileName != null) {
				if (instanceRepository.containsKey(fileName)) {
					returnLogfile = (Logfile) instanceRepository.get(fileName);
				} else {
					returnLogfile = new Logfile(fileName);
					instanceRepository.put(fileName, returnLogfile);
				}
			} else {
				System.err.println("Fehler bei Instantiierung von Logfile");
			}
			return returnLogfile;
		}

	/**
	 * Privater Konstruktor
	 * @param   String fileName
	 */
		private Logfile(String fileName){
			this.fileName = fileName;
			try {
				File f = new File(fileName);
				File dir = new File(f.getParent());
				dir.mkdirs();
				raf = new RandomAccessFile(fileName, "rw");
			} catch (IOException e) {
				System.err.println("Could not open logfile '"+fileName+"'");
			}
		}

	/**
	 * Private Methode, um eine Zeile auszugeben
	 *
	 * @param type  Typ der Logfilezeile (INFO, WARNING, ERROR, DEBUG)
	 * @param text  Lognachricht
	 * @todo an dieser Stelle koennte statt in das File in die Datenbank geloggt werden.
	 */
		private void print(int type, String text) {
	if (text == null) text = "null";
	text = text.replace('\n', ' ');

	String typeText =
			type == LOG_DEBINFO ? "DEBINFO " :
			type == LOG_INFO    ? "INFO    " :
			type == LOG_WARNING ? "WARNING " :
			type == LOG_ERROR   ? "ERROR   " :
			"?       ";

	String sectionText = text;
	GregorianCalendar date = new GregorianCalendar();

	String line = StringUtil.pad2(date.get(Calendar.DATE))+"-"+
			StringUtil.pad2(date.get(Calendar.MONTH)+1)+"-"+
			StringUtil.pad2(date.get(Calendar.YEAR) % 100)+" ";
	int hour = date.get(Calendar.HOUR);
	if (date.get(Calendar.AM_PM) == Calendar.PM) hour+=12;
	line += StringUtil.pad2(hour)+":"+
			StringUtil.pad2(date.get(Calendar.MINUTE))+":"+
			StringUtil.pad2(date.get(Calendar.SECOND))+" "+
			typeText+sectionText;

	print(line);
		}

	/**
	 *  Interne Ausgabeprozedur.
	 *	Erfordert etwas Handarbeit, da PrintStream nicht mit RandomAcccessFile
	 *	kooperiert. Und ein RandomAccessFile brauchen wir, weil FileOutputStream
	 *	kein "append" zul??t.
	 *
	 */
		private void print(String line) {
	if (raf == null) return;
	line += lineSeparator;
	//	byte[] buf = new byte[line.length()];
	//line.getBytes(0, line.length(), buf, 0);

	byte[] buf = line.getBytes();

	try {
			raf.seek(raf.length());
			raf.write(buf, 0, line.length());
	} catch (IOException e) {
			System.err.print("Could not write logfile line: "+line);
	}
		}

	/**
	 * Schreibt Information <code>text</code> ins Logfil.
	 * @param text
	 */
	public void printInfo (String text) {
		print(LOG_INFO, text);
	}

	/**
	 * Schreibt Warnung <code>text</code> ins Logfile.
	 * @param text
	 */
	public void printWarning (String text) {
		print(LOG_WARNING, text);
	}

	/**
	 * Schreibt Fehlermeldung <code>text</code> ins Logfile.
	 * @param text
	 */
	public void printError (String text) {
		print(LOG_ERROR, text);
	}

	/**
	 * Schreibt Debuginformation <code>text</code> ins Logfile.
	 * @param text
	 */
	public void printDebugInfo (String text) {
		print(LOG_DEBINFO, text);
	}

	/**
	 * Finalize-Methode, die alle offenen Dateien schliesst.
	 */
	public void finalize () {
		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) { ; }
			raf = null;
		}
		staticFinalize(fileName);
		try {
			super.finalize();
		} catch (Throwable t) {
			;
		}
	}

	/**
	 * Static-Finalizer
	 * @param fileName
	 */
	private static synchronized void staticFinalize (String fileName) {
		instanceRepository.remove(fileName);
	}
}



