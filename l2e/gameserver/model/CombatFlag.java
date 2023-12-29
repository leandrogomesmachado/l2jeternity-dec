package l2e.gameserver.model;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CombatFlag {
   private Player _player = null;
   private int _playerId = 0;
   private ItemInstance _item = null;
   private ItemInstance _itemInstance;
   private final Location _location;
   private final int _itemId;
   protected final int _fortId;

   public CombatFlag(int fort_id, int x, int y, int z, int heading, int item_id) {
      this._fortId = fort_id;
      this._location = new Location(x, y, z, heading);
      this._itemId = item_id;
   }

   public synchronized void spawnMe() {
      this._itemInstance = ItemsParser.getInstance().createItem("Combat", this._itemId, 1L, null, null);
      this._itemInstance.dropMe(null, this._location.getX(), this._location.getY(), this._location.getZ());
   }

   public synchronized void unSpawnMe() {
      if (this._player != null) {
         this.dropIt();
      }

      if (this._itemInstance != null) {
         this._itemInstance.decayMe();
      }
   }

   public boolean activate(Player player, ItemInstance item) {
      if (player.isMounted()) {
         player.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
         return false;
      } else {
         this._player = player;
         this._playerId = this._player.getObjectId();
         this._itemInstance = null;
         this._item = item;
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
         return true;
      }
   }

   public void dropIt() {
      this._player.setCombatFlagEquipped(false);
      int slot = this._player.getInventory().getSlotFromItem(this._item);
      this._player.getInventory().unEquipItemInBodySlot(slot);
      this._player.destroyItem("CombatFlag", this._item, null, true);
      this._item = null;
      this._player.broadcastUserInfo(true);
      this._player = null;
      this._playerId = 0;
   }

   public int getPlayerObjectId() {
      return this._playerId;
   }

   public ItemInstance getCombatFlagInstance() {
      return this._itemInstance;
   }
}
