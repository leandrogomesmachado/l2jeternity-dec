package l2e.scripts.ai.sevensigns;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import org.apache.commons.lang3.ArrayUtils;

public class LilithMinion extends Fighter {
   private final int[] _enemies = new int[]{32719, 32720, 32721};

   public LilithMinion(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().setIsNoRndWalk(true);
      this.getActiveChar().setIsImmobilized(true);
      super.onEvtSpawn();
      ThreadPoolManager.getInstance().schedule(new LilithMinion.Attack(), 3000L);
   }

   @Override
   protected boolean thinkActive() {
      for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar())) {
         if (ArrayUtils.contains(this._enemies, npc.getId())) {
            this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, npc, Integer.valueOf(100000));
            this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, Integer.valueOf(100000));
         }
      }

      return true;
   }

   public class Attack implements Runnable {
      @Override
      public void run() {
         LilithMinion.this.getActiveChar().setIsImmobilized(false);
      }
   }
}
