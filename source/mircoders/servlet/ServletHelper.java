package mircoders.servlet;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.struts.util.MessageResources;
import mir.entity.adapter.EntityIteratorAdapter;
import mir.generator.Generator;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.util.CachingRewindableIterator;
import mir.util.NullWriter;
import mir.util.ResourceBundleGeneratorFunction;
import mircoders.global.MirGlobal;



public class ServletHelper {
  public static Map makeGenerationData(Locale[] aLocales) throws ServletModuleExc {
    return makeGenerationData(aLocales, "bundles.adminlocal", "bundles.admin");
  }

  public static Map makeGenerationData(Locale[] aLocales, String aBundle) throws ServletModuleExc {
    return makeGenerationData(aLocales, aBundle, aBundle);
  }

  public static Map makeGenerationData(Locale[] aLocales, String aBundle, String aDefaultBundle) throws ServletModuleExc {

    try {
      Map result = new HashMap();

      MirGlobal.localizer().producerAssistant().initializeGenerationValueSet(result);

      result.put("returnurl", null);

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
          new ResourceBundleGeneratorFunction( aLocales,
             new MessageResources[] { MessageResources.getMessageResources(aBundle),
                                   MessageResources.getMessageResources(aDefaultBundle)}));

      return result;
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public static void generateResponse(PrintWriter aWriter, Map aGenerationData, String aGenerator) throws ServletModuleExc {
    Generator generator;

    try {
      generator = MirGlobal.localizer().generators().makeAdminGeneratorLibrary().makeGenerator(aGenerator);

      generator.generate(aWriter, aGenerationData, new PrintWriter(new NullWriter()));
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public static void generateOpenPostingResponse(PrintWriter aWriter, Map aGenerationData, String aGenerator) throws ServletModuleExc {
    Generator generator;

    try {
      generator = MirGlobal.localizer().generators().makeAdminGeneratorLibrary().makeGenerator(aGenerator);

      generator.generate(aWriter, aGenerationData, new PrintWriter(new NullWriter()));
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }
}
