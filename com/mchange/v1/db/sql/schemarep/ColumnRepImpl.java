package com.mchange.v1.db.sql.schemarep;

import com.mchange.lang.ArrayUtils;
import java.util.Arrays;

public class ColumnRepImpl implements ColumnRep {
   String colName;
   int col_type;
   int[] colSize;
   boolean accepts_nulls;
   Object defaultValue;

   public ColumnRepImpl(String var1, int var2) {
      this(var1, var2, null);
   }

   public ColumnRepImpl(String var1, int var2, int[] var3) {
      this(var1, var2, var3, false, null);
   }

   public ColumnRepImpl(String var1, int var2, int[] var3, boolean var4, Object var5) {
      this.colName = var1;
      this.col_type = var2;
      this.colSize = var3;
      this.accepts_nulls = var4;
      this.defaultValue = var5;
   }

   @Override
   public String getColumnName() {
      return this.colName;
   }

   @Override
   public int getColumnType() {
      return this.col_type;
   }

   @Override
   public int[] getColumnSize() {
      return this.colSize;
   }

   @Override
   public boolean acceptsNulls() {
      return this.accepts_nulls;
   }

   @Override
   public Object getDefaultValue() {
      return this.defaultValue;
   }

   @Override
   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         ColumnRepImpl var2 = (ColumnRepImpl)var1;
         if (!this.colName.equals(var2.colName) || this.col_type != var2.col_type || this.accepts_nulls != var2.accepts_nulls) {
            return false;
         } else if (this.colSize != var2.colSize && !Arrays.equals(this.colSize, var2.colSize)) {
            return false;
         } else {
            return this.defaultValue == var2.defaultValue || this.defaultValue == null || this.defaultValue.equals(var2.defaultValue);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int var1 = this.colName.hashCode() ^ this.col_type;
      if (!this.accepts_nulls) {
         var1 = ~var1;
      }

      if (this.colSize != null) {
         var1 ^= ArrayUtils.hashAll(this.colSize);
      }

      if (this.defaultValue != null) {
         var1 ^= this.defaultValue.hashCode();
      }

      return var1;
   }
}
