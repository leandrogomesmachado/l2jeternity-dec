package com.mchange.v2.encounter;

import java.util.WeakHashMap;

/** @deprecated */
public class EqualityEncounterCounter extends AbstractEncounterCounter {
   public EqualityEncounterCounter() {
      super(new WeakHashMap());
   }
}
