package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class Necromancer extends Mystic {
   public Necromancer(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      super.onEvtDead(killer);
      if (killer != null) {
         int chance = this.getActiveChar().getTemplate().getParameter("helpersSpawnChance", 0);
         if (Rnd.chance(chance)) {
            MonsterInstance npc = NpcUtils.spawnSingle(Rnd.chance(50) ? 22818 : 22819, this.getActiveChar().getLocation());
            npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(2));
         }
      }
   }
}
