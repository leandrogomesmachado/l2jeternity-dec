package com.mchange.v1.db.sql.schemarep;

import com.mchange.v1.util.ListUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForeignKeyRepImpl implements ForeignKeyRep {
   List locColNames;
   String refTableName;
   List refColNames;

   public ForeignKeyRepImpl(List var1, String var2, List var3) {
      this.locColNames = Collections.unmodifiableList(new ArrayList(var1));
      this.refTableName = var2;
      this.refColNames = Collections.unmodifiableList(new ArrayList(var3));
   }

   @Override
   public List getLocalColumnNames() {
      return this.locColNames;
   }

   @Override
   public String getReferencedTableName() {
      return this.refTableName;
   }

   @Override
   public List getReferencedColumnNames() {
      return this.refColNames;
   }

   @Override
   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         ForeignKeyRepImpl var2 = (ForeignKeyRepImpl)var1;
         return ListUtils.equivalent(this.locColNames, var2.locColNames)
            && this.refTableName.equals(var2.refTableName)
            && ListUtils.equivalent(this.refColNames, var2.refColNames);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return ListUtils.hashContents(this.locColNames) ^ this.refTableName.hashCode() ^ ListUtils.hashContents(this.refColNames);
   }
}
