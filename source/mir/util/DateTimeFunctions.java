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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class DateTimeFunctions {
  /**
   * private parameter-less constructor to prevent construction
   */
//  private static LoggerWrapper logger = new LoggerWrapper("Utility.DatTimeFunctions");


  private DateTimeFunctions() {
  }

  /**
   * Function to parse a <a href="http://www.w3.org/TR/NOTE-datetime">W3CDTF</a> formatted string.
   *
   *
   * YYYY[-MM[-DD[Thh:mm[:ss[.s*]]TZD]]]
   *
   * @param aString
   * @return
   */
  private final static String SPACE = "[\t\n\r ]*";
  private final static String NUMBER = "[0-9]*";
  private final static String TWODIGITNUMBER = "[0-9][0-9]";
  private final static String FOURDIGITNUMBER = "[0-9][0-9][0-9][0-9]";
  private final static String FRACTION = "(\\.[0-9]*)|)";
  private final static String SIGN = "[-+]";
  private final static String TZD = "(Z|(+|-)([0-9][0-9]:[0-9][0-9]|[0-9][0-9][0-9][0-9]))";

  public static Date parseW3CDTFString(String aString) throws UtilExc, UtilFailure {
    try {
      int year = 1;
      int month = 1;
      int day = 1;
      int hour = 0;
      int minute = 0;
      int second = 0;
      int millisecond = 0;
      int houroffset = 0;
      int minuteoffset = 0;
      boolean negativeOffset = false;


      SimpleParser parser = new SimpleParser(aString.trim());
      String part = parser.parse(NUMBER);
      year=Integer.parseInt(part);
      if (parser.parses("-")) {
        parser.skip("-");
        part = parser.parse(NUMBER);
        month = Integer.parseInt(part);
        if (parser.parses("-")) {
          parser.skip("-");
          part = parser.parse(NUMBER);
          day = Integer.parseInt(part);
          if (parser.parses("T")) {
            parser.skip("T");
            part = parser.parse(NUMBER);
            hour = Integer.parseInt(part);
            parser.skip(":");
            part = parser.parse(NUMBER);
            minute = Integer.parseInt(part);
            if (parser.parses(":")) {
              parser.skip(":");
              part = parser.parse(NUMBER);
              second = Integer.parseInt(part);
              if (parser.parses("\\.")) {
                parser.skip("\\.");
                part = parser.parse(NUMBER).substring(0,3);
                while (part.length()<3)
                  part = "0" + part;
                millisecond = Integer.parseInt(part);
              }
            }
            if (parser.parses("Z|\\+|-")) {
              String sign = parser.parse("Z|\\+|-");
              if (sign.equals("+") || sign.equals("-")) {
                if (parser.parses(TWODIGITNUMBER)) {
                  part = parser.parse(TWODIGITNUMBER);
                  houroffset = Integer.parseInt(part);
                }
                if (parser.parses(":"))
                  parser.skip(":");
                if (parser.parses(TWODIGITNUMBER)) {
                  part = parser.parse(TWODIGITNUMBER);
                  minuteoffset = Integer.parseInt(part);
                }

                if (sign.equals("-")) {
                  negativeOffset=true;
                }
              }
            }
          }
        }
      }



      String timeZoneID = "GMT";
      if (negativeOffset)
        timeZoneID = timeZoneID+"-";
      timeZoneID = timeZoneID + StringRoutines.padStringLeft(Integer.toString(houroffset), 2, '0') +
                                StringRoutines.padStringLeft(Integer.toString(minuteoffset), 2, '0');


      TimeZone zone = TimeZone.getTimeZone(timeZoneID);

      Calendar calendar = new GregorianCalendar(zone);
      calendar.set(year, month-1, day, hour, minute, second);
      calendar.set(Calendar.MILLISECOND, millisecond);

      return calendar.getTime();
    }
    catch (Throwable t) {
//      logger.error("DateTimeFunctions.parseW3CDTFString: error parsing " + aString + ": " + t.toString());

      throw new UtilFailure(t);
    }
  }

  public static String advancedDateFormat(String aFormat, Date aDate, String aTimeZone) {
    return advancedDateFormat(aFormat, aDate, TimeZone.getTimeZone(aTimeZone));
  }

  public static String advancedDateFormat(String aFormat, Date aDate, TimeZone aTimeZone) {
    SimpleDateFormat simpleFormat = new SimpleDateFormat(aFormat);

    simpleFormat.setTimeZone(aTimeZone);
    return simpleFormat.format(aDate);
  }

  public static String dateToSortableString(Date aDate) {
    return advancedDateFormat("yyyyMMddHHmmss", aDate, "GMT");
  }
}