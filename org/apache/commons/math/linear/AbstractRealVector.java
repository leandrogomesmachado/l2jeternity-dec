package org.apache.commons.math.linear;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.BinaryFunction;
import org.apache.commons.math.analysis.ComposableFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MathUnsupportedOperationException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public abstract class AbstractRealVector implements RealVector {
   protected void checkVectorDimensions(RealVector v) {
      this.checkVectorDimensions(v.getDimension());
   }

   protected void checkVectorDimensions(int n) throws DimensionMismatchException {
      int d = this.getDimension();
      if (d != n) {
         throw new DimensionMismatchException(d, n);
      }
   }

   protected void checkIndex(int index) throws MatrixIndexException {
      if (index < 0 || index >= this.getDimension()) {
         throw new MatrixIndexException(LocalizedFormats.INDEX_OUT_OF_RANGE, index, 0, this.getDimension() - 1);
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
   public RealVector add(double[] v) throws IllegalArgumentException {
      double[] result = (double[])v.clone();

      RealVector.Entry e;
      int var10001;
      for(Iterator<RealVector.Entry> it = this.sparseIterator(); it.hasNext() && (e = it.next()) != null; result[var10001] += e.getValue()) {
         var10001 = e.getIndex();
      }

      return new ArrayRealVector(result, false);
   }

   @Override
   public RealVector add(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         double[] values = ((ArrayRealVector)v).getDataRef();
         return this.add(values);
      } else {
         RealVector result = v.copy();
         Iterator<RealVector.Entry> it = this.sparseIterator();

         RealVector.Entry e;
         while(it.hasNext() && (e = it.next()) != null) {
            int index = e.getIndex();
            result.setEntry(index, e.getValue() + result.getEntry(index));
         }

         return result;
      }
   }

   @Override
   public RealVector subtract(double[] v) throws IllegalArgumentException {
      double[] result = (double[])v.clone();

      RealVector.Entry e;
      int index;
      for(Iterator<RealVector.Entry> it = this.sparseIterator(); it.hasNext() && (e = it.next()) != null; result[index] = e.getValue() - result[index]) {
         index = e.getIndex();
      }

      return new ArrayRealVector(result, false);
   }

   @Override
   public RealVector subtract(RealVector v) throws IllegalArgumentException {
      if (v instanceof ArrayRealVector) {
         double[] values = ((ArrayRealVector)v).getDataRef();
         return this.add(values);
      } else {
         RealVector result = v.copy();
         Iterator<RealVector.Entry> it = this.sparseIterator();

         RealVector.Entry e;
         while(it.hasNext() && (e = it.next()) != null) {
            int index = e.getIndex();
            v.setEntry(index, e.getValue() - result.getEntry(index));
         }

         return result;
      }
   }

   @Override
   public RealVector mapAdd(double d) {
      return this.copy().mapAddToSelf(d);
   }

   @Override
   public RealVector mapAddToSelf(double d) {
      if (d != 0.0) {
         try {
            return this.mapToSelf(BinaryFunction.ADD.fix1stArgument(d));
         } catch (FunctionEvaluationException var4) {
            throw new IllegalArgumentException(var4);
         }
      } else {
         return this;
      }
   }

   public abstract AbstractRealVector copy();

   @Override
   public double dotProduct(double[] v) throws IllegalArgumentException {
      return this.dotProduct(new ArrayRealVector(v, false));
   }

   @Override
   public double dotProduct(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v);
      double d = 0.0;
      Iterator<RealVector.Entry> it = this.sparseIterator();

      RealVector.Entry e;
      while(it.hasNext() && (e = it.next()) != null) {
         d += e.getValue() * v.getEntry(e.getIndex());
      }

      return d;
   }

   @Override
   public RealVector ebeDivide(double[] v) throws IllegalArgumentException {
      return this.ebeDivide(new ArrayRealVector(v, false));
   }

   @Override
   public RealVector ebeMultiply(double[] v) throws IllegalArgumentException {
      return this.ebeMultiply(new ArrayRealVector(v, false));
   }

   @Override
   public double getDistance(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v);
      double d = 0.0;

      RealVector.Entry e;
      double diff;
      for(Iterator<RealVector.Entry> it = this.iterator(); it.hasNext() && (e = it.next()) != null; d += diff * diff) {
         diff = e.getValue() - v.getEntry(e.getIndex());
      }

      return FastMath.sqrt(d);
   }

   @Override
   public double getNorm() {
      double sum = 0.0;

      RealVector.Entry e;
      double value;
      for(Iterator<RealVector.Entry> it = this.sparseIterator(); it.hasNext() && (e = it.next()) != null; sum += value * value) {
         value = e.getValue();
      }

      return FastMath.sqrt(sum);
   }

   @Override
   public double getL1Norm() {
      double norm = 0.0;
      Iterator<RealVector.Entry> it = this.sparseIterator();

      RealVector.Entry e;
      while(it.hasNext() && (e = it.next()) != null) {
         norm += FastMath.abs(e.getValue());
      }

      return norm;
   }

   @Override
   public double getLInfNorm() {
      double norm = 0.0;
      Iterator<RealVector.Entry> it = this.sparseIterator();

      RealVector.Entry e;
      while(it.hasNext() && (e = it.next()) != null) {
         norm = FastMath.max(norm, FastMath.abs(e.getValue()));
      }

      return norm;
   }

   @Override
   public double getDistance(double[] v) throws IllegalArgumentException {
      return this.getDistance(new ArrayRealVector(v, false));
   }

   @Override
   public double getL1Distance(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v);
      double d = 0.0;
      Iterator<RealVector.Entry> it = this.iterator();

      RealVector.Entry e;
      while(it.hasNext() && (e = it.next()) != null) {
         d += FastMath.abs(e.getValue() - v.getEntry(e.getIndex()));
      }

      return d;
   }

   @Override
   public double getL1Distance(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double d = 0.0;
      Iterator<RealVector.Entry> it = this.iterator();

      RealVector.Entry e;
      while(it.hasNext() && (e = it.next()) != null) {
         d += FastMath.abs(e.getValue() - v[e.getIndex()]);
      }

      return d;
   }

   @Override
   public double getLInfDistance(RealVector v) throws IllegalArgumentException {
      this.checkVectorDimensions(v);
      double d = 0.0;
      Iterator<RealVector.Entry> it = this.iterator();

      RealVector.Entry e;
      while(it.hasNext() && (e = it.next()) != null) {
         d = FastMath.max(FastMath.abs(e.getValue() - v.getEntry(e.getIndex())), d);
      }

      return d;
   }

   @Override
   public double getLInfDistance(double[] v) throws IllegalArgumentException {
      this.checkVectorDimensions(v.length);
      double d = 0.0;
      Iterator<RealVector.Entry> it = this.iterator();

      RealVector.Entry e;
      while(it.hasNext() && (e = it.next()) != null) {
         d = FastMath.max(FastMath.abs(e.getValue() - v[e.getIndex()]), d);
      }

      return d;
   }

   public int getMinIndex() {
      int minIndex = -1;
      double minValue = Double.POSITIVE_INFINITY;

      for(RealVector.Entry entry : this) {
         if (entry.getValue() <= minValue) {
            minIndex = entry.getIndex();
            minValue = entry.getValue();
         }
      }

      return minIndex;
   }

   public double getMinValue() {
      int minIndex = this.getMinIndex();
      return minIndex < 0 ? Double.NaN : this.getEntry(minIndex);
   }

   public int getMaxIndex() {
      int maxIndex = -1;
      double maxValue = Double.NEGATIVE_INFINITY;

      for(RealVector.Entry entry : this) {
         if (entry.getValue() >= maxValue) {
            maxIndex = entry.getIndex();
            maxValue = entry.getValue();
         }
      }

      return maxIndex;
   }

   public double getMaxValue() {
      int maxIndex = this.getMaxIndex();
      return maxIndex < 0 ? Double.NaN : this.getEntry(maxIndex);
   }

   @Override
   public RealVector mapAbs() {
      return this.copy().mapAbsToSelf();
   }

   @Override
   public RealVector mapAbsToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.ABS);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapAcos() {
      return this.copy().mapAcosToSelf();
   }

   @Override
   public RealVector mapAcosToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.ACOS);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapAsin() {
      return this.copy().mapAsinToSelf();
   }

   @Override
   public RealVector mapAsinToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.ASIN);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapAtan() {
      return this.copy().mapAtanToSelf();
   }

   @Override
   public RealVector mapAtanToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.ATAN);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapCbrt() {
      return this.copy().mapCbrtToSelf();
   }

   @Override
   public RealVector mapCbrtToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.CBRT);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapCeil() {
      return this.copy().mapCeilToSelf();
   }

   @Override
   public RealVector mapCeilToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.CEIL);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapCos() {
      return this.copy().mapCosToSelf();
   }

   @Override
   public RealVector mapCosToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.COS);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapCosh() {
      return this.copy().mapCoshToSelf();
   }

   @Override
   public RealVector mapCoshToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.COSH);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapDivide(double d) {
      return this.copy().mapDivideToSelf(d);
   }

   @Override
   public RealVector mapDivideToSelf(double d) {
      try {
         return this.mapToSelf(BinaryFunction.DIVIDE.fix2ndArgument(d));
      } catch (FunctionEvaluationException var4) {
         throw new IllegalArgumentException(var4);
      }
   }

   @Override
   public RealVector mapExp() {
      return this.copy().mapExpToSelf();
   }

   @Override
   public RealVector mapExpToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.EXP);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapExpm1() {
      return this.copy().mapExpm1ToSelf();
   }

   @Override
   public RealVector mapExpm1ToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.EXPM1);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapFloor() {
      return this.copy().mapFloorToSelf();
   }

   @Override
   public RealVector mapFloorToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.FLOOR);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapInv() {
      return this.copy().mapInvToSelf();
   }

   @Override
   public RealVector mapInvToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.INVERT);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapLog() {
      return this.copy().mapLogToSelf();
   }

   @Override
   public RealVector mapLogToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.LOG);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapLog10() {
      return this.copy().mapLog10ToSelf();
   }

   @Override
   public RealVector mapLog10ToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.LOG10);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapLog1p() {
      return this.copy().mapLog1pToSelf();
   }

   @Override
   public RealVector mapLog1pToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.LOG1P);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapMultiply(double d) {
      return this.copy().mapMultiplyToSelf(d);
   }

   @Override
   public RealVector mapMultiplyToSelf(double d) {
      try {
         return this.mapToSelf(BinaryFunction.MULTIPLY.fix1stArgument(d));
      } catch (FunctionEvaluationException var4) {
         throw new IllegalArgumentException(var4);
      }
   }

   @Override
   public RealVector mapPow(double d) {
      return this.copy().mapPowToSelf(d);
   }

   @Override
   public RealVector mapPowToSelf(double d) {
      try {
         return this.mapToSelf(BinaryFunction.POW.fix2ndArgument(d));
      } catch (FunctionEvaluationException var4) {
         throw new IllegalArgumentException(var4);
      }
   }

   @Override
   public RealVector mapRint() {
      return this.copy().mapRintToSelf();
   }

   @Override
   public RealVector mapRintToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.RINT);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapSignum() {
      return this.copy().mapSignumToSelf();
   }

   @Override
   public RealVector mapSignumToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.SIGNUM);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapSin() {
      return this.copy().mapSinToSelf();
   }

   @Override
   public RealVector mapSinToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.SIN);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapSinh() {
      return this.copy().mapSinhToSelf();
   }

   @Override
   public RealVector mapSinhToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.SINH);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapSqrt() {
      return this.copy().mapSqrtToSelf();
   }

   @Override
   public RealVector mapSqrtToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.SQRT);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapSubtract(double d) {
      return this.copy().mapSubtractToSelf(d);
   }

   @Override
   public RealVector mapSubtractToSelf(double d) {
      return this.mapAddToSelf(-d);
   }

   @Override
   public RealVector mapTan() {
      return this.copy().mapTanToSelf();
   }

   @Override
   public RealVector mapTanToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.TAN);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapTanh() {
      return this.copy().mapTanhToSelf();
   }

   @Override
   public RealVector mapTanhToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.TANH);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealVector mapUlp() {
      return this.copy().mapUlpToSelf();
   }

   @Override
   public RealVector mapUlpToSelf() {
      try {
         return this.mapToSelf(ComposableFunction.ULP);
      } catch (FunctionEvaluationException var2) {
         throw new IllegalArgumentException(var2);
      }
   }

   @Override
   public RealMatrix outerProduct(RealVector v) throws IllegalArgumentException {
      RealMatrix product;
      if (!(v instanceof SparseRealVector) && !(this instanceof SparseRealVector)) {
         product = new Array2DRowRealMatrix(this.getDimension(), v.getDimension());
      } else {
         product = new OpenMapRealMatrix(this.getDimension(), v.getDimension());
      }

      Iterator<RealVector.Entry> thisIt = this.sparseIterator();
      RealVector.Entry thisE = null;

      while(thisIt.hasNext() && (thisE = thisIt.next()) != null) {
         Iterator<RealVector.Entry> otherIt = v.sparseIterator();
         RealVector.Entry otherE = null;

         while(otherIt.hasNext() && (otherE = otherIt.next()) != null) {
            product.setEntry(thisE.getIndex(), otherE.getIndex(), thisE.getValue() * otherE.getValue());
         }
      }

      return product;
   }

   @Override
   public RealMatrix outerProduct(double[] v) throws IllegalArgumentException {
      return this.outerProduct(new ArrayRealVector(v, false));
   }

   @Override
   public RealVector projection(double[] v) throws IllegalArgumentException {
      return this.projection(new ArrayRealVector(v, false));
   }

   @Override
   public void set(double value) {
      Iterator<RealVector.Entry> it = this.iterator();
      RealVector.Entry e = null;

      while(it.hasNext() && (e = it.next()) != null) {
         e.setValue(value);
      }
   }

   @Override
   public double[] toArray() {
      int dim = this.getDimension();
      double[] values = new double[dim];

      for(int i = 0; i < dim; ++i) {
         values[i] = this.getEntry(i);
      }

      return values;
   }

   @Override
   public double[] getData() {
      return this.toArray();
   }

   @Override
   public RealVector unitVector() {
      RealVector copy = this.copy();
      copy.unitize();
      return copy;
   }

   @Override
   public void unitize() {
      this.mapDivideToSelf(this.getNorm());
   }

   @Override
   public Iterator<RealVector.Entry> sparseIterator() {
      return new AbstractRealVector.SparseEntryIterator();
   }

   @Override
   public Iterator<RealVector.Entry> iterator() {
      final int dim = this.getDimension();
      return new Iterator<RealVector.Entry>() {
         private int i = 0;
         private AbstractRealVector.EntryImpl e = AbstractRealVector.this.new EntryImpl();

         @Override
         public boolean hasNext() {
            return this.i < dim;
         }

         public RealVector.Entry next() {
            this.e.setIndex(this.i++);
            return this.e;
         }

         @Override
         public void remove() {
            throw new MathUnsupportedOperationException();
         }
      };
   }

   @Override
   public RealVector map(UnivariateRealFunction function) throws FunctionEvaluationException {
      return this.copy().mapToSelf(function);
   }

   @Override
   public RealVector mapToSelf(UnivariateRealFunction function) throws FunctionEvaluationException {
      Iterator<RealVector.Entry> it = function.value(0.0) == 0.0 ? this.sparseIterator() : this.iterator();

      RealVector.Entry e;
      while(it.hasNext() && (e = it.next()) != null) {
         e.setValue(function.value(e.getValue()));
      }

      return this;
   }

   protected class EntryImpl extends RealVector.Entry {
      public EntryImpl() {
         this.setIndex(0);
      }

      @Override
      public double getValue() {
         return AbstractRealVector.this.getEntry(this.getIndex());
      }

      @Override
      public void setValue(double newValue) {
         AbstractRealVector.this.setEntry(this.getIndex(), newValue);
      }
   }

   protected class SparseEntryIterator implements Iterator<RealVector.Entry> {
      private final int dim = AbstractRealVector.this.getDimension();
      private AbstractRealVector.EntryImpl current = AbstractRealVector.this.new EntryImpl();
      private AbstractRealVector.EntryImpl next = AbstractRealVector.this.new EntryImpl();

      protected SparseEntryIterator() {
         if (this.next.getValue() == 0.0) {
            this.advance(this.next);
         }
      }

      protected void advance(AbstractRealVector.EntryImpl e) {
         if (e != null) {
            do {
               e.setIndex(e.getIndex() + 1);
            } while(e.getIndex() < this.dim && e.getValue() == 0.0);

            if (e.getIndex() >= this.dim) {
               e.setIndex(-1);
            }
         }
      }

      @Override
      public boolean hasNext() {
         return this.next.getIndex() >= 0;
      }

      public RealVector.Entry next() {
         int index = this.next.getIndex();
         if (index < 0) {
            throw new NoSuchElementException();
         } else {
            this.current.setIndex(index);
            this.advance(this.next);
            return this.current;
         }
      }

      @Override
      public void remove() {
         throw new MathUnsupportedOperationException();
      }
   }
}
