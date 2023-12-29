package org.strixplatform.utils;

import org.strixplatform.configs.MainConfig;

public class DataUtils {
   public static void getDecodedDataFromKey(byte[] dataArray, int dataXoredKey) {
      String dataXoredKeyString = String.valueOf(dataXoredKey);
      char[] xorKey = dataXoredKeyString.toCharArray();

      for(int i = 0; i < dataArray.length; ++i) {
         dataArray[i] = (byte)(dataArray[i] ^ xorKey[i % xorKey.length]);
      }
   }

   public static int getDataChecksum(byte[] byteArray, boolean isAdler32) {
      int a = 1;
      int b = 0;

      for(int i = 0; i < byteArray.length; ++i) {
         a = (a + (byteArray[i] & 255)) % (isAdler32 ? '\ufff1' : MainConfig.STRIX_PLATFORM_SECOND_KEY);
         b = (b + a) % (isAdler32 ? '\ufff1' : MainConfig.STRIX_PLATFORM_SECOND_KEY);
      }

      return b << 16 | a;
   }

   public static int getRealDataChecksum(int dataChecksum) {
      return dataChecksum - MainConfig.STRIX_PLATFORM_SECOND_KEY;
   }
}
