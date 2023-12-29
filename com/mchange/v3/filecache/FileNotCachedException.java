package com.mchange.v3.filecache;

import java.io.FileNotFoundException;

public class FileNotCachedException extends FileNotFoundException {
   FileNotCachedException(String var1) {
      super(var1);
   }

   FileNotCachedException() {
   }
}
