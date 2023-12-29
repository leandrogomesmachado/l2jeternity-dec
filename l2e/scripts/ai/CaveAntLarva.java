package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class CaveAntLarva extends Fighter {
   private static final NpcStringId[] MSG1 = new NpcStringId[]{
      NpcStringId.ENOUGH_FOOLING_AROUND_GET_READY_TO_DIE, NpcStringId.YOU_IDIOT_IVE_JUST_BEEN_TOYING_WITH_YOU, NpcStringId.NOW_THE_FUN_STARTS
   };
   private static final NpcStringId[] MSG2 = new NpcStringId[]{
      NpcStringId.I_MUST_ADMIT_NO_ONE_MAKES_MY_BLOOD_BOIL_QUITE_LIKE_YOU_DO, NpcStringId.NOW_THE_BATTLE_BEGINS, NpcStringId.WITNESS_MY_TRUE_POWER
   };
   private static final NpcStringId[] MSG3 = new NpcStringId[]{
      NpcStringId.PREPARE_TO_DIE, NpcStringId.ILL_DOUBLE_MY_STRENGTH, NpcStringId.YOU_HAVE_MORE_SKILL_THAN_I_THOUGHT
   };

   public CaveAntLarva(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      int transformId = 0;
      int chance = 0;
      int hp = 0;
      NpcStringId msg = null;
      short var9;
      byte var10;
      byte var11;
      if (actor.getId() == 21265) {
         var9 = 21271;
         var10 = 30;
         var11 = 100;
         msg = MSG1[Rnd.get(MSG1.length)];
      } else if (actor.getId() == 21271) {
         var9 = 21272;
         var10 = 10;
         var11 = 60;
         msg = MSG2[Rnd.get(MSG2.length)];
      } else {
         var9 = 21273;
         var10 = 5;
         var11 = 30;
         msg = MSG2[Rnd.get(MSG3.length)];
      }

      if (attacker != null && actor.isScriptValue(0) && actor.getCurrentHp() <= actor.getMaxHp() * (double)var11 / 100.0 && Rnd.chance(var10)) {
         actor.setScriptValue(1);
         actor.broadcastPacket(new NpcSay(actor.getObjectId(), 22, actor.getId(), msg), 2000);
         actor.decayMe();
         Attackable npc = (Attackable)Quest.addSpawn(var9, actor.getLocation(), actor.getReflectionId());
         npc.setRunning();
         npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
         attacker.setTarget(npc);
      }

      super.onEvtAttacked(attacker, damage);
   }
}
