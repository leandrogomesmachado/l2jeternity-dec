package l2e.gameserver.model;

import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;

public class TradeItem {
   private int _objectId;
   private final Item _item;
   private final int _location;
   private int _enchant;
   private final int _type1;
   private final int _type2;
   private long _count;
   private long _storeCount;
   private long _price;
   private byte _elemAtkType;
   private int _elemAtkPower;
   private int[] _elemDefAttr = new int[]{0, 0, 0, 0, 0, 0};
   private final int[] _enchantOptions;
   private int _auctionId;

   public TradeItem(ItemInstance item, long count, long price) {
      this._objectId = item.getObjectId();
      this._item = item.getItem();
      this._location = item.getLocationSlot();
      this._enchant = item.getEnchantLevel();
      this._type1 = item.getCustomType1();
      this._type2 = item.getCustomType2();
      this._count = count;
      this._price = price;
      this._elemAtkType = item.getAttackElementType();
      this._elemAtkPower = item.getAttackElementPower();

      for(byte i = 0; i < 6; ++i) {
         this._elemDefAttr[i] = item.getElementDefAttr(i);
      }

      this._enchantOptions = item.getEnchantOptions();
   }

   public TradeItem(Item item, int enchant, long count, long price, int elemAtkType, int elemAtkPower, int[] elemDefAttr) {
      this._objectId = 0;
      this._item = item;
      this._location = 0;
      this._enchant = enchant;
      this._type1 = 0;
      this._type2 = 0;
      this._count = count;
      this._storeCount = count;
      this._price = price;
      this._elemAtkType = (byte)elemAtkType;
      this._elemAtkPower = elemAtkPower;
      this._elemDefAttr = elemDefAttr;
      this._enchantOptions = ItemInstance.DEFAULT_ENCHANT_OPTIONS;
   }

   public TradeItem(TradeItem item, long count, long price) {
      this._objectId = item.getObjectId();
      this._item = item.getItem();
      this._location = item.getLocationSlot();
      this._enchant = item.getEnchant();
      this._type1 = item.getCustomType1();
      this._type2 = item.getCustomType2();
      this._count = count;
      this._storeCount = count;
      this._price = price;
      this._elemAtkType = item.getAttackElementType();
      this._elemAtkPower = item.getAttackElementPower();

      for(byte i = 0; i < 6; ++i) {
         this._elemDefAttr[i] = item.getElementDefAttr(i);
      }

      this._enchantOptions = item.getEnchantOptions();
   }

   public void setObjectId(int objectId) {
      this._objectId = objectId;
   }

   public int getObjectId() {
      return this._objectId;
   }

   public Item getItem() {
      return this._item;
   }

   public int getLocationSlot() {
      return this._location;
   }

   public void setEnchant(int enchant) {
      this._enchant = enchant;
   }

   public int getEnchant() {
      return this._enchant;
   }

   public int getCustomType1() {
      return this._type1;
   }

   public int getCustomType2() {
      return this._type2;
   }

   public void setCount(long count) {
      this._count = count;
   }

   public long getCount() {
      return this._count;
   }

   public long getStoreCount() {
      return this._storeCount;
   }

   public void setPrice(long price) {
      this._price = price;
   }

   public long getPrice() {
      return this._price;
   }

   public void setAttackElementType(byte elemAtkType) {
      this._elemAtkType = elemAtkType;
   }

   public byte getAttackElementType() {
      return this._elemAtkType;
   }

   public void setAttackElementPower(int elemPower) {
      this._elemAtkPower = (byte)elemPower;
   }

   public int getAttackElementPower() {
      return this._elemAtkPower;
   }

   public int getElementDefAttr(byte i) {
      return this._elemDefAttr[i];
   }

   public void setElemDefAttr(int i, int elemDefAttr) {
      this._elemDefAttr[i] = elemDefAttr;
   }

   public int[] getEnchantOptions() {
      return this._enchantOptions;
   }

   public void setAuctionId(int id) {
      this._auctionId = id;
   }

   public int getAuctionId() {
      return this._auctionId;
   }
}
