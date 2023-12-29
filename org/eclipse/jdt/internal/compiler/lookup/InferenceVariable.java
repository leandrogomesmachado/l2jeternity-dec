package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;

public class InferenceVariable extends TypeVariableBinding {
   InvocationSite site;
   TypeBinding typeParameter;
   long nullHints;
   private InferenceVariable prototype;
   int varId;

   public InferenceVariable(
      TypeBinding typeParameter, int parameterRank, int iVarId, InvocationSite site, LookupEnvironment environment, ReferenceBinding object
   ) {
      this(
         typeParameter,
         parameterRank,
         site,
         CharOperation.concat(typeParameter.shortReadableName(), Integer.toString(iVarId).toCharArray(), '#'),
         environment,
         object
      );
      this.varId = iVarId;
   }

   private InferenceVariable(
      TypeBinding typeParameter, int parameterRank, InvocationSite site, char[] sourceName, LookupEnvironment environment, ReferenceBinding object
   ) {
      super(sourceName, null, parameterRank, environment);
      this.site = site;
      this.typeParameter = typeParameter;
      this.tagBits |= typeParameter.tagBits & 108086391056891904L;
      if (typeParameter.isTypeVariable()) {
         TypeVariableBinding typeVariable = (TypeVariableBinding)typeParameter;
         if (typeVariable.firstBound != null) {
            long boundBits = typeVariable.firstBound.tagBits & 108086391056891904L;
            if (boundBits == 72057594037927936L) {
               this.tagBits |= boundBits;
            } else {
               this.nullHints |= boundBits;
            }
         }
      }

      this.superclass = object;
      this.prototype = this;
   }

   void updateSourceName(int newId) {
      int hashPos = CharOperation.indexOf('#', this.sourceName);
      this.varId = newId;
      this.sourceName = CharOperation.concat(CharOperation.subarray(this.sourceName, 0, hashPos), Integer.toString(this.varId).toCharArray(), '#');
   }

   @Override
   public TypeBinding clone(TypeBinding enclosingType) {
      InferenceVariable clone = new InferenceVariable(this.typeParameter, this.rank, this.site, this.sourceName, this.environment, this.superclass);
      clone.tagBits = this.tagBits;
      clone.nullHints = this.nullHints;
      clone.varId = this.varId;
      clone.prototype = this;
      return clone;
   }

   public InferenceVariable prototype() {
      return this.prototype;
   }

   @Override
   public char[] constantPoolName() {
      throw new UnsupportedOperationException();
   }

   @Override
   public PackageBinding getPackage() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isCompatibleWith(TypeBinding right, Scope scope) {
      return true;
   }

   @Override
   public boolean isProperType(boolean admitCapture18) {
      return false;
   }

   @Override
   TypeBinding substituteInferenceVariable(InferenceVariable var, TypeBinding substituteType) {
      return (TypeBinding)(TypeBinding.equalsEquals(this, var) ? substituteType : this);
   }

   @Override
   void collectInferenceVariables(Set<InferenceVariable> variables) {
      variables.add(this);
   }

   @Override
   public ReferenceBinding[] superInterfaces() {
      return Binding.NO_SUPERINTERFACES;
   }

   @Override
   public char[] qualifiedSourceName() {
      throw new UnsupportedOperationException();
   }

   @Override
   public char[] sourceName() {
      return this.sourceName;
   }

   @Override
   public char[] readableName() {
      return this.sourceName;
   }

   @Override
   public boolean hasTypeBit(int bit) {
      throw new UnsupportedOperationException();
   }

   @Override
   public String debugName() {
      return String.valueOf(this.sourceName);
   }

   @Override
   public String toString() {
      return this.debugName();
   }

   @Override
   public int hashCode() {
      int code = this.typeParameter.hashCode() + 17 * this.rank;
      return this.site != null ? 31 * code + this.site.hashCode() : code;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof InferenceVariable)) {
         return false;
      } else {
         InferenceVariable other = (InferenceVariable)obj;
         return this.rank == other.rank && this.site == other.site && TypeBinding.equalsEquals(this.typeParameter, other.typeParameter);
      }
   }

   @Override
   public TypeBinding erasure() {
      if (this.superclass == null) {
         this.superclass = this.environment.getType(TypeConstants.JAVA_LANG_OBJECT);
      }

      return super.erasure();
   }
}
