package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.flow.ExceptionHandlingFlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.InitializationFlowContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.AbortMethod;

public class Clinit extends AbstractMethodDeclaration {
   private static int ENUM_CONSTANTS_THRESHOLD = 2000;
   private FieldBinding assertionSyntheticFieldBinding = null;
   private FieldBinding classLiteralSyntheticField = null;

   public Clinit(CompilationResult compilationResult) {
      super(compilationResult);
      this.modifiers = 0;
      this.selector = TypeConstants.CLINIT;
   }

   public void analyseCode(ClassScope classScope, InitializationFlowContext staticInitializerFlowContext, FlowInfo flowInfo) {
      if (!this.ignoreFurtherInvestigation) {
         try {
            ExceptionHandlingFlowContext clinitContext = new ExceptionHandlingFlowContext(
               staticInitializerFlowContext.parent, this, Binding.NO_EXCEPTIONS, staticInitializerFlowContext, this.scope, FlowInfo.DEAD_END
            );
            if ((flowInfo.tagBits & 1) == 0) {
               this.bits |= 64;
            }

            FlowInfo var10 = flowInfo.mergedWith(staticInitializerFlowContext.initsOnReturn);
            FieldBinding[] fields = this.scope.enclosingSourceType().fields();
            int i = 0;

            for(int count = fields.length; i < count; ++i) {
               FieldBinding field = fields[i];
               if (field.isStatic() && !var10.isDefinitelyAssigned(field)) {
                  if (field.isFinal()) {
                     this.scope.problemReporter().uninitializedBlankFinalField(field, this.scope.referenceType().declarationOf(field.original()));
                  } else if (field.isNonNull()) {
                     this.scope.problemReporter().uninitializedNonNullField(field, this.scope.referenceType().declarationOf(field.original()));
                  }
               }
            }

            staticInitializerFlowContext.checkInitializerExceptions(this.scope, clinitContext, var10);
         } catch (AbortMethod var9) {
            this.ignoreFurtherInvestigation = true;
         }
      }
   }

   @Override
   public void generateCode(ClassScope classScope, ClassFile classFile) {
      int clinitOffset = 0;
      if (!this.ignoreFurtherInvestigation) {
         CompilationResult unitResult = null;
         int problemCount = 0;
         if (classScope != null) {
            TypeDeclaration referenceContext = classScope.referenceContext;
            if (referenceContext != null) {
               unitResult = referenceContext.compilationResult();
               problemCount = unitResult.problemCount;
            }
         }

         boolean restart = false;

         do {
            try {
               clinitOffset = classFile.contentsOffset;
               this.generateCode(classScope, classFile, clinitOffset);
               restart = false;
            } catch (AbortMethod var8) {
               if (var8.compilationResult == CodeStream.RESTART_IN_WIDE_MODE) {
                  classFile.contentsOffset = clinitOffset;
                  --classFile.methodCount;
                  classFile.codeStream.resetInWideMode();
                  if (unitResult != null) {
                     unitResult.problemCount = problemCount;
                  }

                  restart = true;
               } else if (var8.compilationResult == CodeStream.RESTART_CODE_GEN_FOR_UNUSED_LOCALS_MODE) {
                  classFile.contentsOffset = clinitOffset;
                  --classFile.methodCount;
                  classFile.codeStream.resetForCodeGenUnusedLocals();
                  if (unitResult != null) {
                     unitResult.problemCount = problemCount;
                  }

                  restart = true;
               } else {
                  classFile.contentsOffset = clinitOffset;
                  --classFile.methodCount;
                  restart = false;
               }
            }
         } while(restart);
      }
   }

   private void generateCode(ClassScope classScope, ClassFile classFile, int clinitOffset) {
      ConstantPool constantPool = classFile.constantPool;
      int constantPoolOffset = constantPool.currentOffset;
      int constantPoolIndex = constantPool.currentIndex;
      classFile.generateMethodInfoHeaderForClinit();
      int codeAttributeOffset = classFile.contentsOffset;
      classFile.generateCodeAttributeHeader();
      CodeStream codeStream = classFile.codeStream;
      this.resolve(classScope);
      codeStream.reset(this, classFile);
      TypeDeclaration declaringType = classScope.referenceContext;
      MethodScope staticInitializerScope = declaringType.staticInitializerScope;
      staticInitializerScope.computeLocalVariablePositions(0, codeStream);
      if (this.assertionSyntheticFieldBinding != null) {
         codeStream.generateClassLiteralAccessForType(classScope.outerMostClassScope().enclosingSourceType(), this.classLiteralSyntheticField);
         codeStream.invokeJavaLangClassDesiredAssertionStatus();
         BranchLabel falseLabel = new BranchLabel(codeStream);
         codeStream.ifne(falseLabel);
         codeStream.iconst_1();
         BranchLabel jumpLabel = new BranchLabel(codeStream);
         codeStream.decrStackSize(1);
         codeStream.goto_(jumpLabel);
         falseLabel.place();
         codeStream.iconst_0();
         jumpLabel.place();
         codeStream.fieldAccess((byte)-77, this.assertionSyntheticFieldBinding, null);
      }

      FieldDeclaration[] fieldDeclarations = declaringType.fields;
      int sourcePosition = -1;
      int remainingFieldCount = 0;
      if (TypeDeclaration.kind(declaringType.modifiers) == 3) {
         int enumCount = declaringType.enumConstantsCounter;
         if (enumCount <= ENUM_CONSTANTS_THRESHOLD) {
            if (fieldDeclarations != null) {
               int i = 0;

               for(int max = fieldDeclarations.length; i < max; ++i) {
                  FieldDeclaration fieldDecl = fieldDeclarations[i];
                  if (fieldDecl.isStatic()) {
                     if (fieldDecl.getKind() == 3) {
                        fieldDecl.generateCode(staticInitializerScope, codeStream);
                     } else {
                        ++remainingFieldCount;
                     }
                  }
               }
            }
         } else {
            int begin = -1;
            int count = 0;
            if (fieldDeclarations != null) {
               int max = fieldDeclarations.length;

               for(int i = 0; i < max; ++i) {
                  FieldDeclaration fieldDecl = fieldDeclarations[i];
                  if (fieldDecl.isStatic()) {
                     if (fieldDecl.getKind() == 3) {
                        if (begin == -1) {
                           begin = i;
                        }

                        if (++count > ENUM_CONSTANTS_THRESHOLD) {
                           SyntheticMethodBinding syntheticMethod = declaringType.binding.addSyntheticMethodForEnumInitialization(begin, i);
                           codeStream.invoke((byte)-72, syntheticMethod, null);
                           begin = i;
                           count = 1;
                        }
                     } else {
                        ++remainingFieldCount;
                     }
                  }
               }

               if (count != 0) {
                  SyntheticMethodBinding syntheticMethod = declaringType.binding.addSyntheticMethodForEnumInitialization(begin, max);
                  codeStream.invoke((byte)-72, syntheticMethod, null);
               }
            }
         }

         codeStream.generateInlinedValue(enumCount);
         codeStream.anewarray(declaringType.binding);
         if (enumCount > 0 && fieldDeclarations != null) {
            int i = 0;

            for(int max = fieldDeclarations.length; i < max; ++i) {
               FieldDeclaration fieldDecl = fieldDeclarations[i];
               if (fieldDecl.getKind() == 3) {
                  codeStream.dup();
                  codeStream.generateInlinedValue(fieldDecl.binding.id);
                  codeStream.fieldAccess((byte)-78, fieldDecl.binding, null);
                  codeStream.aastore();
               }
            }
         }

         codeStream.fieldAccess((byte)-77, declaringType.enumValuesSyntheticfield, null);
         if (remainingFieldCount != 0) {
            int i = 0;

            for(int max = fieldDeclarations.length; i < max && remainingFieldCount >= 0; ++i) {
               FieldDeclaration fieldDecl = fieldDeclarations[i];
               switch(fieldDecl.getKind()) {
                  case 1:
                     if (fieldDecl.binding.isStatic()) {
                        --remainingFieldCount;
                        sourcePosition = fieldDecl.declarationEnd;
                        fieldDecl.generateCode(staticInitializerScope, codeStream);
                     }
                     break;
                  case 2:
                     if (fieldDecl.isStatic()) {
                        --remainingFieldCount;
                        sourcePosition = ((Initializer)fieldDecl).block.sourceEnd;
                        fieldDecl.generateCode(staticInitializerScope, codeStream);
                     }
                  case 3:
               }
            }
         }
      } else if (fieldDeclarations != null) {
         int i = 0;

         for(int max = fieldDeclarations.length; i < max; ++i) {
            FieldDeclaration fieldDecl = fieldDeclarations[i];
            switch(fieldDecl.getKind()) {
               case 1:
                  if (fieldDecl.binding.isStatic()) {
                     sourcePosition = fieldDecl.declarationEnd;
                     fieldDecl.generateCode(staticInitializerScope, codeStream);
                  }
                  break;
               case 2:
                  if (fieldDecl.isStatic()) {
                     sourcePosition = ((Initializer)fieldDecl).block.sourceEnd;
                     fieldDecl.generateCode(staticInitializerScope, codeStream);
                  }
            }
         }
      }

      if (codeStream.position == 0) {
         classFile.contentsOffset = clinitOffset;
         --classFile.methodCount;
         constantPool.resetForClinit(constantPoolIndex, constantPoolOffset);
      } else {
         if ((this.bits & 64) != 0) {
            int before = codeStream.position;
            codeStream.return_();
            if (sourcePosition != -1) {
               codeStream.recordPositionsFrom(before, sourcePosition);
            }
         }

         codeStream.recordPositionsFrom(0, declaringType.sourceStart);
         classFile.completeCodeAttributeForClinit(codeAttributeOffset);
      }
   }

   @Override
   public boolean isClinit() {
      return true;
   }

   @Override
   public boolean isInitializationMethod() {
      return true;
   }

   @Override
   public boolean isStatic() {
      return true;
   }

   @Override
   public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
   }

   @Override
   public StringBuffer print(int tab, StringBuffer output) {
      printIndent(tab, output).append("<clinit>()");
      this.printBody(tab + 1, output);
      return output;
   }

   @Override
   public void resolve(ClassScope classScope) {
      this.scope = new MethodScope(classScope, classScope.referenceContext, true);
   }

   @Override
   public void traverse(ASTVisitor visitor, ClassScope classScope) {
      visitor.visit(this, classScope);
      visitor.endVisit(this, classScope);
   }

   public void setAssertionSupport(FieldBinding assertionSyntheticFieldBinding, boolean needClassLiteralField) {
      this.assertionSyntheticFieldBinding = assertionSyntheticFieldBinding;
      if (needClassLiteralField) {
         SourceTypeBinding sourceType = this.scope.outerMostClassScope().enclosingSourceType();
         if (!sourceType.isInterface() && !sourceType.isBaseType()) {
            this.classLiteralSyntheticField = sourceType.addSyntheticFieldForClassLiteral(sourceType, this.scope);
         }
      }
   }
}
