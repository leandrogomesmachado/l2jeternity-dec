package com.mchange.v1.cachedstore;

import com.mchange.v1.util.WrapperIterator;
import java.lang.ref.ReferenceQueue;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

class ManualCleanupSoftSet extends AbstractSet implements Vacuumable {
   HashSet inner = new HashSet();
   ReferenceQueue queue = new ReferenceQueue();

   @Override
   public Iterator iterator() {
      return new WrapperIterator(this.inner.iterator(), true) {
         @Override
         protected Object transformObject(Object var1) {
            SoftKey var2 = (SoftKey)var1;
            Object var3 = var2.get();
            return var3 == null ? SKIP_TOKEN : var3;
         }
      };
   }

   @Override
   public int size() {
      return this.inner.size();
   }

   @Override
   public boolean contains(Object var1) {
      return this.inner.contains(new SoftKey(var1, null));
   }

   private ArrayList toArrayList() {
      ArrayList var1 = new ArrayList(this.size());
      Iterator var2 = this.iterator();

      while(var2.hasNext()) {
         var1.add(var2.next());
      }

      return var1;
   }

   @Override
   public Object[] toArray() {
      return this.toArrayList().toArray();
   }

   @Override
   public Object[] toArray(Object[] var1) {
      return this.toArrayList().toArray(var1);
   }

   @Override
   public boolean add(Object var1) {
      return this.inner.add(new SoftKey(var1, this.queue));
   }

   @Override
   public boolean remove(Object var1) {
      return this.inner.remove(new SoftKey(var1, null));
   }

   @Override
   public void clear() {
      this.inner.clear();
   }

   @Override
   public void vacuum() throws CachedStoreException {
      SoftKey var1;
      while((var1 = (SoftKey)this.queue.poll()) != null) {
         this.inner.remove(var1);
      }
   }
}
