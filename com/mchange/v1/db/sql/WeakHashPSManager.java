package com.mchange.v1.db.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class WeakHashPSManager implements PSManager {
   WeakHashMap wmap = new WeakHashMap();

   @Override
   public PreparedStatement getPS(Connection var1, String var2) {
      Map var3 = (Map)this.wmap.get(var1);
      return var3 == null ? null : (PreparedStatement)var3.get(var2);
   }

   @Override
   public void putPS(Connection var1, String var2, PreparedStatement var3) {
      Object var4 = (Map)this.wmap.get(var1);
      if (var4 == null) {
         var4 = new HashMap();
         this.wmap.put(var1, var4);
      }

      var4.put(var2, var3);
   }
}
