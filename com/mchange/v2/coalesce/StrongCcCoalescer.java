package com.mchange.v2.coalesce;

import com.mchange.v1.identicator.IdHashMap;

final class StrongCcCoalescer extends AbstractStrongCoalescer implements Coalescer {
   StrongCcCoalescer(CoalesceChecker var1) {
      super(new IdHashMap(new CoalesceIdenticator(var1)));
   }
}
