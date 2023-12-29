package com.mysql.cj.jdbc;

import java.lang.ref.Reference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AbandonedConnectionCleanupThread implements Runnable {
   private static final ExecutorService cleanupThreadExcecutorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
         Thread t = new Thread(r, "Abandoned connection cleanup thread");
         t.setDaemon(true);
         t.setContextClassLoader(AbandonedConnectionCleanupThread.class.getClassLoader());
         AbandonedConnectionCleanupThread.threadRef = t;
         return t;
      }
   });
   static Thread threadRef = null;

   private AbandonedConnectionCleanupThread() {
   }

   @Override
   public void run() {
      while(true) {
         try {
            this.checkContextClassLoaders();
            Reference<? extends ConnectionImpl> ref = NonRegisteringDriver.refQueue.remove(5000L);
            if (ref != null) {
               try {
                  ((NonRegisteringDriver.ConnectionPhantomReference)ref).cleanup();
               } finally {
                  NonRegisteringDriver.connectionPhantomRefs.remove(ref);
               }
            }
         } catch (InterruptedException var7) {
            threadRef = null;
            return;
         } catch (Exception var8) {
         }
      }
   }

   private void checkContextClassLoaders() {
      try {
         threadRef.getContextClassLoader().getResource("");
      } catch (Throwable var2) {
         uncheckedShutdown();
      }
   }

   private static boolean consistentClassLoaders() {
      ClassLoader callerCtxClassLoader = Thread.currentThread().getContextClassLoader();
      ClassLoader threadCtxClassLoader = threadRef.getContextClassLoader();
      return callerCtxClassLoader != null && threadCtxClassLoader != null && callerCtxClassLoader == threadCtxClassLoader;
   }

   public static void checkedShutdown() {
      shutdown(true);
   }

   public static void uncheckedShutdown() {
      shutdown(false);
   }

   private static void shutdown(boolean checked) {
      if (!checked || consistentClassLoaders()) {
         cleanupThreadExcecutorService.shutdownNow();
      }
   }

   @Deprecated
   public static void shutdown() {
      checkedShutdown();
   }

   static {
      cleanupThreadExcecutorService.execute(new AbandonedConnectionCleanupThread());
   }
}
