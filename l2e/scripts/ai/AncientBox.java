package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class AncientBox extends Fighter {
   public AncientBox(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable npc = this.getActiveChar();
      if (attacker != null && npc.isScriptValue(0)) {
         npc.setScriptValue(1);
         npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.YOU_WILL_BE_CURSED_FOR_SEEKING_THE_TREASURE), 2000);
         npc.setTarget(attacker);
         npc.doCast(new SkillHolder(6033, 1).getSkill());
      }

      super.onEvtAttacked(attacker, damage);
   }
}
