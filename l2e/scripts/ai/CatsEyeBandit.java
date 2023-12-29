package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public final class CatsEyeBandit extends Fighter {
   public CatsEyeBandit(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable npc = this.getActiveChar();
      QuestState qs = attacker.getActingPlayer().getQuestState("_403_PathToRogue");
      if (attacker != null && npc.isScriptValue(0) && qs != null && (qs.getItemEquipped(5) == 1181 || qs.getItemEquipped(5) == 1182)) {
         npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.YOU_CHILDISH_FOOL_DO_YOU_THINK_YOU_CAN_CATCH_ME), 2000);
         npc.setScriptValue(1);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable npc = this.getActiveChar();
      QuestState qs = killer.getActingPlayer().getQuestState("_403_PathToRogue");
      if (qs != null) {
         npc.broadcastPacket(new NpcSay(npc, 22, NpcStringId.I_MUST_DO_SOMETHING_ABOUT_THIS_SHAMEFUL_INCIDENT));
      }

      super.onEvtDead(killer);
   }
}
