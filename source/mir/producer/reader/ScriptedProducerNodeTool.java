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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ScriptedProducerNodeTool {

  public static Object getOrMakeMapValueForKey(Map aMap, Object aKey, Object aDefaultValue) {
    if (aMap.containsKey(aKey))
      return aMap.get(aKey);
    else {
      aMap.put(aKey, aDefaultValue);
      return aDefaultValue;
    }
  }

  public static Stack getRunTimeStack(Map aProductionMap, String aDefinitionName) {
    Map runtimeData = (Map) getOrMakeMapValueForKey(aProductionMap, ScriptedProducerNodeDefinition.SCRIPTED_PRODUCERNODE_RUNTIMEDATA_KEY, new HashMap());
    runtimeData = (Map) getOrMakeMapValueForKey(runtimeData, aDefinitionName, new HashMap());
    return (Stack) getOrMakeMapValueForKey(runtimeData, ScriptedProducerNodeDefinition.SCRIPTED_PRODUCERNODE_RUNTIMESTACK_KEY, new Stack());
  }

  public static void pushNodeParameterValues(Map aProductionMap, String aDefinitionName, Map aNodeParameterValues) {
    Stack runtimeStack = getRunTimeStack(aProductionMap, aDefinitionName);
    runtimeStack.push(aNodeParameterValues);
  }

  public static void popNodeParameterValues(Map aProductionMap, String aDefinitionName) {
    Stack runtimeStack = getRunTimeStack(aProductionMap, aDefinitionName);
    runtimeStack.pop();
  }

  public static void saveMapValues(Map aDestination, Map aSource, Set aKeys) {
    Iterator i = aKeys.iterator();

    while (i.hasNext()) {
      Object key = i.next();
      if (aSource.containsKey(key))
        aDestination.put(key, aSource.get(key));
    }
  }

  public static void restoreMapValues(Map aMap, Set aKeys, Map aSavedValues) {
    Iterator i = aKeys.iterator();

    while (i.hasNext()) {
      Object key = i.next();
      if (aSavedValues.containsKey(key))
        aMap.put(key, aSavedValues.get(key));
      else
        aMap.remove(key);
    }
  }
}