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
import mir.producer.*;

public class ScriptedProducerFactory implements ProducerFactory {
  private List verbs;
  private Map verbNodes;
  private ProducerNode body;
  private String defaultVerb;
  private String name;

  public ScriptedProducerFactory(String aName, List aVerbs, Map aVerbNodes, ProducerNode aBody, String aDefaultVerb) {
    verbs = aVerbs;
    verbNodes = aVerbNodes;
    body = aBody;
    defaultVerb = aDefaultVerb;
    name = aName;
  }

  public String getName() {
    return name;
  }

  public ScriptedProducerFactory(String aName, List aVerbs, Map aVerbNodes, ProducerNode aBody) {
    this(aName, aVerbs, aVerbNodes, aBody, null);
  }

  public Producer makeProducer(String aVerb, Map aStartingValues) throws ProducerFailure, ProducerExc {
    CompositeProducerNode rootNode;
    ProducerNode verbNode;

    if (verbNodes.containsKey(aVerb)) {
      verbNode = (ProducerNode) verbNodes.get(aVerb);
    }
    else if (defaultVerb!=null && verbNodes.containsKey(defaultVerb)) {
      verbNode = (ProducerNode) verbNodes.get(defaultVerb);
    }
    else throw new ProducerExc("Undefined verb: " + aVerb + " (and no default verb was defined)");

    rootNode = new CompositeProducerNode();
    rootNode.addSubNode(verbNode);
    rootNode.addSubNode(body);

    return new NodedProducer(rootNode, aVerb, aStartingValues);
  };

  public Iterator verbs() {
    return verbs.iterator();
  }
}