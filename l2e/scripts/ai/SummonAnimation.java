package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;

public class SummonAnimation extends Fighter {
   public SummonAnimation(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().setShowSummonAnimation(true);
      super.onEvtSpawn();
   }
}
