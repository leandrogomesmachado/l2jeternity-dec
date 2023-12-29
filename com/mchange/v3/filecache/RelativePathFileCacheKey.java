package com.mchange.v3.filecache;

import java.net.MalformedURLException;
import java.net.URL;

public class RelativePathFileCacheKey implements FileCacheKey {
   final URL url;
   final String relPath;

   public RelativePathFileCacheKey(URL var1, String var2) throws MalformedURLException, IllegalArgumentException {
      String var3 = var2.trim();
      if (var1 == null || var2 == null) {
         throw new IllegalArgumentException("parentURL [" + var1 + "] and relative path [" + var2 + "] must be non-null");
      } else if (var3.length() == 0) {
         throw new IllegalArgumentException("relative path [" + var2 + "] must not be a blank string");
      } else if (!var3.equals(var2)) {
         throw new IllegalArgumentException("relative path [" + var2 + "] must not begin or end with whitespace.");
      } else if (var2.startsWith("/")) {
         throw new IllegalArgumentException("Path must be relative, '" + var2 + "' begins with '/'.");
      } else {
         this.url = new URL(var1, var2);
         this.relPath = var2;
      }
   }

   @Override
   public URL getURL() {
      return this.url;
   }

   @Override
   public String getCacheFilePath() {
      return this.relPath;
   }

   @Override
   public boolean equals(Object var1) {
      if (!(var1 instanceof RelativePathFileCacheKey)) {
         return false;
      } else {
         RelativePathFileCacheKey var2 = (RelativePathFileCacheKey)var1;
         return this.url.equals(var2.url) && this.relPath.equals(var2.relPath);
      }
   }

   @Override
   public int hashCode() {
      return this.url.hashCode() ^ this.relPath.hashCode();
   }
}
