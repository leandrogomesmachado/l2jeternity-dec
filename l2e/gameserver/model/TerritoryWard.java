package l2e.gameserver.model;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class TerritoryWard {
   protected Player _player = null;
   public int playerId = 0;
   private ItemInstance _item = null;
   private Npc _npc = null;
   private Location _location;
   private Location _oldLocation;
   private final int _itemId;
   private int _ownerCastleId;
   private final int _territoryId;

   public TerritoryWard(int territory_id, int x, int y, int z, int heading, int item_id, int castleId, Npc npc) {
      this._territoryId = territory_id;
      this._location = new Location(x, y, z, heading);
      this._itemId = item_id;
      this._ownerCastleId = castleId;
      this._npc = npc;
   }

   public int getTerritoryId() {
      return this._territoryId;
   }

   public int getOwnerCastleId() {
      return this._ownerCastleId;
   }

   public void setOwnerCastleId(int newOwner) {
      this._ownerCastleId = newOwner;
   }

   public Npc getNpc() {
      return this._npc;
   }

   public void setNpc(Npc npc) {
      this._npc = npc;
   }

   public Player getPlayer() {
      return this._player;
   }

   public synchronized void spawnBack() {
      if (this._player != null) {
         this.dropIt();
      }

      this._npc = TerritoryWarManager.getInstance().spawnNPC(36491 + this._territoryId, this._oldLocation);
   }

   public synchronized void spawnMe() {
      if (this._player != null) {
         this.dropIt();
      }

      this._npc = TerritoryWarManager.getInstance().spawnNPC(36491 + this._territoryId, this._location);
   }

   public synchronized void unSpawnMe() {
      if (this._player != null) {
         this.dropIt();
      }

      if (this._npc != null && !this._npc.isDecayed()) {
         this._npc.deleteMe();
      }
   }

   public boolean activate(Player player, ItemInstance item) {
      if (player.isMounted()) {
         player.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
         player.destroyItem("CombatFlag", item, null, true);
         this.spawnMe();
         return false;
      } else if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(player) == 0) {
         player.sendMessage("Non participants can't pickup Territory Wards!");
         player.destroyItem("CombatFlag", item, null, true);
         this.spawnMe();
         return false;
      } else {
         this._player = player;
         this.playerId = this._player.getObjectId();
         this._oldLocation = new Location(this._npc.getX(), this._npc.getY(), this._npc.getZ(), this._npc.getHeading());
         this._npc = null;
         if (item == null) {
            this._item = ItemsParser.getInstance().createItem("Combat", this._itemId, 1L, null, null);
         } else {
            this._item = item;
         }

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
         this._player.sendPacket(SystemMessageId.YOU_VE_ACQUIRED_THE_WARD);
         TerritoryWarManager.getInstance().giveTWPoint(player, this._territoryId, 5);
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
      this._location = new Location(this._player.getX(), this._player.getY(), this._player.getZ(), this._player.getHeading());
      this._player = null;
      this.playerId = 0;
   }
}
