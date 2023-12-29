package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class OlMahumTranscender extends Fighter {
   private static final NpcStringId[] MSG1 = new NpcStringId[]{
      NpcStringId.ENOUGH_FOOLING_AROUND_GET_READY_TO_DIE, NpcStringId.YOU_IDIOT_IVE_JUST_BEEN_TOYING_WITH_YOU, NpcStringId.NOW_THE_FUN_STARTS
   };
   private static final NpcStringId[] MSG2 = new NpcStringId[]{
      NpcStringId.I_MUST_ADMIT_NO_ONE_MAKES_MY_BLOOD_BOIL_QUITE_LIKE_YOU_DO, NpcStringId.NOW_THE_BATTLE_BEGINS, NpcStringId.WITNESS_MY_TRUE_POWER
   };
   private static final NpcStringId[] MSG3 = new NpcStringId[]{
      NpcStringId.PREPARE_TO_DIE, NpcStringId.ILL_DOUBLE_MY_STRENGTH, NpcStringId.YOU_HAVE_MORE_SKILL_THAN_I_THOUGHT
   };

   public OlMahumTranscender(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      int transformId = 0;
      int chance = 0;
      NpcStringId msg = null;
      short var8;
      byte var9;
      if (actor.getId() == 21261) {
         var8 = 21262;
         var9 = 20;
         msg = MSG1[Rnd.get(MSG1.length)];
      } else if (actor.getId() == 21262) {
         var8 = 21263;
         var9 = 10;
         msg = MSG2[Rnd.get(MSG2.length)];
      } else {
         var8 = 21264;
         var9 = 5;
         msg = MSG3[Rnd.get(MSG3.length)];
      }

      if (attacker != null && actor.isScriptValue(0) && Rnd.chance(var9)) {
         actor.setScriptValue(1);
         actor.broadcastPacket(new NpcSay(actor.getObjectId(), 22, actor.getId(), msg), 2000);
         actor.decayMe();
         Attackable npc = (Attackable)Quest.addSpawn(var8, actor.getLocation(), actor.getReflectionId());
         npc.setRunning();
         npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
         attacker.setTarget(npc);
      }

      super.onEvtAttacked(attacker, damage);
   }
}
