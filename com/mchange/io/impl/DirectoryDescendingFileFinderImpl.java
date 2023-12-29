package com.mchange.io.impl;

import com.mchange.io.FileEnumeration;
import com.mchange.io.IOEnumeration;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Stack;

/** @deprecated */
public class DirectoryDescendingFileFinderImpl implements IOEnumeration, FileEnumeration {
   private static final Object dummy = new Object();
   Hashtable markedDirex = new Hashtable();
   Stack direx = new Stack();
   Stack files = new Stack();
   FilenameFilter filter;
   boolean canonical;

   public DirectoryDescendingFileFinderImpl(File var1, FilenameFilter var2, boolean var3) throws IOException {
      if (!var1.isDirectory()) {
         throw new IllegalArgumentException(var1.getName() + " is not a directory.");
      } else {
         this.filter = var2;
         this.canonical = var3;
         this.blossomDirectory(var1);

         while(this.files.empty() && !this.direx.empty()) {
            this.blossomDirectory((File)this.direx.pop());
         }
      }
   }

   public DirectoryDescendingFileFinderImpl(File var1) throws IOException {
      this(var1, null, false);
   }

   @Override
   public boolean hasMoreFiles() {
      return !this.files.empty();
   }

   @Override
   public File nextFile() throws IOException {
      if (this.files.empty()) {
         throw new NoSuchElementException();
      } else {
         File var1 = (File)this.files.pop();

         while(this.files.empty() && !this.direx.empty()) {
            this.blossomDirectory((File)this.direx.pop());
         }

         return var1;
      }
   }

   @Override
   public boolean hasMoreElements() {
      return this.hasMoreFiles();
   }

   @Override
   public Object nextElement() throws IOException {
      return this.nextFile();
   }

   private void blossomDirectory(File var1) throws IOException {
      String var2 = var1.getCanonicalPath();
      String[] var3 = this.filter == null ? var1.list() : var1.list(this.filter);
      int var4 = var3.length;

      while(--var4 >= 0) {
         if (this.filter == null || this.filter.accept(var1, var3[var4])) {
            String var5 = (this.canonical ? var2 : var1.getPath()) + File.separator + var3[var4];
            File var6 = new File(var5);
            if (var6.isFile()) {
               this.files.push(var6);
            } else if (!this.markedDirex.containsKey(var6.getCanonicalPath())) {
               this.direx.push(var6);
            }
         }
      }

      this.markedDirex.put(var2, dummy);
   }

   public static void main(String[] var0) {
      try {
         File var1 = new File(var0[0]);
         DirectoryDescendingFileFinderImpl var2 = new DirectoryDescendingFileFinderImpl(var1);

         while(var2.hasMoreFiles()) {
            System.out.println(var2.nextFile().getAbsolutePath());
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }
}
