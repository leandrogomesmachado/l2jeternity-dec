package l2e.gameserver.model.entity.events.model.object;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CTFCombatFlagObject {
   private ItemInstance _item;
   private Player _player = null;

   public void spawnObject(Player player) {
      if (this._item == null) {
         this._player = player;
         this._item = ItemsParser.getInstance().createItem(9819);
         this._player.getInventory().addItem(this._item, "CTFCombatFlag");
         this._player.getInventory().equipItem(this._item);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
         sm.addItemName(this._item);
         this._player.sendPacket(sm);
         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(this._item);
            this._player.sendPacket(iu);
         } else {
            this._player.sendItemList(false);
         }

         this._player.broadcastUserInfo(true);
         this._player.setCombatFlagEquipped(true);
      }
   }

   public void despawnObject() {
      if (this._item != null && this._player != null) {
         this._player.setCombatFlagEquipped(false);
         int slot = this._player.getInventory().getSlotFromItem(this._item);
         this._player.getInventory().unEquipItemInBodySlot(slot);
         this._player.destroyItem("CTFCombatFlag", this._item, this._player, true);
         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addRemovedItem(this._item);
            this._player.sendPacket(iu);
         } else {
            this._player.sendItemList(false);
         }

         this.checkFlag(this._player);
         this._player.broadcastUserInfo(true);
         this._item = null;
         this._player = null;
      }
   }

   private void checkFlag(Player player) {
      if (player.getInventory().getItemByItemId(9819) != null) {
         int slot = player.getInventory().getSlotFromItem(player.getInventory().getItemByItemId(9819));
         player.getInventory().unEquipItemInBodySlot(slot);
         player.destroyItem("CTFCombatFlag", player.getInventory().getItemByItemId(9819), null, false);
      }
   }
}
