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

import java.util.*;
import java.net.*;

//import mir.misc.*;
import mir.generator.*;

public class GeneratorListFunctions {
  private GeneratorListFunctions() {}

  public static class subListFunction implements Generator.GeneratorFunction {
    public Object perform(List aParameters) throws GeneratorExc, GeneratorFailure {
      try {
        int skip;
        int maxSize;

        if (aParameters.size()>3 || aParameters.size()<2)
          throw new GeneratorExc("iteratorSubsetFunction: 2 or 3 parameters expected");
        if (aParameters.get(0)==null)
          return "";

        if (!(aParameters.get(0) instanceof RewindableIterator) && !(aParameters.get(0) instanceof List))
          throw new GeneratorExc("iteratorSubsetFunction: first parameter must be a RewindableIterator (not a "+aParameters.get(0).getClass().getName()+")");

        skip = StringRoutines.interpretAsInteger(aParameters.get(1));
        if (aParameters.size()>=2)
          maxSize = StringRoutines.interpretAsInteger(aParameters.get(2));
        else
          maxSize = -1;


        if ((aParameters.get(0) instanceof RewindableIterator))
          return new SubsetIterator((RewindableIterator) aParameters.get(0), skip, maxSize);
        else {
          List list = (List) aParameters.get(0);

          if (skip>=list.size())
            return new Vector();
          if (maxSize<0 || (skip+maxSize)>=list.size())
            return list.subList(skip, list.size());
          else
            return list.subList(skip, skip+maxSize);
        }
      }
      catch (GeneratorExc e) {
        throw e;
      }
      catch (GeneratorFailure e) {
        throw e;
      }
      catch (Throwable e) {
        throw new GeneratorFailure(e);
      }
    };
  }
}
