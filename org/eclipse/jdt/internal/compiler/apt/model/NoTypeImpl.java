package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

public class NoTypeImpl implements NoType, NullType {
   private final TypeKind _kind;
   public static final NoType NO_TYPE_NONE = new NoTypeImpl(TypeKind.NONE);
   public static final NoType NO_TYPE_VOID = new NoTypeImpl(TypeKind.VOID);
   public static final NoType NO_TYPE_PACKAGE = new NoTypeImpl(TypeKind.PACKAGE);
   public static final NullType NULL_TYPE = new NoTypeImpl(TypeKind.NULL);

   private NoTypeImpl(TypeKind kind) {
      this._kind = kind;
   }

   @Override
   public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      switch(this.getKind()) {
         case NULL:
            return v.visitNull(this, p);
         default:
            return v.visitNoType(this, p);
      }
   }

   @Override
   public TypeKind getKind() {
      return this._kind;
   }

   @Override
   public String toString() {
      switch(this._kind) {
         case VOID:
            return "void";
         case NONE:
         case ARRAY:
         case DECLARED:
         case ERROR:
         case TYPEVAR:
         case WILDCARD:
         default:
            return "none";
         case NULL:
            return "null";
         case PACKAGE:
            return "package";
      }
   }

   @Override
   public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return Factory.EMPTY_ANNOTATION_MIRRORS;
   }

   @Override
   public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return null;
   }

   @Override
   public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return (A[])((Annotation[])Array.newInstance(annotationType, 0));
   }
}
