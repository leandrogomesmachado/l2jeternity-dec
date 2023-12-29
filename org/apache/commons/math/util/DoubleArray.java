package org.apache.commons.math.util;

public interface DoubleArray {
   int getNumElements();

   double getElement(int var1);

   void setElement(int var1, double var2);

   void addElement(double var1);

   double addElementRolling(double var1);

   double[] getElements();

   void clear();
}
