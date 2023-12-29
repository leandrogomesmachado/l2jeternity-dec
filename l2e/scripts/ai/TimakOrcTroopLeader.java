package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class TimakOrcTroopLeader extends Fighter {
   private static final NpcStringId[] ATTACK_LEADER_MSG = new NpcStringId[]{
      NpcStringId.FORCES_OF_DARKNESS_FOLLOW_ME,
      NpcStringId.DESTROY_THE_ENEMY_MY_BROTHERS,
      NpcStringId.SHOW_YOURSELVES,
      NpcStringId.COME_OUT_YOU_CHILDREN_OF_DARKNESS
   };
   private static final int[] BROTHERS = new int[]{20768, 20769, 20770};

   public TimakOrcTroopLeader(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && !actor.isDead() && actor.isScriptValue(0)) {
         actor.setScriptValue(1);
         actor.broadcastPacket(new NpcSay(actor, 22, ATTACK_LEADER_MSG[Rnd.get(ATTACK_LEADER_MSG.length)]), 2000);

         for(int bro : BROTHERS) {
            try {
               MonsterInstance npc = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(bro));
               Location loc = ((MonsterInstance)actor).getMinionPosition();
               npc.setReflectionId(actor.getReflectionId());
               npc.setHeading(actor.getHeading());
               npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
               npc.spawnMe(loc.getX(), loc.getY(), loc.getZ());
               npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(Rnd.get(1, 100)));
            } catch (Exception var10) {
               var10.printStackTrace();
            }
         }
      }

      super.onEvtAttacked(attacker, damage);
   }
}
