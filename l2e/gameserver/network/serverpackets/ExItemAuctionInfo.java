package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.items.ItemInfo;
import l2e.gameserver.model.items.itemauction.ItemAuction;
import l2e.gameserver.model.items.itemauction.ItemAuctionBid;
import l2e.gameserver.model.items.itemauction.ItemAuctionState;

public final class ExItemAuctionInfo extends GameServerPacket {
   private final boolean _refresh;
   private final int _timeRemaining;
   private final ItemAuction _currentAuction;
   private final ItemAuction _nextAuction;

   public ExItemAuctionInfo(boolean refresh, ItemAuction currentAuction, ItemAuction nextAuction) {
      if (currentAuction == null) {
         throw new NullPointerException();
      } else {
         if (currentAuction.getAuctionState() != ItemAuctionState.STARTED) {
            this._timeRemaining = 0;
         } else {
            this._timeRemaining = (int)(currentAuction.getFinishingTimeRemaining() / 1000L);
         }

         this._refresh = refresh;
         this._currentAuction = currentAuction;
         this._nextAuction = nextAuction;
      }
   }

   @Override
   protected void writeImpl() {
      this.writeC(this._refresh ? 0 : 1);
      this.writeD(this._currentAuction.getReflectionId());
      ItemAuctionBid highestBid = this._currentAuction.getHighestBid();
      this.writeQ(highestBid != null ? highestBid.getLastBid() : this._currentAuction.getAuctionInitBid());
      this.writeD(this._timeRemaining);
      this.writeItemInfo(this._currentAuction.getItemInfo());
      if (this._nextAuction != null) {
         this.writeQ(this._nextAuction.getAuctionInitBid());
         this.writeD((int)(this._nextAuction.getStartingTime() / 1000L));
         this.writeItemInfo(this._nextAuction.getItemInfo());
      }
   }

   private final void writeItemInfo(ItemInfo item) {
      this.writeD(item.getItem().getId());
      this.writeD(item.getItem().getId());
      this.writeD(item.getLocation());
      this.writeQ(item.getCount());
      this.writeH(item.getItem().getType2());
      this.writeH(item.getCustomType1());
      this.writeH(0);
      this.writeD(item.getItem().getBodyPart());
      this.writeH(item.getEnchant());
      this.writeH(item.getCustomType2());
      this.writeD(item.getAugmentationBonus());
      this.writeD(item.getMana());
      this.writeD(item.getTime());
      this.writeH(item.getAttackElementType());
      this.writeH(item.getAttackElementPower());

      for(byte i = 0; i < 6; ++i) {
         this.writeH(item.getElementDefAttr(i));
      }

      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
   }
}
