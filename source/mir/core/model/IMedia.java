/*
 * IMedia.java created on 18.08.2003
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

import java.util.Date;
import java.util.Set;

/**
 * IMedia
 * @author idefix
 * @version $Id: IMedia.java,v 1.1 2003/08/19 00:41:54 idfx Exp $
 */
public interface IMedia {
	public abstract java.lang.Integer getId();
	public abstract void setId(java.lang.Integer id);
	public abstract java.lang.String getTitle();
	public abstract void setTitle(java.lang.String title);
	public abstract java.lang.String getSubtitle();
	public abstract void setSubtitle(java.lang.String subtitle);
	public abstract java.lang.String getEdittitle();
	public abstract void setEdittitle(java.lang.String edittitle);
	public abstract java.lang.String getDate();
	public abstract void setDate(java.lang.String date);
	public abstract java.lang.String getCreator();
	public abstract void setCreator(java.lang.String creator);
	public abstract java.lang.String getCreatorMainUrl();
	public abstract void setCreatorMainUrl(java.lang.String creatorMainUrl);
	public abstract java.lang.String getCreatorEmail();
	public abstract void setCreatorEmail(java.lang.String creatorEmail);
	public abstract java.lang.String getCreatorAddress();
	public abstract void setCreatorAddress(java.lang.String creatorAddress);
	public abstract java.lang.String getCreatorPhone();
	public abstract void setCreatorPhone(java.lang.String creatorPhone);
	public abstract java.lang.String getDescription();
	public abstract void setDescription(java.lang.String description);
	public abstract java.lang.String getKeywords();
	public abstract void setKeywords(java.lang.String keywords);
	public abstract java.lang.String getComment();
	public abstract void setComment(java.lang.String comment);
	public abstract java.lang.String getSource();
	public abstract void setSource(java.lang.String source);
	public abstract java.util.Date getPublishDate();
	public abstract void setPublishDate(java.util.Date publishDate);
	public abstract java.lang.String getPublishServer();
	public abstract void setPublishServer(java.lang.String publishServer);
	public abstract java.lang.String getPublishPath();
	public abstract void setPublishPath(java.lang.String publishPath);
	public abstract boolean isIsPublished();
	public abstract void setIsPublished(boolean isPublished);
	public abstract boolean isIsProduced();
	public abstract void setIsProduced(boolean isProduced);
	public abstract java.util.Date getWebdbCreate();
	public abstract void setWebdbCreate(java.util.Date webdbCreate);
	public abstract java.util.Date getWebdbLastchange();
	public abstract void setWebdbLastchange(java.util.Date webdbLastchange);
	public abstract mir.core.model.Feature getFeature();
	public abstract void setFeature(mir.core.model.Feature feature);
	public abstract mir.core.model.MirUser getPublisher();
	public abstract void setPublisher(mir.core.model.MirUser publisher);
	public abstract mir.core.model.Language getLanguage();
	public abstract void setLanguage(mir.core.model.Language language);
	public abstract mir.core.model.Rights getRights();
	public abstract void setRights(mir.core.model.Rights rights);
	/**
	 * @return
	 */
	public abstract Set getContent();
	/**
	 * @param content
	 */
	public abstract void setContent(Set content);
	/**
	 * @return
	 */
	public abstract boolean isProduced();
	/**
	 * @param isProduced
	 */
	public abstract void setProduced(boolean isProduced);
	/**
	 * @return
	 */
	public abstract boolean isPublished();
	/**
	 * @param isPublished
	 */
	public abstract void setPublished(boolean isPublished);
}