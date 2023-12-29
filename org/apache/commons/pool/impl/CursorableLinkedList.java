package org.apache.commons.pool.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

class CursorableLinkedList implements List, Serializable {
   private static final long serialVersionUID = 8836393098519411393L;
   protected transient int _size = 0;
   protected transient CursorableLinkedList.Listable _head = new CursorableLinkedList.Listable(null, null, null);
   protected transient int _modCount = 0;
   protected transient List _cursors = new ArrayList();

   public boolean add(Object o) {
      this.insertListable(this._head.prev(), null, o);
      return true;
   }

   public void add(int index, Object element) {
      if (index == this._size) {
         this.add(element);
      } else {
         if (index < 0 || index > this._size) {
            throw new IndexOutOfBoundsException(index + " < 0 or " + index + " > " + this._size);
         }

         CursorableLinkedList.Listable succ = this.isEmpty() ? null : this.getListableAt(index);
         CursorableLinkedList.Listable pred = null == succ ? null : succ.prev();
         this.insertListable(pred, succ, element);
      }
   }

   public boolean addAll(Collection c) {
      if (c.isEmpty()) {
         return false;
      } else {
         Iterator it = c.iterator();

         while(it.hasNext()) {
            this.insertListable(this._head.prev(), null, it.next());
         }

         return true;
      }
   }

   public boolean addAll(int index, Collection c) {
      if (c.isEmpty()) {
         return false;
      } else if (this._size != index && this._size != 0) {
         CursorableLinkedList.Listable succ = this.getListableAt(index);
         CursorableLinkedList.Listable pred = null == succ ? null : succ.prev();
         Iterator it = c.iterator();

         while(it.hasNext()) {
            pred = this.insertListable(pred, succ, it.next());
         }

         return true;
      } else {
         return this.addAll(c);
      }
   }

   public boolean addFirst(Object o) {
      this.insertListable(null, this._head.next(), o);
      return true;
   }

   public boolean addLast(Object o) {
      this.insertListable(this._head.prev(), null, o);
      return true;
   }

   public void clear() {
      Iterator it = this.iterator();

      while(it.hasNext()) {
         it.next();
         it.remove();
      }
   }

   public boolean contains(Object o) {
      CursorableLinkedList.Listable elt = this._head.next();

      for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.prev(); elt = elt.next()) {
         if (null == o && null == elt.value() || o != null && o.equals(elt.value())) {
            return true;
         }

         past = elt;
      }

      return false;
   }

   public boolean containsAll(Collection c) {
      Iterator it = c.iterator();

      while(it.hasNext()) {
         if (!this.contains(it.next())) {
            return false;
         }
      }

      return true;
   }

   public CursorableLinkedList.Cursor cursor() {
      return new CursorableLinkedList.Cursor(0);
   }

   public CursorableLinkedList.Cursor cursor(int i) {
      return new CursorableLinkedList.Cursor(i);
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof List)) {
         return false;
      } else {
         Iterator it = ((List)o).listIterator();
         CursorableLinkedList.Listable elt = this._head.next();

         for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.prev(); elt = elt.next()) {
            if (!it.hasNext() || (null == elt.value() ? null != it.next() : !elt.value().equals(it.next()))) {
               return false;
            }

            past = elt;
         }

         return !it.hasNext();
      }
   }

   public Object get(int index) {
      return this.getListableAt(index).value();
   }

   public Object getFirst() {
      try {
         return this._head.next().value();
      } catch (NullPointerException var2) {
         throw new NoSuchElementException();
      }
   }

   public Object getLast() {
      try {
         return this._head.prev().value();
      } catch (NullPointerException var2) {
         throw new NoSuchElementException();
      }
   }

   public int hashCode() {
      int hash = 1;
      CursorableLinkedList.Listable elt = this._head.next();

      for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.prev(); elt = elt.next()) {
         hash = 31 * hash + (null == elt.value() ? 0 : elt.value().hashCode());
         past = elt;
      }

      return hash;
   }

   public int indexOf(Object o) {
      int ndx = 0;
      if (null == o) {
         CursorableLinkedList.Listable elt = this._head.next();

         for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.prev(); elt = elt.next()) {
            if (null == elt.value()) {
               return ndx;
            }

            ++ndx;
            past = elt;
         }
      } else {
         CursorableLinkedList.Listable elt = this._head.next();

         for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.prev(); elt = elt.next()) {
            if (o.equals(elt.value())) {
               return ndx;
            }

            ++ndx;
            past = elt;
         }
      }

      return -1;
   }

   public boolean isEmpty() {
      return 0 == this._size;
   }

   public Iterator iterator() {
      return this.listIterator(0);
   }

   public int lastIndexOf(Object o) {
      int ndx = this._size - 1;
      if (null == o) {
         CursorableLinkedList.Listable elt = this._head.prev();

         for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.next(); elt = elt.prev()) {
            if (null == elt.value()) {
               return ndx;
            }

            --ndx;
            past = elt;
         }
      } else {
         CursorableLinkedList.Listable elt = this._head.prev();

         for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.next(); elt = elt.prev()) {
            if (o.equals(elt.value())) {
               return ndx;
            }

            --ndx;
            past = elt;
         }
      }

      return -1;
   }

   public ListIterator listIterator() {
      return this.listIterator(0);
   }

   public ListIterator listIterator(int index) {
      if (index >= 0 && index <= this._size) {
         return new CursorableLinkedList.ListIter(index);
      } else {
         throw new IndexOutOfBoundsException(index + " < 0 or > " + this._size);
      }
   }

   public boolean remove(Object o) {
      CursorableLinkedList.Listable elt = this._head.next();

      for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.prev(); elt = elt.next()) {
         if (null == o && null == elt.value()) {
            this.removeListable(elt);
            return true;
         }

         if (o != null && o.equals(elt.value())) {
            this.removeListable(elt);
            return true;
         }

         past = elt;
      }

      return false;
   }

   public Object remove(int index) {
      CursorableLinkedList.Listable elt = this.getListableAt(index);
      Object ret = elt.value();
      this.removeListable(elt);
      return ret;
   }

   public boolean removeAll(Collection c) {
      if (0 != c.size() && 0 != this._size) {
         boolean changed = false;
         Iterator it = this.iterator();

         while(it.hasNext()) {
            if (c.contains(it.next())) {
               it.remove();
               changed = true;
            }
         }

         return changed;
      } else {
         return false;
      }
   }

   public Object removeFirst() {
      if (this._head.next() != null) {
         Object val = this._head.next().value();
         this.removeListable(this._head.next());
         return val;
      } else {
         throw new NoSuchElementException();
      }
   }

   public Object removeLast() {
      if (this._head.prev() != null) {
         Object val = this._head.prev().value();
         this.removeListable(this._head.prev());
         return val;
      } else {
         throw new NoSuchElementException();
      }
   }

   public boolean retainAll(Collection c) {
      boolean changed = false;
      Iterator it = this.iterator();

      while(it.hasNext()) {
         if (!c.contains(it.next())) {
            it.remove();
            changed = true;
         }
      }

      return changed;
   }

   public Object set(int index, Object element) {
      CursorableLinkedList.Listable elt = this.getListableAt(index);
      Object val = elt.setValue(element);
      this.broadcastListableChanged(elt);
      return val;
   }

   public int size() {
      return this._size;
   }

   public Object[] toArray() {
      Object[] array = new Object[this._size];
      int i = 0;
      CursorableLinkedList.Listable elt = this._head.next();

      for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.prev(); elt = elt.next()) {
         array[i++] = elt.value();
         past = elt;
      }

      return array;
   }

   public Object[] toArray(Object[] a) {
      if (a.length < this._size) {
         a = (Object[])Array.newInstance(a.getClass().getComponentType(), this._size);
      }

      int i = 0;
      CursorableLinkedList.Listable elt = this._head.next();

      for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.prev(); elt = elt.next()) {
         a[i++] = elt.value();
         past = elt;
      }

      if (a.length > this._size) {
         a[this._size] = null;
      }

      return a;
   }

   public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("[");
      CursorableLinkedList.Listable elt = this._head.next();

      for(CursorableLinkedList.Listable past = null; null != elt && past != this._head.prev(); elt = elt.next()) {
         if (this._head.next() != elt) {
            buf.append(", ");
         }

         buf.append(elt.value());
         past = elt;
      }

      buf.append("]");
      return buf.toString();
   }

   public List subList(int i, int j) {
      if (i < 0 || j > this._size || i > j) {
         throw new IndexOutOfBoundsException();
      } else {
         return (List)(i == 0 && j == this._size ? this : new CursorableSubList(this, i, j));
      }
   }

   protected CursorableLinkedList.Listable insertListable(CursorableLinkedList.Listable before, CursorableLinkedList.Listable after, Object value) {
      ++this._modCount;
      ++this._size;
      CursorableLinkedList.Listable elt = new CursorableLinkedList.Listable(before, after, value);
      if (null != before) {
         before.setNext(elt);
      } else {
         this._head.setNext(elt);
      }

      if (null != after) {
         after.setPrev(elt);
      } else {
         this._head.setPrev(elt);
      }

      this.broadcastListableInserted(elt);
      return elt;
   }

   protected void removeListable(CursorableLinkedList.Listable elt) {
      ++this._modCount;
      --this._size;
      if (this._head.next() == elt) {
         this._head.setNext(elt.next());
      }

      if (null != elt.next()) {
         elt.next().setPrev(elt.prev());
      }

      if (this._head.prev() == elt) {
         this._head.setPrev(elt.prev());
      }

      if (null != elt.prev()) {
         elt.prev().setNext(elt.next());
      }

      this.broadcastListableRemoved(elt);
   }

   protected CursorableLinkedList.Listable getListableAt(int index) {
      if (index < 0 || index >= this._size) {
         throw new IndexOutOfBoundsException(index + " < 0 or " + index + " >= " + this._size);
      } else if (index <= this._size / 2) {
         CursorableLinkedList.Listable elt = this._head.next();

         for(int i = 0; i < index; ++i) {
            elt = elt.next();
         }

         return elt;
      } else {
         CursorableLinkedList.Listable elt = this._head.prev();

         for(int i = this._size - 1; i > index; --i) {
            elt = elt.prev();
         }

         return elt;
      }
   }

   protected void registerCursor(CursorableLinkedList.Cursor cur) {
      Iterator it = this._cursors.iterator();

      while(it.hasNext()) {
         WeakReference ref = (WeakReference)it.next();
         if (ref.get() == null) {
            it.remove();
         }
      }

      this._cursors.add(new WeakReference<>(cur));
   }

   protected void unregisterCursor(CursorableLinkedList.Cursor cur) {
      Iterator it = this._cursors.iterator();

      while(it.hasNext()) {
         WeakReference ref = (WeakReference)it.next();
         CursorableLinkedList.Cursor cursor = (CursorableLinkedList.Cursor)ref.get();
         if (cursor == null) {
            it.remove();
         } else if (cursor == cur) {
            ref.clear();
            it.remove();
            break;
         }
      }
   }

   protected void invalidateCursors() {
      for(Iterator it = this._cursors.iterator(); it.hasNext(); it.remove()) {
         WeakReference ref = (WeakReference)it.next();
         CursorableLinkedList.Cursor cursor = (CursorableLinkedList.Cursor)ref.get();
         if (cursor != null) {
            cursor.invalidate();
            ref.clear();
         }
      }
   }

   protected void broadcastListableChanged(CursorableLinkedList.Listable elt) {
      Iterator it = this._cursors.iterator();

      while(it.hasNext()) {
         WeakReference ref = (WeakReference)it.next();
         CursorableLinkedList.Cursor cursor = (CursorableLinkedList.Cursor)ref.get();
         if (cursor == null) {
            it.remove();
         } else {
            cursor.listableChanged(elt);
         }
      }
   }

   protected void broadcastListableRemoved(CursorableLinkedList.Listable elt) {
      Iterator it = this._cursors.iterator();

      while(it.hasNext()) {
         WeakReference ref = (WeakReference)it.next();
         CursorableLinkedList.Cursor cursor = (CursorableLinkedList.Cursor)ref.get();
         if (cursor == null) {
            it.remove();
         } else {
            cursor.listableRemoved(elt);
         }
      }
   }

   protected void broadcastListableInserted(CursorableLinkedList.Listable elt) {
      Iterator it = this._cursors.iterator();

      while(it.hasNext()) {
         WeakReference ref = (WeakReference)it.next();
         CursorableLinkedList.Cursor cursor = (CursorableLinkedList.Cursor)ref.get();
         if (cursor == null) {
            it.remove();
         } else {
            cursor.listableInserted(elt);
         }
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.defaultWriteObject();
      out.writeInt(this._size);

      for(CursorableLinkedList.Listable cur = this._head.next(); cur != null; cur = cur.next()) {
         out.writeObject(cur.value());
      }
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      this._size = 0;
      this._modCount = 0;
      this._cursors = new ArrayList();
      this._head = new CursorableLinkedList.Listable(null, null, null);
      int size = in.readInt();

      for(int i = 0; i < size; ++i) {
         this.add(in.readObject());
      }
   }

   public class Cursor extends CursorableLinkedList.ListIter implements ListIterator {
      boolean _valid = false;

      Cursor(int index) {
         super(index);
         this._valid = true;
         CursorableLinkedList.this.registerCursor(this);
      }

      public int previousIndex() {
         throw new UnsupportedOperationException();
      }

      public int nextIndex() {
         throw new UnsupportedOperationException();
      }

      public void add(Object o) {
         this.checkForComod();
         CursorableLinkedList.Listable elt = CursorableLinkedList.this.insertListable(this._cur.prev(), this._cur.next(), o);
         this._cur.setPrev(elt);
         this._cur.setNext(elt.next());
         this._lastReturned = null;
         ++this._nextIndex;
         ++this._expectedModCount;
      }

      protected void listableRemoved(CursorableLinkedList.Listable elt) {
         if (null == CursorableLinkedList.this._head.prev()) {
            this._cur.setNext(null);
         } else if (this._cur.next() == elt) {
            this._cur.setNext(elt.next());
         }

         if (null == CursorableLinkedList.this._head.next()) {
            this._cur.setPrev(null);
         } else if (this._cur.prev() == elt) {
            this._cur.setPrev(elt.prev());
         }

         if (this._lastReturned == elt) {
            this._lastReturned = null;
         }
      }

      protected void listableInserted(CursorableLinkedList.Listable elt) {
         if (null == this._cur.next() && null == this._cur.prev()) {
            this._cur.setNext(elt);
         } else if (this._cur.prev() == elt.prev()) {
            this._cur.setNext(elt);
         }

         if (this._cur.next() == elt.next()) {
            this._cur.setPrev(elt);
         }

         if (this._lastReturned == elt) {
            this._lastReturned = null;
         }
      }

      protected void listableChanged(CursorableLinkedList.Listable elt) {
         if (this._lastReturned == elt) {
            this._lastReturned = null;
         }
      }

      protected void checkForComod() {
         if (!this._valid) {
            throw new ConcurrentModificationException();
         }
      }

      protected void invalidate() {
         this._valid = false;
      }

      public void close() {
         if (this._valid) {
            this._valid = false;
            CursorableLinkedList.this.unregisterCursor(this);
         }
      }
   }

   class ListIter implements ListIterator {
      CursorableLinkedList.Listable _cur = null;
      CursorableLinkedList.Listable _lastReturned = null;
      int _expectedModCount = CursorableLinkedList.this._modCount;
      int _nextIndex = 0;

      ListIter(int index) {
         if (index == 0) {
            this._cur = new CursorableLinkedList.Listable(null, CursorableLinkedList.this._head.next(), null);
            this._nextIndex = 0;
         } else if (index == CursorableLinkedList.this._size) {
            this._cur = new CursorableLinkedList.Listable(CursorableLinkedList.this._head.prev(), null, null);
            this._nextIndex = CursorableLinkedList.this._size;
         } else {
            CursorableLinkedList.Listable temp = CursorableLinkedList.this.getListableAt(index);
            this._cur = new CursorableLinkedList.Listable(temp.prev(), temp, null);
            this._nextIndex = index;
         }
      }

      public Object previous() {
         this.checkForComod();
         if (!this.hasPrevious()) {
            throw new NoSuchElementException();
         } else {
            Object ret = this._cur.prev().value();
            this._lastReturned = this._cur.prev();
            this._cur.setNext(this._cur.prev());
            this._cur.setPrev(this._cur.prev().prev());
            --this._nextIndex;
            return ret;
         }
      }

      public boolean hasNext() {
         this.checkForComod();
         return null != this._cur.next() && this._cur.prev() != CursorableLinkedList.this._head.prev();
      }

      public Object next() {
         this.checkForComod();
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            Object ret = this._cur.next().value();
            this._lastReturned = this._cur.next();
            this._cur.setPrev(this._cur.next());
            this._cur.setNext(this._cur.next().next());
            ++this._nextIndex;
            return ret;
         }
      }

      public int previousIndex() {
         this.checkForComod();
         return !this.hasPrevious() ? -1 : this._nextIndex - 1;
      }

      public boolean hasPrevious() {
         this.checkForComod();
         return null != this._cur.prev() && this._cur.next() != CursorableLinkedList.this._head.next();
      }

      public void set(Object o) {
         this.checkForComod();

         try {
            this._lastReturned.setValue(o);
         } catch (NullPointerException var3) {
            throw new IllegalStateException();
         }
      }

      public int nextIndex() {
         this.checkForComod();
         return !this.hasNext() ? CursorableLinkedList.this.size() : this._nextIndex;
      }

      public void remove() {
         this.checkForComod();
         if (null == this._lastReturned) {
            throw new IllegalStateException();
         } else {
            this._cur.setNext(this._lastReturned == CursorableLinkedList.this._head.prev() ? null : this._lastReturned.next());
            this._cur.setPrev(this._lastReturned == CursorableLinkedList.this._head.next() ? null : this._lastReturned.prev());
            CursorableLinkedList.this.removeListable(this._lastReturned);
            this._lastReturned = null;
            --this._nextIndex;
            ++this._expectedModCount;
         }
      }

      public void add(Object o) {
         this.checkForComod();
         this._cur.setPrev(CursorableLinkedList.this.insertListable(this._cur.prev(), this._cur.next(), o));
         this._lastReturned = null;
         ++this._nextIndex;
         ++this._expectedModCount;
      }

      protected void checkForComod() {
         if (this._expectedModCount != CursorableLinkedList.this._modCount) {
            throw new ConcurrentModificationException();
         }
      }
   }

   static class Listable implements Serializable {
      private CursorableLinkedList.Listable _prev = null;
      private CursorableLinkedList.Listable _next = null;
      private Object _val = null;

      Listable(CursorableLinkedList.Listable prev, CursorableLinkedList.Listable next, Object val) {
         this._prev = prev;
         this._next = next;
         this._val = val;
      }

      CursorableLinkedList.Listable next() {
         return this._next;
      }

      CursorableLinkedList.Listable prev() {
         return this._prev;
      }

      Object value() {
         return this._val;
      }

      void setNext(CursorableLinkedList.Listable next) {
         this._next = next;
      }

      void setPrev(CursorableLinkedList.Listable prev) {
         this._prev = prev;
      }

      Object setValue(Object val) {
         Object temp = this._val;
         this._val = val;
         return temp;
      }
   }
}
