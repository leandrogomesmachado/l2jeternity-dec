package l2e.gameserver.model.actor.templates.items;

import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.ItemType;

public class WarehouseItem {
   private final Item _item;
   private final int _object;
   private final long _count;
   private final int _owner;
   private final int _locationSlot;
   private final int _enchant;
   private final int _grade;
   private boolean _isAugmented;
   private int _augmentationId;
   private final int _customType1;
   private final int _customType2;
   private final int _mana;
   private final int _agathionEnergy;
   private int _elemAtkType = -2;
   private int _elemAtkPower = 0;
   private final int[] _elemDefAttr = new int[]{0, 0, 0, 0, 0, 0};
   private final int[] _enchantOptions;
   private final int _time;

   public WarehouseItem(ItemInstance item) {
      this._item = item.getItem();
      this._object = item.getObjectId();
      this._count = item.getCount();
      this._owner = item.getOwnerId();
      this._locationSlot = item.getLocationSlot();
      this._enchant = item.getEnchantLevel();
      this._customType1 = item.getCustomType1();
      this._customType2 = item.getCustomType2();
      this._grade = item.getItem().getItemGrade();
      if (item.isAugmented()) {
         this._isAugmented = true;
         this._augmentationId = item.getAugmentation().getAugmentationId();
      } else {
         this._isAugmented = false;
      }

      this._mana = item.getMana();
      this._agathionEnergy = item.getAgathionEnergy();
      this._time = item.isTimeLimitedItem() ? (int)(item.getRemainingTime() / 1000L) : -1;
      this._elemAtkType = item.getAttackElementType();
      this._elemAtkPower = item.getAttackElementPower();

      for(byte i = 0; i < 6; ++i) {
         this._elemDefAttr[i] = item.getElementDefAttr(i);
      }

      this._enchantOptions = item.getEnchantOptions();
   }

   public Item getItem() {
      return this._item;
   }

   public final int getObjectId() {
      return this._object;
   }

   public final int getOwnerId() {
      return this._owner;
   }

   public final int getLocationSlot() {
      return this._locationSlot;
   }

   public final long getCount() {
      return this._count;
   }

   public final int getType1() {
      return this._item.getType1();
   }

   public final int getType2() {
      return this._item.getType2();
   }

   public final ItemType getItemType() {
      return this._item.getItemType();
   }

   public final int getId() {
      return this._item.getId();
   }

   public final int getBodyPart() {
      return this._item.getBodyPart();
   }

   public final int getEnchantLevel() {
      return this._enchant;
   }

   public final int getItemGrade() {
      return this._grade;
   }

   public final boolean isWeapon() {
      return this._item instanceof Weapon;
   }

   public final boolean isArmor() {
      return this._item instanceof Armor;
   }

   public final boolean isEtcItem() {
      return this._item instanceof EtcItem;
   }

   public boolean isAugmented() {
      return this._isAugmented;
   }

   public int getAugmentationId() {
      return this._augmentationId;
   }

   public String getName() {
      return this._item.getNameEn();
   }

   public final int getCustomType1() {
      return this._customType1;
   }

   public final int getCustomType2() {
      return this._customType2;
   }

   public final int getMana() {
      return this._mana;
   }

   public final int getAgathionEnergy() {
      return this._agathionEnergy;
   }

   public int getAttackElementType() {
      return this._elemAtkType;
   }

   public int getAttackElementPower() {
      return this._elemAtkPower;
   }

   public int getElementDefAttr(byte i) {
      return this._elemDefAttr[i];
   }

   public int[] getEnchantOptions() {
      return this._enchantOptions;
   }

   public int getTime() {
      return this._time;
   }

   @Override
   public String toString() {
      return this._item.toString();
   }
}
