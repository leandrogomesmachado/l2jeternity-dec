package com.mchange.v2.ser;

import java.io.InvalidClassException;

public class UnsupportedVersionException extends InvalidClassException {
   public UnsupportedVersionException(String var1) {
      super(var1);
   }

   public UnsupportedVersionException(Object var1, int var2) {
      this(var1.getClass().getName() + " -- unsupported version: " + var2);
   }
}
