/*
 * Content.java
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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Content
 * @author idefix
 * @version $Id: Content.java,v 1.3 2003/08/19 00:41:54 idfx Exp $
 */
public class Content extends Media implements Serializable, IContent {

    /** persistent field */
    private boolean isHtml;

    /** nullable persistent field */
    private String contentData;

    /** persistent field */
    private ArticleType articleType;

    /** nullable persistent field */
    private mir.core.model.Content parentContent;

    /** persistent field */
    private Set childContent;

    /** persistent field */
    private Set topics;

    /** persistent field */
    private Set attachedMedias;

    /** default constructor */
    public Content() {
    }

    public boolean isIsHtml() {
        return this.isHtml;
    }

    public void setIsHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }

    public java.lang.String getContentData() {
        return this.contentData;
    }

    public void setContentData(java.lang.String contentData) {
        this.contentData = contentData;
    }

    public ArticleType getArticleType() {
        return this.articleType;
    }

    public void setArticleType(ArticleType articleType) {
        this.articleType = articleType;
    }

    public mir.core.model.Content getParentContent() {
        return this.parentContent;
    }

    public void setParentContent(mir.core.model.Content parentContent) {
        this.parentContent = parentContent;
    }

    public Set getChildContent() {
        return this.childContent;
    }

    public void setChildContent(Set childContent) {
        this.childContent = childContent;
    }

    public Set getTopics() {
        return this.topics;
    }

    public void setTopics(Set topics) {
        this.topics = topics;
    }

    public Set getAttachedMedias() {
        return this.attachedMedias;
    }

    public void setAttachedMedias(Set attachedMedias) {
        this.attachedMedias = attachedMedias;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

}
