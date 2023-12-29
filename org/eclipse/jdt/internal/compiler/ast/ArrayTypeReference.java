package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ArrayTypeReference extends SingleTypeReference {
   public int dimensions;
   private Annotation[][] annotationsOnDimensions;
   public int originalSourceEnd = this.sourceEnd;
   public int extendedDimensions;

   public ArrayTypeReference(char[] source, int dimensions, long pos) {
      super(source, pos);
      this.dimensions = dimensions;
      this.annotationsOnDimensions = null;
   }

   public ArrayTypeReference(char[] source, int dimensions, Annotation[][] annotationsOnDimensions, long pos) {
      this(source, dimensions, pos);
      if (annotationsOnDimensions != null) {
         this.bits |= 1048576;
      }

      this.annotationsOnDimensions = annotationsOnDimensions;
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

      return new char[][]{CharOperation.concat(this.token, dimChars)};
   }

   @Override
   protected TypeBinding getTypeBinding(Scope scope) {
      if (this.resolvedType != null) {
         return this.resolvedType;
      } else {
         if (this.dimensions > 255) {
            scope.problemReporter().tooManyDimensions(this);
         }

         TypeBinding leafComponentType = scope.getType(this.token);
         return scope.createArrayType(leafComponentType, this.dimensions);
      }
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
            Annotation[] typeAnnotations = this.annotations[0];
            int i = 0;

            for(int length = typeAnnotations == null ? 0 : typeAnnotations.length; i < length; ++i) {
               typeAnnotations[i].traverse(visitor, scope);
            }
         }

         if (this.annotationsOnDimensions != null) {
            int i = 0;

            for(int max = this.annotationsOnDimensions.length; i < max; ++i) {
               Annotation[] annotations2 = this.annotationsOnDimensions[i];
               if (annotations2 != null) {
                  int j = 0;

                  for(int max2 = annotations2.length; j < max2; ++j) {
                     Annotation annotation = annotations2[j];
                     annotation.traverse(visitor, scope);
                  }
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
            Annotation[] typeAnnotations = this.annotations[0];
            int i = 0;

            for(int length = typeAnnotations == null ? 0 : typeAnnotations.length; i < length; ++i) {
               typeAnnotations[i].traverse(visitor, scope);
            }
         }

         if (this.annotationsOnDimensions != null) {
            int i = 0;

            for(int max = this.annotationsOnDimensions.length; i < max; ++i) {
               Annotation[] annotations2 = this.annotationsOnDimensions[i];
               if (annotations2 != null) {
                  int j = 0;

                  for(int max2 = annotations2.length; j < max2; ++j) {
                     Annotation annotation = annotations2[j];
                     annotation.traverse(visitor, scope);
                  }
               }
            }
         }
      }

      visitor.endVisit(this, scope);
   }

   @Override
   protected TypeBinding internalResolveType(Scope scope, int location) {
      return super.internalResolveType(scope, location);
   }

   @Override
   public boolean hasNullTypeAnnotation(TypeReference.AnnotationPosition position) {
      switch(position) {
         case MAIN_TYPE:
            if (this.annotationsOnDimensions != null && this.annotationsOnDimensions.length > 0) {
               Annotation[] innerAnnotations = this.annotationsOnDimensions[0];
               return containsNullAnnotation(innerAnnotations);
            }
            break;
         case LEAF_TYPE:
            return super.hasNullTypeAnnotation(position);
         case ANY:
            if (super.hasNullTypeAnnotation(position)) {
               return true;
            }

            if (this.resolvedType != null && !this.resolvedType.hasNullTypeAnnotations()) {
               return false;
            }

            if (this.annotationsOnDimensions != null) {
               for(int i = 0; i < this.annotationsOnDimensions.length; ++i) {
                  Annotation[] innerAnnotations = this.annotationsOnDimensions[i];
                  if (containsNullAnnotation(innerAnnotations)) {
                     return true;
                  }
               }
            }
      }

      return false;
   }
}
