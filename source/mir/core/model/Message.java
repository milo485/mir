/*
 * Message.java
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
 * Message
 * @author idefix
 * @version $Id: Message.java,v 1.2 2003/08/17 19:13:19 idfx Exp $
 */
public class Message implements Serializable {

    /** identifier field */
    private Integer id;

    /** nullable persistent field */
    private String title;

    /** persistent field */
    private String description;

    /** persistent field */
    private String creator;

    /** persistent field */
    private java.util.Date webdbCreate;

    /** full constructor */
    public Message(java.lang.String title, java.lang.String description, java.lang.String creator, java.util.Date webdbCreate) {
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.webdbCreate = webdbCreate;
    }

    /** default constructor */
    public Message() {
    }

    /** minimal constructor */
    public Message(java.lang.String description, java.lang.String creator, java.util.Date webdbCreate) {
        this.description = description;
        this.creator = creator;
        this.webdbCreate = webdbCreate;
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

    public java.lang.String getCreator() {
        return this.creator;
    }

    public void setCreator(java.lang.String creator) {
        this.creator = creator;
    }

    public java.util.Date getWebdbCreate() {
        return this.webdbCreate;
    }

    public void setWebdbCreate(java.util.Date webdbCreate) {
        this.webdbCreate = webdbCreate;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Message) ) return false;
        Message castOther = (Message) other;
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
