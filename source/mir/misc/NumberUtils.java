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

package  mir.misc;

import freemarker.template.*;

import java.text.NumberFormat;
import java.util.List; 

/**
 * Help methods for number handling.
 *
 * @version $Id: NumberUtils.java,v 1.1.2.2 2002/11/28 19:11:19 mh Exp $
 * @author mh, Mir-coders group
 *
 */

public final class NumberUtils {

  /* 
   * Uses a suffix indicating multiples of 1024 (K), 
   * 1024*1024 (M), and 1024*1024*1024 (G).  For example, 
   * 8500 would be converted to 8.3K, 133456345 to 
   * 127M, 56990456345 to 53G, and so on.  Numbers 
   * smaller than 1024 aren't modified.
   *
   * @param bytes The number of bytes.
   * @return A text representation of the number of bytes,
   *     abbreviated for larger quantities.
   */
  public static String humanReadableSize(double bytes)
  {
    String suffix;
      
    if( bytes >= 1024 * 1024 * 1024 )
    {
      bytes /= (1024 * 1024 * 1024);
      suffix = "G";
    }
    else if( bytes >= 1024 * 1024 )
    {
      bytes /= (1024 * 1024);
      suffix = "M";
    }
    else if( bytes >= 1024 )
    {
      bytes /= 1024;
      suffix = "K";
    }
    else
    {
      suffix = "";
    }
    int frac = (bytes >= 10  ||  bytes == 0)  ?  0  :  1;
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumFractionDigits(frac);
    nf.setMaximumFractionDigits(frac);
    nf.setGroupingUsed(false);
    return nf.format(bytes) + suffix;
  }

}

