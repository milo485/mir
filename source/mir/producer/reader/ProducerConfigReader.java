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

package  mir.producer.reader;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import mir.producer.CompositeProducerNode;
import mir.producer.ProducerFactory;
import mir.producer.ProducerNode;
import mir.producer.SimpleProducerVerb;
import mir.util.XMLReader;
import mir.util.XMLReaderTool;

public class ProducerConfigReader {
  private ProducerNodeBuilderLibrary builderLibrary;
  private ProducerNodeBuilderLibrary scriptedNodeBuilderLibrary;

  public ProducerConfigReader() {
    super();
  };

  public void parseFile(String aFileName, ProducerNodeBuilderLibrary aBuilderLibrary, List aProducerFactories) throws ProducerConfigFailure {
    parseFile(aFileName, aBuilderLibrary, aProducerFactories, new Vector());
  }

  public void parseFile(String aFileName, ProducerNodeBuilderLibrary aBuilderLibrary, List aProducerFactories, List aUsedFiles) throws ProducerConfigFailure {
    try {
      XMLReader reader = new XMLReader();
      aUsedFiles.add(new File(aFileName));

      builderLibrary = aBuilderLibrary;
      scriptedNodeBuilderLibrary = new ProducerNodeBuilderLibrary();

      reader.parseFile(aFileName, new RootSectionHandler(aProducerFactories));

    }
    catch (Throwable e) {
      if ((e instanceof XMLReader.XMLReaderExc) && ((XMLReader.XMLReaderExc) e).getHasLocation()) {
        XMLReader.XMLReaderExc f = (XMLReader.XMLReaderExc) e;
        throw new ProducerConfigFailure("'" + f.getMessage()+"' in " + f.getFilename()+"(line " + f.getLineNr()+", column " + f.getColumnNr() + ")", e);
      }
      throw new ProducerConfigFailure( e );
    }
  }


  public class RootSectionHandler extends XMLReader.AbstractSectionHandler {
    private List producers;

    public RootSectionHandler(List aProducers) {
      producers = aProducers;
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.equals("producers")) {
        return new ProducersSectionHandler(producers);
      }
      else
        throw new XMLReader.XMLReaderExc("Tag 'producers' expected, tag '"+aTag+"' found");
    }

    public void endElement(XMLReader.SectionHandler aHandler) {
    }

    public void finishSection() {
    }
  }


  private final static String   PRODUCER_NAME_ATTRIBUTE = "name";
  private final static String[] PRODUCER_REQUIRED_ATTRIBUTES = { PRODUCER_NAME_ATTRIBUTE };
  private final static String[] PRODUCER_OPTIONAL_ATTRIBUTES = { };

  private final static String   NODE_DEFINITION_NAME_ATTRIBUTE = "name";
  private final static String[] NODE_DEFINITION_REQUIRED_ATTRIBUTES = { NODE_DEFINITION_NAME_ATTRIBUTE };
  private final static String[] NODE_DEFINITION_OPTIONAL_ATTRIBUTES = {  };

  public class ProducersSectionHandler extends XMLReader.AbstractSectionHandler {
    private List producers;
    private Set producerNames;
    private String name;

    public ProducersSectionHandler(List aProducers) {
      producers = aProducers;
      producerNames = new HashSet();
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.equals("producer")) {
        XMLReaderTool.checkAttributes(anAttributes,
                                      PRODUCER_REQUIRED_ATTRIBUTES,
                                      PRODUCER_OPTIONAL_ATTRIBUTES);

        name = (String) anAttributes.get(PRODUCER_NAME_ATTRIBUTE);
        XMLReaderTool.checkValidIdentifier(name);

        if (producerNames.contains(name))
          throw new XMLReader.XMLReaderExc("Duplicate producer name: '" +
                                           name + "'");

        name = (String) anAttributes.get(PRODUCER_NAME_ATTRIBUTE);

        return new ProducerSectionHandler(name);
      }
      else if (aTag.equals("nodedefinition")) {
        XMLReaderTool.checkAttributes(anAttributes,
                                      NODE_DEFINITION_REQUIRED_ATTRIBUTES,
                                      NODE_DEFINITION_OPTIONAL_ATTRIBUTES);

        name = (String) anAttributes.get(NODE_DEFINITION_NAME_ATTRIBUTE);
        XMLReaderTool.checkValidIdentifier(name);

        name = (String) anAttributes.get(NODE_DEFINITION_NAME_ATTRIBUTE);

        return new NodeDefinitionSectionHandler(name);
      }
      throw new XMLReader.XMLReaderExc("Unexpected tag: " + aTag);
    }

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (aHandler instanceof ProducerSectionHandler) {
        producers.add(((ProducerSectionHandler) aHandler).getProducerFactory());
        producerNames.add(((ProducerSectionHandler) aHandler).getProducerFactory().getName());
      }
      else if (aHandler instanceof NodeDefinitionSectionHandler) {
        scriptedNodeBuilderLibrary.registerFactory(name,
            new DefaultProducerNodeBuilders.ScriptedProducerNodeBuilder.factory(
                ((NodeDefinitionSectionHandler) aHandler).getDefinition()));
      }
      else throw new XMLReader.XMLReaderExc("ProducersSectionHandler.endElement Internal error: Unexpected handler: " + aHandler.getClass().getName());
    }

    public void finishSection() {
    }
  }

  public class ProducerSectionHandler extends XMLReader.AbstractSectionHandler {
    private ProducerFactory producerFactory;
    private String factoryName;

    private ProducerNode body;
    private Map verbNodes;
    private List verbs;
    private String defaultVerb;

    public ProducerSectionHandler(String aName) {
      factoryName = aName;
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.equals("verbs")) {
        if (verbs!=null)
          throw new XMLReader.XMLReaderExc("Verbs already processed");
        if (body!=null)
          throw new XMLReader.XMLReaderExc("Verbs should come before body");
        else
          return new ProducerVerbsSectionHandler();
      }
      else if (aTag.equals("body")) {
        if (body==null)
          return new ProducerNodeSectionHandler();
        else
          throw new XMLReader.XMLReaderExc("Body already processed");
      }
      throw new XMLReader.XMLReaderExc("Unexpected tag: '"+aTag+"'");
    }

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (aHandler instanceof ProducerNodeSectionHandler) {
        body = ((ProducerNodeSectionHandler) aHandler).getProducerNode();
      }
      else if (aHandler instanceof ProducerVerbsSectionHandler)
      {
        verbs = ((ProducerVerbsSectionHandler) aHandler).getVerbs();
        verbNodes = ((ProducerVerbsSectionHandler) aHandler).getVerbNodes();
        defaultVerb = ((ProducerVerbsSectionHandler) aHandler).getDefaultVerb();
      }
      else throw new XMLReader.XMLReaderExc("ProducerSectionHandler.endElement Internal error: Unexpected handler: " + aHandler.getClass().getName());
    }

    public void finishSection() throws XMLReader.XMLReaderExc {
      if (verbs==null)
        throw new XMLReader.XMLReaderExc("No verbs defined");

      if (body==null)
        throw new XMLReader.XMLReaderExc("No body defined");

      producerFactory = new ScriptedProducerFactory(factoryName, verbs, verbNodes, body, defaultVerb);
    }

    public ProducerFactory getProducerFactory() {
      return producerFactory;
    }
  }

  private final static String   PRODUCER_VERB_NAME_ATTRIBUTE = "name";
  private final static String   PRODUCER_VERB_DESCRIPTION_ATTRIBUTE = "description";
  private final static String   PRODUCER_VERB_DEFAULT_ATTRIBUTE = "default";
  private final static String[] PRODUCER_VERB_REQUIRED_ATTRIBUTES = { PRODUCER_VERB_NAME_ATTRIBUTE };
  private final static String[] PRODUCER_VERB_OPTIONAL_ATTRIBUTES = { PRODUCER_VERB_DEFAULT_ATTRIBUTE, PRODUCER_VERB_DESCRIPTION_ATTRIBUTE };

  public class ProducerVerbsSectionHandler extends XMLReader.AbstractSectionHandler {
    private Map verbNodes;
    private List verbs;
    private String defaultVerb;
    private String currentVerb;
    private String currentVerbDescription;

    public ProducerVerbsSectionHandler() {
      verbNodes = new HashMap();
      verbs = new Vector();
      defaultVerb = null;
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.equals("verb")) {
        XMLReaderTool.checkAttributes(anAttributes,
                                      PRODUCER_VERB_REQUIRED_ATTRIBUTES,
                                      PRODUCER_VERB_OPTIONAL_ATTRIBUTES);
        currentVerb = (String) anAttributes.get(PRODUCER_VERB_NAME_ATTRIBUTE);

        XMLReaderTool.checkValidIdentifier(currentVerb);

        if (verbNodes.containsKey(currentVerb))
          throw new XMLReader.XMLReaderExc("Duplicate definition of verb '" +
                                           currentVerb + "'");

        if (anAttributes.containsKey(PRODUCER_VERB_DEFAULT_ATTRIBUTE)) {
          if (defaultVerb != null)
            throw new XMLReader.XMLReaderExc("Default verb already declared");

          defaultVerb = currentVerb;
        }

        if (anAttributes.containsKey(PRODUCER_VERB_DESCRIPTION_ATTRIBUTE))
          currentVerbDescription = (String) anAttributes.get(
              PRODUCER_VERB_DESCRIPTION_ATTRIBUTE);
        else
          currentVerbDescription = "";

        return new ProducerNodeSectionHandler();
      }
      else
        throw new XMLReader.XMLReaderExc("Only 'verb' tags allowed here, '" +
                                         aTag + "' encountered.");
    }

    public void endElement(XMLReader.SectionHandler aHandler) {
      verbNodes.put(currentVerb, ((ProducerNodeSectionHandler) aHandler).getProducerNode());
      verbs.add(new SimpleProducerVerb(currentVerb, currentVerbDescription));
    }

    public void finishSection() {
    }

    public String getDefaultVerb() {
      return defaultVerb;
    }

    public List getVerbs() {
      return verbs;
    }

    public Map getVerbNodes() {
      return verbNodes;
    }
  }

  public class EmptySectionHandler extends XMLReader.AbstractSectionHandler {
    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      throw new XMLReader.XMLReaderExc("No tags are allowed here");
    }

    public void endElement(XMLReader.SectionHandler aHandler) {
    }

    public void finishSection() {
    }
  }

  public class MultiProducerNodeSectionHandler extends XMLReader.AbstractSectionHandler {
    private Map nodeParameters;
    private Set validNodeParameters;
    private String currentNodeParameter;
    private String scriptedNodeName;
    private Set allowedNodeParameterReferences;

    public MultiProducerNodeSectionHandler(String aScriptedNodeName, Set anAllowedNodeParameterReferences, Set aValidNodeParameters) {
      allowedNodeParameterReferences = anAllowedNodeParameterReferences;
      scriptedNodeName = aScriptedNodeName;
      validNodeParameters = aValidNodeParameters;
      nodeParameters = new HashMap();
    }
    public MultiProducerNodeSectionHandler(Set aValidNodeParameters) {
      this("", new HashSet(), aValidNodeParameters);
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (!validNodeParameters.contains(aTag))
        throw new XMLReader.XMLReaderExc("Invalid node parameter: '" + aTag + "'");
      else if (nodeParameters.containsKey(aTag))
        throw new XMLReader.XMLReaderExc("Node parameter: '" + aTag + "' already specified");
      else if (anAttributes.size()>0)
        throw new XMLReader.XMLReaderExc("No parameters are allowed here");

      currentNodeParameter = aTag;

      return new ProducerNodeSectionHandler(scriptedNodeName, validNodeParameters);
    }

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc  {
      if (aHandler instanceof ProducerNodeSectionHandler) {
        nodeParameters.put(currentNodeParameter, ((ProducerNodeSectionHandler) aHandler).getProducerNode());
      }
      else {
        throw new XMLReader.XMLReaderExc("Internal error: unknown section handler '" + aHandler.getClass().getName() + "'" );
      }
    }

    public Map getNodeParameters() {
      return nodeParameters;
    }

    public void finishSection() {
    }
  }

  public class ProducerNodeSectionHandler extends XMLReader.AbstractSectionHandler {
    private CompositeProducerNode producerNode;
    private ProducerNodeBuilder currentBuilder;
    private String scriptedNodeName;
    private Set allowedNodeParameterReferences;

    public ProducerNodeSectionHandler(String aScriptedNodeName, Set anAllowedNodeParameterReferences) {
      producerNode = new CompositeProducerNode();
      scriptedNodeName = aScriptedNodeName;
      allowedNodeParameterReferences = anAllowedNodeParameterReferences;
    }

    public ProducerNodeSectionHandler() {
      this("", new HashSet());
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      try {
        if (allowedNodeParameterReferences.contains( (aTag))) {
          if (!anAttributes.isEmpty()) {
            throw new XMLReader.XMLReaderExc("No attributes allowed");
          }

          currentBuilder = new DefaultProducerNodeBuilders.
              ScriptedProducerParameterNodeBuilder(scriptedNodeName, aTag);
          return new EmptySectionHandler();
        }
        else if (scriptedNodeBuilderLibrary.hasBuilderForName(aTag) ||
                 builderLibrary.hasBuilderForName( (aTag))) {

          if (scriptedNodeBuilderLibrary.hasBuilderForName(aTag))
            currentBuilder = scriptedNodeBuilderLibrary.constructBuilder(aTag);
          else
            currentBuilder = builderLibrary.constructBuilder(aTag);

          currentBuilder.setAttributes(anAttributes);
          if (currentBuilder.getAvailableSubNodes().isEmpty()) {
            return new EmptySectionHandler();
          }
          if (currentBuilder.getAvailableSubNodes().size() > 1)
            return new MultiProducerNodeSectionHandler(scriptedNodeName,
                allowedNodeParameterReferences,
                currentBuilder.getAvailableSubNodes());
          else if (currentBuilder.getAvailableSubNodes().size() < 1)
            return new EmptySectionHandler();
          else {
            return new ProducerNodeSectionHandler(scriptedNodeName,
                allowedNodeParameterReferences);
          }
        }
        else
          throw new XMLReader.XMLReaderExc("Unknown producer node tag: '" +
                                           aTag + "'");
      }
      catch (Throwable t) {
        throw new XMLReader.XMLReaderFailure(t);
      }
    }

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc  {
      try {
        if (aHandler instanceof ProducerNodeSectionHandler) {
          currentBuilder.setSubNode(
                (String) (currentBuilder.getAvailableSubNodes().iterator().next()),
                ((ProducerNodeSectionHandler) aHandler).getProducerNode());
        }
        else if (aHandler instanceof MultiProducerNodeSectionHandler) {
          Iterator i;
          Map nodeParameters;
          Map.Entry entry;

          nodeParameters = ( (MultiProducerNodeSectionHandler) aHandler).
              getNodeParameters();
          i = nodeParameters.entrySet().iterator();
          while (i.hasNext()) {
            entry = (Map.Entry) i.next();
            currentBuilder.setSubNode( (String) entry.getKey(),
                                      (ProducerNode) entry.getValue());
          }
        }
        else if (aHandler instanceof EmptySectionHandler) {
          // deliberately empty: nothing expected, so nothing to process
        }
        else {
          throw new XMLReader.XMLReaderExc(
              "Internal error: unknown section handler '" +
              aHandler.getClass().getName() + "'");
        }

        producerNode.addSubNode(currentBuilder.constructNode());
        currentBuilder = null;
      }
      catch (Throwable t) {
        throw new XMLReader.XMLReaderFailure(t);
      }
    }

    public ProducerNode getProducerNode() {
      if (producerNode.getNrSubNodes()==1) {
        return producerNode.getSubNode(0);
      }
      else {
        return producerNode;
      }
    }

    public void finishSection() {
    }
  }

  public class NodeDefinitionSectionHandler extends XMLReader.AbstractSectionHandler {
    private ScriptedProducerNodeDefinition nodeDefinition;
    private ProducerNode body;
    private Map stringParameters;
    private Map integerParameters;
    private Map nodeParameters;
    private String name;

    public NodeDefinitionSectionHandler(String aName) {
      body = null;
      nodeParameters = null;
      stringParameters = null;
      integerParameters = null;
      name = aName;
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.equals("parameters")) {
        if (!anAttributes.isEmpty()) {
          throw new XMLReader.XMLReaderExc( "No attributes allowed for tag 'parameters'" );
        }
        if (nodeParameters!=null) {
          throw new XMLReader.XMLReaderExc( "Parameters have already been declared" );
        }
        if (body!=null) {
          throw new XMLReader.XMLReaderExc( "Parameters should come before definition in nodedefinition '" + name +"'" );
        }

        return new NodeDefinitionParametersSectionHandler();
      }
      else if (aTag.equals("definition")) {
        if (nodeParameters==null)
          throw new XMLReader.XMLReaderExc( "Parameters should come before definition in nodedefinition '" + name +"'"  );

        return new ProducerNodeSectionHandler(name, nodeParameters.keySet());
      }
      else throw new XMLReader.XMLReaderExc("Only 'definition' or 'parameters' tags allowed here, '" + aTag + "' encountered.");
    }

    public void endElement(XMLReader.SectionHandler aHandler) {
      if (aHandler instanceof NodeDefinitionParametersSectionHandler) {
        stringParameters = ((NodeDefinitionParametersSectionHandler) aHandler).getStringParameters();
        integerParameters = ((NodeDefinitionParametersSectionHandler) aHandler).getIntegerParameters();
        nodeParameters = ((NodeDefinitionParametersSectionHandler) aHandler).getNodeParameters();
      }
      else if (aHandler instanceof ProducerNodeSectionHandler) {
        body = ((ProducerNodeSectionHandler) aHandler).getProducerNode();
      }
    }

    public void finishSection() throws XMLReader.XMLReaderExc {
      Iterator i;
      if (body == null)
        throw new XMLReader.XMLReaderExc( "Definition missing" );

      nodeDefinition = new ScriptedProducerNodeDefinition(name);

      nodeDefinition.setBody(body);

      i = nodeParameters.keySet().iterator();
      while (i.hasNext()) {
        nodeDefinition.addNodeParameter((String) i.next());
      }

      i = stringParameters.entrySet().iterator();
      while (i.hasNext()) {
        Map.Entry entry = (Map.Entry) i.next();
        nodeDefinition.addStringParameter((String) entry.getKey(), (String) entry.getValue());
      }

      i = integerParameters.entrySet().iterator();
      while (i.hasNext()) {
        Map.Entry entry = (Map.Entry) i.next();
        nodeDefinition.addIntegerParameter((String) entry.getKey(), (String) entry.getValue());
      }
    }

    public ScriptedProducerNodeDefinition getDefinition() {
      return nodeDefinition;
    }
  }

  private final static String   NODE_DEFINITION_PARAMETER_NAME_ATTRIBUTE = "name";
  private final static String   NODE_DEFINITION_PARAMETER_DEFAULTVALUE_ATTRIBUTE = "defaultvalue";
  private final static String[] NODE_DEFINITION_PARAMETER_REQUIRED_ATTRIBUTES = { NODE_DEFINITION_PARAMETER_NAME_ATTRIBUTE };
  private final static String[] NODE_DEFINITION_PARAMETER_OPTIONAL_ATTRIBUTES = { NODE_DEFINITION_PARAMETER_DEFAULTVALUE_ATTRIBUTE };
  private final static String[] NODE_DEFINITION_NODE_PARAMETER_OPTIONAL_ATTRIBUTES = { };

  public class NodeDefinitionParametersSectionHandler extends XMLReader.AbstractSectionHandler {
    private Map nodeParameters;
    private Map stringParameters;
    private Map integerParameters;

    public NodeDefinitionParametersSectionHandler() {
      nodeParameters = new HashMap();
      stringParameters = new HashMap();
      integerParameters = new HashMap();
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      String parameterName;
      String defaultValue;

      if (aTag.equals("node")) {
        XMLReaderTool.checkAttributes(anAttributes,
            NODE_DEFINITION_PARAMETER_REQUIRED_ATTRIBUTES,
            NODE_DEFINITION_NODE_PARAMETER_OPTIONAL_ATTRIBUTES);
        parameterName = (String) anAttributes.get(
            NODE_DEFINITION_PARAMETER_NAME_ATTRIBUTE);

        if (nodeParameters.containsKey(parameterName))
          throw new XMLReader.XMLReaderExc("Duplicate parameter name: '" +
                                           parameterName + "'");

        XMLReaderTool.checkValidIdentifier(parameterName);

        nodeParameters.put(parameterName, parameterName);

        return new EmptySectionHandler();
      }
      else if (aTag.equals("string") || aTag.equals("integer")) {
        XMLReaderTool.checkAttributes(anAttributes,
            NODE_DEFINITION_PARAMETER_REQUIRED_ATTRIBUTES,
            NODE_DEFINITION_PARAMETER_OPTIONAL_ATTRIBUTES);
        parameterName = (String) anAttributes.get(
            NODE_DEFINITION_PARAMETER_NAME_ATTRIBUTE);

        if (stringParameters.containsKey(parameterName) ||
            integerParameters.containsKey(parameterName))
          throw new XMLReader.XMLReaderExc("Duplicate parameter name: '" +
                                           parameterName + "'");

        XMLReaderTool.checkValidIdentifier(parameterName);

        defaultValue = (String) anAttributes.get(
            NODE_DEFINITION_PARAMETER_DEFAULTVALUE_ATTRIBUTE);

        if (aTag.equals("string"))
          stringParameters.put(parameterName, defaultValue);
        else
          integerParameters.put(parameterName, defaultValue);

        return new EmptySectionHandler();
      }
      else
        throw new XMLReader.XMLReaderExc(
            "Only 'string', 'integer' and 'node' tags allowed here, '" + aTag + "' encountered.");
    }

    public void endElement(XMLReader.SectionHandler aHandler) {
    }

    public void finishSection() {
    }

    public Map getNodeParameters() {
      return nodeParameters;
    }

    public Map getStringParameters() {
      return stringParameters;
    }

    public Map getIntegerParameters() {
      return integerParameters;
    }
  }
}
