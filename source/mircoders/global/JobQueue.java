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


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.log.LoggerWrapper;

// important: objects passed as data must not be altered once put into a job

public class JobQueue {
  private Vector jobHandlers;
  private Map identifierToJobHandler;
  private int nrJobs;
  private int jobCleanupTreshold;
  private JobQueueRunner queueRunner;
  private Thread thread;
  private LoggerWrapper logger;
  private long lastCleanup;

  public static final int STATUS_CREATED = -1;
  public static final int STATUS_PENDING = 0;
  public static final int STATUS_PROCESSING = 1;
  public static final int STATUS_PROCESSED = 2;
  public static final int STATUS_CANCELLED = 3;
  public static final int STATUS_ABORTED = 4;

  public static final int PRIORITY_NORMAL = 100;
  public static final int PRIORITY_LOW = 10;
  public static final int PRIORITY_HIGH = 1000;

  public static final int FINISHEDJOBS_LOGSIZE = 10;

  public JobQueue(LoggerWrapper aLogger) {
    logger = aLogger;
    jobHandlers = new Vector();
    identifierToJobHandler = new HashMap();
    nrJobs = 0;
    lastCleanup = 0;
    jobCleanupTreshold = 900; // seconds
    queueRunner = new JobQueueRunner(logger);
    thread = new Thread(queueRunner);
    thread.start();
  }

  public String appendJob(Job aJob, String aDescription) {
    try {
      if (System.currentTimeMillis() - lastCleanup > 60000)
        cleanupJobList();
    }
    catch (Throwable t) {
      logger.error("error while cleaning up joblist: " + t.toString());
    }

    synchronized (jobHandlers) {
      JobHandler jobHandler = new JobHandler(aJob, Integer.toString(nrJobs), aDescription);
      nrJobs++;
      jobHandlers.add(jobHandler);
      identifierToJobHandler.put(jobHandler.getIdentifier(), jobHandler);
      jobHandler.setPending();

      return jobHandler.getIdentifier();
    }
  }

  public List getJobsInfo() {
    List result = new Vector();

    synchronized (jobHandlers) {
      Iterator i = jobHandlers.iterator();

      while (i.hasNext()) {
        result.add(0, ((JobHandler) i.next()).getJobInfo());
      }
    }

    return result;
  }

  private void cleanupJobList() {
    List toRemove = new Vector();
    synchronized (jobHandlers) {
      Iterator i = jobHandlers.iterator();

      Calendar tresholdCalendar = new GregorianCalendar();
      tresholdCalendar.add(Calendar.SECOND, -jobCleanupTreshold);
      Date treshold = tresholdCalendar.getTime();

      while (i.hasNext()) {
        JobHandler jobHandler = (JobHandler) i.next();

        synchronized (jobHandler) {
          if (jobHandler.isFinished() && jobHandler.getLastChange().before(treshold)) {
            toRemove.add(jobHandler);
          }
        }
      }

      jobHandlers.removeAll(toRemove);
    }

    lastCleanup = System.currentTimeMillis();
  }

  private JobHandler acquirePendingJob() {
    synchronized (jobHandlers) {
      int priorityFound= 0;
      JobHandler jobFound;

      jobFound = null;
      Iterator i = jobHandlers.iterator();
      while (i.hasNext()) {
        JobHandler job = (JobHandler) i.next();

        if (job.isPending() && (jobFound==null || priorityFound<job.getPriority())) {
          jobFound = job;
          priorityFound = job.getPriority();
        }
      }

      return jobFound;
    }
  }

  public void cancelJobs(List aJobs) {
    synchronized (jobHandlers) {
      Iterator i = aJobs.iterator();

      while (i.hasNext()) {
        ((JobHandler) identifierToJobHandler.get(i.next())).cancelOrAbortJob();
      }
    }
  }

  public interface Job {
    void abort();

    /**
     *
     *
     * @return <code>true</code> if terminated normally, <code>false</code> if aborted
     */
    boolean run();
  }

  public static class JobInfo {
    private String identifier;
    private Date lastChange;
    private int status;
    private long runningTime;
    private int priority;
    private String description;

    private JobInfo(String aDescription, int aStatus, Date aLastChange, String anIdentifier, long aRunningTime, int aPriority) {
      description = aDescription;
      lastChange = aLastChange;
      status = aStatus;
      identifier = anIdentifier;
      priority = aPriority;
      runningTime = aRunningTime;
    }

    public String getDescription() {
      return description;
    }

    public int getStatus() {
      return status;
    }

    public int getPriority() {
      return priority;
    }

    public Date getLastChange() {
      return lastChange;
    }

    public String getIdentifier() {
      return identifier;
    }

    public long getRunningTime() {
      return runningTime;
    }
  }

  public class JobHandler {
    private Job job;
    private String identifier;
    private String description;

    private Date lastChange;
    private long starttime;
    private long endtime;
    private int status;
    private int priority;
    private boolean hasRun;

    public JobHandler(Job aJob, String anIdentifier, String aDescription, int aPriority) {
      job = aJob;
      description = aDescription;
      identifier = anIdentifier;
      priority = aPriority;
      status = STATUS_CREATED;
    }

    public JobHandler(Job aJob, String anIdentifier, String aDescription) {
      this(aJob, anIdentifier, aDescription, PRIORITY_NORMAL);
    }

    public JobInfo getJobInfo() {
      return new JobInfo(getDescription(), getStatus(), getLastChange(), getIdentifier(), getRunningTime(), priority);
    }

    private void runJob() {
      if (setProcessing()) {
        if (job.run())
          setProcessed();
        else
          setAborted();
      }
    };

    private void cancelOrAbortJob() {
      synchronized (this) {
        if (isPending())
          setCancelled();
        if (isProcessing())
          job.abort();
      }
    };

    public int getStatus() {
      synchronized(this) {
        return status;
      }
    }

    public String getIdentifier() {
      return identifier;
    }

    public String getDescription() {
      return description;
    }

    public long getRunningTime() {
      synchronized(this) {
        long result = 0;

        if (hasRun) {
          if (isFinished())
            result = endtime;
          else
            result = System.currentTimeMillis();

          result = result - starttime;
        }

        return result;
      }
    }

    public int getPriority() {
      return priority;
    }

    private boolean setProcessing() {
      return setStatus(STATUS_PENDING, STATUS_PROCESSING);
    }

    private void setProcessed() {
      setStatus(STATUS_PROCESSING, STATUS_PROCESSED);
    }

    private void setAborted() {
      setStatus(STATUS_PROCESSING, STATUS_ABORTED);
    }

    private void setPending() {
      setStatus(STATUS_CREATED, STATUS_PENDING);
    }

    private boolean setCancelled() {
      return setStatus(STATUS_PENDING, STATUS_CANCELLED);
    }

    public boolean hasBeenProcessed() {
      return getStatus() == STATUS_PROCESSED;
    }

    public boolean hasBeenAborted() {
      return getStatus() == STATUS_ABORTED;
    }

    public boolean isCancelled() {
      return getStatus() == STATUS_CANCELLED;
    }

    public boolean isFinished() {
      return hasBeenProcessed() || hasBeenAborted() || isCancelled();
    }

    public boolean isProcessing() {
      return getStatus() == STATUS_PROCESSING;
    }

    public boolean isPending() {
      return getStatus() == STATUS_PENDING;
    }

    public Date getLastChange() {
      synchronized (this) {
        return lastChange;
      }
    }

    private boolean setStatus(int anOldStatus, int aNewStatus) {
      synchronized(this) {
        if (status == anOldStatus) {
          status = aNewStatus;
          lastChange = (new GregorianCalendar()).getTime();
          if (isProcessing()) {
            starttime = System.currentTimeMillis();
            hasRun = true;
          }

          if (isFinished()) {
            endtime = System.currentTimeMillis();
          }
          return true;
        }
        else {
          return false;
        }
      }
    }
  }

  private class JobQueueRunner implements Runnable {
    private LoggerWrapper logger;

    public JobQueueRunner(LoggerWrapper aLogger) {
      logger = aLogger;
    }

    public void run() {
      logger.debug("starting JobQueueRunner");

      try {
        while (true) {
          JobHandler job = acquirePendingJob();
          if (job != null) {
            logger.debug("  starting job ("+job.getIdentifier()+"): " +job.getDescription());
            job.runJob();
            logger.debug("  finished job ("+job.getIdentifier()+"): " +job.getDescription());
          }
          else {
            try {
              Thread.sleep(1500);
            }
            catch (InterruptedException e) {
            }
          }
        }
      }
      finally {
        logger.warn("JobQueueRunner terminated");
      }
    }
  }
}

