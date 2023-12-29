package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;

public class Wildcard extends SingleTypeReference {
   public static final int UNBOUND = 0;
   public static final int EXTENDS = 1;
   public static final int SUPER = 2;
   public TypeReference bound;
   public int kind;

   public Wildcard(int kind) {
      super(WILDCARD_NAME, 0L);
      this.kind = kind;
   }

   @Override
   public char[][] getParameterizedTypeName() {
      switch(this.kind) {
         case 0:
            return new char[][]{WILDCARD_NAME};
         case 1:
            return new char[][]{CharOperation.concat(WILDCARD_NAME, WILDCARD_EXTENDS, CharOperation.concatWith(this.bound.getParameterizedTypeName(), '.'))};
         default:
            return new char[][]{CharOperation.concat(WILDCARD_NAME, WILDCARD_SUPER, CharOperation.concatWith(this.bound.getParameterizedTypeName(), '.'))};
      }
   }

   @Override
   public char[][] getTypeName() {
      switch(this.kind) {
         case 0:
            return new char[][]{WILDCARD_NAME};
         case 1:
            return new char[][]{CharOperation.concat(WILDCARD_NAME, WILDCARD_EXTENDS, CharOperation.concatWith(this.bound.getTypeName(), '.'))};
         default:
            return new char[][]{CharOperation.concat(WILDCARD_NAME, WILDCARD_SUPER, CharOperation.concatWith(this.bound.getTypeName(), '.'))};
      }
   }

   private TypeBinding internalResolveType(Scope scope, ReferenceBinding genericType, int rank) {
      TypeBinding boundType = null;
      if (this.bound != null) {
         boundType = scope.kind == 3 ? this.bound.resolveType((ClassScope)scope, 256) : this.bound.resolveType((BlockScope)scope, true, 256);
         this.bits |= this.bound.bits & 1048576;
         if (boundType == null) {
            return null;
         }
      }

      this.resolvedType = scope.environment().createWildcard(genericType, rank, boundType, null, this.kind);
      this.resolveAnnotations(scope, 0);
      if (scope.environment().usesNullTypeAnnotations()) {
         ((WildcardBinding)this.resolvedType).evaluateNullAnnotations(scope, this);
      }

      return this.resolvedType;
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      if (this.annotations != null && this.annotations[0] != null) {
         printAnnotations(this.annotations[0], output);
         output.append(' ');
      }

      switch(this.kind) {
         case 0:
            output.append(WILDCARD_NAME);
            break;
         case 1:
            output.append(WILDCARD_NAME).append(WILDCARD_EXTENDS);
            this.bound.printExpression(0, output);
            break;
         default:
            output.append(WILDCARD_NAME).append(WILDCARD_SUPER);
            this.bound.printExpression(0, output);
      }

      return output;
   }

   @Override
   public TypeBinding resolveType(BlockScope scope, boolean checkBounds, int location) {
      if (this.bound != null) {
         this.bound.resolveType(scope, checkBounds, 256);
         this.bits |= this.bound.bits & 1048576;
      }

      return null;
   }

   @Override
   public TypeBinding resolveType(ClassScope scope, int location) {
      if (this.bound != null) {
         this.bound.resolveType(scope, 256);
         this.bits |= this.bound.bits & 1048576;
      }

      return null;
   }

   @Override
   public TypeBinding resolveTypeArgument(BlockScope blockScope, ReferenceBinding genericType, int rank) {
      return this.internalResolveType(blockScope, genericType, rank);
   }

   @Override
   public TypeBinding resolveTypeArgument(ClassScope classScope, ReferenceBinding genericType, int rank) {
      return this.internalResolveType(classScope, genericType, rank);
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

         if (this.bound != null) {
            this.bound.traverse(visitor, scope);
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

         if (this.bound != null) {
            this.bound.traverse(visitor, scope);
         }
      }

      visitor.endVisit(this, scope);
   }

   @Override
   public boolean isWildcard() {
      return true;
   }
}
