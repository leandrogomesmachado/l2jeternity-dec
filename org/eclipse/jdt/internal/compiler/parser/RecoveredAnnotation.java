package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;

public class RecoveredAnnotation extends RecoveredElement {
   public static final int MARKER = 0;
   public static final int NORMAL = 1;
   public static final int SINGLE_MEMBER = 2;
   private int kind;
   private int identifierPtr;
   private int identifierLengthPtr;
   private int sourceStart;
   public boolean hasPendingMemberValueName;
   public int memberValuPairEqualEnd = -1;
   public Annotation annotation;

   public RecoveredAnnotation(int identifierPtr, int identifierLengthPtr, int sourceStart, RecoveredElement parent, int bracketBalance) {
      super(parent, bracketBalance);
      this.kind = 0;
      this.identifierPtr = identifierPtr;
      this.identifierLengthPtr = identifierLengthPtr;
      this.sourceStart = sourceStart;
   }

   @Override
   public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalanceValue) {
      return (RecoveredElement)(this.annotation == null && (typeDeclaration.bits & 512) != 0 ? this : super.add(typeDeclaration, bracketBalanceValue));
   }

   @Override
   public RecoveredElement addAnnotationName(int identPtr, int identLengthPtr, int annotationStart, int bracketBalanceValue) {
      return new RecoveredAnnotation(identPtr, identLengthPtr, annotationStart, this, bracketBalanceValue);
   }

   public RecoveredElement addAnnotation(Annotation annot, int index) {
      this.annotation = annot;
      return (RecoveredElement)(this.parent != null ? this.parent : this);
   }

   @Override
   public void updateFromParserState() {
      Parser parser = this.parser();
      if (this.annotation == null && this.identifierPtr <= parser.identifierPtr) {
         Annotation annot = null;
         boolean needUpdateRParenPos = false;
         MemberValuePair pendingMemberValueName = null;
         if (this.hasPendingMemberValueName && this.identifierPtr < parser.identifierPtr) {
            char[] memberValueName = parser.identifierStack[this.identifierPtr + 1];
            long pos = parser.identifierPositionStack[this.identifierPtr + 1];
            int start = (int)(pos >>> 32);
            int end = (int)pos;
            int valueEnd = this.memberValuPairEqualEnd > -1 ? this.memberValuPairEqualEnd : end;
            SingleNameReference fakeExpression = new SingleNameReference(RecoveryScanner.FAKE_IDENTIFIER, ((long)valueEnd + 1L << 32) + (long)valueEnd);
            pendingMemberValueName = new MemberValuePair(memberValueName, start, end, fakeExpression);
         }

         parser.identifierPtr = this.identifierPtr;
         parser.identifierLengthPtr = this.identifierLengthPtr;
         TypeReference typeReference = parser.getAnnotationType();
         switch(this.kind) {
            case 1:
               if (parser.astPtr > -1 && parser.astStack[parser.astPtr] instanceof MemberValuePair) {
                  MemberValuePair[] memberValuePairs = null;
                  int argLength = parser.astLengthStack[parser.astLengthPtr];
                  int argStart = parser.astPtr - argLength + 1;
                  if (argLength > 0) {
                     int annotationEnd;
                     if (pendingMemberValueName != null) {
                        memberValuePairs = new MemberValuePair[argLength + 1];
                        System.arraycopy(parser.astStack, argStart, memberValuePairs, 0, argLength);
                        --parser.astLengthPtr;
                        parser.astPtr -= argLength;
                        memberValuePairs[argLength] = pendingMemberValueName;
                        annotationEnd = pendingMemberValueName.sourceEnd;
                     } else {
                        memberValuePairs = new MemberValuePair[argLength];
                        System.arraycopy(parser.astStack, argStart, memberValuePairs, 0, argLength);
                        --parser.astLengthPtr;
                        parser.astPtr -= argLength;
                        MemberValuePair lastMemberValuePair = memberValuePairs[memberValuePairs.length - 1];
                        annotationEnd = lastMemberValuePair.value != null
                           ? (
                              lastMemberValuePair.value instanceof Annotation
                                 ? ((Annotation)lastMemberValuePair.value).declarationSourceEnd
                                 : lastMemberValuePair.value.sourceEnd
                           )
                           : lastMemberValuePair.sourceEnd;
                     }

                     NormalAnnotation normalAnnotation = new NormalAnnotation(typeReference, this.sourceStart);
                     normalAnnotation.memberValuePairs = memberValuePairs;
                     normalAnnotation.declarationSourceEnd = annotationEnd;
                     normalAnnotation.bits |= 32;
                     annot = normalAnnotation;
                     needUpdateRParenPos = true;
                  }
               }
               break;
            case 2:
               if (parser.expressionPtr > -1) {
                  Expression memberValue = parser.expressionStack[parser.expressionPtr--];
                  SingleMemberAnnotation singleMemberAnnotation = new SingleMemberAnnotation(typeReference, this.sourceStart);
                  singleMemberAnnotation.memberValue = memberValue;
                  singleMemberAnnotation.declarationSourceEnd = memberValue.sourceEnd;
                  singleMemberAnnotation.bits |= 32;
                  annot = singleMemberAnnotation;
                  needUpdateRParenPos = true;
               }
         }

         if (!needUpdateRParenPos) {
            if (pendingMemberValueName != null) {
               NormalAnnotation normalAnnotation = new NormalAnnotation(typeReference, this.sourceStart);
               normalAnnotation.memberValuePairs = new MemberValuePair[]{pendingMemberValueName};
               normalAnnotation.declarationSourceEnd = pendingMemberValueName.value.sourceEnd;
               normalAnnotation.bits |= 32;
               annot = normalAnnotation;
            } else {
               MarkerAnnotation markerAnnotation = new MarkerAnnotation(typeReference, this.sourceStart);
               markerAnnotation.declarationSourceEnd = markerAnnotation.sourceEnd;
               markerAnnotation.bits |= 32;
               annot = markerAnnotation;
            }
         }

         parser.currentElement = this.addAnnotation(annot, this.identifierPtr);
         parser.annotationRecoveryCheckPoint(annot.sourceStart, annot.declarationSourceEnd);
         if (this.parent != null) {
            this.parent.updateFromParserState();
         }
      }
   }

   @Override
   public ASTNode parseTree() {
      return this.annotation;
   }

   @Override
   public void resetPendingModifiers() {
      if (this.parent != null) {
         this.parent.resetPendingModifiers();
      }
   }

   public void setKind(int kind) {
      this.kind = kind;
   }

   @Override
   public int sourceEnd() {
      if (this.annotation == null) {
         Parser parser = this.parser();
         return this.identifierPtr < parser.identifierPositionStack.length ? (int)parser.identifierPositionStack[this.identifierPtr] : this.sourceStart;
      } else {
         return this.annotation.declarationSourceEnd;
      }
   }

   @Override
   public String toString(int tab) {
      return this.annotation != null
         ? this.tabString(tab) + "Recovered annotation:\n" + this.annotation.print(tab + 1, new StringBuffer(10))
         : this.tabString(tab) + "Recovered annotation: identiferPtr=" + this.identifierPtr + " identiferlengthPtr=" + this.identifierLengthPtr + "\n";
   }

   public Annotation updatedAnnotationReference() {
      return this.annotation;
   }

   @Override
   public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd) {
      if (this.bracketBalance > 0) {
         --this.bracketBalance;
         return this;
      } else {
         return (RecoveredElement)(this.parent != null ? this.parent.updateOnClosingBrace(braceStart, braceEnd) : this);
      }
   }

   @Override
   public void updateParseTree() {
      this.updatedAnnotationReference();
   }
}
