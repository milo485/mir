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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

// important: objects passed as data must not be altered once put into a job

public class JobQueue {
  private Vector jobs;
  private Vector finishedJobs;
  private Map dataToJob;
  private Map identifierToJob;
  private int nrJobs;

  public static final int STATUS_PENDING = 0;
  public static final int STATUS_PROCESSING = 1;
  public static final int STATUS_PROCESSED = 2;
  public static final int STATUS_CANCELLED = 3;
  public static final int STATUS_ABORTED = 4;

  public static final int PRIORITY_NORMAL = 100;
  public static final int PRIORITY_LOW = 10;
  public static final int PRIORITY_HIGH = 1000;

  public static final int FINISHEDJOBS_LOGSIZE = 10;

  public JobQueue() {
    finishedJobs = new Vector();
    jobs = new Vector();
    dataToJob = new HashMap();
    identifierToJob = new HashMap();
    nrJobs = 0;
  }

  public String appendJob(Object aData) {
    synchronized (jobs) {
      Job job = new Job(aData, Integer.toString(nrJobs));
      nrJobs++;
      jobs.add(job);
      dataToJob.put(aData, job);
      identifierToJob.put(job.getIdentifier(), job);
      return job.getIdentifier();
    }
  }

  public Object acquirePendingJob() {
    synchronized (jobs) {
      int priorityFound= 0;
      Job jobFound;

      do {
        jobFound = null;
        Iterator i = jobs.iterator();
        while (i.hasNext()) {
          Job job = (Job) i.next();

          if (job.isPending() && (jobFound==null || priorityFound<job.getPriority())) {
            jobFound = job;
            priorityFound = job.getPriority();
          }
        }
      }
      while (jobFound!=null && !jobFound.setProcessing());

      if (jobFound!=null)
        return jobFound.getData();
      else
        return null;
    }
  }

  private void finishJob(Job aJob) {
    synchronized (jobs) {
      identifierToJob.remove(aJob.identifier);
      jobs.remove(aJob);
      finishedJobs.insertElementAt(aJob, 0);
      if (finishedJobs.size()>FINISHEDJOBS_LOGSIZE)
        finishedJobs.remove(finishedJobs.size()-1);
    }
  }

  public void jobProcessed(Object aData) {
    synchronized (jobs) {
      Job job = (Job) dataToJob.get(aData);

      if (job!=null) {
        job.setProcessed();
        finishJob(job);
      }
    }
  }

  public void jobAborted(Object aData) {
    synchronized (jobs) {
      Job job = (Job) dataToJob.get(aData);

      if (job!=null) {
        job.setAborted();
        finishJob(job);
      }
    }
  }

  public void cancelJob(Object aData) {
    synchronized (jobs) {
      Job job = (Job) dataToJob.get(aData);

      if (job!=null && job.setCancelled()) {
        finishJob(job);
      }
    }
  }

  public void makeJobListSnapshots(List aJobList, List aFinishedJobList) {
    synchronized (jobs) {
      aJobList.addAll(makeJobListSnapshot());
      aFinishedJobList.addAll(makeFinishedJobListSnapshot());
    }
  }

  public List makeJobListSnapshot() {
    synchronized (jobs) {
      return (List) jobs.clone();
    }
  }

  public List makeFinishedJobListSnapshot() {
    synchronized (jobs) {
      return (List) finishedJobs.clone();
    }
  }

  public class Job implements Cloneable {
    private Object data;
    private Date lastChange;
    private String identifier;
    private int status;
    private int priority;

    public Job(Object aData, String anIdentifier, int aStatus, int aPriority, Date aLastChange) {
      data = aData;
      status = aStatus;
      identifier = anIdentifier;
      priority = aPriority;
      lastChange = aLastChange;
    }

    public Job(Object aData, String anIdentifier, int aStatus, int aPriority) {
      this(aData, anIdentifier, aStatus, aPriority, (new GregorianCalendar()).getTime());
    }

    public Date getLastChange() {
      return lastChange;
    }

    public String getIdentifier() {
      return identifier;
    }

    public Job(Object aData, String anIdentifier) {
      this(aData, anIdentifier, STATUS_PENDING, PRIORITY_NORMAL);
    }

    public Object getData() {
      return data;
    }

    public int getStatus() {
      synchronized(this) {
        return status;
      }
    }

    public int getPriority() {
      return priority;
    }

    protected boolean setProcessing() {
      return setStatus(STATUS_PENDING, STATUS_PROCESSING);
    }

    protected void setProcessed() {
      setStatus(STATUS_PROCESSING, STATUS_PROCESSED);
    }

    protected void setAborted() {
      setStatus(STATUS_PROCESSING, STATUS_ABORTED);
    }

    protected boolean setCancelled() {
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

    private boolean setStatus(int anOldStatus, int aNewStatus) {
      synchronized(this) {
        if (status == anOldStatus) {
          status = aNewStatus;
          lastChange = (new GregorianCalendar()).getTime();
          return true;
        }
        else {
          return false;
        }
      }
    }

    protected Object clone() {
      synchronized(this) {
        return new Job(data, identifier, status, priority, lastChange);
      }
    }
  }
}

