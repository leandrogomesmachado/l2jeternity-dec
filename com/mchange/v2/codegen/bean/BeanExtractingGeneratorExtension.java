package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.CodegenUtils;
import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class BeanExtractingGeneratorExtension implements GeneratorExtension {
   int ctor_modifiers = 1;
   int method_modifiers = 2;

   public void setConstructorModifiers(int var1) {
      this.ctor_modifiers = var1;
   }

   public int getConstructorModifiers() {
      return this.ctor_modifiers;
   }

   public void setExtractMethodModifiers(int var1) {
      this.method_modifiers = var1;
   }

   public int getExtractMethodModifiers() {
      return this.method_modifiers;
   }

   @Override
   public Collection extraGeneralImports() {
      return Collections.EMPTY_SET;
   }

   @Override
   public Collection extraSpecificImports() {
      HashSet var1 = new HashSet();
      var1.add("java.beans.BeanInfo");
      var1.add("java.beans.PropertyDescriptor");
      var1.add("java.beans.Introspector");
      var1.add("java.beans.IntrospectionException");
      var1.add("java.lang.reflect.InvocationTargetException");
      return var1;
   }

   @Override
   public Collection extraInterfaceNames() {
      return Collections.EMPTY_SET;
   }

   @Override
   public void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
      var5.println("private static Class[] NOARGS = new Class[0];");
      var5.println();
      var5.print(CodegenUtils.getModifierString(this.method_modifiers));
      var5.print(" void extractPropertiesFromBean( Object bean ) throws InvocationTargetException, IllegalAccessException, IntrospectionException");
      var5.println("{");
      var5.upIndent();
      var5.println("BeanInfo bi = Introspector.getBeanInfo( bean.getClass() );");
      var5.println("PropertyDescriptor[] pds = bi.getPropertyDescriptors();");
      var5.println("for (int i = 0, len = pds.length; i < len; ++i)");
      var5.println("{");
      var5.upIndent();
      int var6 = 0;

      for(int var7 = var3.length; var6 < var7; ++var6) {
         var5.println("if (\"" + var3[var6].getName() + "\".equals( pds[i].getName() ) )");
         var5.upIndent();
         var5.println("this." + var3[var6].getName() + " = " + this.extractorExpr(var3[var6], var4[var6]) + ';');
         var5.downIndent();
      }

      var5.println("}");
      var5.downIndent();
      var5.println("}");
      var5.println();
      var5.print(CodegenUtils.getModifierString(this.ctor_modifiers));
      var5.println(' ' + var1.getClassName() + "( Object bean ) throws InvocationTargetException, IllegalAccessException, IntrospectionException");
      var5.println("{");
      var5.upIndent();
      var5.println("extractPropertiesFromBean( bean );");
      var5.downIndent();
      var5.println("}");
   }

   private String extractorExpr(Property var1, Class var2) {
      if (var2.isPrimitive()) {
         String var3 = BeangenUtils.capitalize(var1.getSimpleTypeName());
         String var4 = var1.getSimpleTypeName() + "Value()";
         if (var2 == Character.TYPE) {
            var3 = "Character";
         } else if (var2 == Integer.TYPE) {
            var3 = "Integer";
         }

         return "((" + var3 + ") pds[i].getReadMethod().invoke( bean, NOARGS ))." + var4;
      } else {
         return "(" + var1.getSimpleTypeName() + ") pds[i].getReadMethod().invoke( bean, NOARGS )";
      }
   }
}
