package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class AnnotationMethodDeclaration extends MethodDeclaration {
   public Expression defaultValue;
   public int extendedDimensions;

   public AnnotationMethodDeclaration(CompilationResult compilationResult) {
      super(compilationResult);
   }

   @Override
   public void generateCode(ClassFile classFile) {
      classFile.generateMethodInfoHeader(this.binding);
      int methodAttributeOffset = classFile.contentsOffset;
      int attributeNumber = classFile.generateMethodInfoAttributes(this.binding, this);
      classFile.completeMethodInfo(this.binding, methodAttributeOffset, attributeNumber);
   }

   @Override
   public boolean isAnnotationMethod() {
      return true;
   }

   @Override
   public boolean isMethod() {
      return false;
   }

   @Override
   public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
   }

   @Override
   public StringBuffer print(int tab, StringBuffer output) {
      printIndent(tab, output);
      printModifiers(this.modifiers, output);
      if (this.annotations != null) {
         printAnnotations(this.annotations, output);
         output.append(' ');
      }

      TypeParameter[] typeParams = this.typeParameters();
      if (typeParams != null) {
         output.append('<');
         int max = typeParams.length - 1;

         for(int j = 0; j < max; ++j) {
            typeParams[j].print(0, output);
            output.append(", ");
         }

         typeParams[max].print(0, output);
         output.append('>');
      }

      this.printReturnType(0, output).append(this.selector).append('(');
      if (this.arguments != null) {
         for(int i = 0; i < this.arguments.length; ++i) {
            if (i > 0) {
               output.append(", ");
            }

            this.arguments[i].print(0, output);
         }
      }

      output.append(')');
      if (this.thrownExceptions != null) {
         output.append(" throws ");

         for(int i = 0; i < this.thrownExceptions.length; ++i) {
            if (i > 0) {
               output.append(", ");
            }

            this.thrownExceptions[i].print(0, output);
         }
      }

      if (this.defaultValue != null) {
         output.append(" default ");
         this.defaultValue.print(0, output);
      }

      this.printBody(tab + 1, output);
      return output;
   }

   @Override
   public void resolveStatements() {
      super.resolveStatements();
      if (this.arguments != null) {
         this.scope.problemReporter().annotationMembersCannotHaveParameters(this);
      }

      if (this.typeParameters != null) {
         this.scope.problemReporter().annotationMembersCannotHaveTypeParameters(this);
      }

      if (this.extendedDimensions != 0) {
         this.scope.problemReporter().illegalExtendedDimensions(this);
      }

      if (this.binding != null) {
         TypeBinding returnTypeBinding = this.binding.returnType;
         if (returnTypeBinding != null) {
            label38: {
               TypeBinding leafReturnType = returnTypeBinding.leafComponentType();
               if (returnTypeBinding.dimensions() <= 1) {
                  switch(leafReturnType.erasure().id) {
                     case 2:
                     case 3:
                     case 4:
                     case 5:
                     case 7:
                     case 8:
                     case 9:
                     case 10:
                     case 11:
                     case 16:
                        break label38;
                     case 6:
                     case 12:
                     case 13:
                     case 14:
                     case 15:
                     default:
                        if (leafReturnType.isEnum() || leafReturnType.isAnnotationType()) {
                           break label38;
                        }
                  }
               }

               this.scope.problemReporter().invalidAnnotationMemberType(this);
            }

            if (this.defaultValue != null) {
               MemberValuePair pair = new MemberValuePair(this.selector, this.sourceStart, this.sourceEnd, this.defaultValue);
               pair.binding = this.binding;
               if (pair.value.resolvedType == null) {
                  pair.resolveTypeExpecting(this.scope, returnTypeBinding);
               }

               this.binding.setDefaultValue(ElementValuePair.getValue(this.defaultValue));
            } else {
               this.binding.setDefaultValue(null);
            }
         }
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope classScope) {
      if (visitor.visit(this, classScope)) {
         if (this.annotations != null) {
            int annotationsLength = this.annotations.length;

            for(int i = 0; i < annotationsLength; ++i) {
               this.annotations[i].traverse(visitor, this.scope);
            }
         }

         if (this.returnType != null) {
            this.returnType.traverse(visitor, this.scope);
         }

         if (this.defaultValue != null) {
            this.defaultValue.traverse(visitor, this.scope);
         }
      }

      visitor.endVisit(this, classScope);
   }
}
