package mircoders.servlet;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import mir.generator.Generator;
import mir.servlet.ServletModuleException;
import mir.util.NullWriter;
import mir.util.ResourceBundleGeneratorFunction;
import mir.entity.adapter.*;
import mir.util.*;
import mircoders.global.MirGlobal;

import org.apache.struts.util.MessageResources;



public class ServletHelper {
// ML: add logging!

  static Map makeGenerationData(Locale aLocale) throws ServletModuleException {
    try {
      Map result = new HashMap();

      MirGlobal.localizer().producerAssistant().initializeGenerationValueSet(result);

      Object languages =
          new CachingRewindableIterator(
            new EntityIteratorAdapter( "", "id", 30,
               MirGlobal.localizer().dataModel().adapterModel(), "language"));
      Object topics =
          new CachingRewindableIterator(
            new EntityIteratorAdapter("", "id", 30,
               MirGlobal.localizer().dataModel().adapterModel(), "topic"));

      Object articleTypes =
          new CachingRewindableIterator(
            new EntityIteratorAdapter( "", "id", 30,
               MirGlobal.localizer().dataModel().adapterModel(), "articleType"));

      Object commentStatuses =
          new CachingRewindableIterator(
            new EntityIteratorAdapter( "", "id", 30,
               MirGlobal.localizer().dataModel().adapterModel(), "commentStatus"));

      result.put("commentstatuses", commentStatuses);
      result.put("articletypes", articleTypes);
      result.put("languages", languages);
      result.put("topics", topics);

      result.put( "lang",
          new ResourceBundleGeneratorFunction( aLocale,
                  MessageResources.getMessageResources("bundles.adminlocal"),
                  MessageResources.getMessageResources("bundles.admin")));

      return result;
    }
    catch (Throwable t) {
      throw new ServletModuleException(t.getMessage());
    }
  }

  static void generateResponse(PrintWriter aWriter, Map aGenerationData, String aGenerator) throws ServletModuleException {

    Generator generator;

    try {
      generator = MirGlobal.localizer().generators().makeAdminGeneratorLibrary().makeGenerator(aGenerator);

      generator.generate(aWriter, aGenerationData, new PrintWriter(new NullWriter()));
    }
    catch (Throwable t) {
      throw new ServletModuleException(t.getMessage());
    }
  }
}