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

package mircoders.localizer.basic;

import gnu.regexp.RE;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import mir.entity.Entity;
import mir.session.Request;
import mir.util.InternetFunctions;
import mircoders.localizer.MirAntiAbuseFilterType;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MirBasicAntiAbuseFilterTypes {
  private MirBasicAntiAbuseFilterTypes() {
  }

  public static abstract class BasicFilterType implements MirAntiAbuseFilterType {
    private String name;

    public BasicFilterType(String aName) {
      name = aName;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * A basic ip filter. Supports x.x.x.x, x.x.x.x/x and x.x.x.x/x.x.x.x expressions.
   *
   * <p>Title: </p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2003</p>
   * <p>Company: </p>
   * @author not attributable
   * @version 1.0
   */

  public static class IPFilter extends BasicFilterType {
    public IPFilter(String aName) {
      super(aName);
    }

    public boolean validate(String anExpression) {
      try {
        InternetFunctions.isIpAddressInNetwork("1.1.1.1", anExpression);
        return true;
      }
      catch (Throwable t) {
        return false;
      }
    };

    public boolean test(String anExpression, Entity anEntity, Request aRequest) {
      try {
        return InternetFunctions.isIpAddressInNetwork(aRequest.getHeader("ip"), anExpression);
      }
      catch (Throwable t) {
        return false;
      }
    };
  }

  /**
   * A regular expression filter.
   *
   * <p>Title: </p>
   * <p>Description: </p>
   * <p>Copyright: Copyright (c) 2003</p>
   * <p>Company: </p>
   * @author not attributable
   * @version 1.0
   */

  public static class RegularExpressionFilter extends BasicFilterType {
    private boolean exactMatch;
    private boolean caseSensitive;
    private List selectedFields;

    public RegularExpressionFilter(String aName) {
      this(aName, false, false, null);
    }

    public RegularExpressionFilter(String aName, boolean aCaseSensitive, boolean anExactMatch, String[] aSelectedFields) {
      super(aName);

      caseSensitive = aCaseSensitive;
      exactMatch = anExactMatch;
      if (aSelectedFields==null)
        selectedFields = null;
      else
        selectedFields = Arrays.asList(aSelectedFields);
    }

    public boolean validate(String anExpression) {
      try {
        new RE(anExpression);
        return true;
      }
      catch (Throwable t) {
        return false;
      }
    };

    public boolean test(String anExpression, Entity anEntity, Request aRequest) {
      try {
        Iterator j;
        int flags = 0;

        if (caseSensitive)
          flags |= RE.REG_ICASE;

        RE regularExpression = new RE(anExpression, RE.REG_ICASE);

        if (selectedFields!=null)
          j = selectedFields.iterator();
        else
          j = anEntity.getFields().iterator();

        while (j.hasNext()) {
          String field = anEntity.getValue( (String) j.next());

          if (exactMatch) {
            if (field != null && regularExpression.isMatch(field)) {
              return true;
            }
          }
          else {
            if (field != null && regularExpression.getMatch(field) != null) {
              return true;
            }
          }
        }
      }
      catch (Throwable t) {
      }
      return false;
    }
  }
}