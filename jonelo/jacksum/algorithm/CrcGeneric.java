package jonelo.jacksum.algorithm;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import jonelo.jacksum.util.Service;
import jonelo.sugar.util.GeneralString;

public class CrcGeneric extends AbstractChecksum {
   private int width;
   private long poly;
   private long initialValue;
   private boolean refIn;
   private boolean refOut;
   private long xorOut;
   private long[] table;
   private long topBit;
   private long maskAllBits;
   private long maskHelp;

   public CrcGeneric(int var1, long var2, long var4, boolean var6, boolean var7, long var8) throws NoSuchAlgorithmException {
      this.width = var1;
      this.poly = var2;
      this.initialValue = var4;
      this.refIn = var6;
      this.refOut = var7;
      this.xorOut = var8;
      this.init();
   }

   public CrcGeneric(String var1) throws NoSuchAlgorithmException {
      String[] var2 = GeneralString.split(var1, ",");
      if (var2.length != 6) {
         throw new NoSuchAlgorithmException("Can't create the algorithm, 6 parameters are expected");
      } else {
         try {
            this.width = Integer.parseInt(var2[0]);
            this.poly = Long.parseLong(var2[1], 16);
            this.initialValue = new BigInteger(var2[2], 16).longValue();
            this.refIn = var2[3].equalsIgnoreCase("true");
            this.refOut = var2[4].equalsIgnoreCase("true");
            this.xorOut = new BigInteger(var2[5], 16).longValue();
         } catch (NumberFormatException var4) {
            throw new NoSuchAlgorithmException("Unknown algorithm: invalid parameters. " + var4.toString());
         }

         this.init();
      }
   }

   private void init() throws NoSuchAlgorithmException {
      this.topBit = 1L << this.width - 1;
      this.maskAllBits = -1L >>> 64 - this.width;
      this.maskHelp = this.maskAllBits >>> 8;
      this.check();
      this.fillTable();
      this.reset();
   }

   private void check() throws NoSuchAlgorithmException {
      if (this.width < 8 || this.width > 64) {
         throw new NoSuchAlgorithmException("Error: width has to be in range [8..64].");
      } else if (this.poly != (this.poly & this.maskAllBits)) {
         throw new NoSuchAlgorithmException("Error: invalid polynomial for the " + this.width + " bit CRC.");
      } else if (this.initialValue != (this.initialValue & this.maskAllBits)) {
         throw new NoSuchAlgorithmException("Error: invalid init value for the " + this.width + " bit CRC.");
      } else if (this.xorOut != (this.xorOut & this.maskAllBits)) {
         throw new NoSuchAlgorithmException("Error: invalid xorOut value for the " + this.width + " bit CRC.");
      }
   }

   public void reset() {
      this.length = 0L;
      this.value = this.initialValue;
      if (this.refIn) {
         this.value = reflect(this.value, this.width);
      }
   }

   public String getString() {
      StringBuffer var1 = new StringBuffer();
      int var2 = this.width / 4 + (this.width % 4 > 0 ? 1 : 0);
      var1.append(this.width);
      var1.append(",");
      var1.append(Service.hexformat(this.poly, var2).toUpperCase());
      var1.append(",");
      var1.append(Service.hexformat(this.initialValue, var2).toUpperCase());
      var1.append(",");
      var1.append(this.refIn ? "true" : "false");
      var1.append(",");
      var1.append(this.refOut ? "true" : "false");
      var1.append(",");
      var1.append(Service.hexformat(this.xorOut, var2).toUpperCase());
      return var1.toString();
   }

   public String getName() {
      return this.name == null ? this.getString() : this.name;
   }

   public void setInitialValue(long var1) {
      this.initialValue = var1;
   }

   public long getInitialValue() {
      return this.initialValue;
   }

   public void setWidth(int var1) {
      this.width = var1;
   }

   public int getWidth() {
      return this.width;
   }

   public void setPoly(long var1) {
      this.poly = var1;
   }

   public long getPoly() {
      return this.poly;
   }

   public void setRefIn(boolean var1) {
      this.refIn = var1;
   }

   public boolean getRefIn() {
      return this.refIn;
   }

   public void setRefOut(boolean var1) {
      this.refOut = var1;
   }

   public boolean getRefOut() {
      return this.refOut;
   }

   public void setXorOut(long var1) {
      this.xorOut = var1;
   }

   public long getXorOut() {
      return this.xorOut;
   }

   private static long reflect(long var0, int var2) {
      long var3 = 0L;

      for(int var5 = 0; var5 < var2; ++var5) {
         var3 <<= 1;
         var3 |= var0 & 1L;
         var0 >>>= 1;
      }

      return var0 << var2 | var3;
   }

   private void fillTable() {
      this.table = new long[256];

      for(int var4 = 0; var4 < 256; ++var4) {
         long var1 = (long)var4;
         if (this.refIn) {
            var1 = reflect(var1, 8);
         }

         var1 <<= this.width - 8;

         for(int var5 = 0; var5 < 8; ++var5) {
            boolean var3 = (var1 & this.topBit) != 0L;
            var1 <<= 1;
            if (var3) {
               var1 ^= this.poly;
            }
         }

         if (this.refIn) {
            var1 = reflect(var1, this.width);
         }

         this.table[var4] = var1 & this.maskAllBits;
      }
   }

   public void update(byte var1) {
      int var2;
      if (this.refIn) {
         var2 = (int)(this.value ^ (long)var1) & 0xFF;
         this.value >>>= 8;
         this.value &= this.maskHelp;
      } else {
         var2 = (int)(this.value >>> this.width - 8 ^ (long)var1) & 0xFF;
         this.value <<= 8;
      }

      this.value ^= this.table[var2];
      ++this.length;
   }

   public void update(int var1) {
      this.update((byte)(var1 & 0xFF));
   }

   public long getValue() {
      return this.getFinal();
   }

   private long getFinal() {
      long var1 = this.value;
      if (this.refIn != this.refOut) {
         var1 = reflect(var1, this.width);
      }

      return (var1 ^ this.xorOut) & this.maskAllBits;
   }

   public byte[] getByteArray() {
      long var1 = this.getFinal();
      byte[] var3 = new byte[this.width / 8 + (this.width % 8 > 0 ? 1 : 0)];

      for(int var4 = var3.length - 1; var4 > -1; --var4) {
         var3[var4] = (byte)((int)(var1 & 255L));
         var1 >>>= 8;
      }

      return var3;
   }
}
