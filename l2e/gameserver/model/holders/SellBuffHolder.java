package l2e.gameserver.model.holders;

public final class SellBuffHolder {
   private final int _id;
   private final int _level;
   private int _itemId;
   private long _price;

   public SellBuffHolder(int Id, int lvl, int itemId, long price) {
      this._id = Id;
      this._level = lvl;
      this._itemId = itemId;
      this._price = price;
   }

   public final int getId() {
      return this._id;
   }

   public final int getLvl() {
      return this._level;
   }

   public final void setPrice(long price) {
      this._price = price;
   }

   public final long getPrice() {
      return this._price;
   }

   public final void setItemId(int itemId) {
      this._itemId = itemId;
   }

   public final int getItemId() {
      return this._itemId;
   }
}
