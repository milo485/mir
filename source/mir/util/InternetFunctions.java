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

package mir.util;

import java.util.List;

public class InternetFunctions {
  private InternetFunctions() {
  }

  public static boolean isIpAddressInNetwork(String anIpAddress, String aNetwork) throws Exception {
    long ipAddress = parseIPAddress(anIpAddress);
    long network = 0;
    long netMask = (1L<<32)-1;
    List networkParts = StringRoutines.separateString(aNetwork, "/");

    network = parseIPAddress((String) networkParts.get(0));
    if (networkParts.size()>=2) {
      netMask=parseNetmask((String) networkParts.get(1));
    }

    return (ipAddress & netMask ) == (network & netMask);
  }

  public static long parseIPAddress(String anIpAddress) throws Exception {
    int[] parts = {0,0,0,0};
    int i;
    long result;
    List stringParts = StringRoutines.splitString(anIpAddress, ".");

    if (stringParts.size()!=4)
      throw new Exception("Not a valid IP Address: " + anIpAddress);

    try {
      for (i=0; i<4; i++) {
        parts[i] = Integer.parseInt((String) stringParts.get(i));
      }
    }
    catch (Throwable t) {
      throw new Exception("Not a valid IP Address: " + anIpAddress);
    }
    for (i=0; i<4; i++) {
      if (parts[i]<0 || parts[i]>255)
        throw new Exception("Not a valid IP Address: " + anIpAddress);
    }

    return parts[0]<<24 | parts[1]<<16 | parts[2]<<8 | parts[3];
  }

  public static long parseNetmask(String anIpAddress) throws Exception {
    try {
      return parseIPAddress(anIpAddress);
    }
    catch (Throwable t) {
    }

    try {
      int size = Integer.parseInt(anIpAddress);

      if (size<=32)
        return ((1L<<size)-1)<<(32-size);
    }
    catch (Throwable t) {
    }

    return (1L<<32)-1;
  }
}