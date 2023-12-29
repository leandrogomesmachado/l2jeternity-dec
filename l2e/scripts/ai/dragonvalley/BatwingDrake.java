package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class BatwingDrake extends Mystic {
   public BatwingDrake(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (actor != null && !actor.isDead()) {
         int chance = actor.getTemplate().getParameter("helpersSpawnChance", 0);
         if (attacker != null && Rnd.chance(chance)) {
            MonsterInstance npc = NpcUtils.spawnSingle(22828, actor.getX() + Rnd.get(-100, 100), actor.getY() + Rnd.get(-100, 100), actor.getZ());
            npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(2));
         }

         super.onEvtAttacked(attacker, damage);
      }
   }
}
