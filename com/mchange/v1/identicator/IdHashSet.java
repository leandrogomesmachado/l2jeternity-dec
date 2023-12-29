package com.mchange.v1.identicator;

import com.mchange.v1.util.WrapperIterator;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IdHashSet extends AbstractSet implements Set {
   HashSet inner;
   Identicator id;

   private IdHashSet(HashSet var1, Identicator var2) {
      this.inner = var1;
      this.id = var2;
   }

   public IdHashSet(Identicator var1) {
      this(new HashSet(), var1);
   }

   public IdHashSet(Collection var1, Identicator var2) {
      this(new HashSet(2 * var1.size()), var2);
   }

   public IdHashSet(int var1, float var2, Identicator var3) {
      this(new HashSet(var1, var2), var3);
   }

   public IdHashSet(int var1, Identicator var2) {
      this(new HashSet(var1, 0.75F), var2);
   }

   @Override
   public Iterator iterator() {
      return new WrapperIterator(this.inner.iterator(), true) {
         @Override
         protected Object transformObject(Object var1) {
            IdHashKey var2 = (IdHashKey)var1;
            return var2.getKeyObj();
         }
      };
   }

   @Override
   public int size() {
      return this.inner.size();
   }

   @Override
   public boolean contains(Object var1) {
      return this.inner.contains(this.createKey(var1));
   }

   @Override
   public boolean add(Object var1) {
      return this.inner.add(this.createKey(var1));
   }

   @Override
   public boolean remove(Object var1) {
      return this.inner.remove(this.createKey(var1));
   }

   @Override
   public void clear() {
      this.inner.clear();
   }

   private IdHashKey createKey(Object var1) {
      return new StrongIdHashKey(var1, this.id);
   }
}
