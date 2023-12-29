package com.mchange.v1.lang;

public class AmbiguousClassNameException extends Exception {
   AmbiguousClassNameException(String var1, Class var2, Class var3) {
      super(var1 + " could refer either to " + var2.getName() + " or " + var3.getName());
   }
}
