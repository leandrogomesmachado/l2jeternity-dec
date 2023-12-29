package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ControlUnitInstance extends NpcInstance {
   private static final int COND_CAN_OPEN = 0;
   private static final int COND_NO_ITEM = 1;
   private static final int COND_POWER = 2;

   public ControlUnitInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      int cond = this.getCond(player);
      if (cond == 0) {
         if (this.getFort().getSiege().isControlDoorsOpen()) {
            return;
         }

         ItemInstance item = player.getInventory().getItemByItemId(10014);
         if (item == null) {
            this.showChatWindow(player, "data/html/fortress/fortress_controller002.htm");
            return;
         }

         if (player.getInventory().destroyItemByObjectId(item.getObjectId(), 1L, player, Boolean.valueOf(true)) != null) {
            this.getFort().getSiege().spawnPowerUnits();
            this.getFort().getSiege().spawnMainMachine();
            this.getFort().getSiege().openControlDoors(this.getFort().getId());
         } else {
            this.showChatWindow(player, "data/html/fortress/fortress_controller002.htm");
         }
      }
   }

   @Override
   public void showChatWindow(Player player, int val) {
      int cond = this.getCond(player);
      switch(cond) {
         case 0:
            this.showChatWindow(player, "data/html/fortress/fortress_controller001.htm");
            break;
         case 1:
            this.showChatWindow(player, "data/html/fortress/fortress_controller002.htm");
            break;
         case 2:
            this.showChatWindow(player, "data/html/fortress/fortress_controller003.htm");
      }
   }

   private int getCond(Player player) {
      if (!this.getFort().getSiege().getIsInProgress()) {
         return 2;
      } else {
         boolean allPowerDisabled = false;
         if (this.getFort().getSiege().getControlUnits().isEmpty()) {
            allPowerDisabled = true;
         }

         if (allPowerDisabled) {
            return player.getInventory().getItemByItemId(10014) != null && player.getInventory().getItemByItemId(10014).getCount() > 0L ? 0 : 1;
         } else {
            return 2;
         }
      }
   }
}
