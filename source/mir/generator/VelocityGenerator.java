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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.AbstractList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;
import mir.log.LoggerWrapper;
import mir.util.GeneratorFormatAdapters;
import mir.util.RewindableIterator;

public class VelocityGenerator implements Generator {
  private String templateIdentifier;
  private VelocityGeneratorLibrary library;
  private static LoggerWrapper logger = new LoggerWrapper("Generator.velocity");

  public VelocityGenerator(String aTemplate, VelocityGeneratorLibrary aLibrary) {
    templateIdentifier = aTemplate;
    library = aLibrary;
  }

  public void generate(Object anOutputWriter, Map aValues, LoggerWrapper aLogger) throws GeneratorExc, GeneratorFailure {
    Template template;
    Context context = makeMapAdapter(aValues);
    StringWriter stringWriter = new StringWriter();

    try {
      template = library.engine.getTemplate(templateIdentifier);
      if (template == null) {
        throw new GeneratorExc("VelocityGeneratorLibrary: Can't find template " + templateIdentifier);
      }
      template.merge(context, stringWriter);

      ( (PrintWriter) anOutputWriter).print(stringWriter.toString());
    }
    catch (ResourceNotFoundException t) {
      throw new GeneratorExc("VelocityGeneratorLibrary: Can't find template " + templateIdentifier);
    }
    catch (ParseErrorException t) {
      ( (PrintWriter) anOutputWriter).print(t.toString());
    }
    catch (Throwable t) {
      throw new GeneratorFailure(t);
    }

  }

  private static class ContextAdapter implements Context {
    public boolean containsKey(java.lang.Object key) {
      return false;
    }

    public Object get(java.lang.String key) {
      return null;
    }

    public Object[] getKeys() {
      return new Object[] {};
    }

    public Object put(java.lang.String key, java.lang.Object value) {
      return null;
    }

    public Object remove(java.lang.Object key) {
      return null;
    }
  }

  private static Context makeMapAdapter(Map aMap)  {
    return new MapAdapter(aMap);
  }

  private static List makeIteratorAdapter(Iterator anIterator) {
    return new IteratorAdapter(anIterator);
  }

  private static List makeListAdapter(List aList) {
    return new ListAdapter(aList);
  }


  private static Object makeFunctionAdapter(Generator.GeneratorFunction aFunction) {
    return new FunctionAdapter(aFunction);
  }

  private static Object makeBeanAdapter(Object anObject)  {
    return new BeanAdapter(anObject);
  }

  private interface VelocityAdapter {
    public Object getOriginal();
  }

  public static Object unmakeAdapter(Object anObject) {
    if (anObject instanceof VelocityAdapter) {
      return ((VelocityAdapter) anObject).getOriginal();
    }
    else
      return anObject;
  }

  public static Object makeAdapter(Object anObject) {
    if (anObject == null)
      return null;

    if (anObject instanceof Context)
      return anObject;

    else if (anObject instanceof Generator.GeneratorFunction)
      return makeFunctionAdapter((Generator.GeneratorFunction) anObject);
    else if (anObject instanceof Integer)
      return anObject;
    else if (anObject instanceof Boolean)
      return anObject;
    else if (anObject instanceof String)
      return anObject;
    else if (anObject instanceof Map)
      return makeMapAdapter((Map) anObject);
    else if (anObject instanceof Iterator)
      return makeIteratorAdapter((Iterator) anObject);
    else if (anObject instanceof List)
      return makeListAdapter(((List) anObject));
    else if (anObject instanceof Number)
      return makeAdapter(new GeneratorFormatAdapters.NumberFormatAdapter((Number) anObject));
    else if (anObject instanceof Date)
      return makeAdapter(new GeneratorFormatAdapters.DateFormatAdapter((Date) anObject));
    else
      return makeBeanAdapter(anObject);
  }

  public static class FunctionAdapter implements VelocityAdapter {
    private GeneratorFunction function;

    public Object getOriginal() {
      return function;
    }

    private FunctionAdapter(GeneratorFunction aFunction) {
      function = aFunction;
    }

    public Object call(Object aParameters[]) throws GeneratorExc {
      List parameters = new Vector();

      for (int i = 0; i<aParameters.length; i++) {
        parameters.add(unmakeAdapter(aParameters[i]));
      }

      return makeAdapter(function.perform(parameters));
    }

    public Object call() throws GeneratorExc {
      return makeAdapter(function.perform(new Vector()));
    }

    public Object call(Object anObject) throws GeneratorExc {
      return call(new Object[] { anObject });
    }

    public Object call(Object anObject1, Object anObject2) throws GeneratorExc {
      return call(new Object[] { anObject1, anObject2 });
    }

    public Object call(Object anObject1, Object anObject2, Object anObject3) throws GeneratorExc {
      return call(new Object[] { anObject1, anObject2, anObject3 });
    }

    public Object call(Object anObject1, Object anObject2, Object anObject3, Object anObject4) throws GeneratorExc {
      return call(new Object[] { anObject1, anObject2, anObject3, anObject4 });
    }
  }


  private static class MapAdapter implements Context, VelocityAdapter  {
    private Map map;
    private Map valuesCache;

    private MapAdapter(Map aMap) {
      map = aMap;
      valuesCache = new HashMap();
    }

    public Object getOriginal() {
      return map;
    }

    public boolean containsKey(Object aKey) {
      return map.containsKey(aKey);
    }

    public Object get(String aKey) {
      try {
        if (!valuesCache.containsKey(aKey)) {
          Object value = map.get(aKey);

          if (value == null && !map.containsKey(aKey)) {
            return "no key "+aKey+" available";
          }
          else
            valuesCache.put(aKey, makeAdapter(value));
        }

        return valuesCache.get(aKey);
      }
      catch (Throwable t) {
        throw new GeneratorFailure(t);
      }
    }

    public Object[] getKeys() {
      return new Object[] {};
    }

    public Object put(String aKey, Object aValue) {
      valuesCache.remove(aKey);
      map.put(aKey, unmakeAdapter(aValue));

      return aValue;
    }

    public Object remove(java.lang.Object key) {
      return null;
    }
  }

  private static class IteratorAdapter extends AbstractList implements VelocityAdapter  {
    private Iterator iterator;
    private List valuesCache;
    private int position;

    private IteratorAdapter(Iterator anIterator) {
      iterator = anIterator;

      valuesCache = new Vector();
      position=0;


      if (iterator instanceof RewindableIterator) {
        ((RewindableIterator) iterator).rewind();
      }
    }

    private void getUntil(int anIndex) {
      while ((anIndex==-1 || valuesCache.size()<=anIndex) && iterator.hasNext())
      {
        valuesCache.add(makeAdapter(iterator.next()));
      }
    };

    public Object getOriginal() {
      return iterator;
    }

    public Object get(int anIndex) {
      Object result;

      getUntil(anIndex);

      if (anIndex<valuesCache.size())
      {
        result = valuesCache.get(anIndex);

        return result;
      }
      else
        throw new RuntimeException( "Iterator out of bounds" );
    }

    public int size() {
      getUntil(-1);
      return valuesCache.size();
    }

  }

  private static class ListAdapter extends AbstractList implements VelocityAdapter  {
    private List list;
    private List valuesCache;
    private int position;

    private ListAdapter(List aList) {
      list = aList;

      valuesCache = new Vector();
      position=0;
    }

    private void getUntil(int anIndex) {
      while ((anIndex==-1 || valuesCache.size()<=anIndex) && valuesCache.size()<list.size())
      {
        valuesCache.add(makeAdapter(list.get(valuesCache.size())));
      }
    };

    public Object getOriginal() {
      return list;
    }

    public Object get(int anIndex) {
      Object result;

      getUntil(anIndex);

      if (anIndex<valuesCache.size())
      {
        result = valuesCache.get(anIndex);

        return result;
      }
      else
        throw new RuntimeException( "Iterator out of bounds" );
    }

    public int size() {
      return list.size();
    }

  }

/*
  private static class FunctionAdapter implements TemplateMethodModel {
    private Generator.GeneratorFunction function;

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

*/

  private static class BeanAdapter implements Context, VelocityAdapter {
    private Object object;

    public BeanAdapter(Object anObject) {
      object = anObject;
    }

    public boolean containsKey(Object key) {
      return true;
    }

    public Object getOriginal() {
      return object;
    }

    public Object get(String aKey) {
      try {
        if (PropertyUtils.isReadable(object, aKey))
          return makeAdapter(PropertyUtils.getSimpleProperty(object, aKey));
        else
          return makeAdapter(MethodUtils.invokeExactMethod(object, "get", aKey));
      }
      catch (Throwable t) {
        throw new GeneratorFailure(t);
      }
    }

    public Object[] getKeys() {
      return new Object[] {};
    }

    public Object put(String aKey, Object aValue) {
      try {
        if (PropertyUtils.isWriteable(object, aKey))
          PropertyUtils.setSimpleProperty(object, aKey, unmakeAdapter(aValue));
        else
          MethodUtils.invokeExactMethod(object, "set", new Object[] {aKey, unmakeAdapter(aValue)});

        return this;
      }
      catch (Throwable t) {
        throw new GeneratorFailure(t);
      }
    }

    public Object remove(Object aKey) {
      throw new RuntimeException("BeanAdapter.remove not supported");
    }
  }

  private static class VelocityLoggerWrapper implements LogSystem {
    private LoggerWrapper logger;

    public VelocityLoggerWrapper(LoggerWrapper aLogger) {
      logger = aLogger;
    }

    public void init(RuntimeServices aRuntimeServices) {
    }

    public void logVelocityMessage(int aLevel, String aMessage) {
      switch (aLevel) {
        case DEBUG_ID:
          logger.debug(aMessage);
          break;
        case ERROR_ID:
          logger.error(aMessage);
          break;
        case INFO_ID:
          logger.info(aMessage);
          break;
        default:
          logger.warn(aMessage);
          break;
      }
    }
  }

  public static class VelocityGeneratorLibrary implements GeneratorLibrary {
    private VelocityEngine engine;

    public VelocityGeneratorLibrary(String aTemplateRoot) throws GeneratorExc, GeneratorFailure {
      try {
        engine = new VelocityEngine();
        try {
          engine.setProperty(VelocityEngine.RESOURCE_LOADER, "file");
        }
        catch (Throwable t) {
          logger.error(VelocityEngine.RESOURCE_LOADER);
        }

        try {
          engine.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        }
        catch (Throwable t) {
          logger.error("file.resource.loader.class");
        }

        try {
          engine.setProperty("file.resource.loader.path", aTemplateRoot);
        }
        catch (Throwable t) {
          logger.error("file.resource.loader.path");

        }
        try {
          engine.setProperty("file.resource.loader.cache", "true");
        }
        catch (Throwable t) {
          logger.error("file.resource.loader.cache");

        }
        try {
          engine.setProperty("file.resource.loader.modificationCheckInterval", "10");
        }
        catch (Throwable t) {
          logger.error("file.resource.loader.modificationCheckInterval");

        }

        try {
          engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new VelocityLoggerWrapper(logger));
        }
        catch (Throwable t) {
          logger.error(VelocityEngine.RUNTIME_LOG_LOGSYSTEM);

        }
/*        try {
          engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, null);
        }
        catch (Throwable t) {
          logger.error(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS);

        }
*/

        engine.init();
      }
      catch (Throwable t) {
        t.printStackTrace(logger.asPrintWriter(logger.ERROR_MESSAGE));

        logger.error("Failed to set up a VelocityGeneratorLibrary: " + t.toString());
        throw new GeneratorFailure(t);
      }
    }

    public Generator makeGenerator(String anIdentifier) throws GeneratorExc, GeneratorFailure {
      return new VelocityGenerator(anIdentifier, this);
    }
  }

  public static class VelocityGeneratorLibraryFactory implements GeneratorLibraryFactory {
    private String basePath;

    public VelocityGeneratorLibraryFactory(String aBasePath) {
      basePath = aBasePath;
    }

    public GeneratorLibrary makeLibrary(String anInitializationString) throws GeneratorExc, GeneratorFailure {
      return new VelocityGeneratorLibrary(basePath+anInitializationString);
    };
  }
}
