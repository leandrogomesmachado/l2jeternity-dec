package gnu.trove.set.hash;

import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import java.io.IOException;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class TLinkedHashSet<E> extends THashSet<E> {
   TIntList order;

   public TLinkedHashSet() {
   }

   public TLinkedHashSet(int initialCapacity) {
      super(initialCapacity);
   }

   public TLinkedHashSet(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   public TLinkedHashSet(Collection<? extends E> es) {
      super(es);
   }

   @Override
   public int setUp(int initialCapacity) {
      this.order = new TIntArrayList(initialCapacity) {
         @Override
         public void ensureCapacity(int capacity) {
            if (capacity > this._data.length) {
               int newCap = Math.max(TLinkedHashSet.this._set.length, capacity);
               int[] tmp = new int[newCap];
               System.arraycopy(this._data, 0, tmp, 0, this._data.length);
               this._data = tmp;
            }
         }
      };
      return super.setUp(initialCapacity);
   }

   @Override
   public void clear() {
      super.clear();
      this.order.clear();
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder("{");
      boolean first = true;

      for(Iterator<E> it = this.iterator(); it.hasNext(); buf.append(it.next())) {
         if (first) {
            first = false;
         } else {
            buf.append(", ");
         }
      }

      buf.append("}");
      return buf.toString();
   }

   @Override
   public boolean add(E obj) {
      int index = this.insertKey(obj);
      if (index < 0) {
         return false;
      } else if (!this.order.add(index)) {
         throw new IllegalStateException("Order not changed after insert");
      } else {
         this.postInsertHook(this.consumeFreeSlot);
         return true;
      }
   }

   @Override
   protected void removeAt(int index) {
      this.order.remove(index);
      super.removeAt(index);
   }

   @Override
   protected void rehash(int newCapacity) {
      TIntLinkedList oldOrder = new TIntLinkedList(this.order);
      int oldSize = this.size();
      Object[] oldSet = this._set;
      this.order.clear();
      this._set = new Object[newCapacity];
      Arrays.fill(this._set, FREE);
      TIntIterator iterator = oldOrder.iterator();

      while(iterator.hasNext()) {
         int i = iterator.next();
         E o = (E)oldSet[i];
         if (o == FREE || o == REMOVED) {
            throw new IllegalStateException("Iterating over empty location while rehashing");
         }

         if (o != FREE && o != REMOVED) {
            int index = this.insertKey(o);
            if (index < 0) {
               this.throwObjectContractViolation(this._set[-index - 1], o, this.size(), oldSize, oldSet);
            }

            if (!this.order.add(index)) {
               throw new IllegalStateException("Order not changed after insert");
            }
         }
      }
   }

   @Override
   protected void writeEntries(ObjectOutput out) throws IOException {
      TLinkedHashSet<E>.WriteProcedure writeProcedure = new TLinkedHashSet.WriteProcedure(out);
      if (!this.order.forEach(writeProcedure)) {
         throw writeProcedure.getIoException();
      }
   }

   @Override
   public TObjectHashIterator<E> iterator() {
      return new TObjectHashIterator<E>(this) {
         TIntIterator localIterator = TLinkedHashSet.this.order.iterator();
         int lastIndex;

         @Override
         public E next() {
            this.lastIndex = this.localIterator.next();
            return (E)this.objectAtIndex(this.lastIndex);
         }

         @Override
         public boolean hasNext() {
            return this.localIterator.hasNext();
         }

         @Override
         public void remove() {
            this.localIterator.remove();

            try {
               this._hash.tempDisableAutoCompaction();
               TLinkedHashSet.this.removeAt(this.lastIndex);
            } finally {
               this._hash.reenableAutoCompaction(false);
            }
         }
      };
   }

   @Override
   public boolean forEach(TObjectProcedure<? super E> procedure) {
      TLinkedHashSet<E>.ForEachProcedure forEachProcedure = new TLinkedHashSet.ForEachProcedure(this._set, procedure);
      return this.order.forEach(forEachProcedure);
   }

   class ForEachProcedure implements TIntProcedure {
      boolean changed = false;
      final Object[] set;
      final TObjectProcedure<? super E> procedure;

      public ForEachProcedure(Object[] set, TObjectProcedure<? super E> procedure) {
         this.set = set;
         this.procedure = procedure;
      }

      @Override
      public boolean execute(int value) {
         return this.procedure.execute((E)this.set[value]);
      }
   }

   class WriteProcedure implements TIntProcedure {
      final ObjectOutput output;
      IOException ioException;

      WriteProcedure(ObjectOutput output) {
         this.output = output;
      }

      public IOException getIoException() {
         return this.ioException;
      }

      @Override
      public boolean execute(int value) {
         try {
            this.output.writeObject(TLinkedHashSet.this._set[value]);
            return true;
         } catch (IOException var3) {
            this.ioException = var3;
            return false;
         }
      }
   }
}
