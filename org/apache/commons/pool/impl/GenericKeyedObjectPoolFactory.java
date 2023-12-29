package org.apache.commons.pool.impl;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;

public class GenericKeyedObjectPoolFactory implements KeyedObjectPoolFactory {
   protected int _maxIdle = 8;
   protected int _maxActive = 8;
   protected int _maxTotal = -1;
   protected int _minIdle = 0;
   protected long _maxWait = -1L;
   protected byte _whenExhaustedAction = 1;
   protected boolean _testOnBorrow = false;
   protected boolean _testOnReturn = false;
   protected boolean _testWhileIdle = false;
   protected long _timeBetweenEvictionRunsMillis = -1L;
   protected int _numTestsPerEvictionRun = 3;
   protected long _minEvictableIdleTimeMillis = 1800000L;
   protected KeyedPoolableObjectFactory _factory = null;
   protected boolean _lifo = true;

   public GenericKeyedObjectPoolFactory(KeyedPoolableObjectFactory factory) {
      this(factory, 8, (byte)1, -1L, 8, false, false, -1L, 3, 1800000L, false);
   }

   public GenericKeyedObjectPoolFactory(KeyedPoolableObjectFactory factory, GenericKeyedObjectPool.Config config) throws NullPointerException {
      this(
         factory,
         config.maxActive,
         config.whenExhaustedAction,
         config.maxWait,
         config.maxIdle,
         config.maxTotal,
         config.minIdle,
         config.testOnBorrow,
         config.testOnReturn,
         config.timeBetweenEvictionRunsMillis,
         config.numTestsPerEvictionRun,
         config.minEvictableIdleTimeMillis,
         config.testWhileIdle,
         config.lifo
      );
   }

   public GenericKeyedObjectPoolFactory(KeyedPoolableObjectFactory factory, int maxActive) {
      this(factory, maxActive, (byte)1, -1L, 8, -1, false, false, -1L, 3, 1800000L, false);
   }

   public GenericKeyedObjectPoolFactory(KeyedPoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait) {
      this(factory, maxActive, whenExhaustedAction, maxWait, 8, -1, false, false, -1L, 3, 1800000L, false);
   }

   public GenericKeyedObjectPoolFactory(
      KeyedPoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, boolean testOnBorrow, boolean testOnReturn
   ) {
      this(factory, maxActive, whenExhaustedAction, maxWait, 8, -1, testOnBorrow, testOnReturn, -1L, 3, 1800000L, false);
   }

   public GenericKeyedObjectPoolFactory(KeyedPoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle) {
      this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, -1, false, false, -1L, 3, 1800000L, false);
   }

   public GenericKeyedObjectPoolFactory(KeyedPoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle, int maxTotal) {
      this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, maxTotal, false, false, -1L, 3, 1800000L, false);
   }

   public GenericKeyedObjectPoolFactory(
      KeyedPoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle, boolean testOnBorrow, boolean testOnReturn
   ) {
      this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, -1, testOnBorrow, testOnReturn, -1L, 3, 1800000L, false);
   }

   public GenericKeyedObjectPoolFactory(
      KeyedPoolableObjectFactory factory,
      int maxActive,
      byte whenExhaustedAction,
      long maxWait,
      int maxIdle,
      boolean testOnBorrow,
      boolean testOnReturn,
      long timeBetweenEvictionRunsMillis,
      int numTestsPerEvictionRun,
      long minEvictableIdleTimeMillis,
      boolean testWhileIdle
   ) {
      this(
         factory,
         maxActive,
         whenExhaustedAction,
         maxWait,
         maxIdle,
         -1,
         testOnBorrow,
         testOnReturn,
         timeBetweenEvictionRunsMillis,
         numTestsPerEvictionRun,
         minEvictableIdleTimeMillis,
         testWhileIdle
      );
   }

   public GenericKeyedObjectPoolFactory(
      KeyedPoolableObjectFactory factory,
      int maxActive,
      byte whenExhaustedAction,
      long maxWait,
      int maxIdle,
      int maxTotal,
      boolean testOnBorrow,
      boolean testOnReturn,
      long timeBetweenEvictionRunsMillis,
      int numTestsPerEvictionRun,
      long minEvictableIdleTimeMillis,
      boolean testWhileIdle
   ) {
      this(
         factory,
         maxActive,
         whenExhaustedAction,
         maxWait,
         maxIdle,
         maxTotal,
         0,
         testOnBorrow,
         testOnReturn,
         timeBetweenEvictionRunsMillis,
         numTestsPerEvictionRun,
         minEvictableIdleTimeMillis,
         testWhileIdle
      );
   }

   public GenericKeyedObjectPoolFactory(
      KeyedPoolableObjectFactory factory,
      int maxActive,
      byte whenExhaustedAction,
      long maxWait,
      int maxIdle,
      int maxTotal,
      int minIdle,
      boolean testOnBorrow,
      boolean testOnReturn,
      long timeBetweenEvictionRunsMillis,
      int numTestsPerEvictionRun,
      long minEvictableIdleTimeMillis,
      boolean testWhileIdle
   ) {
      this(
         factory,
         maxActive,
         whenExhaustedAction,
         maxWait,
         maxIdle,
         maxTotal,
         minIdle,
         testOnBorrow,
         testOnReturn,
         timeBetweenEvictionRunsMillis,
         numTestsPerEvictionRun,
         minEvictableIdleTimeMillis,
         testWhileIdle,
         true
      );
   }

   public GenericKeyedObjectPoolFactory(
      KeyedPoolableObjectFactory factory,
      int maxActive,
      byte whenExhaustedAction,
      long maxWait,
      int maxIdle,
      int maxTotal,
      int minIdle,
      boolean testOnBorrow,
      boolean testOnReturn,
      long timeBetweenEvictionRunsMillis,
      int numTestsPerEvictionRun,
      long minEvictableIdleTimeMillis,
      boolean testWhileIdle,
      boolean lifo
   ) {
      this._maxIdle = maxIdle;
      this._maxActive = maxActive;
      this._maxTotal = maxTotal;
      this._minIdle = minIdle;
      this._maxWait = maxWait;
      this._whenExhaustedAction = whenExhaustedAction;
      this._testOnBorrow = testOnBorrow;
      this._testOnReturn = testOnReturn;
      this._testWhileIdle = testWhileIdle;
      this._timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
      this._numTestsPerEvictionRun = numTestsPerEvictionRun;
      this._minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
      this._factory = factory;
      this._lifo = lifo;
   }

   public KeyedObjectPool createPool() {
      return new GenericKeyedObjectPool(
         this._factory,
         this._maxActive,
         this._whenExhaustedAction,
         this._maxWait,
         this._maxIdle,
         this._maxTotal,
         this._minIdle,
         this._testOnBorrow,
         this._testOnReturn,
         this._timeBetweenEvictionRunsMillis,
         this._numTestsPerEvictionRun,
         this._minEvictableIdleTimeMillis,
         this._testWhileIdle,
         this._lifo
      );
   }
}
