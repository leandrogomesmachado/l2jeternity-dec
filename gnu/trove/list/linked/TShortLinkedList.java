package gnu.trove.list.linked;

import gnu.trove.TShortCollection;
import gnu.trove.function.TShortFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.list.TShortList;
import gnu.trove.procedure.TShortProcedure;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Random;

public class TShortLinkedList implements TShortList, Externalizable {
   short no_entry_value;
   int size;
   TShortLinkedList.TShortLink head = null;
   TShortLinkedList.TShortLink tail = this.head;

   public TShortLinkedList() {
   }

   public TShortLinkedList(short no_entry_value) {
      this.no_entry_value = no_entry_value;
   }

   public TShortLinkedList(TShortList list) {
      this.no_entry_value = list.getNoEntryValue();
      TShortIterator iterator = list.iterator();

      while(iterator.hasNext()) {
         short next = iterator.next();
         this.add(next);
      }
   }

   @Override
   public short getNoEntryValue() {
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
   public boolean add(short val) {
      TShortLinkedList.TShortLink l = new TShortLinkedList.TShortLink(val);
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
   public void add(short[] vals) {
      for(short val : vals) {
         this.add(val);
      }
   }

   @Override
   public void add(short[] vals, int offset, int length) {
      for(int i = 0; i < length; ++i) {
         short val = vals[offset + i];
         this.add(val);
      }
   }

   @Override
   public void insert(int offset, short value) {
      TShortLinkedList tmp = new TShortLinkedList();
      tmp.add(value);
      this.insert(offset, tmp);
   }

   @Override
   public void insert(int offset, short[] values) {
      this.insert(offset, link(values, 0, values.length));
   }

   @Override
   public void insert(int offset, short[] values, int valOffset, int len) {
      this.insert(offset, link(values, valOffset, len));
   }

   void insert(int offset, TShortLinkedList tmp) {
      TShortLinkedList.TShortLink l = this.getLinkAt(offset);
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
            TShortLinkedList.TShortLink prev = l.getPrevious();
            l.getPrevious().setNext(tmp.head);
            tmp.tail.setNext(l);
            l.setPrevious(tmp.tail);
            tmp.head.setPrevious(prev);
         }
      }
   }

   static TShortLinkedList link(short[] values, int valOffset, int len) {
      TShortLinkedList ret = new TShortLinkedList();

      for(int i = 0; i < len; ++i) {
         ret.add(values[valOffset + i]);
      }

      return ret;
   }

   @Override
   public short get(int offset) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TShortLinkedList.TShortLink l = this.getLinkAt(offset);
         return no(l) ? this.no_entry_value : l.getValue();
      }
   }

   public TShortLinkedList.TShortLink getLinkAt(int offset) {
      if (offset >= this.size()) {
         return null;
      } else {
         return offset <= this.size() >>> 1 ? getLink(this.head, 0, offset, true) : getLink(this.tail, this.size() - 1, offset, false);
      }
   }

   private static TShortLinkedList.TShortLink getLink(TShortLinkedList.TShortLink l, int idx, int offset) {
      return getLink(l, idx, offset, true);
   }

   private static TShortLinkedList.TShortLink getLink(TShortLinkedList.TShortLink l, int idx, int offset, boolean next) {
      for(int i = idx; got(l); l = next ? l.getNext() : l.getPrevious()) {
         if (i == offset) {
            return l;
         }

         i += next ? 1 : -1;
      }

      return null;
   }

   @Override
   public short set(int offset, short val) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TShortLinkedList.TShortLink l = this.getLinkAt(offset);
         if (no(l)) {
            throw new IndexOutOfBoundsException("at offset " + offset);
         } else {
            short prev = l.getValue();
            l.setValue(val);
            return prev;
         }
      }
   }

   @Override
   public void set(int offset, short[] values) {
      this.set(offset, values, 0, values.length);
   }

   @Override
   public void set(int offset, short[] values, int valOffset, int length) {
      for(int i = 0; i < length; ++i) {
         short value = values[valOffset + i];
         this.set(offset + i, value);
      }
   }

   @Override
   public short replace(int offset, short val) {
      return this.set(offset, val);
   }

   @Override
   public void clear() {
      this.size = 0;
      this.head = null;
      this.tail = null;
   }

   @Override
   public boolean remove(short value) {
      boolean changed = false;

      for(TShortLinkedList.TShortLink l = this.head; got(l); l = l.getNext()) {
         if (l.getValue() == value) {
            changed = true;
            this.removeLink(l);
         }
      }

      return changed;
   }

   private void removeLink(TShortLinkedList.TShortLink l) {
      if (!no(l)) {
         --this.size;
         TShortLinkedList.TShortLink prev = l.getPrevious();
         TShortLinkedList.TShortLink next = l.getNext();
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
            if (!(o instanceof Short)) {
               return false;
            }

            Short i = (Short)o;
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(TShortCollection collection) {
      if (this.isEmpty()) {
         return false;
      } else {
         TShortIterator it = collection.iterator();

         while(it.hasNext()) {
            short i = it.next();
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(short[] array) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(short i : array) {
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean addAll(Collection<? extends Short> collection) {
      boolean ret = false;

      for(Short v : collection) {
         if (this.add(v)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(TShortCollection collection) {
      boolean ret = false;
      TShortIterator it = collection.iterator();

      while(it.hasNext()) {
         short i = it.next();
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(short[] array) {
      boolean ret = false;

      for(short i : array) {
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      boolean modified = false;
      TShortIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(TShortCollection collection) {
      boolean modified = false;
      TShortIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(short[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TShortIterator iter = this.iterator();

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
      TShortIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(TShortCollection collection) {
      boolean modified = false;
      TShortIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(short[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TShortIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (Arrays.binarySearch(array, iter.next()) >= 0) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public short removeAt(int offset) {
      TShortLinkedList.TShortLink l = this.getLinkAt(offset);
      if (no(l)) {
         throw new ArrayIndexOutOfBoundsException("no elemenet at " + offset);
      } else {
         short prev = l.getValue();
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
   public void transformValues(TShortFunction function) {
      for(TShortLinkedList.TShortLink l = this.head; got(l); l = l.getNext()) {
         l.setValue(function.execute(l.getValue()));
      }
   }

   @Override
   public void reverse() {
      TShortLinkedList.TShortLink h = this.head;
      TShortLinkedList.TShortLink t = this.tail;
      TShortLinkedList.TShortLink l = this.head;

      while(got(l)) {
         TShortLinkedList.TShortLink next = l.getNext();
         TShortLinkedList.TShortLink prev = l.getPrevious();
         TShortLinkedList.TShortLink tmp = l;
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
         TShortLinkedList.TShortLink start = this.getLinkAt(from);
         TShortLinkedList.TShortLink stop = this.getLinkAt(to);
         TShortLinkedList.TShortLink tmp = null;
         TShortLinkedList.TShortLink tmpHead = start.getPrevious();
         TShortLinkedList.TShortLink l = start;

         while(l != stop) {
            TShortLinkedList.TShortLink next = l.getNext();
            TShortLinkedList.TShortLink prev = l.getPrevious();
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
         TShortLinkedList.TShortLink l = this.getLinkAt(rand.nextInt(this.size()));
         this.removeLink(l);
         this.add(l.getValue());
      }
   }

   @Override
   public TShortList subList(int begin, int end) {
      if (end < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than end index " + end);
      } else if (this.size < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than last index " + this.size);
      } else if (begin < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (end > this.size) {
         throw new IndexOutOfBoundsException("end index < " + this.size);
      } else {
         TShortLinkedList ret = new TShortLinkedList();
         TShortLinkedList.TShortLink tmp = this.getLinkAt(begin);

         for(int i = begin; i < end; ++i) {
            ret.add(tmp.getValue());
            tmp = tmp.getNext();
         }

         return ret;
      }
   }

   @Override
   public short[] toArray() {
      return this.toArray(new short[this.size], 0, this.size);
   }

   @Override
   public short[] toArray(int offset, int len) {
      return this.toArray(new short[len], offset, 0, len);
   }

   @Override
   public short[] toArray(short[] dest) {
      return this.toArray(dest, 0, this.size);
   }

   @Override
   public short[] toArray(short[] dest, int offset, int len) {
      return this.toArray(dest, offset, 0, len);
   }

   @Override
   public short[] toArray(short[] dest, int source_pos, int dest_pos, int len) {
      if (len == 0) {
         return dest;
      } else if (source_pos >= 0 && source_pos < this.size()) {
         TShortLinkedList.TShortLink tmp = this.getLinkAt(source_pos);

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
   public boolean forEach(TShortProcedure procedure) {
      for(TShortLinkedList.TShortLink l = this.head; got(l); l = l.getNext()) {
         if (!procedure.execute(l.getValue())) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachDescending(TShortProcedure procedure) {
      for(TShortLinkedList.TShortLink l = this.tail; got(l); l = l.getPrevious()) {
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
      TShortList tmp = this.subList(fromIndex, toIndex);
      short[] vals = tmp.toArray();
      Arrays.sort(vals);
      this.set(fromIndex, vals);
   }

   @Override
   public void fill(short val) {
      this.fill(0, this.size, val);
   }

   @Override
   public void fill(int fromIndex, int toIndex, short val) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else {
         TShortLinkedList.TShortLink l = this.getLinkAt(fromIndex);
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
   public int binarySearch(short value) {
      return this.binarySearch(value, 0, this.size());
   }

   @Override
   public int binarySearch(short value, int fromIndex, int toIndex) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (toIndex > this.size) {
         throw new IndexOutOfBoundsException("end index > size: " + toIndex + " > " + this.size);
      } else if (toIndex < fromIndex) {
         return -(fromIndex + 1);
      } else {
         int from = fromIndex;
         TShortLinkedList.TShortLink fromLink = this.getLinkAt(fromIndex);
         int to = toIndex;

         while(from < to) {
            int mid = from + to >>> 1;
            TShortLinkedList.TShortLink middle = getLink(fromLink, from, mid);
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
   public int indexOf(short value) {
      return this.indexOf(0, value);
   }

   @Override
   public int indexOf(int offset, short value) {
      int count = offset;

      for(TShortLinkedList.TShortLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
         if (l.getValue() == value) {
            return count;
         }

         ++count;
      }

      return -1;
   }

   @Override
   public int lastIndexOf(short value) {
      return this.lastIndexOf(0, value);
   }

   @Override
   public int lastIndexOf(int offset, short value) {
      if (this.isEmpty()) {
         return -1;
      } else {
         int last = -1;
         int count = offset;

         for(TShortLinkedList.TShortLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
            if (l.getValue() == value) {
               last = count;
            }

            ++count;
         }

         return last;
      }
   }

   @Override
   public boolean contains(short value) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(TShortLinkedList.TShortLink l = this.head; got(l); l = l.getNext()) {
            if (l.getValue() == value) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public TShortIterator iterator() {
      return new TShortIterator() {
         TShortLinkedList.TShortLink l = TShortLinkedList.this.head;
         TShortLinkedList.TShortLink current;

         @Override
         public short next() {
            if (TShortLinkedList.no(this.l)) {
               throw new NoSuchElementException();
            } else {
               short ret = this.l.getValue();
               this.current = this.l;
               this.l = this.l.getNext();
               return ret;
            }
         }

         @Override
         public boolean hasNext() {
            return TShortLinkedList.got(this.l);
         }

         @Override
         public void remove() {
            if (this.current == null) {
               throw new IllegalStateException();
            } else {
               TShortLinkedList.this.removeLink(this.current);
               this.current = null;
            }
         }
      };
   }

   @Override
   public TShortList grep(TShortProcedure condition) {
      TShortList ret = new TShortLinkedList();

      for(TShortLinkedList.TShortLink l = this.head; got(l); l = l.getNext()) {
         if (condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public TShortList inverseGrep(TShortProcedure condition) {
      TShortList ret = new TShortLinkedList();

      for(TShortLinkedList.TShortLink l = this.head; got(l); l = l.getNext()) {
         if (!condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public short max() {
      short ret = -32768;
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TShortLinkedList.TShortLink l = this.head; got(l); l = l.getNext()) {
            if (ret < l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public short min() {
      short ret = 32767;
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TShortLinkedList.TShortLink l = this.head; got(l); l = l.getNext()) {
            if (ret > l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public short sum() {
      short sum = 0;

      for(TShortLinkedList.TShortLink l = this.head; got(l); l = l.getNext()) {
         sum += l.getValue();
      }

      return sum;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeShort(this.no_entry_value);
      out.writeInt(this.size);
      TShortIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         short next = iterator.next();
         out.writeShort(next);
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.no_entry_value = in.readShort();
      int len = in.readInt();

      for(int i = 0; i < len; ++i) {
         this.add(in.readShort());
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
         TShortLinkedList that = (TShortLinkedList)o;
         if (this.no_entry_value != that.no_entry_value) {
            return false;
         } else if (this.size != that.size) {
            return false;
         } else {
            TShortIterator iterator = this.iterator();
            TShortIterator thatIterator = that.iterator();

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
      TShortIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         result = 31 * result + HashFunctions.hash(iterator.next());
      }

      return result;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder("{");
      TShortIterator it = this.iterator();

      while(it.hasNext()) {
         short next = it.next();
         buf.append((int)next);
         if (it.hasNext()) {
            buf.append(", ");
         }
      }

      buf.append("}");
      return buf.toString();
   }

   class RemoveProcedure implements TShortProcedure {
      boolean changed = false;

      @Override
      public boolean execute(short value) {
         if (TShortLinkedList.this.remove(value)) {
            this.changed = true;
         }

         return true;
      }

      public boolean isChanged() {
         return this.changed;
      }
   }

   static class TShortLink {
      short value;
      TShortLinkedList.TShortLink previous;
      TShortLinkedList.TShortLink next;

      TShortLink(short value) {
         this.value = value;
      }

      public short getValue() {
         return this.value;
      }

      public void setValue(short value) {
         this.value = value;
      }

      public TShortLinkedList.TShortLink getPrevious() {
         return this.previous;
      }

      public void setPrevious(TShortLinkedList.TShortLink previous) {
         this.previous = previous;
      }

      public TShortLinkedList.TShortLink getNext() {
         return this.next;
      }

      public void setNext(TShortLinkedList.TShortLink next) {
         this.next = next;
      }
   }
}
