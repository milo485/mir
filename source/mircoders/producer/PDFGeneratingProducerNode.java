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

package mircoders.producer;

import java.util.*;
import java.io.*;

import mir.log.*;
import mir.util.*;
import mir.producer.*;
import mir.misc.PDFUtil;

public class PDFGeneratingProducerNode implements ProducerNode {
  private String generatorExpression;
  private String destinationExpression;
  private String stylesheet;

  public PDFGeneratingProducerNode(String aGenerator, String aDestination,String aStylesheet) {
    generatorExpression=aGenerator;
    destinationExpression=aDestination;
    stylesheet=aStylesheet;
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) {

    String generatorIdentifier;
    String destinationIdentifier;
    String stylesheetIdentifier;

          long startTime;
          long endTime;

          startTime = System.currentTimeMillis();
    try {

      destinationIdentifier = ParameterExpander.expandExpression( aValueMap, destinationExpression );
      generatorIdentifier = ParameterExpander.expandExpression( aValueMap, generatorExpression );
      stylesheetIdentifier = ParameterExpander.expandExpression( aValueMap, stylesheet);

      aLogger.info("Generating " + generatorIdentifier + " into " + destinationIdentifier + " using "+ stylesheetIdentifier);

      PDFUtil.makePDF(generatorIdentifier,destinationIdentifier,stylesheetIdentifier);

    }
    catch (Throwable t) {
      t.printStackTrace();
      aLogger.error("  error while generating: " + t.getMessage() + t.toString());
    }
    endTime = System.currentTimeMillis();

    aLogger.info("  Time: " + (endTime-startTime) + " ms<br>");
  }
}



