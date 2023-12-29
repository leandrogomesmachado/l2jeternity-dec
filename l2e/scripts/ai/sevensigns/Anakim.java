package l2e.scripts.ai.sevensigns;

import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class Anakim extends Mystic {
   private long _lastSkillTime = 0L;

   public Anakim(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      this.getActiveChar().setIsNoRndWalk(true);
      this.getActiveChar().setIsInvul(true);
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      if (this._lastSkillTime < System.currentTimeMillis()) {
         if (this.getLilith() != null) {
            this.getActiveChar().broadcastPacket(new MagicSkillUse(this.getActiveChar(), this.getLilith(), 6191, 1, 5000, 10));
         }

         this._lastSkillTime = System.currentTimeMillis() + 6500L;
      }

      return true;
   }

   private Npc getLilith() {
      Npc lilith = null;

      for(Npc npc : World.getInstance().getAroundNpc(this.getActiveChar())) {
         if (npc.getId() == 32715) {
            lilith = npc;
         }
      }

      return lilith;
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtAggression(Creature attacker, int aggro) {
   }
}
