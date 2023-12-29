package l2e.gameserver.model.items.enchant;

import l2e.gameserver.model.actor.templates.items.Item;

public final class EnchantRateItem {
   private final String _name;
   private int _itemId;
   private int _slot;
   private Boolean _isMagicWeapon = null;

   public EnchantRateItem(String name) {
      this._name = name;
   }

   public String getName() {
      return this._name;
   }

   public void setItemId(int id) {
      this._itemId = id;
   }

   public void addSlot(int slot) {
      this._slot |= slot;
   }

   public void setMagicWeapon(boolean magicWeapon) {
      this._isMagicWeapon = magicWeapon;
   }

   public boolean validate(Item item) {
      if (this._itemId != 0 && this._itemId != item.getId()) {
         return false;
      } else if (this._slot != 0 && (item.getBodyPart() & this._slot) == 0) {
         return false;
      } else {
         return this._isMagicWeapon == null || item.isMagicWeapon() == this._isMagicWeapon;
      }
   }
}
