package org.apache.commons.math.stat.clustering;

import java.io.Serializable;
import java.util.Collection;
import org.apache.commons.math.util.MathUtils;

public class EuclideanIntegerPoint implements Clusterable<EuclideanIntegerPoint>, Serializable {
   private static final long serialVersionUID = 3946024775784901369L;
   private final int[] point;

   public EuclideanIntegerPoint(int[] point) {
      this.point = point;
   }

   public int[] getPoint() {
      return this.point;
   }

   public double distanceFrom(EuclideanIntegerPoint p) {
      return MathUtils.distance(this.point, p.getPoint());
   }

   public EuclideanIntegerPoint centroidOf(Collection<EuclideanIntegerPoint> points) {
      int[] centroid = new int[this.getPoint().length];

      for(EuclideanIntegerPoint p : points) {
         for(int i = 0; i < centroid.length; ++i) {
            centroid[i] += p.getPoint()[i];
         }
      }

      for(int i = 0; i < centroid.length; ++i) {
         centroid[i] /= points.size();
      }

      return new EuclideanIntegerPoint(centroid);
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof EuclideanIntegerPoint)) {
         return false;
      } else {
         int[] otherPoint = ((EuclideanIntegerPoint)other).getPoint();
         if (this.point.length != otherPoint.length) {
            return false;
         } else {
            for(int i = 0; i < this.point.length; ++i) {
               if (this.point[i] != otherPoint[i]) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   @Override
   public int hashCode() {
      int hashCode = 0;
      int[] arr$ = this.point;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Integer i = arr$[i$];
         hashCode += i.hashCode() * 13 + 7;
      }

      return hashCode;
   }

   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder("(");
      int[] coordinates = this.getPoint();

      for(int i = 0; i < coordinates.length; ++i) {
         buff.append(coordinates[i]);
         if (i < coordinates.length - 1) {
            buff.append(",");
         }
      }

      buff.append(")");
      return buff.toString();
   }
}
