package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.io.IOCase;

public class PrefixFileFilter extends AbstractFileFilter implements Serializable {
   private final String[] prefixes;
   private final IOCase caseSensitivity;

   public PrefixFileFilter(String prefix) {
      this(prefix, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(String prefix, IOCase caseSensitivity) {
      if (prefix == null) {
         throw new IllegalArgumentException("The prefix must not be null");
      } else {
         this.prefixes = new String[]{prefix};
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public PrefixFileFilter(String[] prefixes) {
      this(prefixes, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(String[] prefixes, IOCase caseSensitivity) {
      if (prefixes == null) {
         throw new IllegalArgumentException("The array of prefixes must not be null");
      } else {
         this.prefixes = prefixes;
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public PrefixFileFilter(List prefixes) {
      this(prefixes, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(List prefixes, IOCase caseSensitivity) {
      if (prefixes == null) {
         throw new IllegalArgumentException("The list of prefixes must not be null");
      } else {
         this.prefixes = prefixes.toArray(new String[prefixes.size()]);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public boolean accept(File file) {
      String name = file.getName();

      for(int i = 0; i < this.prefixes.length; ++i) {
         if (this.caseSensitivity.checkStartsWith(name, this.prefixes[i])) {
            return true;
         }
      }

      return false;
   }

   public boolean accept(File file, String name) {
      for(int i = 0; i < this.prefixes.length; ++i) {
         if (this.caseSensitivity.checkStartsWith(name, this.prefixes[i])) {
            return true;
         }
      }

      return false;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append(super.toString());
      buffer.append("(");
      if (this.prefixes != null) {
         for(int i = 0; i < this.prefixes.length; ++i) {
            if (i > 0) {
               buffer.append(",");
            }

            buffer.append(this.prefixes[i]);
         }
      }

      buffer.append(")");
      return buffer.toString();
   }
}
