package gnu.trove.impl.unmodifiable;

import gnu.trove.function.TLongFunction;
import gnu.trove.list.TLongList;
import gnu.trove.procedure.TLongProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TUnmodifiableLongList extends TUnmodifiableLongCollection implements TLongList {
   static final long serialVersionUID = -283967356065247728L;
   final TLongList list;

   public TUnmodifiableLongList(TLongList list) {
      super(list);
      this.list = list;
   }

   @Override
   public boolean equals(Object o) {
      return o == this || this.list.equals(o);
   }

   @Override
   public int hashCode() {
      return this.list.hashCode();
   }

   @Override
   public long get(int index) {
      return this.list.get(index);
   }

   @Override
   public int indexOf(long o) {
      return this.list.indexOf(o);
   }

   @Override
   public int lastIndexOf(long o) {
      return this.list.lastIndexOf(o);
   }

   @Override
   public long[] toArray(int offset, int len) {
      return this.list.toArray(offset, len);
   }

   @Override
   public long[] toArray(long[] dest, int offset, int len) {
      return this.list.toArray(dest, offset, len);
   }

   @Override
   public long[] toArray(long[] dest, int source_pos, int dest_pos, int len) {
      return this.list.toArray(dest, source_pos, dest_pos, len);
   }

   @Override
   public boolean forEachDescending(TLongProcedure procedure) {
      return this.list.forEachDescending(procedure);
   }

   @Override
   public int binarySearch(long value) {
      return this.list.binarySearch(value);
   }

   @Override
   public int binarySearch(long value, int fromIndex, int toIndex) {
      return this.list.binarySearch(value, fromIndex, toIndex);
   }

   @Override
   public int indexOf(int offset, long value) {
      return this.list.indexOf(offset, value);
   }

   @Override
   public int lastIndexOf(int offset, long value) {
      return this.list.lastIndexOf(offset, value);
   }

   @Override
   public TLongList grep(TLongProcedure condition) {
      return this.list.grep(condition);
   }

   @Override
   public TLongList inverseGrep(TLongProcedure condition) {
      return this.list.inverseGrep(condition);
   }

   @Override
   public long max() {
      return this.list.max();
   }

   @Override
   public long min() {
      return this.list.min();
   }

   @Override
   public long sum() {
      return this.list.sum();
   }

   @Override
   public TLongList subList(int fromIndex, int toIndex) {
      return new TUnmodifiableLongList(this.list.subList(fromIndex, toIndex));
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TUnmodifiableRandomAccessLongList(this.list) : this;
   }

   @Override
   public void add(long[] vals) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void add(long[] vals, int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public long removeAt(int offset) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void remove(int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, long value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, long[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, long[] values, int valOffset, int len) {
      throw new UnsupportedOperationException();
   }

   @Override
   public long set(int offset, long val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, long[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, long[] values, int valOffset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public long replace(int offset, long val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transformValues(TLongFunction function) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void reverse() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void reverse(int from, int to) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void shuffle(Random rand) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void sort() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void sort(int fromIndex, int toIndex) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void fill(long val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void fill(int fromIndex, int toIndex, long val) {
      throw new UnsupportedOperationException();
   }
}
