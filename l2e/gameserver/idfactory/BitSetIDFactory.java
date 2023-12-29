package l2e.gameserver.idfactory;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import l2e.commons.util.PrimeFinder;
import l2e.gameserver.ThreadPoolManager;

public class BitSetIDFactory extends IdFactory {
   private BitSet _freeIds;
   private AtomicInteger _freeIdCount;
   private AtomicInteger _nextFreeId;

   protected BitSetIDFactory() {
      synchronized(BitSetIDFactory.class) {
         ThreadPoolManager.getInstance().scheduleAtFixedRate(new BitSetIDFactory.BitSetCapacityCheck(), 30000L, 30000L);
         this.initialize();
      }

      this._log.info(this.getClass().getSimpleName() + ": " + this._freeIds.size() + " id's available.");
   }

   public void initialize() {
      try {
         this._freeIds = new BitSet(PrimeFinder.nextPrime(100000));
         this._freeIds.clear();
         this._freeIdCount = new AtomicInteger(1879048191);

         for(int usedObjectId : this.extractUsedObjectIDTable()) {
            int objectID = usedObjectId - 268435456;
            if (objectID < 0) {
               this._log.warning(this.getClass().getSimpleName() + ": Object ID " + usedObjectId + " in DB is less than minimum ID of " + 268435456);
            } else {
               this._freeIds.set(usedObjectId - 268435456);
               this._freeIdCount.decrementAndGet();
            }
         }

         this._nextFreeId = new AtomicInteger(this._freeIds.nextClearBit(0));
         this._initialized = true;
      } catch (Exception var6) {
         this._initialized = false;
         this._log.severe(this.getClass().getSimpleName() + ": Could not be initialized properly: " + var6.getMessage());
      }
   }

   @Override
   public synchronized void releaseId(int objectID) {
      if (objectID - 268435456 > -1) {
         this._freeIds.clear(objectID - 268435456);
         this._freeIdCount.incrementAndGet();
      } else {
         this._log.warning(this.getClass().getSimpleName() + ": Release objectID " + objectID + " failed (< " + 268435456 + ")");
      }
   }

   @Override
   public synchronized int getNextId() {
      int newID = this._nextFreeId.get();
      this._freeIds.set(newID);
      this._freeIdCount.decrementAndGet();
      int nextFree = this._freeIds.nextClearBit(newID);
      if (nextFree < 0) {
         nextFree = this._freeIds.nextClearBit(0);
      }

      if (nextFree < 0) {
         if (this._freeIds.size() >= 1879048191) {
            throw new NullPointerException("Ran out of valid Id's.");
         }

         this.increaseBitSetCapacity();
      }

      this._nextFreeId.set(nextFree);
      return newID + 268435456;
   }

   @Override
   public synchronized int size() {
      return this._freeIdCount.get();
   }

   protected synchronized int usedIdCount() {
      return this.size() - 268435456;
   }

   protected synchronized boolean reachingBitSetCapacity() {
      return PrimeFinder.nextPrime(this.usedIdCount() * 11 / 10) > this._freeIds.size();
   }

   protected synchronized void increaseBitSetCapacity() {
      BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(this.usedIdCount() * 11 / 10));
      newBitSet.or(this._freeIds);
      this._freeIds = newBitSet;
   }

   protected class BitSetCapacityCheck implements Runnable {
      @Override
      public void run() {
         synchronized(BitSetIDFactory.this) {
            if (BitSetIDFactory.this.reachingBitSetCapacity()) {
               BitSetIDFactory.this.increaseBitSetCapacity();
            }
         }
      }
   }
}
