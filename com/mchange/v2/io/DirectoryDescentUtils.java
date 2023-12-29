package com.mchange.v2.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public final class DirectoryDescentUtils {
   public static FileIterator depthFirstEagerDescent(File var0) throws IOException {
      return depthFirstEagerDescent(var0, null, false);
   }

   public static FileIterator depthFirstEagerDescent(File var0, FileFilter var1, boolean var2) throws IOException {
      LinkedList var3 = new LinkedList();
      HashSet var4 = new HashSet();
      depthFirstEagerDescend(var0, var1, var2, var3, var4);
      return new DirectoryDescentUtils.IteratorFileIterator(var3.iterator());
   }

   public static void addSubtree(File var0, FileFilter var1, boolean var2, Collection var3) throws IOException {
      HashSet var4 = new HashSet();
      depthFirstEagerDescend(var0, var1, var2, var3, var4);
   }

   private static void depthFirstEagerDescend(File var0, FileFilter var1, boolean var2, Collection var3, Set var4) throws IOException {
      String var5 = var0.getCanonicalPath();
      if (!var4.contains(var5)) {
         if (var1 == null || var1.accept(var0)) {
            var3.add(var2 ? new File(var5) : var0);
         }

         var4.add(var5);
         String[] var6 = var0.list();
         int var7 = 0;

         for(int var8 = var6.length; var7 < var8; ++var7) {
            File var9 = new File(var0, var6[var7]);
            if (var9.isDirectory()) {
               depthFirstEagerDescend(var9, var1, var2, var3, var4);
            } else if (var1 == null || var1.accept(var9)) {
               var3.add(var2 ? var9.getCanonicalFile() : var9);
            }
         }
      }
   }

   private DirectoryDescentUtils() {
   }

   public static void main(String[] var0) {
      try {
         FileIterator var1 = depthFirstEagerDescent(new File(var0[0]));

         while(var1.hasNext()) {
            System.err.println(var1.nextFile().getPath());
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   private static class IteratorFileIterator implements FileIterator {
      Iterator ii;
      Object last;

      IteratorFileIterator(Iterator var1) {
         this.ii = var1;
      }

      @Override
      public File nextFile() throws IOException {
         return (File)this.next();
      }

      @Override
      public boolean hasNext() throws IOException {
         return this.ii.hasNext();
      }

      @Override
      public Object next() throws IOException {
         return this.last = this.ii.next();
      }

      @Override
      public void remove() throws IOException {
         if (this.last != null) {
            ((File)this.last).delete();
            this.last = null;
         } else {
            throw new IllegalStateException();
         }
      }

      @Override
      public void close() throws IOException {
      }
   }
}
