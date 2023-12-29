package org.apache.commons.pool.impl;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.ObjectPoolFactory;
import org.apache.commons.pool.PoolableObjectFactory;

public class GenericObjectPoolFactory implements ObjectPoolFactory {
   protected int _maxIdle = 8;
   protected int _minIdle = 0;
   protected int _maxActive = 8;
   protected long _maxWait = -1L;
   protected byte _whenExhaustedAction = 1;
   protected boolean _testOnBorrow = false;
   protected boolean _testOnReturn = false;
   protected boolean _testWhileIdle = false;
   protected long _timeBetweenEvictionRunsMillis = -1L;
   protected int _numTestsPerEvictionRun = 3;
   protected long _minEvictableIdleTimeMillis = 1800000L;
   protected long _softMinEvictableIdleTimeMillis = 1800000L;
   protected boolean _lifo = true;
   protected PoolableObjectFactory _factory = null;

   public GenericObjectPoolFactory(PoolableObjectFactory factory) {
      this(factory, 8, (byte)1, -1L, 8, 0, false, false, -1L, 3, 1800000L, false);
   }

   public GenericObjectPoolFactory(PoolableObjectFactory factory, GenericObjectPool.Config config) throws NullPointerException {
      this(
         factory,
         config.maxActive,
         config.whenExhaustedAction,
         config.maxWait,
         config.maxIdle,
         config.minIdle,
         config.testOnBorrow,
         config.testOnReturn,
         config.timeBetweenEvictionRunsMillis,
         config.numTestsPerEvictionRun,
         config.minEvictableIdleTimeMillis,
         config.testWhileIdle,
         config.softMinEvictableIdleTimeMillis,
         config.lifo
      );
   }

   public GenericObjectPoolFactory(PoolableObjectFactory factory, int maxActive) {
      this(factory, maxActive, (byte)1, -1L, 8, 0, false, false, -1L, 3, 1800000L, false);
   }

   public GenericObjectPoolFactory(PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait) {
      this(factory, maxActive, whenExhaustedAction, maxWait, 8, 0, false, false, -1L, 3, 1800000L, false);
   }

   public GenericObjectPoolFactory(
      PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, boolean testOnBorrow, boolean testOnReturn
   ) {
      this(factory, maxActive, whenExhaustedAction, maxWait, 8, 0, testOnBorrow, testOnReturn, -1L, 3, 1800000L, false);
   }

   public GenericObjectPoolFactory(PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle) {
      this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, 0, false, false, -1L, 3, 1800000L, false);
   }

   public GenericObjectPoolFactory(
      PoolableObjectFactory factory, int maxActive, byte whenExhaustedAction, long maxWait, int maxIdle, boolean testOnBorrow, boolean testOnReturn
   ) {
      this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, 0, testOnBorrow, testOnReturn, -1L, 3, 1800000L, false);
   }

   public GenericObjectPoolFactory(
      PoolableObjectFactory factory,
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
         0,
         testOnBorrow,
         testOnReturn,
         timeBetweenEvictionRunsMillis,
         numTestsPerEvictionRun,
         minEvictableIdleTimeMillis,
         testWhileIdle,
         -1L
      );
   }

   public GenericObjectPoolFactory(
      PoolableObjectFactory factory,
      int maxActive,
      byte whenExhaustedAction,
      long maxWait,
      int maxIdle,
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
         minIdle,
         testOnBorrow,
         testOnReturn,
         timeBetweenEvictionRunsMillis,
         numTestsPerEvictionRun,
         minEvictableIdleTimeMillis,
         testWhileIdle,
         -1L
      );
   }

   public GenericObjectPoolFactory(
      PoolableObjectFactory factory,
      int maxActive,
      byte whenExhaustedAction,
      long maxWait,
      int maxIdle,
      int minIdle,
      boolean testOnBorrow,
      boolean testOnReturn,
      long timeBetweenEvictionRunsMillis,
      int numTestsPerEvictionRun,
      long minEvictableIdleTimeMillis,
      boolean testWhileIdle,
      long softMinEvictableIdleTimeMillis
   ) {
      this(
         factory,
         maxActive,
         whenExhaustedAction,
         maxWait,
         maxIdle,
         minIdle,
         testOnBorrow,
         testOnReturn,
         timeBetweenEvictionRunsMillis,
         numTestsPerEvictionRun,
         minEvictableIdleTimeMillis,
         testWhileIdle,
         softMinEvictableIdleTimeMillis,
         true
      );
   }

   public GenericObjectPoolFactory(
      PoolableObjectFactory factory,
      int maxActive,
      byte whenExhaustedAction,
      long maxWait,
      int maxIdle,
      int minIdle,
      boolean testOnBorrow,
      boolean testOnReturn,
      long timeBetweenEvictionRunsMillis,
      int numTestsPerEvictionRun,
      long minEvictableIdleTimeMillis,
      boolean testWhileIdle,
      long softMinEvictableIdleTimeMillis,
      boolean lifo
   ) {
      this._maxIdle = maxIdle;
      this._minIdle = minIdle;
      this._maxActive = maxActive;
      this._maxWait = maxWait;
      this._whenExhaustedAction = whenExhaustedAction;
      this._testOnBorrow = testOnBorrow;
      this._testOnReturn = testOnReturn;
      this._testWhileIdle = testWhileIdle;
      this._timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
      this._numTestsPerEvictionRun = numTestsPerEvictionRun;
      this._minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
      this._softMinEvictableIdleTimeMillis = softMinEvictableIdleTimeMillis;
      this._lifo = lifo;
      this._factory = factory;
   }

   public ObjectPool createPool() {
      return new GenericObjectPool(
         this._factory,
         this._maxActive,
         this._whenExhaustedAction,
         this._maxWait,
         this._maxIdle,
         this._minIdle,
         this._testOnBorrow,
         this._testOnReturn,
         this._timeBetweenEvictionRunsMillis,
         this._numTestsPerEvictionRun,
         this._minEvictableIdleTimeMillis,
         this._testWhileIdle,
         this._softMinEvictableIdleTimeMillis,
         this._lifo
      );
   }
}
