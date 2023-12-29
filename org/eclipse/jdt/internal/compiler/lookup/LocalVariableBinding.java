package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FakedTrackingVariable;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;

public class LocalVariableBinding extends VariableBinding {
   public int resolvedPosition;
   public static final int UNUSED = 0;
   public static final int USED = 1;
   public static final int FAKE_USED = 2;
   public int useFlag;
   public BlockScope declaringScope;
   public LocalDeclaration declaration;
   public int[] initializationPCs;
   public int initializationCount = 0;
   public FakedTrackingVariable closeTracker;

   public LocalVariableBinding(char[] name, TypeBinding type, int modifiers, boolean isArgument) {
      super(name, type, modifiers, isArgument ? Constant.NotAConstant : null);
      if (isArgument) {
         this.tagBits |= 1024L;
      }

      this.tagBits |= 2048L;
   }

   public LocalVariableBinding(LocalDeclaration declaration, TypeBinding type, int modifiers, boolean isArgument) {
      this(declaration.name, type, modifiers, isArgument);
      this.declaration = declaration;
   }

   public LocalVariableBinding(LocalDeclaration declaration, TypeBinding type, int modifiers, MethodScope declaringScope) {
      this(declaration, type, modifiers, true);
      this.declaringScope = declaringScope;
   }

   @Override
   public final int kind() {
      return 2;
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      StringBuffer buffer = new StringBuffer();
      BlockScope scope = this.declaringScope;
      int occurenceCount = 0;
      if (scope != null) {
         MethodScope methodScope = scope instanceof MethodScope ? (MethodScope)scope : scope.enclosingMethodScope();
         ReferenceContext referenceContext = methodScope.referenceContext;
         if (referenceContext instanceof AbstractMethodDeclaration) {
            MethodBinding methodBinding = ((AbstractMethodDeclaration)referenceContext).binding;
            if (methodBinding != null) {
               buffer.append(methodBinding.computeUniqueKey(false));
            }
         } else if (referenceContext instanceof TypeDeclaration) {
            TypeBinding typeBinding = ((TypeDeclaration)referenceContext).binding;
            if (typeBinding != null) {
               buffer.append(typeBinding.computeUniqueKey(false));
            }
         } else if (referenceContext instanceof LambdaExpression) {
            MethodBinding methodBinding = ((LambdaExpression)referenceContext).binding;
            if (methodBinding != null) {
               buffer.append(methodBinding.computeUniqueKey(false));
            }
         }

         this.getScopeKey(scope, buffer);
         LocalVariableBinding[] locals = scope.locals;

         for(int i = 0; i < scope.localIndex; ++i) {
            LocalVariableBinding local = locals[i];
            if (CharOperation.equals(this.name, local.name)) {
               if (this == local) {
                  break;
               }

               ++occurenceCount;
            }
         }
      }

      buffer.append('#');
      buffer.append(this.name);
      boolean addParameterRank = this.isParameter() && this.declaringScope != null;
      if (occurenceCount > 0 || addParameterRank) {
         buffer.append('#');
         buffer.append(occurenceCount);
         if (addParameterRank) {
            int pos = -1;
            LocalVariableBinding[] params = this.declaringScope.locals;

            for(int i = 0; i < params.length; ++i) {
               if (params[i] == this) {
                  pos = i;
                  break;
               }
            }

            if (pos > -1) {
               buffer.append('#');
               buffer.append(pos);
            }
         }
      }

      int length = buffer.length();
      char[] uniqueKey = new char[length];
      buffer.getChars(0, length, uniqueKey, 0);
      return uniqueKey;
   }

   @Override
   public AnnotationBinding[] getAnnotations() {
      if (this.declaringScope != null) {
         SourceTypeBinding sourceType = this.declaringScope.enclosingSourceType();
         if (sourceType == null) {
            return Binding.NO_ANNOTATIONS;
         } else {
            if ((this.tagBits & 8589934592L) == 0L && (this.tagBits & 1024L) != 0L && this.declaration != null) {
               Annotation[] annotationNodes = this.declaration.annotations;
               if (annotationNodes != null) {
                  ASTNode.resolveAnnotations(this.declaringScope, annotationNodes, this, true);
               }
            }

            return sourceType.retrieveAnnotations(this);
         }
      } else {
         if ((this.tagBits & 8589934592L) != 0L) {
            if (this.declaration == null) {
               return Binding.NO_ANNOTATIONS;
            }

            Annotation[] annotations = this.declaration.annotations;
            if (annotations != null) {
               int length = annotations.length;
               AnnotationBinding[] annotationBindings = new AnnotationBinding[length];

               for(int i = 0; i < length; ++i) {
                  AnnotationBinding compilerAnnotation = annotations[i].getCompilerAnnotation();
                  if (compilerAnnotation == null) {
                     return Binding.NO_ANNOTATIONS;
                  }

                  annotationBindings[i] = compilerAnnotation;
               }

               return annotationBindings;
            }
         }

         return Binding.NO_ANNOTATIONS;
      }
   }

   private void getScopeKey(BlockScope scope, StringBuffer buffer) {
      int scopeIndex = scope.scopeIndex();
      if (scopeIndex != -1) {
         this.getScopeKey((BlockScope)scope.parent, buffer);
         buffer.append('#');
         buffer.append(scopeIndex);
      }
   }

   public boolean isSecret() {
      return this.declaration == null && (this.tagBits & 1024L) == 0L;
   }

   public void recordInitializationEndPC(int pc) {
      if (this.initializationPCs[(this.initializationCount - 1 << 1) + 1] == -1) {
         this.initializationPCs[(this.initializationCount - 1 << 1) + 1] = pc;
      }
   }

   public void recordInitializationStartPC(int pc) {
      if (this.initializationPCs != null) {
         if (this.initializationCount > 0) {
            int previousEndPC = this.initializationPCs[(this.initializationCount - 1 << 1) + 1];
            if (previousEndPC == -1) {
               return;
            }

            if (previousEndPC == pc) {
               this.initializationPCs[(this.initializationCount - 1 << 1) + 1] = -1;
               return;
            }
         }

         int index = this.initializationCount << 1;
         if (index == this.initializationPCs.length) {
            System.arraycopy(this.initializationPCs, 0, this.initializationPCs = new int[this.initializationCount << 2], 0, index);
         }

         this.initializationPCs[index] = pc;
         this.initializationPCs[index + 1] = -1;
         ++this.initializationCount;
      }
   }

   @Override
   public void setAnnotations(AnnotationBinding[] annotations, Scope scope) {
      if (scope != null) {
         SourceTypeBinding sourceType = scope.enclosingSourceType();
         if (sourceType != null) {
            sourceType.storeAnnotations(this, annotations);
         }
      }
   }

   public void resetInitializations() {
      this.initializationCount = 0;
      this.initializationPCs = null;
   }

   @Override
   public String toString() {
      String s = super.toString();
      switch(this.useFlag) {
         case 0:
            s = s + "[pos: unused]";
            break;
         case 1:
            s = s + "[pos: " + this.resolvedPosition + "]";
            break;
         case 2:
            s = s + "[pos: fake_used]";
      }

      s = s + "[id:" + this.id + "]";
      if (this.initializationCount > 0) {
         s = s + "[pc: ";

         for(int i = 0; i < this.initializationCount; ++i) {
            if (i > 0) {
               s = s + ", ";
            }

            s = s
               + String.valueOf(this.initializationPCs[i << 1])
               + "-"
               + (this.initializationPCs[(i << 1) + 1] == -1 ? "?" : String.valueOf(this.initializationPCs[(i << 1) + 1]));
         }

         s = s + "]";
      }

      return s;
   }

   @Override
   public boolean isParameter() {
      return (this.tagBits & 1024L) != 0L;
   }

   public boolean isCatchParameter() {
      return false;
   }

   public MethodBinding getEnclosingMethod() {
      BlockScope blockScope = this.declaringScope;
      if (blockScope != null) {
         ReferenceContext referenceContext = blockScope.referenceContext();
         if (referenceContext instanceof Initializer) {
            return null;
         }

         if (referenceContext instanceof AbstractMethodDeclaration) {
            return ((AbstractMethodDeclaration)referenceContext).binding;
         }
      }

      return null;
   }
}
