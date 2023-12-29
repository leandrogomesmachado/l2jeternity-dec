package l2e.gameserver.model.items.multisell;

import l2e.gameserver.model.items.instance.ItemInstance;

public class ItemInfo {
   private final int _enchantLevel;
   private final int _augmentId;
   private final int _timeLimit;
   private final byte _elementId;
   private final int _elementPower;
   private final int[] _elementals = new int[6];

   public ItemInfo(ItemInstance item) {
      this._enchantLevel = item.getEnchantLevel();
      this._timeLimit = (int)item.getTime();
      this._augmentId = item.getAugmentation() != null ? item.getAugmentation().getAugmentationId() : 0;
      this._elementId = item.getAttackElementType();
      this._elementPower = item.getAttackElementPower();
      this._elementals[0] = item.getElementDefAttr((byte)0);
      this._elementals[1] = item.getElementDefAttr((byte)1);
      this._elementals[2] = item.getElementDefAttr((byte)2);
      this._elementals[3] = item.getElementDefAttr((byte)3);
      this._elementals[4] = item.getElementDefAttr((byte)4);
      this._elementals[5] = item.getElementDefAttr((byte)5);
   }

   public ItemInfo(int enchantLevel) {
      this._enchantLevel = enchantLevel;
      this._timeLimit = -1;
      this._augmentId = 0;
      this._elementId = -1;
      this._elementPower = 0;
      this._elementals[0] = 0;
      this._elementals[1] = 0;
      this._elementals[2] = 0;
      this._elementals[3] = 0;
      this._elementals[4] = 0;
      this._elementals[5] = 0;
   }

   public final int getEnchantLevel() {
      return this._enchantLevel;
   }

   public final int getTimeLimit() {
      return this._timeLimit;
   }

   public final int getTime() {
      return (int)(this._timeLimit > 0 ? System.currentTimeMillis() + (long)(this._timeLimit * 60) * 1000L : -1L);
   }

   public final int getAugmentId() {
      return this._augmentId;
   }

   public final byte getElementId() {
      return this._elementId;
   }

   public final int getElementPower() {
      return this._elementPower;
   }

   public final int[] getElementals() {
      return this._elementals;
   }
}
