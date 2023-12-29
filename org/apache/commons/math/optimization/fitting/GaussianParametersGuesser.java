package org.apache.commons.math.optimization.fitting;

import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.exception.ZeroException;
import org.apache.commons.math.exception.util.LocalizedFormats;

public class GaussianParametersGuesser {
   private final WeightedObservedPoint[] observations;
   private double[] parameters;

   public GaussianParametersGuesser(WeightedObservedPoint[] observations) {
      if (observations == null) {
         throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
      } else if (observations.length < 3) {
         throw new NumberIsTooSmallException(observations.length, 3, true);
      } else {
         this.observations = (WeightedObservedPoint[])observations.clone();
      }
   }

   public double[] guess() {
      if (this.parameters == null) {
         this.parameters = this.basicGuess(this.observations);
      }

      return (double[])this.parameters.clone();
   }

   private double[] basicGuess(WeightedObservedPoint[] points) {
      Arrays.sort(points, this.createWeightedObservedPointComparator());
      double[] params = new double[4];
      int minYIdx = this.findMinY(points);
      params[0] = points[minYIdx].getY();
      int maxYIdx = this.findMaxY(points);
      params[1] = points[maxYIdx].getY();
      params[2] = points[maxYIdx].getX();

      double fwhmApprox;
      try {
         double halfY = params[0] + (params[1] - params[0]) / 2.0;
         double fwhmX1 = this.interpolateXAtY(points, maxYIdx, -1, halfY);
         double fwhmX2 = this.interpolateXAtY(points, maxYIdx, 1, halfY);
         fwhmApprox = fwhmX2 - fwhmX1;
      } catch (OutOfRangeException var13) {
         fwhmApprox = points[points.length - 1].getX() - points[0].getX();
      }

      params[3] = fwhmApprox / (2.0 * Math.sqrt(2.0 * Math.log(2.0)));
      return params;
   }

   private int findMinY(WeightedObservedPoint[] points) {
      int minYIdx = 0;

      for(int i = 1; i < points.length; ++i) {
         if (points[i].getY() < points[minYIdx].getY()) {
            minYIdx = i;
         }
      }

      return minYIdx;
   }

   private int findMaxY(WeightedObservedPoint[] points) {
      int maxYIdx = 0;

      for(int i = 1; i < points.length; ++i) {
         if (points[i].getY() > points[maxYIdx].getY()) {
            maxYIdx = i;
         }
      }

      return maxYIdx;
   }

   private double interpolateXAtY(WeightedObservedPoint[] points, int startIdx, int idxStep, double y) throws OutOfRangeException {
      if (idxStep == 0) {
         throw new ZeroException();
      } else {
         WeightedObservedPoint[] twoPoints = this.getInterpolationPointsForY(points, startIdx, idxStep, y);
         WeightedObservedPoint pointA = twoPoints[0];
         WeightedObservedPoint pointB = twoPoints[1];
         if (pointA.getY() == y) {
            return pointA.getX();
         } else {
            return pointB.getY() == y
               ? pointB.getX()
               : pointA.getX() + (y - pointA.getY()) * (pointB.getX() - pointA.getX()) / (pointB.getY() - pointA.getY());
         }
      }
   }

   private WeightedObservedPoint[] getInterpolationPointsForY(WeightedObservedPoint[] points, int startIdx, int idxStep, double y) throws OutOfRangeException {
      if (idxStep == 0) {
         throw new ZeroException();
      } else {
         for(int i = startIdx; idxStep < 0 ? i + idxStep >= 0 : i + idxStep < points.length; i += idxStep) {
            if (this.isBetween(y, points[i].getY(), points[i + idxStep].getY())) {
               return idxStep < 0 ? new WeightedObservedPoint[]{points[i + idxStep], points[i]} : new WeightedObservedPoint[]{points[i], points[i + idxStep]};
            }
         }

         double minY = Double.POSITIVE_INFINITY;
         double maxY = Double.NEGATIVE_INFINITY;

         for(WeightedObservedPoint point : points) {
            minY = Math.min(minY, point.getY());
            maxY = Math.max(maxY, point.getY());
         }

         throw new OutOfRangeException(y, minY, maxY);
      }
   }

   private boolean isBetween(double value, double boundary1, double boundary2) {
      return value >= boundary1 && value <= boundary2 || value >= boundary2 && value <= boundary1;
   }

   private Comparator<WeightedObservedPoint> createWeightedObservedPointComparator() {
      return new Comparator<WeightedObservedPoint>() {
         public int compare(WeightedObservedPoint p1, WeightedObservedPoint p2) {
            if (p1 == null && p2 == null) {
               return 0;
            } else if (p1 == null) {
               return -1;
            } else if (p2 == null) {
               return 1;
            } else if (p1.getX() < p2.getX()) {
               return -1;
            } else if (p1.getX() > p2.getX()) {
               return 1;
            } else if (p1.getY() < p2.getY()) {
               return -1;
            } else if (p1.getY() > p2.getY()) {
               return 1;
            } else if (p1.getWeight() < p2.getWeight()) {
               return -1;
            } else {
               return p1.getWeight() > p2.getWeight() ? 1 : 0;
            }
         }
      };
   }
}
