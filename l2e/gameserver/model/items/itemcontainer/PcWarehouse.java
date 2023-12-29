package l2e.gameserver.model.items.itemcontainer;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class PcWarehouse extends Warehouse {
   private final Player _owner;

   public PcWarehouse(Player owner) {
      this._owner = owner;
   }

   @Override
   public String getName() {
      return "Warehouse";
   }

   public Player getOwner() {
      return this._owner;
   }

   @Override
   public ItemInstance.ItemLocation getBaseLocation() {
      return ItemInstance.ItemLocation.WAREHOUSE;
   }

   @Override
   public boolean validateCapacity(long slots) {
      return (long)this._items.size() + slots <= (long)this._owner.getWareHouseLimit();
   }
}
