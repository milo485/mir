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

package mir.producer.reader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mir.log.LoggerWrapper;
import mir.producer.ProducerExc;
import mir.producer.ProducerFailure;
import mir.producer.ProducerNode;
import mir.util.ParameterExpander;

public class ScriptedProducerNode implements ProducerNode {
  private ScriptedProducerNodeDefinition definition;
  private Map integerParameterValues;
  private Map stringParameterValues;
  private Map nodeParameterValues;

  public ScriptedProducerNode(ScriptedProducerNodeDefinition aDefinition, Map aStringParameterValues, Map anIntegerParameterValues, Map aNodeParameterValues) {
    definition = aDefinition;
    stringParameterValues = new HashMap();
    stringParameterValues.putAll(aStringParameterValues);
    integerParameterValues = new HashMap();
    integerParameterValues.putAll(anIntegerParameterValues);
    nodeParameterValues = new HashMap();
    nodeParameterValues.putAll(aNodeParameterValues);
  }

  public void produce(Map aValues, String aVerb, LoggerWrapper aLogger) throws ProducerFailure, ProducerExc {
    try {
      Map oldValues = new HashMap();
      ScriptedProducerNodeTool.saveMapValues(oldValues, aValues, definition.getStringParameters().keySet());
      ScriptedProducerNodeTool.saveMapValues(oldValues, aValues, definition.getIntegerParameters().keySet());
      try {
        Iterator i = stringParameterValues.entrySet().iterator();

        while (i.hasNext()) {
          Map.Entry entry = (Map.Entry) i.next();

          if (entry.getValue() instanceof String) {
            aValues.put(entry.getKey(), ParameterExpander.expandExpression(aValues, (String) entry.getValue()));
          }
        }

        i = integerParameterValues.entrySet().iterator();

        while (i.hasNext()) {
          Map.Entry entry = (Map.Entry) i.next();

          if (entry.getValue() instanceof String) {
            aValues.put(entry.getKey(), ParameterExpander.evaluateExpression(aValues, (String) entry.getValue()));
          }
        }

        ScriptedProducerNodeTool.pushNodeParameterValues(aValues, definition.getName(), nodeParameterValues);
        try {
          definition.getBody().produce(aValues, aVerb, aLogger);
        }
        finally {
          ScriptedProducerNodeTool.popNodeParameterValues(aValues, definition.getName());
        }
      }
      finally {
        ScriptedProducerNodeTool.restoreMapValues(aValues, definition.getIntegerParameters().keySet(), oldValues);
        ScriptedProducerNodeTool.restoreMapValues(aValues, definition.getStringParameters().keySet(), oldValues);
      }
    }
    catch (Exception e) {
      aLogger.error("Scripted producer node " + definition.getName() + " caused an exception: " + e.getMessage());
    }
  }

}