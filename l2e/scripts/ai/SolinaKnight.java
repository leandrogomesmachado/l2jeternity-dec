package l2e.scripts.ai;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;

public class SolinaKnight extends Fighter {
   private Npc scarecrow = null;

   public SolinaKnight(Attackable actor) {
      super(actor);
      actor.setIsGlobalAI(true);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().getAI().enableAI();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      if (this.scarecrow != null) {
         this.getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this.scarecrow, Integer.valueOf(1));
         return true;
      } else {
         for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar(), 400, 200)) {
            if (npc.getId() == 18912
               && (this.scarecrow == null || this.getActiveChar().getDistance3D(npc) < this.getActiveChar().getDistance3D(this.scarecrow))) {
               this.scarecrow = npc;
            }
         }

         return super.thinkActive();
      }
   }
}
