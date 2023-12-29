package org.jdk;

import java.security.MessageDigest;
import l2e.commons.util.Base64;
import l2e.commons.util.Functions;

public class KeyBuffer {
   private static String _key = "";
   private static boolean _isValid = false;

   public static String applyKey(String s1, String s2) {
      try {
         String toEncrypt = System.getenv("COMPUTERNAME")
            + System.getProperty("user.name")
            + System.getenv("PROCESSOR_IDENTIFIER")
            + System.getenv("PROCESSOR_LEVEL");
         String pcKey = getBufferKey(toEncrypt);
         if (!getGenerateByte(pcKey, getBufferKey(s1), getBufferKey(s2))) {
            _isValid = false;
         }

         return pcKey;
      } catch (Exception var4) {
         return null;
      }
   }

   private static boolean getGenerateByte(String s, String s1, String s2) {
      try {
         String finalKey = s2 + s + s2 + s1;
         MessageDigest md = MessageDigest.getInstance("MD5");
         byte[] raw = finalKey.getBytes("UTF-8");
         byte[] hash = md.digest(raw);
         _key = Base64.encodeBytes(hash);
         Functions.setBuffKey(_key);
         return true;
      } catch (Exception var7) {
         return false;
      }
   }

   private static String getBufferKey(String s) {
      try {
         MessageDigest md = MessageDigest.getInstance("MD5");
         md.update(s.getBytes());
         StringBuffer hexString = new StringBuffer();
         byte[] byteData = md.digest();

         for(byte aByteData : byteData) {
            String hex = Integer.toHexString(255 & aByteData);
            if (hex.length() == 1) {
               hexString.append('0');
            }

            hexString.append(hex);
         }

         return hexString.toString();
      } catch (Exception var9) {
         return null;
      }
   }

   public static String getKey() {
      return _key;
   }

   public static void setValid(boolean val) {
      _isValid = val;
   }

   public static boolean isValid() {
      return _isValid;
   }

   public static boolean isValidBufKey(String key) {
      _isValid = !key.isEmpty() && !_key.isEmpty() && key.equals(_key);
      return _isValid;
   }
}
