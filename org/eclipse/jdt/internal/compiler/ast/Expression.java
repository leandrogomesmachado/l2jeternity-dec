package org.eclipse.jdt.internal.compiler.ast;

import java.util.ArrayList;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.WildcardBinding;
import org.eclipse.jdt.internal.compiler.problem.ShouldNotImplement;
import org.eclipse.jdt.internal.compiler.util.Messages;

public abstract class Expression extends Statement {
   public Constant constant;
   public int statementEnd = -1;
   public int implicitConversion;
   public TypeBinding resolvedType;
   public static Expression[] NO_EXPRESSIONS = new Expression[0];

   public static final boolean isConstantValueRepresentable(Constant constant, int constantTypeID, int targetTypeID) {
      if (targetTypeID == constantTypeID) {
         return true;
      } else {
         switch(targetTypeID) {
            case 2:
               switch(constantTypeID) {
                  case 2:
                     return true;
                  case 3:
                     if (constant.byteValue() == constant.charValue()) {
                        return true;
                     }

                     return false;
                  case 4:
                     if (constant.shortValue() == constant.charValue()) {
                        return true;
                     }

                     return false;
                  case 5:
                  case 6:
                  default:
                     return false;
                  case 7:
                     if (constant.longValue() == (long)constant.charValue()) {
                        return true;
                     }

                     return false;
                  case 8:
                     if (constant.doubleValue() == (double)constant.charValue()) {
                        return true;
                     }

                     return false;
                  case 9:
                     if (constant.floatValue() == (float)constant.charValue()) {
                        return true;
                     }

                     return false;
                  case 10:
                     if (constant.intValue() == constant.charValue()) {
                        return true;
                     }

                     return false;
               }
            case 3:
               switch(constantTypeID) {
                  case 2:
                     if (constant.charValue() == constant.byteValue()) {
                        return true;
                     }

                     return false;
                  case 3:
                     return true;
                  case 4:
                     if (constant.shortValue() == constant.byteValue()) {
                        return true;
                     }

                     return false;
                  case 5:
                  case 6:
                  default:
                     return false;
                  case 7:
                     if (constant.longValue() == (long)constant.byteValue()) {
                        return true;
                     }

                     return false;
                  case 8:
                     if (constant.doubleValue() == (double)constant.byteValue()) {
                        return true;
                     }

                     return false;
                  case 9:
                     if (constant.floatValue() == (float)constant.byteValue()) {
                        return true;
                     }

                     return false;
                  case 10:
                     if (constant.intValue() == constant.byteValue()) {
                        return true;
                     }

                     return false;
               }
            case 4:
               switch(constantTypeID) {
                  case 2:
                     if (constant.charValue() == constant.shortValue()) {
                        return true;
                     }

                     return false;
                  case 3:
                     if (constant.byteValue() == constant.shortValue()) {
                        return true;
                     }

                     return false;
                  case 4:
                     return true;
                  case 5:
                  case 6:
                  default:
                     return false;
                  case 7:
                     if (constant.longValue() == (long)constant.shortValue()) {
                        return true;
                     }

                     return false;
                  case 8:
                     if (constant.doubleValue() == (double)constant.shortValue()) {
                        return true;
                     }

                     return false;
                  case 9:
                     if (constant.floatValue() == (float)constant.shortValue()) {
                        return true;
                     }

                     return false;
                  case 10:
                     if (constant.intValue() == constant.shortValue()) {
                        return true;
                     }

                     return false;
               }
            case 5:
            case 6:
            default:
               return false;
            case 7:
               switch(constantTypeID) {
                  case 2:
                     if ((long)constant.charValue() == constant.longValue()) {
                        return true;
                     }

                     return false;
                  case 3:
                     if ((long)constant.byteValue() == constant.longValue()) {
                        return true;
                     }

                     return false;
                  case 4:
                     if ((long)constant.shortValue() == constant.longValue()) {
                        return true;
                     }

                     return false;
                  case 5:
                  case 6:
                  default:
                     return false;
                  case 7:
                     return true;
                  case 8:
                     if (constant.doubleValue() == (double)constant.longValue()) {
                        return true;
                     }

                     return false;
                  case 9:
                     if (constant.floatValue() == (float)constant.longValue()) {
                        return true;
                     }

                     return false;
                  case 10:
                     if ((long)constant.intValue() == constant.longValue()) {
                        return true;
                     }

                     return false;
               }
            case 8:
               switch(constantTypeID) {
                  case 2:
                     if ((double)constant.charValue() == constant.doubleValue()) {
                        return true;
                     }

                     return false;
                  case 3:
                     if ((double)constant.byteValue() == constant.doubleValue()) {
                        return true;
                     }

                     return false;
                  case 4:
                     if ((double)constant.shortValue() == constant.doubleValue()) {
                        return true;
                     }

                     return false;
                  case 5:
                  case 6:
                  default:
                     return false;
                  case 7:
                     if ((double)constant.longValue() == constant.doubleValue()) {
                        return true;
                     }

                     return false;
                  case 8:
                     return true;
                  case 9:
                     if ((double)constant.floatValue() == constant.doubleValue()) {
                        return true;
                     }

                     return false;
                  case 10:
                     if ((double)constant.intValue() == constant.doubleValue()) {
                        return true;
                     }

                     return false;
               }
            case 9:
               switch(constantTypeID) {
                  case 2:
                     if ((float)constant.charValue() == constant.floatValue()) {
                        return true;
                     }

                     return false;
                  case 3:
                     if ((float)constant.byteValue() == constant.floatValue()) {
                        return true;
                     }

                     return false;
                  case 4:
                     if ((float)constant.shortValue() == constant.floatValue()) {
                        return true;
                     }

                     return false;
                  case 5:
                  case 6:
                  default:
                     return false;
                  case 7:
                     if ((float)constant.longValue() == constant.floatValue()) {
                        return true;
                     }

                     return false;
                  case 8:
                     if (constant.doubleValue() == (double)constant.floatValue()) {
                        return true;
                     }

                     return false;
                  case 9:
                     return true;
                  case 10:
                     if ((float)constant.intValue() == constant.floatValue()) {
                        return true;
                     }

                     return false;
               }
            case 10:
               switch(constantTypeID) {
                  case 2:
                     if (constant.charValue() == constant.intValue()) {
                        return true;
                     }

                     return false;
                  case 3:
                     if (constant.byteValue() == constant.intValue()) {
                        return true;
                     }

                     return false;
                  case 4:
                     if (constant.shortValue() == constant.intValue()) {
                        return true;
                     }

                     return false;
                  case 5:
                  case 6:
                  default:
                     return false;
                  case 7:
                     if (constant.longValue() == (long)constant.intValue()) {
                        return true;
                     }

                     return false;
                  case 8:
                     if (constant.doubleValue() == (double)constant.intValue()) {
                        return true;
                     }

                     return false;
                  case 9:
                     if (constant.floatValue() == (float)constant.intValue()) {
                        return true;
                     }

                     return false;
                  case 10:
                     return true;
               }
         }
      }
   }

   @Override
   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
      return flowInfo;
   }

   public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {
      return this.analyseCode(currentScope, flowContext, flowInfo);
   }

   public final boolean checkCastTypesCompatibility(Scope scope, TypeBinding castType, TypeBinding expressionType, Expression expression) {
      if (castType != null && expressionType != null) {
         boolean use15specifics = scope.compilerOptions().sourceLevel >= 3211264L;
         boolean use17specifics = scope.compilerOptions().sourceLevel >= 3342336L;
         if (castType.isBaseType()) {
            if (expressionType.isBaseType()) {
               if (TypeBinding.equalsEquals(expressionType, castType)) {
                  if (expression != null) {
                     this.constant = expression.constant;
                  }

                  this.tagAsUnnecessaryCast(scope, castType);
                  return true;
               }

               boolean necessary = false;
               if (expressionType.isCompatibleWith(castType) || (necessary = BaseTypeBinding.isNarrowing(castType.id, expressionType.id))) {
                  if (expression != null) {
                     expression.implicitConversion = (castType.id << 4) + expressionType.id;
                     if (expression.constant != Constant.NotAConstant) {
                        this.constant = expression.constant.castTo(expression.implicitConversion);
                     }
                  }

                  if (!necessary) {
                     this.tagAsUnnecessaryCast(scope, castType);
                  }

                  return true;
               }
            } else {
               if (use17specifics
                  && castType.isPrimitiveType()
                  && expressionType instanceof ReferenceBinding
                  && !expressionType.isBoxedPrimitiveType()
                  && this.checkCastTypesCompatibility(scope, scope.boxing(castType), expressionType, expression)) {
                  return true;
               }

               if (use15specifics && scope.environment().computeBoxingType(expressionType).isCompatibleWith(castType)) {
                  this.tagAsUnnecessaryCast(scope, castType);
                  return true;
               }
            }

            return false;
         } else if (use15specifics && expressionType.isBaseType() && scope.environment().computeBoxingType(expressionType).isCompatibleWith(castType)) {
            this.tagAsUnnecessaryCast(scope, castType);
            return true;
         } else if (castType.isIntersectionType18()) {
            ReferenceBinding[] intersectingTypes = castType.getIntersectingTypes();
            int i = 0;

            for(int length = intersectingTypes.length; i < length; ++i) {
               if (!this.checkCastTypesCompatibility(scope, intersectingTypes[i], expressionType, expression)) {
                  return false;
               }
            }

            return true;
         } else {
            switch(expressionType.kind()) {
               case 68:
                  if (TypeBinding.equalsEquals(castType, expressionType)) {
                     this.tagAsUnnecessaryCast(scope, castType);
                     return true;
                  } else {
                     switch(castType.kind()) {
                        case 68:
                           TypeBinding castElementType = ((ArrayBinding)castType).elementsType();
                           TypeBinding exprElementType = ((ArrayBinding)expressionType).elementsType();
                           if (!exprElementType.isBaseType() && !castElementType.isBaseType()) {
                              return this.checkCastTypesCompatibility(scope, castElementType, exprElementType, expression);
                           } else {
                              if (TypeBinding.equalsEquals(castElementType, exprElementType)) {
                                 this.tagAsNeedCheckCast();
                                 return true;
                              }

                              return false;
                           }
                        case 4100:
                           TypeBinding match = expressionType.findSuperTypeOriginatingFrom(castType);
                           if (match == null) {
                              this.checkUnsafeCast(scope, castType, expressionType, null, true);
                           }

                           return this.checkCastTypesCompatibility(scope, ((TypeVariableBinding)castType).upperBound(), expressionType, expression);
                        default:
                           switch(castType.id) {
                              case 1:
                                 this.tagAsUnnecessaryCast(scope, castType);
                                 return true;
                              case 36:
                              case 37:
                                 this.tagAsNeedCheckCast();
                                 return true;
                              default:
                                 return false;
                           }
                     }
                  }
               case 132:
                  if (expressionType == TypeBinding.NULL) {
                     this.tagAsUnnecessaryCast(scope, castType);
                     return true;
                  }

                  return false;
               case 516:
               case 8196:
                  TypeBinding match = expressionType.findSuperTypeOriginatingFrom(castType);
                  if (match != null) {
                     return this.checkUnsafeCast(scope, castType, expressionType, match, false);
                  }

                  TypeBinding bound = ((WildcardBinding)expressionType).bound;
                  if (bound == null) {
                     bound = scope.getJavaLangObject();
                  }

                  return this.checkCastTypesCompatibility(scope, castType, bound, expression);
               case 4100:
                  TypeBinding match = expressionType.findSuperTypeOriginatingFrom(castType);
                  if (match != null) {
                     return this.checkUnsafeCast(scope, castType, expressionType, match, false);
                  }

                  return this.checkCastTypesCompatibility(scope, castType, ((TypeVariableBinding)expressionType).upperBound(), expression);
               case 32772:
                  ReferenceBinding[] intersectingTypes = expressionType.getIntersectingTypes();
                  int i = 0;

                  for(int length = intersectingTypes.length; i < length; ++i) {
                     if (this.checkCastTypesCompatibility(scope, castType, intersectingTypes[i], expression)) {
                        return true;
                     }
                  }

                  return false;
               default:
                  if (!expressionType.isInterface()) {
                     switch(castType.kind()) {
                        case 68:
                           if (expressionType.id == 1) {
                              if (use15specifics) {
                                 this.checkUnsafeCast(scope, castType, expressionType, expressionType, true);
                              }

                              this.tagAsNeedCheckCast();
                              return true;
                           }

                           return false;
                        case 4100:
                           TypeBinding match = expressionType.findSuperTypeOriginatingFrom(castType);
                           if (match == null) {
                              this.checkUnsafeCast(scope, castType, expressionType, null, true);
                           }

                           return this.checkCastTypesCompatibility(scope, ((TypeVariableBinding)castType).upperBound(), expressionType, expression);
                        default:
                           if (castType.isInterface()) {
                              ReferenceBinding refExprType = (ReferenceBinding)expressionType;
                              TypeBinding match = refExprType.findSuperTypeOriginatingFrom(castType);
                              if (match != null) {
                                 return this.checkUnsafeCast(scope, castType, expressionType, match, false);
                              } else if (refExprType.isFinal()) {
                                 return false;
                              } else {
                                 this.tagAsNeedCheckCast();
                                 match = castType.findSuperTypeOriginatingFrom(expressionType);
                                 if (match != null) {
                                    return this.checkUnsafeCast(scope, castType, expressionType, match, true);
                                 } else {
                                    if (use15specifics) {
                                       this.checkUnsafeCast(scope, castType, expressionType, null, true);
                                       if (scope.compilerOptions().complianceLevel < 3342336L) {
                                          if (refExprType.hasIncompatibleSuperType((ReferenceBinding)castType)) {
                                             return false;
                                          }
                                       } else if (!castType.isRawType() && refExprType.hasIncompatibleSuperType((ReferenceBinding)castType)) {
                                          return false;
                                       }
                                    }

                                    return true;
                                 }
                              }
                           } else {
                              TypeBinding match = expressionType.findSuperTypeOriginatingFrom(castType);
                              if (match != null) {
                                 if (expression != null && castType.id == 11) {
                                    this.constant = expression.constant;
                                 }

                                 return this.checkUnsafeCast(scope, castType, expressionType, match, false);
                              } else {
                                 match = castType.findSuperTypeOriginatingFrom(expressionType);
                                 if (match != null) {
                                    this.tagAsNeedCheckCast();
                                    return this.checkUnsafeCast(scope, castType, expressionType, match, true);
                                 } else {
                                    return false;
                                 }
                              }
                           }
                     }
                  } else {
                     switch(castType.kind()) {
                        case 68:
                           switch(expressionType.id) {
                              case 36:
                              case 37:
                                 this.tagAsNeedCheckCast();
                                 return true;
                              default:
                                 return false;
                           }
                        case 4100:
                           TypeBinding match = expressionType.findSuperTypeOriginatingFrom(castType);
                           if (match == null) {
                              this.checkUnsafeCast(scope, castType, expressionType, null, true);
                           }

                           return this.checkCastTypesCompatibility(scope, ((TypeVariableBinding)castType).upperBound(), expressionType, expression);
                        default:
                           if (castType.isInterface()) {
                              ReferenceBinding interfaceType = (ReferenceBinding)expressionType;
                              TypeBinding match = interfaceType.findSuperTypeOriginatingFrom(castType);
                              if (match != null) {
                                 return this.checkUnsafeCast(scope, castType, interfaceType, match, false);
                              } else {
                                 this.tagAsNeedCheckCast();
                                 match = castType.findSuperTypeOriginatingFrom(interfaceType);
                                 if (match != null) {
                                    return this.checkUnsafeCast(scope, castType, interfaceType, match, true);
                                 } else {
                                    if (use15specifics) {
                                       this.checkUnsafeCast(scope, castType, expressionType, null, true);
                                       if (scope.compilerOptions().complianceLevel < 3342336L) {
                                          if (interfaceType.hasIncompatibleSuperType((ReferenceBinding)castType)) {
                                             return false;
                                          }
                                       } else if (!castType.isRawType() && interfaceType.hasIncompatibleSuperType((ReferenceBinding)castType)) {
                                          return false;
                                       }
                                    } else {
                                       MethodBinding[] castTypeMethods = this.getAllOriginalInheritedMethods((ReferenceBinding)castType);
                                       MethodBinding[] expressionTypeMethods = this.getAllOriginalInheritedMethods((ReferenceBinding)expressionType);
                                       int exprMethodsLength = expressionTypeMethods.length;
                                       int i = 0;

                                       for(int castMethodsLength = castTypeMethods.length; i < castMethodsLength; ++i) {
                                          for(int j = 0; j < exprMethodsLength; ++j) {
                                             if (TypeBinding.notEquals(castTypeMethods[i].returnType, expressionTypeMethods[j].returnType)
                                                && CharOperation.equals(castTypeMethods[i].selector, expressionTypeMethods[j].selector)
                                                && castTypeMethods[i].areParametersEqual(expressionTypeMethods[j])) {
                                                return false;
                                             }
                                          }
                                       }
                                    }

                                    return true;
                                 }
                              }
                           } else if (castType.id == 1) {
                              this.tagAsUnnecessaryCast(scope, castType);
                              return true;
                           } else {
                              this.tagAsNeedCheckCast();
                              TypeBinding match = castType.findSuperTypeOriginatingFrom(expressionType);
                              if (match != null) {
                                 return this.checkUnsafeCast(scope, castType, expressionType, match, true);
                              } else if (((ReferenceBinding)castType).isFinal()) {
                                 return false;
                              } else {
                                 if (use15specifics) {
                                    this.checkUnsafeCast(scope, castType, expressionType, null, true);
                                    if (scope.compilerOptions().complianceLevel < 3342336L) {
                                       if (((ReferenceBinding)castType).hasIncompatibleSuperType((ReferenceBinding)expressionType)) {
                                          return false;
                                       }
                                    } else if (!castType.isRawType()
                                       && ((ReferenceBinding)castType).hasIncompatibleSuperType((ReferenceBinding)expressionType)) {
                                       return false;
                                    }
                                 }

                                 return true;
                              }
                           }
                     }
                  }
            }
         }
      } else {
         return true;
      }
   }

   public boolean checkNPE(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo, int ttlForFieldCheck) {
      boolean isNullable = false;
      if (this.resolvedType != null) {
         if ((this.resolvedType.tagBits & 72057594037927936L) != 0L) {
            return true;
         }

         if ((this.resolvedType.tagBits & 36028797018963968L) != 0L) {
            isNullable = true;
         }
      }

      LocalVariableBinding local = this.localVariableBinding();
      if (local != null && (local.type.tagBits & 2L) == 0L) {
         if ((this.bits & 131072) == 0) {
            flowContext.recordUsingNullReference(scope, local, this, 3, flowInfo);
            if (!flowInfo.isDefinitelyNonNull(local)) {
               flowContext.recordAbruptExit();
            }
         }

         flowInfo.markAsComparedEqualToNonNull(local);
         flowContext.markFinallyNullStatus(local, 4);
         return true;
      } else if (isNullable) {
         scope.problemReporter().dereferencingNullableExpression(this);
         return true;
      } else {
         return false;
      }
   }

   public boolean checkNPE(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo) {
      return this.checkNPE(scope, flowContext, flowInfo, 0);
   }

   protected void checkNPEbyUnboxing(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo) {
      int status;
      if ((this.implicitConversion & 1024) != 0 && (this.bits & 131072) == 0 && (status = this.nullStatus(flowInfo, flowContext)) != 4) {
         flowContext.recordUnboxing(scope, this, status, flowInfo);
      }
   }

   public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
      if (TypeBinding.equalsEquals(match, castType)) {
         if (!isNarrowing) {
            this.tagAsUnnecessaryCast(scope, castType);
         }

         return true;
      } else if (match == null
         || castType.isReifiable() && expressionType.isReifiable()
         || (isNarrowing ? !match.isProvablyDistinct(expressionType) : !castType.isProvablyDistinct(match))) {
         if (!isNarrowing) {
            this.tagAsUnnecessaryCast(scope, castType);
         }

         return true;
      } else {
         return false;
      }
   }

   public void computeConversion(Scope scope, TypeBinding runtimeType, TypeBinding compileTimeType) {
      if (runtimeType != null && compileTimeType != null) {
         if (this.implicitConversion == 0) {
            if (runtimeType != TypeBinding.NULL && runtimeType.isBaseType()) {
               if (!compileTimeType.isBaseType()) {
                  TypeBinding unboxedType = scope.environment().computeBoxingType(compileTimeType);
                  this.implicitConversion = 1024;
                  scope.problemReporter().autoboxing(this, compileTimeType, runtimeType);
                  compileTimeType = unboxedType;
               }
            } else {
               if (compileTimeType != TypeBinding.NULL && compileTimeType.isBaseType()) {
                  TypeBinding boxedType = scope.environment().computeBoxingType(runtimeType);
                  if (TypeBinding.equalsEquals(boxedType, runtimeType)) {
                     boxedType = compileTimeType;
                  }

                  if (boxedType.id >= 128) {
                     boxedType = compileTimeType;
                  }

                  this.implicitConversion = 512 | (boxedType.id << 4) + compileTimeType.id;
                  scope.problemReporter().autoboxing(this, compileTimeType, scope.environment().computeBoxingType(boxedType));
                  return;
               }

               if (this.constant != Constant.NotAConstant && this.constant.typeID() != 11) {
                  this.implicitConversion = 512;
                  return;
               }
            }

            int compileTimeTypeID = compileTimeType.id;
            if (compileTimeType.id >= 128) {
               compileTimeTypeID = compileTimeType.erasure().id == 11 ? 11 : 1;
            } else if (runtimeType.isPrimitiveType() && compileTimeType instanceof ReferenceBinding && !compileTimeType.isBoxedPrimitiveType()) {
               compileTimeTypeID = 1;
            }

            int runtimeTypeID = runtimeType.id;
            switch(runtimeType.id) {
               case 2:
               case 3:
               case 4:
                  if (compileTimeTypeID == 1) {
                     this.implicitConversion |= (runtimeTypeID << 4) + compileTimeTypeID;
                  } else {
                     this.implicitConversion |= 160 + compileTimeTypeID;
                  }
                  break;
               case 5:
               case 7:
               case 8:
               case 9:
               case 10:
               case 11:
                  this.implicitConversion |= (runtimeTypeID << 4) + compileTimeTypeID;
               case 6:
            }
         }
      }
   }

   @Override
   public void generateCode(BlockScope currentScope, CodeStream codeStream) {
      if ((this.bits & -2147483648) != 0) {
         this.generateCode(currentScope, codeStream, false);
      }
   }

   public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
      if (this.constant != Constant.NotAConstant) {
         int pc = codeStream.position;
         codeStream.generateConstant(this.constant, this.implicitConversion);
         codeStream.recordPositionsFrom(pc, this.sourceStart);
      } else {
         throw new ShouldNotImplement(Messages.ast_missingCode);
      }
   }

   public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, BranchLabel trueLabel, BranchLabel falseLabel, boolean valueRequired) {
      Constant cst = this.optimizedBooleanConstant();
      this.generateCode(currentScope, codeStream, valueRequired && cst == Constant.NotAConstant);
      if (cst != Constant.NotAConstant && cst.typeID() == 5) {
         int pc = codeStream.position;
         if (cst.booleanValue()) {
            if (valueRequired && falseLabel == null && trueLabel != null) {
               codeStream.goto_(trueLabel);
            }
         } else if (valueRequired && falseLabel != null && trueLabel == null) {
            codeStream.goto_(falseLabel);
         }

         codeStream.recordPositionsFrom(pc, this.sourceStart);
      } else {
         int position = codeStream.position;
         if (valueRequired) {
            if (falseLabel == null) {
               if (trueLabel != null) {
                  codeStream.ifne(trueLabel);
               }
            } else if (trueLabel == null) {
               codeStream.ifeq(falseLabel);
            }
         }

         codeStream.recordPositionsFrom(position, this.sourceEnd);
      }
   }

   public void generateOptimizedStringConcatenation(BlockScope blockScope, CodeStream codeStream, int typeID) {
      if (typeID != 11 || this.constant == Constant.NotAConstant || this.constant.stringValue().length() != 0) {
         this.generateCode(blockScope, codeStream, true);
         codeStream.invokeStringConcatenationAppendForType(typeID);
      }
   }

   public void generateOptimizedStringConcatenationCreation(BlockScope blockScope, CodeStream codeStream, int typeID) {
      codeStream.newStringContatenation();
      codeStream.dup();
      switch(typeID) {
         case 0:
         case 1:
            codeStream.invokeStringConcatenationDefaultConstructor();
            this.generateCode(blockScope, codeStream, true);
            codeStream.invokeStringConcatenationAppendForType(1);
            return;
         case 11:
         case 12:
            if (this.constant != Constant.NotAConstant) {
               String stringValue = this.constant.stringValue();
               if (stringValue.length() == 0) {
                  codeStream.invokeStringConcatenationDefaultConstructor();
                  return;
               }

               codeStream.ldc(stringValue);
            } else {
               this.generateCode(blockScope, codeStream, true);
               codeStream.invokeStringValueOf(1);
            }
            break;
         default:
            this.generateCode(blockScope, codeStream, true);
            codeStream.invokeStringValueOf(typeID);
      }

      codeStream.invokeStringConcatenationStringConstructor();
   }

   private MethodBinding[] getAllOriginalInheritedMethods(ReferenceBinding binding) {
      ArrayList<MethodBinding> collector = new ArrayList<>();
      this.getAllInheritedMethods0(binding, collector);
      int i = 0;

      for(int len = collector.size(); i < len; ++i) {
         collector.set(i, collector.get(i).original());
      }

      return collector.toArray(new MethodBinding[collector.size()]);
   }

   private void getAllInheritedMethods0(ReferenceBinding binding, ArrayList<MethodBinding> collector) {
      if (binding.isInterface()) {
         MethodBinding[] methodBindings = binding.methods();
         int i = 0;

         for(int max = methodBindings.length; i < max; ++i) {
            collector.add(methodBindings[i]);
         }

         ReferenceBinding[] superInterfaces = binding.superInterfaces();
         int ix = 0;

         for(int max = superInterfaces.length; ix < max; ++ix) {
            this.getAllInheritedMethods0(superInterfaces[ix], collector);
         }
      }
   }

   public static Binding getDirectBinding(Expression someExpression) {
      if ((someExpression.bits & 536870912) != 0) {
         return null;
      } else if (someExpression instanceof SingleNameReference) {
         return ((SingleNameReference)someExpression).binding;
      } else {
         if (someExpression instanceof FieldReference) {
            FieldReference fieldRef = (FieldReference)someExpression;
            if (fieldRef.receiver.isThis() && !(fieldRef.receiver instanceof QualifiedThisReference)) {
               return fieldRef.binding;
            }
         } else if (someExpression instanceof Assignment) {
            Expression lhs = ((Assignment)someExpression).lhs;
            if ((lhs.bits & 8192) != 0) {
               return getDirectBinding(((Assignment)someExpression).lhs);
            }

            if (someExpression instanceof PrefixExpression) {
               return getDirectBinding(((Assignment)someExpression).lhs);
            }
         } else if (someExpression instanceof QualifiedNameReference) {
            QualifiedNameReference qualifiedNameReference = (QualifiedNameReference)someExpression;
            if (qualifiedNameReference.indexOfFirstFieldBinding != 1 && qualifiedNameReference.otherBindings == null) {
               return qualifiedNameReference.binding;
            }
         } else if (someExpression.isThis()) {
            return someExpression.resolvedType;
         }

         return null;
      }
   }

   public boolean isCompactableOperation() {
      return false;
   }

   public boolean isConstantValueOfTypeAssignableToType(TypeBinding constantType, TypeBinding targetType) {
      if (this.constant == Constant.NotAConstant) {
         return false;
      } else if (TypeBinding.equalsEquals(constantType, targetType)) {
         return true;
      } else {
         return BaseTypeBinding.isWidening(10, constantType.id) && BaseTypeBinding.isNarrowing(targetType.id, 10)
            ? isConstantValueRepresentable(this.constant, constantType.id, targetType.id)
            : false;
      }
   }

   public boolean isTypeReference() {
      return false;
   }

   public LocalVariableBinding localVariableBinding() {
      return null;
   }

   public void markAsNonNull() {
      this.bits |= 131072;
   }

   public int nullStatus(FlowInfo flowInfo, FlowContext flowContext) {
      return 4;
   }

   public Constant optimizedBooleanConstant() {
      return this.constant;
   }

   public boolean isPertinentToApplicability(TypeBinding targetType, MethodBinding method) {
      return true;
   }

   public TypeBinding postConversionType(Scope scope) {
      TypeBinding convertedType = this.resolvedType;
      int runtimeType = (this.implicitConversion & 0xFF) >> 4;
      switch(runtimeType) {
         case 2:
            convertedType = TypeBinding.CHAR;
            break;
         case 3:
            convertedType = TypeBinding.BYTE;
            break;
         case 4:
            convertedType = TypeBinding.SHORT;
            break;
         case 5:
            convertedType = TypeBinding.BOOLEAN;
         case 6:
         default:
            break;
         case 7:
            convertedType = TypeBinding.LONG;
            break;
         case 8:
            convertedType = TypeBinding.DOUBLE;
            break;
         case 9:
            convertedType = TypeBinding.FLOAT;
            break;
         case 10:
            convertedType = TypeBinding.INT;
      }

      if ((this.implicitConversion & 512) != 0) {
         convertedType = scope.environment().computeBoxingType(convertedType);
      }

      return convertedType;
   }

   @Override
   public StringBuffer print(int indent, StringBuffer output) {
      printIndent(indent, output);
      return this.printExpression(indent, output);
   }

   public abstract StringBuffer printExpression(int var1, StringBuffer var2);

   @Override
   public StringBuffer printStatement(int indent, StringBuffer output) {
      return this.print(indent, output).append(";");
   }

   @Override
   public void resolve(BlockScope scope) {
      this.resolveType(scope);
   }

   public TypeBinding resolveType(BlockScope scope) {
      return null;
   }

   public TypeBinding resolveType(ClassScope scope) {
      return null;
   }

   public TypeBinding resolveTypeExpecting(BlockScope scope, TypeBinding expectedType) {
      this.setExpectedType(expectedType);
      TypeBinding expressionType = this.resolveType(scope);
      if (expressionType == null) {
         return null;
      } else if (TypeBinding.equalsEquals(expressionType, expectedType)) {
         return expressionType;
      } else {
         if (!expressionType.isCompatibleWith(expectedType)) {
            if (!scope.isBoxingCompatibleWith(expressionType, expectedType)) {
               scope.problemReporter().typeMismatchError(expressionType, expectedType, this, null);
               return null;
            }

            this.computeConversion(scope, expectedType, expressionType);
         }

         return expressionType;
      }
   }

   public Expression resolveExpressionExpecting(TypeBinding targetType, Scope scope, InferenceContext18 context) {
      return this;
   }

   public boolean forcedToBeRaw(ReferenceContext referenceContext) {
      if (this instanceof NameReference) {
         Binding receiverBinding = ((NameReference)this).binding;
         if (receiverBinding.isParameter() && (((LocalVariableBinding)receiverBinding).tagBits & 512L) != 0L) {
            return true;
         }

         if (receiverBinding instanceof FieldBinding) {
            FieldBinding field = (FieldBinding)receiverBinding;
            if (field.type.isRawType()) {
               if (referenceContext instanceof AbstractMethodDeclaration) {
                  AbstractMethodDeclaration methodDecl = (AbstractMethodDeclaration)referenceContext;
                  if (TypeBinding.notEquals(field.declaringClass, methodDecl.binding.declaringClass)) {
                     return true;
                  }
               } else if (referenceContext instanceof TypeDeclaration) {
                  TypeDeclaration type = (TypeDeclaration)referenceContext;
                  if (TypeBinding.notEquals(field.declaringClass, type.binding)) {
                     return true;
                  }
               }
            }
         }
      } else if (this instanceof MessageSend) {
         if (!CharOperation.equals(((MessageSend)this).binding.declaringClass.getFileName(), referenceContext.compilationResult().getFileName())) {
            return true;
         }
      } else if (this instanceof FieldReference) {
         FieldBinding field = ((FieldReference)this).binding;
         if (!CharOperation.equals(field.declaringClass.getFileName(), referenceContext.compilationResult().getFileName())) {
            return true;
         }

         if (field.type.isRawType()) {
            if (referenceContext instanceof AbstractMethodDeclaration) {
               AbstractMethodDeclaration methodDecl = (AbstractMethodDeclaration)referenceContext;
               if (TypeBinding.notEquals(field.declaringClass, methodDecl.binding.declaringClass)) {
                  return true;
               }
            } else if (referenceContext instanceof TypeDeclaration) {
               TypeDeclaration type = (TypeDeclaration)referenceContext;
               if (TypeBinding.notEquals(field.declaringClass, type.binding)) {
                  return true;
               }
            }
         }
      } else if (this instanceof ConditionalExpression) {
         ConditionalExpression ternary = (ConditionalExpression)this;
         if (ternary.valueIfTrue.forcedToBeRaw(referenceContext) || ternary.valueIfFalse.forcedToBeRaw(referenceContext)) {
            return true;
         }
      }

      return false;
   }

   public Object reusableJSRTarget() {
      return this.constant != Constant.NotAConstant && (this.implicitConversion & 512) == 0 ? this.constant : null;
   }

   public void setExpectedType(TypeBinding expectedType) {
   }

   public void setExpressionContext(ExpressionContext context) {
   }

   public boolean isCompatibleWith(TypeBinding left, Scope scope) {
      return this.resolvedType != null && this.resolvedType.isCompatibleWith(left, scope);
   }

   public boolean isBoxingCompatibleWith(TypeBinding left, Scope scope) {
      return this.resolvedType != null && this.isBoxingCompatible(this.resolvedType, left, this, scope);
   }

   public boolean sIsMoreSpecific(TypeBinding s, TypeBinding t, Scope scope) {
      return s.isCompatibleWith(t, scope);
   }

   public boolean isExactMethodReference() {
      return false;
   }

   public boolean isPolyExpression() throws UnsupportedOperationException {
      return false;
   }

   public boolean isPolyExpression(MethodBinding method) {
      return false;
   }

   public void tagAsNeedCheckCast() {
   }

   public void tagAsUnnecessaryCast(Scope scope, TypeBinding castType) {
   }

   public Expression toTypeReference() {
      return this;
   }

   @Override
   public void traverse(ASTVisitor visitor, BlockScope scope) {
   }

   public void traverse(ASTVisitor visitor, ClassScope scope) {
   }

   public boolean statementExpression() {
      return false;
   }

   public VariableBinding nullAnnotatedVariableBinding(boolean supportTypeAnnotations) {
      return null;
   }

   public boolean isFunctionalType() {
      return false;
   }

   public Expression[] getPolyExpressions() {
      return this.isPolyExpression() ? new Expression[]{this} : NO_EXPRESSIONS;
   }

   public boolean isPotentiallyCompatibleWith(TypeBinding targetType, Scope scope) {
      return this.isCompatibleWith(targetType, scope);
   }
}
