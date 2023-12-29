package com.mchange.v2.csv;

import com.mchange.lang.PotentiallySecondaryException;

public class MalformedCsvException extends PotentiallySecondaryException {
   public MalformedCsvException(String var1, Throwable var2) {
      super(var1, var2);
   }

   public MalformedCsvException(Throwable var1) {
      super(var1);
   }

   public MalformedCsvException(String var1) {
      super(var1);
   }

   public MalformedCsvException() {
   }
}
