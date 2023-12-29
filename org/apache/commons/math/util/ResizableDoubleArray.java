package org.apache.commons.math.util;

import java.io.Serializable;
import java.util.Arrays;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class ResizableDoubleArray implements DoubleArray, Serializable {
   public static final int ADDITIVE_MODE = 1;
   public static final int MULTIPLICATIVE_MODE = 0;
   private static final long serialVersionUID = -3485529955529426875L;
   protected float contractionCriteria = 2.5F;
   protected float expansionFactor = 2.0F;
   protected int expansionMode = 0;
   protected int initialCapacity = 16;
   protected double[] internalArray;
   protected int numElements = 0;
   protected int startIndex = 0;

   public ResizableDoubleArray() {
      this.internalArray = new double[this.initialCapacity];
   }

   public ResizableDoubleArray(int initialCapacity) {
      this.setInitialCapacity(initialCapacity);
      this.internalArray = new double[this.initialCapacity];
   }

   public ResizableDoubleArray(double[] initialArray) {
      if (initialArray == null) {
         this.internalArray = new double[this.initialCapacity];
      } else {
         this.internalArray = new double[initialArray.length];
         System.arraycopy(initialArray, 0, this.internalArray, 0, initialArray.length);
         this.initialCapacity = initialArray.length;
         this.numElements = initialArray.length;
      }
   }

   public ResizableDoubleArray(int initialCapacity, float expansionFactor) {
      this.expansionFactor = expansionFactor;
      this.setInitialCapacity(initialCapacity);
      this.internalArray = new double[initialCapacity];
      this.setContractionCriteria(expansionFactor + 0.5F);
   }

   public ResizableDoubleArray(int initialCapacity, float expansionFactor, float contractionCriteria) {
      this.expansionFactor = expansionFactor;
      this.setContractionCriteria(contractionCriteria);
      this.setInitialCapacity(initialCapacity);
      this.internalArray = new double[initialCapacity];
   }

   public ResizableDoubleArray(int initialCapacity, float expansionFactor, float contractionCriteria, int expansionMode) {
      this.expansionFactor = expansionFactor;
      this.setContractionCriteria(contractionCriteria);
      this.setInitialCapacity(initialCapacity);
      this.setExpansionMode(expansionMode);
      this.internalArray = new double[initialCapacity];
   }

   public ResizableDoubleArray(ResizableDoubleArray original) {
      copy(original, this);
   }

   @Override
   public synchronized void addElement(double value) {
      ++this.numElements;
      if (this.startIndex + this.numElements > this.internalArray.length) {
         this.expand();
      }

      this.internalArray[this.startIndex + (this.numElements - 1)] = value;
      if (this.shouldContract()) {
         this.contract();
      }
   }

   public synchronized void addElements(double[] values) {
      double[] tempArray = new double[this.numElements + values.length + 1];
      System.arraycopy(this.internalArray, this.startIndex, tempArray, 0, this.numElements);
      System.arraycopy(values, 0, tempArray, this.numElements, values.length);
      this.internalArray = tempArray;
      this.startIndex = 0;
      this.numElements += values.length;
   }

   @Override
   public synchronized double addElementRolling(double value) {
      double discarded = this.internalArray[this.startIndex];
      if (this.startIndex + this.numElements + 1 > this.internalArray.length) {
         this.expand();
      }

      ++this.startIndex;
      this.internalArray[this.startIndex + (this.numElements - 1)] = value;
      if (this.shouldContract()) {
         this.contract();
      }

      return discarded;
   }

   public synchronized double substituteMostRecentElement(double value) {
      if (this.numElements < 1) {
         throw MathRuntimeException.createArrayIndexOutOfBoundsException(LocalizedFormats.CANNOT_SUBSTITUTE_ELEMENT_FROM_EMPTY_ARRAY);
      } else {
         double discarded = this.internalArray[this.startIndex + (this.numElements - 1)];
         this.internalArray[this.startIndex + (this.numElements - 1)] = value;
         return discarded;
      }
   }

   protected void checkContractExpand(float contraction, float expansion) {
      if (contraction < expansion) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CONTRACTION_CRITERIA_SMALLER_THAN_EXPANSION_FACTOR, contraction, expansion);
      } else if ((double)contraction <= 1.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CONTRACTION_CRITERIA_SMALLER_THAN_ONE, contraction);
      } else if ((double)expansion <= 1.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.EXPANSION_FACTOR_SMALLER_THAN_ONE, expansion);
      }
   }

   @Override
   public synchronized void clear() {
      this.numElements = 0;
      this.startIndex = 0;
      this.internalArray = new double[this.initialCapacity];
   }

   public synchronized void contract() {
      double[] tempArray = new double[this.numElements + 1];
      System.arraycopy(this.internalArray, this.startIndex, tempArray, 0, this.numElements);
      this.internalArray = tempArray;
      this.startIndex = 0;
   }

   public synchronized void discardFrontElements(int i) {
      this.discardExtremeElements(i, true);
   }

   public synchronized void discardMostRecentElements(int i) {
      this.discardExtremeElements(i, false);
   }

   private synchronized void discardExtremeElements(int i, boolean front) {
      if (i > this.numElements) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.TOO_MANY_ELEMENTS_TO_DISCARD_FROM_ARRAY, i, this.numElements);
      } else if (i < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.CANNOT_DISCARD_NEGATIVE_NUMBER_OF_ELEMENTS, i);
      } else {
         this.numElements -= i;
         if (front) {
            this.startIndex += i;
         }

         if (this.shouldContract()) {
            this.contract();
         }
      }
   }

   protected synchronized void expand() {
      int newSize = 0;
      if (this.expansionMode == 0) {
         newSize = (int)FastMath.ceil((double)((float)this.internalArray.length * this.expansionFactor));
      } else {
         newSize = this.internalArray.length + FastMath.round(this.expansionFactor);
      }

      double[] tempArray = new double[newSize];
      System.arraycopy(this.internalArray, 0, tempArray, 0, this.internalArray.length);
      this.internalArray = tempArray;
   }

   private synchronized void expandTo(int size) {
      double[] tempArray = new double[size];
      System.arraycopy(this.internalArray, 0, tempArray, 0, this.internalArray.length);
      this.internalArray = tempArray;
   }

   public float getContractionCriteria() {
      return this.contractionCriteria;
   }

   @Override
   public synchronized double getElement(int index) {
      if (index >= this.numElements) {
         throw MathRuntimeException.createArrayIndexOutOfBoundsException(LocalizedFormats.INDEX_LARGER_THAN_MAX, index, this.numElements - 1);
      } else if (index >= 0) {
         return this.internalArray[this.startIndex + index];
      } else {
         throw MathRuntimeException.createArrayIndexOutOfBoundsException(LocalizedFormats.CANNOT_RETRIEVE_AT_NEGATIVE_INDEX, index);
      }
   }

   @Override
   public synchronized double[] getElements() {
      double[] elementArray = new double[this.numElements];
      System.arraycopy(this.internalArray, this.startIndex, elementArray, 0, this.numElements);
      return elementArray;
   }

   public float getExpansionFactor() {
      return this.expansionFactor;
   }

   public int getExpansionMode() {
      return this.expansionMode;
   }

   synchronized int getInternalLength() {
      return this.internalArray.length;
   }

   @Override
   public synchronized int getNumElements() {
      return this.numElements;
   }

   @Deprecated
   public synchronized double[] getValues() {
      return this.internalArray;
   }

   public synchronized double[] getInternalValues() {
      return this.internalArray;
   }

   public void setContractionCriteria(float contractionCriteria) {
      this.checkContractExpand(contractionCriteria, this.getExpansionFactor());
      synchronized(this) {
         this.contractionCriteria = contractionCriteria;
      }
   }

   @Override
   public synchronized void setElement(int index, double value) {
      if (index < 0) {
         throw MathRuntimeException.createArrayIndexOutOfBoundsException(LocalizedFormats.CANNOT_SET_AT_NEGATIVE_INDEX, index);
      } else {
         if (index + 1 > this.numElements) {
            this.numElements = index + 1;
         }

         if (this.startIndex + index >= this.internalArray.length) {
            this.expandTo(this.startIndex + index + 1);
         }

         this.internalArray[this.startIndex + index] = value;
      }
   }

   public void setExpansionFactor(float expansionFactor) {
      this.checkContractExpand(this.getContractionCriteria(), expansionFactor);
      synchronized(this) {
         this.expansionFactor = expansionFactor;
      }
   }

   public void setExpansionMode(int expansionMode) {
      if (expansionMode != 0 && expansionMode != 1) {
         throw MathRuntimeException.createIllegalArgumentException(
            LocalizedFormats.UNSUPPORTED_EXPANSION_MODE, expansionMode, 0, "MULTIPLICATIVE_MODE", 1, "ADDITIVE_MODE"
         );
      } else {
         synchronized(this) {
            this.expansionMode = expansionMode;
         }
      }
   }

   protected void setInitialCapacity(int initialCapacity) {
      if (initialCapacity > 0) {
         synchronized(this) {
            this.initialCapacity = initialCapacity;
         }
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INITIAL_CAPACITY_NOT_POSITIVE, initialCapacity);
      }
   }

   public synchronized void setNumElements(int i) {
      if (i < 0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.INDEX_NOT_POSITIVE, i);
      } else {
         if (this.startIndex + i > this.internalArray.length) {
            this.expandTo(this.startIndex + i);
         }

         this.numElements = i;
      }
   }

   private synchronized boolean shouldContract() {
      if (this.expansionMode == 0) {
         return (float)this.internalArray.length / (float)this.numElements > this.contractionCriteria;
      } else {
         return (float)(this.internalArray.length - this.numElements) > this.contractionCriteria;
      }
   }

   public synchronized int start() {
      return this.startIndex;
   }

   public static void copy(ResizableDoubleArray source, ResizableDoubleArray dest) {
      synchronized(source) {
         synchronized(dest) {
            dest.initialCapacity = source.initialCapacity;
            dest.contractionCriteria = source.contractionCriteria;
            dest.expansionFactor = source.expansionFactor;
            dest.expansionMode = source.expansionMode;
            dest.internalArray = new double[source.internalArray.length];
            System.arraycopy(source.internalArray, 0, dest.internalArray, 0, dest.internalArray.length);
            dest.numElements = source.numElements;
            dest.startIndex = source.startIndex;
         }
      }
   }

   public synchronized ResizableDoubleArray copy() {
      ResizableDoubleArray result = new ResizableDoubleArray();
      copy(this, result);
      return result;
   }

   @Override
   public boolean equals(Object object) {
      if (object == this) {
         return true;
      } else if (!(object instanceof ResizableDoubleArray)) {
         return false;
      } else {
         synchronized(this) {
            boolean var10000;
            synchronized(object) {
               boolean result = true;
               ResizableDoubleArray other = (ResizableDoubleArray)object;
               result = result && other.initialCapacity == this.initialCapacity;
               result = result && other.contractionCriteria == this.contractionCriteria;
               result = result && other.expansionFactor == this.expansionFactor;
               result = result && other.expansionMode == this.expansionMode;
               result = result && other.numElements == this.numElements;
               result = result && other.startIndex == this.startIndex;
               if (!result) {
                  return false;
               }

               var10000 = Arrays.equals(this.internalArray, other.internalArray);
            }

            return var10000;
         }
      }
   }

   @Override
   public synchronized int hashCode() {
      int[] hashData = new int[]{
         new Float(this.expansionFactor).hashCode(),
         new Float(this.contractionCriteria).hashCode(),
         this.expansionMode,
         Arrays.hashCode(this.internalArray),
         this.initialCapacity,
         this.numElements,
         this.startIndex
      };
      return Arrays.hashCode(hashData);
   }
}
