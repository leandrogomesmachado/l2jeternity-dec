package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.CodegenUtils;
import com.mchange.v2.codegen.IndentedWriter;
import com.mchange.v2.log.MLog;
import com.mchange.v2.log.MLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class ExplicitPropsConstructorGeneratorExtension implements GeneratorExtension {
   static final MLogger logger = MLog.getLogger(ExplicitPropsConstructorGeneratorExtension.class);
   String[] propNames;
   boolean skips_silently = false;
   int ctor_modifiers = 1;

   public ExplicitPropsConstructorGeneratorExtension() {
   }

   public ExplicitPropsConstructorGeneratorExtension(String[] var1) {
      this.propNames = var1;
   }

   public String[] getPropNames() {
      return (String[])this.propNames.clone();
   }

   public void setPropNames(String[] var1) {
      this.propNames = (String[])var1.clone();
   }

   public boolean isSkipsSilently() {
      return this.skips_silently;
   }

   public void setsSkipsSilently(boolean var1) {
      this.skips_silently = var1;
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
      HashMap var6 = new HashMap();
      int var7 = 0;

      for(int var8 = var3.length; var7 < var8; ++var7) {
         var6.put(var3[var7].getName(), var3[var7]);
      }

      ArrayList var12 = new ArrayList(this.propNames.length);
      int var13 = 0;

      for(int var9 = this.propNames.length; var13 < var9; ++var13) {
         Property var10 = (Property)var6.get(this.propNames[var13]);
         if (var10 == null) {
            logger.warning(
               "Could not include property '"
                  + this.propNames[var13]
                  + "' in explicit-props-constructor generated for bean class '"
                  + var1.getClassName()
                  + "' because the property is not defined for the bean. Skipping."
            );
         } else {
            var12.add(var10);
         }
      }

      if (var12.size() > 0) {
         Property[] var14 = var12.toArray(new Property[var12.size()]);
         var5.print(CodegenUtils.getModifierString(this.ctor_modifiers));
         var5.print(var1.getClassName() + "( ");
         BeangenUtils.writeArgList(var14, true, var5);
         var5.println(" )");
         var5.println("{");
         var5.upIndent();
         int var15 = 0;

         for(int var16 = var14.length; var15 < var16; ++var15) {
            var5.print("this." + var14[var15].getName() + " = ");
            String var11 = var14[var15].getDefensiveCopyExpression();
            if (var11 == null) {
               var11 = var14[var15].getName();
            }

            var5.println(var11 + ';');
         }

         var5.downIndent();
         var5.println("}");
      }
   }
}
