package com.mchange.v2.codegen.bean;

class PropertyComparator {
   public int compare(Object var1, Object var2) {
      Property var3 = (Property)var1;
      Property var4 = (Property)var2;
      return var3.getName().compareTo(var4.getName());
   }
}
