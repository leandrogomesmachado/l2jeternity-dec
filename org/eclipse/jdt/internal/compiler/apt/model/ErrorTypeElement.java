package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

public class ErrorTypeElement extends TypeElementImpl {
   ErrorTypeElement(BaseProcessingEnvImpl env, ReferenceBinding binding) {
      super(env, binding, null);
   }

   @Override
   public List<? extends TypeMirror> getInterfaces() {
      return Collections.emptyList();
   }

   @Override
   public NestingKind getNestingKind() {
      return NestingKind.TOP_LEVEL;
   }

   @Override
   public Name getQualifiedName() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      char[] qName;
      if (binding.isMemberType()) {
         qName = CharOperation.concatWith(binding.enclosingType().compoundName, binding.sourceName, '.');
         CharOperation.replace(qName, '$', '.');
      } else {
         qName = CharOperation.concatWith(binding.compoundName, '.');
      }

      return new NameImpl(qName);
   }

   @Override
   public TypeMirror getSuperclass() {
      return this._env.getFactory().getNoType(TypeKind.NONE);
   }

   @Override
   public List<? extends TypeParameterElement> getTypeParameters() {
      return Collections.emptyList();
   }

   @Override
   public TypeMirror asType() {
      return this._env.getFactory().getErrorType((ReferenceBinding)this._binding);
   }

   @Override
   public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return null;
   }

   @Override
   public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return Collections.emptyList();
   }

   @Override
   public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return (A[])((Annotation[])Array.newInstance(annotationType, 0));
   }

   @Override
   public List<? extends Element> getEnclosedElements() {
      return Collections.emptyList();
   }

   @Override
   public Element getEnclosingElement() {
      return this._env.getFactory().newPackageElement(this._env.getLookupEnvironment().defaultPackage);
   }

   @Override
   public ElementKind getKind() {
      return ElementKind.CLASS;
   }

   @Override
   public Set<Modifier> getModifiers() {
      return Collections.emptySet();
   }

   @Override
   public Name getSimpleName() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      return new NameImpl(binding.sourceName());
   }
}
