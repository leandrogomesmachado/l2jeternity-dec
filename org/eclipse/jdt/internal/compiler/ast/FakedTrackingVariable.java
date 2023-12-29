package org.eclipse.jdt.internal.compiler.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FinallyFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;

public class FakedTrackingVariable extends LocalDeclaration {
   private static final char[] UNASSIGNED_CLOSEABLE_NAME = "<unassigned Closeable value>".toCharArray();
   private static final char[] UNASSIGNED_CLOSEABLE_NAME_TEMPLATE = "<unassigned Closeable value from line {0}>".toCharArray();
   private static final char[] TEMPLATE_ARGUMENT = "{0}".toCharArray();
   private static final int CLOSE_SEEN = 1;
   private static final int SHARED_WITH_OUTSIDE = 2;
   private static final int OWNED_BY_OUTSIDE = 4;
   private static final int CLOSED_IN_NESTED_METHOD = 8;
   private static final int REPORTED_EXPLICIT_CLOSE = 16;
   private static final int REPORTED_POTENTIAL_LEAK = 32;
   private static final int REPORTED_DEFINITIVE_LEAK = 64;
   public static boolean TEST_372319 = false;
   private int globalClosingState = 0;
   public LocalVariableBinding originalBinding;
   public FakedTrackingVariable innerTracker;
   public FakedTrackingVariable outerTracker;
   MethodScope methodScope;
   private HashMap recordedLocations;
   private ASTNode currentAssignment;
   private FlowContext tryContext;

   public FakedTrackingVariable(LocalVariableBinding original, ASTNode location, FlowInfo flowInfo, FlowContext flowContext, int nullStatus) {
      super(original.name, location.sourceStart, location.sourceEnd);
      this.type = new SingleTypeReference(TypeConstants.OBJECT, ((long)this.sourceStart << 32) + (long)this.sourceEnd);
      this.methodScope = original.declaringScope.methodScope();

      for(this.originalBinding = original; flowContext != null; flowContext = flowContext.parent) {
         if (flowContext instanceof FinallyFlowContext) {
            this.tryContext = ((FinallyFlowContext)flowContext).tryContext;
            break;
         }
      }

      this.resolve(original.declaringScope);
      if (nullStatus != 0) {
         flowInfo.markNullStatus(this.binding, nullStatus);
      }
   }

   private FakedTrackingVariable(BlockScope scope, ASTNode location, FlowInfo flowInfo, int nullStatus) {
      super(UNASSIGNED_CLOSEABLE_NAME, location.sourceStart, location.sourceEnd);
      this.type = new SingleTypeReference(TypeConstants.OBJECT, ((long)this.sourceStart << 32) + (long)this.sourceEnd);
      this.methodScope = scope.methodScope();
      this.originalBinding = null;
      this.resolve(scope);
      if (nullStatus != 0) {
         flowInfo.markNullStatus(this.binding, nullStatus);
      }
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
   }

   @Override
   public void resolve(BlockScope scope) {
      this.binding = new LocalVariableBinding(this.name, scope.getJavaLangObject(), 0, false);
      this.binding.closeTracker = this;
      this.binding.declaringScope = scope;
      this.binding.setConstant(Constant.NotAConstant);
      this.binding.useFlag = 1;
      this.binding.id = scope.registerTrackingVariable(this);
   }

   public static FakedTrackingVariable getCloseTrackingVariable(Expression expression, FlowInfo flowInfo, FlowContext flowContext) {
      while(true) {
         if (expression instanceof CastExpression) {
            expression = ((CastExpression)expression).expression;
         } else {
            if (!(expression instanceof Assignment)) {
               if (expression instanceof ConditionalExpression) {
                  FakedTrackingVariable falseTrackingVariable = getCloseTrackingVariable(
                     ((ConditionalExpression)expression).valueIfFalse, flowInfo, flowContext
                  );
                  if (falseTrackingVariable != null) {
                     return falseTrackingVariable;
                  }

                  return getCloseTrackingVariable(((ConditionalExpression)expression).valueIfTrue, flowInfo, flowContext);
               }

               if (expression instanceof SingleNameReference) {
                  SingleNameReference name = (SingleNameReference)expression;
                  if (name.binding instanceof LocalVariableBinding) {
                     LocalVariableBinding local = (LocalVariableBinding)name.binding;
                     if (local.closeTracker != null) {
                        return local.closeTracker;
                     }

                     if (!isAnyCloseable(expression.resolvedType)) {
                        return null;
                     }

                     if ((local.tagBits & 8192L) != 0L) {
                        return null;
                     }

                     Statement location = local.declaration;
                     local.closeTracker = new FakedTrackingVariable(local, location, flowInfo, flowContext, 1);
                     if (local.isParameter()) {
                        local.closeTracker.globalClosingState |= 4;
                     }

                     return local.closeTracker;
                  }
               } else if (expression instanceof AllocationExpression) {
                  return ((AllocationExpression)expression).closeTracker;
               }

               return null;
            }

            expression = ((Assignment)expression).expression;
         }
      }
   }

   public static void preConnectTrackerAcrossAssignment(ASTNode location, LocalVariableBinding local, Expression rhs, FlowInfo flowInfo) {
      FakedTrackingVariable closeTracker = null;
      if (containsAllocation(rhs)) {
         closeTracker = local.closeTracker;
         if (closeTracker == null && rhs.resolvedType != TypeBinding.NULL) {
            closeTracker = new FakedTrackingVariable(local, location, flowInfo, null, 1);
            if (local.isParameter()) {
               closeTracker.globalClosingState |= 4;
            }
         }

         if (closeTracker != null) {
            closeTracker.currentAssignment = location;
            preConnectTrackerAcrossAssignment(location, local, flowInfo, closeTracker, rhs);
         }
      }
   }

   private static boolean containsAllocation(ASTNode location) {
      if (location instanceof AllocationExpression) {
         return true;
      } else if (location instanceof ConditionalExpression) {
         ConditionalExpression conditional = (ConditionalExpression)location;
         return containsAllocation(conditional.valueIfTrue) || containsAllocation(conditional.valueIfFalse);
      } else {
         return location instanceof CastExpression ? containsAllocation(((CastExpression)location).expression) : false;
      }
   }

   private static void preConnectTrackerAcrossAssignment(
      ASTNode location, LocalVariableBinding local, FlowInfo flowInfo, FakedTrackingVariable closeTracker, Expression expression
   ) {
      if (expression instanceof AllocationExpression) {
         preConnectTrackerAcrossAssignment(location, local, flowInfo, (AllocationExpression)expression, closeTracker);
      } else if (expression instanceof ConditionalExpression) {
         preConnectTrackerAcrossAssignment(location, local, flowInfo, (ConditionalExpression)expression, closeTracker);
      } else if (expression instanceof CastExpression) {
         preConnectTrackerAcrossAssignment(location, local, ((CastExpression)expression).expression, flowInfo);
      }
   }

   private static void preConnectTrackerAcrossAssignment(
      ASTNode location, LocalVariableBinding local, FlowInfo flowInfo, ConditionalExpression conditional, FakedTrackingVariable closeTracker
   ) {
      preConnectTrackerAcrossAssignment(location, local, flowInfo, closeTracker, conditional.valueIfFalse);
      preConnectTrackerAcrossAssignment(location, local, flowInfo, closeTracker, conditional.valueIfTrue);
   }

   private static void preConnectTrackerAcrossAssignment(
      ASTNode location, LocalVariableBinding local, FlowInfo flowInfo, AllocationExpression allocationExpression, FakedTrackingVariable closeTracker
   ) {
      allocationExpression.closeTracker = closeTracker;
      if (allocationExpression.arguments != null && allocationExpression.arguments.length > 0) {
         preConnectTrackerAcrossAssignment(location, local, allocationExpression.arguments[0], flowInfo);
      }
   }

   public static void analyseCloseableAllocation(BlockScope scope, FlowInfo flowInfo, AllocationExpression allocation) {
      if (((ReferenceBinding)allocation.resolvedType).hasTypeBit(8)) {
         if (allocation.closeTracker != null) {
            allocation.closeTracker.withdraw();
            allocation.closeTracker = null;
         }
      } else if (((ReferenceBinding)allocation.resolvedType).hasTypeBit(4)) {
         boolean isWrapper = true;
         if (allocation.arguments != null && allocation.arguments.length > 0) {
            FakedTrackingVariable innerTracker = findCloseTracker(scope, flowInfo, allocation.arguments[0]);
            if (innerTracker != null) {
               FakedTrackingVariable currentInner = innerTracker;

               while(currentInner != allocation.closeTracker) {
                  currentInner = currentInner.innerTracker;
                  if (currentInner == null) {
                     int newStatus = 2;
                     if (allocation.closeTracker == null) {
                        allocation.closeTracker = new FakedTrackingVariable(scope, allocation, flowInfo, 1);
                     } else if (scope.finallyInfo != null) {
                        int finallyStatus = scope.finallyInfo.nullStatus(allocation.closeTracker.binding);
                        if (finallyStatus != 1) {
                           newStatus = finallyStatus;
                        }
                     }

                     if (allocation.closeTracker.innerTracker != null) {
                        innerTracker = pickMoreUnsafe(allocation.closeTracker.innerTracker, innerTracker, scope, flowInfo);
                     }

                     allocation.closeTracker.innerTracker = innerTracker;
                     innerTracker.outerTracker = allocation.closeTracker;
                     flowInfo.markNullStatus(allocation.closeTracker.binding, newStatus);
                     if (newStatus != 2) {
                        for(FakedTrackingVariable currentTracker = innerTracker; currentTracker != null; currentTracker = currentTracker.innerTracker) {
                           flowInfo.markNullStatus(currentTracker.binding, newStatus);
                           currentTracker.globalClosingState |= allocation.closeTracker.globalClosingState;
                        }
                     }

                     return;
                  }
               }

               return;
            }

            if (!isAnyCloseable(allocation.arguments[0].resolvedType)) {
               isWrapper = false;
            }
         } else {
            isWrapper = false;
         }

         if (isWrapper) {
            if (allocation.closeTracker != null) {
               allocation.closeTracker.withdraw();
               allocation.closeTracker = null;
            }
         } else {
            handleRegularResource(scope, flowInfo, allocation);
         }
      } else {
         handleRegularResource(scope, flowInfo, allocation);
      }
   }

   private static FakedTrackingVariable pickMoreUnsafe(FakedTrackingVariable tracker1, FakedTrackingVariable tracker2, BlockScope scope, FlowInfo info) {
      int status1 = info.nullStatus(tracker1.binding);
      int status2 = info.nullStatus(tracker2.binding);
      if (status1 == 2 || status2 == 4) {
         return pick(tracker1, tracker2, scope);
      } else if (status1 == 4 || status2 == 2) {
         return pick(tracker2, tracker1, scope);
      } else if ((status1 & 16) != 0) {
         return pick(tracker1, tracker2, scope);
      } else {
         return (status2 & 16) != 0 ? pick(tracker2, tracker1, scope) : pick(tracker1, tracker2, scope);
      }
   }

   private static FakedTrackingVariable pick(FakedTrackingVariable tracker1, FakedTrackingVariable tracker2, BlockScope scope) {
      tracker2.withdraw();
      return tracker1;
   }

   private static void handleRegularResource(BlockScope scope, FlowInfo flowInfo, AllocationExpression allocation) {
      FakedTrackingVariable presetTracker = allocation.closeTracker;
      if (presetTracker != null && presetTracker.originalBinding != null) {
         int closeStatus = flowInfo.nullStatus(presetTracker.binding);
         if (closeStatus != 4
            && closeStatus != 1
            && !flowInfo.isDefinitelyNull(presetTracker.originalBinding)
            && !(presetTracker.currentAssignment instanceof LocalDeclaration)) {
            allocation.closeTracker.recordErrorLocation(presetTracker.currentAssignment, closeStatus);
         }
      } else {
         allocation.closeTracker = new FakedTrackingVariable(scope, allocation, flowInfo, 1);
      }

      flowInfo.markAsDefinitelyNull(allocation.closeTracker.binding);
   }

   private static FakedTrackingVariable findCloseTracker(BlockScope scope, FlowInfo flowInfo, Expression arg) {
      while(arg instanceof Assignment) {
         Assignment assign = (Assignment)arg;
         LocalVariableBinding innerLocal = assign.localVariableBinding();
         if (innerLocal != null) {
            return innerLocal.closeTracker;
         }

         arg = assign.expression;
      }

      if (arg instanceof SingleNameReference) {
         LocalVariableBinding local = arg.localVariableBinding();
         if (local != null) {
            return local.closeTracker;
         }
      } else if (arg instanceof AllocationExpression) {
         return ((AllocationExpression)arg).closeTracker;
      }

      return null;
   }

   public static void handleResourceAssignment(
      BlockScope scope, FlowInfo upstreamInfo, FlowInfo flowInfo, FlowContext flowContext, ASTNode location, Expression rhs, LocalVariableBinding local
   ) {
      FakedTrackingVariable previousTracker = null;
      FakedTrackingVariable disconnectedTracker = null;
      if (local.closeTracker != null) {
         previousTracker = local.closeTracker;
         int nullStatus = upstreamInfo.nullStatus(local);
         if (nullStatus != 2 && nullStatus != 1) {
            disconnectedTracker = previousTracker;
         }
      }

      if (rhs.resolvedType != TypeBinding.NULL) {
         FakedTrackingVariable rhsTrackVar = getCloseTrackingVariable(rhs, flowInfo, flowContext);
         if (rhsTrackVar != null) {
            if (local.closeTracker == null) {
               if (rhsTrackVar.originalBinding != null) {
                  local.closeTracker = rhsTrackVar;
               }

               if (rhsTrackVar.currentAssignment == location) {
                  rhsTrackVar.globalClosingState &= -7;
               }
            } else {
               label94: {
                  if (rhs instanceof AllocationExpression || rhs instanceof ConditionalExpression) {
                     if (rhsTrackVar == disconnectedTracker) {
                        return;
                     }

                     if (local.closeTracker == rhsTrackVar && (rhsTrackVar.globalClosingState & 4) != 0) {
                        local.closeTracker = new FakedTrackingVariable(local, location, flowInfo, flowContext, 2);
                        break label94;
                     }
                  }

                  local.closeTracker = rhsTrackVar;
               }
            }
         } else {
            label73:
            if (previousTracker != null) {
               FlowContext currentFlowContext = flowContext;
               if (previousTracker.tryContext != null) {
                  while(currentFlowContext != null) {
                     if (previousTracker.tryContext == currentFlowContext) {
                        break label73;
                     }

                     currentFlowContext = currentFlowContext.parent;
                  }
               }

               if ((previousTracker.globalClosingState & 6) == 0 && flowInfo.hasNullInfoFor(previousTracker.binding)) {
                  flowInfo.markAsDefinitelyNull(previousTracker.binding);
               }

               local.closeTracker = analyseCloseableExpression(flowInfo, flowContext, local, location, rhs, previousTracker);
            } else {
               rhsTrackVar = analyseCloseableExpression(flowInfo, flowContext, local, location, rhs, null);
               if (rhsTrackVar != null) {
                  local.closeTracker = rhsTrackVar;
                  if ((rhsTrackVar.globalClosingState & 6) == 0) {
                     flowInfo.markAsDefinitelyNull(rhsTrackVar.binding);
                  }
               }
            }
         }
      }

      if (disconnectedTracker != null) {
         if (disconnectedTracker.innerTracker != null && disconnectedTracker.innerTracker.binding.declaringScope == scope) {
            disconnectedTracker.innerTracker.outerTracker = null;
            scope.pruneWrapperTrackingVar(disconnectedTracker);
         } else {
            int upstreamStatus = upstreamInfo.nullStatus(disconnectedTracker.binding);
            if (upstreamStatus != 4) {
               disconnectedTracker.recordErrorLocation(location, upstreamStatus);
            }
         }
      }
   }

   private static FakedTrackingVariable analyseCloseableExpression(
      FlowInfo flowInfo, FlowContext flowContext, LocalVariableBinding local, ASTNode location, Expression expression, FakedTrackingVariable previousTracker
   ) {
      while(true) {
         if (expression instanceof Assignment) {
            expression = ((Assignment)expression).expression;
         } else {
            if (!(expression instanceof CastExpression)) {
               boolean isResourceProducer = false;
               if (expression.resolvedType instanceof ReferenceBinding) {
                  ReferenceBinding resourceType = (ReferenceBinding)expression.resolvedType;
                  if (resourceType.hasTypeBit(8)) {
                     if (!isBlacklistedMethod(expression)) {
                        return null;
                     }

                     isResourceProducer = true;
                  }
               }

               if (expression instanceof AllocationExpression) {
                  FakedTrackingVariable tracker = ((AllocationExpression)expression).closeTracker;
                  if (tracker != null && tracker.originalBinding == null) {
                     return null;
                  }

                  return tracker;
               }

               if (!(expression instanceof MessageSend) && !(expression instanceof ArrayReference)) {
                  if ((expression.bits & 7) != 1 && (!(expression instanceof QualifiedNameReference) || !((QualifiedNameReference)expression).isFieldAccess())
                     )
                   {
                     if (local.closeTracker != null) {
                        return local.closeTracker;
                     }

                     FakedTrackingVariable newTracker = new FakedTrackingVariable(local, location, flowInfo, flowContext, 1);
                     LocalVariableBinding rhsLocal = expression.localVariableBinding();
                     if (rhsLocal != null && rhsLocal.isParameter()) {
                        newTracker.globalClosingState |= 4;
                     }

                     return newTracker;
                  }

                  FakedTrackingVariable tracker = new FakedTrackingVariable(local, location, flowInfo, flowContext, 1);
                  tracker.globalClosingState |= 4;
                  return tracker;
               }

               FakedTrackingVariable tracker = new FakedTrackingVariable(local, location, flowInfo, flowContext, 16);
               if (!isResourceProducer) {
                  tracker.globalClosingState |= 2;
               }

               return tracker;
            }

            expression = ((CastExpression)expression).expression;
         }
      }
   }

   private static boolean isBlacklistedMethod(Expression expression) {
      if (expression instanceof MessageSend) {
         MethodBinding method = ((MessageSend)expression).binding;
         if (method != null && method.isValidBinding()) {
            return CharOperation.equals(method.declaringClass.compoundName, TypeConstants.JAVA_NIO_FILE_FILES);
         }
      }

      return false;
   }

   public static void cleanUpAfterAssignment(BlockScope currentScope, int lhsBits, Expression expression) {
      while(true) {
         if (expression instanceof Assignment) {
            expression = ((Assignment)expression).expression;
         } else {
            if (!(expression instanceof CastExpression)) {
               if (expression instanceof AllocationExpression) {
                  FakedTrackingVariable tracker = ((AllocationExpression)expression).closeTracker;
                  if (tracker != null && tracker.originalBinding == null) {
                     tracker.withdraw();
                     ((AllocationExpression)expression).closeTracker = null;
                  }
               } else {
                  LocalVariableBinding local = expression.localVariableBinding();
                  if (local != null && local.closeTracker != null && (lhsBits & 1) != 0) {
                     local.closeTracker.withdraw();
                  }
               }

               return;
            }

            expression = ((CastExpression)expression).expression;
         }
      }
   }

   public static boolean isAnyCloseable(TypeBinding typeBinding) {
      return typeBinding instanceof ReferenceBinding && ((ReferenceBinding)typeBinding).hasTypeBit(3);
   }

   public int findMostSpecificStatus(FlowInfo flowInfo, BlockScope currentScope, BlockScope locationScope) {
      int status = 1;

      for(FakedTrackingVariable currentTracker = this; currentTracker != null; currentTracker = currentTracker.innerTracker) {
         LocalVariableBinding currentVar = currentTracker.binding;
         int currentStatus = this.getNullStatusAggressively(currentVar, flowInfo);
         if (locationScope != null) {
            currentStatus = this.mergeCloseStatus(locationScope, currentStatus, currentVar, currentScope);
         }

         if (currentStatus == 4) {
            status = currentStatus;
            break;
         }

         if (status == 2 || status == 1) {
            status = currentStatus;
         }
      }

      return status;
   }

   private int getNullStatusAggressively(LocalVariableBinding local, FlowInfo flowInfo) {
      if (flowInfo == FlowInfo.DEAD_END) {
         return 1;
      } else {
         int reachMode = flowInfo.reachMode();
         int status = 0;

         try {
            if (reachMode != 0) {
               flowInfo.tagBits &= -4;
            }

            status = flowInfo.nullStatus(local);
            if (TEST_372319) {
               try {
                  Thread.sleep(5L);
               } catch (InterruptedException var8) {
               }
            }
         } finally {
            flowInfo.tagBits |= reachMode;
         }

         if ((status & 2) != 0) {
            return (status & 36) != 0 ? 16 : 2;
         } else if ((status & 4) != 0) {
            return (status & 16) != 0 ? 16 : 4;
         } else {
            return (status & 16) != 0 ? 16 : status;
         }
      }
   }

   public int mergeCloseStatus(BlockScope currentScope, int status, LocalVariableBinding local, BlockScope outerScope) {
      if (status != 4) {
         if (currentScope.finallyInfo != null) {
            int finallyStatus = currentScope.finallyInfo.nullStatus(local);
            if (finallyStatus == 4) {
               return finallyStatus;
            }

            if (finallyStatus != 2 && currentScope.finallyInfo.hasNullInfoFor(local)) {
               status = 16;
            }
         }

         if (currentScope != outerScope && currentScope.parent instanceof BlockScope) {
            return this.mergeCloseStatus((BlockScope)currentScope.parent, status, local, outerScope);
         }
      }

      return status;
   }

   public void markClose(FlowInfo flowInfo, FlowContext flowContext) {
      FakedTrackingVariable current = this;

      do {
         flowInfo.markAsDefinitelyNonNull(current.binding);
         current.globalClosingState |= 1;
         flowContext.markFinallyNullStatus(current.binding, 4);
         current = current.innerTracker;
      } while(current != null);
   }

   public void markClosedInNestedMethod() {
      this.globalClosingState |= 8;
   }

   public static FlowInfo markPassedToOutside(BlockScope scope, Expression expression, FlowInfo flowInfo, FlowContext flowContext, boolean owned) {
      FakedTrackingVariable trackVar = getCloseTrackingVariable(expression, flowInfo, flowContext);
      if (trackVar == null) {
         return flowInfo;
      } else {
         FlowInfo infoResourceIsClosed = owned ? flowInfo : flowInfo.copy();
         int flag = owned ? 4 : 2;

         do {
            trackVar.globalClosingState |= flag;
            if (scope.methodScope() != trackVar.methodScope) {
               trackVar.globalClosingState |= 8;
            }

            infoResourceIsClosed.markAsDefinitelyNonNull(trackVar.binding);
         } while((trackVar = trackVar.innerTracker) != null);

         return owned ? infoResourceIsClosed : FlowInfo.conditional(flowInfo, infoResourceIsClosed);
      }
   }

   public boolean hasDefinitelyNoResource(FlowInfo flowInfo) {
      if (this.originalBinding == null) {
         return false;
      } else if (flowInfo.isDefinitelyNull(this.originalBinding)) {
         return true;
      } else {
         return !flowInfo.isDefinitelyAssigned(this.originalBinding) && !flowInfo.isPotentiallyAssigned(this.originalBinding);
      }
   }

   public boolean isClosedInFinallyOfEnclosing(BlockScope scope) {
      for(BlockScope currentScope = scope;
         currentScope.finallyInfo == null || !currentScope.finallyInfo.isDefinitelyNonNull(this.binding);
         currentScope = (BlockScope)currentScope.parent
      ) {
         if (!(currentScope.parent instanceof BlockScope)) {
            return false;
         }
      }

      return true;
   }

   public boolean isResourceBeingReturned(FakedTrackingVariable returnedResource) {
      FakedTrackingVariable current = this;

      while(current != returnedResource) {
         current = current.innerTracker;
         if (current == null) {
            return false;
         }
      }

      this.globalClosingState |= 64;
      return true;
   }

   public void withdraw() {
      this.binding.declaringScope.removeTrackingVar(this);
   }

   public void recordErrorLocation(ASTNode location, int nullStatus) {
      if ((this.globalClosingState & 4) == 0) {
         if (this.recordedLocations == null) {
            this.recordedLocations = new HashMap();
         }

         this.recordedLocations.put(location, nullStatus);
      }
   }

   public boolean reportRecordedErrors(Scope scope, int mergedStatus, boolean atDeadEnd) {
      FakedTrackingVariable current = this;

      while(current.globalClosingState == 0) {
         current = current.innerTracker;
         if (current == null) {
            if (atDeadEnd && this.neverClosedAtLocations()) {
               mergedStatus = 2;
            }

            if ((mergedStatus & 50) != 0) {
               this.reportError(scope.problemReporter(), null, mergedStatus);
               return true;
            }
            break;
         }
      }

      boolean hasReported = false;
      if (this.recordedLocations != null) {
         Iterator locations = this.recordedLocations.entrySet().iterator();

         int reportFlags;
         for(reportFlags = 0; locations.hasNext(); hasReported = true) {
            Entry entry = (Entry)locations.next();
            reportFlags |= this.reportError(scope.problemReporter(), (ASTNode)entry.getKey(), entry.getValue());
         }

         if (reportFlags != 0) {
            current = this;

            do {
               current.globalClosingState |= reportFlags;
            } while((current = current.innerTracker) != null);
         }
      }

      return hasReported;
   }

   private boolean neverClosedAtLocations() {
      if (this.recordedLocations != null) {
         for(Object value : this.recordedLocations.values()) {
            if (!value.equals(2)) {
               return false;
            }
         }
      }

      return true;
   }

   public int reportError(ProblemReporter problemReporter, ASTNode location, int nullStatus) {
      if ((this.globalClosingState & 4) != 0) {
         return 0;
      } else {
         boolean isPotentialProblem = false;
         if (nullStatus == 2) {
            if ((this.globalClosingState & 8) != 0) {
               isPotentialProblem = true;
            }
         } else if ((nullStatus & 48) != 0) {
            isPotentialProblem = true;
         }

         if (isPotentialProblem) {
            if ((this.globalClosingState & 96) != 0) {
               return 0;
            }

            problemReporter.potentiallyUnclosedCloseable(this, location);
         } else {
            if ((this.globalClosingState & 64) != 0) {
               return 0;
            }

            problemReporter.unclosedCloseable(this, location);
         }

         int reportFlag = isPotentialProblem ? 32 : 64;
         if (location == null) {
            FakedTrackingVariable current = this;

            do {
               current.globalClosingState |= reportFlag;
            } while((current = current.innerTracker) != null);
         }

         return reportFlag;
      }
   }

   public void reportExplicitClosing(ProblemReporter problemReporter) {
      if ((this.globalClosingState & 20) == 0) {
         this.globalClosingState |= 16;
         problemReporter.explicitlyClosedAutoCloseable(this);
      }
   }

   public String nameForReporting(ASTNode location, ReferenceContext referenceContext) {
      if (this.name == UNASSIGNED_CLOSEABLE_NAME && location != null && referenceContext != null) {
         CompilationResult compResult = referenceContext.compilationResult();
         if (compResult != null) {
            int[] lineEnds = compResult.getLineSeparatorPositions();
            int resourceLine = Util.getLineNumber(this.sourceStart, lineEnds, 0, lineEnds.length - 1);
            int reportLine = Util.getLineNumber(location.sourceStart, lineEnds, 0, lineEnds.length - 1);
            if (resourceLine != reportLine) {
               char[] replacement = Integer.toString(resourceLine).toCharArray();
               return String.valueOf(CharOperation.replace(UNASSIGNED_CLOSEABLE_NAME_TEMPLATE, TEMPLATE_ARGUMENT, replacement));
            }
         }
      }

      return String.valueOf(this.name);
   }

   public static class IteratorForReporting implements Iterator<FakedTrackingVariable> {
      private final Set<FakedTrackingVariable> varSet;
      private final Scope scope;
      private final boolean atExit;
      private FakedTrackingVariable.IteratorForReporting.Stage stage;
      private Iterator<FakedTrackingVariable> iterator;
      private FakedTrackingVariable next;

      public IteratorForReporting(List<FakedTrackingVariable> variables, Scope scope, boolean atExit) {
         this.varSet = new HashSet<>(variables);
         this.scope = scope;
         this.atExit = atExit;
         this.setUpForStage(FakedTrackingVariable.IteratorForReporting.Stage.OuterLess);
      }

      @Override
      public boolean hasNext() {
         label80: {
            switch(this.stage) {
               case OuterLess:
                  while(this.iterator.hasNext()) {
                     FakedTrackingVariable trackingVar = this.iterator.next();
                     if (trackingVar.outerTracker == null) {
                        return this.found(trackingVar);
                     }
                  }

                  this.setUpForStage(FakedTrackingVariable.IteratorForReporting.Stage.InnerOfProcessed);
               case InnerOfProcessed:
                  break;
               case InnerOfNotEnclosing:
                  break label80;
               case AtExit:
                  return this.atExit && this.iterator.hasNext() ? this.found(this.iterator.next()) : false;
               default:
                  throw new IllegalStateException("Unexpected Stage " + this.stage);
            }

            while(this.iterator.hasNext()) {
               FakedTrackingVariable trackingVar = this.iterator.next();
               FakedTrackingVariable outer = trackingVar.outerTracker;
               if (outer.binding.declaringScope == this.scope && !this.varSet.contains(outer)) {
                  return this.found(trackingVar);
               }
            }

            this.setUpForStage(FakedTrackingVariable.IteratorForReporting.Stage.InnerOfNotEnclosing);
         }

         while(this.iterator.hasNext()) {
            FakedTrackingVariable trackingVar = this.iterator.next();
            FakedTrackingVariable outer = trackingVar.outerTracker;
            if (!this.varSet.contains(outer)) {
               Scope outerTrackerScope = outer.binding.declaringScope;
               Scope currentScope = this.scope;

               do {
                  if (!((currentScope = currentScope.parent) instanceof BlockScope)) {
                     return this.found(trackingVar);
                  }
               } while(outerTrackerScope != currentScope);
               break;
            }
         }

         this.setUpForStage(FakedTrackingVariable.IteratorForReporting.Stage.AtExit);
         return this.atExit && this.iterator.hasNext() ? this.found(this.iterator.next()) : false;
      }

      private boolean found(FakedTrackingVariable trackingVar) {
         this.iterator.remove();
         this.next = trackingVar;
         return true;
      }

      private void setUpForStage(FakedTrackingVariable.IteratorForReporting.Stage nextStage) {
         this.iterator = this.varSet.iterator();
         this.stage = nextStage;
      }

      public FakedTrackingVariable next() {
         return this.next;
      }

      @Override
      public void remove() {
         throw new UnsupportedOperationException();
      }

      static enum Stage {
         OuterLess,
         InnerOfProcessed,
         InnerOfNotEnclosing,
         AtExit;
      }
   }
}
