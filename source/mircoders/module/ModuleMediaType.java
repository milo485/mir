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

    return (Entity) mediaTypes.elementAt(0);
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