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

import mir.generator.*;

import mir.producer.*;
import mir.entity.adapter.*;
import mir.util.*;
import mir.log.*;

public class DefaultProducerNodeBuilders {

  public static void registerBuilders(ProducerNodeBuilderLibrary aBuilderLibrary,
       EntityAdapterModel aModel, Generator.GeneratorLibrary aGeneratorLibrary,
       WriterEngine aWriterEngine, String aSourceBasePath, String aDestinationBasePath) throws ProducerConfigExc {

    aBuilderLibrary.registerBuilder("Set", EvaluatedAssignmentProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("Define", ExpandedAssignmentProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("Log", LoggingProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("Execute", ScriptCallingProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("Resource", ResourceBundleProducerNodeBuilder.class);
    aBuilderLibrary.registerFactory("CopyDir", new DirCopyProducerNodeBuilder.factory( aSourceBasePath, aDestinationBasePath));

    aBuilderLibrary.registerBuilder("DeleteFile", FileDeletingProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("SetFileDate", FileDateSettingProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("If", ConditionalProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("While", LoopProducerNodeBuilder.class);

    aBuilderLibrary.registerFactory("Enumerate", new EnumeratingProducerNodeBuilder.factory(aModel));
    aBuilderLibrary.registerFactory("List", new ListProducerNodeBuilder.factory(aModel));
    aBuilderLibrary.registerFactory("Batch", new BatchingProducerNodeBuilder.factory(aModel));

    aBuilderLibrary.registerFactory("Generate",
        new GeneratingProducerNodeBuilder.factory(aGeneratorLibrary, aWriterEngine));
  }

  public static abstract class AbstractProducerNodeBuilder implements ProducerNodeBuilder {
    private Map attributes;
    private Map subNodes;
    private Set availableSubnodes;

    public AbstractProducerNodeBuilder(String anAvailableSubNodes[]) {
      attributes = new HashMap();
      subNodes = new HashMap();
      availableSubnodes = new HashSet(Arrays.asList(anAvailableSubNodes));
    }

    protected ProducerNode getSubNode(String aName) {
      return (ProducerNode) subNodes.get(aName);
    }

    public void setSubNode(String aName, ProducerNode aNode) {
      subNodes.put(aName, aNode);
    };

    public Set getAvailableSubNodes() {
      return availableSubnodes;
    };
  }

////////////////////////////////////////////////////////////////////////////////

  // general attribute names, specifc builders reference these, to keep attribute
  //    names consistent

  public final static String   SELECTION_ATTRIBUTE = "selection";
  public final static String   ORDER_ATTRIBUTE = "order";
  public final static String   DEFINITION_ATTRIBUTE = "table";
  public final static String   SKIP_ATTRIBUTE = "skip";
  public final static String   KEY_ATTRIBUTE = "key";
  public final static String   LIMIT_ATTRIBUTE = "limit";

////////////////////////////////////////////////////////////////////////////////

  private final static String   ASSIGNMENT_KEY_ATTRIBUTE = KEY_ATTRIBUTE;
  private final static String   ASSIGNMENT_VALUE_ATTRIBUTE = "value";
  private final static String[] ASSIGNMENT_REQUIRED_ATTRIBUTES = { ASSIGNMENT_KEY_ATTRIBUTE, ASSIGNMENT_VALUE_ATTRIBUTE };
  private final static String[] ASSIGNMENT_OPTIONAL_ATTRIBUTES = {};
  private final static String[] ASSIGNMENT_SUBNODES = {};

  public static class ExpandedAssignmentProducerNodeBuilder extends AbstractProducerNodeBuilder {

    private String key;
    private String value;

    public ExpandedAssignmentProducerNodeBuilder() {
      super(ASSIGNMENT_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, ASSIGNMENT_REQUIRED_ATTRIBUTES, ASSIGNMENT_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(ASSIGNMENT_KEY_ATTRIBUTE);
      value = (String) anAttributes.get(ASSIGNMENT_VALUE_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new ExpandedAssignmentProducerNode(key, value);
    };
  }

////////////////////////////////////////////////////////////////////////////////

  public static class EvaluatedAssignmentProducerNodeBuilder extends AbstractProducerNodeBuilder {

    private String key;
    private String value;

    public EvaluatedAssignmentProducerNodeBuilder() {
      super(ASSIGNMENT_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, ASSIGNMENT_REQUIRED_ATTRIBUTES, ASSIGNMENT_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(ASSIGNMENT_KEY_ATTRIBUTE);
      value = (String) anAttributes.get(ASSIGNMENT_VALUE_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new EvaluatedAssignmentProducerNode(key, value);
    };
  }

////////////////////////////////////////////////////////////////////////////////

  public static class EnumeratingProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   ENUMERATION_KEY_ATTRIBUTE = KEY_ATTRIBUTE;
    private final static String   ENUMERATION_DEFINITION_ATTRIBUTE = DEFINITION_ATTRIBUTE;
    private final static String   ENUMERATION_SELECTION_ATTRIBUTE = SELECTION_ATTRIBUTE;
    private final static String   ENUMERATION_ORDER_ATTRIBUTE = ORDER_ATTRIBUTE;
    private final static String   ENUMERATION_DEFAULT_SUBNODE = "default";
    private final static String   ENUMERATION_LIMIT_ATTRIBUTE = LIMIT_ATTRIBUTE;
    private final static String   ENUMERATION_SKIP_ATTRIBUTE = SKIP_ATTRIBUTE;
    private final static String[] ENUMERATION_REQUIRED_ATTRIBUTES = { ENUMERATION_KEY_ATTRIBUTE, ENUMERATION_DEFINITION_ATTRIBUTE };
    private final static String[] ENUMERATION_OPTIONAL_ATTRIBUTES = { ENUMERATION_SELECTION_ATTRIBUTE, ENUMERATION_ORDER_ATTRIBUTE, ENUMERATION_LIMIT_ATTRIBUTE, ENUMERATION_SKIP_ATTRIBUTE};
    private final static String[] ENUMERATION_SUBNODES = {ENUMERATION_DEFAULT_SUBNODE};

    private String key;
    private String definition;
    private String selection;
    private String order;
    private String limit;
    private String skip;
    private EntityAdapterModel model;

    public EnumeratingProducerNodeBuilder(EntityAdapterModel aModel) {
      super(ENUMERATION_SUBNODES);

      model = aModel;
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc  {
      ReaderTool.checkAttributes(anAttributes, ENUMERATION_REQUIRED_ATTRIBUTES, ENUMERATION_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(ENUMERATION_KEY_ATTRIBUTE);
      definition = (String) anAttributes.get(ENUMERATION_DEFINITION_ATTRIBUTE);
      selection = (String) ReaderTool.getStringAttributeWithDefault(anAttributes, ENUMERATION_SELECTION_ATTRIBUTE, "");
      order = (String) ReaderTool.getStringAttributeWithDefault(anAttributes, ENUMERATION_ORDER_ATTRIBUTE, "");
      limit = (String) anAttributes.get(ENUMERATION_LIMIT_ATTRIBUTE);
      skip = (String) anAttributes.get(ENUMERATION_SKIP_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new EntityEnumeratingProducerNode(key, model, definition, selection, order, limit, skip, getSubNode(ENUMERATION_DEFAULT_SUBNODE ));
    };

    public static class factory implements ProducerNodeBuilderFactory {
      private EntityAdapterModel model;

      public factory(EntityAdapterModel aModel) {
        model = aModel;
      }

      public ProducerNodeBuilder makeBuilder() {
        return new EnumeratingProducerNodeBuilder(model);
      }
    }
  }

////////////////////////////////////////////////////////////////////////////////

  public static class LoopProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   LOOP_CONDITION_ATTRIBUTE = "condition";
    private final static String   LOOP_LIMIT_ATTRIBUTE = LIMIT_ATTRIBUTE;
    private final static String   LOOP_DEFAULT_SUBNODE = "default";
    private final static String[] LOOP_REQUIRED_ATTRIBUTES = { LOOP_CONDITION_ATTRIBUTE };
    private final static String[] LOOP_OPTIONAL_ATTRIBUTES = { LOOP_LIMIT_ATTRIBUTE };
    private final static String[] LOOP_SUBNODES = {LOOP_DEFAULT_SUBNODE};

    private String condition;
    private String limit;

    public LoopProducerNodeBuilder() {
      super(LOOP_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc  {
      ReaderTool.checkAttributes(anAttributes, LOOP_REQUIRED_ATTRIBUTES, LOOP_OPTIONAL_ATTRIBUTES);

      condition = (String) anAttributes.get(LOOP_CONDITION_ATTRIBUTE);
      limit = (String) ReaderTool.getStringAttributeWithDefault(anAttributes, LOOP_LIMIT_ATTRIBUTE, "");
    };

    public ProducerNode constructNode() {
      return new LoopProducerNode(condition, limit, getSubNode( LOOP_DEFAULT_SUBNODE ));
    };
  }

////////////////////////////////////////////////////////////////////////////////

  public static class ListProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   LIST_KEY_ATTRIBUTE = KEY_ATTRIBUTE;
    private final static String   LIST_DEFINITION_ATTRIBUTE = DEFINITION_ATTRIBUTE;
    private final static String   LIST_SELECTION_ATTRIBUTE = SELECTION_ATTRIBUTE;
    private final static String   LIST_ORDER_ATTRIBUTE = ORDER_ATTRIBUTE;
    private final static String   LIST_DEFAULT_SUBNODE = "default";
    private final static String   LIST_LIMIT_ATTRIBUTE = LIMIT_ATTRIBUTE;
    private final static String   LIST_SKIP_ATTRIBUTE = SKIP_ATTRIBUTE;
    private final static String[] LIST_REQUIRED_ATTRIBUTES = { LIST_KEY_ATTRIBUTE, LIST_DEFINITION_ATTRIBUTE };
    private final static String[] LIST_OPTIONAL_ATTRIBUTES = { LIST_SELECTION_ATTRIBUTE, LIST_ORDER_ATTRIBUTE, LIST_SKIP_ATTRIBUTE, LIST_LIMIT_ATTRIBUTE};
    private final static String[] LIST_SUBNODES = {};

    private String key;
    private String definition;
    private String selection;
    private String order;
    private String limit;
    private String skip;
    private EntityAdapterModel model;

    public ListProducerNodeBuilder(EntityAdapterModel aModel) {
      super(LIST_SUBNODES);

      model = aModel;
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, LIST_REQUIRED_ATTRIBUTES, LIST_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(LIST_KEY_ATTRIBUTE);
      definition = (String) anAttributes.get(LIST_DEFINITION_ATTRIBUTE);
      selection = (String) ReaderTool.getStringAttributeWithDefault(anAttributes, LIST_SELECTION_ATTRIBUTE, "");
      order = (String) ReaderTool.getStringAttributeWithDefault(anAttributes, LIST_ORDER_ATTRIBUTE, "");
      limit = (String) anAttributes.get(LIST_LIMIT_ATTRIBUTE);
      skip = (String) anAttributes.get(LIST_SKIP_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new EntityListProducerNode(key, model, definition, selection, order, limit, skip, null );
    };

    public static class factory implements ProducerNodeBuilderFactory {
      private EntityAdapterModel model;

      public factory(EntityAdapterModel aModel) {
        model = aModel;
      }

      public ProducerNodeBuilder makeBuilder() {
        return new ListProducerNodeBuilder(model);
      }
    }
  }

////////////////////////////////////////////////////////////////////////////////

  public static class LoggingProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   LOG_MESSAGE_ATTRIBUTE = "message";
    private final static String   LOG_TYPE_ATTRIBUTE = "type";
    private final static String[] LOG_REQUIRED_ATTRIBUTES = { LOG_MESSAGE_ATTRIBUTE };
    private final static String[] LOG_OPTIONAL_ATTRIBUTES = { LOG_TYPE_ATTRIBUTE };
    private final static String[] LOG_SUBNODES = {};

    private String message;
    private int type = LoggerWrapper.INFO_MESSAGE;

    public LoggingProducerNodeBuilder() {
      super(LOG_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      String typeString;

      ReaderTool.checkAttributes(anAttributes, LOG_REQUIRED_ATTRIBUTES, LOG_OPTIONAL_ATTRIBUTES);

      message = (String) anAttributes.get(LOG_MESSAGE_ATTRIBUTE);
      if (anAttributes.containsKey(LOG_TYPE_ATTRIBUTE)) {
        typeString = ((String) anAttributes.get( LOG_TYPE_ATTRIBUTE ));

        if (typeString.toLowerCase().equals("debug"))
          type = LoggerWrapper.DEBUG_MESSAGE;
        else if (typeString.toLowerCase().equals("info"))
          type = LoggerWrapper.INFO_MESSAGE;
        else if (typeString.toLowerCase().equals("error"))
          type = LoggerWrapper.ERROR_MESSAGE;
        else if (typeString.toLowerCase().equals("warning"))
          type = LoggerWrapper.WARN_MESSAGE;
        else if (typeString.toLowerCase().equals("fatal"))
          type = LoggerWrapper.FATAL_MESSAGE;
        else
          throw new ProducerConfigExc("unknown log type: " + typeString + " (allowed are debug, info, warning, error, fatal)");
      }
      else
        type = LoggerWrapper.INFO_MESSAGE;
    };

    public ProducerNode constructNode() {
      return new LoggingProducerNode(message, type);
    };
  }

////////////////////////////////////////////////////////////////////////////////

  public static class ResourceBundleProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   RESOURCEBUNDLE_KEY_ATTRIBUTE = KEY_ATTRIBUTE;
    private final static String   RESOURCEBUNDLE_BUNDLE_ATTRIBUTE = "bundle";
    private final static String   RESOURCEBUNDLE_LANGUAGE_ATTRIBUTE = "language";
    private final static String   RESOURCEBUNDLE_DEFAULT_SUBNODE = "default";
    private final static String[] RESOURCEBUNDLE_REQUIRED_ATTRIBUTES = { RESOURCEBUNDLE_KEY_ATTRIBUTE, RESOURCEBUNDLE_BUNDLE_ATTRIBUTE };
    private final static String[] RESOURCEBUNDLE_OPTIONAL_ATTRIBUTES = { RESOURCEBUNDLE_LANGUAGE_ATTRIBUTE};
    private final static String[] RESOURCEBUNDLE_SUBNODES = {};

    private String key;
    private String bundle;
    private String language;

    public ResourceBundleProducerNodeBuilder() {
      super(RESOURCEBUNDLE_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, RESOURCEBUNDLE_REQUIRED_ATTRIBUTES, RESOURCEBUNDLE_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(RESOURCEBUNDLE_KEY_ATTRIBUTE);
      bundle = (String) anAttributes.get(RESOURCEBUNDLE_BUNDLE_ATTRIBUTE);
      language = (String) anAttributes.get(RESOURCEBUNDLE_LANGUAGE_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new ResourceBundleProducerNode(key, bundle, language);
    };
  }

////////////////////////////////////////////////////////////////////////////////

  public static class FileDateSettingProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   FILEDATESETTING_FILE_ATTRIBUTE = "filename";
    private final static String   FILEDATESETTING_DATE_ATTRIBUTE = "date";
    private final static String[] FILEDATESETTING_REQUIRED_ATTRIBUTES = { FILEDATESETTING_FILE_ATTRIBUTE, FILEDATESETTING_DATE_ATTRIBUTE };
    private final static String[] FILEDATESETTING_OPTIONAL_ATTRIBUTES = { };
    private final static String[] FILEDATESETTING_SUBNODES = {};

    private String fileNameKey;
    private String dateKey;

    public FileDateSettingProducerNodeBuilder() {
      super(FILEDATESETTING_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, FILEDATESETTING_REQUIRED_ATTRIBUTES, FILEDATESETTING_OPTIONAL_ATTRIBUTES);

      fileNameKey = (String) anAttributes.get(FILEDATESETTING_FILE_ATTRIBUTE);
      dateKey = (String) anAttributes.get(FILEDATESETTING_DATE_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new FileDateSettingProducerNode(fileNameKey, dateKey);
    };
  }

////////////////////////////////////////////////////////////////////////////////

  public static class FileDeletingProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   FILEDELETING_FILE_ATTRIBUTE = "filename";
    private final static String[] FILEDELETING_REQUIRED_ATTRIBUTES = { FILEDELETING_FILE_ATTRIBUTE };
    private final static String[] FILEDELETING_OPTIONAL_ATTRIBUTES = { };
    private final static String[] FILEDELETING_SUBNODES = { };

    private String fileNameKey;

    public FileDeletingProducerNodeBuilder() {
      super(FILEDELETING_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, FILEDELETING_REQUIRED_ATTRIBUTES, FILEDELETING_OPTIONAL_ATTRIBUTES);

      fileNameKey = (String) anAttributes.get(FILEDELETING_FILE_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new FileDeletingProducerNode(fileNameKey);
    };
  }

////////////////////////////////////////////////////////////////////////////////

  public static class ScriptCallingProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   SCRIPT_COMMAND_ATTRIBUTE = "command";
    private final static String[] SCRIPT_REQUIRED_ATTRIBUTES = { SCRIPT_COMMAND_ATTRIBUTE };
    private final static String[] SCRIPT_OPTIONAL_ATTRIBUTES = {};
    private final static String[] SCRIPT_SUBNODES = {};

    private String command;

    public ScriptCallingProducerNodeBuilder() {
      super(SCRIPT_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, SCRIPT_REQUIRED_ATTRIBUTES, SCRIPT_OPTIONAL_ATTRIBUTES);

      command = (String) anAttributes.get(SCRIPT_COMMAND_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new ScriptCallingProducerNode(command);
    };
  }

////////////////////////////////////////////////////////////////////////////////

  private final static String   DIRCOPY_SOURCE_ATTRIBUTE = "source";
  private final static String   DIRCOPY_DESTINATION_ATTRIBUTE = "destination";
  private final static String[] DIRCOPY_REQUIRED_ATTRIBUTES = { DIRCOPY_SOURCE_ATTRIBUTE, DIRCOPY_DESTINATION_ATTRIBUTE };
  private final static String[] DIRCOPY_OPTIONAL_ATTRIBUTES = {};
  private final static String[] DIRCOPY_SUBNODES = {};

  public static class DirCopyProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private String source;
    private String destination;
    private String sourceBasePath;
    private String destinationBasePath;

    public DirCopyProducerNodeBuilder(String aSourceBasePath, String aDestinationBasePath) {
      super(DIRCOPY_SUBNODES);

      sourceBasePath = aSourceBasePath;
      destinationBasePath = aDestinationBasePath;
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, DIRCOPY_REQUIRED_ATTRIBUTES, DIRCOPY_OPTIONAL_ATTRIBUTES);

      source = (String) anAttributes.get(DIRCOPY_SOURCE_ATTRIBUTE);
      destination = (String) anAttributes.get(DIRCOPY_DESTINATION_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new DirCopyingProducerNode(sourceBasePath, destinationBasePath, source, destination);
    };

    public static class factory implements ProducerNodeBuilderFactory {
      private String sourceBasePath;
      private String destinationBasePath;

      public factory(String aSourceBasePath, String aDestinationBasePath) {
        sourceBasePath = aSourceBasePath;
        destinationBasePath = aDestinationBasePath;
      }

      public ProducerNodeBuilder makeBuilder() {
        return new DirCopyProducerNodeBuilder(sourceBasePath, destinationBasePath);
      }
    }
  }

////////////////////////////////////////////////////////////////////////////////

  public static class GeneratingProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   GENERATION_GENERATOR_ATTRIBUTE = "generator";
    private final static String   GENERATION_DESTINATION_ATTRIBUTE = "destination";
    private final static String   GENERATION_PARAMETERS_ATTRIBUTE = "parameters";
    private final static String[] GENERATION_REQUIRED_ATTRIBUTES = { GENERATION_GENERATOR_ATTRIBUTE, GENERATION_DESTINATION_ATTRIBUTE };
    private final static String[] GENERATION_OPTIONAL_ATTRIBUTES = { GENERATION_PARAMETERS_ATTRIBUTE};
    private final static String[] GENERATION_SUBNODES = {};

    private String generator;
    private String destination;
    private String parameters;
    private Generator.GeneratorLibrary generatorLibrary;
    private WriterEngine writerEngine;

    public GeneratingProducerNodeBuilder(Generator.GeneratorLibrary aGeneratorLibrary, WriterEngine aWriterEngine) {
      super(GENERATION_SUBNODES);

      writerEngine = aWriterEngine;
      generatorLibrary = aGeneratorLibrary;
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, GENERATION_REQUIRED_ATTRIBUTES, GENERATION_OPTIONAL_ATTRIBUTES);

      generator = (String) anAttributes.get(GENERATION_GENERATOR_ATTRIBUTE);
      destination = (String) anAttributes.get(GENERATION_DESTINATION_ATTRIBUTE);
      parameters = ReaderTool.getStringAttributeWithDefault(anAttributes, GENERATION_PARAMETERS_ATTRIBUTE, "" );
    };

    public ProducerNode constructNode() {
      return new GeneratingProducerNode(generatorLibrary, writerEngine, generator, destination, parameters);
    };

    public static class factory implements ProducerNodeBuilderFactory {
      private Generator.GeneratorLibrary generatorLibrary;
      private WriterEngine writerEngine;

      public factory(Generator.GeneratorLibrary aGeneratorLibrary, WriterEngine aWriterEngine) {
        writerEngine = aWriterEngine;
        generatorLibrary = aGeneratorLibrary;
      }

      public ProducerNodeBuilder makeBuilder() {
        return new GeneratingProducerNodeBuilder(generatorLibrary, writerEngine);
      }
    }
  }

////////////////////////////////////////////////////////////////////////////////

  public static class BatchingProducerNodeBuilder extends AbstractProducerNodeBuilder {

    private final static String   BATCHER_DATAKEY_ATTRIBUTE = KEY_ATTRIBUTE;
    private final static String   BATCHER_INFOKEY_ATTRIBUTE = "infokey";
    private final static String   BATCHER_DEFINITION_ATTRIBUTE = DEFINITION_ATTRIBUTE;
    private final static String   BATCHER_SELECTION_ATTRIBUTE = SELECTION_ATTRIBUTE;
    private final static String   BATCHER_ORDER_ATTRIBUTE = ORDER_ATTRIBUTE;

    private final static String   BATCHER_BATCHSIZE_ATTRIBUTE = "batchsize";
    private final static String   BATCHER_MINBATCHSIZE_ATTRIBUTE = "minbatchsize";
    private final static String   BATCHER_SKIP_ATTRIBUTE = SKIP_ATTRIBUTE;

    private final static String   BATCHER_PROCESS_ATTRIBUTE = "process";

    private final static String   BATCHER_BATCH_SUBNODE = "batches";
    private final static String   BATCHER_BATCHLIST_SUBNODE = "batchlist";
    private final static String[] BATCHER_REQUIRED_ATTRIBUTES = { BATCHER_DATAKEY_ATTRIBUTE, BATCHER_INFOKEY_ATTRIBUTE, BATCHER_DEFINITION_ATTRIBUTE, BATCHER_BATCHSIZE_ATTRIBUTE };
    private final static String[] BATCHER_OPTIONAL_ATTRIBUTES = { BATCHER_SELECTION_ATTRIBUTE, BATCHER_ORDER_ATTRIBUTE, BATCHER_MINBATCHSIZE_ATTRIBUTE, BATCHER_SKIP_ATTRIBUTE, BATCHER_PROCESS_ATTRIBUTE };
    private final static String[] BATCHER_SUBNODES = { BATCHER_BATCH_SUBNODE, BATCHER_BATCHLIST_SUBNODE };

    // ML: batchSize, minBatchSize, skip should be expressions!

    private EntityAdapterModel model;
    private String batchDataKey;
    private String batchInfoKey;
    private String definition;
    private String selection;
    private String order;
    private String batchSize;
    private String minBatchSize;
    private String skip;
    private String process;

    public BatchingProducerNodeBuilder(EntityAdapterModel aModel) {
      super(BATCHER_SUBNODES);

      model = aModel;
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, BATCHER_REQUIRED_ATTRIBUTES, BATCHER_OPTIONAL_ATTRIBUTES);

      batchDataKey = ReaderTool.getStringAttributeWithDefault(anAttributes, BATCHER_DATAKEY_ATTRIBUTE, "data" );
      batchInfoKey = ReaderTool.getStringAttributeWithDefault(anAttributes, BATCHER_INFOKEY_ATTRIBUTE, "info" );
      definition = ReaderTool.getStringAttributeWithDefault(anAttributes, BATCHER_DEFINITION_ATTRIBUTE, "" );
      selection = ReaderTool.getStringAttributeWithDefault(anAttributes, BATCHER_SELECTION_ATTRIBUTE, "" );
      order = ReaderTool.getStringAttributeWithDefault(anAttributes, BATCHER_ORDER_ATTRIBUTE, "" );

      batchSize = ReaderTool.getStringAttributeWithDefault(anAttributes, BATCHER_BATCHSIZE_ATTRIBUTE, "20" );
      minBatchSize = ReaderTool.getStringAttributeWithDefault(anAttributes, BATCHER_MINBATCHSIZE_ATTRIBUTE, "0" );
      skip = ReaderTool.getStringAttributeWithDefault(anAttributes, BATCHER_SKIP_ATTRIBUTE, "0" );
      process = ReaderTool.getStringAttributeWithDefault(anAttributes, BATCHER_PROCESS_ATTRIBUTE, "-1" );
    };

    public ProducerNode constructNode() {
      return new EntityBatchingProducerNode(
          batchDataKey,
          batchInfoKey,
          model,
          definition,
          selection,
          order,
          batchSize,
          minBatchSize,
          skip,
          process,
          getSubNode( BATCHER_BATCH_SUBNODE ),
          getSubNode( BATCHER_BATCHLIST_SUBNODE )
      );
    };

    public static class factory implements ProducerNodeBuilderFactory {
      private EntityAdapterModel model;

      public factory(EntityAdapterModel aModel) {
        model = aModel;
      }

      public ProducerNodeBuilder makeBuilder() {
        return new BatchingProducerNodeBuilder(model);
      }
    }
  }

////////////////////////////////////////////////////////////////////////////////

  public static class ConditionalProducerNodeBuilder extends AbstractProducerNodeBuilder {
    private final static String   IF_CONDITION_ATTRIBUTE = "condition";

    private final static String   IF_TRUE_SUBNODE = "then";
    private final static String   IF_FALSE_SUBNODE = "else";
    private final static String[] IF_REQUIRED_ATTRIBUTES = { IF_CONDITION_ATTRIBUTE };
    private final static String[] IF_OPTIONAL_ATTRIBUTES = {  };
    private final static String[] IF_SUBNODES = { IF_TRUE_SUBNODE, IF_FALSE_SUBNODE };

    private String condition;

    public ConditionalProducerNodeBuilder() {
      super(IF_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributes(anAttributes, IF_REQUIRED_ATTRIBUTES, IF_OPTIONAL_ATTRIBUTES);

      condition = (String) anAttributes.get( IF_CONDITION_ATTRIBUTE );
    };

    public ProducerNode constructNode() {
      return new ConditionalProducerNode(
          condition,
          getSubNode( IF_TRUE_SUBNODE ),
          getSubNode( IF_FALSE_SUBNODE )
      );
    };

  }

////////////////////////////////////////////////////////////////////////////////

  public static class ScriptedProducerParameterNodeBuilder implements ProducerNodeBuilder {
    private String parameterName;
    private String scriptedNodeName;

    public ScriptedProducerParameterNodeBuilder(String aScriptedNodeName, String aParameterName) {
      parameterName = aParameterName;
      scriptedNodeName = aScriptedNodeName;
    }

    public void setSubNode(String aName, ProducerNode aNode) {
    };

    public Set getAvailableSubNodes() {
      return new HashSet();
    };

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      if (!anAttributes.isEmpty())
        throw new ProducerConfigExc("No parameters allowed here");
    };

    public ProducerNode constructNode() {
      return new ScriptedProducerNodeDefinition.NodeParameterProducerNode(scriptedNodeName, parameterName);
    };
  }

////////////////////////////////////////////////////////////////////////////////

  public static class ScriptedProducerNodeBuilder implements ProducerNodeBuilder {
    private ScriptedProducerNodeDefinition definition;
    private Map nodeParameterValues;
    private Map integerParameterValues;
    private Map stringParameterValues;

    public ScriptedProducerNodeBuilder(ScriptedProducerNodeDefinition aDefinition) {
      definition = aDefinition;

      stringParameterValues = new HashMap();
      stringParameterValues.putAll(definition.getStringParameters());

      integerParameterValues = new HashMap();
      integerParameterValues.putAll(definition.getIntegerParameters());

      nodeParameterValues = new HashMap();
    }

    public void setSubNode(String aName, ProducerNode aNode) {
      nodeParameterValues.put(aName, aNode);
    };

    public Set getAvailableSubNodes() {
      return definition.getNodeParameters();
    };

    public void setAttributes(Map anAttributes) throws ProducerConfigExc {
      ReaderTool.checkAttributeSet(anAttributes.keySet(), definition.getRequiredAttributes(), definition.getOptionalAttributes());

      Iterator i = anAttributes.entrySet().iterator();
      while (i.hasNext()) {
        Map.Entry entry = (Map.Entry) i.next();

        if (definition.getIntegerParameters().keySet().contains(entry.getKey()))
          integerParameterValues.put(entry.getKey(), entry.getValue());
        else
          stringParameterValues.put(entry.getKey(), entry.getValue());
      }
    };

    public ProducerNode constructNode() {
      return new ScriptedProducerNode(definition, stringParameterValues, integerParameterValues, nodeParameterValues);
    };

    public static class factory implements ProducerNodeBuilderFactory {
      private ScriptedProducerNodeDefinition definition;

      public factory(ScriptedProducerNodeDefinition aDefinition) {
        definition = aDefinition;
      }

      public ProducerNodeBuilder makeBuilder() {
        return new ScriptedProducerNodeBuilder(definition);
      }
    }
  }
}
