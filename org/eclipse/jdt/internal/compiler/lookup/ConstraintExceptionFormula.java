package org.eclipse.jdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.ast.FunctionalExpression;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.ReferenceExpression;

public class ConstraintExceptionFormula extends ConstraintFormula {
   FunctionalExpression left;

   public ConstraintExceptionFormula(FunctionalExpression left, TypeBinding type) {
      this.left = left;
      this.right = type;
      this.relation = 7;
   }

   @Override
   public Object reduce(InferenceContext18 inferenceContext) {
      Scope scope = inferenceContext.scope;
      if (!this.right.isFunctionalInterface(scope)) {
         return FALSE;
      } else {
         MethodBinding sam = this.right.getSingleAbstractMethod(scope, true);
         if (sam == null) {
            return FALSE;
         } else {
            if (this.left instanceof LambdaExpression) {
               if (((LambdaExpression)this.left).argumentsTypeElided()) {
                  int nParam = sam.parameters.length;

                  for(int i = 0; i < nParam; ++i) {
                     if (!sam.parameters[i].isProperType(true)) {
                        return FALSE;
                     }
                  }
               }

               if (sam.returnType != TypeBinding.VOID && !sam.returnType.isProperType(true)) {
                  return FALSE;
               }
            } else if (!((ReferenceExpression)this.left).isExactMethodReference()) {
               int nParam = sam.parameters.length;

               for(int i = 0; i < nParam; ++i) {
                  if (!sam.parameters[i].isProperType(true)) {
                     return FALSE;
                  }
               }

               if (sam.returnType != TypeBinding.VOID && !sam.returnType.isProperType(true)) {
                  return FALSE;
               }
            }

            TypeBinding[] thrown = sam.thrownExceptions;
            InferenceVariable[] e = new InferenceVariable[thrown.length];
            int n = 0;

            for(int i = 0; i < thrown.length; ++i) {
               if (!thrown[i].isProperType(true)) {
                  e[n++] = (InferenceVariable)thrown[i];
               }
            }

            if (n == 0) {
               return TRUE;
            } else {
               TypeBinding[] ePrime = null;
               if (this.left instanceof LambdaExpression) {
                  LambdaExpression lambda = ((LambdaExpression)this.left).resolveExpressionExpecting(this.right, inferenceContext.scope, inferenceContext);
                  if (lambda == null) {
                     return TRUE;
                  }

                  Set<TypeBinding> ePrimeSet = lambda.getThrownExceptions();
                  ePrime = ePrimeSet.toArray(new TypeBinding[ePrimeSet.size()]);
               } else {
                  ReferenceExpression referenceExpression = ((ReferenceExpression)this.left).resolveExpressionExpecting(this.right, scope, inferenceContext);
                  MethodBinding method = referenceExpression != null ? referenceExpression.binding : null;
                  if (method != null) {
                     ePrime = method.thrownExceptions;
                  }
               }

               if (ePrime == null) {
                  return TRUE;
               } else {
                  int m = ePrime.length;
                  List<ConstraintFormula> result = new ArrayList<>();

                  label108:
                  for(int i = 0; i < m; ++i) {
                     if (!ePrime[i].isUncheckedException(false)) {
                        for(int j = 0; j < thrown.length; ++j) {
                           if (thrown[j].isProperType(true) && ePrime[i].isCompatibleWith(thrown[j])) {
                              continue label108;
                           }
                        }

                        for(int j = 0; j < n; ++j) {
                           result.add(ConstraintTypeFormula.create(ePrime[i], e[j], 2));
                        }
                     }
                  }

                  for(int j = 0; j < n; ++j) {
                     inferenceContext.currentBounds.inThrows.add(e[j].prototype());
                  }

                  return result.toArray(new ConstraintFormula[result.size()]);
               }
            }
         }
      }
   }

   @Override
   Collection<InferenceVariable> inputVariables(InferenceContext18 context) {
      if (this.left instanceof LambdaExpression) {
         if (this.right instanceof InferenceVariable) {
            return Collections.singletonList((InferenceVariable)this.right);
         }

         if (this.right.isFunctionalInterface(context.scope)) {
            LambdaExpression lambda = (LambdaExpression)this.left;
            MethodBinding sam = this.right.getSingleAbstractMethod(context.scope, true);
            Set<InferenceVariable> variables = new HashSet<>();
            if (lambda.argumentsTypeElided()) {
               int len = sam.parameters.length;

               for(int i = 0; i < len; ++i) {
                  sam.parameters[i].collectInferenceVariables(variables);
               }
            }

            if (sam.returnType != TypeBinding.VOID) {
               sam.returnType.collectInferenceVariables(variables);
            }

            return variables;
         }
      } else if (this.left instanceof ReferenceExpression) {
         if (this.right instanceof InferenceVariable) {
            return Collections.singletonList((InferenceVariable)this.right);
         }

         if (this.right.isFunctionalInterface(context.scope)) {
            MethodBinding sam = this.right.getSingleAbstractMethod(context.scope, true);
            Set<InferenceVariable> variables = new HashSet<>();
            int len = sam.parameters.length;

            for(int i = 0; i < len; ++i) {
               sam.parameters[i].collectInferenceVariables(variables);
            }

            sam.returnType.collectInferenceVariables(variables);
            return variables;
         }
      }

      return EMPTY_VARIABLE_LIST;
   }

   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer().append('⟨');
      this.left.printExpression(4, buf);
      buf.append(" ⊆throws ");
      this.appendTypeName(buf, this.right);
      buf.append('⟩');
      return buf.toString();
   }
}
