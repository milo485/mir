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
package mir.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;

/**
 * Hilfsklasse zum Mergen von Template und Daten
 */
public final class FileUtil {

  private static String producerStorageRoot;

  //
  // Initialisierung

  static {
    try {
      producerStorageRoot =
          MirPropertiesConfiguration.instance().getString("Producer.StorageRoot");
    }
    catch (PropertiesConfigExc e) {
      e.printStackTrace();
    }
  }

  /**
   * Privater Konstruktor, um versehentliche Instantiierung zu verhindern
   */
  private FileUtil() {
  }

  public static File getFile(String filename) throws IOException {

    try {
      File f = null;
      f = new File(filename);
      File dir = new File(f.getParent());
      dir.mkdirs();

      return f;
    }
    catch (Exception e) {
      throw new IOException(e.toString());
    }

  }

  public static long write(File f, InputStream in) throws IOException {

    long size = 0;

    if (in != null) {
      try {
        FileOutputStream out = new FileOutputStream(f);

        int read;
        byte[] buf = new byte[8 * 1024];
        while ( (read = in.read(buf)) != -1) {
          out.write(buf, 0, read);
          size += read;
        }

        in.close();
        out.close();
      }
      catch (IOException e) {
        throw new IOException(e.toString());
      }
    }
    return size;
  }

  public static long write(String filename, InputStream in) throws IOException {

    long size = 0;

    if (in != null) {
      try {
        File f = getFile(filename);
        size = write(f, in);
      }
      catch (IOException e) {
        throw new IOException(e.toString());
      }
    }
    return size;
  }

  public static long write(String filename, Reader in, String encoding) throws IOException {

    long size = 0;

    if (in != null) {
      try {
        File f = getFile(filename);
        FileOutputStream fOut = new FileOutputStream(f);
        OutputStreamWriter out = new OutputStreamWriter(fOut, encoding);
        int read;
        char[] cbuf = new char[8 * 1024];
        while ( (read = in.read(cbuf)) != -1) {
          out.write(cbuf, 0, read);
          size += read;
        }

        out.close();
        in.close();
      }
      catch (IOException e) {
        throw new IOException(e.toString());
      }
    }
    return size;
  }

  public static boolean read(String filename, byte out[]) throws IOException {

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
      }
      catch (IOException e) {
        throw new IOException(e.toString());
      }
    }
    else {
      return false;
    }
    return true;
  }

  public static long getSize(String filename) {
    File f = null;
    f = new File(filename);
    long l = 0;

    if (f.exists()) {
      return f.length();
    }
    else {
      return -1;
    }
  }

}
