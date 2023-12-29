package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;

public final class CleftConverterInstance extends NpcInstance {
   public CleftConverterInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void showChatWindow(Player player) {
      this.showChatWindow(player, "data/html/aerialCleft/32520-00.htm");
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (player != null && player.getLastFolkNPC() != null && player.getLastFolkNPC().getObjectId() == this.getObjectId()) {
         if (command.equalsIgnoreCase("Teleport")) {
            if (!AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()) {
               player.teleToClosestTown();
            } else {
               AerialCleftEvent.getInstance().removePlayer(player.getObjectId(), true);
            }
         } else {
            super.onBypassFeedback(player, command);
         }
      }
   }
}
