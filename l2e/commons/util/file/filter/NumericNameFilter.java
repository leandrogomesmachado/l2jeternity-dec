package l2e.commons.util.file.filter;

import java.io.File;

public class NumericNameFilter extends XMLFilter {
   @Override
   public boolean accept(File f) {
      return super.accept(f) && f.getName().matches("\\d+\\.xml");
   }
}
