package com.mysql.cj.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WatchableOutputStream extends ByteArrayOutputStream implements WatchableStream {
   private OutputStreamWatcher watcher;

   @Override
   public void close() throws IOException {
      super.close();
      if (this.watcher != null) {
         this.watcher.streamClosed(this);
      }
   }

   @Override
   public void setWatcher(OutputStreamWatcher watcher) {
      this.watcher = watcher;
   }
}
