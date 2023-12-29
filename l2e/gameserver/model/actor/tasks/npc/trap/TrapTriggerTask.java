package l2e.gameserver.model.actor.tasks.npc.trap;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.instance.TrapInstance;

public class TrapTriggerTask implements Runnable {
   private final TrapInstance _trap;

   public TrapTriggerTask(TrapInstance trap) {
      this._trap = trap;
   }

   @Override
   public void run() {
      try {
         this._trap.doCast(this._trap.getSkill());
         ThreadPoolManager.getInstance().schedule(new TrapUnsummonTask(this._trap), (long)(this._trap.getSkill().getHitTime() + 300));
      } catch (Exception var2) {
         this._trap.unSummon();
      }
   }
}
