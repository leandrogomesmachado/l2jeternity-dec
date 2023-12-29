package l2e.gameserver.model.actor.tasks.npc.trap;

import l2e.gameserver.model.actor.instance.TrapInstance;

public class TrapUnsummonTask implements Runnable {
   private final TrapInstance _trap;

   public TrapUnsummonTask(TrapInstance trap) {
      this._trap = trap;
   }

   @Override
   public void run() {
      this._trap.unSummon();
   }
}
