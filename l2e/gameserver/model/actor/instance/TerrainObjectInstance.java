package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public final class TerrainObjectInstance extends Npc {
   public TerrainObjectInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.TerrainObjectInstance);
   }

   @Override
   public void onAction(Player player, boolean interact) {
      player.sendActionFailed();
   }

   @Override
   public void onActionShift(Player player) {
      if (player.isGM()) {
         super.onActionShift(player);
      } else {
         player.sendActionFailed();
      }
   }
}
