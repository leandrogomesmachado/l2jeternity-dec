package com.mchange.v1.db.sql.schemarep;

public final class TypeRepImpl implements TypeRep {
   int type_code;
   int[] typeSize;

   public TypeRepImpl(int var1, int[] var2) {
      this.type_code = var1;
      this.typeSize = var2;
   }

   @Override
   public int getTypeCode() {
      return this.type_code;
   }

   @Override
   public int[] getTypeSize() {
      return this.typeSize;
   }

   @Override
   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         return var1 instanceof TypeRep ? TypeRepIdenticator.getInstance().identical(this, var1) : false;
      }
   }

   @Override
   public int hashCode() {
      return TypeRepIdenticator.getInstance().hash(this);
   }
}
