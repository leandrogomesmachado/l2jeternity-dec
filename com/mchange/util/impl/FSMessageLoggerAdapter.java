package com.mchange.util.impl;

import com.mchange.util.FailSuppressedMessageLogger;
import com.mchange.util.MessageLogger;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FSMessageLoggerAdapter implements FailSuppressedMessageLogger {
   MessageLogger inner;
   List failures = null;

   public FSMessageLoggerAdapter(MessageLogger var1) {
      this.inner = var1;
   }

   @Override
   public void log(String var1) {
      try {
         this.inner.log(var1);
      } catch (IOException var3) {
         this.addFailure(var3);
      }
   }

   @Override
   public void log(Throwable var1, String var2) {
      try {
         this.inner.log(var1, var2);
      } catch (IOException var4) {
         this.addFailure(var4);
      }
   }

   @Override
   public synchronized Iterator getFailures() {
      if (this.inner instanceof FailSuppressedMessageLogger) {
         return ((FailSuppressedMessageLogger)this.inner).getFailures();
      } else {
         return this.failures != null ? this.failures.iterator() : null;
      }
   }

   @Override
   public synchronized void clearFailures() {
      if (this.inner instanceof FailSuppressedMessageLogger) {
         ((FailSuppressedMessageLogger)this.inner).clearFailures();
      } else {
         this.failures = null;
      }
   }

   private synchronized void addFailure(IOException var1) {
      if (this.failures == null) {
         this.failures = new LinkedList();
      }

      this.failures.add(var1);
   }
}
