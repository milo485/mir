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
package mir.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;

import multex.Exc;
import multex.Failure;

import org.apache.commons.collections.ExtendedProperties;


/**
 * @author idefix
 */
public class MirPropertiesConfiguration extends ExtendedProperties {
  private static MirPropertiesConfiguration instance;
  private static ServletContext context;
  private static String contextPath;

  //if one of these properties is not present a new
  //property is added with its default value;
  private static NeededProperty[] neededWithValue =
  {
    new NeededProperty("Producer.DocRoot", ""),
    new NeededProperty("Producer.ImageRoot", ""),
    new NeededProperty("Producer.Image.Path", ""),
    new NeededProperty("Producer.Media.Path", ""),
    new NeededProperty("Producer.RealMedia.Path", ""),
    new NeededProperty("Producer.Image.IconPath", "")
  };

  /**
   * Constructor for MirPropertiesConfiguration.
   */
  private MirPropertiesConfiguration(ServletContext ctx, String ctxPath)
    throws IOException {
    //loading the defaults-config
    super(ctx.getRealPath("/WEB-INF/") + "/default.properties");
    //loading the user-config
    ExtendedProperties userConfig = 
    	new ExtendedProperties(ctx.getRealPath("/WEB-INF/etc/") + "/config.properties");
    //merging them to one config while overriding the defaults
    this.combine(userConfig);
    addProperty("Home", ctx.getRealPath("/WEB-INF/") + "/");
    checkMissing();
  }

  public static synchronized MirPropertiesConfiguration instance()
    throws PropertiesConfigExc {
    if (instance == null) {
      if (context == null) {
        throw new MirPropertiesConfiguration.PropertiesConfigExc(
          "Context was not set");
      }

      try {
        instance = new MirPropertiesConfiguration(context, contextPath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return instance;
  }

  /**
   * Sets the context.
   * @param context The context to set
   */
  public static void setContext(ServletContext context) {
    MirPropertiesConfiguration.context = context;
  }

  /**
   * Returns the context.
   * @return ServletContext
   */
  public static ServletContext getContext() {
    return context;
  }

  /**
   * Returns all properties in a Map
   * @return Map
   */
  public Map allSettings() {
    Iterator iterator = this.getKeys();
    Map returnMap = new HashMap();
    while (iterator.hasNext()) {
      String key = (String) iterator.next();
      Object o = this.getProperty(key);

      if (o == null) {
        o = new Object();
      }

      returnMap.put(key, o);
    }

    return returnMap;
  }

  /**
   * Returns a String-property concatenated with the home-dir of the
   * installation
   * @param key
   * @return String
   */
  public String getStringWithHome(String key) {
    String returnString = getString(key);

    if (returnString == null) {
      returnString = new String();
    }

    return getString("Home") + returnString;
  }

  /**
   * Checks if one property is missing and adds a default value
   */
  private void checkMissing() {
    for (int i = 0; i < neededWithValue.length; i++) {
      if (super.getProperty(neededWithValue[i].getKey()) == null) {
        addProperty(neededWithValue[i].getKey(), neededWithValue[i].getValue());
      }
    }
  }

  public File getFile(String key) throws FileNotFoundException {
    String path = getStringWithHome(key);
    File returnFile = new File(path);

    if (returnFile.exists()) {
      return returnFile;
    } else {
      throw new FileNotFoundException();
    }
  }

  /**
   * @return the vlaue of this property as String
   * @param key the key of this property
   * @see org.apache.commons.configuration.Configuration#getString(java.lang.String)
   */
  public String getString(String key) {
  	return getString(key, "");
  }
  
  
  /** 
   * @return the value of this property as String
   * @param key the key of the property
   * @param defaultValue the default value of this property if it is null
   * @see org.apache.commons.collections.ExtendedProperties#getString(java.lang.String, java.lang.String)
   */
  public String getString(String key, String defaultValue) {
		Object object = getProperty(key);  	
		if(object == null){
			if (defaultValue == null) {
				return new String();
			}
		  return defaultValue;
		} 			
		if (object instanceof String) {
			return (String)object;
		}
		return object.toString();
  }

  /**
   * Returns a property according to the given key
   * @param key the key of the property
   * @return the value of the property as Object, if no property available it returns a empty String
   * @see org.apache.commons.configuration.Configuration#getString(java.lang.String)
   */
  public Object getProperty(String key) {
    if (super.getProperty(key) == null) {
      return new String();
    }

    return super.getProperty(key);
  }
  
  /**
   * @author idefix
   */
  public static class PropertiesConfigExc extends Exc {
    /**
     * Constructor for PropertiesConfigExc.
     * @param arg0
     */
    public PropertiesConfigExc(String msg) {
      super(msg);
    }
  }

  /**
   * @author idefix
   */
  public static class PropertiesConfigFailure extends Failure {
    /**
     * Constructor for PropertiesConfigExc.
     * @param arg0
     */
    public PropertiesConfigFailure(String msg, Throwable cause) {
      super(msg, cause);
    }
  }

  /**
   * A Class for properties to be checked
   * @author idefix
   */
  private static class NeededProperty {
    private String _key;
    private String _value;

    public NeededProperty(String key, String value) {
      _key = key;
      _value = value;
    }

    public String getKey() {
      return _key;
    }

    public String getValue() {
      return _value;
    }
  }
}
