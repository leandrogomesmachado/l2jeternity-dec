package l2e.gameserver.listener.clan;

import l2e.gameserver.listener.AbstractListener;
import l2e.gameserver.listener.events.ClanWarehouseAddItemEvent;
import l2e.gameserver.listener.events.ClanWarehouseDeleteItemEvent;
import l2e.gameserver.listener.events.ClanWarehouseTransferEvent;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.items.itemcontainer.ClanWarehouse;

public abstract class ClanWarehouseListener extends AbstractListener {
   private final ClanWarehouse _clanWarehouse;

   public ClanWarehouseListener(Clan clan) {
      this._clanWarehouse = (ClanWarehouse)clan.getWarehouse();
      this.register();
   }

   public abstract boolean onAddItem(ClanWarehouseAddItemEvent var1);

   public abstract boolean onDeleteItem(ClanWarehouseDeleteItemEvent var1);

   public abstract boolean onTransferItem(ClanWarehouseTransferEvent var1);

   @Override
   public void register() {
      this._clanWarehouse.addWarehouseListener(this);
   }

   @Override
   public void unregister() {
      this._clanWarehouse.removeWarehouseListener(this);
   }

   public ClanWarehouse getWarehouse() {
      return this._clanWarehouse;
   }
}
