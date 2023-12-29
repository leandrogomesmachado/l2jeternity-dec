package com.mchange.v2.coalesce;

public interface CoalesceChecker {
   boolean checkCoalesce(Object var1, Object var2);

   int coalesceHash(Object var1);
}
