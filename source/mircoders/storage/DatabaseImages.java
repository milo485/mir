/*
 * Copyright (C) 2001, 2002  The Mir-coders group
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
 * the code of this program with the com.oreilly.servlet library, any library
 * licensed under the Apache Software License, The Sun (tm) Java Advanced
 * Imaging library (JAI), The Sun JIMI library (or with modified versions of
 * the above that use the same license as the above), and distribute linked
 * combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than the above
 * mentioned libraries.  If you modify this file, you may extend this exception
 * to your version of the file, but you are not obligated to do so.  If you do
 * not wish to do so, delete this exception statement from your version.
 */

package mircoders.storage;

import java.lang.*;
import java.sql.*;
import java.io.*;
import java.util.*;

import freemarker.template.*;

import mir.storage.*;
import mir.entity.*;
import mir.misc.*;

/**
 * <b>Diese Klasse implementiert die Datenbankverbindung zur MetaObjekt-Tabelle
 *
 *
 */

public class DatabaseImages extends Database implements StorageObject{

	private static DatabaseImages instance;
	private static SimpleList publisherPopupData;

  // the following *has* to be sychronized cause this static method
  // could get preemted and we could end up with 2 instances of DatabaseFoo..
  // see the "Singletons with needles and thread" article at JavaWorld -mh
	public synchronized static DatabaseImages getInstance()
    throws StorageObjectException
	{
		if (instance == null) {
			instance = new DatabaseImages();
			instance.myselfDatabase = instance;
		}
		return instance;
	}

	private DatabaseImages() throws StorageObjectException
	{
		super();
		this.hasTimestamp = true;
		this.theTable="images";
		this.theCoreTable="media";
		try {
			this.theEntityClass = Class.forName("mircoders.entity.EntityImages");
		}
		catch (Exception e) { throw new StorageObjectException(e.toString());	}
	}

	public SimpleList getPopupData() throws StorageObjectException {
		return getPopupData("title",true);
	}

	public void update(Entity theEntity) throws StorageObjectException
	{
		String date = theEntity.getValue("date");
		if (date==null){
			date = StringUtil.date2webdbDate(new GregorianCalendar());
			theEntity.setValueForProperty("date",date);
		}

		super.update(theEntity);
	}


	public String insert(Entity theEntity) throws StorageObjectException
	{
		String date = theEntity.getValue("date");
		if (date==null){
			date = StringUtil.date2webdbDate(new GregorianCalendar());
			theEntity.setValueForProperty("date",date);
		}
		return super.insert(theEntity);
	}

	// initialisierungen aus den statischen Tabellen

}
