package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.eclipse.jdt.internal.compiler.apt.model.Factory;
import org.eclipse.jdt.internal.compiler.apt.model.TypeElementImpl;
import org.eclipse.jdt.internal.compiler.apt.util.ManyToMany;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

public class RoundEnvImpl implements RoundEnvironment {
   private final BaseProcessingEnvImpl _processingEnv;
   private final boolean _isLastRound;
   private final CompilationUnitDeclaration[] _units;
   private final ManyToMany<TypeElement, Element> _annoToUnit;
   private final ReferenceBinding[] _binaryTypes;
   private final Factory _factory;
   private Set<Element> _rootElements = null;

   public RoundEnvImpl(CompilationUnitDeclaration[] units, ReferenceBinding[] binaryTypeBindings, boolean isLastRound, BaseProcessingEnvImpl env) {
      this._processingEnv = env;
      this._isLastRound = isLastRound;
      this._units = units;
      this._factory = this._processingEnv.getFactory();
      AnnotationDiscoveryVisitor visitor = new AnnotationDiscoveryVisitor(this._processingEnv);
      if (this._units != null) {
         for(CompilationUnitDeclaration unit : this._units) {
            unit.scope.suppressImportErrors = true;
            unit.traverse(visitor, unit.scope);
            unit.scope.suppressImportErrors = false;
         }
      }

      this._annoToUnit = visitor._annoToElement;
      if (binaryTypeBindings != null) {
         this.collectAnnotations(binaryTypeBindings);
      }

      this._binaryTypes = binaryTypeBindings;
   }

   private void collectAnnotations(ReferenceBinding[] referenceBindings) {
      for(ReferenceBinding referenceBinding : referenceBindings) {
         if (referenceBinding instanceof ParameterizedTypeBinding) {
            referenceBinding = ((ParameterizedTypeBinding)referenceBinding).genericType();
         }

         AnnotationBinding[] annotationBindings = Factory.getPackedAnnotationBindings(referenceBinding.getAnnotations());

         for(AnnotationBinding annotationBinding : annotationBindings) {
            TypeElement anno = (TypeElement)this._factory.newElement(annotationBinding.getAnnotationType());
            Element element = this._factory.newElement(referenceBinding);
            this._annoToUnit.put(anno, element);
         }

         FieldBinding[] fieldBindings = referenceBinding.fields();

         for(FieldBinding fieldBinding : fieldBindings) {
            annotationBindings = Factory.getPackedAnnotationBindings(fieldBinding.getAnnotations());

            for(AnnotationBinding annotationBinding : annotationBindings) {
               TypeElement anno = (TypeElement)this._factory.newElement(annotationBinding.getAnnotationType());
               Element element = this._factory.newElement(fieldBinding);
               this._annoToUnit.put(anno, element);
            }
         }

         MethodBinding[] methodBindings = referenceBinding.methods();

         for(MethodBinding methodBinding : methodBindings) {
            annotationBindings = Factory.getPackedAnnotationBindings(methodBinding.getAnnotations());

            for(AnnotationBinding annotationBinding : annotationBindings) {
               TypeElement anno = (TypeElement)this._factory.newElement(annotationBinding.getAnnotationType());
               Element element = this._factory.newElement(methodBinding);
               this._annoToUnit.put(anno, element);
            }
         }

         ReferenceBinding[] memberTypes = referenceBinding.memberTypes();
         this.collectAnnotations(memberTypes);
      }
   }

   public Set<TypeElement> getRootAnnotations() {
      return Collections.unmodifiableSet(this._annoToUnit.getKeySet());
   }

   @Override
   public boolean errorRaised() {
      return this._processingEnv.errorRaised();
   }

   @Override
   public Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
      if (a.getKind() != ElementKind.ANNOTATION_TYPE) {
         throw new IllegalArgumentException("Argument must represent an annotation type");
      } else {
         Binding annoBinding = ((TypeElementImpl)a)._binding;
         if (0L == (annoBinding.getAnnotationTagBits() & 281474976710656L)) {
            return Collections.unmodifiableSet(this._annoToUnit.getValues(a));
         } else {
            Set<Element> annotatedElements = new HashSet<>(this._annoToUnit.getValues(a));
            ReferenceBinding annoTypeBinding = (ReferenceBinding)annoBinding;

            for(TypeElement element : ElementFilter.typesIn(this.getRootElements())) {
               ReferenceBinding typeBinding = (ReferenceBinding)((TypeElementImpl)element)._binding;
               this.addAnnotatedElements(annoTypeBinding, typeBinding, annotatedElements);
            }

            return Collections.unmodifiableSet(annotatedElements);
         }
      }
   }

   private void addAnnotatedElements(ReferenceBinding anno, ReferenceBinding type, Set<Element> result) {
      if (type.isClass() && this.inheritsAnno(type, anno)) {
         result.add(this._factory.newElement(type));
      }

      ReferenceBinding[] var7;
      for(ReferenceBinding element : var7 = type.memberTypes()) {
         this.addAnnotatedElements(anno, element, result);
      }
   }

   private boolean inheritsAnno(ReferenceBinding element, ReferenceBinding anno) {
      ReferenceBinding searchedElement = element;

      do {
         if (searchedElement instanceof ParameterizedTypeBinding) {
            searchedElement = ((ParameterizedTypeBinding)searchedElement).genericType();
         }

         AnnotationBinding[] annos = Factory.getPackedAnnotationBindings(searchedElement.getAnnotations());

         for(AnnotationBinding annoBinding : annos) {
            if (annoBinding.getAnnotationType() == anno) {
               return true;
            }
         }
      } while((searchedElement = searchedElement.superclass()) != null);

      return false;
   }

   @Override
   public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a) {
      String canonicalName = a.getCanonicalName();
      if (canonicalName == null) {
         throw new IllegalArgumentException("Argument must represent an annotation type");
      } else {
         TypeElement annoType = this._processingEnv.getElementUtils().getTypeElement(canonicalName);
         return annoType == null ? Collections.emptySet() : this.getElementsAnnotatedWith(annoType);
      }
   }

   @Override
   public Set<? extends Element> getRootElements() {
      if (this._units == null) {
         return Collections.emptySet();
      } else {
         if (this._rootElements == null) {
            Set<Element> elements = new HashSet<>(this._units.length);

            for(CompilationUnitDeclaration unit : this._units) {
               if (unit.scope != null && unit.scope.topLevelTypes != null) {
                  for(SourceTypeBinding binding : unit.scope.topLevelTypes) {
                     Element element = this._factory.newElement(binding);
                     if (element == null) {
                        throw new IllegalArgumentException("Top-level type binding could not be converted to element: " + binding);
                     }

                     elements.add(element);
                  }
               }
            }

            if (this._binaryTypes != null) {
               for(ReferenceBinding typeBinding : this._binaryTypes) {
                  Element element = this._factory.newElement(typeBinding);
                  if (element == null) {
                     throw new IllegalArgumentException("Top-level type binding could not be converted to element: " + typeBinding);
                  }

                  elements.add(element);
               }
            }

            this._rootElements = elements;
         }

         return this._rootElements;
      }
   }

   @Override
   public boolean processingOver() {
      return this._isLastRound;
   }
}
