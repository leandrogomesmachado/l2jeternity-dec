package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;

public abstract class NameReference extends Reference implements InvocationSite {
   public Binding binding;
   public TypeBinding actualReceiverType;

   public NameReference() {
      this.bits |= 7;
   }

   @Override
   public FieldBinding fieldBinding() {
      return (FieldBinding)this.binding;
   }

   @Override
   public FieldBinding lastFieldBinding() {
      return (this.bits & 7) == 1 ? this.fieldBinding() : null;
   }

   @Override
   public InferenceContext18 freshInferenceContext(Scope scope) {
      return null;
   }

   @Override
   public boolean isSuperAccess() {
      return false;
   }

   @Override
   public boolean isTypeAccess() {
      return this.binding == null || this.binding instanceof ReferenceBinding;
   }

   @Override
   public boolean isTypeReference() {
      return this.binding instanceof ReferenceBinding;
   }

   @Override
   public void setActualReceiverType(ReferenceBinding receiverType) {
      if (receiverType != null) {
         this.actualReceiverType = receiverType;
      }
   }

   @Override
   public void setDepth(int depth) {
      this.bits &= -8161;
      if (depth > 0) {
         this.bits |= (depth & 0xFF) << 5;
      }
   }

   @Override
   public void setFieldIndex(int index) {
   }

   public abstract String unboundReferenceErrorName();

   public abstract char[][] getName();

   protected void checkEffectiveFinality(LocalVariableBinding localBinding, Scope scope) {
      if ((this.bits & 524288) != 0 && !localBinding.isFinal() && !localBinding.isEffectivelyFinal()) {
         scope.problemReporter().cannotReferToNonEffectivelyFinalOuterLocal(localBinding, this);
         throw new AbortMethod(scope.referenceCompilationUnit().compilationResult, null);
      }
   }
}
