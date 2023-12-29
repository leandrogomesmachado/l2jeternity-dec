package gnu.trove.impl.unmodifiable;

import gnu.trove.function.TShortFunction;
import gnu.trove.list.TShortList;
import gnu.trove.procedure.TShortProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TUnmodifiableShortList extends TUnmodifiableShortCollection implements TShortList {
   static final long serialVersionUID = -283967356065247728L;
   final TShortList list;

   public TUnmodifiableShortList(TShortList list) {
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
   public short get(int index) {
      return this.list.get(index);
   }

   @Override
   public int indexOf(short o) {
      return this.list.indexOf(o);
   }

   @Override
   public int lastIndexOf(short o) {
      return this.list.lastIndexOf(o);
   }

   @Override
   public short[] toArray(int offset, int len) {
      return this.list.toArray(offset, len);
   }

   @Override
   public short[] toArray(short[] dest, int offset, int len) {
      return this.list.toArray(dest, offset, len);
   }

   @Override
   public short[] toArray(short[] dest, int source_pos, int dest_pos, int len) {
      return this.list.toArray(dest, source_pos, dest_pos, len);
   }

   @Override
   public boolean forEachDescending(TShortProcedure procedure) {
      return this.list.forEachDescending(procedure);
   }

   @Override
   public int binarySearch(short value) {
      return this.list.binarySearch(value);
   }

   @Override
   public int binarySearch(short value, int fromIndex, int toIndex) {
      return this.list.binarySearch(value, fromIndex, toIndex);
   }

   @Override
   public int indexOf(int offset, short value) {
      return this.list.indexOf(offset, value);
   }

   @Override
   public int lastIndexOf(int offset, short value) {
      return this.list.lastIndexOf(offset, value);
   }

   @Override
   public TShortList grep(TShortProcedure condition) {
      return this.list.grep(condition);
   }

   @Override
   public TShortList inverseGrep(TShortProcedure condition) {
      return this.list.inverseGrep(condition);
   }

   @Override
   public short max() {
      return this.list.max();
   }

   @Override
   public short min() {
      return this.list.min();
   }

   @Override
   public short sum() {
      return this.list.sum();
   }

   @Override
   public TShortList subList(int fromIndex, int toIndex) {
      return new TUnmodifiableShortList(this.list.subList(fromIndex, toIndex));
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TUnmodifiableRandomAccessShortList(this.list) : this;
   }

   @Override
   public void add(short[] vals) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void add(short[] vals, int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public short removeAt(int offset) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void remove(int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, short value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, short[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, short[] values, int valOffset, int len) {
      throw new UnsupportedOperationException();
   }

   @Override
   public short set(int offset, short val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, short[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, short[] values, int valOffset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public short replace(int offset, short val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transformValues(TShortFunction function) {
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
   public void fill(short val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void fill(int fromIndex, int toIndex, short val) {
      throw new UnsupportedOperationException();
   }
}
