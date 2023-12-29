package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public class ElementValuePair {
   char[] name;
   public Object value;
   public MethodBinding binding;

   public static Object getValue(Expression expression) {
      if (expression == null) {
         return null;
      } else {
         Constant constant = expression.constant;
         if (constant != null && constant != Constant.NotAConstant) {
            return constant;
         } else if (expression instanceof Annotation) {
            return ((Annotation)expression).getCompilerAnnotation();
         } else if (!(expression instanceof ArrayInitializer)) {
            if (expression instanceof ClassLiteralAccess) {
               return ((ClassLiteralAccess)expression).targetType;
            } else {
               if (expression instanceof Reference) {
                  FieldBinding fieldBinding = null;
                  if (expression instanceof FieldReference) {
                     fieldBinding = ((FieldReference)expression).fieldBinding();
                  } else if (expression instanceof NameReference) {
                     Binding binding = ((NameReference)expression).binding;
                     if (binding != null && binding.kind() == 1) {
                        fieldBinding = (FieldBinding)binding;
                     }
                  }

                  if (fieldBinding != null && (fieldBinding.modifiers & 16384) > 0) {
                     return fieldBinding;
                  }
               }

               return null;
            }
         } else {
            Expression[] exprs = ((ArrayInitializer)expression).expressions;
            int length = exprs == null ? 0 : exprs.length;
            Object[] values = new Object[length];

            for(int i = 0; i < length; ++i) {
               values[i] = getValue(exprs[i]);
            }

            return values;
         }
      }
   }

   public ElementValuePair(char[] name, Expression expression, MethodBinding binding) {
      this(name, getValue(expression), binding);
   }

   public ElementValuePair(char[] name, Object value, MethodBinding binding) {
      this.name = name;
      this.value = value;
      this.binding = binding;
   }

   public char[] getName() {
      return this.name;
   }

   public MethodBinding getMethodBinding() {
      return this.binding;
   }

   public Object getValue() {
      if (this.value instanceof ElementValuePair.UnresolvedEnumConstant) {
         this.value = ((ElementValuePair.UnresolvedEnumConstant)this.value).getResolved();
      } else if (this.value instanceof Object[]) {
         Object[] valueArray = (Object[])this.value;

         for(int i = 0; i < valueArray.length; ++i) {
            Object object = valueArray[i];
            if (object instanceof ElementValuePair.UnresolvedEnumConstant) {
               valueArray[i] = ((ElementValuePair.UnresolvedEnumConstant)object).getResolved();
            }
         }
      }

      return this.value;
   }

   void setMethodBinding(MethodBinding binding) {
      this.binding = binding;
   }

   void setValue(Object value) {
      this.value = value;
   }

   @Override
   public String toString() {
      StringBuffer buffer = new StringBuffer(5);
      buffer.append(this.name).append(" = ");
      buffer.append(this.value);
      return buffer.toString();
   }

   public static class UnresolvedEnumConstant {
      ReferenceBinding enumType;
      LookupEnvironment environment;
      char[] enumConstantName;

      UnresolvedEnumConstant(ReferenceBinding enumType, LookupEnvironment environment, char[] enumConstantName) {
         this.enumType = enumType;
         this.environment = environment;
         this.enumConstantName = enumConstantName;
      }

      FieldBinding getResolved() {
         if (this.enumType.isUnresolvedType()) {
            this.enumType = (ReferenceBinding)BinaryTypeBinding.resolveType(this.enumType, this.environment, false);
         }

         return this.enumType.getField(this.enumConstantName, false);
      }

      public char[] getEnumConstantName() {
         return this.enumConstantName;
      }
   }
}
