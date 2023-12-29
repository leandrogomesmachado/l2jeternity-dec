package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

public class ArrayQualifiedTypeReference extends QualifiedTypeReference {
   int dimensions;
   private Annotation[][] annotationsOnDimensions;
   public int extendedDimensions;

   public ArrayQualifiedTypeReference(char[][] sources, int dim, long[] poss) {
      super(sources, poss);
      this.dimensions = dim;
      this.annotationsOnDimensions = null;
   }

   public ArrayQualifiedTypeReference(char[][] sources, int dim, Annotation[][] annotationsOnDimensions, long[] poss) {
      this(sources, dim, poss);
      this.annotationsOnDimensions = annotationsOnDimensions;
      if (annotationsOnDimensions != null) {
         this.bits |= 1048576;
      }
   }

   @Override
   public int dimensions() {
      return this.dimensions;
   }

   @Override
   public int extraDimensions() {
      return this.extendedDimensions;
   }

   @Override
   public Annotation[][] getAnnotationsOnDimensions(boolean useSourceOrder) {
      if (!useSourceOrder
         && this.annotationsOnDimensions != null
         && this.annotationsOnDimensions.length != 0
         && this.extendedDimensions != 0
         && this.extendedDimensions != this.dimensions) {
         Annotation[][] externalAnnotations = new Annotation[this.dimensions][];
         int baseDimensions = this.dimensions - this.extendedDimensions;
         System.arraycopy(this.annotationsOnDimensions, baseDimensions, externalAnnotations, 0, this.extendedDimensions);
         System.arraycopy(this.annotationsOnDimensions, 0, externalAnnotations, this.extendedDimensions, baseDimensions);
         return externalAnnotations;
      } else {
         return this.annotationsOnDimensions;
      }
   }

   @Override
   public void setAnnotationsOnDimensions(Annotation[][] annotationsOnDimensions) {
      this.annotationsOnDimensions = annotationsOnDimensions;
   }

   @Override
   public char[][] getParameterizedTypeName() {
      int dim = this.dimensions;
      char[] dimChars = new char[dim * 2];

      for(int i = 0; i < dim; ++i) {
         int index = i * 2;
         dimChars[index] = '[';
         dimChars[index + 1] = ']';
      }

      int length = this.tokens.length;
      char[][] qParamName = new char[length][];
      System.arraycopy(this.tokens, 0, qParamName, 0, length - 1);
      qParamName[length - 1] = CharOperation.concat(this.tokens[length - 1], dimChars);
      return qParamName;
   }

   @Override
   protected TypeBinding getTypeBinding(Scope scope) {
      if (this.resolvedType != null) {
         return this.resolvedType;
      } else {
         if (this.dimensions > 255) {
            scope.problemReporter().tooManyDimensions(this);
         }

         LookupEnvironment env = scope.environment();

         try {
            env.missingClassFileLocation = this;
            TypeBinding leafComponentType = super.getTypeBinding(scope);
            if (leafComponentType != null) {
               return this.resolvedType = scope.createArrayType(leafComponentType, this.dimensions);
            }
         } catch (AbortCompilation var8) {
            var8.updateContext(this, scope.referenceCompilationUnit().compilationResult);
            throw var8;
         } finally {
            env.missingClassFileLocation = null;
         }

         return null;
      }
   }

   @Override
   protected TypeBinding internalResolveType(Scope scope, int location) {
      return super.internalResolveType(scope, location);
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      super.printExpression(indent, output);
      if ((this.bits & 16384) != 0) {
         for(int i = 0; i < this.dimensions - 1; ++i) {
            if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[i] != null) {
               output.append(' ');
               printAnnotations(this.annotationsOnDimensions[i], output);
               output.append(' ');
            }

            output.append("[]");
         }

         if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[this.dimensions - 1] != null) {
            output.append(' ');
            printAnnotations(this.annotationsOnDimensions[this.dimensions - 1], output);
            output.append(' ');
         }

         output.append("...");
      } else {
         for(int i = 0; i < this.dimensions; ++i) {
            if (this.annotationsOnDimensions != null && this.annotationsOnDimensions[i] != null) {
               output.append(" ");
               printAnnotations(this.annotationsOnDimensions[i], output);
               output.append(" ");
            }

            output.append("[]");
         }
      }

      return output;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.annotations != null) {
            int annotationsLevels = this.annotations.length;

            for(int i = 0; i < annotationsLevels; ++i) {
               int annotationsLength = this.annotations[i] == null ? 0 : this.annotations[i].length;

               for(int j = 0; j < annotationsLength; ++j) {
                  this.annotations[i][j].traverse(visitor, scope);
               }
            }
         }

         if (this.annotationsOnDimensions != null) {
            int i = 0;

            for(int max = this.annotationsOnDimensions.length; i < max; ++i) {
               Annotation[] annotations2 = this.annotationsOnDimensions[i];
               int j = 0;

               for(int max2 = annotations2 == null ? 0 : annotations2.length; j < max2; ++j) {
                  Annotation annotation = annotations2[j];
                  annotation.traverse(visitor, scope);
               }
            }
         }
      }

      visitor.endVisit(this, scope);
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope scope) {
      if (visitor.visit(this, scope)) {
         if (this.annotations != null) {
            int annotationsLevels = this.annotations.length;

            for(int i = 0; i < annotationsLevels; ++i) {
               int annotationsLength = this.annotations[i] == null ? 0 : this.annotations[i].length;

               for(int j = 0; j < annotationsLength; ++j) {
                  this.annotations[i][j].traverse(visitor, scope);
               }
            }
         }

         if (this.annotationsOnDimensions != null) {
            int i = 0;

            for(int max = this.annotationsOnDimensions.length; i < max; ++i) {
               Annotation[] annotations2 = this.annotationsOnDimensions[i];
               int j = 0;

               for(int max2 = annotations2 == null ? 0 : annotations2.length; j < max2; ++j) {
                  Annotation annotation = annotations2[j];
                  annotation.traverse(visitor, scope);
               }
            }
         }
      }

      visitor.endVisit(this, scope);
   }
}
