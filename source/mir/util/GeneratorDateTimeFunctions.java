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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import mir.generator.Generator;
import mir.generator.GeneratorExc;
import mir.generator.GeneratorFailure;

public class GeneratorDateTimeFunctions {
  private GeneratorDateTimeFunctions() {
  }

  public static class FormatDateFunction implements Generator.GeneratorFunction {
    private String defaultTimezone;

    public FormatDateFunction(String aDefaultTimeZone) {
      defaultTimezone = aDefaultTimeZone;
    }

    public Object perform(List aParameters) throws GeneratorExc, GeneratorFailure {
      try {
        if (aParameters.size()<2 || aParameters.size()>3)
          throw new GeneratorExc("dateFormatFunction <date> <format> [<timezone>]: 2 or 3 parameters expected");

        if (!(aParameters.get(0) instanceof Date) ||
            !(aParameters.get(1) instanceof String) ||
            ( aParameters.size()>2 &&
             !(aParameters.get(2) instanceof String)))
          throw new GeneratorExc("dateFormatFunction <date> <format> [<timezone>]: type mismatch");


        Date date = (Date) aParameters.get(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat( (String) (aParameters.get(1)));

        String timezoneString = "";
        if (aParameters.size()>2)
          timezoneString = (String) aParameters.get(2);
        else
          timezoneString = defaultTimezone;

        TimeZone timezone = null;
        try  {
          timezone = TimeZone.getTimeZone(defaultTimezone);
        }
        catch (Throwable t) {
        }

        if (timezone == null)
          timezone = TimeZone.getDefault();

        dateFormat.setTimeZone(timezone);

        return dateFormat.format(date);
      }
      catch (GeneratorExc e) {
        throw e;
      }
      catch (Throwable t) {
        throw new GeneratorFailure("encodeURIGeneratorFunction: " + t.getMessage(), t);
      }
    };
  }


  public static class DateTimeFunctions  {
    private String defaultTimezone;
    private Generator.GeneratorFunction formatDate;

    public DateTimeFunctions(String aDefaultTimezone) {
      defaultTimezone = aDefaultTimezone;
    }

    public Generator.GeneratorFunction getFormatDate() {
      if (formatDate == null) {
        formatDate = new FormatDateFunction(defaultTimezone);
      }

      return formatDate;
    }
  }

}
