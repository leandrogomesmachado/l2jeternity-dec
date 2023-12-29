package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.Inventory;

public final class RequestSaveInventoryOrder extends GameClientPacket {
   private List<RequestSaveInventoryOrder.InventoryOrder> _order;
   private static final int LIMIT = 125;

   @Override
   protected void readImpl() {
      int sz = this.readD();
      sz = Math.min(sz, 125);
      this._order = new ArrayList<>(sz);

      for(int i = 0; i < sz; ++i) {
         int objectId = this.readD();
         int order = this.readD();
         this._order.add(new RequestSaveInventoryOrder.InventoryOrder(objectId, order));
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         Inventory inventory = player.getInventory();

         for(RequestSaveInventoryOrder.InventoryOrder order : this._order) {
            ItemInstance item = inventory.getItemByObjectId(order.objectID);
            if (item != null && item.getItemLocation() == ItemInstance.ItemLocation.INVENTORY) {
               item.setItemLocation(ItemInstance.ItemLocation.INVENTORY, order.order);
            }
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }

   private static class InventoryOrder {
      int order;
      int objectID;

      public InventoryOrder(int id, int ord) {
         this.objectID = id;
         this.order = ord;
      }
   }
}
