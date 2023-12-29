package com.mchange.io.impl;

import com.mchange.io.StringMemoryFile;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class LazyStringMemoryFileImpl extends LazyReadOnlyMemoryFileImpl implements StringMemoryFile {
   private static final String DEFAULT_ENCODING;
   String encoding = null;
   String string = null;

   public LazyStringMemoryFileImpl(File var1) {
      super(var1);
   }

   public LazyStringMemoryFileImpl(String var1) {
      super(var1);
   }

   @Override
   public synchronized String asString(String var1) throws IOException, UnsupportedEncodingException {
      this.update();
      if (this.encoding != var1) {
         this.string = new String(this.bytes, var1);
      }

      return this.string;
   }

   @Override
   public String asString() throws IOException {
      try {
         return this.asString(DEFAULT_ENCODING);
      } catch (UnsupportedEncodingException var2) {
         throw new InternalError("Default Encoding is not supported?!");
      }
   }

   @Override
   void refreshBytes() throws IOException {
      super.refreshBytes();
      this.encoding = this.string = null;
   }

   static {
      String var0 = System.getProperty("file.encoding");
      DEFAULT_ENCODING = var0 == null ? "8859_1" : var0;
   }
}
