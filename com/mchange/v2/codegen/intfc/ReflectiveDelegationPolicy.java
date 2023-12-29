package com.mchange.v2.codegen.intfc;

public final class ReflectiveDelegationPolicy {
   public static final ReflectiveDelegationPolicy USE_MAIN_DELEGATE_INTERFACE = new ReflectiveDelegationPolicy();
   public static final ReflectiveDelegationPolicy USE_RUNTIME_CLASS = new ReflectiveDelegationPolicy();
   Class delegateClass;

   private ReflectiveDelegationPolicy() {
      this.delegateClass = null;
   }

   public ReflectiveDelegationPolicy(Class var1) {
      if (var1 == null) {
         throw new IllegalArgumentException("Class for reflective delegation cannot be null!");
      } else {
         this.delegateClass = var1;
      }
   }

   @Override
   public String toString() {
      if (this == USE_MAIN_DELEGATE_INTERFACE) {
         return "[ReflectiveDelegationPolicy: Reflectively delegate via the main delegate interface.]";
      } else {
         return this == USE_RUNTIME_CLASS
            ? "[ReflectiveDelegationPolicy: Reflectively delegate via the runtime class of the delegate object.]"
            : "[ReflectiveDelegationPolicy: Reflectively delegate via " + this.delegateClass.getName() + ".]";
      }
   }
}
