package org.apache.commons.math.util;

public class FastMath {
   public static final double PI = Math.PI;
   public static final double E = Math.E;
   private static final double[] EXP_INT_TABLE_A = new double[1500];
   private static final double[] EXP_INT_TABLE_B = new double[1500];
   private static final double[] EXP_FRAC_TABLE_A = new double[1025];
   private static final double[] EXP_FRAC_TABLE_B = new double[1025];
   private static final double[] FACT = new double[20];
   private static final double[][] LN_MANT = new double[1024][];
   private static final double LN_2_A = 0.69314706F;
   private static final double LN_2_B = 1.1730463525082348E-7;
   private static final double[][] LN_SPLIT_COEF = new double[][]{
      {2.0, 0.0},
      {0.6666666F, 3.9736429850260626E-8},
      {0.39999998F, 2.3841857910019882E-8},
      {0.28571427F, 1.7029898543501842E-8},
      {0.22222221F, 1.3245471311735498E-8},
      {0.18181816F, 2.4384203044354907E-8},
      {0.15384614F, 9.140260083262505E-9},
      {0.13333333F, 9.220590270857665E-9},
      {0.11764701F, 1.2393345855018391E-8},
      {0.10526404F, 8.251545029714408E-9},
      {0.09522332F, 1.2675934823758863E-8},
      {0.087136224F, 1.1430250008909141E-8},
      {0.07842259F, 2.404307984052299E-9},
      {0.08371849F, 1.176342548272881E-8},
      {0.03058958F, 1.2958646899018938E-9},
      {0.14982304F, 1.225743062930824E-8}
   };
   private static final double[][] LN_QUICK_COEF = new double[][]{
      {1.0, 5.669184079525E-24},
      {-0.25, -0.25},
      {0.3333333F, 1.986821492305628E-8},
      {-0.25, -6.663542893624021E-14},
      {0.19999999F, 1.1921056801463227E-8},
      {-0.16666666F, -7.800414592973399E-9},
      {0.14285713F, 5.650007086920087E-9},
      {-0.1250253F, -7.44321345601866E-11},
      {0.111138076F, 9.219544613762692E-9}
   };
   private static final double[][] LN_HI_PREC_COEF = new double[][]{
      {1.0, -6.032174644509064E-23},
      {-0.25, -0.25},
      {0.3333333F, 1.9868161777724352E-8},
      {-0.24999997F, -2.957007209750105E-8},
      {0.19999954F, 1.5830993332061267E-10},
      {-0.1662488F, -2.6033824355191673E-8}
   };
   private static final double[] SINE_TABLE_A = new double[14];
   private static final double[] SINE_TABLE_B = new double[14];
   private static final double[] COSINE_TABLE_A = new double[14];
   private static final double[] COSINE_TABLE_B = new double[14];
   private static final double[] TANGENT_TABLE_A = new double[14];
   private static final double[] TANGENT_TABLE_B = new double[14];
   private static final long[] RECIP_2PI = new long[]{
      2935890503282001226L,
      9154082963658192752L,
      3952090531849364496L,
      9193070505571053912L,
      7910884519577875640L,
      113236205062349959L,
      4577762542105553359L,
      -5034868814120038111L,
      4208363204685324176L,
      5648769086999809661L,
      2819561105158720014L,
      -4035746434778044925L,
      -302932621132653753L,
      -2644281811660520851L,
      -3183605296591799669L,
      6722166367014452318L,
      -3512299194304650054L,
      -7278142539171889152L
   };
   private static final long[] PI_O_4_BITS = new long[]{-3958705157555305932L, -4267615245585081135L};
   private static final double[] EIGHTHS = new double[]{0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0, 1.125, 1.25, 1.375, 1.5, 1.625};
   private static final double[] CBRTTWO = new double[]{0.6299605249474366, 0.7937005259840998, 1.0, 1.2599210498948732, 1.5874010519681994};
   private static final long HEX_40000000 = 1073741824L;
   private static final long MASK_30BITS = -1073741824L;
   private static final double TWO_POWER_52 = 4.5035996E15F;

   private FastMath() {
   }

   private static double doubleHighPart(double d) {
      if (d > -Double.MIN_NORMAL && d < Double.MIN_NORMAL) {
         return d;
      } else {
         long xl = Double.doubleToLongBits(d);
         xl &= -1073741824L;
         return Double.longBitsToDouble(xl);
      }
   }

   public static double sqrt(double a) {
      return Math.sqrt(a);
   }

   public static double cosh(double x) {
      if (x != x) {
         return x;
      } else if (x > 20.0) {
         return exp(x) / 2.0;
      } else if (x < -20.0) {
         return exp(-x) / 2.0;
      } else {
         double[] hiPrec = new double[2];
         if (x < 0.0) {
            x = -x;
         }

         exp(x, 0.0, hiPrec);
         double ya = hiPrec[0] + hiPrec[1];
         double yb = -(ya - hiPrec[0] - hiPrec[1]);
         double temp = ya * 1.0737418E9F;
         double yaa = ya + temp - temp;
         double yab = ya - yaa;
         double recip = 1.0 / ya;
         temp = recip * 1.0737418E9F;
         double recipa = recip + temp - temp;
         double recipb = recip - recipa;
         recipb += (1.0 - yaa * recipa - yaa * recipb - yab * recipa - yab * recipb) * recip;
         recipb += -yb * recip * recip;
         temp = ya + recipa;
         yb += -(temp - ya - recipa);
         double var26 = temp + recipb;
         yb += -(var26 - temp - recipb);
         double result = var26 + yb;
         return result * 0.5;
      }
   }

   public static double sinh(double x) {
      boolean negate = false;
      if (x != x) {
         return x;
      } else if (x > 20.0) {
         return exp(x) / 2.0;
      } else if (x < -20.0) {
         return -exp(-x) / 2.0;
      } else if (x == 0.0) {
         return x;
      } else {
         if (x < 0.0) {
            x = -x;
            negate = true;
         }

         double result;
         if (x > 0.25) {
            double[] hiPrec = new double[2];
            exp(x, 0.0, hiPrec);
            double ya = hiPrec[0] + hiPrec[1];
            double yb = -(ya - hiPrec[0] - hiPrec[1]);
            double temp = ya * 1.0737418E9F;
            double yaa = ya + temp - temp;
            double yab = ya - yaa;
            double recip = 1.0 / ya;
            temp = recip * 1.0737418E9F;
            double recipa = recip + temp - temp;
            double recipb = recip - recipa;
            recipb += (1.0 - yaa * recipa - yaa * recipb - yab * recipa - yab * recipb) * recip;
            recipb += -yb * recip * recip;
            recipa = -recipa;
            recipb = -recipb;
            temp = ya + recipa;
            yb += -(temp - ya - recipa);
            double var41 = temp + recipb;
            yb += -(var41 - temp - recipb);
            result = var41 + yb;
            result *= 0.5;
         } else {
            double[] hiPrec = new double[2];
            expm1(x, hiPrec);
            double ya = hiPrec[0] + hiPrec[1];
            double yb = -(ya - hiPrec[0] - hiPrec[1]);
            double denom = 1.0 + ya;
            double denomr = 1.0 / denom;
            double denomb = -(denom - 1.0 - ya) + yb;
            double ratio = ya * denomr;
            double temp = ratio * 1.0737418E9F;
            double ra = ratio + temp - temp;
            double rb = ratio - ra;
            temp = denom * 1.0737418E9F;
            double za = denom + temp - temp;
            double zb = denom - za;
            rb += (ya - za * ra - za * rb - zb * ra - zb * rb) * denomr;
            rb += yb * denomr;
            rb += -ya * denomb * denomr * denomr;
            temp = ya + ra;
            yb += -(temp - ya - ra);
            double var50 = temp + rb;
            yb += -(var50 - temp - rb);
            result = var50 + yb;
            result *= 0.5;
         }

         if (negate) {
            result = -result;
         }

         return result;
      }
   }

   public static double tanh(double x) {
      boolean negate = false;
      if (x != x) {
         return x;
      } else if (x > 20.0) {
         return 1.0;
      } else if (x < -20.0) {
         return -1.0;
      } else if (x == 0.0) {
         return x;
      } else {
         if (x < 0.0) {
            x = -x;
            negate = true;
         }

         double result;
         if (x >= 0.5) {
            double[] hiPrec = new double[2];
            exp(x * 2.0, 0.0, hiPrec);
            double ya = hiPrec[0] + hiPrec[1];
            double yb = -(ya - hiPrec[0] - hiPrec[1]);
            double na = -1.0 + ya;
            double nb = -(na + 1.0 - ya);
            double temp = na + yb;
            nb += -(temp - na - yb);
            double da = 1.0 + ya;
            double db = -(da - 1.0 - ya);
            double var35 = da + yb;
            db += -(var35 - da - yb);
            double var36 = var35 * 1.0737418E9F;
            double daa = var35 + var36 - var36;
            double dab = var35 - daa;
            double ratio = temp / var35;
            double var37 = ratio * 1.0737418E9F;
            double ratioa = ratio + var37 - var37;
            double ratiob = ratio - ratioa;
            ratiob += (temp - daa * ratioa - daa * ratiob - dab * ratioa - dab * ratiob) / var35;
            ratiob += nb / var35;
            ratiob += -db * temp / var35 / var35;
            result = ratioa + ratiob;
         } else {
            double[] hiPrec = new double[2];
            expm1(x * 2.0, hiPrec);
            double ya = hiPrec[0] + hiPrec[1];
            double yb = -(ya - hiPrec[0] - hiPrec[1]);
            double da = 2.0 + ya;
            double db = -(da - 2.0 - ya);
            double temp = da + yb;
            db += -(temp - da - yb);
            double var45 = temp * 1.0737418E9F;
            double daa = temp + var45 - var45;
            double dab = temp - daa;
            double ratio = ya / temp;
            double var46 = ratio * 1.0737418E9F;
            double ratioa = ratio + var46 - var46;
            double ratiob = ratio - ratioa;
            ratiob += (ya - daa * ratioa - daa * ratiob - dab * ratioa - dab * ratiob) / temp;
            ratiob += yb / temp;
            ratiob += -db * ya / temp / temp;
            result = ratioa + ratiob;
         }

         if (negate) {
            result = -result;
         }

         return result;
      }
   }

   public static double acosh(double a) {
      return log(a + sqrt(a * a - 1.0));
   }

   public static double asinh(double a) {
      boolean negative = false;
      if (a < 0.0) {
         negative = true;
         a = -a;
      }

      double absAsinh;
      if (a > 0.167) {
         absAsinh = log(sqrt(a * a + 1.0) + a);
      } else {
         double a2 = a * a;
         if (a > 0.097) {
            absAsinh = a
               * (
                  1.0
                     - a2
                        * (
                           0.3333333333333333
                              - a2
                                 * (
                                    0.2
                                       - a2
                                          * (
                                             0.14285714285714285
                                                - a2
                                                   * (
                                                      0.1111111111111111
                                                         - a2
                                                            * (
                                                               0.09090909090909091
                                                                  - a2
                                                                     * (
                                                                        0.07692307692307693
                                                                           - a2
                                                                              * (0.06666666666666667 - a2 * 0.058823529411764705 * 15.0 / 16.0)
                                                                              * 13.0
                                                                              / 14.0
                                                                     )
                                                                     * 11.0
                                                                     / 12.0
                                                            )
                                                            * 9.0
                                                            / 10.0
                                                   )
                                                   * 7.0
                                                   / 8.0
                                          )
                                          * 5.0
                                          / 6.0
                                 )
                                 * 3.0
                                 / 4.0
                        )
                        / 2.0
               );
         } else if (a > 0.036) {
            absAsinh = a
               * (
                  1.0
                     - a2
                        * (
                           0.3333333333333333
                              - a2
                                 * (
                                    0.2
                                       - a2
                                          * (
                                             0.14285714285714285
                                                - a2
                                                   * (0.1111111111111111 - a2 * (0.09090909090909091 - a2 * 0.07692307692307693 * 11.0 / 12.0) * 9.0 / 10.0)
                                                   * 7.0
                                                   / 8.0
                                          )
                                          * 5.0
                                          / 6.0
                                 )
                                 * 3.0
                                 / 4.0
                        )
                        / 2.0
               );
         } else if (a > 0.0036) {
            absAsinh = a
               * (
                  1.0
                     - a2 * (0.3333333333333333 - a2 * (0.2 - a2 * (0.14285714285714285 - a2 * 0.1111111111111111 * 7.0 / 8.0) * 5.0 / 6.0) * 3.0 / 4.0) / 2.0
               );
         } else {
            absAsinh = a * (1.0 - a2 * (0.3333333333333333 - a2 * 0.2 * 3.0 / 4.0) / 2.0);
         }
      }

      return negative ? -absAsinh : absAsinh;
   }

   public static double atanh(double a) {
      boolean negative = false;
      if (a < 0.0) {
         negative = true;
         a = -a;
      }

      double absAtanh;
      if (a > 0.15) {
         absAtanh = 0.5 * log((1.0 + a) / (1.0 - a));
      } else {
         double a2 = a * a;
         if (a > 0.087) {
            absAtanh = a
               * (
                  1.0
                     + a2
                        * (
                           0.3333333333333333
                              + a2
                                 * (
                                    0.2
                                       + a2
                                          * (
                                             0.14285714285714285
                                                + a2
                                                   * (
                                                      0.1111111111111111
                                                         + a2
                                                            * (
                                                               0.09090909090909091
                                                                  + a2 * (0.07692307692307693 + a2 * (0.06666666666666667 + a2 * 0.058823529411764705))
                                                            )
                                                   )
                                          )
                                 )
                        )
               );
         } else if (a > 0.031) {
            absAtanh = a
               * (
                  1.0
                     + a2
                        * (
                           0.3333333333333333
                              + a2 * (0.2 + a2 * (0.14285714285714285 + a2 * (0.1111111111111111 + a2 * (0.09090909090909091 + a2 * 0.07692307692307693))))
                        )
               );
         } else if (a > 0.003) {
            absAtanh = a * (1.0 + a2 * (0.3333333333333333 + a2 * (0.2 + a2 * (0.14285714285714285 + a2 * 0.1111111111111111))));
         } else {
            absAtanh = a * (1.0 + a2 * (0.3333333333333333 + a2 * 0.2));
         }
      }

      return negative ? -absAtanh : absAtanh;
   }

   public static double signum(double a) {
      return a < 0.0 ? -1.0 : (a > 0.0 ? 1.0 : a);
   }

   public static float signum(float a) {
      return a < 0.0F ? -1.0F : (a > 0.0F ? 1.0F : a);
   }

   public static double nextUp(double a) {
      return nextAfter(a, Double.POSITIVE_INFINITY);
   }

   public static float nextUp(float a) {
      return nextAfter(a, Double.POSITIVE_INFINITY);
   }

   public static double random() {
      return Math.random();
   }

   public static double exp(double x) {
      return exp(x, 0.0, null);
   }

   private static double exp(double x, double extra, double[] hiPrec) {
      double intPartA;
      double intPartB;
      int intVal;
      if (x < 0.0) {
         intVal = (int)(-x);
         if (intVal > 746) {
            if (hiPrec != null) {
               hiPrec[0] = 0.0;
               hiPrec[1] = 0.0;
            }

            return 0.0;
         }

         if (intVal > 709) {
            double result = exp(x + 40.191406F, extra, hiPrec) / 2.8504009514401178E17;
            if (hiPrec != null) {
               hiPrec[0] /= 2.8504009514401178E17;
               hiPrec[1] /= 2.8504009514401178E17;
            }

            return result;
         }

         if (intVal == 709) {
            double result = exp(x + 1.4941406F, extra, hiPrec) / 4.455505956692757;
            if (hiPrec != null) {
               hiPrec[0] /= 4.455505956692757;
               hiPrec[1] /= 4.455505956692757;
            }

            return result;
         }

         ++intVal;
         intPartA = EXP_INT_TABLE_A[750 - intVal];
         intPartB = EXP_INT_TABLE_B[750 - intVal];
         intVal = -intVal;
      } else {
         intVal = (int)x;
         if (intVal > 709) {
            if (hiPrec != null) {
               hiPrec[0] = Double.POSITIVE_INFINITY;
               hiPrec[1] = 0.0;
            }

            return Double.POSITIVE_INFINITY;
         }

         intPartA = EXP_INT_TABLE_A[750 + intVal];
         intPartB = EXP_INT_TABLE_B[750 + intVal];
      }

      int intFrac = (int)((x - (double)intVal) * 1024.0);
      double fracPartA = EXP_FRAC_TABLE_A[intFrac];
      double fracPartB = EXP_FRAC_TABLE_B[intFrac];
      double epsilon = x - ((double)intVal + (double)intFrac / 1024.0);
      double z = 0.04168701738764507;
      z = z * epsilon + 0.1666666505023083;
      z = z * epsilon + 0.5000000000042687;
      z = z * epsilon + 1.0;
      z = z * epsilon + -3.940510424527919E-20;
      double tempA = intPartA * fracPartA;
      double tempB = intPartA * fracPartB + intPartB * fracPartA + intPartB * fracPartB;
      double tempC = tempB + tempA;
      double result;
      if (extra != 0.0) {
         result = tempC * extra * z + tempC * extra + tempC * z + tempB + tempA;
      } else {
         result = tempC * z + tempB + tempA;
      }

      if (hiPrec != null) {
         hiPrec[0] = tempA;
         hiPrec[1] = tempC * extra * z + tempC * extra + tempC * z + tempB;
      }

      return result;
   }

   public static double expm1(double x) {
      return expm1(x, null);
   }

   private static double expm1(double x, double[] hiPrecOut) {
      if (x != x || x == 0.0) {
         return x;
      } else if (!(x <= -1.0) && !(x >= 1.0)) {
         boolean negative = false;
         if (x < 0.0) {
            x = -x;
            negative = true;
         }

         int intFrac = (int)(x * 1024.0);
         double tempA = EXP_FRAC_TABLE_A[intFrac] - 1.0;
         double tempB = EXP_FRAC_TABLE_B[intFrac];
         double temp = tempA + tempB;
         tempB = -(temp - tempA - tempB);
         double var58 = temp * 1.0737418E9F;
         double baseA = temp + var58 - var58;
         double baseB = tempB + (temp - baseA);
         double epsilon = x - (double)intFrac / 1024.0;
         double zb = 0.008336750013465571;
         double var35 = zb * epsilon + 0.041666663879186654;
         double var36 = var35 * epsilon + 0.16666666666745392;
         double var37 = var36 * epsilon + 0.49999999999999994;
         double var38 = var37 * epsilon;
         double var39 = var38 * epsilon;
         double tempx = epsilon + var39;
         double var40 = -(tempx - epsilon - var39);
         double var47 = tempx * 1.0737418E9F;
         double var48 = tempx + var47 - var47;
         double var41 = var40 + (tempx - var48);
         double ya = var48 * baseA;
         tempx = ya + var48 * baseB;
         double yb = -(tempx - ya - var48 * baseB);
         double var50 = tempx + var41 * baseA;
         yb += -(var50 - tempx - var41 * baseA);
         tempx = var50 + var41 * baseB;
         yb += -(tempx - var50 - var41 * baseB);
         double var52 = tempx + baseA;
         yb += -(var52 - baseA - tempx);
         tempx = var52 + var48;
         yb += -(tempx - var52 - var48);
         double var54 = tempx + baseB;
         yb += -(var54 - tempx - baseB);
         tempx = var54 + var41;
         yb += -(tempx - var54 - var41);
         ya = tempx;
         if (negative) {
            double denom = 1.0 + tempx;
            double denomr = 1.0 / denom;
            double denomb = -(denom - 1.0 - tempx) + yb;
            double ratio = tempx * denomr;
            double var56 = ratio * 1.0737418E9F;
            double ra = ratio + var56 - var56;
            double rb = ratio - ra;
            double var57 = denom * 1.0737418E9F;
            double var45 = denom + var57 - var57;
            double var42 = denom - var45;
            rb += (tempx - var45 * ra - var45 * rb - var42 * ra - var42 * rb) * denomr;
            rb += yb * denomr;
            rb += -tempx * denomb * denomr * denomr;
            ya = -ra;
            yb = -rb;
         }

         if (hiPrecOut != null) {
            hiPrecOut[0] = ya;
            hiPrecOut[1] = yb;
         }

         return ya + yb;
      } else {
         double[] hiPrec = new double[2];
         exp(x, 0.0, hiPrec);
         if (x > 0.0) {
            return -1.0 + hiPrec[0] + hiPrec[1];
         } else {
            double ra = -1.0 + hiPrec[0];
            double rb = -(ra + 1.0 - hiPrec[0]);
            rb += hiPrec[1];
            return ra + rb;
         }
      }
   }

   private static double slowexp(double x, double[] result) {
      double[] xs = new double[2];
      double[] ys = new double[2];
      double[] facts = new double[2];
      double[] as = new double[2];
      split(x, xs);
      ys[0] = ys[1] = 0.0;

      for(int i = 19; i >= 0; --i) {
         splitMult(xs, ys, as);
         ys[0] = as[0];
         ys[1] = as[1];
         split(FACT[i], as);
         splitReciprocal(as, facts);
         splitAdd(ys, facts, as);
         ys[0] = as[0];
         ys[1] = as[1];
      }

      if (result != null) {
         result[0] = ys[0];
         result[1] = ys[1];
      }

      return ys[0] + ys[1];
   }

   private static void split(double d, double[] split) {
      if (d < 8.0E298 && d > -8.0E298) {
         double a = d * 1.0737418E9F;
         split[0] = d + a - a;
         split[1] = d - split[0];
      } else {
         double a = d * 9.313226E-10F;
         split[0] = (d + a - d) * 1.0737418E9F;
         split[1] = d - split[0];
      }
   }

   private static void resplit(double[] a) {
      double c = a[0] + a[1];
      double d = -(c - a[0] - a[1]);
      if (c < 8.0E298 && c > -8.0E298) {
         double z = c * 1.0737418E9F;
         a[0] = c + z - z;
         a[1] = c - a[0] + d;
      } else {
         double z = c * 9.313226E-10F;
         a[0] = (c + z - c) * 1.0737418E9F;
         a[1] = c - a[0] + d;
      }
   }

   private static void splitMult(double[] a, double[] b, double[] ans) {
      ans[0] = a[0] * b[0];
      ans[1] = a[0] * b[1] + a[1] * b[0] + a[1] * b[1];
      resplit(ans);
   }

   private static void splitAdd(double[] a, double[] b, double[] ans) {
      ans[0] = a[0] + b[0];
      ans[1] = a[1] + b[1];
      resplit(ans);
   }

   private static void splitReciprocal(double[] in, double[] result) {
      double b = 2.3841858E-7F;
      double a = 0.99999976F;
      if (in[0] == 0.0) {
         in[0] = in[1];
         in[1] = 0.0;
      }

      result[0] = 0.99999976F / in[0];
      result[1] = (2.3841858E-7F * in[0] - 0.99999976F * in[1]) / (in[0] * in[0] + in[0] * in[1]);
      if (result[1] != result[1]) {
         result[1] = 0.0;
      }

      resplit(result);

      for(int i = 0; i < 2; ++i) {
         double err = 1.0 - result[0] * in[0] - result[0] * in[1] - result[1] * in[0] - result[1] * in[1];
         err *= result[0] + result[1];
         result[1] += err;
      }
   }

   private static void quadMult(double[] a, double[] b, double[] result) {
      double[] xs = new double[2];
      double[] ys = new double[2];
      double[] zs = new double[2];
      split(a[0], xs);
      split(b[0], ys);
      splitMult(xs, ys, zs);
      result[0] = zs[0];
      result[1] = zs[1];
      split(b[1], ys);
      splitMult(xs, ys, zs);
      double tmp = result[0] + zs[0];
      result[1] -= tmp - result[0] - zs[0];
      result[0] = tmp;
      tmp = result[0] + zs[1];
      result[1] -= tmp - result[0] - zs[1];
      result[0] = tmp;
      split(a[1], xs);
      split(b[0], ys);
      splitMult(xs, ys, zs);
      tmp = result[0] + zs[0];
      result[1] -= tmp - result[0] - zs[0];
      result[0] = tmp;
      tmp = result[0] + zs[1];
      result[1] -= tmp - result[0] - zs[1];
      result[0] = tmp;
      split(a[1], xs);
      split(b[1], ys);
      splitMult(xs, ys, zs);
      tmp = result[0] + zs[0];
      result[1] -= tmp - result[0] - zs[0];
      result[0] = tmp;
      tmp = result[0] + zs[1];
      result[1] -= tmp - result[0] - zs[1];
      result[0] = tmp;
   }

   private static double expint(int p, double[] result) {
      double[] xs = new double[2];
      double[] as = new double[2];
      double[] ys = new double[2];
      xs[0] = Math.E;
      xs[1] = 1.4456468917292502E-16;
      split(1.0, ys);

      while(p > 0) {
         if ((p & 1) != 0) {
            quadMult(ys, xs, as);
            ys[0] = as[0];
            ys[1] = as[1];
         }

         quadMult(xs, xs, as);
         xs[0] = as[0];
         xs[1] = as[1];
         p >>= 1;
      }

      if (result != null) {
         result[0] = ys[0];
         result[1] = ys[1];
         resplit(result);
      }

      return ys[0] + ys[1];
   }

   public static double log(double x) {
      return log(x, null);
   }

   private static double log(double x, double[] hiPrec) {
      if (x == 0.0) {
         return Double.NEGATIVE_INFINITY;
      } else {
         long bits = Double.doubleToLongBits(x);
         if (((bits & Long.MIN_VALUE) != 0L || x != x) && x != 0.0) {
            if (hiPrec != null) {
               hiPrec[0] = Double.NaN;
            }

            return Double.NaN;
         } else if (x == Double.POSITIVE_INFINITY) {
            if (hiPrec != null) {
               hiPrec[0] = Double.POSITIVE_INFINITY;
            }

            return Double.POSITIVE_INFINITY;
         } else {
            int exp = (int)(bits >> 52) - 1023;
            if ((bits & 9218868437227405312L) == 0L) {
               if (x == 0.0) {
                  if (hiPrec != null) {
                     hiPrec[0] = Double.NEGATIVE_INFINITY;
                  }

                  return Double.NEGATIVE_INFINITY;
               }

               for(bits <<= 1; (bits & 4503599627370496L) == 0L; bits <<= 1) {
                  --exp;
               }
            }

            if ((exp == -1 || exp == 0) && x < 1.01 && x > 0.99 && hiPrec == null) {
               double xa = x - 1.0;
               double xb = xa - x + 1.0;
               double tmp = xa * 1.0737418E9F;
               double aa = xa + tmp - tmp;
               double ab = xa - aa;
               xa = aa;
               xb = ab;
               double ya = LN_QUICK_COEF[LN_QUICK_COEF.length - 1][0];
               double yb = LN_QUICK_COEF[LN_QUICK_COEF.length - 1][1];

               for(int i = LN_QUICK_COEF.length - 2; i >= 0; --i) {
                  aa = ya * xa;
                  ab = ya * xb + yb * xa + yb * xb;
                  tmp = aa * 1.0737418E9F;
                  double var68 = aa + tmp - tmp;
                  yb = aa - var68 + ab;
                  aa = var68 + LN_QUICK_COEF[i][0];
                  ab = yb + LN_QUICK_COEF[i][1];
                  tmp = aa * 1.0737418E9F;
                  ya = aa + tmp - tmp;
                  yb = aa - ya + ab;
               }

               aa = ya * xa;
               ab = ya * xb + yb * xa + yb * xb;
               tmp = aa * 1.0737418E9F;
               ya = aa + tmp - tmp;
               yb = aa - ya + ab;
               return ya + yb;
            } else {
               double[] lnm = LN_MANT[(int)((bits & 4499201580859392L) >> 42)];
               double epsilon = (double)(bits & 4398046511103L) / (4.5035996E15F + (double)(bits & 4499201580859392L));
               double lnza = 0.0;
               double lnzb = 0.0;
               if (hiPrec != null) {
                  double tmp = epsilon * 1.0737418E9F;
                  double aa = epsilon + tmp - tmp;
                  double ab = epsilon - aa;
                  double xa = aa;
                  double numer = (double)(bits & 4398046511103L);
                  double denom = 4.5035996E15F + (double)(bits & 4499201580859392L);
                  aa = numer - aa * denom - ab * denom;
                  double xb = ab + aa / denom;
                  double ya = LN_HI_PREC_COEF[LN_HI_PREC_COEF.length - 1][0];
                  double yb = LN_HI_PREC_COEF[LN_HI_PREC_COEF.length - 1][1];

                  for(int i = LN_HI_PREC_COEF.length - 2; i >= 0; --i) {
                     aa = ya * xa;
                     ab = ya * xb + yb * xa + yb * xb;
                     tmp = aa * 1.0737418E9F;
                     double var85 = aa + tmp - tmp;
                     yb = aa - var85 + ab;
                     aa = var85 + LN_HI_PREC_COEF[i][0];
                     ab = yb + LN_HI_PREC_COEF[i][1];
                     tmp = aa * 1.0737418E9F;
                     ya = aa + tmp - tmp;
                     yb = aa - ya + ab;
                  }

                  aa = ya * xa;
                  ab = ya * xb + yb * xa + yb * xb;
                  lnza = aa + ab;
                  lnzb = -(lnza - aa - ab);
               } else {
                  lnza = -0.16624882440418567;
                  lnza = lnza * epsilon + 0.19999954120254515;
                  lnza = lnza * epsilon + -0.2499999997677497;
                  lnza = lnza * epsilon + 0.3333333333332802;
                  lnza = lnza * epsilon + -0.5;
                  lnza = lnza * epsilon + 1.0;
                  lnza *= epsilon;
               }

               double a = 0.69314706F * (double)exp;
               double b = 0.0;
               double c = a + lnm[0];
               double d = -(c - a - lnm[0]);
               b += d;
               double var74 = c + lnza;
               d = -(var74 - c - lnza);
               b += d;
               c = var74 + 1.1730463525082348E-7 * (double)exp;
               d = -(c - var74 - 1.1730463525082348E-7 * (double)exp);
               b += d;
               double var76 = c + lnm[1];
               d = -(var76 - c - lnm[1]);
               b += d;
               c = var76 + lnzb;
               d = -(c - var76 - lnzb);
               b += d;
               if (hiPrec != null) {
                  hiPrec[0] = c;
                  hiPrec[1] = b;
               }

               return c + b;
            }
         }
      }
   }

   public static double log1p(double x) {
      double xpa = 1.0 + x;
      double xpb = -(xpa - 1.0 - x);
      if (x == -1.0) {
         return x / 0.0;
      } else if (x > 0.0 && 1.0 / x == 0.0) {
         return x;
      } else if (!(x > 1.0E-6) && !(x < -1.0E-6)) {
         double y = x * 0.333333333333333 - 0.5;
         y = y * x + 1.0;
         return y * x;
      } else {
         double[] hiPrec = new double[2];
         double lores = log(xpa, hiPrec);
         if (Double.isInfinite(lores)) {
            return lores;
         } else {
            double fx1 = xpb / xpa;
            double epsilon = 0.5 * fx1 + 1.0;
            epsilon *= fx1;
            return epsilon + hiPrec[1] + hiPrec[0];
         }
      }
   }

   public static double log10(double x) {
      double[] hiPrec = new double[2];
      double lores = log(x, hiPrec);
      if (Double.isInfinite(lores)) {
         return lores;
      } else {
         double tmp = hiPrec[0] * 1.0737418E9F;
         double lna = hiPrec[0] + tmp - tmp;
         double lnb = hiPrec[0] - lna + hiPrec[1];
         double rln10a = 0.43429446F;
         double rln10b = 1.9699272335463627E-8;
         return 1.9699272335463627E-8 * lnb + 1.9699272335463627E-8 * lna + 0.43429446F * lnb + 0.43429446F * lna;
      }
   }

   public static double pow(double x, double y) {
      double[] lns = new double[2];
      if (y == 0.0) {
         return 1.0;
      } else if (x != x) {
         return x;
      } else if (x == 0.0) {
         long bits = Double.doubleToLongBits(x);
         if ((bits & Long.MIN_VALUE) != 0L) {
            long yi = (long)y;
            if (y < 0.0 && y == (double)yi && (yi & 1L) == 1L) {
               return Double.NEGATIVE_INFINITY;
            }

            if (y < 0.0 && y == (double)yi && (yi & 1L) == 1L) {
               return -0.0;
            }

            if (y > 0.0 && y == (double)yi && (yi & 1L) == 1L) {
               return -0.0;
            }
         }

         if (y < 0.0) {
            return Double.POSITIVE_INFINITY;
         } else {
            return y > 0.0 ? 0.0 : Double.NaN;
         }
      } else if (x == Double.POSITIVE_INFINITY) {
         if (y != y) {
            return y;
         } else {
            return y < 0.0 ? 0.0 : Double.POSITIVE_INFINITY;
         }
      } else if (y == Double.POSITIVE_INFINITY) {
         if (x * x == 1.0) {
            return Double.NaN;
         } else {
            return x * x > 1.0 ? Double.POSITIVE_INFINITY : 0.0;
         }
      } else {
         if (x == Double.NEGATIVE_INFINITY) {
            if (y != y) {
               return y;
            }

            if (y < 0.0) {
               long yi = (long)y;
               if (y == (double)yi && (yi & 1L) == 1L) {
                  return -0.0;
               }

               return 0.0;
            }

            if (y > 0.0) {
               long yi = (long)y;
               if (y == (double)yi && (yi & 1L) == 1L) {
                  return Double.NEGATIVE_INFINITY;
               }

               return Double.POSITIVE_INFINITY;
            }
         }

         if (y == Double.NEGATIVE_INFINITY) {
            if (x * x == 1.0) {
               return Double.NaN;
            } else {
               return x * x < 1.0 ? Double.POSITIVE_INFINITY : 0.0;
            }
         } else if (x < 0.0) {
            if (y >= 4.5035996E15F || y <= -4.5035996E15F) {
               return pow(-x, y);
            } else if (y == (double)((long)y)) {
               return ((long)y & 1L) == 0L ? pow(-x, y) : -pow(-x, y);
            } else {
               return Double.NaN;
            }
         } else {
            double ya;
            double yb;
            if (y < 8.0E298 && y > -8.0E298) {
               double tmp1 = y * 1.0737418E9F;
               ya = y + tmp1 - tmp1;
               yb = y - ya;
            } else {
               double tmp1 = y * 9.313226E-10F;
               double tmp2 = tmp1 * 9.313226E-10F;
               ya = (tmp1 + tmp2 - tmp1) * 1.0737418E9F * 1.0737418E9F;
               yb = y - ya;
            }

            double lores = log(x, lns);
            if (Double.isInfinite(lores)) {
               return lores;
            } else {
               double lna = lns[0];
               double lnb = lns[1];
               double tmp1 = lna * 1.0737418E9F;
               double tmp2 = lna + tmp1 - tmp1;
               lnb += lna - tmp2;
               double aa = tmp2 * ya;
               double ab = tmp2 * yb + lnb * ya + lnb * yb;
               lna = aa + ab;
               lnb = -(lna - aa - ab);
               double z = 0.008333333333333333;
               z = z * lnb + 0.041666666666666664;
               z = z * lnb + 0.16666666666666666;
               z = z * lnb + 0.5;
               z = z * lnb + 1.0;
               z *= lnb;
               return exp(lna, z, null);
            }
         }
      }
   }

   private static double[] slowLog(double xi) {
      double[] x = new double[2];
      double[] x2 = new double[2];
      double[] y = new double[2];
      double[] a = new double[2];
      split(xi, x);
      x[0]++;
      resplit(x);
      splitReciprocal(x, a);
      x[0] -= 2.0;
      resplit(x);
      splitMult(x, a, y);
      x[0] = y[0];
      x[1] = y[1];
      splitMult(x, x, x2);
      y[0] = LN_SPLIT_COEF[LN_SPLIT_COEF.length - 1][0];
      y[1] = LN_SPLIT_COEF[LN_SPLIT_COEF.length - 1][1];

      for(int i = LN_SPLIT_COEF.length - 2; i >= 0; --i) {
         splitMult(y, x2, a);
         y[0] = a[0];
         y[1] = a[1];
         splitAdd(y, LN_SPLIT_COEF[i], a);
         y[0] = a[0];
         y[1] = a[1];
      }

      splitMult(y, x, a);
      y[0] = a[0];
      y[1] = a[1];
      return y;
   }

   private static double slowSin(double x, double[] result) {
      double[] xs = new double[2];
      double[] ys = new double[2];
      double[] facts = new double[2];
      double[] as = new double[2];
      split(x, xs);
      ys[0] = ys[1] = 0.0;

      for(int i = 19; i >= 0; --i) {
         splitMult(xs, ys, as);
         ys[0] = as[0];
         ys[1] = as[1];
         if ((i & 1) != 0) {
            split(FACT[i], as);
            splitReciprocal(as, facts);
            if ((i & 2) != 0) {
               facts[0] = -facts[0];
               facts[1] = -facts[1];
            }

            splitAdd(ys, facts, as);
            ys[0] = as[0];
            ys[1] = as[1];
         }
      }

      if (result != null) {
         result[0] = ys[0];
         result[1] = ys[1];
      }

      return ys[0] + ys[1];
   }

   private static double slowCos(double x, double[] result) {
      double[] xs = new double[2];
      double[] ys = new double[2];
      double[] facts = new double[2];
      double[] as = new double[2];
      split(x, xs);
      ys[0] = ys[1] = 0.0;

      for(int i = 19; i >= 0; --i) {
         splitMult(xs, ys, as);
         ys[0] = as[0];
         ys[1] = as[1];
         if ((i & 1) == 0) {
            split(FACT[i], as);
            splitReciprocal(as, facts);
            if ((i & 2) != 0) {
               facts[0] = -facts[0];
               facts[1] = -facts[1];
            }

            splitAdd(ys, facts, as);
            ys[0] = as[0];
            ys[1] = as[1];
         }
      }

      if (result != null) {
         result[0] = ys[0];
         result[1] = ys[1];
      }

      return ys[0] + ys[1];
   }

   private static void buildSinCosTables() {
      double[] result = new double[2];

      for(int i = 0; i < 7; ++i) {
         double x = (double)i / 8.0;
         slowSin(x, result);
         SINE_TABLE_A[i] = result[0];
         SINE_TABLE_B[i] = result[1];
         slowCos(x, result);
         COSINE_TABLE_A[i] = result[0];
         COSINE_TABLE_B[i] = result[1];
      }

      for(int i = 7; i < 14; ++i) {
         double[] xs = new double[2];
         double[] ys = new double[2];
         double[] as = new double[2];
         double[] bs = new double[2];
         double[] temps = new double[2];
         if ((i & 1) == 0) {
            xs[0] = SINE_TABLE_A[i / 2];
            xs[1] = SINE_TABLE_B[i / 2];
            ys[0] = COSINE_TABLE_A[i / 2];
            ys[1] = COSINE_TABLE_B[i / 2];
            splitMult(xs, ys, result);
            SINE_TABLE_A[i] = result[0] * 2.0;
            SINE_TABLE_B[i] = result[1] * 2.0;
            splitMult(ys, ys, as);
            splitMult(xs, xs, temps);
            temps[0] = -temps[0];
            temps[1] = -temps[1];
            splitAdd(as, temps, result);
            COSINE_TABLE_A[i] = result[0];
            COSINE_TABLE_B[i] = result[1];
         } else {
            xs[0] = SINE_TABLE_A[i / 2];
            xs[1] = SINE_TABLE_B[i / 2];
            ys[0] = COSINE_TABLE_A[i / 2];
            ys[1] = COSINE_TABLE_B[i / 2];
            as[0] = SINE_TABLE_A[i / 2 + 1];
            as[1] = SINE_TABLE_B[i / 2 + 1];
            bs[0] = COSINE_TABLE_A[i / 2 + 1];
            bs[1] = COSINE_TABLE_B[i / 2 + 1];
            splitMult(xs, bs, temps);
            splitMult(ys, as, result);
            splitAdd(result, temps, result);
            SINE_TABLE_A[i] = result[0];
            SINE_TABLE_B[i] = result[1];
            splitMult(ys, bs, result);
            splitMult(xs, as, temps);
            temps[0] = -temps[0];
            temps[1] = -temps[1];
            splitAdd(result, temps, result);
            COSINE_TABLE_A[i] = result[0];
            COSINE_TABLE_B[i] = result[1];
         }
      }

      for(int i = 0; i < 14; ++i) {
         double[] xs = new double[2];
         double[] ys = new double[2];
         double[] as = new double[]{COSINE_TABLE_A[i], COSINE_TABLE_B[i]};
         splitReciprocal(as, ys);
         xs[0] = SINE_TABLE_A[i];
         xs[1] = SINE_TABLE_B[i];
         splitMult(xs, ys, as);
         TANGENT_TABLE_A[i] = as[0];
         TANGENT_TABLE_B[i] = as[1];
      }
   }

   private static double polySine(double x) {
      double x2 = x * x;
      double p = 2.7553817452272217E-6;
      p = p * x2 + -1.9841269659586505E-4;
      p = p * x2 + 0.008333333333329196;
      p = p * x2 + -0.16666666666666666;
      return p * x2 * x;
   }

   private static double polyCosine(double x) {
      double x2 = x * x;
      double p = 2.479773539153719E-5;
      p = p * x2 + -0.0013888888689039883;
      p = p * x2 + 0.041666666666621166;
      p = p * x2 + -0.49999999999999994;
      return p * x2;
   }

   private static double sinQ(double xa, double xb) {
      int idx = (int)(xa * 8.0 + 0.5);
      double epsilon = xa - EIGHTHS[idx];
      double sintA = SINE_TABLE_A[idx];
      double sintB = SINE_TABLE_B[idx];
      double costA = COSINE_TABLE_A[idx];
      double costB = COSINE_TABLE_B[idx];
      double sinEpsB = polySine(epsilon);
      double cosEpsA = 1.0;
      double cosEpsB = polyCosine(epsilon);
      double temp = epsilon * 1.0737418E9F;
      double temp2 = epsilon + temp - temp;
      sinEpsB += epsilon - temp2;
      double a = 0.0;
      double b = 0.0;
      double c = a + sintA;
      double d = -(c - a - sintA);
      b += d;
      double t = costA * temp2;
      double var47 = c + t;
      d = -(var47 - c - t);
      a = var47;
      b += d;
      b = b + sintA * cosEpsB + costA * sinEpsB;
      b = b + sintB + costB * temp2 + sintB * cosEpsB + costB * sinEpsB;
      if (xb != 0.0) {
         t = ((costA + costB) * (1.0 + cosEpsB) - (sintA + sintB) * (temp2 + sinEpsB)) * xb;
         c = var47 + t;
         d = -(c - var47 - t);
         a = c;
         b += d;
      }

      return a + b;
   }

   private static double cosQ(double xa, double xb) {
      double pi2a = Math.PI / 2;
      double pi2b = 6.123233995736766E-17;
      double a = (Math.PI / 2) - xa;
      double b = -(a - (Math.PI / 2) + xa);
      b += 6.123233995736766E-17 - xb;
      return sinQ(a, b);
   }

   private static double tanQ(double xa, double xb, boolean cotanFlag) {
      int idx = (int)(xa * 8.0 + 0.5);
      double epsilon = xa - EIGHTHS[idx];
      double sintA = SINE_TABLE_A[idx];
      double sintB = SINE_TABLE_B[idx];
      double costA = COSINE_TABLE_A[idx];
      double costB = COSINE_TABLE_B[idx];
      double sinEpsB = polySine(epsilon);
      double cosEpsA = 1.0;
      double cosEpsB = polyCosine(epsilon);
      double temp = epsilon * 1.0737418E9F;
      double temp2 = epsilon + temp - temp;
      sinEpsB += epsilon - temp2;
      double a = 0.0;
      double b = 0.0;
      double c = a + sintA;
      double d = -(c - a - sintA);
      b += d;
      double t = costA * temp2;
      double var77 = c + t;
      d = -(var77 - c - t);
      b += d;
      b = b + sintA * cosEpsB + costA * sinEpsB;
      b = b + sintB + costB * temp2 + sintB * cosEpsB + costB * sinEpsB;
      double sina = var77 + b;
      double sinb = -(sina - var77 - b);
      d = 0.0;
      c = 0.0;
      b = 0.0;
      a = 0.0;
      t = costA * 1.0;
      c = a + t;
      d = -(c - a - t);
      b += d;
      t = -sintA * temp2;
      double var80 = c + t;
      d = -(var80 - c - t);
      b += d;
      b = b + costB * 1.0 + costA * cosEpsB + costB * cosEpsB;
      b -= sintB * temp2 + sintA * sinEpsB + sintB * sinEpsB;
      double cosa = var80 + b;
      double cosb = -(cosa - var80 - b);
      if (cotanFlag) {
         double tmp = cosa;
         cosa = sina;
         sina = tmp;
         tmp = cosb;
         cosb = sinb;
         sinb = tmp;
      }

      double est = sina / cosa;
      temp = est * 1.0737418E9F;
      double esta = est + temp - temp;
      double estb = est - esta;
      temp = cosa * 1.0737418E9F;
      double cosaa = cosa + temp - temp;
      double cosab = cosa - cosaa;
      double err = (sina - esta * cosaa - esta * cosab - estb * cosaa - estb * cosab) / cosa;
      err += sinb / cosa;
      err += -sina * cosb / cosa / cosa;
      if (xb != 0.0) {
         double xbadj = xb + est * est * xb;
         if (cotanFlag) {
            xbadj = -xbadj;
         }

         err += xbadj;
      }

      return est + err;
   }

   private static void reducePayneHanek(double x, double[] result) {
      long inbits = Double.doubleToLongBits(x);
      int exponent = (int)(inbits >> 52 & 2047L) - 1023;
      inbits &= 4503599627370495L;
      inbits |= 4503599627370496L;
      ++exponent;
      inbits <<= 11;
      int idx = exponent >> 6;
      int shift = exponent - (idx << 6);
      long shpiA;
      long shpiB;
      long shpi0;
      if (shift != 0) {
         shpi0 = idx == 0 ? 0L : RECIP_2PI[idx - 1] << shift;
         shpi0 |= RECIP_2PI[idx] >>> 64 - shift;
         shpiA = RECIP_2PI[idx] << shift | RECIP_2PI[idx + 1] >>> 64 - shift;
         shpiB = RECIP_2PI[idx + 1] << shift | RECIP_2PI[idx + 2] >>> 64 - shift;
      } else {
         shpi0 = idx == 0 ? 0L : RECIP_2PI[idx - 1];
         shpiA = RECIP_2PI[idx];
         shpiB = RECIP_2PI[idx + 1];
      }

      long a = inbits >>> 32;
      long b = inbits & 4294967295L;
      long c = shpiA >>> 32;
      long d = shpiA & 4294967295L;
      long ac = a * c;
      long bd = b * d;
      long bc = b * c;
      long ad = a * d;
      long prodB = bd + (ad << 32);
      long prodA = ac + (ad >>> 32);
      boolean bita = (bd & Long.MIN_VALUE) != 0L;
      boolean bitb = (ad & 2147483648L) != 0L;
      boolean bitsum = (prodB & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prodA;
      }

      bita = (prodB & Long.MIN_VALUE) != 0L;
      bitb = (bc & 2147483648L) != 0L;
      prodB += bc << 32;
      prodA += bc >>> 32;
      bitsum = (prodB & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prodA;
      }

      c = shpiB >>> 32;
      d = shpiB & 4294967295L;
      ac = a * c;
      bc = b * c;
      ad = a * d;
      ac += bc + ad >>> 32;
      bita = (prodB & Long.MIN_VALUE) != 0L;
      bitb = (ac & Long.MIN_VALUE) != 0L;
      prodB += ac;
      bitsum = (prodB & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prodA;
      }

      c = shpi0 >>> 32;
      d = shpi0 & 4294967295L;
      bd = b * d;
      bc = b * c;
      ad = a * d;
      prodA += bd + (bc + ad << 32);
      int intPart = (int)(prodA >>> 62);
      prodA <<= 2;
      prodA |= prodB >>> 62;
      prodB <<= 2;
      a = prodA >>> 32;
      b = prodA & 4294967295L;
      c = PI_O_4_BITS[0] >>> 32;
      d = PI_O_4_BITS[0] & 4294967295L;
      ac = a * c;
      bd = b * d;
      bc = b * c;
      ad = a * d;
      long prod2B = bd + (ad << 32);
      long prod2A = ac + (ad >>> 32);
      bita = (bd & Long.MIN_VALUE) != 0L;
      bitb = (ad & 2147483648L) != 0L;
      bitsum = (prod2B & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prod2A;
      }

      bita = (prod2B & Long.MIN_VALUE) != 0L;
      bitb = (bc & 2147483648L) != 0L;
      prod2B += bc << 32;
      prod2A += bc >>> 32;
      bitsum = (prod2B & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prod2A;
      }

      c = PI_O_4_BITS[1] >>> 32;
      d = PI_O_4_BITS[1] & 4294967295L;
      ac = a * c;
      bc = b * c;
      ad = a * d;
      ac += bc + ad >>> 32;
      bita = (prod2B & Long.MIN_VALUE) != 0L;
      bitb = (ac & Long.MIN_VALUE) != 0L;
      prod2B += ac;
      bitsum = (prod2B & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prod2A;
      }

      a = prodB >>> 32;
      b = prodB & 4294967295L;
      c = PI_O_4_BITS[0] >>> 32;
      d = PI_O_4_BITS[0] & 4294967295L;
      ac = a * c;
      bc = b * c;
      ad = a * d;
      ac += bc + ad >>> 32;
      bita = (prod2B & Long.MIN_VALUE) != 0L;
      bitb = (ac & Long.MIN_VALUE) != 0L;
      prod2B += ac;
      bitsum = (prod2B & Long.MIN_VALUE) != 0L;
      if (bita && bitb || (bita || bitb) && !bitsum) {
         ++prod2A;
      }

      double tmpA = (double)(prod2A >>> 12) / 4.5035996E15F;
      double tmpB = (double)(((prod2A & 4095L) << 40) + (prod2B >>> 24)) / 4.5035996E15F / 4.5035996E15F;
      double sumA = tmpA + tmpB;
      double sumB = -(sumA - tmpA - tmpB);
      result[0] = (double)intPart;
      result[1] = sumA * 2.0;
      result[2] = sumB * 2.0;
   }

   public static double sin(double x) {
      boolean negative = false;
      int quadrant = 0;
      double xb = 0.0;
      double xa = x;
      if (x < 0.0) {
         negative = true;
         xa = -x;
      }

      if (xa == 0.0) {
         long bits = Double.doubleToLongBits(x);
         return bits < 0L ? -0.0 : 0.0;
      } else if (xa == xa && xa != Double.POSITIVE_INFINITY) {
         if (xa > 3294198.0) {
            double[] reduceResults = new double[3];
            reducePayneHanek(xa, reduceResults);
            quadrant = (int)reduceResults[0] & 3;
            xa = reduceResults[1];
            xb = reduceResults[2];
         } else if (xa > Math.PI / 2) {
            int k = (int)(xa * 0.6366197723675814);

            while(true) {
               double a = (double)(-k) * 1.5707963F;
               double remA = xa + a;
               double remB = -(remA - xa - a);
               a = (double)(-k) * 7.549789948768648E-8;
               double var19 = a + remA;
               remB += -(var19 - remA - a);
               a = (double)(-k) * 6.123233995736766E-17;
               remA = a + var19;
               remB += -(remA - var19 - a);
               if (remA > 0.0) {
                  quadrant = k & 3;
                  xa = remA;
                  xb = remB;
                  break;
               }

               --k;
            }
         }

         if (negative) {
            quadrant ^= 2;
         }

         switch(quadrant) {
            case 0:
               return sinQ(xa, xb);
            case 1:
               return cosQ(xa, xb);
            case 2:
               return -sinQ(xa, xb);
            case 3:
               return -cosQ(xa, xb);
            default:
               return Double.NaN;
         }
      } else {
         return Double.NaN;
      }
   }

   public static double cos(double x) {
      int quadrant = 0;
      double xa = x;
      if (x < 0.0) {
         xa = -x;
      }

      if (xa == xa && xa != Double.POSITIVE_INFINITY) {
         double xb = 0.0;
         if (xa > 3294198.0) {
            double[] reduceResults = new double[3];
            reducePayneHanek(xa, reduceResults);
            quadrant = (int)reduceResults[0] & 3;
            xa = reduceResults[1];
            xb = reduceResults[2];
         } else if (xa > Math.PI / 2) {
            int k = (int)(xa * 0.6366197723675814);

            while(true) {
               double a = (double)(-k) * 1.5707963F;
               double remA = xa + a;
               double remB = -(remA - xa - a);
               a = (double)(-k) * 7.549789948768648E-8;
               double var17 = a + remA;
               remB += -(var17 - remA - a);
               a = (double)(-k) * 6.123233995736766E-17;
               remA = a + var17;
               remB += -(remA - var17 - a);
               if (remA > 0.0) {
                  quadrant = k & 3;
                  xa = remA;
                  xb = remB;
                  break;
               }

               --k;
            }
         }

         switch(quadrant) {
            case 0:
               return cosQ(xa, xb);
            case 1:
               return -sinQ(xa, xb);
            case 2:
               return -cosQ(xa, xb);
            case 3:
               return sinQ(xa, xb);
            default:
               return Double.NaN;
         }
      } else {
         return Double.NaN;
      }
   }

   public static double tan(double x) {
      boolean negative = false;
      int quadrant = 0;
      double xa = x;
      if (x < 0.0) {
         negative = true;
         xa = -x;
      }

      if (xa == 0.0) {
         long bits = Double.doubleToLongBits(x);
         return bits < 0L ? -0.0 : 0.0;
      } else if (xa == xa && xa != Double.POSITIVE_INFINITY) {
         double xb = 0.0;
         if (xa > 3294198.0) {
            double[] reduceResults = new double[3];
            reducePayneHanek(xa, reduceResults);
            quadrant = (int)reduceResults[0] & 3;
            xa = reduceResults[1];
            xb = reduceResults[2];
         } else if (xa > Math.PI / 2) {
            int k = (int)(xa * 0.6366197723675814);

            while(true) {
               double a = (double)(-k) * 1.5707963F;
               double remA = xa + a;
               double remB = -(remA - xa - a);
               a = (double)(-k) * 7.549789948768648E-8;
               double var21 = a + remA;
               remB += -(var21 - remA - a);
               a = (double)(-k) * 6.123233995736766E-17;
               remA = a + var21;
               remB += -(remA - var21 - a);
               if (remA > 0.0) {
                  quadrant = k & 3;
                  xa = remA;
                  xb = remB;
                  break;
               }

               --k;
            }
         }

         if (xa > 1.5) {
            double pi2a = Math.PI / 2;
            double pi2b = 6.123233995736766E-17;
            double a = (Math.PI / 2) - xa;
            double b = -(a - (Math.PI / 2) + xa);
            b += 6.123233995736766E-17 - xb;
            xa = a + b;
            xb = -(xa - a - b);
            quadrant ^= 1;
            negative ^= true;
         }

         double result;
         if ((quadrant & 1) == 0) {
            result = tanQ(xa, xb, false);
         } else {
            result = -tanQ(xa, xb, true);
         }

         if (negative) {
            result = -result;
         }

         return result;
      } else {
         return Double.NaN;
      }
   }

   public static double atan(double x) {
      return atan(x, 0.0, false);
   }

   private static double atan(double xa, double xb, boolean leftPlane) {
      boolean negate = false;
      if (xa == 0.0) {
         return leftPlane ? copySign(Math.PI, xa) : xa;
      } else {
         if (xa < 0.0) {
            xa = -xa;
            xb = -xb;
            negate = true;
         }

         if (xa > 1.633123935319537E16) {
            return negate ^ leftPlane ? -Math.PI / 2 : Math.PI / 2;
         } else {
            int idx;
            if (xa < 1.0) {
               idx = (int)((-1.7168146928204135 * xa * xa + 8.0) * xa + 0.5);
            } else {
               double temp = 1.0 / xa;
               idx = (int)(-((-1.7168146928204135 * temp * temp + 8.0) * temp) + 13.07);
            }

            double epsA = xa - TANGENT_TABLE_A[idx];
            double epsB = -(epsA - xa + TANGENT_TABLE_A[idx]);
            epsB += xb - TANGENT_TABLE_B[idx];
            double temp = epsA + epsB;
            epsB = -(temp - epsA - epsB);
            temp = xa * 1.0737418E9F;
            double ya = xa + temp - temp;
            double yb = xb + xa - ya;
            xb += yb;
            if (idx == 0) {
               double denom = 1.0 / (1.0 + (ya + xb) * (TANGENT_TABLE_A[idx] + TANGENT_TABLE_B[idx]));
               ya = temp * denom;
               yb = epsB * denom;
            } else {
               double temp2 = ya * TANGENT_TABLE_A[idx];
               double za = 1.0 + temp2;
               double zb = -(za - 1.0 - temp2);
               temp2 = xb * TANGENT_TABLE_A[idx] + ya * TANGENT_TABLE_B[idx];
               double var38 = za + temp2;
               zb += -(var38 - za - temp2);
               zb += xb * TANGENT_TABLE_B[idx];
               ya = temp / var38;
               double var39 = ya * 1.0737418E9F;
               double yaa = ya + var39 - var39;
               double yab = ya - yaa;
               double var40 = var38 * 1.0737418E9F;
               double zaa = var38 + var40 - var40;
               double zab = var38 - zaa;
               yb = (temp - yaa * zaa - yaa * zab - yab * zaa - yab * zab) / var38;
               yb += -temp * zb / var38 / var38;
               yb += epsB / var38;
            }

            double epsA2 = ya * ya;
            double var47 = 0.07490822288864472;
            double var48 = var47 * epsA2 + -0.09088450866185192;
            double var49 = var48 * epsA2 + 0.11111095942313305;
            double var50 = var49 * epsA2 + -0.1428571423679182;
            double var51 = var50 * epsA2 + 0.19999999999923582;
            double var52 = var51 * epsA2 + -0.33333333333333287;
            double var53 = var52 * epsA2 * ya;
            temp = ya + var53;
            double var54 = -(temp - ya - var53);
            yb = var54 + yb / (1.0 + ya * ya);
            double za = EIGHTHS[idx] + temp;
            double zb = -(za - EIGHTHS[idx] - temp);
            temp = za + yb;
            zb += -(temp - za - yb);
            double result = temp + zb;
            double resultb = -(result - temp - zb);
            if (leftPlane) {
               double pia = Math.PI;
               double pib = 1.2246467991473532E-16;
               za = Math.PI - result;
               zb = -(za - Math.PI + result);
               zb += 1.2246467991473532E-16 - resultb;
               result = za + zb;
               resultb = -(result - za - zb);
            }

            if (negate ^ leftPlane) {
               result = -result;
            }

            return result;
         }
      }
   }

   public static double atan2(double y, double x) {
      if (x != x || y != y) {
         return Double.NaN;
      } else if (y == 0.0) {
         double result = x * y;
         double invx = 1.0 / x;
         double invy = 1.0 / y;
         if (invx == 0.0) {
            return x > 0.0 ? y : copySign(Math.PI, y);
         } else if (!(x < 0.0) && !(invx < 0.0)) {
            return result;
         } else {
            return !(y < 0.0) && !(invy < 0.0) ? Math.PI : -Math.PI;
         }
      } else if (y == Double.POSITIVE_INFINITY) {
         if (x == Double.POSITIVE_INFINITY) {
            return Math.PI / 4;
         } else {
            return x == Double.NEGATIVE_INFINITY ? Math.PI * 3.0 / 4.0 : Math.PI / 2;
         }
      } else if (y != Double.NEGATIVE_INFINITY) {
         if (x == Double.POSITIVE_INFINITY) {
            if (y > 0.0 || 1.0 / y > 0.0) {
               return 0.0;
            }

            if (y < 0.0 || 1.0 / y < 0.0) {
               return -0.0;
            }
         }

         if (x == Double.NEGATIVE_INFINITY) {
            if (y > 0.0 || 1.0 / y > 0.0) {
               return Math.PI;
            }

            if (y < 0.0 || 1.0 / y < 0.0) {
               return -Math.PI;
            }
         }

         if (x == 0.0) {
            if (y > 0.0 || 1.0 / y > 0.0) {
               return Math.PI / 2;
            }

            if (y < 0.0 || 1.0 / y < 0.0) {
               return -Math.PI / 2;
            }
         }

         double r = y / x;
         if (Double.isInfinite(r)) {
            return atan(r, 0.0, x < 0.0);
         } else {
            double ra = doubleHighPart(r);
            double rb = r - ra;
            double xa = doubleHighPart(x);
            double xb = x - xa;
            rb += (y - ra * xa - ra * xb - rb * xa - rb * xb) / x;
            double temp = ra + rb;
            rb = -(temp - ra - rb);
            ra = temp;
            if (temp == 0.0) {
               ra = copySign(0.0, y);
            }

            return atan(ra, rb, x < 0.0);
         }
      } else if (x == Double.POSITIVE_INFINITY) {
         return -Math.PI / 4;
      } else {
         return x == Double.NEGATIVE_INFINITY ? -Math.PI * 3.0 / 4.0 : -Math.PI / 2;
      }
   }

   public static double asin(double x) {
      if (x != x) {
         return Double.NaN;
      } else if (x > 1.0 || x < -1.0) {
         return Double.NaN;
      } else if (x == 1.0) {
         return Math.PI / 2;
      } else if (x == -1.0) {
         return -Math.PI / 2;
      } else if (x == 0.0) {
         return x;
      } else {
         double temp = x * 1.0737418E9F;
         double xa = x + temp - temp;
         double xb = x - xa;
         double ya = xa * xa;
         double yb = xa * xb * 2.0 + xb * xb;
         ya = -ya;
         yb = -yb;
         double za = 1.0 + ya;
         double zb = -(za - 1.0 - ya);
         temp = za + yb;
         zb += -(temp - za - yb);
         double y = sqrt(temp);
         double var27 = y * 1.0737418E9F;
         ya = y + var27 - var27;
         yb = y - ya;
         yb += (temp - ya * ya - 2.0 * ya * yb - yb * yb) / (2.0 * y);
         double dx = zb / (2.0 * y);
         double r = x / y;
         temp = r * 1.0737418E9F;
         double ra = r + temp - temp;
         double rb = r - ra;
         rb += (x - ra * ya - ra * yb - rb * ya - rb * yb) / y;
         rb += -x * dx / y / y;
         temp = ra + rb;
         rb = -(temp - ra - rb);
         return atan(temp, rb, false);
      }
   }

   public static double acos(double x) {
      if (x != x) {
         return Double.NaN;
      } else if (x > 1.0 || x < -1.0) {
         return Double.NaN;
      } else if (x == -1.0) {
         return Math.PI;
      } else if (x == 1.0) {
         return 0.0;
      } else if (x == 0.0) {
         return Math.PI / 2;
      } else {
         double temp = x * 1.0737418E9F;
         double xa = x + temp - temp;
         double xb = x - xa;
         double ya = xa * xa;
         double yb = xa * xb * 2.0 + xb * xb;
         ya = -ya;
         yb = -yb;
         double za = 1.0 + ya;
         double zb = -(za - 1.0 - ya);
         temp = za + yb;
         zb += -(temp - za - yb);
         double y = sqrt(temp);
         double var25 = y * 1.0737418E9F;
         ya = y + var25 - var25;
         yb = y - ya;
         yb += (temp - ya * ya - 2.0 * ya * yb - yb * yb) / (2.0 * y);
         yb += zb / (2.0 * y);
         y = ya + yb;
         yb = -(y - ya - yb);
         double r = y / x;
         if (Double.isInfinite(r)) {
            return Math.PI / 2;
         } else {
            double ra = doubleHighPart(r);
            double rb = r - ra;
            rb += (y - ra * xa - ra * xb - rb * xa - rb * xb) / x;
            rb += yb / x;
            temp = ra + rb;
            rb = -(temp - ra - rb);
            return atan(temp, rb, x < 0.0);
         }
      }
   }

   public static double cbrt(double x) {
      long inbits = Double.doubleToLongBits(x);
      int exponent = (int)(inbits >> 52 & 2047L) - 1023;
      boolean subnormal = false;
      if (exponent == -1023) {
         if (x == 0.0) {
            return x;
         }

         subnormal = true;
         x *= 1.8014398E16F;
         inbits = Double.doubleToLongBits(x);
         exponent = (int)(inbits >> 52 & 2047L) - 1023;
      }

      if (exponent == 1024) {
         return x;
      } else {
         int exp3 = exponent / 3;
         double p2 = Double.longBitsToDouble(inbits & Long.MIN_VALUE | (long)(exp3 + 1023 & 2047) << 52);
         double mant = Double.longBitsToDouble(inbits & 4503599627370495L | 4607182418800017408L);
         double est = -0.010714690733195933;
         est = est * mant + 0.0875862700108075;
         est = est * mant + -0.3058015757857271;
         est = est * mant + 0.7249995199969751;
         est = est * mant + 0.5039018405998233;
         est *= CBRTTWO[exponent % 3 + 2];
         double xs = x / (p2 * p2 * p2);
         est += (xs - est * est * est) / (3.0 * est * est);
         est += (xs - est * est * est) / (3.0 * est * est);
         double temp = est * 1.0737418E9F;
         double ya = est + temp - temp;
         double yb = est - ya;
         double za = ya * ya;
         double zb = ya * yb * 2.0 + yb * yb;
         temp = za * 1.0737418E9F;
         double temp2 = za + temp - temp;
         zb += za - temp2;
         zb = temp2 * yb + ya * zb + zb * yb;
         za = temp2 * ya;
         double na = xs - za;
         double nb = -(na - xs + za);
         nb -= zb;
         est += (na + nb) / (3.0 * est * est);
         est *= p2;
         if (subnormal) {
            est *= 3.8146973E-6F;
         }

         return est;
      }
   }

   public static double toRadians(double x) {
      if (!Double.isInfinite(x) && x != 0.0) {
         double facta = 0.01745329F;
         double factb = 1.997844754509471E-9;
         double xa = doubleHighPart(x);
         double xb = x - xa;
         double result = xb * 1.997844754509471E-9 + xb * 0.01745329F + xa * 1.997844754509471E-9 + xa * 0.01745329F;
         if (result == 0.0) {
            result *= x;
         }

         return result;
      } else {
         return x;
      }
   }

   public static double toDegrees(double x) {
      if (!Double.isInfinite(x) && x != 0.0) {
         double facta = 180.0F / (float)Math.PI;
         double factb = 3.145894820876798E-6;
         double xa = doubleHighPart(x);
         double xb = x - xa;
         return xb * 3.145894820876798E-6 + xb * 180.0F / (float)Math.PI + xa * 3.145894820876798E-6 + xa * 180.0F / (float)Math.PI;
      } else {
         return x;
      }
   }

   public static int abs(int x) {
      return x < 0 ? -x : x;
   }

   public static long abs(long x) {
      return x < 0L ? -x : x;
   }

   public static float abs(float x) {
      return x < 0.0F ? -x : (x == 0.0F ? 0.0F : x);
   }

   public static double abs(double x) {
      return x < 0.0 ? -x : (x == 0.0 ? 0.0 : x);
   }

   public static double ulp(double x) {
      return Double.isInfinite(x) ? Double.POSITIVE_INFINITY : abs(x - Double.longBitsToDouble(Double.doubleToLongBits(x) ^ 1L));
   }

   public static float ulp(float x) {
      return Float.isInfinite(x) ? Float.POSITIVE_INFINITY : abs(x - Float.intBitsToFloat(Float.floatToIntBits(x) ^ 1));
   }

   public static double scalb(double d, int n) {
      if (n > -1023 && n < 1024) {
         return d * Double.longBitsToDouble((long)(n + 1023) << 52);
      } else if (Double.isNaN(d) || Double.isInfinite(d) || d == 0.0) {
         return d;
      } else if (n < -2098) {
         return d > 0.0 ? 0.0 : -0.0;
      } else if (n > 2097) {
         return d > 0.0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
      } else {
         long bits = Double.doubleToLongBits(d);
         long sign = bits & Long.MIN_VALUE;
         int exponent = (int)(bits >>> 52) & 2047;
         long mantissa = bits & 4503599627370495L;
         int scaledExponent = exponent + n;
         if (n < 0) {
            if (scaledExponent > 0) {
               return Double.longBitsToDouble(sign | (long)scaledExponent << 52 | mantissa);
            } else if (scaledExponent > -53) {
               mantissa |= 4503599627370496L;
               long mostSignificantLostBit = mantissa & 1L << -scaledExponent;
               mantissa >>>= 1 - scaledExponent;
               if (mostSignificantLostBit != 0L) {
                  ++mantissa;
               }

               return Double.longBitsToDouble(sign | mantissa);
            } else {
               return sign == 0L ? 0.0 : -0.0;
            }
         } else if (exponent == 0) {
            while(mantissa >>> 52 != 1L) {
               mantissa <<= 1;
               --scaledExponent;
            }

            ++scaledExponent;
            mantissa &= 4503599627370495L;
            if (scaledExponent < 2047) {
               return Double.longBitsToDouble(sign | (long)scaledExponent << 52 | mantissa);
            } else {
               return sign == 0L ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            }
         } else if (scaledExponent < 2047) {
            return Double.longBitsToDouble(sign | (long)scaledExponent << 52 | mantissa);
         } else {
            return sign == 0L ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
         }
      }
   }

   public static float scalb(float f, int n) {
      if (n > -127 && n < 128) {
         return f * Float.intBitsToFloat(n + 127 << 23);
      } else if (Float.isNaN(f) || Float.isInfinite(f) || f == 0.0F) {
         return f;
      } else if (n < -277) {
         return f > 0.0F ? 0.0F : -0.0F;
      } else if (n > 276) {
         return f > 0.0F ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
      } else {
         int bits = Float.floatToIntBits(f);
         int sign = bits & -2147483648;
         int exponent = bits >>> 23 & 0xFF;
         int mantissa = bits & 8388607;
         int scaledExponent = exponent + n;
         if (n < 0) {
            if (scaledExponent > 0) {
               return Float.intBitsToFloat(sign | scaledExponent << 23 | mantissa);
            } else if (scaledExponent > -24) {
               mantissa |= 8388608;
               int mostSignificantLostBit = mantissa & 1 << -scaledExponent;
               mantissa >>>= 1 - scaledExponent;
               if (mostSignificantLostBit != 0) {
                  ++mantissa;
               }

               return Float.intBitsToFloat(sign | mantissa);
            } else {
               return sign == 0 ? 0.0F : -0.0F;
            }
         } else if (exponent == 0) {
            while(mantissa >>> 23 != 1) {
               mantissa <<= 1;
               --scaledExponent;
            }

            ++scaledExponent;
            mantissa &= 8388607;
            if (scaledExponent < 255) {
               return Float.intBitsToFloat(sign | scaledExponent << 23 | mantissa);
            } else {
               return sign == 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
            }
         } else if (scaledExponent < 255) {
            return Float.intBitsToFloat(sign | scaledExponent << 23 | mantissa);
         } else {
            return sign == 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
         }
      }
   }

   public static double nextAfter(double d, double direction) {
      if (Double.isNaN(d) || Double.isNaN(direction)) {
         return Double.NaN;
      } else if (d == direction) {
         return direction;
      } else if (Double.isInfinite(d)) {
         return d < 0.0 ? -Double.MAX_VALUE : Double.MAX_VALUE;
      } else if (d == 0.0) {
         return direction < 0.0 ? -Double.MIN_VALUE : Double.MIN_VALUE;
      } else {
         long bits = Double.doubleToLongBits(d);
         long sign = bits & Long.MIN_VALUE;
         return direction < d ^ sign == 0L
            ? Double.longBitsToDouble(sign | (bits & Long.MAX_VALUE) + 1L)
            : Double.longBitsToDouble(sign | (bits & Long.MAX_VALUE) - 1L);
      }
   }

   public static float nextAfter(float f, double direction) {
      if (!Double.isNaN((double)f) && !Double.isNaN(direction)) {
         if ((double)f == direction) {
            return (float)direction;
         } else if (Float.isInfinite(f)) {
            return f < 0.0F ? -Float.MAX_VALUE : Float.MAX_VALUE;
         } else if (f == 0.0F) {
            return direction < 0.0 ? -Float.MIN_VALUE : Float.MIN_VALUE;
         } else {
            int bits = Float.floatToIntBits(f);
            int sign = bits & -2147483648;
            return direction < (double)f ^ sign == 0
               ? Float.intBitsToFloat(sign | (bits & 2147483647) + 1)
               : Float.intBitsToFloat(sign | (bits & 2147483647) - 1);
         }
      } else {
         return Float.NaN;
      }
   }

   public static double floor(double x) {
      if (x != x) {
         return x;
      } else if (!(x >= 4.5035996E15F) && !(x <= -4.5035996E15F)) {
         long y = (long)x;
         if (x < 0.0 && (double)y != x) {
            --y;
         }

         return y == 0L ? x * (double)y : (double)y;
      } else {
         return x;
      }
   }

   public static double ceil(double x) {
      if (x != x) {
         return x;
      } else {
         double y = floor(x);
         if (y == x) {
            return y;
         } else {
            ++y;
            return y == 0.0 ? x * y : y;
         }
      }
   }

   public static double rint(double x) {
      double y = floor(x);
      double d = x - y;
      if (d > 0.5) {
         return y == -1.0 ? -0.0 : y + 1.0;
      } else if (d < 0.5) {
         return y;
      } else {
         long z = (long)y;
         return (z & 1L) == 0L ? y : y + 1.0;
      }
   }

   public static long round(double x) {
      return (long)floor(x + 0.5);
   }

   public static int round(float x) {
      return (int)floor((double)(x + 0.5F));
   }

   public static int min(int a, int b) {
      return a <= b ? a : b;
   }

   public static long min(long a, long b) {
      return a <= b ? a : b;
   }

   public static float min(float a, float b) {
      if (a > b) {
         return b;
      } else if (a < b) {
         return a;
      } else if (a != b) {
         return Float.NaN;
      } else {
         int bits = Float.floatToRawIntBits(a);
         return bits == Integer.MIN_VALUE ? a : b;
      }
   }

   public static double min(double a, double b) {
      if (a > b) {
         return b;
      } else if (a < b) {
         return a;
      } else if (a != b) {
         return Double.NaN;
      } else {
         long bits = Double.doubleToRawLongBits(a);
         return bits == Long.MIN_VALUE ? a : b;
      }
   }

   public static int max(int a, int b) {
      return a <= b ? b : a;
   }

   public static long max(long a, long b) {
      return a <= b ? b : a;
   }

   public static float max(float a, float b) {
      if (a > b) {
         return a;
      } else if (a < b) {
         return b;
      } else if (a != b) {
         return Float.NaN;
      } else {
         int bits = Float.floatToRawIntBits(a);
         return bits == Integer.MIN_VALUE ? b : a;
      }
   }

   public static double max(double a, double b) {
      if (a > b) {
         return a;
      } else if (a < b) {
         return b;
      } else if (a != b) {
         return Double.NaN;
      } else {
         long bits = Double.doubleToRawLongBits(a);
         return bits == Long.MIN_VALUE ? b : a;
      }
   }

   public static double hypot(double x, double y) {
      if (Double.isInfinite(x) || Double.isInfinite(y)) {
         return Double.POSITIVE_INFINITY;
      } else if (!Double.isNaN(x) && !Double.isNaN(y)) {
         int expX = getExponent(x);
         int expY = getExponent(y);
         if (expX > expY + 27) {
            return abs(x);
         } else if (expY > expX + 27) {
            return abs(y);
         } else {
            int middleExp = (expX + expY) / 2;
            double scaledX = scalb(x, -middleExp);
            double scaledY = scalb(y, -middleExp);
            double scaledH = sqrt(scaledX * scaledX + scaledY * scaledY);
            return scalb(scaledH, middleExp);
         }
      } else {
         return Double.NaN;
      }
   }

   public static double IEEEremainder(double dividend, double divisor) {
      return StrictMath.IEEEremainder(dividend, divisor);
   }

   public static double copySign(double magnitude, double sign) {
      long m = Double.doubleToLongBits(magnitude);
      long s = Double.doubleToLongBits(sign);
      return (m < 0L || s < 0L) && (m >= 0L || s >= 0L) ? -magnitude : magnitude;
   }

   public static float copySign(float magnitude, float sign) {
      int m = Float.floatToIntBits(magnitude);
      int s = Float.floatToIntBits(sign);
      return (m < 0 || s < 0) && (m >= 0 || s >= 0) ? -magnitude : magnitude;
   }

   public static int getExponent(double d) {
      return (int)(Double.doubleToLongBits(d) >>> 52 & 2047L) - 1023;
   }

   public static int getExponent(float f) {
      return (Float.floatToIntBits(f) >>> 23 & 0xFF) - 127;
   }

   static {
      FACT[0] = 1.0;

      for(int i = 1; i < FACT.length; ++i) {
         FACT[i] = FACT[i - 1] * (double)i;
      }

      double[] tmp = new double[2];
      double[] recip = new double[2];

      for(int var5 = 0; var5 < 750; ++var5) {
         expint(var5, tmp);
         EXP_INT_TABLE_A[var5 + 750] = tmp[0];
         EXP_INT_TABLE_B[var5 + 750] = tmp[1];
         if (var5 != 0) {
            splitReciprocal(tmp, recip);
            EXP_INT_TABLE_A[750 - var5] = recip[0];
            EXP_INT_TABLE_B[750 - var5] = recip[1];
         }
      }

      for(int var6 = 0; var6 < EXP_FRAC_TABLE_A.length; ++var6) {
         slowexp((double)var6 / 1024.0, tmp);
         EXP_FRAC_TABLE_A[var6] = tmp[0];
         EXP_FRAC_TABLE_B[var6] = tmp[1];
      }

      for(int var7 = 0; var7 < LN_MANT.length; ++var7) {
         double d = Double.longBitsToDouble((long)var7 << 42 | 4607182418800017408L);
         LN_MANT[var7] = slowLog(d);
      }

      buildSinCosTables();
   }
}
