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

package mir.misc;

import  java.lang.*;
import  java.util.*;
import  java.io.*;
import  java.net.*;
import  freemarker.template.*;
import  mir.entity.*;
import  mir.storage.*;

import javax.servlet.http.*;
import javax.servlet.*;

/**
 * Hilfsklasse zum Mergen von Template und Daten
 */
public final class FileUtil {

  private static String producerStorageRoot;

  //
  // Initialisierung

  static {
    producerStorageRoot = MirConfig.getProp("Producer.StorageRoot");
  }

  /**
   * Privater Konstruktor, um versehentliche Instantiierung zu verhindern
   */
  private FileUtil () {
  }

  public static boolean write(String filename, byte[] in)
    throws IOException {

		boolean retVal = false;

		if (in!=null) {
			try {
        File f = null;
        f = new File(filename);
				File dir = new File(f.getParent());
				dir.mkdirs();

				FileOutputStream outStream;
				outStream = new FileOutputStream(f);
				outStream.write(in);
				outStream.close();
				retVal = true;
			} catch(IOException e) {
        throw new IOException(e.toString());
      }
    }
		return retVal;
	}

  public static boolean read(String filename, byte out[])
    throws IOException {

    File f = null;
    f = new File(filename);

		if (f.exists()) {
			try {
        if (out.length != f.length())
          return false;
				FileInputStream inStream;
				inStream = new FileInputStream(f);
				inStream.read(out);
				inStream.close();
			} catch(IOException e) {
        throw new IOException(e.toString());
      }
    } else {
      return false;
    }
    return true;
  }
    
  public static long getSize(String filename) {
    File f = null;
    f = new File(filename);
    long l=0;

    if (f.exists()) {
      return f.length();
    } else {
      return -1;
    }
  }

 
}
