package org.mozilla.universalchardet.prober;

import java.nio.ByteBuffer;

public abstract class CharsetProber {
   public static final float SHORTCUT_THRESHOLD = 0.95F;
   public static final int ASCII_A = 97;
   public static final int ASCII_Z = 122;
   public static final int ASCII_A_CAPITAL = 65;
   public static final int ASCII_Z_CAPITAL = 90;
   public static final int ASCII_LT = 60;
   public static final int ASCII_GT = 62;
   public static final int ASCII_SP = 32;

   public abstract String getCharSetName();

   public abstract CharsetProber.ProbingState handleData(byte[] var1, int var2, int var3);

   public abstract CharsetProber.ProbingState getState();

   public abstract void reset();

   public abstract float getConfidence();

   public abstract void setOption();

   public ByteBuffer filterWithoutEnglishLetters(byte[] var1, int var2, int var3) {
      ByteBuffer var4 = ByteBuffer.allocate(var3);
      boolean var5 = false;
      int var7 = var2;
      int var8 = var2;

      for(int var9 = var2 + var3; var8 < var9; ++var8) {
         byte var6 = var1[var8];
         if (!this.isAscii(var6)) {
            var5 = true;
         } else if (this.isAsciiSymbol(var6)) {
            if (var5 && var8 > var7) {
               var4.put(var1, var7, var8 - var7);
               var4.put((byte)32);
               var7 = var8 + 1;
               var5 = false;
            } else {
               var7 = var8 + 1;
            }
         }
      }

      if (var5 && var8 > var7) {
         var4.put(var1, var7, var8 - var7);
      }

      return var4;
   }

   public ByteBuffer filterWithEnglishLetters(byte[] var1, int var2, int var3) {
      ByteBuffer var4 = ByteBuffer.allocate(var3);
      boolean var5 = false;
      int var7 = var2;
      int var8 = var2;

      for(int var9 = var2 + var3; var8 < var9; ++var8) {
         byte var6 = var1[var8];
         if (var6 == 62) {
            var5 = false;
         } else if (var6 == 60) {
            var5 = true;
         }

         if (this.isAscii(var6) && this.isAsciiSymbol(var6)) {
            if (var8 > var7 && !var5) {
               var4.put(var1, var7, var8 - var7);
               var4.put((byte)32);
               var7 = var8 + 1;
            } else {
               var7 = var8 + 1;
            }
         }
      }

      if (!var5 && var8 > var7) {
         var4.put(var1, var7, var8 - var7);
      }

      return var4;
   }

   private boolean isAscii(byte var1) {
      return (var1 & 128) == 0;
   }

   private boolean isAsciiSymbol(byte var1) {
      int var2 = var1 & 255;
      return var2 < 65 || var2 > 90 && var2 < 97 || var2 > 122;
   }

   public static enum ProbingState {
      DETECTING,
      FOUND_IT,
      NOT_ME;
   }
}
