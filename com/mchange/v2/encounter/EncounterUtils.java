package com.mchange.v2.encounter;

import com.mchange.v1.identicator.IdHashMap;
import com.mchange.v1.identicator.IdWeakHashMap;
import com.mchange.v1.identicator.Identicator;

public final class EncounterUtils {
   public static EncounterCounter createStrong(Identicator var0) {
      return new GenericEncounterCounter(new IdHashMap(var0));
   }

   public static EncounterCounter createWeak(Identicator var0) {
      return new GenericEncounterCounter(new IdWeakHashMap(var0));
   }

   public static EncounterCounter syncWrap(final EncounterCounter var0) {
      return new EncounterCounter() {
         @Override
         public synchronized long encounter(Object var1) {
            return var0.encounter(var1);
         }

         @Override
         public synchronized long reset(Object var1) {
            return var0.reset(var1);
         }

         @Override
         public synchronized void resetAll() {
            var0.resetAll();
         }
      };
   }

   private EncounterUtils() {
   }
}
