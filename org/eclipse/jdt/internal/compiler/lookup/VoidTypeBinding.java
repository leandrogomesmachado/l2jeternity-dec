package org.eclipse.jdt.internal.compiler.lookup;

public class VoidTypeBinding extends BaseTypeBinding {
   VoidTypeBinding() {
      super(6, TypeConstants.VOID, new char[]{'V'});
   }

   @Override
   public TypeBinding clone(TypeBinding enclosingType) {
      return this;
   }

   @Override
   public void setTypeAnnotations(AnnotationBinding[] annotations, boolean evalNullAnnotations) {
   }

   @Override
   public TypeBinding unannotated() {
      return this;
   }
}
