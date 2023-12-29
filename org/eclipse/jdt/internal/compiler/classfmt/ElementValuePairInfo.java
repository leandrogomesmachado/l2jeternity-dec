package org.eclipse.jdt.internal.compiler.classfmt;

import java.util.Arrays;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;

public class ElementValuePairInfo implements IBinaryElementValuePair {
   static final ElementValuePairInfo[] NoMembers = new ElementValuePairInfo[0];
   private char[] name;
   private Object value;

   ElementValuePairInfo(char[] name, Object value) {
      this.name = name;
      this.value = value;
   }

   @Override
   public char[] getName() {
      return this.name;
   }

   @Override
   public Object getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append(this.name);
      buffer.append('=');
      if (this.value instanceof Object[]) {
         Object[] values = (Object[])this.value;
         buffer.append('{');
         int i = 0;

         for(int l = values.length; i < l; ++i) {
            if (i > 0) {
               buffer.append(", ");
            }

            buffer.append(values[i]);
         }

         buffer.append('}');
      } else {
         buffer.append(this.value);
      }

      return buffer.toString();
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + CharOperation.hashCode(this.name);
      return 31 * result + (this.value == null ? 0 : this.value.hashCode());
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
         ElementValuePairInfo other = (ElementValuePairInfo)obj;
         if (!Arrays.equals(this.name, other.name)) {
            return false;
         } else {
            if (this.value == null) {
               if (other.value != null) {
                  return false;
               }
            } else if (!this.value.equals(other.value)) {
               return false;
            }

            return true;
         }
      }
   }
}
