package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Tower;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class FlameTowerInstance extends Tower {
   public FlameTowerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.FlameTowerInstance);
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this.getCastle().getSiege().getIsInProgress()) {
         this.getCastle().getSiege().disableTraps();
      }

      super.onDeath(killer);
   }
}
