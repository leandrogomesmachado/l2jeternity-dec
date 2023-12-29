package l2e.gameserver.model.items.itemauction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.ItemAuctionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.ItemInfo;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class ItemAuction {
   static final Logger _log = Logger.getLogger(ItemAuctionManager.class.getName());
   private static final long ENDING_TIME_EXTEND_5 = TimeUnit.MILLISECONDS.convert(5L, TimeUnit.MINUTES);
   private static final long ENDING_TIME_EXTEND_3 = TimeUnit.MILLISECONDS.convert(3L, TimeUnit.MINUTES);
   private final int _auctionId;
   private final int _instanceId;
   private final long _startingTime;
   private volatile long _endingTime;
   private final AuctionItem _auctionItem;
   private final ArrayList<ItemAuctionBid> _auctionBids;
   private final Object _auctionStateLock;
   private volatile ItemAuctionState _auctionState;
   private volatile ItemAuctionExtendState _scheduledAuctionEndingExtendState;
   private volatile ItemAuctionExtendState _auctionEndingExtendState;
   private final ItemInfo _itemInfo;
   private ItemAuctionBid _highestBid;
   private int _lastBidPlayerObjId;
   private static final String DELETE_ITEM_AUCTION_BID = "DELETE FROM item_auction_bid WHERE auctionId = ? AND playerObjId = ?";
   private static final String INSERT_ITEM_AUCTION_BID = "INSERT INTO item_auction_bid (auctionId, playerObjId, playerBid) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playerBid = ?";

   public ItemAuction(int auctionId, int instanceId, long startingTime, long endingTime, AuctionItem auctionItem) {
      this(auctionId, instanceId, startingTime, endingTime, auctionItem, new ArrayList<>(), ItemAuctionState.CREATED);
   }

   public ItemAuction(
      int auctionId,
      int instanceId,
      long startingTime,
      long endingTime,
      AuctionItem auctionItem,
      ArrayList<ItemAuctionBid> auctionBids,
      ItemAuctionState auctionState
   ) {
      this._auctionId = auctionId;
      this._instanceId = instanceId;
      this._startingTime = startingTime;
      this._endingTime = endingTime;
      this._auctionItem = auctionItem;
      this._auctionBids = auctionBids;
      this._auctionState = auctionState;
      this._auctionStateLock = new Object();
      this._scheduledAuctionEndingExtendState = ItemAuctionExtendState.INITIAL;
      this._auctionEndingExtendState = ItemAuctionExtendState.INITIAL;
      ItemInstance item = this._auctionItem.createNewItemInstance();
      this._itemInfo = new ItemInfo(item);
      World.getInstance().removeObject(item);

      for(ItemAuctionBid bid : this._auctionBids) {
         if (this._highestBid == null || this._highestBid.getLastBid() < bid.getLastBid()) {
            this._highestBid = bid;
         }
      }
   }

   public final ItemAuctionState getAuctionState() {
      synchronized(this._auctionStateLock) {
         return this._auctionState;
      }
   }

   public final boolean setAuctionState(ItemAuctionState expected, ItemAuctionState wanted) {
      synchronized(this._auctionStateLock) {
         if (this._auctionState != expected) {
            return false;
         } else {
            this._auctionState = wanted;
            this.storeMe();
            return true;
         }
      }
   }

   public final int getAuctionId() {
      return this._auctionId;
   }

   public final int getReflectionId() {
      return this._instanceId;
   }

   public final ItemInfo getItemInfo() {
      return this._itemInfo;
   }

   public final ItemInstance createNewItemInstance() {
      return this._auctionItem.createNewItemInstance();
   }

   public final long getAuctionInitBid() {
      return this._auctionItem.getAuctionInitBid();
   }

   public final ItemAuctionBid getHighestBid() {
      return this._highestBid;
   }

   public final ItemAuctionExtendState getAuctionEndingExtendState() {
      return this._auctionEndingExtendState;
   }

   public final ItemAuctionExtendState getScheduledAuctionEndingExtendState() {
      return this._scheduledAuctionEndingExtendState;
   }

   public final void setScheduledAuctionEndingExtendState(ItemAuctionExtendState state) {
      this._scheduledAuctionEndingExtendState = state;
   }

   public final long getStartingTime() {
      return this._startingTime;
   }

   public final long getEndingTime() {
      return this._endingTime;
   }

   public final long getStartingTimeRemaining() {
      return Math.max(this.getEndingTime() - System.currentTimeMillis(), 0L);
   }

   public final long getFinishingTimeRemaining() {
      return Math.max(this.getEndingTime() - System.currentTimeMillis(), 0L);
   }

   public final void storeMe() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO item_auction (auctionId,instanceId,auctionItemId,startingTime,endingTime,auctionStateId) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE auctionStateId=?"
         );
      ) {
         statement.setInt(1, this._auctionId);
         statement.setInt(2, this._instanceId);
         statement.setInt(3, this._auctionItem.getAuctionItemId());
         statement.setLong(4, this._startingTime);
         statement.setLong(5, this._endingTime);
         statement.setByte(6, this._auctionState.getStateId());
         statement.setByte(7, this._auctionState.getStateId());
         statement.execute();
      } catch (SQLException var33) {
         _log.log(Level.WARNING, "", (Throwable)var33);
      }
   }

   public final int getAndSetLastBidPlayerObjectId(int playerObjId) {
      int lastBid = this._lastBidPlayerObjId;
      this._lastBidPlayerObjId = playerObjId;
      return lastBid;
   }

   private final void updatePlayerBid(ItemAuctionBid bid, boolean delete) {
      this.updatePlayerBidInternal(bid, delete);
   }

   final void updatePlayerBidInternal(ItemAuctionBid bid, boolean delete) {
      String query = delete
         ? "DELETE FROM item_auction_bid WHERE auctionId = ? AND playerObjId = ?"
         : "INSERT INTO item_auction_bid (auctionId, playerObjId, playerBid) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playerBid = ?";

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(query);
      ) {
         ps.setInt(1, this._auctionId);
         ps.setInt(2, bid.getPlayerObjId());
         if (!delete) {
            ps.setLong(3, bid.getLastBid());
            ps.setLong(4, bid.getLastBid());
         }

         ps.execute();
      } catch (SQLException var36) {
         _log.log(Level.WARNING, "", (Throwable)var36);
      }
   }

   public final void registerBid(Player player, long newBid) {
      if (player == null) {
         throw new NullPointerException();
      } else if (newBid < this.getAuctionInitBid()) {
         player.sendPacket(SystemMessageId.BID_PRICE_MUST_BE_HIGHER);
      } else if (newBid > 100000000000L) {
         player.sendPacket(SystemMessageId.BID_CANT_EXCEED_100_BILLION);
      } else if (this.getAuctionState() == ItemAuctionState.STARTED) {
         int playerObjId = player.getObjectId();
         synchronized(this._auctionBids) {
            if (this._highestBid != null && newBid < this._highestBid.getLastBid()) {
               player.sendPacket(SystemMessageId.BID_MUST_BE_HIGHER_THAN_CURRENT_BID);
            } else {
               ItemAuctionBid bid = this.getBidFor(playerObjId);
               if (bid == null) {
                  if (!this.reduceItemCount(player, newBid)) {
                     player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID);
                     return;
                  }

                  bid = new ItemAuctionBid(playerObjId, newBid);
                  this._auctionBids.add(bid);
               } else {
                  if (!bid.isCanceled()) {
                     if (newBid < bid.getLastBid()) {
                        player.sendPacket(SystemMessageId.BID_MUST_BE_HIGHER_THAN_CURRENT_BID);
                        return;
                     }

                     if (!this.reduceItemCount(player, newBid - bid.getLastBid())) {
                        player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID);
                        return;
                     }
                  } else if (!this.reduceItemCount(player, newBid)) {
                     player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID);
                     return;
                  }

                  bid.setLastBid(newBid);
               }

               this.onPlayerBid(player, bid);
               this.updatePlayerBid(bid, false);
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUBMITTED_A_BID_OF_S1);
               sm.addItemNumber(newBid);
               player.sendPacket(sm);
            }
         }
      }
   }

   private final void onPlayerBid(Player player, ItemAuctionBid bid) {
      if (this._highestBid == null) {
         this._highestBid = bid;
      } else if (this._highestBid.getLastBid() < bid.getLastBid()) {
         Player old = this._highestBid.getPlayer();
         if (old != null) {
            old.sendPacket(SystemMessageId.YOU_HAVE_BEEN_OUTBID);
         }

         this._highestBid = bid;
      }

      if (this.getEndingTime() - System.currentTimeMillis() <= 600000L) {
         switch(this._auctionEndingExtendState) {
            case INITIAL:
               this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_5_MIN;
               this._endingTime += ENDING_TIME_EXTEND_5;
               this.broadcastToAllBidders(SystemMessage.getSystemMessage(SystemMessageId.BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_5_MINUTES));
               break;
            case EXTEND_BY_5_MIN:
               if (this.getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId()) {
                  this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_3_MIN;
                  this._endingTime += ENDING_TIME_EXTEND_3;
                  this.broadcastToAllBidders(SystemMessage.getSystemMessage(SystemMessageId.BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_3_MINUTES));
               }
               break;
            case EXTEND_BY_3_MIN:
               if (Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID > 0L && this.getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId()) {
                  this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A;
                  this._endingTime += Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
               }
               break;
            case EXTEND_BY_CONFIG_PHASE_A:
               if (this.getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId()
                  && this._scheduledAuctionEndingExtendState == ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B) {
                  this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B;
                  this._endingTime += Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
               }
               break;
            case EXTEND_BY_CONFIG_PHASE_B:
               if (this.getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId()
                  && this._scheduledAuctionEndingExtendState == ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A) {
                  this._endingTime += Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
                  this._auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A;
               }
         }
      }
   }

   public final void broadcastToAllBidders(final GameServerPacket packet) {
      ThreadPoolManager.getInstance().execute(new Runnable() {
         @Override
         public final void run() {
            ItemAuction.this.broadcastToAllBiddersInternal(packet);
         }
      });
   }

   public final void broadcastToAllBiddersInternal(GameServerPacket packet) {
      int i = this._auctionBids.size();

      while(i-- > 0) {
         ItemAuctionBid bid = this._auctionBids.get(i);
         if (bid != null) {
            Player player = bid.getPlayer();
            if (player != null) {
               player.sendPacket(packet);
            }
         }
      }
   }

   public final boolean cancelBid(Player player) {
      if (player == null) {
         throw new NullPointerException();
      } else {
         switch(this.getAuctionState()) {
            case CREATED:
               return false;
            case FINISHED:
               if (this._startingTime < System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert((long)Config.ALT_ITEM_AUCTION_EXPIRED_AFTER, TimeUnit.DAYS)
                  )
                {
                  return false;
               }
            default:
               int playerObjId = player.getObjectId();
               synchronized(this._auctionBids) {
                  if (this._highestBid == null) {
                     return false;
                  } else {
                     int bidIndex = this.getBidIndexFor(playerObjId);
                     if (bidIndex == -1) {
                        return false;
                     } else {
                        ItemAuctionBid bid = this._auctionBids.get(bidIndex);
                        if (bid.getPlayerObjId() == this._highestBid.getPlayerObjId()) {
                           if (this.getAuctionState() == ItemAuctionState.FINISHED) {
                              return false;
                           } else {
                              player.sendPacket(SystemMessageId.HIGHEST_BID_BUT_RESERVE_NOT_MET);
                              return true;
                           }
                        } else if (bid.isCanceled()) {
                           return false;
                        } else {
                           this.increaseItemCount(player, bid.getLastBid());
                           bid.cancelBid();
                           this.updatePlayerBid(bid, this.getAuctionState() == ItemAuctionState.FINISHED);
                           player.sendPacket(SystemMessageId.CANCELED_BID);
                           return true;
                        }
                     }
                  }
               }
         }
      }
   }

   public final void clearCanceledBids() {
      if (this.getAuctionState() != ItemAuctionState.FINISHED) {
         throw new IllegalStateException("Attempt to clear canceled bids for non-finished auction");
      } else {
         synchronized(this._auctionBids) {
            for(ItemAuctionBid bid : this._auctionBids) {
               if (bid != null && bid.isCanceled()) {
                  this.updatePlayerBid(bid, true);
               }
            }
         }
      }
   }

   private final boolean reduceItemCount(Player player, long count) {
      if (!player.reduceAdena("ItemAuction", count, player, true)) {
         player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID);
         return false;
      } else {
         return true;
      }
   }

   private final void increaseItemCount(Player player, long count) {
      player.addAdena("ItemAuction", count, player, true);
   }

   public final long getLastBid(Player player) {
      ItemAuctionBid bid = this.getBidFor(player.getObjectId());
      return bid != null ? bid.getLastBid() : -1L;
   }

   public final ItemAuctionBid getBidFor(int playerObjId) {
      int index = this.getBidIndexFor(playerObjId);
      return index != -1 ? this._auctionBids.get(index) : null;
   }

   private final int getBidIndexFor(int playerObjId) {
      int i = this._auctionBids.size();

      while(i-- > 0) {
         ItemAuctionBid bid = this._auctionBids.get(i);
         if (bid != null && bid.getPlayerObjId() == playerObjId) {
            return i;
         }
      }

      return -1;
   }
}
