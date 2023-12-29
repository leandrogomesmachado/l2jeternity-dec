package com.mchange.v2.codegen.bean;

import java.io.IOException;
import java.io.Writer;

public interface PropertyBeanGenerator {
   void generate(ClassInfo var1, Property[] var2, Writer var3) throws IOException;
}
