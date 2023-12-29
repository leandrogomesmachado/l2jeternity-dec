package l2e.commons.util.file.filter;

import java.io.File;
import java.io.FileFilter;

public class ExtFilter implements FileFilter {
   private final String _ext;

   public ExtFilter(String ext) {
      this._ext = ext;
   }

   @Override
   public boolean accept(File f) {
      return f.getName().toLowerCase().endsWith(this._ext);
   }
}
