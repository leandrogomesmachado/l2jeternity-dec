package l2e.gameserver.model.entity.auction;

import l2e.gameserver.model.items.instance.ItemInstance;

public class Auction {
   private final int _auctionId;
   private final int _sellerObjectId;
   private final String _sellerName;
   private final ItemInstance _item;
   private long _countToSell;
   private final int _priceItemId;
   private final long _pricePerItem;
   private final AuctionItemTypes _itemType;
   private final boolean _privateStore;

   public Auction(
      int id,
      int sellerObjectId,
      String sellerName,
      ItemInstance item,
      int priceItemId,
      long pricePerItem,
      long countToSell,
      AuctionItemTypes itemType,
      boolean privateStore
   ) {
      this._auctionId = id;
      this._sellerObjectId = sellerObjectId;
      this._sellerName = sellerName;
      this._item = item;
      this._priceItemId = priceItemId;
      this._pricePerItem = pricePerItem;
      this._countToSell = countToSell;
      this._itemType = itemType;
      this._privateStore = privateStore;
   }

   public int getAuctionId() {
      return this._auctionId;
   }

   public int getSellerObjectId() {
      return this._sellerObjectId;
   }

   public String getSellerName() {
      return this._sellerName;
   }

   public ItemInstance getItem() {
      return this._item;
   }

   public void setCount(long count) {
      this._countToSell = count;
   }

   public long getCountToSell() {
      return this._countToSell;
   }

   public int getPriceItemId() {
      return this._priceItemId;
   }

   public long getPricePerItem() {
      return this._pricePerItem;
   }

   public AuctionItemTypes getItemType() {
      return this._itemType;
   }

   public boolean isPrivateStore() {
      return this._privateStore;
   }
}
