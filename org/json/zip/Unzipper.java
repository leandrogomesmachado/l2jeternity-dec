package org.json.zip;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.Kim;

public class Unzipper extends JSONzip {
   BitReader bitreader;

   public Unzipper(BitReader bitreader) {
      this.bitreader = bitreader;
   }

   private boolean bit() throws JSONException {
      try {
         return this.bitreader.bit();
      } catch (Throwable var3) {
         throw new JSONException(var3);
      }
   }

   private Object getAndTick(Keep keep, BitReader bitreader) throws JSONException {
      try {
         int width = keep.bitsize();
         int integer = bitreader.read(width);
         Object value = keep.value(integer);
         if (integer >= keep.length) {
            throw new JSONException("Deep error.");
         } else {
            keep.tick(integer);
            return value;
         }
      } catch (Throwable var6) {
         throw new JSONException(var6);
      }
   }

   public boolean pad(int width) throws JSONException {
      try {
         return this.bitreader.pad(width);
      } catch (Throwable var3) {
         throw new JSONException(var3);
      }
   }

   private int read(int width) throws JSONException {
      try {
         return this.bitreader.read(width);
      } catch (Throwable var3) {
         throw new JSONException(var3);
      }
   }

   private String read(Huff huff, Huff ext, Keep keep) throws JSONException {
      int at = 0;
      int allocation = 256;
      byte[] bytes = new byte[allocation];
      if (this.bit()) {
         return this.getAndTick(keep, this.bitreader).toString();
      } else {
         while(true) {
            if (at >= allocation) {
               allocation *= 2;
               bytes = Arrays.copyOf(bytes, allocation);
            }

            int c = huff.read(this.bitreader);
            if (c == 256) {
               if (at == 0) {
                  return "";
               }

               Kim kim = new Kim(bytes, at);
               keep.register(kim);
               return kim.toString();
            }

            while((c & 128) == 128) {
               bytes[at] = (byte)c;
               ++at;
               c = ext.read(this.bitreader);
            }

            bytes[at] = (byte)c;
            ++at;
         }
      }
   }

   private JSONArray readArray(boolean stringy) throws JSONException {
      JSONArray jsonarray = new JSONArray();
      jsonarray.put(stringy ? this.read(this.stringhuff, this.stringhuffext, this.stringkeep) : this.readValue());

      while(true) {
         while(this.bit()) {
            jsonarray.put(stringy ? this.read(this.stringhuff, this.stringhuffext, this.stringkeep) : this.readValue());
         }

         if (!this.bit()) {
            return jsonarray;
         }

         jsonarray.put(stringy ? this.readValue() : this.read(this.stringhuff, this.stringhuffext, this.stringkeep));
      }
   }

   private Object readJSON() throws JSONException {
      switch(this.read(3)) {
         case 0:
            return new JSONObject();
         case 1:
            return new JSONArray();
         case 2:
            return Boolean.TRUE;
         case 3:
            return Boolean.FALSE;
         case 4:
         default:
            return JSONObject.NULL;
         case 5:
            return this.readObject();
         case 6:
            return this.readArray(true);
         case 7:
            return this.readArray(false);
      }
   }

   private JSONObject readObject() throws JSONException {
      JSONObject jsonobject = new JSONObject();

      do {
         String name = this.read(this.namehuff, this.namehuffext, this.namekeep);
         if (jsonobject.opt(name) != null) {
            throw new JSONException("Duplicate key.");
         }

         jsonobject.put(name, !this.bit() ? this.read(this.stringhuff, this.stringhuffext, this.stringkeep) : this.readValue());
      } while(this.bit());

      return jsonobject;
   }

   private Object readValue() throws JSONException {
      switch(this.read(2)) {
         case 0:
            int nr_bits = !this.bit() ? 4 : (!this.bit() ? 7 : 14);
            int integer = this.read(nr_bits);
            switch(nr_bits) {
               case 7:
                  integer = (int)((long)integer + 16L);
                  break;
               case 14:
                  integer = (int)((long)integer + 144L);
            }

            return integer;
         case 1:
            byte[] bytes = new byte[256];
            int length = 0;

            while(true) {
               int c = this.read(4);
               if (c == endOfNumber) {
                  try {
                     value = JSONObject.stringToValue(new String(bytes, 0, length, "US-ASCII"));
                  } catch (UnsupportedEncodingException var7) {
                     throw new JSONException(var7);
                  }

                  this.valuekeep.register(value);
                  return value;
               }

               bytes[length] = bcd[c];
               ++length;
            }
         case 2:
            return this.getAndTick(this.valuekeep, this.bitreader);
         case 3:
            return this.readJSON();
         default:
            throw new JSONException("Impossible.");
      }
   }

   public Object decode() throws JSONException {
      this.generate();
      return this.readJSON();
   }
}
