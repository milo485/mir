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

import java.lang.*;

/*
 *  differentiates itself from a bug in that it
 *  represents a probable user error
 * 
 * @version $Revision: 1.2 $
 * @author $Author: mh $
 *
 * $Log: FileHandlerUserException.java,v $
 * Revision 1.2  2002/11/04 04:35:21  mh
 * merge media InputStream changes from MIR_1_0 branch
 *
 * Revision 1.1.2.1  2002/11/01 05:38:20  mh
 * Converted media Interface to use streams (Java IO) instead of byte buffers of
 * the entire uplaoded files. These saves loads of unecessary memory use. JAI
 * still consumes quite a bit though.
 *
 * A new temporary file (for JAI) parameter is necessary and is in the config.properties file.
 *
 * A nice side effect of this work is the FileHandler interface which is
 * basically a call back mechanism for WebdbMultipartRequest which allows the
 * uploaded file to handled by different classes. For example, for a media
 * upload, the content-type, etc.. needs to be determined, but if say the
 * FileEditor had a feature to upload static files... another handler wood be
 * needed. Right now only the MediaRequest handler exists.
 *
 *
 */

public final class FileHandlerUserException extends Exception {
  String msg;

	public FileHandlerUserException(String msg) {
    super(msg);
    this.msg = msg;
  }

  public String getMsg() {
    return msg;
  }
}

