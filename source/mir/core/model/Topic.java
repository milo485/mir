/*
 * Topic.java
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
 * Topic
 * @author idefix
 * @version $Id: Topic.java,v 1.2 2003/08/17 19:13:19 idfx Exp $
 */
public class Topic implements Serializable {

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

    /** nullable persistent field */
    private String archivUrl;

    /** nullable persistent field */
    private mir.core.model.Topic parentTopic;
    
    private Set content;

    /** default constructor */
    public Topic() {
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

    public java.lang.String getArchivUrl() {
        return this.archivUrl;
    }

    public void setArchivUrl(java.lang.String archivUrl) {
        this.archivUrl = archivUrl;
    }

    public mir.core.model.Topic getParentTopic() {
        return this.parentTopic;
    }

    public void setParentTopic(mir.core.model.Topic parentTopic) {
        this.parentTopic = parentTopic;
    }

		/**
		 * @return
		 */
		public Set getContent() {
			return content;
		}

		/**
		 * @param content
		 */
		public void setContent(Set content) {
			this.content = content;
		}

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Topic) ) return false;
        Topic castOther = (Topic) other;
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
