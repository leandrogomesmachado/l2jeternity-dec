package org.napile;

public class HashUtils {
   public static int hashCode(int val) {
      return val;
   }

   public static int hashCode(long val) {
      return (int)(val ^ val >>> 32);
   }

   public static int hashCode(Object val) {
      return val == null ? 0 : val.hashCode();
   }
}
