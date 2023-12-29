package com.mchange.v2.collection;

import com.mchange.v2.lang.ObjectUtils;
import java.util.Map.Entry;

public class MapEntry implements Entry {
   Object key;
   Object value;

   public MapEntry(Object var1, Object var2) {
      this.key = var1;
      this.value = var2;
   }

   @Override
   public Object getKey() {
      return this.key;
   }

   @Override
   public Object getValue() {
      return this.value;
   }

   @Override
   public Object setValue(Object var1) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean equals(Object var1) {
      if (!(var1 instanceof Entry)) {
         return false;
      } else {
         Entry var2 = (Entry)var1;
         return ObjectUtils.eqOrBothNull(this.key, var2.getKey()) && ObjectUtils.eqOrBothNull(this.value, var2.getValue());
      }
   }

   @Override
   public int hashCode() {
      return ObjectUtils.hashOrZero(this.key) ^ ObjectUtils.hashOrZero(this.value);
   }
}
