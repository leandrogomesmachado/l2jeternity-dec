package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class CloneableExtension implements GeneratorExtension {
   boolean export_public;
   boolean exception_swallowing;
   String mLoggerName = null;

   public boolean isExportPublic() {
      return this.export_public;
   }

   public void setExportPublic(boolean var1) {
      this.export_public = var1;
   }

   public boolean isExceptionSwallowing() {
      return this.exception_swallowing;
   }

   public void setExceptionSwallowing(boolean var1) {
      this.exception_swallowing = var1;
   }

   public String getMLoggerName() {
      return this.mLoggerName;
   }

   public void setMLoggerName(String var1) {
      this.mLoggerName = var1;
   }

   public CloneableExtension(boolean var1, boolean var2) {
      this.export_public = var1;
      this.exception_swallowing = var2;
   }

   public CloneableExtension() {
      this(true, false);
   }

   @Override
   public Collection extraGeneralImports() {
      return (Collection)(this.mLoggerName == null ? Collections.EMPTY_SET : Arrays.asList("com.mchange.v2.log"));
   }

   @Override
   public Collection extraSpecificImports() {
      return Collections.EMPTY_SET;
   }

   @Override
   public Collection extraInterfaceNames() {
      HashSet var1 = new HashSet();
      var1.add("Cloneable");
      return var1;
   }

   @Override
   public void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException {
      if (this.export_public) {
         var5.print("public Object clone()");
         if (!this.exception_swallowing) {
            var5.println(" throws CloneNotSupportedException");
         } else {
            var5.println();
         }

         var5.println("{");
         var5.upIndent();
         if (this.exception_swallowing) {
            var5.println("try");
            var5.println("{");
            var5.upIndent();
         }

         var5.println("return super.clone();");
         if (this.exception_swallowing) {
            var5.downIndent();
            var5.println("}");
            var5.println("catch (CloneNotSupportedException e)");
            var5.println("{");
            var5.upIndent();
            if (this.mLoggerName == null) {
               var5.println("e.printStackTrace();");
            } else {
               var5.println("if ( " + this.mLoggerName + ".isLoggable( MLevel.FINE ) )");
               var5.upIndent();
               var5.println(this.mLoggerName + ".log( MLevel.FINE, \"Inconsistent clone() definitions between subclass and superclass! \", e );");
               var5.downIndent();
            }

            var5.println("throw new RuntimeException(\"Inconsistent clone() definitions between subclass and superclass! \" + e);");
            var5.downIndent();
            var5.println("}");
         }

         var5.downIndent();
         var5.println("}");
      }
   }
}
