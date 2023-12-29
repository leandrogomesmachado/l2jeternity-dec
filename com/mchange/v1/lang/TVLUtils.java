package com.mchange.v1.lang;

public final class TVLUtils {
   public static final boolean isDefinitelyTrue(Boolean var0) {
      return var0 != null && var0;
   }

   public static final boolean isDefinitelyFalse(Boolean var0) {
      return var0 != null && !var0;
   }

   public static final boolean isPossiblyTrue(Boolean var0) {
      return var0 == null || var0;
   }

   public static final boolean isPossiblyFalse(Boolean var0) {
      return var0 == null || !var0;
   }

   public static final boolean isUnknown(Boolean var0) {
      return var0 == null;
   }
}
