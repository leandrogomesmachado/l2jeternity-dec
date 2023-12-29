package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.AuctionManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;

public class Auction {
   protected static final Logger _log = Logger.getLogger(Auction.class.getName());
   private int _id = 0;
   private long _endDate;
   private int _highestBidderId = 0;
   private String _highestBidderName = "";
   private long _highestBidderMaxBid = 0L;
   private int _itemId = 0;
   private int _itemObjectId = 0;
   private final long _itemQuantity = 0L;
   private String _itemType = "";
   private int _sellerId = 0;
   private String _sellerClanName = "";
   private String _sellerName = "";
   private long _currentBid = 0L;
   private long _startingBid = 0L;
   private final Map<Integer, Auction.Bidder> _bidders = new ConcurrentHashMap<>();
   private static final String[] ItemTypeName = new String[]{"ClanHall"};

   public Auction(int auctionId) {
      this._id = auctionId;
      this.load();
      this.startAutoTask();
   }

   public Auction(int itemId, Clan Clan, long delay, long bid) {
      this._id = itemId;
      this._endDate = System.currentTimeMillis() + delay;
      this._itemId = itemId;
      this._itemType = "ClanHall";
      this._sellerId = Clan.getLeaderId();
      this._sellerName = Clan.getLeaderName();
      this._sellerClanName = Clan.getName();
      this._startingBid = bid;
   }

   private void load() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("Select * from auction where id = ?");
      ) {
         statement.setInt(1, this.getId());

         try (ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
               this._currentBid = rs.getLong("currentBid");
               this._endDate = rs.getLong("endDate");
               this._itemId = rs.getInt("itemId");
               this._itemObjectId = rs.getInt("itemObjectId");
               this._itemType = rs.getString("itemType");
               this._sellerId = rs.getInt("sellerId");
               this._sellerClanName = rs.getString("sellerClanName");
               this._sellerName = rs.getString("sellerName");
               this._startingBid = rs.getLong("startingBid");
            }
         }

         this.loadBid();
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Exception: Auction.load(): " + var59.getMessage(), (Throwable)var59);
      }
   }

   private void loadBid() {
      this._highestBidderId = 0;
      this._highestBidderName = "";
      this._highestBidderMaxBid = 0L;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC"
         );
      ) {
         statement.setInt(1, this.getId());

         try (ResultSet rs = statement.executeQuery()) {
            for(;
               rs.next();
               this._bidders
                  .put(
                     rs.getInt("bidderId"),
                     new Auction.Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getLong("maxBid"), rs.getLong("time_bid"))
                  )
            ) {
               if (rs.isFirst()) {
                  this._highestBidderId = rs.getInt("bidderId");
                  this._highestBidderName = rs.getString("bidderName");
                  this._highestBidderMaxBid = rs.getLong("maxBid");
               }
            }
         }
      } catch (Exception var59) {
         _log.log(Level.WARNING, "Exception: Auction.loadBid(): " + var59.getMessage(), (Throwable)var59);
      }
   }

   private void startAutoTask() {
      long currentTime = System.currentTimeMillis();
      long taskDelay = 0L;
      if (this._endDate <= currentTime) {
         this._endDate = currentTime + 604800000L;
         this.saveAuctionDate();
      } else {
         taskDelay = this._endDate - currentTime;
      }

      ThreadPoolManager.getInstance().schedule(new Auction.AutoEndTask(), taskDelay);
   }

   public static String getItemTypeName(Auction.ItemTypeEnum value) {
      return ItemTypeName[value.ordinal()];
   }

   private void saveAuctionDate() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("Update auction set endDate = ? where id = ?");
      ) {
         statement.setLong(1, this._endDate);
         statement.setInt(2, this._id);
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.SEVERE, "Exception: saveAuctionDate(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   public synchronized void setBid(Player bidder, long bid) {
      long requiredAdena = bid;
      if (this.getHighestBidderName().equals(bidder.getClan().getLeaderName())) {
         requiredAdena = bid - this.getHighestBidderMaxBid();
      }

      if ((this.getHighestBidderId() > 0 && bid > this.getHighestBidderMaxBid() || this.getHighestBidderId() == 0 && bid >= this.getStartingBid())
         && this.takeItem(bidder, requiredAdena)) {
         this.updateInDB(bidder, bid);
         bidder.getClan().setAuctionBiddedAt(this._id, true);
      } else {
         if (bid < this.getStartingBid() || bid <= this.getHighestBidderMaxBid()) {
            bidder.sendPacket(SystemMessageId.BID_PRICE_MUST_BE_HIGHER);
         }
      }
   }

   private void returnItem(String Clan, long quantity, boolean penalty) {
      if (penalty) {
         quantity = (long)((double)quantity * 0.9);
      }

      long limit = PcInventory.MAX_ADENA - ClanHolder.getInstance().getClanByName(Clan).getWarehouse().getAdena();
      quantity = Math.min(quantity, limit);
      ClanHolder.getInstance().getClanByName(Clan).getWarehouse().addItem("Outbidded", 57, quantity, null, null);
   }

   private boolean takeItem(Player bidder, long quantity) {
      if (bidder.getClan() != null && bidder.getClan().getWarehouse().getAdena() >= quantity) {
         bidder.getClan().getWarehouse().destroyItemByItemId("Buy", 57, quantity, bidder, bidder);
         return true;
      } else {
         bidder.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH);
         return false;
      }
   }

   private void updateInDB(Player bidder, long bid) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         if (this.getBidders().get(bidder.getClanId()) != null) {
            try (PreparedStatement statement = con.prepareStatement(
                  "UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?"
               )) {
               statement.setInt(1, bidder.getClanId());
               statement.setString(2, bidder.getClan().getLeaderName());
               statement.setLong(3, bid);
               statement.setLong(4, System.currentTimeMillis());
               statement.setInt(5, this.getId());
               statement.setInt(6, bidder.getClanId());
               statement.execute();
            }
         } else {
            try (PreparedStatement statement = con.prepareStatement(
                  "INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)"
               )) {
               statement.setInt(1, IdFactory.getInstance().getNextId());
               statement.setInt(2, this.getId());
               statement.setInt(3, bidder.getClanId());
               statement.setString(4, bidder.getName());
               statement.setLong(5, bid);
               statement.setString(6, bidder.getClan().getName());
               statement.setLong(7, System.currentTimeMillis());
               statement.execute();
            }

            if (World.getInstance().getPlayer(this._highestBidderName) != null) {
               World.getInstance().getPlayer(this._highestBidderName).sendMessage("You have been out bidded");
            }
         }

         this._highestBidderId = bidder.getClanId();
         this._highestBidderMaxBid = bid;
         this._highestBidderName = bidder.getClan().getLeaderName();
         if (this._bidders.get(this._highestBidderId) == null) {
            this._bidders
               .put(
                  this._highestBidderId,
                  new Auction.Bidder(this._highestBidderName, bidder.getClan().getName(), bid, Calendar.getInstance().getTimeInMillis())
               );
         } else {
            this._bidders.get(this._highestBidderId).setBid(bid);
            this._bidders.get(this._highestBidderId).setTimeBid(Calendar.getInstance().getTimeInMillis());
         }

         bidder.sendPacket(SystemMessageId.BID_IN_CLANHALL_AUCTION);
      } catch (Exception var60) {
         _log.log(Level.SEVERE, "Exception: Auction.updateInDB(Player bidder, int bid): " + var60.getMessage(), (Throwable)var60);
      }
   }

   private void removeBids() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=?");
      ) {
         statement.setInt(1, this.getId());
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + var33.getMessage(), (Throwable)var33);
      }

      for(Auction.Bidder b : this._bidders.values()) {
         if (ClanHolder.getInstance().getClanByName(b.getClanName()).getHideoutId() == 0) {
            this.returnItem(b.getClanName(), b.getBid(), true);
         } else if (World.getInstance().getPlayer(b.getName()) != null) {
            World.getInstance().getPlayer(b.getName()).sendMessage("Congratulation you have won ClanHall!");
         }

         ClanHolder.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
      }

      this._bidders.clear();
   }

   public void deleteAuctionFromDB() {
      AuctionManager.getInstance().getAuctions().remove(this);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM auction WHERE itemId=?");
      ) {
         statement.setInt(1, this._itemId);
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   public void endAuction() {
      if (ClanHallManager.getInstance().loaded()) {
         if (this._highestBidderId == 0 && this._sellerId == 0) {
            this.startAutoTask();
            return;
         }

         if (this._highestBidderId == 0 && this._sellerId > 0) {
            int aucId = AuctionManager.getInstance().getAuctionIndex(this._id);
            AuctionManager.getInstance().getAuctions().remove(aucId);
            return;
         }

         if (this._sellerId > 0) {
            this.returnItem(this._sellerClanName, this._highestBidderMaxBid, true);
            this.returnItem(this._sellerClanName, (long)ClanHallManager.getInstance().getAuctionableHallById(this._itemId).getLease(), false);
         }

         this.deleteAuctionFromDB();
         Clan Clan = ClanHolder.getInstance().getClanByName(this._bidders.get(this._highestBidderId).getClanName());
         this._bidders.remove(this._highestBidderId);
         Clan.setAuctionBiddedAt(0, true);
         this.removeBids();
         ClanHallManager.getInstance().setOwner(this._itemId, Clan);
      } else {
         ThreadPoolManager.getInstance().schedule(new Auction.AutoEndTask(), 3000L);
      }
   }

   public synchronized void cancelBid(int bidder) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?");
      ) {
         statement.setInt(1, this.getId());
         statement.setInt(2, bidder);
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.SEVERE, "Exception: Auction.cancelBid(String bidder): " + var34.getMessage(), (Throwable)var34);
      }

      this.returnItem(this._bidders.get(bidder).getClanName(), this._bidders.get(bidder).getBid(), true);
      ClanHolder.getInstance().getClanByName(this._bidders.get(bidder).getClanName()).setAuctionBiddedAt(0, true);
      this._bidders.clear();
      this.loadBid();
   }

   public void cancelAuction() {
      this.deleteAuctionFromDB();
      this.removeBids();
   }

   public void confirmAuction() {
      AuctionManager.getInstance().getAuctions().add(this);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO auction (id, sellerId, sellerName, sellerClanName, itemType, itemId, itemObjectId, itemQuantity, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?,?,?)"
         );
      ) {
         statement.setInt(1, this.getId());
         statement.setInt(2, this._sellerId);
         statement.setString(3, this._sellerName);
         statement.setString(4, this._sellerClanName);
         statement.setString(5, this._itemType);
         statement.setInt(6, this._itemId);
         statement.setInt(7, this._itemObjectId);
         statement.setLong(8, 0L);
         statement.setLong(9, this._startingBid);
         statement.setLong(10, this._currentBid);
         statement.setLong(11, this._endDate);
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.SEVERE, "Exception: Auction.load(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   public final int getId() {
      return this._id;
   }

   public final long getCurrentBid() {
      return this._currentBid;
   }

   public final long getEndDate() {
      return this._endDate;
   }

   public final int getHighestBidderId() {
      return this._highestBidderId;
   }

   public final String getHighestBidderName() {
      return this._highestBidderName;
   }

   public final long getHighestBidderMaxBid() {
      return this._highestBidderMaxBid;
   }

   public final int getItemId() {
      return this._itemId;
   }

   public final int getObjectId() {
      return this._itemObjectId;
   }

   public final long getItemQuantity() {
      return 0L;
   }

   public final String getItemType() {
      return this._itemType;
   }

   public final int getSellerId() {
      return this._sellerId;
   }

   public final String getSellerName() {
      return this._sellerName;
   }

   public final String getSellerClanName() {
      return this._sellerClanName;
   }

   public final long getStartingBid() {
      return this._startingBid;
   }

   public final Map<Integer, Auction.Bidder> getBidders() {
      return this._bidders;
   }

   public class AutoEndTask implements Runnable {
      @Override
      public void run() {
         try {
            Auction.this.endAuction();
         } catch (Exception var2) {
            Auction._log.log(Level.SEVERE, "", (Throwable)var2);
         }
      }
   }

   public static class Bidder {
      private final String _name;
      private final String _clanName;
      private long _bid;
      private final Calendar _timeBid;

      public Bidder(String name, String clanName, long bid, long timeBid) {
         this._name = name;
         this._clanName = clanName;
         this._bid = bid;
         this._timeBid = Calendar.getInstance();
         this._timeBid.setTimeInMillis(timeBid);
      }

      public String getName() {
         return this._name;
      }

      public String getClanName() {
         return this._clanName;
      }

      public long getBid() {
         return this._bid;
      }

      public Calendar getTimeBid() {
         return this._timeBid;
      }

      public void setTimeBid(long timeBid) {
         this._timeBid.setTimeInMillis(timeBid);
      }

      public void setBid(long bid) {
         this._bid = bid;
      }
   }

   public static enum ItemTypeEnum {
      ClanHall;
   }
}
