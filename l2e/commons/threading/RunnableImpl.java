package l2e.commons.threading;

import java.util.logging.Logger;

public abstract class RunnableImpl implements Runnable {
   private static final Logger _log = Logger.getLogger(RunnableImpl.class.getName());

   public abstract void runImpl() throws Exception;

   @Override
   public final void run() {
      try {
         this.runImpl();
      } catch (Exception var2) {
         _log.warning("Exception: RunnableImpl.run()");
         var2.printStackTrace();
      }
   }
}
