package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class PropsToStringGeneratorExtension implements GeneratorExtension {
   private Collection excludePropNames = null;

   public void setExcludePropertyNames(Collection var1) {
      this.excludePropNames = var1;
   }

   public Collection getExcludePropertyNames() {
      return this.excludePropNames;
   }

   @Override
   public Collection extraGeneralImports() {
      return Collections.EMPTY_SET;
   }

   @Override
   public Collection extraSpecificImports() {
      return Collections.EMPTY_SET;
   }

   @Override
   public Collection extraInterfaceNames() {
      return Collections.EMPTY_SET;
   }

   @Override
   public void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
      var5.println("public String toString()");
      var5.println("{");
      var5.upIndent();
      var5.println("StringBuffer sb = new StringBuffer();");
      var5.println("sb.append( super.toString() );");
      var5.println("sb.append(\" [ \");");
      int var6 = 0;

      for(int var7 = var3.length; var6 < var7; ++var6) {
         Property var8 = var3[var6];
         if (this.excludePropNames == null || !this.excludePropNames.contains(var8.getName())) {
            var5.println("sb.append( \"" + var8.getName() + " -> \"" + " + " + var8.getName() + " );");
            if (var6 != var7 - 1) {
               var5.println("sb.append( \", \");");
            }
         }
      }

      var5.println();
      var5.println("String extraToStringInfo = this.extraToStringInfo();");
      var5.println("if (extraToStringInfo != null)");
      var5.upIndent();
      var5.println("sb.append( extraToStringInfo );");
      var5.downIndent();
      var5.println("sb.append(\" ]\");");
      var5.println("return sb.toString();");
      var5.downIndent();
      var5.println("}");
      var5.println();
      var5.println("protected String extraToStringInfo()");
      var5.println("{ return null; }");
   }
}
