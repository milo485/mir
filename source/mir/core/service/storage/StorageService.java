/*
 * StorageService.java
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

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;
import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

/**
 * 
 * StorageService
 * @author idefix
 * @version $Id: StorageService.java,v 1.4 2003/09/07 16:55:00 idfx Exp $
 */
public abstract class StorageService {
	private Class objectClass;
	private SessionHolder sessionHolder;
	protected MirPropertiesConfiguration _configuration;

	public StorageService(Class objectClass, SessionFactory factory){
		this.objectClass = objectClass;
		sessionHolder = new SessionHolder(factory);
		try {
			_configuration = MirPropertiesConfiguration.instance();
		} catch (PropertiesConfigExc e) {
			throw new multex.Failure("could not get the config", e);
		}
	}
	
	public List list(int offset, int limit){
		return list(offset, limit, null);
	}
	
	public List list(int offset){
		int limit = _configuration.getInt("ServletModule.Default.ListSize");
		return list(offset, limit, null);
	}
		
	public List list(int offset, Expression expression){
		int limit = _configuration.getInt("ServletModule.Default.ListSize");
		return list(offset, limit, expression);
	}	
	public List list(int offset, int limit, Expression expression){
		try {
			Session session = sessionHolder.currentSession();
			Transaction transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(objectClass);
			if(expression != null){
				System.out.println(expression.toString());
				criteria = criteria.add(expression);
			}	
			criteria.setFirstResult(offset)
				.setMaxResults(limit);
			List returnList = criteria.list();
			transaction.commit();
			sessionHolder.closeSession();
			return returnList;
		} catch (HibernateException e) {
			throw new StorageServiceFailure(e);
		} 
	}
	
	public Object load(Integer id){
		try {
			Session session = sessionHolder.currentSession();
			Transaction transaction = session.beginTransaction();
			Object returnObject = session.load(objectClass, id);
			initializeLazyCollections(returnObject);
			transaction.commit();
			sessionHolder.closeSession();
			return returnObject;
		} catch (HibernateException e) {
			throw new StorageServiceFailure(e);
		}	
	}
	
	/**
	 * @param returnObject
	 */
	protected abstract void initializeLazyCollections(Object returnObject) 
		throws HibernateException;
	
	public Integer add(Object newObject){
		try {
			Session session = sessionHolder.currentSession();
			Transaction transaction = session.beginTransaction();
			Integer newid = (Integer)session.save(newObject);
			transaction.commit();
			return newid;
		} catch (HibernateException e) {
			throw new StorageServiceFailure(e);
		}			
	}
	
	public void update(Object toUpdate){
		try {
			Session session = sessionHolder.currentSession();
			Transaction transaction = session.beginTransaction();
			session.update(toUpdate);
			transaction.commit();
		} catch (HibernateException e) {
			throw new StorageServiceFailure(e);
		}			
	}
	
	public void delete(Object toDelete){
		try {
			Session session = sessionHolder.currentSession();
			Transaction transaction = session.beginTransaction();
			session.delete(toDelete);
			transaction.commit();
		} catch (HibernateException e) {
			throw new StorageServiceFailure(e);
		}			
	}
	
	public void finalize() throws Throwable {
		sessionHolder.closeSession();
		super.finalize();	
	}
}
