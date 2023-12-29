package com.mchange.util.impl;

import com.mchange.util.Queue;

public class CircularListQueue implements Queue, Cloneable {
   CircularList list;

   @Override
   public int size() {
      return this.list.size();
   }

   @Override
   public boolean hasMoreElements() {
      return this.list.size() > 0;
   }

   @Override
   public void enqueue(Object var1) {
      this.list.appendElement(var1);
   }

   @Override
   public Object peek() {
      return this.list.getFirstElement();
   }

   @Override
   public Object dequeue() {
      Object var1 = this.list.getFirstElement();
      this.list.removeFirstElement();
      return var1;
   }

   @Override
   public Object clone() {
      return new CircularListQueue((CircularList)this.list.clone());
   }

   public CircularListQueue() {
      this.list = new CircularList();
   }

   private CircularListQueue(CircularList var1) {
      this.list = var1;
   }
}
