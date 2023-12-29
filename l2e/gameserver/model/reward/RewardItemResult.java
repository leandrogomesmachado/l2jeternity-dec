package l2e.gameserver.model.reward;

import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.items.instance.ItemInstance;

public class RewardItemResult {
   private final int _itemId;
   private long _count;
   private boolean _isAdena;

   public RewardItemResult(int itemId) {
      this._itemId = itemId;
      this._count = 1L;
   }

   public RewardItemResult(int itemId, long count) {
      this._itemId = itemId;
      this._count = count;
   }

   public RewardItemResult setCount(long count) {
      this._count = count;
      return this;
   }

   public int getId() {
      return this._itemId;
   }

   public long getCount() {
      return this._count;
   }

   public boolean isAdena() {
      return this._isAdena;
   }

   public void setIsAdena(boolean val) {
      this._isAdena = val;
   }

   public ItemInstance createItem() {
      if (this._count < 1L) {
         return null;
      } else {
         ItemInstance item = ItemsParser.getInstance().createItem(this._itemId);
         if (item != null) {
            item.setCount(this._count);
            return item;
         } else {
            return null;
         }
      }
   }
}
