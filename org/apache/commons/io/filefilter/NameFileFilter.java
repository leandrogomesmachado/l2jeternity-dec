package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.io.IOCase;

public class NameFileFilter extends AbstractFileFilter implements Serializable {
   private final String[] names;
   private final IOCase caseSensitivity;

   public NameFileFilter(String name) {
      this(name, null);
   }

   public NameFileFilter(String name, IOCase caseSensitivity) {
      if (name == null) {
         throw new IllegalArgumentException("The wildcard must not be null");
      } else {
         this.names = new String[]{name};
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public NameFileFilter(String[] names) {
      this(names, null);
   }

   public NameFileFilter(String[] names, IOCase caseSensitivity) {
      if (names == null) {
         throw new IllegalArgumentException("The array of names must not be null");
      } else {
         this.names = names;
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public NameFileFilter(List names) {
      this(names, null);
   }

   public NameFileFilter(List names, IOCase caseSensitivity) {
      if (names == null) {
         throw new IllegalArgumentException("The list of names must not be null");
      } else {
         this.names = names.toArray(new String[names.size()]);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public boolean accept(File file) {
      String name = file.getName();

      for(int i = 0; i < this.names.length; ++i) {
         if (this.caseSensitivity.checkEquals(name, this.names[i])) {
            return true;
         }
      }

      return false;
   }

   public boolean accept(File file, String name) {
      for(int i = 0; i < this.names.length; ++i) {
         if (this.caseSensitivity.checkEquals(name, this.names[i])) {
            return true;
         }
      }

      return false;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append(super.toString());
      buffer.append("(");
      if (this.names != null) {
         for(int i = 0; i < this.names.length; ++i) {
            if (i > 0) {
               buffer.append(",");
            }

            buffer.append(this.names[i]);
         }
      }

      buffer.append(")");
      return buffer.toString();
   }
}
