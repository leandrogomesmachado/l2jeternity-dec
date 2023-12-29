package org.eclipse.jdt.internal.compiler.env;

import java.util.Arrays;
import org.eclipse.jdt.core.compiler.CharOperation;

public class ClassSignature {
   char[] className;

   public ClassSignature(char[] className) {
      this.className = className;
   }

   public char[] getTypeName() {
      return this.className;
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append(this.className);
      buffer.append(".class");
      return buffer.toString();
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + CharOperation.hashCode(this.className);
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
         ClassSignature other = (ClassSignature)obj;
         return Arrays.equals(this.className, other.className);
      }
   }
}
