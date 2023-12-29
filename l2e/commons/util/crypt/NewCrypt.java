package l2e.commons.util.crypt;

public final class NewCrypt {
   private final BlowfishEngine _cipher = new BlowfishEngine();

   public NewCrypt(byte[] blowfishKey) {
      this._cipher.init(blowfishKey);
   }

   public NewCrypt(String key) {
      this(key.getBytes());
   }

   public static boolean verifyChecksum(byte[] raw) {
      return verifyChecksum(raw, 0, raw.length);
   }

   public static boolean verifyChecksum(byte[] raw, int offset, int size) {
      if ((size & 3) == 0 && size > 4) {
         long chksum = 0L;
         int count = size - 4;
         long check = -1L;

         int i;
         for(i = offset; i < count; i += 4) {
            check = (long)(raw[i] & 255);
            check |= (long)(raw[i + 1] << 8 & 0xFF00);
            check |= (long)(raw[i + 2] << 16 & 0xFF0000);
            check |= (long)(raw[i + 3] << 24 & 0xFF000000);
            chksum ^= check;
         }

         check = (long)(raw[i] & 255);
         check |= (long)(raw[i + 1] << 8 & 0xFF00);
         check |= (long)(raw[i + 2] << 16 & 0xFF0000);
         check |= (long)(raw[i + 3] << 24 & 0xFF000000);
         return check == chksum;
      } else {
         return false;
      }
   }

   public static void appendChecksum(byte[] raw) {
      appendChecksum(raw, 0, raw.length);
   }

   public static void appendChecksum(byte[] raw, int offset, int size) {
      long chksum = 0L;
      int count = size - 4;

      int i;
      for(i = offset; i < count; i += 4) {
         long ecx = (long)(raw[i] & 255);
         ecx |= (long)(raw[i + 1] << 8 & 0xFF00);
         ecx |= (long)(raw[i + 2] << 16 & 0xFF0000);
         ecx |= (long)(raw[i + 3] << 24 & 0xFF000000);
         chksum ^= ecx;
      }

      long ecx = (long)(raw[i] & 255);
      ecx |= (long)(raw[i + 1] << 8 & 0xFF00);
      ecx |= (long)(raw[i + 2] << 16 & 0xFF0000);
      ecx |= (long)(raw[i + 3] << 24 & 0xFF000000);
      raw[i] = (byte)((int)(chksum & 255L));
      raw[i + 1] = (byte)((int)(chksum >> 8 & 255L));
      raw[i + 2] = (byte)((int)(chksum >> 16 & 255L));
      raw[i + 3] = (byte)((int)(chksum >> 24 & 255L));
   }

   public static void encXORPass(byte[] raw, int key) {
      encXORPass(raw, 0, raw.length, key);
   }

   static void encXORPass(byte[] raw, int offset, int size, int key) {
      int stop = size - 8;
      int pos = 4 + offset;

      int ecx;
      int var18;
      for(ecx = key; pos < stop; raw[pos++] = (byte)(var18 >> 24 & 0xFF)) {
         int edx = raw[pos] & 255;
         int var15 = edx | (raw[pos + 1] & 255) << 8;
         int var16 = var15 | (raw[pos + 2] & 255) << 16;
         int var17 = var16 | (raw[pos + 3] & 255) << 24;
         ecx += var17;
         var18 = var17 ^ ecx;
         raw[pos++] = (byte)(var18 & 0xFF);
         raw[pos++] = (byte)(var18 >> 8 & 0xFF);
         raw[pos++] = (byte)(var18 >> 16 & 0xFF);
      }

      raw[pos++] = (byte)(ecx & 0xFF);
      raw[pos++] = (byte)(ecx >> 8 & 0xFF);
      raw[pos++] = (byte)(ecx >> 16 & 0xFF);
      raw[pos++] = (byte)(ecx >> 24 & 0xFF);
   }

   public void decrypt(byte[] raw, int offset, int size) {
      for(int i = offset; i < offset + size; i += 8) {
         this._cipher.decryptBlock(raw, i);
      }
   }

   public void crypt(byte[] raw, int offset, int size) {
      for(int i = offset; i < offset + size; i += 8) {
         this._cipher.encryptBlock(raw, i);
      }
   }
}
