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
package mircoders.servlet;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts.util.MessageResources;


import mir.entity.adapter.EntityIteratorAdapter;
import mir.generator.Generator;
import mir.log.LoggerWrapper;
import mir.servlet.ServletModuleExc;
import mir.servlet.ServletModuleFailure;
import mir.util.CachingRewindableIterator;
import mir.util.ResourceBundleGeneratorFunction;
import mircoders.global.MirGlobal;


public class ServletHelper {
  static LoggerWrapper logger = new LoggerWrapper("ServletModule.Helper");


  public static Map makeGenerationData(HttpServletResponse aResponse, Locale[] aLocales) throws ServletModuleExc {
    return makeGenerationData(aResponse, aLocales, "bundles.adminlocal", "bundles.admin");
  }

  public static Map makeGenerationData(HttpServletResponse aResponse, Locale[] aLocales, String aBundle) throws ServletModuleExc {
    return makeGenerationData(aResponse, aLocales, aBundle, aBundle);
  }

  public static Map makeGenerationData(HttpServletResponse aResponse, Locale[] aLocales, String aBundle, String aDefaultBundle) throws ServletModuleExc {

    try {
      Map result = new HashMap();

      MirGlobal.localizer().producerAssistant().initializeGenerationValueSet(result);

      // ML: hackish
      ((Map) result.get("config")).put("actionRoot",
             aResponse.encodeURL(MirGlobal.config().getString("RootUri") + "/servlet/Mir"));

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

      generator.generate(aWriter, aGenerationData, logger);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public static void generateOpenPostingResponse(PrintWriter aWriter, Map aGenerationData, String aGenerator) throws ServletModuleExc {
    Generator generator;

    try {
      generator = MirGlobal.localizer().generators().makeOpenPostingGeneratorLibrary().makeGenerator(aGenerator);

      generator.generate(aWriter, aGenerationData, logger);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }
}
