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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.entity.adapter.EntityAdapterModel;
import mir.generator.Generator;
import mir.generator.WriterEngine;
import mir.log.LoggerWrapper;
import mir.producer.ProducerFactory;
import mir.producer.reader.DefaultProducerNodeBuilders;
import mir.producer.reader.ProducerConfigReader;
import mir.producer.reader.ProducerNodeBuilderLibrary;
import mir.util.FileMonitor;
import mircoders.global.MirGlobal;
import mircoders.global.ProducerEngine;
import mircoders.localizer.MirLocalizerExc;
import mircoders.localizer.MirLocalizerFailure;
import mircoders.localizer.MirProducerLocalizer;
import mircoders.producer.reader.SupplementalProducerNodeBuilders;

public class MirBasicProducerLocalizer implements MirProducerLocalizer {
  private List producerFactories;
  private Map nameToFactory;
  private List allNewProducerTasks;

  protected FileMonitor fileMonitor;
  protected EntityAdapterModel model;
  protected Generator.GeneratorLibrary generatorLibrary;
  protected WriterEngine writerEngine;

  protected LoggerWrapper logger;

  public MirBasicProducerLocalizer() {
    try {
      logger = new LoggerWrapper("Localizer.Basic.Producer");

      String allNewProducers = MirGlobal.getConfigProperty("Mir.Localizer.Producer.AllNewProducers");
      allNewProducerTasks = ProducerEngine.ProducerTask.parseProducerTaskList(allNewProducers);

      producerFactories = new Vector();
      model = MirGlobal.localizer().dataModel().adapterModel();
      generatorLibrary = MirGlobal.localizer().generators().makeProducerGeneratorLibrary();
      writerEngine = MirGlobal.localizer().generators().makeWriterEngine();
      nameToFactory = new HashMap();
    }
    catch (Throwable t) {
      logger.error("MirBasicProducerLocalizer(): Exception "+t.getMessage());
      model = new EntityAdapterModel();
    }
  }

  public List factories() throws MirLocalizerExc {
    if (fileMonitor==null || producerFactories == null || fileMonitor.hasChanged()) {
      try {
        List newProducers = new Vector();
        FileMonitor newFileMonitor = new FileMonitor();
        setupFactories(newProducers, newFileMonitor);

        producerFactories = newProducers;
        fileMonitor = newFileMonitor;
        logger.info("MirBasicProducerLocalizer.factories(): successfully setup factories");

        nameToFactory.clear();
        Iterator i = producerFactories.iterator();
        while (i.hasNext()) {
          ProducerFactory factory = (ProducerFactory) i.next();
          nameToFactory.put(factory.getName(), factory);
        }
      }
      catch (Throwable t) {
        logger.error("MirBasicProducerLocalizer.factories(): Unable to setup factories: "+t.getMessage());
      }
    }

    return producerFactories;
  };

  protected void setupProducerNodeBuilderLibrary(ProducerNodeBuilderLibrary aLibrary) throws MirLocalizerFailure {
    try {
      DefaultProducerNodeBuilders.registerBuilders(
          aLibrary, model, generatorLibrary, writerEngine,
          MirGlobal.getConfigProperty("Home"), MirGlobal.getConfigProperty("Producer.StorageRoot"));
      SupplementalProducerNodeBuilders.registerBuilders(aLibrary, model);
    }
    catch (Throwable t) {
      throw new MirLocalizerFailure(t.getMessage(), t);
    }
  }

  protected void setupFactories(List aFactories, FileMonitor aFileMonitor) throws MirLocalizerExc, MirLocalizerFailure {
    ProducerConfigReader reader;
    ProducerNodeBuilderLibrary library = new ProducerNodeBuilderLibrary();
    setupProducerNodeBuilderLibrary(library);
    List usedFiles = new Vector();
    Iterator i;

    aFileMonitor.clear();
    reader = new ProducerConfigReader();
    reader.parseFile(MirGlobal.getConfigProperty("Home") + File.separatorChar + MirGlobal.getConfigProperty("Mir.Localizer.ProducerConfigFile"), library, aFactories, usedFiles);

    i = usedFiles.iterator();
    while (i.hasNext())
      aFileMonitor.addFile((File) i.next());
  }

  public void produceAllNew() {
    MirGlobal.producerEngine().addTasks(allNewProducerTasks);
  };

  public ProducerFactory getFactoryForName(String aName) {
    try {
      factories();
    }
    catch (Throwable t) {
    }

    return (ProducerFactory) nameToFactory.get(aName);
  }
}
