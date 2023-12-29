package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.io.IOCase;

public class SuffixFileFilter extends AbstractFileFilter implements Serializable {
   private final String[] suffixes;
   private final IOCase caseSensitivity;

   public SuffixFileFilter(String suffix) {
      this(suffix, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(String suffix, IOCase caseSensitivity) {
      if (suffix == null) {
         throw new IllegalArgumentException("The suffix must not be null");
      } else {
         this.suffixes = new String[]{suffix};
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public SuffixFileFilter(String[] suffixes) {
      this(suffixes, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(String[] suffixes, IOCase caseSensitivity) {
      if (suffixes == null) {
         throw new IllegalArgumentException("The array of suffixes must not be null");
      } else {
         this.suffixes = suffixes;
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public SuffixFileFilter(List suffixes) {
      this(suffixes, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(List suffixes, IOCase caseSensitivity) {
      if (suffixes == null) {
         throw new IllegalArgumentException("The list of suffixes must not be null");
      } else {
         this.suffixes = suffixes.toArray(new String[suffixes.size()]);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public boolean accept(File file) {
      String name = file.getName();

      for(int i = 0; i < this.suffixes.length; ++i) {
         if (this.caseSensitivity.checkEndsWith(name, this.suffixes[i])) {
            return true;
         }
      }

      return false;
   }

   public boolean accept(File file, String name) {
      for(int i = 0; i < this.suffixes.length; ++i) {
         if (this.caseSensitivity.checkEndsWith(name, this.suffixes[i])) {
            return true;
         }
      }

      return false;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append(super.toString());
      buffer.append("(");
      if (this.suffixes != null) {
         for(int i = 0; i < this.suffixes.length; ++i) {
            if (i > 0) {
               buffer.append(",");
            }

            buffer.append(this.suffixes[i]);
         }
      }

      buffer.append(")");
      return buffer.toString();
   }
}
