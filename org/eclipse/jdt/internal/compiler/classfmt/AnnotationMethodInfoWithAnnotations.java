package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;

public class AnnotationMethodInfoWithAnnotations extends AnnotationMethodInfo {
   private AnnotationInfo[] annotations;

   AnnotationMethodInfoWithAnnotations(MethodInfo methodInfo, Object defaultValue, AnnotationInfo[] annotations) {
      super(methodInfo, defaultValue);
      this.annotations = annotations;
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
