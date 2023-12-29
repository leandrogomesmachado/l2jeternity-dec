package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class AncientHerbSlayer extends Fighter {
   public AncientHerbSlayer(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && attacker.getFirstEffect(2900) != null) {
         int delta = actor.getLevel() - attacker.getLevel();
         if (delta < 6) {
            actor.doDie(attacker);
         }
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected boolean checkAggression(Creature target) {
      Attackable actor = this.getActiveChar();
      if (actor.getId() == 22659) {
         if (target == null || target.getActingPlayer() == null) {
            return false;
         }

         if (actor.isScriptValue(0)) {
            actor.setScriptValue(1);
            actor.broadcastPacket(
               new CreatureSay(actor.getObjectId(), 0, actor.getName(), NpcStringId.EVEN_THE_MAGIC_FORCE_BINDS_YOU_YOU_WILL_NEVER_BE_FORGIVEN)
            );
         }
      }

      return super.checkAggression(target);
   }
}
