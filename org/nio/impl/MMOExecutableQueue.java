package org.nio.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2e.commons.util.concurrent.Lockable;

public class MMOExecutableQueue<T extends MMOClient> implements Queue<ReceivablePacket<T>>, Lockable, Runnable {
   private static final int NONE = 0;
   private static final int QUEUED = 1;
   private static final int RUNNING = 2;
   private final IMMOExecutor<T> _executor;
   private final Queue<ReceivablePacket<T>> _queue = new ArrayDeque<>();
   private final Lock _lock = new ReentrantLock();
   private final AtomicInteger _state = new AtomicInteger(0);

   public MMOExecutableQueue(IMMOExecutor<T> executor) {
      this._executor = executor;
   }

   @Override
   public void lock() {
      this._lock.lock();
   }

   @Override
   public void unlock() {
      this._lock.unlock();
   }

   @Override
   public void run() {
      while(this._state.compareAndSet(1, 2)) {
         try {
            Runnable t = this.poll();
            if (t != null) {
               t.run();
            }
         } finally {
            this._state.compareAndSet(2, 0);
         }
      }
   }

   @Override
   public int size() {
      return this._queue.size();
   }

   @Override
   public boolean isEmpty() {
      return this._queue.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Iterator<ReceivablePacket<T>> iterator() {
      throw new UnsupportedOperationException();
   }

   @Override
   public Object[] toArray() {
      throw new UnsupportedOperationException();
   }

   @Override
   public <E> E[] toArray(E[] a) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean containsAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean addAll(Collection<? extends ReceivablePacket<T>> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clear() {
      this.lock();
      this._queue.clear();
      this.unlock();
   }

   public boolean add(ReceivablePacket<T> e) {
      this.lock();
      if (!this._queue.add(e)) {
         this.unlock();
         return false;
      } else {
         this.unlock();
         if (this._state.getAndSet(1) == 0) {
            this._executor.execute(this);
         }

         return true;
      }
   }

   public boolean offer(ReceivablePacket<T> e) {
      this.lock();
      boolean result = this._queue.offer(e);
      this.unlock();
      return result;
   }

   public ReceivablePacket<T> remove() {
      this.lock();
      ReceivablePacket<T> result = this._queue.remove();
      this.unlock();
      return result;
   }

   public ReceivablePacket<T> poll() {
      this.lock();
      ReceivablePacket<T> result = this._queue.poll();
      this.unlock();
      return result;
   }

   public ReceivablePacket<T> element() {
      this.lock();
      ReceivablePacket<T> result = this._queue.element();
      this.unlock();
      return result;
   }

   public ReceivablePacket<T> peek() {
      this.lock();
      ReceivablePacket<T> result = this._queue.peek();
      this.unlock();
      return result;
   }
}
