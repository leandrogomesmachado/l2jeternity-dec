package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class MoSMonk extends Fighter {
   public MoSMonk(Attackable actor) {
      super(actor);
   }

   @Override
   protected boolean checkAggression(Creature target) {
      if (target.getActiveWeaponInstance() == null) {
         return false;
      } else if (super.checkAggression(target)) {
         if (this.getActiveChar().isScriptValue(0)) {
            this.getActiveChar().setScriptValue(1);
            this.getActiveChar()
               .broadcastPacket(
                  new NpcSay(this.getActiveChar().getObjectId(), 22, this.getActiveChar().getId(), NpcStringId.YOU_CANNOT_CARRY_A_WEAPON_WITHOUT_AUTHORIZATION),
                  2000
               );
         }

         return true;
      } else {
         return false;
      }
   }
}
