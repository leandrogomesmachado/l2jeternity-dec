package jonelo.sugar.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Base64 {
   public static final int NO_OPTIONS = 0;
   public static final int ENCODE = 1;
   public static final int DECODE = 0;
   public static final int GZIP = 2;
   public static final int DONT_BREAK_LINES = 8;
   private static final int MAX_LINE_LENGTH = 76;
   private static final byte EQUALS_SIGN = 61;
   private static final byte NEW_LINE = 10;
   private static final String PREFERRED_ENCODING = "UTF-8";
   private static final byte[] ALPHABET;
   private static final byte[] _NATIVE_ALPHABET = new byte[]{
      65,
      66,
      67,
      68,
      69,
      70,
      71,
      72,
      73,
      74,
      75,
      76,
      77,
      78,
      79,
      80,
      81,
      82,
      83,
      84,
      85,
      86,
      87,
      88,
      89,
      90,
      97,
      98,
      99,
      100,
      101,
      102,
      103,
      104,
      105,
      106,
      107,
      108,
      109,
      110,
      111,
      112,
      113,
      114,
      115,
      116,
      117,
      118,
      119,
      120,
      121,
      122,
      48,
      49,
      50,
      51,
      52,
      53,
      54,
      55,
      56,
      57,
      43,
      47
   };
   private static final byte[] DECODABET;
   private static final byte WHITE_SPACE_ENC = -5;
   private static final byte EQUALS_SIGN_ENC = -1;

   private Base64() {
   }

   private static byte[] encode3to4(byte[] var0, byte[] var1, int var2) {
      encode3to4(var1, 0, var2, var0, 0);
      return var0;
   }

   private static byte[] encode3to4(byte[] var0, int var1, int var2, byte[] var3, int var4) {
      int var5 = (var2 > 0 ? var0[var1] << 24 >>> 8 : 0) | (var2 > 1 ? var0[var1 + 1] << 24 >>> 16 : 0) | (var2 > 2 ? var0[var1 + 2] << 24 >>> 24 : 0);
      switch(var2) {
         case 1:
            var3[var4] = ALPHABET[var5 >>> 18];
            var3[var4 + 1] = ALPHABET[var5 >>> 12 & 63];
            var3[var4 + 2] = 61;
            var3[var4 + 3] = 61;
            return var3;
         case 2:
            var3[var4] = ALPHABET[var5 >>> 18];
            var3[var4 + 1] = ALPHABET[var5 >>> 12 & 63];
            var3[var4 + 2] = ALPHABET[var5 >>> 6 & 63];
            var3[var4 + 3] = 61;
            return var3;
         case 3:
            var3[var4] = ALPHABET[var5 >>> 18];
            var3[var4 + 1] = ALPHABET[var5 >>> 12 & 63];
            var3[var4 + 2] = ALPHABET[var5 >>> 6 & 63];
            var3[var4 + 3] = ALPHABET[var5 & 63];
            return var3;
         default:
            return var3;
      }
   }

   public static String encodeObject(Serializable var0) {
      return encodeObject(var0, 0);
   }

   public static String encodeObject(Serializable var0, int var1) {
      ByteArrayOutputStream var2 = null;
      Base64.OutputStream var3 = null;
      ObjectOutputStream var4 = null;
      GZIPOutputStream var5 = null;
      int var6 = var1 & 2;
      int var7 = var1 & 8;

      label126: {
         Object var9;
         try {
            var2 = new ByteArrayOutputStream();
            var3 = new Base64.OutputStream(var2, 1 | var7);
            if (var6 == 2) {
               var5 = new GZIPOutputStream(var3);
               var4 = new ObjectOutputStream(var5);
            } else {
               var4 = new ObjectOutputStream(var3);
            }

            var4.writeObject(var0);
            break label126;
         } catch (IOException var33) {
            var33.printStackTrace();
            var9 = null;
         } finally {
            try {
               var4.close();
            } catch (Exception var31) {
            }

            try {
               var5.close();
            } catch (Exception var30) {
            }

            try {
               var3.close();
            } catch (Exception var29) {
            }

            try {
               var2.close();
            } catch (Exception var28) {
            }
         }

         return (String)var9;
      }

      try {
         return new String(var2.toByteArray(), "UTF-8");
      } catch (UnsupportedEncodingException var32) {
         return new String(var2.toByteArray());
      }
   }

   public static String encodeBytes(byte[] var0) {
      return encodeBytes(var0, 0, var0.length, 0);
   }

   public static String encodeBytes(byte[] var0, int var1) {
      return encodeBytes(var0, 0, var0.length, var1);
   }

   public static String encodeBytes(byte[] var0, int var1, int var2) {
      return encodeBytes(var0, var1, var2, 0);
   }

   public static String encodeBytes(byte[] var0, int var1, int var2, int var3) {
      int var4 = var3 & 8;
      int var5 = var3 & 2;
      if (var5 == 2) {
         ByteArrayOutputStream var34 = null;
         GZIPOutputStream var35 = null;
         Base64.OutputStream var36 = null;

         label157: {
            Object var37;
            try {
               var34 = new ByteArrayOutputStream();
               var36 = new Base64.OutputStream(var34, 1 | var4);
               var35 = new GZIPOutputStream(var36);
               var35.write(var0, var1, var2);
               var35.close();
               break label157;
            } catch (IOException var32) {
               var32.printStackTrace();
               var37 = null;
            } finally {
               try {
                  var35.close();
               } catch (Exception var29) {
               }

               try {
                  var36.close();
               } catch (Exception var28) {
               }

               try {
                  var34.close();
               } catch (Exception var27) {
               }
            }

            return (String)var37;
         }

         try {
            return new String(var34.toByteArray(), "UTF-8");
         } catch (UnsupportedEncodingException var30) {
            return new String(var34.toByteArray());
         }
      } else {
         boolean var6 = var4 == 0;
         int var7 = var2 * 4 / 3;
         byte[] var8 = new byte[var7 + (var2 % 3 > 0 ? 4 : 0) + (var6 ? var7 / 76 : 0)];
         int var9 = 0;
         int var10 = 0;
         int var11 = var2 - 2;

         for(int var12 = 0; var9 < var11; var10 += 4) {
            encode3to4(var0, var9 + var1, 3, var8, var10);
            var12 += 4;
            if (var6 && var12 == 76) {
               var8[var10 + 4] = 10;
               ++var10;
               var12 = 0;
            }

            var9 += 3;
         }

         if (var9 < var2) {
            encode3to4(var0, var9 + var1, var2 - var9, var8, var10);
            var10 += 4;
         }

         try {
            return new String(var8, 0, var10, "UTF-8");
         } catch (UnsupportedEncodingException var31) {
            return new String(var8, 0, var10);
         }
      }
   }

   private static int decode4to3(byte[] var0, int var1, byte[] var2, int var3) {
      if (var0[var1 + 2] == 61) {
         int var7 = (DECODABET[var0[var1]] & 255) << 18 | (DECODABET[var0[var1 + 1]] & 255) << 12;
         var2[var3] = (byte)(var7 >>> 16);
         return 1;
      } else if (var0[var1 + 3] == 61) {
         int var6 = (DECODABET[var0[var1]] & 255) << 18 | (DECODABET[var0[var1 + 1]] & 255) << 12 | (DECODABET[var0[var1 + 2]] & 255) << 6;
         var2[var3] = (byte)(var6 >>> 16);
         var2[var3 + 1] = (byte)(var6 >>> 8);
         return 2;
      } else {
         try {
            int var4 = (DECODABET[var0[var1]] & 255) << 18
               | (DECODABET[var0[var1 + 1]] & 255) << 12
               | (DECODABET[var0[var1 + 2]] & 255) << 6
               | DECODABET[var0[var1 + 3]] & 255;
            var2[var3] = (byte)(var4 >> 16);
            var2[var3 + 1] = (byte)(var4 >> 8);
            var2[var3 + 2] = (byte)var4;
            return 3;
         } catch (Exception var5) {
            System.out.println("" + var0[var1] + ": " + DECODABET[var0[var1]]);
            System.out.println("" + var0[var1 + 1] + ": " + DECODABET[var0[var1 + 1]]);
            System.out.println("" + var0[var1 + 2] + ": " + DECODABET[var0[var1 + 2]]);
            System.out.println("" + var0[var1 + 3] + ": " + DECODABET[var0[var1 + 3]]);
            return -1;
         }
      }
   }

   public static byte[] decode(byte[] var0, int var1, int var2) {
      int var3 = var2 * 3 / 4;
      byte[] var4 = new byte[var3];
      int var5 = 0;
      byte[] var6 = new byte[4];
      int var7 = 0;
      boolean var8 = false;
      byte var9 = 0;
      byte var10 = 0;

      for(int var12 = var1; var12 < var1 + var2; ++var12) {
         var9 = (byte)(var0[var12] & 127);
         var10 = DECODABET[var9];
         if (var10 < -5) {
            System.err.println("Bad Base64 input character at " + var12 + ": " + var0[var12] + "(decimal)");
            return null;
         }

         if (var10 >= -1) {
            var6[var7++] = var9;
            if (var7 > 3) {
               var5 += decode4to3(var6, 0, var4, var5);
               var7 = 0;
               if (var9 == 61) {
                  break;
               }
            }
         }
      }

      byte[] var11 = new byte[var5];
      System.arraycopy(var4, 0, var11, 0, var5);
      return var11;
   }

   public static byte[] decode(String var0) {
      byte[] var1;
      try {
         var1 = var0.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var26) {
         var1 = var0.getBytes();
      }

      var1 = decode(var1, 0, var1.length);
      if (var1 != null && var1.length >= 4) {
         int var2 = var1[0] & 255 | var1[1] << 8 & 0xFF00;
         if (35615 == var2) {
            ByteArrayInputStream var3 = null;
            GZIPInputStream var4 = null;
            ByteArrayOutputStream var5 = null;
            byte[] var6 = new byte[2048];
            int var7 = 0;

            try {
               var5 = new ByteArrayOutputStream();
               var3 = new ByteArrayInputStream(var1);
               var4 = new GZIPInputStream(var3);

               while((var7 = var4.read(var6)) >= 0) {
                  var5.write(var6, 0, var7);
               }

               var1 = var5.toByteArray();
            } catch (IOException var27) {
            } finally {
               try {
                  var5.close();
               } catch (Exception var25) {
               }

               try {
                  var4.close();
               } catch (Exception var24) {
               }

               try {
                  var3.close();
               } catch (Exception var23) {
               }
            }
         }
      }

      return var1;
   }

   public static Object decodeToObject(String var0) {
      byte[] var1 = decode(var0);
      ByteArrayInputStream var2 = null;
      ObjectInputStream var3 = null;
      Object var4 = null;

      try {
         var2 = new ByteArrayInputStream(var1);
         var3 = new ObjectInputStream(var2);
         var4 = var3.readObject();
      } catch (IOException var21) {
         var21.printStackTrace();
         var4 = null;
      } catch (ClassNotFoundException var22) {
         var22.printStackTrace();
         var4 = null;
      } finally {
         try {
            var2.close();
         } catch (Exception var20) {
         }

         try {
            var3.close();
         } catch (Exception var19) {
         }
      }

      return var4;
   }

   public static boolean encodeToFile(byte[] var0, String var1) {
      boolean var2 = false;
      Base64.OutputStream var3 = null;

      try {
         var3 = new Base64.OutputStream(new FileOutputStream(var1), 1);
         var3.write(var0);
         var2 = true;
      } catch (IOException var13) {
         var2 = false;
      } finally {
         try {
            var3.close();
         } catch (Exception var12) {
         }
      }

      return var2;
   }

   public static boolean decodeToFile(String var0, String var1) {
      boolean var2 = false;
      Base64.OutputStream var3 = null;

      try {
         var3 = new Base64.OutputStream(new FileOutputStream(var1), 0);
         var3.write(var0.getBytes("UTF-8"));
         var2 = true;
      } catch (IOException var13) {
         var2 = false;
      } finally {
         try {
            var3.close();
         } catch (Exception var12) {
         }
      }

      return var2;
   }

   public static byte[] decodeFromFile(String var0) {
      byte[] var1 = null;
      Base64.InputStream var2 = null;

      Object var7;
      try {
         File var3 = new File(var0);
         Object var4 = null;
         int var5 = 0;
         int var6 = 0;
         if (var3.length() <= 2147483647L) {
            byte[] var20 = new byte[(int)var3.length()];
            var2 = new Base64.InputStream(new BufferedInputStream(new FileInputStream(var3)), 0);

            while((var6 = var2.read(var20, var5, 4096)) >= 0) {
               var5 += var6;
            }

            var1 = new byte[var5];
            System.arraycopy(var20, 0, var1, 0, var5);
            return var1;
         }

         System.err.println("File is too big for this convenience method (" + var3.length() + " bytes).");
         var7 = null;
      } catch (IOException var18) {
         System.err.println("Error decoding from file " + var0);
         return var1;
      } finally {
         if (var2 != null) {
            try {
               var2.close();
            } catch (Exception var17) {
            }
         }
      }

      return (byte[])var7;
   }

   public static String encodeFromFile(String var0) {
      String var1 = null;
      Base64.InputStream var2 = null;

      try {
         File var3 = new File(var0);
         byte[] var4 = new byte[(int)((double)var3.length() * 1.4)];
         int var5 = 0;
         int var6 = 0;
         var2 = new Base64.InputStream(new BufferedInputStream(new FileInputStream(var3)), 1);

         while((var6 = var2.read(var4, var5, 4096)) >= 0) {
            var5 += var6;
         }

         var1 = new String(var4, 0, var5, "UTF-8");
      } catch (IOException var15) {
         System.err.println("Error encoding from file " + var0);
      } finally {
         try {
            var2.close();
         } catch (Exception var14) {
         }
      }

      return var1;
   }

   static {
      byte[] var0;
      try {
         var0 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes("UTF-8");
      } catch (UnsupportedEncodingException var2) {
         var0 = _NATIVE_ALPHABET;
      }

      ALPHABET = var0;
      DECODABET = new byte[]{
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -5,
         -5,
         -9,
         -9,
         -5,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -5,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         62,
         -9,
         -9,
         -9,
         63,
         52,
         53,
         54,
         55,
         56,
         57,
         58,
         59,
         60,
         61,
         -9,
         -9,
         -9,
         -1,
         -9,
         -9,
         -9,
         0,
         1,
         2,
         3,
         4,
         5,
         6,
         7,
         8,
         9,
         10,
         11,
         12,
         13,
         14,
         15,
         16,
         17,
         18,
         19,
         20,
         21,
         22,
         23,
         24,
         25,
         -9,
         -9,
         -9,
         -9,
         -9,
         -9,
         26,
         27,
         28,
         29,
         30,
         31,
         32,
         33,
         34,
         35,
         36,
         37,
         38,
         39,
         40,
         41,
         42,
         43,
         44,
         45,
         46,
         47,
         48,
         49,
         50,
         51,
         -9,
         -9,
         -9,
         -9
      };
   }

   public static class InputStream extends FilterInputStream {
      private boolean encode;
      private int position;
      private byte[] buffer;
      private int bufferLength;
      private int numSigBytes;
      private int lineLength;
      private boolean breakLines;

      public InputStream(java.io.InputStream var1) {
         this(var1, 0);
      }

      public InputStream(java.io.InputStream var1, int var2) {
         super(var1);
         this.breakLines = (var2 & 8) != 8;
         this.encode = (var2 & 1) == 1;
         this.bufferLength = this.encode ? 4 : 3;
         this.buffer = new byte[this.bufferLength];
         this.position = -1;
         this.lineLength = 0;
      }

      public int read() throws IOException {
         if (this.position < 0) {
            if (!this.encode) {
               byte[] var6 = new byte[4];
               int var8 = 0;

               for(var8 = 0; var8 < 4; ++var8) {
                  int var10 = 0;

                  do {
                     var10 = this.in.read();
                  } while(var10 >= 0 && Base64.DECODABET[var10 & 127] <= -5);

                  if (var10 < 0) {
                     break;
                  }

                  var6[var8] = (byte)var10;
               }

               if (var8 != 4) {
                  if (var8 == 0) {
                     return -1;
                  }

                  throw new IOException("Improperly padded Base64 input.");
               }

               this.numSigBytes = Base64.decode4to3(var6, 0, this.buffer, 0);
               this.position = 0;
            } else {
               byte[] var1 = new byte[3];
               int var2 = 0;

               for(int var3 = 0; var3 < 3; ++var3) {
                  try {
                     int var4 = this.in.read();
                     if (var4 >= 0) {
                        var1[var3] = (byte)var4;
                        ++var2;
                     }
                  } catch (IOException var5) {
                     if (var3 == 0) {
                        throw var5;
                     }
                  }
               }

               if (var2 <= 0) {
                  return -1;
               }

               Base64.encode3to4(var1, 0, var2, this.buffer, 0);
               this.position = 0;
               this.numSigBytes = 4;
            }
         }

         if (this.position >= 0) {
            if (this.position >= this.numSigBytes) {
               return -1;
            } else if (this.encode && this.breakLines && this.lineLength >= 76) {
               this.lineLength = 0;
               return 10;
            } else {
               ++this.lineLength;
               byte var7 = this.buffer[this.position++];
               if (this.position >= this.bufferLength) {
                  this.position = -1;
               }

               return var7 & 0xFF;
            }
         } else {
            throw new IOException("Error in Base64 code reading stream.");
         }
      }

      public int read(byte[] var1, int var2, int var3) throws IOException {
         int var4;
         for(var4 = 0; var4 < var3; ++var4) {
            int var5 = this.read();
            if (var5 < 0) {
               if (var4 == 0) {
                  return -1;
               }
               break;
            }

            var1[var2 + var4] = (byte)var5;
         }

         return var4;
      }
   }

   public static class OutputStream extends FilterOutputStream {
      private boolean encode;
      private int position;
      private byte[] buffer;
      private int bufferLength;
      private int lineLength;
      private boolean breakLines;
      private byte[] b4;
      private boolean suspendEncoding;

      public OutputStream(java.io.OutputStream var1) {
         this(var1, 1);
      }

      public OutputStream(java.io.OutputStream var1, int var2) {
         super(var1);
         this.breakLines = (var2 & 8) != 8;
         this.encode = (var2 & 1) == 1;
         this.bufferLength = this.encode ? 3 : 4;
         this.buffer = new byte[this.bufferLength];
         this.position = 0;
         this.lineLength = 0;
         this.suspendEncoding = false;
         this.b4 = new byte[4];
      }

      public void write(int var1) throws IOException {
         if (this.suspendEncoding) {
            super.out.write(var1);
         } else {
            if (this.encode) {
               this.buffer[this.position++] = (byte)var1;
               if (this.position >= this.bufferLength) {
                  this.out.write(Base64.encode3to4(this.b4, this.buffer, this.bufferLength));
                  this.lineLength += 4;
                  if (this.breakLines && this.lineLength >= 76) {
                     this.out.write(10);
                     this.lineLength = 0;
                  }

                  this.position = 0;
               }
            } else if (Base64.DECODABET[var1 & 127] > -5) {
               this.buffer[this.position++] = (byte)var1;
               if (this.position >= this.bufferLength) {
                  int var2 = Base64.decode4to3(this.buffer, 0, this.b4, 0);
                  this.out.write(this.b4, 0, var2);
                  this.position = 0;
               }
            } else if (Base64.DECODABET[var1 & 127] != -5) {
               throw new IOException("Invalid character in Base64 data.");
            }
         }
      }

      public void write(byte[] var1, int var2, int var3) throws IOException {
         if (this.suspendEncoding) {
            super.out.write(var1, var2, var3);
         } else {
            for(int var4 = 0; var4 < var3; ++var4) {
               this.write(var1[var2 + var4]);
            }
         }
      }

      public void flushBase64() throws IOException {
         if (this.position > 0) {
            if (!this.encode) {
               throw new IOException("Base64 input not properly padded.");
            }

            this.out.write(Base64.encode3to4(this.b4, this.buffer, this.position));
            this.position = 0;
         }
      }

      public void close() throws IOException {
         this.flushBase64();
         super.close();
         this.buffer = null;
         this.out = null;
      }

      public void suspendEncoding() throws IOException {
         this.flushBase64();
         this.suspendEncoding = true;
      }

      public void resumeEncoding() {
         this.suspendEncoding = false;
      }
   }
}
