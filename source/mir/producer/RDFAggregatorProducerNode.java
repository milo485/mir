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

import java.util.*;

import mir.log.LoggerWrapper;
import mir.rss.RSSData;
import mir.rss.*;
import mir.util.ExceptionFunctions;
import mir.util.ParameterExpander;

public class RDFAggregatorProducerNode implements ProducerNode {
  private String key;
  private String source;
  private String order;
  private String filter;

  public RDFAggregatorProducerNode(String aKey, String aSource, String anOrder, String aFilter) {
    key = aKey;
    source=aSource;
    order=anOrder;
    filter=aFilter;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure {
    try {
      aLogger.debug(source);
      String evaluatedKey = ParameterExpander.expandExpression( aValueMap, key );
      String evaluatedOrder = ParameterExpander.expandExpression( aValueMap, order );
      Object evaluatedSource = ParameterExpander.evaluateExpression( aValueMap, source );

      Object aggregator = aValueMap.get(evaluatedKey);

      if (aggregator == null) {
        aLogger.debug("creating");
        aggregator = new RSSAggregator(100, "dc:date", true, null, null);
        aValueMap.put(evaluatedKey, aggregator);
      }

      if (aggregator instanceof RSSAggregator) {
        RSSAggregator agg = (RSSAggregator) aggregator;

        if (evaluatedSource instanceof List) {
          aLogger.debug("appending");
          agg.appendItems( (List) evaluatedSource);
          aLogger.debug("now: " + agg.getItems().size());
        }
        else
          throw new ProducerExc("List expected, " + evaluatedSource.toString() + " found");
      }
      else
        throw new ProducerExc("RSSAggregator expected, " + aggregator.toString() + " found");
    }
    catch (Throwable t) {
      Throwable s = ExceptionFunctions.traceCauseException(t);
      aLogger.error("Error while aggregating RDF data: " + s.getClass().getName()+","+ s.getMessage());
    }
  };
}
