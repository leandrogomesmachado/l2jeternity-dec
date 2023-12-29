package com.mchange.v1.identicator.test;

import com.mchange.v1.identicator.IdWeakHashMap;
import com.mchange.v1.identicator.Identicator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TestIdWeakHashMap {
   static final Identicator id = new Identicator() {
      @Override
      public boolean identical(Object var1, Object var2) {
         return ((String)var1).charAt(0) == ((String)var2).charAt(0);
      }

      @Override
      public int hash(Object var1) {
         return ((String)var1).charAt(0);
      }
   };
   static final Map weak = new IdWeakHashMap(id);

   public static void main(String[] var0) {
      doAdds();
      System.gc();
      show();
      setRemoveHi();
      System.gc();
      show();
   }

   static void setRemoveHi() {
      String var0 = new String("bye");
      weak.put(var0, "");
      Set var1 = weak.keySet();
      var1.remove("hi");
      show();
   }

   static void doAdds() {
      String var0 = "hi";
      String var1 = new String("hello");
      String var2 = new String("yoohoo");
      String var3 = new String("poop");
      weak.put(var0, "");
      weak.put(var1, "");
      weak.put(var2, "");
      weak.put(var3, "");
      show();
   }

   static void show() {
      System.out.println("elements:");
      Iterator var0 = weak.keySet().iterator();

      while(var0.hasNext()) {
         System.out.println("\t" + var0.next());
      }

      System.out.println("size: " + weak.size());
   }
}
