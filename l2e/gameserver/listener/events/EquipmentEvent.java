package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.items.instance.ItemInstance;

public class EquipmentEvent implements EventListener {
   private ItemInstance _item;
   private boolean _isEquipped;

   public ItemInstance getItem() {
      return this._item;
   }

   public void setItem(ItemInstance item) {
      this._item = item;
   }

   public boolean isEquipped() {
      return this._isEquipped;
   }

   public void setEquipped(boolean isEquipped) {
      this._isEquipped = isEquipped;
   }
}
