package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class DragonKnight extends Fighter {
   public DragonKnight(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      super.onEvtDead(killer);
      if (killer != null) {
         int chance = this.getActiveChar().getTemplate().getParameter("helpersSpawnChance", 0);
         switch(this.getActiveChar().getId()) {
            case 22844:
               if (Rnd.chance(chance)) {
                  MonsterInstance n = NpcUtils.spawnSingle(22845, this.getActiveChar().getLocation());
                  n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(2));
               }
               break;
            case 22845:
               if (Rnd.chance(chance)) {
                  MonsterInstance n = NpcUtils.spawnSingle(22846, this.getActiveChar().getLocation());
                  n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(2));
               }
         }
      }
   }
}
