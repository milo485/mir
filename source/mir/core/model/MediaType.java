/*
 * MediaType.java
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
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * MediaType
 * @author idefix
 * @version $Id: MediaType.java,v 1.3 2003/08/17 19:13:19 idfx Exp $
 */
public class MediaType implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String name;

    /** persistent field */
    private String mimeType;

    /** persistent field */
    private String classname;

    /** persistent field */
    private String tablename;

    /** nullable persistent field */
    private String dcname;
    
    private Set mediaItems;

    /** full constructor */
    public MediaType(java.lang.String name, java.lang.String mimeType, java.lang.String classname, java.lang.String tablename, java.lang.String dcname) {
        this.name = name;
        this.mimeType = mimeType;
        this.classname = classname;
        this.tablename = tablename;
        this.dcname = dcname;
    }

    /** default constructor */
    public MediaType() {
    }

    /** minimal constructor */
    public MediaType(java.lang.String name, java.lang.String mimeType, java.lang.String classname, java.lang.String tablename) {
        this.name = name;
        this.mimeType = mimeType;
        this.classname = classname;
        this.tablename = tablename;
    }

    public java.lang.Integer getId() {
        return this.id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public java.lang.String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(java.lang.String mimeType) {
        this.mimeType = mimeType;
    }

    public java.lang.String getClassname() {
        return this.classname;
    }

    public void setClassname(java.lang.String classname) {
        this.classname = classname;
    }

    public java.lang.String getTablename() {
        return this.tablename;
    }

    public void setTablename(java.lang.String tablename) {
        this.tablename = tablename;
    }

    public java.lang.String getDcname() {
        return this.dcname;
    }

    public void setDcname(java.lang.String dcname) {
        this.dcname = dcname;
    }

		/**
		 * @return
		 */
		public Set getMediaItems() {
			return mediaItems;
		}

		/**
		 * @param mediaItems
		 */
		public void setMediaItems(Set mediaItems) {
			this.mediaItems = mediaItems;
		}

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof MediaType) ) return false;
        MediaType castOther = (MediaType) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

}
