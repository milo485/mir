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
package mir.config;

import multex.Exc;
import multex.Failure;

import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;


/**
 * @author idefix
 */
public class MirPropertiesConfiguration extends PropertiesConfiguration {
  private static MirPropertiesConfiguration instance;
  private static ServletContext context;
  private static String contextPath;
  
  //if one of these properties is not present a new 
  //emtpy property is added
  private String[] needed = {"Producer.DocRoot"};

  /**
   * Constructor for MirPropertiesConfiguration.
   */
  private MirPropertiesConfiguration(ServletContext ctx, String ctxPath)
    throws IOException {
    super(ctx.getRealPath("/WEB-INF/etc/") + "/config.properties",
      ctx.getRealPath("/WEB-INF/etc/") + "/default.properties");
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

  public Map allSettings() {
    Iterator iterator = this.getKeys();
    Map returnMap = new HashMap();

    while (iterator.hasNext()) {
      String key = (String) iterator.next();
      Object o = this.getString(key);
      if(o == null){        
      	o = new Object();
      }
      returnMap.put(key, o);
    }

    return returnMap;
  }

  /**
   * Returns the context.
   * @return ServletContext
   */
  public static ServletContext getContext() {
    return context;
  }

  public String getStringWithHome(String key) {
    String returnString = getString(key);

    if (returnString == null) {
      returnString = new String();
    }

    return getString("Home") + returnString;
  }

  private void checkMissing(){
  	for(int i = 0; i < needed.length; i++){  	  
  	  if(super.getProperty(needed[i]) == null){
  	  	addProperty(needed[i],"");
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
   * @see org.apache.commons.configuration.Configuration#getString(java.lang.String)
   */
  public String getString(String key) {
    if (super.getString(key) == null) {
      return new String();
    }
    return super.getString(key);
  }
  
  /**
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
}
