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

import java.util.HashMap;
import java.util.Iterator;
import java.util.*;

import mir.entity.Entity;
import mir.entity.adapter.EntityAdapter;
import mir.entity.adapter.EntityAdapterModel;
import mir.log.LoggerWrapper;
import mir.util.ParameterExpander;
import mir.util.*;


public class EntityModifyingProducerNode implements ProducerNode {
  private String entityExpression;
  private String definition;
  private Map fields;
  private boolean create;
  private EntityAdapterModel model;

  public EntityModifyingProducerNode(EntityAdapterModel aModel, boolean aCreate, String aDefinition, String anEntityExpression, Map aFieldValues) {
    entityExpression = anEntityExpression;
    definition = aDefinition;
    create = aCreate;
    model = aModel;
    fields = new HashMap();
    fields.putAll(aFieldValues);
  }

  public void addField(String aField, String aValueExpression) {
    fields.put(aField, aValueExpression);
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerExc, ProducerFailure {
    try {
      Object entityAdapter;

      if (create) {
        entityAdapter = model.createNewEntity(definition);
        ParameterExpander.setValueForKey(aValueMap, entityExpression, entityAdapter);
      }
      else {
        entityAdapter = ParameterExpander.findValueForKey(aValueMap, entityExpression);
      }

      if (entityAdapter instanceof EntityAdapter) {
        Entity entity = ((EntityAdapter) entityAdapter).getEntity();
        Iterator i = fields.entrySet().iterator();
        while (i.hasNext()) {
          Map.Entry entry = (Map.Entry) i.next();
          String entityField = (String) entry.getKey();
          String valueExpression = (String) entry.getValue();

          Object value = ParameterExpander.evaluateExpression(aValueMap, valueExpression);

          if (value instanceof String)
            entity.setValueForProperty(entityField, (String) value);
          else if (value instanceof EntityAdapter)
            entity.setValueForProperty(entityField, ((EntityAdapter) value).getEntity().getId());
          else if (value instanceof Date) {
            entity.setValueForProperty(entityField, JDBCStringRoutines.formatDate((Date) value));
          }
          else
            aLogger.warn("Can't set value " + value + " for field " + entityField);
        }

        if (create)
          entity.insert();
        else
          entity.update();
      }
      else
        throw new ProducerExc( entityExpression + " does not evaluate to an entity");
    }
    catch (Throwable t) {
      aLogger.error("Error while performing entity modification operation: " + t.getMessage());
      t.printStackTrace(aLogger.asPrintWriter(aLogger.DEBUG_MESSAGE));

      throw new ProducerFailure(t.getMessage(), t);
    }
  }
}