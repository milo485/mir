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

package mir.producer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.log.LoggerWrapper;

public class CompositeProducerNode implements ProducerNode {
  private List subNodes;

  public CompositeProducerNode() {
    subNodes = new Vector();
  }

  public CompositeProducerNode(ProducerNode[] aSubNodes) {
    this();

    int i;

    for (i=0; i<aSubNodes.length; i++) {
      addSubNode(aSubNodes[i]);
    }
  }

  public int getNrSubNodes() {
    return subNodes.size();
  }

  public ProducerNode getSubNode(int anIndex) {
    return (ProducerNode) subNodes.get(anIndex);
  }

  public void addSubNode(ProducerNode aSubNode) {
    if (aSubNode!=null)
      subNodes.add(aSubNode);
  }

  public void clear() {
    subNodes.clear();
  }

  protected boolean isAborted(Map aValueMap) {
    Object producerValue = aValueMap.get(NodedProducer.PRODUCER_KEY);
    return (
       (producerValue instanceof NodedProducer) &&
      ((NodedProducer) producerValue).getIsAborted());
  }

  public void produce(Map aValueMap, String aVerb, LoggerWrapper aLogger) throws ProducerFailure, ProducerExc {
    Iterator i = subNodes.iterator();

    while (i.hasNext() && !isAborted(aValueMap)) {
      ProducerNode node = (ProducerNode) i.next();
      node.produce(aValueMap, aVerb, aLogger);
    }
  }
}