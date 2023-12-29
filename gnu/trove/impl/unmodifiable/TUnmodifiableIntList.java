package gnu.trove.impl.unmodifiable;

import gnu.trove.function.TIntFunction;
import gnu.trove.list.TIntList;
import gnu.trove.procedure.TIntProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TUnmodifiableIntList extends TUnmodifiableIntCollection implements TIntList {
   static final long serialVersionUID = -283967356065247728L;
   final TIntList list;

   public TUnmodifiableIntList(TIntList list) {
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
   public int get(int index) {
      return this.list.get(index);
   }

   @Override
   public int indexOf(int o) {
      return this.list.indexOf(o);
   }

   @Override
   public int lastIndexOf(int o) {
      return this.list.lastIndexOf(o);
   }

   @Override
   public int[] toArray(int offset, int len) {
      return this.list.toArray(offset, len);
   }

   @Override
   public int[] toArray(int[] dest, int offset, int len) {
      return this.list.toArray(dest, offset, len);
   }

   @Override
   public int[] toArray(int[] dest, int source_pos, int dest_pos, int len) {
      return this.list.toArray(dest, source_pos, dest_pos, len);
   }

   @Override
   public boolean forEachDescending(TIntProcedure procedure) {
      return this.list.forEachDescending(procedure);
   }

   @Override
   public int binarySearch(int value) {
      return this.list.binarySearch(value);
   }

   @Override
   public int binarySearch(int value, int fromIndex, int toIndex) {
      return this.list.binarySearch(value, fromIndex, toIndex);
   }

   @Override
   public int indexOf(int offset, int value) {
      return this.list.indexOf(offset, value);
   }

   @Override
   public int lastIndexOf(int offset, int value) {
      return this.list.lastIndexOf(offset, value);
   }

   @Override
   public TIntList grep(TIntProcedure condition) {
      return this.list.grep(condition);
   }

   @Override
   public TIntList inverseGrep(TIntProcedure condition) {
      return this.list.inverseGrep(condition);
   }

   @Override
   public int max() {
      return this.list.max();
   }

   @Override
   public int min() {
      return this.list.min();
   }

   @Override
   public int sum() {
      return this.list.sum();
   }

   @Override
   public TIntList subList(int fromIndex, int toIndex) {
      return new TUnmodifiableIntList(this.list.subList(fromIndex, toIndex));
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TUnmodifiableRandomAccessIntList(this.list) : this;
   }

   @Override
   public void add(int[] vals) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void add(int[] vals, int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int removeAt(int offset) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void remove(int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, int value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, int[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, int[] values, int valOffset, int len) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int set(int offset, int val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, int[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, int[] values, int valOffset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int replace(int offset, int val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transformValues(TIntFunction function) {
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
   public void fill(int val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void fill(int fromIndex, int toIndex, int val) {
      throw new UnsupportedOperationException();
   }
}
