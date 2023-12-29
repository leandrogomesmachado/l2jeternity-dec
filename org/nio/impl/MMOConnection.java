package org.nio.impl;

import java.io.IOException;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2e.commons.util.concurrent.Lockable;

public class MMOConnection<T extends MMOClient> implements Lockable {
   private final SelectorThread<T> _selectorThread;
   private final SelectionKey _selectionKey;
   private final Socket _socket;
   private final WritableByteChannel _writableByteChannel;
   private final ReadableByteChannel _readableByteChannel;
   private final Queue<SendablePacket<T>> _sendQueue;
   private final Queue<ReceivablePacket<T>> _recvQueue;
   private T _client;
   private ByteBuffer _readBuffer;
   private ByteBuffer _primaryWriteBuffer;
   private ByteBuffer _secondaryWriteBuffer;
   private final long _connectionOpenTime;
   private boolean _pendingClose;
   private long _pendingCloseTime;
   private boolean _closed;
   private long _pendingWriteTime;
   private final AtomicBoolean _isPengingWrite = new AtomicBoolean();
   private final Lock _lock = new ReentrantLock();

   public MMOConnection(SelectorThread<T> selectorThread, Socket socket, SelectionKey key) {
      this._selectorThread = selectorThread;
      this._selectionKey = key;
      this._socket = socket;
      this._writableByteChannel = socket.getChannel();
      this._readableByteChannel = socket.getChannel();
      this._sendQueue = new ArrayDeque<>();
      this._recvQueue = new MMOExecutableQueue<>(selectorThread.getExecutor());
      this._connectionOpenTime = System.currentTimeMillis();
   }

   @Override
   public void lock() {
      this._lock.lock();
   }

   @Override
   public void unlock() {
      this._lock.unlock();
   }

   protected long getConnectionOpenTime() {
      return this._connectionOpenTime;
   }

   protected void setClient(T client) {
      this._client = client;
   }

   public T getClient() {
      return this._client;
   }

   public void recvPacket(ReceivablePacket<T> rp) {
      if (rp != null) {
         if (!this.isClosed()) {
            this._recvQueue.add(rp);
         }
      }
   }

   public void sendPacket(SendablePacket<T> sp) {
      if (sp != null) {
         this.lock();

         label36: {
            try {
               if (!this.isClosed()) {
                  this._sendQueue.add(sp);
                  break label36;
               }
            } finally {
               this.unlock();
            }

            return;
         }

         this.scheduleWriteInterest();
      }
   }

   public void sendPacket(SendablePacket<T>... args) {
      if (args != null && args.length != 0) {
         this.lock();

         try {
            if (this.isClosed()) {
               return;
            }

            for(SendablePacket<T> sp : args) {
               if (sp != null) {
                  this._sendQueue.add(sp);
               }
            }
         } finally {
            this.unlock();
         }

         this.scheduleWriteInterest();
      }
   }

   public void sendPackets(List<? extends SendablePacket<T>> args) {
      if (args != null && !args.isEmpty()) {
         this.lock();

         try {
            if (this.isClosed()) {
               return;
            }

            for(int i = 0; i < args.size(); ++i) {
               SendablePacket<T> sp;
               if ((sp = args.get(i)) != null) {
                  this._sendQueue.add(sp);
               }
            }
         } finally {
            this.unlock();
         }

         this.scheduleWriteInterest();
      }
   }

   protected SelectionKey getSelectionKey() {
      return this._selectionKey;
   }

   protected void disableReadInterest() {
      try {
         this._selectionKey.interestOps(this._selectionKey.interestOps() & -2);
      } catch (CancelledKeyException var2) {
      }
   }

   protected void scheduleWriteInterest() {
      if (this._isPengingWrite.compareAndSet(false, true)) {
         this._pendingWriteTime = System.currentTimeMillis();
      }
   }

   protected void enableWriteInterest() {
      try {
         if (this._isPengingWrite.compareAndSet(true, false)) {
            this._selectionKey.interestOps(this._selectionKey.interestOps() | 4);
         }
      } catch (CancelledKeyException var2) {
      }
   }

   protected void disableWriteInterest() {
      try {
         this._selectionKey.interestOps(this._selectionKey.interestOps() & -5);
      } catch (CancelledKeyException var2) {
      }
   }

   protected boolean isPendingWrite() {
      return this._isPengingWrite.get();
   }

   protected long getPendingWriteTime() {
      return this._pendingWriteTime;
   }

   public Socket getSocket() {
      return this._socket;
   }

   protected WritableByteChannel getWritableChannel() {
      return this._writableByteChannel;
   }

   protected ReadableByteChannel getReadableByteChannel() {
      return this._readableByteChannel;
   }

   protected Queue<SendablePacket<T>> getSendQueue() {
      return this._sendQueue;
   }

   protected Queue<ReceivablePacket<T>> getRecvQueue() {
      return this._recvQueue;
   }

   protected void createWriteBuffer(ByteBuffer buf) {
      if (this._primaryWriteBuffer == null) {
         this._primaryWriteBuffer = this._selectorThread.getPooledBuffer();
         this._primaryWriteBuffer.put(buf);
      } else {
         ByteBuffer temp = this._selectorThread.getPooledBuffer();
         temp.put(buf);
         int remaining = temp.remaining();
         ((Buffer)this._primaryWriteBuffer).flip();
         int limit = this._primaryWriteBuffer.limit();
         if (remaining >= this._primaryWriteBuffer.remaining()) {
            temp.put(this._primaryWriteBuffer);
            this._selectorThread.recycleBuffer(this._primaryWriteBuffer);
            this._primaryWriteBuffer = temp;
         } else {
            ((Buffer)this._primaryWriteBuffer).limit(remaining);
            temp.put(this._primaryWriteBuffer);
            ((Buffer)this._primaryWriteBuffer).limit(limit);
            this._primaryWriteBuffer.compact();
            this._secondaryWriteBuffer = this._primaryWriteBuffer;
            this._primaryWriteBuffer = temp;
         }
      }
   }

   protected boolean hasPendingWriteBuffer() {
      return this._primaryWriteBuffer != null;
   }

   protected void movePendingWriteBufferTo(ByteBuffer dest) {
      ((Buffer)this._primaryWriteBuffer).flip();
      dest.put(this._primaryWriteBuffer);
      this._selectorThread.recycleBuffer(this._primaryWriteBuffer);
      this._primaryWriteBuffer = this._secondaryWriteBuffer;
      this._secondaryWriteBuffer = null;
   }

   protected void setReadBuffer(ByteBuffer buf) {
      this._readBuffer = buf;
   }

   protected ByteBuffer getReadBuffer() {
      return this._readBuffer;
   }

   public boolean isClosed() {
      return this._pendingClose || this._closed;
   }

   protected boolean isPengingClose() {
      return this._pendingClose;
   }

   protected long getPendingCloseTime() {
      return this._pendingCloseTime;
   }

   protected void close() throws IOException {
      this._closed = true;
      this._socket.close();
   }

   protected void closeNow() {
      this.lock();

      label32: {
         try {
            if (!this.isClosed()) {
               this._sendQueue.clear();
               this._pendingClose = true;
               this._pendingCloseTime = System.currentTimeMillis();
               break label32;
            }
         } finally {
            this.unlock();
         }

         return;
      }

      this.disableReadInterest();
      this.disableWriteInterest();
   }

   public void close(SendablePacket<T> sp) {
      this.lock();

      label32: {
         try {
            if (!this.isClosed()) {
               this._sendQueue.clear();
               this.sendPacket(sp);
               this._pendingClose = true;
               this._pendingCloseTime = System.currentTimeMillis();
               break label32;
            }
         } finally {
            this.unlock();
         }

         return;
      }

      this.disableReadInterest();
   }

   protected void closeLater() {
      this.lock();

      try {
         if (!this.isClosed()) {
            this._pendingClose = true;
            this._pendingCloseTime = System.currentTimeMillis();
            return;
         }
      } finally {
         this.unlock();
      }
   }

   protected void releaseBuffers() {
      if (this._primaryWriteBuffer != null) {
         this._selectorThread.recycleBuffer(this._primaryWriteBuffer);
         this._primaryWriteBuffer = null;
         if (this._secondaryWriteBuffer != null) {
            this._selectorThread.recycleBuffer(this._secondaryWriteBuffer);
            this._secondaryWriteBuffer = null;
         }
      }

      if (this._readBuffer != null) {
         this._selectorThread.recycleBuffer(this._readBuffer);
         this._readBuffer = null;
      }
   }

   protected void clearQueues() {
      this.lock();

      try {
         this._sendQueue.clear();
         this._recvQueue.clear();
      } finally {
         this.unlock();
      }
   }

   protected void onDisconnection() {
      this.getClient().onDisconnection();
   }

   protected void onForcedDisconnection() {
      this.getClient().onForcedDisconnection();
   }

   @Override
   public String toString() {
      return "MMOConnection: selector=" + this._selectorThread + "; client=" + this.getClient();
   }
}
