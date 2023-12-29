package com.mchange.io;

import java.io.InvalidClassException;

public class UnknownStreamClassException extends InvalidClassException {
   public UnknownStreamClassException(ClassNotFoundException var1) {
      super(var1.getMessage());
   }
}
