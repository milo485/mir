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
package mir.util;

import gnu.regexp.RE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class FileFunctions {
  protected static final int FILE_COPY_BUFFER_SIZE = 65536;

  private FileFunctions() {
  }

  public static void copyFile(File aSourceFile, File aDestinationFile) throws IOException {
    FileInputStream inputStream;
    FileOutputStream outputStream;
    int nrBytesRead;
    byte[] buffer = new byte[FILE_COPY_BUFFER_SIZE];

    inputStream = new FileInputStream(aSourceFile);
    try {
      File directory = new File(aDestinationFile.getParent());
        if (directory!=null && !directory.exists()){
          directory.mkdirs();
      }
      outputStream = new FileOutputStream(aDestinationFile);
      try {
        do {
          nrBytesRead = inputStream.read(buffer);
          if (nrBytesRead>0)
            outputStream.write(buffer, 0, nrBytesRead);
        }
        while (nrBytesRead>=0);
      }
      finally {
        outputStream.close();
      }
    }
    finally {
      inputStream.close();
    }
  }

  public static void copyDirectory(File aSourceDirectory, File aDestinationDirectory) throws IOException {
    int i;
    File sourceFile;
    File destinationFile;
    File[] files = aSourceDirectory.listFiles();

    if (!aDestinationDirectory.exists())
      aDestinationDirectory.mkdirs();

    for (i=0; i<files.length; i++) {
      sourceFile = files[i];
      destinationFile=new File(aDestinationDirectory, sourceFile.getName());
      if (sourceFile.isDirectory()) {
        if (!destinationFile.exists())
          destinationFile.mkdir();
        copyDirectory(sourceFile, destinationFile);
      }
      else {
        copyFile(sourceFile, destinationFile);
      }
    }
  }

  public static void copy(File aSource, File aDestination) throws IOException {
    if (aSource.isDirectory()) {
      copyDirectory(aSource, aDestination);
    }
    else if (aDestination.isDirectory()) {
      copyFile(aSource, new File(aDestination, aSource.getName()));
    }
    else {
      copyFile(aSource, aDestination);
    }
  }

  public static class RegExpFileFilter implements FilenameFilter {
    private RE expression;

    public RegExpFileFilter(String anExpression) {
      try {
        expression = new RE(anExpression);
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }

    public boolean accept(File aDir, String aName) {
      return expression.isMatch(aName) && !new File(aDir, aName).isDirectory();
    }
  }

  public static class DirectoryFilter implements FilenameFilter {
    public DirectoryFilter() {
    }

    public boolean accept(File aDir, String aName) {
      return new File(aDir, aName).isDirectory();
    }

  }

  public static List getDirectoryContentsAsList(File aDirectory, FilenameFilter aFilter) {
    Object[] contents = aDirectory.list(aFilter);
    if (contents==null)
      return new Vector();
    else
      return Arrays.asList(contents);
  }


}