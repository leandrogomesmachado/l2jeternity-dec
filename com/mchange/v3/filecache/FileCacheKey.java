package com.mchange.v3.filecache;

import java.net.URL;

public interface FileCacheKey {
   URL getURL();

   String getCacheFilePath();

   @Override
   boolean equals(Object var1);

   @Override
   int hashCode();
}
