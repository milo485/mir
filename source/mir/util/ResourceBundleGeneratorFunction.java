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

package mir.util;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import mir.generator.Generator;
import mir.generator.GeneratorExc;

import org.apache.struts.util.MessageResources;

public class ResourceBundleGeneratorFunction implements Generator.GeneratorFunction {
  private List messages;
  private Locale locale;

  public ResourceBundleGeneratorFunction(Locale aLocale, MessageResources aMessages) {
    this(aLocale, new MessageResources[] {aMessages} );
  }

  public ResourceBundleGeneratorFunction(Locale aLocale, MessageResources aMessages1, MessageResources aMessages2) {
    this(aLocale, new MessageResources[] {aMessages1, aMessages2} );
  }

  public ResourceBundleGeneratorFunction(Locale aLocale, MessageResources[] aMessages) {
    locale = aLocale;
    messages = new Vector();

    for(int i=0; i<aMessages.length; i++) {
      this.messages.add(aMessages[i]);
    }
  }

  public Object perform(List aParameters) throws GeneratorExc {
    List extraParameters = new Vector(aParameters);

    if (aParameters.size()<1)
      throw new GeneratorExc("ResourceBundleGeneratorFunction: at least 1 parameter expected");

    if (!(aParameters.get(0) instanceof String))
      throw new GeneratorExc("encodeHTMLGeneratorFunction: parameters must be strings");

    String key = (String) aParameters.get(0);
    extraParameters.remove(0);

    String message=null;
    Iterator i = messages.iterator();
    while (i.hasNext() && message==null)
      message = ((MessageResources) i.next()).getMessage(locale, key, extraParameters.toArray());

    if (message == null) {
      return new String("??" + key + "??");
    }
    else {
      return message;
    }
  };
}