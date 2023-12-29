package com.mchange.v2.log;

public interface NameTransformer {
   String transformName(String var1);

   String transformName(Class var1);

   String transformName();
}
