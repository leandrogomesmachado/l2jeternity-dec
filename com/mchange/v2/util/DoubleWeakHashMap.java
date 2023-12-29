package com.mchange.v2.util;

import com.mchange.v1.util.AbstractMapEntry;
import com.mchange.v1.util.WrapperIterator;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class DoubleWeakHashMap implements Map {
   HashMap inner;
   ReferenceQueue keyQ = new ReferenceQueue();
   ReferenceQueue valQ = new ReferenceQueue();
   DoubleWeakHashMap.CheckKeyHolder holder = new DoubleWeakHashMap.CheckKeyHolder();
   Set userKeySet = null;
   Collection valuesCollection = null;

   public DoubleWeakHashMap() {
      this.inner = new HashMap();
   }

   public DoubleWeakHashMap(int var1) {
      this.inner = new HashMap(var1);
   }

   public DoubleWeakHashMap(int var1, float var2) {
      this.inner = new HashMap(var1, var2);
   }

   public DoubleWeakHashMap(Map var1) {
      this();
      this.putAll(var1);
   }

   public void cleanCleared() {
      DoubleWeakHashMap.WKey var1;
      while((var1 = (DoubleWeakHashMap.WKey)this.keyQ.poll()) != null) {
         this.inner.remove(var1);
      }

      DoubleWeakHashMap.WVal var2;
      while((var2 = (DoubleWeakHashMap.WVal)this.valQ.poll()) != null) {
         this.inner.remove(var2.getWKey());
      }
   }

   @Override
   public void clear() {
      this.cleanCleared();
      this.inner.clear();
   }

   @Override
   public boolean containsKey(Object var1) {
      this.cleanCleared();

      boolean var2;
      try {
         var2 = this.inner.containsKey(this.holder.set(var1));
      } finally {
         this.holder.clear();
      }

      return var2;
   }

   @Override
   public boolean containsValue(Object var1) {
      for(DoubleWeakHashMap.WVal var3 : this.inner.values()) {
         if (var1.equals(var3.get())) {
            return true;
         }
      }

      return false;
   }

   @Override
   public Set entrySet() {
      this.cleanCleared();
      return new DoubleWeakHashMap.UserEntrySet();
   }

   @Override
   public Object get(Object var1) {
      Object var3;
      try {
         this.cleanCleared();
         DoubleWeakHashMap.WVal var2 = (DoubleWeakHashMap.WVal)this.inner.get(this.holder.set(var1));
         var3 = var2 == null ? null : var2.get();
      } finally {
         this.holder.clear();
      }

      return var3;
   }

   @Override
   public boolean isEmpty() {
      this.cleanCleared();
      return this.inner.isEmpty();
   }

   @Override
   public Set keySet() {
      this.cleanCleared();
      if (this.userKeySet == null) {
         this.userKeySet = new DoubleWeakHashMap.UserKeySet();
      }

      return this.userKeySet;
   }

   @Override
   public Object put(Object var1, Object var2) {
      this.cleanCleared();
      DoubleWeakHashMap.WVal var3 = this.doPut(var1, var2);
      return var3 != null ? var3.get() : null;
   }

   private DoubleWeakHashMap.WVal doPut(Object var1, Object var2) {
      DoubleWeakHashMap.WKey var3 = new DoubleWeakHashMap.WKey(var1, this.keyQ);
      DoubleWeakHashMap.WVal var4 = new DoubleWeakHashMap.WVal(var3, var2, this.valQ);
      return this.inner.put(var3, var4);
   }

   @Override
   public void putAll(Map var1) {
      this.cleanCleared();

      for(Entry var3 : var1.entrySet()) {
         this.doPut(var3.getKey(), var3.getValue());
      }
   }

   @Override
   public Object remove(Object var1) {
      Object var3;
      try {
         this.cleanCleared();
         DoubleWeakHashMap.WVal var2 = (DoubleWeakHashMap.WVal)this.inner.remove(this.holder.set(var1));
         var3 = var2 == null ? null : var2.get();
      } finally {
         this.holder.clear();
      }

      return var3;
   }

   @Override
   public int size() {
      this.cleanCleared();
      return this.inner.size();
   }

   @Override
   public Collection values() {
      if (this.valuesCollection == null) {
         this.valuesCollection = new DoubleWeakHashMap.ValuesCollection();
      }

      return this.valuesCollection;
   }

   static final class CheckKeyHolder {
      Object checkKey;

      public Object get() {
         return this.checkKey;
      }

      public DoubleWeakHashMap.CheckKeyHolder set(Object var1) {
         assert this.checkKey == null : "Illegal concurrenct use of DoubleWeakHashMap!";

         this.checkKey = var1;
         return this;
      }

      public void clear() {
         this.checkKey = null;
      }

      @Override
      public int hashCode() {
         return this.checkKey.hashCode();
      }

      @Override
      public boolean equals(Object var1) {
         assert this.get() != null : "CheckedKeyHolder should never do an equality check while its value is null.";

         if (this == var1) {
            return true;
         } else if (var1 instanceof DoubleWeakHashMap.CheckKeyHolder) {
            return this.get().equals(((DoubleWeakHashMap.CheckKeyHolder)var1).get());
         } else {
            return var1 instanceof DoubleWeakHashMap.WKey ? this.get().equals(((DoubleWeakHashMap.WKey)var1).get()) : false;
         }
      }
   }

   class UserEntry extends AbstractMapEntry {
      Entry innerEntry;
      Object key;
      Object val;

      UserEntry(Entry var2, Object var3, Object var4) {
         this.innerEntry = var2;
         this.key = var3;
         this.val = var4;
      }

      @Override
      public final Object getKey() {
         return this.key;
      }

      @Override
      public final Object getValue() {
         return this.val;
      }

      @Override
      public final Object setValue(Object var1) {
         return this.innerEntry.setValue(new DoubleWeakHashMap.WVal((DoubleWeakHashMap.WKey)this.innerEntry.getKey(), var1, DoubleWeakHashMap.this.valQ));
      }
   }

   private final class UserEntrySet extends AbstractSet {
      private UserEntrySet() {
      }

      private Set innerEntrySet() {
         DoubleWeakHashMap.this.cleanCleared();
         return DoubleWeakHashMap.this.inner.entrySet();
      }

      @Override
      public Iterator iterator() {
         return new WrapperIterator(this.innerEntrySet().iterator(), true) {
            @Override
            protected Object transformObject(Object var1) {
               Entry var2 = (Entry)var1;
               Object var3 = ((DoubleWeakHashMap.WKey)var2.getKey()).get();
               Object var4 = ((DoubleWeakHashMap.WVal)var2.getValue()).get();
               return var3 != null && var4 != null ? DoubleWeakHashMap.this.new UserEntry(var2, var3, var4) : WrapperIterator.SKIP_TOKEN;
            }
         };
      }

      @Override
      public int size() {
         return this.innerEntrySet().size();
      }
   }

   class UserKeySet implements Set {
      @Override
      public boolean add(Object var1) {
         DoubleWeakHashMap.this.cleanCleared();
         throw new UnsupportedOperationException("You cannot add to a Map's key set.");
      }

      @Override
      public boolean addAll(Collection var1) {
         DoubleWeakHashMap.this.cleanCleared();
         throw new UnsupportedOperationException("You cannot add to a Map's key set.");
      }

      @Override
      public void clear() {
         DoubleWeakHashMap.this.clear();
      }

      @Override
      public boolean contains(Object var1) {
         return DoubleWeakHashMap.this.containsKey(var1);
      }

      @Override
      public boolean containsAll(Collection var1) {
         Iterator var2 = var1.iterator();

         while(var2.hasNext()) {
            if (!this.contains(var2.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean isEmpty() {
         return DoubleWeakHashMap.this.isEmpty();
      }

      @Override
      public Iterator iterator() {
         DoubleWeakHashMap.this.cleanCleared();
         return new WrapperIterator(DoubleWeakHashMap.this.inner.keySet().iterator(), true) {
            @Override
            protected Object transformObject(Object var1) {
               Object var2 = ((DoubleWeakHashMap.WKey)var1).get();
               return var2 == null ? WrapperIterator.SKIP_TOKEN : var2;
            }
         };
      }

      @Override
      public boolean remove(Object var1) {
         return DoubleWeakHashMap.this.remove(var1) != null;
      }

      @Override
      public boolean removeAll(Collection var1) {
         boolean var2 = false;
         Iterator var3 = var1.iterator();

         while(var3.hasNext()) {
            var2 |= this.remove(var3.next());
         }

         return var2;
      }

      @Override
      public boolean retainAll(Collection var1) {
         boolean var2 = false;
         Iterator var3 = this.iterator();

         while(var3.hasNext()) {
            if (!var1.contains(var3.next())) {
               var3.remove();
               var2 = true;
            }
         }

         return var2;
      }

      @Override
      public int size() {
         return DoubleWeakHashMap.this.size();
      }

      @Override
      public Object[] toArray() {
         DoubleWeakHashMap.this.cleanCleared();
         return new HashSet(this).toArray();
      }

      @Override
      public Object[] toArray(Object[] var1) {
         DoubleWeakHashMap.this.cleanCleared();
         return new HashSet(this).toArray(var1);
      }
   }

   class ValuesCollection implements Collection {
      @Override
      public boolean add(Object var1) {
         DoubleWeakHashMap.this.cleanCleared();
         throw new UnsupportedOperationException("DoubleWeakHashMap does not support adding to its values Collection.");
      }

      @Override
      public boolean addAll(Collection var1) {
         DoubleWeakHashMap.this.cleanCleared();
         throw new UnsupportedOperationException("DoubleWeakHashMap does not support adding to its values Collection.");
      }

      @Override
      public void clear() {
         DoubleWeakHashMap.this.clear();
      }

      @Override
      public boolean contains(Object var1) {
         return DoubleWeakHashMap.this.containsValue(var1);
      }

      @Override
      public boolean containsAll(Collection var1) {
         Iterator var2 = var1.iterator();

         while(var2.hasNext()) {
            if (!this.contains(var2.next())) {
               return false;
            }
         }

         return true;
      }

      @Override
      public boolean isEmpty() {
         return DoubleWeakHashMap.this.isEmpty();
      }

      @Override
      public Iterator iterator() {
         return new WrapperIterator(DoubleWeakHashMap.this.inner.values().iterator(), true) {
            @Override
            protected Object transformObject(Object var1) {
               Object var2 = ((DoubleWeakHashMap.WVal)var1).get();
               return var2 == null ? WrapperIterator.SKIP_TOKEN : var2;
            }
         };
      }

      @Override
      public boolean remove(Object var1) {
         DoubleWeakHashMap.this.cleanCleared();
         return this.removeValue(var1);
      }

      @Override
      public boolean removeAll(Collection var1) {
         DoubleWeakHashMap.this.cleanCleared();
         boolean var2 = false;
         Iterator var3 = var1.iterator();

         while(var3.hasNext()) {
            var2 |= this.removeValue(var3.next());
         }

         return var2;
      }

      @Override
      public boolean retainAll(Collection var1) {
         DoubleWeakHashMap.this.cleanCleared();
         return this.retainValues(var1);
      }

      @Override
      public int size() {
         return DoubleWeakHashMap.this.size();
      }

      @Override
      public Object[] toArray() {
         DoubleWeakHashMap.this.cleanCleared();
         return new ArrayList(this).toArray();
      }

      @Override
      public Object[] toArray(Object[] var1) {
         DoubleWeakHashMap.this.cleanCleared();
         return new ArrayList(this).toArray(var1);
      }

      private boolean removeValue(Object var1) {
         boolean var2 = false;
         Iterator var3 = DoubleWeakHashMap.this.inner.values().iterator();

         while(var3.hasNext()) {
            DoubleWeakHashMap.WVal var4 = (DoubleWeakHashMap.WVal)var3.next();
            if (var1.equals(var4.get())) {
               var3.remove();
               var2 = true;
            }
         }

         return var2;
      }

      private boolean retainValues(Collection var1) {
         boolean var2 = false;
         Iterator var3 = DoubleWeakHashMap.this.inner.values().iterator();

         while(var3.hasNext()) {
            DoubleWeakHashMap.WVal var4 = (DoubleWeakHashMap.WVal)var3.next();
            if (!var1.contains(var4.get())) {
               var3.remove();
               var2 = true;
            }
         }

         return var2;
      }
   }

   static final class WKey extends WeakReference {
      int cachedHash;

      WKey(Object var1, ReferenceQueue var2) {
         super(var1, var2);
         this.cachedHash = var1.hashCode();
      }

      @Override
      public int hashCode() {
         return this.cachedHash;
      }

      @Override
      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else if (var1 instanceof DoubleWeakHashMap.WKey) {
            DoubleWeakHashMap.WKey var5 = (DoubleWeakHashMap.WKey)var1;
            Object var6 = this.get();
            Object var7 = var5.get();
            return var6 != null && var7 != null ? var6.equals(var7) : false;
         } else if (var1 instanceof DoubleWeakHashMap.CheckKeyHolder) {
            DoubleWeakHashMap.CheckKeyHolder var2 = (DoubleWeakHashMap.CheckKeyHolder)var1;
            Object var3 = this.get();
            Object var4 = var2.get();
            return var3 != null && var4 != null ? var3.equals(var4) : false;
         } else {
            return false;
         }
      }
   }

   static final class WVal extends WeakReference {
      DoubleWeakHashMap.WKey key;

      WVal(DoubleWeakHashMap.WKey var1, Object var2, ReferenceQueue var3) {
         super(var2, var3);
         this.key = var1;
      }

      public DoubleWeakHashMap.WKey getWKey() {
         return this.key;
      }
   }
}
