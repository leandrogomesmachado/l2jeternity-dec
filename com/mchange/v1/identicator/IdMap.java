package com.mchange.v1.identicator;

import com.mchange.v1.util.AbstractMapEntry;
import com.mchange.v1.util.SimpleMapEntry;
import com.mchange.v1.util.WrapperIterator;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

abstract class IdMap extends AbstractMap implements Map {
   Map inner;
   Identicator id;

   protected IdMap(Map var1, Identicator var2) {
      this.inner = var1;
      this.id = var2;
   }

   @Override
   public Object put(Object var1, Object var2) {
      return this.inner.put(this.createIdKey(var1), var2);
   }

   @Override
   public boolean containsKey(Object var1) {
      return this.inner.containsKey(this.createIdKey(var1));
   }

   @Override
   public Object get(Object var1) {
      return this.inner.get(this.createIdKey(var1));
   }

   @Override
   public Object remove(Object var1) {
      return this.inner.remove(this.createIdKey(var1));
   }

   protected Object removeIdHashKey(IdHashKey var1) {
      return this.inner.remove(var1);
   }

   @Override
   public Set entrySet() {
      return new IdMap.UserEntrySet();
   }

   protected final Set internalEntrySet() {
      return this.inner.entrySet();
   }

   protected abstract IdHashKey createIdKey(Object var1);

   protected final Entry createIdEntry(Object var1, Object var2) {
      return new SimpleMapEntry(this.createIdKey(var1), var2);
   }

   protected final Entry createIdEntry(Entry var1) {
      return this.createIdEntry(var1.getKey(), var1.getValue());
   }

   protected static class UserEntry extends AbstractMapEntry {
      private Entry innerEntry;

      UserEntry(Entry var1) {
         this.innerEntry = var1;
      }

      @Override
      public final Object getKey() {
         return ((IdHashKey)this.innerEntry.getKey()).getKeyObj();
      }

      @Override
      public final Object getValue() {
         return this.innerEntry.getValue();
      }

      @Override
      public final Object setValue(Object var1) {
         return this.innerEntry.setValue(var1);
      }
   }

   private final class UserEntrySet extends AbstractSet {
      Set innerEntries = IdMap.this.inner.entrySet();

      private UserEntrySet() {
      }

      @Override
      public Iterator iterator() {
         return new WrapperIterator(this.innerEntries.iterator(), true) {
            @Override
            protected Object transformObject(Object var1) {
               return new IdMap.UserEntry((Entry)var1);
            }
         };
      }

      @Override
      public int size() {
         return this.innerEntries.size();
      }

      @Override
      public boolean contains(Object var1) {
         if (var1 instanceof Entry) {
            Entry var2 = (Entry)var1;
            return this.innerEntries.contains(IdMap.this.createIdEntry(var2));
         } else {
            return false;
         }
      }

      @Override
      public boolean remove(Object var1) {
         if (var1 instanceof Entry) {
            Entry var2 = (Entry)var1;
            return this.innerEntries.remove(IdMap.this.createIdEntry(var2));
         } else {
            return false;
         }
      }

      @Override
      public void clear() {
         IdMap.this.inner.clear();
      }
   }
}
