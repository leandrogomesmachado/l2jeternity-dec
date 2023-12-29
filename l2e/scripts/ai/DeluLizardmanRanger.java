package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Ranger;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import org.apache.commons.lang.ArrayUtils;

public class DeluLizardmanRanger extends Ranger {
   private static final int[] MONSTERS = new int[]{21104, 21105, 21107};
   private static NpcStringId[] MONSTERS_MSG = new NpcStringId[]{
      NpcStringId.S1_HOW_DARE_YOU_INTERRUPT_OUR_FIGHT_HEY_GUYS_HELP,
      NpcStringId.S1_HEY_WERE_HAVING_A_DUEL_HERE,
      NpcStringId.THE_DUEL_IS_OVER_ATTACK,
      NpcStringId.FOUL_KILL_THE_COWARD,
      NpcStringId.HOW_DARE_YOU_INTERRUPT_A_SACRED_DUEL_YOU_MUST_BE_TAUGHT_A_LESSON
   };
   private static NpcStringId[] MONSTERS_ASSIST_MSG = new NpcStringId[]{
      NpcStringId.DIE_YOU_COWARD, NpcStringId.KILL_THE_COWARD, NpcStringId.WHAT_ARE_YOU_LOOKING_AT
   };

   public DeluLizardmanRanger(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.isScriptValue(0)) {
         int i = Rnd.get(5);
         if (i < 2) {
            NpcSay packet = new NpcSay(actor.getObjectId(), 22, actor.getId(), MONSTERS_MSG[i]);
            packet.addStringParameter(attacker.getName().toString());
            actor.broadcastPacket(packet, 2000);
         } else {
            actor.broadcastPacket(new NpcSay(actor.getObjectId(), 22, actor.getId(), MONSTERS_MSG[i]), 2000);
         }

         for(Npc npc : World.getInstance().getAroundNpc(actor, 500, 200)) {
            if (npc.isMonster()
               && ArrayUtils.contains(MONSTERS, npc.getId())
               && !npc.isAttackingNow()
               && !npc.isDead()
               && GeoEngine.canSeeTarget(actor, npc, false)) {
               npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(500));
               npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), MONSTERS_ASSIST_MSG[Rnd.get(3)]), 2000);
            }
         }

         actor.setScriptValue(1);
      }

      super.onEvtAttacked(attacker, damage);
   }
}
