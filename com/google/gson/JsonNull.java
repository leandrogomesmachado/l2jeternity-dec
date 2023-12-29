package com.google.gson;

public final class JsonNull extends JsonElement {
   public static final JsonNull INSTANCE = new JsonNull();

   public JsonNull deepCopy() {
      return INSTANCE;
   }

   @Override
   public int hashCode() {
      return JsonNull.class.hashCode();
   }

   @Override
   public boolean equals(Object other) {
      return this == other || other instanceof JsonNull;
   }
}
