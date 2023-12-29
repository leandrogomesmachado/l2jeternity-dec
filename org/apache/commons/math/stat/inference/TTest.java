package org.apache.commons.math.stat.inference;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;

public interface TTest {
   double pairedT(double[] var1, double[] var2) throws IllegalArgumentException, MathException;

   double pairedTTest(double[] var1, double[] var2) throws IllegalArgumentException, MathException;

   boolean pairedTTest(double[] var1, double[] var2, double var3) throws IllegalArgumentException, MathException;

   double t(double var1, double[] var3) throws IllegalArgumentException;

   double t(double var1, StatisticalSummary var3) throws IllegalArgumentException;

   double homoscedasticT(double[] var1, double[] var2) throws IllegalArgumentException;

   double t(double[] var1, double[] var2) throws IllegalArgumentException;

   double t(StatisticalSummary var1, StatisticalSummary var2) throws IllegalArgumentException;

   double homoscedasticT(StatisticalSummary var1, StatisticalSummary var2) throws IllegalArgumentException;

   double tTest(double var1, double[] var3) throws IllegalArgumentException, MathException;

   boolean tTest(double var1, double[] var3, double var4) throws IllegalArgumentException, MathException;

   double tTest(double var1, StatisticalSummary var3) throws IllegalArgumentException, MathException;

   boolean tTest(double var1, StatisticalSummary var3, double var4) throws IllegalArgumentException, MathException;

   double tTest(double[] var1, double[] var2) throws IllegalArgumentException, MathException;

   double homoscedasticTTest(double[] var1, double[] var2) throws IllegalArgumentException, MathException;

   boolean tTest(double[] var1, double[] var2, double var3) throws IllegalArgumentException, MathException;

   boolean homoscedasticTTest(double[] var1, double[] var2, double var3) throws IllegalArgumentException, MathException;

   double tTest(StatisticalSummary var1, StatisticalSummary var2) throws IllegalArgumentException, MathException;

   double homoscedasticTTest(StatisticalSummary var1, StatisticalSummary var2) throws IllegalArgumentException, MathException;

   boolean tTest(StatisticalSummary var1, StatisticalSummary var2, double var3) throws IllegalArgumentException, MathException;
}
