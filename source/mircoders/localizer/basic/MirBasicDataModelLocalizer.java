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
import mir.entity.*;
import mir.entity.adapter.*;
import mir.media.*;
import mir.misc.*;
import mir.util.*;
import mircoders.storage.*;
import mircoders.global.*;
import mircoders.entity.*;
import mircoders.localizer.*;

public class MirBasicDataModelLocalizer implements MirDataModelLocalizer {
  private EntityAdapterModel model;

  public MirBasicDataModelLocalizer() {
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

      anEntityAdapterDefinition.addCalculatedField("to_media_images",  new ContentToMediaField( "image" ));
      anEntityAdapterDefinition.addCalculatedField("to_uploaded_media", new ContentToMediaField( "uploadedMedia" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_audio", new ContentToMediaField( "audio" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_video", new ContentToMediaField( "video" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_other", new ContentToMediaField( "otherMedia" ));
      anEntityAdapterDefinition.addCalculatedField("to_media_icon", new ContentToIconField());

      anEntityAdapterDefinition.addCalculatedField("article_type", new ContentToArticleTypeField());

      anEntityAdapterDefinition.addCalculatedField("description_parsed", new FilteredField("description"));
      anEntityAdapterDefinition.addCalculatedField("content_data_parsed", new FilteredField("content_data"));

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

      result.addMapping( "feature", DatabaseFeature.getInstance(), new EntityAdapterDefinition());
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
    String fieldName;

    public FilteredField(String aFieldName) {
      fieldName = aFieldName;
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        if (anEntityAdapter.get("is_html")!=null && anEntityAdapter.get("is_html").equals("1")) {
          return anEntityAdapter.get(fieldName);
        }
        else {
          return MirGlobal.localizer().producerAssistant().filterText((String) anEntityAdapter.get(fieldName));
        }
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
    String definition;

    public ContentToMediaField(String aDefinition) {
      definition = aDefinition;
    }

    public Object getValue(EntityAdapter anEntityAdapter) {
      try {
        return anEntityAdapter.getRelation(
          "exists (select * from content_x_media where content_id="+anEntityAdapter.get("id")+" and media_id=id)",
          "title",
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

        tinyIcon = MirGlobal.getConfigProperty("Producer.Icon.TinyText");
        iconAlt = "Text";

        if (iterator.hasNext()) {
          media = (EntityAdapter) iterator.next();

          mediaType = ((EntityUploadedMedia) (media.getEntity())).getMediaType();
          mediaHandler = MediaHelper.getHandler( mediaType );

          if (mediaHandler.isVideo()) {
            tinyIcon = MirGlobal.getConfigProperty("Producer.Icon.TinyVideo");
            iconAlt = "Video";
          }
          else if (mediaHandler.isAudio()) {
            tinyIcon = MirGlobal.getConfigProperty("Producer.Icon.TinyAudio");
            iconAlt = "Audio";
          }
          else if (mediaHandler.isImage()) {
            tinyIcon = MirGlobal.getConfigProperty("Producer.Icon.TinyImage");
            iconAlt = "Image";
          }
          else {
            tinyIcon = mediaHandler.getTinyIconName();
            iconAlt = mediaHandler.getIconAltName();
          }

        }
      }
      catch (Throwable t) {
        System.out.println("ContentToIconField: exception: " +t.getMessage());
        t.printStackTrace(System.out);
        throw new RuntimeException(t.getMessage());
      }

      result = new HashMap();
      result.put("tiny_icon", MirGlobal.getConfigProperty("Producer.ImageRoot") + "/" + tinyIcon);
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
