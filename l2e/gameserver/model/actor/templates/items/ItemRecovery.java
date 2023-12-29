package l2e.gameserver.model.actor.templates.items;

public class ItemRecovery {
   private int _charId;
   private int _itemId;
   private int _objectId;
   private long _count;
   private int _enchantLevel;
   private int _augmentationId;
   private String _elementals;
   private long _time;

   public int getCharId() {
      return this._charId;
   }

   public void setCharId(int charId) {
      this._charId = charId;
   }

   public int getItemId() {
      return this._itemId;
   }

   public void setItemId(int itemId) {
      this._itemId = itemId;
   }

   public int getObjectId() {
      return this._objectId;
   }

   public void setObjectId(int objectId) {
      this._objectId = objectId;
   }

   public long getCount() {
      return this._count;
   }

   public void setCount(long count) {
      this._count = count;
   }

   public int getEnchantLevel() {
      return this._enchantLevel;
   }

   public void setEnchantLevel(int enchantLevel) {
      this._enchantLevel = enchantLevel;
   }

   public long getTime() {
      return this._time;
   }

   public void setTime(long time) {
      this._time = time;
   }

   public void setAugmentationId(int augmentationId) {
      this._augmentationId = augmentationId;
   }

   public int getAugmentationId() {
      return this._augmentationId;
   }

   public void setElementals(String elementals) {
      this._elementals = elementals;
   }

   public String getElementals() {
      return this._elementals;
   }
}
