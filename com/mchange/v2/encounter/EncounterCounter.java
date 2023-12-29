package com.mchange.v2.encounter;

public interface EncounterCounter {
   long encounter(Object var1);

   long reset(Object var1);

   void resetAll();
}
