package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationHolder;
import org.eclipse.jdt.internal.compiler.lookup.AptBinaryLocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodVerifier;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class ExecutableElementImpl extends ElementImpl implements ExecutableElement {
   private Name _name = null;

   ExecutableElementImpl(BaseProcessingEnvImpl env, MethodBinding binding) {
      super(env, binding);
   }

   @Override
   public <R, P> R accept(ElementVisitor<R, P> v, P p) {
      return v.visitExecutable(this, p);
   }

   @Override
   protected AnnotationBinding[] getAnnotationBindings() {
      return ((MethodBinding)this._binding).getAnnotations();
   }

   @Override
   public AnnotationValue getDefaultValue() {
      MethodBinding binding = (MethodBinding)this._binding;
      Object defaultValue = binding.getDefaultValue();
      return defaultValue != null ? new AnnotationMemberValue(this._env, defaultValue, binding) : null;
   }

   @Override
   public List<? extends Element> getEnclosedElements() {
      return Collections.emptyList();
   }

   @Override
   public Element getEnclosingElement() {
      MethodBinding binding = (MethodBinding)this._binding;
      return binding.declaringClass == null ? null : this._env.getFactory().newElement(binding.declaringClass);
   }

   @Override
   public String getFileName() {
      ReferenceBinding dc = ((MethodBinding)this._binding).declaringClass;
      char[] name = dc.getFileName();
      return name == null ? null : new String(name);
   }

   @Override
   public ElementKind getKind() {
      MethodBinding binding = (MethodBinding)this._binding;
      if (binding.isConstructor()) {
         return ElementKind.CONSTRUCTOR;
      } else if (CharOperation.equals(binding.selector, TypeConstants.CLINIT)) {
         return ElementKind.STATIC_INIT;
      } else {
         return CharOperation.equals(binding.selector, TypeConstants.INIT) ? ElementKind.INSTANCE_INIT : ElementKind.METHOD;
      }
   }

   @Override
   public Set<Modifier> getModifiers() {
      MethodBinding binding = (MethodBinding)this._binding;
      return Factory.getModifiers(binding.modifiers, this.getKind());
   }

   @Override
   PackageElement getPackage() {
      MethodBinding binding = (MethodBinding)this._binding;
      return binding.declaringClass == null ? null : this._env.getFactory().newPackageElement(binding.declaringClass.fPackage);
   }

   @Override
   public List<? extends VariableElement> getParameters() {
      MethodBinding binding = (MethodBinding)this._binding;
      int length = binding.parameters == null ? 0 : binding.parameters.length;
      if (length == 0) {
         return Collections.emptyList();
      } else {
         AbstractMethodDeclaration methodDeclaration = binding.sourceMethod();
         List<VariableElement> params = new ArrayList<>(length);
         if (methodDeclaration != null) {
            for(Argument argument : methodDeclaration.arguments) {
               VariableElement param = new VariableElementImpl(this._env, argument.binding);
               params.add(param);
            }
         } else {
            AnnotationBinding[][] parameterAnnotationBindings = null;
            AnnotationHolder annotationHolder = binding.declaringClass.retrieveAnnotationHolder(binding, false);
            if (annotationHolder != null) {
               parameterAnnotationBindings = annotationHolder.getParameterAnnotations();
            }

            int i = 0;

            for(TypeBinding typeBinding : binding.parameters) {
               char[] name = binding.parameterNames.length > i ? binding.parameterNames[i] : null;
               if (name == null) {
                  StringBuilder builder = new StringBuilder("arg");
                  builder.append(i);
                  name = String.valueOf(builder).toCharArray();
               }

               VariableElement param = new VariableElementImpl(
                  this._env,
                  new AptBinaryLocalVariableBinding(name, typeBinding, 0, parameterAnnotationBindings != null ? parameterAnnotationBindings[i] : null, binding)
               );
               params.add(param);
               ++i;
            }
         }

         return Collections.unmodifiableList(params);
      }
   }

   @Override
   public TypeMirror getReturnType() {
      MethodBinding binding = (MethodBinding)this._binding;
      return binding.returnType == null ? null : this._env.getFactory().newTypeMirror(binding.returnType);
   }

   @Override
   public Name getSimpleName() {
      MethodBinding binding = (MethodBinding)this._binding;
      if (this._name == null) {
         this._name = new NameImpl(binding.selector);
      }

      return this._name;
   }

   @Override
   public List<? extends TypeMirror> getThrownTypes() {
      MethodBinding binding = (MethodBinding)this._binding;
      if (binding.thrownExceptions.length == 0) {
         return Collections.emptyList();
      } else {
         List<TypeMirror> list = new ArrayList<>(binding.thrownExceptions.length);

         for(ReferenceBinding exception : binding.thrownExceptions) {
            list.add(this._env.getFactory().newTypeMirror(exception));
         }

         return list;
      }
   }

   @Override
   public List<? extends TypeParameterElement> getTypeParameters() {
      MethodBinding binding = (MethodBinding)this._binding;
      TypeVariableBinding[] variables = binding.typeVariables();
      if (variables.length == 0) {
         return Collections.emptyList();
      } else {
         List<TypeParameterElement> params = new ArrayList<>(variables.length);

         for(TypeVariableBinding variable : variables) {
            params.add(this._env.getFactory().newTypeParameterElement(variable, this));
         }

         return Collections.unmodifiableList(params);
      }
   }

   @Override
   public boolean hides(Element hidden) {
      if (!(hidden instanceof ExecutableElementImpl)) {
         return false;
      } else {
         MethodBinding hiderBinding = (MethodBinding)this._binding;
         MethodBinding hiddenBinding = (MethodBinding)((ExecutableElementImpl)hidden)._binding;
         if (hiderBinding == hiddenBinding) {
            return false;
         } else if (hiddenBinding.isPrivate()) {
            return false;
         } else if (!hiderBinding.isStatic() || !hiddenBinding.isStatic()) {
            return false;
         } else if (!CharOperation.equals(hiddenBinding.selector, hiderBinding.selector)) {
            return false;
         } else if (!this._env.getLookupEnvironment().methodVerifier().isMethodSubsignature(hiderBinding, hiddenBinding)) {
            return false;
         } else {
            return hiderBinding.declaringClass.findSuperTypeOriginatingFrom(hiddenBinding.declaringClass) != null;
         }
      }
   }

   @Override
   public boolean isVarArgs() {
      return ((MethodBinding)this._binding).isVarargs();
   }

   public boolean overrides(ExecutableElement overridden, TypeElement type) {
      MethodBinding overriddenBinding = (MethodBinding)((ExecutableElementImpl)overridden)._binding;
      ReferenceBinding overriderContext = (ReferenceBinding)((TypeElementImpl)type)._binding;
      if ((MethodBinding)this._binding != overriddenBinding
         && !overriddenBinding.isStatic()
         && !overriddenBinding.isPrivate()
         && !((MethodBinding)this._binding).isStatic()) {
         char[] selector = ((MethodBinding)this._binding).selector;
         if (!CharOperation.equals(selector, overriddenBinding.selector)) {
            return false;
         } else if (overriderContext.findSuperTypeOriginatingFrom(((MethodBinding)this._binding).declaringClass) == null
            && ((MethodBinding)this._binding).declaringClass.findSuperTypeOriginatingFrom(overriderContext) == null) {
            return false;
         } else {
            MethodBinding overriderBinding = new MethodBinding((MethodBinding)this._binding, overriderContext);
            if (overriderBinding.isPrivate()) {
               return false;
            } else {
               TypeBinding match = overriderBinding.declaringClass.findSuperTypeOriginatingFrom(overriddenBinding.declaringClass);
               if (!(match instanceof ReferenceBinding)) {
                  return false;
               } else {
                  MethodBinding[] superMethods = ((ReferenceBinding)match).getMethods(selector);
                  LookupEnvironment lookupEnvironment = this._env.getLookupEnvironment();
                  if (lookupEnvironment == null) {
                     return false;
                  } else {
                     MethodVerifier methodVerifier = lookupEnvironment.methodVerifier();
                     int i = 0;

                     for(int length = superMethods.length; i < length; ++i) {
                        if (superMethods[i].original() == overriddenBinding) {
                           return methodVerifier.doesMethodOverride(overriderBinding, superMethods[i]);
                        }
                     }

                     return false;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public TypeMirror getReceiverType() {
      return this._env.getFactory().getReceiverType((MethodBinding)this._binding);
   }

   @Override
   public boolean isDefault() {
      return this._binding != null ? ((MethodBinding)this._binding).isDefaultMethod() : false;
   }
}
