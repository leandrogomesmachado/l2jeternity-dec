package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class LastModifiedFileComparator implements Comparator, Serializable {
   public static final Comparator LASTMODIFIED_COMPARATOR = new LastModifiedFileComparator();
   public static final Comparator LASTMODIFIED_REVERSE = new ReverseComparator(LASTMODIFIED_COMPARATOR);

   public int compare(Object obj1, Object obj2) {
      File file1 = (File)obj1;
      File file2 = (File)obj2;
      long result = file1.lastModified() - file2.lastModified();
      if (result < 0L) {
         return -1;
      } else {
         return result > 0L ? 1 : 0;
      }
   }
}
