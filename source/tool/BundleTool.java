package tool;

import java.util.*;
import java.io.*;
import org.apache.commons.collections.*;

import mir.util.*;

public class BundleTool {

  public static void compare(String aMaster, String aSlave) {
    PropertiesManipulator master;
    PropertiesManipulator slave;
    PropertiesManipulator result;

    try {
      master = PropertiesManipulator.readProperties(new FileInputStream(new File(aMaster)));
    }
    catch (Throwable t) {
      System.out.println("Unable to read master properties: " + t.getMessage());
      return;
    }

    try {
      slave = PropertiesManipulator.readProperties(new FileInputStream(new File(aSlave)));
    }
    catch (FileNotFoundException t) {
      slave = new PropertiesManipulator();
    }
    catch (Throwable t) {
      System.out.println("Unable to read slave properties: " + t.getMessage());
      return;
    }

    int missing=0;

    Iterator i = master.getEntries();
    while (i.hasNext()) {
      Object e = i.next();

      if (e instanceof PropertiesManipulator.Entry) {
        String key = ( (PropertiesManipulator.Entry) e).getKey();

        if (!slave.containsKey(key) || slave.get(key) == null || slave.get(key).length()==0 ) {
          if (missing==0) {
            System.out.println(aSlave+" is missing:");
          }
          System.out.println("  " + key);
          missing++;
        }
      }
    }

    if (missing>0)
      System.out.println("total missing: " +missing);

    missing=0;
    i = slave.getEntries();
    while (i.hasNext()) {
      Object e = i.next();

      if (e instanceof PropertiesManipulator.Entry) {
        String key = ( (PropertiesManipulator.Entry) e).getKey();

        if (!master.containsKey(key)) {
          if (missing==0) {
            System.out.println(aSlave+" has extra:");
          }
          System.out.println("  " + key);
          missing++;
        }
      }
    }
    if (missing>0)
      System.out.println("total extra: " +missing);
  }

  public static void align(String aMaster, String aSlave) {
    PropertiesManipulator master;
    PropertiesManipulator slave;
    PropertiesManipulator result;

    try {
      master = PropertiesManipulator.readProperties(new FileInputStream(new File(aMaster)));
    }
    catch (Throwable t) {
      System.out.println("Unable to read master properties: " + t.getMessage());
      return;
    }

    try {
      slave = PropertiesManipulator.readProperties(new FileInputStream(new File(aSlave)));
    }
    catch (FileNotFoundException t) {
      slave = new PropertiesManipulator();
    }
    catch (Throwable t) {
      System.out.println("Unable to read slave properties: " + t.getMessage());
      return;
    }

    result = new PropertiesManipulator();

    // skip past the header in the slave bundle
    Iterator i = slave.getEntries();
    while (i.hasNext()) {
      Object e = i.next();

      if (e instanceof PropertiesManipulator.EmptyLine) {
        result.addEmptyLine();
      }
      else if (e instanceof PropertiesManipulator.Comment) {
        result.addComment( ( (PropertiesManipulator.Comment) e).getComment());
      }

      if (! (e instanceof PropertiesManipulator.Comment))
        break;
    }

    boolean insideHeader = true;
    i = master.getEntries();
    while (i.hasNext()) {
      Object e = i.next();

      if (!insideHeader && (e instanceof PropertiesManipulator.EmptyLine)) {
        result.addEmptyLine();
      }
      else if (!insideHeader && e instanceof PropertiesManipulator.Comment) {
        result.addComment( ( (PropertiesManipulator.Comment) e).getComment());
      }
      else if (e instanceof PropertiesManipulator.Entry) {
        String key = ( (PropertiesManipulator.Entry) e).getKey();
        String value = slave.get(key);

        if (value==null || value.length()==0) {
          result.addComment("# missing (master value = \"" +master.get(key)+"\")");
        }

        result.addEntry(key, value);
      }

      insideHeader = insideHeader && (e instanceof PropertiesManipulator.Comment);
    }

    try {
      PropertiesManipulator.writeProperties(result, new FileOutputStream(new File(aSlave)));
    }
    catch (Throwable t) {
      System.out.println("Unable to write slave properties: " + t.getMessage());
      return;
    }
  }

  public static void encode(String aBundle, String anEncoding, String anOutputFile) {
    PropertiesManipulator bundle;

    try {
      bundle = PropertiesManipulator.readProperties(new FileInputStream(new File(aBundle)));

      PropertiesManipulator.writeProperties(bundle, new FileOutputStream(anOutputFile), anEncoding, false);
    }
    catch (Throwable t) {
      System.out.println("Unable to read master properties: " + t.getMessage());
      return;
    }
  }

  public static void decode(String aBundle, String anEncoding, String aSourceFile) {
    PropertiesManipulator bundle;

    try {
      bundle = PropertiesManipulator.readProperties(new FileInputStream(new File(aSourceFile)), anEncoding);
    }
    catch (Throwable t) {
      Throwable s = ExceptionFunctions.traceCauseException(t);

      System.out.println("Unable to read sourcefile: " + s.toString());
      return;
    }
    try {
      PropertiesManipulator.writeProperties(bundle, new FileOutputStream(aBundle));
    }
    catch (Throwable t) {
      System.out.println("Unable to write bundle: " + t.toString());
      return;
    }
  }

  public static void main(String[] anArguments) {
    String command = "help";

    if (anArguments.length >= 1) {
      command = anArguments[0];

      if (command.equals("compare")) {
        if (anArguments.length==3) {
          compare(anArguments[1], anArguments[2]);

          return;
        }
      }
      else if (command.equals("align")) {
        if (anArguments.length==3) {
          align(anArguments[1], anArguments[2]);

          return;
        }
      }
      else if (command.equals("encode")) {
        if (anArguments.length==4) {
          encode(anArguments[1], anArguments[2], anArguments[3]);

          return;
        }
      }
      else if (command.equals("decode")) {
        if (anArguments.length==4) {
          decode(anArguments[1], anArguments[2], anArguments[3]);

          return;
        }
      }
    }



    System.out.println("Usage:");

    System.out.println("  BundleTool align <master bundle> <slave bundle>");
    System.out.println("");
    System.out.println("      Reorders keys/values in a slave bundle according to a master bundle.");
    System.out.println("");
    System.out.println("  BundleTool compare  <master bundle> <slave bundle>");
    System.out.println("");
    System.out.println("      Compares availability of bundle keys.");
    System.out.println("");
    System.out.println("  BundleTool encode <bundle> <encoding> <destinationfile>");
    System.out.println("");
    System.out.println("      Encodes the keys/values with a custom encoding.");
    System.out.println("");
    System.out.println("  BundleTool decode <bundle> <encoding> <sourcefile>");
    System.out.println("");
    System.out.println("      Decodes the keys/values with a custom encoding.");
  }

}