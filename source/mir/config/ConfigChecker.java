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

package  mir.config;

import java.util.*;

import  mir.config.exceptions.*;

public class ConfigChecker {
  public final static int STRING = 0;
  public final static int INTEGER = 1;
  public final static int BOOLEAN = 2;
  public final static int DOUBLE = 3;
  public final static int PATH = 4;
//  public final static int ABSOLUTEPATH = 5;
//  public final static int ABSOLUTEURL = 6;

  private Node rootNode;

  public Node getRootNode() {
    return rootNode;
  }

  public ConfigChecker() {
    super();

    rootNode = new Node();
  }

  public void check(ConfigNode aNode) throws ConfigFailure {
    getRootNode().check(aNode);
  }

  public class Node {

    private Map subNodes;
    private Vector constraints;

    public Node() {
      subNodes = new HashMap();
      constraints = new Vector();
    }

    public Node getSubNode(String aName) {
      Node subNode = (Node) subNodes.get(aName);

      if (subNode==null) {
        subNode = new Node();
        subNodes.put(aName, subNode);
      }

      return subNode;
    }

    public void addExistenceConstraint(String aPropertyName) {
      constraints.add(new ExistenceConstraint(aPropertyName));
    }

    public void addTypeConstraint(String aPropertyName, int aType) {
      constraints.add(new TypeConstraint(aPropertyName, aType));
    }

    public void addExistenceAndTypeConstraint(String aPropertyName, int aType) {
      addExistenceConstraint(aPropertyName);
      addTypeConstraint(aPropertyName, aType);
    }

    public void check(ConfigNode aNode) throws ConfigFailure {
      Iterator iterator;

      iterator=constraints.iterator();
      while (iterator.hasNext()) {
        ((Constraint) iterator.next()).check(aNode);
      }

      iterator=subNodes.keySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry entry = (Map.Entry) iterator.next();
        ((Node) entry.getValue()).check(aNode.getSubNode((String) entry.getKey()));
      }

    }

    private class Constraint {
      protected String propertyName;

      Constraint(String aPropertyName) {
        propertyName=aPropertyName;
      }

      public void check(ConfigNode aNode) throws ConfigFailure {
      };
    }

    private class ExistenceConstraint extends Constraint {
      ExistenceConstraint(String aPropertyName) {
        super(aPropertyName);
      }

      public void check(ConfigNode aNode) throws ConfigFailure {
        aNode.getRequiredStringProperty(propertyName);
      };
    }

    private class TypeConstraint extends Constraint {
      private int type;

      TypeConstraint(String aPropertyName, int aType) {
        super(aPropertyName);

        type=aType;
      }

      public void check(ConfigNode aNode) throws ConfigFailure {
        switch(type) {
          case INTEGER:
            aNode.getOptionalIntegerProperty(propertyName, new Integer(0));
            break;
          case STRING:
            aNode.getOptionalStringProperty(propertyName, "");
            break;
          case DOUBLE:
            aNode.getOptionalDoubleProperty(propertyName, new Double(0.0));
            break;
          case BOOLEAN:
            aNode.getOptionalBooleanProperty(propertyName, Boolean.FALSE);
            break;
          default:
            throw new ConfigFailure("Invalid value for type in type constraint: "+new Integer(type).toString());
        }
      }
    }
  }
}
