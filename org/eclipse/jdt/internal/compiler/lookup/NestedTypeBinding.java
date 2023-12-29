package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;

public abstract class NestedTypeBinding extends SourceTypeBinding {
   public SourceTypeBinding enclosingType;
   public SyntheticArgumentBinding[] enclosingInstances;
   private ReferenceBinding[] enclosingTypes = Binding.UNINITIALIZED_REFERENCE_TYPES;
   public SyntheticArgumentBinding[] outerLocalVariables;
   private int outerLocalVariablesSlotSize = -1;

   public NestedTypeBinding(char[][] typeName, ClassScope scope, SourceTypeBinding enclosingType) {
      super(typeName, enclosingType.fPackage, scope);
      this.tagBits |= 2052L;
      this.enclosingType = enclosingType;
   }

   public NestedTypeBinding(NestedTypeBinding prototype) {
      super(prototype);
      this.enclosingType = prototype.enclosingType;
      this.enclosingInstances = prototype.enclosingInstances;
      this.enclosingTypes = prototype.enclosingTypes;
      this.outerLocalVariables = prototype.outerLocalVariables;
      this.outerLocalVariablesSlotSize = prototype.outerLocalVariablesSlotSize;
   }

   public SyntheticArgumentBinding addSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         SyntheticArgumentBinding synthLocal = null;
         if (this.outerLocalVariables == null) {
            synthLocal = new SyntheticArgumentBinding(actualOuterLocalVariable);
            this.outerLocalVariables = new SyntheticArgumentBinding[]{synthLocal};
         } else {
            int size = this.outerLocalVariables.length;
            int newArgIndex = size;
            int i = size;

            while(--i >= 0) {
               if (this.outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable) {
                  return this.outerLocalVariables[i];
               }

               if (this.outerLocalVariables[i].id > actualOuterLocalVariable.id) {
                  newArgIndex = i;
               }
            }

            SyntheticArgumentBinding[] synthLocals = new SyntheticArgumentBinding[size + 1];
            System.arraycopy(this.outerLocalVariables, 0, synthLocals, 0, newArgIndex);
            synthLocals[newArgIndex] = synthLocal = new SyntheticArgumentBinding(actualOuterLocalVariable);
            System.arraycopy(this.outerLocalVariables, newArgIndex, synthLocals, newArgIndex + 1, size - newArgIndex);
            this.outerLocalVariables = synthLocals;
         }

         if (this.scope.referenceCompilationUnit().isPropagatingInnerClassEmulation) {
            this.updateInnerEmulationDependents();
         }

         return synthLocal;
      }
   }

   public SyntheticArgumentBinding addSyntheticArgument(ReferenceBinding targetEnclosingType) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         SyntheticArgumentBinding synthLocal = null;
         if (this.enclosingInstances == null) {
            synthLocal = new SyntheticArgumentBinding(targetEnclosingType);
            this.enclosingInstances = new SyntheticArgumentBinding[]{synthLocal};
         } else {
            int size = this.enclosingInstances.length;
            int newArgIndex = size;
            if (TypeBinding.equalsEquals(this.enclosingType(), targetEnclosingType)) {
               newArgIndex = 0;
            }

            SyntheticArgumentBinding[] newInstances = new SyntheticArgumentBinding[size + 1];
            System.arraycopy(this.enclosingInstances, 0, newInstances, newArgIndex == 0 ? 1 : 0, size);
            newInstances[newArgIndex] = synthLocal = new SyntheticArgumentBinding(targetEnclosingType);
            this.enclosingInstances = newInstances;
         }

         if (this.scope.referenceCompilationUnit().isPropagatingInnerClassEmulation) {
            this.updateInnerEmulationDependents();
         }

         return synthLocal;
      }
   }

   public SyntheticArgumentBinding addSyntheticArgumentAndField(LocalVariableBinding actualOuterLocalVariable) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         SyntheticArgumentBinding synthLocal = this.addSyntheticArgument(actualOuterLocalVariable);
         if (synthLocal == null) {
            return null;
         } else {
            if (synthLocal.matchingField == null) {
               synthLocal.matchingField = this.addSyntheticFieldForInnerclass(actualOuterLocalVariable);
            }

            return synthLocal;
         }
      }
   }

   public SyntheticArgumentBinding addSyntheticArgumentAndField(ReferenceBinding targetEnclosingType) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         SyntheticArgumentBinding synthLocal = this.addSyntheticArgument(targetEnclosingType);
         if (synthLocal == null) {
            return null;
         } else {
            if (synthLocal.matchingField == null) {
               synthLocal.matchingField = this.addSyntheticFieldForInnerclass(targetEnclosingType);
            }

            return synthLocal;
         }
      }
   }

   @Override
   protected void checkRedundantNullnessDefaultRecurse(ASTNode location, Annotation[] annotations, long nullBits, boolean useNullTypeAnnotations) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         ReferenceBinding currentType = this.enclosingType;

         while(((SourceTypeBinding)currentType).checkRedundantNullnessDefaultOne(location, annotations, nullBits, useNullTypeAnnotations)) {
            currentType = currentType.enclosingType();
            if (!(currentType instanceof SourceTypeBinding)) {
               super.checkRedundantNullnessDefaultRecurse(location, annotations, nullBits, useNullTypeAnnotations);
               return;
            }
         }
      }
   }

   @Override
   public ReferenceBinding enclosingType() {
      return this.enclosingType;
   }

   @Override
   public int getEnclosingInstancesSlotSize() {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         return this.enclosingInstances == null ? 0 : this.enclosingInstances.length;
      }
   }

   @Override
   public int getOuterLocalVariablesSlotSize() {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         if (this.outerLocalVariablesSlotSize < 0) {
            this.outerLocalVariablesSlotSize = 0;
            int outerLocalsCount = this.outerLocalVariables == null ? 0 : this.outerLocalVariables.length;

            for(int i = 0; i < outerLocalsCount; ++i) {
               SyntheticArgumentBinding argument = this.outerLocalVariables[i];
               switch(argument.type.id) {
                  case 7:
                  case 8:
                     this.outerLocalVariablesSlotSize += 2;
                     break;
                  default:
                     ++this.outerLocalVariablesSlotSize;
               }
            }
         }

         return this.outerLocalVariablesSlotSize;
      }
   }

   public SyntheticArgumentBinding getSyntheticArgument(LocalVariableBinding actualOuterLocalVariable) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else if (this.outerLocalVariables == null) {
         return null;
      } else {
         int i = this.outerLocalVariables.length;

         while(--i >= 0) {
            if (this.outerLocalVariables[i].actualOuterLocalVariable == actualOuterLocalVariable) {
               return this.outerLocalVariables[i];
            }
         }

         return null;
      }
   }

   public SyntheticArgumentBinding getSyntheticArgument(ReferenceBinding targetEnclosingType, boolean onlyExactMatch, boolean scopeIsConstructorCall) {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else if (this.enclosingInstances == null) {
         return null;
      } else if (scopeIsConstructorCall
         && this.enclosingInstances.length > 0
         && TypeBinding.equalsEquals(this.enclosingInstances[0].type, targetEnclosingType)
         && this.enclosingInstances[0].actualOuterLocalVariable == null) {
         return this.enclosingInstances[0];
      } else {
         int i = this.enclosingInstances.length;

         while(--i >= 0) {
            if (TypeBinding.equalsEquals(this.enclosingInstances[i].type, targetEnclosingType) && this.enclosingInstances[i].actualOuterLocalVariable == null) {
               return this.enclosingInstances[i];
            }
         }

         if (!onlyExactMatch) {
            i = this.enclosingInstances.length;

            while(--i >= 0) {
               if (this.enclosingInstances[i].actualOuterLocalVariable == null
                  && this.enclosingInstances[i].type.findSuperTypeOriginatingFrom(targetEnclosingType) != null) {
                  return this.enclosingInstances[i];
               }
            }
         }

         return null;
      }
   }

   public SyntheticArgumentBinding[] syntheticEnclosingInstances() {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         return this.enclosingInstances;
      }
   }

   @Override
   public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         if (this.enclosingTypes == UNINITIALIZED_REFERENCE_TYPES) {
            if (this.enclosingInstances == null) {
               this.enclosingTypes = null;
            } else {
               int length = this.enclosingInstances.length;
               this.enclosingTypes = new ReferenceBinding[length];

               for(int i = 0; i < length; ++i) {
                  this.enclosingTypes[i] = (ReferenceBinding)this.enclosingInstances[i].type;
               }
            }
         }

         return this.enclosingTypes;
      }
   }

   @Override
   public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
      if (!this.isPrototype()) {
         throw new IllegalStateException();
      } else {
         return this.outerLocalVariables;
      }
   }

   public void updateInnerEmulationDependents() {
   }
}
