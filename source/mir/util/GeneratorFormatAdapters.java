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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import mir.generator.Generator;
import mir.generator.GeneratorExc;
import mir.generator.GeneratorFailure;
import mir.misc.StringUtil;

public class GeneratorFormatAdapters {
  public static class NumberFormatAdapter {
    private Number value;

    public NumberFormatAdapter(Number aValue) {
      value = aValue;
    }

    public Generator.GeneratorFunction getFormat() {
      return new NumberFormattingFunction();
    }

    private class NumberFormattingFunction implements Generator.GeneratorFunction {
      public Object perform(List aParameters) throws GeneratorExc, GeneratorFailure {
        try {
          if (aParameters.size() != 1 || ! (aParameters.get(0)instanceof String))
            throw new GeneratorExc("NumberFormattingFunction <format> : exactly 1 string parameter expected");

          return new DecimalFormat( (String) (aParameters.get(0))).format(value);
        }
        catch (GeneratorExc e) {
          throw e;
        }
        catch (Throwable t) {
          throw new GeneratorFailure("NumberFormattingFunction: " + t.getMessage(), t);
        }
      };
    }
  }

  public static class DateFormatAdapter {
    private Date value;
    private TimeZone defaultTimezone;
    private String defaultTimezoneName;

    public DateFormatAdapter(Date aValue) {
      this(aValue, "");
    }

    public DateFormatAdapter(Date aValue, String aDefaultTimezone) {
      value = aValue;
      defaultTimezoneName = aDefaultTimezone;
      defaultTimezone = null;
    }

    private TimeZone getDefaultTimezone() {
      if (defaultTimezone == null) {
        try {
          defaultTimezone = TimeZone.getTimeZone(defaultTimezoneName);
        }
        catch (Throwable t) {
        }

        if (defaultTimezone==null)
          defaultTimezone = TimeZone.getDefault();
      }

      return defaultTimezone;
    }

    public Generator.GeneratorFunction getFormat() {
      return new DateFormattingFunction();
    }

    public Map getFormatted() {
      return new DateToMapAdapter();
    }

    public Date getDate() {
      return value;
    }

    private class DateFormattingFunction implements Generator.GeneratorFunction {
      public Object perform(List aParameters) throws GeneratorExc, GeneratorFailure {
        try {
          if (aParameters.size() < 1 || aParameters.size() > 2 ||
              !(aParameters.get(0) instanceof String) || (aParameters.size()>1 && ! (aParameters.get(1) instanceof String)))
            throw new GeneratorExc("DateFormattingFunction <format> [timezone]: 1 or 2 string parameters expected");

          SimpleDateFormat dateFormat = new SimpleDateFormat( (String) (aParameters.get(0)));

          TimeZone timezone = null;
          if (aParameters.size() > 1) {
            try  {
              timezone = TimeZone.getTimeZone( (String) aParameters.get(1));
            }
            catch (Throwable t) {
            }
          }

          if (timezone == null)
            timezone = getDefaultTimezone();

          dateFormat.setTimeZone(timezone);

          return dateFormat.format(value);
        }
        catch (GeneratorExc e) {
          throw e;
        }
        catch (Throwable t) {
          throw new GeneratorFailure("DateFormattingFunction: " + t.getMessage(), t);
        }
      };
    }

    /**
     *
     * retained for backwards compatibility
     *
     * <p>Title: </p>
     * <p>Description: </p>
     * <p>Copyright: Copyright (c) 2003</p>
     * <p>Company: </p>
     * @author not attributable
     * @version 1.0
     */

    public class DateToMapAdapter extends AbstractMap {
      public Object get(Object aKey) {
        if (aKey instanceof String) {
          try {
            // ML: quick fix to allow for the dc encoding now...
            if ( ( (String) aKey).equals("dc")) {
              GregorianCalendar calendar = new GregorianCalendar();
              calendar.setTime(value);
              calendar.setTimeZone(getDefaultTimezone());
              return StringUtil.date2w3DateTime(calendar);
            }
            else {
              DateFormat dateFormat = new SimpleDateFormat( (String) aKey);
              dateFormat.setTimeZone(getDefaultTimezone());

              return dateFormat.format(value);
            }
          }
          catch (Throwable t) {
            throw new RuntimeException("Can't format date with format " + (String) aKey + ": " + t.getMessage());
          }
        }
        else
          return null;
      }

      public Set entrySet() {
        return new HashSet();
      }
    }
  }
}