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

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.entity.Entity;
import mir.entity.adapter.EntityAdapter;
import mir.storage.StorageObjectFailure;
import mir.util.StringRoutines;
import mircoders.entity.EntityComment;
import mircoders.entity.EntityContent;
import mircoders.localizer.MirAdminInterfaceLocalizer;
import mircoders.localizer.MirLocalizerExc;
import mircoders.localizer.MirLocalizerFailure;
import mircoders.storage.DatabaseContent;


public class MirBasicAdminInterfaceLocalizer implements MirAdminInterfaceLocalizer {
  private Vector simpleCommentOperations;
  private Vector simpleArticleOperations;
  private Map simpleCommentOperationsMap;
  private Map simpleArticleOperationsMap;
  private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

  public MirBasicAdminInterfaceLocalizer() throws MirLocalizerFailure, MirLocalizerExc {
    simpleCommentOperations = new Vector();
    simpleArticleOperations = new Vector();
    simpleCommentOperationsMap = new HashMap();
    simpleArticleOperationsMap = new HashMap();

    addSimpleArticleOperation(new ChangeArticleFieldOperation("newswire", "to_article_type", "0", "1", false));
    addSimpleArticleOperation(new ModifyArticleFieldOperation("unhide", "is_published", "1", false));
    addSimpleArticleOperation(new ModifyArticleFieldOperation("hide", "is_published", "0", false));

    addSimpleCommentOperation(new ModifyCommentFieldOperation("unhide", "is_published", "1"));
    addSimpleCommentOperation(new ModifyCommentFieldOperation("hide", "is_published", "0"));
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

    public void perform(EntityAdapter aUser, EntityAdapter anEntity) {
      Entity entity = anEntity.getEntity();
      try {
        performModification(aUser, entity);
        entity.update();
      }
      catch (Throwable t) {
      }
    };

    protected abstract boolean isAvailable(Entity anEntity) throws StorageObjectFailure ;
    protected abstract void performModification(EntityAdapter aUser, Entity anEntity) throws StorageObjectFailure ;
  }

  public static abstract class CommentModifyingOperation extends EntityModifyingOperation {
    public CommentModifyingOperation(String aName) {
      super(aName);
    }

    protected boolean isAvailable(Entity anEntity) throws StorageObjectFailure {
      return anEntity instanceof EntityComment && isAvailable((EntityComment) anEntity);
    }

    protected void performModification(EntityAdapter aUser, Entity anEntity) throws StorageObjectFailure {
      performModification(aUser, (EntityComment) anEntity);
      DatabaseContent.getInstance().setUnproduced("id="+anEntity.getValue("to_media"));
    };

    protected abstract boolean isAvailable(EntityComment aComment) throws StorageObjectFailure ;
    protected abstract void performModification(EntityAdapter aUser, EntityComment aComment) throws StorageObjectFailure ;
  }

  public static abstract class ArticleModifyingOperation extends EntityModifyingOperation {
    private boolean logOperation;

    public ArticleModifyingOperation(String aName, boolean aLogOperation) {
      super(aName);

      logOperation = aLogOperation;
    }

    protected boolean isAvailable(Entity anEntity) throws StorageObjectFailure {
      return anEntity instanceof EntityContent && isAvailable((EntityContent) anEntity);
    }

    protected void performModification(EntityAdapter aUser, Entity anEntity) throws StorageObjectFailure {
      performModification(aUser, (EntityContent) anEntity);
      anEntity.setValueForProperty("is_produced", "0");

      if (logOperation) {
        StringBuffer comment = new StringBuffer();
	try {
          comment.append(StringRoutines.interpretAsString(anEntity.getValue("comment")));
	}
	catch (Throwable t) {
	}
        if (comment.length()>0 && comment.charAt(comment.length()-1)!='\n') {
          comment.append('\n');
        }
        comment.append(dateFormatter.format((new GregorianCalendar()).getTime()));
        comment.append(" ");
        try {
          comment.append(StringRoutines.interpretAsString(aUser.get("login")));
        }
        catch (Throwable t) {
        }
        comment.append(" ");
        comment.append(getName());
        anEntity.setValueForProperty("comment", comment.toString());
      }
    };

    protected abstract boolean isAvailable(EntityContent anArticle) throws StorageObjectFailure ;
    protected abstract void performModification(EntityAdapter aUser, EntityContent anArticle) throws StorageObjectFailure ;
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
      return true;
    }

    protected void performModification(EntityAdapter aUser, EntityComment aComment) throws StorageObjectFailure {
      aComment.setValueForProperty(field, value);
    }
  }

  protected static class ModifyCommentFieldOperation extends CommentModifyingOperation {
    private String field;
    private String value;

    public ModifyCommentFieldOperation(String aName, String aField, String aValue) {
      super(aName);

      field = aField;
      value = aValue;
    }

    protected boolean isAvailable(EntityComment aComment) {
      return aComment.getValue(field) == null || !aComment.getValue(field).equals(value);
    }

    protected void performModification(EntityAdapter aUser, EntityComment aComment) throws StorageObjectFailure {
      aComment.setValueForProperty(field, value);
    }
  }

  protected static class SetArticleFieldOperation extends ArticleModifyingOperation {
    private String field;
    private String value;

    public SetArticleFieldOperation(String aName, String aField, String aValue, boolean aLogOperation) {
      super(aName, aLogOperation);

      field = aField;
      value = aValue;
    }

    protected boolean isAvailable(EntityContent anArticle) {
      return true;
    }

    protected void performModification(EntityAdapter aUser, EntityContent anArticle) throws StorageObjectFailure {
      anArticle.setValueForProperty(field, value);
    }
  }

  protected static class ModifyArticleFieldOperation extends ArticleModifyingOperation {
    private String field;
    private String value;

    public ModifyArticleFieldOperation(String aName, String aField, String aValue, boolean aLogOperation) {
      super(aName, aLogOperation);

      field = aField;
      value = aValue;
    }

    protected boolean isAvailable(EntityContent anArticle) {
      return anArticle.getValue(field) == null || !anArticle.getValue(field).equals(value);
    }

    protected void performModification(EntityAdapter aUser, EntityContent anArticle) throws StorageObjectFailure {
      anArticle.setValueForProperty(field, value);
    }
  }

  protected static class ChangeArticleFieldOperation extends ArticleModifyingOperation {
    private String field;
    private String oldValue;
    private String newValue;

    public ChangeArticleFieldOperation(String aName, String aField, String anOldValue, String aNewValue, boolean aLogOperation) {
      super(aName, aLogOperation);

      field = aField;
      newValue = aNewValue;
      oldValue = anOldValue;
    }

    protected boolean isAvailable(EntityContent anArticle) {
      return anArticle.getValue(field) != null && anArticle.getValue(field).equals(oldValue);
    }

    protected void performModification(EntityAdapter aUser, EntityContent anArticle) throws StorageObjectFailure {
      anArticle.setValueForProperty(field, newValue);
    }
  }
}
