package org.apache.commons.math.geometry;

import java.io.Serializable;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.MathUtils;

public class Vector3D implements Serializable {
   public static final Vector3D ZERO = new Vector3D(0.0, 0.0, 0.0);
   public static final Vector3D PLUS_I = new Vector3D(1.0, 0.0, 0.0);
   public static final Vector3D MINUS_I = new Vector3D(-1.0, 0.0, 0.0);
   public static final Vector3D PLUS_J = new Vector3D(0.0, 1.0, 0.0);
   public static final Vector3D MINUS_J = new Vector3D(0.0, -1.0, 0.0);
   public static final Vector3D PLUS_K = new Vector3D(0.0, 0.0, 1.0);
   public static final Vector3D MINUS_K = new Vector3D(0.0, 0.0, -1.0);
   public static final Vector3D NaN = new Vector3D(Double.NaN, Double.NaN, Double.NaN);
   public static final Vector3D POSITIVE_INFINITY = new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
   public static final Vector3D NEGATIVE_INFINITY = new Vector3D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
   private static final Vector3DFormat DEFAULT_FORMAT = Vector3DFormat.getInstance();
   private static final long serialVersionUID = 5133268763396045979L;
   private final double x;
   private final double y;
   private final double z;

   public Vector3D(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vector3D(double alpha, double delta) {
      double cosDelta = FastMath.cos(delta);
      this.x = FastMath.cos(alpha) * cosDelta;
      this.y = FastMath.sin(alpha) * cosDelta;
      this.z = FastMath.sin(delta);
   }

   public Vector3D(double a, Vector3D u) {
      this.x = a * u.x;
      this.y = a * u.y;
      this.z = a * u.z;
   }

   public Vector3D(double a1, Vector3D u1, double a2, Vector3D u2) {
      this.x = a1 * u1.x + a2 * u2.x;
      this.y = a1 * u1.y + a2 * u2.y;
      this.z = a1 * u1.z + a2 * u2.z;
   }

   public Vector3D(double a1, Vector3D u1, double a2, Vector3D u2, double a3, Vector3D u3) {
      this.x = a1 * u1.x + a2 * u2.x + a3 * u3.x;
      this.y = a1 * u1.y + a2 * u2.y + a3 * u3.y;
      this.z = a1 * u1.z + a2 * u2.z + a3 * u3.z;
   }

   public Vector3D(double a1, Vector3D u1, double a2, Vector3D u2, double a3, Vector3D u3, double a4, Vector3D u4) {
      this.x = a1 * u1.x + a2 * u2.x + a3 * u3.x + a4 * u4.x;
      this.y = a1 * u1.y + a2 * u2.y + a3 * u3.y + a4 * u4.y;
      this.z = a1 * u1.z + a2 * u2.z + a3 * u3.z + a4 * u4.z;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public double getNorm1() {
      return FastMath.abs(this.x) + FastMath.abs(this.y) + FastMath.abs(this.z);
   }

   public double getNorm() {
      return FastMath.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public double getNormSq() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public double getNormInf() {
      return FastMath.max(FastMath.max(FastMath.abs(this.x), FastMath.abs(this.y)), FastMath.abs(this.z));
   }

   public double getAlpha() {
      return FastMath.atan2(this.y, this.x);
   }

   public double getDelta() {
      return FastMath.asin(this.z / this.getNorm());
   }

   public Vector3D add(Vector3D v) {
      return new Vector3D(this.x + v.x, this.y + v.y, this.z + v.z);
   }

   public Vector3D add(double factor, Vector3D v) {
      return new Vector3D(this.x + factor * v.x, this.y + factor * v.y, this.z + factor * v.z);
   }

   public Vector3D subtract(Vector3D v) {
      return new Vector3D(this.x - v.x, this.y - v.y, this.z - v.z);
   }

   public Vector3D subtract(double factor, Vector3D v) {
      return new Vector3D(this.x - factor * v.x, this.y - factor * v.y, this.z - factor * v.z);
   }

   public Vector3D normalize() {
      double s = this.getNorm();
      if (s == 0.0) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.CANNOT_NORMALIZE_A_ZERO_NORM_VECTOR);
      } else {
         return this.scalarMultiply(1.0 / s);
      }
   }

   public Vector3D orthogonal() {
      double threshold = 0.6 * this.getNorm();
      if (threshold == 0.0) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_NORM);
      } else if (this.x >= -threshold && this.x <= threshold) {
         double inverse = 1.0 / FastMath.sqrt(this.y * this.y + this.z * this.z);
         return new Vector3D(0.0, inverse * this.z, -inverse * this.y);
      } else if (this.y >= -threshold && this.y <= threshold) {
         double inverse = 1.0 / FastMath.sqrt(this.x * this.x + this.z * this.z);
         return new Vector3D(-inverse * this.z, 0.0, inverse * this.x);
      } else {
         double inverse = 1.0 / FastMath.sqrt(this.x * this.x + this.y * this.y);
         return new Vector3D(inverse * this.y, -inverse * this.x, 0.0);
      }
   }

   public static double angle(Vector3D v1, Vector3D v2) {
      double normProduct = v1.getNorm() * v2.getNorm();
      if (normProduct == 0.0) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_NORM);
      } else {
         double dot = dotProduct(v1, v2);
         double threshold = normProduct * 0.9999;
         if (!(dot < -threshold) && !(dot > threshold)) {
            return FastMath.acos(dot / normProduct);
         } else {
            Vector3D v3 = crossProduct(v1, v2);
            return dot >= 0.0 ? FastMath.asin(v3.getNorm() / normProduct) : Math.PI - FastMath.asin(v3.getNorm() / normProduct);
         }
      }
   }

   public Vector3D negate() {
      return new Vector3D(-this.x, -this.y, -this.z);
   }

   public Vector3D scalarMultiply(double a) {
      return new Vector3D(a * this.x, a * this.y, a * this.z);
   }

   public boolean isNaN() {
      return Double.isNaN(this.x) || Double.isNaN(this.y) || Double.isNaN(this.z);
   }

   public boolean isInfinite() {
      return !this.isNaN() && (Double.isInfinite(this.x) || Double.isInfinite(this.y) || Double.isInfinite(this.z));
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (other instanceof Vector3D) {
         Vector3D rhs = (Vector3D)other;
         if (rhs.isNaN()) {
            return this.isNaN();
         } else {
            return this.x == rhs.x && this.y == rhs.y && this.z == rhs.z;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.isNaN() ? 8 : 31 * (23 * MathUtils.hash(this.x) + 19 * MathUtils.hash(this.y) + MathUtils.hash(this.z));
   }

   public static double dotProduct(Vector3D v1, Vector3D v2) {
      return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
   }

   public static Vector3D crossProduct(Vector3D v1, Vector3D v2) {
      return new Vector3D(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x);
   }

   public static double distance1(Vector3D v1, Vector3D v2) {
      double dx = FastMath.abs(v2.x - v1.x);
      double dy = FastMath.abs(v2.y - v1.y);
      double dz = FastMath.abs(v2.z - v1.z);
      return dx + dy + dz;
   }

   public static double distance(Vector3D v1, Vector3D v2) {
      double dx = v2.x - v1.x;
      double dy = v2.y - v1.y;
      double dz = v2.z - v1.z;
      return FastMath.sqrt(dx * dx + dy * dy + dz * dz);
   }

   public static double distanceInf(Vector3D v1, Vector3D v2) {
      double dx = FastMath.abs(v2.x - v1.x);
      double dy = FastMath.abs(v2.y - v1.y);
      double dz = FastMath.abs(v2.z - v1.z);
      return FastMath.max(FastMath.max(dx, dy), dz);
   }

   public static double distanceSq(Vector3D v1, Vector3D v2) {
      double dx = v2.x - v1.x;
      double dy = v2.y - v1.y;
      double dz = v2.z - v1.z;
      return dx * dx + dy * dy + dz * dz;
   }

   @Override
   public String toString() {
      return DEFAULT_FORMAT.format(this);
   }
}
