package org.mozilla.universalchardet.prober.statemachine;

public abstract class SMModel {
   public static final int START = 0;
   public static final int ERROR = 1;
   public static final int ITSME = 2;
   protected PkgInt classTable;
   protected int classFactor;
   protected PkgInt stateTable;
   protected int[] charLenTable;
   protected String name;

   public SMModel(PkgInt var1, int var2, PkgInt var3, int[] var4, String var5) {
      this.classTable = var1;
      this.classFactor = var2;
      this.stateTable = var3;
      this.charLenTable = var4;
      this.name = var5;
   }

   public int getClass(byte var1) {
      int var2 = var1 & 255;
      return this.classTable.unpack(var2);
   }

   public int getNextState(int var1, int var2) {
      return this.stateTable.unpack(var2 * this.classFactor + var1);
   }

   public int getCharLen(int var1) {
      return this.charLenTable[var1];
   }

   public String getName() {
      return this.name;
   }
}
