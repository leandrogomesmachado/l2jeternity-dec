package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class QualifiedThisReference extends ThisReference {
   public TypeReference qualification;
   ReferenceBinding currentCompatibleType;

   public QualifiedThisReference(TypeReference name, int sourceStart, int sourceEnd) {
      super(sourceStart, sourceEnd);
      this.qualification = name;
      name.bits |= 1073741824;
      this.sourceStart = name.sourceStart;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      return flowInfo;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
      return flowInfo;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
      int pc = codeStream.position;
      if (valueRequired) {
         if ((this.bits & 8160) != 0) {
            Object[] emulationPath = currentScope.getEmulationPath(this.currentCompatibleType, true, false);
            codeStream.generateOuterAccess(emulationPath, this, this.currentCompatibleType, currentScope);
         } else {
            codeStream.aload_0();
         }
      }

      codeStream.recordPositionsFrom(pc, this.sourceStart);
   }

   @Override
   public TypeBinding resolveType(BlockScope scope) {
      this.constant = Constant.NotAConstant;
      TypeBinding type = this.qualification.resolveType(scope, true);
      if (type != null && type.isValidBinding()) {
         type = type.erasure();
         if (type instanceof ReferenceBinding) {
            this.resolvedType = scope.environment().convertToParameterizedType((ReferenceBinding)type);
         } else {
            this.resolvedType = type;
         }

         int depth = this.findCompatibleEnclosing(scope.referenceType().binding, type, scope);
         this.bits &= -8161;
         this.bits |= (depth & 0xFF) << 5;
         if (this.currentCompatibleType == null) {
            if (this.resolvedType.isValidBinding()) {
               scope.problemReporter().noSuchEnclosingInstance(type, this, false);
            }

            return this.resolvedType;
         } else {
            scope.tagAsAccessingEnclosingInstanceStateOf(this.currentCompatibleType, false);
            if (depth == 0) {
               this.checkAccess(scope, null);
            }

            MethodScope methodScope = scope.namedMethodScope();
            if (methodScope != null) {
               MethodBinding method = methodScope.referenceMethodBinding();
               if (method != null) {
                  for(TypeBinding receiver = method.receiver; receiver != null; receiver = receiver.enclosingType()) {
                     if (TypeBinding.equalsEquals(receiver, this.resolvedType)) {
                        return this.resolvedType = receiver;
                     }
                  }
               }
            }

            return this.resolvedType;
         }
      } else {
         return null;
      }
   }

   int findCompatibleEnclosing(ReferenceBinding enclosingType, TypeBinding type, BlockScope scope) {
      int depth = 0;

      for(this.currentCompatibleType = enclosingType;
         this.currentCompatibleType != null && TypeBinding.notEquals(this.currentCompatibleType, type);
         this.currentCompatibleType = this.currentCompatibleType.isStatic() ? null : this.currentCompatibleType.enclosingType()
      ) {
         ++depth;
      }

      return depth;
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      return this.qualification.print(0, output).append(".this");
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      if (visitor.visit(this, blockScope)) {
         this.qualification.traverse(visitor, blockScope);
      }

      visitor.endVisit(this, blockScope);
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope blockScope) {
      if (visitor.visit(this, blockScope)) {
         this.qualification.traverse(visitor, blockScope);
      }

      visitor.endVisit(this, blockScope);
   }
}
