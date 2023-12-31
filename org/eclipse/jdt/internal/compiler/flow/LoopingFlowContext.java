package org.eclipse.jdt.internal.compiler.flow;

import java.util.ArrayList;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FakedTrackingVariable;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

public class LoopingFlowContext extends SwitchFlowContext {
   public BranchLabel continueLabel;
   public UnconditionalFlowInfo initsOnContinue = FlowInfo.DEAD_END;
   private UnconditionalFlowInfo upstreamNullFlowInfo;
   private LoopingFlowContext[] innerFlowContexts = null;
   private UnconditionalFlowInfo[] innerFlowInfos = null;
   private int innerFlowContextsCount = 0;
   private LabelFlowContext[] breakTargetContexts = null;
   private int breakTargetsCount = 0;
   Reference[] finalAssignments;
   VariableBinding[] finalVariables;
   int assignCount = 0;
   LocalVariableBinding[] nullLocals;
   ASTNode[] nullReferences;
   int[] nullCheckTypes;
   UnconditionalFlowInfo[] nullInfos;
   int nullCount;
   private ArrayList escapingExceptionCatchSites = null;
   Scope associatedScope;

   public LoopingFlowContext(
      FlowContext parent,
      FlowInfo upstreamNullFlowInfo,
      ASTNode associatedNode,
      BranchLabel breakLabel,
      BranchLabel continueLabel,
      Scope associatedScope,
      boolean isPreTest
   ) {
      super(parent, associatedNode, breakLabel, isPreTest);
      this.tagBits |= 2;
      this.continueLabel = continueLabel;
      this.associatedScope = associatedScope;
      this.upstreamNullFlowInfo = upstreamNullFlowInfo.unconditionalCopy();
   }

   public void complainOnDeferredFinalChecks(BlockScope scope, FlowInfo flowInfo) {
      for(int i = 0; i < this.assignCount; ++i) {
         VariableBinding variable = this.finalVariables[i];
         if (variable != null) {
            boolean complained = false;
            if (variable instanceof FieldBinding) {
               if (flowInfo.isPotentiallyAssigned((FieldBinding)variable)) {
                  complained = true;
                  scope.problemReporter().duplicateInitializationOfBlankFinalField((FieldBinding)variable, this.finalAssignments[i]);
               }
            } else if (flowInfo.isPotentiallyAssigned((LocalVariableBinding)variable)) {
               variable.tagBits &= -2049L;
               if (variable.isFinal()) {
                  complained = true;
                  scope.problemReporter().duplicateInitializationOfFinalLocal((LocalVariableBinding)variable, this.finalAssignments[i]);
               }
            }

            if (complained) {
               for(FlowContext context = this.getLocalParent(); context != null; context = context.getLocalParent()) {
                  context.removeFinalAssignmentIfAny(this.finalAssignments[i]);
               }
            }
         }
      }
   }

   public void complainOnDeferredNullChecks(BlockScope scope, FlowInfo callerFlowInfo) {
      this.complainOnDeferredNullChecks(scope, callerFlowInfo, true);
   }

   public void complainOnDeferredNullChecks(BlockScope scope, FlowInfo callerFlowInfo, boolean updateInitsOnBreak) {
      for(int i = 0; i < this.innerFlowContextsCount; ++i) {
         this.upstreamNullFlowInfo.addPotentialNullInfoFrom(this.innerFlowContexts[i].upstreamNullFlowInfo).addPotentialNullInfoFrom(this.innerFlowInfos[i]);
      }

      this.innerFlowContextsCount = 0;
      FlowInfo upstreamCopy = this.upstreamNullFlowInfo.copy();
      UnconditionalFlowInfo incomingInfo = this.upstreamNullFlowInfo.addPotentialNullInfoFrom(callerFlowInfo.unconditionalInitsWithoutSideEffect());
      if ((this.tagBits & 1) != 0) {
         for(int i = 0; i < this.nullCount; ++i) {
            LocalVariableBinding local;
            ASTNode location;
            FlowInfo flowInfo;
            local = this.nullLocals[i];
            location = this.nullReferences[i];
            flowInfo = (FlowInfo)(this.nullInfos[i] != null ? incomingInfo.copy().addNullInfoFrom(this.nullInfos[i]) : incomingInfo);
            label220:
            switch(this.nullCheckTypes[i] & -61441) {
               case 3:
                  if (flowInfo.isDefinitelyNull(local)) {
                     this.nullReferences[i] = null;
                     scope.problemReporter().localVariableNullReference(local, location);
                     continue;
                  }
                  break;
               case 16:
                  this.checkUnboxing(scope, (Expression)location, flowInfo);
                  continue;
               case 128:
                  int nullStatus = flowInfo.nullStatus(local);
                  if (nullStatus != 4) {
                     this.parent
                        .recordNullityMismatch(
                           scope, (Expression)location, this.providedExpectedTypes[i][0], this.providedExpectedTypes[i][1], flowInfo, nullStatus, null
                        );
                  }
                  continue;
               case 256:
               case 512:
                  if (flowInfo.isDefinitelyNonNull(local)) {
                     this.nullReferences[i] = null;
                     if ((this.nullCheckTypes[i] & -61441) == 512) {
                        if ((this.nullCheckTypes[i] & 4096) == 0) {
                           scope.problemReporter().localVariableRedundantCheckOnNonNull(local, location);
                        }
                     } else {
                        scope.problemReporter().localVariableNonNullComparedToNull(local, location);
                     }
                     continue;
                  }

                  if (flowInfo.isDefinitelyNull(local)) {
                     this.nullReferences[i] = null;
                     if ((this.nullCheckTypes[i] & -61441) == 256) {
                        if ((this.nullCheckTypes[i] & 4096) == 0) {
                           scope.problemReporter().localVariableRedundantCheckOnNull(local, location);
                        }
                     } else {
                        scope.problemReporter().localVariableNullComparedToNonNull(local, location);
                     }
                     continue;
                  }
                  break;
               case 257:
               case 513:
               case 769:
               case 1025:
                  Expression expression = (Expression)location;
                  if (flowInfo.isDefinitelyNull(local)) {
                     this.nullReferences[i] = null;
                     switch(this.nullCheckTypes[i] & -61696) {
                        case 256:
                           if ((this.nullCheckTypes[i] & 0xFF & -61441) == 1 && (expression.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariableNullReference(local, expression);
                           } else if ((this.nullCheckTypes[i] & 4096) == 0) {
                              scope.problemReporter().localVariableRedundantCheckOnNull(local, expression);
                           }
                           continue;
                        case 512:
                           if ((this.nullCheckTypes[i] & 0xFF & -61441) == 1 && (expression.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariableNullReference(local, expression);
                           } else {
                              scope.problemReporter().localVariableNullComparedToNonNull(local, expression);
                           }
                           continue;
                        case 768:
                           scope.problemReporter().localVariableRedundantNullAssignment(local, expression);
                           continue;
                        case 1024:
                           scope.problemReporter().localVariableNullInstanceof(local, expression);
                           continue;
                     }
                  } else if (flowInfo.isPotentiallyNull(local)) {
                     switch(this.nullCheckTypes[i] & -61696) {
                        case 256:
                           this.nullReferences[i] = null;
                           if ((this.nullCheckTypes[i] & 0xFF & -61441) == 1 && (expression.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariablePotentialNullReference(local, expression);
                              continue;
                           }
                           break label220;
                        case 512:
                           this.nullReferences[i] = null;
                           if ((this.nullCheckTypes[i] & 0xFF & -61441) == 1 && (expression.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariablePotentialNullReference(local, expression);
                              continue;
                           }
                     }
                  }
                  break;
               case 258:
               case 514:
                  if (flowInfo.isDefinitelyNonNull(local)) {
                     this.nullReferences[i] = null;
                     if ((this.nullCheckTypes[i] & -61441) == 514) {
                        if ((this.nullCheckTypes[i] & 4096) == 0) {
                           scope.problemReporter().localVariableRedundantCheckOnNonNull(local, location);
                        }
                     } else {
                        scope.problemReporter().localVariableNonNullComparedToNull(local, location);
                     }
                     continue;
                  }
                  break;
               case 2048:
                  FakedTrackingVariable trackingVar = local.closeTracker;
                  if (trackingVar != null) {
                     if (trackingVar.hasDefinitelyNoResource(flowInfo) || trackingVar.isClosedInFinallyOfEnclosing(scope)) {
                        continue;
                     }

                     if (this.parent.recordExitAgainstResource(scope, flowInfo, trackingVar, location)) {
                        this.nullReferences[i] = null;
                        continue;
                     }
                  }
            }

            if (this.nullCheckTypes[i] != 3 || !upstreamCopy.isDefinitelyNonNull(local)) {
               this.parent.recordUsingNullReference(scope, local, location, this.nullCheckTypes[i], flowInfo);
            }
         }
      } else {
         for(int i = 0; i < this.nullCount; ++i) {
            ASTNode location = this.nullReferences[i];
            LocalVariableBinding local = this.nullLocals[i];
            FlowInfo flowInfo = (FlowInfo)(this.nullInfos[i] != null ? incomingInfo.copy().addNullInfoFrom(this.nullInfos[i]) : incomingInfo);
            switch(this.nullCheckTypes[i] & -61441) {
               case 3:
                  if (flowInfo.isDefinitelyNull(local)) {
                     this.nullReferences[i] = null;
                     scope.problemReporter().localVariableNullReference(local, location);
                  } else if (flowInfo.isPotentiallyNull(local)) {
                     this.nullReferences[i] = null;
                     scope.problemReporter().localVariablePotentialNullReference(local, location);
                  }
                  break;
               case 16:
                  this.checkUnboxing(scope, (Expression)location, flowInfo);
                  break;
               case 128:
                  int nullStatus = flowInfo.nullStatus(local);
                  if (nullStatus != 4) {
                     char[][] annotationName = scope.environment().getNonNullAnnotationName();
                     scope.problemReporter()
                        .nullityMismatch((Expression)location, this.providedExpectedTypes[i][0], this.providedExpectedTypes[i][1], nullStatus, annotationName);
                  }
                  break;
               case 256:
               case 512:
                  if (flowInfo.isDefinitelyNonNull(local)) {
                     this.nullReferences[i] = null;
                     if ((this.nullCheckTypes[i] & -61441) == 512) {
                        if ((this.nullCheckTypes[i] & 4096) == 0) {
                           scope.problemReporter().localVariableRedundantCheckOnNonNull(local, location);
                        }
                     } else {
                        scope.problemReporter().localVariableNonNullComparedToNull(local, location);
                     }
                     break;
                  }
               case 257:
               case 513:
               case 769:
               case 1025:
                  Expression expression = (Expression)location;
                  if (flowInfo.isDefinitelyNull(local)) {
                     this.nullReferences[i] = null;
                     switch(this.nullCheckTypes[i] & -61696) {
                        case 256:
                           if ((this.nullCheckTypes[i] & 0xFF & -61441) == 1 && (expression.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariableNullReference(local, expression);
                           } else if ((this.nullCheckTypes[i] & 4096) == 0) {
                              scope.problemReporter().localVariableRedundantCheckOnNull(local, expression);
                           }
                           break;
                        case 512:
                           if ((this.nullCheckTypes[i] & 0xFF & -61441) == 1 && (expression.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariableNullReference(local, expression);
                           } else {
                              scope.problemReporter().localVariableNullComparedToNonNull(local, expression);
                           }
                           break;
                        case 768:
                           scope.problemReporter().localVariableRedundantNullAssignment(local, expression);
                           break;
                        case 1024:
                           scope.problemReporter().localVariableNullInstanceof(local, expression);
                     }
                  } else if (flowInfo.isPotentiallyNull(local)) {
                     switch(this.nullCheckTypes[i] & -61696) {
                        case 256:
                           this.nullReferences[i] = null;
                           if ((this.nullCheckTypes[i] & 0xFF & -61441) == 1 && (expression.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariablePotentialNullReference(local, expression);
                           }
                           break;
                        case 512:
                           this.nullReferences[i] = null;
                           if ((this.nullCheckTypes[i] & 0xFF & -61441) == 1 && (expression.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariablePotentialNullReference(local, expression);
                           }
                     }
                  }
                  break;
               case 2048:
                  int nullStatus = flowInfo.nullStatus(local);
                  if (nullStatus != 4) {
                     FakedTrackingVariable closeTracker = local.closeTracker;
                     if (closeTracker != null && !closeTracker.hasDefinitelyNoResource(flowInfo) && !closeTracker.isClosedInFinallyOfEnclosing(scope)) {
                        nullStatus = closeTracker.findMostSpecificStatus(flowInfo, scope, null);
                        closeTracker.recordErrorLocation(this.nullReferences[i], nullStatus);
                        closeTracker.reportRecordedErrors(scope, nullStatus, flowInfo.reachMode() != 0);
                        this.nullReferences[i] = null;
                     }
                  }
            }
         }
      }

      if (updateInitsOnBreak) {
         this.initsOnBreak.addPotentialNullInfoFrom(incomingInfo);

         for(int i = 0; i < this.breakTargetsCount; ++i) {
            this.breakTargetContexts[i].initsOnBreak.addPotentialNullInfoFrom(incomingInfo);
         }
      }
   }

   @Override
   public BranchLabel continueLabel() {
      return this.continueLabel;
   }

   @Override
   public String individualToString() {
      StringBuffer buffer = new StringBuffer("Looping flow context");
      buffer.append("[initsOnBreak - ").append(this.initsOnBreak.toString()).append(']');
      buffer.append("[initsOnContinue - ").append(this.initsOnContinue.toString()).append(']');
      buffer.append("[finalAssignments count - ").append(this.assignCount).append(']');
      buffer.append("[nullReferences count - ").append(this.nullCount).append(']');
      return buffer.toString();
   }

   @Override
   public boolean isContinuable() {
      return true;
   }

   public boolean isContinuedTo() {
      return this.initsOnContinue != FlowInfo.DEAD_END;
   }

   @Override
   public void recordBreakTo(FlowContext targetContext) {
      if (targetContext instanceof LabelFlowContext) {
         int current;
         if ((current = this.breakTargetsCount++) == 0) {
            this.breakTargetContexts = new LabelFlowContext[2];
         } else if (current == this.breakTargetContexts.length) {
            System.arraycopy(this.breakTargetContexts, 0, this.breakTargetContexts = new LabelFlowContext[current + 2], 0, current);
         }

         this.breakTargetContexts[current] = (LabelFlowContext)targetContext;
      }
   }

   @Override
   public void recordContinueFrom(FlowContext innerFlowContext, FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 1) == 0) {
         if ((this.initsOnContinue.tagBits & 1) == 0) {
            this.initsOnContinue = this.initsOnContinue.mergedWith(flowInfo.unconditionalInitsWithoutSideEffect());
         } else {
            this.initsOnContinue = flowInfo.unconditionalCopy();
         }

         FlowContext inner = innerFlowContext;

         while(inner != this && !(inner instanceof LoopingFlowContext)) {
            inner = inner.parent;
         }

         if (inner == this) {
            this.upstreamNullFlowInfo.addPotentialNullInfoFrom(flowInfo.unconditionalInitsWithoutSideEffect());
         } else {
            int length = 0;
            if (this.innerFlowContexts == null) {
               this.innerFlowContexts = new LoopingFlowContext[5];
               this.innerFlowInfos = new UnconditionalFlowInfo[5];
            } else if (this.innerFlowContextsCount == (length = this.innerFlowContexts.length) - 1) {
               System.arraycopy(this.innerFlowContexts, 0, this.innerFlowContexts = new LoopingFlowContext[length + 5], 0, length);
               System.arraycopy(this.innerFlowInfos, 0, this.innerFlowInfos = new UnconditionalFlowInfo[length + 5], 0, length);
            }

            this.innerFlowContexts[this.innerFlowContextsCount] = (LoopingFlowContext)inner;
            this.innerFlowInfos[this.innerFlowContextsCount++] = flowInfo.unconditionalInitsWithoutSideEffect();
         }
      }
   }

   @Override
   protected boolean recordFinalAssignment(VariableBinding binding, Reference finalAssignment) {
      if (binding instanceof LocalVariableBinding) {
         Scope scope = ((LocalVariableBinding)binding).declaringScope;

         while((scope = scope.parent) != null) {
            if (scope == this.associatedScope) {
               return false;
            }
         }
      }

      if (this.assignCount == 0) {
         this.finalAssignments = new Reference[5];
         this.finalVariables = new VariableBinding[5];
      } else {
         if (this.assignCount == this.finalAssignments.length) {
            System.arraycopy(this.finalAssignments, 0, this.finalAssignments = new Reference[this.assignCount * 2], 0, this.assignCount);
         }

         System.arraycopy(this.finalVariables, 0, this.finalVariables = new VariableBinding[this.assignCount * 2], 0, this.assignCount);
      }

      this.finalAssignments[this.assignCount] = finalAssignment;
      this.finalVariables[this.assignCount++] = binding;
      return true;
   }

   @Override
   protected void recordNullReference(LocalVariableBinding local, ASTNode expression, int checkType, FlowInfo nullInfo) {
      if (this.nullCount == 0) {
         this.nullLocals = new LocalVariableBinding[5];
         this.nullReferences = new ASTNode[5];
         this.nullCheckTypes = new int[5];
         this.nullInfos = new UnconditionalFlowInfo[5];
      } else if (this.nullCount == this.nullLocals.length) {
         System.arraycopy(this.nullLocals, 0, this.nullLocals = new LocalVariableBinding[this.nullCount * 2], 0, this.nullCount);
         System.arraycopy(this.nullReferences, 0, this.nullReferences = new ASTNode[this.nullCount * 2], 0, this.nullCount);
         System.arraycopy(this.nullCheckTypes, 0, this.nullCheckTypes = new int[this.nullCount * 2], 0, this.nullCount);
         System.arraycopy(this.nullInfos, 0, this.nullInfos = new UnconditionalFlowInfo[this.nullCount * 2], 0, this.nullCount);
      }

      this.nullLocals[this.nullCount] = local;
      this.nullReferences[this.nullCount] = expression;
      this.nullCheckTypes[this.nullCount] = checkType;
      this.nullInfos[this.nullCount++] = nullInfo != null ? nullInfo.unconditionalCopy() : null;
   }

   @Override
   public void recordUnboxing(Scope scope, Expression expression, int nullStatus, FlowInfo flowInfo) {
      if (nullStatus == 2) {
         super.recordUnboxing(scope, expression, nullStatus, flowInfo);
      } else {
         this.recordNullReference(null, expression, 16, flowInfo);
      }
   }

   @Override
   public boolean recordExitAgainstResource(BlockScope scope, FlowInfo flowInfo, FakedTrackingVariable trackingVar, ASTNode reference) {
      LocalVariableBinding local = trackingVar.binding;
      if (flowInfo.isDefinitelyNonNull(local)) {
         return false;
      } else if (flowInfo.isDefinitelyNull(local)) {
         scope.problemReporter().unclosedCloseable(trackingVar, reference);
         return true;
      } else if (flowInfo.isPotentiallyNull(local)) {
         scope.problemReporter().potentiallyUnclosedCloseable(trackingVar, reference);
         return true;
      } else {
         this.recordNullReference(trackingVar.binding, reference, 2048, flowInfo);
         return true;
      }
   }

   @Override
   public void recordUsingNullReference(Scope scope, LocalVariableBinding local, ASTNode location, int checkType, FlowInfo flowInfo) {
      if ((flowInfo.tagBits & 3) == 0 && !flowInfo.isDefinitelyUnknown(local)) {
         checkType |= this.tagBits & 4096;
         int checkTypeWithoutHideNullWarning = checkType & -61441;
         switch(checkTypeWithoutHideNullWarning) {
            case 3:
               if (flowInfo.isDefinitelyNonNull(local)) {
                  return;
               } else if (flowInfo.isDefinitelyNull(local)) {
                  scope.problemReporter().localVariableNullReference(local, location);
                  return;
               } else {
                  if (flowInfo.isPotentiallyNull(local)) {
                     scope.problemReporter().localVariablePotentialNullReference(local, location);
                     return;
                  }

                  this.recordNullReference(local, location, checkType, flowInfo);
                  return;
               }
            case 256:
            case 512:
               Expression reference = (Expression)location;
               if (flowInfo.isDefinitelyNonNull(local)) {
                  if (checkTypeWithoutHideNullWarning == 512) {
                     if ((this.tagBits & 4096) == 0) {
                        scope.problemReporter().localVariableRedundantCheckOnNonNull(local, reference);
                     }

                     flowInfo.initsWhenFalse().setReachMode(2);
                  } else {
                     scope.problemReporter().localVariableNonNullComparedToNull(local, reference);
                     flowInfo.initsWhenTrue().setReachMode(2);
                  }
               } else if (flowInfo.isDefinitelyNull(local)) {
                  if (checkTypeWithoutHideNullWarning == 256) {
                     if ((this.tagBits & 4096) == 0) {
                        scope.problemReporter().localVariableRedundantCheckOnNull(local, reference);
                     }

                     flowInfo.initsWhenFalse().setReachMode(2);
                  } else {
                     scope.problemReporter().localVariableNullComparedToNonNull(local, reference);
                     flowInfo.initsWhenTrue().setReachMode(2);
                  }
               } else if (this.upstreamNullFlowInfo.isDefinitelyNonNull(local) && !flowInfo.isPotentiallyNull(local) && !flowInfo.isPotentiallyUnknown(local)) {
                  this.recordNullReference(local, reference, checkType, flowInfo);
                  flowInfo.markAsDefinitelyNonNull(local);
               } else {
                  if (flowInfo.cannotBeDefinitelyNullOrNonNull(local)) {
                     return;
                  }

                  if (flowInfo.isPotentiallyNonNull(local)) {
                     this.recordNullReference(local, reference, 2 | checkType & -256, flowInfo);
                  } else if (flowInfo.isPotentiallyNull(local)) {
                     this.recordNullReference(local, reference, 1 | checkType & -256, flowInfo);
                  } else {
                     this.recordNullReference(local, reference, checkType, flowInfo);
                  }
               }

               return;
            case 257:
            case 513:
            case 769:
            case 1025:
               Expression reference = (Expression)location;
               if (!flowInfo.isPotentiallyNonNull(local) && !flowInfo.isPotentiallyUnknown(local) && !flowInfo.isProtectedNonNull(local)) {
                  if (flowInfo.isDefinitelyNull(local)) {
                     switch(checkTypeWithoutHideNullWarning & -61696) {
                        case 256:
                           if ((checkTypeWithoutHideNullWarning & 0xFF) == 1 && (reference.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariableNullReference(local, reference);
                              return;
                           }

                           if ((this.tagBits & 4096) == 0) {
                              scope.problemReporter().localVariableRedundantCheckOnNull(local, reference);
                           }

                           flowInfo.initsWhenFalse().setReachMode(2);
                           return;
                        case 512:
                           if ((checkTypeWithoutHideNullWarning & 0xFF) == 1 && (reference.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariableNullReference(local, reference);
                              return;
                           }

                           scope.problemReporter().localVariableNullComparedToNonNull(local, reference);
                           flowInfo.initsWhenTrue().setReachMode(2);
                           return;
                        case 768:
                           scope.problemReporter().localVariableRedundantNullAssignment(local, reference);
                           return;
                        case 1024:
                           scope.problemReporter().localVariableNullInstanceof(local, reference);
                           return;
                     }
                  } else if (flowInfo.isPotentiallyNull(local)) {
                     switch(checkTypeWithoutHideNullWarning & -61696) {
                        case 256:
                           if ((checkTypeWithoutHideNullWarning & 0xFF) == 1 && (reference.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariablePotentialNullReference(local, reference);
                              return;
                           }
                           break;
                        case 512:
                           if ((checkTypeWithoutHideNullWarning & 0xFF) == 1 && (reference.implicitConversion & 1024) != 0) {
                              scope.problemReporter().localVariablePotentialNullReference(local, reference);
                              return;
                           }
                     }
                  }

                  this.recordNullReference(local, reference, checkType, flowInfo);
                  return;
               }

               return;
         }
      }
   }

   @Override
   void removeFinalAssignmentIfAny(Reference reference) {
      for(int i = 0; i < this.assignCount; ++i) {
         if (this.finalAssignments[i] == reference) {
            this.finalAssignments[i] = null;
            this.finalVariables[i] = null;
            return;
         }
      }
   }

   public void simulateThrowAfterLoopBack(FlowInfo flowInfo) {
      if (this.escapingExceptionCatchSites != null) {
         int i = 0;

         for(int exceptionCount = this.escapingExceptionCatchSites.size(); i < exceptionCount; ++i) {
            ((LoopingFlowContext.EscapingExceptionCatchSite)this.escapingExceptionCatchSites.get(i)).simulateThrowAfterLoopBack(flowInfo);
         }

         this.escapingExceptionCatchSites = null;
      }
   }

   public void recordCatchContextOfEscapingException(ExceptionHandlingFlowContext catchingContext, ReferenceBinding caughtException, FlowInfo exceptionInfo) {
      if (this.escapingExceptionCatchSites == null) {
         this.escapingExceptionCatchSites = new ArrayList(5);
      }

      this.escapingExceptionCatchSites.add(new LoopingFlowContext.EscapingExceptionCatchSite(catchingContext, caughtException, exceptionInfo));
   }

   public boolean hasEscapingExceptions() {
      return this.escapingExceptionCatchSites != null;
   }

   @Override
   protected boolean internalRecordNullityMismatch(
      Expression expression, TypeBinding providedType, FlowInfo flowInfo, int nullStatus, TypeBinding expectedType, int checkType
   ) {
      this.recordProvidedExpectedTypes(providedType, expectedType, this.nullCount);
      this.recordNullReference(expression.localVariableBinding(), expression, checkType, flowInfo);
      return true;
   }

   private static class EscapingExceptionCatchSite {
      final ReferenceBinding caughtException;
      final ExceptionHandlingFlowContext catchingContext;
      final FlowInfo exceptionInfo;

      public EscapingExceptionCatchSite(ExceptionHandlingFlowContext catchingContext, ReferenceBinding caughtException, FlowInfo exceptionInfo) {
         this.catchingContext = catchingContext;
         this.caughtException = caughtException;
         this.exceptionInfo = exceptionInfo;
      }

      void simulateThrowAfterLoopBack(FlowInfo flowInfo) {
         this.catchingContext
            .recordHandlingException(
               this.caughtException, flowInfo.unconditionalCopy().addNullInfoFrom(this.exceptionInfo).unconditionalInits(), null, null, null, true
            );
      }
   }
}
