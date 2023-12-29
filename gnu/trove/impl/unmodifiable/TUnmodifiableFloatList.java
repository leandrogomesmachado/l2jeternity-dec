package gnu.trove.impl.unmodifiable;

import gnu.trove.function.TFloatFunction;
import gnu.trove.list.TFloatList;
import gnu.trove.procedure.TFloatProcedure;
import java.util.Random;
import java.util.RandomAccess;

public class TUnmodifiableFloatList extends TUnmodifiableFloatCollection implements TFloatList {
   static final long serialVersionUID = -283967356065247728L;
   final TFloatList list;

   public TUnmodifiableFloatList(TFloatList list) {
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
   public float get(int index) {
      return this.list.get(index);
   }

   @Override
   public int indexOf(float o) {
      return this.list.indexOf(o);
   }

   @Override
   public int lastIndexOf(float o) {
      return this.list.lastIndexOf(o);
   }

   @Override
   public float[] toArray(int offset, int len) {
      return this.list.toArray(offset, len);
   }

   @Override
   public float[] toArray(float[] dest, int offset, int len) {
      return this.list.toArray(dest, offset, len);
   }

   @Override
   public float[] toArray(float[] dest, int source_pos, int dest_pos, int len) {
      return this.list.toArray(dest, source_pos, dest_pos, len);
   }

   @Override
   public boolean forEachDescending(TFloatProcedure procedure) {
      return this.list.forEachDescending(procedure);
   }

   @Override
   public int binarySearch(float value) {
      return this.list.binarySearch(value);
   }

   @Override
   public int binarySearch(float value, int fromIndex, int toIndex) {
      return this.list.binarySearch(value, fromIndex, toIndex);
   }

   @Override
   public int indexOf(int offset, float value) {
      return this.list.indexOf(offset, value);
   }

   @Override
   public int lastIndexOf(int offset, float value) {
      return this.list.lastIndexOf(offset, value);
   }

   @Override
   public TFloatList grep(TFloatProcedure condition) {
      return this.list.grep(condition);
   }

   @Override
   public TFloatList inverseGrep(TFloatProcedure condition) {
      return this.list.inverseGrep(condition);
   }

   @Override
   public float max() {
      return this.list.max();
   }

   @Override
   public float min() {
      return this.list.min();
   }

   @Override
   public float sum() {
      return this.list.sum();
   }

   @Override
   public TFloatList subList(int fromIndex, int toIndex) {
      return new TUnmodifiableFloatList(this.list.subList(fromIndex, toIndex));
   }

   private Object readResolve() {
      return this.list instanceof RandomAccess ? new TUnmodifiableRandomAccessFloatList(this.list) : this;
   }

   @Override
   public void add(float[] vals) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void add(float[] vals, int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public float removeAt(int offset) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void remove(int offset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, float value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, float[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void insert(int offset, float[] values, int valOffset, int len) {
      throw new UnsupportedOperationException();
   }

   @Override
   public float set(int offset, float val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, float[] values) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void set(int offset, float[] values, int valOffset, int length) {
      throw new UnsupportedOperationException();
   }

   @Override
   public float replace(int offset, float val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transformValues(TFloatFunction function) {
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
   public void fill(float val) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void fill(int fromIndex, int toIndex, float val) {
      throw new UnsupportedOperationException();
   }
}
