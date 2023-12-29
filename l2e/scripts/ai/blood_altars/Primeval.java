package l2e.scripts.ai.blood_altars;

import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.entity.BloodAltarsEngine;

public class Primeval extends BloodAltarsEngine {
   private static ScheduledFuture<?> _changeStatusTask = null;
   private int _status = 0;
   private int _progress = 0;

   public Primeval(String name, String descr) {
      super(name, descr);
      this.restoreStatus(this.getName());
   }

   @Override
   public boolean changeSpawnInterval(long time, int status, int progress) {
      if (_changeStatusTask != null) {
         _changeStatusTask.cancel(false);
         _changeStatusTask = null;
      }

      this._status = status;
      this._progress = progress;
      _changeStatusTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            Primeval.this.changeStatus(Primeval.this.getName(), Primeval.this.getChangeTime(), Primeval.this.getStatus());
         }
      }, time);
      return true;
   }

   @Override
   public int getStatus() {
      return this._status;
   }

   @Override
   public int getProgress() {
      return this._progress;
   }

   public static void main(String[] args) {
      new Primeval(Primeval.class.getSimpleName(), "ai");
   }
}
