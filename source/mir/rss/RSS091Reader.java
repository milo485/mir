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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.util.XMLReader;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class RSS091Reader {
  private final static String MAPPED_CHANNEL_PROPERTIES[][]  =
     {
       {"link",        "rss:link"        },
       {"title",       "rss:title"       },
       {"description", "rss:description" },
       {"item",        "rss:item" },
       {"language",    "dc:language" }
     };

  private final static String MAPPED_ITEM_PROPERTIES[][]  =
    {
      {"link",        "rss:link"        },
      {"title",       "rss:title"       },
      {"description", "rss:description" },
      {"author",      "dc:creator" },
    };

  private Map mappedChannelProperties = new HashMap();
  private Map mappedItemProperties = new HashMap();

  public RSS091Reader() {
    int i;

    for (i=0; i<MAPPED_CHANNEL_PROPERTIES.length; i++) {
      mappedChannelProperties.put(MAPPED_CHANNEL_PROPERTIES[i][0], MAPPED_CHANNEL_PROPERTIES[i][1]);
    }

    for (i=0; i<MAPPED_ITEM_PROPERTIES.length; i++) {
      mappedItemProperties.put(MAPPED_ITEM_PROPERTIES[i][0], MAPPED_ITEM_PROPERTIES[i][1]);
    }
  }

  public RSSData parseInputStream(InputStream aStream) throws RSSExc, RSSFailure {
    try {
      XMLReader xmlReader = new XMLReader(false);
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
      if (aTag.getLocalName().equals("rss")) {
        return new RSS091SectionHandler(data);
      }
      else
        throw new XMLReader.XMLReaderFailure(new RSSExc("'rss' tag expected"));
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

  private class RSS091SectionHandler extends XMLReader.AbstractSectionHandler {
    private RSSData data;


    public RSS091SectionHandler(RSSData aData) {
      data = aData;
    }

    public XMLReader.SectionHandler startElement(XMLReader.XMLName aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.getLocalName().equals("channel"))
        return new RSS091ChannelSectionHandler(data);
      else
        throw new XMLReader.XMLReaderExc("channel tag expected, " + aTag.getLocalName() + " found");
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };
  }

  private class RSS091ChannelSectionHandler extends XMLReader.AbstractSectionHandler {
    private String currentTag;

    private RSSData data;
    private List items;
    private RDFResource channel;
    private Map attributes;

    public RSS091ChannelSectionHandler(RSSData aData) {
      data = aData;
      items = new Vector();
      channel = new RDFResource("rss:channel");
      attributes = new HashMap();
    }

    public XMLReader.SectionHandler startElement(XMLReader.XMLName aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      String tag = aTag.getLocalName();

      if (tag.equals("item"))
        return new RSS091ItemSectionHandler();
      else if (mappedChannelProperties.containsKey(tag)) {
        currentTag=(String) mappedChannelProperties.get(tag);
        return new PCDATASectionHandler();
      }
      else
        return new DiscardingSectionHandler();
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (aHandler instanceof PCDATASectionHandler) {
        attributes.put(currentTag, (((PCDATASectionHandler) aHandler).getData()));
      }
      else if (aHandler instanceof RSS091ItemSectionHandler) {
        items.add((((RSS091ItemSectionHandler) aHandler).getItem()));
      }
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
      Iterator i = items.iterator();

      while (i.hasNext()) {
        data.addResource((RDFResource) i.next());
      }
    };
  }

  private class RSS091ItemSectionHandler extends XMLReader.AbstractSectionHandler {
    private String currentTag;

    private RDFResource item;
    private Map attributes;

    public RSS091ItemSectionHandler() {
      attributes = new HashMap();
    }

    public XMLReader.SectionHandler startElement(XMLReader.XMLName aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      String tag = aTag.getLocalName();
      System.out.println(tag);

      if (mappedItemProperties.containsKey(tag)) {
        currentTag=(String) mappedItemProperties.get(tag);
        return new PCDATASectionHandler();
      }
      else
        return new DiscardingSectionHandler();
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (aHandler instanceof PCDATASectionHandler) {
        attributes.put(currentTag, (((PCDATASectionHandler) aHandler).getData()));
      }
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
      item = new RDFResource("rss:item", (String) attributes.get("rss:link"));

      Iterator i = attributes.entrySet().iterator();
      while (i.hasNext()) {
        Map.Entry entry = (Map.Entry) i.next();

        item.set((String) entry.getKey(), entry.getValue());
      }
    };

    public RDFResource getItem() {
      return item;
    }
  }


  private class PCDATASectionHandler extends XMLReader.AbstractSectionHandler {
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
