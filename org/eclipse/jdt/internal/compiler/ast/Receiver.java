package org.eclipse.jdt.internal.compiler.ast;

public class Receiver extends Argument {
   public NameReference qualifyingName;

   public Receiver(char[] name, long posNom, TypeReference typeReference, NameReference qualifyingName, int modifiers) {
      super(name, posNom, typeReference, modifiers);
      this.qualifyingName = qualifyingName;
   }

   @Override
   public boolean isReceiver() {
      return true;
   }

   @Override
   public StringBuffer print(int indent, StringBuffer output) {
      printIndent(indent, output);
      printModifiers(this.modifiers, output);
      if (this.type == null) {
         output.append("<no type> ");
      } else {
         this.type.print(0, output).append(' ');
      }

      if (this.qualifyingName != null) {
         this.qualifyingName.print(indent, output);
         output.append('.');
      }

      return output.append(this.name);
   }
}
