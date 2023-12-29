package org.eclipse.jdt.internal.compiler.tool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Archive {
   public static final Archive UNKNOWN_ARCHIVE = new Archive();
   ZipFile zipFile;
   File file;
   protected Hashtable<String, ArrayList<String>> packagesCache;

   private Archive() {
   }

   public Archive(File file) throws ZipException, IOException {
      this.file = file;
      this.zipFile = new ZipFile(file);
      this.initialize();
   }

   private void initialize() {
      this.packagesCache = new Hashtable<>();
      Enumeration<? extends ZipEntry> e = this.zipFile.entries();

      while(e.hasMoreElements()) {
         String fileName = e.nextElement().getName();
         int last = fileName.lastIndexOf(47);
         String packageName = fileName.substring(0, last + 1);
         String typeName = fileName.substring(last + 1);
         ArrayList<String> types = this.packagesCache.get(packageName);
         if (types == null) {
            if (typeName.length() != 0) {
               types = new ArrayList<>();
               types.add(typeName);
               this.packagesCache.put(packageName, types);
            }
         } else {
            types.add(typeName);
         }
      }
   }

   public ArchiveFileObject getArchiveFileObject(String entryName, Charset charset) {
      return new ArchiveFileObject(this.file, entryName, charset);
   }

   public boolean contains(String entryName) {
      return this.zipFile.getEntry(entryName) != null;
   }

   public Set<String> allPackages() {
      if (this.packagesCache == null) {
         this.initialize();
      }

      return this.packagesCache.keySet();
   }

   public List<String> getTypes(String packageName) {
      if (this.packagesCache == null) {
         try {
            this.zipFile = new ZipFile(this.file);
         } catch (IOException var2) {
            return Collections.emptyList();
         }

         this.initialize();
      }

      return this.packagesCache.get(packageName);
   }

   public void flush() {
      this.packagesCache = null;
   }

   public void close() {
      try {
         if (this.zipFile != null) {
            this.zipFile.close();
         }

         this.packagesCache = null;
      } catch (IOException var1) {
      }
   }

   @Override
   public String toString() {
      return "Archive: " + (this.file == null ? "UNKNOWN_ARCHIVE" : this.file.getAbsolutePath());
   }
}
