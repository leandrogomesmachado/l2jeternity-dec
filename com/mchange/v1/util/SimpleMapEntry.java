package com.mchange.v1.util;

import java.util.Map.Entry;

public class SimpleMapEntry extends AbstractMapEntry implements Entry {
   Object key;
   Object value;

   public SimpleMapEntry(Object var1, Object var2) {
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
      this.value = var1;
      return var1;
   }
}
