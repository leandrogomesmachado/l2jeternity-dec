package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypeElementImpl extends ElementImpl implements TypeElement {
   private final ElementKind _kindHint;

   TypeElementImpl(BaseProcessingEnvImpl env, ReferenceBinding binding, ElementKind kindHint) {
      super(env, binding);
      this._kindHint = kindHint;
   }

   @Override
   public <R, P> R accept(ElementVisitor<R, P> v, P p) {
      return v.visitType(this, p);
   }

   @Override
   protected AnnotationBinding[] getAnnotationBindings() {
      return ((ReferenceBinding)this._binding).getAnnotations();
   }

   @Override
   public List<? extends Element> getEnclosedElements() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      List<Element> enclosed = new ArrayList<>(binding.fieldCount() + binding.methods().length + binding.memberTypes().length);

      MethodBinding[] var6;
      for(MethodBinding method : var6 = binding.methods()) {
         ExecutableElement executable = new ExecutableElementImpl(this._env, method);
         enclosed.add(executable);
      }

      for(FieldBinding field : var14 = binding.fields()) {
         if (!field.isSynthetic()) {
            VariableElement variable = new VariableElementImpl(this._env, field);
            enclosed.add(variable);
         }
      }

      for(ReferenceBinding memberType : var15 = binding.memberTypes()) {
         TypeElement type = new TypeElementImpl(this._env, memberType, null);
         enclosed.add(type);
      }

      Collections.sort(enclosed, new TypeElementImpl.SourceLocationComparator(null));
      return Collections.unmodifiableList(enclosed);
   }

   @Override
   public Element getEnclosingElement() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      ReferenceBinding enclosingType = binding.enclosingType();
      return (Element)(enclosingType == null
         ? this._env.getFactory().newPackageElement(binding.fPackage)
         : this._env.getFactory().newElement(binding.enclosingType()));
   }

   @Override
   public String getFileName() {
      char[] name = ((ReferenceBinding)this._binding).getFileName();
      return name == null ? null : new String(name);
   }

   @Override
   public List<? extends TypeMirror> getInterfaces() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      if (binding.superInterfaces() != null && binding.superInterfaces().length != 0) {
         List<TypeMirror> interfaces = new ArrayList<>(binding.superInterfaces().length);

         ReferenceBinding[] var6;
         for(ReferenceBinding interfaceBinding : var6 = binding.superInterfaces()) {
            TypeMirror interfaceType = this._env.getFactory().newTypeMirror(interfaceBinding);
            if (interfaceType.getKind() == TypeKind.ERROR) {
               if (this._env.getSourceVersion().compareTo(SourceVersion.RELEASE_6) > 0) {
                  interfaces.add(interfaceType);
               }
            } else {
               interfaces.add(interfaceType);
            }
         }

         return Collections.unmodifiableList(interfaces);
      } else {
         return Collections.emptyList();
      }
   }

   @Override
   public ElementKind getKind() {
      if (this._kindHint != null) {
         return this._kindHint;
      } else {
         ReferenceBinding refBinding = (ReferenceBinding)this._binding;
         if (refBinding.isEnum()) {
            return ElementKind.ENUM;
         } else if (refBinding.isAnnotationType()) {
            return ElementKind.ANNOTATION_TYPE;
         } else if (refBinding.isInterface()) {
            return ElementKind.INTERFACE;
         } else if (refBinding.isClass()) {
            return ElementKind.CLASS;
         } else {
            throw new IllegalArgumentException(
               "TypeElement " + new String(refBinding.shortReadableName()) + " has unexpected attributes " + refBinding.modifiers
            );
         }
      }
   }

   @Override
   public Set<Modifier> getModifiers() {
      ReferenceBinding refBinding = (ReferenceBinding)this._binding;
      int modifiers = refBinding.modifiers;
      if (refBinding.isInterface() && refBinding.isNestedType()) {
         modifiers |= 8;
      }

      return Factory.getModifiers(modifiers, this.getKind(), refBinding.isBinaryBinding());
   }

   @Override
   public NestingKind getNestingKind() {
      ReferenceBinding refBinding = (ReferenceBinding)this._binding;
      if (refBinding.isAnonymousType()) {
         return NestingKind.ANONYMOUS;
      } else if (refBinding.isLocalType()) {
         return NestingKind.LOCAL;
      } else {
         return refBinding.isMemberType() ? NestingKind.MEMBER : NestingKind.TOP_LEVEL;
      }
   }

   @Override
   PackageElement getPackage() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      return this._env.getFactory().newPackageElement(binding.fPackage);
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
   public Name getSimpleName() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      return new NameImpl(binding.sourceName());
   }

   @Override
   public TypeMirror getSuperclass() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      ReferenceBinding superBinding = binding.superclass();
      return (TypeMirror)(superBinding != null && !binding.isInterface()
         ? this._env.getFactory().newTypeMirror(superBinding)
         : this._env.getFactory().getNoType(TypeKind.NONE));
   }

   @Override
   public List<? extends TypeParameterElement> getTypeParameters() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
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
      if (!(hidden instanceof TypeElementImpl)) {
         return false;
      } else {
         ReferenceBinding hiddenBinding = (ReferenceBinding)((TypeElementImpl)hidden)._binding;
         if (hiddenBinding.isPrivate()) {
            return false;
         } else {
            ReferenceBinding hiderBinding = (ReferenceBinding)this._binding;
            if (TypeBinding.equalsEquals(hiddenBinding, hiderBinding)) {
               return false;
            } else if (!hiddenBinding.isMemberType() || !hiderBinding.isMemberType()) {
               return false;
            } else if (!CharOperation.equals(hiddenBinding.sourceName, hiderBinding.sourceName)) {
               return false;
            } else {
               return hiderBinding.enclosingType().findSuperTypeOriginatingFrom(hiddenBinding.enclosingType()) != null;
            }
         }
      }
   }

   @Override
   public String toString() {
      ReferenceBinding binding = (ReferenceBinding)this._binding;
      char[] concatWith = CharOperation.concatWith(binding.compoundName, '.');
      if (binding.isNestedType()) {
         CharOperation.replace(concatWith, '$', '.');
         return new String(concatWith);
      } else {
         return new String(concatWith);
      }
   }

   private static final class SourceLocationComparator implements Comparator<Element> {
      private final IdentityHashMap<ElementImpl, Integer> sourceStartCache = new IdentityHashMap<>();

      private SourceLocationComparator() {
      }

      public int compare(Element o1, Element o2) {
         ElementImpl e1 = (ElementImpl)o1;
         ElementImpl e2 = (ElementImpl)o2;
         return this.getSourceStart(e1) - this.getSourceStart(e2);
      }

      private int getSourceStart(ElementImpl e) {
         Integer value = this.sourceStartCache.get(e);
         if (value == null) {
            value = this.determineSourceStart(e);
            this.sourceStartCache.put(e, value);
         }

         return value;
      }

      private int determineSourceStart(ElementImpl e) {
         switch(e.getKind()) {
            case ENUM:
            case CLASS:
            case ANNOTATION_TYPE:
            case INTERFACE:
               TypeElementImpl typeElementImpl = (TypeElementImpl)e;
               Binding typeBinding = typeElementImpl._binding;
               if (typeBinding instanceof SourceTypeBinding) {
                  SourceTypeBinding sourceTypeBinding = (SourceTypeBinding)typeBinding;
                  TypeDeclaration typeDeclaration = (TypeDeclaration)sourceTypeBinding.scope.referenceContext();
                  return typeDeclaration.sourceStart;
               }
               break;
            case ENUM_CONSTANT:
            case FIELD:
               VariableElementImpl variableElementImpl = (VariableElementImpl)e;
               Binding binding = variableElementImpl._binding;
               if (binding instanceof FieldBinding) {
                  FieldBinding fieldBinding = (FieldBinding)binding;
                  FieldDeclaration fieldDeclaration = fieldBinding.sourceField();
                  if (fieldDeclaration != null) {
                     return fieldDeclaration.sourceStart;
                  }
               }
            case PARAMETER:
            case LOCAL_VARIABLE:
            case EXCEPTION_PARAMETER:
            default:
               break;
            case METHOD:
            case CONSTRUCTOR:
               ExecutableElementImpl executableElementImpl = (ExecutableElementImpl)e;
               Binding binding = executableElementImpl._binding;
               if (binding instanceof MethodBinding) {
                  MethodBinding methodBinding = (MethodBinding)binding;
                  return methodBinding.sourceStart();
               }
         }

         return -1;
      }
   }
}
