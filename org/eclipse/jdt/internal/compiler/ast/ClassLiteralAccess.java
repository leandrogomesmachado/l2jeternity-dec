package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class ClassLiteralAccess extends Expression {
   public TypeReference type;
   public TypeBinding targetType;
   FieldBinding syntheticField;

   public ClassLiteralAccess(int sourceEnd, TypeReference type) {
      this.type = type;
      type.bits |= 1073741824;
      this.sourceStart = type.sourceStart;
      this.sourceEnd = sourceEnd;
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      SourceTypeBinding sourceType = currentScope.outerMostClassScope().enclosingSourceType();
      if (!sourceType.isInterface() && !this.targetType.isBaseType() && currentScope.compilerOptions().targetJDK < 3211264L) {
         this.syntheticField = sourceType.addSyntheticFieldForClassLiteral(this.targetType, currentScope);
      }

      return flowInfo;
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
      int pc = codeStream.position;
      if (valueRequired) {
         codeStream.generateClassLiteralAccessForType(this.type.resolvedType, this.syntheticField);
         codeStream.generateImplicitConversion(this.implicitConversion);
      }

      codeStream.recordPositionsFrom(pc, this.sourceStart);
   }

   @Override
   public StringBuffer printExpression(int indent, StringBuffer output) {
      return this.type.print(0, output).append(".class");
   }

   @Override
   public TypeBinding resolveType(BlockScope scope) {
      this.constant = Constant.NotAConstant;
      if ((this.targetType = this.type.resolveType(scope, true)) == null) {
         return null;
      } else {
         LookupEnvironment environment = scope.environment();
         this.targetType = environment.convertToRawType(this.targetType, true);
         if (this.targetType.isArrayType()) {
            ArrayBinding arrayBinding = (ArrayBinding)this.targetType;
            TypeBinding leafComponentType = arrayBinding.leafComponentType;
            if (leafComponentType == TypeBinding.VOID) {
               scope.problemReporter().cannotAllocateVoidArray(this);
               return null;
            }

            if (leafComponentType.isTypeVariable()) {
               scope.problemReporter().illegalClassLiteralForTypeVariable((TypeVariableBinding)leafComponentType, this);
            }
         } else if (this.targetType.isTypeVariable()) {
            scope.problemReporter().illegalClassLiteralForTypeVariable((TypeVariableBinding)this.targetType, this);
         }

         ReferenceBinding classType = scope.getJavaLangClass();
         if (scope.compilerOptions().sourceLevel >= 3211264L) {
            TypeBinding boxedType = null;
            if (this.targetType.id == 6) {
               boxedType = environment.getResolvedType(JAVA_LANG_VOID, scope);
            } else {
               boxedType = scope.boxing(this.targetType);
            }

            if (environment.usesNullTypeAnnotations()) {
               boxedType = environment.createAnnotatedType(boxedType, new AnnotationBinding[]{environment.getNonNullAnnotation()});
            }

            this.resolvedType = environment.createParameterizedType(classType, new TypeBinding[]{boxedType}, null);
         } else {
            this.resolvedType = classType;
         }

         return this.resolvedType;
      }
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope blockScope) {
      if (visitor.visit(this, blockScope)) {
         this.type.traverse(visitor, blockScope);
      }

      visitor.endVisit(this, blockScope);
   }
}
