package com.mchange.v1.xmlprops;

import com.mchange.lang.PotentiallySecondaryException;

public class XmlPropsException extends PotentiallySecondaryException {
   public XmlPropsException(String var1, Throwable var2) {
      super(var1, var2);
   }

   public XmlPropsException(Throwable var1) {
      super(var1);
   }

   public XmlPropsException(String var1) {
      super(var1);
   }

   public XmlPropsException() {
   }
}
