package l2e.gameserver.data.parser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import l2e.commons.util.file.filter.NumericNameFilter;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.buylist.Product;
import l2e.gameserver.model.items.buylist.ProductList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class BuyListParser extends DocumentParser {
   private final Map<Integer, ProductList> _buyLists = new HashMap<>();

   protected BuyListParser() {
      this.setCurrentFileFilter(new NumericNameFilter());
      this.load();
   }

   @Override
   public synchronized void load() {
      this._buyLists.clear();
      this.parseDirectory("data/stats/npcs/buylists", false);
      if (Config.CUSTOM_BUYLIST) {
         this.parseDirectory("data/stats/npcs/buylists/custom", false);
      }

      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._buyLists.size() + " buyLists.");

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement statement = con.createStatement();
         ResultSet rs = statement.executeQuery("SELECT * FROM `buylists`");
      ) {
         while(rs.next()) {
            int buyListId = rs.getInt("buylist_id");
            int itemId = rs.getInt("item_id");
            long count = rs.getLong("count");
            long nextRestockTime = rs.getLong("next_restock_time");
            ProductList buyList = this.getBuyList(buyListId);
            if (buyList == null) {
               this._log.warning("BuyList found in database but not loaded from xml! BuyListId: " + buyListId);
            } else {
               Product product = buyList.getProductByItemId(itemId);
               if (product == null) {
                  this._log.warning("ItemId found in database but not loaded from xml! BuyListId: " + buyListId + " ItemId: " + itemId);
               } else if (count < product.getMaxCount()) {
                  product.setCount(count);
                  product.restartRestockTask(nextRestockTime);
               }
            }
         }
      } catch (Exception var66) {
         this._log.log(Level.WARNING, "Failed to load buyList data from database.", (Throwable)var66);
      }
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      try {
         int buyListId = Integer.parseInt(this.getCurrentFile().getName().replaceAll(".xml", ""));

         for(Node node = this.getCurrentDocument().getFirstChild(); node != null; node = node.getNextSibling()) {
            if ("list".equalsIgnoreCase(node.getNodeName())) {
               ProductList buyList = new ProductList(buyListId);

               for(Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling()) {
                  if ("item".equalsIgnoreCase(list_node.getNodeName())) {
                     int itemId = -1;
                     long price = -1L;
                     long restockDelay = -1L;
                     long count = -1L;
                     NamedNodeMap attrs = list_node.getAttributes();
                     Node attr = attrs.getNamedItem("id");
                     itemId = Integer.parseInt(attr.getNodeValue());
                     attr = attrs.getNamedItem("price");
                     if (attr != null) {
                        price = Long.parseLong(attr.getNodeValue());
                     }

                     attr = attrs.getNamedItem("restock_delay");
                     if (attr != null) {
                        restockDelay = Long.parseLong(attr.getNodeValue());
                     }

                     attr = attrs.getNamedItem("count");
                     if (attr != null) {
                        count = Long.parseLong(attr.getNodeValue());
                     }

                     Item item = ItemsParser.getInstance().getTemplate(itemId);
                     if (item != null) {
                        buyList.addProduct(new Product(buyList.getListId(), item, price, restockDelay, count));
                     } else {
                        this._log.warning("Item not found. BuyList:" + buyList.getListId() + " ItemID:" + itemId + " File:" + this.getCurrentFile().getName());
                     }
                  } else if ("npcs".equalsIgnoreCase(list_node.getNodeName())) {
                     for(Node npcs_node = list_node.getFirstChild(); npcs_node != null; npcs_node = npcs_node.getNextSibling()) {
                        if ("npc".equalsIgnoreCase(npcs_node.getNodeName())) {
                           int npcId = Integer.parseInt(npcs_node.getTextContent());
                           buyList.addAllowedNpc(npcId);
                        }
                     }
                  }
               }

               this._buyLists.put(buyList.getListId(), buyList);
            }
         }
      } catch (Exception var15) {
         this._log.log(Level.WARNING, "Failed to load buyList data from xml File:" + this.getCurrentFile().getName(), (Throwable)var15);
      }
   }

   public ProductList getBuyList(int listId) {
      return this._buyLists.get(listId);
   }

   public static BuyListParser getInstance() {
      return BuyListParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final BuyListParser _instance = new BuyListParser();
   }
}
