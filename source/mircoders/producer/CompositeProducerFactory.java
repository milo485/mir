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
import mir.entity.*;
import mir.producer.*;

public class CompositeProducerFactory implements ProducerFactory {
  private Map factories;          // verb -> Vector ( ProducerFactory )
  private List verbs;
  private String name;

  public CompositeProducerFactory(String aName) {
    factories = new HashMap();
    verbs = new Vector();
    name = aName;
  }

  public CompositeProducerFactory(String aName, ProducerFactory[] aSubProducerFactories) {
    this(aName);

    int i;

    for (i=0; i<aSubProducerFactories.length; i++) {
      addFactory(aSubProducerFactories[i]);
    }
  }

  private List findFactoriesForVerb(String aVerb) {
    List result = (List) factories.get(aVerb);

    if (result==null)
      result = new Vector();

    return result;
  }

  private List factoriesForVerb(ProducerVerb aVerb) {
    List result;

    result=(List) factories.get(aVerb.getName());

    if (result==null) {
      result=new Vector();

      verbs.add(aVerb);
      factories.put(aVerb.getName(), result);
    }

    return result;
  }

  public void addFactory(ProducerFactory aFactory) {
    Iterator i;

    i=aFactory.verbs();

    while (i.hasNext()) {
      factoriesForVerb((ProducerVerb) i.next()).add(aFactory);
    }
  }

  public mir.producer.Producer makeProducer(String aVerb, Map aBasicValueSet) throws ProducerExc, ProducerFailure {
    CompositeProducer result = new CompositeProducer();

    Iterator i=findFactoriesForVerb(aVerb).iterator();

    while (i.hasNext())
      result.addProducer(((ProducerFactory) i.next()).makeProducer(aVerb, aBasicValueSet));

    return result;
  }

  public Iterator verbs() {
    return verbs.iterator();
  }

  public String getName() {
    return name;
  }
}
