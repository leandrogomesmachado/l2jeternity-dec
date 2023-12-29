package org.eclipse.jdt.internal.compiler.impl;

public class BooleanConstant extends Constant {
   private boolean value;
   private static final BooleanConstant TRUE = new BooleanConstant(true);
   private static final BooleanConstant FALSE = new BooleanConstant(false);

   public static Constant fromValue(boolean value) {
      return value ? TRUE : FALSE;
   }

   private BooleanConstant(boolean value) {
      this.value = value;
   }

   @Override
   public boolean booleanValue() {
      return this.value;
   }

   @Override
   public String stringValue() {
      return String.valueOf(this.value);
   }

   @Override
   public String toString() {
      return "(boolean)" + this.value;
   }

   @Override
   public int typeID() {
      return 5;
   }

   @Override
   public int hashCode() {
      return this.value ? 1231 : 1237;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass() != obj.getClass() ? false : false;
      }
   }
}
