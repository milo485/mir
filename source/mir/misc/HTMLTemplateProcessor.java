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

import  java.lang.*;
import  java.util.*;
import  java.io.*;
import  java.net.*;
import  freemarker.template.*;
import  mir.entity.*;
import  mir.storage.*;
import javax.servlet.http.*;
import org.apache.struts.util.MessageResources;


/**
 * Hilfsklasse zum Mergen von Template und Daten
 */
public final class HTMLTemplateProcessor {

  public static String                templateDir;
  private static FileTemplateCache    templateCache;
  private static Logfile              theLog;
  private static String               docRoot;
  private static String               actionRoot;
  private static String               productionHost;
  private static String               audioHost;
  private static String               videoHost;
  private static String               imageHost;
  private static String               imagePath;
  private static String               openAction;
  private static String               defEncoding;
  private static String               generateFO;
  private static String               generatePDF;
  protected static String producerDocRoot =
		MirConfig.getProp("Producer.DocRoot");
  protected static String producerStorageRoot =
		MirConfig.getProp("Producer.StorageRoot");

  //
  // init

  static {
    /** @todo either in the above block or here :) //rk */
    templateDir = MirConfig.getPropWithHome("HTMLTemplateProcessor.Dir");
    templateCache = new FileTemplateCache(templateDir);
    templateCache.setLoadingPolicy(templateCache.LOAD_ON_DEMAND);
    // gone in freemarker 1.7.1
    // templateCache.startAutoUpdate();
    theLog = Logfile.getInstance(MirConfig.getPropWithHome("HTMLTemplateProcessor.Logfile"));
    docRoot = MirConfig.getProp("RootUri");
    //the quick hack is back in effect as it was more broken than ever before
    // -mh
    // sorry: nadir back in town, i have to debug the mirbase.jar in the
    // nadir evironment. from my point of coding, this needs an urgent
    // fixxx.
    // yeah, from my point too - tob.
	  //actionRoot = docRoot + "/servlet/" + MirConfig.getProp("ServletName");
    //actionRoot = docRoot + "/servlet/NadirAktuell";

    actionRoot = docRoot + "/Mir";

    defEncoding = MirConfig.getProp("Mir.DefaultHTMLCharset");
    openAction = MirConfig.getProp("Producer.OpenAction");
    productionHost = MirConfig.getProp("Producer.ProductionHost");
    videoHost = MirConfig.getProp("Producer.Video.Host");
    audioHost = MirConfig.getProp("Producer.Audio.Host");
    imageHost = MirConfig.getProp("Producer.Image.Host");
    imagePath = MirConfig.getProp("Producer.Image.Path");
    producerDocRoot = MirConfig.getProp("Producer.DocRoot");
    producerStorageRoot = MirConfig.getProp("Producer.StorageRoot");
    generateFO = MirConfig.getProp("GenerateFO");
    generatePDF = MirConfig.getProp("GeneratePDF");
  }

  /**
   * empty private constructor, to avoid instantiation
   */
  private HTMLTemplateProcessor () { }


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

    public static void process(String templateFilename, Entity anEntity, PrintWriter out)
      throws HTMLParseException {
        if (anEntity == null)  throw new HTMLParseException("entity is empty!");
        else process(templateFilename, anEntity, out);
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
  public static void process(HttpServletResponse res,String templateFilename,
														 EntityList entList, PrintWriter out, Locale locale)
    throws HTMLParseException {
    process(res, templateFilename, entList, (String)null, (TemplateModelRoot)null, out, locale);
  }

  /**
   * Wandelt Entitylist in freemarker-Struktur um, fügt <code>additionalModel</code>
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
    public static void process(HttpServletResponse res,String templateFilename,
															 EntityList entList, String additionalModelName,
                               TemplateModelRoot additionalModel, PrintWriter out,
															 Locale locale)
      throws HTMLParseException {

      SimpleHash modelRoot = new SimpleHash();

      if (entList == null) {
         process(null,templateFilename, modelRoot, out, locale);
      } else {
        try {
          modelRoot = makeSimpleHashWithEntitylistInfos(entList);

          // Quickhack um mal ein Popup mit reinzunhemen ..
          if (additionalModelName != null && additionalModel != null)
              modelRoot.put(additionalModelName, additionalModel);

          process(res,templateFilename, modelRoot, out, locale);
        } catch (StorageObjectException e) {
          throw new HTMLParseException(e.toString());
        }
      }
    }

  /**
   * Wandelt HashMap <code>mergeData</code> in freemarker-Struktur und mischt diese mit
   * Template <code>templateFilename</code> und gibt das Ergebnis an den PrintWriter
   * <code>out</code>
   *
   * @param templateFilename
   * @param mergeData - a HashMap with mergeData to be converted in SimpleHash
   * @param out
   * @exception HTMLParseException
   */
    public static void process(HttpServletResponse res,String templateFilename,
															 HashMap mergeData, PrintWriter out, Locale locale)
      throws HTMLParseException {
      process(res,templateFilename, makeSimpleHash(mergeData), out, locale);
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
															 Locale locale)
      throws HTMLParseException {
      process(null,templateFilename, (TemplateModelRoot)null, out, locale);
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
	public static void process(HttpServletResponse res,String templateFilename,
														 TemplateModelRoot tmr, PrintWriter out, Locale locale)
	throws HTMLParseException {
		process(res,templateFilename,tmr,null,out,locale);

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
	public static void process(HttpServletResponse res,String templateFilename,
														 TemplateModelRoot tmr, TemplateModelRoot extra,
														 PrintWriter out, Locale locale)
		throws HTMLParseException {
		if (out==null) throw new HTMLParseException("no outputstream");
		Template tmpl = getTemplateFor(templateFilename);
		if (tmpl == null) throw new HTMLParseException("no template: " + templateFilename);
		if (tmr==null) tmr = new SimpleHash();

		/** @todo  what is this for? (rk) */
		String session="";
		if (res!=null) {
			session=res.encodeURL("");
		}

    // @todo wouldn't it be so much easier to just pass the whole damn config
    // Hash here from Mirconfig... ??????? than we could access arbitrary config
    // values in the templates. -mh
		// put standard configuration into tempalteRootmodel
		SimpleHash configHash = new SimpleHash();
		configHash.put("defEncoding", new SimpleScalar(defEncoding));
		configHash.put("producerDocRoot", new SimpleScalar(producerDocRoot));
		configHash.put("storageRoot", new SimpleScalar(producerStorageRoot));
    configHash.put("productionHost", new SimpleScalar(productionHost));
		configHash.put("openAction", new SimpleScalar(openAction));
		configHash.put("actionRootLogin",new SimpleScalar(actionRoot));
		configHash.put("docRoot", new SimpleScalar(docRoot));
		configHash.put("now", new SimpleScalar(StringUtil.date2readableDateTime(new GregorianCalendar())));
		configHash.put("actionRoot", new SimpleScalar(actionRoot+session));
		configHash.put("videoHost", new SimpleScalar(videoHost));
		configHash.put("audioHost", new SimpleScalar(audioHost));
		configHash.put("imageHost", new SimpleScalar(imageHost));
		configHash.put("imagePath", new SimpleScalar(imagePath));
		configHash.put("mirVersion", new SimpleScalar(MirConfig.getProp("Mir.Version")));
		// this conform to updated freemarker syntax
		configHash.put("compressWhitespace", new freemarker.template.utility.CompressWhitespace() );
		configHash.put("generateFO", new SimpleScalar(generateFO));
		configHash.put("generatePDF", new SimpleScalar(generatePDF));

		SimpleHash outPutHash = new SimpleHash();

		if(extra!=null){
			outPutHash.put("extra",extra);
			try{
			while(((SimpleList)extra).hasNext()){
				theLog.printDebugInfo(((SimpleList)extra).next().toString());
			}
			}catch(Exception e){}
		}
		outPutHash.put("data",tmr);
		outPutHash.put("config", configHash);

		MessageResources messages = MessageResources.getMessageResources("bundles.admin");
		outPutHash.put("lang", new MessageMethodModel(locale, messages) );

		outPutHash.put("encodeHTML", new EncodeHTMLMethodModel() );

		tmpl.process(outPutHash,out);
	}


  /**
   *   Converts Entity-List to SimpleList of SimpleHashes.
   *   @param aList ist eine Liste von Entity
   *   @return eine freemarker.template.SimpleList von SimpleHashes.
   *
   *    @deprecated EntityLists comply with TemplateListModel now.
   */
  public static SimpleList makeSimpleList(EntityList aList) throws StorageObjectException
  {
    theLog.printWarning("## using deprecated makeSimpleList(entityList) - a waste of resources");
    SimpleList  simpleList = new SimpleList();
    if (aList != null) {
      for(int i=0;i<aList.size();i++) {
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
  public static SimpleHash makeSimpleHash(EntityList aList) throws StorageObjectException
  {
    SimpleHash      simpleHash = new SimpleHash();
    Entity          currentEntity;

    if (aList != null) {
      for (int i=0;i<aList.size();i++) {
         currentEntity = (Entity)aList.elementAt(i);
         simpleHash.put(currentEntity.getId(), currentEntity);
      }
    }
    return simpleHash;
  }

  /**
   *  Konvertiert ein Entity in ein freemarker.template.SimpleHash-Modell
   *  @param entity ist die Entity
   *  @return SimpleHash mit den entsprechenden freemarker Daten
   *
   *  @deprecated This method is deprecated and will be deleted in the next
   *  release. Entity interfaces freemarker.template.TemplateHashModel now
   *  and can be used in the same way as SimpleHash. It is not necessary any
   *  more to make a SimpleHash from an Entity
   */
  public static SimpleHash makeSimpleHash(Entity entity) {
    if (entity != null) {
      theLog.printWarning("## using deprecated makeSimpleHash(entity) - a waste of resources");
      return makeSimpleHash(entity.getValues());
    }
    else
      return null;
  }

  /**
   *  Konvertiert ein Hashtable mit den keys und values als String
   *  in ein freemarker.template.SimpleHash-Modell
   *  @param mergeData der HashMap mit den String / String Daten
   *  @return SimpleHash mit den entsprechenden freemarker Daten
   *
   */
  public static SimpleHash makeSimpleHash(HashMap mergeData)
  {
    SimpleHash modelRoot = new SimpleHash();
    String aField;
    if (mergeData != null) {
      Set set = mergeData.keySet();
      Iterator it =  set.iterator();
      for (int i=0; i<set.size();i++)  {
        aField = (String)it.next();
        modelRoot.put(aField, (String)mergeData.get(aField));
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

  public static SimpleHash makeSimpleHashWithEntitylistInfos(EntityList entList) throws StorageObjectException {
    SimpleHash modelRoot = new SimpleHash();
    if (entList!=null) {
      modelRoot.put("contentlist", entList);
      modelRoot.put("count", new SimpleScalar((new Integer(entList.getCount())).toString()));
      if (entList.getWhere()!=null) {
        modelRoot.put("where", new SimpleScalar(entList.getWhere()));
        modelRoot.put("where_encoded", new SimpleScalar(URLEncoder.encode(entList.getWhere())));
      }
      if(entList.getOrder()!=null) {
        modelRoot.put("order", new SimpleScalar(entList.getOrder()));
        modelRoot.put("order_encoded", new SimpleScalar(URLEncoder.encode(entList.getOrder())));
      }
      modelRoot.put("from", new SimpleScalar((new Integer(entList.getFrom())).toString()));
      modelRoot.put("to", new SimpleScalar((new Integer(entList.getTo())).toString()));

      if (entList.hasNextBatch())
        modelRoot.put("next", new SimpleScalar((new Integer(entList.getNextBatch())).toString()));
      if (entList.hasPrevBatch())
        modelRoot.put("prev", new SimpleScalar((new Integer(entList.getPrevBatch())).toString()));
    }
    return modelRoot;
  }

  /**
   * Private methods to get template from a templateFilename
   * @param templateFilename
   * @return Template
   * @exception HTMLParseException
   */
  private static Template getTemplateFor(String templateFilename) throws HTMLParseException
  {
    Template returnTemplate = null;
    if (templateFilename!=null)
      returnTemplate = (Template)templateCache.getItem(templateFilename,"template");


    if (returnTemplate==null) {
      theLog.printError("CACHE (ERR): Unknown template: " + templateFilename);
      throw new HTMLParseException("Templatefile: "+ templateFilename + " not found.");
    }

    return returnTemplate;
  }

  public static void stopAutoUpdate(){
    templateCache.stopAutoUpdate();
    templateCache=null;
  }

}
