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
import net.sf.hibernate.expression.Order;

/**
 * 
 * StorageService
 * @author idefix
 * @version $Id: StorageService.java,v 1.5 2003/09/10 20:57:17 idfx Exp $
 */
public abstract class StorageService {
	private final int defaultLimit;
	private Class objectClass;
	private SessionHolder sessionHolder;
	protected MirPropertiesConfiguration _configuration;

	protected StorageService(final Class objectClass, 
		final SessionFactory factory){
		this.objectClass = objectClass;
		sessionHolder = new SessionHolder(factory);
		int limit = 30;
		try {
			_configuration = MirPropertiesConfiguration.instance();
			limit = 
				_configuration.getInt("ServletModule.Default.ListSize");
		} catch (PropertiesConfigExc e) {
			e.printStackTrace();
		}
		defaultLimit = limit;
	}
	
	public List list(final int offset, final int limit){
		return list(offset, limit, null);
	}
	
	public List list(final int offset){
		return list(offset, defaultLimit, null);
	}
		
	public List list(final int offset, final Expression expression){
		return list(offset, defaultLimit, expression);
	}	
	
	public List list(final int offset, final int limit, 
		final Expression expression) {
		return list(offset, limit, expression, null);
	}
	
	/**
	 * Load a list of Objects from the database
	 * @param offset an offset of the list
	 * @param limit the limit number of Objects to be loaded
	 * @param expression a Expression object which describes the 
	 * constraints of the objects in the list
	 * @param order a Order object which describes the order of 
	 * the list
	 * @see Expression
	 * @see Order
	 * @return a list of Objects
	 */
	public List list(final int offset, final int limit, 
		final Expression expression, final Order order) {
		try {
			Session session = null;
			Transaction transaction = null;
			try {
				session = sessionHolder.currentSession();
				transaction = session.beginTransaction();
				Criteria criteria = session.createCriteria(objectClass);
				if(expression != null){
					criteria = criteria.add(expression);
				}	
				if(order != null){
					criteria.addOrder(order);
				}
				criteria.setFirstResult(offset)
					.setMaxResults(limit);
				List returnList = criteria.list();
				transaction.commit();
				return returnList;
			} catch (HibernateException e) {
				if(transaction != null){
					transaction.rollback();
				}
				throw new StorageServiceFailure(e);
			} finally {
				if (session != null) {
					sessionHolder.closeSession();
				}
			}
		} catch (Exception e){
			throw new StorageServiceFailure(e);
		}
	}
	
	/**
	 * Load a Object with the given unique identifier
	 * @param id the identifer of the Object to be loaded
	 * @return the Object according to the id
	 */
	public Object load(final Integer id){
		try {
			Session session = null;
			Transaction transaction = null;
			try {
				session = sessionHolder.currentSession();
				transaction = session.beginTransaction();
				Object returnObject = session.load(objectClass, id);
				initializeLazyCollections(returnObject);
				transaction.commit();
				return returnObject;
			} catch (HibernateException e) {
				if(transaction != null){
					transaction.rollback();
				}
				throw new StorageServiceFailure(e);
			}	finally {
				if(session != null){
					sessionHolder.closeSession();
				}
			}
		} catch (Exception e) {
			throw new StorageServiceFailure(e);
		}
	}
		
	/**
	 * Save a new Object in the database
	 * @param newObject the Object to be saved
	 * @return
	 */
	public Integer save(final Object newObject){
		try {
			Session session = null;
			Transaction transaction = null;
			try {
				session = sessionHolder.currentSession();
				transaction = session.beginTransaction();
				Integer newid = (Integer)session.save(newObject);
				transaction.commit();
				return newid;
			} catch (HibernateException e) {
				if(transaction != null){
					transaction.rollback();
				}
				throw new StorageServiceFailure(e);
			}	finally {
				if(session != null){
					sessionHolder.closeSession();
				}
			}
		} catch (Exception e) {
			throw new StorageServiceFailure(e);
		}			
	}
	
	/**
	 * Update a given Object
	 * @param toUpdate the Object to be updated
	 */
	public void update(final Object toUpdate){
		try {
			Session session = null;
			Transaction transaction = null;
			try {
				session = sessionHolder.currentSession();
				transaction = session.beginTransaction();
				session.update(toUpdate);
				transaction.commit();
			} catch (HibernateException e) {
				if(transaction != null){
					transaction.rollback();
				}
				throw new StorageServiceFailure(e);
			}	finally {
				if(session != null){
					sessionHolder.closeSession();
				}
			}
		} catch (Exception e) {
			throw new StorageServiceFailure(e);
		}					
	}
	
	/**
	 * Delete a given Object from the database
	 * @param toDelete the Object to be deleted
	 */
	public void delete(final Object toDelete){
		try {
			Session session = null;
			Transaction transaction = null;
			try {
				session = sessionHolder.currentSession();
				transaction = session.beginTransaction();
				session.delete(toDelete);
				transaction.commit();
			} catch (HibernateException e) {
				if(transaction != null){
					transaction.rollback();
				}
				throw new StorageServiceFailure(e);
			}	finally {
				if(session != null){
					sessionHolder.closeSession();
				}
			}
		} catch (Exception e) {
			throw new StorageServiceFailure(e);
		}				
	}
	
	/**
	 * Initialize all the lazy loaded collections 
	 * of an object loaded from the database
	 * @param object the object to be initialized
	 */
	protected abstract void initializeLazyCollections(
		final Object object) throws HibernateException;
}
