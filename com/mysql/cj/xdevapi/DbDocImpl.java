package com.mysql.cj.xdevapi;

import java.util.TreeMap;

public class DbDocImpl extends TreeMap<String, JsonValue> implements DbDoc {
   private static final long serialVersionUID = 6557406141541247905L;

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("{");

      for(String key : this.keySet()) {
         if (sb.length() > 1) {
            sb.append(",");
         }

         sb.append("\"").append(key).append("\":").append(this.get(key).toString());
      }

      sb.append("}");
      return sb.toString();
   }

   @Override
   public String toFormattedString() {
      StringBuilder sb = new StringBuilder("{");

      for(String key : this.keySet()) {
         if (sb.length() > 1) {
            sb.append(",");
         }

         sb.append("\n\"").append(key).append("\" : ").append(this.get(key).toFormattedString());
      }

      if (this.size() > 0) {
         sb.append("\n");
      }

      sb.append("}");
      return sb.toString();
   }

   @Override
   public DbDoc add(String key, JsonValue val) {
      this.put(key, val);
      return this;
   }
}
