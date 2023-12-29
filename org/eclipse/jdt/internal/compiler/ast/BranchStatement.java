package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public abstract class BranchStatement extends Statement {
   public char[] label;
   public BranchLabel targetLabel;
   public SubRoutineStatement[] subroutines;
   public int initStateIndex = -1;

   public BranchStatement(char[] label, int sourceStart, int sourceEnd) {
      this.label = label;
      this.sourceStart = sourceStart;
      this.sourceEnd = sourceEnd;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
      if ((this.bits & -2147483648) != 0) {
         int pc = codeStream.position;
         if (this.subroutines != null) {
            int i = 0;

            for(int max = this.subroutines.length; i < max; ++i) {
               SubRoutineStatement sub = this.subroutines[i];
               boolean didEscape = sub.generateSubRoutineInvocation(currentScope, codeStream, this.targetLabel, this.initStateIndex, null);
               if (didEscape) {
                  codeStream.recordPositionsFrom(pc, this.sourceStart);
                  SubRoutineStatement.reenterAllExceptionHandlers(this.subroutines, i, codeStream);
                  if (this.initStateIndex != -1) {
                     codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.initStateIndex);
                     codeStream.addDefinitelyAssignedVariables(currentScope, this.initStateIndex);
                  }

                  return;
               }
            }
         }

         codeStream.goto_(this.targetLabel);
         codeStream.recordPositionsFrom(pc, this.sourceStart);
         SubRoutineStatement.reenterAllExceptionHandlers(this.subroutines, -1, codeStream);
         if (this.initStateIndex != -1) {
            codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.initStateIndex);
            codeStream.addDefinitelyAssignedVariables(currentScope, this.initStateIndex);
         }
      }
   }

   @Override
   public void resolve(BlockScope scope) {
   }
}
