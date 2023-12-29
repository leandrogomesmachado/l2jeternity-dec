package l2e.gameserver.taskmanager.actionrunner;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.ThreadPoolManager;

public abstract class ActionWrapper extends RunnableImpl {
   private static final Logger _log = Logger.getLogger(ActionWrapper.class.getName());
   private final String _name;
   private Future<?> _scheduledFuture;

   public ActionWrapper(String name) {
      this._name = name;
   }

   public void schedule(long time) {
      this._scheduledFuture = ThreadPoolManager.getInstance().schedule(this, time);
   }

   public void cancel() {
      if (this._scheduledFuture != null) {
         this._scheduledFuture.cancel(true);
         this._scheduledFuture = null;
      }
   }

   public abstract void runImpl0() throws Exception;

   @Override
   public void runImpl() {
      try {
         this.runImpl0();
      } catch (Exception var5) {
         _log.log(Level.INFO, "ActionWrapper: Exception: " + var5 + "; name: " + this._name, (Throwable)var5);
      } finally {
         ActionRunner.getInstance().remove(this._name, this);
         this._scheduledFuture = null;
      }
   }

   public String getName() {
      return this._name;
   }
}
