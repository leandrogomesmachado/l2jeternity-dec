package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Stats;

public class ExStorageMaxCount extends GameServerPacket {
   private final Player _activeChar;
   private final int _inventory;
   private final int _warehouse;
   private final int _clan;
   private final int _privateSell;
   private final int _privateBuy;
   private final int _receipeD;
   private final int _recipe;
   private final int _inventoryExtraSlots;
   private final int _inventoryQuestItems;

   public ExStorageMaxCount(Player character) {
      this._activeChar = character;
      this._inventory = this._activeChar.getInventoryLimit();
      this._warehouse = this._activeChar.getWareHouseLimit();
      this._privateSell = this._activeChar.getPrivateSellStoreLimit();
      this._privateBuy = this._activeChar.getPrivateBuyStoreLimit();
      this._clan = Config.WAREHOUSE_SLOTS_CLAN;
      this._receipeD = this._activeChar.getDwarfRecipeLimit();
      this._recipe = this._activeChar.getCommonRecipeLimit();
      this._inventoryExtraSlots = (int)this._activeChar.getStat().calcStat(Stats.INV_LIM, 0.0, null, null);
      this._inventoryQuestItems = Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._inventory);
      this.writeD(this._warehouse);
      this.writeD(this._clan);
      this.writeD(this._privateSell);
      this.writeD(this._privateBuy);
      this.writeD(this._receipeD);
      this.writeD(this._recipe);
      this.writeD(this._inventoryExtraSlots);
      this.writeD(this._inventoryQuestItems);
   }
}
