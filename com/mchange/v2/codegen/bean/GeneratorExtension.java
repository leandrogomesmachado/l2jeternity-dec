package com.mchange.v2.codegen.bean;

import com.mchange.v2.codegen.IndentedWriter;
import java.io.IOException;
import java.util.Collection;

public interface GeneratorExtension {
   Collection extraGeneralImports();

   Collection extraSpecificImports();

   Collection extraInterfaceNames();

   void generate(ClassInfo var1, Class var2, Property[] var3, Class[] var4, IndentedWriter var5) throws IOException;
}
