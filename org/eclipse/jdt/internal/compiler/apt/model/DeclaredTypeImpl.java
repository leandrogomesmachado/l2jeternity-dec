package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class DeclaredTypeImpl extends TypeMirrorImpl implements DeclaredType {
   private final ElementKind _elementKindHint;

   DeclaredTypeImpl(BaseProcessingEnvImpl env, ReferenceBinding binding) {
      super(env, binding);
      this._elementKindHint = null;
   }

   DeclaredTypeImpl(BaseProcessingEnvImpl env, ReferenceBinding binding, ElementKind elementKindHint) {
      super(env, binding);
      this._elementKindHint = elementKindHint;
   }

   @Override
   public Element asElement() {
      return this._env.getFactory().newElement((ReferenceBinding)this._binding, this._elementKindHint);
   }

   @Override
   public TypeMirror getEnclosingType() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      ReferenceBinding enclosingType = binding.enclosingType();
      return (TypeMirror)(enclosingType != null ? this._env.getFactory().newTypeMirror(enclosingType) : this._env.getFactory().getNoType(TypeKind.NONE));
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
      return v.visitDeclared(this, p);
   }

   @Override
   public TypeKind getKind() {
      return TypeKind.DECLARED;
   }

   @Override
   public String toString() {
      return new String(this._binding.readableName());
   }
}
