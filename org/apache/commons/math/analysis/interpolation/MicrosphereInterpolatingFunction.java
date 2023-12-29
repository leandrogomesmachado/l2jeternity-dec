package org.apache.commons.math.analysis.interpolation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.math.DimensionMismatchException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.exception.NoDataException;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.random.UnitSphereRandomVectorGenerator;
import org.apache.commons.math.util.FastMath;

public class MicrosphereInterpolatingFunction implements MultivariateRealFunction {
   private final int dimension;
   private final List<MicrosphereInterpolatingFunction.MicrosphereSurfaceElement> microsphere;
   private final double brightnessExponent;
   private final Map<RealVector, Double> samples;

   public MicrosphereInterpolatingFunction(
      double[][] xval, double[] yval, int brightnessExponent, int microsphereElements, UnitSphereRandomVectorGenerator rand
   ) throws DimensionMismatchException, NoDataException {
      if (xval.length != 0 && xval[0] != null) {
         if (xval.length != yval.length) {
            throw new DimensionMismatchException(xval.length, yval.length);
         } else {
            this.dimension = xval[0].length;
            this.brightnessExponent = (double)brightnessExponent;
            this.samples = new HashMap<>(yval.length);

            for(int i = 0; i < xval.length; ++i) {
               double[] xvalI = xval[i];
               if (xvalI.length != this.dimension) {
                  throw new DimensionMismatchException(xvalI.length, this.dimension);
               }

               this.samples.put(new ArrayRealVector(xvalI), yval[i]);
            }

            this.microsphere = new ArrayList<>(microsphereElements);

            for(int i = 0; i < microsphereElements; ++i) {
               this.microsphere.add(new MicrosphereInterpolatingFunction.MicrosphereSurfaceElement(rand.nextVector()));
            }
         }
      } else {
         throw new NoDataException();
      }
   }

   @Override
   public double value(double[] point) {
      RealVector p = new ArrayRealVector(point);

      for(MicrosphereInterpolatingFunction.MicrosphereSurfaceElement md : this.microsphere) {
         md.reset();
      }

      for(Entry<RealVector, Double> sd : this.samples.entrySet()) {
         RealVector diff = sd.getKey().subtract(p);
         double diffNorm = diff.getNorm();
         if (FastMath.abs(diffNorm) < FastMath.ulp(1.0)) {
            return sd.getValue();
         }

         for(MicrosphereInterpolatingFunction.MicrosphereSurfaceElement md : this.microsphere) {
            double w = FastMath.pow(diffNorm, -this.brightnessExponent);
            md.store(this.cosAngle(diff, md.normal()) * w, sd);
         }
      }

      double value = 0.0;
      double totalWeight = 0.0;

      for(MicrosphereInterpolatingFunction.MicrosphereSurfaceElement md : this.microsphere) {
         double iV = md.illumination();
         Entry<RealVector, Double> sd = md.sample();
         if (sd != null) {
            value += iV * sd.getValue();
            totalWeight += iV;
         }
      }

      return value / totalWeight;
   }

   private double cosAngle(RealVector v, RealVector w) {
      return v.dotProduct(w) / (v.getNorm() * w.getNorm());
   }

   private static class MicrosphereSurfaceElement {
      private final RealVector normal;
      private double brightestIllumination;
      private Entry<RealVector, Double> brightestSample;

      MicrosphereSurfaceElement(double[] n) {
         this.normal = new ArrayRealVector(n);
      }

      RealVector normal() {
         return this.normal;
      }

      void reset() {
         this.brightestIllumination = 0.0;
         this.brightestSample = null;
      }

      void store(double illuminationFromSample, Entry<RealVector, Double> sample) {
         if (illuminationFromSample > this.brightestIllumination) {
            this.brightestIllumination = illuminationFromSample;
            this.brightestSample = sample;
         }
      }

      double illumination() {
         return this.brightestIllumination;
      }

      Entry<RealVector, Double> sample() {
         return this.brightestSample;
      }
   }
}
