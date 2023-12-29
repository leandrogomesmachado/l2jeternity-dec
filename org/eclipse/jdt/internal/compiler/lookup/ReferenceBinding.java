package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NullAnnotationMatching;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

public abstract class ReferenceBinding extends TypeBinding {
   public char[][] compoundName;
   public char[] sourceName;
   public int modifiers;
   public PackageBinding fPackage;
   char[] fileName;
   char[] constantPoolName;
   char[] signature;
   private SimpleLookupTable compatibleCache;
   int typeBits;
   protected MethodBinding[] singleAbstractMethod;
   public static final ReferenceBinding LUB_GENERIC = new ReferenceBinding() {
      {
         this.id = 0;
      }

      @Override
      public boolean hasTypeBit(int bit) {
         return false;
      }
   };
   private static final Comparator<FieldBinding> FIELD_COMPARATOR = new Comparator<FieldBinding>() {
      public int compare(FieldBinding o1, FieldBinding o2) {
         char[] n1 = o1.name;
         char[] n2 = o2.name;
         return ReferenceBinding.compare(n1, n2, n1.length, n2.length);
      }
   };
   private static final Comparator<MethodBinding> METHOD_COMPARATOR = new Comparator<MethodBinding>() {
      public int compare(MethodBinding o1, MethodBinding o2) {
         char[] s1 = o1.selector;
         char[] s2 = o2.selector;
         int c = ReferenceBinding.compare(s1, s2, s1.length, s2.length);
         return c == 0 ? o1.parameters.length - o2.parameters.length : c;
      }
   };
   protected static ProblemMethodBinding samProblemBinding = new ProblemMethodBinding(TypeConstants.ANONYMOUS_METHOD, null, 17);

   public ReferenceBinding(ReferenceBinding prototype) {
      super(prototype);
      this.compoundName = prototype.compoundName;
      this.sourceName = prototype.sourceName;
      this.modifiers = prototype.modifiers;
      this.fPackage = prototype.fPackage;
      this.fileName = prototype.fileName;
      this.constantPoolName = prototype.constantPoolName;
      this.signature = prototype.signature;
      this.compatibleCache = prototype.compatibleCache;
      this.typeBits = prototype.typeBits;
      this.singleAbstractMethod = prototype.singleAbstractMethod;
   }

   public ReferenceBinding() {
   }

   public static FieldBinding binarySearch(char[] name, FieldBinding[] sortedFields) {
      if (sortedFields == null) {
         return null;
      } else {
         int max = sortedFields.length;
         if (max == 0) {
            return null;
         } else {
            int left = 0;
            int right = max - 1;
            int nameLength = name.length;
            int mid = 0;

            while(left <= right) {
               mid = left + (right - left) / 2;
               char[] midName = sortedFields[mid].name;
               int compare = compare(name, sortedFields[mid].name, nameLength, midName.length);
               if (compare < 0) {
                  right = mid - 1;
               } else {
                  if (compare <= 0) {
                     return sortedFields[mid];
                  }

                  left = mid + 1;
               }
            }

            return null;
         }
      }
   }

   public static long binarySearch(char[] selector, MethodBinding[] sortedMethods) {
      if (sortedMethods == null) {
         return -1L;
      } else {
         int max = sortedMethods.length;
         if (max == 0) {
            return -1L;
         } else {
            int left = 0;
            int right = max - 1;
            int selectorLength = selector.length;
            int mid = 0;

            while(left <= right) {
               mid = left + (right - left) / 2;
               char[] midSelector = sortedMethods[mid].selector;
               int compare = compare(selector, sortedMethods[mid].selector, selectorLength, midSelector.length);
               if (compare < 0) {
                  right = mid - 1;
               } else {
                  if (compare <= 0) {
                     int start = mid;
                     int end = mid;

                     while(start > left && CharOperation.equals(sortedMethods[start - 1].selector, selector)) {
                        --start;
                     }

                     while(end < right && CharOperation.equals(sortedMethods[end + 1].selector, selector)) {
                        ++end;
                     }

                     return (long)start + ((long)end << 32);
                  }

                  left = mid + 1;
               }
            }

            return -1L;
         }
      }
   }

   static int compare(char[] str1, char[] str2, int len1, int len2) {
      int n = Math.min(len1, len2);
      int i = 0;

      while(n-- != 0) {
         char c1 = str1[i];
         char c2 = str2[i++];
         if (c1 != c2) {
            return c1 - c2;
         }
      }

      return len1 - len2;
   }

   public static void sortFields(FieldBinding[] sortedFields, int left, int right) {
      Arrays.sort(sortedFields, left, right, FIELD_COMPARATOR);
   }

   public static void sortMethods(MethodBinding[] sortedMethods, int left, int right) {
      Arrays.sort(sortedMethods, left, right, METHOD_COMPARATOR);
   }

   public FieldBinding[] availableFields() {
      return this.fields();
   }

   public MethodBinding[] availableMethods() {
      return this.methods();
   }

   @Override
   public boolean canBeInstantiated() {
      return (this.modifiers & 26112) == 0;
   }

   public boolean canBeSeenBy(PackageBinding invocationPackage) {
      if (this.isPublic()) {
         return true;
      } else if (this.isPrivate()) {
         return false;
      } else {
         return invocationPackage == this.fPackage;
      }
   }

   public boolean canBeSeenBy(ReferenceBinding receiverType, ReferenceBinding invocationType) {
      if (this.isPublic()) {
         return true;
      } else if (TypeBinding.equalsEquals(invocationType, this) && TypeBinding.equalsEquals(invocationType, receiverType)) {
         return true;
      } else if (this.isProtected()) {
         if (TypeBinding.equalsEquals(invocationType, this)) {
            return true;
         } else if (invocationType.fPackage == this.fPackage) {
            return true;
         } else {
            TypeBinding currentType = invocationType.erasure();
            TypeBinding declaringClass = this.enclosingType().erasure();
            if (TypeBinding.equalsEquals(declaringClass, invocationType)) {
               return true;
            } else if (declaringClass == null) {
               return false;
            } else {
               while(currentType.findSuperTypeOriginatingFrom(declaringClass) == null) {
                  currentType = currentType.enclosingType();
                  if (currentType == null) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else if (!this.isPrivate()) {
         if (invocationType.fPackage != this.fPackage) {
            return false;
         } else {
            ReferenceBinding currentType = receiverType;
            TypeBinding originalDeclaringClass = (this.enclosingType() == null ? this : this.enclosingType()).original();

            do {
               if (currentType.isCapture()) {
                  if (TypeBinding.equalsEquals(originalDeclaringClass, currentType.erasure().original())) {
                     return true;
                  }
               } else if (TypeBinding.equalsEquals(originalDeclaringClass, currentType.original())) {
                  return true;
               }

               PackageBinding currentPackage = currentType.fPackage;
               if (currentPackage != null && currentPackage != this.fPackage) {
                  return false;
               }
            } while((currentType = currentType.superclass()) != null);

            return false;
         }
      } else {
         if (!TypeBinding.equalsEquals(receiverType, this) && !TypeBinding.equalsEquals(receiverType, this.enclosingType())) {
            if (!receiverType.isTypeVariable()) {
               return false;
            }

            TypeVariableBinding typeVariable = (TypeVariableBinding)receiverType;
            if (typeVariable.environment.globalOptions.complianceLevel > 3276800L
               || !typeVariable.isErasureBoundTo(this.erasure()) && !typeVariable.isErasureBoundTo(this.enclosingType().erasure())) {
               return false;
            }
         }

         if (TypeBinding.notEquals(invocationType, this)) {
            ReferenceBinding outerInvocationType = invocationType;

            for(ReferenceBinding temp = invocationType.enclosingType(); temp != null; temp = temp.enclosingType()) {
               outerInvocationType = temp;
            }

            ReferenceBinding outerDeclaringClass = (ReferenceBinding)this.erasure();

            for(ReferenceBinding var9 = outerDeclaringClass.enclosingType(); var9 != null; var9 = var9.enclosingType()) {
               outerDeclaringClass = var9;
            }

            if (TypeBinding.notEquals(outerInvocationType, outerDeclaringClass)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean canBeSeenBy(Scope scope) {
      if (this.isPublic()) {
         return true;
      } else {
         SourceTypeBinding invocationType = scope.enclosingSourceType();
         if (TypeBinding.equalsEquals(invocationType, this)) {
            return true;
         } else if (invocationType == null) {
            return !this.isPrivate() && scope.getCurrentPackage() == this.fPackage;
         } else if (this.isProtected()) {
            if (invocationType.fPackage == this.fPackage) {
               return true;
            } else {
               TypeBinding declaringClass = this.enclosingType();
               if (declaringClass == null) {
                  return false;
               } else {
                  declaringClass = declaringClass.erasure();
                  TypeBinding currentType = invocationType.erasure();

                  while(!TypeBinding.equalsEquals(declaringClass, invocationType)) {
                     if (currentType.findSuperTypeOriginatingFrom(declaringClass) != null) {
                        return true;
                     }

                     currentType = currentType.enclosingType();
                     if (currentType == null) {
                        return false;
                     }
                  }

                  return true;
               }
            }
         } else if (!this.isPrivate()) {
            return invocationType.fPackage == this.fPackage;
         } else {
            ReferenceBinding outerInvocationType = invocationType;

            for(ReferenceBinding temp = invocationType.enclosingType(); temp != null; temp = temp.enclosingType()) {
               outerInvocationType = temp;
            }

            ReferenceBinding outerDeclaringClass = (ReferenceBinding)this.erasure();

            for(ReferenceBinding var8 = outerDeclaringClass.enclosingType(); var8 != null; var8 = var8.enclosingType()) {
               outerDeclaringClass = var8;
            }

            return TypeBinding.equalsEquals(outerInvocationType, outerDeclaringClass);
         }
      }
   }

   public char[] computeGenericTypeSignature(TypeVariableBinding[] typeVariables) {
      boolean isMemberOfGeneric = this.isMemberType() && (this.enclosingType().modifiers & 1073741824) != 0;
      if (typeVariables == Binding.NO_TYPE_VARIABLES && !isMemberOfGeneric) {
         return this.signature();
      } else {
         StringBuffer sig = new StringBuffer(10);
         if (isMemberOfGeneric) {
            char[] typeSig = this.enclosingType().genericTypeSignature();
            sig.append(typeSig, 0, typeSig.length - 1);
            sig.append('.');
            sig.append(this.sourceName);
         } else {
            char[] typeSig = this.signature();
            sig.append(typeSig, 0, typeSig.length - 1);
         }

         if (typeVariables == Binding.NO_TYPE_VARIABLES) {
            sig.append(';');
         } else {
            sig.append('<');
            int i = 0;

            for(int length = typeVariables.length; i < length; ++i) {
               sig.append(typeVariables[i].genericTypeSignature());
            }

            sig.append(">;");
         }

         int sigLength = sig.length();
         char[] result = new char[sigLength];
         sig.getChars(0, sigLength, result, 0);
         return result;
      }
   }

   public void computeId() {
      switch(this.compoundName.length) {
         case 3:
            char[] packageName = this.compoundName[0];
            switch(packageName.length) {
               case 3:
                  if (CharOperation.equals(TypeConstants.ORG_JUNIT_ASSERT, this.compoundName)) {
                     this.id = 70;
                  }

                  return;
               case 4:
                  if (!CharOperation.equals(TypeConstants.JAVA, packageName)) {
                     return;
                  }

                  packageName = this.compoundName[1];
                  if (packageName.length == 0) {
                     return;
                  }

                  char[] typeName = this.compoundName[2];
                  if (typeName.length == 0) {
                     return;
                  }

                  if (!CharOperation.equals(TypeConstants.LANG, this.compoundName[1])) {
                     switch(packageName[0]) {
                        case 'i':
                           if (CharOperation.equals(packageName, TypeConstants.IO)) {
                              switch(typeName[0]) {
                                 case 'C':
                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_CLOSEABLE[2])) {
                                       this.typeBits |= 2;
                                    }

                                    return;
                                 case 'E':
                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_EXTERNALIZABLE[2])) {
                                       this.id = 56;
                                    }

                                    return;
                                 case 'I':
                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_IOEXCEPTION[2])) {
                                       this.id = 58;
                                    }

                                    return;
                                 case 'O':
                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_OBJECTSTREAMEXCEPTION[2])) {
                                       this.id = 57;
                                    }

                                    return;
                                 case 'P':
                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_PRINTSTREAM[2])) {
                                       this.id = 53;
                                    }

                                    return;
                                 case 'S':
                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_IO_SERIALIZABLE[2])) {
                                       this.id = 37;
                                    }

                                    return;
                              }
                           }

                           return;
                        case 'u':
                           if (CharOperation.equals(packageName, TypeConstants.UTIL)) {
                              switch(typeName[0]) {
                                 case 'C':
                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_UTIL_COLLECTION[2])) {
                                       this.id = 59;
                                    }

                                    return;
                                 case 'I':
                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_UTIL_ITERATOR[2])) {
                                       this.id = 39;
                                    }

                                    return;
                                 case 'O':
                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_UTIL_OBJECTS[2])) {
                                       this.id = 74;
                                    }

                                    return;
                              }
                           }

                           return;
                        default:
                           return;
                     }
                  }

                  switch(typeName[0]) {
                     case 'A':
                        switch(typeName.length) {
                           case 13:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_AUTOCLOSEABLE[2])) {
                                 this.id = 62;
                                 this.typeBits |= 1;
                              }

                              return;
                           case 14:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ASSERTIONERROR[2])) {
                                 this.id = 35;
                              }

                              return;
                           default:
                              return;
                        }
                     case 'B':
                        switch(typeName.length) {
                           case 4:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_BYTE[2])) {
                                 this.id = 26;
                              }

                              return;
                           case 5:
                           case 6:
                           default:
                              return;
                           case 7:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_BOOLEAN[2])) {
                                 this.id = 33;
                              }

                              return;
                        }
                     case 'C':
                        switch(typeName.length) {
                           case 5:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_CLASS[2])) {
                                 this.id = 16;
                              }

                              return;
                           case 9:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_CHARACTER[2])) {
                                 this.id = 28;
                              } else if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_CLONEABLE[2])) {
                                 this.id = 36;
                              }

                              return;
                           case 22:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_CLASSNOTFOUNDEXCEPTION[2])) {
                                 this.id = 23;
                              }

                              return;
                           default:
                              return;
                        }
                     case 'D':
                        switch(typeName.length) {
                           case 6:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_DOUBLE[2])) {
                                 this.id = 32;
                              }

                              return;
                           case 7:
                           case 8:
                           case 9:
                           default:
                              return;
                           case 10:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_DEPRECATED[2])) {
                                 this.id = 44;
                              }

                              return;
                        }
                     case 'E':
                        switch(typeName.length) {
                           case 4:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ENUM[2])) {
                                 this.id = 41;
                              }

                              return;
                           case 5:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ERROR[2])) {
                                 this.id = 19;
                              }

                              return;
                           case 6:
                           case 7:
                           case 8:
                           default:
                              return;
                           case 9:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_EXCEPTION[2])) {
                                 this.id = 25;
                              }

                              return;
                        }
                     case 'F':
                        if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_FLOAT[2])) {
                           this.id = 31;
                        } else if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_FUNCTIONAL_INTERFACE[2])) {
                           this.id = 77;
                        }

                        return;
                     case 'G':
                     case 'H':
                     case 'J':
                     case 'K':
                     case 'M':
                     case 'P':
                     case 'Q':
                     case 'U':
                     default:
                        return;
                     case 'I':
                        switch(typeName.length) {
                           case 7:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_INTEGER[2])) {
                                 this.id = 29;
                              }

                              return;
                           case 8:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ITERABLE[2])) {
                                 this.id = 38;
                              }

                              return;
                           case 24:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ILLEGALARGUMENTEXCEPTION[2])) {
                                 this.id = 42;
                              }

                              return;
                           default:
                              return;
                        }
                     case 'L':
                        if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_LONG[2])) {
                           this.id = 30;
                        }

                        return;
                     case 'N':
                        if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_NOCLASSDEFERROR[2])) {
                           this.id = 22;
                        }

                        return;
                     case 'O':
                        switch(typeName.length) {
                           case 6:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_OBJECT[2])) {
                                 this.id = 1;
                              }

                              return;
                           case 7:
                           default:
                              return;
                           case 8:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_OVERRIDE[2])) {
                                 this.id = 47;
                              }

                              return;
                        }
                     case 'R':
                        if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_RUNTIMEEXCEPTION[2])) {
                           this.id = 24;
                        }

                        return;
                     case 'S':
                        switch(typeName.length) {
                           case 5:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_SHORT[2])) {
                                 this.id = 27;
                              }

                              return;
                           case 6:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_STRING[2])) {
                                 this.id = 11;
                              } else if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_SYSTEM[2])) {
                                 this.id = 18;
                              }

                              return;
                           case 7:
                           case 8:
                           case 9:
                           case 10:
                           case 14:
                           case 15:
                           default:
                              return;
                           case 11:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_SAFEVARARGS[2])) {
                                 this.id = 60;
                              }

                              return;
                           case 12:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_STRINGBUFFER[2])) {
                                 this.id = 17;
                              }

                              return;
                           case 13:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_STRINGBUILDER[2])) {
                                 this.id = 40;
                              }

                              return;
                           case 16:
                              if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_SUPPRESSWARNINGS[2])) {
                                 this.id = 49;
                              }

                              return;
                        }
                     case 'T':
                        if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_THROWABLE[2])) {
                           this.id = 21;
                        }

                        return;
                     case 'V':
                        if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_VOID[2])) {
                           this.id = 34;
                        }

                        return;
                  }
               case 5:
                  switch(packageName[1]) {
                     case 'a':
                        if (CharOperation.equals(TypeConstants.JAVAX_ANNOTATION_INJECT_INJECT, this.compoundName)) {
                           this.id = 80;
                        }

                        return;
                     case 'u':
                        if (CharOperation.equals(TypeConstants.JUNIT_FRAMEWORK_ASSERT, this.compoundName)) {
                           this.id = 69;
                        }

                        return;
                     default:
                        return;
                  }
               default:
                  return;
            }
         case 4:
            if (CharOperation.equals(TypeConstants.COM_GOOGLE_INJECT_INJECT, this.compoundName)) {
               this.id = 81;
               return;
            }

            if (!CharOperation.equals(TypeConstants.JAVA, this.compoundName[0])) {
               return;
            }

            char[] packageName = this.compoundName[1];
            if (packageName.length == 0) {
               return;
            }

            packageName = this.compoundName[2];
            if (packageName.length == 0) {
               return;
            }

            char[] typeName = this.compoundName[3];
            if (typeName.length == 0) {
               return;
            }

            switch(packageName[0]) {
               case 'a':
                  if (CharOperation.equals(packageName, TypeConstants.ANNOTATION)) {
                     switch(typeName[0]) {
                        case 'A':
                           if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_ANNOTATION[3])) {
                              this.id = 43;
                           }

                           return;
                        case 'D':
                           if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_DOCUMENTED[3])) {
                              this.id = 45;
                           }

                           return;
                        case 'E':
                           if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_ELEMENTTYPE[3])) {
                              this.id = 52;
                           }

                           return;
                        case 'I':
                           if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_INHERITED[3])) {
                              this.id = 46;
                           }

                           return;
                        case 'R':
                           switch(typeName.length) {
                              case 9:
                                 if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_RETENTION[3])) {
                                    this.id = 48;
                                 }

                                 return;
                              case 10:
                                 if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_REPEATABLE[3])) {
                                    this.id = 90;
                                 }

                                 return;
                              case 11:
                              case 12:
                              case 13:
                              case 14:
                              default:
                                 return;
                              case 15:
                                 if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_RETENTIONPOLICY[3])) {
                                    this.id = 51;
                                 }

                                 return;
                           }
                        case 'T':
                           if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_ANNOTATION_TARGET[3])) {
                              this.id = 50;
                           }

                           return;
                     }
                  }

                  return;
               case 'i':
                  if (CharOperation.equals(packageName, TypeConstants.INVOKE)) {
                     if (typeName.length == 0) {
                        return;
                     }

                     switch(typeName[0]) {
                        case 'M':
                           if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_INVOKE_METHODHANDLE_$_POLYMORPHICSIGNATURE[3])) {
                              this.id = 61;
                           }

                           return;
                     }
                  }

                  return;
               case 'r':
                  if (CharOperation.equals(packageName, TypeConstants.REFLECT)) {
                     switch(typeName[0]) {
                        case 'C':
                           if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_REFLECT_CONSTRUCTOR[2])) {
                              this.id = 20;
                           }

                           return;
                        case 'F':
                           if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_REFLECT_FIELD[2])) {
                              this.id = 54;
                           }

                           return;
                        case 'M':
                           if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_REFLECT_METHOD[2])) {
                              this.id = 55;
                           }

                           return;
                     }
                  }

                  return;
               default:
                  return;
            }
         case 5:
            char[] packageName = this.compoundName[0];
            switch(packageName[0]) {
               case 'c':
                  if (!CharOperation.equals(TypeConstants.COM, this.compoundName[0])) {
                     return;
                  }

                  if (CharOperation.equals(TypeConstants.COM_GOOGLE_COMMON_BASE_PRECONDITIONS, this.compoundName)) {
                     this.id = 73;
                  }

                  return;
               case 'j':
                  if (!CharOperation.equals(TypeConstants.JAVA, this.compoundName[0])) {
                     return;
                  }

                  packageName = this.compoundName[1];
                  if (packageName.length == 0) {
                     return;
                  }

                  if (CharOperation.equals(TypeConstants.LANG, packageName)) {
                     packageName = this.compoundName[2];
                     if (packageName.length == 0) {
                        return;
                     }

                     switch(packageName[0]) {
                        case 'i':
                           if (CharOperation.equals(packageName, TypeConstants.INVOKE)) {
                              char[] typeName = this.compoundName[3];
                              if (typeName.length == 0) {
                                 return;
                              }

                              switch(typeName[0]) {
                                 case 'M':
                                    char[] memberTypeName = this.compoundName[4];
                                    if (memberTypeName.length == 0) {
                                       return;
                                    }

                                    if (CharOperation.equals(typeName, TypeConstants.JAVA_LANG_INVOKE_METHODHANDLE_POLYMORPHICSIGNATURE[3])
                                       && CharOperation.equals(memberTypeName, TypeConstants.JAVA_LANG_INVOKE_METHODHANDLE_POLYMORPHICSIGNATURE[4])) {
                                       this.id = 61;
                                    }

                                    return;
                              }
                           }

                           return;
                        default:
                           return;
                     }
                  }

                  return;
               case 'o':
                  if (!CharOperation.equals(TypeConstants.ORG, this.compoundName[0])) {
                     return;
                  }

                  packageName = this.compoundName[1];
                  if (packageName.length == 0) {
                     return;
                  }

                  switch(packageName[0]) {
                     case 'a':
                        if (CharOperation.equals(TypeConstants.APACHE, packageName) && CharOperation.equals(TypeConstants.COMMONS, this.compoundName[2])) {
                           if (CharOperation.equals(TypeConstants.ORG_APACHE_COMMONS_LANG_VALIDATE, this.compoundName)) {
                              this.id = 71;
                           } else if (CharOperation.equals(TypeConstants.ORG_APACHE_COMMONS_LANG3_VALIDATE, this.compoundName)) {
                              this.id = 72;
                           }
                        }

                        return;
                     case 'b':
                     case 'c':
                     case 'd':
                     default:
                        return;
                     case 'e':
                        if (CharOperation.equals(TypeConstants.ECLIPSE, packageName)) {
                           packageName = this.compoundName[2];
                           if (packageName.length == 0) {
                              return;
                           }

                           switch(packageName[0]) {
                              case 'c':
                                 if (CharOperation.equals(packageName, TypeConstants.CORE)) {
                                    char[] typeName = this.compoundName[3];
                                    if (typeName.length == 0) {
                                       return;
                                    }

                                    switch(typeName[0]) {
                                       case 'r':
                                          char[] memberTypeName = this.compoundName[4];
                                          if (memberTypeName.length == 0) {
                                             return;
                                          }

                                          if (CharOperation.equals(typeName, TypeConstants.ORG_ECLIPSE_CORE_RUNTIME_ASSERT[3])
                                             && CharOperation.equals(memberTypeName, TypeConstants.ORG_ECLIPSE_CORE_RUNTIME_ASSERT[4])) {
                                             this.id = 68;
                                          }

                                          return;
                                    }
                                 }

                                 return;
                              default:
                                 return;
                           }
                        }

                        return;
                  }
               default:
                  return;
            }
         case 6:
            if (CharOperation.equals(TypeConstants.ORG, this.compoundName[0])) {
               if (CharOperation.equals(TypeConstants.SPRING, this.compoundName[1])) {
                  if (CharOperation.equals(TypeConstants.AUTOWIRED, this.compoundName[5])
                     && CharOperation.equals(TypeConstants.ORG_SPRING_AUTOWIRED, this.compoundName)) {
                     this.id = 82;
                  }

                  return;
               }

               if (!CharOperation.equals(TypeConstants.JDT, this.compoundName[2]) || !CharOperation.equals(TypeConstants.ITYPEBINDING, this.compoundName[5])) {
                  return;
               }

               if (CharOperation.equals(TypeConstants.ORG_ECLIPSE_JDT_CORE_DOM_ITYPEBINDING, this.compoundName)) {
                  this.typeBits |= 16;
               }
            }
            break;
         case 7:
            if (!CharOperation.equals(TypeConstants.JDT, this.compoundName[2]) || !CharOperation.equals(TypeConstants.TYPEBINDING, this.compoundName[6])) {
               return;
            }

            if (CharOperation.equals(TypeConstants.ORG_ECLIPSE_JDT_INTERNAL_COMPILER_LOOKUP_TYPEBINDING, this.compoundName)) {
               this.typeBits |= 16;
            }
      }
   }

   public void computeId(LookupEnvironment environment) {
      environment.getUnannotatedType(this);
   }

   @Override
   public char[] computeUniqueKey(boolean isLeaf) {
      return !isLeaf ? this.signature() : this.genericTypeSignature();
   }

   @Override
   public char[] constantPoolName() {
      return this.constantPoolName != null ? this.constantPoolName : (this.constantPoolName = CharOperation.concatWith(this.compoundName, '/'));
   }

   @Override
   public String debugName() {
      return this.compoundName != null ? (this.hasTypeAnnotations() ? this.annotatedDebugName() : new String(this.readableName())) : "UNNAMED TYPE";
   }

   @Override
   public int depth() {
      int depth = 0;
      ReferenceBinding current = this;

      while((current = current.enclosingType()) != null) {
         ++depth;
      }

      return depth;
   }

   public boolean detectAnnotationCycle() {
      if ((this.tagBits & 4294967296L) != 0L) {
         return false;
      } else if ((this.tagBits & 2147483648L) != 0L) {
         return true;
      } else {
         this.tagBits |= 2147483648L;
         MethodBinding[] currentMethods = this.methods();
         boolean inCycle = false;
         int i = 0;

         for(int l = currentMethods.length; i < l; ++i) {
            TypeBinding returnType = currentMethods[i].returnType.leafComponentType().erasure();
            if (TypeBinding.equalsEquals(this, returnType)) {
               if (this instanceof SourceTypeBinding) {
                  MethodDeclaration decl = (MethodDeclaration)currentMethods[i].sourceMethod();
                  ((SourceTypeBinding)this).scope.problemReporter().annotationCircularity(this, this, decl != null ? decl.returnType : null);
               }
            } else if (returnType.isAnnotationType() && ((ReferenceBinding)returnType).detectAnnotationCycle()) {
               if (this instanceof SourceTypeBinding) {
                  MethodDeclaration decl = (MethodDeclaration)currentMethods[i].sourceMethod();
                  ((SourceTypeBinding)this).scope.problemReporter().annotationCircularity(this, returnType, decl != null ? decl.returnType : null);
               }

               inCycle = true;
            }
         }

         if (inCycle) {
            return true;
         } else {
            this.tagBits |= 4294967296L;
            return false;
         }
      }
   }

   public final ReferenceBinding enclosingTypeAt(int relativeDepth) {
      ReferenceBinding current = this;

      while(relativeDepth-- > 0 && current != null) {
         current = current.enclosingType();
      }

      return current;
   }

   public int enumConstantCount() {
      int count = 0;
      FieldBinding[] fields = this.fields();
      int i = 0;

      for(int length = fields.length; i < length; ++i) {
         if ((fields[i].modifiers & 16384) != 0) {
            ++count;
         }
      }

      return count;
   }

   public int fieldCount() {
      return this.fields().length;
   }

   public FieldBinding[] fields() {
      return Binding.NO_FIELDS;
   }

   public final int getAccessFlags() {
      return this.modifiers & 65535;
   }

   @Override
   public AnnotationBinding[] getAnnotations() {
      return this.retrieveAnnotations(this);
   }

   @Override
   public long getAnnotationTagBits() {
      return this.tagBits;
   }

   public int getEnclosingInstancesSlotSize() {
      if (this.isStatic()) {
         return 0;
      } else {
         return this.enclosingType() == null ? 0 : 1;
      }
   }

   public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
      return null;
   }

   public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes, CompilationUnitScope refScope) {
      return null;
   }

   public FieldBinding getField(char[] fieldName, boolean needResolve) {
      return null;
   }

   public char[] getFileName() {
      return this.fileName;
   }

   public ReferenceBinding getMemberType(char[] typeName) {
      ReferenceBinding[] memberTypes = this.memberTypes();
      int i = memberTypes.length;

      while(--i >= 0) {
         if (CharOperation.equals(memberTypes[i].sourceName, typeName)) {
            return memberTypes[i];
         }
      }

      return null;
   }

   @Override
   public MethodBinding[] getMethods(char[] selector) {
      return Binding.NO_METHODS;
   }

   public MethodBinding[] getMethods(char[] selector, int suggestedParameterLength) {
      return this.getMethods(selector);
   }

   public int getOuterLocalVariablesSlotSize() {
      return 0;
   }

   @Override
   public PackageBinding getPackage() {
      return this.fPackage;
   }

   public TypeVariableBinding getTypeVariable(char[] variableName) {
      TypeVariableBinding[] typeVariables = this.typeVariables();
      int i = typeVariables.length;

      while(--i >= 0) {
         if (CharOperation.equals(typeVariables[i].sourceName, variableName)) {
            return typeVariables[i];
         }
      }

      return null;
   }

   @Override
   public int hashCode() {
      return this.compoundName != null && this.compoundName.length != 0
         ? CharOperation.hashCode(this.compoundName[this.compoundName.length - 1])
         : super.hashCode();
   }

   public boolean hasIncompatibleSuperType(ReferenceBinding otherType) {
      if (TypeBinding.equalsEquals(this, otherType)) {
         return false;
      } else {
         ReferenceBinding[] interfacesToVisit = null;
         int nextPosition = 0;
         ReferenceBinding currentType = this;

         do {
            TypeBinding match = otherType.findSuperTypeOriginatingFrom(currentType);
            if (match != null && match.isProvablyDistinct(currentType)) {
               return true;
            }

            ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
            if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
               if (interfacesToVisit == null) {
                  interfacesToVisit = itsInterfaces;
                  nextPosition = itsInterfaces.length;
               } else {
                  int itsLength = itsInterfaces.length;
                  if (nextPosition + itsLength >= interfacesToVisit.length) {
                     System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                  }

                  label102:
                  for(int a = 0; a < itsLength; ++a) {
                     ReferenceBinding next = itsInterfaces[a];

                     for(int b = 0; b < nextPosition; ++b) {
                        if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                           continue label102;
                        }
                     }

                     interfacesToVisit[nextPosition++] = next;
                  }
               }
            }
         } while((currentType = currentType.superclass()) != null);

         for(int i = 0; i < nextPosition; ++i) {
            currentType = interfacesToVisit[i];
            if (TypeBinding.equalsEquals(currentType, otherType)) {
               return false;
            }

            TypeBinding var13 = otherType.findSuperTypeOriginatingFrom(currentType);
            if (var13 != null && var13.isProvablyDistinct(currentType)) {
               return true;
            }

            ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
            if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
               int itsLength = itsInterfaces.length;
               if (nextPosition + itsLength >= interfacesToVisit.length) {
                  System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
               }

               label74:
               for(int a = 0; a < itsLength; ++a) {
                  ReferenceBinding next = itsInterfaces[a];

                  for(int b = 0; b < nextPosition; ++b) {
                     if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                        continue label74;
                     }
                  }

                  interfacesToVisit[nextPosition++] = next;
               }
            }
         }

         return false;
      }
   }

   public boolean hasMemberTypes() {
      return false;
   }

   boolean hasNonNullDefaultFor(int location, boolean useTypeAnnotations) {
      for(ReferenceBinding currentType = this; currentType != null; currentType = currentType.enclosingType()) {
         if (useTypeAnnotations) {
            int nullDefault = ((ReferenceBinding)currentType.original()).getNullDefault();
            if (nullDefault != 0) {
               if ((nullDefault & location) != 0) {
                  return true;
               }

               return false;
            }
         } else {
            if ((currentType.tagBits & 144115188075855872L) != 0L) {
               return true;
            }

            if ((currentType.tagBits & 288230376151711744L) != 0L) {
               return false;
            }
         }
      }

      if (useTypeAnnotations) {
         return (this.getPackage().defaultNullness & location) != 0;
      } else {
         return this.getPackage().defaultNullness == 1;
      }
   }

   int getNullDefault() {
      return 0;
   }

   @Override
   public boolean acceptsNonNullDefault() {
      return true;
   }

   public final boolean hasRestrictedAccess() {
      return (this.modifiers & 262144) != 0;
   }

   public boolean hasNullBit(int mask) {
      return (this.typeBits & mask) != 0;
   }

   public boolean implementsInterface(ReferenceBinding anInterface, boolean searchHierarchy) {
      if (TypeBinding.equalsEquals(this, anInterface)) {
         return true;
      } else {
         ReferenceBinding[] interfacesToVisit = null;
         int nextPosition = 0;
         ReferenceBinding currentType = this;

         do {
            ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
            if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
               if (interfacesToVisit == null) {
                  interfacesToVisit = itsInterfaces;
                  nextPosition = itsInterfaces.length;
               } else {
                  int itsLength = itsInterfaces.length;
                  if (nextPosition + itsLength >= interfacesToVisit.length) {
                     System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
                  }

                  label90:
                  for(int a = 0; a < itsLength; ++a) {
                     ReferenceBinding next = itsInterfaces[a];

                     for(int b = 0; b < nextPosition; ++b) {
                        if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                           continue label90;
                        }
                     }

                     interfacesToVisit[nextPosition++] = next;
                  }
               }
            }
         } while(searchHierarchy && (currentType = currentType.superclass()) != null);

         for(int i = 0; i < nextPosition; ++i) {
            currentType = interfacesToVisit[i];
            if (currentType.isEquivalentTo(anInterface)) {
               return true;
            }

            ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
            if (itsInterfaces != null && itsInterfaces != Binding.NO_SUPERINTERFACES) {
               int itsLength = itsInterfaces.length;
               if (nextPosition + itsLength >= interfacesToVisit.length) {
                  System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[nextPosition + itsLength + 5], 0, nextPosition);
               }

               label64:
               for(int a = 0; a < itsLength; ++a) {
                  ReferenceBinding next = itsInterfaces[a];

                  for(int b = 0; b < nextPosition; ++b) {
                     if (TypeBinding.equalsEquals(next, interfacesToVisit[b])) {
                        continue label64;
                     }
                  }

                  interfacesToVisit[nextPosition++] = next;
               }
            }
         }

         return false;
      }
   }

   boolean implementsMethod(MethodBinding method) {
      char[] selector = method.selector;

      for(ReferenceBinding type = this; type != null; type = type.superclass()) {
         MethodBinding[] methods = type.methods();
         long range;
         if ((range = binarySearch(selector, methods)) >= 0L) {
            int start = (int)range;
            int end = (int)(range >> 32);

            for(int i = start; i <= end; ++i) {
               if (methods[i].areParametersEqual(method)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public final boolean isAbstract() {
      return (this.modifiers & 1024) != 0;
   }

   @Override
   public boolean isAnnotationType() {
      return (this.modifiers & 8192) != 0;
   }

   public final boolean isBinaryBinding() {
      return (this.tagBits & 64L) != 0L;
   }

   @Override
   public boolean isClass() {
      return (this.modifiers & 25088) == 0;
   }

   @Override
   public boolean isProperType(boolean admitCapture18) {
      ReferenceBinding outer = this.enclosingType();
      return outer != null && !outer.isProperType(admitCapture18) ? false : super.isProperType(admitCapture18);
   }

   @Override
   public boolean isCompatibleWith(TypeBinding otherType, Scope captureScope) {
      if (equalsEquals(otherType, this)) {
         return true;
      } else if (otherType.id == 1) {
         return true;
      } else {
         if (this.compatibleCache == null) {
            this.compatibleCache = new SimpleLookupTable(3);
            Object result = null;
         } else {
            Object result = this.compatibleCache.get(otherType);
            if (result != null) {
               if (result == Boolean.TRUE) {
                  return true;
               }

               return false;
            }
         }

         this.compatibleCache.put(otherType, Boolean.FALSE);
         if (this.isCompatibleWith0(otherType, captureScope)) {
            this.compatibleCache.put(otherType, Boolean.TRUE);
            return true;
         } else {
            if (captureScope == null && this instanceof TypeVariableBinding && ((TypeVariableBinding)this).firstBound instanceof ParameterizedTypeBinding) {
               this.compatibleCache.put(otherType, null);
            }

            return false;
         }
      }
   }

   private boolean isCompatibleWith0(TypeBinding otherType, Scope captureScope) {
      if (TypeBinding.equalsEquals(otherType, this)) {
         return true;
      } else if (otherType.id == 1) {
         return true;
      } else if (this.isEquivalentTo(otherType)) {
         return true;
      } else {
         switch(otherType.kind()) {
            case 516:
            case 8196:
               return false;
            case 4100:
               if (otherType.isCapture()) {
                  CaptureBinding otherCapture = (CaptureBinding)otherType;
                  TypeBinding otherLowerBound = otherCapture.lowerBound;
                  if (otherCapture.lowerBound != null) {
                     if (otherLowerBound.isArrayType()) {
                        return false;
                     }

                     return this.isCompatibleWith(otherLowerBound);
                  }
               }

               if (otherType instanceof InferenceVariable && captureScope != null) {
                  MethodScope methodScope = captureScope.methodScope();
                  if (methodScope != null) {
                     ReferenceContext referenceContext = methodScope.referenceContext;
                     if (referenceContext instanceof LambdaExpression && ((LambdaExpression)referenceContext).inferenceContext != null) {
                        return true;
                     }
                  }
               }
            case 4:
            case 260:
            case 1028:
            case 2052:
               switch(this.kind()) {
                  case 260:
                  case 1028:
                  case 2052:
                     if (TypeBinding.equalsEquals(this.erasure(), otherType.erasure())) {
                        return false;
                     }
                  default:
                     ReferenceBinding otherReferenceType = (ReferenceBinding)otherType;
                     if (otherReferenceType.isInterface()) {
                        if (this.implementsInterface(otherReferenceType, true)) {
                           return true;
                        }

                        if (this instanceof TypeVariableBinding && captureScope != null) {
                           TypeVariableBinding typeVariable = (TypeVariableBinding)this;
                           if (typeVariable.firstBound instanceof ParameterizedTypeBinding) {
                              TypeBinding bound = typeVariable.firstBound.capture(captureScope, -1, -1);
                              return bound.isCompatibleWith(otherReferenceType);
                           }
                        }
                     }

                     if (this.isInterface()) {
                        return false;
                     }

                     return otherReferenceType.isSuperclassOf(this);
               }
            default:
               return false;
         }
      }
   }

   @Override
   public boolean isSubtypeOf(TypeBinding other) {
      if (this.isSubTypeOfRTL(other)) {
         return true;
      } else {
         TypeBinding candidate = this.findSuperTypeOriginatingFrom(other);
         if (candidate == null) {
            return false;
         } else if (TypeBinding.equalsEquals(candidate, other)) {
            return true;
         } else if (other.isRawType() && TypeBinding.equalsEquals(candidate.erasure(), other.erasure())) {
            return true;
         } else {
            TypeBinding[] sis = other.typeArguments();
            TypeBinding[] tis = candidate.typeArguments();
            if (tis != null && sis != null) {
               if (sis.length != tis.length) {
                  return false;
               } else {
                  for(int i = 0; i < sis.length; ++i) {
                     if (!tis[i].isTypeArgumentContainedBy(sis[i])) {
                        return false;
                     }
                  }

                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }

   protected boolean isSubTypeOfRTL(TypeBinding other) {
      if (TypeBinding.equalsEquals(this, other)) {
         return true;
      } else if (other instanceof CaptureBinding) {
         TypeBinding lower = ((CaptureBinding)other).lowerBound;
         return lower != null && this.isSubtypeOf(lower);
      } else {
         if (other instanceof ReferenceBinding) {
            TypeBinding[] intersecting = ((ReferenceBinding)other).getIntersectingTypes();
            if (intersecting != null) {
               for(int i = 0; i < intersecting.length; ++i) {
                  if (!this.isSubtypeOf(intersecting[i])) {
                     return false;
                  }
               }

               return true;
            }
         }

         return false;
      }
   }

   public final boolean isDefault() {
      return (this.modifiers & 7) == 0;
   }

   public final boolean isDeprecated() {
      return (this.modifiers & 1048576) != 0;
   }

   @Override
   public boolean isEnum() {
      return (this.modifiers & 16384) != 0;
   }

   public final boolean isFinal() {
      return (this.modifiers & 16) != 0;
   }

   public boolean isHierarchyBeingConnected() {
      return (this.tagBits & 512L) == 0L && (this.tagBits & 256L) != 0L;
   }

   public boolean isHierarchyBeingActivelyConnected() {
      return (this.tagBits & 512L) == 0L && (this.tagBits & 256L) != 0L && (this.tagBits & 524288L) == 0L;
   }

   public boolean isHierarchyConnected() {
      return true;
   }

   @Override
   public boolean isInterface() {
      return (this.modifiers & 512) != 0;
   }

   @Override
   public boolean isFunctionalInterface(Scope scope) {
      MethodBinding method;
      return this.isInterface() && (method = this.getSingleAbstractMethod(scope, true)) != null && method.isValidBinding();
   }

   public final boolean isPrivate() {
      return (this.modifiers & 2) != 0;
   }

   public final boolean isOrEnclosedByPrivateType() {
      if (this.isLocalType()) {
         return true;
      } else {
         for(ReferenceBinding type = this; type != null; type = type.enclosingType()) {
            if ((type.modifiers & 2) != 0) {
               return true;
            }
         }

         return false;
      }
   }

   public final boolean isProtected() {
      return (this.modifiers & 4) != 0;
   }

   public final boolean isPublic() {
      return (this.modifiers & 1) != 0;
   }

   @Override
   public final boolean isStatic() {
      return (this.modifiers & 520) != 0 || (this.tagBits & 4L) == 0L;
   }

   public final boolean isStrictfp() {
      return (this.modifiers & 2048) != 0;
   }

   public boolean isSuperclassOf(ReferenceBinding otherType) {
      while((otherType = otherType.superclass()) != null) {
         if (otherType.isEquivalentTo(this)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isThrowable() {
      ReferenceBinding current = this;

      while(true) {
         switch(current.id) {
            case 19:
            case 21:
            case 24:
            case 25:
               return true;
            case 20:
            case 22:
            case 23:
            default:
               if ((current = current.superclass()) == null) {
                  return false;
               }
         }
      }
   }

   @Override
   public boolean isUncheckedException(boolean includeSupertype) {
      switch(this.id) {
         case 19:
         case 24:
            return true;
         case 20:
         case 22:
         case 23:
         default:
            ReferenceBinding current = this;

            while((current = current.superclass()) != null) {
               switch(current.id) {
                  case 19:
                  case 24:
                     return true;
                  case 20:
                  case 22:
                  case 23:
                  default:
                     break;
                  case 21:
                  case 25:
                     return false;
               }
            }

            return false;
         case 21:
         case 25:
            return includeSupertype;
      }
   }

   public final boolean isUsed() {
      return (this.modifiers & 134217728) != 0;
   }

   public final boolean isViewedAsDeprecated() {
      return (this.modifiers & 3145728) != 0 || this.getPackage().isViewedAsDeprecated();
   }

   public ReferenceBinding[] memberTypes() {
      return Binding.NO_MEMBER_TYPES;
   }

   public MethodBinding[] methods() {
      return Binding.NO_METHODS;
   }

   public final ReferenceBinding outermostEnclosingType() {
      ReferenceBinding current = this;

      ReferenceBinding last;
      do {
         last = current;
      } while((current = current.enclosingType()) != null);

      return last;
   }

   @Override
   public char[] qualifiedSourceName() {
      return this.isMemberType() ? CharOperation.concat(this.enclosingType().qualifiedSourceName(), this.sourceName(), '.') : this.sourceName();
   }

   @Override
   public char[] readableName() {
      char[] readableName;
      if (this.isMemberType()) {
         readableName = CharOperation.concat(this.enclosingType().readableName(), this.sourceName, '.');
      } else {
         readableName = CharOperation.concatWith(this.compoundName, '.');
      }

      TypeVariableBinding[] typeVars;
      if ((typeVars = this.typeVariables()) != Binding.NO_TYPE_VARIABLES) {
         StringBuffer nameBuffer = new StringBuffer(10);
         nameBuffer.append(readableName).append('<');
         int i = 0;

         for(int length = typeVars.length; i < length; ++i) {
            if (i > 0) {
               nameBuffer.append(',');
            }

            nameBuffer.append(typeVars[i].readableName());
         }

         nameBuffer.append('>');
         i = nameBuffer.length();
         readableName = new char[i];
         nameBuffer.getChars(0, i, readableName, 0);
      }

      return readableName;
   }

   protected void appendNullAnnotation(StringBuffer nameBuffer, CompilerOptions options) {
      if (options.isAnnotationBasedNullAnalysisEnabled) {
         if (options.usesNullTypeAnnotations()) {
            for(AnnotationBinding annotation : this.typeAnnotations) {
               ReferenceBinding annotationType = annotation.getAnnotationType();
               if (annotationType.hasNullBit(96)) {
                  nameBuffer.append('@').append(annotationType.shortReadableName()).append(' ');
               }
            }
         } else {
            if ((this.tagBits & 72057594037927936L) != 0L) {
               char[][] nonNullAnnotationName = options.nonNullAnnotationName;
               nameBuffer.append('@').append(nonNullAnnotationName[nonNullAnnotationName.length - 1]).append(' ');
            }

            if ((this.tagBits & 36028797018963968L) != 0L) {
               char[][] nullableAnnotationName = options.nullableAnnotationName;
               nameBuffer.append('@').append(nullableAnnotationName[nullableAnnotationName.length - 1]).append(' ');
            }
         }
      }
   }

   public AnnotationHolder retrieveAnnotationHolder(Binding binding, boolean forceInitialization) {
      SimpleLookupTable store = this.storedAnnotations(forceInitialization);
      return store == null ? null : (AnnotationHolder)store.get(binding);
   }

   AnnotationBinding[] retrieveAnnotations(Binding binding) {
      AnnotationHolder holder = this.retrieveAnnotationHolder(binding, true);
      return holder == null ? Binding.NO_ANNOTATIONS : holder.getAnnotations();
   }

   @Override
   public void setAnnotations(AnnotationBinding[] annotations) {
      this.storeAnnotations(this, annotations);
   }

   public void setContainerAnnotationType(ReferenceBinding value) {
   }

   public void tagAsHavingDefectiveContainerType() {
   }

   @Override
   public char[] nullAnnotatedReadableName(CompilerOptions options, boolean shortNames) {
      return shortNames ? this.nullAnnotatedShortReadableName(options) : this.nullAnnotatedReadableName(options);
   }

   char[] nullAnnotatedReadableName(CompilerOptions options) {
      StringBuffer nameBuffer = new StringBuffer(10);
      if (this.isMemberType()) {
         nameBuffer.append(this.enclosingType().nullAnnotatedReadableName(options, false));
         nameBuffer.append('.');
         this.appendNullAnnotation(nameBuffer, options);
         nameBuffer.append(this.sourceName);
      } else if (this.compoundName != null) {
         int l = this.compoundName.length;

         int i;
         for(i = 0; i < l - 1; ++i) {
            nameBuffer.append(this.compoundName[i]);
            nameBuffer.append('.');
         }

         this.appendNullAnnotation(nameBuffer, options);
         nameBuffer.append(this.compoundName[i]);
      } else {
         this.appendNullAnnotation(nameBuffer, options);
         if (this.sourceName != null) {
            nameBuffer.append(this.sourceName);
         } else {
            nameBuffer.append(this.readableName());
         }
      }

      TypeBinding[] arguments = this.typeArguments();
      if (arguments != null && arguments.length > 0) {
         nameBuffer.append('<');
         int i = 0;

         for(int length = arguments.length; i < length; ++i) {
            if (i > 0) {
               nameBuffer.append(',');
            }

            nameBuffer.append(arguments[i].nullAnnotatedReadableName(options, false));
         }

         nameBuffer.append('>');
      }

      int nameLength = nameBuffer.length();
      char[] readableName = new char[nameLength];
      nameBuffer.getChars(0, nameLength, readableName, 0);
      return readableName;
   }

   char[] nullAnnotatedShortReadableName(CompilerOptions options) {
      StringBuffer nameBuffer = new StringBuffer(10);
      if (this.isMemberType()) {
         nameBuffer.append(this.enclosingType().nullAnnotatedReadableName(options, true));
         nameBuffer.append('.');
         this.appendNullAnnotation(nameBuffer, options);
         nameBuffer.append(this.sourceName);
      } else {
         this.appendNullAnnotation(nameBuffer, options);
         if (this.sourceName != null) {
            nameBuffer.append(this.sourceName);
         } else {
            nameBuffer.append(this.shortReadableName());
         }
      }

      TypeBinding[] arguments = this.typeArguments();
      if (arguments != null && arguments.length > 0) {
         nameBuffer.append('<');
         int i = 0;

         for(int length = arguments.length; i < length; ++i) {
            if (i > 0) {
               nameBuffer.append(',');
            }

            nameBuffer.append(arguments[i].nullAnnotatedReadableName(options, true));
         }

         nameBuffer.append('>');
      }

      int nameLength = nameBuffer.length();
      char[] shortReadableName = new char[nameLength];
      nameBuffer.getChars(0, nameLength, shortReadableName, 0);
      return shortReadableName;
   }

   @Override
   public char[] shortReadableName() {
      char[] shortReadableName;
      if (this.isMemberType()) {
         shortReadableName = CharOperation.concat(this.enclosingType().shortReadableName(), this.sourceName, '.');
      } else {
         shortReadableName = this.sourceName;
      }

      TypeVariableBinding[] typeVars;
      if ((typeVars = this.typeVariables()) != Binding.NO_TYPE_VARIABLES) {
         StringBuffer nameBuffer = new StringBuffer(10);
         nameBuffer.append(shortReadableName).append('<');
         int i = 0;

         for(int length = typeVars.length; i < length; ++i) {
            if (i > 0) {
               nameBuffer.append(',');
            }

            nameBuffer.append(typeVars[i].shortReadableName());
         }

         nameBuffer.append('>');
         i = nameBuffer.length();
         shortReadableName = new char[i];
         nameBuffer.getChars(0, i, shortReadableName, 0);
      }

      return shortReadableName;
   }

   @Override
   public char[] signature() {
      return this.signature != null ? this.signature : (this.signature = CharOperation.concat('L', this.constantPoolName(), ';'));
   }

   @Override
   public char[] sourceName() {
      return this.sourceName;
   }

   void storeAnnotationHolder(Binding binding, AnnotationHolder holder) {
      if (holder == null) {
         SimpleLookupTable store = this.storedAnnotations(false);
         if (store != null) {
            store.removeKey(binding);
         }
      } else {
         SimpleLookupTable store = this.storedAnnotations(true);
         if (store != null) {
            store.put(binding, holder);
         }
      }
   }

   void storeAnnotations(Binding binding, AnnotationBinding[] annotations) {
      AnnotationHolder holder = null;
      if (annotations != null && annotations.length != 0) {
         SimpleLookupTable store = this.storedAnnotations(true);
         if (store == null) {
            return;
         }

         holder = (AnnotationHolder)store.get(binding);
         if (holder == null) {
            holder = new AnnotationHolder();
         }
      } else {
         SimpleLookupTable store = this.storedAnnotations(false);
         if (store != null) {
            holder = (AnnotationHolder)store.get(binding);
         }

         if (holder == null) {
            return;
         }
      }

      this.storeAnnotationHolder(binding, holder.setAnnotations(annotations));
   }

   SimpleLookupTable storedAnnotations(boolean forceInitialize) {
      return null;
   }

   @Override
   public ReferenceBinding superclass() {
      return null;
   }

   @Override
   public ReferenceBinding[] superInterfaces() {
      return Binding.NO_SUPERINTERFACES;
   }

   public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
      if (this.isStatic()) {
         return null;
      } else {
         ReferenceBinding enclosingType = this.enclosingType();
         return enclosingType == null ? null : new ReferenceBinding[]{enclosingType};
      }
   }

   MethodBinding[] unResolvedMethods() {
      return this.methods();
   }

   public FieldBinding[] unResolvedFields() {
      return Binding.NO_FIELDS;
   }

   protected int applyCloseableClassWhitelists() {
      switch(this.compoundName.length) {
         case 3:
            if (CharOperation.equals(TypeConstants.JAVA, this.compoundName[0]) && CharOperation.equals(TypeConstants.IO, this.compoundName[1])) {
               char[] simpleName = this.compoundName[2];
               int l = TypeConstants.JAVA_IO_WRAPPER_CLOSEABLES.length;

               for(int i = 0; i < l; ++i) {
                  if (CharOperation.equals(simpleName, TypeConstants.JAVA_IO_WRAPPER_CLOSEABLES[i])) {
                     return 4;
                  }
               }

               l = TypeConstants.JAVA_IO_RESOURCE_FREE_CLOSEABLES.length;

               for(int i = 0; i < l; ++i) {
                  if (CharOperation.equals(simpleName, TypeConstants.JAVA_IO_RESOURCE_FREE_CLOSEABLES[i])) {
                     return 8;
                  }
               }
            }
            break;
         case 4:
            if (CharOperation.equals(TypeConstants.JAVA, this.compoundName[0])
               && CharOperation.equals(TypeConstants.UTIL, this.compoundName[1])
               && CharOperation.equals(TypeConstants.ZIP, this.compoundName[2])) {
               char[] simpleName = this.compoundName[3];
               int l = TypeConstants.JAVA_UTIL_ZIP_WRAPPER_CLOSEABLES.length;

               for(int i = 0; i < l; ++i) {
                  if (CharOperation.equals(simpleName, TypeConstants.JAVA_UTIL_ZIP_WRAPPER_CLOSEABLES[i])) {
                     return 4;
                  }
               }
            }
      }

      int l = TypeConstants.OTHER_WRAPPER_CLOSEABLES.length;

      for(int i = 0; i < l; ++i) {
         if (CharOperation.equals(this.compoundName, TypeConstants.OTHER_WRAPPER_CLOSEABLES[i])) {
            return 4;
         }
      }

      return 0;
   }

   protected int applyCloseableInterfaceWhitelists() {
      switch(this.compoundName.length) {
         case 4:
            if (CharOperation.equals(this.compoundName, TypeConstants.RESOURCE_FREE_CLOSEABLE_STREAM)) {
               return 8;
            }
         default:
            return 0;
      }
   }

   protected MethodBinding[] getInterfaceAbstractContracts(Scope scope, boolean replaceWildcards) throws InvalidInputException {
      if (this.isInterface() && this.isValidBinding()) {
         MethodBinding[] methods = this.methods();
         MethodBinding[] contracts = new MethodBinding[0];
         int contractsCount = 0;
         int contractsLength = 0;
         ReferenceBinding[] superInterfaces = this.superInterfaces();
         int i = 0;

         for(int length = superInterfaces.length; i < length; ++i) {
            MethodBinding[] superInterfaceContracts = superInterfaces[i].getInterfaceAbstractContracts(scope, replaceWildcards);
            int superInterfaceContractsLength = superInterfaceContracts == null ? 0 : superInterfaceContracts.length;
            if (superInterfaceContractsLength != 0) {
               if (contractsLength < contractsCount + superInterfaceContractsLength) {
                  System.arraycopy(
                     contracts, 0, contracts = new MethodBinding[contractsLength = contractsCount + superInterfaceContractsLength], 0, contractsCount
                  );
               }

               System.arraycopy(superInterfaceContracts, 0, contracts, contractsCount, superInterfaceContractsLength);
               contractsCount += superInterfaceContractsLength;
            }
         }

         i = 0;

         for(int length = methods == null ? 0 : methods.length; i < length; ++i) {
            MethodBinding method = methods[i];
            if (method != null && !method.isStatic() && !method.redeclaresPublicObjectMethod(scope)) {
               if (!method.isValidBinding()) {
                  throw new InvalidInputException("Not a functional interface");
               }

               int j = 0;

               while(j < contractsCount) {
                  if (contracts[j] != null && MethodVerifier.doesMethodOverride(method, contracts[j], scope.environment())) {
                     if (j < --contractsCount) {
                        System.arraycopy(contracts, j + 1, contracts, j, contractsCount - j);
                        continue;
                     }
                  }

                  ++j;
               }

               if (!method.isDefaultMethod()) {
                  if (contractsCount == contractsLength) {
                     contractsLength += 16;
                     System.arraycopy(contracts, 0, contracts = new MethodBinding[contractsLength], 0, contractsCount);
                  }

                  contracts[contractsCount++] = method;
               }
            }
         }

         if (contractsCount < contractsLength) {
            System.arraycopy(contracts, 0, contracts = new MethodBinding[contractsCount], 0, contractsCount);
         }

         return contracts;
      } else {
         throw new InvalidInputException("Not a functional interface");
      }
   }

   @Override
   public MethodBinding getSingleAbstractMethod(Scope scope, boolean replaceWildcards) {
      int index = replaceWildcards ? 0 : 1;
      if (this.singleAbstractMethod != null) {
         if (this.singleAbstractMethod[index] != null) {
            return this.singleAbstractMethod[index];
         }
      } else {
         this.singleAbstractMethod = new MethodBinding[2];
      }

      if (this.compoundName != null) {
         scope.compilationUnitScope().recordQualifiedReference(this.compoundName);
      }

      MethodBinding[] methods = null;

      try {
         methods = this.getInterfaceAbstractContracts(scope, replaceWildcards);
         if (methods == null || methods.length == 0) {
            return this.singleAbstractMethod[index] = samProblemBinding;
         }

         int contractParameterLength = 0;
         char[] contractSelector = null;
         int i = 0;

         for(int length = methods.length; i < length; ++i) {
            MethodBinding method = methods[i];
            if (method != null) {
               if (contractSelector == null) {
                  contractSelector = method.selector;
                  contractParameterLength = method.parameters == null ? 0 : method.parameters.length;
               } else {
                  int methodParameterLength = method.parameters == null ? 0 : method.parameters.length;
                  if (methodParameterLength != contractParameterLength || !CharOperation.equals(method.selector, contractSelector)) {
                     return this.singleAbstractMethod[index] = samProblemBinding;
                  }
               }
            }
         }
      } catch (InvalidInputException var30) {
         return this.singleAbstractMethod[index] = samProblemBinding;
      }

      if (methods.length == 1) {
         return this.singleAbstractMethod[index] = methods[0];
      } else {
         LookupEnvironment environment = scope.environment();
         boolean genericMethodSeen = false;
         int length = methods.length;
         boolean analyseNullAnnotations = environment.globalOptions.isAnnotationBasedNullAnalysisEnabled;

         label222:
         for(int i = length - 1; i >= 0; --i) {
            MethodBinding method = methods[i];
            MethodBinding otherMethod = null;
            if (method.typeVariables != Binding.NO_TYPE_VARIABLES) {
               genericMethodSeen = true;
            }

            TypeBinding returnType = method.returnType;
            TypeBinding[] parameters = method.parameters;

            for(int j = 0; j < length; ++j) {
               if (i != j) {
                  otherMethod = methods[j];
                  if (otherMethod.typeVariables != Binding.NO_TYPE_VARIABLES) {
                     genericMethodSeen = true;
                  }

                  if (genericMethodSeen) {
                     otherMethod = MethodVerifier.computeSubstituteMethod(otherMethod, method, environment);
                     if (otherMethod == null) {
                        continue label222;
                     }
                  }

                  if (!MethodVerifier.isSubstituteParameterSubsignature(method, otherMethod, environment)
                     || !MethodVerifier.areReturnTypesCompatible(method, otherMethod, environment)) {
                     continue label222;
                  }

                  if (analyseNullAnnotations) {
                     returnType = NullAnnotationMatching.strongerType(returnType, otherMethod.returnType, environment);
                     parameters = NullAnnotationMatching.weakerTypes(parameters, otherMethod.parameters, environment);
                  }
               }
            }

            ReferenceBinding[] exceptions = new ReferenceBinding[0];
            int exceptionsCount = 0;
            int exceptionsLength = 0;
            MethodBinding theAbstractMethod = method;
            boolean shouldEraseThrows = method.typeVariables == Binding.NO_TYPE_VARIABLES && genericMethodSeen;
            boolean shouldAdaptThrows = method.typeVariables != Binding.NO_TYPE_VARIABLES;
            int typeVariableLength = method.typeVariables.length;

            label198:
            for(int var37 = 0; var37 < length; ++var37) {
               method = methods[var37];
               ReferenceBinding[] methodThrownExceptions = method.thrownExceptions;
               int methodExceptionsLength = methodThrownExceptions == null ? 0 : methodThrownExceptions.length;
               if (methodExceptionsLength == 0) {
                  break;
               }

               if (shouldAdaptThrows && method != theAbstractMethod) {
                  System.arraycopy(methodThrownExceptions, 0, methodThrownExceptions = new ReferenceBinding[methodExceptionsLength], 0, methodExceptionsLength);

                  for(int tv = 0; tv < typeVariableLength; ++tv) {
                     if (methodThrownExceptions[tv] instanceof TypeVariableBinding) {
                        methodThrownExceptions[tv] = theAbstractMethod.typeVariables[tv];
                     }
                  }
               }

               label187:
               for(int j = 0; j < methodExceptionsLength; ++j) {
                  ReferenceBinding methodException = methodThrownExceptions[j];
                  if (shouldEraseThrows) {
                     methodException = (ReferenceBinding)methodException.erasure();
                  }

                  for(int k = 0; k < length; ++k) {
                     if (var37 != k) {
                        otherMethod = methods[k];
                        ReferenceBinding[] otherMethodThrownExceptions = otherMethod.thrownExceptions;
                        int otherMethodExceptionsLength = otherMethodThrownExceptions == null ? 0 : otherMethodThrownExceptions.length;
                        if (otherMethodExceptionsLength == 0) {
                           break label198;
                        }

                        if (shouldAdaptThrows && otherMethod != theAbstractMethod) {
                           System.arraycopy(
                              otherMethodThrownExceptions,
                              0,
                              otherMethodThrownExceptions = new ReferenceBinding[otherMethodExceptionsLength],
                              0,
                              otherMethodExceptionsLength
                           );

                           for(int tv = 0; tv < typeVariableLength; ++tv) {
                              if (otherMethodThrownExceptions[tv] instanceof TypeVariableBinding) {
                                 otherMethodThrownExceptions[tv] = theAbstractMethod.typeVariables[tv];
                              }
                           }
                        }

                        int l = 0;

                        while(true) {
                           if (l >= otherMethodExceptionsLength) {
                              continue label187;
                           }

                           ReferenceBinding otherException = otherMethodThrownExceptions[l];
                           if (shouldEraseThrows) {
                              otherException = (ReferenceBinding)otherException.erasure();
                           }

                           if (methodException.isCompatibleWith(otherException)) {
                              break;
                           }

                           ++l;
                        }
                     }
                  }

                  if (exceptionsCount == exceptionsLength) {
                     exceptionsLength += 16;
                     System.arraycopy(exceptions, 0, exceptions = new ReferenceBinding[exceptionsLength], 0, exceptionsCount);
                  }

                  exceptions[exceptionsCount++] = methodException;
               }
            }

            if (exceptionsCount != exceptionsLength) {
               System.arraycopy(exceptions, 0, exceptions = new ReferenceBinding[exceptionsCount], 0, exceptionsCount);
            }

            this.singleAbstractMethod[index] = new MethodBinding(
               theAbstractMethod.modifiers | 4096, theAbstractMethod.selector, returnType, parameters, exceptions, theAbstractMethod.declaringClass
            );
            this.singleAbstractMethod[index].typeVariables = theAbstractMethod.typeVariables;
            return this.singleAbstractMethod[index];
         }

         return this.singleAbstractMethod[index] = samProblemBinding;
      }
   }

   public static boolean isConsistentIntersection(TypeBinding[] intersectingTypes) {
      TypeBinding[] ci = new TypeBinding[intersectingTypes.length];

      for(int i = 0; i < ci.length; ++i) {
         TypeBinding current = intersectingTypes[i];
         ci[i] = (TypeBinding)(!current.isClass() && !current.isArrayType() ? current.superclass() : current);
      }

      TypeBinding mostSpecific = ci[0];

      for(int i = 1; i < ci.length; ++i) {
         TypeBinding current = ci[i];
         if (!current.isTypeVariable() && !current.isWildcard() && current.isProperType(true) && !mostSpecific.isSubtypeOf(current)) {
            if (!current.isSubtypeOf(mostSpecific)) {
               return false;
            }

            mostSpecific = current;
         }
      }

      return true;
   }
}
