package com.mysql.cj.xdevapi;

import java.util.HashMap;

public class JsonString implements JsonValue {
   static HashMap<Character, String> escapeChars = new HashMap<>();
   private String val = "";

   public String getString() {
      return this.val;
   }

   public JsonString setValue(String value) {
      this.val = value;
      return this;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("\"");

      for(int i = 0; i < this.val.length(); ++i) {
         if (escapeChars.containsKey(this.val.charAt(i))) {
            sb.append(escapeChars.get(this.val.charAt(i)));
         } else {
            sb.append(this.val.charAt(i));
         }
      }

      sb.append("\"");
      return sb.toString();
   }

   static {
      for(JsonParser.EscapeChar ec : JsonParser.EscapeChar.values()) {
         escapeChars.put(ec.CHAR, ec.ESCAPED);
      }
   }
}
