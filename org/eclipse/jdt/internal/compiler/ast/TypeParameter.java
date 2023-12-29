package org.eclipse.jdt.internal.compiler.ast;

import java.util.List;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypeParameter extends AbstractVariableDeclaration {
   public TypeVariableBinding binding;
   public TypeReference[] bounds;

   @Override
   public int getKind() {
      return 6;
   }

   public void checkBounds(Scope scope) {
      if (this.type != null) {
         this.type.checkBounds(scope);
      }

      if (this.bounds != null) {
         int i = 0;

         for(int length = this.bounds.length; i < length; ++i) {
            this.bounds[i].checkBounds(scope);
         }
      }
   }

   public void getAllAnnotationContexts(int targetType, int typeParameterIndex, List allAnnotationContexts) {
      TypeReference.AnnotationCollector collector = new TypeReference.AnnotationCollector(this, targetType, typeParameterIndex, allAnnotationContexts);
      if (this.annotations != null) {
         int annotationsLength = this.annotations.length;

         for(int i = 0; i < annotationsLength; ++i) {
            this.annotations[i].traverse(collector, null);
         }
      }

      switch(collector.targetType) {
         case 0:
            collector.targetType = 17;
            break;
         case 1:
            collector.targetType = 18;
      }

      int boundIndex = 0;
      if (this.type != null) {
         if (this.type.resolvedType.isInterface()) {
            boundIndex = 1;
         }

         if ((this.type.bits & 1048576) != 0) {
            collector.info2 = boundIndex;
            this.type.traverse(collector, null);
         }
      }

      if (this.bounds != null) {
         int boundsLength = this.bounds.length;

         for(int i = 0; i < boundsLength; ++i) {
            TypeReference bound = this.bounds[i];
            if ((bound.bits & 1048576) != 0) {
               collector.info2 = ++boundIndex;
               bound.traverse(collector, null);
            }
         }
      }
   }

   private void internalResolve(Scope scope, boolean staticContext) {
      if (this.binding != null) {
         Binding existingType = scope.parent.getBinding(this.name, 4, this, false);
         if (existingType != null && this.binding != existingType && existingType.isValidBinding() && (existingType.kind() != 4100 || !staticContext)) {
            scope.problemReporter().typeHiding(this, existingType);
         }
      }

      if (this.annotations != null || scope.environment().usesNullTypeAnnotations()) {
         this.resolveAnnotations(scope);
      }
   }

   @Override
   public void resolve(BlockScope scope) {
      this.internalResolve(scope, scope.methodScope().isStatic);
   }

   public void resolve(ClassScope scope) {
      this.internalResolve(scope, scope.enclosingSourceType().isStatic());
   }

   public void resolveAnnotations(Scope scope) {
      BlockScope resolutionScope = Scope.typeAnnotationsResolutionScope(scope);
      if (resolutionScope != null) {
         AnnotationBinding[] annotationBindings = resolveAnnotations(resolutionScope, this.annotations, this.binding, false);
         boolean isAnnotationBasedNullAnalysisEnabled = scope.environment().globalOptions.isAnnotationBasedNullAnalysisEnabled;
         if (annotationBindings != null && annotationBindings.length > 0) {
            this.binding.setTypeAnnotations(annotationBindings, isAnnotationBasedNullAnalysisEnabled);
            scope.referenceCompilationUnit().compilationResult.hasAnnotations = true;
         }

         if (isAnnotationBasedNullAnalysisEnabled && this.binding != null && this.binding.isValidBinding()) {
            this.binding.evaluateNullAnnotations(scope, this);
         }
      }
   }

   @Override
   public StringBuffer printStatement(int indent, StringBuffer output) {
      if (this.annotations != null) {
         printAnnotations(this.annotations, output);
         output.append(' ');
      }

      output.append(this.name);
      if (this.type != null) {
         output.append(" extends ");
         this.type.print(0, output);
      }

      if (this.bounds != null) {
         for(int i = 0; i < this.bounds.length; ++i) {
            output.append(" & ");
            this.bounds[i].print(0, output);
         }
      }

      return output;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.annotations != null) {
            int annotationsLength = this.annotations.length;

            for(int i = 0; i < annotationsLength; ++i) {
               this.annotations[i].traverse(visitor, scope);
            }
         }

         if (this.type != null) {
            this.type.traverse(visitor, scope);
         }

         if (this.bounds != null) {
            int boundsLength = this.bounds.length;

            for(int i = 0; i < boundsLength; ++i) {
               this.bounds[i].traverse(visitor, scope);
            }
         }
      }

      visitor.endVisit(this, scope);
   }

   public void traverse(ASTVisitor visitor, ClassScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.annotations != null) {
            int annotationsLength = this.annotations.length;

            for(int i = 0; i < annotationsLength; ++i) {
               this.annotations[i].traverse(visitor, scope);
            }
         }

         if (this.type != null) {
            this.type.traverse(visitor, scope);
         }

         if (this.bounds != null) {
            int boundsLength = this.bounds.length;

            for(int i = 0; i < boundsLength; ++i) {
               this.bounds[i].traverse(visitor, scope);
            }
         }
      }

      visitor.endVisit(this, scope);
   }
}
