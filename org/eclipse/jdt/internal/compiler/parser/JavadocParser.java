package org.eclipse.jdt.internal.compiler.parser;

import java.util.List;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocArgumentExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocArraySingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocImplicitTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;
import org.eclipse.jdt.internal.compiler.ast.JavadocQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.util.Util;

public class JavadocParser extends AbstractCommentParser {
   public Javadoc docComment;
   private int invalidParamReferencesPtr = -1;
   private ASTNode[] invalidParamReferencesStack;
   private long validValuePositions;
   private long invalidValuePositions;
   public boolean shouldReportProblems = true;
   private int tagWaitingForDescription;

   public JavadocParser(Parser sourceParser) {
      super(sourceParser);
      this.kind = 513;
      if (sourceParser != null && sourceParser.options != null) {
         this.setJavadocPositions = sourceParser.options.processAnnotations;
      }
   }

   public boolean checkDeprecation(int commentPtr) {
      this.javadocStart = this.sourceParser.scanner.commentStarts[commentPtr];
      this.javadocEnd = this.sourceParser.scanner.commentStops[commentPtr] - 1;
      this.firstTagPosition = this.sourceParser.scanner.commentTagStarts[commentPtr];
      this.validValuePositions = -1L;
      this.invalidValuePositions = -1L;
      this.tagWaitingForDescription = 0;
      if (this.checkDocComment) {
         this.docComment = new Javadoc(this.javadocStart, this.javadocEnd);
      } else if (this.setJavadocPositions) {
         this.docComment = new Javadoc(this.javadocStart, this.javadocEnd);
         this.docComment.bits &= -65537;
      } else {
         this.docComment = null;
      }

      if (this.firstTagPosition == 0) {
         switch(this.kind & 0xFF) {
            case 1:
            case 16:
               return false;
         }
      }

      boolean var9;
      try {
         this.source = this.sourceParser.scanner.source;
         this.scanner.setSource(this.source);
         if (this.checkDocComment) {
            this.scanner.lineEnds = this.sourceParser.scanner.lineEnds;
            this.scanner.linePtr = this.sourceParser.scanner.linePtr;
            this.lineEnds = this.scanner.lineEnds;
            this.commentParse();
            return this.deprecated;
         }

         Scanner sourceScanner = this.sourceParser.scanner;
         int firstLineNumber = Util.getLineNumber(this.javadocStart, sourceScanner.lineEnds, 0, sourceScanner.linePtr);
         int lastLineNumber = Util.getLineNumber(this.javadocEnd, sourceScanner.lineEnds, 0, sourceScanner.linePtr);
         this.index = this.javadocStart + 3;
         this.deprecated = false;

         label101:
         for(int line = firstLineNumber; line <= lastLineNumber; ++line) {
            int lineStart = line == firstLineNumber ? this.javadocStart + 3 : this.sourceParser.scanner.getLineStart(line);
            this.index = lineStart;
            this.lineEnd = line == lastLineNumber ? this.javadocEnd - 2 : this.sourceParser.scanner.getLineEnd(line);

            while(this.index < this.lineEnd) {
               char c = this.readChar();
               switch(c) {
                  case '\t':
                  case '\n':
                  case '\f':
                  case '\r':
                  case ' ':
                  case '*':
                     break;
                  case '@':
                     this.parseSimpleTag();
                     if (this.tagValue == 1 && this.abort) {
                     }
                  default:
                     continue label101;
               }
            }
         }

         var9 = this.deprecated;
      } finally {
         this.source = null;
         this.scanner.setSource(null);
      }

      return var9;
   }

   @Override
   protected Object createArgumentReference(char[] name, int dim, boolean isVarargs, Object typeRef, long[] dimPositions, long argNamePos) throws InvalidInputException {
      try {
         TypeReference argTypeRef = (TypeReference)typeRef;
         if (dim > 0) {
            long pos = ((long)argTypeRef.sourceStart << 32) + (long)argTypeRef.sourceEnd;
            if (typeRef instanceof JavadocSingleTypeReference) {
               JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference)typeRef;
               argTypeRef = new JavadocArraySingleTypeReference(singleRef.token, dim, pos);
            } else {
               JavadocQualifiedTypeReference qualifRef = (JavadocQualifiedTypeReference)typeRef;
               argTypeRef = new JavadocArrayQualifiedTypeReference(qualifRef, dim);
            }
         }

         int argEnd = argTypeRef.sourceEnd;
         if (dim > 0) {
            argEnd = (int)dimPositions[dim - 1];
            if (isVarargs) {
               argTypeRef.bits |= 16384;
            }
         }

         if (argNamePos >= 0L) {
            argEnd = (int)argNamePos;
         }

         return new JavadocArgumentExpression(name, argTypeRef.sourceStart, argEnd, argTypeRef);
      } catch (ClassCastException var12) {
         throw new InvalidInputException();
      }
   }

   @Override
   protected Object createFieldReference(Object receiver) throws InvalidInputException {
      try {
         TypeReference typeRef = (TypeReference)receiver;
         if (typeRef == null) {
            char[] name = this.sourceParser.compilationUnit.getMainTypeName();
            typeRef = new JavadocImplicitTypeReference(name, this.memberStart);
         }

         JavadocFieldReference field = new JavadocFieldReference(this.identifierStack[0], this.identifierPositionStack[0]);
         field.receiver = typeRef;
         field.tagSourceStart = this.tagSourceStart;
         field.tagSourceEnd = this.tagSourceEnd;
         field.tagValue = this.tagValue;
         return field;
      } catch (ClassCastException var4) {
         throw new InvalidInputException();
      }
   }

   @Override
   protected Object createMethodReference(Object receiver, List arguments) throws InvalidInputException {
      try {
         TypeReference typeRef = (TypeReference)receiver;
         boolean isConstructor = false;
         int length = this.identifierLengthStack[0];
         if (typeRef == null) {
            char[] name = this.sourceParser.compilationUnit.getMainTypeName();
            TypeDeclaration typeDecl = this.getParsedTypeDeclaration();
            if (typeDecl != null) {
               name = typeDecl.name;
            }

            isConstructor = CharOperation.equals(this.identifierStack[length - 1], name);
            typeRef = new JavadocImplicitTypeReference(name, this.memberStart);
         } else if (typeRef instanceof JavadocSingleTypeReference) {
            char[] name = ((JavadocSingleTypeReference)typeRef).token;
            isConstructor = CharOperation.equals(this.identifierStack[length - 1], name);
         } else {
            if (!(typeRef instanceof JavadocQualifiedTypeReference)) {
               throw new InvalidInputException();
            }

            char[][] tokens = ((JavadocQualifiedTypeReference)typeRef).tokens;
            int last = tokens.length - 1;
            isConstructor = CharOperation.equals(this.identifierStack[length - 1], tokens[last]);
            if (isConstructor) {
               boolean valid = true;
               if (valid) {
                  for(int i = 0; i < length - 1 && valid; ++i) {
                     valid = CharOperation.equals(this.identifierStack[i], tokens[i]);
                  }
               }

               if (!valid) {
                  if (this.reportProblems) {
                     this.sourceParser
                        .problemReporter()
                        .javadocInvalidMemberTypeQualification(
                           (int)(this.identifierPositionStack[0] >>> 32), (int)this.identifierPositionStack[length - 1], -1
                        );
                  }

                  return null;
               }
            }
         }

         if (arguments == null) {
            if (isConstructor) {
               JavadocAllocationExpression allocation = new JavadocAllocationExpression(this.identifierPositionStack[length - 1]);
               allocation.type = typeRef;
               allocation.tagValue = this.tagValue;
               allocation.sourceEnd = this.scanner.getCurrentTokenEndPosition();
               if (length == 1) {
                  allocation.qualification = new char[][]{this.identifierStack[0]};
               } else {
                  System.arraycopy(this.identifierStack, 0, allocation.qualification = new char[length][], 0, length);
                  allocation.sourceStart = (int)(this.identifierPositionStack[0] >>> 32);
               }

               allocation.memberStart = this.memberStart;
               return allocation;
            } else {
               JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[length - 1], this.identifierPositionStack[length - 1]);
               msg.receiver = typeRef;
               msg.tagValue = this.tagValue;
               msg.sourceEnd = this.scanner.getCurrentTokenEndPosition();
               return msg;
            }
         } else {
            JavadocArgumentExpression[] expressions = new JavadocArgumentExpression[arguments.size()];
            arguments.toArray(expressions);
            if (isConstructor) {
               JavadocAllocationExpression allocation = new JavadocAllocationExpression(this.identifierPositionStack[length - 1]);
               allocation.arguments = expressions;
               allocation.type = typeRef;
               allocation.tagValue = this.tagValue;
               allocation.sourceEnd = this.scanner.getCurrentTokenEndPosition();
               if (length == 1) {
                  allocation.qualification = new char[][]{this.identifierStack[0]};
               } else {
                  System.arraycopy(this.identifierStack, 0, allocation.qualification = new char[length][], 0, length);
                  allocation.sourceStart = (int)(this.identifierPositionStack[0] >>> 32);
               }

               allocation.memberStart = this.memberStart;
               return allocation;
            } else {
               JavadocMessageSend msg = new JavadocMessageSend(this.identifierStack[length - 1], this.identifierPositionStack[length - 1], expressions);
               msg.receiver = typeRef;
               msg.tagValue = this.tagValue;
               msg.sourceEnd = this.scanner.getCurrentTokenEndPosition();
               return msg;
            }
         }
      } catch (ClassCastException var10) {
         throw new InvalidInputException();
      }
   }

   @Override
   protected Object createReturnStatement() {
      return new JavadocReturnStatement(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
   }

   @Override
   protected void createTag() {
      this.tagValue = 100;
   }

   @Override
   protected Object createTypeReference(int primitiveToken) {
      TypeReference typeRef = null;
      int size = this.identifierLengthStack[this.identifierLengthPtr];
      if (size == 1) {
         typeRef = new JavadocSingleTypeReference(
            this.identifierStack[this.identifierPtr], this.identifierPositionStack[this.identifierPtr], this.tagSourceStart, this.tagSourceEnd
         );
      } else if (size > 1) {
         char[][] tokens = new char[size][];
         System.arraycopy(this.identifierStack, this.identifierPtr - size + 1, tokens, 0, size);
         long[] positions = new long[size];
         System.arraycopy(this.identifierPositionStack, this.identifierPtr - size + 1, positions, 0, size);
         typeRef = new JavadocQualifiedTypeReference(tokens, positions, this.tagSourceStart, this.tagSourceEnd);
      }

      return typeRef;
   }

   protected TypeDeclaration getParsedTypeDeclaration() {
      for(int ptr = this.sourceParser.astPtr; ptr >= 0; --ptr) {
         Object node = this.sourceParser.astStack[ptr];
         if (node instanceof TypeDeclaration) {
            TypeDeclaration typeDecl = (TypeDeclaration)node;
            if (typeDecl.bodyEnd == 0) {
               return typeDecl;
            }
         }
      }

      return null;
   }

   @Override
   protected boolean parseThrows() {
      boolean valid = super.parseThrows();
      this.tagWaitingForDescription = valid && this.reportProblems ? 4 : 0;
      return valid;
   }

   protected boolean parseReturn() {
      if (this.returnStatement == null) {
         this.returnStatement = this.createReturnStatement();
         return true;
      } else {
         if (this.reportProblems) {
            this.sourceParser
               .problemReporter()
               .javadocDuplicatedReturnTag(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
         }

         return false;
      }
   }

   protected void parseSimpleTag() {
      char first = this.source[this.index++];
      if (first == '\\' && this.source[this.index] == 'u') {
         int pos = this.index++;

         while(this.source[this.index] == 'u') {
            ++this.index;
         }

         int c1;
         int c2;
         int c3;
         int c4;
         if ((c1 = ScannerHelper.getHexadecimalValue(this.source[this.index++])) <= 15
            && c1 >= 0
            && (c2 = ScannerHelper.getHexadecimalValue(this.source[this.index++])) <= 15
            && c2 >= 0
            && (c3 = ScannerHelper.getHexadecimalValue(this.source[this.index++])) <= 15
            && c3 >= 0
            && (c4 = ScannerHelper.getHexadecimalValue(this.source[this.index++])) <= 15
            && c4 >= 0) {
            first = (char)(((c1 * 16 + c2) * 16 + c3) * 16 + c4);
         } else {
            this.index = pos;
         }
      }

      switch(first) {
         case 'd':
            if (this.readChar() == 'e'
               && this.readChar() == 'p'
               && this.readChar() == 'r'
               && this.readChar() == 'e'
               && this.readChar() == 'c'
               && this.readChar() == 'a'
               && this.readChar() == 't'
               && this.readChar() == 'e'
               && this.readChar() == 'd') {
               char c = this.readChar();
               if (ScannerHelper.isWhitespace(c) || c == '*') {
                  this.abort = true;
                  this.deprecated = true;
                  this.tagValue = 1;
               }
            }
      }
   }

   @Override
   protected boolean parseTag(int previousPosition) throws InvalidInputException {
      switch(this.tagWaitingForDescription) {
         case 0:
            break;
         case 1:
         case 3:
         default:
            if (!this.inlineTagStarted) {
               this.sourceParser
                  .problemReporter()
                  .javadocMissingTagDescription(TAG_NAMES[this.tagWaitingForDescription], this.tagSourceStart, this.tagSourceEnd, this.sourceParser.modifiers);
            }
            break;
         case 2:
         case 4:
            if (!this.inlineTagStarted) {
               int start = (int)(this.identifierPositionStack[0] >>> 32);
               int end = (int)this.identifierPositionStack[this.identifierPtr];
               this.sourceParser.problemReporter().javadocMissingTagDescriptionAfterReference(start, end, this.sourceParser.modifiers);
            }
      }

      this.tagWaitingForDescription = 0;
      this.tagSourceStart = this.index;
      this.tagSourceEnd = previousPosition;
      this.scanner.startPosition = this.index;
      int currentPosition = this.index;
      char firstChar = this.readChar();
      switch(firstChar) {
         case ' ':
         case '#':
         case '*':
         case '}':
            if (this.reportProblems) {
               this.sourceParser.problemReporter().javadocInvalidTag(previousPosition, currentPosition);
            }

            if (this.textStart == -1) {
               this.textStart = currentPosition;
            }

            this.scanner.currentCharacter = firstChar;
            return false;
         default:
            if (ScannerHelper.isWhitespace(firstChar)) {
               if (this.reportProblems) {
                  this.sourceParser.problemReporter().javadocInvalidTag(previousPosition, currentPosition);
               }

               if (this.textStart == -1) {
                  this.textStart = currentPosition;
               }

               this.scanner.currentCharacter = firstChar;
               return false;
            } else {
               char[] tagName = new char[32];
               int length = 0;
               char currentChar = firstChar;
               int tagNameLength = tagName.length;
               boolean validTag = true;

               label268:
               while(true) {
                  if (length == tagNameLength) {
                     System.arraycopy(tagName, 0, tagName = new char[tagNameLength + 32], 0, tagNameLength);
                     tagNameLength = tagName.length;
                  }

                  tagName[length++] = currentChar;
                  currentPosition = this.index;
                  currentChar = this.readChar();
                  switch(currentChar) {
                     case ' ':
                     case '*':
                     case '}':
                        break label268;
                     case '#':
                        validTag = false;
                        break;
                     default:
                        if (ScannerHelper.isWhitespace(currentChar)) {
                           break label268;
                        }
                  }
               }

               this.tagSourceEnd = currentPosition - 1;
               this.scanner.currentCharacter = currentChar;
               this.scanner.currentPosition = currentPosition;
               this.index = this.tagSourceEnd + 1;
               if (!validTag) {
                  if (this.reportProblems) {
                     this.sourceParser.problemReporter().javadocInvalidTag(this.tagSourceStart, this.tagSourceEnd);
                  }

                  if (this.textStart == -1) {
                     this.textStart = this.index;
                  }

                  this.scanner.currentCharacter = currentChar;
                  return false;
               } else {
                  this.tagValue = 100;
                  boolean valid = false;
                  switch(firstChar) {
                     case 'a':
                        if (length == TAG_AUTHOR_LENGTH && CharOperation.equals(TAG_AUTHOR, tagName, 0, length)) {
                           this.tagValue = 12;
                           this.tagWaitingForDescription = this.tagValue;
                        }
                        break;
                     case 'b':
                     case 'f':
                     case 'g':
                     case 'h':
                     case 'j':
                     case 'k':
                     case 'm':
                     case 'n':
                     case 'o':
                     case 'q':
                     case 'u':
                     default:
                        this.createTag();
                        break;
                     case 'c':
                        if (length == TAG_CATEGORY_LENGTH && CharOperation.equals(TAG_CATEGORY, tagName, 0, length)) {
                           this.tagValue = 11;
                           if (!this.inlineTagStarted) {
                              valid = this.parseIdentifierTag(false);
                           }
                        } else if (length == TAG_CODE_LENGTH && this.inlineTagStarted && CharOperation.equals(TAG_CODE, tagName, 0, length)) {
                           this.tagValue = 18;
                           this.tagWaitingForDescription = this.tagValue;
                        }
                        break;
                     case 'd':
                        if (length == TAG_DEPRECATED_LENGTH && CharOperation.equals(TAG_DEPRECATED, tagName, 0, length)) {
                           this.deprecated = true;
                           valid = true;
                           this.tagValue = 1;
                           this.tagWaitingForDescription = this.tagValue;
                        } else if (length == TAG_DOC_ROOT_LENGTH && CharOperation.equals(TAG_DOC_ROOT, tagName, 0, length)) {
                           valid = true;
                           this.tagValue = 20;
                        }
                        break;
                     case 'e':
                        if (length == TAG_EXCEPTION_LENGTH && CharOperation.equals(TAG_EXCEPTION, tagName, 0, length)) {
                           this.tagValue = 5;
                           if (!this.inlineTagStarted) {
                              valid = this.parseThrows();
                           }
                        }
                        break;
                     case 'i':
                        if (length == TAG_INHERITDOC_LENGTH && CharOperation.equals(TAG_INHERITDOC, tagName, 0, length)) {
                           switch(this.lastBlockTagValue) {
                              case 0:
                              case 2:
                              case 3:
                              case 4:
                              case 5:
                                 valid = true;
                                 if (this.reportProblems) {
                                    this.recordInheritedPosition(((long)this.tagSourceStart << 32) + (long)this.tagSourceEnd);
                                 }

                                 if (this.inlineTagStarted) {
                                    this.parseInheritDocTag();
                                 }
                                 break;
                              case 1:
                              default:
                                 valid = false;
                                 if (this.reportProblems) {
                                    this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
                                 }
                           }

                           this.tagValue = 9;
                        }
                        break;
                     case 'l':
                        if (length == TAG_LINK_LENGTH && CharOperation.equals(TAG_LINK, tagName, 0, length)) {
                           this.tagValue = 7;
                           if (this.inlineTagStarted || (this.kind & 8) != 0) {
                              valid = this.parseReference();
                           }
                        } else if (length == TAG_LINKPLAIN_LENGTH && CharOperation.equals(TAG_LINKPLAIN, tagName, 0, length)) {
                           this.tagValue = 8;
                           if (this.inlineTagStarted) {
                              valid = this.parseReference();
                           }
                        } else if (length == TAG_LITERAL_LENGTH && this.inlineTagStarted && CharOperation.equals(TAG_LITERAL, tagName, 0, length)) {
                           this.tagValue = 19;
                           this.tagWaitingForDescription = this.tagValue;
                        }
                        break;
                     case 'p':
                        if (length == TAG_PARAM_LENGTH && CharOperation.equals(TAG_PARAM, tagName, 0, length)) {
                           this.tagValue = 2;
                           if (!this.inlineTagStarted) {
                              valid = this.parseParam();
                           }
                        }
                        break;
                     case 'r':
                        if (length == TAG_RETURN_LENGTH && CharOperation.equals(TAG_RETURN, tagName, 0, length)) {
                           this.tagValue = 3;
                           if (!this.inlineTagStarted) {
                              valid = this.parseReturn();
                           }
                        }
                        break;
                     case 's':
                        if (length == TAG_SEE_LENGTH && CharOperation.equals(TAG_SEE, tagName, 0, length)) {
                           this.tagValue = 6;
                           if (!this.inlineTagStarted) {
                              valid = this.parseReference();
                           }
                        } else if (length == TAG_SERIAL_LENGTH && CharOperation.equals(TAG_SERIAL, tagName, 0, length)) {
                           this.tagValue = 13;
                           this.tagWaitingForDescription = this.tagValue;
                        } else if (length == TAG_SERIAL_DATA_LENGTH && CharOperation.equals(TAG_SERIAL_DATA, tagName, 0, length)) {
                           this.tagValue = 14;
                           this.tagWaitingForDescription = this.tagValue;
                        } else if (length == TAG_SERIAL_FIELD_LENGTH && CharOperation.equals(TAG_SERIAL_FIELD, tagName, 0, length)) {
                           this.tagValue = 15;
                           this.tagWaitingForDescription = this.tagValue;
                        } else if (length == TAG_SINCE_LENGTH && CharOperation.equals(TAG_SINCE, tagName, 0, length)) {
                           this.tagValue = 16;
                           this.tagWaitingForDescription = this.tagValue;
                        }
                        break;
                     case 't':
                        if (length == TAG_THROWS_LENGTH && CharOperation.equals(TAG_THROWS, tagName, 0, length)) {
                           this.tagValue = 4;
                           if (!this.inlineTagStarted) {
                              valid = this.parseThrows();
                           }
                        }
                        break;
                     case 'v':
                        if (length == TAG_VALUE_LENGTH && CharOperation.equals(TAG_VALUE, tagName, 0, length)) {
                           this.tagValue = 10;
                           if (this.sourceLevel >= 3211264L) {
                              if (this.inlineTagStarted) {
                                 valid = this.parseReference();
                              }
                           } else if (this.validValuePositions == -1L) {
                              if (this.invalidValuePositions != -1L && this.reportProblems) {
                                 this.sourceParser
                                    .problemReporter()
                                    .javadocUnexpectedTag((int)(this.invalidValuePositions >>> 32), (int)this.invalidValuePositions);
                              }

                              if (valid) {
                                 this.validValuePositions = ((long)this.tagSourceStart << 32) + (long)this.tagSourceEnd;
                                 this.invalidValuePositions = -1L;
                              } else {
                                 this.invalidValuePositions = ((long)this.tagSourceStart << 32) + (long)this.tagSourceEnd;
                              }
                           } else if (this.reportProblems) {
                              this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
                           }
                        } else if (length == TAG_VERSION_LENGTH && CharOperation.equals(TAG_VERSION, tagName, 0, length)) {
                           this.tagValue = 17;
                           this.tagWaitingForDescription = this.tagValue;
                        } else {
                           this.createTag();
                        }
                  }

                  this.textStart = this.index;
                  if (this.tagValue != 100) {
                     if (!this.inlineTagStarted) {
                        this.lastBlockTagValue = this.tagValue;
                     }

                     if (this.inlineTagStarted && JAVADOC_TAG_TYPE[this.tagValue] == 2 || !this.inlineTagStarted && JAVADOC_TAG_TYPE[this.tagValue] == 1) {
                        valid = false;
                        this.tagValue = 100;
                        this.tagWaitingForDescription = 0;
                        if (this.reportProblems) {
                           this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
                        }
                     }
                  }

                  return valid;
               }
            }
      }
   }

   protected void parseInheritDocTag() {
   }

   @Override
   protected boolean parseParam() throws InvalidInputException {
      boolean valid = super.parseParam();
      this.tagWaitingForDescription = valid && this.reportProblems ? 2 : 0;
      return valid;
   }

   @Override
   protected boolean pushParamName(boolean isTypeParam) {
      ASTNode nameRef = null;
      if (isTypeParam) {
         JavadocSingleTypeReference ref = new JavadocSingleTypeReference(
            this.identifierStack[1], this.identifierPositionStack[1], this.tagSourceStart, this.tagSourceEnd
         );
         nameRef = ref;
      } else {
         JavadocSingleNameReference ref = new JavadocSingleNameReference(
            this.identifierStack[0], this.identifierPositionStack[0], this.tagSourceStart, this.tagSourceEnd
         );
         nameRef = ref;
      }

      if (this.astLengthPtr == -1) {
         this.pushOnAstStack(nameRef, true);
      } else {
         if (!isTypeParam) {
            for(int i = 1; i <= this.astLengthPtr; i += 3) {
               if (this.astLengthStack[i] != 0) {
                  if (this.reportProblems) {
                     this.sourceParser.problemReporter().javadocUnexpectedTag(this.tagSourceStart, this.tagSourceEnd);
                  }

                  if ((long)this.invalidParamReferencesPtr == -1L) {
                     this.invalidParamReferencesStack = new JavadocSingleNameReference[10];
                  }

                  int stackLength = this.invalidParamReferencesStack.length;
                  if (++this.invalidParamReferencesPtr >= stackLength) {
                     System.arraycopy(
                        this.invalidParamReferencesStack,
                        0,
                        this.invalidParamReferencesStack = new JavadocSingleNameReference[stackLength + 10],
                        0,
                        stackLength
                     );
                  }

                  this.invalidParamReferencesStack[this.invalidParamReferencesPtr] = nameRef;
                  return false;
               }
            }
         }

         switch(this.astLengthPtr % 3) {
            case 0:
               this.pushOnAstStack(nameRef, false);
               break;
            case 1:
            default:
               return false;
            case 2:
               this.pushOnAstStack(nameRef, true);
         }
      }

      return true;
   }

   @Override
   protected boolean pushSeeRef(Object statement) {
      if (this.astLengthPtr == -1) {
         this.pushOnAstStack(null, true);
         this.pushOnAstStack(null, true);
         this.pushOnAstStack(statement, true);
      } else {
         switch(this.astLengthPtr % 3) {
            case 0:
               this.pushOnAstStack(null, true);
               this.pushOnAstStack(statement, true);
               break;
            case 1:
               this.pushOnAstStack(statement, true);
               break;
            case 2:
               this.pushOnAstStack(statement, false);
               break;
            default:
               return false;
         }
      }

      return true;
   }

   @Override
   protected void pushText(int start, int end) {
      this.tagWaitingForDescription = 0;
   }

   @Override
   protected boolean pushThrowName(Object typeRef) {
      if (this.astLengthPtr == -1) {
         this.pushOnAstStack(null, true);
         this.pushOnAstStack(typeRef, true);
      } else {
         switch(this.astLengthPtr % 3) {
            case 0:
               this.pushOnAstStack(typeRef, true);
               break;
            case 1:
               this.pushOnAstStack(typeRef, false);
               break;
            case 2:
               this.pushOnAstStack(null, true);
               this.pushOnAstStack(typeRef, true);
               break;
            default:
               return false;
         }
      }

      return true;
   }

   @Override
   protected void refreshInlineTagPosition(int previousPosition) {
      if (this.tagWaitingForDescription != 0) {
         this.sourceParser
            .problemReporter()
            .javadocMissingTagDescription(TAG_NAMES[this.tagWaitingForDescription], this.tagSourceStart, this.tagSourceEnd, this.sourceParser.modifiers);
         this.tagWaitingForDescription = 0;
      }
   }

   @Override
   protected void refreshReturnStatement() {
      ((JavadocReturnStatement)this.returnStatement).bits &= -262145;
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("check javadoc: ").append(this.checkDocComment).append("\n");
      buffer.append("javadoc: ").append(this.docComment).append("\n");
      buffer.append(super.toString());
      return buffer.toString();
   }

   @Override
   protected void updateDocComment() {
      switch(this.tagWaitingForDescription) {
         case 0:
            break;
         case 1:
         case 3:
         default:
            if (!this.inlineTagStarted) {
               this.sourceParser
                  .problemReporter()
                  .javadocMissingTagDescription(TAG_NAMES[this.tagWaitingForDescription], this.tagSourceStart, this.tagSourceEnd, this.sourceParser.modifiers);
            }
            break;
         case 2:
         case 4:
            if (!this.inlineTagStarted) {
               int start = (int)(this.identifierPositionStack[0] >>> 32);
               int end = (int)this.identifierPositionStack[this.identifierPtr];
               this.sourceParser.problemReporter().javadocMissingTagDescriptionAfterReference(start, end, this.sourceParser.modifiers);
            }
      }

      this.tagWaitingForDescription = 0;
      if (this.inheritedPositions != null && this.inheritedPositionsPtr != this.inheritedPositions.length) {
         System.arraycopy(this.inheritedPositions, 0, this.inheritedPositions = new long[this.inheritedPositionsPtr], 0, this.inheritedPositionsPtr);
      }

      this.docComment.inheritedPositions = this.inheritedPositions;
      this.docComment.valuePositions = this.validValuePositions != -1L ? this.validValuePositions : this.invalidValuePositions;
      if (this.returnStatement != null) {
         this.docComment.returnStatement = (JavadocReturnStatement)this.returnStatement;
      }

      if (this.invalidParamReferencesPtr >= 0) {
         this.docComment.invalidParameters = new JavadocSingleNameReference[this.invalidParamReferencesPtr + 1];
         System.arraycopy(this.invalidParamReferencesStack, 0, this.docComment.invalidParameters, 0, this.invalidParamReferencesPtr + 1);
      }

      if (this.astLengthPtr != -1) {
         int[] sizes = new int[3];

         for(int i = 0; i <= this.astLengthPtr; ++i) {
            sizes[i % 3] += this.astLengthStack[i];
         }

         this.docComment.seeReferences = new Expression[sizes[2]];
         this.docComment.exceptionReferences = new TypeReference[sizes[1]];
         int paramRefPtr = sizes[0];
         this.docComment.paramReferences = new JavadocSingleNameReference[paramRefPtr];
         int paramTypeParamPtr = sizes[0];
         this.docComment.paramTypeParameters = new JavadocSingleTypeReference[paramTypeParamPtr];

         while(this.astLengthPtr >= 0) {
            int ptr = this.astLengthPtr % 3;
            switch(ptr) {
               case 0:
                  int size = this.astLengthStack[this.astLengthPtr--];

                  for(int i = 0; i < size; ++i) {
                     Expression reference = (Expression)this.astStack[this.astPtr--];
                     if (reference instanceof JavadocSingleNameReference) {
                        --paramRefPtr;
                        this.docComment.paramReferences[paramRefPtr] = (JavadocSingleNameReference)reference;
                     } else if (reference instanceof JavadocSingleTypeReference) {
                        --paramTypeParamPtr;
                        this.docComment.paramTypeParameters[paramTypeParamPtr] = (JavadocSingleTypeReference)reference;
                     }
                  }
                  break;
               case 1:
                  int size = this.astLengthStack[this.astLengthPtr--];

                  for(int i = 0; i < size; ++i) {
                     this.docComment.exceptionReferences[--sizes[ptr]] = (TypeReference)this.astStack[this.astPtr--];
                  }
                  break;
               case 2:
                  int size = this.astLengthStack[this.astLengthPtr--];

                  for(int i = 0; i < size; ++i) {
                     this.docComment.seeReferences[--sizes[ptr]] = (Expression)this.astStack[this.astPtr--];
                  }
            }
         }

         if (paramRefPtr == 0) {
            this.docComment.paramTypeParameters = null;
         } else if (paramTypeParamPtr == 0) {
            this.docComment.paramReferences = null;
         } else {
            int size = sizes[0];
            System.arraycopy(
               this.docComment.paramReferences,
               paramRefPtr,
               this.docComment.paramReferences = new JavadocSingleNameReference[size - paramRefPtr],
               0,
               size - paramRefPtr
            );
            System.arraycopy(
               this.docComment.paramTypeParameters,
               paramTypeParamPtr,
               this.docComment.paramTypeParameters = new JavadocSingleTypeReference[size - paramTypeParamPtr],
               0,
               size - paramTypeParamPtr
            );
         }
      }
   }
}
