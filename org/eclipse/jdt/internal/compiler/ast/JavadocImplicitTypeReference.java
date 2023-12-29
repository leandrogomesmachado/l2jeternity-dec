package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class JavadocImplicitTypeReference extends TypeReference {
   public char[] token;

   public JavadocImplicitTypeReference(char[] name, int pos) {
      this.token = name;
      this.sourceStart = pos;
      this.sourceEnd = pos;
   }

   @Override
   public TypeReference augmentTypeWithAdditionalDimensions(int additionalDimensions, Annotation[][] additionalAnnotations, boolean isVarargs) {
      return null;
   }

   @Override
   protected TypeBinding getTypeBinding(Scope scope) {
      this.constant = Constant.NotAConstant;
      return this.resolvedType = scope.enclosingReceiverType();
   }

   @Override
   public char[] getLastToken() {
      return this.token;
   }

   @Override
   public char[][] getTypeName() {
      return this.token != null ? new char[][]{this.token} : null;
   }

   @Override
   public boolean isThis() {
      return true;
   }

   @Override
   protected TypeBinding internalResolveType(Scope scope, int location) {
      this.constant = Constant.NotAConstant;
      if (this.resolvedType != null) {
         if (this.resolvedType.isValidBinding()) {
            return this.resolvedType;
         } else {
            switch(this.resolvedType.problemId()) {
               case 1:
               case 2:
                  TypeBinding type = this.resolvedType.closestMatch();
                  return type;
               default:
                  return null;
            }
         }
      } else {
         TypeBinding type = this.resolvedType = this.getTypeBinding(scope);
         if (type == null) {
            return null;
         } else {
            boolean hasError;
            if (hasError = !type.isValidBinding()) {
               this.reportInvalidType(scope);
               switch(type.problemId()) {
                  case 1:
                  case 2:
                     type = type.closestMatch();
                     if (type == null) {
                        return null;
                     }
                     break;
                  default:
                     return null;
               }
            }

            if (type.isArrayType() && ((ArrayBinding)type).leafComponentType == TypeBinding.VOID) {
               scope.problemReporter().cannotAllocateVoidArray(this);
               return null;
            } else {
               if (this.isTypeUseDeprecated(type, scope)) {
                  this.reportDeprecatedType(type, scope);
               }

               if (type.isGenericType() || type.isParameterizedType()) {
                  type = scope.environment().convertToRawType(type, true);
               }

               return hasError ? type : (this.resolvedType = type);
            }
         }
      }
   }

   @Override
   protected void reportInvalidType(Scope scope) {
      scope.problemReporter().javadocInvalidType(this, this.resolvedType, scope.getDeclarationModifiers());
   }

   @Override
   protected void reportDeprecatedType(TypeBinding type, Scope scope) {
      scope.problemReporter().javadocDeprecatedType(type, this, scope.getDeclarationModifiers());
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope scope) {
      visitor.visit(this, scope);
      visitor.endVisit(this, scope);
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      return new StringBuffer();
   }
}
