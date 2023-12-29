package com.mchange.io.impl;

import com.mchange.io.ReadOnlyMemoryFile;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LazyReadOnlyMemoryFileImpl implements ReadOnlyMemoryFile {
   File file;
   byte[] bytes = null;
   long last_mod = -1L;
   int last_len = -1;

   public LazyReadOnlyMemoryFileImpl(File var1) {
      this.file = var1;
   }

   public LazyReadOnlyMemoryFileImpl(String var1) {
      this(new File(var1));
   }

   @Override
   public File getFile() {
      return this.file;
   }

   @Override
   public synchronized byte[] getBytes() throws IOException {
      this.update();
      return this.bytes;
   }

   void update() throws IOException {
      if (this.file.lastModified() > this.last_mod) {
         if (this.bytes != null) {
            this.last_len = this.bytes.length;
         }

         this.refreshBytes();
      }
   }

   void refreshBytes() throws IOException {
      ByteArrayOutputStream var1 = this.last_len > 0 ? new ByteArrayOutputStream(2 * this.last_len) : new ByteArrayOutputStream();
      BufferedInputStream var2 = new BufferedInputStream(new FileInputStream(this.file));

      for(int var3 = var2.read(); var3 >= 0; var3 = var2.read()) {
         var1.write((byte)var3);
      }

      this.bytes = var1.toByteArray();
      this.last_mod = this.file.lastModified();
   }
}
