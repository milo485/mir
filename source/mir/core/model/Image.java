/*
 * Image.java
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
import java.sql.Connection;
import java.sql.SQLException;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Lifecycle;
import net.sf.hibernate.Session;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

/**
 * 
 * Image
 * @author idefix
 * @version $Id: Image.java,v 1.4 2003/08/19 00:41:54 idfx Exp $
 */
public class Image extends UploadedMedia 
	implements Serializable, Lifecycle, IImage {

    /** nullable persistent field */
    private int imageData;

    /** nullable persistent field */
    private int iconData;

    /** nullable persistent field */
    private String year;

    /** nullable persistent field */
    private short imgWidth;

    /** nullable persistent field */
    private short imgHeight;

    /** persistent field */
    private short imgFormat;

    /** persistent field */
    private short imgLayout;

    /** persistent field */
    private short imgType;

    /** persistent field */
    private short imgColor;

    /** nullable persistent field */
    private short iconWidth;

    /** nullable persistent field */
    private short iconHeight;

    /** default constructor */
    public Image() {
    }

    public int getImageData() {
        return this.imageData;
    }

    public void setImageData(int imageData) {
        this.imageData = imageData;
    }

    public int getIconData() {
        return this.iconData;
    }

    public void setIconData(int iconData) {
        this.iconData = iconData;
    }

    public java.lang.String getYear() {
        return this.year;
    }

    public void setYear(java.lang.String year) {
        this.year = year;
    }

    public short getImgWidth() {
        return this.imgWidth;
    }

    public void setImgWidth(short imgWidth) {
        this.imgWidth = imgWidth;
    }

    public short getImgHeight() {
        return this.imgHeight;
    }

    public void setImgHeight(short imgHeight) {
        this.imgHeight = imgHeight;
    }

    public short getImgFormat() {
        return this.imgFormat;
    }

    public void setImgFormat(short imgFormat) {
        this.imgFormat = imgFormat;
    }

    public short getImgLayout() {
        return this.imgLayout;
    }

    public void setImgLayout(short imgLayout) {
        this.imgLayout = imgLayout;
    }

    public short getImgType() {
        return this.imgType;
    }

    public void setImgType(short imgType) {
        this.imgType = imgType;
    }

    public short getImgColor() {
        return this.imgColor;
    }

    public void setImgColor(short imgColor) {
        this.imgColor = imgColor;
    }

    public short getIconWidth() {
        return this.iconWidth;
    }

    public void setIconWidth(short iconWidth) {
        this.iconWidth = iconWidth;
    }

    public short getIconHeight() {
        return this.iconHeight;
    }

    public void setIconHeight(short iconHeight) {
        this.iconHeight = iconHeight;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

		/* (non-Javadoc)
		 * @see net.sf.hibernate.Lifecycle#onSave(net.sf.hibernate.Session)
		 */
		public boolean onSave(Session arg0) throws CallbackException {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see net.sf.hibernate.Lifecycle#onUpdate(net.sf.hibernate.Session)
		 */
		public boolean onUpdate(Session arg0) throws CallbackException {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see net.sf.hibernate.Lifecycle#onDelete(net.sf.hibernate.Session)
		 */
		public boolean onDelete(Session arg0) throws CallbackException {
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see net.sf.hibernate.Lifecycle#onLoad(net.sf.hibernate.Session, java.io.Serializable)
		 */
		public void onLoad(Session session, Serializable arg1) {
			// TODO Auto-generated method stub
			System.out.println("hello: ");
			try {
				Connection connection = session.connection();
				if(connection instanceof PGConnection){
					PGConnection pgcon = (PGConnection)connection;
					LargeObjectManager largeObjectManager = pgcon.getLargeObjectAPI();
					LargeObject image = largeObjectManager.open(getImageData());
					System.out.println("juppii" + image.size()  + " " + this.getMediaType().getId());
				}
			} catch (HibernateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

}
