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
package mir.entity.adapter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import mir.entity.Entity;
import mir.misc.StringUtil;
import mir.util.GeneratorFormatAdapters;

public class EntityAdapterDefinition {
  private Map calculatedFields;

  public EntityAdapterDefinition() {
    calculatedFields = new HashMap();
  }

  public EntityAdapter makeEntityAdapter(Entity anEntity, EntityAdapterModel aModel) {
    return new EntityAdapter(anEntity, this, aModel);
  }

  public CalculatedField getCalculatedField(String aFieldName) {
    return (CalculatedField) calculatedFields.get(aFieldName);
  }

  public boolean hasCalculatedField(String aFieldName) {
    return calculatedFields.containsKey(aFieldName);
  }

  public void addCalculatedField(String aFieldName, CalculatedField aField) {
    calculatedFields.put(aFieldName, aField);
  }

  public void addMirDateField(String aDestinationFieldName, String aSourceFieldName, String aDefaultTimezone) {
    addCalculatedField(aDestinationFieldName, new MirDateField(aSourceFieldName, aDefaultTimezone));
  }

  public void addDBDateField(String aDestinationFieldName, String aSourceFieldName, String aDefaultTimezone) {
    addCalculatedField(aDestinationFieldName, new DBDateField(aSourceFieldName, aDefaultTimezone));
  }

  public interface CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter);
  }

  private class MirDateField implements CalculatedField {
    private String fieldName;
    private String defaultTimezone;

    public MirDateField(String aFieldName, String aDefaultTimezone) {
      fieldName = aFieldName;
      defaultTimezone = aDefaultTimezone;
    }

    public Object getValue(EntityAdapter anEntityAdapter) {

         Object result = null;
         String textValue = anEntityAdapter.getEntity().getValue(fieldName);
         Calendar calendar = GregorianCalendar.getInstance();
         int year;
         int month;
         int day;
         Date date;

         if (textValue!=null) {
           try {
             year = Integer.parseInt(textValue.substring(0,4));
             month = Integer.parseInt(textValue.substring(4,6));
             day = Integer.parseInt(textValue.substring(6,8));

             calendar.setTimeZone(TimeZone.getTimeZone(defaultTimezone));
             calendar.set(year, month-1, day);

             date = calendar.getTime();

             result = new GeneratorFormatAdapters.DateFormatAdapter(date, defaultTimezone);
           }
           catch (Throwable t) {
             result=null;
           }
         }
         return result;
       }
  }

  private class DBDateField implements CalculatedField {
    private String fieldName;
    private String defaultTimezone;

    public DBDateField(String aFieldName, String aDefaultTimezone) {
      fieldName = aFieldName;
      defaultTimezone = aDefaultTimezone;
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      Object result = null;
      String text = anEntityAdapter.getEntity().getValue(fieldName);

      if (text!=null) {
        try {
          result = new GeneratorFormatAdapters.DateFormatAdapter(StringUtil.convertMirInternalDateToDate(text), defaultTimezone);
        }
        catch (Throwable t) {
        }
      }

      return result;
    }
  }
}
