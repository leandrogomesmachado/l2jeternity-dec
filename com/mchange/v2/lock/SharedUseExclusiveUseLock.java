package com.mchange.v2.lock;

public interface SharedUseExclusiveUseLock {
   void acquireShared() throws InterruptedException;

   void relinquishShared();

   void acquireExclusive() throws InterruptedException;

   void relinquishExclusive();
}
