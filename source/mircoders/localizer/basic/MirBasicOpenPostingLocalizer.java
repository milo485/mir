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

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import mir.misc.Logfile;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.global.MirGlobal;
import mircoders.global.ProducerEngine;
import mircoders.localizer.MirOpenPostingLocalizer;

public class MirBasicOpenPostingLocalizer implements MirOpenPostingLocalizer {
  private List afterContentProducerTasks;
  private List afterCommentProducerTasks;

  protected static Logfile logger = Logfile.getInstance( MirGlobal.getConfigProperty("Home") + "/" + MirGlobal.getConfigProperty("Mir.Localizer.Logfile"));

  public MirBasicOpenPostingLocalizer() {
    try {
      String contentProducers = MirGlobal.getConfigProperty("Mir.Localizer.OpenPosting.ContentProducers");
      String commentProducers = MirGlobal.getConfigProperty("Mir.Localizer.OpenPosting.CommentProducers");

      afterContentProducerTasks = ProducerEngine.ProducerTask.parseProducerTaskList(contentProducers);
      afterCommentProducerTasks = ProducerEngine.ProducerTask.parseProducerTaskList(commentProducers);
    }
    catch (Throwable t) {
      logger.printError("Setting up MirBasicOpenPostingLocalizer failed: " + t.getMessage());
    }
  }

  public void afterContentPosting() {
    MirGlobal.producerEngine().addTasks(afterContentProducerTasks);
  }

  public void afterContentPosting(EntityContent aContent) {
    afterContentPosting();
  }

  public void afterCommentPosting() {
    MirGlobal.producerEngine().addTasks(afterCommentProducerTasks);
  }

  public void afterCommentPosting(EntityComment aComment) {
    afterCommentPosting();
  }

  public String chooseOpenPostingLanguage(HttpServletRequest req) {
    Locale locale = req.getLocale();

    return locale.getLanguage();
  }

}
