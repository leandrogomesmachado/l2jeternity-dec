package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

public class Block extends Statement {
   public Statement[] statements;
   public int explicitDeclarations;
   public BlockScope scope;

   public Block(int explicitDeclarations) {
      this.explicitDeclarations = explicitDeclarations;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      if (this.statements == null) {
         return flowInfo;
      } else {
         int complaintLevel = (flowInfo.reachMode() & 3) != 0 ? 1 : 0;
         boolean enableSyntacticNullAnalysisForFields = currentScope.compilerOptions().enableSyntacticNullAnalysisForFields;
         int i = 0;

         for(int max = this.statements.length; i < max; ++i) {
            Statement stat = this.statements[i];
            if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel, true)) < 2) {
               flowInfo = stat.analyseCode(this.scope, flowContext, flowInfo);
            }

            flowContext.mergeFinallyNullInfo(flowInfo);
            if (enableSyntacticNullAnalysisForFields) {
               flowContext.expireNullCheckedFieldInfo();
            }
         }

         if (this.scope != currentScope) {
            this.scope.checkUnclosedCloseables(flowInfo, flowContext, null, null);
         }

         if (this.explicitDeclarations > 0) {
            LocalVariableBinding[] locals = this.scope.locals;
            if (locals != null) {
               int numLocals = this.scope.localIndex;

               for(int ix = 0; ix < numLocals; ++ix) {
                  flowInfo.resetAssignmentInfo(locals[ix]);
               }
            }
         }

         return flowInfo;
      }
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
      if ((this.bits & -2147483648) != 0) {
         int pc = codeStream.position;
         if (this.statements != null) {
            int i = 0;

            for(int max = this.statements.length; i < max; ++i) {
               this.statements[i].generateCode(this.scope, codeStream);
            }
         }

         if (this.scope != currentScope) {
            codeStream.exitUserScope(this.scope);
         }

         codeStream.recordPositionsFrom(pc, this.sourceStart);
      }
   }

   @Override
   public boolean isEmptyBlock() {
      return this.statements == null;
   }

   public StringBuffer printBody(int indent, StringBuffer output) {
      if (this.statements == null) {
         return output;
      } else {
         for(int i = 0; i < this.statements.length; ++i) {
            this.statements[i].printStatement(indent + 1, output);
            output.append('\n');
         }

         return output;
      }
   }

   @Override
   public StringBuffer printStatement(int indent, StringBuffer output) {
      printIndent(indent, output);
      output.append("{\n");
      this.printBody(indent, output);
      return printIndent(indent, output).append('}');
   }

   @Override
   public void resolve(BlockScope upperScope) {
      if ((this.bits & 8) != 0) {
         upperScope.problemReporter().undocumentedEmptyBlock(this.sourceStart, this.sourceEnd);
      }

      if (this.statements != null) {
         this.scope = this.explicitDeclarations == 0 ? upperScope : new BlockScope(upperScope, this.explicitDeclarations);
         int i = 0;

         for(int length = this.statements.length; i < length; ++i) {
            this.statements[i].resolve(this.scope);
         }
      }
   }

   public void resolveUsing(BlockScope givenScope) {
      if ((this.bits & 8) != 0) {
         givenScope.problemReporter().undocumentedEmptyBlock(this.sourceStart, this.sourceEnd);
      }

      this.scope = givenScope;
      if (this.statements != null) {
         int i = 0;

         for(int length = this.statements.length; i < length; ++i) {
            this.statements[i].resolve(this.scope);
         }
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      if (visitor.visit(this, blockScope) && this.statements != null) {
         int i = 0;

         for(int length = this.statements.length; i < length; ++i) {
            this.statements[i].traverse(visitor, this.scope);
         }
      }

      visitor.endVisit(this, blockScope);
   }

   @Override
   public void branchChainTo(BranchLabel label) {
      if (this.statements != null) {
         this.statements[this.statements.length - 1].branchChainTo(label);
      }
   }

   @Override
   public boolean doesNotCompleteNormally() {
      int length = this.statements == null ? 0 : this.statements.length;
      return length > 0 && this.statements[length - 1].doesNotCompleteNormally();
   }

   @Override
   public boolean completesByContinue() {
      int length = this.statements == null ? 0 : this.statements.length;
      return length > 0 && this.statements[length - 1].completesByContinue();
   }
}
