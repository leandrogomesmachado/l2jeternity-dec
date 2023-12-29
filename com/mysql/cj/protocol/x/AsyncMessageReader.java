package com.mysql.cj.protocol.x;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.AssertionFailedException;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.MessageListener;
import com.mysql.cj.protocol.MessageReader;
import com.mysql.cj.protocol.SocketConnection;
import com.mysql.cj.x.protobuf.Mysqlx;
import com.mysql.cj.x.protobuf.MysqlxNotice;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CompletionHandler;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncMessageReader implements MessageReader<XMessageHeader, XMessage> {
   private static int READ_AHEAD_DEPTH = 10;
   AsyncMessageReader.CompletedRead currentReadResult;
   ByteBuffer messageBuf;
   private PropertySet propertySet;
   SocketConnection sc;
   CompletionHandler<Integer, Void> headerCompletionHandler = new AsyncMessageReader.HeaderCompletionHandler();
   CompletionHandler<Integer, Void> messageCompletionHandler = new AsyncMessageReader.MessageCompletionHandler();
   RuntimeProperty<Integer> asyncTimeout;
   MessageListener<XMessage> currentMessageListener;
   private BlockingQueue<MessageListener<XMessage>> messageListenerQueue = new LinkedBlockingQueue<>();
   BlockingQueue<AsyncMessageReader.CompletedRead> pendingCompletedReadQueue = new LinkedBlockingQueue<>(READ_AHEAD_DEPTH);
   CompletableFuture<XMessageHeader> pendingMsgHeader;
   Object pendingMsgMonitor = new Object();
   boolean stopAfterNextMessage = false;

   public AsyncMessageReader(PropertySet propertySet, SocketConnection socketConnection) {
      this.propertySet = propertySet;
      this.sc = socketConnection;
      this.asyncTimeout = this.propertySet.getIntegerProperty("xdevapi.asyncResponseTimeout");
   }

   @Override
   public void start() {
      this.headerCompletionHandler.completed(0, null);
   }

   @Override
   public void stopAfterNextMessage() {
      this.stopAfterNextMessage = true;
   }

   private void checkClosed() {
      if (!this.sc.getAsynchronousSocketChannel().isOpen()) {
         throw new CJCommunicationsException("Socket closed");
      }
   }

   @Override
   public void pushMessageListener(MessageListener<XMessage> l) {
      this.checkClosed();
      this.messageListenerQueue.add(l);
   }

   MessageListener<XMessage> getMessageListener(boolean block) {
      try {
         if (this.currentMessageListener == null) {
            this.currentMessageListener = block ? this.messageListenerQueue.take() : this.messageListenerQueue.poll();
         }

         return this.currentMessageListener;
      } catch (InterruptedException var3) {
         throw new CJCommunicationsException(var3);
      }
   }

   void dispatchMessage() {
      if (!this.pendingCompletedReadQueue.isEmpty()) {
         if (this.getMessageListener(true) != null) {
            AsyncMessageReader.CompletedRead res;
            try {
               res = this.pendingCompletedReadQueue.take();
            } catch (InterruptedException var7) {
               throw new CJCommunicationsException("Failed to peek pending message", var7);
            }

            GeneratedMessage message = res.message;
            synchronized(this.pendingMsgMonitor) {
               boolean currentListenerDone = this.currentMessageListener.createFromMessage(new XMessage(message));
               if (currentListenerDone) {
                  this.currentMessageListener = null;
               }

               this.pendingMsgHeader = null;
            }
         }
      }
   }

   void onError(Throwable t) {
      try {
         this.sc.getAsynchronousSocketChannel().close();
      } catch (Exception var6) {
      }

      if (this.currentMessageListener != null) {
         try {
            this.currentMessageListener.error(t);
         } catch (Exception var5) {
         }

         this.currentMessageListener = null;
      }

      this.messageListenerQueue.forEach(l -> {
         try {
            l.error(t);
         } catch (Exception var3) {
         }
      });
      synchronized(this.pendingMsgMonitor) {
         this.pendingMsgHeader = new CompletableFuture<>();
         this.pendingMsgHeader.completeExceptionally(t);
         this.pendingMsgMonitor.notify();
      }

      this.messageListenerQueue.clear();
   }

   public XMessageHeader readHeader() throws IOException {
      XMessageHeader mh;
      synchronized(this.pendingMsgMonitor) {
         this.checkClosed();

         while(this.pendingMsgHeader == null) {
            try {
               this.pendingMsgMonitor.wait();
            } catch (InterruptedException var7) {
               throw new CJCommunicationsException(var7);
            }
         }

         try {
            mh = this.pendingMsgHeader.get();
         } catch (ExecutionException var5) {
            throw new CJCommunicationsException("Failed to peek pending message", var5.getCause());
         } catch (InterruptedException var6) {
            throw new CJCommunicationsException(var6);
         }
      }

      if (mh.getMessageType() == 1) {
         this.readMessage(null, mh);
      }

      return mh;
   }

   public XMessage readMessage(Optional<XMessage> reuse, XMessageHeader hdr) throws IOException {
      return this.readMessage(reuse, hdr.getMessageType());
   }

   public XMessage readMessage(Optional<XMessage> reuse, int expectedType) throws IOException {
      Class<? extends GeneratedMessage> expectedClass = MessageConstants.getMessageClassForType(expectedType);
      CompletableFuture<XMessage> future = new CompletableFuture<>();
      AsyncMessageReader.SyncXMessageListener<? extends GeneratedMessage> r = new AsyncMessageReader.SyncXMessageListener<>(future, expectedClass);
      this.pushMessageListener(r);

      try {
         return future.get((long)this.asyncTimeout.getValue().intValue(), TimeUnit.SECONDS);
      } catch (ExecutionException var7) {
         if (XProtocolError.class.equals(var7.getCause().getClass())) {
            throw new XProtocolError((XProtocolError)var7.getCause());
         } else {
            throw new CJCommunicationsException(var7.getCause().getMessage(), var7.getCause());
         }
      } catch (TimeoutException | InterruptedException var8) {
         throw new CJCommunicationsException(var8);
      }
   }

   private static class CompletedRead {
      public XMessageHeader header = null;
      public GeneratedMessage message = null;

      public CompletedRead() {
      }
   }

   private class HeaderCompletionHandler implements CompletionHandler<Integer, Void> {
      public HeaderCompletionHandler() {
      }

      public void completed(Integer bytesRead, Void attachment) {
         if (bytesRead < 0) {
            AsyncMessageReader.this.onError(new CJCommunicationsException("Socket closed"));
         } else {
            try {
               if (AsyncMessageReader.this.currentReadResult == null) {
                  AsyncMessageReader.this.currentReadResult = new AsyncMessageReader.CompletedRead();
                  AsyncMessageReader.this.currentReadResult.header = new XMessageHeader();
               }

               if (AsyncMessageReader.this.currentReadResult.header.getBuffer().position() < 5) {
                  AsyncMessageReader.this.sc.getAsynchronousSocketChannel().read(AsyncMessageReader.this.currentReadResult.header.getBuffer(), null, this);
                  return;
               }

               AsyncMessageReader.this.messageBuf = ByteBuffer.allocate(AsyncMessageReader.this.currentReadResult.header.getMessageSize());
               if (AsyncMessageReader.this.getMessageListener(false) == null) {
                  synchronized(AsyncMessageReader.this.pendingMsgMonitor) {
                     AsyncMessageReader.this.pendingMsgHeader = CompletableFuture.completedFuture(AsyncMessageReader.this.currentReadResult.header);
                     AsyncMessageReader.this.pendingMsgMonitor.notify();
                  }
               }

               AsyncMessageReader.this.messageCompletionHandler.completed(0, null);
            } catch (Throwable var6) {
               AsyncMessageReader.this.onError(var6);
            }
         }
      }

      public void failed(Throwable exc, Void attachment) {
         if (AsyncMessageReader.this.getMessageListener(false) != null) {
            synchronized(AsyncMessageReader.this.pendingMsgMonitor) {
               AsyncMessageReader.this.pendingMsgMonitor.notify();
            }

            if (AsynchronousCloseException.class.equals(exc.getClass())) {
               AsyncMessageReader.this.currentMessageListener.error(new CJCommunicationsException("Socket closed", exc));
            } else {
               AsyncMessageReader.this.currentMessageListener.error(exc);
            }
         }

         AsyncMessageReader.this.currentMessageListener = null;
      }
   }

   private class MessageCompletionHandler implements CompletionHandler<Integer, Void> {
      public MessageCompletionHandler() {
      }

      public void completed(Integer bytesRead, Void attachment) {
         if (bytesRead < 0) {
            AsyncMessageReader.this.onError(new CJCommunicationsException("Socket closed"));
         } else {
            try {
               if (AsyncMessageReader.this.messageBuf.position() < AsyncMessageReader.this.currentReadResult.header.getMessageSize()) {
                  AsyncMessageReader.this.sc.getAsynchronousSocketChannel().read(AsyncMessageReader.this.messageBuf, null, this);
                  return;
               }

               ByteBuffer buf = AsyncMessageReader.this.messageBuf;
               AsyncMessageReader.this.messageBuf = null;
               Class<? extends GeneratedMessage> messageClass = MessageConstants.getMessageClassForType(
                  AsyncMessageReader.this.currentReadResult.header.getMessageType()
               );
               boolean localStopAfterNextMessage = AsyncMessageReader.this.stopAfterNextMessage;
               ((Buffer)buf).flip();
               AsyncMessageReader.this.currentReadResult.message = this.parseMessage(messageClass, buf);
               AsyncMessageReader.this.pendingCompletedReadQueue.add(AsyncMessageReader.this.currentReadResult);
               AsyncMessageReader.this.currentReadResult = null;
               AsyncMessageReader.this.dispatchMessage();
               if (localStopAfterNextMessage && messageClass != MysqlxNotice.Frame.class) {
                  AsyncMessageReader.this.stopAfterNextMessage = false;
                  AsyncMessageReader.this.currentReadResult = null;
                  return;
               }

               AsyncMessageReader.this.headerCompletionHandler.completed(0, null);
            } catch (Throwable var6) {
               AsyncMessageReader.this.onError(var6);
            }
         }
      }

      public void failed(Throwable exc, Void attachment) {
         if (AsyncMessageReader.this.getMessageListener(false) != null) {
            synchronized(AsyncMessageReader.this.pendingMsgMonitor) {
               AsyncMessageReader.this.pendingMsgMonitor.notify();
            }

            if (AsynchronousCloseException.class.equals(exc.getClass())) {
               AsyncMessageReader.this.currentMessageListener.error(new CJCommunicationsException("Socket closed", exc));
            } else {
               AsyncMessageReader.this.currentMessageListener.error(exc);
            }
         }

         AsyncMessageReader.this.currentMessageListener = null;
      }

      private GeneratedMessage parseMessage(Class<? extends GeneratedMessage> messageClass, ByteBuffer buf) {
         try {
            Parser<? extends GeneratedMessage> parser = (Parser)MessageConstants.MESSAGE_CLASS_TO_PARSER.get(messageClass);
            return (GeneratedMessage)parser.parseFrom(CodedInputStream.newInstance(buf));
         } catch (InvalidProtocolBufferException var4) {
            throw AssertionFailedException.shouldNotHappen(var4);
         }
      }
   }

   private static final class SyncXMessageListener<T extends GeneratedMessage> implements MessageListener<XMessage> {
      private CompletableFuture<XMessage> future;
      private Class<T> expectedClass;

      public SyncXMessageListener(CompletableFuture<XMessage> future, Class<T> expectedClass) {
         this.future = future;
         this.expectedClass = expectedClass;
      }

      public Boolean createFromMessage(XMessage msg) {
         Class<? extends GeneratedMessage> msgClass = msg.getMessage().getClass();
         if (Mysqlx.Error.class.equals(msgClass)) {
            this.future.completeExceptionally(new XProtocolError(Mysqlx.Error.class.cast(msg.getMessage())));
            return true;
         } else if (this.expectedClass.equals(msgClass)) {
            this.future.complete(msg);
            return true;
         } else {
            this.future.completeExceptionally(new WrongArgumentException("Unhandled msg class (" + msgClass + ") + msg=" + msg.getMessage()));
            return true;
         }
      }

      @Override
      public void error(Throwable ex) {
         this.future.completeExceptionally(ex);
      }
   }
}
