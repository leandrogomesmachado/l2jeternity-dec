package org.apache.commons.math.optimization.fitting;

import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.util.FastMath;

public class HarmonicCoefficientsGuesser {
   private final WeightedObservedPoint[] observations;
   private double a;
   private double omega;
   private double phi;

   public HarmonicCoefficientsGuesser(WeightedObservedPoint[] observations) {
      this.observations = (WeightedObservedPoint[])observations.clone();
      this.a = Double.NaN;
      this.omega = Double.NaN;
   }

   public void guess() throws OptimizationException {
      this.sortObservations();
      this.guessAOmega();
      this.guessPhi();
   }

   private void sortObservations() {
      WeightedObservedPoint curr = this.observations[0];

      for(int j = 1; j < this.observations.length; ++j) {
         WeightedObservedPoint prec = curr;
         curr = this.observations[j];
         if (curr.getX() < prec.getX()) {
            int i = j - 1;
            WeightedObservedPoint mI = this.observations[i];

            while(i >= 0 && curr.getX() < mI.getX()) {
               this.observations[i + 1] = mI;
               if (i-- != 0) {
                  mI = this.observations[i];
               }
            }

            this.observations[i + 1] = curr;
            curr = this.observations[j];
         }
      }
   }

   private void guessAOmega() throws OptimizationException {
      double sx2 = 0.0;
      double sy2 = 0.0;
      double sxy = 0.0;
      double sxz = 0.0;
      double syz = 0.0;
      double currentX = this.observations[0].getX();
      double currentY = this.observations[0].getY();
      double f2Integral = 0.0;
      double fPrime2Integral = 0.0;
      double startX = currentX;

      for(int i = 1; i < this.observations.length; ++i) {
         double previousX = currentX;
         double previousY = currentY;
         currentX = this.observations[i].getX();
         currentY = this.observations[i].getY();
         double dx = currentX - previousX;
         double dy = currentY - previousY;
         double f2StepIntegral = dx * (previousY * previousY + previousY * currentY + currentY * currentY) / 3.0;
         double fPrime2StepIntegral = dy * dy / dx;
         double x = currentX - startX;
         f2Integral += f2StepIntegral;
         fPrime2Integral += fPrime2StepIntegral;
         sx2 += x * x;
         sy2 += f2Integral * f2Integral;
         sxy += x * f2Integral;
         sxz += x * fPrime2Integral;
         syz += f2Integral * fPrime2Integral;
      }

      double c1 = sy2 * sxz - sxy * syz;
      double c2 = sxy * sxz - sx2 * syz;
      double c3 = sx2 * sy2 - sxy * sxy;
      if (!(c1 / c2 < 0.0) && !(c2 / c3 < 0.0)) {
         this.a = FastMath.sqrt(c1 / c2);
         this.omega = FastMath.sqrt(c2 / c3);
      } else {
         throw new OptimizationException(LocalizedFormats.UNABLE_TO_FIRST_GUESS_HARMONIC_COEFFICIENTS);
      }
   }

   private void guessPhi() {
      double fcMean = 0.0;
      double fsMean = 0.0;
      double currentX = this.observations[0].getX();
      double currentY = this.observations[0].getY();

      for(int i = 1; i < this.observations.length; ++i) {
         double previousX = currentX;
         double previousY = currentY;
         currentX = this.observations[i].getX();
         currentY = this.observations[i].getY();
         double currentYPrime = (currentY - previousY) / (currentX - previousX);
         double omegaX = this.omega * currentX;
         double cosine = FastMath.cos(omegaX);
         double sine = FastMath.sin(omegaX);
         fcMean += this.omega * currentY * cosine - currentYPrime * sine;
         fsMean += this.omega * currentY * sine + currentYPrime * cosine;
      }

      this.phi = FastMath.atan2(-fsMean, fcMean);
   }

   public double getGuessedAmplitude() {
      return this.a;
   }

   public double getGuessedPulsation() {
      return this.omega;
   }

   public double getGuessedPhase() {
      return this.phi;
   }
}
