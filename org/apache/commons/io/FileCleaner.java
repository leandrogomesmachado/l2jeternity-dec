package org.apache.commons.io;

import java.io.File;

/** @deprecated */
public class FileCleaner {
   static final FileCleaningTracker theInstance = new FileCleaningTracker();

   /** @deprecated */
   public static void track(File file, Object marker) {
      theInstance.track(file, marker);
   }

   /** @deprecated */
   public static void track(File file, Object marker, FileDeleteStrategy deleteStrategy) {
      theInstance.track(file, marker, deleteStrategy);
   }

   /** @deprecated */
   public static void track(String path, Object marker) {
      theInstance.track(path, marker);
   }

   /** @deprecated */
   public static void track(String path, Object marker, FileDeleteStrategy deleteStrategy) {
      theInstance.track(path, marker, deleteStrategy);
   }

   /** @deprecated */
   public static int getTrackCount() {
      return theInstance.getTrackCount();
   }

   /** @deprecated */
   public static synchronized void exitWhenFinished() {
      theInstance.exitWhenFinished();
   }

   public static FileCleaningTracker getInstance() {
      return theInstance;
   }
}
