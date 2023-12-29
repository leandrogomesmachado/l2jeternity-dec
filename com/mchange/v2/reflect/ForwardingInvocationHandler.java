package com.mchange.v2.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ForwardingInvocationHandler implements InvocationHandler {
   Object inner;

   public ForwardingInvocationHandler(Object var1) {
      this.inner = var1;
   }

   @Override
   public Object invoke(Object var1, Method var2, Object[] var3) throws Throwable {
      return var2.invoke(this.inner, var3);
   }
}
