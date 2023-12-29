package com.mchange.v2.coalesce;

import java.util.Iterator;

public interface Coalescer {
   Object coalesce(Object var1);

   int countCoalesced();

   Iterator iterator();
}
