package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CaseStatement extends Statement {
   public Expression constantExpression;
   public BranchLabel targetLabel;

   public CaseStatement(Expression constantExpression, int sourceEnd, int sourceStart) {
      this.constantExpression = constantExpression;
      this.sourceEnd = sourceEnd;
      this.sourceStart = sourceStart;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      if (this.constantExpression != null) {
         if (this.constantExpression.constant == Constant.NotAConstant && !this.constantExpression.resolvedType.isEnum()) {
            currentScope.problemReporter().caseExpressionMustBeConstant(this.constantExpression);
         }

         this.constantExpression.analyseCode(currentScope, flowContext, flowInfo);
      }

      return flowInfo;
   }

   @Override
   public StringBuffer printStatement(int tab, StringBuffer output) {
      printIndent(tab, output);
      if (this.constantExpression == null) {
         output.append("default :");
      } else {
         output.append("case ");
         this.constantExpression.printExpression(0, output).append(" :");
      }

      return output;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
      if ((this.bits & -2147483648) != 0) {
         int pc = codeStream.position;
         this.targetLabel.place();
         codeStream.recordPositionsFrom(pc, this.sourceStart);
      }
   }

   @Override
   public void resolve(BlockScope scope) {
   }

   @Override
   public Constant resolveCase(BlockScope scope, TypeBinding switchExpressionType, SwitchStatement switchStatement) {
      scope.enclosingCase = this;
      if (this.constantExpression == null) {
         if (switchStatement.defaultCase != null) {
            scope.problemReporter().duplicateDefaultCase(this);
         }

         switchStatement.defaultCase = this;
         return Constant.NotAConstant;
      } else {
         switchStatement.cases[switchStatement.caseCount++] = this;
         if (switchExpressionType != null && switchExpressionType.isEnum() && this.constantExpression instanceof SingleNameReference) {
            ((SingleNameReference)this.constantExpression).setActualReceiverType((ReferenceBinding)switchExpressionType);
         }

         TypeBinding caseType = this.constantExpression.resolveType(scope);
         if (caseType != null && switchExpressionType != null) {
            if (!this.constantExpression.isConstantValueOfTypeAssignableToType(caseType, switchExpressionType)
               && !caseType.isCompatibleWith(switchExpressionType)) {
               if (this.isBoxingCompatible(caseType, switchExpressionType, this.constantExpression, scope)) {
                  return this.constantExpression.constant;
               }
            } else {
               if (!caseType.isEnum()) {
                  return this.constantExpression.constant;
               }

               if ((this.constantExpression.bits & 534773760) >> 21 != 0) {
                  scope.problemReporter().enumConstantsCannotBeSurroundedByParenthesis(this.constantExpression);
               }

               if (this.constantExpression instanceof NameReference && (this.constantExpression.bits & 7) == 1) {
                  NameReference reference = (NameReference)this.constantExpression;
                  FieldBinding field = reference.fieldBinding();
                  if ((field.modifiers & 16384) == 0) {
                     scope.problemReporter().enumSwitchCannotTargetField(reference, field);
                  } else if (reference instanceof QualifiedNameReference) {
                     scope.problemReporter().cannotUseQualifiedEnumConstantInCaseLabel(reference, field);
                  }

                  return IntConstant.fromValue(field.original().id + 1);
               }
            }

            scope.problemReporter().typeMismatchError(caseType, switchExpressionType, this.constantExpression, switchStatement.expression);
            return Constant.NotAConstant;
         } else {
            return Constant.NotAConstant;
         }
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      if (visitor.visit(this, blockScope) && this.constantExpression != null) {
         this.constantExpression.traverse(visitor, blockScope);
      }

      visitor.endVisit(this, blockScope);
   }
}
