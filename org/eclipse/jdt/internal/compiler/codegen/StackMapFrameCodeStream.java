package org.eclipse.jdt.internal.compiler.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;

public class StackMapFrameCodeStream extends CodeStream {
   public int[] stateIndexes;
   public int stateIndexesCounter;
   private HashMap framePositions;
   public Set exceptionMarkers;
   public ArrayList stackDepthMarkers;
   public ArrayList stackMarkers;

   public StackMapFrameCodeStream(ClassFile givenClassFile) {
      super(givenClassFile);
      this.generateAttributes |= 16;
   }

   @Override
   public void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
      for(int i = 0; i < this.visibleLocalsCount; ++i) {
         LocalVariableBinding localBinding = this.visibleLocals[i];
         if (localBinding != null) {
            boolean isDefinitelyAssigned = this.isDefinitelyAssigned(scope, initStateIndex, localBinding);
            if (!isDefinitelyAssigned) {
               if (this.stateIndexes != null) {
                  int j = 0;

                  for(int max = this.stateIndexesCounter; j < max; ++j) {
                     if (this.isDefinitelyAssigned(scope, this.stateIndexes[j], localBinding)) {
                        if (localBinding.initializationCount == 0 || localBinding.initializationPCs[(localBinding.initializationCount - 1 << 1) + 1] != -1) {
                           localBinding.recordInitializationStartPC(this.position);
                        }
                        break;
                     }
                  }
               }
            } else if (localBinding.initializationCount == 0 || localBinding.initializationPCs[(localBinding.initializationCount - 1 << 1) + 1] != -1) {
               localBinding.recordInitializationStartPC(this.position);
            }
         }
      }
   }

   public void addExceptionMarker(int pc, TypeBinding typeBinding) {
      if (this.exceptionMarkers == null) {
         this.exceptionMarkers = new HashSet();
      }

      if (typeBinding == null) {
         this.exceptionMarkers.add(new StackMapFrameCodeStream.ExceptionMarker(pc, ConstantPool.JavaLangThrowableConstantPoolName));
      } else {
         switch(typeBinding.id) {
            case 7:
               this.exceptionMarkers.add(new StackMapFrameCodeStream.ExceptionMarker(pc, ConstantPool.JavaLangNoSuchFieldErrorConstantPoolName));
               break;
            case 12:
               this.exceptionMarkers.add(new StackMapFrameCodeStream.ExceptionMarker(pc, ConstantPool.JavaLangClassNotFoundExceptionConstantPoolName));
               break;
            default:
               this.exceptionMarkers.add(new StackMapFrameCodeStream.ExceptionMarker(pc, typeBinding.constantPoolName()));
         }
      }
   }

   public void addFramePosition(int pc) {
      Integer newEntry = pc;
      StackMapFrameCodeStream.FramePosition value;
      if ((value = (StackMapFrameCodeStream.FramePosition)this.framePositions.get(newEntry)) != null) {
         ++value.counter;
      } else {
         this.framePositions.put(newEntry, new StackMapFrameCodeStream.FramePosition());
      }
   }

   @Override
   public void optimizeBranch(int oldPosition, BranchLabel lbl) {
      super.optimizeBranch(oldPosition, lbl);
      this.removeFramePosition(oldPosition);
   }

   public void removeFramePosition(int pc) {
      Integer entry = pc;
      StackMapFrameCodeStream.FramePosition value;
      if ((value = (StackMapFrameCodeStream.FramePosition)this.framePositions.get(entry)) != null) {
         --value.counter;
         if (value.counter <= 0) {
            this.framePositions.remove(entry);
         }
      }
   }

   @Override
   public void addVariable(LocalVariableBinding localBinding) {
      if (localBinding.initializationPCs == null) {
         this.record(localBinding);
      }

      localBinding.recordInitializationStartPC(this.position);
   }

   private void addStackMarker(int pc, int destinationPC) {
      if (this.stackMarkers == null) {
         this.stackMarkers = new ArrayList();
         this.stackMarkers.add(new StackMapFrameCodeStream.StackMarker(pc, destinationPC));
      } else {
         int size = this.stackMarkers.size();
         if (size == 0 || ((StackMapFrameCodeStream.StackMarker)this.stackMarkers.get(size - 1)).pc != this.position) {
            this.stackMarkers.add(new StackMapFrameCodeStream.StackMarker(pc, destinationPC));
         }
      }
   }

   private void addStackDepthMarker(int pc, int delta, TypeBinding typeBinding) {
      if (this.stackDepthMarkers == null) {
         this.stackDepthMarkers = new ArrayList();
         this.stackDepthMarkers.add(new StackMapFrameCodeStream.StackDepthMarker(pc, delta, typeBinding));
      } else {
         int size = this.stackDepthMarkers.size();
         if (size == 0) {
            this.stackDepthMarkers.add(new StackMapFrameCodeStream.StackDepthMarker(pc, delta, typeBinding));
         } else {
            StackMapFrameCodeStream.StackDepthMarker stackDepthMarker = (StackMapFrameCodeStream.StackDepthMarker)this.stackDepthMarkers.get(size - 1);
            if (stackDepthMarker.pc != this.position) {
               this.stackDepthMarkers.add(new StackMapFrameCodeStream.StackDepthMarker(pc, delta, typeBinding));
            } else {
               this.stackDepthMarkers.set(size - 1, new StackMapFrameCodeStream.StackDepthMarker(pc, delta, typeBinding));
            }
         }
      }
   }

   @Override
   public void decrStackSize(int offset) {
      super.decrStackSize(offset);
      this.addStackDepthMarker(this.position, -1, null);
   }

   @Override
   public void recordExpressionType(TypeBinding typeBinding) {
      this.addStackDepthMarker(this.position, 0, typeBinding);
   }

   @Override
   public void generateClassLiteralAccessForType(TypeBinding accessedType, FieldBinding syntheticFieldBinding) {
      if (accessedType.isBaseType() && accessedType != TypeBinding.NULL) {
         this.getTYPE(accessedType.id);
      } else {
         if (this.targetLevel >= 3211264L) {
            this.ldc(accessedType);
         } else {
            BranchLabel endLabel = new BranchLabel(this);
            if (syntheticFieldBinding != null) {
               this.fieldAccess((byte)-78, syntheticFieldBinding, null);
               this.dup();
               this.ifnonnull(endLabel);
               this.pop();
            }

            ExceptionLabel classNotFoundExceptionHandler = new ExceptionLabel(this, TypeBinding.NULL);
            classNotFoundExceptionHandler.placeStart();
            this.ldc(accessedType == TypeBinding.NULL ? "java.lang.Object" : String.valueOf(accessedType.constantPoolName()).replace('/', '.'));
            this.invokeClassForName();
            classNotFoundExceptionHandler.placeEnd();
            if (syntheticFieldBinding != null) {
               this.dup();
               this.fieldAccess((byte)-77, syntheticFieldBinding, null);
            }

            int fromPC = this.position;
            this.goto_(endLabel);
            int savedStackDepth = this.stackDepth;
            this.pushExceptionOnStack(TypeBinding.NULL);
            classNotFoundExceptionHandler.place();
            this.newNoClassDefFoundError();
            this.dup_x1();
            this.swap();
            this.invokeThrowableGetMessage();
            this.invokeNoClassDefFoundErrorStringConstructor();
            this.athrow();
            endLabel.place();
            this.addStackMarker(fromPC, this.position);
            this.stackDepth = savedStackDepth;
         }
      }
   }

   @Override
   public void generateOuterAccess(Object[] mappingSequence, ASTNode invocationSite, Binding target, Scope scope) {
      int currentPosition = this.position;
      super.generateOuterAccess(mappingSequence, invocationSite, target, scope);
      if (currentPosition == this.position) {
         throw new AbortMethod(scope.referenceCompilationUnit().compilationResult, null);
      }
   }

   public StackMapFrameCodeStream.ExceptionMarker[] getExceptionMarkers() {
      Set exceptionMarkerSet = this.exceptionMarkers;
      if (this.exceptionMarkers == null) {
         return null;
      } else {
         int size = exceptionMarkerSet.size();
         StackMapFrameCodeStream.ExceptionMarker[] markers = new StackMapFrameCodeStream.ExceptionMarker[size];
         int n = 0;
         Iterator iterator = exceptionMarkerSet.iterator();

         while(iterator.hasNext()) {
            markers[n++] = (StackMapFrameCodeStream.ExceptionMarker)iterator.next();
         }

         Arrays.sort((Object[])markers);
         return markers;
      }
   }

   public int[] getFramePositions() {
      Set set = this.framePositions.keySet();
      int size = set.size();
      int[] positions = new int[size];
      int n = 0;
      Iterator iterator = set.iterator();

      while(iterator.hasNext()) {
         positions[n++] = iterator.next();
      }

      Arrays.sort(positions);
      return positions;
   }

   public StackMapFrameCodeStream.StackDepthMarker[] getStackDepthMarkers() {
      if (this.stackDepthMarkers == null) {
         return null;
      } else {
         int length = this.stackDepthMarkers.size();
         if (length == 0) {
            return null;
         } else {
            StackMapFrameCodeStream.StackDepthMarker[] result = new StackMapFrameCodeStream.StackDepthMarker[length];
            this.stackDepthMarkers.toArray(result);
            return result;
         }
      }
   }

   public StackMapFrameCodeStream.StackMarker[] getStackMarkers() {
      if (this.stackMarkers == null) {
         return null;
      } else {
         int length = this.stackMarkers.size();
         if (length == 0) {
            return null;
         } else {
            StackMapFrameCodeStream.StackMarker[] result = new StackMapFrameCodeStream.StackMarker[length];
            this.stackMarkers.toArray(result);
            return result;
         }
      }
   }

   public boolean hasFramePositions() {
      return this.framePositions.size() != 0;
   }

   @Override
   public void init(ClassFile targetClassFile) {
      super.init(targetClassFile);
      this.stateIndexesCounter = 0;
      if (this.framePositions != null) {
         this.framePositions.clear();
      }

      if (this.exceptionMarkers != null) {
         this.exceptionMarkers.clear();
      }

      if (this.stackDepthMarkers != null) {
         this.stackDepthMarkers.clear();
      }

      if (this.stackMarkers != null) {
         this.stackMarkers.clear();
      }
   }

   @Override
   public void initializeMaxLocals(MethodBinding methodBinding) {
      super.initializeMaxLocals(methodBinding);
      if (this.framePositions == null) {
         this.framePositions = new HashMap();
      } else {
         this.framePositions.clear();
      }
   }

   public void popStateIndex() {
      --this.stateIndexesCounter;
   }

   public void pushStateIndex(int naturalExitMergeInitStateIndex) {
      if (this.stateIndexes == null) {
         this.stateIndexes = new int[3];
      }

      int length = this.stateIndexes.length;
      if (length == this.stateIndexesCounter) {
         System.arraycopy(this.stateIndexes, 0, this.stateIndexes = new int[length * 2], 0, length);
      }

      this.stateIndexes[this.stateIndexesCounter++] = naturalExitMergeInitStateIndex;
   }

   @Override
   public void removeNotDefinitelyAssignedVariables(Scope scope, int initStateIndex) {
      int index = this.visibleLocalsCount;

      label37:
      for(int i = 0; i < index; ++i) {
         LocalVariableBinding localBinding = this.visibleLocals[i];
         if (localBinding != null && localBinding.initializationCount > 0) {
            boolean isDefinitelyAssigned = this.isDefinitelyAssigned(scope, initStateIndex, localBinding);
            if (!isDefinitelyAssigned) {
               if (this.stateIndexes != null) {
                  int j = 0;

                  for(int max = this.stateIndexesCounter; j < max; ++j) {
                     if (this.isDefinitelyAssigned(scope, this.stateIndexes[j], localBinding)) {
                        continue label37;
                     }
                  }
               }

               localBinding.recordInitializationEndPC(this.position);
            }
         }
      }
   }

   @Override
   public void reset(ClassFile givenClassFile) {
      super.reset(givenClassFile);
      this.stateIndexesCounter = 0;
      if (this.framePositions != null) {
         this.framePositions.clear();
      }

      if (this.exceptionMarkers != null) {
         this.exceptionMarkers.clear();
      }

      if (this.stackDepthMarkers != null) {
         this.stackDepthMarkers.clear();
      }

      if (this.stackMarkers != null) {
         this.stackMarkers.clear();
      }
   }

   @Override
   protected void writePosition(BranchLabel label) {
      super.writePosition(label);
      this.addFramePosition(label.position);
   }

   @Override
   protected void writePosition(BranchLabel label, int forwardReference) {
      super.writePosition(label, forwardReference);
      this.addFramePosition(label.position);
   }

   @Override
   protected void writeSignedWord(int pos, int value) {
      super.writeSignedWord(pos, value);
      this.addFramePosition(this.position);
   }

   @Override
   protected void writeWidePosition(BranchLabel label) {
      super.writeWidePosition(label);
      this.addFramePosition(label.position);
   }

   @Override
   public void areturn() {
      super.areturn();
      this.addFramePosition(this.position);
   }

   @Override
   public void ireturn() {
      super.ireturn();
      this.addFramePosition(this.position);
   }

   @Override
   public void lreturn() {
      super.lreturn();
      this.addFramePosition(this.position);
   }

   @Override
   public void freturn() {
      super.freturn();
      this.addFramePosition(this.position);
   }

   @Override
   public void dreturn() {
      super.dreturn();
      this.addFramePosition(this.position);
   }

   @Override
   public void return_() {
      super.return_();
      this.addFramePosition(this.position);
   }

   @Override
   public void athrow() {
      super.athrow();
      this.addFramePosition(this.position);
   }

   @Override
   public void pushOnStack(TypeBinding binding) {
      super.pushOnStack(binding);
      this.addStackDepthMarker(this.position, 1, binding);
   }

   @Override
   public void pushExceptionOnStack(TypeBinding binding) {
      super.pushExceptionOnStack(binding);
      this.addExceptionMarker(this.position, binding);
   }

   @Override
   public void goto_(BranchLabel label) {
      super.goto_(label);
      this.addFramePosition(this.position);
   }

   @Override
   public void goto_w(BranchLabel label) {
      super.goto_w(label);
      this.addFramePosition(this.position);
   }

   @Override
   public void resetInWideMode() {
      this.resetSecretLocals();
      super.resetInWideMode();
   }

   @Override
   public void resetForCodeGenUnusedLocals() {
      this.resetSecretLocals();
      super.resetForCodeGenUnusedLocals();
   }

   public void resetSecretLocals() {
      int i = 0;

      for(int max = this.locals.length; i < max; ++i) {
         LocalVariableBinding localVariableBinding = this.locals[i];
         if (localVariableBinding != null && localVariableBinding.isSecret()) {
            localVariableBinding.resetInitializations();
         }
      }
   }

   public static class ExceptionMarker implements Comparable {
      public char[] constantPoolName;
      public int pc;

      public ExceptionMarker(int pc, char[] constantPoolName) {
         this.pc = pc;
         this.constantPoolName = constantPoolName;
      }

      @Override
      public int compareTo(Object o) {
         return o instanceof StackMapFrameCodeStream.ExceptionMarker ? this.pc - ((StackMapFrameCodeStream.ExceptionMarker)o).pc : 0;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj instanceof StackMapFrameCodeStream.ExceptionMarker) {
            StackMapFrameCodeStream.ExceptionMarker marker = (StackMapFrameCodeStream.ExceptionMarker)obj;
            return this.pc == marker.pc && CharOperation.equals(this.constantPoolName, marker.constantPoolName);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.pc + this.constantPoolName.hashCode();
      }

      @Override
      public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append('(').append(this.pc).append(',').append(this.constantPoolName).append(')');
         return String.valueOf(buffer);
      }
   }

   static class FramePosition {
      int counter;
   }

   public static class StackDepthMarker {
      public int pc;
      public int delta;
      public TypeBinding typeBinding;

      public StackDepthMarker(int pc, int delta, TypeBinding typeBinding) {
         this.pc = pc;
         this.typeBinding = typeBinding;
         this.delta = delta;
      }

      public StackDepthMarker(int pc, int delta) {
         this.pc = pc;
         this.delta = delta;
      }

      @Override
      public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append('(').append(this.pc).append(',').append(this.delta);
         if (this.typeBinding != null) {
            buffer.append(',').append(this.typeBinding.qualifiedPackageName()).append(this.typeBinding.qualifiedSourceName());
         }

         buffer.append(')');
         return String.valueOf(buffer);
      }
   }

   public static class StackMarker {
      public int pc;
      public int destinationPC;
      public VerificationTypeInfo[] infos;

      public StackMarker(int pc, int destinationPC) {
         this.pc = pc;
         this.destinationPC = destinationPC;
      }

      public void setInfos(VerificationTypeInfo[] infos) {
         this.infos = infos;
      }

      @Override
      public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append("[copy stack items from ").append(this.pc).append(" to ").append(this.destinationPC);
         if (this.infos != null) {
            int i = 0;

            for(int max = this.infos.length; i < max; ++i) {
               if (i > 0) {
                  buffer.append(',');
               }

               buffer.append(this.infos[i]);
            }
         }

         buffer.append(']');
         return String.valueOf(buffer);
      }
   }
}
