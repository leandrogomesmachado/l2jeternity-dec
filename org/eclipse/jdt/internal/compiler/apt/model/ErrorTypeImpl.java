package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class ErrorTypeImpl extends DeclaredTypeImpl implements ErrorType {
   ErrorTypeImpl(BaseProcessingEnvImpl env, ReferenceBinding binding) {
      super(env, binding);
   }

   @Override
   public Element asElement() {
      return this._env.getFactory().newElement((ReferenceBinding)this._binding);
   }

   @Override
   public TypeMirror getEnclosingType() {
      return NoTypeImpl.NO_TYPE_NONE;
   }

   @Override
   public List<? extends TypeMirror> getTypeArguments() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      if (binding.isParameterizedType()) {
         ParameterizedTypeBinding ptb = (ParameterizedTypeBinding)this._binding;
         TypeBinding[] arguments = ptb.arguments;
         int length = arguments == null ? 0 : arguments.length;
         if (length == 0) {
            return Collections.emptyList();
         } else {
            List<TypeMirror> args = new ArrayList<>(length);

            for(TypeBinding arg : arguments) {
               args.add(this._env.getFactory().newTypeMirror(arg));
            }

            return Collections.unmodifiableList(args);
         }
      } else if (!binding.isGenericType()) {
         return Collections.emptyList();
      } else {
         TypeVariableBinding[] typeVariables = binding.typeVariables();
         List<TypeMirror> args = new ArrayList<>(typeVariables.length);

         for(TypeBinding arg : typeVariables) {
            args.add(this._env.getFactory().newTypeMirror(arg));
         }

         return Collections.unmodifiableList(args);
      }
   }

   @Override
   public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      return v.visitError(this, p);
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

   @Override
   public TypeKind getKind() {
      return TypeKind.ERROR;
   }

   @Override
   public String toString() {
      return new String(this._binding.readableName());
   }
}
