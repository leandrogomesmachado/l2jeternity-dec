package org.apache.commons.math.geometry;

import java.io.Serializable;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.exception.util.LocalizedFormats;
import org.apache.commons.math.util.FastMath;

public class Rotation implements Serializable {
   public static final Rotation IDENTITY = new Rotation(1.0, 0.0, 0.0, 0.0, false);
   private static final long serialVersionUID = -2153622329907944313L;
   private final double q0;
   private final double q1;
   private final double q2;
   private final double q3;

   public Rotation(double q0, double q1, double q2, double q3, boolean needsNormalization) {
      if (needsNormalization) {
         double inv = 1.0 / FastMath.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
         q0 *= inv;
         q1 *= inv;
         q2 *= inv;
         q3 *= inv;
      }

      this.q0 = q0;
      this.q1 = q1;
      this.q2 = q2;
      this.q3 = q3;
   }

   public Rotation(Vector3D axis, double angle) {
      double norm = axis.getNorm();
      if (norm == 0.0) {
         throw MathRuntimeException.createArithmeticException(LocalizedFormats.ZERO_NORM_FOR_ROTATION_AXIS);
      } else {
         double halfAngle = -0.5 * angle;
         double coeff = FastMath.sin(halfAngle) / norm;
         this.q0 = FastMath.cos(halfAngle);
         this.q1 = coeff * axis.getX();
         this.q2 = coeff * axis.getY();
         this.q3 = coeff * axis.getZ();
      }
   }

   public Rotation(double[][] m, double threshold) throws NotARotationMatrixException {
      if (m.length == 3 && m[0].length == 3 && m[1].length == 3 && m[2].length == 3) {
         double[][] ort = this.orthogonalizeMatrix(m, threshold);
         double det = ort[0][0] * (ort[1][1] * ort[2][2] - ort[2][1] * ort[1][2])
            - ort[1][0] * (ort[0][1] * ort[2][2] - ort[2][1] * ort[0][2])
            + ort[2][0] * (ort[0][1] * ort[1][2] - ort[1][1] * ort[0][2]);
         if (det < 0.0) {
            throw new NotARotationMatrixException(LocalizedFormats.CLOSEST_ORTHOGONAL_MATRIX_HAS_NEGATIVE_DETERMINANT, det);
         } else {
            double s = ort[0][0] + ort[1][1] + ort[2][2];
            if (s > -0.19) {
               this.q0 = 0.5 * FastMath.sqrt(s + 1.0);
               double inv = 0.25 / this.q0;
               this.q1 = inv * (ort[1][2] - ort[2][1]);
               this.q2 = inv * (ort[2][0] - ort[0][2]);
               this.q3 = inv * (ort[0][1] - ort[1][0]);
            } else {
               s = ort[0][0] - ort[1][1] - ort[2][2];
               if (s > -0.19) {
                  this.q1 = 0.5 * FastMath.sqrt(s + 1.0);
                  double inv = 0.25 / this.q1;
                  this.q0 = inv * (ort[1][2] - ort[2][1]);
                  this.q2 = inv * (ort[0][1] + ort[1][0]);
                  this.q3 = inv * (ort[0][2] + ort[2][0]);
               } else {
                  s = ort[1][1] - ort[0][0] - ort[2][2];
                  if (s > -0.19) {
                     this.q2 = 0.5 * FastMath.sqrt(s + 1.0);
                     double inv = 0.25 / this.q2;
                     this.q0 = inv * (ort[2][0] - ort[0][2]);
                     this.q1 = inv * (ort[0][1] + ort[1][0]);
                     this.q3 = inv * (ort[2][1] + ort[1][2]);
                  } else {
                     s = ort[2][2] - ort[0][0] - ort[1][1];
                     this.q3 = 0.5 * FastMath.sqrt(s + 1.0);
                     double inv = 0.25 / this.q3;
                     this.q0 = inv * (ort[0][1] - ort[1][0]);
                     this.q1 = inv * (ort[0][2] + ort[2][0]);
                     this.q2 = inv * (ort[2][1] + ort[1][2]);
                  }
               }
            }
         }
      } else {
         throw new NotARotationMatrixException(LocalizedFormats.ROTATION_MATRIX_DIMENSIONS, m.length, m[0].length);
      }
   }

   public Rotation(Vector3D u1, Vector3D u2, Vector3D v1, Vector3D v2) {
      double u1u1 = Vector3D.dotProduct(u1, u1);
      double u2u2 = Vector3D.dotProduct(u2, u2);
      double v1v1 = Vector3D.dotProduct(v1, v1);
      double v2v2 = Vector3D.dotProduct(v2, v2);
      if (u1u1 != 0.0 && u2u2 != 0.0 && v1v1 != 0.0 && v2v2 != 0.0) {
         double u1x = u1.getX();
         double u1y = u1.getY();
         double u1z = u1.getZ();
         double u2x = u2.getX();
         double u2y = u2.getY();
         double u2z = u2.getZ();
         double coeff = FastMath.sqrt(u1u1 / v1v1);
         double v1x = coeff * v1.getX();
         double v1y = coeff * v1.getY();
         double v1z = coeff * v1.getZ();
         v1 = new Vector3D(v1x, v1y, v1z);
         double u1u2 = Vector3D.dotProduct(u1, u2);
         double v1v2 = Vector3D.dotProduct(v1, v2);
         double coeffU = u1u2 / u1u1;
         double coeffV = v1v2 / u1u1;
         double beta = FastMath.sqrt((u2u2 - u1u2 * coeffU) / (v2v2 - v1v2 * coeffV));
         double alpha = coeffU - beta * coeffV;
         double v2x = alpha * v1x + beta * v2.getX();
         double v2y = alpha * v1y + beta * v2.getY();
         double v2z = alpha * v1z + beta * v2.getZ();
         v2 = new Vector3D(v2x, v2y, v2z);
         Vector3D uRef = u1;
         Vector3D vRef = v1;
         double dx1 = v1x - u1.getX();
         double dy1 = v1y - u1.getY();
         double dz1 = v1z - u1.getZ();
         double dx2 = v2x - u2.getX();
         double dy2 = v2y - u2.getY();
         double dz2 = v2z - u2.getZ();
         Vector3D k = new Vector3D(dy1 * dz2 - dz1 * dy2, dz1 * dx2 - dx1 * dz2, dx1 * dy2 - dy1 * dx2);
         double c = k.getX() * (u1y * u2z - u1z * u2y) + k.getY() * (u1z * u2x - u1x * u2z) + k.getZ() * (u1x * u2y - u1y * u2x);
         if (c == 0.0) {
            Vector3D u3 = Vector3D.crossProduct(u1, u2);
            Vector3D v3 = Vector3D.crossProduct(v1, v2);
            double u3x = u3.getX();
            double u3y = u3.getY();
            double u3z = u3.getZ();
            double v3x = v3.getX();
            double v3y = v3.getY();
            double v3z = v3.getZ();
            double dx3 = v3x - u3x;
            double dy3 = v3y - u3y;
            double dz3 = v3z - u3z;
            k = new Vector3D(dy1 * dz3 - dz1 * dy3, dz1 * dx3 - dx1 * dz3, dx1 * dy3 - dy1 * dx3);
            c = k.getX() * (u1y * u3z - u1z * u3y) + k.getY() * (u1z * u3x - u1x * u3z) + k.getZ() * (u1x * u3y - u1y * u3x);
            if (c == 0.0) {
               k = new Vector3D(dy2 * dz3 - dz2 * dy3, dz2 * dx3 - dx2 * dz3, dx2 * dy3 - dy2 * dx3);
               c = k.getX() * (u2y * u3z - u2z * u3y) + k.getY() * (u2z * u3x - u2x * u3z) + k.getZ() * (u2x * u3y - u2y * u3x);
               if (c == 0.0) {
                  this.q0 = 1.0;
                  this.q1 = 0.0;
                  this.q2 = 0.0;
                  this.q3 = 0.0;
                  return;
               }

               uRef = u2;
               vRef = v2;
            }
         }

         c = FastMath.sqrt(c);
         double inv = 1.0 / (c + c);
         this.q1 = inv * k.getX();
         this.q2 = inv * k.getY();
         this.q3 = inv * k.getZ();
         k = new Vector3D(
            uRef.getY() * this.q3 - uRef.getZ() * this.q2, uRef.getZ() * this.q1 - uRef.getX() * this.q3, uRef.getX() * this.q2 - uRef.getY() * this.q1
         );
         c = Vector3D.dotProduct(k, k);
         this.q0 = Vector3D.dotProduct(vRef, k) / (c + c);
      } else {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.ZERO_NORM_FOR_ROTATION_DEFINING_VECTOR);
      }
   }

   public Rotation(Vector3D u, Vector3D v) {
      double normProduct = u.getNorm() * v.getNorm();
      if (normProduct == 0.0) {
         throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.ZERO_NORM_FOR_ROTATION_DEFINING_VECTOR);
      } else {
         double dot = Vector3D.dotProduct(u, v);
         if (dot < -0.999999999999998 * normProduct) {
            Vector3D w = u.orthogonal();
            this.q0 = 0.0;
            this.q1 = -w.getX();
            this.q2 = -w.getY();
            this.q3 = -w.getZ();
         } else {
            this.q0 = FastMath.sqrt(0.5 * (1.0 + dot / normProduct));
            double coeff = 1.0 / (2.0 * this.q0 * normProduct);
            this.q1 = coeff * (v.getY() * u.getZ() - v.getZ() * u.getY());
            this.q2 = coeff * (v.getZ() * u.getX() - v.getX() * u.getZ());
            this.q3 = coeff * (v.getX() * u.getY() - v.getY() * u.getX());
         }
      }
   }

   public Rotation(RotationOrder order, double alpha1, double alpha2, double alpha3) {
      Rotation r1 = new Rotation(order.getA1(), alpha1);
      Rotation r2 = new Rotation(order.getA2(), alpha2);
      Rotation r3 = new Rotation(order.getA3(), alpha3);
      Rotation composed = r1.applyTo(r2.applyTo(r3));
      this.q0 = composed.q0;
      this.q1 = composed.q1;
      this.q2 = composed.q2;
      this.q3 = composed.q3;
   }

   public Rotation revert() {
      return new Rotation(-this.q0, this.q1, this.q2, this.q3, false);
   }

   public double getQ0() {
      return this.q0;
   }

   public double getQ1() {
      return this.q1;
   }

   public double getQ2() {
      return this.q2;
   }

   public double getQ3() {
      return this.q3;
   }

   public Vector3D getAxis() {
      double squaredSine = this.q1 * this.q1 + this.q2 * this.q2 + this.q3 * this.q3;
      if (squaredSine == 0.0) {
         return new Vector3D(1.0, 0.0, 0.0);
      } else if (this.q0 < 0.0) {
         double inverse = 1.0 / FastMath.sqrt(squaredSine);
         return new Vector3D(this.q1 * inverse, this.q2 * inverse, this.q3 * inverse);
      } else {
         double inverse = -1.0 / FastMath.sqrt(squaredSine);
         return new Vector3D(this.q1 * inverse, this.q2 * inverse, this.q3 * inverse);
      }
   }

   public double getAngle() {
      if (this.q0 < -0.1 || this.q0 > 0.1) {
         return 2.0 * FastMath.asin(FastMath.sqrt(this.q1 * this.q1 + this.q2 * this.q2 + this.q3 * this.q3));
      } else {
         return this.q0 < 0.0 ? 2.0 * FastMath.acos(-this.q0) : 2.0 * FastMath.acos(this.q0);
      }
   }

   public double[] getAngles(RotationOrder order) throws CardanEulerSingularityException {
      if (order == RotationOrder.XYZ) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_K);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_I);
         if (!(v2.getZ() < -0.9999999999) && !(v2.getZ() > 0.9999999999)) {
            return new double[]{FastMath.atan2(-v1.getY(), v1.getZ()), FastMath.asin(v2.getZ()), FastMath.atan2(-v2.getY(), v2.getX())};
         } else {
            throw new CardanEulerSingularityException(true);
         }
      } else if (order == RotationOrder.XZY) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_J);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_I);
         if (!(v2.getY() < -0.9999999999) && !(v2.getY() > 0.9999999999)) {
            return new double[]{FastMath.atan2(v1.getZ(), v1.getY()), -FastMath.asin(v2.getY()), FastMath.atan2(v2.getZ(), v2.getX())};
         } else {
            throw new CardanEulerSingularityException(true);
         }
      } else if (order == RotationOrder.YXZ) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_K);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_J);
         if (!(v2.getZ() < -0.9999999999) && !(v2.getZ() > 0.9999999999)) {
            return new double[]{FastMath.atan2(v1.getX(), v1.getZ()), -FastMath.asin(v2.getZ()), FastMath.atan2(v2.getX(), v2.getY())};
         } else {
            throw new CardanEulerSingularityException(true);
         }
      } else if (order == RotationOrder.YZX) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_I);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_J);
         if (!(v2.getX() < -0.9999999999) && !(v2.getX() > 0.9999999999)) {
            return new double[]{FastMath.atan2(-v1.getZ(), v1.getX()), FastMath.asin(v2.getX()), FastMath.atan2(-v2.getZ(), v2.getY())};
         } else {
            throw new CardanEulerSingularityException(true);
         }
      } else if (order == RotationOrder.ZXY) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_J);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_K);
         if (!(v2.getY() < -0.9999999999) && !(v2.getY() > 0.9999999999)) {
            return new double[]{FastMath.atan2(-v1.getX(), v1.getY()), FastMath.asin(v2.getY()), FastMath.atan2(-v2.getX(), v2.getZ())};
         } else {
            throw new CardanEulerSingularityException(true);
         }
      } else if (order == RotationOrder.ZYX) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_I);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_K);
         if (!(v2.getX() < -0.9999999999) && !(v2.getX() > 0.9999999999)) {
            return new double[]{FastMath.atan2(v1.getY(), v1.getX()), -FastMath.asin(v2.getX()), FastMath.atan2(v2.getY(), v2.getZ())};
         } else {
            throw new CardanEulerSingularityException(true);
         }
      } else if (order == RotationOrder.XYX) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_I);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_I);
         if (!(v2.getX() < -0.9999999999) && !(v2.getX() > 0.9999999999)) {
            return new double[]{FastMath.atan2(v1.getY(), -v1.getZ()), FastMath.acos(v2.getX()), FastMath.atan2(v2.getY(), v2.getZ())};
         } else {
            throw new CardanEulerSingularityException(false);
         }
      } else if (order == RotationOrder.XZX) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_I);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_I);
         if (!(v2.getX() < -0.9999999999) && !(v2.getX() > 0.9999999999)) {
            return new double[]{FastMath.atan2(v1.getZ(), v1.getY()), FastMath.acos(v2.getX()), FastMath.atan2(v2.getZ(), -v2.getY())};
         } else {
            throw new CardanEulerSingularityException(false);
         }
      } else if (order == RotationOrder.YXY) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_J);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_J);
         if (!(v2.getY() < -0.9999999999) && !(v2.getY() > 0.9999999999)) {
            return new double[]{FastMath.atan2(v1.getX(), v1.getZ()), FastMath.acos(v2.getY()), FastMath.atan2(v2.getX(), -v2.getZ())};
         } else {
            throw new CardanEulerSingularityException(false);
         }
      } else if (order == RotationOrder.YZY) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_J);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_J);
         if (!(v2.getY() < -0.9999999999) && !(v2.getY() > 0.9999999999)) {
            return new double[]{FastMath.atan2(v1.getZ(), -v1.getX()), FastMath.acos(v2.getY()), FastMath.atan2(v2.getZ(), v2.getX())};
         } else {
            throw new CardanEulerSingularityException(false);
         }
      } else if (order == RotationOrder.ZXZ) {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_K);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_K);
         if (!(v2.getZ() < -0.9999999999) && !(v2.getZ() > 0.9999999999)) {
            return new double[]{FastMath.atan2(v1.getX(), -v1.getY()), FastMath.acos(v2.getZ()), FastMath.atan2(v2.getX(), v2.getY())};
         } else {
            throw new CardanEulerSingularityException(false);
         }
      } else {
         Vector3D v1 = this.applyTo(Vector3D.PLUS_K);
         Vector3D v2 = this.applyInverseTo(Vector3D.PLUS_K);
         if (!(v2.getZ() < -0.9999999999) && !(v2.getZ() > 0.9999999999)) {
            return new double[]{FastMath.atan2(v1.getY(), v1.getX()), FastMath.acos(v2.getZ()), FastMath.atan2(v2.getY(), -v2.getX())};
         } else {
            throw new CardanEulerSingularityException(false);
         }
      }
   }

   public double[][] getMatrix() {
      double q0q0 = this.q0 * this.q0;
      double q0q1 = this.q0 * this.q1;
      double q0q2 = this.q0 * this.q2;
      double q0q3 = this.q0 * this.q3;
      double q1q1 = this.q1 * this.q1;
      double q1q2 = this.q1 * this.q2;
      double q1q3 = this.q1 * this.q3;
      double q2q2 = this.q2 * this.q2;
      double q2q3 = this.q2 * this.q3;
      double q3q3 = this.q3 * this.q3;
      double[][] m = new double[][]{new double[3], new double[3], new double[3]};
      m[0][0] = 2.0 * (q0q0 + q1q1) - 1.0;
      m[1][0] = 2.0 * (q1q2 - q0q3);
      m[2][0] = 2.0 * (q1q3 + q0q2);
      m[0][1] = 2.0 * (q1q2 + q0q3);
      m[1][1] = 2.0 * (q0q0 + q2q2) - 1.0;
      m[2][1] = 2.0 * (q2q3 - q0q1);
      m[0][2] = 2.0 * (q1q3 - q0q2);
      m[1][2] = 2.0 * (q2q3 + q0q1);
      m[2][2] = 2.0 * (q0q0 + q3q3) - 1.0;
      return m;
   }

   public Vector3D applyTo(Vector3D u) {
      double x = u.getX();
      double y = u.getY();
      double z = u.getZ();
      double s = this.q1 * x + this.q2 * y + this.q3 * z;
      return new Vector3D(
         2.0 * (this.q0 * (x * this.q0 - (this.q2 * z - this.q3 * y)) + s * this.q1) - x,
         2.0 * (this.q0 * (y * this.q0 - (this.q3 * x - this.q1 * z)) + s * this.q2) - y,
         2.0 * (this.q0 * (z * this.q0 - (this.q1 * y - this.q2 * x)) + s * this.q3) - z
      );
   }

   public Vector3D applyInverseTo(Vector3D u) {
      double x = u.getX();
      double y = u.getY();
      double z = u.getZ();
      double s = this.q1 * x + this.q2 * y + this.q3 * z;
      double m0 = -this.q0;
      return new Vector3D(
         2.0 * (m0 * (x * m0 - (this.q2 * z - this.q3 * y)) + s * this.q1) - x,
         2.0 * (m0 * (y * m0 - (this.q3 * x - this.q1 * z)) + s * this.q2) - y,
         2.0 * (m0 * (z * m0 - (this.q1 * y - this.q2 * x)) + s * this.q3) - z
      );
   }

   public Rotation applyTo(Rotation r) {
      return new Rotation(
         r.q0 * this.q0 - (r.q1 * this.q1 + r.q2 * this.q2 + r.q3 * this.q3),
         r.q1 * this.q0 + r.q0 * this.q1 + (r.q2 * this.q3 - r.q3 * this.q2),
         r.q2 * this.q0 + r.q0 * this.q2 + (r.q3 * this.q1 - r.q1 * this.q3),
         r.q3 * this.q0 + r.q0 * this.q3 + (r.q1 * this.q2 - r.q2 * this.q1),
         false
      );
   }

   public Rotation applyInverseTo(Rotation r) {
      return new Rotation(
         -r.q0 * this.q0 - (r.q1 * this.q1 + r.q2 * this.q2 + r.q3 * this.q3),
         -r.q1 * this.q0 + r.q0 * this.q1 + (r.q2 * this.q3 - r.q3 * this.q2),
         -r.q2 * this.q0 + r.q0 * this.q2 + (r.q3 * this.q1 - r.q1 * this.q3),
         -r.q3 * this.q0 + r.q0 * this.q3 + (r.q1 * this.q2 - r.q2 * this.q1),
         false
      );
   }

   private double[][] orthogonalizeMatrix(double[][] m, double threshold) throws NotARotationMatrixException {
      double[] m0 = m[0];
      double[] m1 = m[1];
      double[] m2 = m[2];
      double x00 = m0[0];
      double x01 = m0[1];
      double x02 = m0[2];
      double x10 = m1[0];
      double x11 = m1[1];
      double x12 = m1[2];
      double x20 = m2[0];
      double x21 = m2[1];
      double x22 = m2[2];
      double fn = 0.0;
      double[][] o = new double[3][3];
      double[] o0 = o[0];
      double[] o1 = o[1];
      double[] o2 = o[2];

      double fn1;
      int i;
      for(i = 0; ++i < 11; fn = fn1) {
         double mx00 = m0[0] * x00 + m1[0] * x10 + m2[0] * x20;
         double mx10 = m0[1] * x00 + m1[1] * x10 + m2[1] * x20;
         double mx20 = m0[2] * x00 + m1[2] * x10 + m2[2] * x20;
         double mx01 = m0[0] * x01 + m1[0] * x11 + m2[0] * x21;
         double mx11 = m0[1] * x01 + m1[1] * x11 + m2[1] * x21;
         double mx21 = m0[2] * x01 + m1[2] * x11 + m2[2] * x21;
         double mx02 = m0[0] * x02 + m1[0] * x12 + m2[0] * x22;
         double mx12 = m0[1] * x02 + m1[1] * x12 + m2[1] * x22;
         double mx22 = m0[2] * x02 + m1[2] * x12 + m2[2] * x22;
         o0[0] = x00 - 0.5 * (x00 * mx00 + x01 * mx10 + x02 * mx20 - m0[0]);
         o0[1] = x01 - 0.5 * (x00 * mx01 + x01 * mx11 + x02 * mx21 - m0[1]);
         o0[2] = x02 - 0.5 * (x00 * mx02 + x01 * mx12 + x02 * mx22 - m0[2]);
         o1[0] = x10 - 0.5 * (x10 * mx00 + x11 * mx10 + x12 * mx20 - m1[0]);
         o1[1] = x11 - 0.5 * (x10 * mx01 + x11 * mx11 + x12 * mx21 - m1[1]);
         o1[2] = x12 - 0.5 * (x10 * mx02 + x11 * mx12 + x12 * mx22 - m1[2]);
         o2[0] = x20 - 0.5 * (x20 * mx00 + x21 * mx10 + x22 * mx20 - m2[0]);
         o2[1] = x21 - 0.5 * (x20 * mx01 + x21 * mx11 + x22 * mx21 - m2[1]);
         o2[2] = x22 - 0.5 * (x20 * mx02 + x21 * mx12 + x22 * mx22 - m2[2]);
         double corr00 = o0[0] - m0[0];
         double corr01 = o0[1] - m0[1];
         double corr02 = o0[2] - m0[2];
         double corr10 = o1[0] - m1[0];
         double corr11 = o1[1] - m1[1];
         double corr12 = o1[2] - m1[2];
         double corr20 = o2[0] - m2[0];
         double corr21 = o2[1] - m2[1];
         double corr22 = o2[2] - m2[2];
         fn1 = corr00 * corr00
            + corr01 * corr01
            + corr02 * corr02
            + corr10 * corr10
            + corr11 * corr11
            + corr12 * corr12
            + corr20 * corr20
            + corr21 * corr21
            + corr22 * corr22;
         if (FastMath.abs(fn1 - fn) <= threshold) {
            return o;
         }

         x00 = o0[0];
         x01 = o0[1];
         x02 = o0[2];
         x10 = o1[0];
         x11 = o1[1];
         x12 = o1[2];
         x20 = o2[0];
         x21 = o2[1];
         x22 = o2[2];
      }

      throw new NotARotationMatrixException(LocalizedFormats.UNABLE_TO_ORTHOGONOLIZE_MATRIX, i - 1);
   }

   public static double distance(Rotation r1, Rotation r2) {
      return r1.applyInverseTo(r2).getAngle();
   }
}
