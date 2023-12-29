package l2e.gameserver.model.items.itemauction;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.ItemAuctionManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ItemAuctionInstance {
   protected static final Logger _log = Logger.getLogger(ItemAuctionInstance.class.getName());
   private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss dd.MM.yy");
   private static final long START_TIME_SPACE = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.MINUTES);
   private static final long FINISH_TIME_SPACE = TimeUnit.MILLISECONDS.convert(10L, TimeUnit.MINUTES);
   private static final String SELECT_AUCTION_ID_BY_INSTANCE_ID = "SELECT auctionId FROM item_auction WHERE instanceId = ?";
   private static final String SELECT_AUCTION_INFO = "SELECT auctionItemId, startingTime, endingTime, auctionStateId FROM item_auction WHERE auctionId = ? ";
   private static final String DELETE_AUCTION_INFO_BY_AUCTION_ID = "DELETE FROM item_auction WHERE auctionId = ?";
   private static final String DELETE_AUCTION_BID_INFO_BY_AUCTION_ID = "DELETE FROM item_auction_bid WHERE auctionId = ?";
   private static final String SELECT_PLAYERS_ID_BY_AUCTION_ID = "SELECT playerObjId, playerBid FROM item_auction_bid WHERE auctionId = ?";
   private static final Comparator<ItemAuction> itemAuctionComparator = new Comparator<ItemAuction>() {
      public final int compare(ItemAuction o1, ItemAuction o2) {
         return Long.valueOf(o2.getStartingTime()).compareTo(o1.getStartingTime());
      }
   };
   private final int _instanceId;
   private final AtomicInteger _auctionIds;
   private final TIntObjectHashMap<ItemAuction> _auctions;
   private final ArrayList<AuctionItem> _items;
   private final AuctionDateGenerator _dateGenerator;
   private ItemAuction _currentAuction;
   private ItemAuction _nextAuction;
   private ScheduledFuture<?> _stateTask;

   public ItemAuctionInstance(int instanceId, AtomicInteger auctionIds, Node node) throws Exception {
      this._instanceId = instanceId;
      this._auctionIds = auctionIds;
      this._auctions = new TIntObjectHashMap<>();
      this._items = new ArrayList<>();
      NamedNodeMap nanode = node.getAttributes();
      StatsSet generatorConfig = new StatsSet();
      int i = nanode.getLength();

      while(i-- > 0) {
         Node n = nanode.item(i);
         if (n != null) {
            generatorConfig.set(n.getNodeName(), n.getNodeValue());
         }
      }

      this._dateGenerator = new AuctionDateGenerator(generatorConfig);

      for(Node na = node.getFirstChild(); na != null; na = na.getNextSibling()) {
         try {
            if ("item".equalsIgnoreCase(na.getNodeName())) {
               NamedNodeMap naa = na.getAttributes();
               int auctionItemId = Integer.parseInt(naa.getNamedItem("auctionItemId").getNodeValue());
               int auctionLenght = Integer.parseInt(naa.getNamedItem("auctionLenght").getNodeValue());
               long auctionInitBid = (long)Integer.parseInt(naa.getNamedItem("auctionInitBid").getNodeValue());
               int itemId = Integer.parseInt(naa.getNamedItem("itemId").getNodeValue());
               int itemCount = Integer.parseInt(naa.getNamedItem("itemCount").getNodeValue());
               if (auctionLenght < 1) {
                  throw new IllegalArgumentException("auctionLenght < 1 for instanceId: " + this._instanceId + ", itemId " + itemId);
               }

               StatsSet itemExtra = new StatsSet();
               AuctionItem item = new AuctionItem(auctionItemId, auctionLenght, auctionInitBid, itemId, (long)itemCount, itemExtra);
               if (!item.checkItemExists()) {
                  throw new IllegalArgumentException("Item with id " + itemId + " not found");
               }

               for(AuctionItem tmp : this._items) {
                  if (tmp.getAuctionItemId() == auctionItemId) {
                     throw new IllegalArgumentException("Dublicated auction item id " + auctionItemId);
                  }
               }

               this._items.add(item);

               for(Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling()) {
                  if ("extra".equalsIgnoreCase(nb.getNodeName())) {
                     NamedNodeMap nab = nb.getAttributes();
                     int ix = nab.getLength();

                     while(ix-- > 0) {
                        Node n = nab.item(ix);
                        if (n != null) {
                           itemExtra.set(n.getNodeName(), n.getNodeValue());
                        }
                     }
                  }
               }
            }
         } catch (IllegalArgumentException var79) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed loading auction item", (Throwable)var79);
         }
      }

      if (this._items.isEmpty()) {
         throw new IllegalArgumentException("No items defined");
      } else {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT auctionId FROM item_auction WHERE instanceId = ?");
         ) {
            ps.setInt(1, this._instanceId);

            try (ResultSet rset = ps.executeQuery()) {
               while(rset.next()) {
                  int auctionId = rset.getInt(1);

                  try {
                     ItemAuction auction = this.loadAuction(auctionId);
                     if (auction != null) {
                        this._auctions.put(auctionId, auction);
                     } else {
                        ItemAuctionManager.deleteAuction(auctionId);
                     }
                  } catch (SQLException var71) {
                     _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed loading auction: " + auctionId, (Throwable)var71);
                  }
               }
            }
         } catch (SQLException var78) {
            _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Failed loading auctions.", (Throwable)var78);
            return;
         }

         if (Config.DEBUG) {
            _log.log(
               Level.INFO,
               this.getClass().getSimpleName()
                  + ": Loaded "
                  + this._items.size()
                  + " item(s) and registered "
                  + this._auctions.size()
                  + " auction(s) for instance "
                  + this._instanceId
                  + "."
            );
         }

         this.checkAndSetCurrentAndNextAuction();
      }
   }

   public final ItemAuction getCurrentAuction() {
      return this._currentAuction;
   }

   public final ItemAuction getNextAuction() {
      return this._nextAuction;
   }

   public final void shutdown() {
      ScheduledFuture<?> stateTask = this._stateTask;
      if (stateTask != null) {
         stateTask.cancel(false);
      }
   }

   private final AuctionItem getAuctionItem(int auctionItemId) {
      int i = this._items.size();

      while(i-- > 0) {
         AuctionItem item = this._items.get(i);
         if (item.getAuctionItemId() == auctionItemId) {
            return item;
         }
      }

      return null;
   }

   final void checkAndSetCurrentAndNextAuction() {
      ItemAuction currentAuction;
      ItemAuction nextAuction;
      ItemAuction[] auctions = this._auctions.values(new ItemAuction[0]);
      currentAuction = null;
      nextAuction = null;
      label69:
      switch(auctions.length) {
         case 0:
            nextAuction = this.createAuction(System.currentTimeMillis() + START_TIME_SPACE);
            break;
         case 1:
            switch(auctions[0].getAuctionState()) {
               case CREATED:
                  if (auctions[0].getStartingTime() < System.currentTimeMillis() + START_TIME_SPACE) {
                     currentAuction = auctions[0];
                     nextAuction = this.createAuction(System.currentTimeMillis() + START_TIME_SPACE);
                  } else {
                     nextAuction = auctions[0];
                  }
                  break label69;
               case STARTED:
                  currentAuction = auctions[0];
                  nextAuction = this.createAuction(Math.max(currentAuction.getEndingTime() + FINISH_TIME_SPACE, System.currentTimeMillis() + START_TIME_SPACE));
                  break label69;
               case FINISHED:
                  currentAuction = auctions[0];
                  nextAuction = this.createAuction(System.currentTimeMillis() + START_TIME_SPACE);
                  break label69;
               default:
                  throw new IllegalArgumentException();
            }
         default:
            Arrays.sort(auctions, itemAuctionComparator);
            long currentTime = System.currentTimeMillis();

            for(ItemAuction auction : auctions) {
               if (auction.getAuctionState() == ItemAuctionState.STARTED) {
                  currentAuction = auction;
                  break;
               }

               if (auction.getStartingTime() <= currentTime) {
                  currentAuction = auction;
                  break;
               }
            }

            for(ItemAuction auction : auctions) {
               if (auction.getStartingTime() > currentTime && currentAuction != auction) {
                  nextAuction = auction;
                  break;
               }
            }

            if (nextAuction == null) {
               nextAuction = this.createAuction(System.currentTimeMillis() + START_TIME_SPACE);
            }
      }

      this._auctions.put(nextAuction.getAuctionId(), nextAuction);
      this._currentAuction = currentAuction;
      this._nextAuction = nextAuction;
      if (currentAuction != null && currentAuction.getAuctionState() != ItemAuctionState.FINISHED) {
         if (currentAuction.getAuctionState() == ItemAuctionState.STARTED) {
            this.setStateTask(
               ThreadPoolManager.getInstance()
                  .schedule(
                     new ItemAuctionInstance.ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getEndingTime() - System.currentTimeMillis(), 0L)
                  )
            );
         } else {
            this.setStateTask(
               ThreadPoolManager.getInstance()
                  .schedule(
                     new ItemAuctionInstance.ScheduleAuctionTask(currentAuction), Math.max(currentAuction.getStartingTime() - System.currentTimeMillis(), 0L)
                  )
            );
         }

         if (Config.DEBUG) {
            _log.log(
               Level.INFO,
               this.getClass().getSimpleName() + ": Schedule current auction " + currentAuction.getAuctionId() + " for instance " + this._instanceId
            );
         }
      } else {
         this.setStateTask(
            ThreadPoolManager.getInstance()
               .schedule(new ItemAuctionInstance.ScheduleAuctionTask(nextAuction), Math.max(nextAuction.getStartingTime() - System.currentTimeMillis(), 0L))
         );
         if (Config.DEBUG) {
            _log.log(
               Level.INFO,
               this.getClass().getSimpleName()
                  + ": Schedule next auction "
                  + nextAuction.getAuctionId()
                  + " on "
                  + this.DATE_FORMAT.format(new Date(nextAuction.getStartingTime()))
                  + " for instance "
                  + this._instanceId
            );
         }
      }
   }

   public final ItemAuction getAuction(int auctionId) {
      return this._auctions.get(auctionId);
   }

   public final ItemAuction[] getAuctionsByBidder(int bidderObjId) {
      ItemAuction[] auctions = this.getAuctions();
      ArrayList<ItemAuction> stack = new ArrayList<>(auctions.length);

      for(ItemAuction auction : this.getAuctions()) {
         if (auction.getAuctionState() != ItemAuctionState.CREATED) {
            ItemAuctionBid bid = auction.getBidFor(bidderObjId);
            if (bid != null) {
               stack.add(auction);
            }
         }
      }

      return stack.toArray(new ItemAuction[stack.size()]);
   }

   public final ItemAuction[] getAuctions() {
      synchronized(this._auctions) {
         return this._auctions.values(new ItemAuction[0]);
      }
   }

   final void onAuctionFinished(ItemAuction auction) {
      if (Config.ALLOW_ITEM_AUCTION_ANNOUNCE) {
         ServerMessage msg = new ServerMessage("ItemAuction.STOP", true);
         msg.add(auction.getAuctionId());
         Announcements.getInstance().announceToAll(msg);
      }

      auction.broadcastToAllBiddersInternal(SystemMessage.getSystemMessage(SystemMessageId.S1_AUCTION_ENDED).addNumber(auction.getAuctionId()));
      ItemAuctionBid bid = auction.getHighestBid();
      if (bid != null) {
         ItemInstance item = auction.createNewItemInstance();
         Player player = bid.getPlayer();
         if (player != null) {
            player.getWarehouse().addWaheHouseItem("ItemAuction", item, null, null);
            player.sendPacket(SystemMessageId.WON_BID_ITEM_CAN_BE_FOUND_IN_WAREHOUSE);
            _log.log(
               Level.INFO,
               this.getClass().getSimpleName()
                  + ": Auction "
                  + auction.getAuctionId()
                  + " has finished. Highest bid by "
                  + player.getName()
                  + " for instance "
                  + this._instanceId
            );
         } else {
            item.setOwnerId(bid.getPlayerObjId());
            item.setItemLocation(ItemInstance.ItemLocation.WAREHOUSE);
            item.updateDatabase();
            World.getInstance().removeObject(item);
            _log.log(
               Level.INFO,
               this.getClass().getSimpleName()
                  + ": Auction "
                  + auction.getAuctionId()
                  + " has finished. Highest bid by "
                  + CharNameHolder.getInstance().getNameById(bid.getPlayerObjId())
                  + " for instance "
                  + this._instanceId
            );
         }

         auction.clearCanceledBids();
      } else {
         _log.log(
            Level.INFO,
            this.getClass().getSimpleName()
               + ": Auction "
               + auction.getAuctionId()
               + " has finished. There have not been any bid for instance "
               + this._instanceId
         );
      }
   }

   final void setStateTask(ScheduledFuture<?> future) {
      ScheduledFuture<?> stateTask = this._stateTask;
      if (stateTask != null) {
         stateTask.cancel(false);
      }

      this._stateTask = future;
   }

   private final ItemAuction createAuction(long after) {
      AuctionItem auctionItem = this._items.get(Rnd.get(this._items.size()));
      long startingTime = this._dateGenerator.nextDate(after);
      long endingTime = startingTime + TimeUnit.MILLISECONDS.convert((long)auctionItem.getAuctionLength(), TimeUnit.MINUTES);
      ItemAuction auction = new ItemAuction(this._auctionIds.getAndIncrement(), this._instanceId, startingTime, endingTime, auctionItem);
      auction.storeMe();
      return auction;
   }

   private final ItemAuction loadAuction(int auctionId) throws SQLException {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         int auctionItemId = 0;
         long startingTime = 0L;
         long endingTime = 0L;
         byte auctionStateId = 0;
         Object auctionState = null;

         try (PreparedStatement ps = con.prepareStatement(
               "SELECT auctionItemId, startingTime, endingTime, auctionStateId FROM item_auction WHERE auctionId = ? "
            )) {
            ps.setInt(1, auctionId);
            Object psx = null;

            try (ResultSet rset = ps.executeQuery()) {
               if (!rset.next()) {
                  _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Auction data not found for auction: " + auctionId);
                  return null;
               }

               auctionItemId = rset.getInt(1);
               startingTime = rset.getLong(2);
               endingTime = rset.getLong(3);
               auctionStateId = rset.getByte(4);
            }
         }

         if (startingTime >= endingTime) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Invalid starting/ending paramaters for auction: " + auctionId);
            return null;
         } else {
            AuctionItem auctionItem = this.getAuctionItem(auctionItemId);
            if (auctionItem == null) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": AuctionItem: " + auctionItemId + ", not found for auction: " + auctionId);
               return null;
            } else {
               ItemAuctionState auctionStatex = ItemAuctionState.stateForStateId(auctionStateId);
               if (auctionStatex == null) {
                  _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Invalid auctionStateId: " + auctionStateId + ", for auction: " + auctionId);
                  return null;
               } else if (auctionStatex == ItemAuctionState.FINISHED
                  && startingTime < System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert((long)Config.ALT_ITEM_AUCTION_EXPIRED_AFTER, TimeUnit.DAYS)) {
                  _log.log(Level.INFO, this.getClass().getSimpleName() + ": Clearing expired auction: " + auctionId);
                  Object ps = null;

                  try (PreparedStatement psx = con.prepareStatement("DELETE FROM item_auction WHERE auctionId = ?")) {
                     psx.setInt(1, auctionId);
                     psx.execute();
                  }

                  ps = null;

                  try (PreparedStatement psx = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId = ?")) {
                     psx.setInt(1, auctionId);
                     psx.execute();
                  }

                  return null;
               } else {
                  ArrayList<ItemAuctionBid> auctionBids = new ArrayList<>();

                  try (PreparedStatement ps = con.prepareStatement("SELECT playerObjId, playerBid FROM item_auction_bid WHERE auctionId = ?")) {
                     ps.setInt(1, auctionId);

                     try (ResultSet rs = ps.executeQuery()) {
                        while(rs.next()) {
                           int playerObjId = rs.getInt(1);
                           long playerBid = rs.getLong(2);
                           ItemAuctionBid bid = new ItemAuctionBid(playerObjId, playerBid);
                           auctionBids.add(bid);
                        }
                     }
                  }

                  return new ItemAuction(auctionId, this._instanceId, startingTime, endingTime, auctionItem, auctionBids, auctionStatex);
               }
            }
         }
      }
   }

   private final class ScheduleAuctionTask implements Runnable {
      private final ItemAuction _auction;

      public ScheduleAuctionTask(ItemAuction auction) {
         this._auction = auction;
      }

      @Override
      public final void run() {
         try {
            this.runImpl();
         } catch (Exception var2) {
            ItemAuctionInstance._log
               .log(Level.SEVERE, this.getClass().getSimpleName() + ": Failed scheduling auction " + this._auction.getAuctionId(), (Throwable)var2);
         }
      }

      private final void runImpl() throws Exception {
         ItemAuctionState state = this._auction.getAuctionState();
         switch(state) {
            case CREATED:
               if (!this._auction.setAuctionState(state, ItemAuctionState.STARTED)) {
                  throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.STARTED.toString() + ", expected: " + state.toString());
               }

               ItemAuctionInstance._log
                  .log(
                     Level.INFO,
                     this.getClass().getSimpleName()
                        + ": Auction "
                        + this._auction.getAuctionId()
                        + " has started for instance "
                        + this._auction.getReflectionId()
                  );
               if (Config.ALLOW_ITEM_AUCTION_ANNOUNCE) {
                  ServerMessage msg = new ServerMessage("ItemAuction.START", true);
                  msg.add(this._auction.getAuctionId());
                  Announcements.getInstance().announceToAll(msg);
               }

               ItemAuctionInstance.this.checkAndSetCurrentAndNextAuction();
               break;
            case STARTED:
               switch(this._auction.getAuctionEndingExtendState()) {
                  case EXTEND_BY_5_MIN:
                     if (this._auction.getScheduledAuctionEndingExtendState() == ItemAuctionExtendState.INITIAL) {
                        this._auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_5_MIN);
                        ItemAuctionInstance.this.setStateTask(
                           ThreadPoolManager.getInstance().schedule(this, Math.max(this._auction.getEndingTime() - System.currentTimeMillis(), 0L))
                        );
                        return;
                     }
                     break;
                  case EXTEND_BY_3_MIN:
                     if (this._auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_3_MIN) {
                        this._auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_3_MIN);
                        ItemAuctionInstance.this.setStateTask(
                           ThreadPoolManager.getInstance().schedule(this, Math.max(this._auction.getEndingTime() - System.currentTimeMillis(), 0L))
                        );
                        return;
                     }
                     break;
                  case EXTEND_BY_CONFIG_PHASE_A:
                     if (this._auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B) {
                        this._auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B);
                        ItemAuctionInstance.this.setStateTask(
                           ThreadPoolManager.getInstance().schedule(this, Math.max(this._auction.getEndingTime() - System.currentTimeMillis(), 0L))
                        );
                        return;
                     }
                     break;
                  case EXTEND_BY_CONFIG_PHASE_B:
                     if (this._auction.getScheduledAuctionEndingExtendState() != ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A) {
                        this._auction.setScheduledAuctionEndingExtendState(ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A);
                        ItemAuctionInstance.this.setStateTask(
                           ThreadPoolManager.getInstance().schedule(this, Math.max(this._auction.getEndingTime() - System.currentTimeMillis(), 0L))
                        );
                        return;
                     }
               }

               if (!this._auction.setAuctionState(state, ItemAuctionState.FINISHED)) {
                  throw new IllegalStateException("Could not set auction state: " + ItemAuctionState.FINISHED.toString() + ", expected: " + state.toString());
               }

               ItemAuctionInstance.this.onAuctionFinished(this._auction);
               ItemAuctionInstance.this.checkAndSetCurrentAndNextAuction();
               break;
            default:
               throw new IllegalStateException("Invalid state: " + state);
         }
      }
   }
}
