package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.util.Util;

public class MethodInfo extends ClassFileStruct implements IBinaryMethod, Comparable {
   private static final char[][] noException = CharOperation.NO_CHAR_CHAR;
   private static final char[][] noArgumentNames = CharOperation.NO_CHAR_CHAR;
   private static final char[] ARG = "arg".toCharArray();
   protected int accessFlags = -1;
   protected int attributeBytes;
   protected char[] descriptor;
   protected char[][] exceptionNames;
   protected char[] name;
   protected char[] signature;
   protected int signatureUtf8Offset = -1;
   protected long tagBits;
   protected char[][] argumentNames;

   public static MethodInfo createMethod(byte[] classFileBytes, int[] offsets, int offset) {
      MethodInfo methodInfo = new MethodInfo(classFileBytes, offsets, offset);
      int attributesCount = methodInfo.u2At(6);
      int readOffset = 8;
      AnnotationInfo[] annotations = null;
      AnnotationInfo[][] parameterAnnotations = null;
      TypeAnnotationInfo[] typeAnnotations = null;

      for(int i = 0; i < attributesCount; ++i) {
         int utf8Offset = methodInfo.constantPoolOffsets[methodInfo.u2At(readOffset)] - methodInfo.structOffset;
         char[] attributeName = methodInfo.utf8At(utf8Offset + 3, methodInfo.u2At(utf8Offset + 1));
         if (attributeName.length > 0) {
            switch(attributeName[0]) {
               case 'M':
                  if (CharOperation.equals(attributeName, AttributeNamesConstants.MethodParametersName)) {
                     methodInfo.decodeMethodParameters(readOffset, methodInfo);
                  }
               case 'N':
               case 'O':
               case 'P':
               case 'Q':
               default:
                  break;
               case 'R':
                  AnnotationInfo[] methodAnnotations = null;
                  AnnotationInfo[][] paramAnnotations = null;
                  TypeAnnotationInfo[] methodTypeAnnotations = null;
                  if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleAnnotationsName)) {
                     methodAnnotations = decodeMethodAnnotations(readOffset, true, methodInfo);
                  } else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleAnnotationsName)) {
                     methodAnnotations = decodeMethodAnnotations(readOffset, false, methodInfo);
                  } else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleParameterAnnotationsName)) {
                     paramAnnotations = decodeParamAnnotations(readOffset, true, methodInfo);
                  } else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleParameterAnnotationsName)) {
                     paramAnnotations = decodeParamAnnotations(readOffset, false, methodInfo);
                  } else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleTypeAnnotationsName)) {
                     methodTypeAnnotations = decodeTypeAnnotations(readOffset, true, methodInfo);
                  } else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleTypeAnnotationsName)) {
                     methodTypeAnnotations = decodeTypeAnnotations(readOffset, false, methodInfo);
                  }

                  if (methodAnnotations != null) {
                     if (annotations == null) {
                        annotations = methodAnnotations;
                     } else {
                        int length = annotations.length;
                        AnnotationInfo[] newAnnotations = new AnnotationInfo[length + methodAnnotations.length];
                        System.arraycopy(annotations, 0, newAnnotations, 0, length);
                        System.arraycopy(methodAnnotations, 0, newAnnotations, length, methodAnnotations.length);
                        annotations = newAnnotations;
                     }
                  } else if (paramAnnotations != null) {
                     int numberOfParameters = paramAnnotations.length;
                     if (parameterAnnotations == null) {
                        parameterAnnotations = paramAnnotations;
                     } else {
                        for(int p = 0; p < numberOfParameters; ++p) {
                           int numberOfAnnotations = paramAnnotations[p] == null ? 0 : paramAnnotations[p].length;
                           if (numberOfAnnotations > 0) {
                              if (parameterAnnotations[p] == null) {
                                 parameterAnnotations[p] = paramAnnotations[p];
                              } else {
                                 int length = parameterAnnotations[p].length;
                                 AnnotationInfo[] newAnnotations = new AnnotationInfo[length + numberOfAnnotations];
                                 System.arraycopy(parameterAnnotations[p], 0, newAnnotations, 0, length);
                                 System.arraycopy(paramAnnotations[p], 0, newAnnotations, length, numberOfAnnotations);
                                 parameterAnnotations[p] = newAnnotations;
                              }
                           }
                        }
                     }
                  } else if (methodTypeAnnotations != null) {
                     if (typeAnnotations == null) {
                        typeAnnotations = methodTypeAnnotations;
                     } else {
                        int length = typeAnnotations.length;
                        TypeAnnotationInfo[] newAnnotations = new TypeAnnotationInfo[length + methodTypeAnnotations.length];
                        System.arraycopy(typeAnnotations, 0, newAnnotations, 0, length);
                        System.arraycopy(methodTypeAnnotations, 0, newAnnotations, length, methodTypeAnnotations.length);
                        typeAnnotations = newAnnotations;
                     }
                  }
                  break;
               case 'S':
                  if (CharOperation.equals(AttributeNamesConstants.SignatureName, attributeName)) {
                     methodInfo.signatureUtf8Offset = methodInfo.constantPoolOffsets[methodInfo.u2At(readOffset + 6)] - methodInfo.structOffset;
                  }
            }
         }

         readOffset = (int)((long)readOffset + 6L + methodInfo.u4At(readOffset + 2));
      }

      methodInfo.attributeBytes = readOffset;
      if (typeAnnotations != null) {
         return new MethodInfoWithTypeAnnotations(methodInfo, annotations, parameterAnnotations, typeAnnotations);
      } else if (parameterAnnotations != null) {
         return new MethodInfoWithParameterAnnotations(methodInfo, annotations, parameterAnnotations);
      } else {
         return (MethodInfo)(annotations != null ? new MethodInfoWithAnnotations(methodInfo, annotations) : methodInfo);
      }
   }

   static AnnotationInfo[] decodeAnnotations(int offset, boolean runtimeVisible, int numberOfAnnotations, MethodInfo methodInfo) {
      AnnotationInfo[] result = new AnnotationInfo[numberOfAnnotations];
      int readOffset = offset;

      for(int i = 0; i < numberOfAnnotations; ++i) {
         result[i] = new AnnotationInfo(methodInfo.reference, methodInfo.constantPoolOffsets, readOffset + methodInfo.structOffset, runtimeVisible, false);
         readOffset += result[i].readOffset;
      }

      return result;
   }

   static AnnotationInfo[] decodeMethodAnnotations(int offset, boolean runtimeVisible, MethodInfo methodInfo) {
      int numberOfAnnotations = methodInfo.u2At(offset + 6);
      if (numberOfAnnotations <= 0) {
         return null;
      } else {
         AnnotationInfo[] annos = decodeAnnotations(offset + 8, runtimeVisible, numberOfAnnotations, methodInfo);
         if (runtimeVisible) {
            int numStandardAnnotations = 0;

            for(int i = 0; i < numberOfAnnotations; ++i) {
               long standardAnnoTagBits = annos[i].standardAnnotationTagBits;
               methodInfo.tagBits |= standardAnnoTagBits;
               if (standardAnnoTagBits != 0L) {
                  annos[i] = null;
                  ++numStandardAnnotations;
               }
            }

            if (numStandardAnnotations != 0) {
               if (numStandardAnnotations == numberOfAnnotations) {
                  return null;
               }

               AnnotationInfo[] temp = new AnnotationInfo[numberOfAnnotations - numStandardAnnotations];
               int tmpIndex = 0;

               for(int i = 0; i < numberOfAnnotations; ++i) {
                  if (annos[i] != null) {
                     temp[tmpIndex++] = annos[i];
                  }
               }

               annos = temp;
            }
         }

         return annos;
      }
   }

   static TypeAnnotationInfo[] decodeTypeAnnotations(int offset, boolean runtimeVisible, MethodInfo methodInfo) {
      int numberOfAnnotations = methodInfo.u2At(offset + 6);
      if (numberOfAnnotations <= 0) {
         return null;
      } else {
         int readOffset = offset + 8;
         TypeAnnotationInfo[] typeAnnos = new TypeAnnotationInfo[numberOfAnnotations];

         for(int i = 0; i < numberOfAnnotations; ++i) {
            TypeAnnotationInfo newInfo = new TypeAnnotationInfo(
               methodInfo.reference, methodInfo.constantPoolOffsets, readOffset + methodInfo.structOffset, runtimeVisible, false
            );
            readOffset += newInfo.readOffset;
            typeAnnos[i] = newInfo;
         }

         return typeAnnos;
      }
   }

   static AnnotationInfo[][] decodeParamAnnotations(int offset, boolean runtimeVisible, MethodInfo methodInfo) {
      AnnotationInfo[][] allParamAnnotations = null;
      int numberOfParameters = methodInfo.u1At(offset + 6);
      if (numberOfParameters > 0) {
         int readOffset = offset + 7;

         for(int i = 0; i < numberOfParameters; ++i) {
            int numberOfAnnotations = methodInfo.u2At(readOffset);
            readOffset += 2;
            if (numberOfAnnotations > 0) {
               if (allParamAnnotations == null) {
                  allParamAnnotations = new AnnotationInfo[numberOfParameters][];
               }

               AnnotationInfo[] annos = decodeAnnotations(readOffset, runtimeVisible, numberOfAnnotations, methodInfo);
               allParamAnnotations[i] = annos;

               for(int aIndex = 0; aIndex < annos.length; ++aIndex) {
                  readOffset += annos[aIndex].readOffset;
               }
            }
         }
      }

      return allParamAnnotations;
   }

   protected MethodInfo(byte[] classFileBytes, int[] offsets, int offset) {
      super(classFileBytes, offsets, offset);
   }

   @Override
   public int compareTo(Object o) {
      MethodInfo otherMethod = (MethodInfo)o;
      int result = new String(this.getSelector()).compareTo(new String(otherMethod.getSelector()));
      return result != 0 ? result : new String(this.getMethodDescriptor()).compareTo(new String(otherMethod.getMethodDescriptor()));
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof MethodInfo)) {
         return false;
      } else {
         MethodInfo otherMethod = (MethodInfo)o;
         return CharOperation.equals(this.getSelector(), otherMethod.getSelector())
            && CharOperation.equals(this.getMethodDescriptor(), otherMethod.getMethodDescriptor());
      }
   }

   @Override
   public int hashCode() {
      return CharOperation.hashCode(this.getSelector()) + CharOperation.hashCode(this.getMethodDescriptor());
   }

   @Override
   public IBinaryAnnotation[] getAnnotations() {
      return null;
   }

   @Override
   public char[][] getArgumentNames() {
      if (this.argumentNames == null) {
         this.readCodeAttribute();
      }

      return this.argumentNames;
   }

   @Override
   public Object getDefaultValue() {
      return null;
   }

   @Override
   public char[][] getExceptionTypeNames() {
      if (this.exceptionNames == null) {
         this.readExceptionAttributes();
      }

      return this.exceptionNames;
   }

   @Override
   public char[] getGenericSignature() {
      if (this.signatureUtf8Offset != -1) {
         if (this.signature == null) {
            this.signature = this.utf8At(this.signatureUtf8Offset + 3, this.u2At(this.signatureUtf8Offset + 1));
         }

         return this.signature;
      } else {
         return null;
      }
   }

   @Override
   public char[] getMethodDescriptor() {
      if (this.descriptor == null) {
         int utf8Offset = this.constantPoolOffsets[this.u2At(4)] - this.structOffset;
         this.descriptor = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
      }

      return this.descriptor;
   }

   @Override
   public int getModifiers() {
      if (this.accessFlags == -1) {
         this.accessFlags = this.u2At(0);
         this.readModifierRelatedAttributes();
      }

      return this.accessFlags;
   }

   @Override
   public IBinaryAnnotation[] getParameterAnnotations(int index, char[] classFileName) {
      return null;
   }

   @Override
   public int getAnnotatedParametersCount() {
      return 0;
   }

   @Override
   public IBinaryTypeAnnotation[] getTypeAnnotations() {
      return null;
   }

   @Override
   public char[] getSelector() {
      if (this.name == null) {
         int utf8Offset = this.constantPoolOffsets[this.u2At(2)] - this.structOffset;
         this.name = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
      }

      return this.name;
   }

   @Override
   public long getTagBits() {
      return this.tagBits;
   }

   protected void initialize() {
      this.getModifiers();
      this.getSelector();
      this.getMethodDescriptor();
      this.getExceptionTypeNames();
      this.getGenericSignature();
      this.getArgumentNames();
      this.reset();
   }

   @Override
   public boolean isClinit() {
      char[] selector = this.getSelector();
      return selector[0] == '<' && selector.length == 8;
   }

   @Override
   public boolean isConstructor() {
      char[] selector = this.getSelector();
      return selector[0] == '<' && selector.length == 6;
   }

   public boolean isSynthetic() {
      return (this.getModifiers() & 4096) != 0;
   }

   private void readExceptionAttributes() {
      int attributesCount = this.u2At(6);
      int readOffset = 8;

      for(int i = 0; i < attributesCount; ++i) {
         int utf8Offset = this.constantPoolOffsets[this.u2At(readOffset)] - this.structOffset;
         char[] attributeName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
         if (CharOperation.equals(attributeName, AttributeNamesConstants.ExceptionsName)) {
            int entriesNumber = this.u2At(readOffset + 6);
            readOffset += 8;
            if (entriesNumber == 0) {
               this.exceptionNames = noException;
            } else {
               this.exceptionNames = new char[entriesNumber][];

               for(int j = 0; j < entriesNumber; ++j) {
                  utf8Offset = this.constantPoolOffsets[this.u2At(this.constantPoolOffsets[this.u2At(readOffset)] - this.structOffset + 1)]
                     - this.structOffset;
                  this.exceptionNames[j] = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
                  readOffset += 2;
               }
            }
         } else {
            readOffset = (int)((long)readOffset + 6L + this.u4At(readOffset + 2));
         }
      }

      if (this.exceptionNames == null) {
         this.exceptionNames = noException;
      }
   }

   private void readModifierRelatedAttributes() {
      int attributesCount = this.u2At(6);
      int readOffset = 8;

      for(int i = 0; i < attributesCount; ++i) {
         int utf8Offset = this.constantPoolOffsets[this.u2At(readOffset)] - this.structOffset;
         char[] attributeName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
         if (attributeName.length != 0) {
            switch(attributeName[0]) {
               case 'A':
                  if (CharOperation.equals(attributeName, AttributeNamesConstants.AnnotationDefaultName)) {
                     this.accessFlags |= 131072;
                  }
                  break;
               case 'D':
                  if (CharOperation.equals(attributeName, AttributeNamesConstants.DeprecatedName)) {
                     this.accessFlags |= 1048576;
                  }
                  break;
               case 'S':
                  if (CharOperation.equals(attributeName, AttributeNamesConstants.SyntheticName)) {
                     this.accessFlags |= 4096;
                  }
                  break;
               case 'V':
                  if (CharOperation.equals(attributeName, AttributeNamesConstants.VarargsName)) {
                     this.accessFlags |= 128;
                  }
            }
         }

         readOffset = (int)((long)readOffset + 6L + this.u4At(readOffset + 2));
      }
   }

   public int sizeInBytes() {
      return this.attributeBytes;
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      this.toString(buffer);
      return buffer.toString();
   }

   void toString(StringBuffer buffer) {
      buffer.append(this.getClass().getName());
      this.toStringContent(buffer);
   }

   protected void toStringContent(StringBuffer buffer) {
      int modifiers = this.getModifiers();
      char[] desc = this.getGenericSignature();
      if (desc == null) {
         desc = this.getMethodDescriptor();
      }

      buffer.append('{')
         .append(
            ((modifiers & 1048576) != 0 ? "deprecated " : Util.EMPTY_STRING)
               + ((modifiers & 1) == 1 ? "public " : Util.EMPTY_STRING)
               + ((modifiers & 2) == 2 ? "private " : Util.EMPTY_STRING)
               + ((modifiers & 4) == 4 ? "protected " : Util.EMPTY_STRING)
               + ((modifiers & 8) == 8 ? "static " : Util.EMPTY_STRING)
               + ((modifiers & 16) == 16 ? "final " : Util.EMPTY_STRING)
               + ((modifiers & 64) == 64 ? "bridge " : Util.EMPTY_STRING)
               + ((modifiers & 128) == 128 ? "varargs " : Util.EMPTY_STRING)
         )
         .append(this.getSelector())
         .append(desc)
         .append('}');
   }

   private void readCodeAttribute() {
      int attributesCount = this.u2At(6);
      int readOffset = 8;
      if (attributesCount != 0) {
         for(int i = 0; i < attributesCount; ++i) {
            int utf8Offset = this.constantPoolOffsets[this.u2At(readOffset)] - this.structOffset;
            char[] attributeName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
            if (CharOperation.equals(attributeName, AttributeNamesConstants.CodeName)) {
               this.decodeCodeAttribute(readOffset);
               if (this.argumentNames == null) {
                  this.argumentNames = noArgumentNames;
               }

               return;
            }

            readOffset = (int)((long)readOffset + 6L + this.u4At(readOffset + 2));
         }
      }

      this.argumentNames = noArgumentNames;
   }

   private void decodeCodeAttribute(int offset) {
      int readOffset = offset + 10;
      int codeLength = (int)this.u4At(readOffset);
      readOffset += 4 + codeLength;
      int exceptionTableLength = this.u2At(readOffset);
      readOffset += 2;
      if (exceptionTableLength != 0) {
         for(int i = 0; i < exceptionTableLength; ++i) {
            readOffset += 8;
         }
      }

      int attributesCount = this.u2At(readOffset);
      readOffset += 2;

      for(int i = 0; i < attributesCount; ++i) {
         int utf8Offset = this.constantPoolOffsets[this.u2At(readOffset)] - this.structOffset;
         char[] attributeName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
         if (CharOperation.equals(attributeName, AttributeNamesConstants.LocalVariableTableName)) {
            this.decodeLocalVariableAttribute(readOffset, codeLength);
         }

         readOffset = (int)((long)readOffset + 6L + this.u4At(readOffset + 2));
      }
   }

   private void decodeLocalVariableAttribute(int offset, int codeLength) {
      int readOffset = offset + 6;
      int length = this.u2At(readOffset);
      if (length != 0) {
         readOffset += 2;
         this.argumentNames = new char[length][];
         int argumentNamesIndex = 0;

         for(int i = 0; i < length; ++i) {
            int startPC = this.u2At(readOffset);
            if (startPC != 0) {
               break;
            }

            int nameIndex = this.u2At(4 + readOffset);
            int utf8Offset = this.constantPoolOffsets[nameIndex] - this.structOffset;
            char[] localVariableName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
            if (!CharOperation.equals(localVariableName, ConstantPool.This)) {
               this.argumentNames[argumentNamesIndex++] = localVariableName;
            }

            readOffset += 10;
         }

         if (argumentNamesIndex != this.argumentNames.length) {
            System.arraycopy(this.argumentNames, 0, this.argumentNames = new char[argumentNamesIndex][], 0, argumentNamesIndex);
         }
      }
   }

   private void decodeMethodParameters(int offset, MethodInfo methodInfo) {
      int readOffset = offset + 6;
      int length = this.u1At(readOffset);
      if (length != 0) {
         ++readOffset;
         this.argumentNames = new char[length][];

         for(int i = 0; i < length; ++i) {
            int nameIndex = this.u2At(readOffset);
            if (nameIndex != 0) {
               int utf8Offset = this.constantPoolOffsets[nameIndex] - this.structOffset;
               char[] parameterName = this.utf8At(utf8Offset + 3, this.u2At(utf8Offset + 1));
               this.argumentNames[i] = parameterName;
            } else {
               this.argumentNames[i] = CharOperation.concat(ARG, String.valueOf(i).toCharArray());
            }

            readOffset += 4;
         }
      }
   }
}
