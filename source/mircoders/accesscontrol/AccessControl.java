/*
 * Copyright (C) 2001, 2002 The Mir-coders group
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

package mircoders.accesscontrol;

import java.util.List;
import java.util.Vector;

import mir.config.MirPropertiesConfiguration;
import mir.log.LoggerWrapper;
import mircoders.entity.EntityUsers;

public class AccessControl {
  private UserAccessControl user;
  private GeneralAccessControl general;
  protected LoggerWrapper logger = new LoggerWrapper("Global.AccessControl");
  protected MirPropertiesConfiguration configuration;

  public AccessControl() {
    try {
      configuration = MirPropertiesConfiguration.instance();

      user = new UserAccessControl(configuration.getVector("AccessControl.SuperUsers"));
      general = new GeneralAccessControl();
    }
    catch (Throwable t) {
      throw new RuntimeException(t.toString());
    }
  }

  public UserAccessControl user() {
    return user;
  }

  public GeneralAccessControl general() {
    return general;
  }

  public class GeneralAccessControl {
    public boolean mayDeleteArticles(EntityUsers aSubject) {
      return configuration.getString("Mir.Localizer.Admin.AllowDeleteArticle", "0").equals("1");
    }

    public void assertMayDeleteArticles(EntityUsers aSubject) throws AuthorizationExc, AuthorizationFailure {
      try {
        if (!mayDeleteArticles(aSubject))
          throw new AuthorizationExc("not allowed to delete articles");
      }
      catch (Throwable t) {
        throw new AuthorizationFailure(t);
      }
    }

    public boolean mayDeleteComments(EntityUsers aSubject) {
      return configuration.getString("Mir.Localizer.Admin.AllowDeleteComment", "0").equals("1");
    }

    public void assertMayDeleteComments(EntityUsers aSubject) throws AuthorizationExc, AuthorizationFailure {
      try {
        if (!mayDeleteArticles(aSubject))
          throw new AuthorizationExc("not allowed to delete comments");
      }
      catch (Throwable t) {
        throw new AuthorizationFailure(t);
      }
    }
  }

  public class UserAccessControl {
    private List superusers;

    public UserAccessControl(List aSuperUsers) {
      superusers = new Vector(aSuperUsers);
    }

    public void assertMayAddUsers(EntityUsers aSubject) throws AuthorizationExc, AuthorizationFailure {
      try {
        if (!mayAddUsers(aSubject))
          throw new AuthorizationExc("not allowed to add users");
      }
      catch (Throwable t) {
        throw new AuthorizationFailure(t);
      }

    }

    public boolean mayAddUsers(EntityUsers aSubject) {
      return superusers.contains(aSubject.getValue("login"));
    }

    public void assertMayEditUser(EntityUsers aSubject, EntityUsers anObject) throws AuthorizationExc, AuthorizationFailure {
      try {
        if (!mayEditUser(aSubject, anObject))
          throw new AuthorizationExc("not allowed to edit user " + anObject.getId());
      }
      catch (Throwable t) {
        throw new AuthorizationFailure(t);
      }

    }

    public boolean mayEditUser(EntityUsers aSubject, EntityUsers anObject) {
      return superusers.contains(aSubject.getValue("login"));
    }

    public boolean mayEditUsers(EntityUsers aSubject) {
      return superusers.contains(aSubject.getValue("login"));
    }

    public void assertMayDeleteUser(EntityUsers aSubject, EntityUsers anObject) throws AuthorizationExc, AuthorizationFailure {
      try {
        if (!mayDeleteUser(aSubject, anObject))
          throw new AuthorizationExc("not allowed to delete user " + anObject.getId());
      }
      catch (Throwable t) {
        throw new AuthorizationFailure(t);
      }
    }

    public boolean mayDeleteUser(EntityUsers aSubject, EntityUsers anObject) {
      return superusers.contains(aSubject.getValue("login"));
    }

    public boolean mayDeleteUsers(EntityUsers aSubject) {
      return superusers.contains(aSubject.getValue("login"));
    }

    public boolean mayChangeUserPassword(EntityUsers aSubject, EntityUsers anObject) {
      return aSubject.getId().equals(anObject.getId()) || superusers.contains(aSubject.getValue("login"));
    }

    public void assertMayChangeUserPassword(EntityUsers aSubject, EntityUsers anObject) throws AuthorizationExc, AuthorizationFailure {
      try {
        if (!mayChangeUserPassword(aSubject, anObject))
          throw new AuthorizationExc("not allowed to change user " + anObject.getId()+"'s password");
      }
      catch (Throwable t) {
        throw new AuthorizationFailure(t);
      }
    }
  }
}
