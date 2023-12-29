package com.mchange.v2.codegen.intfc;

import com.mchange.v1.lang.ClassUtils;
import com.mchange.v2.codegen.CodegenUtils;
import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class DelegatorGenerator {
   int class_modifiers = 1025;
   int method_modifiers = 1;
   int wrapping_ctor_modifiers = 1;
   int default_ctor_modifiers = 1;
   boolean wrapping_constructor = true;
   boolean default_constructor = true;
   boolean inner_getter = true;
   boolean inner_setter = true;
   Class superclass = null;
   Class[] extraInterfaces = null;
   Method[] reflectiveDelegateMethods = null;
   ReflectiveDelegationPolicy reflectiveDelegationPolicy = ReflectiveDelegationPolicy.USE_MAIN_DELEGATE_INTERFACE;
   static final Comparator classComp = new Comparator() {
      @Override
      public int compare(Object var1, Object var2) {
         return ((Class)var1).getName().compareTo(((Class)var2).getName());
      }
   };

   public void setGenerateInnerSetter(boolean var1) {
      this.inner_setter = var1;
   }

   public boolean isGenerateInnerSetter() {
      return this.inner_setter;
   }

   public void setGenerateInnerGetter(boolean var1) {
      this.inner_getter = var1;
   }

   public boolean isGenerateInnerGetter() {
      return this.inner_getter;
   }

   public void setGenerateNoArgConstructor(boolean var1) {
      this.default_constructor = var1;
   }

   public boolean isGenerateNoArgConstructor() {
      return this.default_constructor;
   }

   public void setGenerateWrappingConstructor(boolean var1) {
      this.wrapping_constructor = var1;
   }

   public boolean isGenerateWrappingConstructor() {
      return this.wrapping_constructor;
   }

   public void setWrappingConstructorModifiers(int var1) {
      this.wrapping_ctor_modifiers = var1;
   }

   public int getWrappingConstructorModifiers() {
      return this.wrapping_ctor_modifiers;
   }

   public void setNoArgConstructorModifiers(int var1) {
      this.default_ctor_modifiers = var1;
   }

   public int getNoArgConstructorModifiers() {
      return this.default_ctor_modifiers;
   }

   public void setMethodModifiers(int var1) {
      this.method_modifiers = var1;
   }

   public int getMethodModifiers() {
      return this.method_modifiers;
   }

   public void setClassModifiers(int var1) {
      this.class_modifiers = var1;
   }

   public int getClassModifiers() {
      return this.class_modifiers;
   }

   public void setSuperclass(Class var1) {
      this.superclass = var1;
   }

   public Class getSuperclass() {
      return this.superclass;
   }

   public void setExtraInterfaces(Class[] var1) {
      this.extraInterfaces = var1;
   }

   public Class[] getExtraInterfaces() {
      return this.extraInterfaces;
   }

   public Method[] getReflectiveDelegateMethods() {
      return this.reflectiveDelegateMethods;
   }

   public void setReflectiveDelegateMethods(Method[] var1) {
      this.reflectiveDelegateMethods = var1;
   }

   public ReflectiveDelegationPolicy getReflectiveDelegationPolicy() {
      return this.reflectiveDelegationPolicy;
   }

   public void setReflectiveDelegationPolicy(ReflectiveDelegationPolicy var1) {
      this.reflectiveDelegationPolicy = var1;
   }

   public void writeDelegator(Class var1, String var2, Writer var3) throws IOException {
      IndentedWriter var4 = CodegenUtils.toIndentedWriter(var3);
      String var5 = var2.substring(0, var2.lastIndexOf(46));
      String var6 = CodegenUtils.fqcnLastElement(var2);
      String var7 = this.superclass != null ? ClassUtils.simpleClassName(this.superclass) : null;
      String var8 = ClassUtils.simpleClassName(var1);
      String[] var9 = null;
      if (this.extraInterfaces != null) {
         var9 = new String[this.extraInterfaces.length];
         int var10 = 0;

         for(int var11 = this.extraInterfaces.length; var10 < var11; ++var10) {
            var9[var10] = ClassUtils.simpleClassName(this.extraInterfaces[var10]);
         }
      }

      TreeSet var15 = new TreeSet(classComp);
      Method[] var16 = var1.getMethods();
      if (!CodegenUtils.inSamePackage(var1.getName(), var2)) {
         var15.add(var1);
      }

      if (this.superclass != null && !CodegenUtils.inSamePackage(this.superclass.getName(), var2)) {
         var15.add(this.superclass);
      }

      if (this.extraInterfaces != null) {
         int var12 = 0;

         for(int var13 = this.extraInterfaces.length; var12 < var13; ++var12) {
            Class var14 = this.extraInterfaces[var12];
            if (!CodegenUtils.inSamePackage(var14.getName(), var2)) {
               var15.add(var14);
            }
         }
      }

      this.ensureImports(var2, var15, var16);
      if (this.reflectiveDelegateMethods != null) {
         this.ensureImports(var2, var15, this.reflectiveDelegateMethods);
      }

      if (this.reflectiveDelegationPolicy.delegateClass != null && !CodegenUtils.inSamePackage(this.reflectiveDelegationPolicy.delegateClass.getName(), var2)) {
         var15.add(this.reflectiveDelegationPolicy.delegateClass);
      }

      this.generateBannerComment(var4);
      var4.println("package " + var5 + ';');
      var4.println();
      Iterator var17 = var15.iterator();

      while(var17.hasNext()) {
         var4.println("import " + ((Class)var17.next()).getName() + ';');
      }

      this.generateExtraImports(var4);
      var4.println();
      this.generateClassJavaDocComment(var4);
      var4.print(CodegenUtils.getModifierString(this.class_modifiers) + " class " + var6);
      if (this.superclass != null) {
         var4.print(" extends " + var7);
      }

      var4.print(" implements " + var8);
      if (var9 != null) {
         int var18 = 0;

         for(int var22 = var9.length; var18 < var22; ++var18) {
            var4.print(", " + var9[var18]);
         }
      }

      var4.println();
      var4.println("{");
      var4.upIndent();
      var4.println("protected " + var8 + " inner;");
      var4.println();
      if (this.reflectiveDelegateMethods != null) {
         var4.println("protected Class __delegateClass = null;");
      }

      var4.println();
      var4.println("private void __setInner( " + var8 + " inner )");
      var4.println("{");
      var4.upIndent();
      var4.println("this.inner = inner;");
      if (this.reflectiveDelegateMethods != null) {
         String var19;
         if (this.reflectiveDelegationPolicy == ReflectiveDelegationPolicy.USE_MAIN_DELEGATE_INTERFACE) {
            var19 = var8 + ".class";
         } else if (this.reflectiveDelegationPolicy == ReflectiveDelegationPolicy.USE_RUNTIME_CLASS) {
            var19 = "inner.getClass()";
         } else {
            var19 = ClassUtils.simpleClassName(this.reflectiveDelegationPolicy.delegateClass) + ".class";
         }

         var4.println("this.__delegateClass = inner == null ? null : " + var19 + ";");
      }

      var4.downIndent();
      var4.println("}");
      var4.println();
      if (this.wrapping_constructor) {
         var4.println(CodegenUtils.getModifierString(this.wrapping_ctor_modifiers) + ' ' + var6 + '(' + var8 + " inner)");
         var4.println("{ __setInner( inner ); }");
      }

      if (this.default_constructor) {
         var4.println();
         var4.println(CodegenUtils.getModifierString(this.default_ctor_modifiers) + ' ' + var6 + "()");
         var4.println("{}");
      }

      if (this.inner_setter) {
         var4.println();
         var4.println(CodegenUtils.getModifierString(this.method_modifiers) + " void setInner( " + var8 + " inner )");
         var4.println("{ __setInner( inner ); }");
      }

      if (this.inner_getter) {
         var4.println();
         var4.println(CodegenUtils.getModifierString(this.method_modifiers) + ' ' + var8 + " getInner()");
         var4.println("{ return inner; }");
      }

      var4.println();
      int var20 = 0;

      for(int var23 = var16.length; var20 < var23; ++var20) {
         Method var25 = var16[var20];
         if (var20 != 0) {
            var4.println();
         }

         var4.println(CodegenUtils.methodSignature(this.method_modifiers, var25, null));
         var4.println("{");
         var4.upIndent();
         this.generatePreDelegateCode(var1, var2, var25, var4);
         this.generateDelegateCode(var1, var2, var25, var4);
         this.generatePostDelegateCode(var1, var2, var25, var4);
         var4.downIndent();
         var4.println("}");
      }

      if (this.reflectiveDelegateMethods != null) {
         var4.println("// Methods not in core interface to be delegated via reflection");
         int var21 = 0;

         for(int var24 = this.reflectiveDelegateMethods.length; var21 < var24; ++var21) {
            Method var26 = this.reflectiveDelegateMethods[var21];
            if (var21 != 0) {
               var4.println();
            }

            var4.println(CodegenUtils.methodSignature(this.method_modifiers, var26, null));
            var4.println("{");
            var4.upIndent();
            this.generatePreDelegateCode(var1, var2, var26, var4);
            this.generateReflectiveDelegateCode(var1, var2, var26, var4);
            this.generatePostDelegateCode(var1, var2, var26, var4);
            var4.downIndent();
            var4.println("}");
         }
      }

      var4.println();
      this.generateExtraDeclarations(var1, var2, var4);
      var4.downIndent();
      var4.println("}");
   }

   private void ensureImports(String var1, Set var2, Method[] var3) {
      int var4 = 0;

      for(int var5 = var3.length; var4 < var5; ++var4) {
         Class[] var6 = var3[var4].getParameterTypes();
         int var7 = 0;

         for(int var8 = var6.length; var7 < var8; ++var7) {
            if (!CodegenUtils.inSamePackage(var6[var7].getName(), var1)) {
               var2.add(CodegenUtils.unarrayClass(var6[var7]));
            }
         }

         Class[] var10 = var3[var4].getExceptionTypes();
         int var11 = 0;

         for(int var9 = var10.length; var11 < var9; ++var11) {
            if (!CodegenUtils.inSamePackage(var10[var11].getName(), var1)) {
               var2.add(CodegenUtils.unarrayClass(var10[var11]));
            }
         }

         if (!CodegenUtils.inSamePackage(var3[var4].getReturnType().getName(), var1)) {
            var2.add(CodegenUtils.unarrayClass(var3[var4].getReturnType()));
         }
      }
   }

   protected void generateDelegateCode(Class var1, String var2, Method var3, IndentedWriter var4) throws IOException {
      Class var5 = var3.getReturnType();
      var4.println((var5 == Void.TYPE ? "" : "return ") + "inner." + CodegenUtils.methodCall(var3) + ";");
   }

   protected void generateReflectiveDelegateCode(Class var1, String var2, Method var3, IndentedWriter var4) throws IOException {
      Class var5 = var3.getReturnType();
      String var6 = CodegenUtils.reflectiveMethodParameterTypeArray(var3);
      String var7 = CodegenUtils.reflectiveMethodObjectArray(var3);
      Class[] var8 = var3.getExceptionTypes();
      HashSet var9 = new HashSet();
      var9.addAll(Arrays.asList(var8));
      var4.println("try");
      var4.println("{");
      var4.upIndent();
      var4.println("Method m = __delegateClass.getMethod(\"" + var3.getName() + "\", " + var6 + ");");
      var4.println((var5 == Void.TYPE ? "" : "return (" + ClassUtils.simpleClassName(var5) + ") ") + "m.invoke( inner, " + var7 + " );");
      var4.downIndent();
      var4.println("}");
      if (!var9.contains(IllegalAccessException.class)) {
         var4.println("catch (IllegalAccessException iae)");
         var4.println("{");
         var4.upIndent();
         var4.println(
            "throw new RuntimeException(\"A reflectively delegated method '"
               + var3.getName()
               + "' cannot access the object to which the call is delegated\", iae);"
         );
         var4.downIndent();
         var4.println("}");
      }

      var4.println("catch (InvocationTargetException ite)");
      var4.println("{");
      var4.upIndent();
      var4.println("Throwable cause = ite.getCause();");
      var4.println("if (cause instanceof RuntimeException) throw (RuntimeException) cause;");
      var4.println("if (cause instanceof Error) throw (Error) cause;");
      int var10 = var8.length;
      if (var10 > 0) {
         for(int var11 = 0; var11 < var10; ++var11) {
            String var12 = ClassUtils.simpleClassName(var8[var11]);
            var4.println("if (cause instanceof " + var12 + ") throw (" + var12 + ") cause;");
         }
      }

      var4.println("throw new RuntimeException(\"Target of reflectively delegated method '" + var3.getName() + "' threw an Exception.\", cause);");
      var4.downIndent();
      var4.println("}");
   }

   protected void generateBannerComment(IndentedWriter var1) throws IOException {
      var1.println("/*");
      var1.println(" * This class generated by " + this.getClass().getName());
      var1.println(" * " + new Date());
      var1.println(" * DO NOT HAND EDIT!!!!");
      var1.println(" */");
   }

   protected void generateClassJavaDocComment(IndentedWriter var1) throws IOException {
      var1.println("/**");
      var1.println(" * This class was generated by " + this.getClass().getName() + ".");
      var1.println(" */");
   }

   protected void generateExtraImports(IndentedWriter var1) throws IOException {
   }

   protected void generatePreDelegateCode(Class var1, String var2, Method var3, IndentedWriter var4) throws IOException {
   }

   protected void generatePostDelegateCode(Class var1, String var2, Method var3, IndentedWriter var4) throws IOException {
   }

   protected void generateExtraDeclarations(Class var1, String var2, IndentedWriter var3) throws IOException {
   }
}
