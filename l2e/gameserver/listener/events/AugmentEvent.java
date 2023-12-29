package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.items.instance.ItemInstance;

public class AugmentEvent implements EventListener {
   private ItemInstance _item;
   private Augmentation _augmentation;
   private boolean _isAugment;

   public ItemInstance getItem() {
      return this._item;
   }

   public void setItem(ItemInstance item) {
      this._item = item;
   }

   public Augmentation getAugmentation() {
      return this._augmentation;
   }

   public void setAugmentation(Augmentation augmentation) {
      this._augmentation = augmentation;
   }

   public boolean isAugment() {
      return this._isAugment;
   }

   public void setIsAugment(boolean isAugment) {
      this._isAugment = isAugment;
   }
}
