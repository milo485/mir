/*
 * Comment.java
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
 * Comment
 * @author idefix
 * @version $Id: Comment.java,v 1.2 2003/08/17 19:13:19 idfx Exp $
 */
public class Comment implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String title;

    /** persistent field */
    private String creator;

    /** persistent field */
    private String description;

    /** nullable persistent field */
    private String mainUrl;

    /** nullable persistent field */
    private String email;

    /** nullable persistent field */
    private String address;

    /** nullable persistent field */
    private String phone;

    /** persistent field */
    private java.util.Date webdbCreate;

    /** persistent field */
    private boolean isPublished;

    /** persistent field */
    private int toLanguage;

    /** persistent field */
    private int toMedia;

    /** nullable persistent field */
    private short toCommentStatus;

    /** nullable persistent field */
    private int checksum;

    /** persistent field */
    private boolean isHtml;

    /** full constructor */
    public Comment(java.lang.Integer id, java.lang.String title, java.lang.String creator, java.lang.String description, java.lang.String mainUrl, java.lang.String email, java.lang.String address, java.lang.String phone, java.util.Date webdbCreate, boolean isPublished, int toLanguage, int toMedia, short toCommentStatus, int checksum, boolean isHtml) {
        this.id = id;
        this.title = title;
        this.creator = creator;
        this.description = description;
        this.mainUrl = mainUrl;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.webdbCreate = webdbCreate;
        this.isPublished = isPublished;
        this.toLanguage = toLanguage;
        this.toMedia = toMedia;
        this.toCommentStatus = toCommentStatus;
        this.checksum = checksum;
        this.isHtml = isHtml;
    }

    /** default constructor */
    public Comment() {
    }

    /** minimal constructor */
    public Comment(java.lang.Integer id, java.lang.String title, java.lang.String creator, java.lang.String description, java.util.Date webdbCreate, boolean isPublished, int toLanguage, int toMedia, boolean isHtml) {
        this.id = id;
        this.title = title;
        this.creator = creator;
        this.description = description;
        this.webdbCreate = webdbCreate;
        this.isPublished = isPublished;
        this.toLanguage = toLanguage;
        this.toMedia = toMedia;
        this.isHtml = isHtml;
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

    public java.lang.String getCreator() {
        return this.creator;
    }

    public void setCreator(java.lang.String creator) {
        this.creator = creator;
    }

    public java.lang.String getDescription() {
        return this.description;
    }

    public void setDescription(java.lang.String description) {
        this.description = description;
    }

    public java.lang.String getMainUrl() {
        return this.mainUrl;
    }

    public void setMainUrl(java.lang.String mainUrl) {
        this.mainUrl = mainUrl;
    }

    public java.lang.String getEmail() {
        return this.email;
    }

    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    public java.lang.String getAddress() {
        return this.address;
    }

    public void setAddress(java.lang.String address) {
        this.address = address;
    }

    public java.lang.String getPhone() {
        return this.phone;
    }

    public void setPhone(java.lang.String phone) {
        this.phone = phone;
    }

    public java.util.Date getWebdbCreate() {
        return this.webdbCreate;
    }

    public void setWebdbCreate(java.util.Date webdbCreate) {
        this.webdbCreate = webdbCreate;
    }

    public boolean isIsPublished() {
        return this.isPublished;
    }

    public void setIsPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }

    public int getToLanguage() {
        return this.toLanguage;
    }

    public void setToLanguage(int toLanguage) {
        this.toLanguage = toLanguage;
    }

    public int getToMedia() {
        return this.toMedia;
    }

    public void setToMedia(int toMedia) {
        this.toMedia = toMedia;
    }

    public short getToCommentStatus() {
        return this.toCommentStatus;
    }

    public void setToCommentStatus(short toCommentStatus) {
        this.toCommentStatus = toCommentStatus;
    }

    public int getChecksum() {
        return this.checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public boolean isIsHtml() {
        return this.isHtml;
    }

    public void setIsHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Comment) ) return false;
        Comment castOther = (Comment) other;
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
