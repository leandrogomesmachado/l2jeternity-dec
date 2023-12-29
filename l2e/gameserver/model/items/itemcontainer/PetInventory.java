package l2e.gameserver.model.items.itemcontainer;

import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;

public class PetInventory extends Inventory {
   private final PetInstance _owner;

   public PetInventory(PetInstance owner) {
      this._owner = owner;
   }

   public PetInstance getOwner() {
      return this._owner;
   }

   @Override
   public int getOwnerId() {
      try {
         return this._owner.getOwner().getObjectId();
      } catch (NullPointerException var3) {
         return 0;
      }
   }

   @Override
   protected void refreshWeight() {
      super.refreshWeight();
      this.getOwner().updateAndBroadcastStatus(1);
   }

   public boolean validateCapacity(ItemInstance item) {
      int slots = 0;
      if ((!item.isStackable() || this.getItemByItemId(item.getId()) == null) && !item.getItem().isHerb()) {
         ++slots;
      }

      return this.validateCapacity((long)slots);
   }

   @Override
   public boolean validateCapacity(long slots) {
      return (long)this._items.size() + slots <= (long)this._owner.getInventoryLimit();
   }

   public boolean validateWeight(ItemInstance item, long count) {
      int weight = 0;
      Item template = ItemsParser.getInstance().getTemplate(item.getId());
      if (template == null) {
         return false;
      } else {
         weight = (int)((long)weight + count * (long)template.getWeight());
         return this.validateWeight((long)weight);
      }
   }

   @Override
   public boolean validateWeight(long weight) {
      return (long)this._totalWeight + weight <= (long)this._owner.getMaxLoad();
   }

   @Override
   protected ItemInstance.ItemLocation getBaseLocation() {
      return ItemInstance.ItemLocation.PET;
   }

   @Override
   protected ItemInstance.ItemLocation getEquipLocation() {
      return ItemInstance.ItemLocation.PET_EQUIP;
   }

   @Override
   public void restore() {
      super.restore();

      for(ItemInstance item : this._items) {
         if (item.isEquipped() && !item.getItem().checkCondition(this.getOwner(), this.getOwner(), false)) {
            this.unEquipItemInSlot(item.getLocationSlot());
         }
      }
   }

   public void transferItemsToOwner() {
      for(ItemInstance item : this._items) {
         this.getOwner()
            .transferItem(
               "return", item.getObjectId(), item.getCount(), this.getOwner().getOwner().getInventory(), this.getOwner().getOwner(), this.getOwner()
            );
      }
   }
}
