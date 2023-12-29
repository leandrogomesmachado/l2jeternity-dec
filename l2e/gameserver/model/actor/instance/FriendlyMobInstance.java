package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class FriendlyMobInstance extends Attackable {
   public FriendlyMobInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.FriendlyMobInstance);
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      if (attacker.isPlayer()) {
         return ((Player)attacker).getKarma() > 0;
      } else {
         return false;
      }
   }

   @Override
   public boolean isAggressive() {
      return true;
   }
}
