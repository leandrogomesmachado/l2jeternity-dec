package l2e.gameserver.model.items;

public class ItemRequest {
   int _objectId;
   int _itemId;
   long _count;
   long _price;

   public ItemRequest(int objectId, long count, long price) {
      this._objectId = objectId;
      this._count = count;
      this._price = price;
   }

   public ItemRequest(int objectId, int itemId, long count, long price) {
      this._objectId = objectId;
      this._itemId = itemId;
      this._count = count;
      this._price = price;
   }

   public int getObjectId() {
      return this._objectId;
   }

   public int getId() {
      return this._itemId;
   }

   public void setCount(long count) {
      this._count = count;
   }

   public long getCount() {
      return this._count;
   }

   public long getPrice() {
      return this._price;
   }

   @Override
   public int hashCode() {
      return this._objectId;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ItemRequest)) {
         return false;
      } else {
         return this._objectId != ((ItemRequest)obj)._objectId;
      }
   }
}
