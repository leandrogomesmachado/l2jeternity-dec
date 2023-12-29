package l2e.commons.util.file.filter;

import java.io.File;
import java.io.FileFilter;

public class HTMLFilter implements FileFilter {
   @Override
   public boolean accept(File f) {
      if (f != null && f.isFile()) {
         String name = f.getName().toLowerCase();
         return name.endsWith(".htm") || name.endsWith(".html");
      } else {
         return false;
      }
   }
}
