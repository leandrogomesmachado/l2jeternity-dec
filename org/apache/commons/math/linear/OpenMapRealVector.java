package org.apache.commons.math.linear;

import java.io.Serializable;
import java.util.Iterator;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.OpenIntToDoubleHashMap;

public class OpenMapRealVector extends AbstractRealVector implements SparseRealVector, Serializable {
   public static final double DEFAULT_ZERO_TOLERANCE = 1.0E-12;
   private static final long serialVersionUID = 8772222695580707260L;
   private final OpenIntToDoubleHashMap entries;
   private final int virtualSize;
   private final double epsilon;

   public OpenMapRealVector() {
      this(0, 1.0E-12);
   }

   public OpenMapRealVector(int dimension) {
      this(dimension, 1.0E-12);
   }

   public OpenMapRealVector(int dimension, double epsilon) {
      this.virtualSize = dimension;
      this.entries = new OpenIntToDoubleHashMap(0.0);
      this.epsilon = epsilon;
   }

   protected OpenMapRealVector(OpenMapRealVector v, int resize) {
      this.virtualSize = v.getDimension() + resize;
      this.entries = new OpenIntToDoubleHashMap(v.entries);
      this.epsilon = v.epsilon;
   }

   public OpenMapRealVector(int dimension, int expectedSize) {
      this(dimension, expectedSize, 1.0E-12);
   }

   public OpenMapRealVector(int dimension, int expectedSize, double epsilon) {
      this.virtualSize = dimension;
      this.entries = new OpenIntToDoubleHashMap(expectedSize, 0.0);
      this.epsilon = epsilon;
   }

   public OpenMapRealVector(double[] values) {
      this(values, 1.0E-12);
   }

   public OpenMapRealVector(double[] values, double epsilon) {
      this.virtualSize = values.length;
      this.entries = new OpenIntToDoubleHashMap(0.0);
      this.epsilon = epsilon;

      for(int key = 0; key < values.length; ++key) {
         double value = values[key];
         if (!this.isDefaultValue(value)) {
            this.entries.put(key, value);
         }
      }
   }

   public OpenMapRealVector(Double[] values) {
      this(values, 1.0E-12);
   }

   public OpenMapRealVector(Double[] values, double epsilon) {
      this.virtualSize = values.length;
      this.entries = new OpenIntToDoubleHashMap(0.0);
      this.epsilon = epsilon;

      for(int key = 0; key < values.length; ++key) {
         double value = values[key];
         if (!this.isDefaultValue(value)) {
            this.entries.put(key, value);
         }
      }
   }

   public OpenMapRealVector(OpenMapRealVector v) {
      this.virtualSize = v.getDimension();
      this.entries = new OpenIntToDoubleHashMap(v.getEntries());
      this.epsilon = v.epsilon;
   }

   public OpenMapRealVector(RealVector v) {
      this.virtualSize = v.getDimension();
      this.entries = new OpenIntToDoubleHashMap(0.0);
      this.epsilon = 1.0E-12;

      for(int key = 0; key < this.virtualSize; ++key) {
         double value = v.getEntry(key);
         if (!this.isDefaultValue(value)) {
            this.entries.put(key, value);
         }
      }
   }

   private OpenIntToDoubleHashMap getEntries() {
      return this.entries;
   }

   protected boolean isDefaultValue(double value) {
      return FastMath.abs(value) < this.epsilon;
   }

   @Override
   public RealVector add(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      return (RealVector)(v instanceof OpenMapRealVector ? this.add((OpenMapRealVector)v) : super.add(v));
   }

   public OpenMapRealVector add(OpenMapRealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      boolean copyThis = this.entries.size() > v.entries.size();
      OpenMapRealVector res = copyThis ? this.copy() : v.copy();
      OpenIntToDoubleHashMap.Iterator iter = copyThis ? v.entries.iterator() : this.entries.iterator();
      OpenIntToDoubleHashMap randomAccess = copyThis ? this.entries : v.entries;

      while(iter.hasNext()) {
         iter.advance();
         int key = iter.key();
         if (randomAccess.containsKey(key)) {
            res.setEntry(key, randomAccess.get(key) + iter.value());
         } else {
            res.setEntry(key, iter.value());
         }
      }

      return res;
   }

   public OpenMapRealVector append(OpenMapRealVector v) {
      OpenMapRealVector res = new OpenMapRealVector(this, v.getDimension());
      OpenIntToDoubleHashMap.Iterator iter = v.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key() + this.virtualSize, iter.value());
      }

      return res;
   }

   public OpenMapRealVector append(RealVector v) {
      return v instanceof OpenMapRealVector ? this.append((OpenMapRealVector)v) : this.append(v.getData());
   }

   public OpenMapRealVector append(double d) {
      OpenMapRealVector res = new OpenMapRealVector(this, 1);
      res.setEntry(this.virtualSize, d);
      return res;
   }

   public OpenMapRealVector append(double[] a) {
      OpenMapRealVector res = new OpenMapRealVector(this, a.length);

      for(int i = 0; i < a.length; ++i) {
         res.setEntry(i + this.virtualSize, a[i]);
      }

      return res;
   }

   public OpenMapRealVector copy() {
      return new OpenMapRealVector(this);
   }

   public double dotProduct(OpenMapRealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      boolean thisIsSmaller = this.entries.size() < v.entries.size();
      OpenIntToDoubleHashMap.Iterator iter = thisIsSmaller ? this.entries.iterator() : v.entries.iterator();
      OpenIntToDoubleHashMap larger = thisIsSmaller ? v.entries : this.entries;

      double d;
      for(d = 0.0; iter.hasNext(); d += iter.value() * larger.get(iter.key())) {
         iter.advance();
      }

      return d;
   }

   @Override
   public double dotProduct(RealVector v) throws IllegalArgumentException {
      return v instanceof OpenMapRealVector ? this.dotProduct((OpenMapRealVector)v) : super.dotProduct(v);
   }

   public OpenMapRealVector ebeDivide(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      OpenMapRealVector res = new OpenMapRealVector(this);
      OpenIntToDoubleHashMap.Iterator iter = res.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key(), iter.value() / v.getEntry(iter.key()));
      }

      return res;
   }

   public OpenMapRealVector ebeDivide(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      OpenMapRealVector res = new OpenMapRealVector(this);
      OpenIntToDoubleHashMap.Iterator iter = res.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key(), iter.value() / v[iter.key()]);
      }

      return res;
   }

   public OpenMapRealVector ebeMultiply(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      OpenMapRealVector res = new OpenMapRealVector(this);
      OpenIntToDoubleHashMap.Iterator iter = res.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
      }

      return res;
   }

   public OpenMapRealVector ebeMultiply(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      OpenMapRealVector res = new OpenMapRealVector(this);
      OpenIntToDoubleHashMap.Iterator iter = res.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         res.setEntry(iter.key(), iter.value() * v[iter.key()]);
      }

      return res;
   }

   public OpenMapRealVector getSubVector(int index, int n) throws MatrixIndexException {
      this.checkIndex(index);
      this.checkIndex(index + n - 1);
      OpenMapRealVector res = new OpenMapRealVector(n);
      int end = index + n;
      OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         int key = iter.key();
         if (key >= index && key < end) {
            res.setEntry(key - index, iter.value());
         }
      }

      return res;
   }

   @Override
   public double[] getData() {
      double[] res = new double[this.virtualSize];

      for(OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator(); iter.hasNext(); res[iter.key()] = iter.value()) {
         iter.advance();
      }

      return res;
   }

   @Override
   public int getDimension() {
      return this.virtualSize;
   }

   public double getDistance(OpenMapRealVector v) throws IllegalArgumentException {
      OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator();

      double res;
      double delta;
      for(res = 0.0; iter.hasNext(); res += delta * delta) {
         iter.advance();
         int key = iter.key();
         delta = iter.value() - v.getEntry(key);
      }

      iter = v.getEntries().iterator();

      while(iter.hasNext()) {
         iter.advance();
         int key = iter.key();
         if (!this.entries.containsKey(key)) {
            delta = iter.value();
            res += delta * delta;
         }
      }

      return FastMath.sqrt(res);
   }

   @Override
   public double getDistance(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      return v instanceof OpenMapRealVector ? this.getDistance((OpenMapRealVector)v) : this.getDistance(v.getData());
   }

   @Override
   public double getDistance(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double res = 0.0;

      for(int i = 0; i < v.length; ++i) {
         double delta = this.entries.get(i) - v[i];
         res += delta * delta;
      }

      return FastMath.sqrt(res);
   }

   @Override
   public double getEntry(int index) throws MatrixIndexException {
      this.checkIndex(index);
      return this.entries.get(index);
   }

   public double getL1Distance(OpenMapRealVector v) {
      double max = 0.0;

      double delta;
      for(OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator(); iter.hasNext(); max += delta) {
         iter.advance();
         delta = FastMath.abs(iter.value() - v.getEntry(iter.key()));
      }

      OpenIntToDoubleHashMap.Iterator var8 = v.getEntries().iterator();

      while(var8.hasNext()) {
         var8.advance();
         int key = var8.key();
         if (!this.entries.containsKey(key)) {
            double delta = FastMath.abs(var8.value());
            max += FastMath.abs(delta);
         }
      }

      return max;
   }

   @Override
   public double getL1Distance(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      return v instanceof OpenMapRealVector ? this.getL1Distance((OpenMapRealVector)v) : this.getL1Distance(v.getData());
   }

   @Override
   public double getL1Distance(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double max = 0.0;

      for(int i = 0; i < v.length; ++i) {
         double delta = FastMath.abs(this.getEntry(i) - v[i]);
         max += delta;
      }

      return max;
   }

   private double getLInfDistance(OpenMapRealVector v) {
      double max = 0.0;
      OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         double delta = FastMath.abs(iter.value() - v.getEntry(iter.key()));
         if (delta > max) {
            max = delta;
         }
      }

      iter = v.getEntries().iterator();

      while(iter.hasNext()) {
         iter.advance();
         int key = iter.key();
         if (!this.entries.containsKey(key) && iter.value() > max) {
            max = iter.value();
         }
      }

      return max;
   }

   @Override
   public double getLInfDistance(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      return v instanceof OpenMapRealVector ? this.getLInfDistance((OpenMapRealVector)v) : this.getLInfDistance(v.getData());
   }

   @Override
   public double getLInfDistance(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double max = 0.0;

      for(int i = 0; i < v.length; ++i) {
         double delta = FastMath.abs(this.getEntry(i) - v[i]);
         if (delta > max) {
            max = delta;
         }
      }

      return max;
   }

   @Override
   public boolean isInfinite() {
      boolean infiniteFound = false;
      OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         double value = iter.value();
         if (Double.isNaN(value)) {
            return false;
         }

         if (Double.isInfinite(value)) {
            infiniteFound = true;
         }
      }

      return infiniteFound;
   }

   @Override
   public boolean isNaN() {
      OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         if (Double.isNaN(iter.value())) {
            return true;
         }
      }

      return false;
   }

   public OpenMapRealVector mapAdd(double d) {
      return this.copy().mapAddToSelf(d);
   }

   public OpenMapRealVector mapAddToSelf(double d) {
      for(int i = 0; i < this.virtualSize; ++i) {
         this.setEntry(i, this.getEntry(i) + d);
      }

      return this;
   }

   @Override
   public RealMatrix outerProduct(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      RealMatrix res = new OpenMapRealMatrix(this.virtualSize, this.virtualSize);
      OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator();

      while(iter.hasNext()) {
         iter.advance();
         int row = iter.key();
         double value = iter.value();

         for(int col = 0; col < this.virtualSize; ++col) {
            res.setEntry(row, col, value * v[col]);
         }
      }

      return res;
   }

   @Override
   public RealVector projection(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      return v.mapMultiply(this.dotProduct(v) / v.dotProduct(v));
   }

   public OpenMapRealVector projection(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      return (OpenMapRealVector)this.projection(new OpenMapRealVector(v));
   }

   @Override
   public void setEntry(int index, double value) throws MatrixIndexException {
      this.checkIndex(index);
      if (!this.isDefaultValue(value)) {
         this.entries.put(index, value);
      } else if (this.entries.containsKey(index)) {
         this.entries.remove(index);
      }
   }

   @Override
   public void setSubVector(int index, RealVector v) throws MatrixIndexException {
      this.checkIndex(index);
      this.checkIndex(index + v.getDimension() - 1);
      this.setSubVector(index, v.getData());
   }

   @Override
   public void setSubVector(int index, double[] v) throws MatrixIndexException {
      this.checkIndex(index);
      this.checkIndex(index + v.length - 1);

      for(int i = 0; i < v.length; ++i) {
         this.setEntry(i + index, v[i]);
      }
   }

   @Override
   public void set(double value) {
      for(int i = 0; i < this.virtualSize; ++i) {
         this.setEntry(i, value);
      }
   }

   public OpenMapRealVector subtract(OpenMapRealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      OpenMapRealVector res = this.copy();
      OpenIntToDoubleHashMap.Iterator iter = v.getEntries().iterator();

      while(iter.hasNext()) {
         iter.advance();
         int key = iter.key();
         if (this.entries.containsKey(key)) {
            res.setEntry(key, this.entries.get(key) - iter.value());
         } else {
            res.setEntry(key, -iter.value());
         }
      }

      return res;
   }

   public OpenMapRealVector subtract(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
      return v instanceof OpenMapRealVector ? this.subtract((OpenMapRealVector)v) : this.subtract(v.getData());
   }

   public OpenMapRealVector subtract(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      OpenMapRealVector res = new OpenMapRealVector(this);

      for(int i = 0; i < v.length; ++i) {
         if (this.entries.containsKey(i)) {
            res.setEntry(i, this.entries.get(i) - v[i]);
         } else {
            res.setEntry(i, -v[i]);
         }
      }

      return res;
   }

   public OpenMapRealVector unitVector() {
      OpenMapRealVector res = this.copy();
      res.unitize();
      return res;
   }

   @Override
   public void unitize() {
      double norm = this.getNorm();
      if (this.isDefaultValue(norm)) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.CANNOT_NORMALIZE_A_ZERO_NORM_VECTOR);
      } else {
         OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator();

         while(iter.hasNext()) {
            iter.advance();
            this.entries.put(iter.key(), iter.value() / norm);
         }
      }
   }

   @Override
   public double[] toArray() {
      return this.getData();
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      long temp = Double.doubleToLongBits(this.epsilon);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      result = 31 * result + this.virtualSize;

      for(OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator(); iter.hasNext(); result = 31 * result + (int)(temp ^ temp >> 32)) {
         iter.advance();
         temp = Double.doubleToLongBits(iter.value());
      }

      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof OpenMapRealVector)) {
         return false;
      } else {
         OpenMapRealVector other = (OpenMapRealVector)obj;
         if (this.virtualSize != other.virtualSize) {
            return false;
         } else if (Double.doubleToLongBits(this.epsilon) != Double.doubleToLongBits(other.epsilon)) {
            return false;
         } else {
            OpenIntToDoubleHashMap.Iterator iter = this.entries.iterator();

            while(iter.hasNext()) {
               iter.advance();
               double test = other.getEntry(iter.key());
               if (Double.doubleToLongBits(test) != Double.doubleToLongBits(iter.value())) {
                  return false;
               }
            }

            iter = other.getEntries().iterator();

            while(iter.hasNext()) {
               iter.advance();
               double test = iter.value();
               if (Double.doubleToLongBits(test) != Double.doubleToLongBits(this.getEntry(iter.key()))) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   @Deprecated
   public double getSparcity() {
      return this.getSparsity();
   }

   public double getSparsity() {
      return (double)this.entries.size() / (double)this.getDimension();
   }

   @Override
   public Iterator<RealVector.Entry> sparseIterator() {
      return new OpenMapRealVector.OpenMapSparseIterator();
   }

   protected class OpenMapEntry extends RealVector.Entry {
      private final OpenIntToDoubleHashMap.Iterator iter;

      protected OpenMapEntry(OpenIntToDoubleHashMap.Iterator iter) {
         this.iter = iter;
      }

      @Override
      public double getValue() {
         return this.iter.value();
      }

      @Override
      public void setValue(double value) {
         OpenMapRealVector.this.entries.put(this.iter.key(), value);
      }

      @Override
      public int getIndex() {
         return this.iter.key();
      }
   }

   protected class OpenMapSparseIterator implements Iterator<RealVector.Entry> {
      private final OpenIntToDoubleHashMap.Iterator iter = OpenMapRealVector.this.entries.iterator();
      private final RealVector.Entry current = OpenMapRealVector.this.new OpenMapEntry(this.iter);

      @Override
      public boolean hasNext() {
         return this.iter.hasNext();
      }

      public RealVector.Entry next() {
         this.iter.advance();
         return this.current;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException("Not supported");
      }
   }
}
