package mircoders.servlet;

import java.util.*;
import java.io.*;

import org.apache.struts.util.MessageResources;

import mir.entity.*;
import mir.entity.adapter.*;
import mir.generator.*;
import mir.servlet.*;
import mir.util.*;

import mircoders.global.*;



public class ServletHelper {
// ML: add logging!

  static Map makeGenerationData(Locale aLocale) throws ServletModuleException {
    try {
      MessageResources messages;
      Map result = new HashMap();

      MirGlobal.localizer().producerAssistant().initializeGenerationValueSet(result);
      messages = MessageResources.getMessageResources("bundles.admin");

      result.put( "lang", new ResourceBundleGeneratorFunction( aLocale, messages));

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