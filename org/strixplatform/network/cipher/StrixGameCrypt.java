package org.strixplatform.network.cipher;

import org.strixplatform.configs.MainConfig;
import org.strixplatform.logging.Log;

public class StrixGameCrypt {
   private final byte[] inKey = new byte[16];
   private final byte[] outKey = new byte[16];
   private boolean isEnabled = false;
   private final GuardCipher cryptIn = new GuardCipher();
   private final GuardCipher cryptOut = new GuardCipher();

   public void setKey(byte[] key) {
      System.arraycopy(key, 0, this.inKey, 0, 16);
      System.arraycopy(key, 0, this.outKey, 0, 16);
      if (MainConfig.STRIX_PLATFORM_ENABLED) {
         this.cryptIn.setKey(key);
         this.cryptOut.setKey(key);
      }
   }

   public boolean decrypt(byte[] raw, int offset, int size) {
      if (!this.isEnabled) {
         return true;
      } else if (MainConfig.STRIX_PLATFORM_ENABLED) {
         if (this.cryptIn.keySeted) {
            this.cryptIn.chiper(raw, offset, size);
            return true;
         } else {
            Log.audit("Key not setted. Nulled received packet. Maybe used network hook.");

            for(int i = 0; i < size; ++i) {
               raw[offset + i] = 0;
            }

            return false;
         }
      } else {
         int temp = 0;

         for(int i = 0; i < size; ++i) {
            int temp2 = raw[offset + i] & 255;
            raw[offset + i] = (byte)(temp2 ^ this.inKey[i & 15] ^ temp);
            temp = temp2;
         }

         int old = this.inKey[8] & 255;
         old |= this.inKey[9] << 8 & 0xFF00;
         old |= this.inKey[10] << 16 & 0xFF0000;
         old |= this.inKey[11] << 24 & 0xFF000000;
         old += size;
         this.inKey[8] = (byte)(old & 0xFF);
         this.inKey[9] = (byte)(old >> 8 & 0xFF);
         this.inKey[10] = (byte)(old >> 16 & 0xFF);
         this.inKey[11] = (byte)(old >> 24 & 0xFF);
         return true;
      }
   }

   public boolean encrypt(byte[] raw, int offset, int size) {
      if (!this.isEnabled) {
         this.isEnabled = true;
         return true;
      } else if (MainConfig.STRIX_PLATFORM_ENABLED) {
         if (this.cryptOut.keySeted) {
            this.cryptOut.chiper(raw, offset, size);
            return true;
         } else {
            Log.audit("Key not setted. Nulled send packet. Maybe used network hook.");

            for(int i = 0; i < size; ++i) {
               raw[offset + i] = 0;
            }

            return false;
         }
      } else {
         int temp = 0;

         for(int i = 0; i < size; ++i) {
            int temp2 = raw[offset + i] & 255;
            temp ^= temp2 ^ this.outKey[i & 15];
            raw[offset + i] = (byte)temp;
         }

         int old = this.outKey[8] & 255;
         old |= this.outKey[9] << 8 & 0xFF00;
         old |= this.outKey[10] << 16 & 0xFF0000;
         old |= this.outKey[11] << 24 & 0xFF000000;
         old += size;
         this.outKey[8] = (byte)(old & 0xFF);
         this.outKey[9] = (byte)(old >> 8 & 0xFF);
         this.outKey[10] = (byte)(old >> 16 & 0xFF);
         this.outKey[11] = (byte)(old >> 24 & 0xFF);
         return true;
      }
   }
}
