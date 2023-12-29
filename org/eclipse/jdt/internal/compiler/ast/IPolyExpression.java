package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public interface IPolyExpression {
   void setExpressionContext(ExpressionContext var1);

   ExpressionContext getExpressionContext();

   void setExpectedType(TypeBinding var1);

   TypeBinding invocationTargetType();

   boolean isPotentiallyCompatibleWith(TypeBinding var1, Scope var2);

   boolean isCompatibleWith(TypeBinding var1, Scope var2);

   boolean isBoxingCompatibleWith(TypeBinding var1, Scope var2);

   boolean sIsMoreSpecific(TypeBinding var1, TypeBinding var2, Scope var3);

   boolean isPertinentToApplicability(TypeBinding var1, MethodBinding var2);

   boolean isPolyExpression(MethodBinding var1);

   boolean isPolyExpression();

   boolean isFunctionalType();

   Expression[] getPolyExpressions();

   TypeBinding resolveType(BlockScope var1);

   Expression resolveExpressionExpecting(TypeBinding var1, Scope var2, InferenceContext18 var3);
}
