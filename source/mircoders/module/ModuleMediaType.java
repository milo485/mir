package mircoders.module;

import java.util.List;

import mir.entity.Entity;
import mir.entity.EntityList;
import mir.log.LoggerWrapper;
import mir.module.AbstractModule;
import mir.module.ModuleExc;
import mir.module.ModuleFailure;
import mir.storage.StorageObject;
import mir.util.JDBCStringRoutines;
import mir.util.StringRoutines;
import mircoders.storage.DatabaseMediaType;

public class ModuleMediaType extends AbstractModule {
  static LoggerWrapper logger = new LoggerWrapper("Module.Content");

  public ModuleMediaType() {
    this (DatabaseMediaType.getInstance());
  }

  public ModuleMediaType(StorageObject aStorage) {
    this.theStorage = aStorage;
  }

  public Entity findMediaTypeForMimeType(String aMimeType) throws ModuleExc, ModuleFailure {
    List contentTypeParts = StringRoutines.splitString(aMimeType, "/");

    if (contentTypeParts.size()!=2) {
      throw new InvalidMimeTypeExc("Invalid mimetype: " + aMimeType, aMimeType);
    }
    String mimeTypeMajor = (String) contentTypeParts.get(0);

    EntityList mediaTypes;

    mediaTypes = DatabaseMediaType.getInstance().selectByWhereClause("mime_type = '"+JDBCStringRoutines.escapeStringLiteral(aMimeType)+"'");
    if (mediaTypes.size() == 0) {
      mediaTypes = DatabaseMediaType.getInstance().selectByWhereClause("mime_type = '"+JDBCStringRoutines.escapeStringLiteral(mimeTypeMajor+"/*")+"'");
    }
    if (mediaTypes.size() == 0) {
      throw new UnsupportedMimeTypeExc("Unsupported mimetype: " + aMimeType, aMimeType);
    }

    return (Entity) mediaTypes.get(0);
  }

  public static class MimeTypeExc extends ModuleExc {
    private String mimeType;

    public MimeTypeExc(String aMessage, String aMimeType) {
      super (aMessage);
      mimeType = aMimeType;
    }

    public String getMimeType() {
      return mimeType;
    }
  }

  public static class UnsupportedMimeTypeExc extends MimeTypeExc {
    public UnsupportedMimeTypeExc(String aMessage, String aMimeType) {
      super(aMessage, aMimeType);
    }
  }

  public static class InvalidMimeTypeExc extends MimeTypeExc {
    public InvalidMimeTypeExc(String aMessage, String aMimeType) {
      super(aMessage, aMimeType);
    }
  }
}