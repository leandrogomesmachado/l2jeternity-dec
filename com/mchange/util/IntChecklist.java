package com.mchange.util;

public interface IntChecklist {
   void check(int var1);

   void uncheck(int var1);

   boolean isChecked(int var1);

   void clear();

   int countChecked();

   int[] getChecked();

   IntEnumeration checked();
}
