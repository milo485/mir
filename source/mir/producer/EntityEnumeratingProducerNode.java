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

import java.util.Iterator;
import java.util.Map;

import mir.entity.adapter.EntityAdapterModel;
import mir.entity.adapter.EntityIteratorAdapter;
import mir.log.LoggerWrapper;
import mir.util.ParameterExpander;

public class EntityEnumeratingProducerNode extends ProducerNodeDecorator {
  private String key;
  private EntityAdapterModel model;
  private String definition;
  private String skip;
  private String limit;
  private String whereClause;
  private String orderByClause;


  public EntityEnumeratingProducerNode(
              String aKey,
              EntityAdapterModel aModel, String aDefinition,
              String aWhereClause, String anOrderByClause,
              String aLimit, String aSkip,
              ProducerNode aSubNode) {
    super(aSubNode);

    model = aModel;
    definition = aDefinition;
    key = aKey;

    whereClause = aWhereClause;
    orderByClause = anOrderByClause;

    limit= aLimit;
    skip = aSkip;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure {
    Iterator browser;

    try {
      browser = new EntityIteratorAdapter(
          ParameterExpander.expandExpression( aValueMap, whereClause ),
          ParameterExpander.expandExpression( aValueMap, orderByClause ),
          100,
          model,
          definition,
          ParameterExpander.evaluateIntegerExpressionWithDefault( aValueMap, limit, -1),
          ParameterExpander.evaluateIntegerExpressionWithDefault( aValueMap, skip, 0));

      while (browser.hasNext() && !isAborted(aValueMap)) {
        ParameterExpander.setValueForKey( aValueMap, key, browser.next());
        super.produce(aValueMap, aVerb, aLogger);
      }
    }
    catch (Throwable t) {
      aLogger.error("Exception occurred inside an EntityEnumeratingProducerNode: " + t.getMessage());
    }
  };
}