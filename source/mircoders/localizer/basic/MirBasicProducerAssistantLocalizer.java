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

package mircoders.localizer.basic;

import java.util.*;
import java.io.*;
import freemarker.template.utility.*;
import mir.misc.*;
import mir.entity.*;
import mir.entity.adapter.*;
import mir.util.*;
import mircoders.module.*;
import mircoders.storage.*;
import mircoders.localizer.*;
import mircoders.global.*;

public class MirBasicProducerAssistantLocalizer implements MirProducerAssistantLocalizer {
  protected static Logfile logger = Logfile.getInstance( MirGlobal.getConfigProperty("Home") + "/" + MirGlobal.getConfigProperty("Mir.Localizer.Logfile"));

  public void initializeGenerationValueSet(Map aValueSet) {
    Iterator i;

    Map configMap = new HashMap();
    Map utilityMap = new HashMap();

// obsolete:
    configMap.put("producerDocRoot", MirGlobal.getConfigProperty("Producer.DocRoot"));
    configMap.put("storageRoot", MirGlobal.getConfigProperty("Producer.StorageRoot"));
    configMap.put("productionHost", MirGlobal.getConfigProperty("Producer.ProductionHost"));
    configMap.put("openAction", MirGlobal.getConfigProperty("Producer.OpenAction"));
    configMap.put("docRoot", MirGlobal.getConfigProperty("RootUri"));
    configMap.put("actionRoot", MirGlobal.getConfigProperty("RootUri")+"/servlet/Mir");
    configMap.put("now", new DateToMapAdapter((new GregorianCalendar()).getTime()));
    configMap.put("videoHost", MirGlobal.getConfigProperty("Producer.Video.Host"));
    configMap.put("audioHost", MirGlobal.getConfigProperty("Producer.Audio.Host"));
    configMap.put("imageHost", MirGlobal.getConfigProperty("Producer.Image.Host"));
    configMap.put("imagePath", MirGlobal.getConfigProperty("Producer.Image.Path"));
    configMap.put("mirVersion", MirGlobal.getConfigProperty("Mir.Version"));
    configMap.put("defEncoding", MirGlobal.getConfigProperty("Mir.DefaultEncoding"));

// "new":
    configMap.putAll( MirConfig.allSettings() );

    utilityMap.put("compressWhitespace", new freemarker.template.utility.CompressWhitespace() );
    utilityMap.put("encodeHTML", new GeneratorHTMLFunctions.encodeHTMLGeneratorFunction());
    utilityMap.put("encodeURI", new GeneratorHTMLFunctions.encodeURIGeneratorFunction());
    utilityMap.put("subString", new GeneratorStringFunctions.subStringFunction());
    utilityMap.put("subList", new GeneratorListFunctions.subListFunction());
    utilityMap.put("isOdd", new GeneratorIntegerFunctions.isOddFunction());
    utilityMap.put("increment", new GeneratorIntegerFunctions.incrementFunction());


    aValueSet.put("config", configMap);
    aValueSet.put("utility", utilityMap);

    EntityList topicList=null;
    EntityList entityList=null;
    EntityList parentList=null;
    EntityList languageList=null;

    try {
      ModuleTopics topicsModule = new ModuleTopics(DatabaseTopics.getInstance());
      ModuleLanguage languageModule = new ModuleLanguage(DatabaseLanguage.getInstance());

      topicList = topicsModule.getTopicsList();
      languageList = languageModule.getByWhereClause("", "id", -1);
    }
    catch (Throwable t) {
      logger.printError("initializeGenerationValueSet: Exception "+t.getMessage());
    }

    aValueSet.put("topics", topicList);
    aValueSet.put("imclist", entityList);
    aValueSet.put("parentlist", parentList);

    Map articleTypeMap = new HashMap();
    articleTypeMap.put("openposting", "0");
    articleTypeMap.put("newswire", "1");
    articleTypeMap.put("feature", "2");
    articleTypeMap.put("topicspecial", "3");
    articleTypeMap.put("startspecial", "4");

    try {
      i = new EntityIteratorAdapter( "", "", 20, MirGlobal.localizer().dataModel().adapterModel(), "articleType"  );

      while (i.hasNext()) {
        EntityAdapter articleType = (EntityAdapter) i.next();

        articleTypeMap.put(articleType.get("name"), articleType.get("id"));
      }
    }
    catch (Throwable t) {
      logger.printError("initializeGenerationValueSet: Exception while collecting article types "+t.getMessage());
    }
    aValueSet.put("articletype", articleTypeMap);

    Map commentStatusMap = new HashMap();
    try {
      i = new EntityIteratorAdapter( "", "", 20, MirGlobal.localizer().dataModel().adapterModel(), "commentStatus"  );

      while (i.hasNext()) {
        EntityAdapter commentStatus = (EntityAdapter) i.next();

        commentStatusMap.put(commentStatus.get("name"), commentStatus.get("id"));
      }
    }
    catch (Throwable t) {
      logger.printError("initializeGenerationValueSet: Exception while collecting comment statuses"+t.getMessage());
    }
    aValueSet.put("commentstatus", articleTypeMap);

  };

  public String filterText(String aText) {
    return StringUtil.createHTML(
        StringUtil.deleteForbiddenTags(aText),
        MirGlobal.getConfigProperty("Producer.ImageRoot"),
        MirGlobal.getConfigProperty("Producer.MailLinkName"),
        MirGlobal.getConfigProperty("Producer.ExtLinkName"),
        MirGlobal.getConfigProperty("Producer.IntLinkName")
    );
  }
}
