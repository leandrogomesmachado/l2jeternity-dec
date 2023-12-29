package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.NullAnnotationMatching;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ParameterizedTypeBinding extends ReferenceBinding implements Substitution {
   protected ReferenceBinding type;
   public TypeBinding[] arguments;
   public LookupEnvironment environment;
   public char[] genericTypeSignature;
   public ReferenceBinding superclass;
   public ReferenceBinding[] superInterfaces;
   public FieldBinding[] fields;
   public ReferenceBinding[] memberTypes;
   public MethodBinding[] methods;
   protected ReferenceBinding enclosingType;

   public ParameterizedTypeBinding(ReferenceBinding type, TypeBinding[] arguments, ReferenceBinding enclosingType, LookupEnvironment environment) {
      this.environment = environment;
      this.enclosingType = enclosingType;
      this.initialize(type, arguments);
      if (type instanceof UnresolvedReferenceBinding) {
         ((UnresolvedReferenceBinding)type).addWrapper(this, environment);
      }

      if (arguments != null) {
         int i = 0;

         for(int l = arguments.length; i < l; ++i) {
            if (arguments[i] instanceof UnresolvedReferenceBinding) {
               ((UnresolvedReferenceBinding)arguments[i]).addWrapper(this, environment);
            }

            if (arguments[i].hasNullTypeAnnotations()) {
               this.tagBits |= 1048576L;
            }
         }
      }

      if (enclosingType != null && enclosingType.hasNullTypeAnnotations()) {
         this.tagBits |= 1048576L;
      }

      this.tagBits |= 16777216L;
      this.typeBits = type.typeBits;
   }

   @Override
   public ReferenceBinding actualType() {
      return this.type;
   }

   @Override
   public boolean isParameterizedType() {
      return true;
   }

   public void boundCheck(Scope scope, TypeReference[] argumentReferences) {
      if ((this.tagBits & 4194304L) == 0L) {
         boolean hasErrors = false;
         TypeVariableBinding[] typeVariables = this.type.typeVariables();
         if (this.arguments != null && typeVariables != null) {
            int i = 0;

            for(int length = typeVariables.length; i < length; ++i) {
               TypeConstants.BoundCheckStatus checkStatus = typeVariables[i].boundCheck(this, this.arguments[i], scope, argumentReferences[i]);
               hasErrors |= checkStatus != TypeConstants.BoundCheckStatus.OK;
               if (!checkStatus.isOKbyJLS() && (this.arguments[i].tagBits & 128L) == 0L) {
                  scope.problemReporter().typeMismatchError(this.arguments[i], typeVariables[i], this.type, argumentReferences[i]);
               }
            }
         }

         if (!hasErrors) {
            this.tagBits |= 4194304L;
         }
      }
   }

   @Override
   public boolean canBeInstantiated() {
      return (this.tagBits & 1073741824L) == 0L && super.canBeInstantiated();
   }

   public ParameterizedTypeBinding capture(Scope scope, int start, int end) {
      if ((this.tagBits & 1073741824L) == 0L) {
         return this;
      } else {
         TypeBinding[] originalArguments = this.arguments;
         int length = originalArguments.length;
         TypeBinding[] capturedArguments = new TypeBinding[length];
         ReferenceBinding contextType = scope.enclosingSourceType();
         if (contextType != null) {
            contextType = contextType.outermostEnclosingType();
         }

         CompilationUnitScope compilationUnitScope = scope.compilationUnitScope();
         ASTNode cud = compilationUnitScope.referenceContext;
         long sourceLevel = this.environment.globalOptions.sourceLevel;
         boolean needUniqueCapture = sourceLevel >= 3407872L;

         for(int i = 0; i < length; ++i) {
            TypeBinding argument = originalArguments[i];
            if (argument.kind() == 516) {
               WildcardBinding wildcard = (WildcardBinding)argument;
               if (wildcard.boundKind == 2 && wildcard.bound.id == 1) {
                  capturedArguments[i] = wildcard.bound;
               } else if (needUniqueCapture) {
                  capturedArguments[i] = this.environment.createCapturedWildcard(wildcard, contextType, start, end, cud, compilationUnitScope.nextCaptureID());
               } else {
                  capturedArguments[i] = new CaptureBinding(wildcard, contextType, start, end, cud, compilationUnitScope.nextCaptureID());
               }
            } else {
               capturedArguments[i] = argument;
            }
         }

         ParameterizedTypeBinding capturedParameterizedType = this.environment
            .createParameterizedType(this.type, capturedArguments, this.enclosingType(), this.typeAnnotations);

         for(int i = 0; i < length; ++i) {
            TypeBinding argument = capturedArguments[i];
            if (argument.isCapture()) {
               ((CaptureBinding)argument).initializeBounds(scope, capturedParameterizedType);
            }
         }

         return capturedParameterizedType;
      }
   }

   @Override
   public TypeBinding uncapture(Scope scope) {
      if ((this.tagBits & 2305843009213693952L) == 0L) {
         return this;
      } else {
         int length = this.arguments == null ? 0 : this.arguments.length;
         TypeBinding[] freeTypes = new TypeBinding[length];

         for(int i = 0; i < length; ++i) {
            freeTypes[i] = this.arguments[i].uncapture(scope);
         }

         return scope.environment()
            .createParameterizedType(
               this.type, freeTypes, (ReferenceBinding)(this.enclosingType != null ? this.enclosingType.uncapture(scope) : null), this.typeAnnotations
            );
      }
   }

   @Override
   public List<TypeBinding> collectMissingTypes(List<TypeBinding> missingTypes) {
      if ((this.tagBits & 128L) != 0L) {
         if (this.enclosingType != null) {
            missingTypes = this.enclosingType.collectMissingTypes(missingTypes);
         }

         missingTypes = this.genericType().collectMissingTypes(missingTypes);
         if (this.arguments != null) {
            int i = 0;

            for(int max = this.arguments.length; i < max; ++i) {
               missingTypes = this.arguments[i].collectMissingTypes(missingTypes);
            }
         }
      }

      return missingTypes;
   }

   @Override
   public void collectSubstitutes(Scope scope, TypeBinding actualType, InferenceContext inferenceContext, int constraint) {
      if ((this.tagBits & 536870912L) == 0L) {
         TypeBinding actualEquivalent = actualType.findSuperTypeOriginatingFrom(this.type);
         if (actualEquivalent != null && actualEquivalent.isRawType()) {
            inferenceContext.isUnchecked = true;
         }
      } else if (actualType != TypeBinding.NULL && actualType.kind() != 65540) {
         if (actualType instanceof ReferenceBinding) {
            TypeBinding formalEquivalent;
            TypeBinding actualEquivalent;
            switch(constraint) {
               case 0:
               case 1:
                  formalEquivalent = this;
                  actualEquivalent = actualType.findSuperTypeOriginatingFrom(this.type);
                  if (actualEquivalent == null) {
                     return;
                  }
                  break;
               case 2:
               default:
                  formalEquivalent = this.findSuperTypeOriginatingFrom(actualType);
                  if (formalEquivalent == null) {
                     return;
                  }

                  actualEquivalent = actualType;
            }

            ReferenceBinding formalEnclosingType = formalEquivalent.enclosingType();
            if (formalEnclosingType != null) {
               formalEnclosingType.collectSubstitutes(scope, actualEquivalent.enclosingType(), inferenceContext, constraint);
            }

            if (this.arguments != null) {
               TypeBinding[] formalArguments;
               switch(formalEquivalent.kind()) {
                  case 260:
                     formalArguments = ((ParameterizedTypeBinding)formalEquivalent).arguments;
                     break;
                  case 1028:
                     if (inferenceContext.depth > 0) {
                        inferenceContext.status = 1;
                     }

                     return;
                  case 2052:
                     formalArguments = formalEquivalent.typeVariables();
                     break;
                  default:
                     return;
               }

               TypeBinding[] actualArguments;
               switch(actualEquivalent.kind()) {
                  case 260:
                     actualArguments = ((ParameterizedTypeBinding)actualEquivalent).arguments;
                     break;
                  case 1028:
                     if (inferenceContext.depth > 0) {
                        inferenceContext.status = 1;
                     } else {
                        inferenceContext.isUnchecked = true;
                     }

                     return;
                  case 2052:
                     actualArguments = actualEquivalent.typeVariables();
                     break;
                  default:
                     return;
               }

               if (formalArguments != null && actualArguments != null) {
                  ++inferenceContext.depth;
                  int i = 0;

                  for(int length = formalArguments.length; i < length; ++i) {
                     TypeBinding formalArgument = formalArguments[i];
                     TypeBinding actualArgument = actualArguments[i];
                     if (formalArgument.isWildcard()) {
                        formalArgument.collectSubstitutes(scope, actualArgument, inferenceContext, constraint);
                     } else {
                        if (actualArgument.isWildcard()) {
                           WildcardBinding actualWildcardArgument = (WildcardBinding)actualArgument;
                           if (actualWildcardArgument.otherBounds == null) {
                              if (constraint == 2) {
                                 switch(actualWildcardArgument.boundKind) {
                                    case 1:
                                       formalArgument.collectSubstitutes(scope, actualWildcardArgument.bound, inferenceContext, 2);
                                       continue;
                                    case 2:
                                       formalArgument.collectSubstitutes(scope, actualWildcardArgument.bound, inferenceContext, 1);
                                 }
                              }
                              continue;
                           }
                        }

                        formalArgument.collectSubstitutes(scope, actualArgument, inferenceContext, 0);
                     }
                  }

                  --inferenceContext.depth;
               }
            }
         }
      }
   }

   @Override
   public void computeId() {
      this.id = Integer.MAX_VALUE;
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      StringBuffer sig = new StringBuffer(10);
      ReferenceBinding enclosing;
      if (!this.isMemberType() || !(enclosing = this.enclosingType()).isParameterizedType() && !enclosing.isRawType()) {
         if (this.type.isLocalType()) {
            LocalTypeBinding localTypeBinding = (LocalTypeBinding)this.type;
            enclosing = localTypeBinding.enclosingType();

            ReferenceBinding temp;
            while((temp = enclosing.enclosingType()) != null) {
               enclosing = temp;
            }

            char[] typeSig = enclosing.computeUniqueKey(false);
            sig.append(typeSig, 0, typeSig.length - 1);
            sig.append('$');
            sig.append(localTypeBinding.sourceStart);
         } else {
            char[] typeSig = this.type.computeUniqueKey(false);
            sig.append(typeSig, 0, typeSig.length - 1);
         }
      } else {
         char[] typeSig = enclosing.computeUniqueKey(false);
         sig.append(typeSig, 0, typeSig.length - 1);
         sig.append('.').append(this.sourceName());
      }

      ReferenceBinding captureSourceType = null;
      if (this.arguments != null) {
         sig.append('<');
         int i = 0;

         for(int length = this.arguments.length; i < length; ++i) {
            TypeBinding typeBinding = this.arguments[i];
            sig.append(typeBinding.computeUniqueKey(false));
            if (typeBinding instanceof CaptureBinding) {
               captureSourceType = ((CaptureBinding)typeBinding).sourceType;
            }
         }

         sig.append('>');
      }

      sig.append(';');
      if (captureSourceType != null && TypeBinding.notEquals(captureSourceType, this.type)) {
         sig.insert(0, "&");
         sig.insert(0, captureSourceType.computeUniqueKey(false));
      }

      int sigLength = sig.length();
      char[] uniqueKey = new char[sigLength];
      sig.getChars(0, sigLength, uniqueKey, 0);
      return uniqueKey;
   }

   @Override
   public char[] constantPoolName() {
      return this.type.constantPoolName();
   }

   @Override
   public TypeBinding clone(TypeBinding outerType) {
      return new ParameterizedTypeBinding(this.type, this.arguments, (ReferenceBinding)outerType, this.environment);
   }

   public ParameterizedMethodBinding createParameterizedMethod(MethodBinding originalMethod) {
      return new ParameterizedMethodBinding(this, originalMethod);
   }

   @Override
   public String debugName() {
      if (this.hasTypeAnnotations()) {
         return this.annotatedDebugName();
      } else {
         StringBuffer nameBuffer = new StringBuffer(10);
         if (this.type instanceof UnresolvedReferenceBinding) {
            nameBuffer.append(this.type);
         } else {
            nameBuffer.append(this.type.sourceName());
         }

         if (this.arguments != null && this.arguments.length > 0) {
            nameBuffer.append('<');
            int i = 0;

            for(int length = this.arguments.length; i < length; ++i) {
               if (i > 0) {
                  nameBuffer.append(',');
               }

               nameBuffer.append(this.arguments[i].debugName());
            }

            nameBuffer.append('>');
         }

         return nameBuffer.toString();
      }
   }

   @Override
   public String annotatedDebugName() {
      StringBuffer nameBuffer = new StringBuffer(super.annotatedDebugName());
      if (this.arguments != null && this.arguments.length > 0) {
         nameBuffer.append('<');
         int i = 0;

         for(int length = this.arguments.length; i < length; ++i) {
            if (i > 0) {
               nameBuffer.append(',');
            }

            nameBuffer.append(this.arguments[i].annotatedDebugName());
         }

         nameBuffer.append('>');
      }

      return nameBuffer.toString();
   }

   @Override
   public ReferenceBinding enclosingType() {
      return this.enclosingType;
   }

   @Override
   public LookupEnvironment environment() {
      return this.environment;
   }

   @Override
   public TypeBinding erasure() {
      return this.type.erasure();
   }

   @Override
   public int fieldCount() {
      return this.type.fieldCount();
   }

   @Override
   public FieldBinding[] fields() {
      if ((this.tagBits & 8192L) != 0L) {
         return this.fields;
      } else {
         try {
            FieldBinding[] originalFields = this.type.fields();
            int length = originalFields.length;
            FieldBinding[] parameterizedFields = new FieldBinding[length];

            for(int i = 0; i < length; ++i) {
               parameterizedFields[i] = new ParameterizedFieldBinding(this, originalFields[i]);
            }

            this.fields = parameterizedFields;
         } finally {
            if (this.fields == null) {
               this.fields = Binding.NO_FIELDS;
            }

            this.tagBits |= 8192L;
         }

         return this.fields;
      }
   }

   public ReferenceBinding genericType() {
      if (this.type instanceof UnresolvedReferenceBinding) {
         ((UnresolvedReferenceBinding)this.type).resolve(this.environment, false);
      }

      return this.type;
   }

   @Override
   public char[] genericTypeSignature() {
      if (this.genericTypeSignature == null) {
         if ((this.modifiers & 1073741824) == 0) {
            this.genericTypeSignature = this.type.signature();
         } else {
            StringBuffer sig = new StringBuffer(10);
            if (this.isMemberType()) {
               ReferenceBinding enclosing = this.enclosingType();
               char[] typeSig = enclosing.genericTypeSignature();
               sig.append(typeSig, 0, typeSig.length - 1);
               if ((enclosing.modifiers & 1073741824) != 0) {
                  sig.append('.');
               } else {
                  sig.append('$');
               }

               sig.append(this.sourceName());
            } else {
               char[] typeSig = this.type.signature();
               sig.append(typeSig, 0, typeSig.length - 1);
            }

            if (this.arguments != null) {
               sig.append('<');
               int i = 0;

               for(int length = this.arguments.length; i < length; ++i) {
                  sig.append(this.arguments[i].genericTypeSignature());
               }

               sig.append('>');
            }

            sig.append(';');
            int sigLength = sig.length();
            this.genericTypeSignature = new char[sigLength];
            sig.getChars(0, sigLength, this.genericTypeSignature, 0);
         }
      }

      return this.genericTypeSignature;
   }

   @Override
   public long getAnnotationTagBits() {
      return this.type.getAnnotationTagBits();
   }

   @Override
   public int getEnclosingInstancesSlotSize() {
      return this.genericType().getEnclosingInstancesSlotSize();
   }

   @Override
   public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
      int argCount = argumentTypes.length;
      MethodBinding match = null;
      if ((this.tagBits & 32768L) != 0L) {
         long range;
         if ((range = ReferenceBinding.binarySearch(TypeConstants.INIT, this.methods)) >= 0L) {
            int imethod = (int)range;

            label66:
            for(int end = (int)(range >> 32); imethod <= end; ++imethod) {
               MethodBinding method = this.methods[imethod];
               if (method.parameters.length == argCount) {
                  TypeBinding[] toMatch = method.parameters;

                  for(int iarg = 0; iarg < argCount; ++iarg) {
                     if (TypeBinding.notEquals(toMatch[iarg], argumentTypes[iarg])) {
                        continue label66;
                     }
                  }

                  if (match != null) {
                     return null;
                  }

                  match = method;
               }
            }
         }
      } else {
         MethodBinding[] matchingMethods = this.getMethods(TypeConstants.INIT);
         int m = matchingMethods.length;

         label51:
         while(--m >= 0) {
            MethodBinding method = matchingMethods[m];
            TypeBinding[] toMatch = method.parameters;
            if (toMatch.length == argCount) {
               for(int p = 0; p < argCount; ++p) {
                  if (TypeBinding.notEquals(toMatch[p], argumentTypes[p])) {
                     continue label51;
                  }
               }

               if (match != null) {
                  return null;
               }

               match = method;
            }
         }
      }

      return match;
   }

   @Override
   public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
      int argCount = argumentTypes.length;
      boolean foundNothing = true;
      MethodBinding match = null;
      if ((this.tagBits & 32768L) != 0L) {
         long range;
         if ((range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0L) {
            int imethod = (int)range;

            label104:
            for(int end = (int)(range >> 32); imethod <= end; ++imethod) {
               MethodBinding method = this.methods[imethod];
               foundNothing = false;
               if (method.parameters.length == argCount) {
                  TypeBinding[] toMatch = method.parameters;

                  for(int iarg = 0; iarg < argCount; ++iarg) {
                     if (TypeBinding.notEquals(toMatch[iarg], argumentTypes[iarg])) {
                        continue label104;
                     }
                  }

                  if (match != null) {
                     return null;
                  }

                  match = method;
               }
            }
         }
      } else {
         MethodBinding[] matchingMethods = this.getMethods(selector);
         foundNothing = matchingMethods == Binding.NO_METHODS;
         int m = matchingMethods.length;

         label89:
         while(--m >= 0) {
            MethodBinding method = matchingMethods[m];
            TypeBinding[] toMatch = method.parameters;
            if (toMatch.length == argCount) {
               for(int p = 0; p < argCount; ++p) {
                  if (TypeBinding.notEquals(toMatch[p], argumentTypes[p])) {
                     continue label89;
                  }
               }

               if (match != null) {
                  return null;
               }

               match = method;
            }
         }
      }

      if (match != null) {
         return match.hasSubstitutedParameters() ? null : match;
      } else {
         if (foundNothing && (this.arguments == null || this.arguments.length <= 1)) {
            if (this.isInterface()) {
               if (this.superInterfaces().length == 1) {
                  if (refScope != null) {
                     refScope.recordTypeReference(this.superInterfaces[0]);
                  }

                  return this.superInterfaces[0].getExactMethod(selector, argumentTypes, refScope);
               }
            } else if (this.superclass() != null) {
               if (refScope != null) {
                  refScope.recordTypeReference(this.superclass);
               }

               return this.superclass.getExactMethod(selector, argumentTypes, refScope);
            }
         }

         return null;
      }
   }

   @Override
   public FieldBinding getField(char[] fieldName, boolean needResolve) {
      this.fields();
      return ReferenceBinding.binarySearch(fieldName, this.fields);
   }

   @Override
   public ReferenceBinding getMemberType(char[] typeName) {
      this.memberTypes();
      int typeLength = typeName.length;
      int i = this.memberTypes.length;

      while(--i >= 0) {
         ReferenceBinding memberType = this.memberTypes[i];
         if (memberType.sourceName.length == typeLength && CharOperation.equals(memberType.sourceName, typeName)) {
            return memberType;
         }
      }

      return null;
   }

   @Override
   public MethodBinding[] getMethods(char[] selector) {
      long range;
      if (this.methods != null && (range = ReferenceBinding.binarySearch(selector, this.methods)) >= 0L) {
         int start = (int)range;
         int length = (int)(range >> 32) - start + 1;
         MethodBinding[] result;
         System.arraycopy(this.methods, start, result = new MethodBinding[length], 0, length);
         return result;
      } else if ((this.tagBits & 32768L) != 0L) {
         return Binding.NO_METHODS;
      } else {
         MethodBinding[] parameterizedMethods = null;

         MethodBinding[] var9;
         try {
            MethodBinding[] originalMethods = this.type.getMethods(selector);
            int length = originalMethods.length;
            if (length == 0) {
               return Binding.NO_METHODS;
            }

            parameterizedMethods = new MethodBinding[length];
            boolean useNullTypeAnnotations = this.environment.usesNullTypeAnnotations();

            for(int i = 0; i < length; ++i) {
               parameterizedMethods[i] = this.createParameterizedMethod(originalMethods[i]);
               if (useNullTypeAnnotations) {
                  parameterizedMethods[i] = NullAnnotationMatching.checkForContradictions(parameterizedMethods[i], null, null);
               }
            }

            if (this.methods == null) {
               MethodBinding[] temp = new MethodBinding[length];
               System.arraycopy(parameterizedMethods, 0, temp, 0, length);
               this.methods = temp;
            } else {
               int total = length + this.methods.length;
               MethodBinding[] temp = new MethodBinding[total];
               System.arraycopy(parameterizedMethods, 0, temp, 0, length);
               System.arraycopy(this.methods, 0, temp, length, this.methods.length);
               if (total > 1) {
                  ReferenceBinding.sortMethods(temp, 0, total);
               }

               this.methods = temp;
            }

            var9 = parameterizedMethods;
         } finally {
            if (parameterizedMethods == null) {
               MethodBinding[] var13 = Binding.NO_METHODS;
               this.methods = Binding.NO_METHODS;
            }
         }

         return var9;
      }
   }

   @Override
   public int getOuterLocalVariablesSlotSize() {
      return this.genericType().getOuterLocalVariablesSlotSize();
   }

   @Override
   public boolean hasMemberTypes() {
      return this.type.hasMemberTypes();
   }

   @Override
   public boolean hasTypeBit(int bit) {
      TypeBinding erasure = this.erasure();
      return erasure instanceof ReferenceBinding ? ((ReferenceBinding)erasure).hasTypeBit(bit) : false;
   }

   @Override
   public boolean implementsMethod(MethodBinding method) {
      return this.type.implementsMethod(method);
   }

   void initialize(ReferenceBinding someType, TypeBinding[] someArguments) {
      this.type = someType;
      this.sourceName = someType.sourceName;
      this.compoundName = someType.compoundName;
      this.fPackage = someType.fPackage;
      this.fileName = someType.fileName;
      this.modifiers = someType.modifiers & -1073741825;
      if (someArguments != null) {
         this.modifiers |= 1073741824;
      } else if (this.enclosingType != null) {
         this.modifiers |= this.enclosingType.modifiers & 1073741824;
         this.tagBits |= this.enclosingType.tagBits & 2305843009750564992L;
      }

      if (someArguments != null) {
         this.arguments = someArguments;
         int i = 0;

         for(int length = someArguments.length; i < length; ++i) {
            TypeBinding someArgument = someArguments[i];
            switch(someArgument.kind()) {
               case 516:
                  this.tagBits |= 1073741824L;
                  if (((WildcardBinding)someArgument).boundKind != 0) {
                     this.tagBits |= 8388608L;
                  }
                  break;
               case 8196:
                  this.tagBits |= 1082130432L;
                  break;
               default:
                  this.tagBits |= 8388608L;
            }

            this.tagBits |= someArgument.tagBits & 2305843009750567040L;
         }
      }

      this.tagBits |= someType.tagBits & 2846274964498155676L;
      this.tagBits &= -40961L;
   }

   protected void initializeArguments() {
   }

   @Override
   void initializeForStaticImports() {
      this.type.initializeForStaticImports();
   }

   @Override
   public boolean isBoundParameterizedType() {
      return (this.tagBits & 8388608L) != 0L;
   }

   @Override
   public boolean isEquivalentTo(TypeBinding otherType) {
      if (equalsEquals(this, otherType)) {
         return true;
      } else if (otherType == null) {
         return false;
      } else {
         switch(otherType.kind()) {
            case 260:
               ParameterizedTypeBinding otherParamType = (ParameterizedTypeBinding)otherType;
               if (TypeBinding.notEquals(this.type, otherParamType.type)) {
                  return false;
               } else {
                  if (!this.isStatic()) {
                     ReferenceBinding enclosing = this.enclosingType();
                     if (enclosing != null) {
                        ReferenceBinding otherEnclosing = otherParamType.enclosingType();
                        if (otherEnclosing == null) {
                           return false;
                        }

                        if ((otherEnclosing.tagBits & 1073741824L) == 0L) {
                           if (TypeBinding.notEquals(enclosing, otherEnclosing)) {
                              return false;
                           }
                        } else if (!enclosing.isEquivalentTo(otherParamType.enclosingType())) {
                           return false;
                        }
                     }
                  }

                  if (this.arguments != ParameterizedSingleTypeReference.DIAMOND_TYPE_ARGUMENTS) {
                     if (this.arguments == null) {
                        if (otherParamType.arguments == null) {
                           return true;
                        }

                        return false;
                     }

                     int length = this.arguments.length;
                     TypeBinding[] otherArguments = otherParamType.arguments;
                     if (otherArguments == null || otherArguments.length != length) {
                        return false;
                     }

                     for(int i = 0; i < length; ++i) {
                        if (!this.arguments[i].isTypeArgumentContainedBy(otherArguments[i])) {
                           return false;
                        }
                     }
                  }

                  return true;
               }
            case 516:
            case 8196:
               return ((WildcardBinding)otherType).boundCheck(this);
            case 1028:
               return TypeBinding.equalsEquals(this.erasure(), otherType.erasure());
            default:
               return TypeBinding.equalsEquals(this.erasure(), otherType);
         }
      }
   }

   @Override
   public boolean isHierarchyConnected() {
      return this.superclass != null && this.superInterfaces != null;
   }

   @Override
   public boolean isProperType(boolean admitCapture18) {
      if (this.arguments != null) {
         for(int i = 0; i < this.arguments.length; ++i) {
            if (!this.arguments[i].isProperType(admitCapture18)) {
               return false;
            }
         }
      }

      return super.isProperType(admitCapture18);
   }

   @Override
   TypeBinding substituteInferenceVariable(InferenceVariable var, TypeBinding substituteType) {
      if (this.arguments != null) {
         TypeBinding[] newArgs = null;
         int length = this.arguments.length;

         for(int i = 0; i < length; ++i) {
            TypeBinding oldArg = this.arguments[i];
            TypeBinding newArg = oldArg.substituteInferenceVariable(var, substituteType);
            if (TypeBinding.notEquals(newArg, oldArg)) {
               if (newArgs == null) {
                  System.arraycopy(this.arguments, 0, newArgs = new TypeBinding[length], 0, length);
               }

               newArgs[i] = newArg;
            }
         }

         if (newArgs != null) {
            return this.environment.createParameterizedType(this.type, newArgs, this.enclosingType);
         }
      }

      return this;
   }

   @Override
   public boolean isRawSubstitution() {
      return this.isRawType();
   }

   @Override
   public TypeBinding unannotated() {
      return (TypeBinding)(this.hasTypeAnnotations() ? this.environment.getUnannotatedType(this) : this);
   }

   @Override
   public TypeBinding withoutToplevelNullAnnotation() {
      if (!this.hasNullTypeAnnotations()) {
         return this;
      } else {
         ReferenceBinding unannotatedGenericType = (ReferenceBinding)this.environment.getUnannotatedType(this.type);
         AnnotationBinding[] newAnnotations = this.environment.filterNullTypeAnnotations(this.typeAnnotations);
         return this.environment.createParameterizedType(unannotatedGenericType, this.arguments, this.enclosingType, newAnnotations);
      }
   }

   @Override
   public int kind() {
      return 260;
   }

   @Override
   public ReferenceBinding[] memberTypes() {
      if (this.memberTypes == null) {
         try {
            ReferenceBinding[] originalMemberTypes = this.type.memberTypes();
            int length = originalMemberTypes.length;
            ReferenceBinding[] parameterizedMemberTypes = new ReferenceBinding[length];

            for(int i = 0; i < length; ++i) {
               parameterizedMemberTypes[i] = this.environment.createParameterizedType(originalMemberTypes[i], null, this);
            }

            this.memberTypes = parameterizedMemberTypes;
         } finally {
            if (this.memberTypes == null) {
               this.memberTypes = Binding.NO_MEMBER_TYPES;
            }
         }
      }

      return this.memberTypes;
   }

   @Override
   public boolean mentionsAny(TypeBinding[] parameters, int idx) {
      if (super.mentionsAny(parameters, idx)) {
         return true;
      } else {
         if (this.arguments != null) {
            int len = this.arguments.length;

            for(int i = 0; i < len; ++i) {
               if (TypeBinding.notEquals(this.arguments[i], this) && this.arguments[i].mentionsAny(parameters, idx)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   @Override
   void collectInferenceVariables(Set<InferenceVariable> variables) {
      if (this.arguments != null) {
         int len = this.arguments.length;

         for(int i = 0; i < len; ++i) {
            if (TypeBinding.notEquals(this.arguments[i], this)) {
               this.arguments[i].collectInferenceVariables(variables);
            }
         }
      }
   }

   @Override
   public MethodBinding[] methods() {
      if ((this.tagBits & 32768L) != 0L) {
         return this.methods;
      } else {
         try {
            MethodBinding[] originalMethods = this.type.methods();
            int length = originalMethods.length;
            MethodBinding[] parameterizedMethods = new MethodBinding[length];
            boolean useNullTypeAnnotations = this.environment.usesNullTypeAnnotations();

            for(int i = 0; i < length; ++i) {
               parameterizedMethods[i] = this.createParameterizedMethod(originalMethods[i]);
               if (useNullTypeAnnotations) {
                  parameterizedMethods[i] = NullAnnotationMatching.checkForContradictions(parameterizedMethods[i], null, null);
               }
            }

            this.methods = parameterizedMethods;
         } finally {
            if (this.methods == null) {
               this.methods = Binding.NO_METHODS;
            }

            this.tagBits |= 32768L;
         }

         return this.methods;
      }
   }

   @Override
   public int problemId() {
      return this.type.problemId();
   }

   @Override
   public char[] qualifiedPackageName() {
      return this.type.qualifiedPackageName();
   }

   @Override
   public char[] qualifiedSourceName() {
      return this.type.qualifiedSourceName();
   }

   @Override
   public char[] readableName() {
      StringBuffer nameBuffer = new StringBuffer(10);
      if (this.isMemberType()) {
         nameBuffer.append(CharOperation.concat(this.enclosingType().readableName(), this.sourceName, '.'));
      } else {
         nameBuffer.append(CharOperation.concatWith(this.type.compoundName, '.'));
      }

      if (this.arguments != null && this.arguments.length > 0) {
         nameBuffer.append('<');
         int i = 0;

         for(int length = this.arguments.length; i < length; ++i) {
            if (i > 0) {
               nameBuffer.append(',');
            }

            nameBuffer.append(this.arguments[i].readableName());
         }

         nameBuffer.append('>');
      }

      int nameLength = nameBuffer.length();
      char[] readableName = new char[nameLength];
      nameBuffer.getChars(0, nameLength, readableName, 0);
      return readableName;
   }

   ReferenceBinding resolve() {
      if ((this.tagBits & 16777216L) == 0L) {
         return this;
      } else {
         this.tagBits &= -16777217L;
         ReferenceBinding resolvedType = (ReferenceBinding)BinaryTypeBinding.resolveType(this.type, this.environment, false);
         this.tagBits |= resolvedType.tagBits & 2048L;
         if (this.arguments != null) {
            int argLength = this.arguments.length;

            for(int i = 0; i < argLength; ++i) {
               TypeBinding resolveType = BinaryTypeBinding.resolveType(this.arguments[i], this.environment, true);
               this.arguments[i] = resolveType;
               this.tagBits |= resolvedType.tagBits & 2048L;
            }
         }

         return this;
      }
   }

   @Override
   public char[] shortReadableName() {
      StringBuffer nameBuffer = new StringBuffer(10);
      if (this.isMemberType()) {
         nameBuffer.append(CharOperation.concat(this.enclosingType().shortReadableName(), this.sourceName, '.'));
      } else {
         nameBuffer.append(this.type.sourceName);
      }

      if (this.arguments != null && this.arguments.length > 0) {
         nameBuffer.append('<');
         int i = 0;

         for(int length = this.arguments.length; i < length; ++i) {
            if (i > 0) {
               nameBuffer.append(',');
            }

            nameBuffer.append(this.arguments[i].shortReadableName());
         }

         nameBuffer.append('>');
      }

      int nameLength = nameBuffer.length();
      char[] shortReadableName = new char[nameLength];
      nameBuffer.getChars(0, nameLength, shortReadableName, 0);
      return shortReadableName;
   }

   @Override
   public char[] nullAnnotatedReadableName(CompilerOptions options, boolean shortNames) {
      return shortNames ? this.nullAnnotatedShortReadableName(options) : this.nullAnnotatedReadableName(options);
   }

   @Override
   char[] nullAnnotatedReadableName(CompilerOptions options) {
      StringBuffer nameBuffer = new StringBuffer(10);
      if (this.isMemberType()) {
         nameBuffer.append(this.enclosingType().nullAnnotatedReadableName(options, false));
         nameBuffer.append('.');
         this.appendNullAnnotation(nameBuffer, options);
         nameBuffer.append(this.sourceName);
      } else if (this.type.compoundName != null) {
         int l = this.type.compoundName.length;

         int i;
         for(i = 0; i < l - 1; ++i) {
            nameBuffer.append(this.type.compoundName[i]);
            nameBuffer.append('.');
         }

         this.appendNullAnnotation(nameBuffer, options);
         nameBuffer.append(this.type.compoundName[i]);
      } else {
         this.appendNullAnnotation(nameBuffer, options);
         if (this.type.sourceName != null) {
            nameBuffer.append(this.type.sourceName);
         } else {
            nameBuffer.append(this.type.readableName());
         }
      }

      if (this.arguments != null && this.arguments.length > 0) {
         nameBuffer.append('<');
         int i = 0;

         for(int length = this.arguments.length; i < length; ++i) {
            if (i > 0) {
               nameBuffer.append(',');
            }

            nameBuffer.append(this.arguments[i].nullAnnotatedReadableName(options, false));
         }

         nameBuffer.append('>');
      }

      int nameLength = nameBuffer.length();
      char[] readableName = new char[nameLength];
      nameBuffer.getChars(0, nameLength, readableName, 0);
      return readableName;
   }

   @Override
   char[] nullAnnotatedShortReadableName(CompilerOptions options) {
      StringBuffer nameBuffer = new StringBuffer(10);
      if (this.isMemberType()) {
         nameBuffer.append(this.enclosingType().nullAnnotatedReadableName(options, true));
         nameBuffer.append('.');
         this.appendNullAnnotation(nameBuffer, options);
         nameBuffer.append(this.sourceName);
      } else {
         this.appendNullAnnotation(nameBuffer, options);
         if (this.type.sourceName != null) {
            nameBuffer.append(this.type.sourceName);
         } else {
            nameBuffer.append(this.type.shortReadableName());
         }
      }

      if (this.arguments != null && this.arguments.length > 0) {
         nameBuffer.append('<');
         int i = 0;

         for(int length = this.arguments.length; i < length; ++i) {
            if (i > 0) {
               nameBuffer.append(',');
            }

            nameBuffer.append(this.arguments[i].nullAnnotatedReadableName(options, true));
         }

         nameBuffer.append('>');
      }

      int nameLength = nameBuffer.length();
      char[] shortReadableName = new char[nameLength];
      nameBuffer.getChars(0, nameLength, shortReadableName, 0);
      return shortReadableName;
   }

   @Override
   public char[] signature() {
      if (this.signature == null) {
         this.signature = this.type.signature();
      }

      return this.signature;
   }

   @Override
   public char[] sourceName() {
      return this.type.sourceName();
   }

   @Override
   public TypeBinding substitute(TypeVariableBinding originalVariable) {
      ParameterizedTypeBinding currentType = this;

      while(true) {
         TypeVariableBinding[] typeVariables = currentType.type.typeVariables();
         int length = typeVariables.length;
         if (originalVariable.rank < length && TypeBinding.equalsEquals(typeVariables[originalVariable.rank], originalVariable)) {
            if (currentType.arguments == null) {
               currentType.initializeArguments();
            }

            if (currentType.arguments != null) {
               if (currentType.arguments.length == 0) {
                  return originalVariable;
               }

               TypeBinding substitute = currentType.arguments[originalVariable.rank];
               return originalVariable.combineTypeAnnotations(substitute);
            }
         }

         if (currentType.isStatic()) {
            break;
         }

         ReferenceBinding enclosing = currentType.enclosingType();
         if (!(enclosing instanceof ParameterizedTypeBinding)) {
            break;
         }

         currentType = (ParameterizedTypeBinding)enclosing;
      }

      return originalVariable;
   }

   @Override
   public ReferenceBinding superclass() {
      if (this.superclass == null) {
         ReferenceBinding genericSuperclass = this.type.superclass();
         if (genericSuperclass == null) {
            return null;
         }

         this.superclass = (ReferenceBinding)Scope.substitute(this, genericSuperclass);
         this.typeBits |= this.superclass.typeBits & 19;
         if ((this.typeBits & 3) != 0) {
            this.typeBits |= this.applyCloseableClassWhitelists();
         }
      }

      return this.superclass;
   }

   @Override
   public ReferenceBinding[] superInterfaces() {
      if (this.superInterfaces == null) {
         if (this.type.isHierarchyBeingConnected()) {
            return Binding.NO_SUPERINTERFACES;
         }

         this.superInterfaces = Scope.substitute(this, this.type.superInterfaces());
         if (this.superInterfaces != null) {
            int i = this.superInterfaces.length;

            while(--i >= 0) {
               this.typeBits |= this.superInterfaces[i].typeBits & 19;
               if ((this.typeBits & 3) != 0) {
                  this.typeBits |= this.applyCloseableInterfaceWhitelists();
               }
            }
         }
      }

      return this.superInterfaces;
   }

   @Override
   public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env) {
      boolean update = false;
      if (this.type == unresolvedType) {
         this.type = resolvedType;
         update = true;
         ReferenceBinding enclosing = resolvedType.enclosingType();
         if (enclosing != null) {
            this.enclosingType = (ReferenceBinding)env.convertUnresolvedBinaryToRawType(enclosing);
         }
      }

      if (this.arguments != null) {
         int i = 0;

         for(int l = this.arguments.length; i < l; ++i) {
            if (this.arguments[i] == unresolvedType) {
               this.arguments[i] = env.convertUnresolvedBinaryToRawType(resolvedType);
               update = true;
            }
         }
      }

      if (update) {
         this.initialize(this.type, this.arguments);
      }
   }

   @Override
   public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
      return this.genericType().syntheticEnclosingInstanceTypes();
   }

   @Override
   public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
      return this.genericType().syntheticOuterLocalVariables();
   }

   @Override
   public String toString() {
      if (this.hasTypeAnnotations()) {
         return this.annotatedDebugName();
      } else {
         StringBuffer buffer = new StringBuffer(30);
         if (this.type instanceof UnresolvedReferenceBinding) {
            buffer.append(this.debugName());
         } else {
            if (this.isDeprecated()) {
               buffer.append("deprecated ");
            }

            if (this.isPublic()) {
               buffer.append("public ");
            }

            if (this.isProtected()) {
               buffer.append("protected ");
            }

            if (this.isPrivate()) {
               buffer.append("private ");
            }

            if (this.isAbstract() && this.isClass()) {
               buffer.append("abstract ");
            }

            if (this.isStatic() && this.isNestedType()) {
               buffer.append("static ");
            }

            if (this.isFinal()) {
               buffer.append("final ");
            }

            if (this.isEnum()) {
               buffer.append("enum ");
            } else if (this.isAnnotationType()) {
               buffer.append("@interface ");
            } else if (this.isClass()) {
               buffer.append("class ");
            } else {
               buffer.append("interface ");
            }

            buffer.append(this.debugName());
            buffer.append("\n\textends ");
            buffer.append(this.superclass != null ? this.superclass.debugName() : "NULL TYPE");
            if (this.superInterfaces != null) {
               if (this.superInterfaces != Binding.NO_SUPERINTERFACES) {
                  buffer.append("\n\timplements : ");
                  int i = 0;

                  for(int length = this.superInterfaces.length; i < length; ++i) {
                     if (i > 0) {
                        buffer.append(", ");
                     }

                     buffer.append(this.superInterfaces[i] != null ? this.superInterfaces[i].debugName() : "NULL TYPE");
                  }
               }
            } else {
               buffer.append("NULL SUPERINTERFACES");
            }

            if (this.enclosingType() != null) {
               buffer.append("\n\tenclosing type : ");
               buffer.append(this.enclosingType().debugName());
            }

            if (this.fields != null) {
               if (this.fields != Binding.NO_FIELDS) {
                  buffer.append("\n/*   fields   */");
                  int i = 0;

                  for(int length = this.fields.length; i < length; ++i) {
                     buffer.append('\n').append(this.fields[i] != null ? this.fields[i].toString() : "NULL FIELD");
                  }
               }
            } else {
               buffer.append("NULL FIELDS");
            }

            if (this.methods != null) {
               if (this.methods != Binding.NO_METHODS) {
                  buffer.append("\n/*   methods   */");
                  int i = 0;

                  for(int length = this.methods.length; i < length; ++i) {
                     buffer.append('\n').append(this.methods[i] != null ? this.methods[i].toString() : "NULL METHOD");
                  }
               }
            } else {
               buffer.append("NULL METHODS");
            }

            buffer.append("\n\n");
         }

         return buffer.toString();
      }
   }

   @Override
   public TypeVariableBinding[] typeVariables() {
      return this.arguments == null ? this.type.typeVariables() : Binding.NO_TYPE_VARIABLES;
   }

   @Override
   public TypeBinding[] typeArguments() {
      return this.arguments;
   }

   @Override
   public FieldBinding[] unResolvedFields() {
      return this.fields;
   }

   @Override
   protected MethodBinding[] getInterfaceAbstractContracts(Scope scope, boolean replaceWildcards) throws InvalidInputException {
      if (replaceWildcards) {
         TypeBinding[] types = this.getNonWildcardParameterization(scope);
         if (types == null) {
            return new MethodBinding[]{new ProblemMethodBinding(TypeConstants.ANONYMOUS_METHOD, null, 18)};
         }

         for(int i = 0; i < types.length; ++i) {
            if (TypeBinding.notEquals(types[i], this.arguments[i])) {
               ParameterizedTypeBinding declaringType = scope.environment().createParameterizedType(this.type, types, this.type.enclosingType());
               TypeVariableBinding[] typeParameters = this.type.typeVariables();
               int j = 0;

               for(int length = typeParameters.length; j < length; ++j) {
                  if (!typeParameters[j].boundCheck(declaringType, types[j], scope, null).isOKbyJLS()) {
                     return new MethodBinding[]{new ProblemMethodBinding(TypeConstants.ANONYMOUS_METHOD, null, 18)};
                  }
               }

               return declaringType.getInterfaceAbstractContracts(scope, replaceWildcards);
            }
         }
      }

      return super.getInterfaceAbstractContracts(scope, replaceWildcards);
   }

   @Override
   public MethodBinding getSingleAbstractMethod(Scope scope, boolean replaceWildcards) {
      return this.getSingleAbstractMethod(scope, replaceWildcards, -1, -1);
   }

   public MethodBinding getSingleAbstractMethod(Scope scope, boolean replaceWildcards, int start, int end) {
      int index = replaceWildcards ? (end < 0 ? 0 : 1) : 2;
      if (this.singleAbstractMethod != null) {
         if (this.singleAbstractMethod[index] != null) {
            return this.singleAbstractMethod[index];
         }
      } else {
         this.singleAbstractMethod = new MethodBinding[3];
      }

      if (!this.isValidBinding()) {
         return null;
      } else {
         ReferenceBinding genericType = this.genericType();
         MethodBinding theAbstractMethod = genericType.getSingleAbstractMethod(scope, replaceWildcards);
         if (theAbstractMethod != null && theAbstractMethod.isValidBinding()) {
            ParameterizedTypeBinding declaringType = null;
            TypeBinding[] types = this.arguments;
            if (replaceWildcards) {
               types = this.getNonWildcardParameterization(scope);
               if (types == null) {
                  return this.singleAbstractMethod[index] = new ProblemMethodBinding(TypeConstants.ANONYMOUS_METHOD, null, 18);
               }
            } else if (types == null) {
               types = NO_TYPES;
            }

            if (end >= 0) {
               int i = 0;

               for(int length = types.length; i < length; ++i) {
                  types[i] = types[i].capture(scope, start, end);
               }
            }

            declaringType = scope.environment().createParameterizedType(genericType, types, genericType.enclosingType());
            TypeVariableBinding[] typeParameters = genericType.typeVariables();
            int i = 0;

            for(int length = typeParameters.length; i < length; ++i) {
               if (!typeParameters[i].boundCheck(declaringType, types[i], scope, null).isOKbyJLS()) {
                  return this.singleAbstractMethod[index] = new ProblemMethodBinding(TypeConstants.ANONYMOUS_METHOD, null, 18);
               }
            }

            ReferenceBinding substitutedDeclaringType = (ReferenceBinding)declaringType.findSuperTypeOriginatingFrom(theAbstractMethod.declaringClass);
            MethodBinding[] choices = substitutedDeclaringType.getMethods(theAbstractMethod.selector);
            int ix = 0;

            for(int length = choices.length; ix < length; ++ix) {
               MethodBinding method = choices[ix];
               if (method.isAbstract() && !method.redeclaresPublicObjectMethod(scope)) {
                  this.singleAbstractMethod[index] = method;
                  break;
               }
            }

            return this.singleAbstractMethod[index];
         } else {
            return this.singleAbstractMethod[index] = theAbstractMethod;
         }
      }
   }

   public TypeBinding[] getNonWildcardParameterization(Scope scope) {
      TypeBinding[] typeArguments = this.arguments;
      if (typeArguments == null) {
         return NO_TYPES;
      } else {
         TypeVariableBinding[] typeParameters = this.genericType().typeVariables();
         TypeBinding[] types = new TypeBinding[typeArguments.length];
         int i = 0;

         for(int length = typeArguments.length; i < length; ++i) {
            TypeBinding typeArgument = typeArguments[i];
            if (typeArgument.kind() == 516) {
               if (typeParameters[i].mentionsAny(typeParameters, i)) {
                  return null;
               }

               WildcardBinding wildcard = (WildcardBinding)typeArgument;
               switch(wildcard.boundKind) {
                  case 0:
                     types[i] = typeParameters[i].firstBound;
                     if (types[i] == null) {
                        types[i] = typeParameters[i].superclass;
                     }
                     break;
                  case 1:
                     TypeBinding[] otherUBounds = wildcard.otherBounds;
                     TypeBinding[] otherBBounds = typeParameters[i].otherUpperBounds();
                     int len = 1 + (otherUBounds != null ? otherUBounds.length : 0) + otherBBounds.length;
                     if (typeParameters[i].firstBound != null) {
                        ++len;
                     }

                     TypeBinding[] allBounds = new TypeBinding[len];
                     int idx = 0;
                     allBounds[idx++] = wildcard.bound;
                     if (otherUBounds != null) {
                        for(int j = 0; j < otherUBounds.length; ++j) {
                           allBounds[idx++] = otherUBounds[j];
                        }
                     }

                     if (typeParameters[i].firstBound != null) {
                        allBounds[idx++] = typeParameters[i].firstBound;
                     }

                     for(int j = 0; j < otherBBounds.length; ++j) {
                        allBounds[idx++] = otherBBounds[j];
                     }

                     TypeBinding[] glb = Scope.greaterLowerBound(allBounds, null, this.environment);
                     if (glb == null || glb.length == 0) {
                        return null;
                     }

                     if (glb.length == 1) {
                        types[i] = glb[0];
                     } else {
                        try {
                           ReferenceBinding[] refs = new ReferenceBinding[glb.length];
                           System.arraycopy(glb, 0, refs, 0, glb.length);
                           types[i] = this.environment.createIntersectionType18(refs);
                        } catch (ArrayStoreException var16) {
                           scope.problemReporter().genericInferenceError("Cannot compute glb of " + Arrays.toString((Object[])glb), null);
                           return null;
                        }
                     }
                     break;
                  case 2:
                     types[i] = wildcard.bound;
               }
            } else {
               types[i] = typeArgument;
            }
         }

         return types;
      }
   }

   @Override
   public long updateTagBits() {
      if (this.arguments != null) {
         for(TypeBinding argument : this.arguments) {
            this.tagBits |= argument.updateTagBits();
         }
      }

      return super.updateTagBits();
   }
}
