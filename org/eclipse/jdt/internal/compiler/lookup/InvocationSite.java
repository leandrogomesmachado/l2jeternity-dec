package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ExpressionContext;

public interface InvocationSite {
   TypeBinding[] genericTypeArguments();

   boolean isSuperAccess();

   boolean isQualifiedSuper();

   boolean isTypeAccess();

   void setActualReceiverType(ReferenceBinding var1);

   void setDepth(int var1);

   void setFieldIndex(int var1);

   int sourceEnd();

   int sourceStart();

   TypeBinding invocationTargetType();

   boolean receiverIsImplicitThis();

   boolean checkingPotentialCompatibility();

   void acceptPotentiallyCompatibleMethods(MethodBinding[] var1);

   InferenceContext18 freshInferenceContext(Scope var1);

   ExpressionContext getExpressionContext();

   public static class EmptyWithAstNode implements InvocationSite {
      ASTNode node;

      public EmptyWithAstNode(ASTNode node) {
         this.node = node;
      }

      @Override
      public TypeBinding[] genericTypeArguments() {
         return null;
      }

      @Override
      public boolean isSuperAccess() {
         return false;
      }

      @Override
      public boolean isTypeAccess() {
         return false;
      }

      @Override
      public void setActualReceiverType(ReferenceBinding receiverType) {
      }

      @Override
      public void setDepth(int depth) {
      }

      @Override
      public void setFieldIndex(int depth) {
      }

      @Override
      public int sourceEnd() {
         return this.node.sourceEnd;
      }

      @Override
      public int sourceStart() {
         return this.node.sourceStart;
      }

      @Override
      public TypeBinding invocationTargetType() {
         return null;
      }

      @Override
      public boolean receiverIsImplicitThis() {
         return false;
      }

      @Override
      public InferenceContext18 freshInferenceContext(Scope scope) {
         return null;
      }

      @Override
      public ExpressionContext getExpressionContext() {
         return ExpressionContext.VANILLA_CONTEXT;
      }

      @Override
      public boolean isQualifiedSuper() {
         return false;
      }

      @Override
      public boolean checkingPotentialCompatibility() {
         return false;
      }

      @Override
      public void acceptPotentiallyCompatibleMethods(MethodBinding[] methods) {
      }
   }
}
