package org.eclipse.jdt.internal.compiler.impl;

public class StringConstant extends Constant {
   private String value;

   public static Constant fromValue(String value) {
      return new StringConstant(value);
   }

   private StringConstant(String value) {
      this.value = value;
   }

   @Override
   public String stringValue() {
      return this.value;
   }

   @Override
   public String toString() {
      return "(String)\"" + this.value + "\"";
   }

   @Override
   public int typeID() {
      return 11;
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + (this.value == null ? 0 : this.value.hashCode());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         StringConstant other = (StringConstant)obj;
         if (this.value == null) {
            return other.value == null;
         } else {
            return this.value.equals(other.value);
         }
      }
   }
}
