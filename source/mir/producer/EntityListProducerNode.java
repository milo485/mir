/*
 * Copyright (C) 2001, 2002  The Mir-coders group
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
 * the code of this program with the com.oreilly.servlet library, any library
 * licensed under the Apache Software License, The Sun (tm) Java Advanced
 * Imaging library (JAI), The Sun JIMI library (or with modified versions of
 * the above that use the same license as the above), and distribute linked
 * combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than the above
 * mentioned libraries.  If you modify this file, you may extend this exception
 * to your version of the file, but you are not obligated to do so.  If you do
 * not wish to do so, delete this exception statement from your version.
 */

package mir.producer;

import java.util.Map;
import java.util.Vector;

import mir.entity.adapter.EntityAdapterModel;
import mir.entity.adapter.EntityIteratorAdapter;
import mir.log.LoggerWrapper;
import mir.util.CachingRewindableIterator;
import mir.util.ParameterExpander;

public class EntityListProducerNode extends ProducerNodeDecorator {
  private String key;
  private String whereClause;
  private String orderByClause;
  private int batchSize;
  private EntityAdapterModel model;
  private String definition;
  private String limitExpression;
  private String skipExpression;

  public EntityListProducerNode(String aKey,
      EntityAdapterModel aModel, String aDefinition,
      String aWhereClause, String anOrderByClause,
      String aLimitExpression, String aSkipExpression, ProducerNode aSubNode) {
    super(aSubNode);

    model = aModel;
    definition = aDefinition;
    key = aKey;
    whereClause = aWhereClause;
    orderByClause = anOrderByClause;
    limitExpression = aLimitExpression;
    skipExpression = aSkipExpression;
  }

  public EntityListProducerNode(String aKey,
      EntityAdapterModel aModel, String aDefinition,
      String aWhereClause, String anOrderByClause,
      int aLimit, int aSkip, ProducerNode aSubNode) {
    this(aKey,  aModel, aDefinition, aWhereClause, anOrderByClause,
         Integer.toString(aLimit), Integer.toString(aSkip), aSubNode);
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure, ProducerExc {
    try {
      int limit = ParameterExpander.evaluateIntegerExpressionWithDefault(aValueMap, limitExpression, -1);
      int skip = ParameterExpander.evaluateIntegerExpressionWithDefault(aValueMap, skipExpression, 0);

      if (skipExpression != null && !skipExpression.trim().equals(""))
        skip = ParameterExpander.evaluateIntegerExpression(aValueMap, skipExpression);

      ParameterExpander.setValueForKey(
        aValueMap,
        key,
        new CachingRewindableIterator(
          new EntityIteratorAdapter(
            ParameterExpander.expandExpression( aValueMap, whereClause ),
            ParameterExpander.expandExpression( aValueMap, orderByClause ),
            Math.min(50, limit),
            model,
            definition,
            limit,
            skip )
        )
      );
    }
    catch (Throwable t) {
      aLogger.error("cannot retrieve list into key " + key + ": " + t.getMessage());
      try {
        ParameterExpander.setValueForKey(
          aValueMap,
          key,
          new CachingRewindableIterator(new Vector().iterator())
        );
      }
      catch (Throwable s) {
      }
    }

    super.produce(aValueMap, aVerb, aLogger);
  };

}