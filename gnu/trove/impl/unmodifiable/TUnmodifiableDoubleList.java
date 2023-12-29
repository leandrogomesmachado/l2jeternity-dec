package gnu.trove.impl.unmodifiable;

import gnu.trove.function.TDoubleFunction;
import gnu.trove.list.TDoubleList;
import gnu.trove.procedure.TDoubleProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TUnmodifiableDoubleList extends TUnmodifiableDoubleCollection implements TDoubleList {
   static final long serialVersionUID = -283967356065247728L;
   final TDoubleList list;

   public TUnmodifiableDoubleList(TDoubleList list) {
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
   public double get(int index) {
      return this.list.get(index);
   }

   @Override
   public int indexOf(double o) {
      return this.list.indexOf(o);
   }

   @Override
   public int lastIndexOf(double o) {
      return this.list.lastIndexOf(o);
   }

   @Override
   public double[] toArray(int offset, int len) {
      return this.list.toArray(offset, len);
   }

   @Override
   public double[] toArray(double[] dest, int offset, int len) {
      return this.list.toArray(dest, offset, len);
   }

   @Override
   public double[] toArray(double[] dest, int source_pos, int dest_pos, int len) {
      return this.list.toArray(dest, source_pos, dest_pos, len);
   }

   @Override
   public boolean forEachDescending(TDoubleProcedure procedure) {
      return this.list.forEachDescending(procedure);
   }

   @Override
   public int binarySearch(double value) {
      return this.list.binarySearch(value);
   }

   @Override
   public int binarySearch(double value, int fromIndex, int toIndex) {
      return this.list.binarySearch(value, fromIndex, toIndex);
   }

   @Override
   public int indexOf(int offset, double value) {
      return this.list.indexOf(offset, value);
   }

   @Override
   public int lastIndexOf(int offset, double value) {
      return this.list.lastIndexOf(offset, value);
   }

   @Override
   public TDoubleList grep(TDoubleProcedure condition) {
      return this.list.grep(condition);
   }

   @Override
   public TDoubleList inverseGrep(TDoubleProcedure condition) {
      return this.list.inverseGrep(condition);
   }

   @Override
   public double max() {
      return this.list.max();
   }

   @Override
   public double min() {
      return this.list.min();
   }

   @Override
   public double sum() {
      return this.list.sum();
   }

   @Override
   public TDoubleList subList(int fromIndex, int toIndex) {
      return new TUnmodifiableDoubleList(this.list.subList(fromIndex, toIndex));
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TUnmodifiableRandomAccessDoubleList(this.list) : this;
   }

   @Override
   public void add(double[] vals) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void add(double[] vals, int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public double removeAt(int offset) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void remove(int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, double value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, double[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, double[] values, int valOffset, int len) {
      throw new UnsupportedOperationException();
   }

   @Override
   public double set(int offset, double val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, double[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, double[] values, int valOffset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public double replace(int offset, double val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transformValues(TDoubleFunction function) {
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
   public void fill(double val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void fill(int fromIndex, int toIndex, double val) {
      throw new UnsupportedOperationException();
   }
}
