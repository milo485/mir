/*
 * IImage.java created on 18.08.2003
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

/**
 * IImage
 * @author idefix
 * @version $Id: IImage.java,v 1.1 2003/08/19 00:41:54 idfx Exp $
 */
public interface IImage extends IUploadedMedia {
	public abstract int getImageData();
	public abstract void setImageData(int imageData);
	public abstract int getIconData();
	public abstract void setIconData(int iconData);
	public abstract java.lang.String getYear();
	public abstract void setYear(java.lang.String year);
	public abstract short getImgWidth();
	public abstract void setImgWidth(short imgWidth);
	public abstract short getImgHeight();
	public abstract void setImgHeight(short imgHeight);
	public abstract short getImgFormat();
	public abstract void setImgFormat(short imgFormat);
	public abstract short getImgLayout();
	public abstract void setImgLayout(short imgLayout);
	public abstract short getImgType();
	public abstract void setImgType(short imgType);
	public abstract short getImgColor();
	public abstract void setImgColor(short imgColor);
	public abstract short getIconWidth();
	public abstract void setIconWidth(short iconWidth);
	public abstract short getIconHeight();
	public abstract void setIconHeight(short iconHeight);
}