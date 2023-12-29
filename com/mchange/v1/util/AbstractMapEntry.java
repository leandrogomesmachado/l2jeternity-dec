package com.mchange.v1.util;

import com.mchange.v2.lang.ObjectUtils;
import java.util.Map.Entry;

public abstract class AbstractMapEntry implements Entry {
   @Override
   public abstract Object getKey();

   @Override
   public abstract Object getValue();

   @Override
   public abstract Object setValue(Object var1);

   @Override
   public boolean equals(Object var1) {
      if (!(var1 instanceof Entry)) {
         return false;
      } else {
         Entry var2 = (Entry)var1;
         return ObjectUtils.eqOrBothNull(this.getKey(), var2.getKey()) && ObjectUtils.eqOrBothNull(this.getValue(), var2.getValue());
      }
   }

   @Override
   public int hashCode() {
      return (this.getKey() == null ? 0 : this.getKey().hashCode()) ^ (this.getValue() == null ? 0 : this.getValue().hashCode());
   }
}
