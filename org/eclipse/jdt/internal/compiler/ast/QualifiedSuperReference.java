package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class QualifiedSuperReference extends QualifiedThisReference {
   public QualifiedSuperReference(TypeReference name, int pos, int sourceEnd) {
      super(name, pos, sourceEnd);
   }

   @Override
   public boolean isSuper() {
      return true;
   }

   @Override
   public boolean isQualifiedSuper() {
      return true;
   }

   @Override
   public boolean isThis() {
      return false;
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      return this.qualification.print(0, output).append(".super");
   }

   @Override
   public TypeBinding resolveType(BlockScope scope) {
      if ((this.bits & 534773760) != 0) {
         scope.problemReporter().invalidParenthesizedExpression(this);
         return null;
      } else {
         super.resolveType(scope);
         if (this.resolvedType != null && !this.resolvedType.isValidBinding()) {
            scope.problemReporter().illegalSuperAccess(this.qualification.resolvedType, this.resolvedType, this);
            return null;
         } else if (this.currentCompatibleType == null) {
            return null;
         } else if (this.currentCompatibleType.id == 1) {
            scope.problemReporter().cannotUseSuperInJavaLangObject(this);
            return null;
         } else {
            return this.resolvedType = this.currentCompatibleType.isInterface() ? this.currentCompatibleType : this.currentCompatibleType.superclass();
         }
      }
   }

   @Override
   int findCompatibleEnclosing(ReferenceBinding enclosingType, TypeBinding type, BlockScope scope) {
      if (!type.isInterface()) {
         return super.findCompatibleEnclosing(enclosingType, type, scope);
      } else {
         CompilerOptions compilerOptions = scope.compilerOptions();
         ReferenceBinding[] supers = enclosingType.superInterfaces();
         int length = supers.length;
         boolean isJava8 = compilerOptions.complianceLevel >= 3407872L;
         boolean isLegal = true;
         char[][] compoundName = null;
         ReferenceBinding closestMatch = null;

         for(int i = 0; i < length; ++i) {
            if (TypeBinding.equalsEquals(supers[i].erasure(), type)) {
               this.currentCompatibleType = closestMatch = supers[i];
            } else if (supers[i].erasure().isCompatibleWith(type)) {
               isLegal = false;
               compoundName = supers[i].compoundName;
               if (closestMatch == null) {
                  closestMatch = supers[i];
               }
            }
         }

         if (!isLegal || !isJava8) {
            this.currentCompatibleType = null;
            this.resolvedType = new ProblemReferenceBinding(compoundName, closestMatch, isJava8 ? 21 : 29);
         }

         return 0;
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      if (visitor.visit(this, blockScope)) {
         this.qualification.traverse(visitor, blockScope);
      }

      visitor.endVisit(this, blockScope);
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope blockScope) {
      if (visitor.visit(this, blockScope)) {
         this.qualification.traverse(visitor, blockScope);
      }

      visitor.endVisit(this, blockScope);
   }
}
