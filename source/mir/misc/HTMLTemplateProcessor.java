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

package mir.misc;

import freemarker.template.*;
import mir.util.*;
import mir.generator.*;
import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import mir.entity.Entity;
import mir.entity.EntityList;
import mir.storage.StorageObjectFailure;
import org.apache.struts.util.MessageResources;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;


/**
 * Hilfsklasse zum Mergen von Template und Daten
 */
public final class HTMLTemplateProcessor {

  public static String templateDir;
  private static MirPropertiesConfiguration configuration;
  private static FileTemplateCache templateCache;
  private static Logfile theLog;
  private static String docRoot;
  private static String actionRoot;

  static {
    try {
      configuration = MirPropertiesConfiguration.instance();
    } catch (PropertiesConfigExc e) {
      e.printStackTrace();
    }
    theLog = Logfile.getInstance(
      configuration.getStringWithHome("HTMLTemplateProcessor.Logfile"));
    templateDir = 
    	configuration.getStringWithHome("HTMLTemplateProcessor.Dir");
    theLog.printDebugInfo("templateDir: " + templateDir);
    templateCache = new FileTemplateCache(templateDir);
    templateCache.setLoadingPolicy(FileTemplateCache.LOAD_ON_DEMAND);
    // gone in freemarker 1.7.1: templateCache.startAutoUpdate();


    docRoot = configuration.getString("RootUri");
    try {
      actionRoot = docRoot +configuration.getString("Producer.ActionServlet");
    }
    catch (ConfigException ce) {
      // if  Producer.ActionServlet is not set in the conf file
      actionRoot = docRoot + "/Mir";
    }
  }

  /**
   * empty private constructor, to avoid instantiation
   */
  private HTMLTemplateProcessor() {
  }

  // process-methods to merge different datastructures
  // with freemarker templates

  /**
       * Wandelt <code>anEntity</code> in freemarker-Struktur um, mischt die Daten mit
   * Template <code>templateFilename</code> und gibt das Ergebnis an den PrintWriter
   * <code>out</code>
   *
   * @param templateFilename
   * @param anEntity
   * @param out
   * @exception HTMLParseException
   */

  public static void process(String templateFilename, Entity anEntity,
                             PrintWriter out) throws HTMLParseException {
    if (anEntity == null)
      throw new HTMLParseException("entity is empty!");
    else
      process(templateFilename, anEntity, out);
  }

  /**
   * Wandelt Liste mit Entities <code>entList</code> in freemarker-Struktur um, mischt die Daten mit
   * Template <code>templateFilename</code> und gibt das Ergebnis an den PrintWriter
   * <code>out</code>
   *
   * @param templateFilename
   * @param entList
   * @param out
   * @exception HTMLParseException
   */
  public static void process(HttpServletResponse res, String templateFilename,
   EntityList entList, PrintWriter out, Locale locale) throws HTMLParseException {
    process(res, templateFilename, entList, (String)null, (TemplateModelRoot)null,
            out, locale);
  }

  /**
   * Wandelt Entitylist in freemarker-Struktur um, f?gt <code>additionalModel</code>
       * unter dem Namen <code>additionalModelName</code> ein und mischt die Daten mit
   * Template <code>templateFilename</code> und gibt das Ergebnis an den PrintWriter
   * <code>out</code>
   *
   * @param templateFilename
   * @param entList
   * @param additionalModelName
   * @param additionalModel
   * @param out
   * @exception HTMLParseException
   */

  public static void process(HttpServletResponse res, String templateFilename,
                             EntityList entList, String additionalModelName,
                             TemplateModelRoot additionalModel, PrintWriter out,
                             Locale locale) throws HTMLParseException {

    SimpleHash modelRoot = new SimpleHash();

    if (entList == null) {
      process(null, templateFilename, modelRoot, out, locale);
    }
    else {
      try {
        modelRoot = makeSimpleHashWithEntitylistInfos(entList);

        // Quickhack um mal ein Popup mit reinzunhemen ..
        if (additionalModelName != null && additionalModel != null)
          modelRoot.put(additionalModelName, additionalModel);

        process(res, templateFilename, modelRoot, out, locale);
      }
      catch (StorageObjectFailure e) {
        throw new HTMLParseException(e.toString());
      }
    }
  }


  /**
   * Gibt Template <code>templateFilename</code> an den PrintWriter
   * <code>out</code>
   *
   * @param templateFilename
   * @param mergeData
   * @param out
   * @exception HTMLParseException
   */
  public static void process(String templateFilename, PrintWriter out,
                             Locale locale) throws HTMLParseException {
    process(null, templateFilename, (TemplateModelRoot)null, out, locale);
  }

  /**
   * Mischt die freemarker-Struktur <code>tmr</code> mit
   * Template <code>templateFilename</code> und gibt das Ergebnis an den PrintWriter
   * <code>out</code>
   *
   * @param templateFilename
   * @param mergeData
   * @param out
   * @exception HTMLParseException
   */
  public static void process(HttpServletResponse res, String templateFilename,
                             TemplateModelRoot tmr, PrintWriter out,
                             Locale locale) throws HTMLParseException {
    process(res, templateFilename, tmr, null, out, locale);
  }

  public static void process(HttpServletResponse res, String templateFilename,
                             TemplateModelRoot tmr, TemplateModelRoot extra,
                             PrintWriter out, Locale locale) throws HTMLParseException {
    process(res, templateFilename, tmr, extra, out, locale, "bundles.adminlocal", "bundles.admin");
  }

  public static void process(HttpServletResponse res, String templateFilename,
       TemplateModelRoot tmr, TemplateModelRoot extra, PrintWriter out,
       Locale locale, String bundles) throws HTMLParseException {
    process(res, templateFilename, tmr, extra, out, locale, bundles, null);
  }

  /**
   * Mischt die freemarker-Struktur <code>tmr</code> mit
   * Template <code>templateFilename</code> und gibt das Ergebnis an den PrintWriter
   * <code>out</code>
   *
   * @param templateFilename
   * @param mergeData
   * @param out
   * @exception HTMLParseException
   */
  public static void process(HttpServletResponse res, String templateFilename,
                             TemplateModelRoot tmr, TemplateModelRoot extra,
                             PrintWriter out, Locale locale, String bundles,
                             String bundles2) throws
      HTMLParseException {
    if (out == null)
      throw new HTMLParseException("no outputstream");
    Template tmpl = getTemplateFor(templateFilename);
    if (tmpl == null)
      throw new HTMLParseException("no template: " + templateFilename);
    if (tmr == null)
      tmr = new SimpleHash();

      /** @todo  what is this for? (rk) */
    String session = "";
    if (res != null) {
      session = res.encodeURL("");
    }

    SimpleHash configHash = new SimpleHash();

    // pass the whole config hash to the templates
    Iterator it = configuration.getKeys();
    String key;
    while (it.hasNext()) {
      key = (String) it.next();
      configHash.put(key, new SimpleScalar(
      	configuration.getString(key))
      );
    }

    // this does not come directly from the config file
    configHash.put("docRoot", new SimpleScalar(docRoot));
    configHash.put("actionRoot", new SimpleScalar(actionRoot + session));
    configHash.put("now",
                   new SimpleScalar(StringUtil.date2readableDateTime(new GregorianCalendar())));

    // this conform to updated freemarker syntax
    configHash.put("compressWhitespace",
                   new freemarker.template.utility.CompressWhitespace());

    SimpleHash utilityHash = new SimpleHash();
    try {
      utilityHash.put("compressWhitespace",
                      new freemarker.template.utility.CompressWhitespace());
      utilityHash.put("encodeURI",
                      FreemarkerGenerator.makeAdapter(new GeneratorHTMLFunctions.
          encodeURIGeneratorFunction()));
      utilityHash.put("encodeHTML",
                      FreemarkerGenerator.makeAdapter(new GeneratorHTMLFunctions.
          encodeHTMLGeneratorFunction()));
      utilityHash.put("isOdd",
                      FreemarkerGenerator.makeAdapter(new GeneratorIntegerFunctions.
          isOddFunction()));
      utilityHash.put("increment",
                      FreemarkerGenerator.makeAdapter(new GeneratorIntegerFunctions.
          incrementFunction()));
    }
    catch (Throwable t) {
      throw new HTMLParseException(t.getMessage());
    }

    SimpleHash outPutHash = new SimpleHash();

    if (extra != null) {
      outPutHash.put("extra", extra);
      try {
        while ( ( (SimpleList) extra).hasNext()) {
          theLog.printDebugInfo( ( (SimpleList) extra).next().toString());
        }
      }
      catch (Exception e) {
      }
    }
    outPutHash.put("data", tmr);
    outPutHash.put("config", configHash);
    outPutHash.put("utility", utilityHash);

    MessageResources messages = MessageResources.getMessageResources(bundles);
    if (bundles2!=null) {
      outPutHash.put("lang", new MessageMethodModel(locale, MessageResources.getMessageResources(bundles), MessageResources.getMessageResources(bundles2)));
    }
    else {
      outPutHash.put("lang", new MessageMethodModel(locale, MessageResources.getMessageResources(bundles)));
    }

    tmpl.process(outPutHash, out);
  }

  /**
   *   Converts Entity-List to SimpleList of SimpleHashes.
   *   @param aList ist eine Liste von Entity
   *   @return eine freemarker.template.SimpleList von SimpleHashes.
   *
   *    @deprecated EntityLists comply with TemplateListModel now.
   */
  public static SimpleList makeSimpleList(EntityList aList) throws
      StorageObjectFailure {
    theLog.printWarning(
        "## using deprecated makeSimpleList(entityList) - a waste of resources");
    SimpleList simpleList = new SimpleList();
    if (aList != null) {
      for (int i = 0; i < aList.size(); i++) {
        simpleList.add(aList.elementAt(i));
      }
    }
    return simpleList;
  }

  /**
   *  Konvertiert ein EntityList in ein freemarker.template.SimpleHash-Modell. Im Hash
   *  sind die einzelnen Entities ueber ihre id zu erreichen.
   *  @param aList ist die EntityList
   *  @return SimpleHash mit den entsprechenden freemarker Daten
   *
   */
  public static SimpleHash makeSimpleHash(EntityList aList) throws
      StorageObjectFailure {
    SimpleHash simpleHash = new SimpleHash();
    Entity currentEntity;

    if (aList != null) {
      for (int i = 0; i < aList.size(); i++) {
        currentEntity = (Entity) aList.elementAt(i);
        simpleHash.put(currentEntity.getId(), currentEntity);
      }
    }
    return simpleHash;
  }

  /**
   *  Konvertiert ein Hashtable mit den keys und values als String
   *  in ein freemarker.template.SimpleHash-Modell
   *  @param mergeData der HashMap mit den String / String Daten
   *  @return SimpleHash mit den entsprechenden freemarker Daten
   *
   */
  public static SimpleHash makeSimpleHash(HashMap mergeData) {
    SimpleHash modelRoot = new SimpleHash();
    String aField;
    if (mergeData != null) {
      Set set = mergeData.keySet();
      Iterator it = set.iterator();
      for (int i = 0; i < set.size(); i++) {
        aField = (String) it.next();
        modelRoot.put(aField, (String) mergeData.get(aField));
      }
    }
    return modelRoot;
  }

  /**
   * Converts EntityList in SimpleHash and adds additional information
   * to the returned SimpleHash
   *
   * @param entList
   * @return SimpleHash returns SimpleHash with the converted EntityList plus
   *        additional Data about the list.
   * @exception StorageObjectException
   */

  public static SimpleHash makeSimpleHashWithEntitylistInfos(EntityList entList) throws
      StorageObjectFailure {
    SimpleHash modelRoot = new SimpleHash();
    if (entList != null) {
      modelRoot.put("contentlist", entList);
      modelRoot.put("count",
                    new SimpleScalar( (new Integer(entList.getCount())).toString()));
      if (entList.getWhere() != null) {
        modelRoot.put("where", new SimpleScalar(entList.getWhere()));
        modelRoot.put("where_encoded",
                      new SimpleScalar(URLEncoder.encode(entList.getWhere())));
      }
      if (entList.getOrder() != null) {
        modelRoot.put("order", new SimpleScalar(entList.getOrder()));
        modelRoot.put("order_encoded",
                      new SimpleScalar(URLEncoder.encode(entList.getOrder())));
      }
      modelRoot.put("from",
                    new SimpleScalar( (new Integer(entList.getFrom())).toString()));
      modelRoot.put("to",
                    new SimpleScalar( (new Integer(entList.getTo())).toString()));

      if (entList.hasNextBatch())
        modelRoot.put("next",
                      new SimpleScalar( (new Integer(entList.getNextBatch())).
                                       toString()));
      if (entList.hasPrevBatch())
        modelRoot.put("prev",
                      new SimpleScalar( (new Integer(entList.getPrevBatch())).
                                       toString()));
    }
    return modelRoot;
  }

  /**
   * Private methods to get template from a templateFilename
   * @param templateFilename
   * @return Template
   * @exception HTMLParseException
   */
  private static Template getTemplateFor(String templateFilename) throws
      HTMLParseException {
    Template returnTemplate = null;
    if (templateFilename != null)
      returnTemplate = (Template) templateCache.getItem(templateFilename,
          "template");

    if (returnTemplate == null) {
      theLog.printError("CACHE (ERR): Unknown template: " + templateFilename);
      throw new HTMLParseException("Templatefile: " + templateFilename +
                                   " not found.");
    }

    return returnTemplate;
  }

  public static void stopAutoUpdate() {
    templateCache.stopAutoUpdate();
    templateCache = null;
  }

}