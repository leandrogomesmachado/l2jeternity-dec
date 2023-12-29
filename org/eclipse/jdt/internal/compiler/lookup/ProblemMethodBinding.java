package org.eclipse.jdt.internal.compiler.lookup;

public class ProblemMethodBinding extends MethodBinding {
   private int problemReason;
   public MethodBinding closestMatch;
   public InferenceContext18 inferenceContext;

   public ProblemMethodBinding(char[] selector, TypeBinding[] args, int problemReason) {
      this.selector = selector;
      this.parameters = args != null && args.length != 0 ? args : Binding.NO_PARAMETERS;
      this.problemReason = problemReason;
      this.thrownExceptions = Binding.NO_EXCEPTIONS;
   }

   public ProblemMethodBinding(char[] selector, TypeBinding[] args, ReferenceBinding declaringClass, int problemReason) {
      this.selector = selector;
      this.parameters = args != null && args.length != 0 ? args : Binding.NO_PARAMETERS;
      this.declaringClass = declaringClass;
      this.problemReason = problemReason;
      this.thrownExceptions = Binding.NO_EXCEPTIONS;
   }

   public ProblemMethodBinding(MethodBinding closestMatch, char[] selector, TypeBinding[] args, int problemReason) {
      this(selector, args, problemReason);
      this.closestMatch = closestMatch;
      if (closestMatch != null && problemReason != 3) {
         this.declaringClass = closestMatch.declaringClass;
         this.returnType = closestMatch.returnType;
         if (problemReason == 23) {
            this.thrownExceptions = closestMatch.thrownExceptions;
            this.typeVariables = closestMatch.typeVariables;
            this.modifiers = closestMatch.modifiers;
            this.tagBits = closestMatch.tagBits;
         }
      }
   }

   @Override
   MethodBinding computeSubstitutedMethod(MethodBinding method, LookupEnvironment env) {
      return (MethodBinding)(this.closestMatch == null ? this : this.closestMatch.computeSubstitutedMethod(method, env));
   }

   @Override
   public MethodBinding findOriginalInheritedMethod(MethodBinding inheritedMethod) {
      return (MethodBinding)(this.closestMatch == null ? this : this.closestMatch.findOriginalInheritedMethod(inheritedMethod));
   }

   @Override
   public MethodBinding genericMethod() {
      return (MethodBinding)(this.closestMatch == null ? this : this.closestMatch.genericMethod());
   }

   @Override
   public MethodBinding original() {
      return (MethodBinding)(this.closestMatch == null ? this : this.closestMatch.original());
   }

   @Override
   public MethodBinding shallowOriginal() {
      return (MethodBinding)(this.closestMatch == null ? this : this.closestMatch.shallowOriginal());
   }

   @Override
   public MethodBinding tiebreakMethod() {
      return (MethodBinding)(this.closestMatch == null ? this : this.closestMatch.tiebreakMethod());
   }

   @Override
   public boolean hasSubstitutedParameters() {
      return this.closestMatch != null ? this.closestMatch.hasSubstitutedParameters() : false;
   }

   @Override
   public boolean isParameterizedGeneric() {
      return this.closestMatch instanceof ParameterizedGenericMethodBinding;
   }

   @Override
   public final int problemId() {
      return this.problemReason;
   }
}
