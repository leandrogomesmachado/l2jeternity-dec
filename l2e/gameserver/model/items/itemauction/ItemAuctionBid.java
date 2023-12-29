package l2e.gameserver.model.items.itemauction;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public final class ItemAuctionBid {
   private final int _playerObjId;
   private long _lastBid;

   public ItemAuctionBid(int playerObjId, long lastBid) {
      this._playerObjId = playerObjId;
      this._lastBid = lastBid;
   }

   public final int getPlayerObjId() {
      return this._playerObjId;
   }

   public final long getLastBid() {
      return this._lastBid;
   }

   final void setLastBid(long lastBid) {
      this._lastBid = lastBid;
   }

   final void cancelBid() {
      this._lastBid = -1L;
   }

   final boolean isCanceled() {
      return this._lastBid <= 0L;
   }

   final Player getPlayer() {
      return World.getInstance().getPlayer(this._playerObjId);
   }
}
