package com.mchange.v2.coalesce;

import com.mchange.v1.identicator.IdWeakHashMap;

final class WeakCcCoalescer extends AbstractWeakCoalescer implements Coalescer {
   WeakCcCoalescer(CoalesceChecker var1) {
      super(new IdWeakHashMap(new CoalesceIdenticator(var1)));
   }
}
