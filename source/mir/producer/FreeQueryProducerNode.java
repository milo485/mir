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

package mir.producer;

import java.util.Map;

import mir.log.LoggerWrapper;
import mir.storage.Database;
import mir.util.ParameterExpander;

public class FreeQueryProducerNode implements ProducerNode {
  public static final int QUERY_TYPE_SET = 1;
  public static final int QUERY_TYPE_ROW = 2;
  public static final int QUERY_TYPE_VALUE = 3;

  private Database database = new Database();
  private String key;
  private String query;
  private String limitExpression;
  private int type;

  public FreeQueryProducerNode(String aKey, String aQuery, String aLimit, int aType) {
    key = aKey;
    query = aQuery;
    limitExpression = aLimit;
    type = aType;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure {
    Object result = null;

    try {
      switch (type) {
        case QUERY_TYPE_VALUE:
          result = database.executeFreeSingleValueSql(ParameterExpander.expandExpression(aValueMap, query));
          break;

        case QUERY_TYPE_ROW:
          result = database.executeFreeSingleRowSql(ParameterExpander.expandExpression(aValueMap, query));
          break;

        case QUERY_TYPE_SET:
          int limit=10;
          if (limitExpression!=null)
            limit=ParameterExpander.evaluateIntegerExpression(aValueMap, limitExpression);

          result = database.executeFreeSql(
            ParameterExpander.expandExpression( aValueMap, query ),
            limit);
          break;
      }
    }
    catch (Throwable t) {
      aLogger.error("Error while executing free query: " + t.toString());
    }

    try {
      ParameterExpander.setValueForKey(
          aValueMap,
          ParameterExpander.expandExpression(aValueMap, key),
          result);
    }
    catch (Throwable t) {
      aLogger.error("Error while setting key " + key + ": " + t.toString());
    }
  };

}