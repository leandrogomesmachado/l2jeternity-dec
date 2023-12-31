package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.util.ArrayList;

public class FileFinder {
   public static String[] find(File f, String pattern) {
      ArrayList files = new ArrayList();
      find0(f, pattern, files);
      String[] result = new String[files.size()];
      files.toArray(result);
      return result;
   }

   private static void find0(File f, String pattern, ArrayList collector) {
      if (f.isDirectory()) {
         String[] files = f.list();
         if (files == null) {
            return;
         }

         int i = 0;

         for(int max = files.length; i < max; ++i) {
            File current = new File(f, files[i]);
            if (current.isDirectory()) {
               find0(current, pattern, collector);
            } else if (current.getName().toUpperCase().endsWith(pattern)) {
               collector.add(current.getAbsolutePath());
            }
         }
      }
   }
}
