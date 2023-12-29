package com.mchange.v1.identicator;

import com.mchange.v1.util.WrapperIterator;
import java.lang.ref.ReferenceQueue;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class IdWeakHashMap extends IdMap implements Map {
   ReferenceQueue rq = new ReferenceQueue();

   public IdWeakHashMap(Identicator var1) {
      super(new HashMap(), var1);
   }

   @Override
   public int size() {
      this.cleanCleared();
      return super.size();
   }

   @Override
   public boolean isEmpty() {
      boolean var1;
      try {
         var1 = super.isEmpty();
      } finally {
         this.cleanCleared();
      }

      return var1;
   }

   @Override
   public boolean containsKey(Object var1) {
      boolean var2;
      try {
         var2 = super.containsKey(var1);
      } finally {
         this.cleanCleared();
      }

      return var2;
   }

   @Override
   public boolean containsValue(Object var1) {
      boolean var2;
      try {
         var2 = super.containsValue(var1);
      } finally {
         this.cleanCleared();
      }

      return var2;
   }

   @Override
   public Object get(Object var1) {
      Object var2;
      try {
         var2 = super.get(var1);
      } finally {
         this.cleanCleared();
      }

      return var2;
   }

   @Override
   public Object put(Object var1, Object var2) {
      Object var3;
      try {
         var3 = super.put(var1, var2);
      } finally {
         this.cleanCleared();
      }

      return var3;
   }

   @Override
   public Object remove(Object var1) {
      Object var2;
      try {
         var2 = super.remove(var1);
      } finally {
         this.cleanCleared();
      }

      return var2;
   }

   @Override
   public void putAll(Map var1) {
      try {
         super.putAll(var1);
      } finally {
         this.cleanCleared();
      }
   }

   @Override
   public void clear() {
      try {
         super.clear();
      } finally {
         this.cleanCleared();
      }
   }

   @Override
   public Set keySet() {
      Set var1;
      try {
         var1 = super.keySet();
      } finally {
         this.cleanCleared();
      }

      return var1;
   }

   @Override
   public Collection values() {
      Collection var1;
      try {
         var1 = super.values();
      } finally {
         this.cleanCleared();
      }

      return var1;
   }

   @Override
   public Set entrySet() {
      IdWeakHashMap.WeakUserEntrySet var1;
      try {
         var1 = new IdWeakHashMap.WeakUserEntrySet();
      } finally {
         this.cleanCleared();
      }

      return var1;
   }

   @Override
   public boolean equals(Object var1) {
      boolean var2;
      try {
         var2 = super.equals(var1);
      } finally {
         this.cleanCleared();
      }

      return var2;
   }

   @Override
   public int hashCode() {
      int var1;
      try {
         var1 = super.hashCode();
      } finally {
         this.cleanCleared();
      }

      return var1;
   }

   @Override
   protected IdHashKey createIdKey(Object var1) {
      return new WeakIdHashKey(var1, this.id, this.rq);
   }

   private void cleanCleared() {
      WeakIdHashKey.Ref var1;
      while((var1 = (WeakIdHashKey.Ref)this.rq.poll()) != null) {
         this.removeIdHashKey(var1.getKey());
      }
   }

   private final class WeakUserEntrySet extends AbstractSet {
      Set innerEntries = IdWeakHashMap.this.internalEntrySet();

      private WeakUserEntrySet() {
      }

      @Override
      public Iterator iterator() {
         WrapperIterator var1;
         try {
            var1 = new WrapperIterator(this.innerEntries.iterator(), true) {
               @Override
               protected Object transformObject(Object var1) {
                  Entry var2 = (Entry)var1;
                  final Object var3 = ((IdHashKey)var2.getKey()).getKeyObj();
                  return var3 == null ? WrapperIterator.SKIP_TOKEN : new IdMap.UserEntry(var2) {
                     Object preventRefClear = var3;
                  };
               }
            };
         } finally {
            IdWeakHashMap.this.cleanCleared();
         }

         return var1;
      }

      @Override
      public int size() {
         IdWeakHashMap.this.cleanCleared();
         return this.innerEntries.size();
      }

      @Override
      public boolean contains(Object var1) {
         boolean var3;
         try {
            if (!(var1 instanceof Entry)) {
               return false;
            }

            Entry var2 = (Entry)var1;
            var3 = this.innerEntries.contains(IdWeakHashMap.this.createIdEntry(var2));
         } finally {
            IdWeakHashMap.this.cleanCleared();
         }

         return var3;
      }

      @Override
      public boolean remove(Object var1) {
         boolean var3;
         try {
            if (!(var1 instanceof Entry)) {
               return false;
            }

            Entry var2 = (Entry)var1;
            var3 = this.innerEntries.remove(IdWeakHashMap.this.createIdEntry(var2));
         } finally {
            IdWeakHashMap.this.cleanCleared();
         }

         return var3;
      }

      @Override
      public void clear() {
         try {
            IdWeakHashMap.this.inner.clear();
         } finally {
            IdWeakHashMap.this.cleanCleared();
         }
      }
   }
}
