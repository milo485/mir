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

import java.util.*;
import java.io.*;
import mir.producer.*;

public class ScriptedProducerNodeDefinition {
  private Map integerParameters;               // name -> default value
  private Map stringParameters;               // name -> default value
  private Set nodeParameters;
  private ProducerNode body;
  private String name;

  public static String SCRIPTED_PRODUCERNODE_RUNTIMEDATA_KEY = "$SCRIPTRUNTIMEDATA";
  public static String SCRIPTED_PRODUCERNODE_RUNTIMESTACK_KEY = "stack";

  public ScriptedProducerNodeDefinition(String aName) {
    name = aName;
    integerParameters = new HashMap();
    stringParameters = new HashMap();
    nodeParameters = new HashSet();
    body = new CompositeProducerNode();
  }

  public void addStringParameter(String aName, String aDefaultValue) {
    stringParameters.put(aName, aDefaultValue);
  }

  public void addIntegerParameter(String aName, String aDefaultValue) {
    integerParameters.put(aName, aDefaultValue);
  }

  public void addNodeParameter(String aName) {
    nodeParameters.add(aName);
  }

  public void setBody(ProducerNode aBody) {
    body = aBody;
  }

  protected Map getStringParameters() {
    return stringParameters;
  }

  protected Map getIntegerParameters() {
    return integerParameters;
  }

  protected Set getNodeParameters() {
    return nodeParameters;
  }

  protected ProducerNode getBody() {
    return body;
  }

  public String getName() {
    return name;
  }

  public Set getRequiredAttributes() {
    return getAttributesSelection(true);
  }

  public Set getOptionalAttributes() {
    return getAttributesSelection(false);
  }

  public Set getAttributesSelection(boolean aRequired) {
    Set result = new HashSet();
    Iterator i = stringParameters.entrySet().iterator();

    while (i.hasNext()) {
      Map.Entry entry = (Map.Entry) i.next();

      if ((entry.getValue() == null) == aRequired ) {
        result.add(entry.getKey());
      }
    }

    i = integerParameters.entrySet().iterator();

    while (i.hasNext()) {
      Map.Entry entry = (Map.Entry) i.next();

      if ((entry.getValue() == null) == aRequired ) {
        result.add(entry.getKey());
      }
    }

    return result;
  }


  protected static class NodeParameterProducerNode implements ProducerNode {
    private String parameterName;
    private String definitionName;

    public NodeParameterProducerNode(String aDefinitionName, String aParameterName) {
      definitionName = aDefinitionName;
      parameterName = aParameterName;
    }

    public void produce(Map aValues, String aVerb, PrintWriter aLogger) throws ProducerExc, ProducerFailure {
      ProducerNode producerNode;

      Map runTimeData = (Map) ((Map) aValues.get(SCRIPTED_PRODUCERNODE_RUNTIMEDATA_KEY)).get(definitionName);
      Map parameters = (Map) ((Stack) runTimeData.get( SCRIPTED_PRODUCERNODE_RUNTIMESTACK_KEY )).peek();

      producerNode = (ProducerNode) parameters.get(parameterName);

      if (producerNode != null)
        producerNode.produce(aValues, aVerb, aLogger);
    }

    public Set buildVerbSet() {
      return new HashSet();
    }
  }
}