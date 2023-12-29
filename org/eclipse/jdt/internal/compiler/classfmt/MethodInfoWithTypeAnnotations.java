package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

class MethodInfoWithTypeAnnotations extends MethodInfoWithParameterAnnotations {
   private TypeAnnotationInfo[] typeAnnotations;

   MethodInfoWithTypeAnnotations(
      MethodInfo methodInfo, AnnotationInfo[] annotations, AnnotationInfo[][] parameterAnnotations, TypeAnnotationInfo[] typeAnnotations
   ) {
      super(methodInfo, annotations, parameterAnnotations);
      this.typeAnnotations = typeAnnotations;
   }

   @Override
   public IBinaryTypeAnnotation[] getTypeAnnotations() {
      return this.typeAnnotations;
   }

   @Override
   protected void initialize() {
      int i = 0;

      for(int l = this.typeAnnotations == null ? 0 : this.typeAnnotations.length; i < l; ++i) {
         this.typeAnnotations[i].initialize();
      }

      super.initialize();
   }

   @Override
   protected void reset() {
      int i = 0;

      for(int l = this.typeAnnotations == null ? 0 : this.typeAnnotations.length; i < l; ++i) {
         this.typeAnnotations[i].reset();
      }

      super.reset();
   }

   @Override
   protected void toStringContent(StringBuffer buffer) {
      super.toStringContent(buffer);
      buffer.append("type annotations = \n");
      int i = 0;

      for(int l = this.typeAnnotations == null ? 0 : this.typeAnnotations.length; i < l; ++i) {
         buffer.append(this.typeAnnotations[i].toString());
         buffer.append('\n');
      }
   }
}
