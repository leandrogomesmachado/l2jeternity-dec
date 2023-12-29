package l2e.gameserver.model.items.itemauction;

import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.StatsSet;

public final class AuctionItem {
   private final int _auctionItemId;
   private final int _auctionLength;
   private final long _auctionInitBid;
   private final int _itemId;
   private final long _itemCount;
   private final StatsSet _itemExtra;

   public AuctionItem(int auctionItemId, int auctionLength, long auctionInitBid, int itemId, long itemCount, StatsSet itemExtra) {
      this._auctionItemId = auctionItemId;
      this._auctionLength = auctionLength;
      this._auctionInitBid = auctionInitBid;
      this._itemId = itemId;
      this._itemCount = itemCount;
      this._itemExtra = itemExtra;
   }

   public final boolean checkItemExists() {
      Item item = ItemsParser.getInstance().getTemplate(this._itemId);
      return item != null;
   }

   public final int getAuctionItemId() {
      return this._auctionItemId;
   }

   public final int getAuctionLength() {
      return this._auctionLength;
   }

   public final long getAuctionInitBid() {
      return this._auctionInitBid;
   }

   public final int getId() {
      return this._itemId;
   }

   public final long getCount() {
      return this._itemCount;
   }

   public final ItemInstance createNewItemInstance() {
      ItemInstance item = ItemsParser.getInstance().createItem("ItemAuction", this._itemId, this._itemCount, null, null);
      item.setEnchantLevel(item.getDefaultEnchantLevel());
      int augmentationId = this._itemExtra.getInteger("augmentation_id", 0);
      if (augmentationId > 0) {
         item.setAugmentation(new Augmentation(augmentationId));
      }

      return item;
   }
}
