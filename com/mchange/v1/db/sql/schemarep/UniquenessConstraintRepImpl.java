package com.mchange.v1.db.sql.schemarep;

import com.mchange.v1.util.SetUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UniquenessConstraintRepImpl implements UniquenessConstraintRep {
   Set uniqueColNames;

   public UniquenessConstraintRepImpl(Collection var1) {
      this.uniqueColNames = Collections.unmodifiableSet(new HashSet(var1));
   }

   @Override
   public Set getUniqueColumnNames() {
      return this.uniqueColNames;
   }

   @Override
   public boolean equals(Object var1) {
      return var1 != null
         && this.getClass() == var1.getClass()
         && SetUtils.equivalentDisregardingSort(this.uniqueColNames, ((UniquenessConstraintRepImpl)var1).uniqueColNames);
   }

   @Override
   public int hashCode() {
      return this.getClass().hashCode() ^ SetUtils.hashContentsDisregardingSort(this.uniqueColNames);
   }
}
