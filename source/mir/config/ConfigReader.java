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
package mir.config;

import mir.config.exceptions.ConfigDefineNotKnownException;
import mir.config.exceptions.ConfigFailure;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class ConfigReader {
  final static String propertyTagName = "property";
  final static String propertyNameAttribute = "name";
  final static String propertyValueAttribute = "value";
  final static String defineTagName = "define";
  final static String defineNameAttribute = "name";
  final static String defineValueAttribute = "value";
  final static String includeTagName = "include";
  final static String includeFileAttribute = "file";

  public ConfigReader() {
    super();
  }

  public void parseFile(String aFileName, ConfigNodeBuilder aRootNode)
    throws ConfigFailure {
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();

      parserFactory.setNamespaceAware(false);
      parserFactory.setValidating(true);

      ConfigReaderHandler handler =
        new ConfigReaderHandler(aRootNode, parserFactory);

      handler.includeFile(aFileName);
    } catch (Throwable e) {
      if (e instanceof SAXParseException &&
          ((SAXParseException) e).getException() instanceof ConfigFailure) {
        throw (ConfigFailure) ((SAXParseException) e).getException();
      } else {
        e.printStackTrace();
        throw new ConfigFailure(e.getMessage());
      }
    }
  }

  private class ConfigReaderHandler extends DefaultHandler {
    ConfigNodeBuilder builder;
    Stack nodeStack;
    Locator locator;
    DefinesManager definesManager;
    int level;
    Stack includeFileStack;
    SAXParserFactory parserFactory;

    public ConfigReaderHandler(ConfigNodeBuilder aBuilder,
      SAXParserFactory aParserFactory) {
      super();

      builder = aBuilder;
      nodeStack = new Stack();
      includeFileStack = new Stack();
      definesManager = new DefinesManager();
      parserFactory = aParserFactory;
      level = 0;
    }

    public String getLocatorDescription(Locator aLocator) {
      return aLocator.getPublicId() + " (" + aLocator.getLineNumber() + ")";
    }

    public void setDocumentLocator(Locator aLocator) {
      locator = aLocator;
    }

    private void includeFile(String aFileName)
      throws ConfigFailure, SAXParseException, SAXException {
      File file;
      SAXParser parser;
      InputSource inputSource;
      System.err.println("about to include " + aFileName);

      try {
        if (!includeFileStack.empty()) {
          file =
            new File(new File((String) includeFileStack.peek()).getParent(),
              aFileName);
        } else {
          file = new File(aFileName);
        }

        System.err.println("about to include " + file.getCanonicalPath());

        if (includeFileStack.contains(file.getCanonicalPath())) {
          throw new ConfigFailure("recursive inclusion of file " +
            file.getCanonicalPath(), getLocatorDescription(locator));
        }

        parser = parserFactory.newSAXParser();

        inputSource = new InputSource(new FileInputStream(file));
        inputSource.setPublicId(file.getCanonicalPath());

        includeFileStack.push(file.getCanonicalPath());

        try {
          parser.parse(inputSource, this);
        } finally {
          includeFileStack.pop();
        }
      } catch (ParserConfigurationException e) {
        throw new ConfigFailure("Internal exception while including \"" +
          aFileName + "\": " + e.getMessage(), e, getLocatorDescription(locator));
      } catch (SAXParseException e) {
        throw e;
      } catch (ConfigFailure e) {
        throw e;
      } catch (FileNotFoundException e) {
        throw new ConfigFailure("Include file \"" + aFileName +
          "\" not found: " + e.getMessage(), e, getLocatorDescription(locator));
      } catch (IOException e) {
        throw new ConfigFailure("unable to open include file \"" + aFileName +
          "\": " + e.getMessage(), e, getLocatorDescription(locator));
      }
    }

    public void startElement(String aUri, String aTag, String aQualifiedName,
      Attributes anAttributes) throws SAXException {
      nodeStack.push(builder);
      level++;

      try {
        if (builder == null) {
          throw new ConfigFailure("define, include and property tags cannot have content",
            getLocatorDescription(locator));
        }

        if (aQualifiedName.equals(propertyTagName)) {
          String name = anAttributes.getValue(propertyNameAttribute);
          String value = anAttributes.getValue(propertyValueAttribute);

          if (name == null) {
            throw new ConfigFailure("property has no name attribute",
              getLocatorDescription(locator));
          } else if (value == null) {
            throw new ConfigFailure("property \"" + name +
              "\" has no value attribute", getLocatorDescription(locator));
          }

          builder.addProperty(name,
            definesManager.resolve(value, getLocatorDescription(locator)),
            value, getLocatorDescription(locator));
          builder = null;
        } else if (aQualifiedName.equals(defineTagName)) {
          String name = anAttributes.getValue(defineNameAttribute);
          String value = anAttributes.getValue(defineValueAttribute);

          if (name == null) {
            throw new ConfigFailure("define has no name attribute",
              getLocatorDescription(locator));
          } else if (value == null) {
            throw new ConfigFailure("define \"" + name +
              "\" has no value attribute", getLocatorDescription(locator));
          }

          definesManager.addDefine(name,
            definesManager.resolve(value, getLocatorDescription(locator)));
          builder = null;
        } else if (aQualifiedName.equals(includeTagName)) {
          String fileName = anAttributes.getValue(includeFileAttribute);

          if (fileName == null) {
            throw new ConfigFailure("include has no file attribute",
              getLocatorDescription(locator));
          }

          includeFile(definesManager.resolve(fileName,
              getLocatorDescription(locator)));
          builder = null;
        } else {
          builder =
            builder.makeSubNode(aQualifiedName, getLocatorDescription(locator));
        }
      } catch (ConfigFailure e) {
        throw new SAXParseException(e.getMessage(), locator, e);
      }
    }

    public void endElement(String aUri, String aTag, String aQualifiedName)
      throws SAXParseException {
      builder = (ConfigNodeBuilder) nodeStack.pop();
      level--;
    }

    public void characters(char[] aBuffer, int aStart, int anEnd)
      throws SAXParseException {
      String text = new String(aBuffer, aStart, anEnd).trim();

      if (text.length() > 0) {
        throw new SAXParseException("Text not allowed", locator,
          new ConfigFailure("text not allowed", getLocatorDescription(locator)));
      }
    }
  }

  private class DefinesManager {
    Map defines;

    public DefinesManager() {
      defines = new HashMap();
    }

    public void addDefine(String aName, String anExpression) {
      defines.put(aName, anExpression);
    }

    public String resolve(String anExpression, String aLocation)
      throws ConfigFailure {
      int previousPosition = 0;
      int position;
      int endOfNamePosition;
      String name;

      StringBuffer result = new StringBuffer();

      while ((position = anExpression.indexOf("$", previousPosition)) >= 0) {
        result.append(anExpression.substring(previousPosition, position));

        if (position >= (anExpression.length() - 1)) {
          result.append(anExpression.substring(position, anExpression.length()));
          previousPosition = anExpression.length();
        } else {
          if (anExpression.charAt(position + 1) == '{') {
            endOfNamePosition = anExpression.indexOf('}', position);

            if (endOfNamePosition >= 0) {
              name = anExpression.substring(position + 2, endOfNamePosition);

              if (defines.containsKey(name)) {
                result.append((String) defines.get(name));
                previousPosition = endOfNamePosition + 1;
              } else {
                throw new ConfigDefineNotKnownException("Variable \"" + name +
                  "\" not defined", aLocation);
              }
            } else {
              throw new ConfigFailure("Missing }", aLocation);
            }
          } else {
            previousPosition = position + 2;
            result.append(anExpression.charAt(position + 1));
          }
        }
      }

      result.append(anExpression.substring(previousPosition,
          anExpression.length()));

      return result.toString();
    }
  }
}
