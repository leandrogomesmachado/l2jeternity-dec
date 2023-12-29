package org.json;

import java.util.Arrays;

public class Kim {
   private byte[] bytes = null;
   private int hashcode = 0;
   public int length = 0;
   private String string = null;

   public Kim(byte[] bytes, int from, int thru) {
      int sum = 1;
      this.hashcode = 0;
      this.length = thru - from;
      if (this.length > 0) {
         this.bytes = new byte[this.length];

         for(int at = 0; at < this.length; ++at) {
            int value = bytes[at + from] & 255;
            sum += value;
            this.hashcode += sum;
            this.bytes[at] = (byte)value;
         }

         this.hashcode += sum << 16;
      }
   }

   public Kim(byte[] bytes, int length) {
      this(bytes, 0, length);
   }

   public Kim(Kim kim, int from, int thru) {
      this(kim.bytes, from, thru);
   }

   public Kim(String string) throws JSONException {
      int stringLength = string.length();
      this.hashcode = 0;
      this.length = 0;
      if (stringLength > 0) {
         for(int i = 0; i < stringLength; ++i) {
            int c = string.charAt(i);
            if (c <= 127) {
               ++this.length;
            } else if (c <= 16383) {
               this.length += 2;
            } else {
               if (c >= 55296 && c <= 57343) {
                  int d = string.charAt(++i);
                  if (c > 56319 || d < 56320 || d > 57343) {
                     throw new JSONException("Bad UTF16");
                  }
               }

               this.length += 3;
            }
         }

         this.bytes = new byte[this.length];
         int at = 0;
         int sum = 1;

         for(int i = 0; i < stringLength; ++i) {
            int character = string.charAt(i);
            if (character <= 127) {
               this.bytes[at] = (byte)character;
               sum += character;
               this.hashcode += sum;
               ++at;
            } else if (character <= 16383) {
               int b = 128 | character >>> 7;
               this.bytes[at] = (byte)b;
               int var18 = sum + b;
               this.hashcode += var18;
               ++at;
               b = character & 127;
               this.bytes[at] = (byte)b;
               sum = var18 + b;
               this.hashcode += sum;
               ++at;
            } else {
               if (character >= 55296 && character <= 56319) {
                  character = ((character & 1023) << 10 | string.charAt(++i) & 1023) + 65536;
               }

               int b = 128 | character >>> 14;
               this.bytes[at] = (byte)b;
               int var19 = sum + b;
               this.hashcode += var19;
               ++at;
               b = 128 | character >>> 7 & 0xFF;
               this.bytes[at] = (byte)b;
               int var20 = var19 + b;
               this.hashcode += var20;
               ++at;
               b = character & 127;
               this.bytes[at] = (byte)b;
               sum = var20 + b;
               this.hashcode += sum;
               ++at;
            }
         }

         this.hashcode += sum << 16;
      }
   }

   public int characterAt(int at) throws JSONException {
      int c = this.get(at);
      if ((c & 128) == 0) {
         return c;
      } else {
         int c1 = this.get(at + 1);
         if ((c1 & 128) == 0) {
            int character = (c & 127) << 7 | c1;
            if (character > 127) {
               return character;
            }
         } else {
            int c2 = this.get(at + 2);
            int character = (c & 127) << 14 | (c1 & 127) << 7 | c2;
            if ((c2 & 128) == 0 && character > 16383 && character <= 1114111 && (character < 55296 || character > 57343)) {
               return character;
            }
         }

         throw new JSONException("Bad character at " + at);
      }
   }

   public static int characterSize(int character) throws JSONException {
      if (character >= 0 && character <= 1114111) {
         return character <= 127 ? 1 : (character <= 16383 ? 2 : 3);
      } else {
         throw new JSONException("Bad character " + character);
      }
   }

   public int copy(byte[] bytes, int at) {
      System.arraycopy(this.bytes, 0, bytes, at, this.length);
      return at + this.length;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Kim)) {
         return false;
      } else {
         Kim that = (Kim)obj;
         if (this == that) {
            return true;
         } else {
            return this.hashcode != that.hashcode ? false : Arrays.equals(this.bytes, that.bytes);
         }
      }
   }

   public int get(int at) throws JSONException {
      if (at >= 0 && at <= this.length) {
         return this.bytes[at] & 0xFF;
      } else {
         throw new JSONException("Bad character at " + at);
      }
   }

   @Override
   public int hashCode() {
      return this.hashcode;
   }

   @Override
   public String toString() throws JSONException {
      if (this.string == null) {
         int length = 0;
         char[] chars = new char[this.length];

         int c;
         for(int at = 0; at < this.length; at += characterSize(c)) {
            c = this.characterAt(at);
            if (c < 65536) {
               chars[length] = (char)c;
               ++length;
            } else {
               chars[length] = (char)(55296 | c - 65536 >>> 10);
               ++length;
               chars[length] = (char)(56320 | c & 1023);
               ++length;
            }
         }

         this.string = new String(chars, 0, length);
      }

      return this.string;
   }
}
