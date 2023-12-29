package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypeVariableImpl extends TypeMirrorImpl implements TypeVariable {
   TypeVariableImpl(BaseProcessingEnvImpl env, TypeVariableBinding binding) {
      super(env, binding);
   }

   @Override
   public Element asElement() {
      return this._env.getFactory().newElement(this._binding);
   }

   @Override
   public TypeMirror getLowerBound() {
      return this._env.getFactory().getNullType();
   }

   @Override
   public TypeMirror getUpperBound() {
      TypeVariableBinding typeVariableBinding = (TypeVariableBinding)this._binding;
      TypeBinding firstBound = typeVariableBinding.firstBound;
      ReferenceBinding[] superInterfaces = typeVariableBinding.superInterfaces;
      if (firstBound != null && superInterfaces.length != 0) {
         return firstBound != null && superInterfaces.length == 1 && TypeBinding.equalsEquals(superInterfaces[0], firstBound)
            ? this._env.getFactory().newTypeMirror(typeVariableBinding.upperBound())
            : this._env.getFactory().newTypeMirror((TypeVariableBinding)this._binding);
      } else {
         return this._env.getFactory().newTypeMirror(typeVariableBinding.upperBound());
      }
   }

   @Override
   public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      return v.visitTypeVariable(this, p);
   }

   @Override
   public TypeKind getKind() {
      return TypeKind.TYPEVAR;
   }
}
