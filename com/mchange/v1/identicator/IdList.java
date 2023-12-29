package com.mchange.v1.identicator;

import com.mchange.v1.util.IteratorUtils;
import com.mchange.v1.util.ListUtils;
import com.mchange.v1.util.WrapperIterator;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class IdList implements List {
   Identicator id;
   List inner;

   public IdList(Identicator var1, List var2) {
      this.id = var1;
      this.inner = var2;
   }

   @Override
   public int size() {
      return this.inner.size();
   }

   @Override
   public boolean isEmpty() {
      return this.inner.isEmpty();
   }

   @Override
   public boolean contains(Object var1) {
      new StrongIdHashKey(var1, this.id);
      return this.inner.contains(var1);
   }

   @Override
   public Iterator iterator() {
      return new WrapperIterator(this.inner.iterator(), true) {
         @Override
         protected Object transformObject(Object var1) {
            if (var1 instanceof IdHashKey) {
               IdHashKey var2 = (IdHashKey)var1;
               return var2.getKeyObj();
            } else {
               return var1;
            }
         }
      };
   }

   @Override
   public Object[] toArray() {
      return this.toArray(new Object[this.size()]);
   }

   @Override
   public Object[] toArray(Object[] var1) {
      return IteratorUtils.toArray(this.iterator(), this.size(), var1);
   }

   @Override
   public boolean add(Object var1) {
      return this.inner.add(new StrongIdHashKey(var1, this.id));
   }

   @Override
   public boolean remove(Object var1) {
      return this.inner.remove(new StrongIdHashKey(var1, this.id));
   }

   @Override
   public boolean containsAll(Collection var1) {
      Iterator var2 = var1.iterator();

      while(var2.hasNext()) {
         StrongIdHashKey var3 = new StrongIdHashKey(var2.next(), this.id);
         if (!this.inner.contains(var3)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean addAll(Collection var1) {
      Iterator var2 = var1.iterator();

      boolean var3;
      StrongIdHashKey var4;
      for(var3 = false; var2.hasNext(); var3 |= this.inner.add(var4)) {
         var4 = new StrongIdHashKey(var2.next(), this.id);
      }

      return var3;
   }

   @Override
   public boolean addAll(int var1, Collection var2) {
      for(Iterator var3 = var2.iterator(); var3.hasNext(); ++var1) {
         StrongIdHashKey var4 = new StrongIdHashKey(var3.next(), this.id);
         this.inner.add(var1, var4);
      }

      return var2.size() > 0;
   }

   @Override
   public boolean removeAll(Collection var1) {
      Iterator var2 = var1.iterator();

      boolean var3;
      StrongIdHashKey var4;
      for(var3 = false; var2.hasNext(); var3 |= this.inner.remove(var4)) {
         var4 = new StrongIdHashKey(var2.next(), this.id);
      }

      return var3;
   }

   @Override
   public boolean retainAll(Collection var1) {
      Iterator var2 = this.inner.iterator();
      boolean var3 = false;

      while(var2.hasNext()) {
         IdHashKey var4 = (IdHashKey)var2.next();
         if (!var1.contains(var4.getKeyObj())) {
            this.inner.remove(var4);
            var3 = true;
         }
      }

      return var3;
   }

   @Override
   public void clear() {
      this.inner.clear();
   }

   @Override
   public boolean equals(Object var1) {
      return var1 instanceof List ? ListUtils.equivalent(this, (List)var1) : false;
   }

   @Override
   public int hashCode() {
      return ListUtils.hashContents(this);
   }

   @Override
   public Object get(int var1) {
      return ((IdHashKey)this.inner.get(var1)).getKeyObj();
   }

   @Override
   public Object set(int var1, Object var2) {
      IdHashKey var3 = this.inner.set(var1, new StrongIdHashKey(var2, this.id));
      return var3.getKeyObj();
   }

   @Override
   public void add(int var1, Object var2) {
      this.inner.add(var1, new StrongIdHashKey(var2, this.id));
   }

   @Override
   public Object remove(int var1) {
      IdHashKey var2 = (IdHashKey)this.inner.remove(var1);
      return var2 == null ? null : var2.getKeyObj();
   }

   @Override
   public int indexOf(Object var1) {
      return this.inner.indexOf(new StrongIdHashKey(var1, this.id));
   }

   @Override
   public int lastIndexOf(Object var1) {
      return this.inner.lastIndexOf(new StrongIdHashKey(var1, this.id));
   }

   @Override
   public ListIterator listIterator() {
      return new LinkedList(this).listIterator();
   }

   @Override
   public ListIterator listIterator(int var1) {
      return new LinkedList(this).listIterator(var1);
   }

   @Override
   public List subList(int var1, int var2) {
      return new IdList(this.id, this.inner.subList(var1, var2));
   }
}
