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

package mir.entity;

import mir.storage.StorageObject;
import mir.storage.StorageObjectFailure;
import mir.util.RewindableIterator;

public class EntityBrowser implements RewindableIterator {

  private StorageObject storage;
  private String whereClause;
  private String orderByClause;
  private int batchSize;
  private int toFetch;
  private EntityList currentBatch;

  private int skip;
  private int limit;

  private int batchPosition;
  private int positionInBatch;

  public EntityBrowser(StorageObject aStorage, String aWhereClause, String anOrderByClause,
                       int aBatchSize, int aLimit, int aSkip) throws StorageObjectFailure {

    storage=aStorage;
    whereClause=aWhereClause;
    orderByClause=anOrderByClause;
    batchSize=aBatchSize;
    skip=aSkip;
    limit=aLimit;

    rewind();
  }

  public EntityBrowser(StorageObject aStorage,
          String aWhereClause, String anOrderByClause,
          int aBatchSize) throws StorageObjectFailure {
    this(aStorage, aWhereClause, anOrderByClause, aBatchSize, -1, 0);
  }

  public void readCurrentBatch(int aSkip) throws StorageObjectFailure {
    currentBatch = storage.selectByWhereClause(whereClause, orderByClause, aSkip, batchSize);
    batchPosition = aSkip;
    positionInBatch = 0;
  }

  public void rewind() {
    try {
      readCurrentBatch(skip);
    }
    catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  public boolean hasNext() {
    try {
      if (limit>-1 && batchPosition+positionInBatch>=skip+limit)
        return false;

      if (positionInBatch>=currentBatch.size() && currentBatch.hasNextBatch()) {
        readCurrentBatch(batchPosition+positionInBatch);
      }

      return (positionInBatch<currentBatch.size());
    }
    catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  public Object next() {
    try {
      if (hasNext()) {
        Entity result = currentBatch.elementAt(positionInBatch);
        positionInBatch=positionInBatch+1;

        return result;
      }
      else {
        return null;
      }
    }
    catch (Throwable t) {
      throw new RuntimeException(t.getMessage());
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}