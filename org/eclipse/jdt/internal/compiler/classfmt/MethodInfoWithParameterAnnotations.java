package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;

class MethodInfoWithParameterAnnotations extends MethodInfoWithAnnotations {
   private AnnotationInfo[][] parameterAnnotations;

   MethodInfoWithParameterAnnotations(MethodInfo methodInfo, AnnotationInfo[] annotations, AnnotationInfo[][] parameterAnnotations) {
      super(methodInfo, annotations);
      this.parameterAnnotations = parameterAnnotations;
   }

   @Override
   public IBinaryAnnotation[] getParameterAnnotations(int index, char[] classFileName) {
      try {
         return this.parameterAnnotations == null ? null : this.parameterAnnotations[index];
      } catch (ArrayIndexOutOfBoundsException var6) {
         StringBuffer message = new StringBuffer("Mismatching number of parameter annotations, ");
         message.append(index);
         message.append('>');
         message.append(this.parameterAnnotations.length - 1);
         message.append(" in ");
         message.append(this.getSelector());
         char[] desc = this.getGenericSignature();
         if (desc != null) {
            message.append(desc);
         } else {
            message.append(this.getMethodDescriptor());
         }

         if (classFileName != null) {
            message.append(" in ").append(classFileName);
         }

         throw new IllegalStateException(message.toString(), var6);
      }
   }

   @Override
   public int getAnnotatedParametersCount() {
      return this.parameterAnnotations == null ? 0 : this.parameterAnnotations.length;
   }

   @Override
   protected void initialize() {
      int i = 0;

      for(int l = this.parameterAnnotations == null ? 0 : this.parameterAnnotations.length; i < l; ++i) {
         AnnotationInfo[] infos = this.parameterAnnotations[i];
         int j = 0;

         for(int k = infos == null ? 0 : infos.length; j < k; ++j) {
            infos[j].initialize();
         }
      }

      super.initialize();
   }

   @Override
   protected void reset() {
      int i = 0;

      for(int l = this.parameterAnnotations == null ? 0 : this.parameterAnnotations.length; i < l; ++i) {
         AnnotationInfo[] infos = this.parameterAnnotations[i];
         int j = 0;

         for(int k = infos == null ? 0 : infos.length; j < k; ++j) {
            infos[j].reset();
         }
      }

      super.reset();
   }

   @Override
   protected void toStringContent(StringBuffer buffer) {
      super.toStringContent(buffer);
      int i = 0;

      for(int l = this.parameterAnnotations == null ? 0 : this.parameterAnnotations.length; i < l; ++i) {
         buffer.append("param" + (i - 1));
         buffer.append('\n');
         AnnotationInfo[] infos = this.parameterAnnotations[i];
         int j = 0;

         for(int k = infos == null ? 0 : infos.length; j < k; ++j) {
            buffer.append(infos[j]);
            buffer.append('\n');
         }
      }
   }
}
