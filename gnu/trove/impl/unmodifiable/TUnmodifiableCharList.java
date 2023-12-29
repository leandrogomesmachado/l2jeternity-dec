package gnu.trove.impl.unmodifiable;

import gnu.trove.function.TCharFunction;
import gnu.trove.list.TCharList;
import gnu.trove.procedure.TCharProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TUnmodifiableCharList extends TUnmodifiableCharCollection implements TCharList {
   static final long serialVersionUID = -283967356065247728L;
   final TCharList list;

   public TUnmodifiableCharList(TCharList list) {
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
   public char get(int index) {
      return this.list.get(index);
   }

   @Override
   public int indexOf(char o) {
      return this.list.indexOf(o);
   }

   @Override
   public int lastIndexOf(char o) {
      return this.list.lastIndexOf(o);
   }

   @Override
   public char[] toArray(int offset, int len) {
      return this.list.toArray(offset, len);
   }

   @Override
   public char[] toArray(char[] dest, int offset, int len) {
      return this.list.toArray(dest, offset, len);
   }

   @Override
   public char[] toArray(char[] dest, int source_pos, int dest_pos, int len) {
      return this.list.toArray(dest, source_pos, dest_pos, len);
   }

   @Override
   public boolean forEachDescending(TCharProcedure procedure) {
      return this.list.forEachDescending(procedure);
   }

   @Override
   public int binarySearch(char value) {
      return this.list.binarySearch(value);
   }

   @Override
   public int binarySearch(char value, int fromIndex, int toIndex) {
      return this.list.binarySearch(value, fromIndex, toIndex);
   }

   @Override
   public int indexOf(int offset, char value) {
      return this.list.indexOf(offset, value);
   }

   @Override
   public int lastIndexOf(int offset, char value) {
      return this.list.lastIndexOf(offset, value);
   }

   @Override
   public TCharList grep(TCharProcedure condition) {
      return this.list.grep(condition);
   }

   @Override
   public TCharList inverseGrep(TCharProcedure condition) {
      return this.list.inverseGrep(condition);
   }

   @Override
   public char max() {
      return this.list.max();
   }

   @Override
   public char min() {
      return this.list.min();
   }

   @Override
   public char sum() {
      return this.list.sum();
   }

   @Override
   public TCharList subList(int fromIndex, int toIndex) {
      return new TUnmodifiableCharList(this.list.subList(fromIndex, toIndex));
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TUnmodifiableRandomAccessCharList(this.list) : this;
   }

   @Override
   public void add(char[] vals) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void add(char[] vals, int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public char removeAt(int offset) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void remove(int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, char value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, char[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, char[] values, int valOffset, int len) {
      throw new UnsupportedOperationException();
   }

   @Override
   public char set(int offset, char val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, char[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, char[] values, int valOffset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public char replace(int offset, char val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transformValues(TCharFunction function) {
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
   public void fill(char val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void fill(int fromIndex, int toIndex, char val) {
      throw new UnsupportedOperationException();
   }
}
