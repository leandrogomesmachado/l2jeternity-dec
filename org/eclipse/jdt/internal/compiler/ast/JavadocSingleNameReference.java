package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

public class JavadocSingleNameReference extends SingleNameReference {
   public int tagSourceStart;
   public int tagSourceEnd;

   public JavadocSingleNameReference(char[] source, long pos, int tagStart, int tagEnd) {
      super(source, pos);
      this.tagSourceStart = tagStart;
      this.tagSourceEnd = tagEnd;
      this.bits |= 32768;
   }

   @Override
   public void resolve(BlockScope scope) {
      this.resolve(scope, true, scope.compilerOptions().reportUnusedParameterIncludeDocCommentReference);
   }

   public void resolve(BlockScope scope, boolean warn, boolean considerParamRefAsUsage) {
      LocalVariableBinding variableBinding = scope.findVariable(this.token);
      if (variableBinding != null && variableBinding.isValidBinding() && (variableBinding.tagBits & 1024L) != 0L) {
         this.binding = variableBinding;
         if (considerParamRefAsUsage) {
            variableBinding.useFlag = 1;
         }
      } else {
         if (warn) {
            try {
               MethodScope methScope = (MethodScope)scope;
               scope.problemReporter().javadocUndeclaredParamTagName(this.token, this.sourceStart, this.sourceEnd, methScope.referenceMethod().modifiers);
            } catch (Exception var6) {
               scope.problemReporter().javadocUndeclaredParamTagName(this.token, this.sourceStart, this.sourceEnd, -1);
            }
         }
      }
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
