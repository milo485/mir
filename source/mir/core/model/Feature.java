/*
 * Feature.java
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Feature
 * @author idefix
 * @version $Id: Feature.java,v 1.2 2003/08/17 19:13:19 idfx Exp $
 */
public class Feature implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String title;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private String filename;

    /** nullable persistent field */
    private String mainUrl;

    /** persistent field */
    private boolean isPublished;

    /** full constructor */
    public Feature(java.lang.String title, java.lang.String description, java.lang.String filename, java.lang.String mainUrl, boolean isPublished) {
        this.title = title;
        this.description = description;
        this.filename = filename;
        this.mainUrl = mainUrl;
        this.isPublished = isPublished;
    }

    /** default constructor */
    public Feature() {
    }

    /** minimal constructor */
    public Feature(java.lang.String title, java.lang.String filename, boolean isPublished) {
        this.title = title;
        this.filename = filename;
        this.isPublished = isPublished;
    }

    public java.lang.Integer getId() {
        return this.id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.String getTitle() {
        return this.title;
    }

    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    public java.lang.String getDescription() {
        return this.description;
    }

    public void setDescription(java.lang.String description) {
        this.description = description;
    }

    public java.lang.String getFilename() {
        return this.filename;
    }

    public void setFilename(java.lang.String filename) {
        this.filename = filename;
    }

    public java.lang.String getMainUrl() {
        return this.mainUrl;
    }

    public void setMainUrl(java.lang.String mainUrl) {
        this.mainUrl = mainUrl;
    }

    public boolean isIsPublished() {
        return this.isPublished;
    }

    public void setIsPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Feature) ) return false;
        Feature castOther = (Feature) other;
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
