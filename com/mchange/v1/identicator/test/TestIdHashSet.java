package com.mchange.v1.identicator.test;

import com.mchange.v1.identicator.IdHashSet;
import com.mchange.v1.identicator.Identicator;

public class TestIdHashSet {
   public static void main(String[] var0) {
      Identicator var1 = new Identicator() {
         @Override
         public boolean identical(Object var1, Object var2) {
            return ((String)var1).charAt(0) == ((String)var2).charAt(0);
         }

         @Override
         public int hash(Object var1) {
            return ((String)var1).charAt(0);
         }
      };
      IdHashSet var2 = new IdHashSet(var1);
      System.out.println(var2.add("hello"));
      System.out.println(var2.add("world"));
      System.out.println(var2.add("hi"));
      System.out.println(var2.size());
      Object[] var3 = var2.toArray();

      for(int var4 = 0; var4 < var3.length; ++var4) {
         System.out.println(var3[var4]);
      }
   }
}
