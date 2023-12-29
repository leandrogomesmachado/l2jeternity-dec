package com.mchange.v1.db.sql.schemarep;

import com.mchange.v1.util.ListUtils;
import com.mchange.v1.util.MapUtils;
import com.mchange.v1.util.SetUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableRepImpl implements TableRep {
   String tableName;
   List colNameList;
   Map namesToColReps;
   Set primaryKeyColNames;
   Set foreignKeyReps;
   Set uniqConstrReps;

   public TableRepImpl(String var1, List var2, Collection var3, Collection var4, Collection var5) {
      this.tableName = var1;
      ArrayList var6 = new ArrayList();
      HashMap var7 = new HashMap();
      int var8 = 0;

      for(int var9 = var2.size(); var8 < var9; ++var8) {
         ColumnRep var10 = (ColumnRep)var2.get(var8);
         String var11 = var10.getColumnName();
         var6.add(var11);
         var7.put(var11, var10);
      }

      this.colNameList = Collections.unmodifiableList(var6);
      this.namesToColReps = Collections.unmodifiableMap(var7);
      this.primaryKeyColNames = var3 == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(new HashSet(var3));
      this.foreignKeyReps = var4 == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(new HashSet(var4));
      this.uniqConstrReps = var5 == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(new HashSet(var5));
   }

   @Override
   public String getTableName() {
      return this.tableName;
   }

   @Override
   public Iterator getColumnNames() {
      return this.colNameList.iterator();
   }

   @Override
   public ColumnRep columnRepForName(String var1) {
      return (ColumnRep)this.namesToColReps.get(var1);
   }

   @Override
   public Set getPrimaryKeyColumnNames() {
      return this.primaryKeyColNames;
   }

   @Override
   public Set getForeignKeyReps() {
      return this.foreignKeyReps;
   }

   @Override
   public Set getUniquenessConstraintReps() {
      return this.uniqConstrReps;
   }

   @Override
   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         TableRepImpl var2 = (TableRepImpl)var1;
         return this.tableName.equals(var2.tableName)
            && ListUtils.equivalent(this.colNameList, var2.colNameList)
            && MapUtils.equivalentDisregardingSort(this.namesToColReps, var2.namesToColReps)
            && SetUtils.equivalentDisregardingSort(this.primaryKeyColNames, var2.primaryKeyColNames)
            && SetUtils.equivalentDisregardingSort(this.foreignKeyReps, var2.foreignKeyReps)
            && SetUtils.equivalentDisregardingSort(this.uniqConstrReps, var2.uniqConstrReps);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.tableName.hashCode()
         ^ ListUtils.hashContents(this.colNameList)
         ^ MapUtils.hashContentsDisregardingSort(this.namesToColReps)
         ^ SetUtils.hashContentsDisregardingSort(this.primaryKeyColNames)
         ^ SetUtils.hashContentsDisregardingSort(this.foreignKeyReps)
         ^ SetUtils.hashContentsDisregardingSort(this.uniqConstrReps);
   }
}
