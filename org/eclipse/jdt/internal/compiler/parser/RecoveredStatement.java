package org.eclipse.jdt.internal.compiler.parser;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public class RecoveredStatement extends RecoveredElement {
   public Statement statement;

   public RecoveredStatement(Statement statement, RecoveredElement parent, int bracketBalance) {
      super(parent, bracketBalance);
      this.statement = statement;
   }

   @Override
   public ASTNode parseTree() {
      return this.statement;
   }

   @Override
   public int sourceEnd() {
      return this.statement.sourceEnd;
   }

   @Override
   public String toString(int tab) {
      return this.tabString(tab) + "Recovered statement:\n" + this.statement.print(tab + 1, new StringBuffer(10));
   }

   public Statement updatedStatement(int depth, Set<TypeDeclaration> knownTypes) {
      return this.statement;
   }

   @Override
   public void updateParseTree() {
      this.updatedStatement(0, new HashSet<>());
   }

   @Override
   public void updateSourceEndIfNecessary(int bodyStart, int bodyEnd) {
      if (this.statement.sourceEnd == 0) {
         this.statement.sourceEnd = bodyEnd;
      }
   }

   @Override
   public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd) {
      if (--this.bracketBalance <= 0 && this.parent != null) {
         this.updateSourceEndIfNecessary(braceStart, braceEnd);
         return this.parent.updateOnClosingBrace(braceStart, braceEnd);
      } else {
         return this;
      }
   }
}
