package org.eclipse.jdt.internal.compiler.env;

import java.util.Arrays;
import org.eclipse.jdt.core.compiler.CharOperation;

public class EnumConstantSignature {
   char[] typeName;
   char[] constName;

   public EnumConstantSignature(char[] typeName, char[] constName) {
      this.typeName = typeName;
      this.constName = constName;
   }

   public char[] getTypeName() {
      return this.typeName;
   }

   public char[] getEnumConstantName() {
      return this.constName;
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append(this.typeName);
      buffer.append('.');
      buffer.append(this.constName);
      return buffer.toString();
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + CharOperation.hashCode(this.constName);
      return 31 * result + CharOperation.hashCode(this.typeName);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         EnumConstantSignature other = (EnumConstantSignature)obj;
         return !Arrays.equals(this.constName, other.constName) ? false : Arrays.equals(this.typeName, other.typeName);
      }
   }
}
