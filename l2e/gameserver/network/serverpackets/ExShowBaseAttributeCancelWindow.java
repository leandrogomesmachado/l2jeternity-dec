package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ExShowBaseAttributeCancelWindow extends GameServerPacket {
   private final ItemInstance[] _items;
   private long _price;

   public ExShowBaseAttributeCancelWindow(Player player) {
      this._items = player.getInventory().getElementItems();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._items.length);

      for(ItemInstance item : this._items) {
         this.writeD(item.getObjectId());
         this.writeQ(this.getPrice(item));
      }
   }

   private long getPrice(ItemInstance item) {
      switch(item.getItem().getCrystalType()) {
         case 5:
            if (item.getItem() instanceof Weapon) {
               this._price = 50000L;
            } else {
               this._price = 40000L;
            }
            break;
         case 6:
            if (item.getItem() instanceof Weapon) {
               this._price = 100000L;
            } else {
               this._price = 80000L;
            }
            break;
         case 7:
            if (item.getItem() instanceof Weapon) {
               this._price = 200000L;
            } else {
               this._price = 160000L;
            }
      }

      return this._price;
   }
}
