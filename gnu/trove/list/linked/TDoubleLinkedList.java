package gnu.trove.list.linked;

import gnu.trove.TDoubleCollection;
import gnu.trove.function.TDoubleFunction;
import gnu.trove.impl.HashFunctions;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.list.TDoubleList;
import gnu.trove.procedure.TDoubleProcedure;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Random;

public class TDoubleLinkedList implements TDoubleList, Externalizable {
   double no_entry_value;
   int size;
   TDoubleLinkedList.TDoubleLink head = null;
   TDoubleLinkedList.TDoubleLink tail = this.head;

   public TDoubleLinkedList() {
   }

   public TDoubleLinkedList(double no_entry_value) {
      this.no_entry_value = no_entry_value;
   }

   public TDoubleLinkedList(TDoubleList list) {
      this.no_entry_value = list.getNoEntryValue();
      TDoubleIterator iterator = list.iterator();

      while(iterator.hasNext()) {
         double next = iterator.next();
         this.add(next);
      }
   }

   @Override
   public double getNoEntryValue() {
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
   public boolean add(double val) {
      TDoubleLinkedList.TDoubleLink l = new TDoubleLinkedList.TDoubleLink(val);
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
   public void add(double[] vals) {
      for(double val : vals) {
         this.add(val);
      }
   }

   @Override
   public void add(double[] vals, int offset, int length) {
      for(int i = 0; i < length; ++i) {
         double val = vals[offset + i];
         this.add(val);
      }
   }

   @Override
   public void insert(int offset, double value) {
      TDoubleLinkedList tmp = new TDoubleLinkedList();
      tmp.add(value);
      this.insert(offset, tmp);
   }

   @Override
   public void insert(int offset, double[] values) {
      this.insert(offset, link(values, 0, values.length));
   }

   @Override
   public void insert(int offset, double[] values, int valOffset, int len) {
      this.insert(offset, link(values, valOffset, len));
   }

   void insert(int offset, TDoubleLinkedList tmp) {
      TDoubleLinkedList.TDoubleLink l = this.getLinkAt(offset);
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
            TDoubleLinkedList.TDoubleLink prev = l.getPrevious();
            l.getPrevious().setNext(tmp.head);
            tmp.tail.setNext(l);
            l.setPrevious(tmp.tail);
            tmp.head.setPrevious(prev);
         }
      }
   }

   static TDoubleLinkedList link(double[] values, int valOffset, int len) {
      TDoubleLinkedList ret = new TDoubleLinkedList();

      for(int i = 0; i < len; ++i) {
         ret.add(values[valOffset + i]);
      }

      return ret;
   }

   @Override
   public double get(int offset) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TDoubleLinkedList.TDoubleLink l = this.getLinkAt(offset);
         return no(l) ? this.no_entry_value : l.getValue();
      }
   }

   public TDoubleLinkedList.TDoubleLink getLinkAt(int offset) {
      if (offset >= this.size()) {
         return null;
      } else {
         return offset <= this.size() >>> 1 ? getLink(this.head, 0, offset, true) : getLink(this.tail, this.size() - 1, offset, false);
      }
   }

   private static TDoubleLinkedList.TDoubleLink getLink(TDoubleLinkedList.TDoubleLink l, int idx, int offset) {
      return getLink(l, idx, offset, true);
   }

   private static TDoubleLinkedList.TDoubleLink getLink(TDoubleLinkedList.TDoubleLink l, int idx, int offset, boolean next) {
      for(int i = idx; got(l); l = next ? l.getNext() : l.getPrevious()) {
         if (i == offset) {
            return l;
         }

         i += next ? 1 : -1;
      }

      return null;
   }

   @Override
   public double set(int offset, double val) {
      if (offset > this.size) {
         throw new IndexOutOfBoundsException("index " + offset + " exceeds size " + this.size);
      } else {
         TDoubleLinkedList.TDoubleLink l = this.getLinkAt(offset);
         if (no(l)) {
            throw new IndexOutOfBoundsException("at offset " + offset);
         } else {
            double prev = l.getValue();
            l.setValue(val);
            return prev;
         }
      }
   }

   @Override
   public void set(int offset, double[] values) {
      this.set(offset, values, 0, values.length);
   }

   @Override
   public void set(int offset, double[] values, int valOffset, int length) {
      for(int i = 0; i < length; ++i) {
         double value = values[valOffset + i];
         this.set(offset + i, value);
      }
   }

   @Override
   public double replace(int offset, double val) {
      return this.set(offset, val);
   }

   @Override
   public void clear() {
      this.size = 0;
      this.head = null;
      this.tail = null;
   }

   @Override
   public boolean remove(double value) {
      boolean changed = false;

      for(TDoubleLinkedList.TDoubleLink l = this.head; got(l); l = l.getNext()) {
         if (l.getValue() == value) {
            changed = true;
            this.removeLink(l);
         }
      }

      return changed;
   }

   private void removeLink(TDoubleLinkedList.TDoubleLink l) {
      if (!no(l)) {
         --this.size;
         TDoubleLinkedList.TDoubleLink prev = l.getPrevious();
         TDoubleLinkedList.TDoubleLink next = l.getNext();
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
            if (!(o instanceof Double)) {
               return false;
            }

            Double i = (Double)o;
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(TDoubleCollection collection) {
      if (this.isEmpty()) {
         return false;
      } else {
         TDoubleIterator it = collection.iterator();

         while(it.hasNext()) {
            double i = it.next();
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean containsAll(double[] array) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(double i : array) {
            if (!this.contains(i)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean addAll(Collection<? extends Double> collection) {
      boolean ret = false;

      for(Double v : collection) {
         if (this.add(v)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(TDoubleCollection collection) {
      boolean ret = false;
      TDoubleIterator it = collection.iterator();

      while(it.hasNext()) {
         double i = it.next();
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean addAll(double[] array) {
      boolean ret = false;

      for(double i : array) {
         if (this.add(i)) {
            ret = true;
         }
      }

      return ret;
   }

   @Override
   public boolean retainAll(Collection<?> collection) {
      boolean modified = false;
      TDoubleIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(TDoubleCollection collection) {
      boolean modified = false;
      TDoubleIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (!collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean retainAll(double[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TDoubleIterator iter = this.iterator();

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
      TDoubleIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(TDoubleCollection collection) {
      boolean modified = false;
      TDoubleIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (collection.contains(iter.next())) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public boolean removeAll(double[] array) {
      Arrays.sort(array);
      boolean modified = false;
      TDoubleIterator iter = this.iterator();

      while(iter.hasNext()) {
         if (Arrays.binarySearch(array, iter.next()) >= 0) {
            iter.remove();
            modified = true;
         }
      }

      return modified;
   }

   @Override
   public double removeAt(int offset) {
      TDoubleLinkedList.TDoubleLink l = this.getLinkAt(offset);
      if (no(l)) {
         throw new ArrayIndexOutOfBoundsException("no elemenet at " + offset);
      } else {
         double prev = l.getValue();
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
   public void transformValues(TDoubleFunction function) {
      for(TDoubleLinkedList.TDoubleLink l = this.head; got(l); l = l.getNext()) {
         l.setValue(function.execute(l.getValue()));
      }
   }

   @Override
   public void reverse() {
      TDoubleLinkedList.TDoubleLink h = this.head;
      TDoubleLinkedList.TDoubleLink t = this.tail;
      TDoubleLinkedList.TDoubleLink l = this.head;

      while(got(l)) {
         TDoubleLinkedList.TDoubleLink next = l.getNext();
         TDoubleLinkedList.TDoubleLink prev = l.getPrevious();
         TDoubleLinkedList.TDoubleLink tmp = l;
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
         TDoubleLinkedList.TDoubleLink start = this.getLinkAt(from);
         TDoubleLinkedList.TDoubleLink stop = this.getLinkAt(to);
         TDoubleLinkedList.TDoubleLink tmp = null;
         TDoubleLinkedList.TDoubleLink tmpHead = start.getPrevious();
         TDoubleLinkedList.TDoubleLink l = start;

         while(l != stop) {
            TDoubleLinkedList.TDoubleLink next = l.getNext();
            TDoubleLinkedList.TDoubleLink prev = l.getPrevious();
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
         TDoubleLinkedList.TDoubleLink l = this.getLinkAt(rand.nextInt(this.size()));
         this.removeLink(l);
         this.add(l.getValue());
      }
   }

   @Override
   public TDoubleList subList(int begin, int end) {
      if (end < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than end index " + end);
      } else if (this.size < begin) {
         throw new IllegalArgumentException("begin index " + begin + " greater than last index " + this.size);
      } else if (begin < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (end > this.size) {
         throw new IndexOutOfBoundsException("end index < " + this.size);
      } else {
         TDoubleLinkedList ret = new TDoubleLinkedList();
         TDoubleLinkedList.TDoubleLink tmp = this.getLinkAt(begin);

         for(int i = begin; i < end; ++i) {
            ret.add(tmp.getValue());
            tmp = tmp.getNext();
         }

         return ret;
      }
   }

   @Override
   public double[] toArray() {
      return this.toArray(new double[this.size], 0, this.size);
   }

   @Override
   public double[] toArray(int offset, int len) {
      return this.toArray(new double[len], offset, 0, len);
   }

   @Override
   public double[] toArray(double[] dest) {
      return this.toArray(dest, 0, this.size);
   }

   @Override
   public double[] toArray(double[] dest, int offset, int len) {
      return this.toArray(dest, offset, 0, len);
   }

   @Override
   public double[] toArray(double[] dest, int source_pos, int dest_pos, int len) {
      if (len == 0) {
         return dest;
      } else if (source_pos >= 0 && source_pos < this.size()) {
         TDoubleLinkedList.TDoubleLink tmp = this.getLinkAt(source_pos);

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
   public boolean forEach(TDoubleProcedure procedure) {
      for(TDoubleLinkedList.TDoubleLink l = this.head; got(l); l = l.getNext()) {
         if (!procedure.execute(l.getValue())) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean forEachDescending(TDoubleProcedure procedure) {
      for(TDoubleLinkedList.TDoubleLink l = this.tail; got(l); l = l.getPrevious()) {
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
      TDoubleList tmp = this.subList(fromIndex, toIndex);
      double[] vals = tmp.toArray();
      Arrays.sort(vals);
      this.set(fromIndex, vals);
   }

   @Override
   public void fill(double val) {
      this.fill(0, this.size, val);
   }

   @Override
   public void fill(int fromIndex, int toIndex, double val) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else {
         TDoubleLinkedList.TDoubleLink l = this.getLinkAt(fromIndex);
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
   public int binarySearch(double value) {
      return this.binarySearch(value, 0, this.size());
   }

   @Override
   public int binarySearch(double value, int fromIndex, int toIndex) {
      if (fromIndex < 0) {
         throw new IndexOutOfBoundsException("begin index can not be < 0");
      } else if (toIndex > this.size) {
         throw new IndexOutOfBoundsException("end index > size: " + toIndex + " > " + this.size);
      } else if (toIndex < fromIndex) {
         return -(fromIndex + 1);
      } else {
         int from = fromIndex;
         TDoubleLinkedList.TDoubleLink fromLink = this.getLinkAt(fromIndex);
         int to = toIndex;

         while(from < to) {
            int mid = from + to >>> 1;
            TDoubleLinkedList.TDoubleLink middle = getLink(fromLink, from, mid);
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
   public int indexOf(double value) {
      return this.indexOf(0, value);
   }

   @Override
   public int indexOf(int offset, double value) {
      int count = offset;

      for(TDoubleLinkedList.TDoubleLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
         if (l.getValue() == value) {
            return count;
         }

         ++count;
      }

      return -1;
   }

   @Override
   public int lastIndexOf(double value) {
      return this.lastIndexOf(0, value);
   }

   @Override
   public int lastIndexOf(int offset, double value) {
      if (this.isEmpty()) {
         return -1;
      } else {
         int last = -1;
         int count = offset;

         for(TDoubleLinkedList.TDoubleLink l = this.getLinkAt(offset); got(l.getNext()); l = l.getNext()) {
            if (l.getValue() == value) {
               last = count;
            }

            ++count;
         }

         return last;
      }
   }

   @Override
   public boolean contains(double value) {
      if (this.isEmpty()) {
         return false;
      } else {
         for(TDoubleLinkedList.TDoubleLink l = this.head; got(l); l = l.getNext()) {
            if (l.getValue() == value) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public TDoubleIterator iterator() {
      return new TDoubleIterator() {
         TDoubleLinkedList.TDoubleLink l = TDoubleLinkedList.this.head;
         TDoubleLinkedList.TDoubleLink current;

         @Override
         public double next() {
            if (TDoubleLinkedList.no(this.l)) {
               throw new NoSuchElementException();
            } else {
               double ret = this.l.getValue();
               this.current = this.l;
               this.l = this.l.getNext();
               return ret;
            }
         }

         @Override
         public boolean hasNext() {
            return TDoubleLinkedList.got(this.l);
         }

         @Override
         public void remove() {
            if (this.current == null) {
               throw new IllegalStateException();
            } else {
               TDoubleLinkedList.this.removeLink(this.current);
               this.current = null;
            }
         }
      };
   }

   @Override
   public TDoubleList grep(TDoubleProcedure condition) {
      TDoubleList ret = new TDoubleLinkedList();

      for(TDoubleLinkedList.TDoubleLink l = this.head; got(l); l = l.getNext()) {
         if (condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public TDoubleList inverseGrep(TDoubleProcedure condition) {
      TDoubleList ret = new TDoubleLinkedList();

      for(TDoubleLinkedList.TDoubleLink l = this.head; got(l); l = l.getNext()) {
         if (!condition.execute(l.getValue())) {
            ret.add(l.getValue());
         }
      }

      return ret;
   }

   @Override
   public double max() {
      double ret = Double.NEGATIVE_INFINITY;
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TDoubleLinkedList.TDoubleLink l = this.head; got(l); l = l.getNext()) {
            if (ret < l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public double min() {
      double ret = Double.POSITIVE_INFINITY;
      if (this.isEmpty()) {
         throw new IllegalStateException();
      } else {
         for(TDoubleLinkedList.TDoubleLink l = this.head; got(l); l = l.getNext()) {
            if (ret > l.getValue()) {
               ret = l.getValue();
            }
         }

         return ret;
      }
   }

   @Override
   public double sum() {
      double sum = 0.0;

      for(TDoubleLinkedList.TDoubleLink l = this.head; got(l); l = l.getNext()) {
         sum += l.getValue();
      }

      return sum;
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeDouble(this.no_entry_value);
      out.writeInt(this.size);
      TDoubleIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         double next = iterator.next();
         out.writeDouble(next);
      }
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.no_entry_value = in.readDouble();
      int len = in.readInt();

      for(int i = 0; i < len; ++i) {
         this.add(in.readDouble());
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
         TDoubleLinkedList that = (TDoubleLinkedList)o;
         if (this.no_entry_value != that.no_entry_value) {
            return false;
         } else if (this.size != that.size) {
            return false;
         } else {
            TDoubleIterator iterator = this.iterator();
            TDoubleIterator thatIterator = that.iterator();

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
      TDoubleIterator iterator = this.iterator();

      while(iterator.hasNext()) {
         result = 31 * result + HashFunctions.hash(iterator.next());
      }

      return result;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder("{");
      TDoubleIterator it = this.iterator();

      while(it.hasNext()) {
         double next = it.next();
         buf.append(next);
         if (it.hasNext()) {
            buf.append(", ");
         }
      }

      buf.append("}");
      return buf.toString();
   }

   class RemoveProcedure implements TDoubleProcedure {
      boolean changed = false;

      @Override
      public boolean execute(double value) {
         if (TDoubleLinkedList.this.remove(value)) {
            this.changed = true;
         }

         return true;
      }

      public boolean isChanged() {
         return this.changed;
      }
   }

   static class TDoubleLink {
      double value;
      TDoubleLinkedList.TDoubleLink previous;
      TDoubleLinkedList.TDoubleLink next;

      TDoubleLink(double value) {
         this.value = value;
      }

      public double getValue() {
         return this.value;
      }

      public void setValue(double value) {
         this.value = value;
      }

      public TDoubleLinkedList.TDoubleLink getPrevious() {
         return this.previous;
      }

      public void setPrevious(TDoubleLinkedList.TDoubleLink previous) {
         this.previous = previous;
      }

      public TDoubleLinkedList.TDoubleLink getNext() {
         return this.next;
      }

      public void setNext(TDoubleLinkedList.TDoubleLink next) {
         this.next = next;
      }
   }
}
