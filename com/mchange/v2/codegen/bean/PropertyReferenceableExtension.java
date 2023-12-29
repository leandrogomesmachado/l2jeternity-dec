package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.IndentedWriter;
import com.mchange.v2.naming.JavaBeanObjectFactory;
import com.mchange.v2.naming.JavaBeanReferenceMaker;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class PropertyReferenceableExtension implements GeneratorExtension {
   boolean explicit_reference_properties = false;
   String factoryClassName = JavaBeanObjectFactory.class.getName();
   String javaBeanReferenceMakerClassName = JavaBeanReferenceMaker.class.getName();

   public void setUseExplicitReferenceProperties(boolean var1) {
      this.explicit_reference_properties = var1;
   }

   public boolean getUseExplicitReferenceProperties() {
      return this.explicit_reference_properties;
   }

   public void setFactoryClassName(String var1) {
      this.factoryClassName = var1;
   }

   public String getFactoryClassName() {
      return this.factoryClassName;
   }

   @Override
   public Collection extraGeneralImports() {
      return new HashSet();
   }

   @Override
   public Collection extraSpecificImports() {
      HashSet var1 = new HashSet();
      var1.add("javax.naming.Reference");
      var1.add("javax.naming.Referenceable");
      var1.add("javax.naming.NamingException");
      var1.add("com.mchange.v2.naming.JavaBeanObjectFactory");
      var1.add("com.mchange.v2.naming.JavaBeanReferenceMaker");
      var1.add("com.mchange.v2.naming.ReferenceMaker");
      return var1;
   }

   @Override
   public Collection extraInterfaceNames() {
      HashSet var1 = new HashSet();
      var1.add("Referenceable");
      return var1;
   }

   @Override
   public void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
      var5.println("final static JavaBeanReferenceMaker referenceMaker = new " + this.javaBeanReferenceMakerClassName + "();");
      var5.println();
      var5.println("static");
      var5.println("{");
      var5.upIndent();
      var5.println("referenceMaker.setFactoryClassName( \"" + this.factoryClassName + "\" );");
      if (this.explicit_reference_properties) {
         int var6 = 0;

         for(int var7 = var3.length; var6 < var7; ++var6) {
            var5.println("referenceMaker.addReferenceProperty(\"" + var3[var6].getName() + "\");");
         }
      }

      var5.downIndent();
      var5.println("}");
      var5.println();
      var5.println("public Reference getReference() throws NamingException");
      var5.println("{");
      var5.upIndent();
      var5.println("return referenceMaker.createReference( this );");
      var5.downIndent();
      var5.println("}");
   }
}
