/*
 * UserService.java created on 17.08.2003
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
package mir.core.service.storage;


import java.util.List;

import mir.core.model.MirUser;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.expression.Expression;

/**
 * UserService
 * @author idefix
 * @version $Id: UserService.java,v 1.3 2003/09/07 16:55:00 idfx Exp $
 */
public class UserService extends StorageService {

	/**
	 * @param objectClass
	 * @param factory
	 */
	public UserService(SessionFactory factory) {
		super(MirUser.class, factory);
	}
	
	public MirUser loadUser(String user, String password){
		List list = list(0, 1, 
			Expression.and(
				Expression.and(
					Expression.eq("login", user),
					Expression.eq("password", password)
				),
				Expression.eq("isAdmin", new Boolean(true))
			)
		);
		if(list.size() == 0){
			return null;
		}
		return (MirUser)list.get(0);
	}

	/**
	 * @see mir.core.service.storage.StorageService#initializeLazyCollections(java.lang.Object)
	 */
	protected void initializeLazyCollections(Object returnObject) throws HibernateException {		
		//do nothing
	}
}
