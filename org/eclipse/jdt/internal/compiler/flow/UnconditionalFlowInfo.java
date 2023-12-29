package org.eclipse.jdt.internal.compiler.flow;

import java.util.Arrays;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class UnconditionalFlowInfo extends FlowInfo {
   public static final boolean COVERAGE_TEST_FLAG = false;
   public static int CoverageTestId;
   public long definiteInits;
   public long potentialInits;
   public long nullBit1;
   public long nullBit2;
   public long nullBit3;
   public long nullBit4;
   public long iNBit;
   public long iNNBit;
   public static final int extraLength = 8;
   public long[][] extra;
   public int maxFieldCount;
   public static final int BitCacheSize = 64;
   public static final int IN = 6;
   public static final int INN = 7;

   public static UnconditionalFlowInfo fakeInitializedFlowInfo(int localsCount, int maxFieldCount) {
      UnconditionalFlowInfo flowInfo = new UnconditionalFlowInfo();
      flowInfo.maxFieldCount = maxFieldCount;

      for(int i = 0; i < localsCount; ++i) {
         flowInfo.markAsDefinitelyAssigned(i + maxFieldCount);
      }

      return flowInfo;
   }

   @Override
   public FlowInfo addInitializationsFrom(FlowInfo inits) {
      return this.addInfoFrom(inits, true);
   }

   @Override
   public FlowInfo addNullInfoFrom(FlowInfo inits) {
      return this.addInfoFrom(inits, false);
   }

   private FlowInfo addInfoFrom(FlowInfo inits, boolean handleInits) {
      if (this == DEAD_END) {
         return this;
      } else if (inits == DEAD_END) {
         return this;
      } else {
         UnconditionalFlowInfo otherInits = inits.unconditionalInits();
         if (handleInits) {
            this.definiteInits |= otherInits.definiteInits;
            this.potentialInits |= otherInits.potentialInits;
         }

         boolean thisHadNulls = (this.tagBits & 4) != 0;
         boolean otherHasNulls = (otherInits.tagBits & 4) != 0;
         if (otherHasNulls) {
            if (!thisHadNulls) {
               this.nullBit1 = otherInits.nullBit1;
               this.nullBit2 = otherInits.nullBit2;
               this.nullBit3 = otherInits.nullBit3;
               this.nullBit4 = otherInits.nullBit4;
               this.iNBit = otherInits.iNBit;
               this.iNNBit = otherInits.iNNBit;
            } else {
               long a1 = this.nullBit1;
               long a2 = this.nullBit2;
               long a3 = this.nullBit3;
               long a4 = this.nullBit4;
               long protNN1111 = a1 & a2 & a3 & a4;
               long acceptNonNull = otherInits.iNNBit;
               long acceptNull = otherInits.iNBit | protNN1111;
               long dontResetToStart = ~protNN1111 | acceptNonNull;
               a1 &= dontResetToStart;
               a2 = dontResetToStart & acceptNull & a2;
               a3 = dontResetToStart & acceptNonNull & a3;
               a4 &= dontResetToStart;
               a1 &= a2 | a3 | a4;
               long b1 = otherInits.nullBit1;
               long var10003 = a3 & a4;
               long b2 = otherInits.nullBit2;
               long nb2;
               var10003 &= nb2 = ~otherInits.nullBit2;
               long b4 = otherInits.nullBit4;
               long nb4;
               var10003 &= nb4 = ~otherInits.nullBit4;
               long na3;
               long na4;
               long var10004 = (na4 = ~a4) | (na3 = ~a3);
               long na2;
               long var10005 = (na2 = ~a2) & nb2;
               long b3 = otherInits.nullBit3;
               long nb3;
               this.nullBit1 = otherInits.nullBit1 | a1 & (var10003 | var10004 & (var10005 | a2 & (nb3 = ~otherInits.nullBit3) & nb4));
               long na1;
               long nb1;
               this.nullBit2 = b2 & (nb4 | nb3) | na3 & na4 & b2 | a2 & (nb3 & nb4 | (nb1 = ~b1) & (na3 | (na1 = ~a1)) | a1 & b2);
               this.nullBit3 = b3 & (nb1 & (b2 | a2 | na1) | b1 & (b4 | nb2 | a1 & a3) | na1 & na2 & na4)
                  | a3 & nb2 & nb4
                  | nb1 & ((na2 & a4 | na1) & a3 | a1 & na2 & na4 & b2);
               this.nullBit4 = nb1 & (a4 & (na3 & nb3 | (a3 | na2) & nb2) | a1 & (a3 & nb2 & b4 | a2 & b2 & (b4 | a3 & na4 & nb3)))
                  | b1 & (a3 & a4 & b4 | na2 & na4 & nb3 & b4 | a2 & ((b3 | a4) & b4 | na3 & a4 & b2 & b3) | na1 & (b4 | (a4 | a2) & b2 & b3))
                  | (na1 & (na3 & nb3 | na2 & nb2) | a1 & (nb2 & nb3 | a2 & a3)) & b4;
               this.iNBit &= otherInits.iNBit;
               this.iNNBit &= otherInits.iNNBit;
            }

            this.tagBits |= 4;
         }

         if (this.extra != null || otherInits.extra != null) {
            int mergeLimit = 0;
            int copyLimit = 0;
            if (this.extra != null) {
               if (otherInits.extra != null) {
                  int length;
                  int otherLength;
                  if ((length = this.extra[0].length) >= (otherLength = otherInits.extra[0].length)) {
                     mergeLimit = otherLength;
                  } else {
                     for(int j = 0; j < 8; ++j) {
                        System.arraycopy(this.extra[j], 0, this.extra[j] = new long[otherLength], 0, length);
                     }

                     mergeLimit = length;
                     copyLimit = otherLength;
                  }
               }
            } else if (otherInits.extra != null) {
               this.extra = new long[8][];
               int otherLength;
               System.arraycopy(otherInits.extra[0], 0, this.extra[0] = new long[otherLength = otherInits.extra[0].length], 0, otherLength);
               System.arraycopy(otherInits.extra[1], 0, this.extra[1] = new long[otherLength], 0, otherLength);
               if (otherHasNulls) {
                  for(int j = 2; j < 8; ++j) {
                     System.arraycopy(otherInits.extra[j], 0, this.extra[j] = new long[otherLength], 0, otherLength);
                  }
               } else {
                  for(int j = 2; j < 8; ++j) {
                     this.extra[j] = new long[otherLength];
                  }

                  System.arraycopy(otherInits.extra[6], 0, this.extra[6], 0, otherLength);
                  System.arraycopy(otherInits.extra[7], 0, this.extra[7], 0, otherLength);
               }
            }

            if (handleInits) {
               int i;
               for(i = 0; i < mergeLimit; ++i) {
                  this.extra[0][i] |= otherInits.extra[0][i];
                  this.extra[1][i] |= otherInits.extra[1][i];
               }

               while(i < copyLimit) {
                  this.extra[0][i] = otherInits.extra[0][i];
                  this.extra[1][i] = otherInits.extra[1][i];
                  ++i;
               }
            }

            if (!thisHadNulls) {
               if (copyLimit < mergeLimit) {
                  copyLimit = mergeLimit;
               }

               mergeLimit = 0;
            }

            if (!otherHasNulls) {
               copyLimit = 0;
               mergeLimit = 0;
            }

            int i;
            for(i = 0; i < mergeLimit; ++i) {
               long a1 = this.extra[2][i];
               long a2 = this.extra[3][i];
               long a3 = this.extra[4][i];
               long a4 = this.extra[5][i];
               long protNN1111 = a1 & a2 & a3 & a4;
               long acceptNonNull = otherInits.extra[7][i];
               long acceptNull = otherInits.extra[6][i] | protNN1111;
               long dontResetToStart = ~protNN1111 | acceptNonNull;
               a1 &= dontResetToStart;
               a2 = dontResetToStart & acceptNull & a2;
               a3 = dontResetToStart & acceptNonNull & a3;
               a4 &= dontResetToStart;
               a1 &= a2 | a3 | a4;
               long na2;
               long na3;
               long na4;
               long b1;
               long b2;
               long b3;
               long b4;
               long nb2;
               long nb3;
               long nb4;
               this.extra[2][i] = (b1 = otherInits.extra[2][i])
                  | a1
                     & (
                        a3 & a4 & (nb2 = ~(b2 = otherInits.extra[3][i])) & (nb4 = ~(b4 = otherInits.extra[5][i]))
                           | ((na4 = ~a4) | (na3 = ~a3)) & ((na2 = ~a2) & nb2 | a2 & (nb3 = ~(b3 = otherInits.extra[4][i])) & nb4)
                     );
               long na1;
               long nb1;
               this.extra[3][i] = b2 & (nb4 | nb3) | na3 & na4 & b2 | a2 & (nb3 & nb4 | (nb1 = ~b1) & (na3 | (na1 = ~a1)) | a1 & b2);
               this.extra[4][i] = b3 & (nb1 & (b2 | a2 | na1) | b1 & (b4 | nb2 | a1 & a3) | na1 & na2 & na4)
                  | a3 & nb2 & nb4
                  | nb1 & ((na2 & a4 | na1) & a3 | a1 & na2 & na4 & b2);
               this.extra[5][i] = nb1 & (a4 & (na3 & nb3 | (a3 | na2) & nb2) | a1 & (a3 & nb2 & b4 | a2 & b2 & (b4 | a3 & na4 & nb3)))
                  | b1 & (a3 & a4 & b4 | na2 & na4 & nb3 & b4 | a2 & ((b3 | a4) & b4 | na3 & a4 & b2 & b3) | na1 & (b4 | (a4 | a2) & b2 & b3))
                  | (na1 & (na3 & nb3 | na2 & nb2) | a1 & (nb2 & nb3 | a2 & a3)) & b4;
               this.extra[6][i] &= otherInits.extra[6][i];
               this.extra[7][i] &= otherInits.extra[7][i];
            }

            while(i < copyLimit) {
               for(int j = 2; j < 8; ++j) {
                  this.extra[j][i] = otherInits.extra[j][i];
               }

               ++i;
            }
         }

         return this;
      }
   }

   @Override
   public FlowInfo addPotentialInitializationsFrom(FlowInfo inits) {
      if (this == DEAD_END) {
         return this;
      } else if (inits == DEAD_END) {
         return this;
      } else {
         UnconditionalFlowInfo otherInits = inits.unconditionalInits();
         this.potentialInits |= otherInits.potentialInits;
         if (this.extra != null) {
            if (otherInits.extra != null) {
               int i = 0;
               int length;
               int otherLength;
               if ((length = this.extra[0].length) < (otherLength = otherInits.extra[0].length)) {
                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[otherLength], 0, length);
                  }

                  while(i < length) {
                     this.extra[1][i] |= otherInits.extra[1][i];
                     ++i;
                  }

                  while(i < otherLength) {
                     this.extra[1][i] = otherInits.extra[1][i];
                     ++i;
                  }
               } else {
                  while(i < otherLength) {
                     this.extra[1][i] |= otherInits.extra[1][i];
                     ++i;
                  }
               }
            }
         } else if (otherInits.extra != null) {
            int otherLength = otherInits.extra[0].length;
            this.createExtraSpace(otherLength);
            System.arraycopy(otherInits.extra[1], 0, this.extra[1], 0, otherLength);
         }

         this.addPotentialNullInfoFrom(otherInits);
         return this;
      }
   }

   public UnconditionalFlowInfo addPotentialNullInfoFrom(UnconditionalFlowInfo otherInits) {
      if ((this.tagBits & 3) == 0 && (otherInits.tagBits & 3) == 0 && (otherInits.tagBits & 4) != 0) {
         boolean thisHadNulls = (this.tagBits & 4) != 0;
         boolean thisHasNulls = false;
         if (thisHadNulls) {
            long a1 = this.nullBit1;
            long a3 = this.nullBit3;
            long a4 = this.nullBit4;
            long var10002 = this.nullBit3 & this.nullBit4;
            long b2 = otherInits.nullBit2;
            long nb2;
            long var10003 = nb2 = ~otherInits.nullBit2;
            long b4 = otherInits.nullBit4;
            long nb4;
            var10003 &= nb4 = ~otherInits.nullBit4;
            long b1 = otherInits.nullBit1;
            long b3 = otherInits.nullBit3;
            var10002 &= var10003 | otherInits.nullBit1 & otherInits.nullBit3;
            long a2 = this.nullBit2;
            long na2;
            long na3;
            long na4;
            long nb3;
            this.nullBit1 &= var10002
               | (na2 = ~this.nullBit2) & (b1 & b3 | ((na4 = ~a4) | (na3 = ~a3)) & nb2)
               | a2 & (na4 | na3) & ((nb3 = ~b3) & nb4 | b1 & b2);
            long na1;
            long nb1;
            this.nullBit2 = b2 & (nb3 | (nb1 = ~b1)) | a2 & (nb3 & nb4 | b2 | na3 | (na1 = ~a1));
            this.nullBit3 = b3 & (nb1 & b2 | a2 & (nb2 | a3) | na1 & nb2 | a1 & na2 & na4 & b1) | a3 & (nb2 & nb4 | na2 & a4 | na1) | a1 & na2 & na4 & b2;
            this.nullBit4 = na3 & (nb1 & nb3 & b4 | a4 & (nb3 | b1 & b2))
               | nb2 & (na3 & b1 & nb3 | na2 & (nb1 & b4 | b1 & nb3 | a4))
               | a3 & (a4 & (nb2 | b1 & b3) | a1 & a2 & (nb1 & b4 | na4 & (b2 | b1) & nb3));
            if ((this.nullBit2 | this.nullBit3 | this.nullBit4) != 0L) {
               thisHasNulls = true;
            }
         } else {
            this.nullBit1 = 0L;
            long b2 = otherInits.nullBit2;
            long b3 = otherInits.nullBit3;
            long nb3;
            long var70 = nb3 = ~otherInits.nullBit3;
            long b1 = otherInits.nullBit1;
            long nb1;
            this.nullBit2 = otherInits.nullBit2 & (var70 | (nb1 = ~otherInits.nullBit1));
            long nb2;
            this.nullBit3 = b3 & (nb1 | (nb2 = ~b2));
            long var10001 = ~b1 & ~b3;
            long b4 = otherInits.nullBit4;
            this.nullBit4 = var10001 & otherInits.nullBit4 | ~b2 & (b1 & ~b3 | ~b1 & b4);
            if ((this.nullBit2 | this.nullBit3 | this.nullBit4) != 0L) {
               thisHasNulls = true;
            }
         }

         if (otherInits.extra != null) {
            int mergeLimit = 0;
            int copyLimit = otherInits.extra[0].length;
            if (this.extra == null) {
               this.createExtraSpace(copyLimit);
            } else {
               mergeLimit = copyLimit;
               if (copyLimit > this.extra[0].length) {
                  mergeLimit = this.extra[0].length;

                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[copyLimit], 0, mergeLimit);
                  }

                  if (!thisHadNulls) {
                     mergeLimit = 0;
                  }
               }
            }

            int i;
            for(i = 0; i < mergeLimit; ++i) {
               long a1;
               long a2;
               long a3;
               long a4;
               long na2;
               long na3;
               long na4;
               long var48;
               long var51;
               long var54;
               long var57;
               long var63;
               long var66;
               long nb4;
               this.extra[2][i] = (a1 = this.extra[2][i])
                  & (
                     (a3 = this.extra[4][i])
                           & (a4 = this.extra[5][i])
                           & (
                              (var63 = ~(var51 = otherInits.extra[3][i])) & (nb4 = ~(var57 = otherInits.extra[5][i]))
                                 | (var48 = otherInits.extra[2][i]) & (var54 = otherInits.extra[4][i])
                           )
                        | (na2 = ~(a2 = this.extra[3][i])) & (var48 & var54 | ((na4 = ~a4) | (na3 = ~a3)) & var63)
                        | a2 & (na4 | na3) & ((var66 = ~var54) & nb4 | var48 & var51)
                  );
               long na1;
               long var60;
               this.extra[3][i] = var51 & (var66 | (var60 = ~var48)) | a2 & (var66 & nb4 | var51 | na3 | (na1 = ~a1));
               this.extra[4][i] = var54 & (var60 & var51 | a2 & (var63 | a3) | na1 & var63 | a1 & na2 & na4 & var48)
                  | a3 & (var63 & nb4 | na2 & a4 | na1)
                  | a1 & na2 & na4 & var51;
               this.extra[5][i] = na3 & (var60 & var66 & var57 | a4 & (var66 | var48 & var51))
                  | var63 & (na3 & var48 & var66 | na2 & (var60 & var57 | var48 & var66 | a4))
                  | a3 & (a4 & (var63 | var48 & var54) | a1 & a2 & (var60 & var57 | na4 & (var51 | var48) & var66));
               if ((this.extra[3][i] | this.extra[4][i] | this.extra[5][i]) != 0L) {
                  thisHasNulls = true;
               }
            }

            for(; i < copyLimit; ++i) {
               this.extra[2][i] = 0L;
               long var49;
               long var52;
               long var55;
               long var61;
               this.extra[3][i] = (var52 = otherInits.extra[3][i]) & (~(var55 = otherInits.extra[4][i]) | (var61 = ~(var49 = otherInits.extra[2][i])));
               long var64;
               this.extra[4][i] = var55 & (var61 | (var64 = ~var52));
               long var58;
               this.extra[5][i] = ~var49 & ~var55 & (var58 = otherInits.extra[5][i]) | ~var52 & (var49 & ~var55 | ~var49 & var58);
               if ((this.extra[3][i] | this.extra[4][i] | this.extra[5][i]) != 0L) {
                  thisHasNulls = true;
               }
            }
         }

         if (thisHasNulls) {
            this.tagBits |= 4;
         } else {
            this.tagBits &= 4;
         }

         return this;
      } else {
         return this;
      }
   }

   @Override
   public final boolean cannotBeDefinitelyNullOrNonNull(LocalVariableBinding local) {
      if ((this.tagBits & 4) == 0 || (local.type.tagBits & 2L) != 0L) {
         return false;
      } else {
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            return ((~this.nullBit1 & (this.nullBit2 & this.nullBit3 | this.nullBit4) | ~this.nullBit2 & ~this.nullBit3 & this.nullBit4) & 1L << position)
               != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[0].length) {
               return false;
            } else {
               long a2;
               long a3;
               long a4;
               return (
                     (
                           ~this.extra[2][vectorIndex]
                                 & ((a2 = this.extra[3][vectorIndex]) & (a3 = this.extra[4][vectorIndex]) | (a4 = this.extra[5][vectorIndex]))
                              | ~a2 & ~a3 & a4
                        )
                        & 1L << position % 64
                  )
                  != 0L;
            }
         }
      }
   }

   @Override
   public final boolean cannotBeNull(LocalVariableBinding local) {
      if ((this.tagBits & 4) == 0 || (local.type.tagBits & 2L) != 0L) {
         return false;
      } else {
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            return (this.nullBit1 & this.nullBit3 & (this.nullBit2 & this.nullBit4 | ~this.nullBit2) & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[0].length) {
               return false;
            } else {
               return (
                     this.extra[2][vectorIndex]
                        & this.extra[4][vectorIndex]
                        & (this.extra[3][vectorIndex] & this.extra[5][vectorIndex] | ~this.extra[3][vectorIndex])
                        & 1L << position % 64
                  )
                  != 0L;
            }
         }
      }
   }

   @Override
   public final boolean canOnlyBeNull(LocalVariableBinding local) {
      if ((this.tagBits & 4) == 0 || (local.type.tagBits & 2L) != 0L) {
         return false;
      } else {
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            return (this.nullBit1 & this.nullBit2 & (~this.nullBit3 | ~this.nullBit4) & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[0].length) {
               return false;
            } else {
               return (
                     this.extra[2][vectorIndex]
                        & this.extra[3][vectorIndex]
                        & (~this.extra[4][vectorIndex] | ~this.extra[5][vectorIndex])
                        & 1L << position % 64
                  )
                  != 0L;
            }
         }
      }
   }

   @Override
   public FlowInfo copy() {
      if (this == DEAD_END) {
         return this;
      } else {
         UnconditionalFlowInfo copy = new UnconditionalFlowInfo();
         copy.definiteInits = this.definiteInits;
         copy.potentialInits = this.potentialInits;
         boolean hasNullInfo = (this.tagBits & 4) != 0;
         if (hasNullInfo) {
            copy.nullBit1 = this.nullBit1;
            copy.nullBit2 = this.nullBit2;
            copy.nullBit3 = this.nullBit3;
            copy.nullBit4 = this.nullBit4;
         }

         copy.iNBit = this.iNBit;
         copy.iNNBit = this.iNNBit;
         copy.tagBits = this.tagBits;
         copy.maxFieldCount = this.maxFieldCount;
         if (this.extra != null) {
            copy.extra = new long[8][];
            int length;
            System.arraycopy(this.extra[0], 0, copy.extra[0] = new long[length = this.extra[0].length], 0, length);
            System.arraycopy(this.extra[1], 0, copy.extra[1] = new long[length], 0, length);
            if (hasNullInfo) {
               for(int j = 2; j < 8; ++j) {
                  System.arraycopy(this.extra[j], 0, copy.extra[j] = new long[length], 0, length);
               }
            } else {
               for(int j = 2; j < 8; ++j) {
                  copy.extra[j] = new long[length];
               }
            }
         }

         return copy;
      }
   }

   public UnconditionalFlowInfo discardInitializationInfo() {
      if (this == DEAD_END) {
         return this;
      } else {
         this.definiteInits = this.potentialInits = 0L;
         if (this.extra != null) {
            int i = 0;

            for(int length = this.extra[0].length; i < length; ++i) {
               this.extra[0][i] = this.extra[1][i] = 0L;
            }
         }

         return this;
      }
   }

   public UnconditionalFlowInfo discardNonFieldInitializations() {
      int limit = this.maxFieldCount;
      if (limit < 64) {
         long mask = (1L << limit) - 1L;
         this.definiteInits &= mask;
         this.potentialInits &= mask;
         this.nullBit1 &= mask;
         this.nullBit2 &= mask;
         this.nullBit3 &= mask;
         this.nullBit4 &= mask;
         this.iNBit &= mask;
         this.iNNBit &= mask;
      }

      if (this.extra == null) {
         return this;
      } else {
         int length = this.extra[0].length;
         int vectorIndex;
         if ((vectorIndex = limit / 64 - 1) >= length) {
            return this;
         } else {
            if (vectorIndex >= 0) {
               long mask = (1L << limit % 64) - 1L;

               for(int j = 0; j < 8; ++j) {
                  this.extra[j][vectorIndex] &= mask;
               }
            }

            for(int i = vectorIndex + 1; i < length; ++i) {
               for(int j = 0; j < 8; ++j) {
                  this.extra[j][i] = 0L;
               }
            }

            return this;
         }
      }
   }

   @Override
   public FlowInfo initsWhenFalse() {
      return this;
   }

   @Override
   public FlowInfo initsWhenTrue() {
      return this;
   }

   private final boolean isDefinitelyAssigned(int position) {
      if (position < 64) {
         return (this.definiteInits & 1L << position) != 0L;
      } else if (this.extra == null) {
         return false;
      } else {
         int vectorIndex;
         if ((vectorIndex = position / 64 - 1) >= this.extra[0].length) {
            return false;
         } else {
            return (this.extra[0][vectorIndex] & 1L << position % 64) != 0L;
         }
      }
   }

   @Override
   public final boolean isDefinitelyAssigned(FieldBinding field) {
      return (this.tagBits & 1) != 0 ? true : this.isDefinitelyAssigned(field.id);
   }

   @Override
   public final boolean isDefinitelyAssigned(LocalVariableBinding local) {
      return (this.tagBits & 1) != 0 && (local.declaration.bits & 1073741824) != 0 ? true : this.isDefinitelyAssigned(local.id + this.maxFieldCount);
   }

   @Override
   public final boolean isDefinitelyNonNull(LocalVariableBinding local) {
      if ((this.tagBits & 3) == 0 && (this.tagBits & 4) != 0) {
         if ((local.type.tagBits & 2L) == 0L && local.constant() == Constant.NotAConstant) {
            int position = local.id + this.maxFieldCount;
            if (position < 64) {
               return (this.nullBit1 & this.nullBit3 & (~this.nullBit2 | this.nullBit4) & 1L << position) != 0L;
            } else if (this.extra == null) {
               return false;
            } else {
               int vectorIndex;
               if ((vectorIndex = position / 64 - 1) >= this.extra[2].length) {
                  return false;
               } else {
                  return (
                        this.extra[2][vectorIndex]
                           & this.extra[4][vectorIndex]
                           & (~this.extra[3][vectorIndex] | this.extra[5][vectorIndex])
                           & 1L << position % 64
                     )
                     != 0L;
               }
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public final boolean isDefinitelyNull(LocalVariableBinding local) {
      if ((this.tagBits & 3) == 0 && (this.tagBits & 4) != 0 && (local.type.tagBits & 2L) == 0L) {
         int position = local.id + this.maxFieldCount;
         if (position < 64) {
            return (this.nullBit1 & this.nullBit2 & (~this.nullBit3 | ~this.nullBit4) & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[2].length) {
               return false;
            } else {
               return (
                     this.extra[2][vectorIndex]
                        & this.extra[3][vectorIndex]
                        & (~this.extra[4][vectorIndex] | ~this.extra[5][vectorIndex])
                        & 1L << position % 64
                  )
                  != 0L;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public final boolean isDefinitelyUnknown(LocalVariableBinding local) {
      if ((this.tagBits & 3) == 0 && (this.tagBits & 4) != 0) {
         int position = local.id + this.maxFieldCount;
         if (position < 64) {
            return (this.nullBit1 & this.nullBit4 & ~this.nullBit2 & ~this.nullBit3 & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[2].length) {
               return false;
            } else {
               return (
                     this.extra[2][vectorIndex] & this.extra[5][vectorIndex] & ~this.extra[3][vectorIndex] & ~this.extra[4][vectorIndex] & 1L << position % 64
                  )
                  != 0L;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public final boolean hasNullInfoFor(LocalVariableBinding local) {
      if ((this.tagBits & 3) == 0 && (this.tagBits & 4) != 0) {
         int position = local.id + this.maxFieldCount;
         if (position < 64) {
            return ((this.nullBit1 | this.nullBit2 | this.nullBit3 | this.nullBit4) & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[2].length) {
               return false;
            } else {
               return (
                     (this.extra[2][vectorIndex] | this.extra[3][vectorIndex] | this.extra[4][vectorIndex] | this.extra[5][vectorIndex]) & 1L << position % 64
                  )
                  != 0L;
            }
         }
      } else {
         return false;
      }
   }

   private final boolean isPotentiallyAssigned(int position) {
      if (position < 64) {
         return (this.potentialInits & 1L << position) != 0L;
      } else if (this.extra == null) {
         return false;
      } else {
         int vectorIndex;
         if ((vectorIndex = position / 64 - 1) >= this.extra[0].length) {
            return false;
         } else {
            return (this.extra[1][vectorIndex] & 1L << position % 64) != 0L;
         }
      }
   }

   @Override
   public final boolean isPotentiallyAssigned(FieldBinding field) {
      return this.isPotentiallyAssigned(field.id);
   }

   @Override
   public final boolean isPotentiallyAssigned(LocalVariableBinding local) {
      return local.constant() != Constant.NotAConstant ? true : this.isPotentiallyAssigned(local.id + this.maxFieldCount);
   }

   @Override
   public final boolean isPotentiallyNonNull(LocalVariableBinding local) {
      if ((this.tagBits & 4) == 0 || (local.type.tagBits & 2L) != 0L) {
         return false;
      } else {
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            return (this.nullBit3 & (~this.nullBit1 | ~this.nullBit2) & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[2].length) {
               return false;
            } else {
               return (this.extra[4][vectorIndex] & (~this.extra[2][vectorIndex] | ~this.extra[3][vectorIndex]) & 1L << position % 64) != 0L;
            }
         }
      }
   }

   @Override
   public final boolean isPotentiallyNull(LocalVariableBinding local) {
      if ((this.tagBits & 4) == 0 || (local.type.tagBits & 2L) != 0L) {
         return false;
      } else {
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            return (this.nullBit2 & (~this.nullBit1 | ~this.nullBit3) & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[2].length) {
               return false;
            } else {
               return (this.extra[3][vectorIndex] & (~this.extra[2][vectorIndex] | ~this.extra[4][vectorIndex]) & 1L << position % 64) != 0L;
            }
         }
      }
   }

   @Override
   public final boolean isPotentiallyUnknown(LocalVariableBinding local) {
      if ((this.tagBits & 3) == 0 && (this.tagBits & 4) != 0) {
         int position = local.id + this.maxFieldCount;
         if (position < 64) {
            return (this.nullBit4 & (~this.nullBit1 | ~this.nullBit2 & ~this.nullBit3) & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[2].length) {
               return false;
            } else {
               return (
                     this.extra[5][vectorIndex]
                        & (~this.extra[2][vectorIndex] | ~this.extra[3][vectorIndex] & ~this.extra[4][vectorIndex])
                        & 1L << position % 64
                  )
                  != 0L;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public final boolean isProtectedNonNull(LocalVariableBinding local) {
      if ((this.tagBits & 4) == 0 || (local.type.tagBits & 2L) != 0L) {
         return false;
      } else {
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            return (this.nullBit1 & this.nullBit3 & this.nullBit4 & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[0].length) {
               return false;
            } else {
               return (this.extra[2][vectorIndex] & this.extra[4][vectorIndex] & this.extra[5][vectorIndex] & 1L << position % 64) != 0L;
            }
         }
      }
   }

   @Override
   public final boolean isProtectedNull(LocalVariableBinding local) {
      if ((this.tagBits & 4) == 0 || (local.type.tagBits & 2L) != 0L) {
         return false;
      } else {
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            return (this.nullBit1 & this.nullBit2 & (this.nullBit3 ^ this.nullBit4) & 1L << position) != 0L;
         } else if (this.extra == null) {
            return false;
         } else {
            int vectorIndex;
            if ((vectorIndex = position / 64 - 1) >= this.extra[0].length) {
               return false;
            } else {
               return (
                     this.extra[2][vectorIndex] & this.extra[3][vectorIndex] & (this.extra[4][vectorIndex] ^ this.extra[5][vectorIndex]) & 1L << position % 64
                  )
                  != 0L;
            }
         }
      }
   }

   protected static boolean isTrue(boolean expression, String message) {
      if (!expression) {
         throw new UnconditionalFlowInfo.AssertionFailedException("assertion failed: " + message);
      } else {
         return expression;
      }
   }

   @Override
   public void markAsComparedEqualToNonNull(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.tagBits |= 4;
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            long mask;
            long var10000 = mask = 1L << position;
            long a1 = this.nullBit1;
            var10000 &= this.nullBit1;
            long a2 = this.nullBit2;
            long na2;
            var10000 &= na2 = ~this.nullBit2;
            long a3 = this.nullBit3;
            var10000 &= ~this.nullBit3;
            long a4 = this.nullBit4;
            if ((var10000 & this.nullBit4) != 0L) {
               this.nullBit4 &= ~mask;
            } else if ((mask & a1 & na2 & a3) == 0L) {
               this.nullBit4 |= mask;
               if ((mask & a1) == 0L) {
                  if ((mask & a2 & (a3 ^ a4)) != 0L) {
                     this.nullBit2 &= ~mask;
                  } else if ((mask & (a2 | a3 | a4)) == 0L) {
                     this.nullBit2 |= mask;
                  }
               }
            }

            this.nullBit1 |= mask;
            this.nullBit3 |= mask;
            this.iNBit &= ~mask;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null) {
               int length = vectorIndex + 1;
               this.createExtraSpace(length);
            } else {
               int oldLength;
               if (vectorIndex >= (oldLength = this.extra[0].length)) {
                  int newLength = vectorIndex + 1;

                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[newLength], 0, oldLength);
                  }
               }
            }

            long mask;
            long a1;
            long a2;
            long a3;
            long a4;
            long na2;
            if ((
                  (mask = 1L << position % 64)
                     & (a1 = this.extra[2][vectorIndex])
                     & (na2 = ~(a2 = this.extra[3][vectorIndex]))
                     & ~(a3 = this.extra[4][vectorIndex])
                     & (a4 = this.extra[5][vectorIndex])
               )
               != 0L) {
               this.extra[5][vectorIndex] &= ~mask;
            } else if ((mask & a1 & na2 & a3) == 0L) {
               this.extra[5][vectorIndex] |= mask;
               if ((mask & a1) == 0L) {
                  if ((mask & a2 & (a3 ^ a4)) != 0L) {
                     this.extra[3][vectorIndex] &= ~mask;
                  } else if ((mask & (a2 | a3 | a4)) == 0L) {
                     this.extra[3][vectorIndex] |= mask;
                  }
               }
            }

            this.extra[2][vectorIndex] |= mask;
            this.extra[4][vectorIndex] |= mask;
            this.extra[6][vectorIndex] &= ~mask;
         }
      }
   }

   @Override
   public void markAsComparedEqualToNull(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.tagBits |= 4;
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            long mask;
            if (((mask = 1L << position) & this.nullBit1) != 0L) {
               if ((mask & (~this.nullBit2 | this.nullBit3 | ~this.nullBit4)) != 0L) {
                  this.nullBit4 &= ~mask;
               }
            } else if ((mask & this.nullBit4) != 0L) {
               this.nullBit3 &= ~mask;
            } else if ((mask & this.nullBit2) != 0L) {
               this.nullBit3 &= ~mask;
               this.nullBit4 |= mask;
            } else {
               this.nullBit3 |= mask;
            }

            this.nullBit1 |= mask;
            this.nullBit2 |= mask;
            this.iNNBit &= ~mask;
         } else {
            int vectorIndex = position / 64 - 1;
            long mask = 1L << position % 64;
            if (this.extra == null) {
               int length = vectorIndex + 1;
               this.createExtraSpace(length);
            } else {
               int oldLength;
               if (vectorIndex >= (oldLength = this.extra[0].length)) {
                  int newLength = vectorIndex + 1;

                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[newLength], 0, oldLength);
                  }
               }
            }

            if ((mask & this.extra[2][vectorIndex]) != 0L) {
               if ((mask & (~this.extra[3][vectorIndex] | this.extra[4][vectorIndex] | ~this.extra[5][vectorIndex])) != 0L) {
                  this.extra[5][vectorIndex] &= ~mask;
               }
            } else if ((mask & this.extra[5][vectorIndex]) != 0L) {
               this.extra[4][vectorIndex] &= ~mask;
            } else if ((mask & this.extra[3][vectorIndex]) != 0L) {
               this.extra[4][vectorIndex] &= ~mask;
               this.extra[5][vectorIndex] |= mask;
            } else {
               this.extra[4][vectorIndex] |= mask;
            }

            this.extra[2][vectorIndex] |= mask;
            this.extra[3][vectorIndex] |= mask;
            this.extra[7][vectorIndex] &= ~mask;
         }
      }
   }

   private final void markAsDefinitelyAssigned(int position) {
      if (this != DEAD_END) {
         if (position < 64) {
            long mask;
            this.definiteInits |= mask = 1L << position;
            this.potentialInits |= mask;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null) {
               int length = vectorIndex + 1;
               this.createExtraSpace(length);
            } else {
               int oldLength;
               if (vectorIndex >= (oldLength = this.extra[0].length)) {
                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[vectorIndex + 1], 0, oldLength);
                  }
               }
            }

            long mask;
            this.extra[0][vectorIndex] |= mask = 1L << position % 64;
            this.extra[1][vectorIndex] |= mask;
         }
      }
   }

   @Override
   public void markAsDefinitelyAssigned(FieldBinding field) {
      if (this != DEAD_END) {
         this.markAsDefinitelyAssigned(field.id);
      }
   }

   @Override
   public void markAsDefinitelyAssigned(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.markAsDefinitelyAssigned(local.id + this.maxFieldCount);
      }
   }

   @Override
   public void markAsDefinitelyNonNull(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.tagBits |= 4;
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            long mask;
            this.nullBit1 |= mask = 1L << position;
            this.nullBit3 |= mask;
            long var8;
            this.nullBit2 &= var8 = ~mask;
            this.nullBit4 &= var8;
            this.iNBit &= var8;
            this.iNNBit &= var8;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null) {
               int length = vectorIndex + 1;
               this.createExtraSpace(length);
            } else {
               int oldLength;
               if (vectorIndex >= (oldLength = this.extra[0].length)) {
                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[vectorIndex + 1], 0, oldLength);
                  }
               }
            }

            long mask;
            this.extra[2][vectorIndex] |= mask = 1L << position % 64;
            this.extra[4][vectorIndex] |= mask;
            long var10;
            this.extra[3][vectorIndex] &= var10 = ~mask;
            this.extra[5][vectorIndex] &= var10;
            this.extra[6][vectorIndex] &= var10;
            this.extra[7][vectorIndex] &= var10;
         }
      }
   }

   @Override
   public void markAsDefinitelyNull(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.tagBits |= 4;
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            long mask;
            this.nullBit1 |= mask = 1L << position;
            this.nullBit2 |= mask;
            long var8;
            this.nullBit3 &= var8 = ~mask;
            this.nullBit4 &= var8;
            this.iNBit &= var8;
            this.iNNBit &= var8;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null) {
               int length = vectorIndex + 1;
               this.createExtraSpace(length);
            } else {
               int oldLength;
               if (vectorIndex >= (oldLength = this.extra[0].length)) {
                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[vectorIndex + 1], 0, oldLength);
                  }
               }
            }

            long mask;
            this.extra[2][vectorIndex] |= mask = 1L << position % 64;
            this.extra[3][vectorIndex] |= mask;
            long var10;
            this.extra[4][vectorIndex] &= var10 = ~mask;
            this.extra[5][vectorIndex] &= var10;
            this.extra[6][vectorIndex] &= var10;
            this.extra[7][vectorIndex] &= var10;
         }
      }
   }

   @Override
   public void markAsDefinitelyUnknown(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.tagBits |= 4;
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            long mask;
            this.nullBit1 |= mask = 1L << position;
            this.nullBit4 |= mask;
            long var8;
            this.nullBit2 &= var8 = ~mask;
            this.nullBit3 &= var8;
            this.iNBit &= var8;
            this.iNNBit &= var8;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null) {
               int length = vectorIndex + 1;
               this.createExtraSpace(length);
            } else {
               int oldLength;
               if (vectorIndex >= (oldLength = this.extra[0].length)) {
                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[vectorIndex + 1], 0, oldLength);
                  }
               }
            }

            long mask;
            this.extra[2][vectorIndex] |= mask = 1L << position % 64;
            this.extra[5][vectorIndex] |= mask;
            long var10;
            this.extra[3][vectorIndex] &= var10 = ~mask;
            this.extra[4][vectorIndex] &= var10;
            this.extra[6][vectorIndex] &= var10;
            this.extra[7][vectorIndex] &= var10;
         }
      }
   }

   @Override
   public void resetNullInfo(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.tagBits |= 4;
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            long mask;
            this.nullBit1 &= mask = ~(1L << position);
            this.nullBit2 &= mask;
            this.nullBit3 &= mask;
            this.nullBit4 &= mask;
            this.iNBit &= mask;
            this.iNNBit &= mask;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null || vectorIndex >= this.extra[2].length) {
               return;
            }

            long mask;
            this.extra[2][vectorIndex] &= mask = ~(1L << position % 64);
            this.extra[3][vectorIndex] &= mask;
            this.extra[4][vectorIndex] &= mask;
            this.extra[5][vectorIndex] &= mask;
            this.extra[6][vectorIndex] &= mask;
            this.extra[7][vectorIndex] &= mask;
         }
      }
   }

   @Override
   public void markPotentiallyUnknownBit(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.tagBits |= 4;
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            long mask = 1L << position;
            isTrue((this.nullBit1 & mask) == 0L, "Adding 'unknown' mark in unexpected state");
            this.nullBit4 |= mask;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null) {
               int length = vectorIndex + 1;
               this.createExtraSpace(length);
            } else {
               int oldLength;
               if (vectorIndex >= (oldLength = this.extra[0].length)) {
                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[vectorIndex + 1], 0, oldLength);
                  }
               }
            }

            long mask = 1L << position % 64;
            isTrue((this.extra[2][vectorIndex] & mask) == 0L, "Adding 'unknown' mark in unexpected state");
            this.extra[5][vectorIndex] |= mask;
         }
      }
   }

   @Override
   public void markPotentiallyNullBit(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.tagBits |= 4;
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            long mask = 1L << position;
            isTrue((this.nullBit1 & mask) == 0L, "Adding 'potentially null' mark in unexpected state");
            this.nullBit2 |= mask;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null) {
               int length = vectorIndex + 1;
               this.createExtraSpace(length);
            } else {
               int oldLength;
               if (vectorIndex >= (oldLength = this.extra[0].length)) {
                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[vectorIndex + 1], 0, oldLength);
                  }
               }
            }

            long mask = 1L << position % 64;
            this.extra[3][vectorIndex] |= mask;
            isTrue((this.extra[2][vectorIndex] & mask) == 0L, "Adding 'potentially null' mark in unexpected state");
         }
      }
   }

   @Override
   public void markPotentiallyNonNullBit(LocalVariableBinding local) {
      if (this != DEAD_END) {
         this.tagBits |= 4;
         int position;
         if ((position = local.id + this.maxFieldCount) < 64) {
            long mask = 1L << position;
            isTrue((this.nullBit1 & mask) == 0L, "Adding 'potentially non-null' mark in unexpected state");
            this.nullBit3 |= mask;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null) {
               int length = vectorIndex + 1;
               this.createExtraSpace(length);
            } else {
               int oldLength;
               if (vectorIndex >= (oldLength = this.extra[0].length)) {
                  for(int j = 0; j < 8; ++j) {
                     System.arraycopy(this.extra[j], 0, this.extra[j] = new long[vectorIndex + 1], 0, oldLength);
                  }
               }
            }

            long mask = 1L << position % 64;
            isTrue((this.extra[2][vectorIndex] & mask) == 0L, "Adding 'potentially non-null' mark in unexpected state");
            this.extra[4][vectorIndex] |= mask;
         }
      }
   }

   @Override
   public UnconditionalFlowInfo mergedWith(UnconditionalFlowInfo otherInits) {
      if ((otherInits.tagBits & 1) != 0 && this != DEAD_END) {
         return this;
      } else if ((this.tagBits & 1) != 0) {
         return (UnconditionalFlowInfo)otherInits.copy();
      } else {
         this.definiteInits &= otherInits.definiteInits;
         this.potentialInits |= otherInits.potentialInits;
         boolean thisHasNulls = (this.tagBits & 4) != 0;
         boolean otherHasNulls = (otherInits.tagBits & 4) != 0;
         boolean thisHadNulls = thisHasNulls;
         if ((otherInits.tagBits & 2) != 0) {
            otherHasNulls = false;
         } else if ((this.tagBits & 2) != 0) {
            this.nullBit1 = otherInits.nullBit1;
            this.nullBit2 = otherInits.nullBit2;
            this.nullBit3 = otherInits.nullBit3;
            this.nullBit4 = otherInits.nullBit4;
            this.iNBit = otherInits.iNBit;
            this.iNNBit = otherInits.iNNBit;
            thisHadNulls = false;
            thisHasNulls = otherHasNulls;
            this.tagBits = otherInits.tagBits;
         } else if (thisHasNulls) {
            if (otherHasNulls) {
               long a1 = this.nullBit1;
               long b1 = otherInits.nullBit1;
               long var10001 = this.nullBit1 & otherInits.nullBit1;
               long a2 = this.nullBit2;
               long b2 = otherInits.nullBit2;
               long a3 = this.nullBit3;
               long a4 = this.nullBit4;
               long var10004 = this.nullBit3 & this.nullBit4;
               long b3 = otherInits.nullBit3;
               long b4 = otherInits.nullBit4;
               long na2;
               long na3;
               long nb2;
               this.nullBit1 = var10001
                  & (
                     this.nullBit2 & (otherInits.nullBit2 & ~(var10004 ^ otherInits.nullBit3 & otherInits.nullBit4) | a3 & a4 & (nb2 = ~b2))
                        | (na2 = ~a2) & (b2 & b3 & b4 | nb2 & ((na3 = ~a3) ^ b3))
                  );
               long na1;
               long na4;
               long nb1;
               long nb3;
               long nb4;
               this.nullBit2 = b2 & ((nb3 = ~b3) | (nb1 = ~b1) | a3 & (a4 | (na1 = ~a1)) & (nb4 = ~b4))
                  | a2 & (b2 | (na4 = ~a4) & b3 & (b4 | nb1) | na3 | na1);
               this.nullBit3 = a3 & (na1 | a1 & na2 | b3 & (na4 ^ b4)) | b3 & (nb1 | b1 & nb2);
               this.nullBit4 = na3 & (nb1 & nb3 & b4 | b1 & (nb2 & nb3 | a4 & b2 & nb4) | na1 & a4 & (nb3 | b1 & b2))
                  | a3 & a4 & (b3 & b4 | b1 & nb2 | na1 & a2)
                  | na2 & (nb1 & b4 | b1 & nb3 | na1 & a4) & nb2
                  | a1 & (na3 & (nb3 & b4 | b1 & b2 & b3 & nb4 | na2 & (nb3 | nb2)) | na2 & b3 & b4 | a2 & (nb1 & b4 | a3 & na4 & b1) & nb3)
                  | nb1 & b2 & b3 & b4;
            } else {
               long a1 = this.nullBit1;
               this.nullBit1 = 0L;
               long a2 = this.nullBit2;
               long a3 = this.nullBit3;
               long na1;
               long na3;
               this.nullBit2 &= na3 = ~this.nullBit3 | (na1 = ~a1);
               long na2;
               long var10002 = na2 = ~a2;
               long a4 = this.nullBit4;
               this.nullBit3 = a3 & (var10002 & this.nullBit4 | na1) | a1 & na2 & ~a4;
               this.nullBit4 = (na3 | na2) & na1 & a4 | a1 & na3 & na2;
            }

            this.iNBit |= otherInits.iNBit;
            this.iNNBit |= otherInits.iNNBit;
         } else if (otherHasNulls) {
            this.nullBit1 = 0L;
            long b2 = otherInits.nullBit2;
            long b3 = otherInits.nullBit3;
            long var91 = ~otherInits.nullBit3;
            long b1 = otherInits.nullBit1;
            long nb1;
            long nb3;
            this.nullBit2 = otherInits.nullBit2 & (nb3 = var91 | (nb1 = ~otherInits.nullBit1));
            long nb2;
            var91 = nb2 = ~b2;
            long b4 = otherInits.nullBit4;
            this.nullBit3 = b3 & (var91 & otherInits.nullBit4 | nb1) | b1 & nb2 & ~b4;
            this.nullBit4 = (nb3 | nb2) & nb1 & b4 | b1 & nb3 & nb2;
            this.iNBit |= otherInits.iNBit;
            this.iNNBit |= otherInits.iNNBit;
            thisHasNulls = this.nullBit2 != 0L || this.nullBit3 != 0L || this.nullBit4 != 0L;
         }

         if (this.extra != null || otherInits.extra != null) {
            int mergeLimit = 0;
            int copyLimit = 0;
            int resetLimit = 0;
            if (this.extra != null) {
               if (otherInits.extra == null) {
                  resetLimit = this.extra[0].length;
               } else {
                  int length;
                  int otherLength;
                  if ((length = this.extra[0].length) >= (otherLength = otherInits.extra[0].length)) {
                     mergeLimit = otherLength;
                     resetLimit = length;
                  } else {
                     for(int j = 0; j < 8; ++j) {
                        System.arraycopy(this.extra[j], 0, this.extra[j] = new long[otherLength], 0, length);
                     }

                     mergeLimit = length;
                     copyLimit = otherLength;
                  }
               }
            } else if (otherInits.extra != null) {
               int otherLength = otherInits.extra[0].length;
               this.extra = new long[8][];

               for(int j = 0; j < 8; ++j) {
                  this.extra[j] = new long[otherLength];
               }

               System.arraycopy(otherInits.extra[1], 0, this.extra[1], 0, otherLength);
               System.arraycopy(otherInits.extra[6], 0, this.extra[6], 0, otherLength);
               System.arraycopy(otherInits.extra[7], 0, this.extra[7], 0, otherLength);
               copyLimit = otherLength;
            }

            int i;
            for(i = 0; i < mergeLimit; ++i) {
               this.extra[0][i] &= otherInits.extra[0][i];
               this.extra[1][i] |= otherInits.extra[1][i];
            }

            while(i < copyLimit) {
               this.extra[1][i] = otherInits.extra[1][i];
               ++i;
            }

            while(i < resetLimit) {
               this.extra[0][i] = 0L;
               ++i;
            }

            if (!otherHasNulls) {
               if (resetLimit < mergeLimit) {
                  resetLimit = mergeLimit;
               }

               copyLimit = 0;
               mergeLimit = 0;
            }

            if (!thisHadNulls) {
               resetLimit = 0;
            }

            for(i = 0; i < mergeLimit; ++i) {
               long a1;
               long a2;
               long a3;
               long a4;
               long na2;
               long na3;
               long nb2;
               long b1;
               long b2;
               long b3;
               long b4;
               this.extra[2][i] = (a1 = this.extra[2][i])
                  & (b1 = otherInits.extra[2][i])
                  & (
                     (a2 = this.extra[3][i])
                           & (
                              (b2 = otherInits.extra[3][i])
                                    & ~((a3 = this.extra[4][i]) & (a4 = this.extra[5][i]) ^ (b3 = otherInits.extra[4][i]) & (b4 = otherInits.extra[5][i]))
                                 | a3 & a4 & (nb2 = ~b2)
                           )
                        | (na2 = ~a2) & (b2 & b3 & b4 | nb2 & ((na3 = ~a3) ^ b3))
                  );
               long na1;
               long na4;
               long nb1;
               long nb3;
               long nb4;
               this.extra[3][i] = b2 & ((nb3 = ~b3) | (nb1 = ~b1) | a3 & (a4 | (na1 = ~a1)) & (nb4 = ~b4))
                  | a2 & (b2 | (na4 = ~a4) & b3 & (b4 | nb1) | na3 | na1);
               this.extra[4][i] = a3 & (na1 | a1 & na2 | b3 & (na4 ^ b4)) | b3 & (nb1 | b1 & nb2);
               this.extra[5][i] = na3 & (nb1 & nb3 & b4 | b1 & (nb2 & nb3 | a4 & b2 & nb4) | na1 & a4 & (nb3 | b1 & b2))
                  | a3 & a4 & (b3 & b4 | b1 & nb2 | na1 & a2)
                  | na2 & (nb1 & b4 | b1 & nb3 | na1 & a4) & nb2
                  | a1 & (na3 & (nb3 & b4 | b1 & b2 & b3 & nb4 | na2 & (nb3 | nb2)) | na2 & b3 & b4 | a2 & (nb1 & b4 | a3 & na4 & b1) & nb3)
                  | nb1 & b2 & b3 & b4;
               this.extra[6][i] |= otherInits.extra[6][i];
               this.extra[7][i] |= otherInits.extra[7][i];
               thisHasNulls = thisHasNulls || this.extra[3][i] != 0L || this.extra[4][i] != 0L || this.extra[5][i] != 0L;
            }

            while(i < copyLimit) {
               this.extra[2][i] = 0L;
               long nb1;
               long nb3;
               long b1;
               long b2;
               long b3;
               this.extra[3][i] = (b2 = otherInits.extra[3][i]) & (nb3 = ~(b3 = otherInits.extra[4][i]) | (nb1 = ~(b1 = otherInits.extra[2][i])));
               long nb2;
               long b4;
               this.extra[4][i] = b3 & ((nb2 = ~b2) & (b4 = otherInits.extra[5][i]) | nb1) | b1 & nb2 & ~b4;
               this.extra[5][i] = (nb3 | nb2) & nb1 & b4 | b1 & nb3 & nb2;
               this.extra[6][i] |= otherInits.extra[6][i];
               this.extra[7][i] |= otherInits.extra[7][i];
               thisHasNulls = thisHasNulls || this.extra[3][i] != 0L || this.extra[4][i] != 0L || this.extra[5][i] != 0L;
               ++i;
            }

            while(i < resetLimit) {
               long a1 = this.extra[2][i];
               this.extra[2][i] = 0L;
               long a2;
               long a3;
               long na1;
               long na3;
               this.extra[3][i] = (a2 = this.extra[3][i]) & (na3 = ~(a3 = this.extra[4][i]) | (na1 = ~a1));
               long a4;
               long na2;
               this.extra[4][i] = a3 & ((na2 = ~a2) & (a4 = this.extra[5][i]) | na1) | a1 & na2 & ~a4;
               this.extra[5][i] = (na3 | na2) & na1 & a4 | a1 & na3 & na2;
               thisHasNulls = thisHasNulls || this.extra[3][i] != 0L || this.extra[4][i] != 0L || this.extra[5][i] != 0L;
               ++i;
            }
         }

         if (thisHasNulls) {
            this.tagBits |= 4;
         } else {
            this.tagBits &= -5;
         }

         return this;
      }
   }

   static int numberOfEnclosingFields(ReferenceBinding type) {
      int count = 0;

      for(ReferenceBinding var2 = type.enclosingType(); var2 != null; var2 = var2.enclosingType()) {
         count += var2.fieldCount();
      }

      return count;
   }

   @Override
   public UnconditionalFlowInfo nullInfoLessUnconditionalCopy() {
      if (this == DEAD_END) {
         return this;
      } else {
         UnconditionalFlowInfo copy = new UnconditionalFlowInfo();
         copy.definiteInits = this.definiteInits;
         copy.potentialInits = this.potentialInits;
         copy.iNBit = -1L;
         copy.iNNBit = -1L;
         copy.tagBits = this.tagBits & -5;
         copy.tagBits |= 64;
         copy.maxFieldCount = this.maxFieldCount;
         if (this.extra != null) {
            copy.extra = new long[8][];
            int length;
            System.arraycopy(this.extra[0], 0, copy.extra[0] = new long[length = this.extra[0].length], 0, length);
            System.arraycopy(this.extra[1], 0, copy.extra[1] = new long[length], 0, length);

            for(int j = 2; j < 8; ++j) {
               copy.extra[j] = new long[length];
            }

            Arrays.fill(copy.extra[6], -1L);
            Arrays.fill(copy.extra[7], -1L);
         }

         return copy;
      }
   }

   @Override
   public FlowInfo safeInitsWhenTrue() {
      return this.copy();
   }

   @Override
   public FlowInfo setReachMode(int reachMode) {
      if (this == DEAD_END) {
         return this;
      } else {
         if (reachMode == 0) {
            this.tagBits &= -4;
         } else if (reachMode == 2) {
            this.tagBits |= 2;
         } else {
            if ((this.tagBits & 3) == 0) {
               this.potentialInits = 0L;
               if (this.extra != null) {
                  int i = 0;

                  for(int length = this.extra[0].length; i < length; ++i) {
                     this.extra[1][i] = 0L;
                  }
               }
            }

            this.tagBits |= reachMode;
         }

         return this;
      }
   }

   @Override
   public String toString() {
      if (this == DEAD_END) {
         return "FlowInfo.DEAD_END";
      } else if ((this.tagBits & 4) != 0) {
         if (this.extra == null) {
            return "FlowInfo<def: "
               + this.definiteInits
               + ", pot: "
               + this.potentialInits
               + ", reachable:"
               + ((this.tagBits & 3) == 0)
               + ", null: "
               + this.nullBit1
               + this.nullBit2
               + this.nullBit3
               + this.nullBit4
               + ", incoming: "
               + this.iNBit
               + this.iNNBit
               + ">";
         } else {
            String def = "FlowInfo<def:[" + this.definiteInits;
            String pot = "], pot:[" + this.potentialInits;
            String nullS = ", null:[" + this.nullBit1 + this.nullBit2 + this.nullBit3 + this.nullBit4;
            int i = 0;

            int ceil;
            for(ceil = this.extra[0].length > 3 ? 3 : this.extra[0].length; i < ceil; ++i) {
               def = def + "," + this.extra[0][i];
               pot = pot + "," + this.extra[1][i];
               nullS = nullS
                  + ","
                  + this.extra[2][i]
                  + this.extra[3][i]
                  + this.extra[4][i]
                  + this.extra[5][i]
                  + ", incoming: "
                  + this.extra[6][i]
                  + this.extra[7];
            }

            if (ceil < this.extra[0].length) {
               def = def + ",...";
               pot = pot + ",...";
               nullS = nullS + ",...";
            }

            return def + pot + "], reachable:" + ((this.tagBits & 3) == 0) + nullS + "]>";
         }
      } else if (this.extra == null) {
         return "FlowInfo<def: " + this.definiteInits + ", pot: " + this.potentialInits + ", reachable:" + ((this.tagBits & 3) == 0) + ", no null info>";
      } else {
         String def = "FlowInfo<def:[" + this.definiteInits;
         String pot = "], pot:[" + this.potentialInits;
         int i = 0;

         int ceil;
         for(ceil = this.extra[0].length > 3 ? 3 : this.extra[0].length; i < ceil; ++i) {
            def = def + "," + this.extra[0][i];
            pot = pot + "," + this.extra[1][i];
         }

         if (ceil < this.extra[0].length) {
            def = def + ",...";
            pot = pot + ",...";
         }

         return def + pot + "], reachable:" + ((this.tagBits & 3) == 0) + ", no null info>";
      }
   }

   @Override
   public UnconditionalFlowInfo unconditionalCopy() {
      return (UnconditionalFlowInfo)this.copy();
   }

   @Override
   public UnconditionalFlowInfo unconditionalFieldLessCopy() {
      UnconditionalFlowInfo copy = new UnconditionalFlowInfo();
      copy.tagBits = this.tagBits;
      copy.maxFieldCount = this.maxFieldCount;
      int limit = this.maxFieldCount;
      if (limit < 64) {
         long mask;
         copy.definiteInits = this.definiteInits & (mask = ~((1L << limit) - 1L));
         copy.potentialInits = this.potentialInits & mask;
         copy.nullBit1 = this.nullBit1 & mask;
         copy.nullBit2 = this.nullBit2 & mask;
         copy.nullBit3 = this.nullBit3 & mask;
         copy.nullBit4 = this.nullBit4 & mask;
         copy.iNBit = this.iNBit & mask;
         copy.iNNBit = this.iNNBit & mask;
      }

      if (this.extra == null) {
         return copy;
      } else {
         int length;
         int vectorIndex;
         if ((vectorIndex = limit / 64 - 1) >= (length = this.extra[0].length)) {
            return copy;
         } else {
            copy.extra = new long[8][];
            int copyStart;
            if ((copyStart = vectorIndex + 1) < length) {
               int copyLength = length - copyStart;

               for(int j = 0; j < 8; ++j) {
                  System.arraycopy(this.extra[j], copyStart, copy.extra[j] = new long[length], copyStart, copyLength);
               }
            } else if (vectorIndex >= 0) {
               copy.createExtraSpace(length);
            }

            if (vectorIndex >= 0) {
               long mask = ~((1L << limit % 64) - 1L);

               for(int j = 0; j < 8; ++j) {
                  copy.extra[j][vectorIndex] = this.extra[j][vectorIndex] & mask;
               }
            }

            return copy;
         }
      }
   }

   @Override
   public UnconditionalFlowInfo unconditionalInits() {
      return this;
   }

   @Override
   public UnconditionalFlowInfo unconditionalInitsWithoutSideEffect() {
      return this;
   }

   @Override
   public void resetAssignmentInfo(LocalVariableBinding local) {
      this.resetAssignmentInfo(local.id + this.maxFieldCount);
   }

   public void resetAssignmentInfo(int position) {
      if (this != DEAD_END) {
         if (position < 64) {
            long mask;
            this.definiteInits &= mask = ~(1L << position);
            this.potentialInits &= mask;
         } else {
            int vectorIndex = position / 64 - 1;
            if (this.extra == null || vectorIndex >= this.extra[0].length) {
               return;
            }

            long mask;
            this.extra[0][vectorIndex] &= mask = ~(1L << position % 64);
            this.extra[1][vectorIndex] &= mask;
         }
      }
   }

   private void createExtraSpace(int length) {
      this.extra = new long[8][];

      for(int j = 0; j < 8; ++j) {
         this.extra[j] = new long[length];
      }

      if ((this.tagBits & 64) != 0) {
         Arrays.fill(this.extra[6], -1L);
         Arrays.fill(this.extra[7], -1L);
      }
   }

   public static class AssertionFailedException extends RuntimeException {
      private static final long serialVersionUID = 1827352841030089703L;

      public AssertionFailedException(String message) {
         super(message);
      }
   }
}
