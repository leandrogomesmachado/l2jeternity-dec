package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;

public class AnnotationMethodInfo extends MethodInfo {
   protected Object defaultValue = null;

   public static MethodInfo createAnnotationMethod(byte[] classFileBytes, int[] offsets, int offset) {
      MethodInfo methodInfo = new MethodInfo(classFileBytes, offsets, offset);
      int attributesCount = methodInfo.u2At(6);
      int readOffset = 8;
      AnnotationInfo[] annotations = null;
      Object defaultValue = null;

      for(int i = 0; i < attributesCount; ++i) {
         int utf8Offset = methodInfo.constantPoolOffsets[methodInfo.u2At(readOffset)] - methodInfo.structOffset;
         char[] attributeName = methodInfo.utf8At(utf8Offset + 3, methodInfo.u2At(utf8Offset + 1));
         if (attributeName.length > 0) {
            switch(attributeName[0]) {
               case 'A':
                  if (CharOperation.equals(attributeName, AttributeNamesConstants.AnnotationDefaultName)) {
                     AnnotationInfo info = new AnnotationInfo(methodInfo.reference, methodInfo.constantPoolOffsets, readOffset + 6 + methodInfo.structOffset);
                     defaultValue = info.decodeDefaultValue();
                  }
                  break;
               case 'R':
                  AnnotationInfo[] methodAnnotations = null;
                  if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeVisibleAnnotationsName)) {
                     methodAnnotations = decodeMethodAnnotations(readOffset, true, methodInfo);
                  } else if (CharOperation.equals(attributeName, AttributeNamesConstants.RuntimeInvisibleAnnotationsName)) {
                     methodAnnotations = decodeMethodAnnotations(readOffset, false, methodInfo);
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
      if (defaultValue != null) {
         return (MethodInfo)(annotations != null
            ? new AnnotationMethodInfoWithAnnotations(methodInfo, defaultValue, annotations)
            : new AnnotationMethodInfo(methodInfo, defaultValue));
      } else {
         return (MethodInfo)(annotations != null ? new MethodInfoWithAnnotations(methodInfo, annotations) : methodInfo);
      }
   }

   AnnotationMethodInfo(MethodInfo methodInfo, Object defaultValue) {
      super(methodInfo.reference, methodInfo.constantPoolOffsets, methodInfo.structOffset);
      this.defaultValue = defaultValue;
      this.accessFlags = methodInfo.accessFlags;
      this.attributeBytes = methodInfo.attributeBytes;
      this.descriptor = methodInfo.descriptor;
      this.exceptionNames = methodInfo.exceptionNames;
      this.name = methodInfo.name;
      this.signature = methodInfo.signature;
      this.signatureUtf8Offset = methodInfo.signatureUtf8Offset;
      this.tagBits = methodInfo.tagBits;
   }

   @Override
   public Object getDefaultValue() {
      return this.defaultValue;
   }

   @Override
   protected void toStringContent(StringBuffer buffer) {
      super.toStringContent(buffer);
      if (this.defaultValue != null) {
         buffer.append(" default ");
         if (!(this.defaultValue instanceof Object[])) {
            buffer.append(this.defaultValue);
         } else {
            buffer.append('{');
            Object[] elements = (Object[])this.defaultValue;
            int i = 0;

            for(int len = elements.length; i < len; ++i) {
               if (i > 0) {
                  buffer.append(", ");
               }

               buffer.append(elements[i]);
            }

            buffer.append('}');
         }

         buffer.append('\n');
      }
   }
}
