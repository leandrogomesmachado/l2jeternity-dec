package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;

public class SignatureWrapper {
   public char[] signature;
   public int start;
   public int end;
   public int bracket;
   private boolean use15specifics;
   private boolean useExternalAnnotations;

   public SignatureWrapper(char[] signature, boolean use15specifics) {
      this.signature = signature;
      this.start = 0;
      this.end = this.bracket = -1;
      this.use15specifics = use15specifics;
      if (!use15specifics) {
         this.removeTypeArguments();
      }
   }

   public SignatureWrapper(char[] signature, boolean use15specifics, boolean useExternalAnnotations) {
      this.signature = signature;
      this.start = 0;
      this.end = this.bracket = -1;
      this.use15specifics = use15specifics;
      this.useExternalAnnotations = useExternalAnnotations;
      if (!use15specifics) {
         this.removeTypeArguments();
      }
   }

   public SignatureWrapper(char[] signature) {
      this(signature, true);
   }

   public boolean atEnd() {
      return this.start < 0 || this.start >= this.signature.length;
   }

   public boolean isParameterized() {
      return this.bracket == this.end;
   }

   public int computeEnd() {
      int index = this.start;
      if (!this.useExternalAnnotations) {
         while(this.signature[index] == '[') {
            ++index;
         }
      } else {
         label51:
         while(true) {
            switch(this.signature[index]) {
               case '0':
               case '1':
               case '@':
                  if (index == this.start) {
                     break label51;
                  }
               case '[':
                  break;
               default:
                  break label51;
            }

            ++index;
         }
      }

      switch(this.signature[index]) {
         case 'L':
         case 'T':
            this.end = CharOperation.indexOf(';', this.signature, this.start);
            if (this.bracket <= this.start) {
               this.bracket = CharOperation.indexOf('<', this.signature, this.start);
            }

            if (this.bracket > this.start && this.bracket < this.end) {
               this.end = this.bracket;
            } else if (this.end == -1) {
               this.end = this.signature.length + 1;
            }
            break;
         default:
            this.end = index;
      }

      if (!this.use15specifics && this.end == this.bracket) {
         this.start = this.skipAngleContents(this.end) + 1;
         this.bracket = -1;
      } else {
         this.start = this.end + 1;
      }

      return this.end;
   }

   private void removeTypeArguments() {
      StringBuilder buffer = new StringBuilder();
      int offset = 0;
      int index = this.start;
      if (this.signature[0] == '<') {
         ++index;
      }

      for(; index < this.signature.length; ++index) {
         if (this.signature[index] == '<') {
            buffer.append(this.signature, offset, index - offset);
            index = offset = this.skipAngleContents(index);
         }
      }

      buffer.append(this.signature, offset, index - offset);
      this.signature = new char[buffer.length()];
      buffer.getChars(0, this.signature.length, this.signature, 0);
   }

   public int skipAngleContents(int i) {
      if (this.signature[i] != '<') {
         return i;
      } else {
         int depth = 0;
         int length = this.signature.length;
         ++i;

         for(; i < length; ++i) {
            switch(this.signature[i]) {
               case '<':
                  ++depth;
               case '=':
               default:
                  break;
               case '>':
                  if (--depth < 0) {
                     return i + 1;
                  }
            }
         }

         return i;
      }
   }

   public char[] nextWord() {
      this.end = CharOperation.indexOf(';', this.signature, this.start);
      if (this.bracket <= this.start) {
         this.bracket = CharOperation.indexOf('<', this.signature, this.start);
      }

      int dot = CharOperation.indexOf('.', this.signature, this.start);
      if (this.bracket > this.start && this.bracket < this.end) {
         this.end = this.bracket;
      }

      if (dot > this.start && dot < this.end) {
         this.end = dot;
      }

      return CharOperation.subarray(this.signature, this.start, this.start = this.end);
   }

   public char[] nextName() {
      this.end = CharOperation.indexOf(';', this.signature, this.start);
      if (this.bracket <= this.start) {
         this.bracket = CharOperation.indexOf('<', this.signature, this.start);
      }

      if (this.bracket > this.start && this.bracket < this.end) {
         this.end = this.bracket;
      }

      return CharOperation.subarray(this.signature, this.start, this.start = this.end);
   }

   public char[] peekFullType() {
      int s = this.start;
      int b = this.bracket;
      int e = this.end;
      int peekEnd = this.skipAngleContents(this.computeEnd());
      this.start = s;
      this.bracket = b;
      this.end = e;
      return CharOperation.subarray(this.signature, s, peekEnd + 1);
   }

   public char[] getFrom(int s) {
      if (this.end == this.bracket) {
         this.end = this.skipAngleContents(this.bracket);
         this.start = this.end + 1;
      }

      return CharOperation.subarray(this.signature, s, this.end + 1);
   }

   public char[] tail() {
      return CharOperation.subarray(this.signature, this.start, this.signature.length);
   }

   @Override
   public String toString() {
      return new String(this.signature) + " @ " + this.start;
   }
}
