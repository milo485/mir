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
package mircoders.localizer.basic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.config.MirPropertiesConfiguration;
import mir.entity.Entity;
import mir.entity.adapter.EntityAdapter;
import mir.entity.adapter.EntityAdapterDefinition;
import mir.entity.adapter.EntityAdapterModel;
import mir.log.LoggerWrapper;
import mir.media.MediaHelper;
import mir.media.MirMedia;
import mir.util.ParameterExpander;
import mir.util.RewindableIterator;
import mircoders.entity.EntityUploadedMedia;
import mircoders.global.MirGlobal;
import mircoders.localizer.MirAdminInterfaceLocalizer;
import mircoders.localizer.MirDataModelLocalizer;
import mircoders.localizer.MirLocalizerExc;
import mircoders.localizer.MirLocalizerFailure;
import mircoders.storage.DatabaseArticleType;
import mircoders.storage.DatabaseAudio;
import mircoders.storage.DatabaseBreaking;
import mircoders.storage.DatabaseComment;
import mircoders.storage.DatabaseCommentStatus;
import mircoders.storage.DatabaseContent;
import mircoders.storage.DatabaseImageType;
import mircoders.storage.DatabaseImages;
import mircoders.storage.DatabaseLanguage;
import mircoders.storage.DatabaseMedia;
import mircoders.storage.DatabaseMediaType;
import mircoders.storage.DatabaseMediafolder;
import mircoders.storage.DatabaseMessages;
import mircoders.storage.DatabaseOther;
import mircoders.storage.DatabaseTopics;
import mircoders.storage.DatabaseUploadedMedia;
import mircoders.storage.DatabaseUsers;
import mircoders.storage.DatabaseVideo;

public class MirBasicDataModelLocalizer implements MirDataModelLocalizer {
  private EntityAdapterModel model;
  protected LoggerWrapper logger;
  protected MirPropertiesConfiguration configuration;

  public MirBasicDataModelLocalizer() throws MirLocalizerFailure, MirLocalizerExc {
    model=null;
    logger = new LoggerWrapper("Localizer.DataModel");

    try {
      configuration = MirPropertiesConfiguration.instance();
    }
    catch (Throwable e) {
      throw new MirLocalizerFailure("Can't get configuration: " + e.getMessage(), e);
    }
  }

  public EntityAdapterModel adapterModel() throws MirLocalizerFailure {
    if (model==null)
      model = buildModel();

    return model;
  };

  protected void constructContentAdapterDefinition(EntityAdapterDefinition anEntityAdapterDefinition) throws MirLocalizerFailure {
    try {
      anEntityAdapterDefinition.addDBDateField("creationdate", "webdb_create");
      anEntityAdapterDefinition.addDBDateField("changedate", "webdb_lastchange");
      anEntityAdapterDefinition.addMirDateField("date", "date");
      anEntityAdapterDefinition.addCalculatedField("to_topics", new ContentToTopicsField());
      anEntityAdapterDefinition.addCalculatedField("to_comments", new ContentToCommentsField());
      anEntityAdapterDefinition.addCalculatedField("language", new ContentToLanguageField());

      anEntityAdapterDefinition.addCalculatedField("commentcount", new ContentCommentCountField(" and is_published='1'"));
      anEntityAdapterDefinition.addCalculatedField("fullcommentcount", new ContentCommentCountField(""));

      anEntityAdapterDefinition.addCalculatedField("to_uploaded_media", new ContentToMediaField( "uploadedMedia" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_images",  new ContentToMediaField( "image" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_audio", new ContentToMediaField( "audio" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_video", new ContentToMediaField( "video" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_other", new ContentToMediaField( "otherMedia" ));
      anEntityAdapterDefinition.addCalculatedField("to_all_uploaded_media", new ContentToMediaField( "uploadedMedia", false));
      anEntityAdapterDefinition.addCalculatedField("to_all_media_images",  new ContentToMediaField( "image", false));
      anEntityAdapterDefinition.addCalculatedField("to_all_media_audio", new ContentToMediaField( "audio", false));
      anEntityAdapterDefinition.addCalculatedField("to_all_media_video", new ContentToMediaField( "video", false));
      anEntityAdapterDefinition.addCalculatedField("to_all_media_other", new ContentToMediaField( "otherMedia", false));
      anEntityAdapterDefinition.addCalculatedField("to_media_icon", new ContentToIconField());

      anEntityAdapterDefinition.addCalculatedField("article_type", new ContentToArticleTypeField());

      anEntityAdapterDefinition.addCalculatedField("description_parsed", new FilteredField("description"));
      anEntityAdapterDefinition.addCalculatedField("content_data_parsed", new FilteredField("content_data"));

      anEntityAdapterDefinition.addCalculatedField("children", new ContentToChildrenField());
      anEntityAdapterDefinition.addCalculatedField("parent", new ContentToParentField());

      anEntityAdapterDefinition.addCalculatedField("publicurl", new ExpandedField(configuration.getString("Article.PublicUrl")));

      anEntityAdapterDefinition.addCalculatedField("operations",
          new EntityToSimpleOperationsField(MirGlobal.localizer().adminInterface().simpleArticleOperations()));
    }
    catch (Throwable t) {
      throw new MirLocalizerFailure(t.getMessage(), t);
    }
  }

  protected void constructCommentAdapterDefinition(EntityAdapterDefinition anEntityAdapterDefinition) throws MirLocalizerFailure {
    try {
      anEntityAdapterDefinition.addDBDateField("creationdate", "webdb_create");
      anEntityAdapterDefinition.addCalculatedField("to_content", new CommentToContentField());
      anEntityAdapterDefinition.addCalculatedField("status", new CommentToStatusField());

      anEntityAdapterDefinition.addCalculatedField("to_uploaded_media", new CommentToMediaField( "uploadedMedia" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_images",  new CommentToMediaField( "image" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_audio", new CommentToMediaField( "audio" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_video", new CommentToMediaField( "video" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_other", new CommentToMediaField( "otherMedia" ));
      anEntityAdapterDefinition.addCalculatedField("to_all_uploaded_media", new CommentToMediaField( "uploadedMedia", false));
      anEntityAdapterDefinition.addCalculatedField("to_all_media_images",  new CommentToMediaField( "image", false));
      anEntityAdapterDefinition.addCalculatedField("to_all_media_audio", new CommentToMediaField( "audio", false));
      anEntityAdapterDefinition.addCalculatedField("to_all_media_video", new CommentToMediaField( "video", false));
      anEntityAdapterDefinition.addCalculatedField("to_all_media_other", new CommentToMediaField( "otherMedia", false));

      anEntityAdapterDefinition.addCalculatedField("publicurl", new ExpandedField(configuration.getString("Comment.PublicUrl")));

      anEntityAdapterDefinition.addCalculatedField("description_parsed", new FilteredField("description"));
      anEntityAdapterDefinition.addCalculatedField("operations",
          new EntityToSimpleOperationsField(MirGlobal.localizer().adminInterface().simpleCommentOperations()));
    }
    catch (Throwable t) {
      throw new MirLocalizerFailure(t.getMessage(), t);
    }
  }

  protected EntityAdapterModel buildModel() throws MirLocalizerFailure {
    EntityAdapterModel result = new EntityAdapterModel();

    try {
      EntityAdapterDefinition definition;

      definition = new EntityAdapterDefinition();
      constructContentAdapterDefinition( definition );
      result.addMapping( "content", DatabaseContent.getInstance(), definition);

      definition = new EntityAdapterDefinition();
      constructCommentAdapterDefinition( definition );
      result.addMapping( "comment", DatabaseComment.getInstance(), definition);

      result.addMapping( "articleType", DatabaseArticleType.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "commentStatus", DatabaseCommentStatus.getInstance(), new EntityAdapterDefinition());

      definition = new EntityAdapterDefinition();
      definition.addDBDateField("creationdate", "webdb_create");
      result.addMapping( "breakingNews", DatabaseBreaking.getInstance(), definition);

      result.addMapping( "imageType", DatabaseImageType.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "language", DatabaseLanguage.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "mediaFolder", DatabaseMediafolder.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "mediaType", DatabaseMediaType.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "internalMessage", DatabaseMessages.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "topic", DatabaseTopics.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "user", DatabaseUsers.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "media", DatabaseMedia.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "uploadedMedia", DatabaseUploadedMedia.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "image", DatabaseImages.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "audio", DatabaseAudio.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "video", DatabaseVideo.getInstance(), new EntityAdapterDefinition());
      result.addMapping( "otherMedia", DatabaseOther.getInstance(), new EntityAdapterDefinition());
    }
    catch (Throwable t) {
      throw new MirLocalizerFailure(t.getMessage(), t);
    }

    return result;
  }

  protected class CommentToContentField implements EntityAdapterDefinition.CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return anEntityAdapter.getToOneRelation(
                    "id="+anEntityAdapter.get("to_media"),
                    "id",
                    "content" );
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class CommentToStatusField implements EntityAdapterDefinition.CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return anEntityAdapter.getToOneRelation(
                    "id="+anEntityAdapter.get("to_comment_status"),
                    "id",
                    "commentStatus" );
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class EntityToSimpleOperationsField implements EntityAdapterDefinition.CalculatedField {
    private List operations;

    public EntityToSimpleOperationsField(List anOperations) {
      operations = anOperations;
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        Iterator i = operations.iterator();
        List availableOperations = new Vector();

        while (i.hasNext()) {
          MirAdminInterfaceLocalizer.MirSimpleEntityOperation operation =
            (MirAdminInterfaceLocalizer.MirSimpleEntityOperation) i.next();

          if (operation.isAvailable(anEntityAdapter)) {
            availableOperations.add(operation.getName());
          }
        };

        return availableOperations;
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class FilteredField implements EntityAdapterDefinition.CalculatedField {
    private String fieldName;

    public FilteredField(String aFieldName) {
      fieldName = aFieldName;
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        if (anEntityAdapter.get("is_html")!=null && anEntityAdapter.get("is_html").equals("1")) {
          return MirGlobal.localizer().producerAssistant().filterHTMLText((String) anEntityAdapter.get(fieldName));
        }
        else {
          return MirGlobal.localizer().producerAssistant().filterNonHTMLText((String) anEntityAdapter.get(fieldName));
        }
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class ExpandedField implements EntityAdapterDefinition.CalculatedField {
    private String expression;

    public ExpandedField(String anExpression) {
      expression = anExpression;
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return ParameterExpander.expandExpression(anEntityAdapter, expression);
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class EvaluatedField implements EntityAdapterDefinition.CalculatedField {
    private String expression;

    public EvaluatedField(String anExpression) {
      expression = anExpression;
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return ParameterExpander.evaluateExpression(anEntityAdapter, expression);
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class ContentToParentField implements EntityAdapterDefinition.CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        logger.debug("ContentToParentField.getValue");
        return anEntityAdapter.getToOneRelation(
                    "id="+anEntityAdapter.get("to_content"),
                    "id",
                    "content" );
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class ContentToChildrenField implements EntityAdapterDefinition.CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return anEntityAdapter.getRelation(
                    "to_content="+anEntityAdapter.get("id"),
                    "id",
                    "content" );
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class ContentToLanguageField implements EntityAdapterDefinition.CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return anEntityAdapter.getToOneRelation(
                    "id="+anEntityAdapter.get("to_language"),
                    "id",
                    "language" );
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class ContentToArticleTypeField implements EntityAdapterDefinition.CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return anEntityAdapter.getToOneRelation(
                    "id="+anEntityAdapter.get("to_article_type"),
                    "id",
                    "articleType" );
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class ContentToCommentsField implements EntityAdapterDefinition.CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return anEntityAdapter.getRelation(
                    "to_media="+anEntityAdapter.get("id")+" and is_published='1'",
                    "webdb_create",
                    "comment" );
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class ContentToTopicsField implements EntityAdapterDefinition.CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return anEntityAdapter.getRelation(
                    "exists (select * from content_x_topic where content_id="+anEntityAdapter.get("id")+" and topic_id=id)",
                    "title",
                    "topic" );
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class ContentToMediaField implements EntityAdapterDefinition.CalculatedField {
    private String definition;
    private boolean published;

    public ContentToMediaField(String aDefinition, boolean anOnlyPublished) {
      definition = aDefinition;
      published = anOnlyPublished;
    }

    public ContentToMediaField(String aDefinition) {
      this(aDefinition, true);
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        String condition = "exists (select * from content_x_media where content_id="+anEntityAdapter.get("id")+" and media_id=id)";
        if (published)
          condition = "is_published='t' and " + condition;
        return anEntityAdapter.getRelation(
           condition,
          "id",
          definition);
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class CommentToMediaField implements EntityAdapterDefinition.CalculatedField {
    private String definition;
    private boolean published;

    public CommentToMediaField(String aDefinition, boolean anOnlyPublished) {
      definition = aDefinition;
      published = anOnlyPublished;
    }

    public CommentToMediaField(String aDefinition) {
      this(aDefinition, true);
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        String condition = "exists (select * from comment_x_media where comment_id="+anEntityAdapter.get("id")+" and media_id=id)";
        if (published)
          condition = "is_published='t' and " + condition;
        return anEntityAdapter.getRelation(
           condition,
          "id",
          definition);
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }

  protected class ContentToIconField implements EntityAdapterDefinition.CalculatedField {
    public Object getValue(EntityAdapter anEntityAdapter) {
      EntityAdapter media;
      Entity mediaType;
      RewindableIterator iterator;
      Map result;
      MirMedia mediaHandler;
      String tinyIcon;
      String iconAlt;

      try {
        iterator = (RewindableIterator) (anEntityAdapter.get("to_uploaded_media"));
        iterator.rewind();

        tinyIcon = MirGlobal.config().getString("Producer.Icon.TinyText");
        iconAlt = "Text";

        if (iterator.hasNext()) {
          media = (EntityAdapter) iterator.next();

          mediaType = ((EntityUploadedMedia) (media.getEntity())).getMediaType();
          mediaHandler = MediaHelper.getHandler( mediaType );

          if (mediaHandler.isVideo()) {
            tinyIcon = MirGlobal.config().getString("Producer.Icon.TinyVideo");
            iconAlt = "Video";
          }
          else if (mediaHandler.isAudio()) {
            tinyIcon = MirGlobal.config().getString("Producer.Icon.TinyAudio");
            iconAlt = "Audio";
          }
          else if (mediaHandler.isImage()) {
            tinyIcon = MirGlobal.config().getString("Producer.Icon.TinyImage");
            iconAlt = "Image";
          }
          else {
            tinyIcon = mediaHandler.getTinyIconName();
            iconAlt = mediaHandler.getIconAltName();
          }

        }
      }
      catch (Throwable t) {
        logger.error("ContentToIconField: " +t.getMessage());
        throw new RuntimeException(t.getMessage());
      }

      result = new HashMap();
      result.put("tiny_icon", MirGlobal.config().getString("Producer.ImageRoot") + "/" + tinyIcon);
      result.put("icon_alt", iconAlt);

      return result;
    }
  }

  protected class ContentCommentCountField implements EntityAdapterDefinition.CalculatedField {
    private String extraCondition;

    public ContentCommentCountField(String anExtraCondition) {
      super();

      extraCondition = anExtraCondition;
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return Integer.toString(
            DatabaseComment.getInstance().getSize(
                  "to_media="+anEntityAdapter.get("id")+" " + extraCondition));
      }
      catch (Throwable t) {
        throw new RuntimeException(t.getMessage());
      }
    }
  }
}
