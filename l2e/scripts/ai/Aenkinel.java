package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.quest.Quest;

public class Aenkinel extends Fighter {
   public Aenkinel(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      int instanceId = actor.getReflectionId();
      if (actor.getId() == 25694) {
         for(int i = 0; i < 4; ++i) {
            Quest.addSpawn(18820, actor.getLocation(), actor.getGeoIndex(), instanceId, 250);
         }
      } else if (actor.getId() == 25695) {
         for(int i = 0; i < 4; ++i) {
            Quest.addSpawn(18823, actor.getLocation(), actor.getGeoIndex(), instanceId, 250);
         }
      }

      super.onEvtDead(killer);
   }
}
