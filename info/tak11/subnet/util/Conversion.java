package info.tak11.subnet.util;

import java.text.DecimalFormat;

public class Conversion {
   public static String toBinary(String dec) {
      DecimalFormat df = new DecimalFormat("00000000");
      int v = Integer.parseInt(dec);
      String s = Integer.toBinaryString(v);
      int val = Integer.parseInt(s);
      return df.format((long)val);
   }

   public static String toDecimal(String bin) {
      return Integer.toString(Integer.parseInt(bin, 2));
   }

   public static String ipToString(int a, int b, int c, int d) {
      return Integer.toString(a) + "." + Integer.toString(b) + "." + Integer.toString(c) + "." + Integer.toString(d);
   }

   public static char[][] ipToBin(String ip) {
      String[] split = ip.split("[.]");
      char[] a1 = toBinary(split[0]).toCharArray();
      char[] b1 = toBinary(split[1]).toCharArray();
      char[] c1 = toBinary(split[2]).toCharArray();
      char[] d1 = toBinary(split[3]).toCharArray();
      return new char[][]{a1, b1, c1, d1};
   }
}
