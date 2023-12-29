package l2e.scripts.ai.dragonvalley;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;

public class DustRider extends Fighter {
   private long _lastAttackTime = 0L;

   public DustRider(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      this._lastAttackTime = System.currentTimeMillis();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (this._lastAttackTime != 0L) {
         if (this._lastAttackTime + 300000L < System.currentTimeMillis() && actor.getAggroRange() == 0) {
            actor.getTemplate().setAggroRange(400);
         }

         if (this._lastAttackTime + 1800000L < System.currentTimeMillis()) {
            actor.deleteMe();
         }
      }

      return super.thinkActive();
   }

   @Override
   public int getRateDEBUFF() {
      return 5;
   }

   @Override
   public int getRateDAM() {
      return 80;
   }
}
