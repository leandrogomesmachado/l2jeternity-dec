package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class ExplicitDefaultConstructorGeneratorExtension implements GeneratorExtension {
   int ctor_modifiers = 1;

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
      BeangenUtils.writeExplicitDefaultConstructor(this.ctor_modifiers, var1, var5);
   }
}
