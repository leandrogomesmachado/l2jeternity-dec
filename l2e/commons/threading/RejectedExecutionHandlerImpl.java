package l2e.commons.threading;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {
   private static final Logger _log = Logger.getLogger(RejectedExecutionHandlerImpl.class.getName());

   @Override
   public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      if (!executor.isShutdown()) {
         _log.log(Level.WARNING, r + " from " + executor, (Throwable)(new RejectedExecutionException()));
         if (Thread.currentThread().getPriority() > 5) {
            new Thread(r).start();
         } else {
            r.run();
         }
      }
   }
}
