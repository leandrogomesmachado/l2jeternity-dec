package com.mchange.util;

import java.io.IOException;

public interface MessageLogger {
   void log(String var1) throws IOException;

   void log(Throwable var1, String var2) throws IOException;
}
