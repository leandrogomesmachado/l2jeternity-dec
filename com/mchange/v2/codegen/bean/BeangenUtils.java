package com.mchange.v2.codegen.bean;

import com.mchange.v1.lang.ClassUtils;
import com.mchange.v2.codegen.CodegenUtils;
import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Comparator;

public final class BeangenUtils {
   public static final Comparator PROPERTY_COMPARATOR = new Comparator() {
      @Override
      public int compare(Object var1, Object var2) {
         Property var3 = (Property)var1;
         Property var4 = (Property)var2;
         return String.CASE_INSENSITIVE_ORDER.compare(var3.getName(), var4.getName());
      }
   };

   public static String capitalize(String var0) {
      char var1 = var0.charAt(0);
      return Character.toUpperCase(var1) + var0.substring(1);
   }

   public static void writeExplicitDefaultConstructor(int var0, ClassInfo var1, IndentedWriter var2) throws IOException {
      var2.print(CodegenUtils.getModifierString(var0));
      var2.println(' ' + var1.getClassName() + "()");
      var2.println("{}");
   }

   public static void writeArgList(Property[] var0, boolean var1, IndentedWriter var2) throws IOException {
      int var3 = 0;

      for(int var4 = var0.length; var3 < var4; ++var3) {
         if (var3 != 0) {
            var2.print(", ");
         }

         if (var1) {
            var2.print(var0[var3].getSimpleTypeName() + ' ');
         }

         var2.print(var0[var3].getName());
      }
   }

   /** @deprecated */
   public static void writePropertyMember(Property var0, IndentedWriter var1) throws IOException {
      writePropertyVariable(var0, var1);
   }

   public static void writePropertyVariable(Property var0, IndentedWriter var1) throws IOException {
      writePropertyVariable(var0, var0.getDefaultValueExpression(), var1);
   }

   /** @deprecated */
   public static void writePropertyMember(Property var0, String var1, IndentedWriter var2) throws IOException {
      writePropertyVariable(var0, var1, var2);
   }

   public static void writePropertyVariable(Property var0, String var1, IndentedWriter var2) throws IOException {
      var2.print(CodegenUtils.getModifierString(var0.getVariableModifiers()));
      var2.print(' ' + var0.getSimpleTypeName() + ' ' + var0.getName());
      if (var1 != null) {
         var2.print(" = " + var1);
      }

      var2.println(';');
   }

   public static void writePropertyGetter(Property var0, IndentedWriter var1) throws IOException {
      writePropertyGetter(var0, var0.getDefensiveCopyExpression(), var1);
   }

   public static void writePropertyGetter(Property var0, String var1, IndentedWriter var2) throws IOException {
      String var3 = "boolean".equals(var0.getSimpleTypeName()) ? "is" : "get";
      var2.print(CodegenUtils.getModifierString(var0.getGetterModifiers()));
      var2.println(' ' + var0.getSimpleTypeName() + ' ' + var3 + capitalize(var0.getName()) + "()");
      String var4 = var1;
      if (var1 == null) {
         var4 = var0.getName();
      }

      var2.println("{ return " + var4 + "; }");
   }

   public static void writePropertySetter(Property var0, IndentedWriter var1) throws IOException {
      writePropertySetter(var0, var0.getDefensiveCopyExpression(), var1);
   }

   public static void writePropertySetter(Property var0, String var1, IndentedWriter var2) throws IOException {
      String var3 = var1;
      if (var1 == null) {
         var3 = var0.getName();
      }

      String var4 = "this." + var0.getName();
      String var5 = "this." + var0.getName() + " = " + var3 + ';';
      writePropertySetterWithGetExpressionSetStatement(var0, var4, var5, var2);
   }

   public static void writePropertySetterWithGetExpressionSetStatement(Property var0, String var1, String var2, IndentedWriter var3) throws IOException {
      var3.print(CodegenUtils.getModifierString(var0.getSetterModifiers()));
      var3.print(" void set" + capitalize(var0.getName()) + "( " + var0.getSimpleTypeName() + ' ' + var0.getName() + " )");
      if (var0.isConstrained()) {
         var3.println(" throws PropertyVetoException");
      } else {
         var3.println();
      }

      var3.println('{');
      var3.upIndent();
      if (changeMarked(var0)) {
         var3.println(var0.getSimpleTypeName() + " oldVal = " + var1 + ';');
         String var4 = "oldVal";
         String var5 = var0.getName();
         String var7 = var0.getSimpleTypeName();
         String var6;
         if (ClassUtils.isPrimitive(var7)) {
            Class var8 = ClassUtils.classForPrimitive(var7);
            if (var8 == Byte.TYPE) {
               var4 = "new Byte( " + var4 + " )";
               var5 = "new Byte( " + var5 + " )";
            } else if (var8 == Character.TYPE) {
               var4 = "new Character( " + var4 + " )";
               var5 = "new Character( " + var5 + " )";
            } else if (var8 == Short.TYPE) {
               var4 = "new Short( " + var4 + " )";
               var5 = "new Short( " + var5 + " )";
            } else if (var8 == Float.TYPE) {
               var4 = "new Float( " + var4 + " )";
               var5 = "new Float( " + var5 + " )";
            } else if (var8 == Double.TYPE) {
               var4 = "new Double( " + var4 + " )";
               var5 = "new Double( " + var5 + " )";
            }

            var6 = "oldVal != " + var0.getName();
         } else {
            var6 = "! eqOrBothNull( oldVal, " + var0.getName() + " )";
         }

         if (var0.isConstrained()) {
            var3.println("if ( " + var6 + " )");
            var3.upIndent();
            var3.println("vcs.fireVetoableChange( \"" + var0.getName() + "\", " + var4 + ", " + var5 + " );");
            var3.downIndent();
         }

         var3.println(var2);
         if (var0.isBound()) {
            var3.println("if ( " + var6 + " )");
            var3.upIndent();
            var3.println("pcs.firePropertyChange( \"" + var0.getName() + "\", " + var4 + ", " + var5 + " );");
            var3.downIndent();
         }
      } else {
         var3.println(var2);
      }

      var3.downIndent();
      var3.println('}');
   }

   public static boolean hasBoundProperties(Property[] var0) {
      int var1 = 0;

      for(int var2 = var0.length; var1 < var2; ++var1) {
         if (var0[var1].isBound()) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasConstrainedProperties(Property[] var0) {
      int var1 = 0;

      for(int var2 = var0.length; var1 < var2; ++var1) {
         if (var0[var1].isConstrained()) {
            return true;
         }
      }

      return false;
   }

   private static boolean changeMarked(Property var0) {
      return var0.isBound() || var0.isConstrained();
   }

   private BeangenUtils() {
   }
}
