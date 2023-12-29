package com.mysql.cj.protocol;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class SerializingBufferWriter implements CompletionHandler<Long, Void> {
   private static int WRITES_AT_ONCE = 200;
   protected AsynchronousSocketChannel channel;
   private Queue<SerializingBufferWriter.ByteBufferWrapper> pendingWrites = new LinkedList<>();

   public SerializingBufferWriter(AsynchronousSocketChannel channel) {
      this.channel = channel;
   }

   private void initiateWrite() {
      try {
         ByteBuffer[] bufs = this.pendingWrites
            .stream()
            .limit((long)WRITES_AT_ONCE)
            .map(SerializingBufferWriter.ByteBufferWrapper::getBuffer)
            .toArray(size -> new ByteBuffer[size]);
         this.channel.write(bufs, 0, bufs.length, 0L, TimeUnit.MILLISECONDS, null, this);
      } catch (WritePendingException | ReadPendingException var2) {
         return;
      } catch (Throwable var3) {
         this.failed(var3, null);
      }
   }

   public void queueBuffer(ByteBuffer buf, CompletionHandler<Long, Void> callback) {
      synchronized(this.pendingWrites) {
         this.pendingWrites.add(new SerializingBufferWriter.ByteBufferWrapper(buf, callback));
         if (this.pendingWrites.size() == 1) {
            this.initiateWrite();
         }
      }
   }

   public void completed(Long bytesWritten, Void v) {
      LinkedList<CompletionHandler<Long, Void>> completedWrites = new LinkedList<>();
      synchronized(this.pendingWrites) {
         while(this.pendingWrites.peek() != null && !this.pendingWrites.peek().getBuffer().hasRemaining() && completedWrites.size() < WRITES_AT_ONCE) {
            completedWrites.add(this.pendingWrites.remove().getHandler());
         }

         completedWrites.stream().filter(Objects::nonNull).forEach(l -> {
            try {
               l.completed(0L, null);
            } catch (Throwable var4) {
               Throwable ex = var4;

               try {
                  l.failed(ex, null);
               } catch (Throwable var3xx) {
                  var3xx.printStackTrace();
               }
            }
         });
         if (this.pendingWrites.size() > 0) {
            this.initiateWrite();
         }
      }
   }

   public void failed(Throwable t, Void v) {
      try {
         this.channel.close();
      } catch (Exception var7) {
      }

      LinkedList<CompletionHandler<Long, Void>> failedWrites = new LinkedList<>();
      synchronized(this.pendingWrites) {
         while(this.pendingWrites.peek() != null) {
            SerializingBufferWriter.ByteBufferWrapper bw = this.pendingWrites.remove();
            if (bw.getHandler() != null) {
               failedWrites.add(bw.getHandler());
            }
         }
      }

      failedWrites.forEach(l -> {
         try {
            l.failed(t, null);
         } catch (Exception var3xx) {
         }
      });
      failedWrites.clear();
   }

   public void setChannel(AsynchronousSocketChannel channel) {
      this.channel = channel;
   }

   private static class ByteBufferWrapper {
      private ByteBuffer buffer;
      private CompletionHandler<Long, Void> handler = null;

      ByteBufferWrapper(ByteBuffer buffer, CompletionHandler<Long, Void> completionHandler) {
         this.buffer = buffer;
         this.handler = completionHandler;
      }

      public ByteBuffer getBuffer() {
         return this.buffer;
      }

      public CompletionHandler<Long, Void> getHandler() {
         return this.handler;
      }
   }
}
