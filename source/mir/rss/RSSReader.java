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
package mir.rss;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.Vector;
import java.text.*;

import mir.util.XMLReader;
import mir.util.*;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class RSSReader {
  private static final String RDF_NAMESPACE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  private static final String RSS_1_0_NAMESPACE_URI = "http://purl.org/rss/1.0/";
  private static final String DUBLINCORE_NAMESPACE_URI = "http://purl.org/dc/elements/1.1/";
  private static final String EVENT_NAMESPACE_URI = "http://purl.org/rss/1.0/modules/event/";
  private static final String TAXONOMY_NAMESPACE_URI = "http://web.resource.org/rss/1.0/modules/taxonomy/";

  private static final XMLReader.XMLName RDF_ABOUT_PARAMETER = new XMLReader.XMLName(RDF_NAMESPACE_URI, "about");
  private static final XMLReader.XMLName RDF_SEQUENCE_TAG = new XMLReader.XMLName(RDF_NAMESPACE_URI, "Seq");
  private static final XMLReader.XMLName RDF_BAG_PARAMETER = new XMLReader.XMLName(RDF_NAMESPACE_URI, "Bag");

  private static final XMLReader.XMLName RSS_CHANNEL_TAG = new XMLReader.XMLName(RSS_1_0_NAMESPACE_URI, "channel");
  private static final XMLReader.XMLName RSS_ITEM_TAG = new XMLReader.XMLName(RSS_1_0_NAMESPACE_URI, "item");
  private static final XMLReader.XMLName RSS_ITEMS_TAG = new XMLReader.XMLName(RSS_1_0_NAMESPACE_URI, "items");

  private List modules;
  private Map namespaceURItoModule;
  private Map moduleToPrefix;
  private RSSModule rdfModule;

  public RSSReader() {
    modules = new Vector();
    namespaceURItoModule = new HashMap();
    moduleToPrefix = new HashMap();

    registerModule(new RSSBasicModule(RDF_NAMESPACE_URI, "RDF module"), "rdf");
    registerModule(new RSSBasicModule(RSS_1_0_NAMESPACE_URI, "RSS 1.0 module"), "rss");

    RSSBasicModule dcModule = new RSSBasicModule(DUBLINCORE_NAMESPACE_URI, "Dublin Core RSS module 1.1");
    dcModule.addProperty("date", RSSBasicModule.W3CDTF_PROPERTY_TYPE);
    registerModule(dcModule, "dc");

    registerModule(new RSSBasicModule(EVENT_NAMESPACE_URI, "Event RSS module"), "ev");
    registerModule(new RSSBasicModule(TAXONOMY_NAMESPACE_URI, "Taxonomy RSS module"), "taxo");
  }

  public void registerModule(RSSModule aModule, String aPrefix) {
    modules.add(aModule);
    namespaceURItoModule.put(aModule.getNamespaceURI(), aModule);
    moduleToPrefix.put(aModule, aPrefix);
  }

  public RSSData parseInputStream(InputStream aStream) throws RSSExc, RSSFailure {
    try {
      XMLReader xmlReader = new XMLReader(true);
      RSSData result = new RSSData();
      xmlReader.parseInputStream(aStream, new RootSectionHandler(result));

      return result;
    }
    catch (Throwable t) {
      throw new RSSFailure(t);
    }
  }

  public RSSData parseUrl(String anUrl) throws RSSExc, RSSFailure {
    try {
      InputStream inputStream = (InputStream) new URL(anUrl).getContent(new Class[] {InputStream.class});

      if (inputStream==null)
        throw new RSSExc("RSSChannel.parseUrl: Can't get url content");

      return parseInputStream(inputStream);
    }
    catch (Throwable t) {
      throw new RSSFailure(t);
    }
  }

  private class RootSectionHandler extends XMLReader.AbstractSectionHandler {
    private RSSData data;

    public RootSectionHandler(RSSData aData) {
      data = aData;
    }

    public XMLReader.SectionHandler startElement(XMLReader.XMLName aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.getLocalName().equals("RDF")) {
        return new RDFSectionHandler(data);
      }
      else
        throw new XMLReader.XMLReaderFailure(new RSSExc("'RDF' tag expected"));
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };
  }

  private class RDFSectionHandler extends XMLReader.AbstractSectionHandler {
    private RSSData data;


    public RDFSectionHandler(RSSData aData) {
      data = aData;
    }

    public XMLReader.SectionHandler startElement(XMLReader.XMLName aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      String identifier = (String) anAttributes.get(RDF_ABOUT_PARAMETER);
      String rdfClass = makeQualifiedName(aTag);

      return new RDFResourceSectionHandler(rdfClass, identifier);
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (aHandler instanceof RDFResourceSectionHandler) {
        data.addResource(((RDFResourceSectionHandler) aHandler).getResource());
      }
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };
  }

  private XMLReader.SectionHandler makePropertyValueSectionHandler(XMLReader.XMLName aTag, Map anAttributes) {
    RSSModule module = (RSSModule) namespaceURItoModule.get(aTag.getNamespaceURI());

    if (aTag.getLocalName().equals("date"))
      aTag = aTag;


    if (module!=null) {
      RSSModule.RSSModuleProperty property = module.getPropertyForName(aTag.getLocalName());

      if (property!=null) {
        switch (property.getType()) {
          case
            RSSModule.PCDATA_PROPERTY_TYPE:
              return new PCDATASectionHandler();
          case
            RSSModule.RDFCOLLECTION_PROPERTY_TYPE:
              return new RDFCollectionSectionHandler();
//          case
//            RSSModule.RDF_PROPERTY_TYPE:
//              return new RDFValueSectionHandler();
          case
            RSSModule.W3CDTF_PROPERTY_TYPE:
              return new DateSectionHandler();
        }
      }

    }

    return new FlexiblePropertyValueSectionHandler();
  }

  private String makeQualifiedName(XMLReader.XMLName aName) {
    String result=aName.getLocalName();
    RSSModule module = (RSSModule) namespaceURItoModule.get(aName.getNamespaceURI());
    if (module!=null) {
      String prefix = (String) moduleToPrefix.get(module);

      if (prefix!=null && prefix.length()>0)
        result = prefix+":"+result;
    }

    return result;
  }

  private class RDFResourceSectionHandler extends XMLReader.AbstractSectionHandler {
    private String image;
    private XMLReader.XMLName currentTag;
    private RDFResource resource;

    public RDFResourceSectionHandler(String anRDFClass, String anIdentifier) {
      resource = new RDFResource(anRDFClass, anIdentifier);
    }

    public XMLReader.SectionHandler startElement(XMLReader.XMLName aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      currentTag = aTag;

      return makePropertyValueSectionHandler(aTag, anAttributes);
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (aHandler instanceof PropertyValueSectionHandler) {
        resource.set(makeQualifiedName(currentTag), ( (PropertyValueSectionHandler) aHandler).getValue());
      }
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };

    public RDFResource getResource() {
      return resource;
    }
  }

  private abstract class PropertyValueSectionHandler extends XMLReader.AbstractSectionHandler {
    public abstract Object getValue();
  }

  private class FlexiblePropertyValueSectionHandler extends PropertyValueSectionHandler {
    private StringBuffer stringData;
    private Object structuredData;

    public FlexiblePropertyValueSectionHandler() {
      stringData = new StringBuffer();
      structuredData=null;
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.equals(RDF_SEQUENCE_TAG))
        return new RDFSequenceSectionHandler();
      else
        return new DiscardingSectionHandler();
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (aHandler instanceof RDFSequenceSectionHandler) {
        structuredData= ((RDFSequenceSectionHandler) aHandler).getItems();
      }
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      stringData.append(aCharacters);
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };

    public String getData() {
      return stringData.toString();
    }

    public Object getValue() {
      if (structuredData==null)
        return stringData.toString();
      else
        return structuredData;
    }
  }

  private class RDFCollectionSectionHandler extends PropertyValueSectionHandler {
    private List items;

    public RDFCollectionSectionHandler() {
      items = new Vector();
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.equals(RDF_SEQUENCE_TAG))
        return new RDFSequenceSectionHandler();
      else
        return new DiscardingSectionHandler();
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (aHandler instanceof RDFSequenceSectionHandler) {
        items.addAll(((RDFSequenceSectionHandler) aHandler).getItems());
      }
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };

    public List getItems() {
      return items;
    }

    public Object getValue() {
      return items;
    }
  }

  private class PCDATASectionHandler extends PropertyValueSectionHandler {
    private StringBuffer data;

    public PCDATASectionHandler() {
      data = new StringBuffer();
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      throw new XMLReader.XMLReaderFailure(new RSSExc("No subtags allowed here"));
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      data.append(aCharacters);
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };

    public String getData() {
      return data.toString();
    }

    public Object getValue() {
      return data.toString();
    }
  }

  private class DateSectionHandler extends PropertyValueSectionHandler {
    private StringBuffer data;

    public DateSectionHandler() {
      data = new StringBuffer();
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      throw new XMLReader.XMLReaderFailure(new RSSExc("No subtags allowed here"));
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      data.append(aCharacters);
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };

    private final static String SPACE = "[\t\n\r ]*";
    private final static String NUMBER = "[0-9]*";
    private final static String SIGN = "[-+]";

    public Object getValue() {
      try {
        String expression = data.toString().trim();
        SimpleParser parser = new SimpleParser(expression);

        String year="";
        String month="";
        String day="";
        String hour="";
        String minutes="";

          year = parser.parse(NUMBER);
          parser.skip("-");
          month = parser.parse(NUMBER);
          parser.skip("-");
          day = parser.parse(NUMBER);
          parser.skip("T");
          hour = parser.parse(NUMBER);
          parser.skip(":");
          minutes = parser.parse(NUMBER);

          SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm");
          d.setLenient(true);
          expression = year + "-" + month + "-" + day + " " + hour + ":" + minutes;

        System.out.println(expression);

        Date result = d.parse(expression);

        return result;
      }
      catch (Throwable t) {
        return null;
      }
    }
  }


  private class RDFSequenceSectionHandler extends XMLReader.AbstractSectionHandler {
    private List items;

    public RDFSequenceSectionHandler() {
      items = new Vector();
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.equals("rdf:li")) {
        String item = (String) anAttributes.get("rdf:resource");

        if (item!=null)
          items.add(item);
      }

      return new DiscardingSectionHandler();
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };

    public List getItems() {
      return items;
    }
  }

  private class RDFLiteralSectionHandler extends PropertyValueSectionHandler {
    private StringBuffer data;
    private String tag;

    public RDFLiteralSectionHandler() {
      data = new StringBuffer();
    }

    protected StringBuffer getData() {
      return data;
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      tag=aTag;
      data.append("<"+tag+">");

      return new RDFLiteralSectionHandler();
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      data.append(((RDFLiteralSectionHandler) aHandler).getData());
      data.append("</"+tag+">");
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      data.append(aCharacters);
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };

    public Object getValue() {
      return data.toString();
    }
  }

  private class DiscardingSectionHandler extends XMLReader.AbstractSectionHandler {
    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      return this;
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };
  }
}
