package gnu.trove.list.linked;

import gnu.trove.TCharCollection;
import gnu.trove.function.TCharFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.iterator.TCharIterator;
import gnu.trove.list.TCharList;
import gnu.trove.procedure.TCharProcedure;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Random;

public class TCharLinkedList implements TCharList, Externalizable {
   char no_entry_value;
   int size;
   TCharLinkedList.TCharLink head = null;
   TCharLinkedList.TCharLink tail = this.head;

   public TCharLinkedList() {
   }

   public TCharLinkedList(char no_entry_value) {
      this.no_entry_value = no_entry_value;
   }

   public TCharLinkedList(TCharList list) {
      this.no_entry_value = list.getNoEntryValue();
      TCharIterator iterator = list.iterator();

      while(iterator.hasNext()) {
         char next = iterator.next();
         this.add(next);
      }
   }

   @Override
   public char getNoEntryValue() {
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
   public boolean add(char val) {
      TCharLinkedList.TCharLink l = new TCharLinkedList.TCharLink(val);
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
   public void add(char[] vals) {
      for(char val : vals) {
         this.add(val);
      }
   }

   @Override
   public void add(char[] vals, int offset, int length) {
      for(int i = 0; i < length; ++i) {
         char val = vals[offset + i];
         this.add(val);
      }
   }

   @Override
   public void insert(int offset, char value) {
      TCharLinkedList tmp = new TCharLinkedList();
      tmp.add(value);
      this.insert(offset, tmp);
   }

   @Override
   public void insert(int offset, char[] values) {
      this.insert(offset, link(values, 0, values.length));
   }

   @Override
   public void insert(int offset, char[] values, int valOffset, int len) {
      this.insert(offset, link(values, valOffset, len));
   }

   void insert(int offset, TCharLinkedList tmp) {
      TCharLinkedList.TCharLink l = this.getLinkAt(offset);
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
            TCharLinkedList.TCharLink prev = l.getPrevious();
            l.getPrevious().setNext(tmp.head);
            tmp.tail.setNext(l);
            l.setPrevious(tmp.tail);
            tmp.head.setPrevious(prev);
         }
      }
   }

   static TCharLinkedList link(char[] values, int valOffset, int len) {
      TCharLinkedList ret = new TCharLinkedList();

      for(int i = 0; i < len; ++i) {
         ret.add(values[valOffset + i]);
      }

      return ret;
   }

   @Override
   public char get(int offset) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TCharLinkedList.TCharLink l = this.getLinkAt(offset);
         return no(l) ? this.no_entry_value : l.getValue();
      }
   }

   public TCharLinkedList.TCharLink getLinkAt(int offset) {
      if (offset >= this.size()) {
         return null;
      } else {
         return offset <= this.size() >>> 1 ? getLink(this.head, 0, offset, true) : getLink(this.tail, this.size() - 1, offset, false);
      }
   }

   private static TCharLinkedList.TCharLink getLink(TCharLinkedList.TCharLink l, int idx, int offset) {
      return getLink(l, idx, offset, true);
   }

   private static TCharLinkedList.TCharLink getLink(TCharLinkedList.TCharLink l, int idx, int offset, boolean next) {
      for(int i = idx; got(l); l = next ? l.getNext() : l.getPrevious()) {
         if (i == offset) {
            return l;
         }

         i += next ? 1 : -1;
      }

      return null;
   }

   @Override
   public char set(int offset, char val) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TCharLinkedList.TCharLink l = this.getLinkAt(offset);
         if (no(l)) {
            throw new IndexOutOfBoundsException("at offset " + offset);
         } else {
            char prev = l.getValue();
            l.setValue(val);
            return prev;
         }
      }
   }

   @Override
   public void set(int offset, char[] values) {
      this.set(offset, values, 0, values.length);
   }

   @Override
   public void set(int offset, char[] values, int valOffset, int length) {
      for(int i = 0; i < length; ++i) {
         char value = values[valOffset + i];
         this.set(offset + i, value);
      }
   }

   @Override
   public char replace(int offset, char val) {
      return this.set(offset, val);
   }

   @Override
   public void clear() {
      this.size = 0;
      this.head = null;
      this.tail = null;
   }

   @Override
   public boolean remove(char value) {
      boolean changed = false;

      for(TCharLinkedList.TCharLink l = this.head; got(l); l = l.getNext()) {
         if (l.getValue() == value) {
            changed = true;
            this.removeLink(l);
         }
      }

      return changed;
   }

   private void removeLink(TCharLinkedList.TCharLink l) {
      if (!no(l)) {
         --this.size;
         TCharLinkedList.TCharLink prev = l.getPrevious();
         TCharLinkedList.TCharLink next = l.getNext();
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
            if (!(o instanceof Character)) {
               return false;
            }

            Character i = (Character)o;
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(TCharCollection collection) {
      if (this.isEmpty()) {
         return false;
      } else {
         TCharIterator it = collection.iterator();

         while(it.hasNext()) {
            char i = it.next();
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(char[] array) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(char i : array) {
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean addAll(Collection<? extends Character> collection) {
      boolean ret = false;

      for(Character v : collection) {
         if (this.add(v)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(TCharCollection collection) {
      boolean ret = false;
      TCharIterator it = collection.iterator();

      while(it.hasNext()) {
         char i = it.next();
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(char[] array) {
      boolean ret = false;

      for(char i : array) {
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      boolean modified = false;
      TCharIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(TCharCollection collection) {
      boolean modified = false;
      TCharIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(char[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TCharIterator iter = this.iterator();

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
      TCharIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(TCharCollection collection) {
      boolean modified = false;
      TCharIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(char[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TCharIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (Arrays.binarySearch(array, iter.next()) >= 0) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public char removeAt(int offset) {
      TCharLinkedList.TCharLink l = this.getLinkAt(offset);
      if (no(l)) {
         throw new ArrayIndexOutOfBoundsException("no elemenet at " + offset);
      } else {
         char prev = l.getValue();
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
   public void transformValues(TCharFunction function) {
      for(TCharLinkedList.TCharLink l = this.head; got(l); l = l.getNext()) {
         l.setValue(function.execute(l.getValue()));
      }
   }

   @Override
   public void reverse() {
      TCharLinkedList.TCharLink h = this.head;
      TCharLinkedList.TCharLink t = this.tail;
      TCharLinkedList.TCharLink l = this.head;

      while(got(l)) {
         TCharLinkedList.TCharLink next = l.getNext();
         TCharLinkedList.TCharLink prev = l.getPrevious();
         TCharLinkedList.TCharLink tmp = l;
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
         TCharLinkedList.TCharLink start = this.getLinkAt(from);
         TCharLinkedList.TCharLink stop = this.getLinkAt(to);
         TCharLinkedList.TCharLink tmp = null;
         TCharLinkedList.TCharLink tmpHead = start.getPrevious();
         TCharLinkedList.TCharLink l = start;

         while(l != stop) {
            TCharLinkedList.TCharLink next = l.getNext();
            TCharLinkedList.TCharLink prev = l.getPrevious();
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
         TCharLinkedList.TCharLink l = this.getLinkAt(rand.nextInt(this.size()));
         this.removeLink(l);
         this.add(l.getValue());
      }
   }

   @Override
   public TCharList subList(int begin, int end) {
      if (end < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than end index " + end);
      } else if (this.size < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than last index " + this.size);
      } else if (begin < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (end > this.size) {
         throw new IndexOutOfBoundsException("end index < " + this.size);
      } else {
         TCharLinkedList ret = new TCharLinkedList();
         TCharLinkedList.TCharLink tmp = this.getLinkAt(begin);

         for(int i = begin; i < end; ++i) {
            ret.add(tmp.getValue());
            tmp = tmp.getNext();
         }

         return ret;
      }
   }

   @Override
   public char[] toArray() {
      return this.toArray(new char[this.size], 0, this.size);
   }

   @Override
   public char[] toArray(int offset, int len) {
      return this.toArray(new char[len], offset, 0, len);
   }

   @Override
   public char[] toArray(char[] dest) {
      return this.toArray(dest, 0, this.size);
   }

   @Override
   public char[] toArray(char[] dest, int offset, int len) {
      return this.toArray(dest, offset, 0, len);
   }

   @Override
   public char[] toArray(char[] dest, int source_pos, int dest_pos, int len) {
      if (len == 0) {
         return dest;
      } else if (source_pos >= 0 && source_pos < this.size()) {
         TCharLinkedList.TCharLink tmp = this.getLinkAt(source_pos);

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
   public boolean forEach(TCharProcedure procedure) {
      for(TCharLinkedList.TCharLink l = this.head; got(l); l = l.getNext()) {
         if (!procedure.execute(l.getValue())) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachDescending(TCharProcedure procedure) {
      for(TCharLinkedList.TCharLink l = this.tail; got(l); l = l.getPrevious()) {
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
      TCharList tmp = this.subList(fromIndex, toIndex);
      char[] vals = tmp.toArray();
      Arrays.sort(vals);
      this.set(fromIndex, vals);
   }

   @Override
   public void fill(char val) {
      this.fill(0, this.size, val);
   }

   @Override
   public void fill(int fromIndex, int toIndex, char val) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else {
         TCharLinkedList.TCharLink l = this.getLinkAt(fromIndex);
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
   public int binarySearch(char value) {
      return this.binarySearch(value, 0, this.size());
   }

   @Override
   public int binarySearch(char value, int fromIndex, int toIndex) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (toIndex > this.size) {
         throw new IndexOutOfBoundsException("end index > size: " + toIndex + " > " + this.size);
      } else if (toIndex < fromIndex) {
         return -(fromIndex + 1);
      } else {
         int from = fromIndex;
         TCharLinkedList.TCharLink fromLink = this.getLinkAt(fromIndex);
         int to = toIndex;

         while(from < to) {
            int mid = from + to >>> 1;
            TCharLinkedList.TCharLink middle = getLink(fromLink, from, mid);
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
   public int indexOf(char value) {
      return this.indexOf(0, value);
   }

   @Override
   public int indexOf(int offset, char value) {
      int count = offset;

      for(TCharLinkedList.TCharLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
         if (l.getValue() == value) {
            return count;
         }

         ++count;
      }

      return -1;
   }

   @Override
   public int lastIndexOf(char value) {
      return this.lastIndexOf(0, value);
   }

   @Override
   public int lastIndexOf(int offset, char value) {
      if (this.isEmpty()) {
         return -1;
      } else {
         int last = -1;
         int count = offset;

         for(TCharLinkedList.TCharLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
            if (l.getValue() == value) {
               last = count;
            }

            ++count;
         }

         return last;
      }
   }

   @Override
   public boolean contains(char value) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(TCharLinkedList.TCharLink l = this.head; got(l); l = l.getNext()) {
            if (l.getValue() == value) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public TCharIterator iterator() {
      return new TCharIterator() {
         TCharLinkedList.TCharLink l = TCharLinkedList.this.head;
         TCharLinkedList.TCharLink current;

         @Override
         public char next() {
            if (TCharLinkedList.no(this.l)) {
               throw new NoSuchElementException();
            } else {
               char ret = this.l.getValue();
               this.current = this.l;
               this.l = this.l.getNext();
               return ret;
            }
         }

         @Override
         public boolean hasNext() {
            return TCharLinkedList.got(this.l);
         }

         @Override
         public void remove() {
            if (this.current == null) {
               throw new IllegalStateException();
            } else {
               TCharLinkedList.this.removeLink(this.current);
               this.current = null;
            }
         }
      };
   }

   @Override
   public TCharList grep(TCharProcedure condition) {
      TCharList ret = new TCharLinkedList();

      for(TCharLinkedList.TCharLink l = this.head; got(l); l = l.getNext()) {
         if (condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public TCharList inverseGrep(TCharProcedure condition) {
      TCharList ret = new TCharLinkedList();

      for(TCharLinkedList.TCharLink l = this.head; got(l); l = l.getNext()) {
         if (!condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public char max() {
      char ret = 0;
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TCharLinkedList.TCharLink l = this.head; got(l); l = l.getNext()) {
            if (ret < l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public char min() {
      char ret = '\uffff';
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TCharLinkedList.TCharLink l = this.head; got(l); l = l.getNext()) {
            if (ret > l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public char sum() {
      char sum = 0;

      for(TCharLinkedList.TCharLink l = this.head; got(l); l = l.getNext()) {
         sum += l.getValue();
      }

      return sum;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeChar(this.no_entry_value);
      out.writeInt(this.size);
      TCharIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         char next = iterator.next();
         out.writeChar(next);
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.no_entry_value = in.readChar();
      int len = in.readInt();

      for(int i = 0; i < len; ++i) {
         this.add(in.readChar());
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
         TCharLinkedList that = (TCharLinkedList)o;
         if (this.no_entry_value != that.no_entry_value) {
            return false;
         } else if (this.size != that.size) {
            return false;
         } else {
            TCharIterator iterator = this.iterator();
            TCharIterator thatIterator = that.iterator();

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
      TCharIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         result = 31 * result + HashFunctions.hash(iterator.next());
      }

      return result;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder("{");
      TCharIterator it = this.iterator();

      while(it.hasNext()) {
         char next = it.next();
         buf.append(next);
         if (it.hasNext()) {
            buf.append(", ");
         }
      }

      buf.append("}");
      return buf.toString();
   }

   class RemoveProcedure implements TCharProcedure {
      boolean changed = false;

      @Override
      public boolean execute(char value) {
         if (TCharLinkedList.this.remove(value)) {
            this.changed = true;
         }

         return true;
      }

      public boolean isChanged() {
         return this.changed;
      }
   }

   static class TCharLink {
      char value;
      TCharLinkedList.TCharLink previous;
      TCharLinkedList.TCharLink next;

      TCharLink(char value) {
         this.value = value;
      }

      public char getValue() {
         return this.value;
      }

      public void setValue(char value) {
         this.value = value;
      }

      public TCharLinkedList.TCharLink getPrevious() {
         return this.previous;
      }

      public void setPrevious(TCharLinkedList.TCharLink previous) {
         this.previous = previous;
      }

      public TCharLinkedList.TCharLink getNext() {
         return this.next;
      }

      public void setNext(TCharLinkedList.TCharLink next) {
         this.next = next;
      }
   }
}
