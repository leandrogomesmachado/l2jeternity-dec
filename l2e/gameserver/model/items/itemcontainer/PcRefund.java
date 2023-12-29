package l2e.gameserver.model.items.itemcontainer;

import java.util.logging.Level;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class PcRefund extends ItemContainer {
   private final Player _owner;

   public PcRefund(Player owner) {
      this._owner = owner;
   }

   @Override
   public String getName() {
      return "Refund";
   }

   public Player getOwner() {
      return this._owner;
   }

   @Override
   public ItemInstance.ItemLocation getBaseLocation() {
      return ItemInstance.ItemLocation.REFUND;
   }

   @Override
   protected void addItem(ItemInstance item) {
      super.addItem(item);

      try {
         if (this.getSize() > 12) {
            ItemInstance removedItem = this._items.remove(0);
            if (removedItem != null) {
               ItemsParser.getInstance().destroyItem("ClearRefund", removedItem, this.getOwner(), null);
               removedItem.updateDatabase(true);
            }
         }
      } catch (Exception var3) {
         _log.log(Level.SEVERE, "addItem()", (Throwable)var3);
      }
   }

   @Override
   public void refreshWeight() {
   }

   @Override
   public void deleteMe() {
      try {
         for(ItemInstance item : this._items) {
            if (item != null) {
               ItemsParser.getInstance().destroyItem("ClearRefund", item, this.getOwner(), null);
               item.updateDatabase(true);
            }
         }
      } catch (Exception var3) {
         _log.log(Level.SEVERE, "deleteMe()", (Throwable)var3);
      }

      this._items.clear();
   }

   @Override
   public void restore() {
   }
}
