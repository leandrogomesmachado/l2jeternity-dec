package com.mchange.v2.log.jdk14logging;

import com.mchange.v2.log.MLevel;
import java.util.logging.Level;

final class Jdk14LoggingUtils {
   public static MLevel mlevelFromLevel(Level var0) {
      if (var0 == Level.ALL) {
         return MLevel.ALL;
      } else if (var0 == Level.CONFIG) {
         return MLevel.CONFIG;
      } else if (var0 == Level.FINE) {
         return MLevel.FINE;
      } else if (var0 == Level.FINER) {
         return MLevel.FINER;
      } else if (var0 == Level.FINEST) {
         return MLevel.FINEST;
      } else if (var0 == Level.INFO) {
         return MLevel.INFO;
      } else if (var0 == Level.OFF) {
         return MLevel.OFF;
      } else if (var0 == Level.SEVERE) {
         return MLevel.SEVERE;
      } else if (var0 == Level.WARNING) {
         return MLevel.WARNING;
      } else {
         throw new IllegalArgumentException("Unexpected Jdk14 logging level: " + var0);
      }
   }

   private Jdk14LoggingUtils() {
   }
}
