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