package org.eclipse.jdt.internal.compiler.parser;

import java.util.ArrayList;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public abstract class TypeConverter {
   int namePos;
   protected ProblemReporter problemReporter;
   protected boolean has1_5Compliance;
   private char memberTypeSeparator;

   protected TypeConverter(ProblemReporter problemReporter, char memberTypeSeparator) {
      this.problemReporter = problemReporter;
      this.has1_5Compliance = problemReporter.options.originalComplianceLevel >= 3211264L;
      this.memberTypeSeparator = memberTypeSeparator;
   }

   private void addIdentifiers(String typeSignature, int start, int endExclusive, int identCount, ArrayList fragments) {
      if (identCount == 1) {
         char[] identifier;
         typeSignature.getChars(start, endExclusive, identifier = new char[endExclusive - start], 0);
         fragments.add(identifier);
      } else {
         fragments.add(this.extractIdentifiers(typeSignature, start, endExclusive - 1, identCount));
      }
   }

   protected ImportReference createImportReference(String[] importName, int start, int end, boolean onDemand, int modifiers) {
      int length = importName.length;
      long[] positions = new long[length];
      long position = ((long)start << 32) + (long)end;
      char[][] qImportName = new char[length][];

      for(int i = 0; i < length; ++i) {
         qImportName[i] = importName[i].toCharArray();
         positions[i] = position;
      }

      return new ImportReference(qImportName, positions, onDemand, modifiers);
   }

   protected TypeParameter createTypeParameter(char[] typeParameterName, char[][] typeParameterBounds, int start, int end) {
      TypeParameter parameter = new TypeParameter();
      parameter.name = typeParameterName;
      parameter.sourceStart = start;
      parameter.sourceEnd = end;
      if (typeParameterBounds != null) {
         int length = typeParameterBounds.length;
         if (length > 0) {
            parameter.type = this.createTypeReference(typeParameterBounds[0], start, end);
            if (length > 1) {
               parameter.bounds = new TypeReference[length - 1];

               for(int i = 1; i < length; ++i) {
                  TypeReference bound = this.createTypeReference(typeParameterBounds[i], start, end);
                  bound.bits |= 16;
                  parameter.bounds[i - 1] = bound;
               }
            }
         }
      }

      return parameter;
   }

   protected TypeReference createTypeReference(char[] typeName, int start, int end, boolean includeGenericsAnyway) {
      int length = typeName.length;
      this.namePos = 0;
      return this.decodeType2(typeName, length, start, end, true);
   }

   protected TypeReference createTypeReference(char[] typeName, int start, int end) {
      int length = typeName.length;
      this.namePos = 0;
      return this.decodeType2(typeName, length, start, end, false);
   }

   protected TypeReference createTypeReference(String typeSignature, int start, int end) {
      int length = typeSignature.length();
      this.namePos = 0;
      return this.decodeType(typeSignature, length, start, end);
   }

   private TypeReference decodeType(String typeSignature, int length, int start, int end) {
      int identCount = 1;
      int dim = 0;
      int nameFragmentStart = this.namePos;
      int nameFragmentEnd = -1;
      boolean nameStarted = false;
      ArrayList fragments = null;

      while(true) {
         label189: {
            if (this.namePos < length) {
               char currentChar = typeSignature.charAt(this.namePos);
               switch(currentChar) {
                  case '$':
                     if (this.memberTypeSeparator != '$') {
                        break label189;
                     }
                  case '.':
                     if (!nameStarted) {
                        nameFragmentStart = this.namePos + 1;
                        nameStarted = true;
                     } else if (this.namePos > nameFragmentStart) {
                        ++identCount;
                     }
                     break label189;
                  case '*': {
                     ++this.namePos;
                     Wildcard result = new Wildcard(0);
                     result.sourceStart = start;
                     result.sourceEnd = end;
                     return result;
                  }
                  case '+': {
                     ++this.namePos;
                     Wildcard result = new Wildcard(1);
                     result.bound = this.decodeType(typeSignature, length, start, end);
                     result.sourceStart = start;
                     result.sourceEnd = end;
                     return result;
                  }
                  case '-': {
                     ++this.namePos;
                     Wildcard result = new Wildcard(2);
                     result.bound = this.decodeType(typeSignature, length, start, end);
                     result.sourceStart = start;
                     result.sourceEnd = end;
                     return result;
                  }
                  case ';':
                  case '>':
                     nameFragmentEnd = this.namePos - 1;
                     ++this.namePos;
                     break;
                  case '<':
                     nameFragmentEnd = this.namePos - 1;
                     if (this.has1_5Compliance) {
                        if (fragments == null) {
                           fragments = new ArrayList(2);
                        }

                        this.addIdentifiers(typeSignature, nameFragmentStart, nameFragmentEnd + 1, identCount, fragments);
                        ++this.namePos;
                        TypeReference[] arguments = this.decodeTypeArguments(typeSignature, length, start, end);
                        fragments.add(arguments);
                        identCount = 1;
                        nameStarted = false;
                        break label189;
                     }
                     break;
                  case 'B':
                     if (!nameStarted) {
                        ++this.namePos;
                        if (dim == 0) {
                           return new SingleTypeReference(TypeBinding.BYTE.simpleName, ((long)start << 32) + (long)end);
                        }

                        return new ArrayTypeReference(TypeBinding.BYTE.simpleName, dim, ((long)start << 32) + (long)end);
                     }
                     break label189;
                  case 'C':
                     if (!nameStarted) {
                        ++this.namePos;
                        if (dim == 0) {
                           return new SingleTypeReference(TypeBinding.CHAR.simpleName, ((long)start << 32) + (long)end);
                        }

                        return new ArrayTypeReference(TypeBinding.CHAR.simpleName, dim, ((long)start << 32) + (long)end);
                     }
                     break label189;
                  case 'D':
                     if (!nameStarted) {
                        ++this.namePos;
                        if (dim == 0) {
                           return new SingleTypeReference(TypeBinding.DOUBLE.simpleName, ((long)start << 32) + (long)end);
                        }

                        return new ArrayTypeReference(TypeBinding.DOUBLE.simpleName, dim, ((long)start << 32) + (long)end);
                     }
                     break label189;
                  case 'F':
                     if (!nameStarted) {
                        ++this.namePos;
                        if (dim == 0) {
                           return new SingleTypeReference(TypeBinding.FLOAT.simpleName, ((long)start << 32) + (long)end);
                        }

                        return new ArrayTypeReference(TypeBinding.FLOAT.simpleName, dim, ((long)start << 32) + (long)end);
                     }
                     break label189;
                  case 'I':
                     if (!nameStarted) {
                        ++this.namePos;
                        if (dim == 0) {
                           return new SingleTypeReference(TypeBinding.INT.simpleName, ((long)start << 32) + (long)end);
                        }

                        return new ArrayTypeReference(TypeBinding.INT.simpleName, dim, ((long)start << 32) + (long)end);
                     }
                     break label189;
                  case 'J':
                     if (!nameStarted) {
                        ++this.namePos;
                        if (dim == 0) {
                           return new SingleTypeReference(TypeBinding.LONG.simpleName, ((long)start << 32) + (long)end);
                        }

                        return new ArrayTypeReference(TypeBinding.LONG.simpleName, dim, ((long)start << 32) + (long)end);
                     }
                     break label189;
                  case 'L':
                  case 'Q':
                  case 'T':
                     if (!nameStarted) {
                        nameFragmentStart = this.namePos + 1;
                        nameStarted = true;
                     }
                     break label189;
                  case 'S':
                     if (!nameStarted) {
                        ++this.namePos;
                        if (dim == 0) {
                           return new SingleTypeReference(TypeBinding.SHORT.simpleName, ((long)start << 32) + (long)end);
                        }

                        return new ArrayTypeReference(TypeBinding.SHORT.simpleName, dim, ((long)start << 32) + (long)end);
                     }
                     break label189;
                  case 'V':
                     if (!nameStarted) {
                        ++this.namePos;
                        return new SingleTypeReference(TypeBinding.VOID.simpleName, ((long)start << 32) + (long)end);
                     }
                     break label189;
                  case 'Z':
                     if (!nameStarted) {
                        ++this.namePos;
                        if (dim == 0) {
                           return new SingleTypeReference(TypeBinding.BOOLEAN.simpleName, ((long)start << 32) + (long)end);
                        }

                        return new ArrayTypeReference(TypeBinding.BOOLEAN.simpleName, dim, ((long)start << 32) + (long)end);
                     }
                     break label189;
                  case '[':
                     ++dim;
                  default:
                     break label189;
               }
            }

            if (fragments == null) {
               if (identCount == 1) {
                  if (dim == 0) {
                     char[] nameFragment = new char[nameFragmentEnd - nameFragmentStart + 1];
                     typeSignature.getChars(nameFragmentStart, nameFragmentEnd + 1, nameFragment, 0);
                     return new SingleTypeReference(nameFragment, ((long)start << 32) + (long)end);
                  }

                  char[] nameFragment = new char[nameFragmentEnd - nameFragmentStart + 1];
                  typeSignature.getChars(nameFragmentStart, nameFragmentEnd + 1, nameFragment, 0);
                  return new ArrayTypeReference(nameFragment, dim, ((long)start << 32) + (long)end);
               }

               long[] positions = new long[identCount];
               long pos = ((long)start << 32) + (long)end;

               for(int i = 0; i < identCount; ++i) {
                  positions[i] = pos;
               }

               char[][] identifiers = this.extractIdentifiers(typeSignature, nameFragmentStart, nameFragmentEnd, identCount);
               if (dim == 0) {
                  return new QualifiedTypeReference(identifiers, positions);
               }

               return new ArrayQualifiedTypeReference(identifiers, dim, positions);
            }

            if (nameStarted) {
               this.addIdentifiers(typeSignature, nameFragmentStart, nameFragmentEnd + 1, identCount, fragments);
            }

            int fragmentLength = fragments.size();
            if (fragmentLength == 2) {
               Object firstFragment = fragments.get(0);
               if (firstFragment instanceof char[]) {
                  return new ParameterizedSingleTypeReference((char[])firstFragment, (TypeReference[])fragments.get(1), dim, ((long)start << 32) + (long)end);
               }
            }

            identCount = 0;

            for(int i = 0; i < fragmentLength; ++i) {
               Object element = fragments.get(i);
               if (element instanceof char[][]) {
                  identCount += ((char[][])element).length;
               } else if (element instanceof char[]) {
                  ++identCount;
               }
            }

            char[][] tokens = new char[identCount][];
            TypeReference[][] arguments = new TypeReference[identCount][];
            int index = 0;

            for(int i = 0; i < fragmentLength; ++i) {
               Object element = fragments.get(i);
               if (element instanceof char[][]) {
                  char[][] fragmentTokens = (char[][])element;
                  int fragmentTokenLength = fragmentTokens.length;
                  System.arraycopy(fragmentTokens, 0, tokens, index, fragmentTokenLength);
                  index += fragmentTokenLength;
               } else if (element instanceof char[]) {
                  tokens[index++] = (char[])element;
               } else {
                  arguments[index - 1] = (TypeReference[])element;
               }
            }

            long[] positions = new long[identCount];
            long pos = ((long)start << 32) + (long)end;

            for(int i = 0; i < identCount; ++i) {
               positions[i] = pos;
            }

            return new ParameterizedQualifiedTypeReference(tokens, arguments, dim, positions);
         }

         ++this.namePos;
      }
   }

   private TypeReference decodeType2(char[] typeName, int length, int start, int end, boolean includeGenericsAnyway) {
      int identCount = 1;
      int dim = 0;
      int nameFragmentStart = this.namePos;
      int nameFragmentEnd = -1;
      ArrayList fragments = null;

      while(true) {
         label94: {
            if (this.namePos < length) {
               char currentChar = typeName[this.namePos];
               switch(currentChar) {
                  case ',':
                  case '>':
                     break;
                  case '.':
                     if (nameFragmentStart < 0) {
                        nameFragmentStart = this.namePos + 1;
                     }

                     ++identCount;
                     break label94;
                  case '<':
                     if ((this.has1_5Compliance || includeGenericsAnyway) && fragments == null) {
                        fragments = new ArrayList(2);
                     }

                     nameFragmentEnd = this.namePos - 1;
                     if (this.has1_5Compliance || includeGenericsAnyway) {
                        char[][] identifiers = CharOperation.splitOn('.', typeName, nameFragmentStart, this.namePos);
                        fragments.add(identifiers);
                     }

                     ++this.namePos;
                     TypeReference[] arguments = this.decodeTypeArguments(typeName, length, start, end, includeGenericsAnyway);
                     if (this.has1_5Compliance || includeGenericsAnyway) {
                        fragments.add(arguments);
                        identCount = 0;
                        nameFragmentStart = -1;
                        nameFragmentEnd = -1;
                     }
                     break label94;
                  case '?':
                     ++this.namePos;

                     while(typeName[this.namePos] == ' ') {
                        ++this.namePos;
                     }

                     label65:
                     switch(typeName[this.namePos]) {
                        case 'e':
                           int max = TypeConstants.WILDCARD_EXTENDS.length - 1;

                           for(int ahead = 1; ahead < max; ++ahead) {
                              if (typeName[this.namePos + ahead] != TypeConstants.WILDCARD_EXTENDS[ahead + 1]) {
                                 break label65;
                              }
                           }

                           this.namePos += max;
                           Wildcard result = new Wildcard(1);
                           result.bound = this.decodeType2(typeName, length, start, end, includeGenericsAnyway);
                           result.sourceStart = start;
                           result.sourceEnd = end;
                           return result;
                        case 's':
                           int max = TypeConstants.WILDCARD_SUPER.length - 1;
                           int ahead = 1;

                           while(true) {
                              if (ahead >= max) {
                                 this.namePos += max;
                                 Wildcard result = new Wildcard(2);
                                 result.bound = this.decodeType2(typeName, length, start, end, includeGenericsAnyway);
                                 result.sourceStart = start;
                                 result.sourceEnd = end;
                                 return result;
                              }

                              if (typeName[this.namePos + ahead] != TypeConstants.WILDCARD_SUPER[ahead + 1]) {
                                 break;
                              }

                              ++ahead;
                           }
                     }

                     Wildcard result = new Wildcard(0);
                     result.sourceStart = start;
                     result.sourceEnd = end;
                     return result;
                  case '[':
                     if (dim == 0 && nameFragmentEnd < 0) {
                        nameFragmentEnd = this.namePos - 1;
                     }

                     ++dim;
                  case ']':
                  default:
                     break label94;
               }
            }

            return this.decodeType3(typeName, length, start, end, identCount, dim, nameFragmentStart, nameFragmentEnd, fragments);
         }

         ++this.namePos;
      }
   }

   private TypeReference decodeType3(
      char[] typeName, int length, int start, int end, int identCount, int dim, int nameFragmentStart, int nameFragmentEnd, ArrayList fragments
   ) {
      if (nameFragmentEnd < 0) {
         nameFragmentEnd = this.namePos - 1;
      }

      if (fragments == null) {
         if (identCount == 1) {
            if (dim != 0) {
               int nameFragmentLength = nameFragmentEnd - nameFragmentStart + 1;
               char[] nameFragment = new char[nameFragmentLength];
               System.arraycopy(typeName, nameFragmentStart, nameFragment, 0, nameFragmentLength);
               return new ArrayTypeReference(nameFragment, dim, ((long)start << 32) + (long)end);
            } else {
               char[] nameFragment;
               if (nameFragmentStart == 0 && nameFragmentEnd < 0) {
                  nameFragment = typeName;
               } else {
                  int nameFragmentLength = nameFragmentEnd - nameFragmentStart + 1;
                  System.arraycopy(typeName, nameFragmentStart, nameFragment = new char[nameFragmentLength], 0, nameFragmentLength);
               }

               return new SingleTypeReference(nameFragment, ((long)start << 32) + (long)end);
            }
         } else {
            long[] positions = new long[identCount];
            long pos = ((long)start << 32) + (long)end;

            for(int i = 0; i < identCount; ++i) {
               positions[i] = pos;
            }

            char[][] identifiers = CharOperation.splitOn('.', typeName, nameFragmentStart, nameFragmentEnd + 1);
            return (TypeReference)(dim == 0
               ? new QualifiedTypeReference(identifiers, positions)
               : new ArrayQualifiedTypeReference(identifiers, dim, positions));
         }
      } else {
         if (nameFragmentStart > 0 && nameFragmentStart < length) {
            char[][] identifiers = CharOperation.splitOn('.', typeName, nameFragmentStart, nameFragmentEnd + 1);
            fragments.add(identifiers);
         }

         int fragmentLength = fragments.size();
         if (fragmentLength == 2) {
            char[][] firstFragment = (char[][])fragments.get(0);
            if (firstFragment.length == 1) {
               return new ParameterizedSingleTypeReference(firstFragment[0], (TypeReference[])fragments.get(1), dim, ((long)start << 32) + (long)end);
            }
         }

         identCount = 0;

         for(int i = 0; i < fragmentLength; ++i) {
            Object element = fragments.get(i);
            if (element instanceof char[][]) {
               identCount += ((char[][])element).length;
            }
         }

         char[][] tokens = new char[identCount][];
         TypeReference[][] arguments = new TypeReference[identCount][];
         int index = 0;

         for(int i = 0; i < fragmentLength; ++i) {
            Object element = fragments.get(i);
            if (element instanceof char[][]) {
               char[][] fragmentTokens = (char[][])element;
               int fragmentTokenLength = fragmentTokens.length;
               System.arraycopy(fragmentTokens, 0, tokens, index, fragmentTokenLength);
               index += fragmentTokenLength;
            } else {
               arguments[index - 1] = (TypeReference[])element;
            }
         }

         long[] positions = new long[identCount];
         long pos = ((long)start << 32) + (long)end;

         for(int i = 0; i < identCount; ++i) {
            positions[i] = pos;
         }

         return new ParameterizedQualifiedTypeReference(tokens, arguments, dim, positions);
      }
   }

   private TypeReference[] decodeTypeArguments(char[] typeName, int length, int start, int end, boolean includeGenericsAnyway) {
      ArrayList argumentList = new ArrayList(1);

      int count;
      for(count = 0; this.namePos < length; ++this.namePos) {
         TypeReference argument = this.decodeType2(typeName, length, start, end, includeGenericsAnyway);
         ++count;
         argumentList.add(argument);
         if (this.namePos >= length || typeName[this.namePos] == '>') {
            break;
         }
      }

      TypeReference[] typeArguments = new TypeReference[count];
      argumentList.toArray(typeArguments);
      return typeArguments;
   }

   private TypeReference[] decodeTypeArguments(String typeSignature, int length, int start, int end) {
      ArrayList argumentList = new ArrayList(1);
      int count = 0;

      while(this.namePos < length) {
         TypeReference argument = this.decodeType(typeSignature, length, start, end);
         ++count;
         argumentList.add(argument);
         if (this.namePos >= length || typeSignature.charAt(this.namePos) == '>') {
            break;
         }
      }

      TypeReference[] typeArguments = new TypeReference[count];
      argumentList.toArray(typeArguments);
      return typeArguments;
   }

   private char[][] extractIdentifiers(String typeSignature, int start, int endInclusive, int identCount) {
      char[][] result = new char[identCount][];
      int charIndex = start;
      int i = 0;

      while(charIndex < endInclusive) {
         char currentChar;
         if ((currentChar = typeSignature.charAt(charIndex)) != this.memberTypeSeparator && currentChar != '.') {
            ++charIndex;
         } else {
            typeSignature.getChars(start, charIndex, result[i++] = new char[charIndex - start], 0);
            start = ++charIndex;
         }
      }

      typeSignature.getChars(start, charIndex + 1, result[i++] = new char[charIndex - start + 1], 0);
      return result;
   }
}
