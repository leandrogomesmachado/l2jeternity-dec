package l2e.commons.util.concurrent;

public interface ReadWriteLockable {
   void writeLock();

   void writeUnlock();

   void readLock();

   void readUnlock();
}
