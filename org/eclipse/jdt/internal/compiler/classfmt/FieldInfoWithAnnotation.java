package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;

public class FieldInfoWithAnnotation extends FieldInfo {
   private AnnotationInfo[] annotations;

   FieldInfoWithAnnotation(FieldInfo info, AnnotationInfo[] annos) {
      super(info.reference, info.constantPoolOffsets, info.structOffset);
      this.accessFlags = info.accessFlags;
      this.attributeBytes = info.attributeBytes;
      this.constant = info.constant;
      this.constantPoolOffsets = info.constantPoolOffsets;
      this.descriptor = info.descriptor;
      this.name = info.name;
      this.signature = info.signature;
      this.signatureUtf8Offset = info.signatureUtf8Offset;
      this.tagBits = info.tagBits;
      this.wrappedConstantValue = info.wrappedConstantValue;
      this.annotations = annos;
   }

   @Override
   public IBinaryAnnotation[] getAnnotations() {
      return this.annotations;
   }

   @Override
   protected void initialize() {
      if (this.annotations != null) {
         int i = 0;

         for(int max = this.annotations.length; i < max; ++i) {
            this.annotations[i].initialize();
         }
      }

      super.initialize();
   }

   @Override
   protected void reset() {
      if (this.annotations != null) {
         int i = 0;

         for(int max = this.annotations.length; i < max; ++i) {
            this.annotations[i].reset();
         }
      }

      super.reset();
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer(this.getClass().getName());
      if (this.annotations != null) {
         buffer.append('\n');

         for(int i = 0; i < this.annotations.length; ++i) {
            buffer.append(this.annotations[i]);
            buffer.append('\n');
         }
      }

      this.toStringContent(buffer);
      return buffer.toString();
   }
}
