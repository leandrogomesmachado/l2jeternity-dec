package org.eclipse.jdt.internal.compiler.apt.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ManyToMany<T1, T2> {
   private final Map<T1, Set<T2>> _forward = new HashMap<>();
   private final Map<T2, Set<T1>> _reverse = new HashMap<>();
   private boolean _dirty = false;

   public synchronized boolean clear() {
      boolean hadContent = !this._forward.isEmpty() || !this._reverse.isEmpty();
      this._reverse.clear();
      this._forward.clear();
      this._dirty |= hadContent;
      return hadContent;
   }

   public synchronized void clearDirtyBit() {
      this._dirty = false;
   }

   public synchronized boolean containsKey(T1 key) {
      return this._forward.containsKey(key);
   }

   public synchronized boolean containsKeyValuePair(T1 key, T2 value) {
      Set<T2> values = this._forward.get(key);
      return values == null ? false : values.contains(value);
   }

   public synchronized boolean containsValue(T2 value) {
      return this._reverse.containsKey(value);
   }

   public synchronized Set<T1> getKeys(T2 value) {
      Set<T1> keys = this._reverse.get(value);
      return (Set<T1>)(keys == null ? Collections.emptySet() : new HashSet<>(keys));
   }

   public synchronized Set<T2> getValues(T1 key) {
      Set<T2> values = this._forward.get(key);
      return (Set<T2>)(values == null ? Collections.emptySet() : new HashSet<>(values));
   }

   public synchronized Set<T1> getKeySet() {
      Set<T1> keys = new HashSet<>(this._forward.keySet());
      return keys;
   }

   public synchronized Set<T2> getValueSet() {
      Set<T2> values = new HashSet<>(this._reverse.keySet());
      return values;
   }

   public synchronized boolean isDirty() {
      return this._dirty;
   }

   public synchronized boolean keyHasOtherValues(T1 key, T2 value) {
      Set<T2> values = this._forward.get(key);
      if (values == null) {
         return false;
      } else {
         int size = values.size();
         if (size == 0) {
            return false;
         } else if (size > 1) {
            return true;
         } else {
            return !values.contains(value);
         }
      }
   }

   public synchronized boolean put(T1 key, T2 value) {
      Set<T2> values = this._forward.get(key);
      if (values == null) {
         values = new HashSet<>();
         this._forward.put(key, values);
      }

      boolean added = values.add(value);
      this._dirty |= added;
      Set<T1> keys = this._reverse.get(value);
      if (keys == null) {
         keys = new HashSet<>();
         this._reverse.put(value, keys);
      }

      keys.add(key);

      assert this.checkIntegrity();

      return added;
   }

   public synchronized boolean remove(T1 key, T2 value) {
      Set<T2> values = this._forward.get(key);
      if (values == null) {
         assert this.checkIntegrity();

         return false;
      } else {
         boolean removed = values.remove(value);
         if (values.isEmpty()) {
            this._forward.remove(key);
         }

         if (removed) {
            this._dirty = true;
            Set<T1> keys = this._reverse.get(value);
            keys.remove(key);
            if (keys.isEmpty()) {
               this._reverse.remove(value);
            }
         }

         assert this.checkIntegrity();

         return removed;
      }
   }

   public synchronized boolean removeKey(T1 key) {
      Set<T2> values = this._forward.get(key);
      if (values == null) {
         assert this.checkIntegrity();

         return false;
      } else {
         for(T2 value : values) {
            Set<T1> keys = this._reverse.get(value);
            if (keys != null) {
               keys.remove(key);
               if (keys.isEmpty()) {
                  this._reverse.remove(value);
               }
            }
         }

         this._forward.remove(key);
         this._dirty = true;

         assert this.checkIntegrity();

         return true;
      }
   }

   public synchronized boolean removeValue(T2 value) {
      Set<T1> keys = this._reverse.get(value);
      if (keys == null) {
         assert this.checkIntegrity();

         return false;
      } else {
         for(T1 key : keys) {
            Set<T2> values = this._forward.get(key);
            if (values != null) {
               values.remove(value);
               if (values.isEmpty()) {
                  this._forward.remove(key);
               }
            }
         }

         this._reverse.remove(value);
         this._dirty = true;

         assert this.checkIntegrity();

         return true;
      }
   }

   public synchronized boolean valueHasOtherKeys(T2 value, T1 key) {
      Set<T1> keys = this._reverse.get(key);
      if (keys == null) {
         return false;
      } else {
         int size = keys.size();
         if (size == 0) {
            return false;
         } else if (size > 1) {
            return true;
         } else {
            return !keys.contains(key);
         }
      }
   }

   private boolean checkIntegrity() {
      for(Entry<T1, Set<T2>> entry : this._forward.entrySet()) {
         Set<T2> values = entry.getValue();
         if (values.isEmpty()) {
            throw new IllegalStateException("Integrity compromised: forward map contains an empty set");
         }

         for(T2 value : values) {
            Set<T1> keys = this._reverse.get(value);
            if (keys == null || !keys.contains(entry.getKey())) {
               throw new IllegalStateException("Integrity compromised: forward map contains an entry missing from reverse map: " + value);
            }
         }
      }

      for(Entry<T2, Set<T1>> entry : this._reverse.entrySet()) {
         Set<T1> keys = entry.getValue();
         if (keys.isEmpty()) {
            throw new IllegalStateException("Integrity compromised: reverse map contains an empty set");
         }

         for(T1 key : keys) {
            Set<T2> values = this._forward.get(key);
            if (values == null || !values.contains(entry.getKey())) {
               throw new IllegalStateException("Integrity compromised: reverse map contains an entry missing from forward map: " + key);
            }
         }
      }

      return true;
   }
}
