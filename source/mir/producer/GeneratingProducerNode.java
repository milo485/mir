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
package mir.producer;

import java.util.Map;

import mir.generator.Generator;
import mir.generator.WriterEngine;
import mir.log.LoggerWrapper;
import mir.util.ParameterExpander;

public class GeneratingProducerNode implements ProducerNode {
  private String generatorExpression;
  private String destinationExpression;
  private String parametersExpression;
  private Generator.GeneratorLibrary generatorLibrary;
  private WriterEngine writerEngine;

  public GeneratingProducerNode(Generator.GeneratorLibrary aGeneratorLibrary, WriterEngine aWriterEngine, String aGenerator, String aDestination, String aParameters) {
    generatorExpression=aGenerator;
    destinationExpression=aDestination;
    parametersExpression=aParameters;
    generatorLibrary = aGeneratorLibrary;
    writerEngine = aWriterEngine;
  }

  public GeneratingProducerNode(Generator.GeneratorLibrary aGeneratorLibrary, WriterEngine aWriterEngine, String aGenerator, String aDestination) {
    this(aGeneratorLibrary, aWriterEngine, aGenerator, aDestination, "");
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure {
    Generator generator;
    Object writer;
    String generatorIdentifier;
    String destinationIdentifier;
    String parameters;

    long startTime;
    long endTime;

    startTime = System.currentTimeMillis();
    try {
      destinationIdentifier = ParameterExpander.expandExpression( aValueMap, destinationExpression );
      generatorIdentifier = ParameterExpander.expandExpression( aValueMap, generatorExpression );
      parameters = ParameterExpander.expandExpression( aValueMap, parametersExpression );

      writer = writerEngine.openWriter( destinationIdentifier, parameters );
      generator = generatorLibrary.makeGenerator( generatorIdentifier );
      generator.generate(writer, aValueMap, aLogger);
      writerEngine.closeWriter( writer );

      endTime = System.currentTimeMillis();
      aLogger.info("Generated " + generatorIdentifier + " into " + destinationIdentifier + " [" + parameters + "] in " + (endTime-startTime) + " ms");
    }
    catch (Throwable t) {
      aLogger.error("  error while generating: " + t.getClass().getName() + ": " + t.getMessage());
    }
  }
}