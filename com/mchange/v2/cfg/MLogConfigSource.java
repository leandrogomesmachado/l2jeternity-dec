package com.mchange.v2.cfg;

import java.util.List;

public final class MLogConfigSource {
   public static MultiPropertiesConfig readVmConfig(String[] var0, String[] var1, List var2) {
      return ConfigUtils.readVmConfig(var0, var1, var2);
   }

   private MLogConfigSource() {
   }
}
