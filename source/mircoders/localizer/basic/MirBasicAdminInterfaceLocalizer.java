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
import mir.entity.adapter.*;
import mir.storage.*;
import mir.entity.*;
import mircoders.localizer.*;
import mircoders.entity.*;
import mircoders.storage.*;


public class MirBasicAdminInterfaceLocalizer implements MirAdminInterfaceLocalizer {
  private Map simpleCommentOperations;
  private Map simpleArticleOperations;

  public MirBasicAdminInterfaceLocalizer() throws MirLocalizerFailure, MirLocalizerException {
    simpleCommentOperations = new HashMap();
    simpleArticleOperations = new HashMap();

    buildSimpleCommentOperations(simpleCommentOperations);
    buildSimpleArticleOperations(simpleArticleOperations);
  }

  public Map simpleCommentOperations() {
    return simpleCommentOperations;
  };

  public Map simpleArticleOperations() {
    return simpleArticleOperations;
  };

  public void buildSimpleCommentOperations(Map anOperations) throws MirLocalizerFailure, MirLocalizerException {
    anOperations.put("hide", new HideCommentOperation());
    anOperations.put("unhide", new UnhideCommentOperation());
  };

  public void buildSimpleArticleOperations(Map anOperations)  throws MirLocalizerFailure, MirLocalizerException {
    anOperations.put("hide", new HideArticleOperation());
    anOperations.put("unhide", new UnhideArticleOperation());
  };

  protected abstract static class EntityModifyingOperation implements MirSimpleEntityOperation {
    public boolean isAvailable(EntityAdapter anEntity) {
      try {

        Entity entity = anEntity.getEntity();
        return (entity instanceof EntityComment) && isAvailable((EntityComment) entity);
      }
      catch (Throwable t) {
        return false;
      }
    };

    public void perform(EntityAdapter anEntity) {
      Entity entity = anEntity.getEntity();
      try {
        performModification(entity);
        entity.update();
      }
      catch (Throwable t) {
      }
    };

    protected abstract boolean isAvailable(Entity anEntity) throws StorageObjectException ;
    protected abstract void performModification(Entity anEntity) throws StorageObjectException ;
  }

  public static abstract class CommentModifyingOperation extends EntityModifyingOperation {
    protected boolean isAvailable(Entity anEntity) throws StorageObjectException {
      return anEntity instanceof EntityComment && isAvailable((EntityComment) anEntity);
    }

    protected void performModification(Entity anEntity) throws StorageObjectException {
      performModification((EntityComment) anEntity);
      DatabaseContent.getInstance().setUnproduced("id="+anEntity.getValue("to_media"));
    };

    protected abstract boolean isAvailable(EntityComment aComment) throws StorageObjectException ;
    protected abstract void performModification(EntityComment aComment) throws StorageObjectException ;
  }

  public static abstract class ArticleModifyingOperation extends EntityModifyingOperation {
    protected boolean isAvailable(Entity anEntity) throws StorageObjectException {
      return anEntity instanceof EntityContent && isAvailable((EntityComment) anEntity);
    }

    protected void performModification(Entity anEntity) throws StorageObjectException {
      performModification((EntityContent) anEntity);
      anEntity.setValueForProperty("is_produced", "0");
    };

    protected abstract boolean isAvailable(EntityContent anArticle) throws StorageObjectException ;
    protected abstract void performModification(EntityContent anArticle) throws StorageObjectException ;
  }

  private static class HideCommentOperation extends CommentModifyingOperation {
    protected boolean isAvailable(EntityComment aComment) {
      return aComment.getValue("is_published").equals("1");
    }
    protected void performModification(EntityComment aComment) throws StorageObjectException {
      aComment.setValueForProperty("is_published", "0");
    }
  }

  private static class UnhideCommentOperation extends CommentModifyingOperation {
    protected boolean isAvailable(EntityComment aComment) {
      return aComment.getValue("is_published").equals("0");
    }
    protected void performModification(EntityComment aComment) throws StorageObjectException {
      aComment.setValueForProperty("is_published", "1");
    }
  }

  private static class HideArticleOperation extends ArticleModifyingOperation {
    protected boolean isAvailable(EntityContent anArticle) {
      return anArticle.getValue("is_published").equals("1");
    }
    protected void performModification(EntityContent anArticle) throws StorageObjectException {
      anArticle.setValueForProperty("is_published", "0");
    }
  }

  private static class UnhideArticleOperation extends ArticleModifyingOperation {
    protected boolean isAvailable(EntityContent anArticle) {
      return anArticle.getValue("is_published").equals("0");
    }
    protected void performModification(EntityContent anArticle) throws StorageObjectException {
      anArticle.setValueForProperty("is_published", "1");
    }
  }
}