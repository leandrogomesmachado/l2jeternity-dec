package l2e.commons.threading;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableWrapper implements Runnable {
   private static final Logger _log = Logger.getLogger(RunnableWrapper.class.getName());
   private final Runnable _runnable;

   public RunnableWrapper(Runnable runnable) {
      this._runnable = runnable;
   }

   @Override
   public void run() {
      try {
         this._runnable.run();
      } catch (Exception var2) {
         _log.log(Level.WARNING, "Exception: " + var2, (Throwable)var2);
      }
   }
}
