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

package mircoders.global;

import java.util.*;
import java.io.*;
import mir.producer.*;
import mir.util.*;
import multex.Exc;
import multex.Failure;

public class ProducerEngine {
//  private Map producers;
  private JobQueue producerJobQueue;
  private Thread queueThread;
  private PrintWriter log;

  protected ProducerEngine() {
    producerJobQueue = new JobQueue();
    try {
      RandomAccessFile raFile = (new RandomAccessFile(MirGlobal.getConfigProperty("Home") + "/" + MirGlobal.getConfigProperty("Producer.Logfile"), "rw"));
                        raFile.seek(raFile.length());
                log = new PrintWriter(new FileWriter( raFile.getFD()));
    }
    catch (Exception e) {
      log = new PrintWriter(new NullWriter());
    }
    queueThread = new Thread(new ProducerJobQueueThread());
    queueThread.start();
  }

  public void addJob(String aProducerFactory, String aVerb) {
    producerJobQueue.appendJob(new ProducerJob(aProducerFactory, aVerb));
    log.println(aProducerFactory+"."+aVerb+" added to queue");
    log.flush();
  }

  public void addTask(ProducerTask aTask) {
    addJob(aTask.getProducer(), aTask.getVerb());
  }

  public void addTasks(List aTasks) {
    Iterator i = aTasks.iterator();

    while (i.hasNext()) {
      addTask((ProducerTask) i.next());
    }
  }

  private String convertStatus(JobQueue.Job aJob) {
    if (aJob.hasBeenProcessed())
      return "processed";
    if (aJob.isProcessing())
      return "processing";
    if (aJob.isPending())
      return "pending";
    if (aJob.isCancelled())
      return "cancelled";
    if (aJob.hasBeenAborted())
      return "aborted";

    return "unknown";
  }

  private Map convertJob(JobQueue.Job aJob) {
    Map result = new HashMap();
    ProducerJob producerJob = (ProducerJob) aJob.getData();

    result.put("identifier", aJob.getIdentifier());
    result.put("factory", producerJob.getFactoryName());
    result.put("verb", producerJob.getVerb());
    result.put("priority", new Integer(aJob.getPriority()));
    result.put("status", convertStatus(aJob));
    result.put("lastchange", new DateToMapAdapter(aJob.getLastChange()));

    return result;
  }

  private void convertJobList(List aSourceJobList, List aDestination) {
    Iterator i = aSourceJobList.iterator();

    while (i.hasNext())
      aDestination.add(convertJob((JobQueue.Job) i.next()));
  }

  public List getQueueStatus() {
    List result = new Vector();
    List pendingJobs = new Vector();
    List finishedJobs = new Vector();

    producerJobQueue.makeJobListSnapshots(pendingJobs, finishedJobs);

    convertJobList(pendingJobs, result);
    convertJobList(finishedJobs, result);

    return result;
  }

private class ProducerJob {
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

    public void execute() {
      ProducerFactory factory;
      long startTime;
      long endTime;
      Map startingMap = new HashMap();

      startTime = System.currentTimeMillis();
      log.println("Producing job: "+factoryName+"."+verb);

      try {
        factory = MirGlobal.localizer().producers().getFactoryForName( factoryName );

        if (factory!=null) {
          MirGlobal.localizer().producerAssistant().initializeGenerationValueSet(startingMap);

          synchronized(factory) {
            producer = factory.makeProducer(verb, startingMap);
          }
          if (producer!=null) {
            producer.produce(log);
          }
        }
      }
      catch (Throwable t) {
        log.println("  exception "+t.getMessage());
        t.printStackTrace(log);
      }
      log.println("Done producing job: "+factoryName+"."+verb);
      endTime = System.currentTimeMillis();
      log.println("Time: " + (endTime-startTime) + " ms");
      log.flush();
    }

    boolean isAborted() {
      return false;
    }
  }

  private class ProducerJobQueueThread implements Runnable {
    public void run() {
      log.println("starting ProducerJobQueueThread");
      log.flush();

      while (true) {
        ProducerJob job = (ProducerJob) producerJobQueue.acquirePendingJob();
        if (job!=null) {
          job.execute();
          if (job.isAborted())
            producerJobQueue.jobAborted(job);
          else
            producerJobQueue.jobProcessed(job);
        }
        else
        {
          try {
            Thread.sleep(1500);
          }
          catch (InterruptedException e) {
          }
        }
      }
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
        String taskExpression = (String) i.next();
        List parts = StringRoutines.splitString(taskExpression, ".");

        if (parts.size()!=2)
          throw new ProducerEngineExc("Invalid producer expression: '" + taskExpression + "'");
        else
          result.add(new ProducerEngine.ProducerTask((String) parts.get(0), (String) parts.get(1)));
      }

      return result;
    }
  }
}