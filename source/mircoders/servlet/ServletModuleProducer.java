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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mir.generator.Generator;
import mir.log.LoggerWrapper;
import mir.producer.ProducerFactory;
import mir.servlet.ServletModule;
import mir.servlet.ServletModuleFailure;
import mir.util.ResourceBundleGeneratorFunction;
import mircoders.global.MirGlobal;

import org.apache.struts.util.MessageResources;

public class ServletModuleProducer extends ServletModule
{
  private static ServletModuleProducer instance = new ServletModuleProducer();
  public static ServletModule getInstance() { return instance; }

  Object comments;
  Map generationData;
  Generator generator;
  int totalNrComments;
  List producersData;

  void generateResponse(String aGeneratorIdentifier, PrintWriter aWriter, Map aResponseData, Locale aLocale) {
    try {
      generator = MirGlobal.localizer().generators().makeAdminGeneratorLibrary().makeGenerator(aGeneratorIdentifier);
      MirGlobal.localizer().producerAssistant().initializeGenerationValueSet(aResponseData);
      aResponseData.put( "lang", new ResourceBundleGeneratorFunction( aLocale, MessageResources.getMessageResources("bundles.admin")));
      generator.generate(aWriter, aResponseData, logger);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  private ServletModuleProducer() {
    super();
    logger = new LoggerWrapper("ServletModule.Producer");
    defaultAction="showProducerQueueStatus";
  }

  public void showMessage(PrintWriter aWriter, Locale aLocale, String aMessage, String anArgument1, String anArgument2) {
    Map responseData;
    try {
      responseData = new HashMap();
      responseData.put("message", aMessage);
      responseData.put("argument1", anArgument1);
      responseData.put("argument2", anArgument2);
      generateResponse("infomessage.template", aWriter, responseData, aLocale);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }


  public void showProducerQueueStatus(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    Object comments;
    Map generationData;
    Generator generator;
    int totalNrComments;
    List producersData;

    try {
      generator = MirGlobal.localizer().generators().makeAdminGeneratorLibrary().makeGenerator("producerqueue.template");

      generationData = ServletHelper.makeGenerationData(aResponse, new Locale[] { getLocale(aRequest), getFallbackLocale(aRequest)});
      generationData.put( "thisurl", "module=Producer&do=showProducerQueueStatus");

      producersData = new Vector();
      Iterator i = MirGlobal.localizer().producers().factories().iterator();
      while (i.hasNext()) {
        ProducerFactory factory = (ProducerFactory) i.next();

        List producerVerbs = new Vector();
        Iterator j = factory.verbs();
        while (j.hasNext()) {
          Map verbData = new HashMap();
          ProducerFactory.ProducerVerb verb = (ProducerFactory.ProducerVerb) j.next();
          verbData.put("name", verb.getName());
          verbData.put("description", verb.getDescription());

          producerVerbs.add(verbData);
        }

        Map producerData = new HashMap();
        producerData.put("name", factory.getName());
        producerData.put("verbs", producerVerbs);

        producersData.add(producerData);
      }
      generationData.put("producers", producersData);

      generationData.put("queue", MirGlobal.producerEngine().getQueueStatus());
      generator.generate(aResponse.getWriter(), generationData, logger);
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public void produce(HttpServletRequest req, HttpServletResponse res) {
    /*
     * This method will only be called by external scripts (e.g. from cron jobs).
     * The output therefore is very simple.
     *
     */

    try {
      PrintWriter out = res.getWriter();

      if (req.getParameter("producer")!=null) {
        String producerParam = req.getParameter("producer");
        String verbParam = req.getParameter("verb");

        MirGlobal.producerEngine().addJob(producerParam, verbParam);
        out.println("job added");
      }
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public void produceAllNew(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    try {
      MirGlobal.localizer().producers().produceAllNew();
      showMessage(aResponse.getWriter(), getLocale(aRequest), "produceAllNewAddedToQueue", "", "");
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public void enqueue(HttpServletRequest aRequest, HttpServletResponse aResponse) {
    try {
      if (aRequest.getParameter("producer")!=null) {
        String producerParam = aRequest.getParameter("producer");
        String verbParam = aRequest.getParameter("verb");

        MirGlobal.producerEngine().addJob(producerParam, verbParam);

        showProducerQueueStatus(aRequest, aResponse);
      }
    }
    catch (Throwable t) {
      throw new ServletModuleFailure(t);
    }
  }

  public void cancelAbortJob(HttpServletRequest aRequest, HttpServletResponse aResponse)  {
    // ML: to be coded
  }
}
