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

package mir.misc;

import java.io.IOException;

import mir.config.MirPropertiesConfiguration;
import mir.config.MirPropertiesConfiguration.PropertiesConfigExc;

/**
 * Title:        Indy
 * Description:  This class provides some satic help methods
 * Copyright:    Copyright (c) 2001
 * Company:      indymedia.de
 * @author idfx
 * @version 1.0
 */

public class Helper {

  /**
   * rsync the webrepository
   * needs a rsyncscript
   * returns the exit-code
	 * returns 255 if rsync should not be used
   */
  public static int rsync(){
    MirPropertiesConfiguration configuration = null;
    try {
      configuration = MirPropertiesConfiguration.instance();
    } catch (PropertiesConfigExc e) {
      e.printStackTrace();
      return 255;
    }
    
		if(!configuration.getString("Rsync").toLowerCase().equals("yes")){
			return 255;
		}
		
	  Process p;
    int returnValue = -1;
    try {
      Runtime run = Runtime.getRuntime();
      p = run.exec(configuration.getString("Rsync.Script.Path"));
      returnValue = p.waitFor();
    } catch (IOException e) {
      return returnValue;
    } catch (InterruptedException e) {
      return returnValue;
    }
    return returnValue;
  }
}
