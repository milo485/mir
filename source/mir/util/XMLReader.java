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
package mir.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import multex.Exc;
import multex.Failure;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLReader {
  private Locator locator;
  private String filename;
  private boolean namespaceAware;

  public XMLReader() {
    this(false);
  }

  public XMLReader(boolean aNameSpaceAware) {
    namespaceAware = aNameSpaceAware;
    filename="";
  }

  public void parseFile(String aFileName, SectionHandler aRootHandler) throws XMLReaderFailure, XMLReaderExc {
    filename= aFileName;
    try {
      parseInputStream(new FileInputStream(aFileName), aRootHandler);
    }
    catch (Throwable t) {
      throw new XMLReaderFailure(t);
    }
  }

  public void parseInputStream(InputStream anInputStream, SectionHandler aRootHandler) throws XMLReaderFailure, XMLReaderExc {
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();

      parserFactory.setNamespaceAware(namespaceAware);
      parserFactory.setValidating(true);

      XMLReaderHandler handler = new XMLReaderHandler(parserFactory, aRootHandler);

      handler.processInputStream(anInputStream);
    }
    catch (Throwable e) {
      Throwable t = ExceptionFunctions.traceCauseException(e);

      if (t instanceof XMLReaderExc) {
        if (locator!=null && filename!=null)
          ((XMLReaderExc) t).setLocation(filename, locator.getLineNumber(), locator.getColumnNumber());
        throw (XMLReaderExc) t;
      }

      if (t instanceof XMLReaderFailure) {
        throw (XMLReaderFailure) t;
      }

      throw new XMLReaderFailure(t);
    }
  }

  private class XMLReaderHandler extends DefaultHandler {
    private SAXParserFactory parserFactory;
    private SectionsManager manager;
    private InputSource inputSource;

    public XMLReaderHandler(SAXParserFactory aParserFactory, SectionHandler aRootHandler) {
      super();

      parserFactory=aParserFactory;
      manager = new SectionsManager();
      manager.pushHandler(aRootHandler);
   }

    public void setDocumentLocator(Locator aLocator) {
      locator=aLocator;
    }

    private void processInputStream(InputStream anInputStream) throws XMLReaderExc, XMLReaderFailure {
      try {
        SAXParser parser=parserFactory.newSAXParser();

        inputSource = new InputSource(anInputStream);
        parser.parse(inputSource, this);
      }
      catch (ParserConfigurationException e) {
        throw new XMLReaderExc("Internal exception: "+e.getMessage());
      }
      catch (Throwable e) {
        throw new XMLReaderFailure(e);
      }
    }

    public void startElement(String aUri, String aTag, String aQualifiedName, Attributes anAttributes) throws SAXException {
      Map attributesMap;
      int i;

      try {
        attributesMap = new HashMap();
        for (i=0; i<anAttributes.getLength(); i++)
          attributesMap.put(anAttributes.getQName(i), anAttributes.getValue(i));

        SectionHandler handler = manager.currentHandler().startElement(aQualifiedName, attributesMap);

        manager.pushHandler( handler );
      }
      catch (XMLReaderExc e) {
        throw new SAXParseException(e.getMessage(), null, e);
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
      catch (XMLReaderExc e) {
        throw new SAXParseException(e.getMessage(), null, e);
      }
      catch (Exception e) {
        throw new SAXException(e);
      }
    }

    public void characters(char[] aBuffer, int aStart, int anEnd) throws SAXException {
      try {
        String text = new String(aBuffer, aStart, anEnd);

        manager.currentHandler().characters(text);
      }
      catch (XMLReaderExc e) {
        throw new SAXParseException(e.getMessage(), null, e);
      }
      catch (Exception e) {
        throw new SAXException(e);
      }
    }
  }

  private class SectionsManager {
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

  public static interface SectionHandler {
    public abstract SectionHandler startElement(String aTag, Map anAttributes) throws XMLReaderExc;

    public abstract void endElement(SectionHandler aHandler) throws XMLReaderExc;

    public void characters(String aCharacters) throws XMLReaderExc;

    public void finishSection() throws XMLReaderExc;
  }

  public static abstract class AbstractSectionHandler implements SectionHandler {
    public SectionHandler startElement(String aTag, Map anAttributes) throws XMLReaderExc {
      return null;
    };

    public void endElement(SectionHandler aHandler) throws XMLReaderExc {
    };

    public void finishSection() throws XMLReaderExc {
    }

    public void characters(String aCharacters) throws XMLReaderExc {
      if ( aCharacters.trim().length() > 0) {
        throw new XMLReaderExc("Text not allowed");
      }
    }
  }

  public static class XMLReaderExc extends Exc {
    private boolean hasLocation;
    private String filename;
    private int lineNr;
    private int columnNr;

    public XMLReaderExc(String aMessage) {
      super(aMessage);
      hasLocation = false;
    }

    protected void setLocation(String aFilename, int aLineNr, int aColumnNr) {
      filename = aFilename;
      lineNr = aLineNr;
      columnNr = aColumnNr;
      hasLocation = true;
    }

    public boolean getHasLocation() {
      return hasLocation;
    }

    public int getLineNr() {
      return lineNr;
    }

    public int getColumnNr() {
      return columnNr;
    }

    public String getFilename() {
      return filename;
    }
  }

  public static class XMLReaderFailure extends Failure {
    public XMLReaderFailure(String aMessage, Throwable aCause) {
      super(aMessage, aCause);
    }

    public XMLReaderFailure(Throwable aCause) {
      super(aCause.getMessage(), aCause);
    }
  }

}