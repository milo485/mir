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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mir.producer.CompositeProducerNode;
import mir.producer.ProducerFactory;
import mir.producer.ProducerNode;
import mir.producer.SimpleProducerVerb;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

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
      builderLibrary = aBuilderLibrary;
      scriptedNodeBuilderLibrary = new ProducerNodeBuilderLibrary();

      SAXParserFactory parserFactory = SAXParserFactory.newInstance();

      parserFactory.setNamespaceAware(false);
      parserFactory.setValidating(true);

      ProducerConfigHandler handler = new ProducerConfigHandler(parserFactory, aProducerFactories, aUsedFiles);

      handler.includeFile(aFileName);
    }
    catch (Throwable e) {
      if (e instanceof SAXParseException && ((SAXParseException) e).getException() instanceof ProducerConfigFailure) {
        throw (ProducerConfigFailure) ((SAXParseException) e).getException();
      }
      else {
        throw new ProducerConfigFailure( e );
      }
    }
  }

  private class ProducerConfigHandler extends DefaultHandler {
    private Locator locator;
    private Stack includeFileStack;
    private SAXParserFactory parserFactory;
    private SectionsManager manager;
    private List usedFiles;
    private InputSource inputSource;

    public ProducerConfigHandler(SAXParserFactory aParserFactory, List aProducers, List aUsedFiles) {
      super();

      includeFileStack=new Stack();
      parserFactory=aParserFactory;
      includeFileStack = new Stack();
      manager = new SectionsManager();
      usedFiles = aUsedFiles;

      manager.pushHandler(new RootSectionHandler(aProducers));
   }

    public String getLocatorDescription(Locator aLocator) {
      return aLocator.getPublicId()+" ("+aLocator.getLineNumber()+")";
    }

    public void setDocumentLocator(Locator aLocator) {
      locator=aLocator;
    }

    private void includeFile(String aFileName) throws ProducerConfigExc, ProducerConfigFailure, SAXParseException, SAXException {
      File file;
      SAXParser parser;

      try {
        if (!includeFileStack.empty())
          file = new File(new File((String) includeFileStack.peek()).getParent(), aFileName);
        else
          file = new File(aFileName);

        System.err.println("about to include "+file.getCanonicalPath());

        if (includeFileStack.contains(file.getCanonicalPath())) {
          throw new ProducerConfigExc("recursive inclusion of file "+file.getCanonicalPath());
        }

        usedFiles.add(file);

        parser=parserFactory.newSAXParser();

        inputSource = new InputSource(new FileInputStream(file));
        inputSource.setPublicId(file.getCanonicalPath());

        includeFileStack.push(file.getCanonicalPath());
        try {
          parser.parse(inputSource, this);
        }
        finally {
          includeFileStack.pop();
        }
      }
      catch (ParserConfigurationException e) {
        throw new ProducerConfigExc("Internal exception while including \""+aFileName+"\": "+e.getMessage());
      }
      catch (SAXParseException e) {
        throw e;
      }
      catch (ProducerConfigFailure e) {
        throw e;
      }
      catch (FileNotFoundException e) {
        throw new ProducerConfigExc("Include file \""+aFileName+"\" not found: "+e.getMessage());
      }
      catch (IOException e) {
        throw new ProducerConfigExc("unable to open include file \""+aFileName+"\": "+e.getMessage());
      }
    }

    public void startElement(String aUri, String aTag, String aQualifiedName, Attributes anAttributes) throws SAXException {
      Map attributesMap;
      int i;

      try {
        if (aQualifiedName.equals("include")) {
          String fileName=anAttributes.getValue("file");

          if (fileName==null) {
            throw new ProducerConfigExc("include has no file attribute");
          }

          includeFile(fileName);
        }
        else {
          attributesMap = new HashMap();
          for (i=0; i<anAttributes.getLength(); i++)
            attributesMap.put(anAttributes.getQName(i), anAttributes.getValue(i));

          manager.pushHandler( manager.currentHandler().startElement(aQualifiedName, attributesMap) );
        }
      }
      catch (ProducerConfigExc e) {
        throw new SAXParseException(e.getMessage(), locator, new ProducerConfigExc("Config error at ["+getLocatorDescription(locator)+"]: "+e.getMessage()));
      }
      catch (Exception e) {
        throw new SAXException(e);
      }
    }

    public void endElement(String aUri, String aTag, String aQualifiedName) throws SAXException {
      try
      {
        if (!aQualifiedName.equals("include")) {
          SectionHandler handler = manager.popHandler();

          handler.finishSection();

          if (!manager.isEmpty()) {
            manager.currentHandler().endElement(handler);
          }
        }
      }
      catch (ProducerConfigExc e) {
        throw new SAXParseException(e.getMessage(), locator, new ProducerConfigExc("Config error at ["+getLocatorDescription(locator)+"]: "+e.getMessage()));
      }
      catch (Exception e) {
        throw new SAXException(e);
      }
    }

    public void characters(char[] aBuffer, int aStart, int anEnd) throws SAXParseException {
      String text = new String(aBuffer, aStart, anEnd).trim();
      if ( text.length() > 0) {
        throw new SAXParseException("Text not allowed", locator, new ProducerConfigExc("Config error at ["+getLocatorDescription(locator)+"]: Text not allowed"));
      }
    }

  }
  public class SectionsManager {
    Stack handlerStack;

    public SectionsManager() {
      handlerStack = new Stack();
    }

    public void pushHandler(SectionHandler aSectionHandler) {
      handlerStack.push(aSectionHandler);
    }

    public SectionHandler popHandler() {
      return (SectionHandler) handlerStack.pop();
    }

    public SectionHandler currentHandler() {
      return (SectionHandler) handlerStack.peek();
    }

    public boolean isEmpty() {
      return handlerStack.isEmpty();
    }
  }

  public abstract class SectionHandler {
    public abstract SectionHandler startElement(String aTag, Map anAttributes) throws ProducerConfigExc;

    public abstract void endElement(SectionHandler aHandler) throws ProducerConfigExc;
//    {
//    }

    public void finishSection() throws ProducerConfigExc {
    }
  }

  public class RootSectionHandler extends SectionHandler {
    private List producers;

    public RootSectionHandler(List aProducers) {
      producers = aProducers;
    }

    public SectionHandler startElement(String aTag, Map anAttributes) throws ProducerConfigExc {
      if (aTag.equals("producers")) {
        return new ProducersSectionHandler(producers);
      }
      else
        throw new ProducerConfigExc ("Tag 'producers' expected, tag '"+aTag+"' found");
    }

    public void endElement(SectionHandler aHandler) {
    }

    public void finishSection() throws ProducerConfigExc {
    }
  }


  private final static String   PRODUCER_NAME_ATTRIBUTE = "name";
  private final static String[] PRODUCER_REQUIRED_ATTRIBUTES = { PRODUCER_NAME_ATTRIBUTE };
  private final static String[] PRODUCER_OPTIONAL_ATTRIBUTES = { };

  private final static String   NODE_DEFINITION_NAME_ATTRIBUTE = "name";
  private final static String[] NODE_DEFINITION_REQUIRED_ATTRIBUTES = { NODE_DEFINITION_NAME_ATTRIBUTE };
  private final static String[] NODE_DEFINITION_OPTIONAL_ATTRIBUTES = {  };

  public class ProducersSectionHandler extends SectionHandler {
    private List producers;
    private Set producerNames;
    private String name;

    public ProducersSectionHandler(List aProducers) {
      producers = aProducers;
      producerNames = new HashSet();
    }

    public SectionHandler startElement(String aTag, Map anAttributes) throws ProducerConfigExc {

      if (aTag.equals("producer")) {
        ReaderTool.checkAttributes(anAttributes, PRODUCER_REQUIRED_ATTRIBUTES, PRODUCER_OPTIONAL_ATTRIBUTES);

        name = (String) anAttributes.get(PRODUCER_NAME_ATTRIBUTE);
        ReaderTool.checkValidIdentifier( name );

        if (producerNames.contains(name))
          throw new ProducerConfigExc("Duplicate producer name: '" + name + "'");

        name = (String) anAttributes.get(PRODUCER_NAME_ATTRIBUTE);

        return new ProducerSectionHandler(name);
      }
      else if (aTag.equals("nodedefinition")) {
        ReaderTool.checkAttributes(anAttributes, NODE_DEFINITION_REQUIRED_ATTRIBUTES, NODE_DEFINITION_OPTIONAL_ATTRIBUTES);

        name = (String) anAttributes.get(NODE_DEFINITION_NAME_ATTRIBUTE);
        ReaderTool.checkValidIdentifier( name );

//        if (producers.containsKey(name))
//          throw new ProducerConfigExc("Duplicate producer name: '" + name + "'");

        name = (String) anAttributes.get(NODE_DEFINITION_NAME_ATTRIBUTE);

        return new NodeDefinitionSectionHandler(name);
      }

      throw new ProducerConfigExc("Unexpected tag: "+aTag );
    }

    public void endElement(SectionHandler aHandler) throws ProducerConfigExc {
      if (aHandler instanceof ProducerSectionHandler) {
        producers.add(((ProducerSectionHandler) aHandler).getProducerFactory());
        producerNames.add(((ProducerSectionHandler) aHandler).getProducerFactory().getName());
      }
      else if (aHandler instanceof NodeDefinitionSectionHandler) {
        scriptedNodeBuilderLibrary.registerFactory(name,
            new DefaultProducerNodeBuilders.ScriptedProducerNodeBuilder.factory(
                ((NodeDefinitionSectionHandler) aHandler).getDefinition()));
      }
      else throw new ProducerConfigExc("ProducersSectionHandler.endElement Internal error: Unexpected handler: " + aHandler.getClass().getName());
    }

    public void finishSection() throws ProducerConfigExc {
    }
  }

  public class ProducerSectionHandler extends SectionHandler {
    private ProducerFactory producerFactory;
    private String factoryName;

    private ProducerNode body;
    private Map verbNodes;
    private List verbs;
    private String defaultVerb;

    public ProducerSectionHandler(String aName) {
      factoryName = aName;
    }

    public SectionHandler startElement(String aTag, Map anAttributes)  throws ProducerConfigExc {
      if (aTag.equals("verbs")) {
        if (verbs!=null)
          throw new ProducerConfigExc("Verbs already processed");
        if (body!=null)
          throw new ProducerConfigExc("Verbs should come before body");
        else
          return new ProducerVerbsSectionHandler();
      }
      else if (aTag.equals("body")) {
        if (body==null)
          return new ProducerNodeSectionHandler();
        else
          throw new ProducerConfigExc("Body already processed");
      }
      throw new ProducerConfigExc("Unexpected tag: '"+aTag+"'");
    }

    public void endElement(SectionHandler aHandler) throws ProducerConfigExc {
      if (aHandler instanceof ProducerNodeSectionHandler) {
        body = ((ProducerNodeSectionHandler) aHandler).getProducerNode();
      }
      else if (aHandler instanceof ProducerVerbsSectionHandler)
      {
        verbs = ((ProducerVerbsSectionHandler) aHandler).getVerbs();
        verbNodes = ((ProducerVerbsSectionHandler) aHandler).getVerbNodes();
        defaultVerb = ((ProducerVerbsSectionHandler) aHandler).getDefaultVerb();
      }
      else throw new ProducerConfigExc("ProducerSectionHandler.endElement Internal error: Unexpected handler: " + aHandler.getClass().getName());
    }

    public void finishSection() throws ProducerConfigExc {
      if (verbs==null)
        throw new ProducerConfigExc("No verbs defined");

      if (body==null)
        throw new ProducerConfigExc("No body defined");

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

  public class ProducerVerbsSectionHandler extends SectionHandler {
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

    public SectionHandler startElement(String aTag, Map anAttributes) throws ProducerConfigExc {
      if (aTag.equals("verb")) {
        ReaderTool.checkAttributes(anAttributes, PRODUCER_VERB_REQUIRED_ATTRIBUTES, PRODUCER_VERB_OPTIONAL_ATTRIBUTES);
        currentVerb = (String) anAttributes.get( PRODUCER_VERB_NAME_ATTRIBUTE );

        ReaderTool.checkValidIdentifier( currentVerb );

        if (verbNodes.containsKey(currentVerb))
          throw new ProducerConfigExc( "Duplicate definition of verb '" + currentVerb + "'" );

        if (anAttributes.containsKey(PRODUCER_VERB_DEFAULT_ATTRIBUTE)) {
          if (defaultVerb!=null)
            throw new ProducerConfigExc( "Default verb already declared" );

          defaultVerb = currentVerb;
        }

        if (anAttributes.containsKey( PRODUCER_VERB_DESCRIPTION_ATTRIBUTE ))
          currentVerbDescription = (String) anAttributes.get( PRODUCER_VERB_DESCRIPTION_ATTRIBUTE );
        else
          currentVerbDescription = "";

        return new ProducerNodeSectionHandler();
      }
      else throw new ProducerConfigExc("Only 'verb' tags allowed here, '" + aTag + "' encountered.");
    }

    public void endElement(SectionHandler aHandler) {
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

  public class EmptySectionHandler extends SectionHandler {
    public SectionHandler startElement(String aTag, Map anAttributes) throws ProducerConfigExc {
      throw new ProducerConfigExc("No tags are allowed here");
    }

    public void endElement(SectionHandler aHandler) {
    }

  }

  public class MultiProducerNodeSectionHandler extends SectionHandler {
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

    public SectionHandler startElement(String aTag, Map anAttributes) throws ProducerConfigExc {
      if (!validNodeParameters.contains(aTag))
        throw new ProducerConfigExc("Invalid node parameter: '" + aTag + "'");
      else if (nodeParameters.containsKey(aTag))
        throw new ProducerConfigExc("Node parameter: '" + aTag + "' already specified");
      else if (anAttributes.size()>0)
        throw new ProducerConfigExc("No parameters are allowed here");

      currentNodeParameter = aTag;

      return new ProducerNodeSectionHandler(scriptedNodeName, validNodeParameters);
    }

    public void endElement(SectionHandler aHandler) throws ProducerConfigExc  {
      if (aHandler instanceof ProducerNodeSectionHandler) {
        nodeParameters.put(currentNodeParameter, ((ProducerNodeSectionHandler) aHandler).getProducerNode());
      }
      else {
        throw new ProducerConfigExc("Internal error: unknown section handler '" + aHandler.getClass().getName() + "'" );
      }
    }

    public Map getNodeParameters() {
      return nodeParameters;
    }
  }

  public class ProducerNodeSectionHandler extends SectionHandler {
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

    public SectionHandler startElement(String aTag, Map anAttributes) throws ProducerConfigExc {
      if (allowedNodeParameterReferences.contains((aTag))) {
        if (!anAttributes.isEmpty()) {
          throw new ProducerConfigExc( "No attributes allowed" );
        }

        currentBuilder = new DefaultProducerNodeBuilders.ScriptedProducerParameterNodeBuilder(scriptedNodeName, aTag);
//        producerNode.addSubNode(
//        new ScriptedProducerNodeDefinition.NodeParameterProducerNode(scriptedNodeName, aTag));
        return new EmptySectionHandler();
      }
      else if (scriptedNodeBuilderLibrary.hasBuilderForName(aTag) || builderLibrary.hasBuilderForName((aTag))) {

        if (scriptedNodeBuilderLibrary.hasBuilderForName(aTag))
          currentBuilder = scriptedNodeBuilderLibrary.constructBuilder(aTag);
        else
          currentBuilder = builderLibrary.constructBuilder(aTag);

        currentBuilder.setAttributes(anAttributes);
        if (currentBuilder.getAvailableSubNodes().isEmpty())  {
          return new EmptySectionHandler();
        }
        if (currentBuilder.getAvailableSubNodes().size()>1)
          return new MultiProducerNodeSectionHandler(scriptedNodeName, allowedNodeParameterReferences, currentBuilder.getAvailableSubNodes());
        else if (currentBuilder.getAvailableSubNodes().size()<1)
          return new EmptySectionHandler();
        else {
          return new ProducerNodeSectionHandler(scriptedNodeName, allowedNodeParameterReferences);
        }
      }
      else
        throw new ProducerConfigExc("Unknown producer node tag: '" + aTag + "'");
    }

    public void endElement(SectionHandler aHandler) throws ProducerConfigExc  {
      if (aHandler instanceof ProducerNodeSectionHandler) {
        currentBuilder.setSubNode((String) (currentBuilder.getAvailableSubNodes().iterator().next()),
                    ((ProducerNodeSectionHandler) aHandler).getProducerNode());
      }
      else if (aHandler instanceof MultiProducerNodeSectionHandler) {
        Iterator i;
        Map nodeParameters;
        Map.Entry entry;

        nodeParameters = ((MultiProducerNodeSectionHandler) aHandler).getNodeParameters();
        i = nodeParameters.entrySet().iterator();
        while (i.hasNext()) {
          entry = (Map.Entry) i.next();
          currentBuilder.setSubNode((String) entry.getKey(), (ProducerNode) entry.getValue());
        }
      }
      else if (aHandler instanceof EmptySectionHandler) {
        // deliberately empty: nothing expected, so nothing to process
      }
      else {
        throw new ProducerConfigExc("Internal error: unknown section handler '" + aHandler.getClass().getName() + "'" );
      }

      producerNode.addSubNode(currentBuilder.constructNode());
      currentBuilder = null;
    }

    public ProducerNode getProducerNode() {
      if (producerNode.getNrSubNodes()==1) {
        return producerNode.getSubNode(0);
      }
      else {
        return producerNode;
      }
    }
  }

  public class NodeDefinitionSectionHandler extends SectionHandler {
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

    public SectionHandler startElement(String aTag, Map anAttributes) throws ProducerConfigExc {
      if (aTag.equals("parameters")) {
        if (!anAttributes.isEmpty()) {
          throw new ProducerConfigExc( "No attributes allowed for tag 'parameters'" );
        }
        if (nodeParameters!=null) {
          throw new ProducerConfigExc( "Parameters have already been declared" );
        }
        if (body!=null) {
          throw new ProducerConfigExc( "Parameters should come before definition" );
        }

        return new NodeDefinitionParametersSectionHandler();
      }
      else if (aTag.equals("definition")) {
        return new ProducerNodeSectionHandler(name, nodeParameters.keySet());
      }
      else throw new ProducerConfigExc("Only 'definition' or 'parameters' tags allowed here, '" + aTag + "' encountered.");
    }

    public void endElement(SectionHandler aHandler) {
      if (aHandler instanceof NodeDefinitionParametersSectionHandler) {
        stringParameters = ((NodeDefinitionParametersSectionHandler) aHandler).getStringParameters();
        integerParameters = ((NodeDefinitionParametersSectionHandler) aHandler).getIntegerParameters();
        nodeParameters = ((NodeDefinitionParametersSectionHandler) aHandler).getNodeParameters();
      }
      else if (aHandler instanceof ProducerNodeSectionHandler) {
        body = ((ProducerNodeSectionHandler) aHandler).getProducerNode();
      }
    }

    public void finishSection() throws ProducerConfigExc {
      Iterator i;
      if (body == null)
        throw new ProducerConfigExc( "Definition missing" );

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

  public class NodeDefinitionParametersSectionHandler extends SectionHandler {
    private Map nodeParameters;
    private Map stringParameters;
    private Map integerParameters;

    public NodeDefinitionParametersSectionHandler() {
      nodeParameters = new HashMap();
      stringParameters = new HashMap();
      integerParameters = new HashMap();
    }

    public SectionHandler startElement(String aTag, Map anAttributes) throws ProducerConfigExc {
      String parameterName;
      String defaultValue;

      if (aTag.equals("node")) {
        ReaderTool.checkAttributes(anAttributes, NODE_DEFINITION_PARAMETER_REQUIRED_ATTRIBUTES, NODE_DEFINITION_NODE_PARAMETER_OPTIONAL_ATTRIBUTES);
        parameterName = (String) anAttributes.get( NODE_DEFINITION_PARAMETER_NAME_ATTRIBUTE );

        if (nodeParameters.containsKey(parameterName))
          throw new ProducerConfigExc("Duplicate parameter name: '" + parameterName + "'");

        ReaderTool.checkValidIdentifier( parameterName );

        nodeParameters.put(parameterName, parameterName);

        return new EmptySectionHandler();
      }
      else if (aTag.equals("string") || aTag.equals("integer")) {
        ReaderTool.checkAttributes(anAttributes, NODE_DEFINITION_PARAMETER_REQUIRED_ATTRIBUTES, NODE_DEFINITION_PARAMETER_OPTIONAL_ATTRIBUTES);
        parameterName = (String) anAttributes.get( NODE_DEFINITION_PARAMETER_NAME_ATTRIBUTE );

        if (stringParameters.containsKey(parameterName) || integerParameters.containsKey(parameterName))
          throw new ProducerConfigExc("Duplicate parameter name: '" + parameterName + "'");

        ReaderTool.checkValidIdentifier( parameterName );

        defaultValue = (String) anAttributes.get( NODE_DEFINITION_PARAMETER_DEFAULTVALUE_ATTRIBUTE );

        if (aTag.equals("string"))
          stringParameters.put(parameterName, defaultValue);
        else
          integerParameters.put(parameterName, defaultValue);

        return new EmptySectionHandler();
      }
      else throw new ProducerConfigExc("Only 'string', 'integer' and 'node' tags allowed here, '" + aTag + "' encountered.");

    }

    public void endElement(SectionHandler aHandler) {
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
