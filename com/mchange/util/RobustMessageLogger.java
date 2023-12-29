package com.mchange.util;

public interface RobustMessageLogger extends MessageLogger {
   @Override
   void log(String var1);

   @Override
   void log(Throwable var1, String var2);
}
