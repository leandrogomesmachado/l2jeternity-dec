package org.json.zip;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.Kim;

public class Zipper extends JSONzip {
   final BitWriter bitwriter;

   public Zipper(BitWriter bitwriter) {
      this.bitwriter = bitwriter;
   }

   private static int bcd(char digit) {
      if (digit >= '0' && digit <= '9') {
         return digit - 48;
      } else {
         switch(digit) {
            case '+':
               return 12;
            case ',':
            default:
               return 13;
            case '-':
               return 11;
            case '.':
               return 10;
         }
      }
   }

   public void flush() throws JSONException {
      this.pad(8);
   }

   private void one() throws JSONException {
      this.write(1, 1);
   }

   public void pad(int width) throws JSONException {
      try {
         this.bitwriter.pad(width);
      } catch (Throwable var3) {
         throw new JSONException(var3);
      }
   }

   private void write(int integer, int width) throws JSONException {
      try {
         this.bitwriter.write(integer, width);
      } catch (Throwable var4) {
         throw new JSONException(var4);
      }
   }

   private void write(int integer, Huff huff) throws JSONException {
      huff.write(integer, this.bitwriter);
   }

   private void write(Kim kim, Huff huff, Huff ext) throws JSONException {
      for(int at = 0; at < kim.length; ++at) {
         int c = kim.get(at);
         this.write(c, huff);

         while((c & 128) == 128) {
            c = kim.get(++at);
            this.write(c, ext);
         }
      }
   }

   private void write(int integer, Keep keep) throws JSONException {
      int width = keep.bitsize();
      keep.tick(integer);
      this.write(integer, width);
   }

   private void write(JSONArray jsonarray) throws JSONException {
      boolean stringy = false;
      int length = jsonarray.length();
      if (length == 0) {
         this.write(1, 3);
      } else {
         Object value = jsonarray.get(0);
         if (value == null) {
            value = JSONObject.NULL;
         }

         if (value instanceof String) {
            stringy = true;
            this.write(6, 3);
            this.writeString((String)value);
         } else {
            this.write(7, 3);
            this.writeValue(value);
         }

         for(int i = 1; i < length; ++i) {
            value = jsonarray.get(i);
            if (value == null) {
               value = JSONObject.NULL;
            }

            if (value instanceof String != stringy) {
               this.zero();
            }

            this.one();
            if (value instanceof String) {
               this.writeString((String)value);
            } else {
               this.writeValue(value);
            }
         }

         this.zero();
         this.zero();
      }
   }

   private void writeJSON(Object value) throws JSONException {
      if (JSONObject.NULL.equals(value)) {
         this.write(4, 3);
      } else if (Boolean.FALSE.equals(value)) {
         this.write(3, 3);
      } else if (Boolean.TRUE.equals(value)) {
         this.write(2, 3);
      } else {
         if (value instanceof Map) {
            value = new JSONObject((Map<String, Object>)value);
         } else if (value instanceof Collection) {
            value = new JSONArray((Collection<Object>)value);
         } else if (value.getClass().isArray()) {
            value = new JSONArray(value);
         }

         if (value instanceof JSONObject) {
            this.write((JSONObject)value);
         } else {
            if (!(value instanceof JSONArray)) {
               throw new JSONException("Unrecognized object");
            }

            this.write((JSONArray)value);
         }
      }
   }

   private void writeName(String name) throws JSONException {
      Kim kim = new Kim(name);
      int integer = this.namekeep.find(kim);
      if (integer != -1) {
         this.one();
         this.write(integer, this.namekeep);
      } else {
         this.zero();
         this.write(kim, this.namehuff, this.namehuffext);
         this.write(256, this.namehuff);
         this.namekeep.register(kim);
      }
   }

   private void write(JSONObject jsonobject) throws JSONException {
      boolean first = true;
      Iterator<String> keys = jsonobject.keys();

      while(keys.hasNext()) {
         Object key = keys.next();
         if (key instanceof String) {
            if (first) {
               first = false;
               this.write(5, 3);
            } else {
               this.one();
            }

            this.writeName((String)key);
            Object value = jsonobject.get((String)key);
            if (value instanceof String) {
               this.zero();
               this.writeString((String)value);
            } else {
               this.one();
               this.writeValue(value);
            }
         }
      }

      if (first) {
         this.write(0, 3);
      } else {
         this.zero();
      }
   }

   private void writeString(String string) throws JSONException {
      if (string.length() == 0) {
         this.zero();
         this.write(256, this.stringhuff);
      } else {
         Kim kim = new Kim(string);
         int integer = this.stringkeep.find(kim);
         if (integer != -1) {
            this.one();
            this.write(integer, this.stringkeep);
         } else {
            this.zero();
            this.write(kim, this.stringhuff, this.stringhuffext);
            this.write(256, this.stringhuff);
            this.stringkeep.register(kim);
         }
      }
   }

   private void writeValue(Object value) throws JSONException {
      if (value instanceof Number) {
         String string = JSONObject.numberToString((Number)value);
         int integer = this.valuekeep.find(string);
         if (integer != -1) {
            this.write(2, 2);
            this.write(integer, this.valuekeep);
            return;
         }

         if (value instanceof Integer || value instanceof Long) {
            long longer = ((Number)value).longValue();
            if (longer >= 0L && longer < 16528L) {
               this.write(0, 2);
               if (longer < 16L) {
                  this.zero();
                  this.write((int)longer, 4);
                  return;
               }

               this.one();
               if (longer < 144L) {
                  this.zero();
                  this.write((int)(longer - 16L), 7);
                  return;
               }

               this.one();
               this.write((int)(longer - 144L), 14);
               return;
            }
         }

         this.write(1, 2);

         for(int i = 0; i < string.length(); ++i) {
            this.write(bcd(string.charAt(i)), 4);
         }

         this.write(endOfNumber, 4);
         this.valuekeep.register(string);
      } else {
         this.write(3, 2);
         this.writeJSON(value);
      }
   }

   private void zero() throws JSONException {
      this.write(0, 1);
   }

   public void encode(JSONObject jsonobject) throws JSONException {
      this.generate();
      this.writeJSON(jsonobject);
   }

   public void encode(JSONArray jsonarray) throws JSONException {
      this.generate();
      this.writeJSON(jsonarray);
   }
}
