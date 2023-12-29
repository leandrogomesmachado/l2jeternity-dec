package l2e.gameserver.instancemanager;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.items.itemauction.ItemAuctionInstance;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ItemAuctionManager {
   private static final Logger _log = Logger.getLogger(ItemAuctionManager.class.getName());
   private final TIntObjectHashMap<ItemAuctionInstance> _managerInstances = new TIntObjectHashMap<>();
   private final AtomicInteger _auctionIds = new AtomicInteger(1);

   public static final ItemAuctionManager getInstance() {
      return ItemAuctionManager.SingletonHolder._instance;
   }

   protected ItemAuctionManager() {
      if (!Config.ALT_ITEM_AUCTION_ENABLED) {
         _log.log(Level.INFO, "ItemAuctionManager: Disabled by config.");
      } else {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            Statement statement = con.createStatement();
            ResultSet rset = statement.executeQuery("SELECT auctionId FROM item_auction ORDER BY auctionId DESC LIMIT 0, 1");
         ) {
            if (rset.next()) {
               this._auctionIds.set(rset.getInt(1) + 1);
            }
         } catch (SQLException var63) {
            _log.log(Level.SEVERE, "ItemAuctionManager: Failed loading auctions.", (Throwable)var63);
         }

         File file = new File(Config.DATAPACK_ROOT + "/data/stats/items/itemAuctions.xml");
         if (!file.exists()) {
            _log.log(Level.WARNING, "ItemAuctionManager: Missing itemAuctions.xml!");
         } else {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);

            try {
               Document doc = factory.newDocumentBuilder().parse(file);

               for(Node na = doc.getFirstChild(); na != null; na = na.getNextSibling()) {
                  if ("list".equalsIgnoreCase(na.getNodeName())) {
                     for(Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling()) {
                        if ("instance".equalsIgnoreCase(nb.getNodeName())) {
                           NamedNodeMap nab = nb.getAttributes();
                           int instanceId = Integer.parseInt(nab.getNamedItem("id").getNodeValue());
                           if (this._managerInstances.containsKey(instanceId)) {
                              throw new Exception("Dublicated instanceId " + instanceId);
                           }

                           ItemAuctionInstance instance = new ItemAuctionInstance(instanceId, this._auctionIds, nb);
                           this._managerInstances.put(instanceId, instance);
                        }
                     }
                  }
               }

               _log.log(Level.INFO, "ItemAuctionManager: Loaded " + this._managerInstances.size() + " instance(s).");
            } catch (Exception var57) {
               _log.log(Level.SEVERE, "ItemAuctionManager: Failed loading auctions from xml.", (Throwable)var57);
            }
         }
      }
   }

   public final void shutdown() {
      ItemAuctionInstance[] instances = this._managerInstances.values(new ItemAuctionInstance[0]);

      for(ItemAuctionInstance instance : instances) {
         instance.shutdown();
      }
   }

   public final ItemAuctionInstance getManagerInstance(int instanceId) {
      return this._managerInstances.get(instanceId);
   }

   public final int getNextAuctionId() {
      return this._auctionIds.getAndIncrement();
   }

   public static final void deleteAuction(int auctionId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?");
      ) {
         statement.setInt(1, auctionId);
         statement.execute();

         try (PreparedStatement statementx = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?")) {
            statementx.setInt(1, auctionId);
            statementx.execute();
         }
      } catch (SQLException var57) {
         _log.log(Level.SEVERE, "L2ItemAuctionManagerInstance: Failed deleting auction: " + auctionId, (Throwable)var57);
      }
   }

   private static class SingletonHolder {
      protected static final ItemAuctionManager _instance = new ItemAuctionManager();
   }
}
