package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;

public class MethodInfoWithAnnotations extends MethodInfo {
   protected AnnotationInfo[] annotations;

   MethodInfoWithAnnotations(MethodInfo methodInfo, AnnotationInfo[] annotations) {
      super(methodInfo.reference, methodInfo.constantPoolOffsets, methodInfo.structOffset);
      this.annotations = annotations;
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
   public IBinaryAnnotation[] getAnnotations() {
      return this.annotations;
   }

   @Override
   protected void initialize() {
      int i = 0;

      for(int l = this.annotations == null ? 0 : this.annotations.length; i < l; ++i) {
         if (this.annotations[i] != null) {
            this.annotations[i].initialize();
         }
      }

      super.initialize();
   }

   @Override
   protected void reset() {
      int i = 0;

      for(int l = this.annotations == null ? 0 : this.annotations.length; i < l; ++i) {
         if (this.annotations[i] != null) {
            this.annotations[i].reset();
         }
      }

      super.reset();
   }

   @Override
   protected void toStringContent(StringBuffer buffer) {
      super.toStringContent(buffer);
      int i = 0;

      for(int l = this.annotations == null ? 0 : this.annotations.length; i < l; ++i) {
         buffer.append(this.annotations[i]);
         buffer.append('\n');
      }
   }
}
