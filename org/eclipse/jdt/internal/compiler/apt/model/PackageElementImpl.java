package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class PackageElementImpl extends ElementImpl implements PackageElement {
   PackageElementImpl(BaseProcessingEnvImpl env, PackageBinding binding) {
      super(env, binding);
   }

   @Override
   public <R, P> R accept(ElementVisitor<R, P> v, P p) {
      return v.visitPackage(this, p);
   }

   @Override
   protected AnnotationBinding[] getAnnotationBindings() {
      PackageBinding packageBinding = (PackageBinding)this._binding;
      char[][] compoundName = CharOperation.arrayConcat(packageBinding.compoundName, TypeConstants.PACKAGE_INFO_NAME);
      ReferenceBinding type = this._env.getLookupEnvironment().getType(compoundName);
      AnnotationBinding[] annotations = null;
      if (type != null && type.isValidBinding()) {
         annotations = type.getAnnotations();
      }

      return annotations;
   }

   @Override
   public List<? extends Element> getEnclosedElements() {
      PackageBinding binding = (PackageBinding)this._binding;
      LookupEnvironment environment = binding.environment;
      char[][][] typeNames = null;
      INameEnvironment nameEnvironment = binding.environment.nameEnvironment;
      if (nameEnvironment instanceof FileSystem) {
         typeNames = ((FileSystem)nameEnvironment).findTypeNames(binding.compoundName);
      }

      HashSet<Element> set = new HashSet<>();
      if (typeNames != null) {
         for(char[][] typeName : typeNames) {
            ReferenceBinding type = environment.getType(typeName);
            if (type != null && type.isValidBinding()) {
               set.add(this._env.getFactory().newElement(type));
            }
         }
      }

      ArrayList<Element> list = new ArrayList<>(set.size());
      list.addAll(set);
      return Collections.unmodifiableList(list);
   }

   @Override
   public Element getEnclosingElement() {
      return null;
   }

   @Override
   public ElementKind getKind() {
      return ElementKind.PACKAGE;
   }

   @Override
   PackageElement getPackage() {
      return this;
   }

   @Override
   public Name getSimpleName() {
      char[][] compoundName = ((PackageBinding)this._binding).compoundName;
      int length = compoundName.length;
      return length == 0 ? new NameImpl(CharOperation.NO_CHAR) : new NameImpl(compoundName[length - 1]);
   }

   @Override
   public Name getQualifiedName() {
      return new NameImpl(CharOperation.concatWith(((PackageBinding)this._binding).compoundName, '.'));
   }

   @Override
   public boolean isUnnamed() {
      PackageBinding binding = (PackageBinding)this._binding;
      return binding.compoundName == CharOperation.NO_CHAR_CHAR;
   }
}
