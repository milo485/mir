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

package mir.rss;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.util.XMLReader;
import mir.util.XMLReaderTool;

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
  public RSSReader() {
  }

  public RSSData parseInputStream(InputStream aStream) throws RSSExc, RSSFailure {
    try {
      XMLReader xmlReader = new XMLReader();
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

  private static class RootSectionHandler extends XMLReader.AbstractSectionHandler {
    private RSSData data;

    public RootSectionHandler(RSSData aData) {
      data = aData;
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (XMLReaderTool.getLocalNameFromQualifiedName(aTag).equals("RDF")) {
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

  private static class RDFSectionHandler extends XMLReader.AbstractSectionHandler {
    private RSSData data;

    public RDFSectionHandler(RSSData aData) {
      data = aData;
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      String identifier = (String) anAttributes.get("rdf:about");

      if (aTag.equals("channel")) {
        if (identifier==null)
          throw new XMLReader.XMLReaderFailure(new RSSExc("Missing rdf:about"));
        else
          return new ChannelSectionHandler(identifier);
      }
      else if (aTag.equals("item")) {
        if (identifier==null)
          throw new XMLReader.XMLReaderFailure(new RSSExc("Missing rdf:about"));
        else
          return new ItemSectionHandler(identifier);
      }
      else
        return new DiscardingSectionHandler();
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (aHandler instanceof ItemSectionHandler) {
        data.addItem(((ItemSectionHandler) aHandler).getItem());
      }
      else if (aHandler instanceof ChannelSectionHandler) {
        data.setChannel(((ChannelSectionHandler) aHandler).getChannel());
      }
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };
  }

  private static class ChannelSectionHandler extends XMLReader.AbstractSectionHandler {
    private String image;
    private String currentTag;
    private RSSChannel channel;

    public ChannelSectionHandler(String anIdentifier) {
      channel = new RSSChannel(anIdentifier);
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      currentTag = aTag;
      if (currentTag.equals("items")) {
        return new ChannelItemsSectionHandler();
      }
      else if (currentTag.equals("description") ||
               currentTag.equals("link") ||
               currentTag.equals("title")) {
        return new PCDATASectionHandler();
      }

      return new DiscardingSectionHandler();
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (currentTag.equals("items")) {
        channel.setItems(((ChannelItemsSectionHandler) aHandler).getItems());
      }
      if (currentTag.equals("description")) {
        channel.setDescription(((PCDATASectionHandler) aHandler).getData());
      }
      else if (currentTag.equals("title")) {
        channel.setTitle(((PCDATASectionHandler) aHandler).getData());
      }
      else if (currentTag.equals("link")) {
        channel.setLink(((PCDATASectionHandler) aHandler).getData());
      }
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };

    public RSSChannel getChannel () {
      return channel;
    }
  }

  private static class ChannelItemsSectionHandler extends XMLReader.AbstractSectionHandler {
    private List items;

    public ChannelItemsSectionHandler() {
      items = new Vector();
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      if (aTag.equals("rdf:Seq"))
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
  }

  private static class ItemSectionHandler extends XMLReader.AbstractSectionHandler {
    private String currentTag;
    private RSSItem item;

    public ItemSectionHandler(String anIdentifier) {
      item = new RSSItem(anIdentifier);
    }

    public XMLReader.SectionHandler startElement(String aTag, Map anAttributes) throws XMLReader.XMLReaderExc {
      currentTag = aTag;

      if (currentTag.equals("description") ||
               currentTag.equals("link") ||
               currentTag.equals("title")) {
        return new PCDATASectionHandler();
      }

      return new DiscardingSectionHandler();
    };

    public void endElement(XMLReader.SectionHandler aHandler) throws XMLReader.XMLReaderExc {
      if (currentTag.equals("description")) {
        item.setDescription(((PCDATASectionHandler) aHandler).getData());
      }
      else if (currentTag.equals("title")) {
        item.setTitle(((PCDATASectionHandler) aHandler).getData());
      }
      else if (currentTag.equals("link")) {
        item.setLink(((PCDATASectionHandler) aHandler).getData());
      }
    };

    public void characters(String aCharacters) throws XMLReader.XMLReaderExc {
      if (aCharacters.trim().length()>0)
        throw new XMLReader.XMLReaderExc("No character data allowed here");
    };

    public void finishSection() throws XMLReader.XMLReaderExc {
    };

    public RSSItem getItem() {
      return item;
    };
  }

  private static class PCDATASectionHandler extends XMLReader.AbstractSectionHandler {
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


  private static class RDFSequenceSectionHandler extends XMLReader.AbstractSectionHandler {
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

  private static class RDFLiteralSectionHandler extends XMLReader.AbstractSectionHandler {
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
  }

  private static class DiscardingSectionHandler extends XMLReader.AbstractSectionHandler {
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
