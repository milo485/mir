package tool;

import gnu.regexp.RE;

import java.security.MessageDigest;
import java.util.TimeZone;

import mir.util.StringRoutines;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ConfigTool {
  public ConfigTool() {
  }

  public static void timezone(String aSpecification) {
    try {
      RE specification = new RE(aSpecification);
      String[] timeZoneIds = TimeZone.getAvailableIDs();

      System.out.println("ID\tOffset\tDST?\tName");
      for (int i=0; i<timeZoneIds.length; i++) {
        if (specification.isMatch(timeZoneIds[i])) {
          TimeZone timeZone = TimeZone.getTimeZone(timeZoneIds[i]);
          long offset = timeZone.getRawOffset()/(1000*60);
          String sign = "";
          if (offset<0) {
            offset=-offset;
            sign = "-";
          }

          System.out.println(timeZone.getID() + "\t" + sign + offset/60 + ":" + StringRoutines.padStringLeft(Long.toString(offset%60),2,'0')+"\t"+(timeZone.useDaylightTime()?"yes":"no")+"\t"+ timeZone.getDisplayName());
        }
      }
    }
    catch (Throwable t) {
      System.err.println(t.toString());
    }
  }

  public static void digest(String aDigest, String aData) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(aDigest);

      System.out.println(StringRoutines.convertToHex(messageDigest.digest(aData.getBytes("UTF-8"))));
    }
    catch (Throwable t) {
      System.err.println(t.toString());
    }
  }

  public static void main(String[] anArguments) {
    String command = "help";

    if (anArguments.length >= 1) {
      command = anArguments[0];

      if (command.equals("timezone")) {
        if (anArguments.length<=2) {
          if (anArguments.length==2)
            timezone(anArguments[1]);
          else
            timezone(".*");
        }

        return;
      }
      else if (command.equals("digest")) {
        if (anArguments.length == 3) {
          digest(anArguments[1], anArguments[2]);

          return;
        }
      }
    }



    System.out.println("Usage:");

    System.out.println("  ConfigTool timezone [regexp]");
    System.out.println("");
    System.out.println("      Shows the available timezones");
    System.out.println("");
    System.out.println("  BundleTool digest <digestname> <string>");
    System.out.println("");
    System.out.println("      Calculates the digest of a string.");
    System.out.println("");
  }
}