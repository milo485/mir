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
package mircoders.global;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.log.LoggerToWriterAdapter;
import mir.log.LoggerWrapper;
import mir.producer.Producer;
import mir.producer.ProducerFactory;
import mir.util.*;
import mir.config.*;
import mir.util.StringRoutines;
import multex.Exc;
import multex.Failure;

public class ProducerEngine {
//  private Map producers;
  private JobQueue producerJobQueue;
  private LoggerWrapper logger;


  protected ProducerEngine() {
    logger = new LoggerWrapper("Producer");
    producerJobQueue = new JobQueue(new LoggerWrapper("Producer.Queue"));
  }

  public void addJob(String aProducerFactory, String aVerb) {
    producerJobQueue.appendJob(new ProducerJob(aProducerFactory, aVerb), aProducerFactory+"."+aVerb);
  }

  public void cancelJobs(List aJobs) {
    producerJobQueue.cancelJobs(aJobs);
  };

  public void addTask(ProducerTask aTask) {
    addJob(aTask.getProducer(), aTask.getVerb());
  }

  public void addTasks(List aTasks) {
    Iterator i = aTasks.iterator();

    while (i.hasNext()) {
      addTask((ProducerTask) i.next());
    }
  }

  private String convertStatus(JobQueue.JobInfo aJob) {
    switch (aJob.getStatus()) {
      case JobQueue.STATUS_ABORTED:
        return "aborted";
      case JobQueue.STATUS_CANCELLED:
        return "cancelled";
      case JobQueue.STATUS_CREATED:
        return "created";
      case JobQueue.STATUS_PENDING:
        return "pending";
      case JobQueue.STATUS_PROCESSED:
        return "processed";
      case JobQueue.STATUS_PROCESSING:
        return "processing";
    }
    return "unknown";
  }

  private Map convertJob(JobQueue.JobInfo aJob) {
    try {
      Map result = new HashMap();
      result.put("identifier", aJob.getIdentifier());
      result.put("description", aJob.getDescription());
      result.put("priority", new Integer(aJob.getPriority()));
      result.put("runningtime", new Double( (double) aJob.getRunningTime() / 1000));
      result.put("status", convertStatus(aJob));
      result.put("lastchange", new GeneratorFormatAdapters.DateFormatAdapter(aJob.getLastChange(), MirPropertiesConfiguration.instance().getString("Mir.DefaultTimezone")));
      result.put("finished", new Boolean(
          aJob.getStatus() == JobQueue.STATUS_PROCESSED ||
          aJob.getStatus() == JobQueue.STATUS_CANCELLED ||
          aJob.getStatus() == JobQueue.STATUS_ABORTED));

      return result;
    }
    catch (Throwable t) {
      throw new RuntimeException(t.toString());
    }
  }

  private List convertJobInfoList(List aJobInfoList) {
    List result = new Vector();

    Iterator i = aJobInfoList.iterator();

    while (i.hasNext())
      result.add(convertJob((JobQueue.JobInfo) i.next()));

    return result;
  }

  public List getQueueStatus() {
    return convertJobInfoList(producerJobQueue.getJobsInfo());
  }

  private class ProducerJob implements JobQueue.Job {
    private String factoryName;
    private String verb;
    private Producer producer;

    public ProducerJob(String aFactory, String aVerb) {
      factoryName = aFactory;
      verb = aVerb;
      producer=null;
    }

    public String getFactoryName() {
      return factoryName;
    }

    public String getVerb() {
      return verb;
    }

    public void abort() {
      if (producer!=null) {
        producer.abort();
      }
    }

    public boolean run() {
      ProducerFactory factory;
      long startTime;
      long endTime;
      boolean result = false;
      Map startingMap = new HashMap();

      startTime = System.currentTimeMillis();
      logger.info("Producing job: "+factoryName+"."+verb);

      try {
        factory = MirGlobal.localizer().producers().getFactoryForName( factoryName );

        if (factory!=null) {
          MirGlobal.localizer().producerAssistant().initializeGenerationValueSet(startingMap);

          synchronized(factory) {
            producer = factory.makeProducer(verb, startingMap);
          }
          if (producer!=null) {
            result = producer.produce(logger);
          }
        }
      }
      catch (Throwable t) {
        logger.error("Exception occurred while producing " + factoryName + "." + verb + t.getMessage());
        t.printStackTrace(new PrintWriter(new LoggerToWriterAdapter(logger, LoggerWrapper.ERROR_MESSAGE)));
      }
      endTime = System.currentTimeMillis();
      logger.info("Done producing job: " + factoryName + "." + verb + ", time elapsed:" + (endTime-startTime) + " ms");

      return result;
    }

    boolean isAborted() {
      return false;
    }
  }

  public static class ProducerEngineExc extends Exc {
    public ProducerEngineExc(String aMessage) {
      super(aMessage);
    }
  }

  public static class ProducerEngineRuntimeExc extends Failure {
    public ProducerEngineRuntimeExc(String msg, Exception cause){
      super(msg,cause);
    }
  }

  public static class ProducerTask {
    private String producer;
    private String verb;

    public ProducerTask(String aProducer, String aVerb) {
      producer = aProducer;
      verb = aVerb;
    }

    public String getVerb() {
      return verb;
    }

    public String getProducer() {
      return producer;
    }

    public static List parseProducerTaskList(String aList) throws ProducerEngineExc {
      Iterator i;
      List result = new Vector();

      i = StringRoutines.splitString(aList, ";").iterator();
      while (i.hasNext()) {
        String taskExpression = ((String) i.next()).trim();

        if (taskExpression.length()>0) {
          List parts = StringRoutines.splitString(taskExpression, ".");

          if (parts.size() != 2)
            throw new ProducerEngineExc("Invalid producer expression: '" + taskExpression + "'");
          else
            result.add(new ProducerEngine.ProducerTask( (String) parts.get(0), (String) parts.get(1)));
        }
      }

      return result;
    }
  }
}