package com.mchange.v3.filecache;

import com.mchange.v1.io.InputStreamUtils;
import com.mchange.v1.io.OutputStreamUtils;
import com.mchange.v2.io.DirectoryDescentUtils;
import com.mchange.v2.io.FileIterator;
import com.mchange.v2.log.MLevel;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class FileCache {
   static final MLogger logger = MLog.getLogger(FileCache.class);
   final File cacheDir;
   final int buffer_size;
   final boolean read_only;
   final List<URLFetcher> fetchers;
   static final FileFilter NOT_DIR_FF = new FileFilter() {
      @Override
      public boolean accept(File var1) {
         return !var1.isDirectory();
      }
   };

   private InputStream fetchURL(URL var1) throws IOException {
      LinkedList var2 = null;

      for(URLFetcher var4 : this.fetchers) {
         try {
            return var4.openStream(var1, logger);
         } catch (FileNotFoundException var6) {
            throw var6;
         } catch (IOException var7) {
            if (logger.isLoggable(MLevel.FINE)) {
               logger.log(MLevel.FINE, "URLFetcher " + var4 + " failed on Exception. Will try next fetcher, if any.", (Throwable)var7);
            }

            if (var2 == null) {
               var2 = new LinkedList();
            }

            var2.add(var7);
         }
      }

      if (logger.isLoggable(MLevel.WARNING)) {
         logger.log(MLevel.WARNING, "All URLFetchers failed on URL " + var1);
         int var8 = 0;

         for(int var9 = var2.size(); var8 < var9; ++var8) {
            logger.log(MLevel.WARNING, "URLFetcher Exception #" + (var8 + 1), (Throwable)var2.get(var8));
         }
      }

      throw new IOException("Failed to fetch URL '" + var1 + "'.");
   }

   public FileCache(File var1, int var2, boolean var3) throws IOException {
      this(var1, var2, var3, Collections.singletonList(URLFetchers.DEFAULT));
   }

   public FileCache(File var1, int var2, boolean var3, URLFetcher... var4) throws IOException {
      this(var1, var2, var3, Arrays.asList(var4));
   }

   public FileCache(File var1, int var2, boolean var3, List<URLFetcher> var4) throws IOException {
      this.cacheDir = var1;
      this.buffer_size = var2;
      this.read_only = var3;
      this.fetchers = Collections.unmodifiableList(var4);
      if (var1.exists()) {
         if (!var1.isDirectory()) {
            this.loggedIOException(MLevel.SEVERE, var1 + "exists and is not a directory. Can't use as cacheDir.");
         } else if (!var1.canRead()) {
            this.loggedIOException(MLevel.SEVERE, var1 + "must be readable.");
         } else if (!var1.canWrite() && !var3) {
            this.loggedIOException(MLevel.SEVERE, var1 + "not writable, and not read only.");
         }
      } else if (!var1.mkdir()) {
         this.loggedIOException(MLevel.SEVERE, var1 + "does not exist and could not be created.");
      }
   }

   public void ensureCached(FileCacheKey var1, boolean var2) throws IOException {
      File var3 = this.file(var1);
      if (!this.read_only) {
         if (var2 || !var3.exists()) {
            BufferedInputStream var4 = null;
            BufferedOutputStream var5 = null;

            try {
               if (logger.isLoggable(MLevel.FINE)) {
                  logger.log(MLevel.FINE, "Caching file for " + var1 + " to " + var3.getAbsolutePath() + "...");
               }

               File var6 = var3.getParentFile();
               if (!var6.exists()) {
                  var6.mkdirs();
               }

               var4 = new BufferedInputStream(this.fetchURL(var1.getURL()), this.buffer_size);
               var5 = new BufferedOutputStream(new FileOutputStream(var3), this.buffer_size);

               for(int var7 = var4.read(); var7 >= 0; var7 = var4.read()) {
                  var5.write(var7);
               }

               if (logger.isLoggable(MLevel.INFO)) {
                  logger.log(MLevel.INFO, "Cached file for " + var1 + ".");
               }
            } catch (IOException var11) {
               logger.log(MLevel.WARNING, "An exception occurred while caching file for " + var1 + ". Deleting questionable cached file.", (Throwable)var11);
               var3.delete();
               throw var11;
            } finally {
               InputStreamUtils.attemptClose(var4);
               OutputStreamUtils.attemptClose(var5);
            }
         } else if (logger.isLoggable(MLevel.FINE)) {
            logger.log(MLevel.FINE, "File for " + var1 + " already exists and force_reacquire is not set.");
         }
      } else {
         if (var2) {
            String var14 = "force_reacquire canot be set on a read_only FileCache.";
            IllegalArgumentException var16 = new IllegalArgumentException(var14);
            logger.log(MLevel.WARNING, var14, (Throwable)var16);
            throw var16;
         }

         if (!var3.exists()) {
            String var13 = "Cache is read only, and file for key '" + var1 + "' does not exist.";
            FileNotCachedException var15 = new FileNotCachedException(var13);
            logger.log(MLevel.FINE, var13, (Throwable)var15);
            throw var15;
         }
      }
   }

   public InputStream fetch(FileCacheKey var1, boolean var2) throws IOException {
      this.ensureCached(var1, var2);
      return new FileInputStream(this.file(var1));
   }

   public boolean isCached(FileCacheKey var1) throws IOException {
      return this.file(var1).exists();
   }

   public int countCached() throws IOException {
      int var1 = 0;

      for(FileIterator var2 = DirectoryDescentUtils.depthFirstEagerDescent(this.cacheDir, NOT_DIR_FF, false); var2.hasNext(); ++var1) {
         var2.next();
      }

      return var1;
   }

   public int countCached(FileFilter var1) throws IOException {
      int var2 = 0;

      for(FileIterator var3 = DirectoryDescentUtils.depthFirstEagerDescent(this.cacheDir, new FileCache.NotDirAndFileFilter(var1), false);
         var3.hasNext();
         ++var2
      ) {
         var3.next();
      }

      return var2;
   }

   public File fileForKey(FileCacheKey var1) {
      return this.file(var1);
   }

   private File file(FileCacheKey var1) {
      return new File(this.cacheDir, var1.getCacheFilePath());
   }

   private void loggedIOException(MLevel var1, String var2) throws IOException {
      IOException var3 = new IOException(var2);
      logger.log(var1, var2, (Throwable)var3);
      throw var3;
   }

   static class NotDirAndFileFilter implements FileFilter {
      FileFilter ff;

      NotDirAndFileFilter(FileFilter var1) {
         this.ff = var1;
      }

      @Override
      public boolean accept(File var1) {
         return !var1.isDirectory() && this.ff.accept(var1);
      }
   }
}
