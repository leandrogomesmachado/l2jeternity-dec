package org.eclipse.jdt.internal.compiler.ast;

import java.util.Arrays;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CaseLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.SwitchFlowContext;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SwitchStatement extends Statement {
   public Expression expression;
   public Statement[] statements;
   public BlockScope scope;
   public int explicitDeclarations;
   public BranchLabel breakLabel;
   public CaseStatement[] cases;
   public CaseStatement defaultCase;
   public int blockStart;
   public int caseCount;
   int[] constants;
   String[] stringConstants;
   public static final int CASE = 0;
   public static final int FALLTHROUGH = 1;
   public static final int ESCAPING = 2;
   private static final char[] SecretStringVariableName = " switchDispatchString".toCharArray();
   public SyntheticMethodBinding synthetic;
   int preSwitchInitStateIndex = -1;
   int mergedInitStateIndex = -1;
   CaseStatement[] duplicateCaseStatements = null;
   int duplicateCaseStatementsCounter = 0;
   private LocalVariableBinding dispatchStringCopy = null;

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      FlowInfo var14;
      try {
         flowInfo = this.expression.analyseCode(currentScope, flowContext, flowInfo);
         if ((this.expression.implicitConversion & 1024) != 0
            || this.expression.resolvedType != null && (this.expression.resolvedType.id == 11 || this.expression.resolvedType.isEnum())) {
            this.expression.checkNPE(currentScope, flowContext, flowInfo, 1);
         }

         SwitchFlowContext switchContext = new SwitchFlowContext(flowContext, this, this.breakLabel = new BranchLabel(), true);
         FlowInfo caseInits = FlowInfo.DEAD_END;
         this.preSwitchInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
         int caseIndex = 0;
         if (this.statements != null) {
            int initialComplaintLevel = (flowInfo.reachMode() & 3) != 0 ? 1 : 0;
            int complaintLevel = initialComplaintLevel;
            int fallThroughState = 0;
            int i = 0;

            for(int max = this.statements.length; i < max; ++i) {
               Statement statement = this.statements[i];
               if (caseIndex < this.caseCount && statement == this.cases[caseIndex]) {
                  this.scope.enclosingCase = this.cases[caseIndex];
                  ++caseIndex;
                  if (fallThroughState == 1 && (statement.bits & 536870912) == 0) {
                     this.scope.problemReporter().possibleFallThroughCase(this.scope.enclosingCase);
                  }

                  caseInits = caseInits.mergedWith(flowInfo.unconditionalInits());
                  complaintLevel = initialComplaintLevel;
                  fallThroughState = 0;
               } else if (statement == this.defaultCase) {
                  this.scope.enclosingCase = this.defaultCase;
                  if (fallThroughState == 1 && (statement.bits & 536870912) == 0) {
                     this.scope.problemReporter().possibleFallThroughCase(this.scope.enclosingCase);
                  }

                  caseInits = caseInits.mergedWith(flowInfo.unconditionalInits());
                  complaintLevel = initialComplaintLevel;
                  fallThroughState = 0;
               } else {
                  fallThroughState = 1;
               }

               if ((complaintLevel = statement.complainIfUnreachable(caseInits, this.scope, complaintLevel, true)) < 2) {
                  caseInits = statement.analyseCode(this.scope, switchContext, caseInits);
                  if (caseInits == FlowInfo.DEAD_END) {
                     fallThroughState = 2;
                  }

                  switchContext.expireNullCheckedFieldInfo();
               }
            }
         }

         TypeBinding resolvedTypeBinding = this.expression.resolvedType;
         if (resolvedTypeBinding.isEnum()) {
            SourceTypeBinding sourceTypeBinding = currentScope.classScope().referenceContext.binding;
            this.synthetic = sourceTypeBinding.addSyntheticMethodForSwitchEnum(resolvedTypeBinding);
         }

         if (this.defaultCase != null) {
            FlowInfo mergedInfo = caseInits.mergedWith(switchContext.initsOnBreak);
            this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(mergedInfo);
            return mergedInfo;
         }

         flowInfo.addPotentialInitializationsFrom(caseInits.mergedWith(switchContext.initsOnBreak));
         this.mergedInitStateIndex = currentScope.methodScope().recordInitializationStates(flowInfo);
         var14 = flowInfo;
      } finally {
         if (this.scope != null) {
            this.scope.enclosingCase = null;
         }
      }

      return var14;
   }

   public void generateCodeForStringSwitch(BlockScope currentScope, CodeStream codeStream) {
      try {
         if ((this.bits & -2147483648) != 0) {
            int pc = codeStream.position;
            boolean hasCases = this.caseCount != 0;

            class StringSwitchCase implements Comparable {
               int hashCode;
               String string;
               BranchLabel label;

               public StringSwitchCase(int hashCode, String string, BranchLabel label) {
                  this.hashCode = hashCode;
                  this.string = string;
                  this.label = label;
               }

               @Override
               public int compareTo(Object o) {
                  StringSwitchCase that = (StringSwitchCase)o;
                  if (this.hashCode == that.hashCode) {
                     return 0;
                  } else {
                     return this.hashCode > that.hashCode ? 1 : -1;
                  }
               }

               @Override
               public String toString() {
                  return "StringSwitchCase :\ncase " + this.hashCode + ":(" + this.string + ")\n";
               }
            }

            StringSwitchCase[] stringCases = new StringSwitchCase[this.caseCount];
            BranchLabel[] sourceCaseLabels = new BranchLabel[this.caseCount];
            CaseLabel[] hashCodeCaseLabels = new CaseLabel[this.caseCount];
            this.constants = new int[this.caseCount];
            int i = 0;

            for(int max = this.caseCount; i < max; ++i) {
               this.cases[i].targetLabel = sourceCaseLabels[i] = new BranchLabel(codeStream);
               sourceCaseLabels[i].tagBits |= 2;
               stringCases[i] = new StringSwitchCase(this.stringConstants[i].hashCode(), this.stringConstants[i], sourceCaseLabels[i]);
               hashCodeCaseLabels[i] = new CaseLabel(codeStream);
               hashCodeCaseLabels[i].tagBits |= 2;
            }

            Arrays.sort((Object[])stringCases);
            i = 0;
            int lastHashCode = 0;
            int ix = 0;

            for(int length = this.caseCount; ix < length; ++ix) {
               int hashCode = stringCases[ix].hashCode;
               if (ix == 0 || hashCode != lastHashCode) {
                  lastHashCode = this.constants[i++] = hashCode;
               }
            }

            if (i != this.caseCount) {
               System.arraycopy(this.constants, 0, this.constants = new int[i], 0, i);
               System.arraycopy(hashCodeCaseLabels, 0, hashCodeCaseLabels = new CaseLabel[i], 0, i);
            }

            int[] sortedIndexes = new int[i];
            int ixx = 0;

            while(ixx < i) {
               sortedIndexes[ixx] = ixx++;
            }

            CaseLabel defaultCaseLabel = new CaseLabel(codeStream);
            defaultCaseLabel.tagBits |= 2;
            this.breakLabel.initialize(codeStream);
            BranchLabel defaultBranchLabel = new BranchLabel(codeStream);
            if (hasCases) {
               defaultBranchLabel.tagBits |= 2;
            }

            if (this.defaultCase != null) {
               this.defaultCase.targetLabel = defaultBranchLabel;
            }

            this.expression.generateCode(currentScope, codeStream, true);
            codeStream.store(this.dispatchStringCopy, true);
            codeStream.addVariable(this.dispatchStringCopy);
            codeStream.invokeStringHashCode();
            if (hasCases) {
               codeStream.lookupswitch(defaultCaseLabel, this.constants, sortedIndexes, hashCodeCaseLabels);
               int ixxx = 0;
               int j = 0;

               for(int max = this.caseCount; ixxx < max; ++ixxx) {
                  int hashCode = stringCases[ixxx].hashCode;
                  if (ixxx == 0 || hashCode != lastHashCode) {
                     lastHashCode = hashCode;
                     if (ixxx != 0) {
                        codeStream.goto_(defaultBranchLabel);
                     }

                     hashCodeCaseLabels[j++].place();
                  }

                  codeStream.load(this.dispatchStringCopy);
                  codeStream.ldc(stringCases[ixxx].string);
                  codeStream.invokeStringEquals();
                  codeStream.ifne(stringCases[ixxx].label);
               }

               codeStream.goto_(defaultBranchLabel);
            } else {
               codeStream.pop();
            }

            int caseIndex = 0;
            if (this.statements != null) {
               int ixxx = 0;

               for(int maxCases = this.statements.length; ixxx < maxCases; ++ixxx) {
                  Statement statement = this.statements[ixxx];
                  if (caseIndex < this.caseCount && statement == this.cases[caseIndex]) {
                     this.scope.enclosingCase = this.cases[caseIndex];
                     if (this.preSwitchInitStateIndex != -1) {
                        codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
                     }

                     ++caseIndex;
                  } else if (statement == this.defaultCase) {
                     defaultCaseLabel.place();
                     this.scope.enclosingCase = this.defaultCase;
                     if (this.preSwitchInitStateIndex != -1) {
                        codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
                     }
                  }

                  statement.generateCode(this.scope, codeStream);
               }
            }

            if (this.mergedInitStateIndex != -1) {
               codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
               codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
            }

            codeStream.removeVariable(this.dispatchStringCopy);
            if (this.scope != currentScope) {
               codeStream.exitUserScope(this.scope);
            }

            this.breakLabel.place();
            if (this.defaultCase == null) {
               codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd, true);
               defaultCaseLabel.place();
               defaultBranchLabel.place();
            }

            codeStream.recordPositionsFrom(pc, this.sourceStart);
            return;
         }
      } finally {
         if (this.scope != null) {
            this.scope.enclosingCase = null;
         }
      }
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
      if (this.expression.resolvedType.id == 11) {
         this.generateCodeForStringSwitch(currentScope, codeStream);
      } else {
         try {
            if ((this.bits & -2147483648) != 0) {
               int pc = codeStream.position;
               this.breakLabel.initialize(codeStream);
               CaseLabel[] caseLabels = new CaseLabel[this.caseCount];
               int i = 0;

               for(int max = this.caseCount; i < max; ++i) {
                  this.cases[i].targetLabel = caseLabels[i] = new CaseLabel(codeStream);
                  caseLabels[i].tagBits |= 2;
               }

               CaseLabel defaultLabel = new CaseLabel(codeStream);
               boolean hasCases = this.caseCount != 0;
               if (hasCases) {
                  defaultLabel.tagBits |= 2;
               }

               if (this.defaultCase != null) {
                  this.defaultCase.targetLabel = defaultLabel;
               }

               TypeBinding resolvedType = this.expression.resolvedType;
               boolean valueRequired = false;
               if (resolvedType.isEnum()) {
                  codeStream.invoke((byte)-72, this.synthetic, null);
                  this.expression.generateCode(currentScope, codeStream, true);
                  codeStream.invokeEnumOrdinal(resolvedType.constantPoolName());
                  codeStream.iaload();
                  if (!hasCases) {
                     codeStream.pop();
                  }
               } else {
                  valueRequired = this.expression.constant == Constant.NotAConstant || hasCases;
                  this.expression.generateCode(currentScope, codeStream, valueRequired);
               }

               if (hasCases) {
                  int[] sortedIndexes = new int[this.caseCount];
                  int ix = 0;

                  while(ix < this.caseCount) {
                     sortedIndexes[ix] = ix++;
                  }

                  int[] localKeysCopy;
                  System.arraycopy(this.constants, 0, localKeysCopy = new int[this.caseCount], 0, this.caseCount);
                  CodeStream.sort(localKeysCopy, 0, this.caseCount - 1, sortedIndexes);
                  int max = localKeysCopy[this.caseCount - 1];
                  int min = localKeysCopy[0];
                  if ((long)((double)this.caseCount * 2.5) > (long)max - (long)min) {
                     if (max > 2147418112 && currentScope.compilerOptions().complianceLevel < 3145728L) {
                        codeStream.lookupswitch(defaultLabel, this.constants, sortedIndexes, caseLabels);
                     } else {
                        codeStream.tableswitch(defaultLabel, min, max, this.constants, sortedIndexes, caseLabels);
                     }
                  } else {
                     codeStream.lookupswitch(defaultLabel, this.constants, sortedIndexes, caseLabels);
                  }

                  codeStream.recordPositionsFrom(codeStream.position, this.expression.sourceEnd);
               } else if (valueRequired) {
                  codeStream.pop();
               }

               int caseIndex = 0;
               if (this.statements != null) {
                  int ix = 0;

                  for(int maxCases = this.statements.length; ix < maxCases; ++ix) {
                     Statement statement = this.statements[ix];
                     if (caseIndex < this.caseCount && statement == this.cases[caseIndex]) {
                        this.scope.enclosingCase = this.cases[caseIndex];
                        if (this.preSwitchInitStateIndex != -1) {
                           codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
                        }

                        ++caseIndex;
                     } else if (statement == this.defaultCase) {
                        this.scope.enclosingCase = this.defaultCase;
                        if (this.preSwitchInitStateIndex != -1) {
                           codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.preSwitchInitStateIndex);
                        }
                     }

                     statement.generateCode(this.scope, codeStream);
                  }
               }

               if (this.mergedInitStateIndex != -1) {
                  codeStream.removeNotDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
                  codeStream.addDefinitelyAssignedVariables(currentScope, this.mergedInitStateIndex);
               }

               if (this.scope != currentScope) {
                  codeStream.exitUserScope(this.scope);
               }

               this.breakLabel.place();
               if (this.defaultCase == null) {
                  codeStream.recordPositionsFrom(codeStream.position, this.sourceEnd, true);
                  defaultLabel.place();
               }

               codeStream.recordPositionsFrom(pc, this.sourceStart);
               return;
            }
         } finally {
            if (this.scope != null) {
               this.scope.enclosingCase = null;
            }
         }
      }
   }

   @Override
   public StringBuffer printStatement(int indent, StringBuffer output) {
      printIndent(indent, output).append("switch (");
      this.expression.printExpression(0, output).append(") {");
      if (this.statements != null) {
         for(int i = 0; i < this.statements.length; ++i) {
            output.append('\n');
            if (this.statements[i] instanceof CaseStatement) {
               this.statements[i].printStatement(indent, output);
            } else {
               this.statements[i].printStatement(indent + 2, output);
            }
         }
      }

      output.append("\n");
      return printIndent(indent, output).append('}');
   }

   @Override
   public void resolve(BlockScope upperScope) {
      try {
         boolean isEnumSwitch = false;
         boolean isStringSwitch = false;
         TypeBinding expressionType = this.expression.resolveType(upperScope);
         CompilerOptions compilerOptions = upperScope.compilerOptions();
         if (expressionType != null) {
            this.expression.computeConversion(upperScope, expressionType, expressionType);
            if (!expressionType.isValidBinding()) {
               expressionType = null;
            } else {
               label316: {
                  if (expressionType.isBaseType()) {
                     if (this.expression.isConstantValueOfTypeAssignableToType(expressionType, TypeBinding.INT)
                        || expressionType.isCompatibleWith(TypeBinding.INT)) {
                        break label316;
                     }
                  } else {
                     if (expressionType.isEnum()) {
                        isEnumSwitch = true;
                        if (compilerOptions.complianceLevel < 3211264L) {
                           upperScope.problemReporter().incorrectSwitchType(this.expression, expressionType);
                        }
                        break label316;
                     }

                     if (upperScope.isBoxingCompatibleWith(expressionType, TypeBinding.INT)) {
                        this.expression.computeConversion(upperScope, TypeBinding.INT, expressionType);
                        break label316;
                     }

                     if (compilerOptions.complianceLevel >= 3342336L && expressionType.id == 11) {
                        isStringSwitch = true;
                        break label316;
                     }
                  }

                  upperScope.problemReporter().incorrectSwitchType(this.expression, expressionType);
                  expressionType = null;
               }
            }
         }

         if (isStringSwitch) {
            this.dispatchStringCopy = new LocalVariableBinding(SecretStringVariableName, upperScope.getJavaLangString(), 0, false);
            upperScope.addLocalVariable(this.dispatchStringCopy);
            this.dispatchStringCopy.setConstant(Constant.NotAConstant);
            this.dispatchStringCopy.useFlag = 1;
         }

         if (this.statements == null) {
            if ((this.bits & 8) != 0) {
               upperScope.problemReporter().undocumentedEmptyBlock(this.blockStart, this.sourceEnd);
            }
         } else {
            this.scope = new BlockScope(upperScope);
            int length;
            this.cases = new CaseStatement[length = this.statements.length];
            if (!isStringSwitch) {
               this.constants = new int[length];
            } else {
               this.stringConstants = new String[length];
            }

            int counter = 0;

            for(int i = 0; i < length; ++i) {
               Statement statement = this.statements[i];
               Constant constant;
               if ((constant = statement.resolveCase(this.scope, expressionType, this)) != Constant.NotAConstant) {
                  if (!isStringSwitch) {
                     int key = constant.intValue();

                     for(int j = 0; j < counter; ++j) {
                        if (this.constants[j] == key) {
                           this.reportDuplicateCase((CaseStatement)statement, this.cases[j], length);
                        }
                     }

                     this.constants[counter++] = key;
                  } else {
                     String key = constant.stringValue();

                     for(int j = 0; j < counter; ++j) {
                        if (this.stringConstants[j].equals(key)) {
                           this.reportDuplicateCase((CaseStatement)statement, this.cases[j], length);
                        }
                     }

                     this.stringConstants[counter++] = key;
                  }
               }
            }

            if (length != counter) {
               if (!isStringSwitch) {
                  System.arraycopy(this.constants, 0, this.constants = new int[counter], 0, counter);
               } else {
                  System.arraycopy(this.stringConstants, 0, this.stringConstants = new String[counter], 0, counter);
               }
            }
         }

         if (this.defaultCase == null) {
            if (compilerOptions.getSeverity(1073774592) == 256) {
               if (isEnumSwitch) {
                  upperScope.methodScope().hasMissingSwitchDefault = true;
               }
            } else {
               upperScope.problemReporter().missingDefaultCase(this, isEnumSwitch, expressionType);
            }
         }

         if (isEnumSwitch && compilerOptions.complianceLevel >= 3211264L && (this.defaultCase == null || compilerOptions.reportMissingEnumCaseDespiteDefault)) {
            int constantCount = this.constants == null ? 0 : this.constants.length;
            if (constantCount == this.caseCount && this.caseCount != ((ReferenceBinding)expressionType).enumConstantCount()) {
               FieldBinding[] enumFields = ((ReferenceBinding)expressionType.erasure()).fields();
               int i = 0;

               label245:
               for(int max = enumFields.length; i < max; ++i) {
                  FieldBinding enumConstant = enumFields[i];
                  if ((enumConstant.modifiers & 16384) != 0) {
                     for(int j = 0; j < this.caseCount; ++j) {
                        if (enumConstant.id + 1 == this.constants[j]) {
                           continue label245;
                        }
                     }

                     boolean suppress = this.defaultCase != null && (this.defaultCase.bits & 1073741824) != 0;
                     if (!suppress) {
                        upperScope.problemReporter().missingEnumConstantCase(this, enumConstant);
                     }
                  }
               }
            }
         }
      } finally {
         if (this.scope != null) {
            this.scope.enclosingCase = null;
         }
      }
   }

   private void reportDuplicateCase(CaseStatement duplicate, CaseStatement original, int length) {
      if (this.duplicateCaseStatements == null) {
         this.scope.problemReporter().duplicateCase(original);
         this.scope.problemReporter().duplicateCase(duplicate);
         this.duplicateCaseStatements = new CaseStatement[length];
         this.duplicateCaseStatements[this.duplicateCaseStatementsCounter++] = original;
         this.duplicateCaseStatements[this.duplicateCaseStatementsCounter++] = duplicate;
      } else {
         boolean found = false;

         for(int k = 2; k < this.duplicateCaseStatementsCounter; ++k) {
            if (this.duplicateCaseStatements[k] == duplicate) {
               found = true;
               break;
            }
         }

         if (!found) {
            this.scope.problemReporter().duplicateCase(duplicate);
            this.duplicateCaseStatements[this.duplicateCaseStatementsCounter++] = duplicate;
         }
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      if (visitor.visit(this, blockScope)) {
         this.expression.traverse(visitor, blockScope);
         if (this.statements != null) {
            int statementsLength = this.statements.length;

            for(int i = 0; i < statementsLength; ++i) {
               this.statements[i].traverse(visitor, this.scope);
            }
         }
      }

      visitor.endVisit(this, blockScope);
   }

   @Override
   public void branchChainTo(BranchLabel label) {
      if (this.breakLabel.forwardReferenceCount() > 0) {
         label.becomeDelegateFor(this.breakLabel);
      }
   }

   @Override
   public boolean doesNotCompleteNormally() {
      if (this.statements != null && this.statements.length != 0) {
         int i = 0;

         for(int length = this.statements.length; i < length; ++i) {
            if (this.statements[i].breaksOut(null)) {
               return false;
            }
         }

         return this.statements[this.statements.length - 1].doesNotCompleteNormally();
      } else {
         return false;
      }
   }

   @Override
   public boolean completesByContinue() {
      if (this.statements != null && this.statements.length != 0) {
         int i = 0;

         for(int length = this.statements.length; i < length; ++i) {
            if (this.statements[i].completesByContinue()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
