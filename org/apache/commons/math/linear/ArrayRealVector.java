package org.apache.commons.math.linear;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class ArrayRealVector extends AbstractRealVector implements Serializable {
   private static final long serialVersionUID = -1097961340710804027L;
   private static final RealVectorFormat DEFAULT_FORMAT = RealVectorFormat.getInstance();
   protected double[] data;

   public ArrayRealVector() {
      this.data = new double[0];
   }

   public ArrayRealVector(int size) {
      this.data = new double[size];
   }

   public ArrayRealVector(int size, double preset) {
      this.data = new double[size];
      Arrays.fill(this.data, preset);
   }

   public ArrayRealVector(double[] d) {
      this.data = (double[])d.clone();
   }

   public ArrayRealVector(double[] d, boolean copyArray) {
      this.data = copyArray ? (double[])d.clone() : d;
   }

   public ArrayRealVector(double[] d, int pos, int size) {
      if (d.length < pos + size) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POSITION_SIZE_MISMATCH_INPUT_ARRAY, pos, size, d.length);
      } else {
         this.data = new double[size];
         System.arraycopy(d, pos, this.data, 0, size);
      }
   }

   public ArrayRealVector(Double[] d) {
      this.data = new double[d.length];

      for(int i = 0; i < d.length; ++i) {
         this.data[i] = d[i];
      }
   }

   public ArrayRealVector(Double[] d, int pos, int size) {
      if (d.length < pos + size) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.POSITION_SIZE_MISMATCH_INPUT_ARRAY, pos, size, d.length);
      } else {
         this.data = new double[size];

         for(int i = pos; i < pos + size; ++i) {
            this.data[i - pos] = d[i];
         }
      }
   }

   public ArrayRealVector(RealVector v) {
      this.data = new double[v.getDimension()];

      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = v.getEntry(i);
      }
   }

   public ArrayRealVector(ArrayRealVector v) {
      this(v, true);
   }

   public ArrayRealVector(ArrayRealVector v, boolean deep) {
      this.data = deep ? (double[])v.data.clone() : v.data;
   }

   public ArrayRealVector(ArrayRealVector v1, ArrayRealVector v2) {
      this.data = new double[v1.data.length + v2.data.length];
      System.arraycopy(v1.data, 0, this.data, 0, v1.data.length);
      System.arraycopy(v2.data, 0, this.data, v1.data.length, v2.data.length);
   }

   public ArrayRealVector(ArrayRealVector v1, RealVector v2) {
      int l1 = v1.data.length;
      int l2 = v2.getDimension();
      this.data = new double[l1 + l2];
      System.arraycopy(v1.data, 0, this.data, 0, l1);

      for(int i = 0; i < l2; ++i) {
         this.data[l1 + i] = v2.getEntry(i);
      }
   }

   public ArrayRealVector(RealVector v1, ArrayRealVector v2) {
      int l1 = v1.getDimension();
      int l2 = v2.data.length;
      this.data = new double[l1 + l2];

      for(int i = 0; i < l1; ++i) {
         this.data[i] = v1.getEntry(i);
      }

      System.arraycopy(v2.data, 0, this.data, l1, l2);
   }

   public ArrayRealVector(ArrayRealVector v1, double[] v2) {
      int l1 = v1.getDimension();
      int l2 = v2.length;
      this.data = new double[l1 + l2];
      System.arraycopy(v1.data, 0, this.data, 0, l1);
      System.arraycopy(v2, 0, this.data, l1, l2);
   }

   public ArrayRealVector(double[] v1, ArrayRealVector v2) {
      int l1 = v1.length;
      int l2 = v2.getDimension();
      this.data = new double[l1 + l2];
      System.arraycopy(v1, 0, this.data, 0, l1);
      System.arraycopy(v2.data, 0, this.data, l1, l2);
   }

   public ArrayRealVector(double[] v1, double[] v2) {
      int l1 = v1.length;
      int l2 = v2.length;
      this.data = new double[l1 + l2];
      System.arraycopy(v1, 0, this.data, 0, l1);
      System.arraycopy(v2, 0, this.data, l1, l2);
   }

   @Override
   public AbstractRealVector copy() {
      return new ArrayRealVector(this, true);
   }

   @Override
   public RealVector add(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         return this.add((ArrayRealVector)v);
      } else {
         this.checkVectorDimensions(v);
         double[] out = (double[])this.data.clone();

         RealVector.Entry e;
         int var10001;
         for(Iterator<RealVector.Entry> it = v.sparseIterator(); it.hasNext() && (e = it.next()) != null; out[var10001] += e.getValue()) {
            var10001 = e.getIndex();
         }

         return new ArrayRealVector(out, false);
      }
   }

   @Override
   public RealVector add(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double[] out = (double[])this.data.clone();

      for(int i = 0; i < this.data.length; ++i) {
         out[i] += v[i];
      }

      return new ArrayRealVector(out, false);
   }

   public ArrayRealVector add(ArrayRealVector v) throws IllegalArgumentException {
      return (ArrayRealVector)this.add(v.data);
   }

   @Override
   public RealVector subtract(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         return this.subtract((ArrayRealVector)v);
      } else {
         this.checkVectorDimensions(v);
         double[] out = (double[])this.data.clone();

         RealVector.Entry e;
         int var10001;
         for(Iterator<RealVector.Entry> it = v.sparseIterator(); it.hasNext() && (e = it.next()) != null; out[var10001] -= e.getValue()) {
            var10001 = e.getIndex();
         }

         return new ArrayRealVector(out, false);
      }
   }

   @Override
   public RealVector subtract(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double[] out = (double[])this.data.clone();

      for(int i = 0; i < this.data.length; ++i) {
         out[i] -= v[i];
      }

      return new ArrayRealVector(out, false);
   }

   public ArrayRealVector subtract(ArrayRealVector v) throws IllegalArgumentException {
      return (ArrayRealVector)this.subtract(v.data);
   }

   @Override
   public RealVector mapAddToSelf(double d) {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] += d;
      }

      return this;
   }

   @Override
   public RealVector mapSubtractToSelf(double d) {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] -= d;
      }

      return this;
   }

   @Override
   public RealVector mapMultiplyToSelf(double d) {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] *= d;
      }

      return this;
   }

   @Override
   public RealVector mapDivideToSelf(double d) {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] /= d;
      }

      return this;
   }

   @Override
   public RealVector mapPowToSelf(double d) {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.pow(this.data[i], d);
      }

      return this;
   }

   @Override
   public RealVector mapExpToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.exp(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapExpm1ToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.expm1(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapLogToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.log(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapLog10ToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.log10(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapLog1pToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.log1p(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapCoshToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.cosh(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapSinhToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.sinh(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapTanhToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.tanh(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapCosToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.cos(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapSinToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.sin(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapTanToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.tan(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapAcosToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.acos(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapAsinToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.asin(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapAtanToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.atan(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapInvToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = 1.0 / this.data[i];
      }

      return this;
   }

   @Override
   public RealVector mapAbsToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.abs(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapSqrtToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.sqrt(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapCbrtToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.cbrt(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapCeilToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.ceil(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapFloorToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.floor(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapRintToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.rint(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapSignumToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.signum(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector mapUlpToSelf() {
      for(int i = 0; i < this.data.length; ++i) {
         this.data[i] = FastMath.ulp(this.data[i]);
      }

      return this;
   }

   @Override
   public RealVector ebeMultiply(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         return this.ebeMultiply((ArrayRealVector)v);
      } else {
         this.checkVectorDimensions(v);
         double[] out = (double[])this.data.clone();

         for(int i = 0; i < this.data.length; ++i) {
            out[i] *= v.getEntry(i);
         }

         return new ArrayRealVector(out, false);
      }
   }

   @Override
   public RealVector ebeMultiply(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double[] out = (double[])this.data.clone();

      for(int i = 0; i < this.data.length; ++i) {
         out[i] *= v[i];
      }

      return new ArrayRealVector(out, false);
   }

   public ArrayRealVector ebeMultiply(ArrayRealVector v) throws IllegalArgumentException {
      return (ArrayRealVector)this.ebeMultiply(v.data);
   }

   @Override
   public RealVector ebeDivide(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         return this.ebeDivide((ArrayRealVector)v);
      } else {
         this.checkVectorDimensions(v);
         double[] out = (double[])this.data.clone();

         for(int i = 0; i < this.data.length; ++i) {
            out[i] /= v.getEntry(i);
         }

         return new ArrayRealVector(out, false);
      }
   }

   @Override
   public RealVector ebeDivide(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double[] out = (double[])this.data.clone();

      for(int i = 0; i < this.data.length; ++i) {
         out[i] /= v[i];
      }

      return new ArrayRealVector(out, false);
   }

   public ArrayRealVector ebeDivide(ArrayRealVector v) throws IllegalArgumentException {
      return (ArrayRealVector)this.ebeDivide(v.data);
   }

   @Override
   public double[] getData() {
      return (double[])this.data.clone();
   }

   public double[] getDataRef() {
      return this.data;
   }

   @Override
   public double dotProduct(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         return this.dotProduct((ArrayRealVector)v);
      } else {
         this.checkVectorDimensions(v);
         double dot = 0.0;
         Iterator<RealVector.Entry> it = v.sparseIterator();

         RealVector.Entry e;
         while(it.hasNext() && (e = it.next()) != null) {
            dot += this.data[e.getIndex()] * e.getValue();
         }

         return dot;
      }
   }

   @Override
   public double dotProduct(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double dot = 0.0;

      for(int i = 0; i < this.data.length; ++i) {
         dot += this.data[i] * v[i];
      }

      return dot;
   }

   public double dotProduct(ArrayRealVector v) throws IllegalArgumentException {
      return this.dotProduct(v.data);
   }

   @Override
   public double getNorm() {
      double sum = 0.0;

      for(double a : this.data) {
         sum += a * a;
      }

      return FastMath.sqrt(sum);
   }

   @Override
   public double getL1Norm() {
      double sum = 0.0;

      for(double a : this.data) {
         sum += FastMath.abs(a);
      }

      return sum;
   }

   @Override
   public double getLInfNorm() {
      double max = 0.0;

      for(double a : this.data) {
         max = FastMath.max(max, FastMath.abs(a));
      }

      return max;
   }

   @Override
   public double getDistance(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         return this.getDistance((ArrayRealVector)v);
      } else {
         this.checkVectorDimensions(v);
         double sum = 0.0;

         for(int i = 0; i < this.data.length; ++i) {
            double delta = this.data[i] - v.getEntry(i);
            sum += delta * delta;
         }

         return FastMath.sqrt(sum);
      }
   }

   @Override
   public double getDistance(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double sum = 0.0;

      for(int i = 0; i < this.data.length; ++i) {
         double delta = this.data[i] - v[i];
         sum += delta * delta;
      }

      return FastMath.sqrt(sum);
   }

   public double getDistance(ArrayRealVector v) throws IllegalArgumentException {
      return this.getDistance(v.data);
   }

   @Override
   public double getL1Distance(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         return this.getL1Distance((ArrayRealVector)v);
      } else {
         this.checkVectorDimensions(v);
         double sum = 0.0;

         for(int i = 0; i < this.data.length; ++i) {
            double delta = this.data[i] - v.getEntry(i);
            sum += FastMath.abs(delta);
         }

         return sum;
      }
   }

   @Override
   public double getL1Distance(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double sum = 0.0;

      for(int i = 0; i < this.data.length; ++i) {
         double delta = this.data[i] - v[i];
         sum += FastMath.abs(delta);
      }

      return sum;
   }

   public double getL1Distance(ArrayRealVector v) throws IllegalArgumentException {
      return this.getL1Distance(v.data);
   }

   @Override
   public double getLInfDistance(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         return this.getLInfDistance((ArrayRealVector)v);
      } else {
         this.checkVectorDimensions(v);
         double max = 0.0;

         for(int i = 0; i < this.data.length; ++i) {
            double delta = this.data[i] - v.getEntry(i);
            max = FastMath.max(max, FastMath.abs(delta));
         }

         return max;
      }
   }

   @Override
   public double getLInfDistance(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double max = 0.0;

      for(int i = 0; i < this.data.length; ++i) {
         double delta = this.data[i] - v[i];
         max = FastMath.max(max, FastMath.abs(delta));
      }

      return max;
   }

   public double getLInfDistance(ArrayRealVector v) throws IllegalArgumentException {
      return this.getLInfDistance(v.data);
   }

   @Override
   public RealVector unitVector() throws ArithmeticException {
      double norm = this.getNorm();
      if (norm == 0.0) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_NORM);
      } else {
         return this.mapDivide(norm);
      }
   }

   @Override
   public void unitize() throws ArithmeticException {
      double norm = this.getNorm();
      if (norm == 0.0) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.CANNOT_NORMALIZE_A_ZERO_NORM_VECTOR);
      } else {
         this.mapDivideToSelf(norm);
      }
   }

   @Override
   public RealVector projection(RealVector v) {
      return v.mapMultiply(this.dotProduct(v) / v.dotProduct(v));
   }

   @Override
   public RealVector projection(double[] v) {
      return this.projection(new ArrayRealVector(v, false));
   }

   public ArrayRealVector projection(ArrayRealVector v) {
      return (ArrayRealVector)v.mapMultiply(this.dotProduct(v) / v.dotProduct(v));
   }

   @Override
   public RealMatrix outerProduct(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         return this.outerProduct((ArrayRealVector)v);
      } else {
         this.checkVectorDimensions(v);
         int m = this.data.length;
         RealMatrix out = MatrixUtils.createRealMatrix(m, m);

         for(int i = 0; i < this.data.length; ++i) {
            for(int j = 0; j < this.data.length; ++j) {
               out.setEntry(i, j, this.data[i] * v.getEntry(j));
            }
         }

         return out;
      }
   }

   public RealMatrix outerProduct(ArrayRealVector v) throws IllegalArgumentException {
      return this.outerProduct(v.data);
   }

   @Override
   public RealMatrix outerProduct(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      int m = this.data.length;
      RealMatrix out = MatrixUtils.createRealMatrix(m, m);

      for(int i = 0; i < this.data.length; ++i) {
         for(int j = 0; j < this.data.length; ++j) {
            out.setEntry(i, j, this.data[i] * v[j]);
         }
      }

      return out;
   }

   @Override
   public double getEntry(int index) throws MatrixIndexException {
      return this.data[index];
   }

   @Override
   public int getDimension() {
      return this.data.length;
   }

   @Override
   public RealVector append(RealVector v) {
      try {
         return new ArrayRealVector(this, (ArrayRealVector)v);
      } catch (ClassCastException var3) {
         return new ArrayRealVector(this, v);
      }
   }

   public ArrayRealVector append(ArrayRealVector v) {
      return new ArrayRealVector(this, v);
   }

   @Override
   public RealVector append(double in) {
      double[] out = new double[this.data.length + 1];
      System.arraycopy(this.data, 0, out, 0, this.data.length);
      out[this.data.length] = in;
      return new ArrayRealVector(out, false);
   }

   @Override
   public RealVector append(double[] in) {
      return new ArrayRealVector(this, in);
   }

   @Override
   public RealVector getSubVector(int index, int n) {
      ArrayRealVector out = new ArrayRealVector(n);

      try {
         System.arraycopy(this.data, index, out.data, 0, n);
      } catch (IndexOutOfBoundsException var5) {
         this.checkIndex(index);
         this.checkIndex(index + n - 1);
      }

      return out;
   }

   @Override
   public void setEntry(int index, double value) {
      try {
         this.data[index] = value;
      } catch (IndexOutOfBoundsException var5) {
         this.checkIndex(index);
      }
   }

   @Override
   public void setSubVector(int index, RealVector v) {
      try {
         try {
            this.set(index, (ArrayRealVector)v);
         } catch (ClassCastException var5) {
            for(int i = index; i < index + v.getDimension(); ++i) {
               this.data[i] = v.getEntry(i - index);
            }
         }
      } catch (IndexOutOfBoundsException var6) {
         this.checkIndex(index);
         this.checkIndex(index + v.getDimension() - 1);
      }
   }

   @Override
   public void setSubVector(int index, double[] v) {
      try {
         System.arraycopy(v, 0, this.data, index, v.length);
      } catch (IndexOutOfBoundsException var4) {
         this.checkIndex(index);
         this.checkIndex(index + v.length - 1);
      }
   }

   public void set(int index, ArrayRealVector v) throws MatrixIndexException {
      this.setSubVector(index, v.data);
   }

   @Override
   public void set(double value) {
      Arrays.fill(this.data, value);
   }

   @Override
   public double[] toArray() {
      return (double[])this.data.clone();
   }

   @Override
   public String toString() {
      return DEFAULT_FORMAT.format(this);
   }

   @Override
   protected void checkVectorDimensions(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.getDimension());
   }

   @Override
   protected void checkVectorDimensions(int n) throws IllegalArgumentException {
      if (this.data.length != n) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.VECTOR_LENGTH_MISMATCH, this.data.length, n);
      }
   }

   @Override
   public boolean isNaN() {
      for(double v : this.data) {
         if (Double.isNaN(v)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isInfinite() {
      if (this.isNaN()) {
         return false;
      } else {
         for(double v : this.data) {
            if (Double.isInfinite(v)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (other != null && other instanceof RealVector) {
         RealVector rhs = (RealVector)other;
         if (this.data.length != rhs.getDimension()) {
            return false;
         } else if (rhs.isNaN()) {
            return this.isNaN();
         } else {
            for(int i = 0; i < this.data.length; ++i) {
               if (this.data[i] != rhs.getEntry(i)) {
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
      return this.isNaN() ? 9 : MathUtils.hash(this.data);
   }
}
