package mir.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FilenameFilter;

import gnu.regexp.RE;

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

}