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
  private Vector simpleCommentOperations;
  private Vector simpleArticleOperations;
  private Map simpleCommentOperationsMap;
  private Map simpleArticleOperationsMap;

  public MirBasicAdminInterfaceLocalizer() throws MirLocalizerFailure, MirLocalizerExc {
    simpleCommentOperations = new Vector();
    simpleArticleOperations = new Vector();
    simpleCommentOperationsMap = new HashMap();
    simpleArticleOperationsMap = new HashMap();

    addSimpleArticleOperation(new ChangeArticleFieldOperation("newswire", "to_article_type", "0", "1"));
    addSimpleArticleOperation(new SetArticleFieldOperation("unhide", "is_published", "1"));
    addSimpleArticleOperation(new SetArticleFieldOperation("hide", "is_published", "0"));

    addSimpleCommentOperation(new SetCommentFieldOperation("unhide", "is_published", "1"));
    addSimpleCommentOperation(new SetCommentFieldOperation("hide", "is_published", "0"));
  }

  public List simpleCommentOperations() {
    return simpleCommentOperations;
  };

  public List simpleArticleOperations() {
    return simpleArticleOperations;
  };

  public MirSimpleEntityOperation simpleArticleOperationForName(String aName) {
    return (MirSimpleEntityOperation) simpleArticleOperationsMap.get(aName);
  };

  public MirSimpleEntityOperation simpleCommentOperationForName(String aName) {
    return (MirSimpleEntityOperation) simpleCommentOperationsMap.get(aName);
  };

  public void removeSimpleArticleOperation(String aName) {
    simpleArticleOperations.remove(simpleArticleOperationsMap.get(aName));
    simpleArticleOperationsMap.remove(aName);
  }

  public void addSimpleArticleOperation(MirSimpleEntityOperation anOperation) {
    removeSimpleArticleOperation(anOperation.getName());
    simpleArticleOperationsMap.put(anOperation.getName(), anOperation);
    simpleArticleOperations.add(anOperation);
  }

  public void removeSimpleCommentOperation(String aName) {
    simpleCommentOperations.remove(simpleCommentOperationsMap.get(aName));
    simpleCommentOperationsMap.remove(aName);
  }

  public void addSimpleCommentOperation(MirSimpleEntityOperation anOperation) {
    removeSimpleCommentOperation(anOperation.getName());
    simpleCommentOperationsMap.put(anOperation.getName(), anOperation);
    simpleCommentOperations.add(anOperation);
  }

  protected abstract static class EntityModifyingOperation implements MirSimpleEntityOperation {
    private String name;

    protected EntityModifyingOperation(String aName) {
      name = aName;
    }

    public String getName() {
      return name;
    };

    public boolean isAvailable(EntityAdapter anEntity) {
      try {
        Entity entity = anEntity.getEntity();
        return isAvailable(entity);
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
    public CommentModifyingOperation(String aName) {
      super(aName);
    }

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
    public ArticleModifyingOperation(String aName) {
      super(aName);
    }

    protected boolean isAvailable(Entity anEntity) throws StorageObjectException {
      return anEntity instanceof EntityContent && isAvailable((EntityContent) anEntity);
    }

    protected void performModification(Entity anEntity) throws StorageObjectException {
      performModification((EntityContent) anEntity);
      anEntity.setValueForProperty("is_produced", "0");
    };

    protected abstract boolean isAvailable(EntityContent anArticle) throws StorageObjectException ;
    protected abstract void performModification(EntityContent anArticle) throws StorageObjectException ;
  }

  protected static class SetCommentFieldOperation extends CommentModifyingOperation {
    private String field;
    private String value;

    public SetCommentFieldOperation(String aName, String aField, String aValue) {
      super(aName);

      field = aField;
      value = aValue;
    }

    protected boolean isAvailable(EntityComment aComment) {
      return aComment.getValue(field) == null || !aComment.getValue(field).equals(value);
    }

    protected void performModification(EntityComment aComment) throws StorageObjectException {
      aComment.setValueForProperty(field, value);
    }
  }

  protected static class SetArticleFieldOperation extends ArticleModifyingOperation {
    private String field;
    private String value;

    public SetArticleFieldOperation(String aName, String aField, String aValue) {
      super(aName);

      field = aField;
      value = aValue;
    }

    protected boolean isAvailable(EntityContent anArticle) {
      return anArticle.getValue(field) == null || !anArticle.getValue(field).equals(value);
    }

    protected void performModification(EntityContent anArticle) throws StorageObjectException {
      anArticle.setValueForProperty(field, value);
    }
  }

  protected static class ChangeArticleFieldOperation extends ArticleModifyingOperation {
    private String field;
    private String oldValue;
    private String newValue;

    public ChangeArticleFieldOperation(String aName, String aField, String anOldValue, String aNewValue) {
      super(aName);

      field = aField;
      newValue = aNewValue;
      oldValue = anOldValue;
    }

    protected boolean isAvailable(EntityContent anArticle) {
      return anArticle.getValue(field) != null && anArticle.getValue(field).equals(oldValue);
    }

    protected void performModification(EntityContent anArticle) throws StorageObjectException {
      anArticle.setValueForProperty(field, newValue);
    }
  }
}