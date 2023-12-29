package org.eclipse.jdt.internal.compiler.env;

public interface ITypeAnnotationWalker {
   IBinaryAnnotation[] NO_ANNOTATIONS = new IBinaryAnnotation[0];
   ITypeAnnotationWalker EMPTY_ANNOTATION_WALKER = new ITypeAnnotationWalker() {
      @Override
      public ITypeAnnotationWalker toField() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toThrows(int rank) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toTypeArgument(int rank) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toMethodParameter(short index) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toSupertype(short index, char[] superTypeSignature) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toTypeParameterBounds(boolean isClassTypeParameter, int parameterRank) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toTypeBound(short boundIndex) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toTypeParameter(boolean isClassTypeParameter, int rank) {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toMethodReturn() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toReceiver() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toWildcardBound() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toNextArrayDimension() {
         return this;
      }

      @Override
      public ITypeAnnotationWalker toNextNestedType() {
         return this;
      }

      @Override
      public IBinaryAnnotation[] getAnnotationsAtCursor(int currentTypeId) {
         return NO_ANNOTATIONS;
      }
   };

   ITypeAnnotationWalker toField();

   ITypeAnnotationWalker toMethodReturn();

   ITypeAnnotationWalker toReceiver();

   ITypeAnnotationWalker toTypeParameter(boolean var1, int var2);

   ITypeAnnotationWalker toTypeParameterBounds(boolean var1, int var2);

   ITypeAnnotationWalker toTypeBound(short var1);

   ITypeAnnotationWalker toSupertype(short var1, char[] var2);

   ITypeAnnotationWalker toMethodParameter(short var1);

   ITypeAnnotationWalker toThrows(int var1);

   ITypeAnnotationWalker toTypeArgument(int var1);

   ITypeAnnotationWalker toWildcardBound();

   ITypeAnnotationWalker toNextArrayDimension();

   ITypeAnnotationWalker toNextNestedType();

   IBinaryAnnotation[] getAnnotationsAtCursor(int var1);
}
