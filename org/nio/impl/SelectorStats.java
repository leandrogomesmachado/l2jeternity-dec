package org.nio.impl;

import java.util.concurrent.atomic.AtomicLong;

public class SelectorStats {
   private final AtomicLong _connectionsTotal = new AtomicLong();
   private final AtomicLong _connectionsCurrent = new AtomicLong();
   private final AtomicLong _connectionsMax = new AtomicLong();
   private final AtomicLong _incomingBytesTotal = new AtomicLong();
   private final AtomicLong _outgoingBytesTotal = new AtomicLong();
   private final AtomicLong _incomingPacketsTotal = new AtomicLong();
   private final AtomicLong _outgoingPacketsTotal = new AtomicLong();
   private final AtomicLong _bytesMaxPerRead = new AtomicLong();
   private final AtomicLong _bytesMaxPerWrite = new AtomicLong();

   public void increaseOpenedConnections() {
      if (this._connectionsCurrent.incrementAndGet() > this._connectionsMax.get()) {
         this._connectionsMax.incrementAndGet();
      }

      this._connectionsTotal.incrementAndGet();
   }

   public void decreaseOpenedConnections() {
      this._connectionsCurrent.decrementAndGet();
   }

   public void increaseIncomingBytes(int size) {
      if ((long)size > this._bytesMaxPerRead.get()) {
         this._bytesMaxPerRead.set((long)size);
      }

      this._incomingBytesTotal.addAndGet((long)size);
   }

   public void increaseOutgoingBytes(int size) {
      if ((long)size > this._bytesMaxPerWrite.get()) {
         this._bytesMaxPerWrite.set((long)size);
      }

      this._outgoingBytesTotal.addAndGet((long)size);
   }

   public void increaseIncomingPacketsCount() {
      this._incomingPacketsTotal.incrementAndGet();
   }

   public void increaseOutgoingPacketsCount() {
      this._outgoingPacketsTotal.incrementAndGet();
   }

   public long getTotalConnections() {
      return this._connectionsTotal.get();
   }

   public long getCurrentConnections() {
      return this._connectionsCurrent.get();
   }

   public long getMaximumConnections() {
      return this._connectionsMax.get();
   }

   public long getIncomingBytesTotal() {
      return this._incomingBytesTotal.get();
   }

   public long getOutgoingBytesTotal() {
      return this._outgoingBytesTotal.get();
   }

   public long getIncomingPacketsTotal() {
      return this._incomingPacketsTotal.get();
   }

   public long getOutgoingPacketsTotal() {
      return this._outgoingPacketsTotal.get();
   }

   public long getMaxBytesPerRead() {
      return this._bytesMaxPerRead.get();
   }

   public long getMaxBytesPerWrite() {
      return this._bytesMaxPerWrite.get();
   }
}
