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
package mir.generator;

import java.util.HashMap;
import java.util.Map;

import mir.log.LoggerWrapper;
import mir.util.SimpleParser;

public class GeneratorLibraryRepository {
  private Map factories;
  private LoggerWrapper logger;

  public GeneratorLibraryRepository() {
    factories = new HashMap();
    logger = new LoggerWrapper("Generator");
  }

  public void registerLibraryFactory(String aName, Generator.GeneratorLibraryFactory aFactory) {
    factories.put(aName, aFactory);
  }

  public Generator.GeneratorLibrary constructLibrary(String aName, String aParameters) throws GeneratorExc {
    if (!factories.containsKey(aName))
      throw new GeneratorExc("Unknown library factory: "+aName);

    return ((Generator.GeneratorLibraryFactory) factories.get(aName)).makeLibrary(aParameters);
  }

  private final static String SPACE = "[\t\n\r ]*";
  private final static String IDENTIFIER = "[a-zA-Z_][a-zA-Z0-9_]*";
  private final static String EQUALS = "=";
  private final static String LEFT_PARENTHESIS = "[(]";
  private final static String RIGHT_PARENTHESIS = "[)]";
  private final static String FACTORY_PARAMETERS = "[^)]*";
  private final static String SEMICOLON = ";";

  public Generator.GeneratorLibrary constructCompositeLibrary(String aSpecification) throws GeneratorExc, GeneratorFailure {
    String identifier;
    String factory;
    String factoryParameters;
    CompositeGeneratorLibrary result = new CompositeGeneratorLibrary();
    boolean first=true;

    SimpleParser parser = new SimpleParser(aSpecification);
    try {
      parser.skip(SPACE);
      while (!parser.isAtEnd()) {
        identifier = parser.parse(IDENTIFIER, "library key expected");
        parser.skip(SPACE);
        parser.parse(EQUALS, "'=' expected");
        parser.skip(SPACE);
        factory = parser.parse(IDENTIFIER, "factory name expected");
        parser.skip(SPACE);
        parser.parse(LEFT_PARENTHESIS, "'(' expected");
        factoryParameters = parser.parse(FACTORY_PARAMETERS, "parameters expected");
        parser.parse(RIGHT_PARENTHESIS, "')' expected");

        result.addLibrary(identifier, constructLibrary(factory, factoryParameters), first);
        first=false;
        parser.skip(SPACE);

        if (!parser.isAtEnd()) {
          parser.parse(SEMICOLON, "; expected");
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace(logger.asPrintWriter(LoggerWrapper.DEBUG_MESSAGE));
      throw new GeneratorFailure("Failed to construct generator library: " + e.getMessage(), e);
    }

    return result;
  }
}