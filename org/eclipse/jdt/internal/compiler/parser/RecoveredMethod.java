package org.eclipse.jdt.internal.compiler.parser;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.Util;

public class RecoveredMethod extends RecoveredElement implements TerminalTokens {
   public AbstractMethodDeclaration methodDeclaration;
   public RecoveredAnnotation[] annotations;
   public int annotationCount;
   public int modifiers;
   public int modifiersStart;
   public RecoveredType[] localTypes;
   public int localTypeCount;
   public RecoveredBlock methodBody;
   public boolean discardBody = true;
   int pendingModifiers;
   int pendingModifersSourceStart = -1;
   RecoveredAnnotation[] pendingAnnotations;
   int pendingAnnotationCount;

   public RecoveredMethod(AbstractMethodDeclaration methodDeclaration, RecoveredElement parent, int bracketBalance, Parser parser) {
      super(parent, bracketBalance, parser);
      this.methodDeclaration = methodDeclaration;
      this.foundOpeningBrace = !this.bodyStartsAtHeaderEnd();
      if (this.foundOpeningBrace) {
         ++this.bracketBalance;
      }
   }

   @Override
   public RecoveredElement add(Block nestedBlockDeclaration, int bracketBalanceValue) {
      return this.add(nestedBlockDeclaration, bracketBalanceValue, false);
   }

   public RecoveredElement add(Block nestedBlockDeclaration, int bracketBalanceValue, boolean isArgument) {
      if (this.methodDeclaration.declarationSourceEnd > 0 && nestedBlockDeclaration.sourceStart > this.methodDeclaration.declarationSourceEnd) {
         this.resetPendingModifiers();
         return (RecoveredElement)(this.parent == null ? this : this.parent.add(nestedBlockDeclaration, bracketBalanceValue));
      } else {
         if (!this.foundOpeningBrace && !isArgument) {
            this.foundOpeningBrace = true;
            ++this.bracketBalance;
         }

         this.methodBody = new RecoveredBlock(nestedBlockDeclaration, this, bracketBalanceValue);
         return (RecoveredElement)(nestedBlockDeclaration.sourceEnd == 0 ? this.methodBody : this);
      }
   }

   @Override
   public RecoveredElement add(FieldDeclaration fieldDeclaration, int bracketBalanceValue) {
      this.resetPendingModifiers();
      char[][] fieldTypeName;
      if ((fieldDeclaration.modifiers & -17) != 0
         || fieldDeclaration.type == null
         || (fieldTypeName = fieldDeclaration.type.getTypeName()).length == 1 && CharOperation.equals(fieldTypeName[0], TypeBinding.VOID.sourceName())) {
         if (this.parent == null) {
            return this;
         } else {
            this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(fieldDeclaration.declarationSourceStart - 1));
            return this.parent.add(fieldDeclaration, bracketBalanceValue);
         }
      } else if (this.methodDeclaration.declarationSourceEnd > 0 && fieldDeclaration.declarationSourceStart > this.methodDeclaration.declarationSourceEnd) {
         return (RecoveredElement)(this.parent == null ? this : this.parent.add(fieldDeclaration, bracketBalanceValue));
      } else {
         if (!this.foundOpeningBrace) {
            this.foundOpeningBrace = true;
            ++this.bracketBalance;
         }

         return this;
      }
   }

   @Override
   public RecoveredElement add(LocalDeclaration localDeclaration, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.methodDeclaration.declarationSourceEnd != 0 && localDeclaration.declarationSourceStart > this.methodDeclaration.declarationSourceEnd) {
         return (RecoveredElement)(this.parent == null ? this : this.parent.add(localDeclaration, bracketBalanceValue));
      } else if (this.methodBody != null) {
         return this.methodBody.add(localDeclaration, bracketBalanceValue, true);
      } else {
         Block block = new Block(0);
         block.sourceStart = this.methodDeclaration.bodyStart;
         RecoveredElement currentBlock = this.add(block, 1, localDeclaration.isArgument());
         if (this.bracketBalance > 0) {
            for(int i = 0; i < this.bracketBalance - 1; ++i) {
               currentBlock = currentBlock.add(new Block(0), 1);
            }

            this.bracketBalance = 1;
         }

         return currentBlock.add(localDeclaration, bracketBalanceValue);
      }
   }

   @Override
   public RecoveredElement add(Statement statement, int bracketBalanceValue) {
      this.resetPendingModifiers();
      if (this.methodDeclaration.declarationSourceEnd != 0 && statement.sourceStart > this.methodDeclaration.declarationSourceEnd) {
         return (RecoveredElement)(this.parent == null ? this : this.parent.add(statement, bracketBalanceValue));
      } else if (this.methodBody != null) {
         return this.methodBody.add(statement, bracketBalanceValue, true);
      } else {
         Block block = new Block(0);
         block.sourceStart = this.methodDeclaration.bodyStart;
         RecoveredElement currentBlock = this.add(block, 1);
         if (this.bracketBalance > 0) {
            for(int i = 0; i < this.bracketBalance - 1; ++i) {
               currentBlock = currentBlock.add(new Block(0), 1);
            }

            this.bracketBalance = 1;
         }

         return currentBlock.add(statement, bracketBalanceValue);
      }
   }

   @Override
   public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalanceValue) {
      if (this.methodDeclaration.declarationSourceEnd != 0 && typeDeclaration.declarationSourceStart > this.methodDeclaration.declarationSourceEnd) {
         return (RecoveredElement)(this.parent == null ? this : this.parent.add(typeDeclaration, bracketBalanceValue));
      } else if ((typeDeclaration.bits & 256) == 0 && !this.parser().methodRecoveryActivated && !this.parser().statementRecoveryActivated) {
         switch(TypeDeclaration.kind(typeDeclaration.modifiers)) {
            case 2:
            case 4:
               this.resetPendingModifiers();
               this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(typeDeclaration.declarationSourceStart - 1));
               if (this.parent == null) {
                  return this;
               }

               return this.parent.add(typeDeclaration, bracketBalanceValue);
            case 3:
            default:
               if (this.localTypes == null) {
                  this.localTypes = new RecoveredType[5];
                  this.localTypeCount = 0;
               } else if (this.localTypeCount == this.localTypes.length) {
                  System.arraycopy(this.localTypes, 0, this.localTypes = new RecoveredType[2 * this.localTypeCount], 0, this.localTypeCount);
               }

               RecoveredType element = new RecoveredType(typeDeclaration, this, bracketBalanceValue);
               this.localTypes[this.localTypeCount++] = element;
               if (this.pendingAnnotationCount > 0) {
                  element.attach(this.pendingAnnotations, this.pendingAnnotationCount, this.pendingModifiers, this.pendingModifersSourceStart);
               }

               this.resetPendingModifiers();
               if (!this.foundOpeningBrace) {
                  this.foundOpeningBrace = true;
                  ++this.bracketBalance;
               }

               return element;
         }
      } else {
         if (this.methodBody == null) {
            Block block = new Block(0);
            block.sourceStart = this.methodDeclaration.bodyStart;
            this.add(block, 1);
         }

         this.methodBody.attachPendingModifiers(this.pendingAnnotations, this.pendingAnnotationCount, this.pendingModifiers, this.pendingModifersSourceStart);
         this.resetPendingModifiers();
         return this.methodBody.add(typeDeclaration, bracketBalanceValue, true);
      }
   }

   public boolean bodyStartsAtHeaderEnd() {
      return this.methodDeclaration.bodyStart == this.methodDeclaration.sourceEnd + 1;
   }

   @Override
   public ASTNode parseTree() {
      return this.methodDeclaration;
   }

   @Override
   public void resetPendingModifiers() {
      this.pendingAnnotations = null;
      this.pendingAnnotationCount = 0;
      this.pendingModifiers = 0;
      this.pendingModifersSourceStart = -1;
   }

   @Override
   public int sourceEnd() {
      return this.methodDeclaration.declarationSourceEnd;
   }

   @Override
   public String toString(int tab) {
      StringBuffer result = new StringBuffer(this.tabString(tab));
      result.append("Recovered method:\n");
      this.methodDeclaration.print(tab + 1, result);
      if (this.annotations != null) {
         for(int i = 0; i < this.annotationCount; ++i) {
            result.append("\n");
            result.append(this.annotations[i].toString(tab + 1));
         }
      }

      if (this.localTypes != null) {
         for(int i = 0; i < this.localTypeCount; ++i) {
            result.append("\n");
            result.append(this.localTypes[i].toString(tab + 1));
         }
      }

      if (this.methodBody != null) {
         result.append("\n");
         result.append(this.methodBody.toString(tab + 1));
      }

      return result.toString();
   }

   @Override
   public void updateBodyStart(int bodyStart) {
      this.foundOpeningBrace = true;
      this.methodDeclaration.bodyStart = bodyStart;
   }

   public AbstractMethodDeclaration updatedMethodDeclaration(int depth, Set<TypeDeclaration> knownTypes) {
      if (this.modifiers != 0) {
         this.methodDeclaration.modifiers |= this.modifiers;
         if (this.modifiersStart < this.methodDeclaration.declarationSourceStart) {
            this.methodDeclaration.declarationSourceStart = this.modifiersStart;
         }
      }

      if (this.annotationCount > 0) {
         int existingCount = this.methodDeclaration.annotations == null ? 0 : this.methodDeclaration.annotations.length;
         Annotation[] annotationReferences = new Annotation[existingCount + this.annotationCount];
         if (existingCount > 0) {
            System.arraycopy(this.methodDeclaration.annotations, 0, annotationReferences, this.annotationCount, existingCount);
         }

         for(int i = 0; i < this.annotationCount; ++i) {
            annotationReferences[i] = this.annotations[i].updatedAnnotationReference();
         }

         this.methodDeclaration.annotations = annotationReferences;
         int start = this.annotations[0].annotation.sourceStart;
         if (start < this.methodDeclaration.declarationSourceStart) {
            this.methodDeclaration.declarationSourceStart = start;
         }
      }

      if (this.methodBody != null) {
         Block block = this.methodBody.updatedBlock(depth, knownTypes);
         if (block != null) {
            this.methodDeclaration.statements = block.statements;
            if (this.methodDeclaration.declarationSourceEnd == 0) {
               this.methodDeclaration.declarationSourceEnd = block.sourceEnd;
               this.methodDeclaration.bodyEnd = block.sourceEnd;
            }

            if (this.methodDeclaration.isConstructor()) {
               ConstructorDeclaration constructor = (ConstructorDeclaration)this.methodDeclaration;
               if (this.methodDeclaration.statements != null && this.methodDeclaration.statements[0] instanceof ExplicitConstructorCall) {
                  constructor.constructorCall = (ExplicitConstructorCall)this.methodDeclaration.statements[0];
                  int length = this.methodDeclaration.statements.length;
                  System.arraycopy(this.methodDeclaration.statements, 1, this.methodDeclaration.statements = new Statement[length - 1], 0, length - 1);
               }

               if (constructor.constructorCall == null) {
                  constructor.constructorCall = SuperReference.implicitSuperConstructorCall();
               }
            }
         }
      } else if (this.methodDeclaration.declarationSourceEnd == 0) {
         if (this.methodDeclaration.sourceEnd + 1 == this.methodDeclaration.bodyStart) {
            this.methodDeclaration.declarationSourceEnd = this.methodDeclaration.sourceEnd;
            this.methodDeclaration.bodyStart = this.methodDeclaration.sourceEnd;
            this.methodDeclaration.bodyEnd = this.methodDeclaration.sourceEnd;
         } else {
            this.methodDeclaration.declarationSourceEnd = this.methodDeclaration.bodyStart;
            this.methodDeclaration.bodyEnd = this.methodDeclaration.bodyStart;
         }
      }

      if (this.localTypeCount > 0) {
         this.methodDeclaration.bits |= 2;
      }

      return this.methodDeclaration;
   }

   @Override
   public void updateFromParserState() {
      if (this.bodyStartsAtHeaderEnd() && this.parent != null) {
         Parser parser = this.parser();
         if (parser.listLength > 0 && parser.astLengthPtr > 0) {
            if (this.methodDeclaration.sourceEnd == parser.rParenPos) {
               int length = parser.astLengthStack[parser.astLengthPtr];
               int astPtr = parser.astPtr - length;
               boolean canConsume = astPtr >= 0;
               if (canConsume) {
                  if (!(parser.astStack[astPtr] instanceof AbstractMethodDeclaration)) {
                     canConsume = false;
                  }

                  int i = 1;

                  for(int max = length + 1; i < max; ++i) {
                     if (!(parser.astStack[astPtr + i] instanceof TypeReference)) {
                        canConsume = false;
                     }
                  }
               }

               if (canConsume) {
                  parser.consumeMethodHeaderThrowsClause();
               } else {
                  parser.listLength = 0;
               }
            } else {
               if (parser.currentToken == 24 || parser.currentToken == 28) {
                  parser.astLengthStack[parser.astLengthPtr]--;
                  --parser.astPtr;
                  --parser.listLength;
                  parser.currentToken = 0;
               }

               int argLength = parser.astLengthStack[parser.astLengthPtr];
               int argStart = parser.astPtr - argLength + 1;
               boolean needUpdateRParenPos = parser.rParenPos < parser.lParenPos;

               MemberValuePair[] memberValuePairs;
               for(memberValuePairs = null; argLength > 0 && parser.astStack[parser.astPtr] instanceof MemberValuePair; needUpdateRParenPos = true) {
                  System.arraycopy(parser.astStack, argStart, memberValuePairs = new MemberValuePair[argLength], 0, argLength);
                  --parser.astLengthPtr;
                  parser.astPtr -= argLength;
                  argLength = parser.astLengthStack[parser.astLengthPtr];
                  argStart = parser.astPtr - argLength + 1;
               }

               for(int count = 0; count < argLength; ++count) {
                  ASTNode aNode = parser.astStack[argStart + count];
                  if (!(aNode instanceof Argument)) {
                     parser.astLengthStack[parser.astLengthPtr] = count;
                     parser.astPtr = argStart + count - 1;
                     parser.listLength = count;
                     parser.currentToken = 0;
                     break;
                  }

                  Argument argument = (Argument)aNode;
                  char[][] argTypeName = argument.type.getTypeName();
                  if ((argument.modifiers & -17) != 0 || argTypeName.length == 1 && CharOperation.equals(argTypeName[0], TypeBinding.VOID.sourceName())) {
                     parser.astLengthStack[parser.astLengthPtr] = count;
                     parser.astPtr = argStart + count - 1;
                     parser.listLength = count;
                     parser.currentToken = 0;
                     break;
                  }

                  if (needUpdateRParenPos) {
                     parser.rParenPos = argument.sourceEnd + 1;
                  }
               }

               if (parser.listLength > 0 && parser.astLengthPtr > 0) {
                  int length = parser.astLengthStack[parser.astLengthPtr];
                  int astPtr = parser.astPtr - length;
                  boolean canConsume = astPtr >= 0;
                  if (canConsume) {
                     if (!(parser.astStack[astPtr] instanceof AbstractMethodDeclaration)) {
                        canConsume = false;
                     }

                     int i = 1;

                     for(int max = length + 1; i < max; ++i) {
                        if (!(parser.astStack[astPtr + i] instanceof Argument)) {
                           canConsume = false;
                        }
                     }
                  }

                  if (canConsume) {
                     parser.consumeMethodHeaderRightParen();
                     if (parser.currentElement == this) {
                        if (this.methodDeclaration.arguments != null) {
                           this.methodDeclaration.sourceEnd = this.methodDeclaration.arguments[this.methodDeclaration.arguments.length - 1].sourceEnd;
                        } else {
                           this.methodDeclaration.sourceEnd = this.methodDeclaration.receiver.sourceEnd;
                        }

                        this.methodDeclaration.bodyStart = this.methodDeclaration.sourceEnd + 1;
                        parser.lastCheckPoint = this.methodDeclaration.bodyStart;
                     }
                  }
               }

               if (memberValuePairs != null) {
                  System.arraycopy(memberValuePairs, 0, parser.astStack, parser.astPtr + 1, memberValuePairs.length);
                  parser.astPtr += memberValuePairs.length;
                  parser.astLengthStack[++parser.astLengthPtr] = memberValuePairs.length;
               }
            }
         }
      }
   }

   @Override
   public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd) {
      if (this.methodDeclaration.isAnnotationMethod()) {
         this.updateSourceEndIfNecessary(braceStart, braceEnd);
         return (RecoveredElement)(!this.foundOpeningBrace && this.parent != null ? this.parent.updateOnClosingBrace(braceStart, braceEnd) : this);
      } else {
         if (this.parent != null && this.parent instanceof RecoveredType) {
            int mods = ((RecoveredType)this.parent).typeDeclaration.modifiers;
            if (TypeDeclaration.kind(mods) == 2 && !this.foundOpeningBrace) {
               this.updateSourceEndIfNecessary(braceStart - 1, braceStart - 1);
               return this.parent.updateOnClosingBrace(braceStart, braceEnd);
            }
         }

         return super.updateOnClosingBrace(braceStart, braceEnd);
      }
   }

   @Override
   public RecoveredElement updateOnOpeningBrace(int braceStart, int braceEnd) {
      if (this.bracketBalance == 0) {
         switch(this.parser().lastIgnoredToken) {
            case -1:
            case 112:
               break;
            default:
               this.foundOpeningBrace = true;
               this.bracketBalance = 1;
         }
      }

      return super.updateOnOpeningBrace(braceStart, braceEnd);
   }

   @Override
   public void updateParseTree() {
      this.updatedMethodDeclaration(0, new HashSet<>());
   }

   @Override
   public void updateSourceEndIfNecessary(int braceStart, int braceEnd) {
      if (this.methodDeclaration.declarationSourceEnd == 0) {
         if (this.parser().rBraceSuccessorStart >= braceEnd) {
            this.methodDeclaration.declarationSourceEnd = this.parser().rBraceEnd;
            this.methodDeclaration.bodyEnd = this.parser().rBraceStart;
         } else {
            this.methodDeclaration.declarationSourceEnd = braceEnd;
            this.methodDeclaration.bodyEnd = braceStart - 1;
         }
      }
   }

   @Override
   public RecoveredElement addAnnotationName(int identifierPtr, int identifierLengthPtr, int annotationStart, int bracketBalanceValue) {
      if (this.pendingAnnotations == null) {
         this.pendingAnnotations = new RecoveredAnnotation[5];
         this.pendingAnnotationCount = 0;
      } else if (this.pendingAnnotationCount == this.pendingAnnotations.length) {
         System.arraycopy(
            this.pendingAnnotations, 0, this.pendingAnnotations = new RecoveredAnnotation[2 * this.pendingAnnotationCount], 0, this.pendingAnnotationCount
         );
      }

      RecoveredAnnotation element = new RecoveredAnnotation(identifierPtr, identifierLengthPtr, annotationStart, this, bracketBalanceValue);
      this.pendingAnnotations[this.pendingAnnotationCount++] = element;
      return element;
   }

   @Override
   public void addModifier(int flag, int modifiersSourceStart) {
      this.pendingModifiers |= flag;
      if (this.pendingModifersSourceStart < 0) {
         this.pendingModifersSourceStart = modifiersSourceStart;
      }
   }

   void attach(TypeParameter[] parameters, int startPos) {
      if (this.methodDeclaration.modifiers == 0) {
         int lastParameterEnd = parameters[parameters.length - 1].sourceEnd;
         Parser parser = this.parser();
         Scanner scanner = parser.scanner;
         if (Util.getLineNumber(this.methodDeclaration.declarationSourceStart, scanner.lineEnds, 0, scanner.linePtr)
            == Util.getLineNumber(lastParameterEnd, scanner.lineEnds, 0, scanner.linePtr)) {
            if (parser.modifiersSourceStart <= lastParameterEnd || parser.modifiersSourceStart >= this.methodDeclaration.declarationSourceStart) {
               if (this.methodDeclaration instanceof MethodDeclaration) {
                  ((MethodDeclaration)this.methodDeclaration).typeParameters = parameters;
                  this.methodDeclaration.declarationSourceStart = startPos;
               } else if (this.methodDeclaration instanceof ConstructorDeclaration) {
                  ((ConstructorDeclaration)this.methodDeclaration).typeParameters = parameters;
                  this.methodDeclaration.declarationSourceStart = startPos;
               }
            }
         }
      }
   }

   public void attach(RecoveredAnnotation[] annots, int annotCount, int mods, int modsSourceStart) {
      if (annotCount > 0) {
         Annotation[] existingAnnotations = this.methodDeclaration.annotations;
         if (existingAnnotations != null) {
            this.annotations = new RecoveredAnnotation[annotCount];
            this.annotationCount = 0;

            label33:
            for(int i = 0; i < annotCount; ++i) {
               for(int j = 0; j < existingAnnotations.length; ++j) {
                  if (annots[i].annotation == existingAnnotations[j]) {
                     continue label33;
                  }
               }

               this.annotations[this.annotationCount++] = annots[i];
            }
         } else {
            this.annotations = annots;
            this.annotationCount = annotCount;
         }
      }

      if (mods != 0) {
         this.modifiers = mods;
         this.modifiersStart = modsSourceStart;
      }
   }
}
