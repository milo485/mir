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

package mircoders.producer.reader;

import java.util.Map;

import mir.entity.adapter.EntityAdapterModel;
import mir.producer.ProducerNode;
import mir.producer.reader.DefaultProducerNodeBuilders;
import mir.producer.reader.ProducerConfigExc;
import mir.producer.reader.ProducerNodeBuilderLibrary;
import mir.util.XMLReader;
import mir.util.XMLReaderTool;
import mircoders.producer.ContentMarkingProducerNode;
import mircoders.producer.ContentModifyingProducerNode;
import mircoders.producer.IndexingProducerNode;
import mircoders.producer.MediaGeneratingProducerNode;
import mircoders.producer.PDFGeneratingProducerNode;
import mircoders.producer.PDFPreFormattingProducerNode;
import mircoders.producer.UnIndexingProducerNode;


public class SupplementalProducerNodeBuilders {

  public static void registerBuilders(ProducerNodeBuilderLibrary aBuilderLibrary, EntityAdapterModel aModel) throws ProducerConfigExc {
    aBuilderLibrary.registerBuilder("ModifyContent", ContentModifyingProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("MarkContent", ContentMarkingProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("GenerateMedia", MediaGeneratingProducerNodeBuilder.class);

    aBuilderLibrary.registerBuilder("IndexContent",ContentIndexingProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("UnIndexContent",ContentUnIndexingProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("PDFPreFormat", PDFPreFormattingProducerNodeBuilder.class);
    aBuilderLibrary.registerBuilder("PDFGenerate", PDFGeneratingProducerNodeBuilder.class);
  }

  private final static String   MARKER_KEY_ATTRIBUTE = DefaultProducerNodeBuilders.KEY_ATTRIBUTE;
  private final static String[] MARKER_REQUIRED_ATTRIBUTES = { MARKER_KEY_ATTRIBUTE };
  private final static String[] MARKER_OPTIONAL_ATTRIBUTES = {};
  private final static String[] MARKER_SUBNODES = {};

  public static class ContentMarkingProducerNodeBuilder extends DefaultProducerNodeBuilders.AbstractProducerNodeBuilder {

    private String key;

    public ContentMarkingProducerNodeBuilder() {
      super(MARKER_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc, XMLReader.XMLReaderExc {
      XMLReaderTool.checkAttributes(anAttributes, MARKER_REQUIRED_ATTRIBUTES, MARKER_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(MARKER_KEY_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new ContentMarkingProducerNode(key);
    };
  }
  private final static String   INDEXER_KEY_ATTRIBUTE = DefaultProducerNodeBuilders.KEY_ATTRIBUTE;
  private final static String   INDEXER_INDEX_ATTRIBUTE = "pathToIndex";
  private final static String[] INDEXER_REQUIRED_ATTRIBUTES = { INDEXER_KEY_ATTRIBUTE, INDEXER_INDEX_ATTRIBUTE };
  private final static String[] INDEXER_OPTIONAL_ATTRIBUTES = {};
  private final static String[] INDEXER_SUBNODES = {};

  public static class ContentIndexingProducerNodeBuilder extends DefaultProducerNodeBuilders.AbstractProducerNodeBuilder {

    private String key;
    private String pathToIndex;

    public ContentIndexingProducerNodeBuilder() {
      super(INDEXER_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc, XMLReader.XMLReaderExc {
      XMLReaderTool.checkAttributes(anAttributes, INDEXER_REQUIRED_ATTRIBUTES, INDEXER_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(INDEXER_KEY_ATTRIBUTE);
      pathToIndex = (String) anAttributes.get(INDEXER_INDEX_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new IndexingProducerNode(key,pathToIndex);
    };
  }

  private final static String   UNINDEXER_KEY_ATTRIBUTE = DefaultProducerNodeBuilders.KEY_ATTRIBUTE;
  private final static String   UNINDEXER_INDEX_ATTRIBUTE = "pathToIndex";
  private final static String[] UNINDEXER_REQUIRED_ATTRIBUTES = { UNINDEXER_KEY_ATTRIBUTE, UNINDEXER_INDEX_ATTRIBUTE };
  private final static String[] UNINDEXER_OPTIONAL_ATTRIBUTES = {};
  private final static String[] UNINDEXER_SUBNODES = {};

  public static class ContentUnIndexingProducerNodeBuilder extends DefaultProducerNodeBuilders.AbstractProducerNodeBuilder {

    private String key;
    private String pathToIndex;

    public ContentUnIndexingProducerNodeBuilder() {
      super(UNINDEXER_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc, XMLReader.XMLReaderExc {
      XMLReaderTool.checkAttributes(anAttributes, UNINDEXER_REQUIRED_ATTRIBUTES, UNINDEXER_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(UNINDEXER_KEY_ATTRIBUTE);
      pathToIndex = (String) anAttributes.get(UNINDEXER_INDEX_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new UnIndexingProducerNode(key,pathToIndex);
    };
  }

  private final static String   CONTENT_MODIFIER_KEY_ATTRIBUTE = DefaultProducerNodeBuilders.KEY_ATTRIBUTE;
  private final static String   CONTENT_MODIFIER_FIELD_ATTRIBUTE = "field";
  private final static String   CONTENT_MODIFIER_VALUE_ATTRIBUTE = "value";
  private final static String[] CONTENT_MODIFIER_REQUIRED_ATTRIBUTES = { CONTENT_MODIFIER_KEY_ATTRIBUTE, CONTENT_MODIFIER_FIELD_ATTRIBUTE, CONTENT_MODIFIER_VALUE_ATTRIBUTE };
  private final static String[] CONTENT_MODIFIER_OPTIONAL_ATTRIBUTES = {};
  private final static String[] CONTENT_MODIFIER_SUBNODES = {};

  public static class ContentModifyingProducerNodeBuilder extends DefaultProducerNodeBuilders.AbstractProducerNodeBuilder {

    private String key;
    private String field;
    private String value;

    public ContentModifyingProducerNodeBuilder() {
      super(CONTENT_MODIFIER_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc, XMLReader.XMLReaderExc {
      XMLReaderTool.checkAttributes(anAttributes, CONTENT_MODIFIER_REQUIRED_ATTRIBUTES, CONTENT_MODIFIER_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(CONTENT_MODIFIER_KEY_ATTRIBUTE);
      field = (String) anAttributes.get(CONTENT_MODIFIER_FIELD_ATTRIBUTE);
      value = (String) anAttributes.get(CONTENT_MODIFIER_VALUE_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new ContentModifyingProducerNode(key, field, value);
    };
  }

  private final static String   MEDIA_KEY_ATTRIBUTE = DefaultProducerNodeBuilders.KEY_ATTRIBUTE;
  private final static String[] MEDIA_REQUIRED_ATTRIBUTES = { MEDIA_KEY_ATTRIBUTE };
  private final static String[] MEDIA_OPTIONAL_ATTRIBUTES = {};
  private final static String[] MEDIA_SUBNODES = {};

  public static class MediaGeneratingProducerNodeBuilder extends DefaultProducerNodeBuilders.AbstractProducerNodeBuilder {

    private String key;

    public MediaGeneratingProducerNodeBuilder() {
      super(MEDIA_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc, XMLReader.XMLReaderExc {
      XMLReaderTool.checkAttributes(anAttributes, MEDIA_REQUIRED_ATTRIBUTES, MEDIA_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(MEDIA_KEY_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new MediaGeneratingProducerNode(key);
    };
  }

  public static class PDFPreFormattingProducerNodeBuilder extends DefaultProducerNodeBuilders.AbstractProducerNodeBuilder {
    private final static String   MARKER_KEY_ATTRIBUTE = DefaultProducerNodeBuilders.KEY_ATTRIBUTE;
    private final static String   PDF_NUM_LINES_ATTRIBUTE = "numLinesBetweenImages";
    private final static String   PDF_CONTENT_WIDTH_ATTRIBUTE = "contentAreaWidthCM";
    private final static String   PDF_CHAR_WIDTH_ATTRIBUTE = "charWidthCM";
    private final static String   PDF_PIXEL_WIDTH_ATTRIBUTE = "pixelWidthCM";
    private final static String   PDF_LINE_HEIGHT_ATTRIBUTE = "lineHeightCM";
    private final static String[] MARKER_REQUIRED_ATTRIBUTES = { MARKER_KEY_ATTRIBUTE, PDF_NUM_LINES_ATTRIBUTE, PDF_CONTENT_WIDTH_ATTRIBUTE,PDF_CHAR_WIDTH_ATTRIBUTE,PDF_PIXEL_WIDTH_ATTRIBUTE,PDF_LINE_HEIGHT_ATTRIBUTE };
    private final static String[] MARKER_OPTIONAL_ATTRIBUTES = {};
    private final static String[] MARKER_SUBNODES = {};

    private String key;
    private String numLinesBetweenImages;
    private String contentAreaWidthCM;
    private String characterWidthCM;
    private String pixelWidthCM;
    private String lineHeightCM;

    public PDFPreFormattingProducerNodeBuilder() {
      super(MARKER_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc, XMLReader.XMLReaderExc {
      XMLReaderTool.checkAttributes(anAttributes, MARKER_REQUIRED_ATTRIBUTES, MARKER_OPTIONAL_ATTRIBUTES);

      key = (String) anAttributes.get(MARKER_KEY_ATTRIBUTE);
      numLinesBetweenImages = (String) anAttributes.get(PDF_NUM_LINES_ATTRIBUTE);
      contentAreaWidthCM = (String) anAttributes.get(PDF_CONTENT_WIDTH_ATTRIBUTE);
      characterWidthCM = (String) anAttributes.get(PDF_CHAR_WIDTH_ATTRIBUTE);
      pixelWidthCM   = (String) anAttributes.get(PDF_PIXEL_WIDTH_ATTRIBUTE);
      lineHeightCM = (String) anAttributes.get(PDF_LINE_HEIGHT_ATTRIBUTE);


    };

    public ProducerNode constructNode() {
      return new PDFPreFormattingProducerNode(key,numLinesBetweenImages,contentAreaWidthCM,characterWidthCM,pixelWidthCM,lineHeightCM);
    };
  }

  public static class PDFGeneratingProducerNodeBuilder extends DefaultProducerNodeBuilders.AbstractProducerNodeBuilder {
    private final static String   MARKER_KEY_ATTRIBUTE = DefaultProducerNodeBuilders.KEY_ATTRIBUTE;
    private final static String   PDF_GENERATOR_ATTRIBUTE = "generator";
    private final static String   PDF_DESTINATION_ATTRIBUTE = "destination";
    private final static String   PDF_STYLESHEET_ATTRIBUTE = "stylesheet";
    private final static String[] MARKER_REQUIRED_ATTRIBUTES = {PDF_GENERATOR_ATTRIBUTE,PDF_DESTINATION_ATTRIBUTE,PDF_STYLESHEET_ATTRIBUTE  };
    private final static String[] MARKER_OPTIONAL_ATTRIBUTES = {};
    private final static String[] MARKER_SUBNODES = {};

    private String generator;
    private String destination;
    private String stylesheet;


    public PDFGeneratingProducerNodeBuilder() {
      super(MARKER_SUBNODES);
    }

    public void setAttributes(Map anAttributes) throws ProducerConfigExc, XMLReader.XMLReaderExc {
      XMLReaderTool.checkAttributes(anAttributes, MARKER_REQUIRED_ATTRIBUTES, MARKER_OPTIONAL_ATTRIBUTES);

      generator = (String) anAttributes.get(PDF_GENERATOR_ATTRIBUTE);
      destination = (String) anAttributes.get(PDF_DESTINATION_ATTRIBUTE);
      stylesheet = (String) anAttributes.get(PDF_STYLESHEET_ATTRIBUTE);
    };

    public ProducerNode constructNode() {
      return new PDFGeneratingProducerNode(generator,destination,stylesheet);
    };
  }
}


