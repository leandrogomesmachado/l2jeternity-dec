package org.apache.commons.math.dfp;

public class DfpDec extends Dfp {
   protected DfpDec(DfpField factory) {
      super(factory);
   }

   protected DfpDec(DfpField factory, byte x) {
      super(factory, x);
   }

   protected DfpDec(DfpField factory, int x) {
      super(factory, x);
   }

   protected DfpDec(DfpField factory, long x) {
      super(factory, x);
   }

   protected DfpDec(DfpField factory, double x) {
      super(factory, x);
      this.round(0);
   }

   public DfpDec(Dfp d) {
      super(d);
      this.round(0);
   }

   protected DfpDec(DfpField factory, String s) {
      super(factory, s);
      this.round(0);
   }

   protected DfpDec(DfpField factory, byte sign, byte nans) {
      super(factory, sign, nans);
   }

   @Override
   public Dfp newInstance() {
      return new DfpDec(this.getField());
   }

   @Override
   public Dfp newInstance(byte x) {
      return new DfpDec(this.getField(), x);
   }

   @Override
   public Dfp newInstance(int x) {
      return new DfpDec(this.getField(), x);
   }

   @Override
   public Dfp newInstance(long x) {
      return new DfpDec(this.getField(), x);
   }

   @Override
   public Dfp newInstance(double x) {
      return new DfpDec(this.getField(), x);
   }

   @Override
   public Dfp newInstance(Dfp d) {
      if (this.getField().getRadixDigits() != d.getField().getRadixDigits()) {
         this.getField().setIEEEFlagsBits(1);
         Dfp result = this.newInstance(this.getZero());
         result.nans = 3;
         return this.dotrap(1, "newInstance", d, result);
      } else {
         return new DfpDec(d);
      }
   }

   @Override
   public Dfp newInstance(String s) {
      return new DfpDec(this.getField(), s);
   }

   @Override
   public Dfp newInstance(byte sign, byte nans) {
      return new DfpDec(this.getField(), sign, nans);
   }

   protected int getDecimalDigits() {
      return this.getRadixDigits() * 4 - 3;
   }

   @Override
   protected int round(int in) {
      int msb = this.mant[this.mant.length - 1];
      if (msb == 0) {
         return 0;
      } else {
         int cmaxdigits = this.mant.length * 4;

         for(int lsbthreshold = 1000; lsbthreshold > msb; --cmaxdigits) {
            lsbthreshold /= 10;
         }

         int digits = this.getDecimalDigits();
         int lsbshift = cmaxdigits - digits;
         int lsd = lsbshift / 4;
         int var15 = 1;

         for(int i = 0; i < lsbshift % 4; ++i) {
            var15 *= 10;
         }

         int lsb = this.mant[lsd];
         if (var15 <= 1 && digits == 4 * this.mant.length - 3) {
            return super.round(in);
         } else {
            int discarded;
            int n;
            if (var15 == 1) {
               n = this.mant[lsd - 1] / 1000 % 10;
               this.mant[lsd - 1] %= 1000;
               discarded = in | this.mant[lsd - 1];
            } else {
               n = lsb * 10 / var15 % 10;
               discarded = in | lsb % (var15 / 10);
            }

            for(int i = 0; i < lsd; ++i) {
               discarded |= this.mant[i];
               this.mant[i] = 0;
            }

            this.mant[lsd] = lsb / var15 * var15;
            boolean inc;
            switch(this.getField().getRoundingMode()) {
               case ROUND_DOWN:
                  inc = false;
                  break;
               case ROUND_UP:
                  inc = n != 0 || discarded != 0;
                  break;
               case ROUND_HALF_UP:
                  inc = n >= 5;
                  break;
               case ROUND_HALF_DOWN:
                  inc = n > 5;
                  break;
               case ROUND_HALF_EVEN:
                  inc = n > 5 || n == 5 && discarded != 0 || n == 5 && discarded == 0 && (lsb / var15 & 1) == 1;
                  break;
               case ROUND_HALF_ODD:
                  inc = n > 5 || n == 5 && discarded != 0 || n == 5 && discarded == 0 && (lsb / var15 & 1) == 0;
                  break;
               case ROUND_CEIL:
                  inc = this.sign == 1 && (n != 0 || discarded != 0);
                  break;
               case ROUND_FLOOR:
               default:
                  inc = this.sign == -1 && (n != 0 || discarded != 0);
            }

            if (inc) {
               int rh = var15;

               for(int i = lsd; i < this.mant.length; ++i) {
                  int r = this.mant[i] + rh;
                  rh = r / 10000;
                  this.mant[i] = r % 10000;
               }

               if (rh != 0) {
                  this.shiftRight();
                  this.mant[this.mant.length - 1] = rh;
               }
            }

            if (this.exp < -32767) {
               this.getField().setIEEEFlagsBits(8);
               return 8;
            } else if (this.exp > 32768) {
               this.getField().setIEEEFlagsBits(4);
               return 4;
            } else if (n == 0 && discarded == 0) {
               return 0;
            } else {
               this.getField().setIEEEFlagsBits(16);
               return 16;
            }
         }
      }
   }

   @Override
   public Dfp nextAfter(Dfp x) {
      String trapName = "nextAfter";
      if (this.getField().getRadixDigits() != x.getField().getRadixDigits()) {
         this.getField().setIEEEFlagsBits(1);
         Dfp result = this.newInstance(this.getZero());
         result.nans = 3;
         return this.dotrap(1, "nextAfter", x, result);
      } else {
         boolean up = false;
         if (this.lessThan(x)) {
            up = true;
         }

         if (this.equals(x)) {
            return this.newInstance(x);
         } else {
            if (this.lessThan(this.getZero())) {
               up = !up;
            }

            Dfp result;
            if (up) {
               Dfp inc = this.power10(this.log10() - this.getDecimalDigits() + 1);
               inc = copysign(inc, this);
               if (this.equals(this.getZero())) {
                  inc = this.power10K(-32767 - this.mant.length - 1);
               }

               if (inc.equals(this.getZero())) {
                  result = copysign(this.newInstance(this.getZero()), this);
               } else {
                  result = this.add(inc);
               }
            } else {
               Dfp inc = this.power10(this.log10());
               inc = copysign(inc, this);
               if (this.equals(inc)) {
                  inc = inc.divide(this.power10(this.getDecimalDigits()));
               } else {
                  inc = inc.divide(this.power10(this.getDecimalDigits() - 1));
               }

               if (this.equals(this.getZero())) {
                  inc = this.power10K(-32767 - this.mant.length - 1);
               }

               if (inc.equals(this.getZero())) {
                  result = copysign(this.newInstance(this.getZero()), this);
               } else {
                  result = this.subtract(inc);
               }
            }

            if (result.classify() == 1 && this.classify() != 1) {
               this.getField().setIEEEFlagsBits(16);
               result = this.dotrap(16, "nextAfter", x, result);
            }

            if (result.equals(this.getZero()) && !this.equals(this.getZero())) {
               this.getField().setIEEEFlagsBits(16);
               result = this.dotrap(16, "nextAfter", x, result);
            }

            return result;
         }
      }
   }
}
