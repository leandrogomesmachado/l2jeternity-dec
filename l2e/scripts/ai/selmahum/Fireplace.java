package l2e.scripts.ai.selmahum;

import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;

public class Fireplace extends Fighter {
   private static final long delay = 300000L;

   public Fireplace(Attackable actor) {
      super(actor);
      actor.setIsInvul(true);
      actor.setIsRunner(true);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      if (Rnd.chance(60)) {
         this.getActiveChar().setDisplayEffect(1);
      }

      this.getActiveChar().getAI().enableAI();
      ThreadPoolManager.getInstance().scheduleAtFixedRate(new Fireplace.Switch(), 10000L, 300000L);
   }

   public class Switch implements Runnable {
      @Override
      public void run() {
         Attackable actor = Fireplace.this.getActiveChar();
         if (actor.getDisplayEffect() == 1) {
            actor.setDisplayEffect(0);
         } else {
            actor.setDisplayEffect(1);
            if (Rnd.chance(70)) {
               NpcUtils.spawnSingle(18933, actor.getLocation(), 150000L);
            }
         }
      }
   }
}
