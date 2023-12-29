package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.entity.Auction;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.clanhall.AuctionableHall;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.type.ClanHallZone;

public final class ClanHallManager {
   protected static final Logger _log = Logger.getLogger(ClanHallManager.class.getName());
   private final Map<Integer, AuctionableHall> _clanHall = new ConcurrentHashMap<>();
   private final Map<Integer, AuctionableHall> _freeClanHall = new ConcurrentHashMap<>();
   private final Map<Integer, AuctionableHall> _allAuctionableClanHalls = new HashMap<>();
   private static Map<Integer, ClanHall> _allClanHalls = new HashMap<>();
   private boolean _loaded = false;

   public static ClanHallManager getInstance() {
      return ClanHallManager.SingletonHolder._instance;
   }

   public boolean loaded() {
      return this._loaded;
   }

   protected ClanHallManager() {
      this.load();
   }

   private final void load() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            StatsSet set = new StatsSet();
            int id = rs.getInt("id");
            int ownerId = rs.getInt("ownerId");
            int lease = rs.getInt("lease");
            set.set("id", id);
            set.set("ownerId", ownerId);
            set.set("lease", lease);
            set.set("paidUntil", rs.getLong("paidUntil"));
            set.set("grade", rs.getInt("Grade"));
            set.set("paid", rs.getBoolean("paid"));
            AuctionableHall ch = new AuctionableHall(set);
            this._allAuctionableClanHalls.put(id, ch);
            addClanHall(ch);
            if (ch.getOwnerId() > 0) {
               this._clanHall.put(id, ch);
            } else {
               this._freeClanHall.put(id, ch);
               Auction auc = AuctionManager.getInstance().getAuction(id);
               if (auc == null && lease > 0) {
                  AuctionManager.getInstance().initNPC(id);
               }
            }
         }

         rs.close();
         statement.close();
         _log.info(
            this.getClass().getSimpleName()
               + ": Loaded: "
               + this.getClanHalls().size()
               + " occupy and "
               + this.getFreeClanHalls().size()
               + " free clan halls."
         );
         this._loaded = true;
      } catch (Exception var21) {
         _log.log(Level.WARNING, "Exception: ClanHallManager.load(): " + var21.getMessage(), (Throwable)var21);
      }
   }

   public static final Map<Integer, ClanHall> getAllClanHalls() {
      return _allClanHalls;
   }

   public final Map<Integer, AuctionableHall> getFreeClanHalls() {
      return this._freeClanHall;
   }

   public final Map<Integer, AuctionableHall> getClanHalls() {
      return this._clanHall;
   }

   public final Map<Integer, AuctionableHall> getAllAuctionableClanHalls() {
      return this._allAuctionableClanHalls;
   }

   public static final void addClanHall(ClanHall hall) {
      _allClanHalls.put(hall.getId(), hall);
   }

   public final boolean isFree(int chId) {
      return this._freeClanHall.containsKey(chId);
   }

   public final synchronized void setFree(int chId) {
      this._freeClanHall.put(chId, this._clanHall.get(chId));
      ClanHolder.getInstance().getClan(this._freeClanHall.get(chId).getOwnerId()).setHideoutId(0);
      this._freeClanHall.get(chId).free();
      this._clanHall.remove(chId);
   }

   public final synchronized void setOwner(int chId, Clan clan) {
      if (!this._clanHall.containsKey(chId)) {
         this._clanHall.put(chId, this._freeClanHall.get(chId));
         this._freeClanHall.remove(chId);
      } else {
         this._clanHall.get(chId).free();
      }

      ClanHolder.getInstance().getClan(clan.getId()).setHideoutId(chId);
      this._clanHall.get(chId).setOwner(clan);
   }

   public final ClanHall getClanHallById(int clanHallId) {
      return _allClanHalls.get(clanHallId);
   }

   public final AuctionableHall getAuctionableHallById(int clanHallId) {
      return this._allAuctionableClanHalls.get(clanHallId);
   }

   public final ClanHall getClanHall(int x, int y, int z) {
      for(ClanHall temp : getAllClanHalls().values()) {
         if (temp.checkIfInZone(x, y, z)) {
            return temp;
         }
      }

      return null;
   }

   public final ClanHall getClanHall(GameObject activeObject) {
      return this.getClanHall(activeObject.getX(), activeObject.getY(), activeObject.getZ());
   }

   public final AuctionableHall getNearbyClanHall(int x, int y, int maxDist) {
      ClanHallZone zone = null;

      for(Entry<Integer, AuctionableHall> ch : this._clanHall.entrySet()) {
         zone = ch.getValue().getZone();
         if (zone != null && zone.getDistanceToZone(x, y) < (double)maxDist) {
            return ch.getValue();
         }
      }

      for(Entry<Integer, AuctionableHall> ch : this._freeClanHall.entrySet()) {
         zone = ch.getValue().getZone();
         if (zone != null && zone.getDistanceToZone(x, y) < (double)maxDist) {
            return ch.getValue();
         }
      }

      return null;
   }

   public final ClanHall getNearbyAbstractHall(int x, int y, int maxDist) {
      ClanHallZone zone = null;

      for(Entry<Integer, ClanHall> ch : _allClanHalls.entrySet()) {
         zone = ch.getValue().getZone();
         if (zone != null && zone.getDistanceToZone(x, y) < (double)maxDist) {
            return ch.getValue();
         }
      }

      return null;
   }

   public final AuctionableHall getClanHallByOwner(Clan clan) {
      for(Entry<Integer, AuctionableHall> ch : this._clanHall.entrySet()) {
         if (clan.getId() == ch.getValue().getOwnerId()) {
            return ch.getValue();
         }
      }

      return null;
   }

   public final ClanHall getAbstractHallByOwner(Clan clan) {
      for(Entry<Integer, AuctionableHall> ch : this._clanHall.entrySet()) {
         if (clan.getId() == ch.getValue().getOwnerId()) {
            return ch.getValue();
         }
      }

      for(Entry<Integer, SiegableHall> ch : CHSiegeManager.getInstance().getConquerableHalls().entrySet()) {
         if (clan.getId() == ch.getValue().getOwnerId()) {
            return ch.getValue();
         }
      }

      return null;
   }

   private static class SingletonHolder {
      protected static final ClanHallManager _instance = new ClanHallManager();
   }
}
