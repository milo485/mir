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

import mir.config.exceptions.*;

public class ConfigSimpleNode implements ConfigNode, ConfigNodeBuilder {
  private Map properties;
  private Map subNodes;
  private String locationDescription;
  private String path;

  public ConfigSimpleNode() {
    this("", "");
  }

  public ConfigSimpleNode(String aLocationDescription) {
    this("", aLocationDescription);
  }

  public ConfigSimpleNode(String aPath, String aLocationDescription) {
    super ();

    path = aPath;
    locationDescription = aLocationDescription;
    properties = new HashMap();
    subNodes = new HashMap();
  }

// ConfigNodeBuilder helpers:

  private String makeSubNodePath(String aSubNode) {
    if (path!=null && path.length()>0)
      return path+"/"+aSubNode;
    else
      return aSubNode;
  }

  private String makePropertyPath(String aProperty) {
    if (path!=null && path.length()>0)
      return path+"/"+aProperty;
    else
      return aProperty;
  }

  public ConfigNodeBuilder mimicSubNode(String aName, String aLocationDescription) {
    ConfigNodeBuilder result = new ConfigSimpleNode(makeSubNodePath(aName), aLocationDescription);

    return result;
  }

// ConfigNodeBuilder methods:

  public ConfigNodeBuilder makeSubNode(String aName, String aLocationDescription) {
    if (subNodes.containsKey(aName)) {
      return (ConfigNodeBuilder) subNodes.get(aName);
    }
    else {
      ConfigNodeBuilder result = mimicSubNode(aName, aLocationDescription);
      subNodes.put(aName, result);

      return result;
    }
  }

  public void addProperty(String aName, String aValue, String anUnexpandedValue, String aLocationDescription) {
    properties.put(aName, new property(aValue, anUnexpandedValue, aLocationDescription, makePropertyPath(aName)));
  }

// ConfigNode helpers

  public boolean hasProperty(String aPropertyName) {
    return properties.containsKey(aPropertyName);
  }

  public property getProperty(String aPropertyName) {
    return (property) properties.get(aPropertyName);
  }

  private property getRequiredProperty(String aPropertyName) throws ConfigMissingPropertyException {
    if (!hasProperty(aPropertyName)) {
      throw new ConfigMissingPropertyException("required property \""+aPropertyName+"\" not found", getLocationDescription());
    }

    return getProperty(aPropertyName);
  }


// ConfigNode methods:

  public String getLocationDescription() {
    return getPath()+" ("+locationDescription+")";
  };

  public String getPath() {
    return path;
  };


  public ConfigNode getSubNode(String aSubNodeName) {
    if (subNodes.containsKey(aSubNodeName)) {
      return (ConfigNode) subNodes.get(aSubNodeName);
    }
    else
    {
      return (ConfigNode) mimicSubNode(aSubNodeName, locationDescription);
    }
  }

  public Boolean getRequiredBooleanProperty(String aPropertyName) throws ConfigMissingPropertyException, ConfigInvalidPropertyTypeException {
    return getRequiredProperty(aPropertyName).interpretAsBoolean();
  }

  public Integer getRequiredIntegerProperty(String aPropertyName) throws ConfigMissingPropertyException, ConfigInvalidPropertyTypeException {
    return getRequiredProperty(aPropertyName).interpretAsInteger();
  }

  public String getRequiredStringProperty(String aPropertyName) throws ConfigMissingPropertyException, ConfigInvalidPropertyTypeException {
    return getRequiredProperty(aPropertyName).interpretAsString();
  }

  public Double getRequiredDoubleProperty(String aPropertyName) throws ConfigMissingPropertyException, ConfigInvalidPropertyTypeException {
    return getRequiredProperty(aPropertyName).interpretAsDouble();
  }


  public Boolean getOptionalBooleanProperty(String aPropertyName, Boolean aDefaultValue) throws ConfigInvalidPropertyTypeException {
    if (!hasProperty(aPropertyName)) {
      return aDefaultValue;
    }
    else {
      return getProperty(aPropertyName).interpretAsBoolean();
    }
  }

  public Integer getOptionalIntegerProperty(String aPropertyName, Integer aDefaultValue) throws ConfigInvalidPropertyTypeException {
    if (!hasProperty(aPropertyName)) {
      return aDefaultValue;
    }
    else {
      return getProperty(aPropertyName).interpretAsInteger();
    }
  }

  public String getOptionalStringProperty(String aPropertyName, String aDefaultValue) throws ConfigInvalidPropertyTypeException {
    if (!hasProperty(aPropertyName)) {
      return aDefaultValue;
    }
    else {
      return getProperty(aPropertyName).interpretAsString();
    }
  }

  public Double getOptionalDoubleProperty(String aPropertyName, Double aDefaultValue) throws ConfigInvalidPropertyTypeException {
    if (!hasProperty(aPropertyName)) {
      return aDefaultValue;
    }
    else {
      return getProperty(aPropertyName).interpretAsDouble();
    }
  }

// property helper class

  private class property {
    private String value;
    private String unexpandedValue;
    private String path;
    private String locationDescription;

    public property( String aValue, String anUnexpandedValue, String aLocationDescription, String aPath ) {
      value = aValue;
      unexpandedValue = anUnexpandedValue;
      locationDescription = aLocationDescription;
      path = aPath;
    }

    public String getValue() {
      return value;
    }

    public String getUnexpandedValue() {
      return unexpandedValue;
    }

    public String getPath() {
      return path;
    }

    public String getLocationDescription() {
      return getPath()+" ("+locationDescription+")";
    }

    public String getValueDescription() {
      return "\""+value+"\" (\""+unexpandedValue+"\")";
    }

    public Boolean interpretAsBoolean() throws ConfigInvalidPropertyTypeException {
      if (value.equals("1"))
        return Boolean.TRUE;
      else if (value.equals("0"))
        return Boolean.FALSE;
      else
        throw new ConfigInvalidPropertyTypeException(getValueDescription() + " is not a boolean", getLocationDescription());
    }

    public String interpretAsString() throws ConfigInvalidPropertyTypeException {
      return value;
    }

    public Integer interpretAsInteger() throws ConfigInvalidPropertyTypeException {
      try {
        return Integer.valueOf(value);
      }
      catch (Throwable e) {
        throw new ConfigInvalidPropertyTypeException("\""+value+"\" (\""+unexpandedValue+"\") is not an integer", getLocationDescription());
      }
    }

    public Double interpretAsDouble() throws ConfigInvalidPropertyTypeException {
      try {
        return Double.valueOf(value);
      }
      catch (Throwable e) {
        throw new ConfigInvalidPropertyTypeException("\""+value+"\" (\""+unexpandedValue+"\") is not a double", getLocationDescription());
      }
    }
  }
}


