package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class BloodyKarik extends Fighter {
   public BloodyKarik(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      super.onEvtDead(killer);
      if (killer != null) {
         int chance = this.getActiveChar().getTemplate().getParameter("helpersSpawnChance", 0);
         if (Rnd.chance(chance) && this.getActiveChar().isScriptValue(0)) {
            String[] amount = this.getActiveChar().getTemplate().getParameter("helpersRndAmount", "4;4").split(";");
            int rnd = Rnd.get(Integer.parseInt(amount[0]), Integer.parseInt(amount[1]));

            for(int x = 0; x < rnd; ++x) {
               MonsterInstance npc = NpcUtils.spawnSingle(22854, Location.findAroundPosition(this.getActiveChar(), 60, 100));
               npc.setScriptValue(1);
               npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(2));
            }
         }
      }
   }
}
