package org.eclipse.jdt.internal.compiler.codegen;

import java.text.MessageFormat;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class StackMapFrame {
   public static final int USED = 1;
   public static final int SAME_FRAME = 0;
   public static final int CHOP_FRAME = 1;
   public static final int APPEND_FRAME = 2;
   public static final int SAME_FRAME_EXTENDED = 3;
   public static final int FULL_FRAME = 4;
   public static final int SAME_LOCALS_1_STACK_ITEMS = 5;
   public static final int SAME_LOCALS_1_STACK_ITEMS_EXTENDED = 6;
   public int pc;
   public int numberOfStackItems;
   private int numberOfLocals;
   public int localIndex;
   public VerificationTypeInfo[] locals;
   public VerificationTypeInfo[] stackItems;
   private int numberOfDifferentLocals = -1;
   public int tagBits;

   public StackMapFrame(int initialLocalSize) {
      this.locals = new VerificationTypeInfo[initialLocalSize];
      this.numberOfLocals = -1;
      this.numberOfDifferentLocals = -1;
   }

   public int getFrameType(StackMapFrame prevFrame) {
      int offsetDelta = this.getOffsetDelta(prevFrame);
      switch(this.numberOfStackItems) {
         case 0:
            switch(this.numberOfDifferentLocals(prevFrame)) {
               case -3:
               case -2:
               case -1:
                  return 1;
               case 0:
                  return offsetDelta <= 63 ? 0 : 3;
               case 1:
               case 2:
               case 3:
                  return 2;
               default:
                  return 4;
            }
         case 1:
            switch(this.numberOfDifferentLocals(prevFrame)) {
               case 0:
                  return offsetDelta <= 63 ? 5 : 6;
            }
      }

      return 4;
   }

   public void addLocal(int resolvedPosition, VerificationTypeInfo info) {
      if (this.locals == null) {
         this.locals = new VerificationTypeInfo[resolvedPosition + 1];
         this.locals[resolvedPosition] = info;
      } else {
         int length = this.locals.length;
         if (resolvedPosition >= length) {
            System.arraycopy(this.locals, 0, this.locals = new VerificationTypeInfo[resolvedPosition + 1], 0, length);
         }

         this.locals[resolvedPosition] = info;
      }
   }

   public void addStackItem(VerificationTypeInfo info) {
      if (info == null) {
         throw new IllegalArgumentException("info cannot be null");
      } else {
         if (this.stackItems == null) {
            this.stackItems = new VerificationTypeInfo[1];
            this.stackItems[0] = info;
            this.numberOfStackItems = 1;
         } else {
            int length = this.stackItems.length;
            if (this.numberOfStackItems == length) {
               System.arraycopy(this.stackItems, 0, this.stackItems = new VerificationTypeInfo[length + 1], 0, length);
            }

            this.stackItems[this.numberOfStackItems++] = info;
         }
      }
   }

   public void addStackItem(TypeBinding binding) {
      if (this.stackItems == null) {
         this.stackItems = new VerificationTypeInfo[1];
         this.stackItems[0] = new VerificationTypeInfo(binding);
         this.numberOfStackItems = 1;
      } else {
         int length = this.stackItems.length;
         if (this.numberOfStackItems == length) {
            System.arraycopy(this.stackItems, 0, this.stackItems = new VerificationTypeInfo[length + 1], 0, length);
         }

         this.stackItems[this.numberOfStackItems++] = new VerificationTypeInfo(binding);
      }
   }

   public StackMapFrame duplicate() {
      int length = this.locals.length;
      StackMapFrame result = new StackMapFrame(length);
      result.numberOfLocals = -1;
      result.numberOfDifferentLocals = -1;
      result.pc = this.pc;
      result.numberOfStackItems = this.numberOfStackItems;
      if (length != 0) {
         result.locals = new VerificationTypeInfo[length];

         for(int i = 0; i < length; ++i) {
            VerificationTypeInfo verificationTypeInfo = this.locals[i];
            if (verificationTypeInfo != null) {
               result.locals[i] = verificationTypeInfo.duplicate();
            }
         }
      }

      length = this.numberOfStackItems;
      if (length != 0) {
         result.stackItems = new VerificationTypeInfo[length];

         for(int i = 0; i < length; ++i) {
            result.stackItems[i] = this.stackItems[i].duplicate();
         }
      }

      return result;
   }

   public int numberOfDifferentLocals(StackMapFrame prevFrame) {
      if (this.numberOfDifferentLocals != -1) {
         return this.numberOfDifferentLocals;
      } else if (prevFrame == null) {
         this.numberOfDifferentLocals = 0;
         return 0;
      } else {
         VerificationTypeInfo[] prevLocals = prevFrame.locals;
         VerificationTypeInfo[] currentLocals = this.locals;
         int prevLocalsLength = prevLocals == null ? 0 : prevLocals.length;
         int currentLocalsLength = currentLocals == null ? 0 : currentLocals.length;
         int prevNumberOfLocals = prevFrame.getNumberOfLocals();
         int currentNumberOfLocals = this.getNumberOfLocals();
         int result = 0;
         if (prevNumberOfLocals == 0) {
            if (currentNumberOfLocals != 0) {
               result = currentNumberOfLocals;
               int counter = 0;

               for(int i = 0; i < currentLocalsLength && counter < currentNumberOfLocals; ++i) {
                  if (currentLocals[i] == null) {
                     result = Integer.MAX_VALUE;
                     this.numberOfDifferentLocals = result;
                     return result;
                  }

                  switch(currentLocals[i].id()) {
                     case 7:
                     case 8:
                        ++i;
                     default:
                        ++counter;
                  }
               }
            }
         } else if (currentNumberOfLocals == 0) {
            int counter = 0;
            result = -prevNumberOfLocals;

            for(int i = 0; i < prevLocalsLength && counter < prevNumberOfLocals; ++i) {
               if (prevLocals[i] == null) {
                  result = Integer.MAX_VALUE;
                  this.numberOfDifferentLocals = result;
                  return result;
               }

               switch(prevLocals[i].id()) {
                  case 7:
                  case 8:
                     ++i;
                  default:
                     ++counter;
               }
            }
         } else {
            int indexInPrevLocals = 0;
            int indexInCurrentLocals = 0;
            int currentLocalsCounter = 0;

            int prevLocalsCounter;
            for(prevLocalsCounter = 0; indexInCurrentLocals < currentLocalsLength && currentLocalsCounter < currentNumberOfLocals; ++indexInCurrentLocals) {
               VerificationTypeInfo currentLocal = currentLocals[indexInCurrentLocals];
               if (currentLocal != null) {
                  ++currentLocalsCounter;
                  switch(currentLocal.id()) {
                     case 7:
                     case 8:
                        ++indexInCurrentLocals;
                  }
               }

               if (indexInPrevLocals >= prevLocalsLength || prevLocalsCounter >= prevNumberOfLocals) {
                  if (currentLocal == null) {
                     result = Integer.MAX_VALUE;
                     this.numberOfDifferentLocals = result;
                     return result;
                  }

                  ++result;
                  ++indexInCurrentLocals;
                  break;
               }

               VerificationTypeInfo prevLocal = prevLocals[indexInPrevLocals];
               if (prevLocal != null) {
                  ++prevLocalsCounter;
                  switch(prevLocal.id()) {
                     case 7:
                     case 8:
                        ++indexInPrevLocals;
                  }
               }

               if (!this.equals(prevLocal, currentLocal) || indexInPrevLocals != indexInCurrentLocals) {
                  result = Integer.MAX_VALUE;
                  this.numberOfDifferentLocals = result;
                  return result;
               }

               if (result != 0) {
                  result = Integer.MAX_VALUE;
                  this.numberOfDifferentLocals = result;
                  return result;
               }

               ++indexInPrevLocals;
            }

            if (currentLocalsCounter < currentNumberOfLocals) {
               for(; indexInCurrentLocals < currentLocalsLength && currentLocalsCounter < currentNumberOfLocals; ++indexInCurrentLocals) {
                  VerificationTypeInfo currentLocal = currentLocals[indexInCurrentLocals];
                  if (currentLocal == null) {
                     result = Integer.MAX_VALUE;
                     this.numberOfDifferentLocals = result;
                     return result;
                  }

                  ++result;
                  ++currentLocalsCounter;
                  switch(currentLocal.id()) {
                     case 7:
                     case 8:
                        ++indexInCurrentLocals;
                        break;
                  }
               }
            } else if (prevLocalsCounter < prevNumberOfLocals) {
               for(result = -result; indexInPrevLocals < prevLocalsLength && prevLocalsCounter < prevNumberOfLocals; ++indexInPrevLocals) {
                  VerificationTypeInfo prevLocal = prevLocals[indexInPrevLocals];
                  if (prevLocal == null) {
                     result = Integer.MAX_VALUE;
                     this.numberOfDifferentLocals = result;
                     return result;
                  }

                  --result;
                  ++prevLocalsCounter;
                  switch(prevLocal.id()) {
                     case 7:
                     case 8:
                        ++indexInPrevLocals;
                        break;
                  }
               }
            }
         }

         this.numberOfDifferentLocals = result;
         return result;
      }
   }

   public int getNumberOfLocals() {
      if (this.numberOfLocals != -1) {
         return this.numberOfLocals;
      } else {
         int result = 0;
         int length = this.locals == null ? 0 : this.locals.length;

         for(int i = 0; i < length; ++i) {
            if (this.locals[i] != null) {
               switch(this.locals[i].id()) {
                  case 7:
                  case 8:
                     ++i;
                  default:
                     ++result;
               }
            }
         }

         this.numberOfLocals = result;
         return result;
      }
   }

   public int getOffsetDelta(StackMapFrame prevFrame) {
      if (prevFrame == null) {
         return this.pc;
      } else {
         return prevFrame.pc == -1 ? this.pc : this.pc - prevFrame.pc - 1;
      }
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      this.printFrame(buffer, this);
      return String.valueOf(buffer);
   }

   private void printFrame(StringBuffer buffer, StackMapFrame frame) {
      String pattern = "[pc : {0} locals: {1} stack items: {2}\nlocals: {3}\nstack: {4}\n]";
      int localsLength = frame.locals == null ? 0 : frame.locals.length;
      buffer.append(
         MessageFormat.format(
            pattern,
            Integer.toString(frame.pc),
            Integer.toString(frame.getNumberOfLocals()),
            Integer.toString(frame.numberOfStackItems),
            this.print(frame.locals, localsLength),
            this.print(frame.stackItems, frame.numberOfStackItems)
         )
      );
   }

   private String print(VerificationTypeInfo[] infos, int length) {
      StringBuffer buffer = new StringBuffer();
      buffer.append('[');
      if (infos != null) {
         for(int i = 0; i < length; ++i) {
            if (i != 0) {
               buffer.append(',');
            }

            VerificationTypeInfo verificationTypeInfo = infos[i];
            if (verificationTypeInfo == null) {
               buffer.append("top");
            } else {
               buffer.append(verificationTypeInfo);
            }
         }
      }

      buffer.append(']');
      return String.valueOf(buffer);
   }

   public void putLocal(int resolvedPosition, VerificationTypeInfo info) {
      if (this.locals == null) {
         this.locals = new VerificationTypeInfo[resolvedPosition + 1];
         this.locals[resolvedPosition] = info;
      } else {
         int length = this.locals.length;
         if (resolvedPosition >= length) {
            System.arraycopy(this.locals, 0, this.locals = new VerificationTypeInfo[resolvedPosition + 1], 0, length);
         }

         this.locals[resolvedPosition] = info;
      }
   }

   public void replaceWithElementType() {
      VerificationTypeInfo info = this.stackItems[this.numberOfStackItems - 1];
      VerificationTypeInfo info2 = info.duplicate();
      info2.replaceWithElementType();
      this.stackItems[this.numberOfStackItems - 1] = info2;
   }

   public int getIndexOfDifferentLocals(int differentLocalsCount) {
      for(int i = this.locals.length - 1; i >= 0; --i) {
         VerificationTypeInfo currentLocal = this.locals[i];
         if (currentLocal != null) {
            if (--differentLocalsCount == 0) {
               return i;
            }
         }
      }

      return 0;
   }

   private boolean equals(VerificationTypeInfo info, VerificationTypeInfo info2) {
      if (info == null) {
         return info2 == null;
      } else {
         return info2 == null ? false : info.equals(info2);
      }
   }
}
