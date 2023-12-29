package com.mysql.cj.protocol.x;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Parser;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.FullReadInputStream;
import com.mysql.cj.protocol.MessageListener;
import com.mysql.cj.protocol.MessageReader;
import com.mysql.cj.x.protobuf.Mysqlx;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SyncMessageReader implements MessageReader<XMessageHeader, XMessage> {
   private FullReadInputStream inputStream;
   private XMessageHeader header;
   BlockingQueue<MessageListener<XMessage>> messageListenerQueue = new LinkedBlockingQueue<>();
   Object dispatchingThreadMonitor = new Object();
   Object waitingSyncOperationMonitor = new Object();
   Thread dispatchingThread = null;

   public SyncMessageReader(FullReadInputStream inputStream) {
      this.inputStream = inputStream;
   }

   public XMessageHeader readHeader() throws IOException {
      synchronized(this.waitingSyncOperationMonitor) {
         if (this.header == null) {
            this.header = this.readHeaderLocal();
         }

         if (this.header.getMessageType() == 1) {
            throw new XProtocolError(this.readMessageLocal(Mysqlx.Error.class));
         } else {
            return this.header;
         }
      }
   }

   private XMessageHeader readHeaderLocal() throws IOException {
      try {
         byte[] len = new byte[5];
         this.inputStream.readFully(len);
         this.header = new XMessageHeader(len);
      } catch (IOException var2) {
         throw new CJCommunicationsException("Cannot read packet header", var2);
      }

      return this.header;
   }

   private <T extends GeneratedMessage> T readMessageLocal(Class<T> messageClass) {
      Parser<T> parser = (Parser)MessageConstants.MESSAGE_CLASS_TO_PARSER.get(messageClass);
      byte[] packet = new byte[this.header.getMessageSize()];

      try {
         this.inputStream.readFully(packet);
      } catch (IOException var11) {
         throw new CJCommunicationsException("Cannot read packet payload", var11);
      }

      GeneratedMessage ex;
      try {
         ex = (GeneratedMessage)parser.parseFrom(packet);
      } catch (InvalidProtocolBufferException var9) {
         throw new WrongArgumentException(var9);
      } finally {
         this.header = null;
      }

      return (T)ex;
   }

   public XMessage readMessage(Optional<XMessage> reuse, XMessageHeader hdr) throws IOException {
      return this.readMessage(reuse, hdr.getMessageType());
   }

   public XMessage readMessage(Optional<XMessage> reuse, int expectedType) throws IOException {
      synchronized(this.waitingSyncOperationMonitor) {
         XMessage var10000;
         try {
            Class<? extends GeneratedMessage> messageClass = MessageConstants.getMessageClassForType(this.readHeader().getMessageType());
            Class<? extends GeneratedMessage> expectedClass = MessageConstants.getMessageClassForType(expectedType);
            if (expectedClass != messageClass) {
               throw new WrongArgumentException(
                  "Unexpected message class. Expected '" + expectedClass.getSimpleName() + "' but actually received '" + messageClass.getSimpleName() + "'"
               );
            }

            var10000 = new XMessage(this.readMessageLocal(messageClass));
         } catch (IOException var7) {
            throw new XProtocolError(var7.getMessage(), var7);
         }

         return var10000;
      }
   }

   @Override
   public void pushMessageListener(MessageListener<XMessage> listener) {
      try {
         this.messageListenerQueue.put(listener);
      } catch (InterruptedException var8) {
         throw new CJCommunicationsException("Cannot queue message listener.", var8);
      }

      synchronized(this.dispatchingThreadMonitor) {
         if (this.dispatchingThread == null) {
            SyncMessageReader.ListenersDispatcher ld = new SyncMessageReader.ListenersDispatcher();
            this.dispatchingThread = new Thread(ld, "Message listeners dispatching thread");
            this.dispatchingThread.start();
            int millis = 5000;

            while(!ld.started) {
               try {
                  Thread.sleep(10L);
                  millis -= 10;
               } catch (InterruptedException var7) {
                  throw new XProtocolError(var7.getMessage(), var7);
               }

               if (millis <= 0) {
                  throw new XProtocolError("Timeout for starting ListenersDispatcher exceeded.");
               }
            }
         }
      }
   }

   private class ListenersDispatcher implements Runnable {
      private static final long POLL_TIMEOUT = 100L;
      boolean started = false;

      public ListenersDispatcher() {
      }

      @Override
      public void run() {
         synchronized(SyncMessageReader.this.waitingSyncOperationMonitor) {
            this.started = true;

            try {
               while(true) {
                  MessageListener<XMessage> l;
                  while((l = SyncMessageReader.this.messageListenerQueue.poll(100L, TimeUnit.MILLISECONDS)) != null) {
                     try {
                        XMessage msg = null;

                        while(true) {
                           XMessageHeader hdr = SyncMessageReader.this.readHeader();
                           msg = SyncMessageReader.this.readMessage(null, hdr);
                           if (l.createFromMessage(msg)) {
                              break;
                           }
                        }
                     } catch (Throwable var6) {
                        l.error(var6);
                     }
                  }

                  synchronized(SyncMessageReader.this.dispatchingThreadMonitor) {
                     if (SyncMessageReader.this.messageListenerQueue.peek() == null) {
                        SyncMessageReader.this.dispatchingThread = null;
                        return;
                     }
                  }
               }
            } catch (InterruptedException var8) {
               throw new CJCommunicationsException("Read operation interrupted.", var8);
            }
         }
      }
   }
}
