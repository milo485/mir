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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.entity.adapter.EntityAdapterModel;
import mir.entity.adapter.EntityIteratorAdapter;
import mir.log.LoggerWrapper;
import mir.util.ParameterExpander;

public class EntityBatchingProducerNode implements ProducerNode {
  private String batchInfoKey;
  private String batchDataKey;
  private EntityAdapterModel model;
  private String definition;
  private String whereClause;
  private String orderByClause;
  private String nrEntitiesToSkipExpression;
  private String nrEntitiesPerBatchExpression;
  private String minNrEntitiesInFirstBatchExpression;
  private String nrBatchesToProcessExpression;
  private ProducerNode batchSubNode;
  private ProducerNode batchListSubNode;

  public EntityBatchingProducerNode(
        String aBatchDataKey,
        String aBatchInfoKey,
        EntityAdapterModel aModel,
        String aDefinition,
        String aWhereClause,
        String anOrderByClause,
        String anrEntitiesPerBatchExpression,
        String aminNrEntitiesInFirstBatchExpression,
        String anrEntitiesToSkipExpression,
        String aNrBatchesToProcessExpression,
        ProducerNode aBatchSubNode,
        ProducerNode aBatchListSubNode) {

    batchSubNode = aBatchSubNode;
    batchListSubNode = aBatchListSubNode;

    batchDataKey = aBatchDataKey;
    batchInfoKey = aBatchInfoKey;
    model = aModel;
    definition = aDefinition;
    whereClause = aWhereClause;
    orderByClause = anOrderByClause;
    nrEntitiesToSkipExpression = anrEntitiesToSkipExpression;
    nrEntitiesPerBatchExpression = anrEntitiesPerBatchExpression;
    minNrEntitiesInFirstBatchExpression = aminNrEntitiesInFirstBatchExpression;
    nrBatchesToProcessExpression = aNrBatchesToProcessExpression;
  }

  protected boolean isAborted(Map aValueMap) {
    Object producerValue = aValueMap.get(NodedProducer.PRODUCER_KEY);
    return (
       (producerValue instanceof NodedProducer) &&
      ((NodedProducer) producerValue).getIsAborted());
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure {
    Iterator browser;
    int nrEntities;
    int nrBatchesAfterFirst;
    int nrEntitiesInFirstBatch;
    int nrBatchesToProcess;
    List batchesData;
    int i;
    int position;
    Map batchData;
    String expandedWhereClause;
    String expandedOrderByClause;

    List batchLocations;
    BatchLocation location;

    int nrEntitiesToSkip;
    int nrEntitiesPerBatch;
    int minNrEntitiesInFirstBatch;

//  ML: The order by clause should lead to a result set in _reverse order_: the first row will be
//      the last entity presented on the last page


    try {
      nrBatchesToProcess = ParameterExpander.evaluateIntegerExpressionWithDefault( aValueMap, nrBatchesToProcessExpression, -1 );

      expandedWhereClause = ParameterExpander.expandExpression( aValueMap, whereClause );
      expandedOrderByClause = ParameterExpander.expandExpression( aValueMap, orderByClause );

      nrEntitiesToSkip = ParameterExpander.evaluateIntegerExpression( aValueMap, nrEntitiesToSkipExpression);
      nrEntitiesPerBatch = ParameterExpander.evaluateIntegerExpression( aValueMap, nrEntitiesPerBatchExpression);
      minNrEntitiesInFirstBatch = ParameterExpander.evaluateIntegerExpression( aValueMap, minNrEntitiesInFirstBatchExpression);

      batchesData = new Vector();
      batchLocations = new Vector();

      nrEntities = model.getMappingForName(definition).getStorage().getSize(expandedWhereClause)-nrEntitiesToSkip;
      nrEntitiesInFirstBatch = nrEntities % nrEntitiesPerBatch;
      while (nrEntitiesInFirstBatch<minNrEntitiesInFirstBatch && nrEntities-nrEntitiesInFirstBatch>=nrEntitiesPerBatch)
        nrEntitiesInFirstBatch = nrEntitiesInFirstBatch + nrEntitiesPerBatch;
      nrBatchesAfterFirst = (nrEntities-nrEntitiesInFirstBatch)/nrEntitiesPerBatch;

      batchLocations.add(new BatchLocation(nrBatchesAfterFirst*nrEntitiesPerBatch, nrEntitiesInFirstBatch));
      batchData = new HashMap();
      batchData.put("identifier", "");
      batchData.put("index", new Integer(nrBatchesAfterFirst+1));
      batchData.put("size", new Integer(nrEntitiesInFirstBatch));
      batchesData.add(batchData);

      for (i=0; i<nrBatchesAfterFirst; i++) {
        batchLocations.add(1, new BatchLocation(i*nrEntitiesPerBatch, nrEntitiesPerBatch));
        batchData = new HashMap();
        batchData.put("identifier", new Integer(i));
        batchData.put("index", new Integer(i+1));
        batchData.put("size", new Integer(nrEntitiesPerBatch));
        batchesData.add(1, batchData);
      }

      batchData = new HashMap();
      ParameterExpander.setValueForKey(aValueMap, batchInfoKey, batchData);
      batchData.put("all", batchesData);
      batchData.put("first", batchesData.get(0));
      batchData.put("last", batchesData.get(batchesData.size()-1));
      batchData.put("count", new Integer(batchesData.size()));

      if (batchListSubNode!=null && (!isAborted(aValueMap))) {
        batchListSubNode.produce(aValueMap, aVerb, aLogger);
      }

      if (nrBatchesToProcess<0 || nrBatchesToProcess>nrBatchesAfterFirst+1) {
        nrBatchesToProcess = nrBatchesAfterFirst+1;
      }

      if (batchSubNode!=null) {
        for (i=0; i<nrBatchesToProcess && !isAborted(aValueMap); i++) {
          location = (BatchLocation) batchLocations.get(i);

          batchData.put("current", batchesData.get(i));
          if (i>0)
            batchData.put("previous", batchesData.get(i-1));
          else
            batchData.put("previous", null);

          if (i<batchesData.size()-1)
            batchData.put("next", batchesData.get(i+1));
          else
            batchData.put("next", null);

          Iterator j = new EntityIteratorAdapter(expandedWhereClause, expandedOrderByClause,
                    location.nrEntities, model, definition, location.nrEntities, location.firstEntity);
          List entities = new Vector();

          while (j.hasNext())
            entities.add(0, j.next());

          ParameterExpander.setValueForKey(aValueMap, batchDataKey, entities );

          batchSubNode.produce(aValueMap, aVerb, aLogger);
        }
      }
    }
    catch (Throwable t) {
      aLogger.error("EntityBatchingProducerNode caused an exception: " + t.getMessage());
    }
  };

  private class BatchLocation {
    int nrEntities;
    int firstEntity;

    public BatchLocation(int aFirstEntity, int aNrEntities) {
      firstEntity = aFirstEntity;
      nrEntities = aNrEntities;
    }
  }
}

