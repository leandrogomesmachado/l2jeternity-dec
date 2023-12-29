package org.apache.commons.pool.impl;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;

public class StackKeyedObjectPoolFactory implements KeyedObjectPoolFactory {
   protected KeyedPoolableObjectFactory _factory = null;
   protected int _maxSleeping = 8;
   protected int _initCapacity = 4;

   public StackKeyedObjectPoolFactory() {
      this((KeyedPoolableObjectFactory)null, 8, 4);
   }

   public StackKeyedObjectPoolFactory(int max) {
      this((KeyedPoolableObjectFactory)null, max, 4);
   }

   public StackKeyedObjectPoolFactory(int max, int init) {
      this((KeyedPoolableObjectFactory)null, max, init);
   }

   public StackKeyedObjectPoolFactory(KeyedPoolableObjectFactory factory) {
      this(factory, 8, 4);
   }

   public StackKeyedObjectPoolFactory(KeyedPoolableObjectFactory factory, int max) {
      this(factory, max, 4);
   }

   public StackKeyedObjectPoolFactory(KeyedPoolableObjectFactory factory, int max, int init) {
      this._factory = factory;
      this._maxSleeping = max;
      this._initCapacity = init;
   }

   public KeyedObjectPool createPool() {
      return new StackKeyedObjectPool(this._factory, this._maxSleeping, this._initCapacity);
   }
}
