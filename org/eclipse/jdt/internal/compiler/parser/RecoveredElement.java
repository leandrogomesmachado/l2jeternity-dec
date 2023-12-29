package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.util.Util;

public class RecoveredElement {
   public RecoveredElement parent;
   public int bracketBalance;
   public boolean foundOpeningBrace;
   protected Parser recoveringParser;
   public int lambdaNestLevel;

   public RecoveredElement(RecoveredElement parent, int bracketBalance) {
      this(parent, bracketBalance, null);
   }

   public RecoveredElement(RecoveredElement parent, int bracketBalance, Parser parser) {
      this.parent = parent;
      this.bracketBalance = bracketBalance;
      this.recoveringParser = parser;
   }

   public RecoveredElement addAnnotationName(int identifierPtr, int identifierLengthPtr, int annotationStart, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.parent == null) {
         return this;
      } else {
         this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(annotationStart - 1));
         return this.parent.addAnnotationName(identifierPtr, identifierLengthPtr, annotationStart, bracketBalanceValue);
      }
   }

   public RecoveredElement add(AbstractMethodDeclaration methodDeclaration, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.parent == null) {
         return this;
      } else {
         this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(methodDeclaration.declarationSourceStart - 1));
         return this.parent.add(methodDeclaration, bracketBalanceValue);
      }
   }

   public RecoveredElement add(Block nestedBlockDeclaration, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.parent == null) {
         return this;
      } else {
         this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(nestedBlockDeclaration.sourceStart - 1));
         return this.parent.add(nestedBlockDeclaration, bracketBalanceValue);
      }
   }

   public RecoveredElement add(FieldDeclaration fieldDeclaration, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.parent == null) {
         return this;
      } else {
         this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(fieldDeclaration.declarationSourceStart - 1));
         return this.parent.add(fieldDeclaration, bracketBalanceValue);
      }
   }

   public RecoveredElement add(ImportReference importReference, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.parent == null) {
         return this;
      } else {
         this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(importReference.declarationSourceStart - 1));
         return this.parent.add(importReference, bracketBalanceValue);
      }
   }

   public RecoveredElement add(LocalDeclaration localDeclaration, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.parent == null) {
         return this;
      } else {
         this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(localDeclaration.declarationSourceStart - 1));
         return this.parent.add(localDeclaration, bracketBalanceValue);
      }
   }

   public RecoveredElement add(Statement statement, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.parent == null) {
         return this;
      } else {
         if (this instanceof RecoveredType) {
            TypeDeclaration typeDeclaration = ((RecoveredType)this).typeDeclaration;
            if (typeDeclaration != null
               && (typeDeclaration.bits & 512) != 0
               && statement.sourceStart > typeDeclaration.sourceStart
               && statement.sourceEnd < typeDeclaration.sourceEnd) {
               return this;
            }
         }

         this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(statement.sourceStart - 1));
         return this.parent.add(statement, bracketBalanceValue);
      }
   }

   public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.parent == null) {
         return this;
      } else {
         this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(typeDeclaration.declarationSourceStart - 1));
         return this.parent.add(typeDeclaration, bracketBalanceValue);
      }
   }

   protected void addBlockStatement(RecoveredBlock recoveredBlock) {
      Block block = recoveredBlock.blockDeclaration;
      if (block.statements != null) {
         Statement[] statements = block.statements;

         for(int i = 0; i < statements.length; ++i) {
            recoveredBlock.add(statements[i], 0);
         }
      }
   }

   public void addModifier(int flag, int modifiersSourceStart) {
   }

   public int depth() {
      int depth = 0;
      RecoveredElement current = this;

      while((current = current.parent) != null) {
         ++depth;
      }

      return depth;
   }

   public RecoveredInitializer enclosingInitializer() {
      for(RecoveredElement current = this; current != null; current = current.parent) {
         if (current instanceof RecoveredInitializer) {
            return (RecoveredInitializer)current;
         }
      }

      return null;
   }

   public RecoveredMethod enclosingMethod() {
      for(RecoveredElement current = this; current != null; current = current.parent) {
         if (current instanceof RecoveredMethod) {
            return (RecoveredMethod)current;
         }
      }

      return null;
   }

   public RecoveredType enclosingType() {
      for(RecoveredElement current = this; current != null; current = current.parent) {
         if (current instanceof RecoveredType) {
            return (RecoveredType)current;
         }
      }

      return null;
   }

   public Parser parser() {
      for(RecoveredElement current = this; current != null; current = current.parent) {
         if (current.recoveringParser != null) {
            return current.recoveringParser;
         }
      }

      return null;
   }

   public ASTNode parseTree() {
      return null;
   }

   public void resetPendingModifiers() {
   }

   public void preserveEnclosingBlocks() {
      for(RecoveredElement current = this; current != null; current = current.parent) {
         if (current instanceof RecoveredBlock) {
            ((RecoveredBlock)current).preserveContent = true;
         }

         if (current instanceof RecoveredType) {
            ((RecoveredType)current).preserveContent = true;
         }
      }
   }

   public int previousAvailableLineEnd(int position) {
      Parser parser = this.parser();
      if (parser == null) {
         return position;
      } else {
         Scanner scanner = parser.scanner;
         if (scanner.lineEnds == null) {
            return position;
         } else {
            int index = Util.getLineNumber(position, scanner.lineEnds, 0, scanner.linePtr);
            if (index < 2) {
               return position;
            } else {
               int previousLineEnd = scanner.lineEnds[index - 2];
               char[] source = scanner.source;

               for(int i = previousLineEnd + 1; i < position; ++i) {
                  if (source[i] != ' ' && source[i] != '\t') {
                     return position;
                  }
               }

               return previousLineEnd;
            }
         }
      }
   }

   public int sourceEnd() {
      return 0;
   }

   protected String tabString(int tab) {
      StringBuffer result = new StringBuffer();

      for(int i = tab; i > 0; --i) {
         result.append("  ");
      }

      return result.toString();
   }

   public RecoveredElement topElement() {
      RecoveredElement current = this;

      while(current.parent != null) {
         current = current.parent;
      }

      return current;
   }

   @Override
   public String toString() {
      return this.toString(0);
   }

   public String toString(int tab) {
      return super.toString();
   }

   public RecoveredType type() {
      for(RecoveredElement current = this; current != null; current = current.parent) {
         if (current instanceof RecoveredType) {
            return (RecoveredType)current;
         }
      }

      return null;
   }

   public void updateBodyStart(int bodyStart) {
      this.foundOpeningBrace = true;
   }

   public void updateFromParserState() {
   }

   public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd) {
      if (--this.bracketBalance <= 0 && this.parent != null) {
         this.updateSourceEndIfNecessary(braceStart, braceEnd);
         return this.parent;
      } else {
         return this;
      }
   }

   public RecoveredElement updateOnOpeningBrace(int braceStart, int braceEnd) {
      if (this.bracketBalance++ == 0) {
         this.updateBodyStart(braceEnd + 1);
         return this;
      } else {
         return null;
      }
   }

   public void updateParseTree() {
   }

   public void updateSourceEndIfNecessary(int braceStart, int braceEnd) {
   }

   public void updateSourceEndIfNecessary(int sourceEnd) {
      this.updateSourceEndIfNecessary(sourceEnd + 1, sourceEnd);
   }
}
