package jonelo.jacksum.adapt.org.bouncycastle.util;

public final class Arrays {
   private Arrays() {
   }

   public static boolean areEqual(byte[] var0, byte[] var1) {
      if (var0.length != var1.length) {
         return false;
      } else {
         for(int var2 = 0; var2 != var0.length; ++var2) {
            if (var0[var2] != var1[var2]) {
               return false;
            }
         }

         return true;
      }
   }

   public static void fill(byte[] var0, byte var1) {
      for(int var2 = 0; var2 < var0.length; ++var2) {
         var0[var2] = var1;
      }
   }

   public static void fill(long[] var0, long var1) {
      for(int var3 = 0; var3 < var0.length; ++var3) {
         var0[var3] = var1;
      }
   }

   public static void fill(short[] var0, short var1) {
      for(int var2 = 0; var2 < var0.length; ++var2) {
         var0[var2] = var1;
      }
   }
}
