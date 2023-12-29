package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.CodegenUtils;
import java.io.IOException;
import java.lang.reflect.Modifier;

public class InnerBeanPropertyBeanGenerator extends SimplePropertyBeanGenerator {
   String innerBeanClassName;
   int inner_bean_member_modifiers = 4;
   int inner_bean_accessor_modifiers = 4;
   int inner_bean_replacer_modifiers = 4;
   String innerBeanInitializationExpression = null;

   public void setInnerBeanClassName(String var1) {
      this.innerBeanClassName = var1;
   }

   public String getInnerBeanClassName() {
      return this.innerBeanClassName;
   }

   private String defaultInnerBeanInitializationExpression() {
      return "new " + this.innerBeanClassName + "()";
   }

   private String findInnerBeanClassName() {
      return this.innerBeanClassName == null ? "InnerBean" : this.innerBeanClassName;
   }

   private String findInnerBeanInitializationExpression() {
      return this.innerBeanInitializationExpression == null ? this.defaultInnerBeanInitializationExpression() : this.innerBeanInitializationExpression;
   }

   private int findInnerClassModifiers() {
      int var1 = 8;
      if (Modifier.isPublic(this.inner_bean_accessor_modifiers) || Modifier.isPublic(this.inner_bean_replacer_modifiers)) {
         var1 |= 1;
      } else if (Modifier.isProtected(this.inner_bean_accessor_modifiers) || Modifier.isProtected(this.inner_bean_replacer_modifiers)) {
         var1 |= 4;
      } else if (Modifier.isPrivate(this.inner_bean_accessor_modifiers) && Modifier.isPrivate(this.inner_bean_replacer_modifiers)) {
         var1 |= 2;
      }

      return var1;
   }

   private void writeSyntheticInnerBeanClass() throws IOException {
      int var1 = this.props.length;
      Property[] var2 = new Property[var1];

      for(int var3 = 0; var3 < var1; ++var3) {
         var2[var3] = new SimplePropertyMask(this.props[var3]) {
            @Override
            public int getVariableModifiers() {
               return 130;
            }
         };
      }

      WrapperClassInfo var4 = new WrapperClassInfo(this.info) {
         @Override
         public String getClassName() {
            return "InnerBean";
         }

         @Override
         public int getModifiers() {
            return InnerBeanPropertyBeanGenerator.this.findInnerClassModifiers();
         }
      };
      this.createInnerGenerator().generate(var4, var2, this.iw);
   }

   protected PropertyBeanGenerator createInnerGenerator() {
      SimplePropertyBeanGenerator var1 = new SimplePropertyBeanGenerator();
      var1.setInner(true);
      var1.addExtension(new SerializableExtension());
      CloneableExtension var2 = new CloneableExtension();
      var2.setExceptionSwallowing(true);
      var1.addExtension(var2);
      return var1;
   }

   @Override
   protected void writeOtherVariables() throws IOException {
      this.iw
         .println(
            CodegenUtils.getModifierString(this.inner_bean_member_modifiers)
               + ' '
               + this.findInnerBeanClassName()
               + " innerBean = "
               + this.findInnerBeanInitializationExpression()
               + ';'
         );
      this.iw.println();
      this.iw.println(CodegenUtils.getModifierString(this.inner_bean_accessor_modifiers) + ' ' + this.findInnerBeanClassName() + " accessInnerBean()");
      this.iw.println("{ return innerBean; }");
   }

   @Override
   protected void writeOtherFunctions() throws IOException {
      this.iw
         .print(
            CodegenUtils.getModifierString(this.inner_bean_replacer_modifiers)
               + ' '
               + this.findInnerBeanClassName()
               + " replaceInnerBean( "
               + this.findInnerBeanClassName()
               + " innerBean )"
         );
      if (this.constrainedProperties()) {
         this.iw.println(" throws PropertyVetoException");
      } else {
         this.iw.println();
      }

      this.iw.println("{");
      this.iw.upIndent();
      this.iw.println("beforeReplaceInnerBean();");
      this.iw.println("this.innerBean = innerBean;");
      this.iw.println("afterReplaceInnerBean();");
      this.iw.downIndent();
      this.iw.println("}");
      this.iw.println();
      boolean var1 = Modifier.isAbstract(this.info.getModifiers());
      this.iw.print("protected ");
      if (var1) {
         this.iw.print("abstract ");
      }

      this.iw.print("void beforeReplaceInnerBean()");
      if (this.constrainedProperties()) {
         this.iw.print(" throws PropertyVetoException");
      }

      if (var1) {
         this.iw.println(';');
      } else {
         this.iw.println(" {} //hook method for subclasses");
      }

      this.iw.println();
      this.iw.print("protected ");
      if (var1) {
         this.iw.print("abstract ");
      }

      this.iw.print("void afterReplaceInnerBean()");
      if (var1) {
         this.iw.println(';');
      } else {
         this.iw.println(" {} //hook method for subclasses");
      }

      this.iw.println();
      BeangenUtils.writeExplicitDefaultConstructor(1, this.info, this.iw);
      this.iw.println();
      this.iw.println("public " + this.info.getClassName() + "(" + this.findInnerBeanClassName() + " innerBean)");
      this.iw.println("{ this.innerBean = innerBean; }");
   }

   @Override
   protected void writeOtherClasses() throws IOException {
      if (this.innerBeanClassName == null) {
         this.writeSyntheticInnerBeanClass();
      }
   }

   @Override
   protected void writePropertyVariable(Property var1) throws IOException {
   }

   @Override
   protected void writePropertyGetter(Property var1, Class var2) throws IOException {
      String var3 = var1.getSimpleTypeName();
      String var4 = "boolean".equals(var3) ? "is" : "get";
      String var5 = var4 + BeangenUtils.capitalize(var1.getName());
      this.iw.print(CodegenUtils.getModifierString(var1.getGetterModifiers()));
      this.iw.println(' ' + var1.getSimpleTypeName() + ' ' + var5 + "()");
      this.iw.println('{');
      this.iw.upIndent();
      this.iw.println(var3 + ' ' + var1.getName() + " = innerBean." + var5 + "();");
      String var6 = this.getGetterDefensiveCopyExpression(var1, var2);
      if (var6 == null) {
         var6 = var1.getName();
      }

      this.iw.println("return " + var6 + ';');
      this.iw.downIndent();
      this.iw.println('}');
   }

   @Override
   protected void writePropertySetter(Property var1, Class var2) throws IOException {
      String var3 = var1.getSimpleTypeName();
      String var4 = "boolean".equals(var3) ? "is" : "get";
      String var5 = this.getSetterDefensiveCopyExpression(var1, var2);
      if (var5 == null) {
         var5 = var1.getName();
      }

      String var6 = "innerBean." + var4 + BeangenUtils.capitalize(var1.getName()) + "()";
      String var7 = "innerBean.set" + BeangenUtils.capitalize(var1.getName()) + "( " + var5 + " );";
      BeangenUtils.writePropertySetterWithGetExpressionSetStatement(var1, var6, var7, this.iw);
   }
}
