package l2e.commons.util.file.filter;

import java.io.File;
import java.io.FileFilter;

public class BMPFilter implements FileFilter {
   @Override
   public boolean accept(File f) {
      return f != null && f.isFile() ? f.getName().toLowerCase().endsWith(".bmp") : false;
   }
}
