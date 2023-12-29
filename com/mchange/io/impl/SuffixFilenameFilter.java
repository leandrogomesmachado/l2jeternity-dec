package com.mchange.io.impl;

import java.io.File;
import java.io.FilenameFilter;

public class SuffixFilenameFilter implements FilenameFilter {
   public static final int ALWAYS = 0;
   public static final int NEVER = 1;
   public static final int MATCH = 2;
   String[] suffixes = null;
   int accept_dirs;

   public SuffixFilenameFilter(String[] var1, int var2) {
      this.suffixes = var1;
      this.accept_dirs = var2;
   }

   public SuffixFilenameFilter(String var1, int var2) {
      this.suffixes = new String[]{var1};
      this.accept_dirs = var2;
   }

   @Override
   public boolean accept(File var1, String var2) {
      if (this.accept_dirs != 2 && new File(var1, var2).isDirectory()) {
         return this.accept_dirs == 0;
      } else {
         int var3 = this.suffixes.length;

         while(--var3 >= 0) {
            if (var2.endsWith(this.suffixes[var3])) {
               return true;
            }
         }

         return false;
      }
   }
}
