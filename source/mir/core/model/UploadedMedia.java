/*
 * UploadedMedia.java
 * 
 * Copyright (C) 2001, 2002, 2003 The Mir-coders group
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
package mir.core.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * UploadedMedia
 * @author idefix
 * @version $Id: UploadedMedia.java,v 1.4 2003/08/19 00:41:54 idfx Exp $
 */
public class UploadedMedia extends Media implements Serializable, IUploadedMedia {

    /** persistent field */
    private boolean iconIsProduced;

    /** nullable persistent field */
    private String iconPath;

		/** nullable persistent field */
		private MediaFolder mediaFolder;

		/** nullable persistent field */
		private MediaType mediaType;

    /** default constructor */
    public UploadedMedia() {
    }

		public boolean isIconIsProduced() {
        return this.iconIsProduced;
    }

    public void setIconIsProduced(boolean iconIsProduced) {
        this.iconIsProduced = iconIsProduced;
    }

    public java.lang.String getIconPath() {
        return this.iconPath;
    }

    public void setIconPath(java.lang.String iconPath) {
        this.iconPath = iconPath;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

		public mir.core.model.MediaFolder getMediaFolder() {
		    return this.mediaFolder;
		}

		public void setMediaFolder(mir.core.model.MediaFolder mediaFolder) {
		    this.mediaFolder = mediaFolder;
		}

		public mir.core.model.MediaType getMediaType() {
		    return this.mediaType;
		}

		public void setMediaType(mir.core.model.MediaType mediaType) {
		    this.mediaType = mediaType;
		}

}
