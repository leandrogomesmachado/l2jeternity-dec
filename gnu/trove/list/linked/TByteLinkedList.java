package gnu.trove.list.linked;

import gnu.trove.TByteCollection;
import gnu.trove.function.TByteFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.iterator.TByteIterator;
import gnu.trove.list.TByteList;
import gnu.trove.procedure.TByteProcedure;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Random;

public class TByteLinkedList implements TByteList, Externalizable {
   byte no_entry_value;
   int size;
   TByteLinkedList.TByteLink head = null;
   TByteLinkedList.TByteLink tail = this.head;

   public TByteLinkedList() {
   }

   public TByteLinkedList(byte no_entry_value) {
      this.no_entry_value = no_entry_value;
   }

   public TByteLinkedList(TByteList list) {
      this.no_entry_value = list.getNoEntryValue();
      TByteIterator iterator = list.iterator();

      while(iterator.hasNext()) {
         byte next = iterator.next();
         this.add(next);
      }
   }

   @Override
   public byte getNoEntryValue() {
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
   public boolean add(byte val) {
      TByteLinkedList.TByteLink l = new TByteLinkedList.TByteLink(val);
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
   public void add(byte[] vals) {
      for(byte val : vals) {
         this.add(val);
      }
   }

   @Override
   public void add(byte[] vals, int offset, int length) {
      for(int i = 0; i < length; ++i) {
         byte val = vals[offset + i];
         this.add(val);
      }
   }

   @Override
   public void insert(int offset, byte value) {
      TByteLinkedList tmp = new TByteLinkedList();
      tmp.add(value);
      this.insert(offset, tmp);
   }

   @Override
   public void insert(int offset, byte[] values) {
      this.insert(offset, link(values, 0, values.length));
   }

   @Override
   public void insert(int offset, byte[] values, int valOffset, int len) {
      this.insert(offset, link(values, valOffset, len));
   }

   void insert(int offset, TByteLinkedList tmp) {
      TByteLinkedList.TByteLink l = this.getLinkAt(offset);
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
            TByteLinkedList.TByteLink prev = l.getPrevious();
            l.getPrevious().setNext(tmp.head);
            tmp.tail.setNext(l);
            l.setPrevious(tmp.tail);
            tmp.head.setPrevious(prev);
         }
      }
   }

   static TByteLinkedList link(byte[] values, int valOffset, int len) {
      TByteLinkedList ret = new TByteLinkedList();

      for(int i = 0; i < len; ++i) {
         ret.add(values[valOffset + i]);
      }

      return ret;
   }

   @Override
   public byte get(int offset) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TByteLinkedList.TByteLink l = this.getLinkAt(offset);
         return no(l) ? this.no_entry_value : l.getValue();
      }
   }

   public TByteLinkedList.TByteLink getLinkAt(int offset) {
      if (offset >= this.size()) {
         return null;
      } else {
         return offset <= this.size() >>> 1 ? getLink(this.head, 0, offset, true) : getLink(this.tail, this.size() - 1, offset, false);
      }
   }

   private static TByteLinkedList.TByteLink getLink(TByteLinkedList.TByteLink l, int idx, int offset) {
      return getLink(l, idx, offset, true);
   }

   private static TByteLinkedList.TByteLink getLink(TByteLinkedList.TByteLink l, int idx, int offset, boolean next) {
      for(int i = idx; got(l); l = next ? l.getNext() : l.getPrevious()) {
         if (i == offset) {
            return l;
         }

         i += next ? 1 : -1;
      }

      return null;
   }

   @Override
   public byte set(int offset, byte val) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TByteLinkedList.TByteLink l = this.getLinkAt(offset);
         if (no(l)) {
            throw new IndexOutOfBoundsException("at offset " + offset);
         } else {
            byte prev = l.getValue();
            l.setValue(val);
            return prev;
         }
      }
   }

   @Override
   public void set(int offset, byte[] values) {
      this.set(offset, values, 0, values.length);
   }

   @Override
   public void set(int offset, byte[] values, int valOffset, int length) {
      for(int i = 0; i < length; ++i) {
         byte value = values[valOffset + i];
         this.set(offset + i, value);
      }
   }

   @Override
   public byte replace(int offset, byte val) {
      return this.set(offset, val);
   }

   @Override
   public void clear() {
      this.size = 0;
      this.head = null;
      this.tail = null;
   }

   @Override
   public boolean remove(byte value) {
      boolean changed = false;

      for(TByteLinkedList.TByteLink l = this.head; got(l); l = l.getNext()) {
         if (l.getValue() == value) {
            changed = true;
            this.removeLink(l);
         }
      }

      return changed;
   }

   private void removeLink(TByteLinkedList.TByteLink l) {
      if (!no(l)) {
         --this.size;
         TByteLinkedList.TByteLink prev = l.getPrevious();
         TByteLinkedList.TByteLink next = l.getNext();
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
            if (!(o instanceof Byte)) {
               return false;
            }

            Byte i = (Byte)o;
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(TByteCollection collection) {
      if (this.isEmpty()) {
         return false;
      } else {
         TByteIterator it = collection.iterator();

         while(it.hasNext()) {
            byte i = it.next();
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(byte[] array) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(byte i : array) {
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean addAll(Collection<? extends Byte> collection) {
      boolean ret = false;

      for(Byte v : collection) {
         if (this.add(v)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(TByteCollection collection) {
      boolean ret = false;
      TByteIterator it = collection.iterator();

      while(it.hasNext()) {
         byte i = it.next();
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(byte[] array) {
      boolean ret = false;

      for(byte i : array) {
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      boolean modified = false;
      TByteIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(TByteCollection collection) {
      boolean modified = false;
      TByteIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(byte[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TByteIterator iter = this.iterator();

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
      TByteIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(TByteCollection collection) {
      boolean modified = false;
      TByteIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(byte[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TByteIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (Arrays.binarySearch(array, iter.next()) >= 0) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public byte removeAt(int offset) {
      TByteLinkedList.TByteLink l = this.getLinkAt(offset);
      if (no(l)) {
         throw new ArrayIndexOutOfBoundsException("no elemenet at " + offset);
      } else {
         byte prev = l.getValue();
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
   public void transformValues(TByteFunction function) {
      for(TByteLinkedList.TByteLink l = this.head; got(l); l = l.getNext()) {
         l.setValue(function.execute(l.getValue()));
      }
   }

   @Override
   public void reverse() {
      TByteLinkedList.TByteLink h = this.head;
      TByteLinkedList.TByteLink t = this.tail;
      TByteLinkedList.TByteLink l = this.head;

      while(got(l)) {
         TByteLinkedList.TByteLink next = l.getNext();
         TByteLinkedList.TByteLink prev = l.getPrevious();
         TByteLinkedList.TByteLink tmp = l;
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
         TByteLinkedList.TByteLink start = this.getLinkAt(from);
         TByteLinkedList.TByteLink stop = this.getLinkAt(to);
         TByteLinkedList.TByteLink tmp = null;
         TByteLinkedList.TByteLink tmpHead = start.getPrevious();
         TByteLinkedList.TByteLink l = start;

         while(l != stop) {
            TByteLinkedList.TByteLink next = l.getNext();
            TByteLinkedList.TByteLink prev = l.getPrevious();
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
         TByteLinkedList.TByteLink l = this.getLinkAt(rand.nextInt(this.size()));
         this.removeLink(l);
         this.add(l.getValue());
      }
   }

   @Override
   public TByteList subList(int begin, int end) {
      if (end < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than end index " + end);
      } else if (this.size < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than last index " + this.size);
      } else if (begin < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (end > this.size) {
         throw new IndexOutOfBoundsException("end index < " + this.size);
      } else {
         TByteLinkedList ret = new TByteLinkedList();
         TByteLinkedList.TByteLink tmp = this.getLinkAt(begin);

         for(int i = begin; i < end; ++i) {
            ret.add(tmp.getValue());
            tmp = tmp.getNext();
         }

         return ret;
      }
   }

   @Override
   public byte[] toArray() {
      return this.toArray(new byte[this.size], 0, this.size);
   }

   @Override
   public byte[] toArray(int offset, int len) {
      return this.toArray(new byte[len], offset, 0, len);
   }

   @Override
   public byte[] toArray(byte[] dest) {
      return this.toArray(dest, 0, this.size);
   }

   @Override
   public byte[] toArray(byte[] dest, int offset, int len) {
      return this.toArray(dest, offset, 0, len);
   }

   @Override
   public byte[] toArray(byte[] dest, int source_pos, int dest_pos, int len) {
      if (len == 0) {
         return dest;
      } else if (source_pos >= 0 && source_pos < this.size()) {
         TByteLinkedList.TByteLink tmp = this.getLinkAt(source_pos);

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
   public boolean forEach(TByteProcedure procedure) {
      for(TByteLinkedList.TByteLink l = this.head; got(l); l = l.getNext()) {
         if (!procedure.execute(l.getValue())) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachDescending(TByteProcedure procedure) {
      for(TByteLinkedList.TByteLink l = this.tail; got(l); l = l.getPrevious()) {
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
      TByteList tmp = this.subList(fromIndex, toIndex);
      byte[] vals = tmp.toArray();
      Arrays.sort(vals);
      this.set(fromIndex, vals);
   }

   @Override
   public void fill(byte val) {
      this.fill(0, this.size, val);
   }

   @Override
   public void fill(int fromIndex, int toIndex, byte val) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else {
         TByteLinkedList.TByteLink l = this.getLinkAt(fromIndex);
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
   public int binarySearch(byte value) {
      return this.binarySearch(value, 0, this.size());
   }

   @Override
   public int binarySearch(byte value, int fromIndex, int toIndex) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (toIndex > this.size) {
         throw new IndexOutOfBoundsException("end index > size: " + toIndex + " > " + this.size);
      } else if (toIndex < fromIndex) {
         return -(fromIndex + 1);
      } else {
         int from = fromIndex;
         TByteLinkedList.TByteLink fromLink = this.getLinkAt(fromIndex);
         int to = toIndex;

         while(from < to) {
            int mid = from + to >>> 1;
            TByteLinkedList.TByteLink middle = getLink(fromLink, from, mid);
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
   public int indexOf(byte value) {
      return this.indexOf(0, value);
   }

   @Override
   public int indexOf(int offset, byte value) {
      int count = offset;

      for(TByteLinkedList.TByteLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
         if (l.getValue() == value) {
            return count;
         }

         ++count;
      }

      return -1;
   }

   @Override
   public int lastIndexOf(byte value) {
      return this.lastIndexOf(0, value);
   }

   @Override
   public int lastIndexOf(int offset, byte value) {
      if (this.isEmpty()) {
         return -1;
      } else {
         int last = -1;
         int count = offset;

         for(TByteLinkedList.TByteLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
            if (l.getValue() == value) {
               last = count;
            }

            ++count;
         }

         return last;
      }
   }

   @Override
   public boolean contains(byte value) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(TByteLinkedList.TByteLink l = this.head; got(l); l = l.getNext()) {
            if (l.getValue() == value) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public TByteIterator iterator() {
      return new TByteIterator() {
         TByteLinkedList.TByteLink l = TByteLinkedList.this.head;
         TByteLinkedList.TByteLink current;

         @Override
         public byte next() {
            if (TByteLinkedList.no(this.l)) {
               throw new NoSuchElementException();
            } else {
               byte ret = this.l.getValue();
               this.current = this.l;
               this.l = this.l.getNext();
               return ret;
            }
         }

         @Override
         public boolean hasNext() {
            return TByteLinkedList.got(this.l);
         }

         @Override
         public void remove() {
            if (this.current == null) {
               throw new IllegalStateException();
            } else {
               TByteLinkedList.this.removeLink(this.current);
               this.current = null;
            }
         }
      };
   }

   @Override
   public TByteList grep(TByteProcedure condition) {
      TByteList ret = new TByteLinkedList();

      for(TByteLinkedList.TByteLink l = this.head; got(l); l = l.getNext()) {
         if (condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public TByteList inverseGrep(TByteProcedure condition) {
      TByteList ret = new TByteLinkedList();

      for(TByteLinkedList.TByteLink l = this.head; got(l); l = l.getNext()) {
         if (!condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public byte max() {
      byte ret = -128;
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TByteLinkedList.TByteLink l = this.head; got(l); l = l.getNext()) {
            if (ret < l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public byte min() {
      byte ret = 127;
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TByteLinkedList.TByteLink l = this.head; got(l); l = l.getNext()) {
            if (ret > l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public byte sum() {
      byte sum = 0;

      for(TByteLinkedList.TByteLink l = this.head; got(l); l = l.getNext()) {
         sum += l.getValue();
      }

      return sum;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeByte(this.no_entry_value);
      out.writeInt(this.size);
      TByteIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         byte next = iterator.next();
         out.writeByte(next);
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.no_entry_value = in.readByte();
      int len = in.readInt();

      for(int i = 0; i < len; ++i) {
         this.add(in.readByte());
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
         TByteLinkedList that = (TByteLinkedList)o;
         if (this.no_entry_value != that.no_entry_value) {
            return false;
         } else if (this.size != that.size) {
            return false;
         } else {
            TByteIterator iterator = this.iterator();
            TByteIterator thatIterator = that.iterator();

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
      TByteIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         result = 31 * result + HashFunctions.hash(iterator.next());
      }

      return result;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder("{");
      TByteIterator it = this.iterator();

      while(it.hasNext()) {
         byte next = it.next();
         buf.append((int)next);
         if (it.hasNext()) {
            buf.append(", ");
         }
      }

      buf.append("}");
      return buf.toString();
   }

   class RemoveProcedure implements TByteProcedure {
      boolean changed = false;

      @Override
      public boolean execute(byte value) {
         if (TByteLinkedList.this.remove(value)) {
            this.changed = true;
         }

         return true;
      }

      public boolean isChanged() {
         return this.changed;
      }
   }

   static class TByteLink {
      byte value;
      TByteLinkedList.TByteLink previous;
      TByteLinkedList.TByteLink next;

      TByteLink(byte value) {
         this.value = value;
      }

      public byte getValue() {
         return this.value;
      }

      public void setValue(byte value) {
         this.value = value;
      }

      public TByteLinkedList.TByteLink getPrevious() {
         return this.previous;
      }

      public void setPrevious(TByteLinkedList.TByteLink previous) {
         this.previous = previous;
      }

      public TByteLinkedList.TByteLink getNext() {
         return this.next;
      }

      public void setNext(TByteLinkedList.TByteLink next) {
         this.next = next;
      }
   }
}
