package gnu.trove.list.linked;

import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.procedure.TIntProcedure;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Random;

public class TIntLinkedList implements TIntList, Externalizable {
   int no_entry_value;
   int size;
   TIntLinkedList.TIntLink head = null;
   TIntLinkedList.TIntLink tail = this.head;

   public TIntLinkedList() {
   }

   public TIntLinkedList(int no_entry_value) {
      this.no_entry_value = no_entry_value;
   }

   public TIntLinkedList(TIntList list) {
      this.no_entry_value = list.getNoEntryValue();
      TIntIterator iterator = list.iterator();

      while(iterator.hasNext()) {
         int next = iterator.next();
         this.add(next);
      }
   }

   @Override
   public int getNoEntryValue() {
      return this.no_entry_value;
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public boolean isEmpty() {
      return this.size() == 0;
   }

   @Override
   public boolean add(int val) {
      TIntLinkedList.TIntLink l = new TIntLinkedList.TIntLink(val);
      if (no(this.head)) {
         this.head = l;
         this.tail = l;
      } else {
         l.setPrevious(this.tail);
         this.tail.setNext(l);
         this.tail = l;
      }

      ++this.size;
      return true;
   }

   @Override
   public void add(int[] vals) {
      for(int val : vals) {
         this.add(val);
      }
   }

   @Override
   public void add(int[] vals, int offset, int length) {
      for(int i = 0; i < length; ++i) {
         int val = vals[offset + i];
         this.add(val);
      }
   }

   @Override
   public void insert(int offset, int value) {
      TIntLinkedList tmp = new TIntLinkedList();
      tmp.add(value);
      this.insert(offset, tmp);
   }

   @Override
   public void insert(int offset, int[] values) {
      this.insert(offset, link(values, 0, values.length));
   }

   @Override
   public void insert(int offset, int[] values, int valOffset, int len) {
      this.insert(offset, link(values, valOffset, len));
   }

   void insert(int offset, TIntLinkedList tmp) {
      TIntLinkedList.TIntLink l = this.getLinkAt(offset);
      this.size += tmp.size;
      if (l == this.head) {
         tmp.tail.setNext(this.head);
         this.head.setPrevious(tmp.tail);
         this.head = tmp.head;
      } else {
         if (no(l)) {
            if (this.size == 0) {
               this.head = tmp.head;
               this.tail = tmp.tail;
            } else {
               this.tail.setNext(tmp.head);
               tmp.head.setPrevious(this.tail);
               this.tail = tmp.tail;
            }
         } else {
            TIntLinkedList.TIntLink prev = l.getPrevious();
            l.getPrevious().setNext(tmp.head);
            tmp.tail.setNext(l);
            l.setPrevious(tmp.tail);
            tmp.head.setPrevious(prev);
         }
      }
   }

   static TIntLinkedList link(int[] values, int valOffset, int len) {
      TIntLinkedList ret = new TIntLinkedList();

      for(int i = 0; i < len; ++i) {
         ret.add(values[valOffset + i]);
      }

      return ret;
   }

   @Override
   public int get(int offset) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TIntLinkedList.TIntLink l = this.getLinkAt(offset);
         return no(l) ? this.no_entry_value : l.getValue();
      }
   }

   public TIntLinkedList.TIntLink getLinkAt(int offset) {
      if (offset >= this.size()) {
         return null;
      } else {
         return offset <= this.size() >>> 1 ? getLink(this.head, 0, offset, true) : getLink(this.tail, this.size() - 1, offset, false);
      }
   }

   private static TIntLinkedList.TIntLink getLink(TIntLinkedList.TIntLink l, int idx, int offset) {
      return getLink(l, idx, offset, true);
   }

   private static TIntLinkedList.TIntLink getLink(TIntLinkedList.TIntLink l, int idx, int offset, boolean next) {
      for(int i = idx; got(l); l = next ? l.getNext() : l.getPrevious()) {
         if (i == offset) {
            return l;
         }

         i += next ? 1 : -1;
      }

      return null;
   }

   @Override
   public int set(int offset, int val) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TIntLinkedList.TIntLink l = this.getLinkAt(offset);
         if (no(l)) {
            throw new IndexOutOfBoundsException("at offset " + offset);
         } else {
            int prev = l.getValue();
            l.setValue(val);
            return prev;
         }
      }
   }

   @Override
   public void set(int offset, int[] values) {
      this.set(offset, values, 0, values.length);
   }

   @Override
   public void set(int offset, int[] values, int valOffset, int length) {
      for(int i = 0; i < length; ++i) {
         int value = values[valOffset + i];
         this.set(offset + i, value);
      }
   }

   @Override
   public int replace(int offset, int val) {
      return this.set(offset, val);
   }

   @Override
   public void clear() {
      this.size = 0;
      this.head = null;
      this.tail = null;
   }

   @Override
   public boolean remove(int value) {
      boolean changed = false;

      for(TIntLinkedList.TIntLink l = this.head; got(l); l = l.getNext()) {
         if (l.getValue() == value) {
            changed = true;
            this.removeLink(l);
         }
      }

      return changed;
   }

   private void removeLink(TIntLinkedList.TIntLink l) {
      if (!no(l)) {
         --this.size;
         TIntLinkedList.TIntLink prev = l.getPrevious();
         TIntLinkedList.TIntLink next = l.getNext();
         if (got(prev)) {
            prev.setNext(next);
         } else {
            this.head = next;
         }

         if (got(next)) {
            next.setPrevious(prev);
         } else {
            this.tail = prev;
         }

         l.setNext(null);
         l.setPrevious(null);
      }
   }

   @Override
   public boolean containsAll(Collection<?> collection) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(Object o : collection) {
            if (!(o instanceof Integer)) {
               return false;
            }

            Integer i = (Integer)o;
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(TIntCollection collection) {
      if (this.isEmpty()) {
         return false;
      } else {
         TIntIterator it = collection.iterator();

         while(it.hasNext()) {
            int i = it.next();
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(int[] array) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(int i : array) {
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean addAll(Collection<? extends Integer> collection) {
      boolean ret = false;

      for(Integer v : collection) {
         if (this.add(v)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(TIntCollection collection) {
      boolean ret = false;
      TIntIterator it = collection.iterator();

      while(it.hasNext()) {
         int i = it.next();
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(int[] array) {
      boolean ret = false;

      for(int i : array) {
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      boolean modified = false;
      TIntIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(TIntCollection collection) {
      boolean modified = false;
      TIntIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(int[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TIntIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (Arrays.binarySearch(array, iter.next()) < 0) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(Collection<?> collection) {
      boolean modified = false;
      TIntIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(TIntCollection collection) {
      boolean modified = false;
      TIntIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(int[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TIntIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (Arrays.binarySearch(array, iter.next()) >= 0) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public int removeAt(int offset) {
      TIntLinkedList.TIntLink l = this.getLinkAt(offset);
      if (no(l)) {
         throw new ArrayIndexOutOfBoundsException("no elemenet at " + offset);
      } else {
         int prev = l.getValue();
         this.removeLink(l);
         return prev;
      }
   }

   @Override
   public void remove(int offset, int length) {
      for(int i = 0; i < length; ++i) {
         this.removeAt(offset);
      }
   }

   @Override
   public void transformValues(TIntFunction function) {
      for(TIntLinkedList.TIntLink l = this.head; got(l); l = l.getNext()) {
         l.setValue(function.execute(l.getValue()));
      }
   }

   @Override
   public void reverse() {
      TIntLinkedList.TIntLink h = this.head;
      TIntLinkedList.TIntLink t = this.tail;
      TIntLinkedList.TIntLink l = this.head;

      while(got(l)) {
         TIntLinkedList.TIntLink next = l.getNext();
         TIntLinkedList.TIntLink prev = l.getPrevious();
         TIntLinkedList.TIntLink tmp = l;
         l = l.getNext();
         tmp.setNext(prev);
         tmp.setPrevious(next);
      }

      this.head = t;
      this.tail = h;
   }

   @Override
   public void reverse(int from, int to) {
      if (from > to) {
         throw new IllegalArgumentException("from > to : " + from + ">" + to);
      } else {
         TIntLinkedList.TIntLink start = this.getLinkAt(from);
         TIntLinkedList.TIntLink stop = this.getLinkAt(to);
         TIntLinkedList.TIntLink tmp = null;
         TIntLinkedList.TIntLink tmpHead = start.getPrevious();
         TIntLinkedList.TIntLink l = start;

         while(l != stop) {
            TIntLinkedList.TIntLink next = l.getNext();
            TIntLinkedList.TIntLink prev = l.getPrevious();
            tmp = l;
            l = l.getNext();
            tmp.setNext(prev);
            tmp.setPrevious(next);
         }

         if (got(tmp)) {
            tmpHead.setNext(tmp);
            stop.setPrevious(tmpHead);
         }

         start.setNext(stop);
         stop.setPrevious(start);
      }
   }

   @Override
   public void shuffle(Random rand) {
      for(int i = 0; i < this.size; ++i) {
         TIntLinkedList.TIntLink l = this.getLinkAt(rand.nextInt(this.size()));
         this.removeLink(l);
         this.add(l.getValue());
      }
   }

   @Override
   public TIntList subList(int begin, int end) {
      if (end < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than end index " + end);
      } else if (this.size < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than last index " + this.size);
      } else if (begin < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (end > this.size) {
         throw new IndexOutOfBoundsException("end index < " + this.size);
      } else {
         TIntLinkedList ret = new TIntLinkedList();
         TIntLinkedList.TIntLink tmp = this.getLinkAt(begin);

         for(int i = begin; i < end; ++i) {
            ret.add(tmp.getValue());
            tmp = tmp.getNext();
         }

         return ret;
      }
   }

   @Override
   public int[] toArray() {
      return this.toArray(new int[this.size], 0, this.size);
   }

   @Override
   public int[] toArray(int offset, int len) {
      return this.toArray(new int[len], offset, 0, len);
   }

   @Override
   public int[] toArray(int[] dest) {
      return this.toArray(dest, 0, this.size);
   }

   @Override
   public int[] toArray(int[] dest, int offset, int len) {
      return this.toArray(dest, offset, 0, len);
   }

   @Override
   public int[] toArray(int[] dest, int source_pos, int dest_pos, int len) {
      if (len == 0) {
         return dest;
      } else if (source_pos >= 0 && source_pos < this.size()) {
         TIntLinkedList.TIntLink tmp = this.getLinkAt(source_pos);

         for(int i = 0; i < len; ++i) {
            dest[dest_pos + i] = tmp.getValue();
            tmp = tmp.getNext();
         }

         return dest;
      } else {
         throw new ArrayIndexOutOfBoundsException(source_pos);
      }
   }

   @Override
   public boolean forEach(TIntProcedure procedure) {
      for(TIntLinkedList.TIntLink l = this.head; got(l); l = l.getNext()) {
         if (!procedure.execute(l.getValue())) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachDescending(TIntProcedure procedure) {
      for(TIntLinkedList.TIntLink l = this.tail; got(l); l = l.getPrevious()) {
         if (!procedure.execute(l.getValue())) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void sort() {
      this.sort(0, this.size);
   }

   @Override
   public void sort(int fromIndex, int toIndex) {
      TIntList tmp = this.subList(fromIndex, toIndex);
      int[] vals = tmp.toArray();
      Arrays.sort(vals);
      this.set(fromIndex, vals);
   }

   @Override
   public void fill(int val) {
      this.fill(0, this.size, val);
   }

   @Override
   public void fill(int fromIndex, int toIndex, int val) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else {
         TIntLinkedList.TIntLink l = this.getLinkAt(fromIndex);
         if (toIndex > this.size) {
            for(int i = fromIndex; i < this.size; ++i) {
               l.setValue(val);
               l = l.getNext();
            }

            for(int i = this.size; i < toIndex; ++i) {
               this.add(val);
            }
         } else {
            for(int i = fromIndex; i < toIndex; ++i) {
               l.setValue(val);
               l = l.getNext();
            }
         }
      }
   }

   @Override
   public int binarySearch(int value) {
      return this.binarySearch(value, 0, this.size());
   }

   @Override
   public int binarySearch(int value, int fromIndex, int toIndex) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (toIndex > this.size) {
         throw new IndexOutOfBoundsException("end index > size: " + toIndex + " > " + this.size);
      } else if (toIndex < fromIndex) {
         return -(fromIndex + 1);
      } else {
         int from = fromIndex;
         TIntLinkedList.TIntLink fromLink = this.getLinkAt(fromIndex);
         int to = toIndex;

         while(from < to) {
            int mid = from + to >>> 1;
            TIntLinkedList.TIntLink middle = getLink(fromLink, from, mid);
            if (middle.getValue() == value) {
               return mid;
            }

            if (middle.getValue() < value) {
               from = mid + 1;
               fromLink = middle.next;
            } else {
               to = mid - 1;
            }
         }

         return -(from + 1);
      }
   }

   @Override
   public int indexOf(int value) {
      return this.indexOf(0, value);
   }

   @Override
   public int indexOf(int offset, int value) {
      int count = offset;

      for(TIntLinkedList.TIntLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
         if (l.getValue() == value) {
            return count;
         }

         ++count;
      }

      return -1;
   }

   @Override
   public int lastIndexOf(int value) {
      return this.lastIndexOf(0, value);
   }

   @Override
   public int lastIndexOf(int offset, int value) {
      if (this.isEmpty()) {
         return -1;
      } else {
         int last = -1;
         int count = offset;

         for(TIntLinkedList.TIntLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
            if (l.getValue() == value) {
               last = count;
            }

            ++count;
         }

         return last;
      }
   }

   @Override
   public boolean contains(int value) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(TIntLinkedList.TIntLink l = this.head; got(l); l = l.getNext()) {
            if (l.getValue() == value) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public TIntIterator iterator() {
      return new TIntIterator() {
         TIntLinkedList.TIntLink l = TIntLinkedList.this.head;
         TIntLinkedList.TIntLink current;

         @Override
         public int next() {
            if (TIntLinkedList.no(this.l)) {
               throw new NoSuchElementException();
            } else {
               int ret = this.l.getValue();
               this.current = this.l;
               this.l = this.l.getNext();
               return ret;
            }
         }

         @Override
         public boolean hasNext() {
            return TIntLinkedList.got(this.l);
         }

         @Override
         public void remove() {
            if (this.current == null) {
               throw new IllegalStateException();
            } else {
               TIntLinkedList.this.removeLink(this.current);
               this.current = null;
            }
         }
      };
   }

   @Override
   public TIntList grep(TIntProcedure condition) {
      TIntList ret = new TIntLinkedList();

      for(TIntLinkedList.TIntLink l = this.head; got(l); l = l.getNext()) {
         if (condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public TIntList inverseGrep(TIntProcedure condition) {
      TIntList ret = new TIntLinkedList();

      for(TIntLinkedList.TIntLink l = this.head; got(l); l = l.getNext()) {
         if (!condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public int max() {
      int ret = Integer.MIN_VALUE;
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TIntLinkedList.TIntLink l = this.head; got(l); l = l.getNext()) {
            if (ret < l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public int min() {
      int ret = Integer.MAX_VALUE;
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TIntLinkedList.TIntLink l = this.head; got(l); l = l.getNext()) {
            if (ret > l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public int sum() {
      int sum = 0;

      for(TIntLinkedList.TIntLink l = this.head; got(l); l = l.getNext()) {
         sum += l.getValue();
      }

      return sum;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeInt(this.no_entry_value);
      out.writeInt(this.size);
      TIntIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         int next = iterator.next();
         out.writeInt(next);
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.no_entry_value = in.readInt();
      int len = in.readInt();

      for(int i = 0; i < len; ++i) {
         this.add(in.readInt());
      }
   }

   static boolean got(Object ref) {
      return ref != null;
   }

   static boolean no(Object ref) {
      return ref == null;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TIntLinkedList that = (TIntLinkedList)o;
         if (this.no_entry_value != that.no_entry_value) {
            return false;
         } else if (this.size != that.size) {
            return false;
         } else {
            TIntIterator iterator = this.iterator();
            TIntIterator thatIterator = that.iterator();

            while(iterator.hasNext()) {
               if (!thatIterator.hasNext()) {
                  return false;
               }

               if (iterator.next() != thatIterator.next()) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = HashFunctions.hash(this.no_entry_value);
      result = 31 * result + this.size;
      TIntIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         result = 31 * result + HashFunctions.hash(iterator.next());
      }

      return result;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder("{");
      TIntIterator it = this.iterator();

      while(it.hasNext()) {
         int next = it.next();
         buf.append(next);
         if (it.hasNext()) {
            buf.append(", ");
         }
      }

      buf.append("}");
      return buf.toString();
   }

   class RemoveProcedure implements TIntProcedure {
      boolean changed = false;

      @Override
      public boolean execute(int value) {
         if (TIntLinkedList.this.remove(value)) {
            this.changed = true;
         }

         return true;
      }

      public boolean isChanged() {
         return this.changed;
      }
   }

   static class TIntLink {
      int value;
      TIntLinkedList.TIntLink previous;
      TIntLinkedList.TIntLink next;

      TIntLink(int value) {
         this.value = value;
      }

      public int getValue() {
         return this.value;
      }

      public void setValue(int value) {
         this.value = value;
      }

      public TIntLinkedList.TIntLink getPrevious() {
         return this.previous;
      }

      public void setPrevious(TIntLinkedList.TIntLink previous) {
         this.previous = previous;
      }

      public TIntLinkedList.TIntLink getNext() {
         return this.next;
      }

      public void setNext(TIntLinkedList.TIntLink next) {
         this.next = next;
      }
   }
}
