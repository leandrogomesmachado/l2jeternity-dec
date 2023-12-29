package com.mchange.v1.db.sql.schemarep;

import com.mchange.v1.identicator.Identicator;
import java.util.Arrays;

public class TypeRepIdenticator implements Identicator {
   private static final TypeRepIdenticator INSTANCE = new TypeRepIdenticator();

   public static TypeRepIdenticator getInstance() {
      return INSTANCE;
   }

   private TypeRepIdenticator() {
   }

   @Override
   public boolean identical(Object var1, Object var2) {
      if (var1 == var2) {
         return true;
      } else {
         TypeRep var3 = (TypeRep)var1;
         TypeRep var4 = (TypeRep)var2;
         return var3.getTypeCode() == var4.getTypeCode() && Arrays.equals(var3.getTypeSize(), var4.getTypeSize());
      }
   }

   @Override
   public int hash(Object var1) {
      TypeRep var2 = (TypeRep)var1;
      int var3 = var2.getTypeCode();
      int[] var4 = var2.getTypeSize();
      if (var4 != null) {
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            var3 ^= var4[var6];
         }

         var3 ^= var5;
      }

      return var3;
   }
}
