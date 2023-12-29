package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.entity.Auction;

public final class AuctionManager {
   protected static final Logger _log = Logger.getLogger(AuctionManager.class.getName());
   private final List<Auction> _auctions = new ArrayList<>();
   private static final String[] ITEM_INIT_DATA = new String[]{
      "(22, 0, 'NPC', 'NPC Clan', 'ClanHall', 22, 0, 'Moonstone Hall', 1, 20000000, 0, 1073037600000)",
      "(23, 0, 'NPC', 'NPC Clan', 'ClanHall', 23, 0, 'Onyx Hall', 1, 20000000, 0, 1073037600000)",
      "(24, 0, 'NPC', 'NPC Clan', 'ClanHall', 24, 0, 'Topaz Hall', 1, 20000000, 0, 1073037600000)",
      "(25, 0, 'NPC', 'NPC Clan', 'ClanHall', 25, 0, 'Ruby Hall', 1, 20000000, 0, 1073037600000)",
      "(26, 0, 'NPC', 'NPC Clan', 'ClanHall', 26, 0, 'Crystal Hall', 1, 20000000, 0, 1073037600000)",
      "(27, 0, 'NPC', 'NPC Clan', 'ClanHall', 27, 0, 'Onyx Hall', 1, 20000000, 0, 1073037600000)",
      "(28, 0, 'NPC', 'NPC Clan', 'ClanHall', 28, 0, 'Sapphire Hall', 1, 20000000, 0, 1073037600000)",
      "(29, 0, 'NPC', 'NPC Clan', 'ClanHall', 29, 0, 'Moonstone Hall', 1, 20000000, 0, 1073037600000)",
      "(30, 0, 'NPC', 'NPC Clan', 'ClanHall', 30, 0, 'Emerald Hall', 1, 20000000, 0, 1073037600000)",
      "(31, 0, 'NPC', 'NPC Clan', 'ClanHall', 31, 0, 'The Atramental Barracks', 1, 8000000, 0, 1073037600000)",
      "(32, 0, 'NPC', 'NPC Clan', 'ClanHall', 32, 0, 'The Scarlet Barracks', 1, 8000000, 0, 1073037600000)",
      "(33, 0, 'NPC', 'NPC Clan', 'ClanHall', 33, 0, 'The Viridian Barracks', 1, 8000000, 0, 1073037600000)",
      "(36, 0, 'NPC', 'NPC Clan', 'ClanHall', 36, 0, 'The Golden Chamber', 1, 50000000, 0, 1106827200000)",
      "(37, 0, 'NPC', 'NPC Clan', 'ClanHall', 37, 0, 'The Silver Chamber', 1, 50000000, 0, 1106827200000)",
      "(38, 0, 'NPC', 'NPC Clan', 'ClanHall', 38, 0, 'The Mithril Chamber', 1, 50000000, 0, 1106827200000)",
      "(39, 0, 'NPC', 'NPC Clan', 'ClanHall', 39, 0, 'Silver Manor', 1, 50000000, 0, 1106827200000)",
      "(40, 0, 'NPC', 'NPC Clan', 'ClanHall', 40, 0, 'Gold Manor', 1, 50000000, 0, 1106827200000)",
      "(41, 0, 'NPC', 'NPC Clan', 'ClanHall', 41, 0, 'The Bronze Chamber', 1, 50000000, 0, 1106827200000)",
      "(42, 0, 'NPC', 'NPC Clan', 'ClanHall', 42, 0, 'The Golden Chamber', 1, 50000000, 0, 1106827200000)",
      "(43, 0, 'NPC', 'NPC Clan', 'ClanHall', 43, 0, 'The Silver Chamber', 1, 50000000, 0, 1106827200000)",
      "(44, 0, 'NPC', 'NPC Clan', 'ClanHall', 44, 0, 'The Mithril Chamber', 1, 50000000, 0, 1106827200000)",
      "(45, 0, 'NPC', 'NPC Clan', 'ClanHall', 45, 0, 'The Bronze Chamber', 1, 50000000, 0, 1106827200000)",
      "(46, 0, 'NPC', 'NPC Clan', 'ClanHall', 46, 0, 'Silver Manor', 1, 50000000, 0, 1106827200000)",
      "(47, 0, 'NPC', 'NPC Clan', 'ClanHall', 47, 0, 'Moonstone Hall', 1, 50000000, 0, 1106827200000)",
      "(48, 0, 'NPC', 'NPC Clan', 'ClanHall', 48, 0, 'Onyx Hall', 1, 50000000, 0, 1106827200000)",
      "(49, 0, 'NPC', 'NPC Clan', 'ClanHall', 49, 0, 'Emerald Hall', 1, 50000000, 0, 1106827200000)",
      "(50, 0, 'NPC', 'NPC Clan', 'ClanHall', 50, 0, 'Sapphire Hall', 1, 50000000, 0, 1106827200000)",
      "(51, 0, 'NPC', 'NPC Clan', 'ClanHall', 51, 0, 'Mont Chamber', 1, 50000000, 0, 1106827200000)",
      "(52, 0, 'NPC', 'NPC Clan', 'ClanHall', 52, 0, 'Astaire Chamber', 1, 50000000, 0, 1106827200000)",
      "(53, 0, 'NPC', 'NPC Clan', 'ClanHall', 53, 0, 'Aria Chamber', 1, 50000000, 0, 1106827200000)",
      "(54, 0, 'NPC', 'NPC Clan', 'ClanHall', 54, 0, 'Yiana Chamber', 1, 50000000, 0, 1106827200000)",
      "(55, 0, 'NPC', 'NPC Clan', 'ClanHall', 55, 0, 'Roien Chamber', 1, 50000000, 0, 1106827200000)",
      "(56, 0, 'NPC', 'NPC Clan', 'ClanHall', 56, 0, 'Luna Chamber', 1, 50000000, 0, 1106827200000)",
      "(57, 0, 'NPC', 'NPC Clan', 'ClanHall', 57, 0, 'Traban Chamber', 1, 50000000, 0, 1106827200000)",
      "(58, 0, 'NPC', 'NPC Clan', 'ClanHall', 58, 0, 'Eisen Hall', 1, 20000000, 0, 1106827200000)",
      "(59, 0, 'NPC', 'NPC Clan', 'ClanHall', 59, 0, 'Heavy Metal Hall', 1, 20000000, 0, 1106827200000)",
      "(60, 0, 'NPC', 'NPC Clan', 'ClanHall', 60, 0, 'Molten Ore Hall', 1, 20000000, 0, 1106827200000)",
      "(61, 0, 'NPC', 'NPC Clan', 'ClanHall', 61, 0, 'Titan Hall', 1, 20000000, 0, 1106827200000)"
   };
   private static final int[] ItemInitDataId = new int[]{
      22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61
   };

   public static final AuctionManager getInstance() {
      return AuctionManager.SingletonHolder._instance;
   }

   protected AuctionManager() {
      this.load();
   }

   public void reload() {
      this._auctions.clear();
      this.load();
   }

   private final void load() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT id FROM auction ORDER BY id");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this._auctions.add(new Auction(rs.getInt("id")));
         }

         rs.close();
         statement.close();
         _log.info(this.getClass().getSimpleName() + ": Loaded: " + this.getAuctions().size() + " auction(s)");
      } catch (Exception var15) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Exception: AuctionManager.load(): " + var15.getMessage(), (Throwable)var15);
      }
   }

   public final Auction getAuction(int auctionId) {
      int index = this.getAuctionIndex(auctionId);
      return index >= 0 ? this.getAuctions().get(index) : null;
   }

   public final int getAuctionIndex(int auctionId) {
      for(int i = 0; i < this.getAuctions().size(); ++i) {
         Auction auction = this.getAuctions().get(i);
         if (auction != null && auction.getId() == auctionId) {
            return i;
         }
      }

      return -1;
   }

   public final List<Auction> getAuctions() {
      return this._auctions;
   }

   public void initNPC(int id) {
      int i = 0;

      while(i < ItemInitDataId.length && ItemInitDataId[i] != id) {
         ++i;
      }

      if (i < ItemInitDataId.length && ItemInitDataId[i] == id) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            Statement s = con.createStatement();
         ) {
            s.executeUpdate("INSERT INTO `auction` VALUES " + ITEM_INIT_DATA[i]);
            this._auctions.add(new Auction(id));
            _log.info(this.getClass().getSimpleName() + ": Created auction for ClanHall: " + id);
         } catch (Exception var35) {
            _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Exception: Auction.initNPC(): " + var35.getMessage(), (Throwable)var35);
         }
      } else {
         _log.warning(this.getClass().getSimpleName() + ": Clan Hall auction not found for Id :" + id);
      }
   }

   private static class SingletonHolder {
      protected static final AuctionManager _instance = new AuctionManager();
   }
}
