package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class ExecutableTypeImpl extends TypeMirrorImpl implements ExecutableType {
   ExecutableTypeImpl(BaseProcessingEnvImpl env, MethodBinding binding) {
      super(env, binding);
   }

   @Override
   public List<? extends TypeMirror> getParameterTypes() {
      MethodBinding binding = (MethodBinding)this._binding;
      TypeBinding[] parameters = binding.parameters;
      int length = parameters.length;
      boolean isEnumConstructor = binding.isConstructor()
         && binding.declaringClass.isEnum()
         && binding.declaringClass.isBinaryBinding()
         && (binding.modifiers & 1073741824) == 0;
      if (isEnumConstructor) {
         if (length == 2) {
            return Collections.emptyList();
         } else {
            ArrayList<TypeMirror> list = new ArrayList<>();

            for(int i = 2; i < length; ++i) {
               list.add(this._env.getFactory().newTypeMirror(parameters[i]));
            }

            return Collections.unmodifiableList(list);
         }
      } else if (length == 0) {
         return Collections.emptyList();
      } else {
         ArrayList<TypeMirror> list = new ArrayList<>();

         for(TypeBinding typeBinding : parameters) {
            list.add(this._env.getFactory().newTypeMirror(typeBinding));
         }

         return Collections.unmodifiableList(list);
      }
   }

   @Override
   public TypeMirror getReturnType() {
      return this._env.getFactory().newTypeMirror(((MethodBinding)this._binding).returnType);
   }

   @Override
   protected AnnotationBinding[] getAnnotationBindings() {
      return ((MethodBinding)this._binding).returnType.getTypeAnnotations();
   }

   @Override
   public List<? extends TypeMirror> getThrownTypes() {
      ArrayList<TypeMirror> list = new ArrayList<>();
      ReferenceBinding[] thrownExceptions = ((MethodBinding)this._binding).thrownExceptions;
      if (thrownExceptions.length != 0) {
         for(ReferenceBinding referenceBinding : thrownExceptions) {
            list.add(this._env.getFactory().newTypeMirror(referenceBinding));
         }
      }

      return Collections.unmodifiableList(list);
   }

   @Override
   public List<? extends TypeVariable> getTypeVariables() {
      ArrayList<TypeVariable> list = new ArrayList<>();
      TypeVariableBinding[] typeVariables = ((MethodBinding)this._binding).typeVariables();
      if (typeVariables.length != 0) {
         for(TypeVariableBinding typeVariableBinding : typeVariables) {
            list.add((TypeVariable)this._env.getFactory().newTypeMirror(typeVariableBinding));
         }
      }

      return Collections.unmodifiableList(list);
   }

   @Override
   public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      return v.visitExecutable(this, p);
   }

   @Override
   public TypeKind getKind() {
      return TypeKind.EXECUTABLE;
   }

   @Override
   public TypeMirror getReceiverType() {
      return this._env.getFactory().getReceiverType((MethodBinding)this._binding);
   }

   @Override
   public String toString() {
      return new String(((MethodBinding)this._binding).returnType.readableName());
   }
}
