package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class JavadocQualifiedTypeReference extends QualifiedTypeReference {
   public int tagSourceStart;
   public int tagSourceEnd;
   public PackageBinding packageBinding;

   public JavadocQualifiedTypeReference(char[][] sources, long[] pos, int tagStart, int tagEnd) {
      super(sources, pos);
      this.tagSourceStart = tagStart;
      this.tagSourceEnd = tagEnd;
      this.bits |= 32768;
   }

   private TypeBinding internalResolveType(Scope scope, boolean checkBounds) {
      this.constant = Constant.NotAConstant;
      if (this.resolvedType != null) {
         return this.resolvedType.isValidBinding() ? this.resolvedType : this.resolvedType.closestMatch();
      } else {
         TypeBinding type = this.resolvedType = this.getTypeBinding(scope);
         if (type == null) {
            return null;
         } else if (!type.isValidBinding()) {
            Binding binding = scope.getTypeOrPackage(this.tokens);
            if (binding instanceof PackageBinding) {
               this.packageBinding = (PackageBinding)binding;
            } else {
               this.reportInvalidType(scope);
            }

            return null;
         } else {
            if (type.isGenericType() || type.isParameterizedType()) {
               this.resolvedType = scope.environment().convertToRawType(type, true);
            }

            return this.resolvedType;
         }
      }
   }

   @Override
   protected void reportDeprecatedType(TypeBinding type, Scope scope) {
      scope.problemReporter().javadocDeprecatedType(type, this, scope.getDeclarationModifiers());
   }

   @Override
   protected void reportDeprecatedType(TypeBinding type, Scope scope, int index) {
      scope.problemReporter().javadocDeprecatedType(type, this, scope.getDeclarationModifiers(), index);
   }

   @Override
   protected void reportInvalidType(Scope scope) {
      scope.problemReporter().javadocInvalidType(this, this.resolvedType, scope.getDeclarationModifiers());
   }

   @Override
   public TypeBinding resolveType(BlockScope blockScope, boolean checkBounds, int location) {
      return this.internalResolveType(blockScope, checkBounds);
   }

   @Override
   public TypeBinding resolveType(ClassScope classScope, int location) {
      return this.internalResolveType(classScope, false);
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
}
