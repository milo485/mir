/*
 * Media.java
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
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Media
 * @author idefix
 * @version $Id: Media.java,v 1.5 2003/09/05 20:23:59 idfx Exp $
 */
public class Media implements Serializable, IMedia {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String title;

    /** nullable persistent field */
    private String subtitle;

    /** nullable persistent field */
    private String edittitle;

    /** persistent field */
    private String date;

    /** nullable persistent field */
    private String creator;

    /** nullable persistent field */
    private String creatorMainUrl;

    /** nullable persistent field */
    private String creatorEmail;

    /** nullable persistent field */
    private String creatorAddress;

    /** nullable persistent field */
    private String creatorPhone;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String keywords;

    /** nullable persistent field */
    private String comment;

    /** nullable persistent field */
    private String source;

    /** nullable persistent field */
    private Date publishDate;

    /** nullable persistent field */
    private String publishServer;

    /** nullable persistent field */
    private String publishPath;

    /** persistent field */
    private boolean isPublished;

    /** persistent field */
    private boolean isProduced;

    /** persistent field */
    private Date webdbCreate;

    /** nullable persistent field */
    private Date webdbLastchange;

    /** nullable persistent field */
    private mir.core.model.Feature feature;

    /** nullable persistent field */
    private MirUser publisher;

    /** nullable persistent field */
    private Language language;

    /** nullable persistent field */
    private Rights rights;
    
    private Set content;

    /** default constructor */
    public Media() {
    
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

    public java.lang.String getSubtitle() {
        return this.subtitle;
    }

    public void setSubtitle(java.lang.String subtitle) {
        this.subtitle = subtitle;
    }

    public java.lang.String getEdittitle() {
        return this.edittitle;
    }

    public void setEdittitle(java.lang.String edittitle) {
        this.edittitle = edittitle;
    }

    public java.lang.String getDate() {
        return this.date;
    }

    public void setDate(java.lang.String date) {
        this.date = date;
    }

    public java.lang.String getCreator() {
        return this.creator;
    }

    public void setCreator(java.lang.String creator) {
        this.creator = creator;
    }

    public java.lang.String getCreatorMainUrl() {
        return this.creatorMainUrl;
    }

    public void setCreatorMainUrl(java.lang.String creatorMainUrl) {
        this.creatorMainUrl = creatorMainUrl;
    }

    public java.lang.String getCreatorEmail() {
        return this.creatorEmail;
    }

    public void setCreatorEmail(java.lang.String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public java.lang.String getCreatorAddress() {
        return this.creatorAddress;
    }

    public void setCreatorAddress(java.lang.String creatorAddress) {
        this.creatorAddress = creatorAddress;
    }

    public java.lang.String getCreatorPhone() {
        return this.creatorPhone;
    }

    public void setCreatorPhone(java.lang.String creatorPhone) {
        this.creatorPhone = creatorPhone;
    }

    public java.lang.String getDescription() {
        return this.description;
    }

    public void setDescription(java.lang.String description) {
        this.description = description;
    }

    public java.lang.String getKeywords() {
        return this.keywords;
    }

    public void setKeywords(java.lang.String keywords) {
        this.keywords = keywords;
    }

    public java.lang.String getComment() {
        return this.comment;
    }

    public void setComment(java.lang.String comment) {
        this.comment = comment;
    }

    public java.lang.String getSource() {
        return this.source;
    }

    public void setSource(java.lang.String source) {
        this.source = source;
    }

    public java.util.Date getPublishDate() {
        return this.publishDate;
    }

    public void setPublishDate(java.util.Date publishDate) {
        this.publishDate = publishDate;
    }

    public java.lang.String getPublishServer() {
        return this.publishServer;
    }

    public void setPublishServer(java.lang.String publishServer) {
        this.publishServer = publishServer;
    }

    public java.lang.String getPublishPath() {
        return this.publishPath;
    }

    public void setPublishPath(java.lang.String publishPath) {
        this.publishPath = publishPath;
    }

    public java.util.Date getWebdbCreate() {
        return this.webdbCreate;
    }

    public void setWebdbCreate(java.util.Date webdbCreate) {
        this.webdbCreate = webdbCreate;
    }

    public java.util.Date getWebdbLastchange() {
        return this.webdbLastchange;
    }

    public void setWebdbLastchange(java.util.Date webdbLastchange) {
        this.webdbLastchange = webdbLastchange;
    }

    public mir.core.model.Feature getFeature() {
        return this.feature;
    }

    public void setFeature(mir.core.model.Feature feature) {
        this.feature = feature;
    }

    public mir.core.model.MirUser getPublisher() {
        return this.publisher;
    }

    public void setPublisher(mir.core.model.MirUser publisher) {
        this.publisher = publisher;
    }

    public mir.core.model.Language getLanguage() {
        return this.language;
    }

    public void setLanguage(mir.core.model.Language language) {
        this.language = language;
    }

    public mir.core.model.Rights getRights() {
        return this.rights;
    }

    public void setRights(mir.core.model.Rights rights) {
        this.rights = rights;
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

		/**
		 * @return
		 */
		public boolean isProduced() {
			return isProduced;
		}

		/**
		 * @param isProduced
		 */
		public void setProduced(boolean isProduced) {
			this.isProduced = isProduced;
		}

		/**
		 * @return
		 */
		public boolean isPublished() {
			return isPublished;
		}

		/**
		 * @param isPublished
		 */
		public void setPublished(boolean isPublished) {
			this.isPublished = isPublished;
		}

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Media) ) return false;
        Media castOther = (Media) other;
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
