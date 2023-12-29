package l2e.commons.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Base64 {
   private static final Logger _log = Logger.getLogger(Base64.class.getName());
   public static final int NO_OPTIONS = 0;
   public static final int ENCODE = 1;
   public static final int DECODE = 0;
   public static final int GZIP = 2;
   public static final int DONT_BREAK_LINES = 8;
   private static final int MAX_LINE_LENGTH = 76;
   private static final byte EQUALS_SIGN = 61;
   private static final byte NEW_LINE = 10;
   private static final Charset PREFERRED_ENCODING = StandardCharsets.UTF_8;
   private static final byte[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(PREFERRED_ENCODING);
   static final byte[] DECODABET = new byte[]{
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
   private static final byte WHITE_SPACE_ENC = -5;
   private static final byte EQUALS_SIGN_ENC = -1;

   private Base64() {
   }

   static byte[] encode3to4(byte[] b4, byte[] threeBytes, int numSigBytes) {
      encode3to4(threeBytes, 0, numSigBytes, b4, 0);
      return b4;
   }

   static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset) {
      int inBuff = (numSigBytes > 0 ? source[srcOffset] << 24 >>> 8 : 0)
         | (numSigBytes > 1 ? source[srcOffset + 1] << 24 >>> 16 : 0)
         | (numSigBytes > 2 ? source[srcOffset + 2] << 24 >>> 24 : 0);
      switch(numSigBytes) {
         case 1:
            destination[destOffset] = ALPHABET[inBuff >>> 18];
            destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 63];
            destination[destOffset + 2] = 61;
            destination[destOffset + 3] = 61;
            return destination;
         case 2:
            destination[destOffset] = ALPHABET[inBuff >>> 18];
            destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 63];
            destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 63];
            destination[destOffset + 3] = 61;
            return destination;
         case 3:
            destination[destOffset] = ALPHABET[inBuff >>> 18];
            destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 63];
            destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 63];
            destination[destOffset + 3] = ALPHABET[inBuff & 63];
            return destination;
         default:
            return destination;
      }
   }

   public static String encodeObject(Serializable serializableObject) {
      return encodeObject(serializableObject, 0);
   }

   public static String encodeObject(Serializable serializableObject, int options) {
      int gzip = options & 2;
      int dontBreakLines = options & 8;
      byte[] value = null;

      try (
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         Base64.OutputStream b64os = new Base64.OutputStream(baos, 1 | dontBreakLines);
         GZIPOutputStream gzipOutputStream = new GZIPOutputStream(b64os);
         FilterOutputStream os = (FilterOutputStream)(gzip == 2 ? gzipOutputStream : b64os);
         ObjectOutputStream oos = new ObjectOutputStream(os);
      ) {
         oos.writeObject(serializableObject);
         value = baos.toByteArray();
      } catch (IOException var136) {
         var136.printStackTrace();
         return null;
      }

      return value != null ? new String(value, PREFERRED_ENCODING) : null;
   }

   public static String encodeBytes(byte[] source) {
      return encodeBytes(source, 0, source.length, 0);
   }

   public static String encodeBytes(byte[] source, int options) {
      return encodeBytes(source, 0, source.length, options);
   }

   public static String encodeBytes(byte[] source, int off, int len) {
      return encodeBytes(source, off, len, 0);
   }

   public static String encodeBytes(byte[] source, int off, int len, int options) {
      int dontBreakLines = options & 8;
      int gzip = options & 2;
      if (gzip == 2) {
         byte[] value = null;

         try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Base64.OutputStream b64os = new Base64.OutputStream(baos, 1 | dontBreakLines);
            GZIPOutputStream gzos = new GZIPOutputStream(b64os);
         ) {
            gzos.write(source, off, len);
            value = baos.toByteArray();
         } catch (IOException var65) {
            _log.warning("Base64: " + var65.getMessage());
            return null;
         }

         if (value != null) {
            return new String(value, PREFERRED_ENCODING);
         }
      }

      boolean breakLines = dontBreakLines == 0;
      int len43 = len * 4 / 3;
      byte[] outBuff = new byte[len43 + (len % 3 > 0 ? 4 : 0) + (breakLines ? len43 / 76 : 0)];
      int d = 0;
      int e = 0;
      int len2 = len - 2;

      for(int lineLength = 0; d < len2; e += 4) {
         encode3to4(source, d + off, 3, outBuff, e);
         lineLength += 4;
         if (breakLines && lineLength == 76) {
            outBuff[e + 4] = 10;
            ++e;
            lineLength = 0;
         }

         d += 3;
      }

      if (d < len) {
         encode3to4(source, d + off, len - d, outBuff, e);
         e += 4;
      }

      return new String(outBuff, 0, e, PREFERRED_ENCODING);
   }

   static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset) {
      if (source[srcOffset + 2] == 61) {
         int outBuff = (DECODABET[source[srcOffset]] & 255) << 18 | (DECODABET[source[srcOffset + 1]] & 255) << 12;
         destination[destOffset] = (byte)(outBuff >>> 16);
         return 1;
      } else if (source[srcOffset + 3] == 61) {
         int outBuff = (DECODABET[source[srcOffset]] & 255) << 18
            | (DECODABET[source[srcOffset + 1]] & 255) << 12
            | (DECODABET[source[srcOffset + 2]] & 255) << 6;
         destination[destOffset] = (byte)(outBuff >>> 16);
         destination[destOffset + 1] = (byte)(outBuff >>> 8);
         return 2;
      } else {
         try {
            int outBuff = (DECODABET[source[srcOffset]] & 255) << 18
               | (DECODABET[source[srcOffset + 1]] & 255) << 12
               | (DECODABET[source[srcOffset + 2]] & 255) << 6
               | DECODABET[source[srcOffset + 3]] & 255;
            destination[destOffset] = (byte)(outBuff >> 16);
            destination[destOffset + 1] = (byte)(outBuff >> 8);
            destination[destOffset + 2] = (byte)outBuff;
            return 3;
         } catch (Exception var5) {
            System.out.println(StringUtil.concat(String.valueOf(source[srcOffset]), ": ", String.valueOf(DECODABET[source[srcOffset]])));
            System.out.println(StringUtil.concat(String.valueOf(source[srcOffset + 1]), ": ", String.valueOf(DECODABET[source[srcOffset + 1]])));
            System.out.println(StringUtil.concat(String.valueOf(source[srcOffset + 2]), ": ", String.valueOf(DECODABET[source[srcOffset + 2]])));
            System.out.println(StringUtil.concat(String.valueOf(source[srcOffset + 3]), ": ", String.valueOf(DECODABET[source[srcOffset + 3]])));
            return -1;
         }
      }
   }

   public static byte[] decode(byte[] source, int off, int len) {
      int len34 = len * 3 / 4;
      byte[] outBuff = new byte[len34];
      int outBuffPosn = 0;
      byte[] b4 = new byte[4];
      int b4Posn = 0;
      int i = 0;
      byte sbiCrop = 0;
      byte sbiDecode = 0;

      for(int var12 = off; var12 < off + len; ++var12) {
         sbiCrop = (byte)(source[var12] & 127);
         sbiDecode = DECODABET[sbiCrop];
         if (sbiDecode < -5) {
            System.err.println(StringUtil.concat("Bad Base64 input character at ", String.valueOf(var12), ": ", String.valueOf(source[var12]), "(decimal)"));
            return null;
         }

         if (sbiDecode >= -1) {
            b4[b4Posn++] = sbiCrop;
            if (b4Posn > 3) {
               outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
               b4Posn = 0;
               if (sbiCrop == 61) {
                  break;
               }
            }
         }
      }

      byte[] out = new byte[outBuffPosn];
      System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
      return out;
   }

   public static byte[] decode(String s) {
      byte[] bytes = s.getBytes(PREFERRED_ENCODING);
      bytes = decode(bytes, 0, bytes.length);
      if (bytes != null && bytes.length >= 2) {
         int head = bytes[0] & 255 | bytes[1] << 8 & 0xFF00;
         if (bytes.length >= 4 && 35615 == head) {
            byte[] buffer = new byte[2048];
            int length = 0;

            try (
               ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
               GZIPInputStream gzis = new GZIPInputStream(bais);
               ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ) {
               while((length = gzis.read(buffer)) >= 0) {
                  baos.write(buffer, 0, length);
               }

               bytes = baos.toByteArray();
            } catch (IOException var63) {
            }
         }
      }

      return bytes;
   }

   public static Object decodeToObject(String encodedObject) {
      byte[] objBytes = decode(encodedObject);
      Object obj = null;

      try (
         ByteArrayInputStream bais = new ByteArrayInputStream(objBytes);
         ObjectInputStream ois = new ObjectInputStream(bais);
      ) {
         obj = ois.readObject();
      } catch (IOException var37) {
         _log.warning("Base64: " + var37.getMessage());
      } catch (ClassNotFoundException var38) {
         _log.warning("Base64: " + var38.getMessage());
      }

      return obj;
   }

   public static class InputStream extends FilterInputStream {
      private final boolean encode;
      private int position;
      private final byte[] buffer;
      private final int bufferLength;
      private int numSigBytes;
      private int lineLength;
      private final boolean breakLines;

      public InputStream(java.io.InputStream pIn) {
         this(pIn, 0);
      }

      public InputStream(java.io.InputStream pIn, int options) {
         super(pIn);
         this.breakLines = (options & 8) != 8;
         this.encode = (options & 1) == 1;
         this.bufferLength = this.encode ? 4 : 3;
         this.buffer = new byte[this.bufferLength];
         this.position = -1;
         this.lineLength = 0;
      }

      @Override
      public int read() throws IOException {
         if (this.position < 0) {
            if (!this.encode) {
               byte[] b4 = new byte[4];
               int i = 0;

               for(i = 0; i < 4; ++i) {
                  int b = 0;

                  do {
                     b = this.in.read();
                  } while(b >= 0 && Base64.DECODABET[b & 127] <= -5);

                  if (b < 0) {
                     break;
                  }

                  b4[i] = (byte)b;
               }

               if (i != 4) {
                  if (i == 0) {
                     return -1;
                  }

                  throw new IOException("Improperly padded Base64 input.");
               }

               this.numSigBytes = Base64.decode4to3(b4, 0, this.buffer, 0);
               this.position = 0;
            } else {
               byte[] b3 = new byte[3];
               int numBinaryBytes = 0;

               for(int i = 0; i < 3; ++i) {
                  try {
                     int b = this.in.read();
                     if (b >= 0) {
                        b3[i] = (byte)b;
                        ++numBinaryBytes;
                     }
                  } catch (IOException var5) {
                     if (i == 0) {
                        throw var5;
                     }
                  }
               }

               if (numBinaryBytes <= 0) {
                  return -1;
               }

               Base64.encode3to4(b3, 0, numBinaryBytes, this.buffer, 0);
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
               int b = this.buffer[this.position++];
               if (this.position >= this.bufferLength) {
                  this.position = -1;
               }

               return b & 0xFF;
            }
         } else {
            throw new IOException("Error in Base64 code reading stream.");
         }
      }

      @Override
      public int read(byte[] dest, int off, int len) throws IOException {
         int i;
         for(i = 0; i < len; ++i) {
            int b = this.read();
            if (b < 0) {
               if (i == 0) {
                  return -1;
               }
               break;
            }

            dest[off + i] = (byte)b;
         }

         return i;
      }
   }

   public static class OutputStream extends FilterOutputStream {
      private final boolean encode;
      private int position;
      private byte[] buffer;
      private final int bufferLength;
      private int lineLength;
      private final boolean breakLines;
      private final byte[] b4;
      private boolean suspendEncoding;

      public OutputStream(java.io.OutputStream pOut) {
         this(pOut, 1);
      }

      public OutputStream(java.io.OutputStream pOut, int options) {
         super(pOut);
         this.breakLines = (options & 8) != 8;
         this.encode = (options & 1) == 1;
         this.bufferLength = this.encode ? 3 : 4;
         this.buffer = new byte[this.bufferLength];
         this.position = 0;
         this.lineLength = 0;
         this.suspendEncoding = false;
         this.b4 = new byte[4];
      }

      @Override
      public void write(int theByte) throws IOException {
         if (this.suspendEncoding) {
            super.out.write(theByte);
         } else {
            if (this.encode) {
               this.buffer[this.position++] = (byte)theByte;
               if (this.position >= this.bufferLength) {
                  this.out.write(Base64.encode3to4(this.b4, this.buffer, this.bufferLength));
                  this.lineLength += 4;
                  if (this.breakLines && this.lineLength >= 76) {
                     this.out.write(10);
                     this.lineLength = 0;
                  }

                  this.position = 0;
               }
            } else if (Base64.DECODABET[theByte & 127] > -5) {
               this.buffer[this.position++] = (byte)theByte;
               if (this.position >= this.bufferLength) {
                  int len = Base64.decode4to3(this.buffer, 0, this.b4, 0);
                  this.out.write(this.b4, 0, len);
                  this.position = 0;
               }
            } else if (Base64.DECODABET[theByte & 127] != -5) {
               throw new IOException("Invalid character in Base64 data.");
            }
         }
      }

      @Override
      public void write(byte[] theBytes, int off, int len) throws IOException {
         if (this.suspendEncoding) {
            super.out.write(theBytes, off, len);
         } else {
            for(int i = 0; i < len; ++i) {
               this.write(theBytes[off + i]);
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

      @Override
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
