/*
 * BreakingNewsItem.java
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
 * BreakingNewsItem
 * @author idefix
 * @version $Id: BreakingNewsItem.java,v 1.2 2003/08/17 19:13:19 idfx Exp $
 */
public class BreakingNewsItem implements Serializable {

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String text;

    /** persistent field */
    private java.util.Date webdbCreate;

    /** full constructor */
    public BreakingNewsItem(java.lang.String text, java.util.Date webdbCreate) {
        this.text = text;
        this.webdbCreate = webdbCreate;
    }

    /** default constructor */
    public BreakingNewsItem() {
    }

    public java.lang.Integer getId() {
        return this.id;
    }

    public void setId(java.lang.Integer id) {
        this.id = id;
    }

    public java.lang.String getText() {
        return this.text;
    }

    public void setText(java.lang.String text) {
        this.text = text;
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
        if ( !(other instanceof BreakingNewsItem) ) return false;
        BreakingNewsItem castOther = (BreakingNewsItem) other;
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
