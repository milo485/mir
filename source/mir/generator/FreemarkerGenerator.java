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

package mir.generator;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.misc.MessageMethodModel;
import mir.util.RewindableIterator;

import org.apache.struts.util.MessageResources;

import freemarker.template.FileTemplateCache;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateListModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelRoot;
import freemarker.template.TemplateScalarModel;

public class FreemarkerGenerator implements Generator {
  private Template template;

  public FreemarkerGenerator(Template aTemplate) {
    template = aTemplate;
  }

  public void generate(Object anOutputWriter, Map aValues, PrintWriter aLogger) throws GeneratorExc, GeneratorFailure {
    if (!(anOutputWriter instanceof PrintWriter))
      throw new GeneratorExc("Writer for a FreemarkerGenerator must be a PrintWriter");

    try {
      template.process((TemplateModelRoot) makeMapAdapter(aValues), (PrintWriter) anOutputWriter);
    }
    catch (Throwable t) {
      aLogger.println("Exception occurred: "+t.getMessage());
      t.printStackTrace(aLogger);
      throw new GeneratorFailure( t );
    }
  }

  private static TemplateScalarModel makeStringAdapter(String aString) {
    return new SimpleScalar(aString);
  }

  private static TemplateHashModel makeMapAdapter(Map aMap)  {
    return new MapAdapter(aMap);
  }

  private static TemplateListModel makeIteratorAdapter(Iterator anIterator) {
    return new IteratorAdapter(anIterator);
  }

  private static TemplateMethodModel makeFunctionAdapter(Generator.GeneratorFunction aFunction) {
    return new FunctionAdapter(aFunction);
  }

  public static TemplateModel makeAdapter(Object anObject) throws TemplateModelException {
    if (anObject == null)
      return null;
    if (anObject instanceof TemplateModel)
      return (TemplateModel) anObject;
    else if (anObject instanceof Generator.GeneratorFunction)
      return makeFunctionAdapter((Generator.GeneratorFunction) anObject);
    else if (anObject instanceof MessageResources)
      return new MessageMethodModel((MessageResources) anObject);
    else if (anObject instanceof Integer)
      return makeStringAdapter(((Integer) anObject).toString());
    else if (anObject instanceof String)
      return makeStringAdapter((String) anObject);
    else if (anObject instanceof Map)
      return makeMapAdapter((Map) anObject);
    else if (anObject instanceof Iterator)
      return makeIteratorAdapter((Iterator) anObject);
    else if (anObject instanceof List)
      return makeIteratorAdapter(((List) anObject).iterator());
    else
      throw new TemplateModelException("Unadaptable class: " + anObject.getClass().getName());
  }

  private static class MapAdapter implements TemplateModelRoot {
    Map map;
    Map valuesCache;

    private MapAdapter(Map aMap) {
      map = aMap;
      valuesCache = new HashMap();
    }

    public void put(String aKey, TemplateModel aModel) {
      valuesCache.put(aKey, aModel);
    }

    public void remove(String aKey) {
      // ML: kinda tricky...
    }

    public boolean isEmpty() {
      return map.isEmpty();
    }

    public TemplateModel get(String aKey) throws TemplateModelException {
      try {
        if (!valuesCache.containsKey(aKey)) {
          Object value = map.get(aKey);

          if (value == null && !map.containsKey(aKey)) {
            throw new TemplateModelException("MapAdapter: no key "+aKey+" available");
          }

          valuesCache.put(aKey, makeAdapter(value));
        }

        return (TemplateModel) valuesCache.get(aKey);
      }
      catch (TemplateModelException e) {
        throw e;
      }
      catch (Throwable t) {
        throw new TemplateModelException(t.getMessage());
      }
    }
  }

  private static class IteratorAdapter implements TemplateListModel {
    Iterator iterator;
    List valuesCache;
    int position;

    private IteratorAdapter(Iterator anIterator) {
      iterator = anIterator;

      valuesCache = new Vector();
      position=0;


      if (iterator instanceof RewindableIterator) {
        ((RewindableIterator) iterator).rewind();
      }
    }

    public boolean isEmpty() {
      return valuesCache.isEmpty() && !iterator.hasNext();
    }

    private void getUntil(int anIndex) throws TemplateModelException {
      while (valuesCache.size()<=anIndex && iterator.hasNext())
      {
        valuesCache.add(makeAdapter(iterator.next()));
      }
    };

    public TemplateModel get(int anIndex) throws TemplateModelException {
      TemplateModel result;

      getUntil(anIndex);

      if (anIndex<valuesCache.size())
      {
        result = (TemplateModel) valuesCache.get(anIndex);

        return result;
      }
      else
        throw new TemplateModelException( "Iterator out of bounds" );
    }

    public boolean hasNext() {
      return position<valuesCache.size() || iterator.hasNext();
    }

    public boolean isRewound() {
      return position==0;
    }

    public TemplateModel next() throws TemplateModelException {
      TemplateModel result;

      if (hasNext()) {
        result = get(position);
        position++;
      }
      else
        throw new TemplateModelException( "Iterator out of bounds" );

      return result;
    }

    public void rewind() {
      position=0;
    }
  }

  private static class ListAdapter implements TemplateListModel {
    List list;
    List valuesCache;
    int position;

    private ListAdapter(List aList) {
      list = aList;
      valuesCache = new Vector();
      position=0;
    }

    public boolean isEmpty() {
      return list.isEmpty();
    }

    public TemplateModel get(int i) throws TemplateModelException {

      if (i>=valuesCache.size() && i<list.size()) {
        for(int j=valuesCache.size(); j<=i; j++) {
          valuesCache.add(makeAdapter(list.get(j)));
        }
      }

      if (i<valuesCache.size())
        return (TemplateModel) valuesCache.get(i);
      else
        throw new TemplateModelException( "Iterator out of bounds" );
    }

    public boolean hasNext() {
      return position<list.size();
    }

    public boolean isRewound() {
      return position==0;
    }

    public TemplateModel next() throws TemplateModelException {
      TemplateModel result;

      if (hasNext()) {
        result = get(position);
        position++;
      }
      else {
        throw new TemplateModelException( "Iterator out of bounds" );
      }

      return result;
    }

    public void rewind() {
      position = 0;
    }
  }

  private static class FunctionAdapter implements TemplateMethodModel {
    Generator.GeneratorFunction function;

    public FunctionAdapter(Generator.GeneratorFunction aFunction) {
      function = aFunction;
    }

    public TemplateModel exec(List anArguments) throws TemplateModelException {
      try {
        return makeAdapter(function.perform(anArguments));
      }
      catch (Throwable t) {
        throw new TemplateModelException(t.getMessage());
      }
    }

    public boolean isEmpty() {
      return false;
    }

  }

  public static class FreemarkerGeneratorLibrary implements GeneratorLibrary {
    private FileTemplateCache templateCache;

    public FreemarkerGeneratorLibrary(String aTemplateRoot) {
      templateCache = new FileTemplateCache( aTemplateRoot+"/" );
      templateCache.setLoadingPolicy(FileTemplateCache.LOAD_ON_DEMAND);
    }

    public Generator makeGenerator(String anIdentifier) throws GeneratorExc, GeneratorFailure {
      Template template = (Template) templateCache.getItem(anIdentifier, "template");

      if (template==null) {
        throw new GeneratorExc("FreemarkerGeneratorLibrary: Can't find template "+templateCache.getDirectory()+anIdentifier);
      }

      return new FreemarkerGenerator(template);
    }
  }

  public static class FreemarkerGeneratorLibraryFactory implements GeneratorLibraryFactory {
    private String basePath;

    public FreemarkerGeneratorLibraryFactory(String aBasePath) {
      basePath = aBasePath;
    }

    public GeneratorLibrary makeLibrary(String anInitializationString) {
      return new FreemarkerGeneratorLibrary(basePath+anInitializationString);
    };
  }
}
