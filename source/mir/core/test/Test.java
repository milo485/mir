/*
 * Test.java
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
package mir.core.test;

import java.util.Iterator;
import java.util.List;

import mir.core.model.Audio;
import mir.core.model.Content;
import mir.core.model.IImage;
import mir.core.model.Image;
import mir.core.model.Media;
import mir.core.model.Topic;
import mir.core.model.UploadedMedia;
import mir.core.model.Video;
import mir.core.service.storage.ContentService;
import mir.core.service.storage.ImageService;
import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

/**
 * Test
 * @version $Id: Test.java,v 1.8 2003/09/10 20:59:01 idfx Exp $
 * @author idefix
 */
public class Test {

	public static void main(String[] args) {
		//BasicConfigurator.configure();
		try {
			SessionFactory factory = new Configuration().configure().buildSessionFactory();
			Session session = factory.openSession();
			Transaction transaction = session.beginTransaction();
			Criteria criteria = session.createCriteria(Topic.class);
			List list = criteria.setMaxResults(10).list();
			for(Iterator iterator = list.iterator(); iterator.hasNext();){
				Topic media = (Topic)iterator.next();
				System.out.println(media.toString());
			}
			criteria = session.createCriteria(Media.class);
			list = criteria.setMaxResults(10).list();
			for(Iterator iterator = list.iterator(); iterator.hasNext();){
				Media media = (Media)iterator.next();
				System.out.println(media.toString());
			}
			transaction.commit();
			session.close();

			ContentService contentService = new ContentService(factory);
			
			Object content = contentService.load(new Integer(7));
			System.out.println(content + content.getClass().getName());
			
			System.out.println("****** content media");

			list = contentService.list(0,10);
			for(Iterator iterator = list.iterator(); iterator.hasNext();){
				Content media = (Content)iterator.next();
				System.out.println(media.getTitle());
			}

			session = factory.openSession();
			transaction = session.beginTransaction();
			criteria = session.createCriteria(UploadedMedia.class);
			list = criteria.setMaxResults(10).list();
			System.out.println("****** uploaded media");
			for(Iterator iterator = list.iterator(); iterator.hasNext();){
				UploadedMedia media = (UploadedMedia)iterator.next();
				System.out.println(media.toString() + media.getIconPath());
			}
			transaction.commit();
			session.close();
			ImageService imageService = new ImageService(factory);
			list = imageService.list(0,10);
			System.out.println("****** image media");
			for(Iterator iterator = list.iterator(); iterator.hasNext();){
				Image media = (Image)iterator.next();
				System.out.println(media.toString() + media.getIconPath());
			}
			IImage image = (IImage)imageService.load(new Integer(18));
			System.out.println(image + " " + image.getImage().length);
			session = factory.openSession();
			transaction = session.beginTransaction();
			criteria = session.createCriteria(Audio.class);
			list = criteria.setMaxResults(10).list();
			System.out.println("****** audio media");
			for(Iterator iterator = list.iterator(); iterator.hasNext();){
				Audio media = (Audio)iterator.next();
				System.out.println(media.toString() + media.getIconPath());
			}
			criteria = session.createCriteria(Video.class);
			list = criteria.setMaxResults(10).list();
			System.out.println("****** video media");
			for(Iterator iterator = list.iterator(); iterator.hasNext();){
				Video media = (Video)iterator.next();
				System.out.println(media.toString() + media.getIconPath());
			}
			transaction.commit();
			session.close();
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
