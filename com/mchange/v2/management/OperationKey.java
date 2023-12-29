package com.mchange.v2.management;

import java.util.Arrays;

public final class OperationKey {
   String name;
   String[] signature;

   public OperationKey(String var1, String[] var2) {
      this.name = var1;
      this.signature = var2;
   }

   @Override
   public boolean equals(Object var1) {
      if (!(var1 instanceof OperationKey)) {
         return false;
      } else {
         OperationKey var2 = (OperationKey)var1;
         return this.name.equals(var2.name) && Arrays.equals((Object[])this.signature, (Object[])var2.signature);
      }
   }

   @Override
   public int hashCode() {
      return this.name.hashCode() ^ Arrays.hashCode((Object[])this.signature);
   }
}
